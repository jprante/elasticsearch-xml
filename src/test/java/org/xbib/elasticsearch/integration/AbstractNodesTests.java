package org.xbib.elasticsearch.integration;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import java.net.URI;
import java.util.Map;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public abstract class AbstractNodesTests {

    private Map<String, Node> nodes = newHashMap();

    private Map<String, Client> clients = newHashMap();

    private Map<String, InetSocketTransportAddress> addresses = newHashMap();

    private Map<String, InetSocketTransportAddress> httpAddresses = newHashMap();

    private Settings defaultSettings = ImmutableSettings
            .settingsBuilder()
            .put("cluster.name", "test-cluster-" + NetworkUtils.getLocalAddress().getHostName())
            .build();

    protected URI getHttpAddressOfNode(String id) {
        InetSocketTransportAddress address = httpAddresses.get(id);
        return URI.create("http://" + address.address().getHostName() + ":" + (address.address().getPort()));
    }

    public Node startNode(String id, Settings settings) {
        Node node = buildNode(id, settings).start();
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
        NodesInfoResponse response = client(id).admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
        NodeInfo nodeInfo = response.iterator().next();
        Object obj = nodeInfo.getTransport().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            addresses.put(id, (InetSocketTransportAddress) obj);
        }
        obj = nodeInfo.getHttp().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            httpAddresses.put(id, (InetSocketTransportAddress) obj);
        }
        return node;
    }

    public Node buildNode(String id, Settings settings) {
        String settingsSource = getClass().getName().replace('.', '/') + ".yml";
        Settings finalSettings = settingsBuilder()
                .loadFromClasspath(settingsSource)
                .put(defaultSettings)
                .put(settings)
                .put("name", id)
                .put("gateway.type", "none")
                .put("cluster.routing.schedule", "50ms")
                .build();
        Node node = nodeBuilder().settings(finalSettings).build();
        nodes.put(id, node);
        clients.put(id, node.client());
        return node;
    }

    public Node node(String id) {
        return nodes.get(id);
    }

    public Client client(String id) {
        return clients.get(id);
    }

    public void closeAllNodes() {
        for (Client client : clients.values()) {
            client.close();
        }
        clients.clear();
        for (Node node : nodes.values()) {
            node.close();
        }
        nodes.clear();
    }
}