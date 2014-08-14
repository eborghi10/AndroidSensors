# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/o-to-the-l/Files/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# obfuscation will break reflection and
# make these rules much more complex
-dontobfuscate

# keep all ros classes with names
-keep class org.ros.** { *; }
-keepnames class org.ros.** { *; }
-dontwarn org.ros.**

# keep ros messages (not part of the org.ros namespace)
-keep class rosgraph_msgs.** { *; }
-keep class sensor_msgs.** { *; }
-keep class std_msgs.** { *; }
-keep class tf2_msgs.** { *; }

-dontwarn org.apache.**
-dontwarn org.jboss.netty.**
-dontwarn com.google.common.**
-dontwarn org.xbill.**
