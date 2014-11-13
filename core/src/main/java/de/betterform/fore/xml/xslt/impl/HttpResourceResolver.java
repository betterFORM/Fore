/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xslt.impl;

import de.betterform.fore.connector.ConnectorFactory;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMSource;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

/**
 * Resolves file resources.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: HttpResourceResolver.java 2922 2007-10-17 14:07:48Z lars $
 */
public class HttpResourceResolver implements ResourceResolver {

    /**
     * Resolves file resources.
     *
     * @param uri the URI denoting a resource.
     * @return the resource specified by the URI or <code>null</code> if this
     * resolver can't handle the URI.
     * @throws java.io.IOException if an error occurred during resolution.
     */
    public Resource resolve(URI uri) throws XFormsException {
        if (!uri.getScheme().equals("http")) {
            return null;
        }

        //String absoluteURI = resolve(uri);
        ConnectorFactory connectorFactory = null;
        InputStream inputStream = null;
        Document doc = null;
        connectorFactory = ConnectorFactory.getFactory();
        connectorFactory.setContext(new HashMap());
        doc = (Document) connectorFactory.createURIResolver(uri.toString(), null).resolve();
        
        return new HttpResource(new DOMSource(doc));
    }
}