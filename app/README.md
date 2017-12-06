## Admitad SDK test project

  There're you can find some examples of sdk usage. 
  
  You can press "ADD MANY TO QUEUE" button to send 100 different logs to make stress tests.
  
  MainActivity is able to handle intent with `admitad` scheme and pass it to the SDK: 
  
   ```java
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);		          
    Uri uri = Uri.parse("admitad://?admitad_uid=TextAndroidUidOutside");
    intent.setData(uri);
    startActivity(intent);
   ```
