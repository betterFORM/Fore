package de.betterform.fore.agent.web.atmosphere;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.betterform.fore.agent.web.WebUtil;
import de.betterform.fore.agent.web.flux.SocketProcessor;
import de.betterform.fore.xml.events.XMLEvent;
import de.betterform.fore.xml.xforms.exception.XFormsException;
import de.betterform.fore.xml.xforms.model.ModelItem;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.events.Event;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@ManagedService(path = "/msg/{xfid}")
public class BetterSocket{
    private final Logger logger = LoggerFactory.getLogger(BetterSocket.class);
    private AtmosphereResourceFactory resourceFactory;

    @PathParam("xfid")
    private String xfSession;

    @Ready
    public void onReady(final AtmosphereResource r) {
        if(logger.isDebugEnabled()){
            logger.debug("Browser {} connected." + r.uuid());
            logger.debug("xfSession: " + xfSession);
        }
        this.resourceFactory = AtmosphereResourceFactory.getDefault();

        //fetch xforms session id from http session which has been put there in XFormsFilter
        HttpSession httpSessionsession=r.getRequest().getSession();
        String xformsKey = (String) httpSessionsession.getAttribute("xfSessionKey");
        SocketProcessor socketProcessor = (SocketProcessor) WebUtil.getWebProcessor(xformsKey,
                                                                                    r.getRequest(),
                                                                                    r.getResponse(),
                                                                                    r.getRequest().getSession());

        //store the processor instance in AtmosphereSession
        setSessionValue(r,"processor",socketProcessor);

        //return events that already executed during xforms model init
//        eventListToJSON(r, socketProcessor);

        if (logger.isDebugEnabled()) {
            logger.debug("XForms Session key: " + xformsKey);
        }
    }


    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            logger.info("Browser {} unexpectedly disconnected" + event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            logger.info("Browser {} closed the connection" + event.getResource().uuid());
        }
    }


    @org.atmosphere.config.service.Message(
            encoders = {JacksonEncoder.class},
            decoders = {JacksonDecoder.class}
            )
    public Message onMessage(Message message) throws IOException {

        AtmosphereResource resource = resourceFactory.find(message.getUuid());
        SocketProcessor xp = getSessionValue(resource,"processor",SocketProcessor.class);

        if (logger.isDebugEnabled()) {
            logger.debug("message: " + message);
            logger.debug("message.uuid: " + message.getUuid());
            logger.debug("AtmosphereResource: " + resource);
            logger.debug("XFormsProcessor: " + xp);
        }
        if(message.getEventType().equalsIgnoreCase("init-model")){
            eventListToJSON(resource, xp);
        }
        if(message.getEventType().equalsIgnoreCase("DOMActivate")){
            try {
                xp.dispatchEvent(message.getTargetId());
                eventListToJSON(resource, xp);

            }  catch (XFormsException e) {
                e.printStackTrace();
            }
            message.setValue("hat getan");

        }
        if(message.getEventType().equalsIgnoreCase("init")){
//            message.setValue("joooh");
            try {
                ModelItem item = xp.fetchState(message.getRef());
                String value = item.getValue();

                //passing ModelItem state
                message.setReadonly(item.isReadonly());
                message.setRequired(item.isRequired());
                message.setRelevant(item.isRelevant());
                message.setValid(item.isValid());
                message.setDatatype(item.getXSIType());

                message.setValue(value);
            } catch (XFormsException e) {
                e.printStackTrace();
            }
        }
        if(message.getEventType().equalsIgnoreCase("valueChange")){
            try {
                xp.setControlValue(message.getId(),message.getValue());
//                ModelItem item = xp.fetchState(message.getRef());
//                xp.setNodeValue(item, message.getValue());
//                message.setReadonly(item.isReadonly());
//                message.setRequired(item.isRequired());
//                message.setRelevant(item.isRelevant());
//                message.setValid(item.isValid());
//                message.setDatatype(item.getXSIType());
                eventListToJSON(resource, xp);

            } catch (XFormsException e) {
                e.printStackTrace();
            }
        }


        return message;

    }

    public void setSessionValue(AtmosphereResource resource, String name, Object value) {
        AtmosphereResourceSessionFactory factory = AtmosphereResourceSessionFactory.getDefault();
        AtmosphereResourceSession session = factory.getSession(resource);
        session.setAttribute(name, value);
    }

    public static <T> T getSessionValue(AtmosphereResource resource, String name, Class<T> type) {
        AtmosphereResourceSessionFactory factory = AtmosphereResourceSessionFactory.getDefault();
        AtmosphereResourceSession session = factory.getSession(resource, false);
        T value = null;
        if (session != null) {
            value = session.getAttribute(name, type);
        }
        return value;
    }

    private void eventListToJSON(AtmosphereResource r, SocketProcessor socketProcessor) {

        Broadcaster privateChannel = BroadcasterFactory.getDefault().lookup(xfSession,true);
        privateChannel.addAtmosphereResource(r);
        List<XMLEvent> xmlEvents = socketProcessor.getEventQueue().getEventList();
        for (int i = 0; i < xmlEvents.size(); i++) {
            Event ev = xmlEvents.get(i);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
            String json = "";
            try {
                json = mapper.writeValueAsString(ev);

                if (logger.isDebugEnabled()) {
                    logger.debug("mapped json object: " + json);
                }
                privateChannel.broadcast(json,r);
            } catch (JsonProcessingException e) {
                logger.error("mapping xml event to JSON failed"+ e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socketProcessor.getEventQueue().flush();
    }

}
