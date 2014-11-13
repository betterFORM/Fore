/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
package de.betterform.fore.xml.xforms.model;


import de.betterform.fore.xml.events.XFormsEventNames;
import de.betterform.fore.xml.xforms.TestEventListener;
import de.betterform.fore.xml.xforms.XFormsProcessorImpl;
import de.betterform.fore.xml.xforms.XMLTestBase;
import de.betterform.fore.xml.xforms.exception.XFormsLinkException;
import org.w3c.dom.events.EventTarget;

/**
 * Test cases for the instance implementation.
 *
 * @author Tobi Krebs
 */
public class InvalidInstanceSrcTest extends XMLTestBase {
    private XFormsProcessorImpl xformsProcesssorImpl;
    private TestEventListener LinkListener;

    /**
     * Tests instance initialization.
     *
     * @throws Exception if any error occurred during the test.
     */
    protected void setUp() throws Exception {
        this.xformsProcesssorImpl = new XFormsProcessorImpl();
        this.xformsProcesssorImpl.setXForms(getClass().getResourceAsStream("InvalidInstanceURITest.xhtml"));
        String path = getClass().getResource("InvalidInstanceURITest.xhtml").getPath();
        String baseURI = "file://" + path.substring(0, path.lastIndexOf("InvalidInstanceURITest.xhtml"));
        this.xformsProcesssorImpl.setBaseURI(baseURI);

        this.LinkListener = new TestEventListener();
        EventTarget eventTarget = (EventTarget) this.xformsProcesssorImpl.getXForms();
        eventTarget.addEventListener(XFormsEventNames.LINK_EXCEPTION, this.LinkListener, true);
    }

    protected void tearDown() throws Exception {
        EventTarget eventTarget = (EventTarget) this.xformsProcesssorImpl.getXForms();
        eventTarget.removeEventListener(XFormsEventNames.INSERT, this.LinkListener, true);
        this.LinkListener=null;
        this.xformsProcesssorImpl.shutdown();
        super.tearDown();
    }

    protected String getTestCaseURI() {
        return "InvalidInstanceURITest.xhtml";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testInvalidInstance() throws Exception {
        try{
            this.xformsProcesssorImpl.init();
            fail();
        }catch (XFormsLinkException e){
            assertNotNull(e);
            assertEquals("doesnotexist.xml", this.LinkListener.getContext("resource-uri"));
        }


    }
}

// end of class
