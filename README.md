# Burp RetryRequests (Using the Montoya API)
__Author: Nick Coblentz__

RetryRequests is a Burp Suite plugin that exposes a context-menu (right-click -> Extensions -> RetryRequests) to re-send, as is, one or more selected HTTP1/2 requests. This plugin is useful for anyone that prefers to test authorization controls, CSRF prevention, and other controls using session handling rules (Settings -> Sessions -> Session Handling Rules) and manual review through the Logger tab. Alternatives to that approach is to use plugins like Autorize or AutoRepeater.

New Features:
- Retry Verbs (2023-09-22): Retries the same request but iterates through the following verbs: `OPTIONS`,`POST`,`PUT`,`PATCH`,`HEAD`,`GET`,`TRACE`,`TRACK`,`LOCK`,`UNLOCK`,`FAKE`,`DELETE`

## How to build this plugin
### Command-Line
```bash
$ ./gradlew jar
```
### InteliJ
1. Open the project in Intellij
2. Open the Gradle sidebar on the right hand side
3. Choose Tasks -> Build -> Jar

## How to add this plugin to Burp
1. Open Burp Suite
2. Go to Extensions -> Installed -> Add
   - Extension Type: Java
   - Extension file: build/libs/RetryRequestsMontoya-1.0-SNAPSHOT.jar
