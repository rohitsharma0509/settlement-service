# settlement-service

This service is responsible to calculate net payment amount, send request to generate / upload S1 file and adjust pocket once return file processed.

-------------------
### What you'll need
A favorite text editor or IDE\
JDK 8 or later\
Install Gradle

-------------
### Build
1. run cmd `gradle clean build` or `gradlew clean build`.
2. This command will run unit test cases also.

-----
### Run
1. update mongoDb uri to localhost mongo address in application-local.yml file.
2. Kafka is also required, therefore please run the kafka server,bootstrap etc on local.
3. add  "-Dspring.profiles.active=local" in cmd as arguments 
4. run cmd ` gradle -Dspring.profiles.active=local clean :bootRun` or `gradlew -Dspring.profiles.active=local clean :bootRun`.
-----
  BUILD SUCCESSFUL
  Total time: 4.009 secs
  
  Project ran successfully.

  ##Nexus credential configuration
  In `~/.m2/settings.xml` add the below configuration
  ```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
        <id>rider-maven-release</id>
        <username>username</username>
        <password>password</password>
    </server>
  </servers>
</settings>