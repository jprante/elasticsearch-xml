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

import java.util.Arrays;

/**
 * This class defines the basic properties of characters in XML 1.1. The data
 * in this class can be used to verify that a character is a valid
 * XML 1.1 character or if the character is a space, name start, or name
 * character.
 * <p>
 * A series of convenience methods are supplied to ease the burden
 * of the developer.  Using the character as an index into the <code>XML11CHARS</code>
 * array and applying the appropriate mask flag (e.g.
 * <code>MASK_VALID</code>), yields the same results as calling the
 * convenience methods. There is one exception: check the comments
 * for the <code>isValid</code> method for details.
 *
 * Taken from org.apache.xerces.util.XML11Char.java
 *
 */
public class XML11Char {

    /** Character flags for XML 1.1. */
    private static final byte XML11CHARS [] = new byte [1 << 16];

    /** XML 1.1 Valid character mask. */
    public static final int MASK_XML11_VALID = 0x01;

    /** XML 1.1 Space character mask. */
    public static final int MASK_XML11_SPACE = 0x02;

    /** XML 1.1 Name start character mask. */
    public static final int MASK_XML11_NAME_START = 0x04;

    /** XML 1.1 Name character mask. */
    public static final int MASK_XML11_NAME = 0x08;

    /** XML 1.1 control character mask */
    public static final int MASK_XML11_CONTROL = 0x10;

    /** XML 1.1 content for external entities (valid - "special" chars - control chars) */
    public static final int MASK_XML11_CONTENT = 0x20;

    /** XML namespaces 1.1 NCNameStart */
    public static final int MASK_XML11_NCNAME_START = 0x40;

    /** XML namespaces 1.1 NCName */
    public static final int MASK_XML11_NCNAME = 0x80;
    
    /** XML 1.1 content for internal entities (valid - "special" chars) */
    public static final int MASK_XML11_CONTENT_INTERNAL = MASK_XML11_CONTROL | MASK_XML11_CONTENT; 

    static {
        Arrays.fill(XML11CHARS, 1, 9, (byte) 17 ); // Fill 8 of value (byte) 17
        XML11CHARS[9] = 35;
        XML11CHARS[10] = 3;
        Arrays.fill(XML11CHARS, 11, 13, (byte) 17 ); // Fill 2 of value (byte) 17
        XML11CHARS[13] = 3;
        Arrays.fill(XML11CHARS, 14, 32, (byte) 17 ); // Fill 18 of value (byte) 17
        XML11CHARS[32] = 35;
        Arrays.fill(XML11CHARS, 33, 38, (byte) 33 ); // Fill 5 of value (byte) 33
        XML11CHARS[38] = 1;
        Arrays.fill(XML11CHARS, 39, 45, (byte) 33 ); // Fill 6 of value (byte) 33
        Arrays.fill(XML11CHARS, 45, 47, (byte) -87 ); // Fill 2 of value (byte) -87
        XML11CHARS[47] = 33;
        Arrays.fill(XML11CHARS, 48, 58, (byte) -87 ); // Fill 10 of value (byte) -87
        XML11CHARS[58] = 45;
        XML11CHARS[59] = 33;
        XML11CHARS[60] = 1;
        Arrays.fill(XML11CHARS, 61, 65, (byte) 33 ); // Fill 4 of value (byte) 33
        Arrays.fill(XML11CHARS, 65, 91, (byte) -19 ); // Fill 26 of value (byte) -19
        Arrays.fill(XML11CHARS, 91, 93, (byte) 33 ); // Fill 2 of value (byte) 33
        XML11CHARS[93] = 1;
        XML11CHARS[94] = 33;
        XML11CHARS[95] = -19;
        XML11CHARS[96] = 33;
        Arrays.fill(XML11CHARS, 97, 123, (byte) -19 ); // Fill 26 of value (byte) -19
        Arrays.fill(XML11CHARS, 123, 127, (byte) 33 ); // Fill 4 of value (byte) 33
        Arrays.fill(XML11CHARS, 127, 133, (byte) 17 ); // Fill 6 of value (byte) 17
        XML11CHARS[133] = 35;
        Arrays.fill(XML11CHARS, 134, 160, (byte) 17 ); // Fill 26 of value (byte) 17
        Arrays.fill(XML11CHARS, 160, 183, (byte) 33 ); // Fill 23 of value (byte) 33
        XML11CHARS[183] = -87;
        Arrays.fill(XML11CHARS, 184, 192, (byte) 33 ); // Fill 8 of value (byte) 33
        Arrays.fill(XML11CHARS, 192, 215, (byte) -19 ); // Fill 23 of value (byte) -19
        XML11CHARS[215] = 33;
        Arrays.fill(XML11CHARS, 216, 247, (byte) -19 ); // Fill 31 of value (byte) -19
        XML11CHARS[247] = 33;
        Arrays.fill(XML11CHARS, 248, 768, (byte) -19 ); // Fill 520 of value (byte) -19
        Arrays.fill(XML11CHARS, 768, 880, (byte) -87 ); // Fill 112 of value (byte) -87
        Arrays.fill(XML11CHARS, 880, 894, (byte) -19 ); // Fill 14 of value (byte) -19
        XML11CHARS[894] = 33;
        Arrays.fill(XML11CHARS, 895, 8192, (byte) -19 ); // Fill 7297 of value (byte) -19
        Arrays.fill(XML11CHARS, 8192, 8204, (byte) 33 ); // Fill 12 of value (byte) 33
        Arrays.fill(XML11CHARS, 8204, 8206, (byte) -19 ); // Fill 2 of value (byte) -19
        Arrays.fill(XML11CHARS, 8206, 8232, (byte) 33 ); // Fill 26 of value (byte) 33
        XML11CHARS[8232] = 35;
        Arrays.fill(XML11CHARS, 8233, 8255, (byte) 33 ); // Fill 22 of value (byte) 33
        Arrays.fill(XML11CHARS, 8255, 8257, (byte) -87 ); // Fill 2 of value (byte) -87
        Arrays.fill(XML11CHARS, 8257, 8304, (byte) 33 ); // Fill 47 of value (byte) 33
        Arrays.fill(XML11CHARS, 8304, 8592, (byte) -19 ); // Fill 288 of value (byte) -19
        Arrays.fill(XML11CHARS, 8592, 11264, (byte) 33 ); // Fill 2672 of value (byte) 33
        Arrays.fill(XML11CHARS, 11264, 12272, (byte) -19 ); // Fill 1008 of value (byte) -19
        Arrays.fill(XML11CHARS, 12272, 12289, (byte) 33 ); // Fill 17 of value (byte) 33
        Arrays.fill(XML11CHARS, 12289, 55296, (byte) -19 ); // Fill 43007 of value (byte) -19
        Arrays.fill(XML11CHARS, 57344, 63744, (byte) 33 ); // Fill 6400 of value (byte) 33
        Arrays.fill(XML11CHARS, 63744, 64976, (byte) -19 ); // Fill 1232 of value (byte) -19
        Arrays.fill(XML11CHARS, 64976, 65008, (byte) 33 ); // Fill 32 of value (byte) 33
        Arrays.fill(XML11CHARS, 65008, 65534, (byte) -19 ); // Fill 526 of value (byte) -19

    }

    /**
     * Returns true if the specified character is a space character
     * as amdended in the XML 1.1 specification.
     *
     * @param c The character to check.
     */
    public static boolean isXML11Space(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_SPACE) != 0);
    }

    /**
     * Returns true if the specified character is valid. This method
     * also checks the surrogate character range from 0x10000 to 0x10FFFF.
     * <p>
     * If the program chooses to apply the mask directly to the
     * <code>XML11CHARS</code> array, then they are responsible for checking
     * the surrogate character range.
     *
     * @param c The character to check.
     */
    public static boolean isXML11Valid(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_VALID) != 0) 
                || (0x10000 <= c && c <= 0x10FFFF);
    }

    /**
     * Returns true if the specified character is invalid.
     *
     * @param c The character to check.
     */
    public static boolean isXML11Invalid(int c) {
        return !isXML11Valid(c);
    }

    /**
     * Returns true if the specified character is valid and permitted outside
     * of a character reference.  
     * That is, this method will return false for the same set as
     * isXML11Valid, except it also reports false for "control characters".
     *
     * @param c The character to check.
     */
    public static boolean isXML11ValidLiteral(int c) {
        return ((c < 0x10000 && ((XML11CHARS[c] & MASK_XML11_VALID) != 0 && (XML11CHARS[c] & MASK_XML11_CONTROL) == 0))
            || (0x10000 <= c && c <= 0x10FFFF)); 
    }

    /**
     * Returns true if the specified character can be considered 
     * content in an external parsed entity.
     *
     * @param c The character to check.
     */
    public static boolean isXML11Content(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_CONTENT) != 0) ||
               (0x10000 <= c && c <= 0x10FFFF);
    }
    
    /**
     * Returns true if the specified character can be considered 
     * content in an internal parsed entity.
     *
     * @param c The character to check.
     */
    public static boolean isXML11InternalEntityContent(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_CONTENT_INTERNAL) != 0) ||
               (0x10000 <= c && c <= 0x10FFFF);
    }

    /**
     * Returns true if the specified character is a valid name start
     * character as defined by production [4] in the XML 1.1
     * specification.
     *
     * @param c The character to check.
     */
    public static boolean isXML11NameStart(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NAME_START) != 0)
            || (0x10000 <= c && c < 0xF0000);
    }

    /**
     * Returns true if the specified character is a valid name
     * character as defined by production [4a] in the XML 1.1
     * specification.
     *
     * @param c The character to check.
     */
    public static boolean isXML11Name(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NAME) != 0) 
            || (c >= 0x10000 && c < 0xF0000);
    }

    /**
     * Returns true if the specified character is a valid NCName start
     * character as defined by production [4] in Namespaces in XML
     * 1.1 recommendation.
     *
     * @param c The character to check.
     */
    public static boolean isXML11NCNameStart(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NCNAME_START) != 0)
            || (0x10000 <= c && c < 0xF0000);
    }

    /**
     * Returns true if the specified character is a valid NCName
     * character as defined by production [5] in Namespaces in XML
     * 1.1 recommendation.
     *
     * @param c The character to check.
     */
    public static boolean isXML11NCName(int c) {
        return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NCNAME) != 0)
            || (0x10000 <= c && c < 0xF0000);
    }
    
    /**
     * Returns whether the given character is a valid 
     * high surrogate for a name character. This includes
     * all high surrogates for characters [0x10000-0xEFFFF].
     * In other words everything excluding planes 15 and 16.
     *
     * @param c The character to check.
     */
    public static boolean isXML11NameHighSurrogate(int c) {
        return (0xD800 <= c && c <= 0xDB7F);
    }

    /**
     * Check to see if a string is a valid Name
     *
     * @param name string to check
     * @return true if name is a valid Name
     */
    public static boolean isXML11ValidName(String name) {
        int length = name.length();
        if (length == 0)
            return false;
        int i = 1;
        char ch = name.charAt(0);
        if( !isXML11NameStart(ch) ) {
            if ( length > 1 && isXML11NameHighSurrogate(ch) ) {
                char ch2 = name.charAt(1);
                if ( !XMLChar.isLowSurrogate(ch2) || 
                     !isXML11NameStart(XMLChar.supplemental(ch, ch2)) ) {
                    return false;
                }
                i = 2;
            }
            else {
                return false;
            }
        }
        while (i < length) {
            ch = name.charAt(i);
            if ( !isXML11Name(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = name.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            ++i;
        }
        return true;
    }
    

    /**
     * Check to see if a string is a valid NCName
     *
     * @param ncName string to check
     * @return true if name is a valid NCName
     */
    public static boolean isXML11ValidNCName(String ncName) {
        int length = ncName.length();
        if (length == 0)
            return false;
        int i = 1;
        char ch = ncName.charAt(0);
        if( !isXML11NCNameStart(ch) ) {
            if ( length > 1 && isXML11NameHighSurrogate(ch) ) {
                char ch2 = ncName.charAt(1);
                if ( !XMLChar.isLowSurrogate(ch2) || 
                     !isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                    return false;
                }
                i = 2;
            }
            else {
                return false;
            }
        }
        while (i < length) {
            ch = ncName.charAt(i);
            if ( !isXML11NCName(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = ncName.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11NCName(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            ++i;
        }
        return true;
    }


    /**
     * Check to see if a string is a valid Nmtoken
     *
     * @param nmtoken string to check
     * @return true if nmtoken is a valid Nmtoken 
     */
    public static boolean isXML11ValidNmtoken(String nmtoken) {
        int length = nmtoken.length();
        if (length == 0)
            return false;
        for (int i = 0; i < length; ++i ) {
            char ch = nmtoken.charAt(i);
            if( !isXML11Name(ch) ) {
                if ( ++i < length && isXML11NameHighSurrogate(ch) ) {
                    char ch2 = nmtoken.charAt(i);
                    if ( !XMLChar.isLowSurrogate(ch2) || 
                         !isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

}
