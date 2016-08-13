package org.xbib.elasticsearch.xml;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
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
import java.util.Random;

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
    public void testHealthResponse() throws Exception {
        URL url = new URL(getHttpAddressOfNode("node0").toURL(), "/_cluster/health?xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        if ((line = reader.readLine()) != null) {
            assertTrue(line.startsWith("<root xmlns=\"http://elasticsearch.org/ns/1.0/\">"));
            assertTrue(line.endsWith("</root>"));
        }
        reader.close();
    }

    @Test
    public void testBigAndFatResponse() throws Exception {
        Client client = getClient();
        for (int i = 0; i < 10000; i++) {
            client.index(new IndexRequest("test", "test", Integer.toString(i))
                    .source("{\"random\":\""+randomString(32)+ " " + randomString(32) + "\"}")).actionGet();
        }
        client.admin().indices().refresh(new RefreshRequest("test")).actionGet();
        URL url = new URL(getHttpAddressOfNode("node0").toURL(), "/test/test/_search?xml&pretty&size=10000");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            count += line.length();
        }
        assertTrue(count >= 2309156);
        reader.close();
        client.admin().indices().delete(new DeleteIndexRequest("test"));
    }

    private static Random random = new Random();

    private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz").toCharArray();

    protected String randomString(int len) {
        final char[] buf = new char[len];
        final int n = numbersAndLetters.length - 1;
        for (int i = 0; i < buf.length; i++) {
            buf[i] = numbersAndLetters[random.nextInt(n)];
        }
        return new String(buf);
    }
}
