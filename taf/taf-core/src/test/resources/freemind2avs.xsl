<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8"
        indent="yes" omit-xml-declaration="no" />
    <xsl:comment>
        This file has been created with freemind2avs.xsl
    </xsl:comment>

    <!-- the template to output for each node -->
    <xsl:template match="node">
        <story>
            <xsl:attribute name="TITLE">
<xsl:value-of select="@TEXT" />
</xsl:attribute>
            <xsl:for-each select="node">
                <xsl:call-template name="type" />
            </xsl:for-each>
        </story>
    </xsl:template> <!-- xsl:template match="node" -->



    <xsl:template name="type">
        <type>
            <xsl:attribute name="TITLE">
				<xsl:value-of select="@TEXT" />
			</xsl:attribute>
            <xsl:for-each select="node">
                <xsl:call-template name="tc" />
            </xsl:for-each>
        </type>
    </xsl:template>


    <xsl:template name="tc">
        <testcase>
            <xsl:attribute name="TITLE">
	<xsl:value-of select="@TEXT" />
	</xsl:attribute>
            <xsl:strip-space elements="DESCRIPTION PRIORITY" />
            <xsl:for-each select="node">
                <xsl:variable name="title"
                    select="translate(@TEXT,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
                <xsl:choose>
                    <xsl:when test="starts-with($title,'description')">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                description
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'priority' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                priority
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'component' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                component
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'group' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                group
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'pre' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                pre
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'vusers' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                vusers
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'context' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                context
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="starts-with($title, 'atc' )">
                        <xsl:call-template name="field">
                            <xsl:with-param name="name">
                                atc
                            </xsl:with-param>
                            <xsl:with-param name="text"
                                select="@TEXT" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="ap" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="arrowlink">
            </xsl:for-each>
        </testcase>
    </xsl:template>

    <xsl:template name="field">
        <xsl:param name="name" />
        <xsl:param name="text" />
        <xsl:if test="string-length($text) = 0">
            <xsl:text>Looks little more exciting</xsl:text>
        </xsl:if>
        <xsl:element name="{$name}">
            <xsl:variable name="value"
                select="substring($text,string-length($name)+2)" />
            <xsl:value-of select="normalize-space($value)" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="simple-subnode">
        <xsl:choose>
            <xsl:when test="richcontent">
                <xsl:apply-templates
                    select="richcontent[@TYPE='NODE']/html/body" mode="richcontent" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@TEXT" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- xsl:template name="output-nodecontent" -->
    <xsl:template match="body" mode="richcontent">
        <xsl:variable name="text" select="*|text()" />
        <xsl:call-template name="strip-tags">
            <xsl:with-param name="text" select="$text" />
        </xsl:call-template>
    </xsl:template> <!-- xsl:template name="htmlnode" -->

    <xsl:template name="strip-tags">
        <xsl:param name="text" />
        <xsl:choose>
            <xsl:when test="contains($text, '&lt;')">
                <xsl:value-of select="substring-before($text, '&lt;')" />
                <xsl:call-template name="strip-tags">
                    <xsl:with-param name="text"
                        select="substring-after($text, '&gt;')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="ap">
        <actionpoint>
            <description>
                <xsl:call-template name="simple-subnode" />
            </description>
            <xsl:for-each select="node">
                <xsl:call-template name="vp" />
            </xsl:for-each>
        </actionpoint>
    </xsl:template>

    <xsl:template name="vp">
        <verificationpoint>
            <xsl:call-template name="simple-subnode" />
        </verificationpoint>
    </xsl:template>

</xsl:stylesheet>
