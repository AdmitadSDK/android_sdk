To add this lib to the project:

1) Add jar to the app/libs

2) Click on jar and press "add as library"

To generate jar:

1) run createJar gradle task

How to use SDK:

1) SDK is being initialized async, so you must call AdmitadTracker#initialize before using. You have to pass context, postback key, callback (optional)

2) Admitad uid is required for sending logs. You can pass deeplink by method AdmitadTracker#handleDeeplink

3) After you call AdmitadTracker#initialize, you can start logging even if sdk is still not initialized. For logging you can use methods AdmitadTracker#log*.
    For every log type there're different params you have to pass, callback is optional;

4) To log purchase or order you have to create AdmitadOrder object using builder. e.g.:

    final AdmitadOrder order = new AdmitadOrder.Builder("123", "100.00")
                .setCurrencyCode("RUB")
                .putItem(new AdmitadOrder.Item("Item1", "ItemName1", 3))
                .putItem(new AdmitadOrder.Item("Item2", "ItemName2", 5))
                .setUserInfo(new AdmitadOrder.UserInfo().putExtra("Surname", "Kek").putExtra("Age", "10"))
                .build();

5) To subscribe for specific event, you can pass callbacks to the log* method.

6) To subscribe for all events, you can call method AdmitadTracker#addListener. This method will be always called on sending.