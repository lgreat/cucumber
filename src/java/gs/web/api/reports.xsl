<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:output method='html' version='1.0' encoding='utf-8' indent='no'/>
    <xsl:output indent="yes"/>
    <xsl:template match="/reportResults">
        <div class="report">
            <xsl:apply-templates select="reportResult" />
        </div>
    </xsl:template>

    <xsl:template match="reportResult">
        <div class="result">
            <xsl:apply-templates select="field" />
        </div>
    </xsl:template>

    <xsl:template match="field">
        <div class="field">
            <span>
                <xsl:attribute name="class">
                    <xsl:value-of select="@type" />
                </xsl:attribute>
                <xsl:value-of select="value"/>
            </span>
        </div>
    </xsl:template>
</xsl:stylesheet>