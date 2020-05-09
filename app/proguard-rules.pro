# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# 序列化
-keep class * implements android.os.Parcelable {public static final android.os.Parcelable$Creator *;}
# 注解
-keepattributes *Annotation*
# 异常
-keepattributes Exceptions
# 泛型
-keepattributes Signature
# 反射
-keepattributes EnclosingMethod

-keepattributes InnerClasses,Signature,EnclosingMethod

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keep class android.support.** {*;}
-keep public class * extends android.support.**
-keep public class * extends androidx.versionedparcelable.VersionedParcelable {
      <init>();
 }

-keep class com.xzhou.book.models.Entities$* {*;}
# 保留Parcelable序列化的类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    *;
}

#保留自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{*;}
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.LinearLayoutManager
-keep public class * extends android.app.GridLayoutManager

-dontwarn org.jsoup.**
-keep class org.jsoup.**{*;}
-keep class android.support.constraint.ConstraintLayout

-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}

-dontwarn com.yanzhenjie.permission.**
-keep class com.yanzhenjie.permission.** { *;}
-keep class com.yanzhenjie.permission.FileProvider

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

-keep class butterknife.*
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keep class **$$ViewBinder { *; }

## gson[version 2.8.0]
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.models.** { *; }
-keep interface com.google.gson.examples.models.** { *; }
-keep class com.google.gson.examples.upgrade.internal.VersionInfo {*;}
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer-keep
-keep class * implements com.google.gson.JsonDeserializer
## gson

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {  **[] $VALUES;  public *;}