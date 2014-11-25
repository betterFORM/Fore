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
    <xsl:param name="data" select="'record:foo;trackedDate:bar;created:heute;project:mine;duration:3;'"/>
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

    <xsl:template match="data">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="datavalues"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*" mode="datavalues">
        <xsl:message>name:<xsl:value-of select="name()"/>::value:<xsl:value-of select="."/></xsl:message>

        <xsl:element name="{name(.)}">
            <xsl:if test="string-length($data) != 0">
                <xsl:variable name="name" select="name(.)"/>
                <xsl:variable name="formData" select="tokenize($data, ';')"/>
                <xsl:variable name="theValue">
                    <xsl:for-each select="$formData">
                        <xsl:if test="starts-with(.,$name)">
                            <xsl:variable name="varname" select="substring-before(.,':')"/>
                            <xsl:if test="$varname = $name">
                                <xsl:variable name="varValue" select="substring-after(.,':')"/>
                                <xsl:value-of select="$varValue"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:if test="string-length($theValue) != 0">
                    <xsl:value-of select="$theValue"/>
                </xsl:if>
            </xsl:if>
            <xsl:apply-templates mode="datavalues"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="text()" mode="datavalues"/>

</xsl:stylesheet>
