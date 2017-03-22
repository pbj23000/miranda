package com.ltsllc.miranda.node.states;

/**
 * Created by Clark on 2/7/2017.
 */

import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.State;
import com.ltsllc.miranda.cluster.*;
import com.ltsllc.miranda.cluster.messages.ConnectMessage;
import com.ltsllc.miranda.cluster.messages.VersionsMessage;
import com.ltsllc.miranda.deliveries.SystemDeliveriesFile;
import com.ltsllc.miranda.event.SystemMessages;
import com.ltsllc.miranda.file.GetFileResponseMessage;
import com.ltsllc.miranda.file.GetFileResponseWireMessage;
import com.ltsllc.miranda.network.Network;
import com.ltsllc.miranda.node.Node;
import com.ltsllc.miranda.node.messages.*;
import com.ltsllc.miranda.node.networkMessages.*;
import com.ltsllc.miranda.subsciptions.SubscriptionsFile;
import com.ltsllc.miranda.miranda.Miranda;
import com.ltsllc.miranda.topics.TopicsFile;
import com.ltsllc.miranda.user.GetUsersFileMessage;
import com.ltsllc.miranda.user.UsersFile;
import org.apache.log4j.Logger;

/**
 * When a node is added via a connect it enters this state, waiting for a join message.
 */
public class NewNodeState extends NodeState {
    private static Logger logger = Logger.getLogger(NewNodeState.class);

    private Cluster cluster;

    public NewNodeState (Node node, Network network, Cluster cluster) {
        super(node, network);

        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public static void setLogger (Logger logger) {
        NewNodeState.logger = logger;
    }

    public State processNetworkMessage(NetworkMessage networkMessage) {
        State nextState = this;

        WireMessage wireMessage = networkMessage.getWireMessage();

        switch (wireMessage.getWireSubject()) {
            case Join: {
                JoinWireMessage joinWireMessage = (JoinWireMessage) networkMessage.getWireMessage();
                nextState = processJoinWireMessage (joinWireMessage);
                break;
            }

            case Versions: {
                VersionsWireMessage versionsWireMessage = (VersionsWireMessage) networkMessage.getWireMessage();
                nextState = processVersionsWireMessage (versionsWireMessage);
                break;
            }

            case GetFileResponse: {
                GetFileResponseWireMessage getFileResponseWireMessage = (GetFileResponseWireMessage) networkMessage.getWireMessage();
                nextState = processGetFileResponseWireMessage (getFileResponseWireMessage);
                break;
            }

            default:
                nextState = super.processNetworkMessage(networkMessage);
                break;
        }

        return nextState;
    }

    @Override
    public State processMessage(Message message) {
        State nextState = this;

        switch (message.getSubject()) {
            case NetworkMessage: {
                NetworkMessage networkMessage = (NetworkMessage) message;
                nextState = processNetworkMessage(networkMessage);
                break;
            }

            case GetClusterFile: {
                GetClusterFileMessage getClusterFileMessage = (GetClusterFileMessage) message;
                nextState = processGetClusterFileMessage(getClusterFileMessage);
                break;
            }

            case GetUsersFile: {
                GetUsersFileMessage getUsersFileMessage = (GetUsersFileMessage) message;
                nextState = processGetUsersFileMessage (getUsersFileMessage);
                break;
            }

            case GetTopicsFile: {
                GetTopicsFileMessage getTopicsFileMessage = (GetTopicsFileMessage) message;
                nextState = processGetTopicsFileMessage (getTopicsFileMessage);
                break;
            }

            case GetSubscriptionsFile: {
                GetSubscriptionsFileMessage getSubscriptionsFileMessage = (GetSubscriptionsFileMessage) message;
                nextState = processGetSubscriptionsFileMessage(getSubscriptionsFileMessage);
                break;
            }

            case GetVersion: {
                GetVersionMessage getVersionMessage = (GetVersionMessage) message;
                nextState = processGetVersionMessage (getVersionMessage);
                break;
            }

            case Connect: {
                ConnectMessage connectMessage = (ConnectMessage) message;
                nextState = processConnectMessage(connectMessage);
                break;
            }

            case GetSystemMessages: {
                GetSystemMessagesMessage getSystemMessagesMessage = (GetSystemMessagesMessage) message;
                nextState = processGetSystemMessagesMessage(getSystemMessagesMessage);
                break;
            }

            case GetDeliveries: {
                GetDeliveriesMessage getDeliveriesMessage = (GetDeliveriesMessage) message;
                nextState = processGetDeliveriesMessage(getDeliveriesMessage);
                break;
            }

            case GetFile: {
                GetFileMessage getFileMessage = (GetFileMessage) message;
                nextState = processGetFileMessage (getFileMessage);
                break;
            }

            default:
                nextState = super.processMessage(message);
                break;
        }

        return nextState;
    }


    private State processGetClusterFileMessage (GetClusterFileMessage getClusterFileMessage) {
        GetFileWireMessage getFileWireMessage = new GetFileWireMessage(Cluster.FILE_NAME);
        sendOnWire(getFileWireMessage);

        return this;
    }

    private State processJoinWireMessage (JoinWireMessage joinWireMessage) {
        getNode().setDns(joinWireMessage.getDns());
        getNode().setIp(joinWireMessage.getIp());
        getNode().setPort(joinWireMessage.getPort());
        getNode().setDescription(joinWireMessage.getDescription());

        getCluster().sendNewNode(getNode().getQueue(), this, getNode());

        JoinResponseWireMessage joinResponseWireMessage = new JoinResponseWireMessage(JoinResponseWireMessage.Responses.Success);
        sendOnWire(joinResponseWireMessage);

        return new NodeReadyState (getNode(), getNetwork());
    }


    private State processGetSubscriptionsFileMessage (GetSubscriptionsFileMessage getSubscriptionsFileMessage) {
        GetFileWireMessage getFileWireMessage = new GetFileWireMessage("subscriptions");
        sendOnWire(getFileWireMessage);

        return this;
    }

    private State processGetVersionMessage (GetVersionMessage getVersionMessage) {
        GetVersionsWireMessage getVersionsWireMessage = new GetVersionsWireMessage();
        sendOnWire(getVersionsWireMessage);

        return this;
    }

    private State processConnectMessage (ConnectMessage connectMessage) {
        String message = getNode() +  " in state " + this + " was told to connect when it already has a connection!  "
                + " ignoring message.";

        logger.warn(message, connectMessage.getWhere());

        return getNode().getCurrentState();
    }


    private State processVersionsWireMessage (VersionsWireMessage versionsWireMessage) {
        VersionsMessage versionsMessage = new VersionsMessage(getNode().getQueue(), this, versionsWireMessage.getVersions());
        send(Miranda.getInstance().getQueue(), versionsMessage);

        return this;
    }


    private State processGetFileResponseWireMessage (GetFileResponseWireMessage getFileResponseWireMessage) {
        GetFileResponseMessage getFileResponseMessage = new GetFileResponseMessage(getNode().getQueue(), this,
                getFileResponseWireMessage.getRequester(), getFileResponseWireMessage.getContents());

        if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(Cluster.FILE_NAME)) {
            send(Cluster.getInstance().getQueue(), getFileResponseMessage);
        } else if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(UsersFile.FILE_NAME)) {
            send(UsersFile.getInstance().getQueue(), getFileResponseMessage);
        } else if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(TopicsFile.FILE_NAME)) {
            send(TopicsFile.getInstance().getQueue(), getFileResponseMessage);
        } else if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(SubscriptionsFile.FILE_NAME)) {
            send(SubscriptionsFile.getInstance().getQueue(), getFileResponseMessage);
        } else if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(SystemMessages.FILE_NAME)) {
            send(SystemMessages.getInstance().getQueue(), getFileResponseMessage);
        } else if (getFileResponseWireMessage.getRequester().equalsIgnoreCase(SystemDeliveriesFile.FILE_NAME)) {
            send(SystemDeliveriesFile.getInstance().getQueue(), getFileResponseMessage);
        }

        return this;
    }

    private State processGetUsersFileMessage (GetUsersFileMessage getUsersFileMessage) {
        GetFileWireMessage getFileWireMessage = new GetFileWireMessage(UsersFile.FILE_NAME);
        sendOnWire(getFileWireMessage);

        return this;
    }

    private State processGetTopicsFileMessage (GetTopicsFileMessage getTopicsFileMessage) {
        GetFileWireMessage getFileWireMessage = new GetFileWireMessage(TopicsFile.FILE_NAME);
        sendOnWire(getFileWireMessage);

        return this;
    }

    private State processGetFileMessage (GetFileMessage getFileMessage) {
        GetFileWireMessage getFileWireMessage = new GetFileWireMessage(getFileMessage.getFilename());
        sendOnWire(getFileWireMessage);

        return this;
    }

    private State processGetSystemMessagesMessage (GetSystemMessagesMessage getSystemMessagesMessage) {
        GetMessagesWireMessage getMessagesWireMessage = new GetMessagesWireMessage(getSystemMessagesMessage.getFilename());
        sendOnWire(getMessagesWireMessage);

        return this;
    }

    private State processGetDeliveriesMessage (GetDeliveriesMessage getDeliveriesMessage) {
        GetDeliveriesWireMessage getDeliveriesWireMessage = new GetDeliveriesWireMessage(getDeliveriesMessage.getFilename());
        sendOnWire(getDeliveriesWireMessage);

        return this;
    }
}