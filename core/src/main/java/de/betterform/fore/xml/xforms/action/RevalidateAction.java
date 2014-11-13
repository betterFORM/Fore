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
 * Implements the action as defined in <code>10.1.5 The revalidate
 * Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: RevalidateAction.java 3253 2008-07-08 09:26:40Z lasse $
 */
public class RevalidateAction extends AbstractAction {
    private static Log LOGGER = LogFactory.getLog(RevalidateAction.class);

    /**
     * Creates a revalidate action implementation.
     *
     * @param element the element.
     * @param model the context model.
     */
    public RevalidateAction(Element element, Model model) {
        super(element, model);
    }

    // implementation of 'de.betterform.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>revalidate</code> action.
     *
     * @throws XFormsException if an error occurred during
     * <code>revalidate</code> processing.
     */
    public void perform() throws XFormsException {
        // revalidate model
        this.model.revalidate();

        // update behaviour
        doRevalidate(false);
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
