/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.connector.serializer;

import de.betterform.fore.connector.InstanceSerializer;
import de.betterform.fore.xml.xforms.model.ModelItem;
import de.betterform.fore.xml.xforms.model.submission.Submission;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Multipart/Related serializer
 *
 * @author Peter Mikula <peter.mikula@digital-artefacts.fi>
 * @version $Id: MultipartRelatedSerializer.java 2099 2006-03-28 16:24:07Z unl $
 */
public class MultipartRelatedSerializer implements InstanceSerializer {

    public void serialize(Submission submission, Node node, SerializerRequestWrapper wrapper, String defaultEncoding) throws Exception {
        Map cache = new HashMap();
        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart part = new MimeBodyPart();
        multipart.addBodyPart(part);

        String encoding = defaultEncoding;
        if (submission.getEncoding() != null) {
            encoding = submission.getEncoding();
        }

        if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.DOCUMENT_NODE) {
        	part.setText(new String(serializeXML(multipart, cache,
                submission, node, encoding), encoding), encoding);
        }
        else {
        	part.setText(node.getTextContent());
        }
        part.setContentID("<instance.xml@start>");
        part.addHeader("Content-Type", "application/xml");
        part.addHeader("Content-Transfer-Encoding", "base64");
        part.setDisposition("attachment");
        part.setFileName("instance.xml");

        multipart.setSubType("related; type=\"" + submission.getMediatype() + "\"; start=\"instance.xml@start\"");
        // FIXME: Is this a global http header or a local mime header?
        wrapper.getBodyStream().write(("Content-Type: " + multipart.getContentType() + "\n\nThis is a MIME message.\n").getBytes(encoding));
        multipart.writeTo(wrapper.getBodyStream());
    }

    protected byte[] serializeXML(MimeMultipart multipart, Map cache,
                                  Submission submission, Node instance, String encoding) throws Exception {
        // traverse the tree and replace fileupload
        if (instance instanceof Document) {
            visitNode(cache, ((Document) instance).getDocumentElement(), multipart);
        }
        else {
            visitNode(cache, instance, multipart);
        }

        SerializerRequestWrapper wrapper = new SerializerRequestWrapper(new ByteArrayOutputStream());
        XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(submission, instance, wrapper, encoding);
        ByteArrayOutputStream stream = (ByteArrayOutputStream) wrapper.getBodyStream();        
        return stream.toByteArray();
    }

    protected void visitNode(Map cache, Node node, MimeMultipart multipart) throws Exception {

        ModelItem item = (ModelItem) node.getUserData("");
        if (item != null && item.getDeclarationView().getDatatype() != null && item.getDeclarationView().getDatatype().equalsIgnoreCase("anyURI")) {
            String name = item.getFilename();
            if (name == null || item.getValue() == null || item.getValue().equals("")) {
                return;
            }

            String cid = (String) cache.get(name);
            if (cid == null) {
                int count = multipart.getCount();
                cid = name + "@part" + (count + 1);

                MimeBodyPart part = new MimeBodyPart();
                part.setContentID("<" + cid + ">");

                DataHandler dh = new DataHandler(new ModelItemDataSource(item));
                part.setDataHandler(dh);

                part.addHeader("Content-Type", item.getMediatype());
                part.addHeader("Content-Transfer-Encoding", "base64");
                part.setDisposition("attachment");
                part.setFileName(name);
                multipart.addBodyPart(part);
                cache.put(name, cid);
            }

            Element element = (Element) node;
            // remove text node
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() != Node.TEXT_NODE) {
                    continue;
                }
                n.setNodeValue("cid:" + cid);
                break;
            }
        }
        else {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    visitNode(cache, n, multipart);
                }
            }
        }
    }

    private class ModelItemDataSource implements DataSource {

        private ModelItem item;

        ModelItemDataSource(ModelItem item) {
            this.item = item;
        }

        public String getContentType() {
            String type = item.getMediatype();
            if (type == null) {
                type = "application/octet-stream";
            }
            return type;
        }

        public String getName() {
            return item.getFilename();
        }

        public InputStream getInputStream() throws IOException {

            byte [] data = null;
            try {
                if (item.getDeclarationView().getDatatype().equals("anyURI")) {
                    URL url = new URL(item.getValue());
                    return url.openStream();
                }
                else if (item.getDeclarationView().getDatatype().equals("base64Binary")) {
                    data = Base64.decodeBase64(item.getValue().getBytes());
                }
                else if (item.getDeclarationView().getDatatype().equals("hexBinary")) {
                    data = Hex.decodeHex(item.getValue().toCharArray());
                }
                else {
                    throw new IOException("unsupported data type");
                }
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            return new ByteArrayInputStream(data);
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("unsupported operation.");
        }
    }
}

// end of class
