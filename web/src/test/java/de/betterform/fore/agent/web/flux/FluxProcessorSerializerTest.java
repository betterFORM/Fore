/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.agent.web.flux;

import de.betterform.fore.agent.web.cache.XFSessionCache;
import de.betterform.fore.xml.dom.DOMUtil;
import de.betterform.fore.xml.xpath.impl.saxon.XPathUtil;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * @author Tobi Krebs <tobias.krebs@betterform.de>
*/
    public class FluxProcessorSerializerTest  extends TestCase {
    private static final Log LOGGER = LogFactory.getLog(FluxProcessorSerializerTest.class);
    private List fluxProcessors;
    private Cache<String, SocketProcessor>  cache = null;
    private DefaultCacheManager cacheManager;
    private Cache<String, SocketProcessor> oneElementInMemory;
    private String baseURI;
    private int cnt=10;

    @Override
    protected void setUp() throws Exception {
        LOGGER.debug(".....::::setting up tests::::::.....");
        String path = getClass().getResource("localization.xhtml").getPath();
        baseURI = "file://" + path.substring(0, path.lastIndexOf("localization.xhtml"));
        this.fluxProcessors  = new ArrayList<SocketProcessor>(100);
        for (int i = 0; i < cnt; i++) {
            SocketProcessor processor = new SocketProcessor();
            processor.setBaseURI(baseURI);
            processor.setXForms(getClass().getResourceAsStream("localization.xhtml"));
            processor.setContext(new HashMap());
//            processor.setContextParam("key",i+"");
            processor.init();
            this.fluxProcessors.add(processor);
            LOGGER.debug(".....::::::::::..... " + i);
            LOGGER.debug(".....::::::::::..... " + processor.toString());

        }
        this.oneElementInMemory = XFSessionCache.getCache("xfTestConfigOneElementInMemory");
/*
        if (this.cache == null) {
            if (this.cacheManager == null) {
                this.cacheManager = new DefaultCacheManager("infinispan.xml");
            }
            this.cache = this.cacheManager.getCache("xfTestConfigOneElementInMemory");
        }
*/
        LOGGER.debug(".....::::setting up tests - done ::::::.....");
        LOGGER.debug(".....::::setting up tests - done ::::::.....");
        LOGGER.debug(".....::::setting up tests - done ::::::.....");

    }

    @Override
   protected void tearDown() throws Exception {
        super.tearDown();
        XFSessionCache.unloadCache();
        this.oneElementInMemory = null;
//        this.cacheManager.removeCache("xfTestConfigOneElementInMemory");
        /*
        CacheManager cacheManager = CacheManager.getInstance();
        Cache cache = cacheManager.getCache("xfTestConfigOneElementInMemory");
        cache.flush();
        cacheManager.shutdown();
        */
    }
/*
   private Cache initCache(String cacheName) {
       CacheManager cacheManager = CacheManager.getInstance();
       cacheManager.clearAll();
        assertTrue(cacheManager.cacheExists(cacheName));
        Cache cache = cacheManager.getCache(cacheName);
        LOGGER.info("Stats: " + cache.getStatistics());
        return cache;

    }*/

    private Cache initCache(String cacheName) {
        return this.cache;
    }

/*
    private XFSessionCache initCache(String cacheName) {
        return XFSessionCache.getInstance();
    } */

    /*
   public void testCreateSingleCache() throws Exception {
       Cache oneElementInMemory =initCache("xfTestConfigOneElementInMemory");
       assertNotNull(oneElementInMemory);
   }

   public void testCreateMultipleCache() throws Exception {
       Cache oneElementInMemory1 =initCache("xfTestConfigOneElementInMemory");
        assertNotNull(oneElementInMemory1);
       Cache oneElementInMemory2 =initCache("xfTestConfigOneElementInMemory");
        assertNotNull(oneElementInMemory2);

        assertEquals(oneElementInMemory1, oneElementInMemory2);
    }

   public void testPutIntoCache() throws Exception {
       LOGGER.info("...::: testPutIntoCache :::...");
       Cache oneElementInMemory =initCache("xfTestConfigOneElementInMemory");
        Iterator<String> keys  =this.fluxProcessors.keySet().iterator();

       while(keys.hasNext()) {
           String key = keys.next();
           SocketProcessor processor  = this.fluxProcessors.get(key);
           assertEquals(key, processor.getKey());
           oneElementInMemory.put(key, processor);
       }
      // LOGGER.info("Stats: " + oneElementInMemory.getStatistics());
   }

    public void testPutAndGetCache() throws Exception {
        LOGGER.info("...::: testPutAndGetCache :::...");
       Cache oneElementInMemory =initCache("xfTestConfigOneElementInMemory");
        Iterator<String> keys  =this.fluxProcessors.keySet().iterator();

        while(keys.hasNext()) {
            String key = keys.next();
            SocketProcessor processor  = this.fluxProcessors.get(key);
            assertEquals(key, processor.getKey());
            oneElementInMemory.put(key, processor);
        }

        keys  =this.fluxProcessors.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            SocketProcessor processor  = (SocketProcessor) oneElementInMemory.get(key);
            assertNotNull(processor);
        }

    }
    */

    public void testPutAndGetFluXProcessorsCache() throws Exception {
        System.err.println("" + System.currentTimeMillis());
        int errors = 0;
        LOGGER.info("...::: testPutAndGetFluXProcessorCache :::...");
//        Cache oneElementInMemory =initCache("xfTestConfigOneElementInMemory");

        for (int i=0;i<cnt;i++){
            String key = i + "";
            SocketProcessor processor  = (SocketProcessor) this.fluxProcessors.get(i);
            //Element e =new Element(key, processor);
            processor.setContextParam("key",key);
            LOGGER.debug("putting ... " + processor.getContextParam("key"));

            oneElementInMemory.put(key, processor);

            for(int j=0; j<10;j++){
                processor.dispatchEvent("insert");
            }

        }

        LOGGER.debug("####################### now reading");

        for (int i=0;i<cnt;i++){
            String key = i + "";
            LOGGER.debug("");
            LOGGER.debug("GETTING ... " + key);
            LOGGER.debug("inCache ... " + oneElementInMemory.containsKey(key));
            LOGGER.debug("inCache ... " + oneElementInMemory.containsKey(key));
            SocketProcessor processor = (SocketProcessor) oneElementInMemory.get(key);
            assertNotNull(processor);
            processor.init();
            assertEquals(this.baseURI,processor.getBaseURI());
            assertEquals("schade",   processor.getXFormsModel(null).getInstanceDocument("internal").getElementsByTagName("item").item(0).getTextContent());

            processor.dispatchEvent("t-refresh");

            assertEquals("hello", processor.getXFormsModel(null).getInstanceDocument("internal").getElementsByTagName("item").item(0).getTextContent());

            LOGGER.debug("Instance >>>>>>>>>>>>>>>>>>>>>");
            DOMUtil.prettyPrintDOM(processor.getXformsProcessor().getContainer().getDefaultModel().getDefaultInstance().getInstanceDocument());

            assertEquals("17", XPathUtil.evaluateAsString(processor.getXForms(), "//xf:repeat/bf:data/@bf:index"));

            LOGGER.debug("GET: key in processor: " + processor.getXForms().getDocumentElement().getAttribute("bf:serialized"));

            LOGGER.debug("TOGGLE");
            processor.dispatchEvent("toggle");
            assertEquals("default", XPathUtil.evaluateAsString(processor.getXForms(), "//xf:case[bf:data/@bf:selected = 'true']/@id"));


        }

        LOGGER.info("Errors: " + errors);
    }



/*
    public void testPutAndGetFluXProcessorCache() throws Exception {
        LOGGER.info("...::: testPutAndGetFluXProcessorCache :::...");
        Cache oneElementInMemory =initCache("xfTestConfigOneElementInMemory");
        Iterator<String> keys  =this.fluxProcessors.keySet().iterator();

        String key1 = keys.next();
        String key2 = keys.next();
        
        FluxProcessor processor  = this.fluxProcessors.get(key1);
        assertEquals(key1, processor.getKey());
        oneElementInMemory.put(key1, processor);
        processor  = this.fluxProcessors.get(key2);
        assertEquals(key2, processor.getKey());
        oneElementInMemory.put(key2, processor);
       
           Exception exception = null;
           try {
                processor = (FluxProcessor) oneElementInMemory.get(key1);
               assertNotNull(processor);
               assertNotNull(processor);
               assertNull(processor.getContext());
               Document document = processor.getXForms() ;
               String localName = document.getDocumentElement().getLocalName();
               assertEquals("html", localName);
               processor.init();
               assertEquals("schade",   processor.getXFormsModel(null).getInstanceDocument("internal").getElementsByTagName("item").item(0).getTextContent());
               processor.dispatchEvent("t-refresh");

               processor = (FluxProcessor) oneElementInMemory.get(key2);
               assertNotNull(processor);
               assertNotNull(processor);
               assertNull(processor.getContext());
               document = processor.getXForms() ;
               localName = document.getDocumentElement().getLocalName();
               assertEquals("html", localName);
               processor.init();
               assertEquals("schade",   processor.getXFormsModel(null).getInstanceDocument("internal").getElementsByTagName("item").item(0).getTextContent());
               processor.dispatchEvent("t-refresh");
           } catch (Exception e) {
               e.printStackTrace();
               exception = e;
           }
           assertNull(exception);

    }              */

}
