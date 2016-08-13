package org.xbib.elasticsearch.plugin.xml;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.rest.xml.XmlService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * XML plugin
 */
public class XmlPlugin extends Plugin {

    @Override
    public String name() {
        return "xml";
    }

    @Override
    public String description() {
        return "XML plugin";
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        services.add(XmlService.class);
        return services;
    }

}
