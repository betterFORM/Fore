package de.betterform.fore.agent.web;

import de.betterform.fore.thirdparty.DOMBuilder;
import de.betterform.fore.xml.config.Config;
import de.betterform.fore.xml.config.XFormsConfigException;
import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.xslt.TransformerService;
import de.betterform.fore.xml.xslt.impl.CachingTransformerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exist.EXistException;
import org.exist.security.PermissionDeniedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;


/**
 * takes HTML5 input, parses it and turns it into an XML representation that will be transformed to
 * XForms syntax.
 *
 * @author Joern Turner
 */
public class ExistModelGenerator {
    private static final Log LOG = LogFactory.getLog(ExistModelGenerator.class);
    private final ExistBroker broker;
    private String htmlFilePath;
    private HttpServletRequest request;
    private CachingTransformerService transformerService;
    private WebFactory webFactory;
    private String referer;
    private boolean cached=false;

    public ExistModelGenerator(ExistBroker broker){
        this.broker=broker;
    }

    public String getHtmlFilePath() {
        return htmlFilePath;
    }

    /**
     * resolve the pathes for input HTML and generated model and generates XForms model (if needed).
     *
     * @return the XForms model for the HTML input
     * @throws java.net.URISyntaxException
     * @throws javax.xml.transform.TransformerException
     * @throws de.betterform.fore.xml.config.XFormsConfigException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    public Node generateModel(String referredDocument,CachingTransformerService cachingTransformerService)
            throws PermissionDeniedException, IOException, EXistException, TransformerException, XFormsConfigException {
        this.request = request;
        this.transformerService = cachingTransformerService;
        this.webFactory = webFactory;

        // ???????????????????????? resolve and get file from exist ??????????????????????????
//        htmlFilePath = getAbsoluteHTMLFilePath(referer);

        org.w3c.dom.Document foreModel = broker.getDocument(referredDocument);
        DOMUtil.prettyPrintDOM(foreModel);

        String data = getRequestParams();
        LOG.debug("Data: " + data);

        if(foreModel == null){
            //Parse original HTML and sanitize to XHTML
//            org.w3c.dom.Document domDoc = getSanitizedHtml();
            String reqUri = request.getRequestURI();
            DOMResult domResult = generateModel(reqUri, data, foreModel);
            DOMUtil.prettyPrintDOM(domResult.getNode());

            //store it
            broker.storeDocument((org.w3c.dom.Document) domResult.getNode());
            return domResult.getNode();
        }else{
            cached=true;
            return updateModel(data,foreModel).getNode();
        }
    }

    /**
     * If model has already been generated this returns true
     * @return true if the model already exists. False otherwise.
     */
    public boolean isCached() {
        return cached;
    }

    /**
     * parse HTML5 and return wellformed (XHTML) HTML for it.
     * @return returns a W3C document for incoming (potentially incomplete and non-wellformed) HTML.
     * @throws java.io.IOException
     */
    private org.w3c.dom.Document getSanitizedHtml() throws IOException {
        URL uri = new URL(referer);
        Document doc = Jsoup.parse(uri, 1000);
        return DOMBuilder.jsoup2DOM(doc);
    }

    /**
     * takes errorinfo XML as string, transform it and return the full HTML file with error-information as a stream.
     * Errors will be output as a set of CSS classes on the respective control.
     *
     * @param errorXML the error information (serialized from ErrorInfo objects) as a XML string
     * @return complete HTML document with error information as CSS classes
     * @throws de.betterform.fore.xml.config.XFormsConfigException
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.IOException
     */
    public ByteArrayOutputStream mixinErrors(String errorXML) throws XFormsConfigException, TransformerException, IOException {
        String styles = Config.getInstance().getProperty("error-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        org.w3c.dom.Document htmlDoc = getSanitizedHtml();
        DOMUtil.prettyPrintDOM(htmlDoc);

        transformer.setParameter("errors", new StreamSource(new StringReader(errorXML)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        StreamResult result = new StreamResult(outputStream);
//        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(htmlDoc), result);
//        DOMUtil.prettyPrintDOM(result.getNode());
        return outputStream;
    }

    public ByteArrayOutputStream mixinXMLErrors(org.w3c.dom.Document errors) throws XFormsConfigException, TransformerException, IOException {
        String styles = Config.getInstance().getProperty("error-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        org.w3c.dom.Document htmlDoc = getSanitizedHtml();
        DOMUtil.prettyPrintDOM(htmlDoc);

        transformer.setParameter("errors", new DOMSource(errors.getDocumentElement()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        StreamResult result = new StreamResult(outputStream);
//        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(htmlDoc), result);
//        DOMUtil.prettyPrintDOM(result.getNode());
        return outputStream;
    }

    /**
     * Parses a string containing HTML5 and transforms it into a semantically equivalent XForms model document
     * @param html5
     * @param cachingTransformerService
     * @return
     */
    public static Node html2Xforms(String html5, CachingTransformerService cachingTransformerService) {
        Document doc = Jsoup.parse(html5);
        org.w3c.dom.Document domDoc = DOMBuilder.jsoup2DOM(doc);
        try {
            String styles = Config.getInstance().getProperty("preprocessor-transform");
            Transformer transformer = cachingTransformerService.getTransformerByName(styles);
            DOMResult domResult = new DOMResult();
            transformer.transform(new DOMSource(domDoc), domResult);
            return domResult.getNode();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (XFormsConfigException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getRequestParams() {
        Enumeration<String> params = this.request.getParameterNames();
        StringBuffer formData = new StringBuffer();
        while(params.hasMoreElements()){
            String name = params.nextElement();
            String value = request.getParameter(name);
            formData.append(name);
            formData.append(":");
            if(value != null){
                formData.append(value);
            }else{
                formData.append("");
            }
            formData.append(";");
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("data send by form: " + formData.toString());
        }
        return formData.toString();
    }

    private File getModelFile() {
        File htmlFile = new File(htmlFilePath);
        File parentFile = htmlFile.getParentFile();
        String fileName = htmlFile.getName();
        String baseName = fileName.substring(0,fileName.indexOf("."));
        String xfmFileName = baseName + ".xfm";
        return new File(parentFile,xfmFileName);
    }

/*
    private String getAbsoluteHTMLFilePath(String referer) {
        String contextname = request.getContextPath();
        int pos = referer.indexOf(contextname);
        String relPath = referer.substring(pos+contextname.length());
        String absPath=null;
        try {
            absPath = WebFactory.getRealPath(relPath,webFactory.getServletContext());
        } catch (XFormsConfigException e) {
            e.printStackTrace();
        }
        return absPath;
    }
*/

    private DOMResult generateModel(String reqUri, String data, org.w3c.dom.Document domDoc) throws XFormsConfigException, TransformerException {
        //generate XForms Model for incoming HTML via XSLT
        String styles = Config.getInstance().getProperty("preprocessor-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        transformer.setParameter("data", data);
        transformer.setParameter("submission",reqUri);
        DOMResult domResult = new DOMResult();
        transformer.transform(new DOMSource(domDoc), domResult);
        return domResult;
    }

    private DOMResult updateModel(String data, org.w3c.dom.Document domDoc) throws XFormsConfigException, TransformerException {
        //generate XForms Model for incoming HTML via XSLT
        String styles = Config.getInstance().getProperty("update-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        transformer.setParameter("data", data);
        DOMResult domResult = new DOMResult();
        transformer.transform(new DOMSource(domDoc), domResult);
        return domResult;
    }


}
