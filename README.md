# Admitad SDK for Android

## Table of contents

* [Example app](#example-app)
* [Basic integration](#basic-integration)
    * [Add the SDK to your project](#sdk-add)
    * [Usage](#sdk-usage)
* [License](#license)   

## <a id="example-app"></a>Example app

You can find examples of use [in this directory](app/src/main/java/ru/tachos/admitadstatistic/MainActivity.java).
You can open the Android project to see an example on how the admitad SDK can be integrated.

## <a id="basic-integration"></a>Basic integration

These are the minimal steps required to integrate the admitad SDK into your Android project. We are going to assume that you use Android Studio for your Android development and target an Android API level 14 (Ice Cream Sandwich) or later.

### <a id="sdk-add"></a>Add the SDK to your project

Add repository to the root gradle:

```gradle
allprojects {
    repositories {
        jcenter()
        google()
    }
}
```

old version:

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://maven.google.com" }
    }
}
```

And this to the project's gradle:

```gradle
 implementation 'ru.tachos.admitadstatisticsdk:admitadstatisticsdk:1.4.0'
```
   
old version:

```gradle
compile('ru.tachos.admitadstatisticsdk:admitadstatisticsdk:1.4.0') {
        transitive = true
}
```

### <a id="sdk-usage"></a>Usage

  * SDK is being initialized async, so you must call AdmitadTracker#initialize before using. We'd like to reccomend to initialize in the `Application#OnCreate` or in the launcher Activity in `Activity#onCreate`. You have to pass context, postback key (non-null key is mandatory, exception is thrown otherwise), callback (optional)
  
  ```java
   AdmitadTracker.initialize(getApplicationContext(), YOUR_ANDROID_POSTBACK_KEY, new TrackerInitializationCallback() {
            @Override
            public void onInitializationSuccess() {
            }

            @Override
            public void onInitializationFailed(Exception exception) {
            }
        });
  ```
  
  * Admitad uid is required for sending logs. You may pass deeplink by method AdmitadTracker#handleDeeplink. The deeplink must have parameter called `admitad_uid` (e.g. `schema://host?admitad_uid=YOUR_UID`). If SDK has no UID then no logs will be sent.
  
  ```java
   AdmitadTracker.getInstance().handleDeeplink(Uri.parse("schema://host?admitad_uid=YOUR_UID"));
  ```
  
  Example of handling deeplink:
  
  ```java
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      onNewIntent(getIntent());
  }
  
  @Override
  protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      setIntent(intent);
        
      if (intent.getData() != null) {
          AdmitadTracker.getInstance().handleDeeplink(intent.getData());
      }
  }
  ```
  
  See more examples in the [test project](app/)
  
  * When `AdmitadTracker#initialize` is called, it's possible to start tracking even if sdk is not initialized, if sdk has any uid value, logs will be stored and send ASAP. There're several events sdk is able to log:
  
      * #### Registration 
      
      ```java
      AdmitadTracker.getInstance().logRegistration(*USER_ID*);
      ```
      
      * #### Returned user
      
      ```java
      AdmitadTracker.getInstance().logUserReturn(*USER_ID*, *DAY_COUNT*);
      ```

      * #### Loyalty
      
      ```java
      AdmitadTracker.getInstance().logUserLoyalty(*USER_ID*, *OPEN_APP_COUNT*);
      ```
  
  * To log confirmed purchase or paid order you have to create AdmitadOrder object using builder. e.g.:
  
  ```java
    final AdmitadOrder order = new AdmitadOrder.Builder("123", "100.00")
                .setCurrencyCode("RUB")
                .putItem(new AdmitadOrder.Item("Item1", "ItemName1", 300))
                .putItem(new AdmitadOrder.Item("Item2", "ItemName2", 500))
                .setUserInfo(new AdmitadOrder.UserInfo().putExtra("Surname", "UserSurname").putExtra("Age", "18"))
                .build();
  ```
  
  * Then you can log using `order`:
  
      * #### Paid order
      
      ```java
      AdmitadTracker.getInstance().logOrder(order);
      ```

      * #### Confirmed purchase
      
      ```java
      AdmitadTracker.getInstance().logPurchase(order);
      ```

   * To subscribe for specific event, you can pass callbacks to any log* method, e.g.: 
   
  ```java
     AdmitadTracker.getInstance().logRegistration(*USER_ID*, new TrackerListener() {

            @Override
            public void onSuccess(AdmitadEvent result) {
                 Log.d("AdmitadTracker", "Registration event sent successfully + " + result.toString());
            }

            @Override
            public void onFailure(@AdmitadTrackerCode int errorCode, @Nullable String errorText) {
                 Log.d("AdmitadTracker", "Failed to send registration event: errorCode = " + errorCode + ", errorText = " + errorText);
            }
        });
   ```

  * To subscribe for all events, you can call method `AdmitadTracker#addListener`. This method will be always called on sending.

  ```java
   AdmitadTracker.getInstance().addListener(new TrackerListener() {

            @Override
            public void onSuccess(AdmitadEvent result) {
                Log.d("AdmitadTracker", "Event sent successfully + " + result.toString());
            }

            @Override
            public void onFailure(@AdmitadTrackerCode int errorCode, @Nullable String errorText) {
                Log.d("AdmitadTracker", "Failed to send event: errorCode = " + errorCode + ", errorText = " + errorText);
            }
        });
  ```
  
  * Error code can be one of the AdmitadTrackedCode: 
  
  ```java
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
  
  * To enable logs you can call any time: 
  
  ``` java
  AdmitadTracker.setLogEnabled(true);
  ```

## <a id="license"></a>License

The admitad SDK is licensed under the MIT License.

Copyright (c) 2017 Admitad GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
