package org.xbib.elasticsearch.common.xcontent;

import com.google.common.base.Charsets;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.BytesStream;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public final class XmlXContentBuilder implements BytesStream {

    public static enum FieldCaseConversion {
        /**
         * No conversion will occur.
         */
        NONE,
        /**
         * Camel Case will be converted to Underscore casing.
         */
        UNDERSCORE,
        /**
         * Underscore will be converted to Camel case.
         */
        CAMELCASE
    }

    public final static DateTimeFormatter defaultDatePrinter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    protected static FieldCaseConversion globalFieldCaseConversion = FieldCaseConversion.NONE;

    public static void globalFieldCaseConversion(FieldCaseConversion globalFieldCaseConversion) {
        XmlXContentBuilder.globalFieldCaseConversion = globalFieldCaseConversion;
    }

    public static XmlXContentBuilder builder(XContent xContent) throws IOException {
        return new XmlXContentBuilder(xContent, new BytesStreamOutput());
    }

    private XContentGenerator generator;

    private final OutputStream bos;

    private FieldCaseConversion fieldCaseConversion = globalFieldCaseConversion;

    private StringBuilder cachedStringBuilder;

    private boolean humanReadable = false;

    /**
     * Constructs a new builder using the provided xcontent and an OutputStream. Make sure
     * to call {@link #close()} when the builder is done with.
     */
    public XmlXContentBuilder(XContent xContent, OutputStream bos) throws IOException {
        this.bos = bos;
        this.generator = xContent.createGenerator(bos);
    }

    public XmlXContentBuilder fieldCaseConversion(FieldCaseConversion fieldCaseConversion) {
        this.fieldCaseConversion = fieldCaseConversion;
        return this;
    }

    public XContentType contentType() {
        return generator.contentType();
    }

    public XmlXContentBuilder prettyPrint() {
        generator.usePrettyPrint();
        return this;
    }

    public XmlXContentBuilder lfAtEnd() {
        generator.usePrintLineFeedAtEnd();
        return this;
    }

    public XmlXContentBuilder humanReadable(boolean humanReadable) {
        this.humanReadable = humanReadable;
        return this;
    }

    public boolean humanReadable() {
        return this.humanReadable;
    }

    // TODO make ToXContent understand XML

    /*public XContentBuilder field(String name, ToXContent xContent) throws IOException {
        field(name);
        xContent.toXContent(this, ToXContent.EMPTY_PARAMS);
        return this;
    }

    public XContentBuilder field(String name, ToXContent xContent, ToXContent.Params params) throws IOException {
        field(name);
        xContent.toXContent(this, params);
        return this;
    }*/

    public XmlXContentBuilder startObject(String name) throws IOException {
        field(name);
        startObject();
        return this;
    }

    public XmlXContentBuilder startObject(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startObject();
        return this;
    }

    public XmlXContentBuilder startObject(XContentBuilderString name) throws IOException {
        field(name);
        startObject();
        return this;
    }

    public XmlXContentBuilder startObject(XContentBuilderString name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startObject();
        return this;
    }

    public XmlXContentBuilder startObject() throws IOException {
        generator.writeStartObject();
        return this;
    }

    public XmlXContentBuilder endObject() throws IOException {
        generator.writeEndObject();
        return this;
    }

    public XmlXContentBuilder array(String name, String... values) throws IOException {
        startArray(name);
        for (String value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder array(XContentBuilderString name, String... values) throws IOException {
        startArray(name);
        for (String value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder array(String name, Object... values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder array(XContentBuilderString name, Object... values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder startArray(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startArray();
        return this;
    }

    public XmlXContentBuilder startArray(String name) throws IOException {
        field(name);
        startArray();
        return this;
    }

    public XmlXContentBuilder startArray(XContentBuilderString name) throws IOException {
        field(name);
        startArray();
        return this;
    }

    public XmlXContentBuilder startArray() throws IOException {
        generator.writeStartArray();
        return this;
    }

    public XmlXContentBuilder endArray() throws IOException {
        generator.writeEndArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name) throws IOException {
        if (fieldCaseConversion == FieldCaseConversion.UNDERSCORE) {
            generator.writeFieldName(name.underscore());
        } else if (fieldCaseConversion == FieldCaseConversion.CAMELCASE) {
            generator.writeFieldName(name.camelCase());
        } else {
            generator.writeFieldName(name.underscore());
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, FieldCaseConversion conversion) throws IOException {
        if (conversion == FieldCaseConversion.UNDERSCORE) {
            generator.writeFieldName(name.underscore());
        } else if (conversion == FieldCaseConversion.CAMELCASE) {
            generator.writeFieldName(name.camelCase());
        } else {
            generator.writeFieldName(name.underscore());
        }
        return this;
    }

    public XmlXContentBuilder field(String name) throws IOException {
        if (fieldCaseConversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toUnderscoreCase(name, cachedStringBuilder);
        } else if (fieldCaseConversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toCamelCase(name, cachedStringBuilder);
        }
        generator.writeFieldName(name);
        return this;
    }

    public XmlXContentBuilder field(String name, FieldCaseConversion conversion) throws IOException {
        if (conversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toUnderscoreCase(name, cachedStringBuilder);
        } else if (conversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toCamelCase(name, cachedStringBuilder);
        }
        generator.writeFieldName(name);
        return this;
    }

    public XmlXContentBuilder field(String name, char[] value, int offset, int length) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value, offset, length);
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, char[] value, int offset, int length) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value, offset, length);
        }
        return this;
    }

    public XmlXContentBuilder field(String name, String value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XmlXContentBuilder field(String name, String value, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, String value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, String value, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XmlXContentBuilder field(String name, Integer value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.intValue());
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Integer value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.intValue());
        }
        return this;
    }

    public XmlXContentBuilder field(String name, int value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, int value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(String name, Long value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.longValue());
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Long value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.longValue());
        }
        return this;
    }

    public XmlXContentBuilder field(String name, long value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, long value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(String name, Float value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.floatValue());
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Float value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value.floatValue());
        }
        return this;
    }

    public XmlXContentBuilder field(String name, float value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, float value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(String name, Double value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Double value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XmlXContentBuilder field(String name, double value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, double value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder field(String name, BigDecimal value) throws IOException {
        return field(name, value, value.scale(), RoundingMode.HALF_UP, true);
    }

    public XmlXContentBuilder field(XContentBuilderString name, BigDecimal value) throws IOException {
        return field(name, value, value.scale(), RoundingMode.HALF_UP, true);
    }

    public XmlXContentBuilder field(String name, BigDecimal value, int scale, RoundingMode rounding, boolean toDouble) throws IOException {
        field(name);
        if (toDouble) {
            try {
                generator.writeNumber(value.setScale(scale, rounding).doubleValue());
            } catch (ArithmeticException e) {
                generator.writeString(value.toEngineeringString());
            }
        } else {
            generator.writeString(value.toEngineeringString());
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, BigDecimal value, int scale, RoundingMode rounding, boolean toDouble) throws IOException {
        field(name);
        if (toDouble) {
            try {
                generator.writeNumber(value.setScale(scale, rounding).doubleValue());
            } catch (ArithmeticException e) {
                generator.writeString(value.toEngineeringString());
            }
        } else {
            generator.writeString(value.toEngineeringString());
        }
        return this;
    }

    public XmlXContentBuilder field(String name, BytesReference value) throws IOException {
        field(name);
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, BytesReference value) throws IOException {
        field(name);
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, BytesRef value) throws IOException {
        field(name);
        generator.writeUTF8String(value.bytes, value.offset, value.length);
        return this;
    }

    public XmlXContentBuilder field(String name, Text value) throws IOException {
        field(name);
        if (value.hasBytes() && value.bytes().hasArray()) {
            generator.writeUTF8String(value.bytes().array(), value.bytes().arrayOffset(), value.bytes().length());
            return this;
        }
        if (value.hasString()) {
            generator.writeString(value.string());
            return this;
        }
        // TODO: TextBytesOptimization we can use a buffer here to convert it? maybe add a request to jackson to support InputStream as well?
        BytesArray bytesArray = value.bytes().toBytesArray();
        generator.writeUTF8String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length());
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Text value) throws IOException {
        field(name);
        if (value.hasBytes() && value.bytes().hasArray()) {
            generator.writeUTF8String(value.bytes().array(), value.bytes().arrayOffset(), value.bytes().length());
            return this;
        }
        if (value.hasString()) {
            generator.writeString(value.string());
            return this;
        }
        // TODO: TextBytesOptimization we can use a buffer here to convert it? maybe add a request to jackson to support InputStream as well?
        BytesArray bytesArray = value.bytes().toBytesArray();
        generator.writeUTF8String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length());
        return this;
    }

    public XmlXContentBuilder field(String name, byte[] value, int offset, int length) throws IOException {
        field(name);
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XmlXContentBuilder field(String name, Map<String, Object> value) throws IOException {
        field(name);
        value(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Map<String, Object> value) throws IOException {
        field(name);
        value(value);
        return this;
    }

    public XmlXContentBuilder field(String name, Iterable value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Iterable value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, String... value) throws IOException {
        startArray(name);
        for (String o : value) {
            value(o);
        }
        endArray();
        return this;
    }


    public XmlXContentBuilder field(XContentBuilderString name, String... value) throws IOException {
        startArray(name);
        for (String o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, Object... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Object... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, int... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, int offset, int length, int... value) throws IOException {
        assert ((offset >= 0) && (value.length > length));
        startArray(name);
        for (int i = offset; i < length; i++) {
            value(value[i]);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, int... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, long... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, long... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, float... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, float... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, double... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, double... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder field(String name, Object value) throws IOException {
        field(name);
        writeValue(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, Object value) throws IOException {
        field(name);
        writeValue(value);
        return this;
    }

    public XmlXContentBuilder value(Object value) throws IOException {
        writeValue(value);
        return this;
    }

    public XmlXContentBuilder field(String name, boolean value) throws IOException {
        field(name);
        generator.writeBoolean(value);
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, boolean value) throws IOException {
        field(name);
        generator.writeBoolean(value);
        return this;
    }

    public XmlXContentBuilder field(String name, byte[] value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeBinary(value);
        }
        return this;
    }

    public XmlXContentBuilder field(XContentBuilderString name, byte[] value) throws IOException {
        field(name);
        return value(value);
    }

    public XmlXContentBuilder field(String name, ReadableInstant date) throws IOException {
        field(name);
        return value(date);
    }

    public XmlXContentBuilder field(XContentBuilderString name, ReadableInstant date) throws IOException {
        field(name);
        return value(date);
    }

    public XmlXContentBuilder field(String name, ReadableInstant date, DateTimeFormatter formatter) throws IOException {
        field(name);
        return value(date, formatter);
    }

    public XmlXContentBuilder field(XContentBuilderString name, ReadableInstant date, DateTimeFormatter formatter) throws IOException {
        field(name);
        return value(date, formatter);
    }

    public XmlXContentBuilder field(String name, Date date) throws IOException {
        field(name);
        return value(date);
    }

    public XmlXContentBuilder field(XContentBuilderString name, Date date) throws IOException {
        field(name);
        return value(date);
    }

    public XmlXContentBuilder field(String name, Date date, DateTimeFormatter formatter) throws IOException {
        field(name);
        return value(date, formatter);
    }

    public XmlXContentBuilder field(XContentBuilderString name, Date date, DateTimeFormatter formatter) throws IOException {
        field(name);
        return value(date, formatter);
    }

    public XmlXContentBuilder nullField(String name) throws IOException {
        generator.writeNullField(name);
        return this;
    }

    public XmlXContentBuilder nullField(XContentBuilderString name) throws IOException {
        field(name);
        generator.writeNull();
        return this;
    }

    public XmlXContentBuilder nullValue() throws IOException {
        generator.writeNull();
        return this;
    }

    public XmlXContentBuilder timeValueField(XContentBuilderString rawFieldName, XContentBuilderString readableFieldName, TimeValue timeValue) throws IOException {
        if (humanReadable) {
            field(readableFieldName, timeValue.toString());
        }
        field(rawFieldName, timeValue.millis());
        return this;
    }

    public XmlXContentBuilder timeValueField(XContentBuilderString rawFieldName, XContentBuilderString readableFieldName, long rawTime) throws IOException {
        if (humanReadable) {
            field(readableFieldName, new TimeValue(rawTime).toString());
        }
        field(rawFieldName, rawTime);
        return this;
    }

    public XmlXContentBuilder byteSizeField(XContentBuilderString rawFieldName, XContentBuilderString readableFieldName, ByteSizeValue byteSizeValue) throws IOException {
        if (humanReadable) {
            field(readableFieldName, byteSizeValue.toString());
        }
        field(rawFieldName, byteSizeValue.bytes());
        return this;
    }

    public XmlXContentBuilder byteSizeField(XContentBuilderString rawFieldName, XContentBuilderString readableFieldName, long rawSize) throws IOException {
        if (humanReadable) {
            field(readableFieldName, new ByteSizeValue(rawSize).toString());
        }
        field(rawFieldName, rawSize);
        return this;
    }

    public XmlXContentBuilder value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.booleanValue());
    }

    public XmlXContentBuilder value(boolean value) throws IOException {
        generator.writeBoolean(value);
        return this;
    }

    public XmlXContentBuilder value(ReadableInstant date) throws IOException {
        return value(date, defaultDatePrinter);
    }

    public XmlXContentBuilder value(ReadableInstant date, DateTimeFormatter dateTimeFormatter) throws IOException {
        if (date == null) {
            return nullValue();
        }
        return value(dateTimeFormatter.print(date));
    }

    public XmlXContentBuilder value(Date date) throws IOException {
        return value(date, defaultDatePrinter);
    }

    public XmlXContentBuilder value(Date date, DateTimeFormatter dateTimeFormatter) throws IOException {
        if (date == null) {
            return nullValue();
        }
        return value(dateTimeFormatter.print(date.getTime()));
    }

    public XmlXContentBuilder value(Integer value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.intValue());
    }

    public XmlXContentBuilder value(int value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder value(Long value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.longValue());
    }

    public XmlXContentBuilder value(long value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder value(Float value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.floatValue());
    }

    public XmlXContentBuilder value(float value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder value(Double value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.doubleValue());
    }

    public XmlXContentBuilder value(double value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XmlXContentBuilder value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeString(value);
        return this;
    }

    public XmlXContentBuilder value(byte[] value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value);
        return this;
    }

    public XmlXContentBuilder value(byte[] value, int offset, int length) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XmlXContentBuilder value(BytesReference value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XmlXContentBuilder value(Text value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        if (value.hasBytes() && value.bytes().hasArray()) {
            generator.writeUTF8String(value.bytes().array(), value.bytes().arrayOffset(), value.bytes().length());
            return this;
        }
        if (value.hasString()) {
            generator.writeString(value.string());
            return this;
        }
        BytesArray bytesArray = value.bytes().toBytesArray();
        generator.writeUTF8String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length());
        return this;
    }

    public XmlXContentBuilder map(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        writeMap(map);
        return this;
    }

    public XmlXContentBuilder value(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        writeMap(map);
        return this;
    }

    public XmlXContentBuilder value(Iterable value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        startArray();
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XmlXContentBuilder copyCurrentStructure(XContentParser parser) throws IOException {
        generator.copyCurrentStructure(parser);
        return this;
    }

    public XmlXContentBuilder flush() throws IOException {
        generator.flush();
        return this;
    }

    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public XContentGenerator generator() {
        return this.generator;
    }

    public OutputStream stream() {
        return this.bos;
    }

    @Override
    public BytesReference bytes() {
        close();
        return ((BytesStream) bos).bytes();
    }

    /**
     * Returns the actual stream used.
     */
    public BytesStream bytesStream() throws IOException {
        close();
        return (BytesStream) bos;
    }

    /**
     * Returns a string representation of the builder (only applicable for text based xcontent).
     */
    public String string() throws IOException {
        close();
        BytesArray bytesArray = bytes().toBytesArray();
        return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), Charsets.UTF_8);
    }

    private void writeMap(Map<String, Object> map) throws IOException {
        generator.writeStartObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            field(entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                generator.writeNull();
            } else {
                writeValue(value);
            }
        }
        generator.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    private void writeValue(Object value) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        Class type = value.getClass();
        if (type == String.class) {
            generator.writeString((String) value);
        } else if (type == Integer.class) {
            generator.writeNumber(((Integer) value).intValue());
        } else if (type == Long.class) {
            generator.writeNumber(((Long) value).longValue());
        } else if (type == Float.class) {
            generator.writeNumber(((Float) value).floatValue());
        } else if (type == Double.class) {
            generator.writeNumber(((Double) value).doubleValue());
        } else if (type == Short.class) {
            generator.writeNumber(((Short) value).shortValue());
        } else if (type == Boolean.class) {
            generator.writeBoolean(((Boolean) value).booleanValue());
        } else if (type == GeoPoint.class) {
            generator.writeStartObject();
            generator.writeNumberField("lat", ((GeoPoint) value).lat());
            generator.writeNumberField("lon", ((GeoPoint) value).lon());
            generator.writeEndObject();
        } else if (value instanceof Map) {
            writeMap((Map) value);
        } else if (value instanceof Iterable) {
            generator.writeStartArray();
            for (Object v : (Iterable) value) {
                writeValue(v);
            }
            generator.writeEndArray();
        } else if (value instanceof Object[]) {
            generator.writeStartArray();
            for (Object v : (Object[]) value) {
                writeValue(v);
            }
            generator.writeEndArray();
        } else if (type == byte[].class) {
            generator.writeBinary((byte[]) value);
        } else if (value instanceof Date) {
            generator.writeString(XmlXContentBuilder.defaultDatePrinter.print(((Date) value).getTime()));
        } else if (value instanceof Calendar) {
            generator.writeString(XmlXContentBuilder.defaultDatePrinter.print((((Calendar) value)).getTimeInMillis()));
        } else if (value instanceof ReadableInstant) {
            generator.writeString(XmlXContentBuilder.defaultDatePrinter.print((((ReadableInstant) value)).getMillis()));
        } else if (value instanceof BytesReference) {
            BytesReference bytes = (BytesReference) value;
            if (!bytes.hasArray()) {
                bytes = bytes.toBytesArray();
            }
            generator.writeBinary(bytes.array(), bytes.arrayOffset(), bytes.length());
        } else if (value instanceof Text) {
            Text text = (Text) value;
            if (text.hasBytes() && text.bytes().hasArray()) {
                generator.writeUTF8String(text.bytes().array(), text.bytes().arrayOffset(), text.bytes().length());
            } else if (text.hasString()) {
                generator.writeString(text.string());
            } else {
                BytesArray bytesArray = text.bytes().toBytesArray();
                generator.writeUTF8String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length());
            }
        //} else if (value instanceof ToXContent) {
        //    ((ToXContent) value).toXContent(this, ToXContent.EMPTY_PARAMS);
        } else if (value instanceof double[]) {
            generator.writeStartArray();
            for (double v : (double[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof long[]) {
            generator.writeStartArray();
            for (long v : (long[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof int[]) {
            generator.writeStartArray();
            for (int v : (int[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof float[]) {
            generator.writeStartArray();
            for (float v : (float[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof short[]) {
            generator.writeStartArray();
            for (float v : (short[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else {
            // if this is a "value" object, like enum, DistanceUnit, ..., just toString it
            // yea, it can be misleading when toString a Java class, but really, jackson should be used in that case
            generator.writeString(value.toString());
            //throw new ElasticsearchIllegalArgumentException("type not supported for generic value conversion: " + type);
        }
    }
}
