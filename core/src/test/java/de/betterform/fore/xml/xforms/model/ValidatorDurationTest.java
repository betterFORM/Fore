/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.model;

import de.betterform.fore.xml.xforms.BetterFormTestCase;
import de.betterform.fore.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * Validator Durations test cases.
 *
 * @author ;
 * @version $Id: ValidatorDurationTest.java 3264 2008-12-07 12:26:54Z lars $
 */
public class ValidatorDurationTest extends BetterFormTestCase {
//    static {
//        org.apache.log4j.BasicConfigurator.configure();
//    }

    /**
     * Tests loaded datatypes.
     *
     * @throws Exception if any error occurred during the test.
     */
    public void testDayTimeDuration() throws Exception {
        ModelItem modelItem = getDefaultModel().getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/car/rentalLeaseLength[1]"));
        assertEquals("P5DT3H4M2S", modelItem.getValue());
        assertEquals(true, modelItem.isValid());

    }

    protected String getTestCaseURI() {
        return "5.2.4.a-Duration.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }

}
