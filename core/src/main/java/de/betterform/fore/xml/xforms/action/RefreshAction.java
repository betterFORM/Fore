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
 * Implements the action as defined in <code>10.1.6 The refresh Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: RefreshAction.java 3253 2008-07-08 09:26:40Z lasse $
 */
public class RefreshAction extends AbstractAction {
    private static final Log LOGGER = LogFactory.getLog(RefreshAction.class);

    /**
     * Creates a refresh action implementation.
     *
     * @param element the element.
     * @param model the context model.
     */
    public RefreshAction(Element element, Model model) {
        super(element, model);
    }

    // implementation of 'de.betterform.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>refresh</code> action.
     *
     * @throws XFormsException if an error occurred during <code>refresh</code>
     * processing.
     */
    public void perform() throws XFormsException {
        // refresh model
        this.model.refresh();

        // update behavior
        doRefresh(false);
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Log getLogger() {
        return LOGGER;
    }
}

// end of class
