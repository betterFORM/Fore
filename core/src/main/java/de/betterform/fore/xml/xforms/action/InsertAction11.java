/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.action;

import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.Model;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * Implements the action as defined in <code>9.3.5 The insert Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: InsertAction.java 2629 2007-08-07 15:38:48Z lars $
 */
public class InsertAction11 extends InsertAction {
    private static final Log LOGGER = LogFactory.getLog(InsertAction11.class);

    /**
     * Creates an insert action implementation.
     *
     * @param element the element.
     * @param model   the context model.
     */
    public InsertAction11(Element element, Model model) {
        super(element, model);
    }

    // lifecycle methods

    /**
     * Performs element init.
     */
    public void init() throws XFormsException {
        super.init();
    }


    /**
     * Performs the <code>insert</code> action.
     *
     * @throws de.betterform.fore.xml.xforms.exception.XFormsException
     *          if an error occurred during <code>insert</code>
     *          processing.
     */
    public void perform() throws XFormsException {
        // XXX implement XForms 1.1 insert action
        super.perform();
    }
}


