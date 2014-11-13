/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.ui;

import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.Model;
import de.betterform.fore.xml.xforms.ui.state.HelperElementState;
import de.betterform.fore.xml.xpath.impl.saxon.XPathCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * Implementation of <b>8.2.3 The value Element</b>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: Value.java 3253 2008-07-08 09:26:40Z lasse $
 */
public class Value extends BindingElement {
    private static final Log LOGGER = LogFactory.getLog(Value.class);

    /**
     * Creates a new value element handler.
     *
     * @param element the host document element.
     * @param model the context model.
     */
    public Value(Element element, Model model) {
        super(element, model);
    }

    // lifecycle methods

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace(this + " init");
        }
        

        initializeDefaultAction();
        updateXPathContext();
        if(hasBindingExpression()){
            initializeElementState();
        }else if(getXFormsAttribute(VALUE_ATTRIBUTE) != null){
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(this + " xf:value has a value attribute");
            }
            evalValueAttribute();
        }

    }


    /**
     * Performs element update.
     *
     * @throws XFormsException if any error occurred during update.
     */
    public void refresh() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " update");
        }
        updateXPathContext();
        if(hasBindingExpression()){
            updateElementState();
        }else if(getXFormsAttribute("value") != null){
            evalValueAttribute();
        }

    }

    /**
     * Performs element disposal.
     *
     * @throws XFormsException if any error occurred during disposal.
     */
    public void dispose() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " dispose");
        }

        disposeDefaultAction();
        disposeElementState();
        disposeSelf();
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
        return hasBindingExpression() ? new HelperElementState() : null;
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Log getLogger() {
        return LOGGER;
    }

    /**
     * Evaluates value attribute on value element and writes the result as text child to the node
     * @throws XFormsException
     */
    private void evalValueAttribute() throws XFormsException {
        String valueExpr = getXFormsAttribute(VALUE_ATTRIBUTE);
        String itemValue = XPathCache.getInstance().evaluateAsString(this.getNodeset(), getPosition(), valueExpr , getPrefixMapping(), xpathFunctionContext);
        DOMUtil.setElementValue(this.element,itemValue);
    }
}

// end of class
