/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.ui;

import de.betterform.fore.xml.events.DOMEventNames;
import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;

public class RepeatEmptyTest extends BetterFormTestCase {


    public void testEmptyRepeat() throws Exception {
            assertNotNull(this.processor.getXForms());
            this.processor.dispatch("insert", DOMEventNames.ACTIVATE);
            // DOMUtil.prettyPrintDOM(this.processor.getXForms());
        }

    protected String getTestCaseURI() {
        return "RepeatEmptyTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;  
    }
}
