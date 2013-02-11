barchart-http
=============

A lightweight, high performance, asynchronous HTTP server built on top of Netty 4.

Key features:

1. High performance NIO built using Netty 4's core HTTP classes
2. Asynchronous response handling support built into the request API
3. Simple programmatic configuration and lifecycle management for easily embedding in other server applications
4. Hassle-free websocket support
5. Implements J2EE servlet API via a lightweight compatibility layer

High Performance
----------------

Barchart HTTP server is built on top of Netty 4, allowing it to take full advantage of Netty's high performance
NIO stack. The server is in its early stages and formal benchmarks are incomplete, but in its current state it
outperforms Apache 2.x for basic requests by 3x or more (see Benchmarks below.)

Asynchronous Request Framework
------------------------------

The request handling API supports asynchronous request processing natively, without needing to block on worker
threads before returning from onRequest(). Activating asynchronous request handling is done by calling
response.suspend():

```java
public void onRequest(ServerRequest request, ServerResponse response) throws IOException {

    String id = request.getParameter("id");
    
    // Asynchronous lookup method, which will call back when complete
    lookupUsername(id, this);
    
    response.suspend();
    
}
```
The connection will remain open until the request handler explicitly tells it to close the connection.
To do this, the callback calls response.finish():

```java
// Called by lookupUsername() on completion
public void completeRequest(String username) {

    response.write("Username is " + username);
    
    response.finish();
    
}
```

Failure to call response.finish() will result in a hung connection that stays open until the client
decides to close it remotely, so you should guarantee that it is called on every request.

For additional convenience, there is a base request handler implementation that allows you to register
task Futures, and automatically cancels them for you in the event of a client disconnect or channel
exception:

```java
public class MyRequestHandler extends CancellableRequestHandler {

    public void onRequest(ServerRequest request, ServerResponse response) throws IOException {
    
        Future<String> lookupFuture = lookupUsername(request.getParameter("name"), this);
        
        // Registers the async task with the framework for cancellation on connection failure
        cancelOnAbort(lookupFuture);
        
        response.suspend();
        
    }
    
}
```

Configuration and Lifecycle
---------------------------

Configuration is done programmatically with a fluent API, to allow for easy adaptation with whatever
application configuration mechanism you want to implement.

```java
HttpServer httpServer = new HttpServer().configure(new HttpServerConfig()
    .address(new InetSocketAddress("0.0.0.0", 8080))
    .requestHandler("/login", new LoginHandler())
    .errorHandler(new PrettyErrorHandler())
    .logger(new RotatingFileLogger("/var/log/http", "test-site"))
    .maxConnections(1000)
);

httpServer.listen();
```

The configuration object is mutable, and many changes to it will be applied to the server at runtime
as long as it does not require a rebind. This allows you to dynamically add or remove request handler
paths while the server is running, for example, but not change the address or port the server is
listening on.

```java
// Add a new handler at runtime
httpServer.config().requestHandler("/userinfo", new UserInfoHandler());

// Remove a handler at runtime - future requests to this URI will fail
httpServer.config().removeRequestHandler("/userinfo");
```

[HttpServerConfig](https://github.com/barchart/barchart-http/blob/master/src/main/java/com/barchart/http/server/HttpServerConfig.java).requestHandler() accepts either a
[RequestHandler](https://github.com/barchart/barchart-http/blob/master/src/main/java/com/barchart/http/request/RequestHandler.java)
or [RequestHandlerFactory](https://github.com/barchart/barchart-http/blob/master/src/main/java/com/barchart/http/request/RequestHandlerFactory.java)
instance, allowing you flexibility in controlling the handler lifecycle.

Shutting down the server can be done gracefully or forcibly:

```java
// Graceful shutdown, leaves existing client connections active
ChannelFuture shutdownFuture = httpServer.shutdown();

// To restart, wait for shutdown to complete:
shutdownFuture.get();
ChannelFuture listenFuture = httpService.listen();

// Forcible shutdown, closes all active client connections
ChannelGroupFuture killFuture = httpServer.kill();

// Wait for all connections to close (server and client)
killFuture.get();
```

Websockets
----------

Websocket support is planned, building on top of Netty 4's websocket channel handlers. The API is still
under discussion.

Servlet API Compatibility
-------------------------

The server can function as a J2EE servlet container using a lightweight API wrapper built on top of the
native request API. This is currently partially implemented as part of the 
[barchart-osgi-servlet](https://github.com/barchart/barchart-osgi/tree/master/barchart-osgi-servlet) project,
but requires some refactoring and additional feature development before it is usable.

Benchmarks
----------

Benchmarks are incomplete, but very promising so far. The following test run was against a test server
running inside Eclipse:

```
$ ab -n 20000 -c 200 "http://localhost:8080/"
This is ApacheBench, Version 2.3 <$Revision: 655654 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 2000 requests
Completed 4000 requests
Completed 6000 requests
Completed 8000 requests
Completed 10000 requests
Completed 12000 requests
Completed 14000 requests
Completed 16000 requests
Completed 18000 requests
Completed 20000 requests
Finished 20000 requests


Server Software:        
Server Hostname:        localhost
Server Port:            8080

Document Path:          /
Document Length:        7 bytes

Concurrency Level:      200
Time taken for tests:   0.777 seconds
Complete requests:      20000
Failed requests:        0
Write errors:           0
Total transferred:      900000 bytes
HTML transferred:       140000 bytes
Requests per second:    25723.80 [#/sec] (mean)
Time per request:       7.775 [ms] (mean)
Time per request:       0.039 [ms] (mean, across all concurrent requests)
Transfer rate:          1130.44 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        2    4   0.7      4      10
Processing:     1    4   0.8      4      11
Waiting:        0    3   0.8      3      10
Total:          4    8   0.8      7      19
WARNING: The median and mean for the total time are not within a normal deviation
        These results are probably not that reliable.

Percentage of the requests served within a certain time (ms)
  50%      7
  66%      8
  75%      8
  80%      8
  90%      8
  95%      9
  98%     10
  99%     10
 100%     19 (longest request)
```

In comparison, Apache 2.x on the same machine maxed out at 4570 req/sec.
