/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.events.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.betterform.fore.xml.events.XMLEvent;
import org.apache.xerces.dom.events.EventImpl;
import org.w3c.dom.events.EventTarget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * XMLEvent implementation based on Xerces' DOM event implementation.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: XercesXMLEvent.java 3576 2008-10-09 15:46:30Z lars $
 */
public class XercesXMLEvent extends EventImpl implements XMLEvent {

    /**
     * The contextual information.
     */
    private Map contextInfo = null;

    /**
     * Creates a new Xerces XML Event.
     */
    public XercesXMLEvent() {
        // NOP
    }

    /**
     * Creates a new Xerces XML Event.
     *
     * @param type the event type.
     */
    public XercesXMLEvent(String type) {
        this.type = type;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }

    /**
     * Returns the default contextual information for this event.
     *
     * @return the default contextual information for this event.
     */
    public Map getContextInfo() {
            if (this.contextInfo == null) {
                return null;
            } else {
                return this.contextInfo;
            }

    }

    /**
     * Returns the contextual information for this event denoted by the
     * specified property.
     * <p/>
     * If no such contextual information is available, this method returns
     * <code>null</code>.
     *
     * @param property the name of the context property.
     * @return contextual information for this event.
     */
    public Object getContextInfo(String property) {
        if (this.contextInfo != null && property != null) {
            return this.contextInfo.get(property);
        }
        return null;
    }

    /**
     * Returns the set of property names used for contextual information.
     * <p/>
     * If no such contextual information is available, this method returns
     * <code>null</code>.
     *
     * @return the set of property names used for contextual information.
     */
    @JsonIgnore
    public Collection getPropertyNames() {
        if (this.contextInfo != null) {
            return this.contextInfo.keySet();
        }
        return null;
    }

    @Override
    public String toString() {
          StringBuilder result = new StringBuilder();
          String newLine = System.getProperty("line.separator");

          result.append( this.getClass().getName() );
          result.append( " { " );
            result.append("type: " + this.type);
            result.append("contextInfo: " + this.contextInfo + "; ");
            result.append("target: " + this.target);
          result.append(" }");

          return result.toString();
    }

    public void addProperty(String name,Object value) {
        if(this.contextInfo != null && name != null){
            this.contextInfo.put(name,value);
        }
    }

    /**
     * Initializes this event.
     *
     * @param type       specifies the event type.
     * @param bubbles    specifies wether the event can bubble.
     * @param cancelable specifies wether the event's default action can be
     *                   prevented.
     * @param context    optionally specifies contextual information.
     */
    public void initXMLEvent(String type, boolean bubbles, boolean cancelable, Object context) {
        super.initEvent(type, bubbles, cancelable);

        if (context != null) {
            if (Map.class.isAssignableFrom(context.getClass())) {
                this.contextInfo = (Map) context;
            } else {
                this.contextInfo = new HashMap();
                this.contextInfo.put(DIRTY_DEFAULT_INFO, context);
            }
        } else {
            this.contextInfo = new HashMap();
            this.contextInfo.put(DIRTY_DEFAULT_INFO, context);

/*
            if (this.contextInfo != null) {
                this.contextInfo.put(DIRTY_DEFAULT_INFO, context);
            } else {
                this.contextInfo = new HashMap();
                this.contextInfo.put(DIRTY_DEFAULT_INFO, context);

            }
*/
        }


    }

    @Override
    @JsonIgnore
    public EventTarget getTarget() {
        return super.getTarget();
    }

    @Override
    @JsonIgnore
    public EventTarget getCurrentTarget() {
        return super.getCurrentTarget();
    }

    @Override
    @JsonIgnore
    public boolean getBubbles() {
        return super.getBubbles();
    }

    @Override
    @JsonIgnore
    public boolean getCancelable() {
        return super.getCancelable();
    }

    @Override
    @JsonIgnore
    public short getEventPhase() {
        return super.getEventPhase();
    }

    @Override
    @JsonIgnore
    public void stopPropagation() {
        super.stopPropagation();
    }

    @Override
    @JsonIgnore
    public void preventDefault() {
        super.preventDefault();
    }

/*
    public Object getTargetName(){
        return (String) this.contextInfo.get("targetName");
    }

    public String getTargetId(){
       return (String) this.contextInfo.get("targetId");
    }
*/
}
