
package org.xbib.elasticsearch.rest.xml;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;

public class XmlService extends AbstractLifecycleComponent<XmlService> {

    private final RestController controller;

    @Inject
    public XmlService(Settings settings, RestController controller) {
        super(settings);
        this.controller = controller;
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        controller.registerFilter(new XmlFilter());
    }

    @Override
    protected void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }
}
