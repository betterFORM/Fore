/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.events;

import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.action.EventCountListener;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;
import de.betterform.fore.xml.xpath.impl.saxon.XPathUtil;
import org.w3c.dom.Document;
import org.w3c.dom.events.EventTarget;


/**

 *
 * @author Joern Turner
 * @author Lars Windauer
 *

 */
public class XFormsComplexSelectTest extends BetterFormTestCase {
    static {
        org.apache.log4j.BasicConfigurator.configure();
    }
    
    private EventCountListener valueChangeCountListener;

    protected String getTestCaseURI() {
        return "XFormsComplexSelectTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

    /**
     * Sets up the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.valueChangeCountListener = new EventCountListener(XFormsEventNames.VALUE_CHANGED);
        EventTarget eventTarget = (EventTarget) this.processor.getXForms();
        eventTarget.addEventListener(XFormsEventNames.VALUE_CHANGED, this.valueChangeCountListener, true);


    }

    /**
     * Tears down the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void tearDown() throws Exception {
        EventTarget eventTarget = (EventTarget) this.processor.getXForms();
        eventTarget.removeEventListener(XFormsEventNames.VALUE_CHANGED, this.valueChangeCountListener, true);
        valueChangeCountListener = null;        
    }


    /**
     * Tests a modal message.
     *
     * @throws Exception if any error occurred during the test.
     */
    public void testSelect1ValueChangeListener() throws Exception {
        this.processor.dispatch("t-changeValue", DOMEventNames.ACTIVATE);
        XFormsProcessorImpl bean = (XFormsProcessorImpl) this.processor;
        Document instance = bean.getContainer().getDefaultModel().getDefaultInstance().getInstanceDocument();
         DOMUtil.prettyPrintDOM(instance);
        assertEquals(2, this.valueChangeCountListener.getCount());

        assertEquals("true",XPathUtil.evaluateAsString(instance,"//result/select1"));
        assertEquals("true",XPathUtil.evaluateAsString(instance,"//result/select2"));



    }


}
