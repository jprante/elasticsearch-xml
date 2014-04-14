
package org.xbib.elasticsearch.common.xcontent.xml;

import javax.xml.namespace.QName;

/**
 * XML parameters for XML XContent
 */
public class XmlXParams {

    public final static QName DEFAULT_ROOT = new QName("http://elasticsearch.org/ns/1.0/", "root", "es");

    private final QName root;

    private XmlNamespaceContext namespaceContext;

    public XmlXParams() {
        this(null, null);
    }

    public XmlXParams(XmlNamespaceContext namespaceContext) {
        this(null, namespaceContext);
    }

    public XmlXParams(QName root, XmlNamespaceContext namespaceContext) {
        this.root = root != null ? root : DEFAULT_ROOT;
        if (namespaceContext == null) {
            namespaceContext = XmlNamespaceContext.getDefaultInstance();
            namespaceContext.addNamespace(DEFAULT_ROOT.getPrefix(), DEFAULT_ROOT.getNamespaceURI());
        } else {
            namespaceContext.addNamespace(DEFAULT_ROOT.getPrefix(), DEFAULT_ROOT.getNamespaceURI());
            this.namespaceContext = namespaceContext;
        }
    }

    public QName getQName() {
        return root;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

}
