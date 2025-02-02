<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acoli="http://acoli.cs.uni-frankfurt.de/" version="2.0">

<!-- Better string escape for URI than encode-for-uri: also replaces dots with underscores and escapes backslashes -->
<xsl:function name="acoli:encode-for-uri-rdf">
    <xsl:param name="str"/>
    <xsl:value-of select="replace(encode-for-uri($str), '[.;]+', '_')"/>
</xsl:function>

<xsl:output method="text" indent="no"/>
   
    <xsl:param name="LANG">SET_LANG</xsl:param>
    <xsl:variable name="lexicon">
        <xsl:text>http://linguistic.linkeddata.es/id/apertium/lexicon</xsl:text>
    <xsl:value-of select="upper-case($LANG)"/>
    </xsl:variable>
    
    <xsl:param name="dc_source"/>
    
    <xsl:template match="/">
		<xsl:text disable-output-escaping="yes">@prefix apertium: &lt;http://wiki.apertium.org/wiki/Bidix#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix dc: &lt;http://purl.org/dc/elements/1.1/> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix foaf: &lt;http://xmlns.com/foaf/0.1/> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix lexinfo: &lt;http://www.lexinfo.net/ontology/2.0/lexinfo#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix xsd: &lt;http://www.w3.org/2001/XMLSchema#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix owl: &lt;http://www.w3.org/2002/07/owl#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix ontolex: &lt;http://www.w3.org/ns/lemon/ontolex#> .&#10;</xsl:text>
        <xsl:text disable-output-escaping="yes">@prefix lime: &lt;http://www.w3.org/ns/lemon/lime#> .&#10;</xsl:text>
        <xsl:value-of disable-output-escaping="yes" select="concat('@prefix : &lt;',$lexicon,'/> . &#10;&#10;')"/>
        
        <xsl:value-of select="concat('&lt;',$lexicon,'>')"/>
        <xsl:text> a lime:Lexicon</xsl:text>
        <xsl:text>;&#10; lime:language "</xsl:text>
        <xsl:value-of select="lower-case($LANG)"/>
        <xsl:text>";&#10; </xsl:text>

        <xsl:text> rdfs:label "Apertium </xsl:text>
        <xsl:value-of select="upper-case($LANG)"/>
        <xsl:text> Lexicon"@en;&#10; </xsl:text>
        <xsl:text> rdfs:comment "This is an RDF OntoLex-lemon </xsl:text>
        <xsl:value-of select="upper-case($LANG)"/>
        <xsl:text> lexicon that comes from the original Apertium bilingual dictionaries."@en .&#10; </xsl:text>

        <xsl:for-each select="//e/p/l">
            <xsl:variable name="l">
                <xsl:variable name="tmp">
                    <xsl:for-each select=".//text()">
                        <xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:value-of select="replace(normalize-space($tmp),'&quot;','')"/>
            </xsl:variable>
            <xsl:if test="normalize-space($l)!='' and matches(encode-for-uri($l),'^[_a-zA-Z0-9%].*')">
                <!-- try to get POS in a variety of ways -->
                <!-- note, this one is only to include the tag in the URI, adding to the entry is below -->
                <xsl:variable name="s-raw">
                    <xsl:variable name="pos1" select="s[1]/@n"/>
                    <xsl:variable name="pos2" select="../../par[1]/@n"/>
                    
                    <xsl:choose>
                        <xsl:when test="string-length($pos1)>0">
                            <xsl:value-of select="acoli:encode-for-uri-rdf($pos1)"/>
                        </xsl:when>
                        <xsl:when test="string-length($pos2)>0">
                            <xsl:value-of select="acoli:encode-for-uri-rdf($pos2)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="acoli:encode-for-uri-rdf($pos1)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="s" select="replace($s-raw, '.*__', '')"/>
                <xsl:variable name="lexent" select="concat(':',replace(acoli:encode-for-uri-rdf(concat($l,'-',$s,'-',lower-case($LANG))),'---*','-unkn-'))"/>
                <xsl:variable name="form" select="concat($lexent,'-form')"/>
                
                <xsl:value-of select="concat('&lt;',$lexicon,'>')"/>
                <xsl:text> lime:entry </xsl:text>
                <xsl:value-of select="$lexent"/>
                <xsl:text> . &#10;</xsl:text>
                
                <xsl:value-of select="$lexent"/>
                <xsl:text> a ontolex:LexicalEntry</xsl:text>
                <xsl:for-each select="s/@n|../../par/@n">
                    <xsl:if test="string-length(.)>0">
                        <xsl:text>; &#10; lexinfo:morphosyntacticProperty apertium:</xsl:text>
                        <!-- and for this precaution I want to thank Dutch and it's "'n_een" morphosyntactic property -->
                        <xsl:value-of select="acoli:encode-for-uri-rdf(.)"/>
                    </xsl:if>
                </xsl:for-each>
                <!-- if no pos given, check whether we have some information about the construction that the entry is contained in -->
                <xsl:if test="count(s/@n|../../par/@n)=0">
                    <xsl:for-each select="ancestor::pardef[1]/@n">
                        <xsl:if test="string-length(.)>0">
                            <xsl:text>; &#10; lexinfo:morphosyntacticProperty apertium:</xsl:text>
                            <xsl:value-of select="acoli:encode-for-uri-rdf(.)"/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>

                <!-- if this is a composite tag, add the last part (POS) as a morphosyntactic property -->
                <xsl:if test="contains($s-raw, '__')">
                    <xsl:text>; &#10; lexinfo:morphosyntacticProperty apertium:</xsl:text>
                    <xsl:value-of select="$s"/>
                </xsl:if>
                    
                <xsl:text>; &#10; ontolex:lexicalForm </xsl:text>
                <xsl:value-of select="$form"/>
                <xsl:if test="string-length($dc_source)>0">
                    <xsl:text>; &#10; dc:source </xsl:text>
					<xsl:value-of select="concat('&lt;',$dc_source,'>')"/>
                </xsl:if>
                <xsl:text> . &#10;</xsl:text>
                
                <xsl:value-of select="$form"/>
                <xsl:text> a ontolex:Form</xsl:text>
                <xsl:text>; &#10; ontolex:writtenRep """</xsl:text>
                <xsl:value-of select="replace($l, '\\', '\\\\')"/>
                <xsl:text>"""@</xsl:text>
                <xsl:value-of select="lower-case($LANG)"/>
                <xsl:text> . &#10;&#10;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>