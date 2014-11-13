/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.model;

import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.XMLTestBase;
import de.betterform.fore.xml.xforms.exception.XFormsComputeException;

/**
 * betterFORM Project
 * User: Tobi Krebs
 * Date: 15.12.11
 * Time: 14:05
 */
public class InvalidFunctionTest extends XMLTestBase {
    private XFormsProcessorImpl xformsProcesssorImpl;
    
    /**
     * Sets up the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void setUp() throws Exception {
        
        this.xformsProcesssorImpl = new XFormsProcessorImpl();
        this.xformsProcesssorImpl.setXForms(getClass().getResourceAsStream("InvalidFunctionTest.xhtml"));
    }
    
    
    public void testInvalidFunction() {
        Exception exception = null;
        try {
            this.xformsProcesssorImpl.init();
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof XFormsComputeException);
    }

    /**
     * Tears down the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void tearDown() throws Exception {
        this.xformsProcesssorImpl.shutdown();
        this.xformsProcesssorImpl = null;
    }
}
