
package org.xbib.elasticsearch.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.AbstractXContentParser;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 *
 */
public class XmlXContentParser extends AbstractXContentParser {

    final JsonParser parser;

    public XmlXContentParser(JsonParser parser) {
        this.parser = parser;
    }


    @Override
    public XContentType contentType() {
        //return XmlXContentType.XML;
        return null;
    }


    @Override
    public XContentParser.Token nextToken() throws IOException {
        return convertToken(parser.nextToken());
    }


    @Override
    public void skipChildren() throws IOException {
        parser.skipChildren();
    }


    @Override
    public XContentParser.Token currentToken() {
        return convertToken(parser.getCurrentToken());
    }


    @Override
    public XContentParser.NumberType numberType() throws IOException {
        return convertNumberType(parser.getNumberType());
    }


    @Override
    public boolean estimatedNumberType() {
        return true;
    }


    @Override
    public String currentName() throws IOException {
        return parser.getCurrentName();
    }


    @Override
    protected boolean doBooleanValue() throws IOException {
        return parser.getBooleanValue();
    }


    @Override
    public String text() throws IOException {
        return parser.getText();
    }

    @Override
    public BytesRef utf8Bytes() throws IOException {
        return new BytesRef(CharBuffer.wrap(parser.getTextCharacters(), parser.getTextOffset(), parser.getTextLength()));
    }

    @Override
    public Object objectText() throws IOException {
        JsonToken currentToken = parser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            return text();
        } else if (currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getNumberValue();
        } else if (currentToken == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        } else if (currentToken == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        } else if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        } else {
            return text();
        }
    }

    @Override
    public Object objectBytes() throws IOException {
        JsonToken currentToken = parser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            return bytes();
        } else if (currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getNumberValue();
        } else if (currentToken == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        } else if (currentToken == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        } else if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        } else {
            return bytes();
        }
    }

    @Override
    public boolean hasTextCharacters() {
        return parser.hasTextCharacters();
    }


    @Override
    public char[] textCharacters() throws IOException {
        return parser.getTextCharacters();
    }


    @Override
    public int textLength() throws IOException {
        return parser.getTextLength();
    }


    @Override
    public int textOffset() throws IOException {
        return parser.getTextOffset();
    }


    @Override
    public Number numberValue() throws IOException {
        return parser.getNumberValue();
    }


    @Override
    public short doShortValue() throws IOException {
        return parser.getShortValue();
    }


    @Override
    public int doIntValue() throws IOException {
        return parser.getIntValue();
    }


    @Override
    public long doLongValue() throws IOException {
        return parser.getLongValue();
    }


    @Override
    public float doFloatValue() throws IOException {
        return parser.getFloatValue();
    }


    @Override
    public double doDoubleValue() throws IOException {
        return parser.getDoubleValue();
    }


    @Override
    public byte[] binaryValue() throws IOException {
        return parser.getBinaryValue();
    }


    @Override
    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private NumberType convertNumberType(JsonParser.NumberType numberType) {
        switch (numberType) {
            case INT:
                return NumberType.INT;
            case LONG:
                return NumberType.LONG;
            case FLOAT:
                return NumberType.FLOAT;
            case DOUBLE:
                return NumberType.DOUBLE;
        }
        throw new IllegalStateException("No matching token for number_type [" + numberType + "]");
    }

    private Token convertToken(JsonToken token) {
        if (token == null) {
            return null;
        }
        switch (token) {
            case FIELD_NAME:
                return Token.FIELD_NAME;
            case VALUE_FALSE:
            case VALUE_TRUE:
                return Token.VALUE_BOOLEAN;
            case VALUE_STRING:
                return Token.VALUE_STRING;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return Token.VALUE_NUMBER;
            case VALUE_NULL:
                return Token.VALUE_NULL;
            case START_OBJECT:
                return Token.START_OBJECT;
            case END_OBJECT:
                return Token.END_OBJECT;
            case START_ARRAY:
                return Token.START_ARRAY;
            case END_ARRAY:
                return Token.END_ARRAY;
            case VALUE_EMBEDDED_OBJECT:
                return Token.VALUE_EMBEDDED_OBJECT;
        }
        throw new IllegalStateException("No matching token for json_token [" + token + "]");
    }
}