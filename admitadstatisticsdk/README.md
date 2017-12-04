## Updating

To update SDK you have to be included in the [admitadsdk organization](https://bintray.com/admitadsdk)

  * Add these fields to your `local.properties`:
  
  ```
  bintray.user=YOUR_USER_NAME
  bintray.apikey=YOUR_API_KEY
  bintray.orgName=admitadsdk
  ```
  
  * Add your public key to organization keys
  
  * Generate your private key using [instruction: part 3](https://inthecheesefactory.com/blog/how-to-upload-library-to-jcenter-maven-central-as-dependency/en)
  
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
