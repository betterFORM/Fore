/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.model.constraints;

import de.betterform.fore.xml.xforms.exception.XFormsComputeException;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.ModelItem;
import de.betterform.fore.xml.xpath.impl.saxon.BetterFormXPathContext;
import de.betterform.fore.xml.xpath.impl.saxon.XPathCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

/**
 * The <code>calculate</code> vertex implementation used in the recalculation
 * sequence algorithm.
 * <p/>
 * This class also implements the virtual <code>value</code> vertex.
 *
 * @author This code is based on the ideas of Mikko Honkala from the X-Smiles
 *         project. Although it has been heavily refactored and rewritten to
 *         meet our needs.
 * @version $Id: CalculateVertex.java 3253 2008-07-08 09:26:40Z lasse $
 */
public class CalculateVertex extends Vertex {
    private static Log LOGGER = LogFactory.getLog(CalculateVertex.class);

    /**
     * Creates a new CalculateVertex object.
     *
     * @param relativeContext the parent xpath context
     * @param instanceNode the instance item this constraint is attached to
     * @param xpathExpression the xpath expression from the bind Element
     */
    public CalculateVertex(BetterFormXPathContext relativeContext, Node instanceNode, String xpathExpression) {
        super(relativeContext, instanceNode, xpathExpression);
    }

    /**
     * returns the type of Vertex
     *
     * @return type of Vertex
     */
    public short getVertexType() {
        return CALCULATE_VERTEX;
    }

    /**
     * evaluates xpath expression in context of its parent context
     * (relativeContext).
     * @throws XFormsException 
     */
    public void compute() throws XFormsComputeException {

            if (this.xpathExpression != null) {
                    ModelItem modelItem = (ModelItem) this.instanceNode.getUserData("");
                try {
                    String result = XPathCache.getInstance().evaluateAsString(relativeContext, "string(" + this.xpathExpression + ")");
                    modelItem.setValue(result);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("evaluated expression '" + this.xpathExpression + "' to '" + result + "'");
                    }
                } catch (XFormsException xfe) {
                       throw new XFormsComputeException(xfe.getMessage(), (Exception) xfe.getCause(), modelItem.getModel().getTarget(), xfe.getMessage());
                }
            }
    }

    /**
     * returns true, if both instanceNode and xpathexpression are the same
     *
     * @param object an instance of this Vertex
     * @return true, if both instanceNode and xpathexpression are the same
     */
    public boolean equals(Object object) {
        if (this.xpathExpression == null) {
            if (object == null) {
                return false;
            }

            if (!(object instanceof CalculateVertex)) {
                return false;
            }

            CalculateVertex v = (CalculateVertex) object;

            return this.instanceNode.equals(v.instanceNode) && (v.xpathExpression == null);
        }

        return super.equals(object);
    }

    /**
     * overwrites object toString().
     *
     * @return Vertex info as String
     */
    public String toString() {
        if (this.xpathExpression == null) {
            return super.toString() + " - value(" + (this.instanceNode.getNodeType() == Node.ATTRIBUTE_NODE ? "@" : "") + this.instanceNode.getNodeName() + ")";
        }

        return super.toString() + " - calculate(" + this.xpathExpression + ")";
    }
}

// end of class
