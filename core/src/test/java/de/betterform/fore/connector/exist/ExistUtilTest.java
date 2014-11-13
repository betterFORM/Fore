/*
 * Copyright (c) 2013. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.connector.exist;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import junit.framework.TestCase;
import de.betterform.fore.connector.util.URIUtil;

public class ExistUtilTest extends TestCase {
  
  private URI uri1;
  private URI uri2;
  private URI uri3;
  private URI uri4;
  
  @Override
  protected void setUp() throws Exception {
    uri1 = new URI("http://localhost:8080/path1/path2/uri1.xml");
    uri2 = new URI("http://localhost:8080/file1.xqm?function=foo&k1=v1&k2=v2&xsl=file1.xsl&xpath=//data");
    uri3 = new URI("http://localhost:8080/path1/path2/uri1.xml?function=foo&param1=value1&param2=value2");
    uri4 = new URI("http://localhost:8080");
  }

  
  public void testCalculateArity() throws URISyntaxException {
    
    Map<String, String> p1 = URIUtil.getQueryParameters(uri1);
    Map<String, String> p2 = URIUtil.getQueryParameters(uri2);
    Map<String, String> p3 = URIUtil.getQueryParameters(uri3);
    Map<String, String> p4 = URIUtil.getQueryParameters(uri4);

    assertEquals(0, ExistUtil.getXQueryFunctionArity(p1));
    assertEquals(2, ExistUtil.getXQueryFunctionArity(p2));
    assertEquals(2, ExistUtil.getXQueryFunctionArity(p3));
    assertEquals(0, ExistUtil.getXQueryFunctionArity(p4));
  }
  
  public void testGetAsFunctionParameters() throws URISyntaxException {
    
    assertNotNull(ExistUtil.getAsFunctionParameters(null));
    assertEquals(0, ExistUtil.getAsFunctionParameters(null).size());
    
    assertEquals(0, ExistUtil.getAsFunctionParameters(URIUtil.getQueryParameters(uri1)).size());
    assertEquals(2, ExistUtil.getAsFunctionParameters(URIUtil.getQueryParameters(uri2)).size());
    assertEquals(2, ExistUtil.getAsFunctionParameters(URIUtil.getQueryParameters(uri3)).size());
    assertEquals(0, ExistUtil.getAsFunctionParameters(URIUtil.getQueryParameters(uri4)).size());
  }

}
