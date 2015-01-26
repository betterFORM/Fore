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
import org.exist.source.FileSource;
import org.exist.util.LockException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


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
    private CachingTransformerService transformerService;
    private String referer;
    private boolean cached = false;
    private URI stylesheetPath;

    public ExistModelGenerator(ExistBroker broker) {
        this.broker = broker;
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
    public Node fetchModel(String referredDocument,
                           CachingTransformerService cachingTransformerService,
                           String data,
                           String reqUri)
            throws PermissionDeniedException, IOException, EXistException, TransformerException, XFormsConfigException, LockException, SAXException, URISyntaxException, ParserConfigurationException {
        this.transformerService = cachingTransformerService;

        // ???????????????????????? resolve and get file from exist ??????????????????????????
        this.referer = referredDocument;
        org.w3c.dom.Document formDoc = broker.getDocument(this.referer);

        DOMUtil.prettyPrintDOM(formDoc);
        LOG.debug("Data: " + data);
//        org.w3c.dom.Document newDoc = DOMUtil.newDocument(true,false);
//        DOMUtil.importNode(newDoc,formDoc);
//        DOMUtil.prettyPrintDOM(newDoc);

        String foreModelPath = referredDocument.substring(0,referredDocument.indexOf(".")) + ".xml";
        org.w3c.dom.Document foreModel = broker.getDocument(foreModelPath);

        if (foreModel == null) {
            DOMResult domResult = generateModel(reqUri, data, formDoc);
            DOMUtil.prettyPrintDOM(domResult.getNode());

            //store it
            broker.storeDocument((org.w3c.dom.Document) domResult.getNode(),foreModelPath);
            return domResult.getNode();
        } else {
            cached = true;
            return updateModel(data, foreModel).getNode();
        }
    }

    /**
     * If model has already been generated this returns true
     *
     * @return true if the model already exists. False otherwise.
     */
    public boolean isCached() {
        return cached;
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
    public ByteArrayOutputStream mixinErrors(String errorXML) throws XFormsConfigException, TransformerException, IOException, EXistException, PermissionDeniedException, ParserConfigurationException, SAXException {
        String styles = Config.getInstance().getProperty("error-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        org.w3c.dom.Document htmlDoc = broker.getDocument(this.referer);
        DOMUtil.prettyPrintDOM(htmlDoc);

        transformer.setParameter("errors", new StreamSource(new StringReader(errorXML)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        StreamResult result = new StreamResult(outputStream);
//        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(htmlDoc), result);
//        DOMUtil.prettyPrintDOM(result.getNode());
        return outputStream;
    }

    public ByteArrayOutputStream mixinXMLErrors(org.w3c.dom.Document errors) throws XFormsConfigException, TransformerException, IOException, EXistException, PermissionDeniedException, ParserConfigurationException, SAXException {
        String styles = Config.getInstance().getProperty("error-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        org.w3c.dom.Document htmlDoc = broker.getDocument(this.referer);
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
     *
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

    private DOMResult generateModel(String reqUri, String data, org.w3c.dom.Document domDoc) throws XFormsConfigException, TransformerException {
        //generate XForms Model for incoming HTML via XSLT
/*
        String styles = Config.getInstance().getProperty("preprocessor-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        transformer.setParameter("data", data);
        transformer.setParameter("submission", reqUri);
        DOMSource source = new DOMSource(domDoc);
        DOMResult domResult = new DOMResult();

        transformer.transform(source, domResult);
        return domResult;
*/


//        String styles = Config.getInstance().getProperty("preprocessor-transform");
//        File stylesheetFile = new File(this.stylesheetPath);
//        if(! stylesheetFile.exists()){
//            throw new XFormsConfigException("stylesheet not found: " + stylesheetPath);
//        }
//        Transformer transformer = TransformerFactory.newInstance().newTransformer(new javax.xml.transform.stream.StreamSource(stylesheetFile));


        File stylesheetFile = new File (this.stylesheetPath);
        if(! stylesheetFile.exists()){
            throw new XFormsConfigException("stylesheet not found: " + stylesheetPath);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer(new javax.xml.transform.stream.StreamSource(stylesheetFile));
        transformer.setParameter("data", data);
        transformer.setParameter("submission", reqUri);


//        String styles = Config.getInstance().getProperty("preprocessor-transform");

//        Transformer transformer = this.transformerService.getTransformerByName(styles);

        DOMSource in = new DOMSource(domDoc);
        DOMResult result = new DOMResult();
        transformer.transform( in ,result);
        return result;
    }



    private DOMResult updateModel(String data, org.w3c.dom.Document domDoc) throws XFormsConfigException, TransformerException {
        //generate XForms Model for incoming HTML via XSLT
        String styles = Config.getInstance().getProperty("update-transform");
        Transformer transformer = this.transformerService.getTransformerByName(styles);
        transformer.setParameter("data", data);
        DOMResult domResult = new DOMResult();
        transformer.transform(new DOMSource(domDoc), domResult);
        DOMUtil.prettyPrintDOM(domResult.getNode());
        return domResult;
    }


    public void setStylesheetPath(URI stylesheetPath) {
        this.stylesheetPath = stylesheetPath;
    }
}
