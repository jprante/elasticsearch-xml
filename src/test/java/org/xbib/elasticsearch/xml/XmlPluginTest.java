package org.xbib.elasticsearch.xml;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.integration.AbstractNodesTests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import static org.testng.AssertJUnit.assertTrue;

public class XmlPluginTest extends AbstractNodesTests {

    private Client client;

    @BeforeClass
    public void createNodes() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("index.number_of_shards", numberOfShards())
                .put("index.number_of_replicas", 0)
                .build();
        for (int i = 0; i < numberOfNodes(); i++) {
            startNode("node" + i, settings);
        }
        client = getClient();
    }

    protected int numberOfShards() {
        return 1;
    }

    protected int numberOfNodes() {
        return 1;
    }

    @AfterClass
    public void closeNodes() {
        client.close();
        closeAllNodes();
    }

    protected Client getClient() {
        return client("node0");
    }

    @Test
    public void testPlugin() throws Exception {
        URL url = new URL(getHttpAddressOfNode("node0").toURL(), "/_cluster/health?xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        if ((line = reader.readLine()) != null) {
            assertTrue(line.startsWith("<root xmlns=\"http://elasticsearch.org/ns/1.0/\">"));
            assertTrue(line.endsWith("</root>"));
        }
        reader.close();
    }
}
