package org.xbib.elasticsearch.xml;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class XmlPluginTest extends NodeTestUtils {

    @Test
    public void testHealthResponse() throws Exception {
        InetSocketTransportAddress httpAddress = findHttpAddress(client("1"));
        if (httpAddress == null) {
            throw new IllegalArgumentException("no HTTP address found");
        }
        URL base = new URL("http://" + httpAddress.getHost() + ":" + httpAddress.getPort());
        URL url = new URL(base, "/_cluster/health?xml");

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
        Client client = client("1");
        for (int i = 0; i < 10000; i++) {
            client.index(new IndexRequest("test", "test", Integer.toString(i))
                    .source("{\"random\":\""+randomString(32)+ " " + randomString(32) + "\"}")).actionGet();
        }
        client.admin().indices().refresh(new RefreshRequest("test")).actionGet();
        InetSocketTransportAddress httpAddress = findHttpAddress(client);
        if (httpAddress == null) {
            throw new IllegalArgumentException("no HTTP address found");
        }
        URL base = new URL("http://" + httpAddress.getHost() + ":" + httpAddress.getPort());
        URL url = new URL(base, "/test/test/_search?xml&pretty&size=10000");
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

    public static InetSocketTransportAddress findHttpAddress(Client client) {
        NodesInfoRequestBuilder nodesInfoRequestBuilder = new NodesInfoRequestBuilder(client, NodesInfoAction.INSTANCE);
        nodesInfoRequestBuilder.setHttp(true).setTransport(false);
        NodesInfoResponse response = nodesInfoRequestBuilder.execute().actionGet();
        Object obj = response.iterator().next().getHttp().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            return (InetSocketTransportAddress) obj;
        }
        return null;
    }
}
