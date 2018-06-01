package org.ollide.rosandroid;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import sensor_msgs.NavSatFix;
import std_msgs.Header;

public class LocationPublisherNode extends AbstractNodeMain {
    private static final String TAG = LocationPublisherNode.class.getSimpleName();

    private static float maxFrequency = 100.f;
    private static float minElapse = 1000 / maxFrequency;

    private static float minFrequency = 20.f;
    private static float maxElapse = 1000 / minFrequency;

    private long previousPublishTime = System.currentTimeMillis();
    private boolean isMessagePending;

    private String topic_name;

    private final LocationListener locationListener;
    private Location cachedLocation;
    private String navSatFixFrameId;
    private OnFrameIdChangeListener locationFrameIdChangeListener;

    public LocationPublisherNode() {
        this.topic_name = "fix";
        isMessagePending = false;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    cachedLocation = location;
                    isMessagePending = true;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "Provider: " + s + ", Status: " + i + ", Extras: " + bundle);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "Provider enabled: " + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "Provider disabled: " + s);
            }
        };

        locationFrameIdChangeListener = new OnFrameIdChangeListener() {
            @Override
            public void onFrameIdChanged(String newFrameId) {
                navSatFixFrameId = newFrameId;
            }
        };
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        final Publisher<NavSatFix> locationPublisher = connectedNode.newPublisher(this.topic_name, "sensor_msgs/NavSatFix");
        final NavSatFix navSatFix = locationPublisher.newMessage();

        connectedNode.executeCancellableLoop(new CancellableLoop() {
            int sequenceNumber = 1;
            Header header = connectedNode.getTopicMessageFactory().newFromType(Header._TYPE);

            @Override
            protected void loop() throws InterruptedException {
                if ((cachedLocation != null) &&
                        (isMessagePending
                                || (System.currentTimeMillis() - previousPublishTime) >= maxElapse // Or, is max elapse reached?
                        )) {
                    header.setStamp(connectedNode.getCurrentTime());
                    header.setFrameId(navSatFixFrameId);
                    header.setSeq(sequenceNumber);
                    navSatFix.setHeader(header);

                    navSatFix.setLatitude(cachedLocation.getLatitude());
                    navSatFix.setLongitude(cachedLocation.getLongitude());
                    Log.d(TAG, "LOCATION PUBLISHED");
                    locationPublisher.publish(navSatFix);

                    //Wait until minimum time has elapsed
                    long elapsed = System.currentTimeMillis() - previousPublishTime;
                    long remainingTime = (long) (minElapse - elapsed);
                    if (remainingTime > 0)
                        Thread.sleep(remainingTime);
                    previousPublishTime = System.currentTimeMillis();

                    isMessagePending = false;
                    ++this.sequenceNumber;
                } else {
                    Thread.sleep(1);
                }
            }
        });
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_android_sensors/location_publisher_node");
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    public OnFrameIdChangeListener getFrameIdListener() {
        return locationFrameIdChangeListener;
    }
}
