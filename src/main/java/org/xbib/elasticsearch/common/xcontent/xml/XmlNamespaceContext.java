
package org.xbib.elasticsearch.common.xcontent.xml;

import org.elasticsearch.common.collect.Maps;

import javax.xml.namespace.NamespaceContext;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;

/**
 * Contains a simple context for XML namespaces
 *
 */
public class XmlNamespaceContext implements NamespaceContext {

    private static final String DEFAULT_RESOURCE = "xml-namespaces";

    private final SortedMap<String, String> namespaces = Maps.newTreeMap();

    private final SortedMap<String, Set<String>> prefixes = Maps.newTreeMap();

    protected XmlNamespaceContext() {
    }

    protected XmlNamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    protected static String bundleName() {
        return DEFAULT_RESOURCE;
    }

    /**
     * Empty namespace context.
     *
     * @return a namespace context instance
     */
    public static XmlNamespaceContext newInstance() {
        return new XmlNamespaceContext();
    }

    /**
     * Default namespaces from default resource bundle.
     * @return a namespace context instance
     */
    public static XmlNamespaceContext getDefaultInstance() {
        return newInstance(bundleName());
    }

    /**
     * Namespaces from given resource bundle name.
     * @return a namespace context instance
     */
    public static XmlNamespaceContext newInstance(String bundleName) {
        try {
            return new XmlNamespaceContext(ResourceBundle.getBundle(bundleName));
        } catch (MissingResourceException e) {
            return new XmlNamespaceContext();
        }
    }

    public final synchronized void addNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
        if (prefixes.containsKey(namespace)) {
            prefixes.get(namespace).add(prefix);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(prefix);
            prefixes.put(namespace, set);
        }
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            return null;
        }
        return namespaces.containsKey(prefix) ? namespaces.get(prefix) : null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        Iterator<String> it = getPrefixes(namespaceURI);
        return it != null && it.hasNext() ? it.next() : null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace URI cannot be null");
        }
        return prefixes.containsKey(namespace) ?
                prefixes.get(namespace).iterator() : null;
    }

    public String toString() {
        return namespaces.toString();
    }

}
