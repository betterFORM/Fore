/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.model.submission;

import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.XFormsElement;
import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * Created by IntelliJ IDEA.
 * @author Lars Windauer
 * @author Joern Turner
 * Date: May 5, 2009

 */
public class SubmissionResourceTest extends BetterFormTestCase {
/*
    static {
        org.apache.log4j.BasicConfigurator.configure();
    }
*/

    public void testSubmissionResource() throws Exception{

        XFormsElement element = ((XFormsProcessorImpl) this.processor).lookup("s-test");
        assertNotNull(element);
        assertEquals("echo:foo",((Submission)element).getResource());
        assertEquals( element.getXFormsAttribute("resource"),"echo:foo");



    }


    /**
     * Sets up the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

    }

    /**
     * Tears down the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected String getTestCaseURI() {
        return "SubmissionResourceTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

}
