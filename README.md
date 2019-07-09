Wiremock servlet classpath issue on Meizu devices
=================================================

This project reproduces a bug in the following scenario:
* An application performs a simple `GET /hello/world` request on a server
* Wiremock is configured to respond to this request with a simple `text/plain` string response consisting of a string of XXXXXX characters
* When the response is 256 characters or longer, and the instrumentation test is run on a Meizu Note 5 (Android 7.0) device, wiremock fails to provide the matched response. We see in the client logs an exception message:
```
No interface method isAsyncStarted()Z in class Ljavax/servlet/http/HttpServletRequest; or its super classes (declaration of 'javax.servlet.http.HttpServletRequest' appears in /system/framework/meizu2_jcifs.jar)
```

Note: this issue has been found to occur with the following request libraries:
* volley
* retrofit (this project example)
* okhttp3 directly (without retrofit)

Root cause summary
------------------
Both wiremock and Meizu embed `javax.servlet` classes, without package relocation. The versions of the two sets of classes are not the same, resuling exceptions about missing methods.

The test
--------
The test launches an activity which:
* Performs the network request
* Updates a `TextView` with the response result (the content if successful, an error message otherwise)

The test then verifies that the `TextView` contains the expected response.
 
Steps to reproduce the problem
------------------------------
On a Meizu Note 5:
```
./gradlew cAT
```

* Expected behavior: the test passes
* Actual behavior: the test fails

Change the value of `responseBodyCharacterCount` in `ExampleInstrumentedTest` to verify that the test fails on the Meizu if it's 256 or larger, and passes for values 0-255.


Analysis
--------
When the size of the response is larger than 256 characters, we end up here, in [AbstractCompressedStream.java](https://github.com/eclipse/jetty.project/blob/jetty-9.2.x/jetty-servlets/src/main/java/org/eclipse/jetty/servlets/gzip/AbstractCompressedStream.java#L126), in version 9.2 of jetty:
```java
    public void flush() throws IOException
    {
        if (_out == null || _bOut != null)
        {
            long length=_wrapper.getContentLength();
            if (length > 0 && length < _wrapper.getMinCompressSize())
                doNotCompress(false);
            else
                doCompress();     <-------- HERE
        }

        _out.flush();
    }
```

Inside `doCompress()`, we end up here (code decompiled by Android Studio, as this code doesn't appear in version 9.2 of jetty, but is what's actually executed...):
```java
                String prefix = "";
                if (this._request.getDispatcherType() == DispatcherType.INCLUDE) {
                    prefix = "wiremock.org.eclipse.jetty.server.include.";
                }
```

`getDispatcherType()` doesn't exist, so we end up with this error:
```
java.lang.NoSuchMethodError: No interface method getDispatcherType()Ljavax/servlet/DispatcherType; in class Ljavax/servlet/http/HttpServletRequest; or its super classes (declaration of 'javax.servlet.http.HttpServletRequest' appears in /system/framework/meizu2_jcifs.jar)
	at wiremock.org.eclipse.jetty.servlets.gzip.AbstractCompressedStream.doCompress(AbstractCompressedStream.java:248)
	at wiremock.org.eclipse.jetty.servlets.gzip.AbstractCompressedStream.close(AbstractCompressedStream.java:157)
	at com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet.writeAndTranslateExceptions(WireMockHandlerDispatchingServlet.java:235)
```
This is caught by [ServletHandler.doHandle()](https://github.com/eclipse/jetty.project/blob/jetty-9.2.x/jetty-servlet/src/main/java/org/eclipse/jetty/servlet/ServletHandler.java#L684), which ends up here:
```java
        finally
        {
            // Complete async errored requests
            if (th!=null && request.isAsyncStarted())  <--------------- HERE
                baseRequest.getHttpChannelState().errorComplete(); 
```

`isAsyncStarted()` is also not available on the Meizu, which is what causes the request to fail (with the message about this missing method).

