# Admitad SDK for Android

Min SDK = 14

## Download 

Add repository to the root gradle 
```
allprojects {
    repositories {
        jcenter()
    }
}
```
And this to the project gradle
```
compile('ru.tachos.admitadstatisticsdk:admitadstatisticsdk:1.3.5') {
        transitive = true
}
```
## Usage

  * SDK is being initialized async, so you must call AdmitadTracker#initialize before using. You have to pass context, postback key, callback (optional)
  
  ```
   AdmitadTracker.initialize(getApplicationContext(), YOUR_ANDROID_POSTBACK, new TrackerInitializationCallback() {
            @Override
            public void onInitializationSuccess() {
            }

            @Override
            public void onInitializationFailed(Exception exception) {
            }
        });
  ```
  
  * Admitad uid is required for sending logs. You can pass deeplink by method AdmitadTracker#handleDeeplink.
  
  ```
   AdmitadTracker.getInstance().handleDeeplink(Uri.parse("schema://host?uid=YOUR_UID"));
  ```
  
  You can open example project this way: 
  
  ```
  Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("admitad_test://?uid=TextAndroidUidOutside");
        intent.setData(uri);
        startActivity(intent);
  ```
  
  * After you call AdmitadTracker#initialize, you can start logging even if sdk is still not initialized. For logging you can use methods AdmitadTracker#log*. For every log type there're different params you have to pass, callback is optional;
  
  * To log purchase or order you have to create AdmitadOrder object using builder. e.g.:
  
  ```
    final AdmitadOrder order = new AdmitadOrder.Builder("123", "100.00")
                .setCurrencyCode("RUB")
                .putItem(new AdmitadOrder.Item("Item1", "ItemName1", 300))
                .putItem(new AdmitadOrder.Item("Item2", "ItemName2", 500))
                .setUserInfo(new AdmitadOrder.UserInfo().putExtra("Surname", "UserSurname").putExtra("Age", "18"))
                .build();
  ```

   * To subscribe for specific event, you can pass callbacks to the log* method.
   
  ```
     AdmitadTracker.getInstance().logRegistration("TestRegistrationUid", new TrackerListener() {

            @Override
            public void onSuccess(AdmitadEvent result) {
            }

            @Override
            public void onFailure(int errorCode, @Nullable String errorText) {
            }
        });
   ```

  * To subscribe for all events, you can call method AdmitadTracker#addListener. This method will be always called on sending.

  ```
   AdmitadTracker.getInstance().addListener(new TrackerListener() {

            @Override
            public void onSuccess(AdmitadEvent result) {
            }

            @Override
            public void onFailure(@AdmitadTrackerCode int errorCode, @Nullable String errorText) {
            }
        });
  ```
  
  * Error code can be one of the AdmitadTrackedCode 
  
  ```
  public @interface AdmitadTrackerCode {
    int NONE = 0;
    int SUCCESS = 200;

    int ERROR_GENERIC = -100;
    int ERROR_NO_INTERNET = -200;
    int ERROR_SERVER_UNAVAILABLE = -1;
    int ERROR_SDK_NOT_INITIALIZED = -1000;
    int ERROR_SDK_POSTBACK_CODE_MISSED = -1100;
    int ERROR_SDK_GAID_MISSED = -1200;
    int ERROR_SDK_ADMITAD_UID_MISSED = -1300;
  }
  ```
  
  * You can find examples of use here: https://github.com/AdmitadSDK/android_sdk/blob/master/app/src/main/java/ru/tachos/admitadstatistic/MainActivity.java 
  
  * To enable logs you can call any time, even if initialize is not called 
  
  ``` 
  AdmitadTracker.setLogEnabled(true);
  ```
  
## Updating

To update SDK you have to be included in the [admitadsdk organization](https://bintray.com/admitadsdk)

  * add these fields to your `local.properties`
  
  ```
  bintray.user=YOUR_USER_NAME
  bintray.apikey=YOUR_API_KEY
  bintray.orgName=admitadsdk
  ```
  
  * Add your public key to organization keys
  
  * Increase version name in the `build.gradle` 
  
  ```
  libraryVersion = '1.3.6'
  ```
  
  * Update developer's data in the same file
  
  * If you add some libraries to the dependencies, your have to add dependencies manually in the `publish.gradle`. You are welcome to find another way. 
  
  ```
     dependencies {
           "dependency" {
                  groupId "com.google.android.gms"
                  artifactId "play-services-ads"
                  version GOOGLE_VERSION
            }
            "dependency" {
                   groupId "com.squareup.okhttp3"
                   artifactId "okhttp"
                   version OK_HTTP_VERSION
            }
      }
   ```
   
   * In the terminal
   
   ```
   gradlew install
   ```
   
   ```
   gradlew bintrayUpload
   ```

## License (MIT)
```
Copyright 2017 Admitad GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
