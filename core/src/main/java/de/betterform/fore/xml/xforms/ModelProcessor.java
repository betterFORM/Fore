package de.betterform.fore.xml.xforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.betterform.fore.xml.config.Config;
import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.events.XFormsEventNames;
import de.betterform.fore.xml.events.XMLEvent;
import de.betterform.fore.xml.xforms.exception.XFormsBindingException;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.exception.XFormsInternalSubmitException;
import de.betterform.fore.xml.xforms.model.ModelItem;
import de.betterform.fore.xml.xforms.model.submission.Submission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * processes standalong XForms models.
 */
public class ModelProcessor extends AbstractProcessorDecorator {
    private static final Log LOG = LogFactory.getLog(ModelProcessor.class);
    private final Errors errors;

    private boolean isSuccess=true;
    private List<XMLEvent> events;
    private InputStream responseStream=null;
    private Submission defaultSubmission;
    private Exception exception;

    public ModelProcessor() {
        super();
        this.errors = new Errors() ;
        this.events = new ArrayList();
    }

    public List<ErrorInfo> getErrors(){
        return this.errors.getErrorInfo();
    }

    public Submission getDefaultSubmission() {
        return defaultSubmission;
    }

    @Override
    public void init() throws XFormsException {
        this.configuration = Config.getInstance();
        addEventListeners();

        // init processor
        this.xformsProcessor.init();

    }

    @Override
    public void handleEvent(Event event) {
        Object result;
        try {
            if (event instanceof XMLEvent) {
                XMLEvent xmlEvent = (XMLEvent) event;
                String type = xmlEvent.getType();

                if(XFormsEventNames.MODEL_CONSTRUCT_DONE.equalsIgnoreCase(type)){

                    Iterator iterator = this.xformsProcessor.getContainer().getDefaultModel().getDefaultInstance().iterateModelItems();
                    while(iterator.hasNext()){
                        boolean invalid=false;
                        ModelItem modelItem = (ModelItem) iterator.next();
                        String value = modelItem.getValue();

                        if(!modelItem.getLocalUpdateView().isDatatypeValid()){
                            ErrorInfo errorInfo = new ErrorInfo();
                            errorInfo.setErrorType(ErrorInfo.DATATYPE_INVALID);
                            String datatype = modelItem.getDeclarationView().getDatatype();
                            errorInfo.setDataType(datatype);
                            errorInfo.setRef(((Node) modelItem.getNode()).getLocalName());
                            errorInfo.setPath(modelItem.toString());
                            errorInfo.setValue(value);
                            this.errors.add(errorInfo);
                        }
                        if(modelItem.getRefreshView().isInvalidMarked()){
                            ErrorInfo errorInfo = new ErrorInfo();
                            errorInfo.setErrorType(ErrorInfo.CONSTRAINT_INVALID);
                            errorInfo.setRef(((Node) modelItem.getNode()).getLocalName());
                            errorInfo.setPath(modelItem.toString());
                            errorInfo.setValue(value);
                            this.errors.add(errorInfo);
                        }
                        if(modelItem.getRefreshView().isRequiredMarked()){
                            if(modelItem.getValue().length()==0){
                                ErrorInfo errorInfo = new ErrorInfo();
                                errorInfo.setErrorType(ErrorInfo.REQUIRED_INVALID);
                                errorInfo.setRef(((Node)modelItem.getNode()).getLocalName());
                                errorInfo.setPath(modelItem.toString());
                                errorInfo.setValue(value);
                                this.errors.add(errorInfo);
                            }
                        }
                    }
                }else if(XFormsEventNames.SUBMIT_ERROR.equalsIgnoreCase(type)){
                    //todo : this must be propagated up -> changed into error to display to the user if the backend script fails
                    LOG.debug("XForms submit error");
                    isSuccess=false;
                    this.exception = new XFormsException((String) xmlEvent.getContextInfo("response-reason-phrase"));
                }else if(XFormsEventNames.SUBMIT_DONE.equalsIgnoreCase(type)){
                    LOG.debug("XForms submit done");
                    this.responseStream = (InputStream) xmlEvent.getContextInfo(XFormsProcessor.SUBMISSION_RESPONSE_STREAM);
                }
                this.events.add(xmlEvent);
            }
        } catch (Exception e) {
            this.exception = e;
            handleEventException(e);
        }
    }

    public Exception getException(){
        return this.exception;
    }

    /*
    <errors>
        <error-info ref="street" facet="required|type|constraint">
            <alert></alert>
        </error-info>
    </errors>
    */
    public String serialize() throws JsonProcessingException {

        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(this.errors);

        if(LOG.isDebugEnabled()){
            LOG.debug("errors as xml string: " + xml);
        }
        //create document + root
/*
        Document serialized = DOMUtil.newDocument(false,false);
        Element root = serialized.createElement("errors");
        serialized.appendChild(root);

        for (ErrorInfo error : this.errors) {
        }
*/
        return xml;

    }
    public Document serializeAsDOM() throws IOException, SAXException, ParserConfigurationException {
        String result = serialize();
        return DOMUtil.parseString(result,false,false);
    }

    /**
     * submits the HTML form via XForms
     * @throws XFormsException
     */
    public InputStream submit() throws XFormsException {
        // todo:  hard-coded id for now
        String id = "s-default";

        // find submission matching the resource string
        Container container = getXformsProcessor().getContainer();

        Object submissionObject = getXformsProcessor().getContainer().lookup(id);
        if (submissionObject == null || !(submissionObject instanceof Submission)) {
            try {
                throw new XFormsBindingException("invalid submission id " + id,((Submission) submissionObject).getTarget(),null);
            } catch (XFormsBindingException e) {
                LOG.error("a binding exception occurred which shouldn't have happend: " + e.getMessage());
            }
        }
        this.defaultSubmission = (Submission) submissionObject;

        // dispatch xforms-submit to submission
        container.dispatch(((Submission) submissionObject).getTarget(), XFormsEventNames.SUBMIT, null);
        return this.responseStream;
    }

    /**
     * triggers standard submission.
     *
     * @return if form was submitted successfully returns true, false otherwise
     * @throws XFormsException
     */
    public boolean isSuccess() throws XFormsException {
        return this.errors.getErrorInfo().size()==0 && isSuccess;
    }

    @JacksonXmlRootElement(localName = "errors")
    public class Errors{
        private List<ErrorInfo> errorInfo=new ArrayList();

        void add(ErrorInfo info){
            this.errorInfo.add(info);
        }
        @JacksonXmlElementWrapper(useWrapping = false)
        public List getErrorInfo(){
            return errorInfo;
        }
    }

    class ErrorInfo{
        public static final String DATATYPE_INVALID="datatype-failed";
        public static final String CONSTRAINT_INVALID="constraint-failed";
        public static final String REQUIRED_INVALID="required-failed";

        private String ref="";
        private String dataType="";
        private String errorType;
        private String path;
        private String alert;
        private String value;

        ErrorInfo(){
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getDatatype() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAlert() {
            return alert;
        }

        public void setAlert(String alert) {
            this.alert = alert;
        }

        public String getValue(){ return value;}

        public void setValue(String value){ this.value = value; }
    }
}
