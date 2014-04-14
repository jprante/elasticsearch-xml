
package org.xbib.elasticsearch.plugin.xml;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.rest.xml.XmlService;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 * XML plugin
 */
public class XmlPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "xml" + "-"
                + Build.getInstance().getVersion() + "-"
                + Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "XML plugin";
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(XmlService.class);
        return services;
    }

}
