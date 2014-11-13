/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xslt.impl;

import de.betterform.fore.xml.xforms.exception.XFormsException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * A inputStream resource.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: FileResource.java 2961 2007-10-26 14:20:08Z lars $
 */
public class ClassPathResource implements Resource {
    private InputStream inputStream;

    /**
     * Creates a new inputStream resource.
     *
     * @param - inputstream
     */
    public ClassPathResource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Returns the last modified timestamp of this resource.
     *
     * @return the last modified timestamp of this resource.
     */
    public long lastModified() {
        return -1;
    }

    /**
     * Returns the input stream of this resource.
     *
     * @return the input stream of this resource.
     */
    public InputStream getInputStream() throws XFormsException {
        return this.inputStream;
    }
    public Source getSource() throws XFormsException {
        return new StreamSource(this.inputStream);
    }
}
