/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.agent.web.servlet;

import de.betterform.fore.agent.web.WebFactory;
import de.betterform.fore.agent.web.WebUtil;
import de.betterform.fore.xml.dom.DOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * This servlet is expected to be used in conjunction with XFormsFilter. The filter must be mapped
 * to the URL of the servlet in web.xml.<br><br>
 * <p/>
 * Takes a POST request as input and expects a XHTML/XForms ducument as POST body.
 * The body will be passed as DOM and forwarded as a request attribute.
 *
 * @author Joern Turner
 */
public class XFormsPostServlet extends HttpServlet {
    private static final Log LOGGER = LogFactory.getLog(XFormsPostServlet.class);
    /**
     * this constant is used to signal that a xforms document is inited by this servlet. It's used by the XFormsServlet
     * to distinguish between 'normal' POST requests and an init through this servlet.
     */
    public static final String INIT_BY_POST = "betterform.init-by-post";
    /**
     * read from the inputStream and parse that as DOM. The result is passed into a request attribute for
     * processing by the XFormsFilter.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.debug("hit XFormsPostServlet");


        request.setCharacterEncoding("UTF-8");
        WebUtil.nonCachingResponse(response);

        Document doc;
        try {
            //parse it
            doc = DOMUtil.parseInputStream(request.getInputStream(), true, false);
        } catch (ParserConfigurationException e) {
            throw new ServletException(e);
        } catch (SAXException e) {
            throw new ServletException(e);
        }

        //do the Filter twist
        request.setAttribute(WebFactory.XFORMS_NODE, doc);
        request.setAttribute(XFormsPostServlet.INIT_BY_POST, doc);
        response.getOutputStream().close();
    }

}
