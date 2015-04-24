<?xml version="1.0" encoding="UTF-8"?>
<!--
~ Copyright (c) 2012. betterFORM Project - http://www.betterform.de
~ Licensed under the terms of BSD License
-->

<xsl:stylesheet version="2.0"
        xmlns=""
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:ev="http://www.w3.org/2001/xml-events"
        xmlns:xi="http://www.w3.org/2001/XInclude"
        xmlns:bfc="http://betterform.sourceforge.net/xforms/controls"
        xmlns:xf="http://www.w3.org/2002/xforms"
        xmlns:bf="http://betterform.sourceforge.net/xforms"
        exclude-result-prefixes="bf xsl">

    <!-- 'data' will be passed in case we deal with a html form submit and second layer validation -->
    <!--<xsl:param name="data" select="'display-name:ccc,line4:just a test,line5:,web:,line1:,type:,note:[aaa;sdfdsfds],line3:,country:,line2:'"/>-->
    <xsl:param name="data" select="'address[1].line2:1112,display-name:foobar,address[2].line3:2222,address[1].line3:1113,address[2].line4:2224,address[2].type:a,address[1].line4:,address[1].type:,web:,address[2].country:,address[1].line1:1111,address[2].line2:,address[2].line1:22222,address[1].country:,address[1].line5:,address[2].line5:,note:,'"/>
    <!--<xsl:param name="data" select="''"/>-->

    <xsl:output method="xhtml" omit-xml-declaration="yes"/>

    <!--
    #################################################################
    injects http request form data into generated XForms model as instance
    #################################################################
    -->

    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="@*|node()|text()">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xf:instance">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="datavalues"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*" mode="datavalues">
        <xsl:message>name:<xsl:value-of select="name()"/></xsl:message>

        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="formData" select="tokenize($data, ',')"/>

        <xsl:variable name="theValue">
            <xsl:for-each select="$formData">
                <xsl:message>formData token: <xsl:value-of select="."/></xsl:message>
                <xsl:if test="starts-with(.,$name)">
                    <!--<xsl:variable name="varname" select="substring-before(.,':')"/>-->
                    <xsl:variable name="varname" select="substring-before(.,':')"/>
                    <xsl:if test="$varname = $name">
                        <xsl:choose>
                            <xsl:when test="starts-with(.,'[')">
                                <xsl:value-of select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="varValue" select="substring-after(.,':')"/>
                                <xsl:value-of select="$varValue"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:message>the value: <xsl:value-of select="$theValue"/></xsl:message>
        <xsl:choose>
            <xsl:when test="starts-with($theValue,'[')">
                <xsl:variable name="subtokens" select="tokenize($theValue,';')"/>
                <xsl:for-each select="$subtokens">
                    <xsl:message>token <xsl:value-of select="."/></xsl:message>
                    <xsl:element name="{$name}">
                        <xsl:choose>
                            <xsl:when test="starts-with(.,'[')">
                                <xsl:value-of select="substring(.,2)"/>
                            </xsl:when>
                            <xsl:when test="ends-with(.,']')">
                                <xsl:value-of select="substring(.,1,string-length(.)-1)"/>
                            </xsl:when>
                            <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="string-length($theValue) != 0">
                <xsl:element name="{name(.)}">
                    <xsl:value-of select="$theValue"/>
                </xsl:element>
            </xsl:when>
            <xsl:when test="string-length($theValue) = 0">
                <xsl:element name="{name(.)}">
                    <xsl:apply-templates mode="datavalues"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="datavalues"/>
            </xsl:otherwise>
        </xsl:choose>




    </xsl:template>

    <xsl:template match="text()" mode="datavalues"/>

</xsl:stylesheet>
