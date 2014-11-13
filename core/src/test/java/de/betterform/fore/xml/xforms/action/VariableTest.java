/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.action;

import de.betterform.fore.xml.events.DOMEventNames;
import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * Test cases for XForms 1.1. 'if' attribute for Conditional Execution of Actions
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: IfTest.java 2797 2007-08-10 12:45:24Z joern $
 */
public class VariableTest extends BetterFormTestCase {
//    static {
//        org.apache.log4j.BasicConfigurator.configure();
//    }


    public void testVariableCreation() throws Exception {
        this.processor.dispatch("trigger-variables", DOMEventNames.ACTIVATE);

        // DOMUtil.prettyPrintDOM(((XFormsProcessorImpl)this.processor).getContainer().getDefaultModel().getDefaultInstance().getInstanceDocument());

        String p1 = (String) this.processor.getContextParam("foo");
        String p2 = (String) this.processor.getContextParam("bar");

        assertEquals(p1, "bar");
        assertEquals(p2, "true");
        

    }


    protected String getTestCaseURI() {
        return "VariableTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

}
