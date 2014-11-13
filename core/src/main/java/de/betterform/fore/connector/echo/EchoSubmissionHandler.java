/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.connector.echo;

import de.betterform.fore.connector.AbstractConnector;
import de.betterform.fore.connector.SubmissionHandler;
import de.betterform.fore.connector.serializer.SerializerRequestWrapper;
import de.betterform.fore.xml.xforms.XFormsProcessor;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.submission.Submission;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This SubmissionHandler is mainly for testing purposes. It simply returns the DOM it receives.
 *
 * @author Joern Turner
 * @version $Id: EchoSubmissionHandler.java 3253 2008-07-08 09:26:40Z lasse $
 */
public class EchoSubmissionHandler extends AbstractConnector implements SubmissionHandler {

    /**
     * Returns (echoes) the instance data as submission response.
     *
     * @param submission the submission issuing the request.
     * @param instance the instance data to be serialized and submitted.
     * @return a map holding the original protocol specific response.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {
        try {
            SerializerRequestWrapper wrapper = new SerializerRequestWrapper(new ByteArrayOutputStream());
            serialize(submission, instance, wrapper);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(((ByteArrayOutputStream) wrapper.getBodyStream()).toByteArray());
            wrapper.getBodyStream().close();

            Map response = new HashMap();
            response.put(XFormsProcessor.SUBMISSION_RESPONSE_STREAM, inputStream);

            return response;
        }
        catch (Exception e) {
            throw new XFormsException(e);
        }
    }
}
