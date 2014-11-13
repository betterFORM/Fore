/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.ui;

import de.betterform.fore.xml.config.Config;
import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.events.DefaultAction;
import de.betterform.fore.xml.events.XFormsEventNames;
import de.betterform.fore.xml.ns.NamespaceConstants;
import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.action.UpdateHandler;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.Model;
import de.betterform.fore.xml.xforms.model.ModelItem;
import de.betterform.fore.xml.xforms.ui.state.BoundElementState;
import de.betterform.fore.xml.xforms.ui.state.UIElementStateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;

import java.math.BigDecimal;
import java.text.*;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Locale;

/**
 * Base class for all form controls.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: AbstractFormControl.java 3506 2008-08-29 15:51:38Z lars $
 */
public abstract class AbstractFormControl extends BindingElement implements DefaultAction {
       private static Log LOGGER = LogFactory.getLog(AbstractFormControl.class);

    /**
     * Creates a new abstract form control.
     *
     * @param element the host document element.
     * @param model the context model.
     */
    public AbstractFormControl(Element element, Model model) {
        super(element, model);
    }

    // todo: extract interface
    // form control methods

    /**
     * Sets the value of this form control.
     * <p/>
     * The bound instance data is updated and the event sequence for this
     * control is executed. Event sequences are described in Chapter 4.6 of
     * XForms 1.0 Recommendation.
     *
     * @param value the value to be set.
     */
    public abstract void setValue(String value) throws XFormsException;

    /**
     * Returns the current value of this form control.
     *
     * @return the current value of this form control.
     */
    public Object getValue()
    {
        if(this.elementState != null)
        {
        		return this.elementState.getValue();
        }

        return null;
    }

    public Object getSchemaValue(){
        if(this.elementState != null)
        {
        		return this.elementState.getSchemaValue();
        }

        return null;

    }

    /**
     * Returns the datatype of the bound node.
     *
     * @return the datatype of the bound node.
     */
    public String getDatatype() throws XFormsException {
        if (hasBindingExpression()) {
            Node n = getInstanceNode();
            if(n == null){
                getLogger().warn("Node for path '" + getBindingExpression() + "' does not exist:" + DOMUtil.getCanonicalPath(this.element));
                //TODO: clarify what to do
                //throw new XFormsException("Node for path '" + getBindingExpression() + "' does not exist::" + DOMUtil.getCanonicalPath(this.element));
            }
            ModelItem modelItem = this.model.getInstance(getInstanceId()).getModelItem(n);
            if (modelItem != null) {
                return UIElementStateUtil.getDatatype(modelItem, this.element);
            }else{
                return "string";
            }
        }

        return null;
    }

    // lifecycle methods

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        super.init();
        initializeActions();
    }

    // lifecycle template methods

    /**
     * Initializes the default action.
     */
    protected void initializeDefaultAction() {
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.BINDING_EXCEPTION, this);
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.PREVIOUS, this);
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.NEXT, this);
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.FOCUS, this);
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.HELP, this);
        this.container.getXMLEventService().registerDefaultAction(this.target, XFormsEventNames.HINT, this);
    }

    /**
     * Disposes the default action.
     */
    protected void disposeDefaultAction() {
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.BINDING_EXCEPTION, this);
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.PREVIOUS, this);
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.NEXT, this);
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.FOCUS, this);
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.HELP, this);
        this.container.getXMLEventService().deregisterDefaultAction(this.target, XFormsEventNames.HINT, this);
    }

    // template methods

    /**
     * Factory method for the element state.
     *
     * @return an element state implementation or <code>null</code> if no state
     *         keeping is required.
     * @throws XFormsException if an error occurred during creation.
     */
    protected UIElementState createElementState() throws XFormsException {
        return hasBindingExpression() ? new BoundElementState() : null;
    }

    /**
     * Dispatches the '4.6.7 Value Change with Focus Change' event sequence.
     *
     * @throws XFormsException if an error occurred during event sequencing.
     */
    protected void dispatchValueChangeSequence() throws XFormsException {
        // prevent betterform internal value change dispatching on this control
        if (this.elementState != null) {
            this.elementState.setProperty("dispatchValueChange", Boolean.FALSE);
        }

        // update behaviour
        UpdateHandler updateHandler = this.model.getUpdateHandler();
        if (updateHandler == null) {
            this.container.dispatch(this.model.getTarget(), XFormsEventNames.RECALCULATE, null);
            this.container.dispatch(this.model.getTarget(), XFormsEventNames.REVALIDATE, null);
            this.container.dispatch(this.model.getTarget(), XFormsEventNames.REFRESH, null);
        }
        else {
            updateHandler.doRecalculate(true);
            updateHandler.doRevalidate(true);
            updateHandler.doRefresh(true);
        }

        // reset betterform internal value change dispatching on this control
        if (this.elementState != null) {
            this.elementState.setProperty("dispatchValueChange", Boolean.TRUE);
        }
    }

    // implementation of 'de.betterform.xml.events.DefaultAction'

    /**
     * Performs the implementation specific default action for this event.
     *
     * @param event the event.
     */
    public void performDefault(Event event){
        super.performDefault(event);
        if (isCancelled(event)) {
            getLogger().debug(this + " event " + event.getType() + " cancelled");
            return;
        }
        if (event.getType().equals(XFormsEventNames.PREVIOUS)) {
            // todo
            getLogger().warn(this + " default action for " + event.getType() + " is not implemented yet");
            return;
        }
        if (event.getType().equals(XFormsEventNames.NEXT)) {
            // todo
            getLogger().warn(this + " default action for " + event.getType() + " is not implemented yet");
            return;
        }
        if (event.getType().equals(XFormsEventNames.FOCUS)) {
/*
            //giving focus if we're relevant
            try{
                if(UIElementStateUtil.getModelItem(this) != null && UIElementStateUtil.getModelItem(this).isRelevant()){
                    String currentFocussedControl = this.container.getFocussedControlId();
                    if (currentFocussedControl != null){
                        this.container.dispatch(this.container.getFocussedControlId() , DOMEventNames.FOCUS_OUT, null);
                    }
                    this.container.setFocussedControlId(this.id);
                    this.container.dispatch(this.target , DOMEventNames.FOCUS_IN, null);
                }
            }catch (XFormsException e){
                getLogger().warn("Exeption occured during dispatch of FOCUS_IN event");
            }
*/
            return;
        }
        if (event.getType().equals(XFormsEventNames.HELP)) {
            // todo
            getLogger().warn(this + " default action for " + event.getType() + " is not implemented yet");
            return;
        }
        if (event.getType().equals(XFormsEventNames.HINT)) {
            // todo
            getLogger().warn(this + " default action for " + event.getType() + " is not implemented yet");
            return;
        }
    }

    /**
     * convert a localized value into its XML Schema datatype representation. If the value given cannot be parsed with
     * the locale in betterForm context the default locale (US) will be used as fallback. This can be convenient for
     * user-agents that do not pass a localized value back.
     *
     * @param value the value to convert
     * @return converted value that can be used to update instance data and match the Schema datatype lexical space
     * @throws java.text.ParseException in case the incoming string cannot be converted into a Schema datatype representation
     */
    protected String delocaliseValue(String value) throws XFormsException, ParseException {
        if(value == null || value.equals("")){
            return value;
        }
        if (Config.getInstance().getProperty(XFormsProcessorImpl.BETTERFORM_ENABLE_L10N).equals("true")) {
            Locale locale = (Locale) getModel().getContainer().getProcessor().getContext().get(XFormsProcessorImpl.BETTERFORM_LOCALE);
            XFormsProcessorImpl processor = this.model.getContainer().getProcessor();

            if (processor.hasControlType(this.id, NamespaceConstants.XMLSCHEMA_PREFIX + ":float") ||
                processor.hasControlType(this.id, NamespaceConstants.XMLSCHEMA_PREFIX + ":decimal") ||
                processor.hasControlType(this.id, NamespaceConstants.XMLSCHEMA_PREFIX + ":double")) {

                NumberFormat formatter = NumberFormat.getNumberInstance(locale);
                formatter.setMaximumFractionDigits(Double.SIZE);
                BigDecimal number;

                try {
                    number = strictParse(value,locale);
                } catch (ParseException e) {
                    LOGGER.warn("value: '" + value + "' could not be parsed for locale: " + locale);
                    return value;
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("value: '" + value + "' could not be parsed for locale: " + locale);
                    return value;
                } catch (InputMismatchException ime) {
                    LOGGER.warn("value: '" + value + "' could not be parsed for locale: " + locale);
                    return value;
                }
                return number.toPlainString();
            }
            else if (processor.hasControlType(this.id, NamespaceConstants.XMLSCHEMA_PREFIX + ":date")) {
                DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                Date d = null;
                try {
                    d = df.parse(value);
                } catch (ParseException e) {
                    //try the default locale - else fail with ParseException
                    df = new SimpleDateFormat("yyyy-MM-dd");
                    df.setLenient(false);
                    d = df.parse(value);
                }
                df = new SimpleDateFormat("yyyy-MM-dd");
                return df.format(d);
            } else if (processor.hasControlType(this.id, NamespaceConstants.XMLSCHEMA_PREFIX + ":dateTime")) {
                String timezone = "";
                // int position = ;
                if (value.contains("GMT")) {
                    timezone = value.substring(value.indexOf("GMT") + 3, value.length());
                }else if(value.contains("+")) {
                    timezone = value.substring(value.indexOf("+"), value.length());

                }else if(value.contains("Z")){
                    timezone = "Z";
                }

                DateFormat sf = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
                Date d = null;
                try {
                    d = sf.parse(value);
                } catch (ParseException e) {
                    //try the default locale - else fail with ParseException
                    sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    d = null;
                    d = sf.parse(value);
                }
                sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String converted = sf.format(d);
                if (!timezone.equals("")) {
                    return converted + timezone;
                }
                return converted;
            }
        }
        return value;
    }

    private BigDecimal strictParse(String value,Locale locale) throws ParseException {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        format.setParseBigDecimal(true);
        value = value.trim();
        ParsePosition pos = new ParsePosition(0);
        BigDecimal number = (BigDecimal) format.parse(value, pos);
        boolean okay = pos.getIndex() == value.length() && pos.getErrorIndex() == -1;
        if (!okay)
            throw new ParseException("Could not parse '" + value + "' as a number",pos.getErrorIndex());
        return number;
    }

                }

// end of class
