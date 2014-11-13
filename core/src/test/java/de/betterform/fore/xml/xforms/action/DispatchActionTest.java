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
public class DispatchActionTest extends BetterFormTestCase {
//    static {
//        org.apache.log4j.BasicConfigurator.configure();
//    }


    public void testContextInfo() throws Exception {
        this.processor.dispatch("triggerContextInfo", DOMEventNames.ACTIVATE);

        assertEquals("hello", evaluateInDefaultContextAsString("string(/data/staticResult[1])"));
        assertEquals("fromInstance", evaluateInDefaultContextAsString("string(/data/xpathResult[1])"));
    }


    protected String getTestCaseURI() {
        return "dispatchActionTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

}
