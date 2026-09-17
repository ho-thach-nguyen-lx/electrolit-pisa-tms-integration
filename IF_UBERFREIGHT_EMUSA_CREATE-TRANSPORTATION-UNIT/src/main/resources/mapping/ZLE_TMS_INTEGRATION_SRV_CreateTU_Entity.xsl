<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"
        version="1.0"
        encoding="UTF-8"
        indent="yes" />

    <xsl:template match="/">
        <CreateTUSet>
            <xsl:copy-of select="CreateTU" />
        </CreateTUSet>
    </xsl:template>
</xsl:stylesheet>
