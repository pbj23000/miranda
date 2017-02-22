package com.ltsllc.mirada;

import com.ltsllc.miranda.Consumer;
import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.cluster.ClusterFile;
import com.ltsllc.miranda.cluster.LoadMessage;
import com.ltsllc.miranda.file.MirandaProperties;
import com.ltsllc.miranda.node.NodeElement;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ssltest.TestCase;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Clark on 2/20/2017.
 */
public class TestClusterFile extends TestCase {
    private static final String CLUSTER_FILENAME = "testClusterFile";

    private static Logger logger = Logger.getLogger(TestClusterFile.class);

    private ClusterFile clusterFile;

    public ClusterFile getClusterFile() {
        return clusterFile;
    }

    private static final String[] CLUSTER_FILE_CONTENTS = {
            "[",
            "    {",
            "        \"dns\" : \"foo.com\",",
            "        \"ip\" : \"192.168.1.1\",",
            "        \"port\" : 6789,",
            "        \"description\" : \"a ssltest node\",",
            "        \"expires\" : " + Long.MAX_VALUE,
            "    }",
            "]"
    };

    private static final String[] CLUSTER_FILE_CONTENTS2 = {
            "[",
            "    {",
            "        \"dns\" : \"bar.com\",",
            "        \"ip\" : \"192.168.1.2\",",
            "        \"port\" : 6790,",
            "        \"description\" : \"a different ssltest node\",",
            "        \"expires\" : " + Long.MAX_VALUE,
            "    }",
            "]"
    };

    private BlockingQueue<Message> cluster = new LinkedBlockingQueue<Message>();

    public BlockingQueue<Message> getCluster() {
        return cluster;
    }

    public void setupClusterFile() {
        MirandaProperties properties = MirandaProperties.getInstance();
        String filename = properties.getProperty(MirandaProperties.PROPERTY_CLUSTER_FILE);

        deleteFile(filename);

        putFile(filename, CLUSTER_FILE_CONTENTS);

        ClusterFile.initialize(filename, getWriter(), getCluster());
        this.clusterFile = ClusterFile.getInstance();
    }

    @Before
    public void setup() {
        reset();
        setuplog4j();
        setupMirandaProperties();
        setupClusterFile();
    }

    @After
    public void cleanup() {
        deleteFile(CLUSTER_FILENAME);
    }

    @Test
    public void testInitialize() {
        NodeElement nodeElement = new NodeElement("foo.com", "192.168.1.1", 6789, "a ssltest node");
        assert (getClusterFile().contains(nodeElement));
    }

    @Test
    public void testLoad() {
        assert (null != com.ltsllc.miranda.cluster.ClusterFile.getInstance());

        putFile(CLUSTER_FILENAME, CLUSTER_FILE_CONTENTS2);

        LoadMessage loadMessage = new LoadMessage(null, CLUSTER_FILENAME, this);
        Consumer.staticSend(loadMessage, com.ltsllc.miranda.cluster.ClusterFile.getInstance().getQueue());

        pause(125);

        NodeElement nodeElement = new NodeElement("bar.com", "192.168.1.2", 6790, "a different ssltest node");
        com.ltsllc.miranda.cluster.ClusterFile.getInstance().contains(nodeElement);
    }

    @Test
    public void testUpdateNode() {
        NodeElement nodeElement = new NodeElement("foo.com", "192.168.1.2", 6789, "a ssltest node");
        long now = System.currentTimeMillis();
        nodeElement.setLastConnected(now);
        com.ltsllc.miranda.cluster.ClusterFile.getInstance().updateNode(nodeElement);
        List<NodeElement> nodes = com.ltsllc.miranda.cluster.ClusterFile.getInstance().getData();
        for (NodeElement element : nodes) {
            if (element.equals(nodeElement)) {
                assert (element.getLastConnected() == now);
            }
        }
    }


    @Test
    public void testAdd() {
        NodeElement nodeElement = new NodeElement("bar.com", "192.168.1.2", 6790, "a different ssltest node");
        getClusterFile().addNode(nodeElement);

        pause(125);

        assert (getClusterFile().contains(nodeElement));

        logger.info("my writer: " + getWriter() + ", clusterFile writer: " + getClusterFile().getWriterQueue());

        assert (contains(Message.Subjects.Write, getWriter()));
    }
}