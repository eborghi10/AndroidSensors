package org.ollide.rosandroid;

import android.util.Log;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimplePublisherNode implements NodeMain {

    private static final String TAG = SimplePublisherNode.class.getSimpleName();

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("SimplePublisher/TimeLoopNode");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Publisher<std_msgs.String> publisher = connectedNode.newPublisher(GraphName.of("time"), std_msgs.String._TYPE);

        final CancellableLoop loop = new CancellableLoop() {
            @Override
            protected void loop() throws InterruptedException {
                // retrieve current system time
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                Log.i(TAG, "publishing the current time: " + time);

                // create and publish a simple string message
                std_msgs.String str = publisher.newMessage();
                str.setData("The current time is: " + time);
                publisher.publish(str);

                // go to sleep for one second
                Thread.sleep(1000);
            }
        };
        connectedNode.executeCancellableLoop(loop);
    }

    @Override
    public void onShutdown(Node node) {
        // intentionally left blank
    }

    @Override
    public void onShutdownComplete(Node node) {
        // intentionally left blank
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        // intentionally left blank
    }
}
