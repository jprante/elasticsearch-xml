package org.xbib.elasticsearch.common.xcontent.xml;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.FastStringReader;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.xbib.elasticsearch.common.xcontent.XmlXContentBuilder;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * A XML based content implementation using Jackson XML dataformat
 *
 */
public class XmlXContent implements XContent {

    /*
     *  This is a bit tricky. We want Woodstox. Woodstox is stax2-api, and can indent XML. We package it in the plugin.
     *  But how can we force to load Woodstox instead of internal com.sun JDK streaming XML parser?
     *
     *  1) Elasticsearch runs the JVM in a "parent" classpath and a plugin in a separate "child" classpath.
     *  2) the Java XML API under XMLInputFactory.newInstance() / XMLOutputFactory.newInstance()
     *  uses a mixture of META-INF/services and system property configuration.
     *  Both work only on Elasticsearch parent classloader. They can not use resources inside a plugin.
     *  So, XML factory lookup does only work on the ES "lib" folder.
     *  3) com.fasterxml.jackson.dataformat.xml.XmlFactory creates internal XMLStreamWriter instances to create an
     *  indenting XML stream in the generator. We need XMLStreamWriter2 for indentation.
     *  But XmlFactory uses the ES parent class loader because it's JDK code in javax.xml.stream that tries to load it.
     *  Therefore, XML indentation crashes with:
     *  java.lang.UnsupportedOperationException: Not implemented
     *  at org.codehaus.stax2.ri.Stax2WriterAdapter.writeRaw(Stax2WriterAdapter.java:380)
     *  because it only sees the Stax API implementation of the JDK, not Woodstox of the plugin.
     *  4) We can work around it by:
     *     a) setting system properties in this static initializer (as early as possible)
     *     b) use direct initialization of WstxInputFactory / WstxOutputFactory, no javax.xml.stream class loading
     */
    static {
        System.setProperty("javax.xml.stream.XMLInputFactory", WstxInputFactory.class.getName());
        System.setProperty("javax.xml.stream.XMLOutputFactory", WstxOutputFactory.class.getName());

        XMLInputFactory inputFactory = new WstxInputFactory(); // do not use  XMLInputFactory.newInstance()
        inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);

        XMLOutputFactory outputFactory = new WstxOutputFactory(); // do not use  XMLOutputFactory.newInstance()
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);

        xmlFactory = new XmlFactory(inputFactory, outputFactory);

        xmlXContent = new XmlXContent();
    }

    public static XmlXContentBuilder contentBuilder() throws IOException {
        return XmlXContentBuilder.builder(xmlXContent);
    }

    public static XmlXContentBuilder contentBuilder(XmlXParams params) throws IOException {
        XmlXContentBuilder builder = XmlXContentBuilder.builder(xmlXContent);
        ((XmlXContentGenerator) builder.generator()).setParams(params);
        return builder;
    }

    private final static XmlFactory xmlFactory;

    private final static XmlXContent xmlXContent;

    private XmlXContent() {

    }

    @Override
    public XContentType type() {
        //return XmlXContentType.XML;
        return null;
    }

    public static XmlXContent xmlXContent() {
        return xmlXContent;
    }

    protected static XmlFactory xmlFactory() {
        return xmlFactory;
    }

    @Override
    public byte streamSeparator() {
        throw new UnsupportedOperationException("xml does not support stream parsing...");
    }

    @Override
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new XmlXContentGenerator(xmlFactory.createGenerator(os, JsonEncoding.UTF8));
    }

    @Override
    public XContentGenerator createGenerator(OutputStream os, String[] filters) throws IOException {
        // ignore filters (for now)
        return new XmlXContentGenerator(xmlFactory.createGenerator(os, JsonEncoding.UTF8));
    }

    @Override
    public XContentParser createParser(String content) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(new FastStringReader(content)));
    }

    @Override
    public XContentParser createParser(InputStream is) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(is));
    }

    @Override
    public XContentParser createParser(byte[] data) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(data));
    }

    @Override
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(data, offset, length));
    }

    @Override
    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }

    @Override
    public XContentParser createParser(Reader reader) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(reader));
    }
}
