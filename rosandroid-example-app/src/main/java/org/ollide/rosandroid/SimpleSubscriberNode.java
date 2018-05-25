package org.ollide.rosandroid;

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class SimpleSubscriberNode extends AbstractNodeMain implements NodeMain {
    private static final java.lang.String TAG = SimpleSubscriberNode.class.getSimpleName();

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("SimpleSubscriberNode/ListenerNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<String> subscriber = connectedNode.newSubscriber("chatter", std_msgs.String._TYPE);
        subscriber.addMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(std_msgs.String message) {
                Log.i(TAG, "I heard: \"" + message.getData() + "\"");
            }
        });
    }
}
