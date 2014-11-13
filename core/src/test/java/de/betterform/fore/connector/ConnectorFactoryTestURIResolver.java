/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.connector;

/**
 * Dummy URI resolver for the connector factory test.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: ConnectorFactoryTestURIResolver.java 2797 2007-08-10 12:45:24Z joern $
 */
public class ConnectorFactoryTestURIResolver extends AbstractConnector implements URIResolver {

    /**
     * Returns <code>null</code>.
     *
     * @return <code>null</code>.
     */
    public Object resolve() {
        return null;
    }
}

// end of class
