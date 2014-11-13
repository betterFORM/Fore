<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (c) 2012. betterFORM Project - http://www.betterform.de
  ~ Licensed under the terms of BSD License
  -->

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="xsl mvn"
    xmlns:mvn="http://maven.apache.org/POM/4.0.0">
    <!--
        author: Joern Turner
        This stylesheet is used to update the dependencies in the Maven pom.xml file when changed in
        build.properties.xml.

    -->
    <xsl:param name="buildprops"/>
    <xsl:param name="module"/>
                 
    <xsl:output method="xml"
                omit-xml-declaration="yes" 
                indent="yes"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>


    <xsl:template match="mvn:project/mvn:artifactId">
        <artifactId><xsl:value-of select="$module"/></artifactId>
    </xsl:template>

    <xsl:template match="mvn:project/mvn:version">
        <version><xsl:value-of select="document($buildprops)/root//app/version/number"/></version>
    </xsl:template>


    <xsl:template match="mvn:project/mvn:dependencies">
        <xsl:message>processing dependencies of module: <xsl:value-of select="$module"/></xsl:message>
        <dependencies  xmlns="http://maven.apache.org/POM/4.0.0">
            <xsl:choose>
                <xsl:when test="not(../mvn:parent)">
                    <!-- no deps for the root pom except from the ones existing in that file -->
                    <xsl:copy-of select="*"/>
                </xsl:when>
                <xsl:when test="$module = document($buildprops)/root/web/app/name">
                    <dependency>
                        <groupId>de.betterform.fore</groupId>
                        <artifactId><xsl:value-of select="document($buildprops)/root/core/app/name"/></artifactId>
                        <version><xsl:value-of select="document($buildprops)/root/app/version/number"/></version>
                    </dependency>
                    <xsl:for-each select="document($buildprops)/root/web//pathelement[@artifactId]">
                        <dependency>
                            <groupId><xsl:value-of select="@groupId | @groupid"/></groupId>
                            <artifactId><xsl:value-of select="@artifactId"/></artifactId>
                            <version><xsl:value-of select="@version"/></version>
                            <xsl:if test="string-length(@scope)&gt; 0"><scope><xsl:value-of select="@scope"/></scope></xsl:if>
                        </dependency>
                    </xsl:for-each>
                    <xsl:call-template name="addCoreDependencies"/>
                </xsl:when>
                <xsl:when test="$module = document($buildprops)/root/betty/app/name">
                    <dependency>
                        <groupId>de.betterform.fore</groupId>
                        <artifactId><xsl:value-of select="document($buildprops)/root/core/app/name"/></artifactId>
                        <version><xsl:value-of select="document($buildprops)/root/app/version/number"/></version>
                    </dependency>

                    <xsl:for-each select="document($buildprops)/root/betty//pathelement[@artifactId]">
                        <dependency>
                            <groupId><xsl:value-of select="@groupId | @groupid"/></groupId>
                            <artifactId><xsl:value-of select="@artifactId"/></artifactId>
                            <version><xsl:value-of select="@version"/></version>
                            <xsl:if test="string-length(@scope)&gt; 0"><scope><xsl:value-of select="@scope"/></scope></xsl:if>
                        </dependency>
                    </xsl:for-each>
                    <xsl:call-template name="addCoreDependencies"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- the core deps -->
                    <xsl:call-template name="addCoreDependencies"/>
                </xsl:otherwise>
            </xsl:choose>
        </dependencies>
    </xsl:template>

    <xsl:template name="addCoreDependencies">
        <xsl:for-each select="document($buildprops)/root/core//pathelement[@artifactId]">
            <dependency>
                <groupId><xsl:value-of select="@groupId | @groupid"/></groupId>
                <artifactId><xsl:value-of select="@artifactId"/></artifactId>
                <version><xsl:value-of select="@version"/></version>
                <xsl:if test="string-length(@scope)&gt; 0"><scope><xsl:value-of select="@scope"/></scope></xsl:if>
                <xsl:if test="@artifactId = 'log4j'">
                    <exclusions>
                        <exclusion>
                            <artifactId>jmxtools</artifactId>
                            <groupId>com.sun.jdmk</groupId>
                        </exclusion>
                        <exclusion>
                            <artifactId>jmxri</artifactId>
                            <groupId>com.sun.jmx</groupId>
                        </exclusion>
                    </exclusions>
                </xsl:if>
            </dependency>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="mvn:project/mvn:parent/mvn:version">
        <version><xsl:value-of select="document($buildprops)/root/app/version/number"/></version>
    </xsl:template>

</xsl:stylesheet>
