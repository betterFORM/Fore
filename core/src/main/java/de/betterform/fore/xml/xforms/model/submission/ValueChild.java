/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

/**
 * 
 */
package de.betterform.fore.xml.xforms.model.submission;

import de.betterform.fore.xml.xforms.XFormsElement;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.Model;
import de.betterform.fore.xml.xpath.impl.saxon.XPathCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * @author nvdbleek
 *
 */
public class ValueChild extends XFormsElement {
	private static Log LOGGER = LogFactory.getLog(ValueChild.class);
	
	private String staticValue;
	private String valueExpr;
	private int position = 1;
	
	public ValueChild(Element element, Model model) {
		super(element, model);
	}


	@Override
	public void init() throws XFormsException {
		valueExpr = getXFormsAttribute(VALUE_ATTRIBUTE);
		
		if (valueExpr == null) {
			staticValue = this.element.getTextContent();
		}
	}
	
	@Override
	public void dispose() throws XFormsException {
	}
	
	
	public void setPosition(int position) {
		this.position = position;
	}


	public String getValue() throws XFormsException {
		if (valueExpr != null) {
			return XPathCache.getInstance().evaluateAsString(evalInScopeContext(), this.position,"string(" + valueExpr + ")",prefixMapping,xpathFunctionContext);
		}
		return staticValue;
	}

	@Override
	protected Log getLogger() {
		return LOGGER;
	}

	
}
