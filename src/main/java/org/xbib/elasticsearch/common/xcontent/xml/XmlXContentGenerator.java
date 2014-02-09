/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.elasticsearch.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentType;
import org.xbib.elasticsearch.common.xcontent.XmlXContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentString;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * Content generator for XML format
 *
 */
public class XmlXContentGenerator implements XContentGenerator {

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

    public XmlXParams getParams() {
        return params;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return params.getNamespaceContext();
    }

    @Override
    public XContentType contentType() {
        // TODO add XML content type to Elasticsearch core code
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
            if (!started) {
                for (String prefix : getNamespaceContext().getNamespaces().keySet()) {
                    generator.getStaxWriter().writeNamespace(prefix, getNamespaceContext().getNamespaceURI(prefix));
                }
                started = true;
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
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
        generator.writeString(text);
        if (context && prefix != null) {
            try {
                params.getNamespaceContext().addNamespace(prefix, text);
                generator.getStaxWriter().writeNamespace(prefix, text);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            prefix = null;
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        generator.writeString(text, offset, len);
        if (context && prefix != null) {
            try {
                String s = new String(text, offset, len);
                params.getNamespaceContext().addNamespace(prefix, s);
                generator.getStaxWriter().writeNamespace(prefix, s);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            prefix = null;
        }
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        generator.writeUTF8String(text, offset, length);
        if (context && prefix != null) {
            try {
                String s = new String(text, offset, length);
                params.getNamespaceContext().addNamespace(prefix, s);
                generator.getStaxWriter().writeNamespace(prefix, s);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            prefix = null;
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
        generator.writeStringField(fieldName, value);
        if (context && value != null) {
            try {
                params.getNamespaceContext().addNamespace(fieldName, value);
                generator.getStaxWriter().writeNamespace(fieldName, value);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
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
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldNameXml(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldNameXml(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldNameXml(fieldName);
        JsonParser parser;
        if (content.hasArray()) {
            parser = XmlXContent.xmlFactory().createParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = XmlXContent.xmlFactory().createParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldNameXml(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content, offset, length);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void copyCurrentStructure(XContentParser parser) throws IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof XmlXContentParser) {
            generator.copyCurrentStructure(((XmlXContentParser) parser).parser);
        } else {
            XmlXContentHelper.copyCurrentStructure(this, parser);
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
            nsURI = context.getNamespaceURI(nsPrefix);
            if (nsURI == null) {
                throw new IOException("unknown namespace prefix: " + nsPrefix);
            }
            name = name.substring(pos + 1);
        }
        return new QName(nsURI, name, nsPrefix);
    }


}
