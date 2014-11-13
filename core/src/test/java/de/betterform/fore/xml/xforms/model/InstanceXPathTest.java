/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
package de.betterform.fore.xml.xforms.model;


import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;
import junit.framework.TestCase;

import java.io.InputStream;

// end of class

/**
 * Test cases for the instance implementation.
 *
 * @author Joern Turner
 * @version $Id: InstanceTest.java 3264 2008-07-16 12:26:54Z joern $
 */
public class InstanceXPathTest extends TestCase {

    private XFormsProcessorImpl xformsProcesssorImpl;

    /**
     * Sets up the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void setUp() throws Exception {
    }

    /**
     * Tears down the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void tearDown() throws Exception {
    }


    protected InputStream getTestCaseDocumentAsStream() {
        return getClass().getResourceAsStream("XF11_Chapter2.xhtml");
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

    public void testXPathPrefixResolution() throws Exception{
        String path = getClass().getResource("XF11_Chapter2.xhtml").getPath();
        this.xformsProcesssorImpl = new XFormsProcessorImpl();
        this.xformsProcesssorImpl.setBaseURI("file://" + path);
        this.xformsProcesssorImpl.setXForms(getTestCaseDocumentAsStream());
        this.xformsProcesssorImpl.init();

        // DOMUtil.prettyPrintDOM(this.xformsProcesssorImpl.getContainer().getDefaultModel().getDefaultInstance().getInstanceDocument());
    }

}
