/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.action.extensions;

import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.events.BetterFormEventNames;
import de.betterform.fore.xml.xforms.action.AbstractAction;
import de.betterform.fore.xml.xforms.exception.XFormsBindingException;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.Model;
import de.betterform.fore.xml.xforms.ui.AbstractUIElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * Implements the hide action as defined in <a src="http://www.w3.org/MarkUp/Forms/wiki/Dialog">XForms 1.2 Dialog proposal</a>
 *
 * @author Ronald van Kuijk, based on work by
 * @author Ulrich Nicolas Liss&eacute;
 */
public class HideAction extends AbstractAction {
    private static Log LOGGER = LogFactory.getLog(HideAction.class);
    private String referencedDialog = null;

    /**
     * Creates a setfocus action implementation.
     *
     * @param element the element.
     * @param model   the context model.
     */
    public HideAction(Element element, Model model) {
        super(element, model);
    }

    // lifecycle methods

    /**
     * Performs element init.
     */
    public void init() throws XFormsException {
        super.init();

        if (this.referencedDialog == null) {
            this.referencedDialog = getXFormsAttribute("dialog");
        }
        if (this.referencedDialog == null) {
            throw new XFormsBindingException("missing dialog attribute at " + DOMUtil.getCanonicalPath(this.getElement()), this.target, null);
        }
    }

    // implementation of 'de.betterform.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>setfocus</code> action.
     *
     * @throws XFormsException if an error occurred during <code>setfocus</code>
     *                         processing.
     */
    public void perform() throws XFormsException {
        // check dialog idref
        Object dialogObject = this.container.lookup(this.referencedDialog);
        if (dialogObject == null || !(dialogObject instanceof AbstractUIElement)) {
            throw new XFormsBindingException("invalid control id at " + DOMUtil.getCanonicalPath(this.getElement()), this.target, this.referencedDialog);
        }

        // dispatch xforms-focus to form control
        this.container.dispatch(((AbstractUIElement) dialogObject).getTarget(), BetterFormEventNames.HIDE, null);
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
