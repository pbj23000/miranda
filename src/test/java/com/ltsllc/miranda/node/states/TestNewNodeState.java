/*
 * Copyright 2017 Long Term Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ltsllc.miranda.node.states;

import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.State;
import com.ltsllc.miranda.Version;
import com.ltsllc.miranda.cluster.Cluster;
import com.ltsllc.miranda.cluster.messages.ConnectMessage;
import com.ltsllc.miranda.file.GetFileResponseWireMessage;
import com.ltsllc.miranda.node.NameVersion;
import com.ltsllc.miranda.node.messages.GetClusterFileMessage;
import com.ltsllc.miranda.node.messages.GetSubscriptionsFileMessage;
import com.ltsllc.miranda.node.messages.GetTopicsFileMessage;
import com.ltsllc.miranda.node.networkMessages.*;
import com.ltsllc.miranda.subsciptions.SubscriptionsFile;
import com.ltsllc.miranda.topics.TopicsFile;
import com.ltsllc.miranda.user.UsersFile;
import com.ltsllc.miranda.user.messages.GetUsersFileMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

/**
 * Created by Clark on 3/22/2017.
 */
public class TestNewNodeState extends TesterNodeState {
    private NewNodeState newNodeState;

    public NewNodeState getNewNodeState() {
        return newNodeState;
    }

    public void reset () {
        super.reset();

        newNodeState = null;
    }

    @Before
    public void setup () {
        reset();

        super.setup();

        newNodeState = new NewNodeState(getMockNode(), getMockNetwork(), getMockCluster());
    }

    @Test
    public void testProcessJoinWireMessage () {
        JoinResponseWireMessage joinResponseWireMessage = new JoinResponseWireMessage(JoinResponseWireMessage.Responses.Success);
        JoinWireMessage joinWireMessage = new JoinWireMessage("foo.com", "192.168.1.1", 6789, "a node");
        NetworkMessage networkMessage = new NetworkMessage(null, this, joinWireMessage);

        State nextState = getNewNodeState().processMessage(networkMessage);

        verify(getMockNetwork(), atLeastOnce()).sendNetworkMessage(Matchers.any(BlockingQueue.class), Matchers.any(),
                Matchers.anyInt(), Matchers.eq(joinResponseWireMessage));

        assert (nextState instanceof NodeReadyState);
    }

    @Test
    public void testProcessVersionsWireMessage () {
        BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
        setupMockMiranda();

        List<NameVersion> nameVersions = new ArrayList<NameVersion>();
        Version version = new Version();
        version.setSha1("whatever");
        nameVersions.add (new NameVersion(Cluster.NAME, version));
        VersionsWireMessage versionsWireMessage = new VersionsWireMessage(nameVersions);
        NetworkMessage networkMessage = new NetworkMessage(null, this, versionsWireMessage);

        when(getMockMiranda().getQueue()).thenReturn(queue);

        State nextState = getNewNodeState().processMessage(networkMessage);

        assert (nextState instanceof NewNodeState);
        assert (contains(Message.Subjects.Versions, queue));
    }

    @Test
    public void testGetFileResponseWireMessageCluster () {
        setupMockMiranda();
        BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
        setupMockCluster();
        GetFileResponseWireMessage getFileResponseWireMessage = new GetFileResponseWireMessage(Cluster.NAME, "whatever");
        NetworkMessage networkMessage = new NetworkMessage(null, this, getFileResponseWireMessage);

        when(getMockMiranda().getCluster()).thenReturn(getMockCluster());
        when (getMockCluster().getQueue()).thenReturn(queue);

        State nextState = getNewNodeState().processMessage(networkMessage);

        assert (contains(Message.Subjects.GetFileResponse, queue));
        assert (nextState instanceof NewNodeState);
    }

    @Test
    public void testGetFileResponseWireMessageUsers () {
        BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
        setupMockUsersFile();
        GetFileResponseWireMessage getFileResponseWireMessage = new GetFileResponseWireMessage(UsersFile.FILE_NAME, "whatever");
        NetworkMessage networkMessage = new NetworkMessage(null, this, getFileResponseWireMessage);

        when (getMockUsersFile().getQueue()).thenReturn(queue);

        State nextState = getNewNodeState().processMessage(networkMessage);

        assert (contains(Message.Subjects.GetFileResponse, queue));
        assert (nextState instanceof NewNodeState);
    }

    @Test
    public void testGetFileResponseWireMessageTopics () {
        BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
        setupMockTopicsFile();
        GetFileResponseWireMessage getFileResponseWireMessage = new GetFileResponseWireMessage(TopicsFile.FILE_NAME, "whatever");
        NetworkMessage networkMessage = new NetworkMessage(null, this, getFileResponseWireMessage);

        when (getMockTopicsFile().getQueue()).thenReturn(queue);

        State nextState = getNewNodeState().processMessage(networkMessage);

        assert (contains(Message.Subjects.GetFileResponse, queue));
        assert (nextState instanceof NewNodeState);
    }

    @Test
    public void testGetFileResponseWireMessageSubscriptions () {
        BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
        setupMockSubscriptionsFile();
        GetFileResponseWireMessage getFileResponseWireMessage = new GetFileResponseWireMessage(SubscriptionsFile.FILE_NAME, "whatever");
        NetworkMessage networkMessage = new NetworkMessage(null, this, getFileResponseWireMessage);

        when (getMockSubscriptionsFile().getQueue()).thenReturn(queue);

        State nextState = getNewNodeState().processMessage(networkMessage);

        assert (contains(Message.Subjects.GetFileResponse, queue));
        assert (nextState instanceof NewNodeState);
    }


    public void testProcessGetFileMessage (String file) {
        Message message = null;

        if (file.equalsIgnoreCase(UsersFile.FILE_NAME))
            message = new GetUsersFileMessage(null, this);
        else if (file.equalsIgnoreCase(TopicsFile.FILE_NAME))
            message = new GetTopicsFileMessage(null, this);
        else if (file.equalsIgnoreCase(SubscriptionsFile.FILE_NAME))
            message = new GetSubscriptionsFileMessage(null, this);
        else if (file.equalsIgnoreCase(Cluster.NAME))
            message = new GetClusterFileMessage(null, this);
        else {
            System.err.println ("Unrecognized file: " + file);
            System.exit(1);
        }

        State nextState = getNewNodeState().processMessage(message);

        WireMessage wireMessage = new GetFileWireMessage(file);

        verify(getMockNetwork(), atLeastOnce()).sendNetworkMessage(Matchers.any(BlockingQueue.class), Matchers.any(),
                Matchers.anyInt(), Matchers.eq(wireMessage));

        assert (nextState instanceof NewNodeState);
    }

    @Test
    public void testProcessGetUsersFileMessage () {
        testProcessGetFileMessage(UsersFile.FILE_NAME);
    }

    @Test
    public void testProcessGetClusterFileMessage () {
        testProcessGetFileMessage(Cluster.NAME);
    }

    @Test
    public void testProcessGetTopicsFileMessage () {
        testProcessGetFileMessage(TopicsFile.FILE_NAME);
    }

    @Test
    public void testProcessGetSubscriptionsFileMessage () {
        testProcessGetFileMessage(SubscriptionsFile.FILE_NAME);
    }

    @Test
    public void testProcessConnectMessage () {
        NewNodeState.setLogger(getMockLogger());
        ConnectMessage connectMessage = new ConnectMessage(null, this);

        when(getMockNode().getCurrentState()).thenReturn(getNewNodeState());

        State nextState = getNewNodeState().processMessage(connectMessage);

        assert (nextState == getNewNodeState());
        verify (getMockLogger(), atLeastOnce()).warn(Matchers.anyString(), Matchers.any(Throwable.class));
    }
}
