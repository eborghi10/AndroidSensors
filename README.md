## Rosjava Project Template for Android Studio ##
This project template makes it easy to get started with Android programming for
[ROS.org](http://www.ros.org/wiki/). Its structure complies with the new Gradle-based
build system and can be opened and assembled in Android Studio with no further changes.

### Project Structure ###
The example Android application can be found in [rosandroid-example-app](rosandroid-example-app).
It contains a simple Activity which extends RosActivity and starts a node publishing messages
on a ROS topic named ``time``. You can use all ROS Java components here because the rosandroid-core
is declared as a dependency ([libraries/rosandroid-core](libraries/rosandroid-core)). The
rosandroid-core consists of Android specific Java code only. All other dependencies (mainly rosjava
itself and common messages for ROS) are integrated through ROS's maven repository on GitHub. 

ROS automatically launches a MasterChooser activity to establish a connection to a running ROS
instance. This comes in handy for most developers but if you prefer a custom approach or design to
connect to your ROS master you need to change these classes, which is why I've included the ROS
Android source code.

### Requirements ###
This template has been used with ROS Hydro and Indigo, it may not work with older or newer versions of ROS.
It is optimized to work with the latest release of [Android Studio](https://developer.android.com/sdk/index.html) (currently 1.1.0) but can also be build from
the command-line. As this is plain Android/Java/Gradle no full install of ROS is needed to compile,
therefore you can not only use Ubuntu but also Windows or Mac.

### Contribution ###
Feel free to contribute to this project by either raising issues or handing in pull requests.
