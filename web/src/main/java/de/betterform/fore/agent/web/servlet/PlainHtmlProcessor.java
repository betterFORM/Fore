/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */


package de.betterform.fore.agent.web.servlet;

import de.betterform.fore.agent.web.WebFactory;
import de.betterform.fore.agent.web.WebProcessor;
import de.betterform.fore.agent.web.event.UIEvent;
import de.betterform.fore.xml.events.BetterFormEventNames;
import de.betterform.fore.xml.events.XMLEvent;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the interaction with non-scripted browser clients and provides an interface
 * to the XForms processor.
 *
 * @author joern turner
 * @version $Id: ServletAdapter.java 2875 2007-09-28 09:43:30Z lars $
 * @deprecated request/response model not supported any more
 */
public class PlainHtmlProcessor extends WebProcessor implements EventListener {

    private static final Log LOGGER = LogFactory.getLog(PlainHtmlProcessor.class);

    /**
     * Creates a new ServletAdapter object.
     */
    public PlainHtmlProcessor() {
        super();
    }

    /**
     * place to put application-specific params or configurations before
     * actually starting off the XFormsProcessor. It's the responsibility of
     * this method to call xFormsProcessorImpl.init() to finish up the processor setup.
     *
     * @throws XFormsException If an error occurs
     */
    public void init() throws XFormsException {
        super.init();
    }


    /**
     * ServletAdapter knows and executes only one UIEvent: 'http-request'
     * which will contain the HttpServletRequest as contextInfo.
     *
     * @param event only events of type 'http-request' will be handled
     * @throws XFormsException
     */
    public void handleUIEvent(UIEvent event) throws XFormsException {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("PlainHtmlProcessor.handleUIEvent event: " + event.getEventName());
        }
        super.handleUIEvent(event);

        if (event.getEventName().equalsIgnoreCase("http-request")) {
            HttpServletRequest request = (HttpServletRequest) event.getContextInfo();
            getHttpRequestHandler().handleRequest(request);
        } else {
            LOGGER.warn("ignoring unknown event '" + event.getEventName() + "'");
        }
    }

    /**
     * This method is called whenever an event occurs of the type for which the
     * <code> EventListener</code> interface was registered.
     *
     * @param event The <code>Event</code> contains contextual information about
     *              the event. It also contains the <code>stopPropagation</code> and
     *              <code>preventDefault</code> methods which are used in determining the
     *              event's flow and default action.
     */
    public void handleEvent(Event event) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("PlainHtmlProcessor.handleEvent event type: " + event.getType());
        }
        super.handleEvent(event);

        try {
            if (event instanceof XMLEvent) {
                XMLEvent xmlEvent = (XMLEvent) event;
                String type = xmlEvent.getType();
                if (BetterFormEventNames.REPLACE_ALL.equals(type) || BetterFormEventNames.REPLACE_ALL_XFORMS.equals(type)) {
                    // get event context and store it in session
                    Map submissionResponse = new HashMap();
                    submissionResponse.put("header", xmlEvent.getContextInfo("header"));
                    submissionResponse.put("body", xmlEvent.getContextInfo("body"));
                    this.xformsProcessor.setContextParam(WebFactory.BETTERFORM_SUBMISSION_RESPONSE, submissionResponse);

                    this.exitEvent = xmlEvent;
                    shutdown();
                } else if (BetterFormEventNames.LOAD_URI.equals(type)) {
                    // get event properties
                    String show = (String) xmlEvent.getContextInfo("show");

                    if ("replace".equals(show)) {
                        this.exitEvent = xmlEvent;
                        shutdown();
                    }
                }

            }
        }
        catch (Exception e) {
            handleEventException(e);
        }
    }

}

// end of class
