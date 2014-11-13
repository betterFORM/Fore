/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.xpath.saxon.function;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.StringValue;

import java.util.GregorianCalendar;

/**
 * Implementation of 7.9.2 The local-dateTime() Function <p/> This function
 * returns a lexical xsd:dateTime obtained as if by the following rules: the
 * result of now() is converted to a local dateTime based on the user agent time
 * zone information. If no time zone information is available, then the result
 * of now() is returned.
 * 
 * @author Nick Van den Bleeken
 * @version $Id$
 */
public class LocalDateTime extends XFormsFunction {

    private static final long serialVersionUID = 7762214764963406284L;

    /**
     * Evaluate in a general context
     */
    public Item evaluateItem(XPathContext xpathContext) throws XPathException {
	GregorianCalendar now = new GregorianCalendar();

	return new StringValue(new DateTimeValue(now, true).getStringValue());
    }
}
