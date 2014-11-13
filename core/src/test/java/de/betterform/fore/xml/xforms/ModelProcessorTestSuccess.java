/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms;

import de.betterform.fore.xml.config.Config;
import de.betterform.fore.xml.xforms.action.EventCountListener;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;
import de.betterform.fore.xml.xpath.impl.saxon.XPathUtil;
import org.w3c.dom.Document;

import java.io.File;


/**

 *
 * @author Joern Turner
 * @author Lars Windauer
 *

 */
public class ModelProcessorTestSuccess extends BetterFormTestCase {

    protected String getTestCaseURI() {
        return "ModelProcessorSuccess.html";
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
        Config.getInstance(Config.class.getResource("default.xml").getPath());
        this.processor = new ModelProcessor();

        File f;
        String testCaseURI = getTestCaseURI();
        if(testCaseURI.startsWith("conform:")){
            testCaseURI = testCaseURI.substring(8);
            String relative2file = new File(relativePath, testCaseURI).toString();
            //todo: set a default config
            f = new File(relative2file);
        }else{
            f = new File(getClass().getResource(testCaseURI).getPath());
        }
        String s = f.getCanonicalPath();
        this.processor.setXForms(getXmlResource(f.toString()));
        processor.setBaseURI(f.getParentFile().toURI().toString());

        this.processor.init();

        this.defaultContext = getDefaultContext();
        this.defaultFunctionContext = getDefaultFunctionContext();
        this.documentContext = XPathUtil.getRootContext((Document) this.processor.getXForms(), this.processor.getBaseURI());
    }
    /**
     * Tears down the test.
     *
     * @throws Exception in any error occurred during setup.
     */
    protected void tearDown() throws Exception {

    }

    /**
     * Tests a modal message.
     *
     * @throws Exception if any error occurred during the test.
     */
    public void testInvalidListener() throws Exception {

        assertTrue(((ModelProcessor) this.processor).isSuccess());
    }


}
