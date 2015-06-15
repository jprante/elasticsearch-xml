
package org.xbib.elasticsearch.common.xcontent.xml;

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
import java.io.Writer;

/**
 * A XML based content implementation using Jackson XML dataformat
 *
 */
public class XmlXContent implements XContent {

    private final static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    static {
        inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);

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
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new XmlXContentGenerator(xmlFactory.createGenerator(writer));
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
