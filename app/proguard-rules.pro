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
# 注解
-keepattributes *Annotation*
# 异常
-keepattributes Exceptions
# 泛型
-keepattributes Signature
# 反射
-keepattributes EnclosingMethod

-keepattributes InnerClasses,Signature,EnclosingMethod

-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep public class * extends androidx.versionedparcelable.VersionedParcelable {
      <init>();
 }

#-renamesourcefileattribute SourceFile
-keep class com.google.gson.stream.** { *; }
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod
# Gson specific classes
-dontwarn com.google.gson.**
-keep class com.xzhou.book.models.Entities$* {*;}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    *;
}
-keepclassmembers class * implements java.io.Serializable {
    *;
}

#保留自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{*;}

-dontwarn org.jsoup.**
-keep class org.jsoup.**{*;}

-keep class com.chad.library.adapter.** {
*;
}
-keepclasseswithmembers class com.chad.library.adapter.base.BaseQuickAdapter {
    *;
}
-keepclasseswithmembers class * extends com.chad.library.adapter.base.BaseViewHolder{
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

#-------------- okhttp3 start-------------
# OkHttp3
# https://github.com/square/okhttp
# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.* { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# okhttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
#----------okhttp end--------------

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