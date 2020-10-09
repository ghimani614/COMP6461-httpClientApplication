# Java networking examples

There are several examples to help you be familiar with networking programming in JDK. It is recommended to use common tools to develop your assignment. Therefore we use Maven for this example.

## Requirement
1. [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Apache Maven](https://maven.apache.org/) 

## Structure
There are 5 applications in this example for the echo and time protocols.

### Echo Protocol
- **Blocking Echo Server** - an implementation of the echo server using a blocking NIO socket
- **Blocking Echo Client** - an implementation of the echo client using a blocking NIO socket

## Compile and package
1. Open the terminal and cd to the `netsample` directory
2. Run `mvn package` to compile and package this example
You should see two jar files in the `target` directory: one with all dependencies (we use logging and argument parse libraries); one without dependencies.

## Run the sample applications
Use the following the commands to run applications.

### Blocking Echo Server
`java -cp target/netsample-1.0-SNAPSHOT-jar-with-dependencies.jar ca.concordia.echo.BlockingEchoServer --port 8007`
You should see the message 'ca.concordia.echo.BlockingEchoServer - EchoServer is listening at /0:0:0:0:0:0:0:0:8007' which means your echo server is ready.

### Blocking Echo Client
Once your echo server client is listening, you can use the echo client by this command
`java -cp target/netsample-1.0-SNAPSHOT-jar-with-dependencies.jar ca.concordia.echo.BlockingEchoClient --host localhost --port 8007`
If there is no error, you should be able to type into your console; and receive an echo from the echo server.


## Description
This Java project implements all the functionalities of COMP 6461 Lab Assignment 1, including all the optional tasks(bonus marks). 


## Configuration
This project was developed using Intellij, so Intellij is required. No additional configuration is required, Intellij IDE will handle and build the project automatically. 

Environment:
1. Java 9 or later
2. Intellij Community 2020.2

Third-party libraries:
1. JSON.simple 1.1: Used for reading and parsing JSON files. 


## Implementation
Rules:
1. All reserved command keywords are case sensitive, but JSON data and URLs are not.
2. It is not allowed to have multiple space characters between each term, like "httpc  help   get ". Starting with space is invalid, it must starts with "httpc" without any exception.
3. URL has to be wrapped by a pair of apostrophes, like 'http://httpbin.org/post'.
4. This program has assumed Content-Type to be application/json in any situation, changing Content-Type using POST command has no effect.
