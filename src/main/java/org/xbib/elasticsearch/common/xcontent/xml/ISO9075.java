
package org.xbib.elasticsearch.common.xcontent.xml;

public class ISO9075 {
    private static final int MASK = (1 << 4) - 1;

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private ISO9075() {
    }

    public static String encode(String toEncode) {
        if ((toEncode == null) || (toEncode.length() == 0)) {
            return toEncode;
        } else if (XML11Char.isXML11ValidName(toEncode) && (!toEncode.contains("_x"))) {
            return toEncode;
        } else {
            StringBuilder builder = new StringBuilder(toEncode.length());
            for (int i = 0; i < toEncode.length(); i++) {
                char c = toEncode.charAt(i);
                if (i == 0) {
                    if (XML11Char.isXML11NCNameStart(c)) {
                        if (matchesEncodedPattern(toEncode, i)) {
                            encode('_', builder);
                        } else {
                            builder.append(c);
                        }
                    } else {
                        encode(c, builder);
                    }
                } else if (!XML11Char.isXML11NCName(c)) {
                    encode(c, builder);
                } else {
                    if (matchesEncodedPattern(toEncode, i)) {
                        encode('_', builder);
                    } else {
                        builder.append(c);
                    }
                }
            }
            return builder.toString();
        }
    }

    private static boolean matchesEncodedPattern(String string, int position) {
        return (string.length() >= position + 6)
                && (string.charAt(position) == '_') && (string.charAt(position + 1) == 'x')
                && isHexChar(string.charAt(position + 2)) && isHexChar(string.charAt(position + 3))
                && isHexChar(string.charAt(position + 4)) && isHexChar(string.charAt(position + 5))
                && (string.charAt(position + 6) == '_');
    }

    private static boolean isHexChar(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;
            default:
                return false;
        }
    }

    public static String decode(String toDecode) {
        if ((toDecode == null) || (toDecode.length() < 7) || (!toDecode.contains("_x"))) {
            return toDecode;
        }
        StringBuilder decoded = new StringBuilder();
        for (int i = 0, l = toDecode.length(); i < l; i++) {
            if (matchesEncodedPattern(toDecode, i)) {
                decoded.append(((char) Integer.parseInt(toDecode.substring(i + 2, i + 6), 16)));
                i += 6;
            } else {
                decoded.append(toDecode.charAt(i));
            }
        }
        return decoded.toString();
    }

    private static void encode(char c, StringBuilder builder) {
        char[] buf = new char[]{'_', 'x', '0', '0', '0', '0', '_'};
        int charPos = 6;
        do {
            buf[--charPos] = DIGITS[c & MASK];
            c >>>= 4;
        }
        while (c != 0);
        builder.append(buf);
    }
}