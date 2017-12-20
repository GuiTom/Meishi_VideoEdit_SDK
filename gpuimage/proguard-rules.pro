# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/chenchao/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
#============以下为自己添加的混淆规则======================
#-libraryjars 'C:\Program Files\Java\jre1.8.0_121\lib\rt.jar'

#忽略警告
#-ignorewarnings

#不要压缩(这个必须，因为开启混淆的时候 默认 会把没有被调用的代码 全都排除掉)
-dontshrink

#避免混淆泛型 如果混淆报错建议关掉
#-keepattributes Signature


#============保持相应的类不被混淆===========================

#保持BuildConfig不被混淆(因为混淆之后就无法在导出jar时排除该类)
#同理 相应的TestActivity以及TestService也需要保持不被混淆
-keep class com.kok.http.BuildConfig{
public *;
}
#保持调用接口不被混淆
#-keep class com.kok.http.core.HttpUtils{
#public *;
#}
#-keep class com.kok.http.core.HttpErrorCode{
#public *;
#}
#-keep class com.kok.http.callback.**{*;}