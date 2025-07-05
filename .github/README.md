# How to build app on Debian 12 

## Prerequisites
- Create a GitHub package read only + non expiration token at https://github.com/settings/tokens
- Download and extract Android SDK build tools & Java Jdk https://mega.nz/file/PV8gVBqC#TM1ZfeukD_3dbACAR8KjMMcj8dIfNnGH7aH51a-778A
  <pre>tar -xpvf dependencies.tar.xz -C ~/</pre>
- Set your JAVA_HOME variable 
  <pre>echo 'JAVA_HOME=/home/YOUR_USERNAME/Android/Jdk/bin' >> ~/.bashrc && source ~/.bashrc</pre>
- Add Java Jdk & Android SDK folder to PATH variable
  <pre>echo 'export PATH=$PATH:~/Android/Sdk/platform-tools:$JAVA_HOME' >> ~/.bashrc && source ~/.bashrc</pre>
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
gpr.user=YOUR GITHUB ACCOUNT NAME
sdk.dir=SDK LOCATION</pre>
<p>If you did everything right so far, after you run this command, you will get a signed APK file.</p>
<pre>./gradlew assembleRelease</pre>

<p>The APK file's location is:</p>

```/tangem-app-android/app/build/outputs/apk/release```
