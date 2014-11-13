/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.action;

import de.betterform.fore.xml.xforms.exception.XFormsException;

/**
 * Interface for XForms Action implementations.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: XFormsAction.java 2106 2006-04-06 10:52:28Z unl $
 */
public interface XFormsAction {

    /**
     * Performs the action.
     * <p/>
     * This method is called when the event for which this action is registered
     * arrives at the observer.
     *
     * @throws XFormsException if an error occurred during action processing.
     */
    void perform() throws XFormsException;
}

// end of interface
