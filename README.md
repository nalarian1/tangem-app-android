# Compiling Tangem's application on Debian 12 using command-line

## Prerequisites
- Create a GitHub package read only + non expiration token at https://github.com/settings/tokens
- Download and extract Android SDK build tools & Java Jdk https://drive.proton.me/urls/Q11SGHD4AR#oQB7d1iixVPI
  <pre>tar -xpvf sdktools.tar.gz -C ~/</pre>
- Set your JAVA_HOME variable 
  <pre>echo 'JAVA_HOME=~/Android/Jdk/jdk-17.0.12' >> ~/.bashrc && source ~/.bashrc</pre>
- Add Java Jdk's bin folder to PATH
  <pre>echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc && source ~/.bashrc</pre>
- We will compile a signed apk later on so you need to create a keystore file. Make sure not to forget the password you will be asked for.
  <pre>keytool -genkey -v -keystore YOUR KEY PATH.key -alias YOUR KEY ALIAS NAME -keyalg RSA -keysize 2048 -validity 10000</pre>
- <details>
  <summary>Create a file called "gradle.properties" in ~/.gradle which should contain:</summary>
   <pre>STORE_KEY=YOUR KEY PATH.key
  STORE_KEY_ALIAS=YOUR KEY ALIAS NAME
  STORE_PASSWORD=YOUR KEY PASSWORD</pre>
  </details>


## Build the app
<p>Clone this repo</p>
<pre>git clone https://github.com/nalarian1/tangem-app-android.git
cd tangem-app-android</pre>
<p>Create a file named "local.properties" in the project's root folder and set the following variables:</p>
<pre>gpr.key=YOUR GITHUB TOKEN
grp.user=YOUR GITHUB ACCOUNT NAME
sdk.dir=SDK LOCATION</pre>
<p>If you did everything right so far, after you ran this command, you will get a signed APK file.</p>
<pre>./gradlew assembleRelease</pre>

<p>The APK file's location is:</p>

```/tangem-app-android/app/build/outputs/apk/release```
