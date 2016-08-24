package org.xbib.elasticsearch.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentString;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Content generator for XML format
 *
 */
public class XmlXContentGenerator implements XContentGenerator {

    private final static ESLogger logger = ESLoggerFactory.getLogger(XmlXContentGenerator.class.getName());

    protected final ToXmlGenerator generator;

    private XmlXParams params;

    private boolean started;

    private boolean context;

    private String prefix;

    public XmlXContentGenerator(ToXmlGenerator generator) {
        this.generator = generator;
        this.params = new XmlXParams();
        this.started = false;
        this.context = false;
        this.prefix = null;
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
    }

    public XmlXContentGenerator setParams(XmlXParams params) {
        this.params = params;
        return this;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return params.getNamespaceContext();
    }

    @Override
    public XContentType contentType() {
        //return XmlXContentType.XML;
        return null;
    }

    @Override
    public void usePrettyPrint() {
        generator.useDefaultPrettyPrinter();
    }

    @Override
    public void usePrintLineFeedAtEnd() {
        // nothing here
    }

    @Override
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        try {
            if (!started) {
                generator.getStaxWriter().setDefaultNamespace(params.getQName().getNamespaceURI());
                generator.startWrappedValue(null, params.getQName());
            }
            generator.writeStartObject();
            if (!started ) {
                if (getNamespaceContext() != null &&  getNamespaceContext().getNamespaces() != null) {
                    for (String prefix : getNamespaceContext().getNamespaces().keySet()) {
                        generator.getStaxWriter().writeNamespace(prefix, getNamespaceContext().getNamespaceURI(prefix));
                    }
                }
                started = true;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
        context = false;
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        writeFieldNameXml(name);
    }

    @Override
    public void writeFieldName(XContentString name) throws IOException {
        writeFieldNameXml(name.getValue());
    }

    @Override
    public void writeString(String text) throws IOException {
        try {
            generator.writeString(text);
            if (context && prefix != null) {
                params.getNamespaceContext().addNamespace(prefix, text);
                generator.getStaxWriter().writeNamespace(prefix, text);
                prefix = null;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage() + ": " + text, e);
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        String s = new String(text, offset, len);
        try {
            generator.writeString(s);
            if (context && prefix != null) {
                params.getNamespaceContext().addNamespace(prefix, s);
                generator.getStaxWriter().writeNamespace(prefix, s);
                prefix = null;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage() + ": " + s, e);
        }
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        String s = new String(text, offset, length);
        try {
            generator.writeUTF8String(text, offset, length);
            if (context && prefix != null) {
                params.getNamespaceContext().addNamespace(prefix, s);
                generator.getStaxWriter().writeNamespace(prefix, s);
                prefix = null;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage() + ": " + s, e);
        }
    }

    @Override
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        generator.writeBinary(data, offset, len);
    }

    @Override
    public void writeBinary(byte[] data) throws IOException {
        generator.writeBinary(data);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(double d) throws IOException {
        generator.writeNumber(d);
    }

    @Override
    public void writeNumber(float f) throws IOException {
        generator.writeNumber(f);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        generator.writeBoolean(state);
    }

    @Override
    public void writeBooleanField(XContentString fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        generator.writeBoolean(value);
    }

    @Override
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    @Override
    public void writeNullField(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        generator.writeNull();
    }

    @Override
    public void writeStringField(String fieldName, String value) throws IOException {
        try {
            generator.writeStringField(fieldName, value);
            if (context && value != null) {
                params.getNamespaceContext().addNamespace(fieldName, value);
                generator.getStaxWriter().writeNamespace(fieldName, value);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage() + ": " + fieldName + "=" + value, e);
        }
    }

    @Override
    public void writeStringField(XContentString fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        generator.writeString(value);
    }

    @Override
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        generator.writeBooleanField(fieldName, value);
    }

    @Override
    public void writeNullField(String fieldName) throws IOException {
        generator.writeNullField(fieldName);
    }

    @Override
    public void writeNumberField(String fieldName, int value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, long value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, double value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(String fieldName, float value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        generator.writeBinaryField(fieldName, data);
    }

    @Override
    public void writeBinaryField(XContentString fieldName, byte[] value) throws IOException {
        writeFieldName(fieldName);
        generator.writeBinary(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    public void writeArrayFieldStart(String fieldName) throws IOException {
        generator.writeArrayFieldStart(fieldName);
    }

    @Override
    public void writeArrayFieldStart(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        generator.writeStartArray();
    }

    public void writeObjectFieldStart(String fieldName) throws IOException {
        generator.writeObjectFieldStart(fieldName);
    }

    @Override
    public void writeObjectFieldStart(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        generator.writeStartObject();
    }

    @Override
    public void writeRawField(String fieldName, InputStream content) throws IOException {
        writeFieldNameXml(fieldName);
        try (JsonParser parser = XmlXContent.xmlFactory().createParser(content)) {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        }
    }

    @Override
    public void writeRawField(String fieldName, BytesReference content) throws IOException {
        writeFieldNameXml(fieldName);
        try (JsonParser parser = XmlXContent.xmlFactory().createParser(content.toBytes())) {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        }
    }

    @Override
    public void writeRawValue(BytesReference content) throws IOException {
        generator.writeRawValue(content.toUtf8());
    }

    @Override
    public void copyCurrentStructure(XContentParser parser) throws IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof XmlXContentParser) {
            generator.copyCurrentStructure(((XmlXContentParser) parser).parser);
        } else {
            copyCurrentStructure(this, parser);
        }
    }

    public static void copyCurrentStructure(XContentGenerator generator, XContentParser parser) throws IOException {
        XContentParser.Token t = parser.currentToken();

        // Let's handle field-name separately first
        if (t == XContentParser.Token.FIELD_NAME) {
            generator.writeFieldName(parser.currentName());
            t = parser.nextToken();
            // fall-through to copy the associated value
        }

        switch (t) {
            case START_ARRAY:
                generator.writeStartArray();
                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                    copyCurrentStructure(generator, parser);
                }
                generator.writeEndArray();
                break;
            case START_OBJECT:
                generator.writeStartObject();
                while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                    copyCurrentStructure(generator, parser);
                }
                generator.writeEndObject();
                break;
            default: // others are simple:
                copyCurrentEvent(generator, parser);
        }
    }

    public static void copyCurrentEvent(XContentGenerator generator, XContentParser parser) throws IOException {
        switch (parser.currentToken()) {
            case START_OBJECT:
                generator.writeStartObject();
                break;
            case END_OBJECT:
                generator.writeEndObject();
                break;
            case START_ARRAY:
                generator.writeStartArray();
                break;
            case END_ARRAY:
                generator.writeEndArray();
                break;
            case FIELD_NAME:
                generator.writeFieldName(parser.currentName());
                break;
            case VALUE_STRING:
                if (parser.hasTextCharacters()) {
                    generator.writeString(parser.textCharacters(), parser.textOffset(), parser.textLength());
                } else {
                    generator.writeString(parser.text());
                }
                break;
            case VALUE_NUMBER:
                switch (parser.numberType()) {
                    case INT:
                        generator.writeNumber(parser.intValue());
                        break;
                    case LONG:
                        generator.writeNumber(parser.longValue());
                        break;
                    case FLOAT:
                        generator.writeNumber(parser.floatValue());
                        break;
                    case DOUBLE:
                        generator.writeNumber(parser.doubleValue());
                        break;
                }
                break;
            case VALUE_BOOLEAN:
                generator.writeBoolean(parser.booleanValue());
                break;
            case VALUE_NULL:
                generator.writeNull();
                break;
            case VALUE_EMBEDDED_OBJECT:
                generator.writeBinary(parser.binaryValue());
        }
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

    private void writeFieldNameXml(String name) throws IOException {
        if (!context) {
            this.context = "@context".equals(name);
            this.prefix = null;
        }
        if (name.startsWith("@")) {
            // setting to attribute is simple but tricky, it allows to declare namespaces in StaX
            generator.setNextIsAttribute(true);
        } else if (context) {
            prefix = name;
        }
        QName qname = toQName(name);
        generator.setNextName(qname);
        generator.writeFieldName(qname.getLocalPart());
    }

    private QName toQName(String name) throws IOException {
        QName root = params.getQName();
        XmlNamespaceContext context = params.getNamespaceContext();
        String nsPrefix = root.getPrefix();
        String nsURI = root.getNamespaceURI();
        if (name.startsWith("_") || name.startsWith("@")) {
            name = name.substring(1);
        }
        name = ISO9075.encode(name);
        int pos = name.indexOf(':');
        if (pos > 0) {
            nsPrefix = name.substring(0, pos);
            nsURI = context != null ? context.getNamespaceURI(nsPrefix) : XmlXParams.DEFAULT_ROOT.getNamespaceURI();
            if (nsURI == null) {
                throw new IOException("unknown namespace prefix: " + nsPrefix);
            }
            name = name.substring(pos + 1);
        }
        return new QName(nsURI, name, nsPrefix);
    }
}
