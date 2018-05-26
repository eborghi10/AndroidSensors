package org.ollide.rosandroid;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import sensor_msgs.Imu;
import std_msgs.Header;

public class ImuPublisherNode extends AbstractNodeMain {
    private static float maxFrequency = 100.f;
    private float minElapse = 1000 / maxFrequency;

    //TODO Ensure that data from accelerometer, gyroscope, and orientation sensor that is published within the same message does not vary in terms of the time they are message, otherwise drop.
    private long previousPublishTime = System.currentTimeMillis();
    private boolean isAccelerometerMessagePending;
    private boolean isGyroscopeMessagePending;
    private boolean isOrientationMessagePending;

    private String topic_name;
    private SensorEventListener accelerometerListener;
    private SensorEventListener gyroscopeListener;
    private SensorEventListener orientationListener;

    private float ax, ay, az;
    private float aRoll, aPitch, aYaw;
    private float roll, pitch, yaw;
    private String imuFrameId;
    private float prevRoll, prevPitch, prevYaw;
    private OnFrameIdChangeListener imuFrameIdChangeListener;

    public ImuPublisherNode() {
        this.topic_name = "imu_data";
        isAccelerometerMessagePending = false;
        isGyroscopeMessagePending = false;
        isOrientationMessagePending = false;

        accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (!(
                        ax == sensorEvent.values[0] &&
                        ay == sensorEvent.values[1] &&
                        az == sensorEvent.values[2]
                )) {
                    ax = sensorEvent.values[0];
                    ay = sensorEvent.values[1];
                    az = sensorEvent.values[2];
                    isAccelerometerMessagePending = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        gyroscopeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (!(
                        aRoll == -sensorEvent.values[1] &&
                        aPitch == -sensorEvent.values[2] &&
                        aYaw == sensorEvent.values[0]
                )) {
                    aRoll = -sensorEvent.values[1];
                    aPitch = -sensorEvent.values[2];
                    aYaw = sensorEvent.values[0];
                    isGyroscopeMessagePending = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        orientationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (!(
                        roll == -sensorEvent.values[1] &&
                        pitch == -sensorEvent.values[2] &&
                        yaw == 360 - sensorEvent.values[0]
                )) {
                    roll = -sensorEvent.values[1];
                    pitch = -sensorEvent.values[2];
                    yaw = 360 - sensorEvent.values[0];

                    isOrientationMessagePending = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        imuFrameIdChangeListener = new OnFrameIdChangeListener() {
            @Override
            public void onFrameIdChanged(String newFrameId) {
                imuFrameId = newFrameId;
            }
        };
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_android_sensors/imu_publisher_node");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        final Publisher<Imu> imuPublisher = connectedNode.newPublisher(this.topic_name, Imu._TYPE);

        connectedNode.executeCancellableLoop(new CancellableLoop() {
            int sequenceNumber = 1;
            Header header = connectedNode.getTopicMessageFactory().newFromType(Header._TYPE);
            Imu imuMessage = imuPublisher.newMessage();

            @Override
            protected void loop() throws InterruptedException {
                long currentTimeMillis = System.currentTimeMillis();
                if (isAccelerometerMessagePending && isGyroscopeMessagePending && isOrientationMessagePending) {
                    header.setStamp(connectedNode.getCurrentTime());
                    header.setFrameId(imuFrameId);
                    header.setSeq(sequenceNumber);
                    imuMessage.setHeader(header);

                    imuMessage.getLinearAcceleration().setX(ax);
                    imuMessage.getLinearAcceleration().setY(ay);
                    imuMessage.getLinearAcceleration().setZ(az);

                    float dt = (currentTimeMillis - previousPublishTime) / 1000.f;
                    float dRoll = (roll - prevRoll);
                    if (dRoll > 180)
                        dRoll = 360 - dRoll;
                    float dPitch = (pitch - prevPitch);
                    if (dPitch > 180)
                        dPitch = 360 - dPitch;
                    float dYaw = (yaw - prevYaw);
                    if (dYaw > 180)
                        dYaw = 360 - dYaw;

                    imuMessage.getAngularVelocity().setX(dRoll / dt);
                    imuMessage.getAngularVelocity().setY(dPitch / dt);
                    imuMessage.getAngularVelocity().setZ(dYaw / dt);

                    prevRoll = roll;
                    prevPitch = pitch;
                    prevYaw = yaw;

                    imuMessage.getOrientation().setW(roll);
                    imuMessage.getOrientation().setX(roll);
                    imuMessage.getOrientation().setY(pitch);
                    imuMessage.getOrientation().setZ(yaw);

                    imuPublisher.publish(imuMessage);

                    //Wait until minimum time has elapsed
                    long elapsed = currentTimeMillis - previousPublishTime;
                    long remainingTime = (long) (minElapse - elapsed);
                    if (remainingTime > 0)
                        Thread.sleep(remainingTime);
                    previousPublishTime = System.currentTimeMillis();

                    isAccelerometerMessagePending = false;
                    isGyroscopeMessagePending = false;
                    isOrientationMessagePending = false;

                    ++this.sequenceNumber;
                } else {
                    Thread.sleep(1);
                }
            }
        });
    }

    public SensorEventListener getAccelerometerListener() {
        return accelerometerListener;
    }

    public SensorEventListener getGyroscopeListener() {
        return gyroscopeListener;
    }

    public SensorEventListener getOrientationListener() {
        return orientationListener;
    }

    public OnFrameIdChangeListener getFrameIdListener() {
        return imuFrameIdChangeListener;
    }
}
