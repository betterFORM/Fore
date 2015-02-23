<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (c) 2012. betterFORM Project - http://www.betterform.de
  ~ Licensed under the terms of BSD License
  -->

<!-- $Id: sort-instance.xsl,v 1.4 2006/03/21 19:24:57 uli Exp $ -->
<xsl:stylesheet version="2.0"
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:xhtml="http://www.w3.org/1999/xhtml"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="appContext" select="'/betterform'"/>
    <xsl:output method="xhtml" encoding="UTF-8" indent="yes"/>


    <xsl:param name="errors" />
    <xsl:param name="data" select="''"/>

    <xsl:output method="xhtml" omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:variable name="CR">
        <xsl:text>&#xa;</xsl:text>
    </xsl:variable>

    <xsl:template match="/xhtml:html">
        <xsl:message>errors: <xsl:value-of select="$errors"/></xsl:message>
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="xhtml:head">
        <xsl:copy>
            <xsl:apply-templates/>
            <style type="text/css">
                .required-failed ~ .fore-required:after{
                content:attr(title);
                color:blue;
                }
                .constraint-failed ~ .fore-constraint:after{
                content:attr(title);
                color:orange;
                }
                .datatype-failed ~ .fore-type:after{
                content:attr(title);
                color:darkred;
                }
            </style>
        </xsl:copy>
    </xsl:template>

    <!-- todo: copy old values from form into fields -->
    <xsl:template match="*[@name]">


        <xsl:variable name="currName" select="@name"/>
        <xsl:message><xsl:value-of select="$currName"/></xsl:message>
        <xsl:variable name="matching-errors" select="$errors//errorInfo[ref = $currName]"/>
        <xsl:variable name="classes">
            <xsl:if test="@class">
                <xsl:value-of select="@class"/><xsl:text> </xsl:text>
            </xsl:if>
            <xsl:for-each select="$matching-errors">
                <xsl:value-of select="errorType"/>
                <xsl:if test="position()!=last()"><xsl:text> </xsl:text></xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:message>classes:'<xsl:value-of select="$classes"/>'</xsl:message>
        <!--<xsl:message>value:'<xsl:value-of select="$errors[1]//errorInfo[ref = $currName]/value"/>'</xsl:message>-->

        <xsl:variable name="attrs" select="@*"/>
        <xsl:copy>
            <xsl:copy-of select="@*[not(name()='class') and not(name()='value')]"/>
            <xsl:attribute name="class" select="$classes"/>

<!--
            <xsl:attribute name="value"><xsl:value-of select="$errors//errorInfo[ref = $currName]/value"/></xsl:attribute>
-->

            <!-- pass-through data-values that the user already entered -->
            <xsl:variable name="formData" select="tokenize($data, ';')"/>
            <xsl:variable name="theValue">
                <xsl:for-each select="$formData">
                    <xsl:if test="starts-with(.,$currName)">
                        <xsl:variable name="varname" select="substring-before(.,':')"/>
                        <xsl:if test="$varname = $currName">
                            <xsl:variable name="varValue" select="substring-after(.,':')"/>
                            <xsl:value-of select="$varValue"/>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>
            </xsl:variable>
            <xsl:if test="string-length($theValue) != 0">
                <xsl:attribute name="value" select="$theValue"/>
            </xsl:if>

            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- todo: implement textarea -->
    <xsl:template match="xhtml:textarea[@name]" priority="10">
    </xsl:template>

    <xsl:template match="@*|node()|text()">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
