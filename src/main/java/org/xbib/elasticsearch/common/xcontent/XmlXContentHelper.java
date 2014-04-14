
package org.xbib.elasticsearch.common.xcontent;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.base.Charsets;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.compress.CompressedStreamInput;
import org.elasticsearch.common.compress.Compressor;
import org.elasticsearch.common.compress.CompressorFactory;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentParser;

import org.xbib.elasticsearch.common.xcontent.xml.XmlXParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class XmlXContentHelper {

    public static XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        Compressor compressor = CompressorFactory.compressor(bytes);
        if (compressor != null) {
            CompressedStreamInput compressedInput = compressor.streamInput(bytes.streamInput());
            XmlXContentType contentType = XmlXContentFactory.xContentType(compressedInput);
            compressedInput.resetToBufferStart();
            return XmlXContentFactory.xContent(contentType).createParser(compressedInput);
        } else {
            return XmlXContentFactory.xContent(bytes).createParser(bytes.streamInput());
        }
    }

    public static XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        Compressor compressor = CompressorFactory.compressor(data, offset, length);
        if (compressor != null) {
            CompressedStreamInput compressedInput = compressor.streamInput(new BytesStreamInput(data, offset, length, false));
            XmlXContentType contentType = XmlXContentFactory.xContentType(compressedInput);
            compressedInput.resetToBufferStart();
            return XmlXContentFactory.xContent(contentType).createParser(compressedInput);
        } else {
            return XmlXContentFactory.xContent(data, offset, length).createParser(data, offset, length);
        }
    }

    public static Tuple<XmlXContentType, Map<String, Object>> convertToMap(BytesReference bytes, boolean ordered)
            throws ElasticsearchParseException {
        if (bytes.hasArray()) {
            return convertToMap(bytes.array(), bytes.arrayOffset(), bytes.length(), ordered);
        }
        try {
            XContentParser parser;
            XmlXContentType contentType;
            Compressor compressor = CompressorFactory.compressor(bytes);
            if (compressor != null) {
                CompressedStreamInput compressedStreamInput = compressor.streamInput(bytes.streamInput());
                contentType = XmlXContentFactory.xContentType(compressedStreamInput);
                compressedStreamInput.resetToBufferStart();
                parser = XmlXContentFactory.xContent(contentType).createParser(compressedStreamInput);
            } else {
                contentType = XmlXContentFactory.xContentType(bytes);
                parser = XmlXContentFactory.xContent(contentType).createParser(bytes.streamInput());
            }
            if (ordered) {
                return Tuple.tuple(contentType, parser.mapOrderedAndClose());
            } else {
                return Tuple.tuple(contentType, parser.mapAndClose());
            }
        } catch (IOException e) {
            throw new ElasticsearchParseException("Failed to parse content to map", e);
        }
    }

    public static Tuple<XmlXContentType, Map<String, Object>> convertToMap(byte[] data, boolean ordered)
            throws ElasticsearchParseException {
        return convertToMap(data, 0, data.length, ordered);
    }

    public static Tuple<XmlXContentType, Map<String, Object>> convertToMap(byte[] data, int offset, int length, boolean ordered)
            throws ElasticsearchParseException {
        try {
            XContentParser parser;
            XmlXContentType contentType;
            Compressor compressor = CompressorFactory.compressor(data, offset, length);
            if (compressor != null) {
                CompressedStreamInput compressedStreamInput = compressor.streamInput(new BytesStreamInput(data, offset, length, false));
                contentType = XmlXContentFactory.xContentType(compressedStreamInput);
                compressedStreamInput.resetToBufferStart();
                parser = XmlXContentFactory.xContent(contentType).createParser(compressedStreamInput);
            } else {
                contentType = XmlXContentFactory.xContentType(data, offset, length);
                parser = XmlXContentFactory.xContent(contentType).createParser(data, offset, length);
            }
            if (ordered) {
                return Tuple.tuple(contentType, parser.mapOrderedAndClose());
            } else {
                return Tuple.tuple(contentType, parser.mapAndClose());
            }
        } catch (IOException e) {
            throw new ElasticsearchParseException("Failed to parse content to map", e);
        }
    }

    public static String convertToJson(BytesReference bytes, boolean reformatJson) throws IOException {
        return convertToJson(bytes, reformatJson, false);
    }

    public static String convertToJson(BytesReference bytes, boolean reformatJson, boolean prettyPrint) throws IOException {
        if (bytes.hasArray()) {
            return convertToJson(bytes.array(), bytes.arrayOffset(), bytes.length(), reformatJson, prettyPrint);
        }
        XmlXContentType xmlXContentType = XmlXContentFactory.xContentType(bytes);
        if (xmlXContentType == XmlXContentType.JSON && !reformatJson) {
            BytesArray bytesArray = bytes.toBytesArray();
            return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), Charsets.UTF_8);
        }
        XContentParser parser = null;
        try {
            parser = XmlXContentFactory.xContent(xmlXContentType).createParser(bytes.streamInput());
            parser.nextToken();
            XmlXContentBuilder builder = XmlXContentFactory.jsonBuilder();
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static String convertToJson(byte[] data, int offset, int length, boolean reformatJson) throws IOException {
        return convertToJson(data, offset, length, reformatJson, false);
    }

    public static String convertToJson(byte[] data, int offset, int length, boolean reformatJson, boolean prettyPrint) throws IOException {
        XmlXContentType xmlXContentType = XmlXContentFactory.xContentType(data, offset, length);
        if (xmlXContentType == XmlXContentType.JSON && !reformatJson) {
            return new String(data, offset, length, Charsets.UTF_8);
        }
        XContentParser parser = null;
        try {
            parser = XmlXContentFactory.xContent(xmlXContentType).createParser(data, offset, length);
            parser.nextToken();
            XmlXContentBuilder builder = XmlXContentFactory.jsonBuilder();
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public static String convertToXml(XmlXParams params, byte[] data, int offset, int length) throws IOException {
        return convertToXml(params, data, offset, length, false);
    }

    public static String convertToXml(XmlXParams params, byte[] data, int offset, int length, boolean prettyPrint) throws IOException {
        XmlXContentType xmlXContentType = XmlXContentFactory.xContentType(data, offset, length);
        XContentParser parser = null;
        try {
            parser = XmlXContentFactory.xContent(xmlXContentType).createParser(data, offset, length);
            parser.nextToken();
            XmlXContentBuilder builder = XmlXContentFactory.xmlBuilder(params);
            if (prettyPrint) {
                builder.prettyPrint();
            }
            builder.copyCurrentStructure(parser);
            return builder.string();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    /**
     * Updates the provided changes into the source. If the key exists in the changes, it overrides the one in source
     * unless both are Maps, in which case it recuersively updated it.
     */
    public static void update(Map<String, Object> source, Map<String, Object> changes) {
        for (Map.Entry<String, Object> changesEntry : changes.entrySet()) {
            if (!source.containsKey(changesEntry.getKey())) {
                // safe to copy, change does not exist in source
                source.put(changesEntry.getKey(), changesEntry.getValue());
            } else {
                if (source.get(changesEntry.getKey()) instanceof Map && changesEntry.getValue() instanceof Map) {
                    // recursive merge maps
                    update((Map<String, Object>) source.get(changesEntry.getKey()), (Map<String, Object>) changesEntry.getValue());
                } else {
                    // update the field
                    source.put(changesEntry.getKey(), changesEntry.getValue());
                }
            }
        }
    }

    /**
     * Merges the defaults provided as the second parameter into the content of the first. Only does recursive merge
     * for inner maps.
     */
    @SuppressWarnings({"unchecked"})
    public static void mergeDefaults(Map<String, Object> content, Map<String, Object> defaults) {
        for (Map.Entry<String, Object> defaultEntry : defaults.entrySet()) {
            if (!content.containsKey(defaultEntry.getKey())) {
                // copy it over, it does not exists in the content
                content.put(defaultEntry.getKey(), defaultEntry.getValue());
            } else {
                // in the content and in the default, only merge compound ones (maps)
                if (content.get(defaultEntry.getKey()) instanceof Map && defaultEntry.getValue() instanceof Map) {
                    mergeDefaults((Map<String, Object>) content.get(defaultEntry.getKey()), (Map<String, Object>) defaultEntry.getValue());
                } else if (content.get(defaultEntry.getKey()) instanceof List && defaultEntry.getValue() instanceof List) {
                    List defaultList = (List) defaultEntry.getValue();
                    List contentList = (List) content.get(defaultEntry.getKey());

                    List mergedList = new ArrayList();
                    if (allListValuesAreMapsOfOne(defaultList) && allListValuesAreMapsOfOne(contentList)) {
                        // all are in the form of [ {"key1" : {}}, {"key2" : {}} ], merge based on keys
                        Map<String, Map<String, Object>> processed = Maps.newLinkedHashMap();
                        for (Object o : contentList) {
                            Map<String, Object> map = (Map<String, Object>) o;
                            Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                            processed.put(entry.getKey(), map);
                        }
                        for (Object o : defaultList) {
                            Map<String, Object> map = (Map<String, Object>) o;
                            Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                            if (processed.containsKey(entry.getKey())) {
                                mergeDefaults(processed.get(entry.getKey()), map);
                            }
                        }
                        for (Map<String, Object> map : processed.values()) {
                            mergedList.add(map);
                        }
                    } else {
                        // if both are lists, simply combine them, first the defaults, then the content
                        // just make sure not to add the same value twice
                        mergedList.addAll(defaultList);
                        for (Object o : contentList) {
                            if (!mergedList.contains(o)) {
                                mergedList.add(o);
                            }
                        }
                    }
                    content.put(defaultEntry.getKey(), mergedList);
                }
            }
        }
    }

    private static boolean allListValuesAreMapsOfOne(List list) {
        for (Object o : list) {
            if (!(o instanceof Map)) {
                return false;
            }
            if (((Map) o).size() != 1) {
                return false;
            }
        }
        return true;
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

}
