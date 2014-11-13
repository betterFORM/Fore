/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.ui;

import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * Created by IntelliJ IDEA.
 * User: joern
 * Date: Oct 8, 2009
 * Time: 9:45:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class BecomingReadonlyTest extends BetterFormTestCase {

    protected void setUp() throws Exception {
         super.setUp();
    }

    public void testInitialReadonly() throws Exception{
        // dump(this.processor.getXForms());
        assertEquals("true", evaluateInDefaultContextAsString("/person-name/readonly"));
    }


    protected String getTestCaseURI() {
        return "8.1.1.b.mod3.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
