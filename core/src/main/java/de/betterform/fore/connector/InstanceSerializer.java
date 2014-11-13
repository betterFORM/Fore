/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.connector;

import de.betterform.fore.connector.serializer.SerializerRequestWrapper;
import de.betterform.fore.xml.xforms.model.submission.Submission;
import org.w3c.dom.Node;

/**
 * Interface for serialization of instances. Implementations should
 * be registered for specific scheme/method/mediatype using
 * <tt>registerSerializer</tt> method for given connector.
 *
 * @author Peter Mikula <peter.mikula@digital-artefacts.fi>
 */
public interface InstanceSerializer {

    /**
     * Serialize instance into the <tt>SerializerWrapperObject</tt>
     *
     * @param submission                submission information.
     * @param instance                  instance to serialize.
     * @param serializerRequestWrapper  object to write into.
     * @param defaultEncoding           use this encoding in case user did not provide one.
     */
    void serialize(Submission submission, Node instance, SerializerRequestWrapper serializerRequestWrapper,    
                   String defaultEncoding) throws Exception;

}
