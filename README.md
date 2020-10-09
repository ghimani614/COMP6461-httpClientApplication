# Lab Assignment 1

This Java project implements all the functionalities of COMP 6461 Lab Assignment 1, including all the optional tasks(bonus marks). It was developed based on the blocking echo server template.

## Requirement
1. [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Apache Maven](https://maven.apache.org/) 
3. [Intellij Community 2020.2](https://www.jetbrains.com/idea/download/) 

Third-party libraries:
1. JSON.simple 1.1: Used for reading and parsing JSON files.


## Configuration
Use Intellij IDE to open the project, navigate to netsample/src/main/java/ca/concordia/httpc, there are httpcServer class and httpc class. Before running the program, make sure json-simple-1.1.jar is added to dependency. It is already added by default, if it is not, right click netsample/lib folder and select "Add as Library" option in Intellij. The server should be run before the client, so right click httpcServer class and select "Run 'httpcServer.main()'" in the project view, then use the same way to run httpc.


## Implementation
Rules:
1. All reserved command keywords are case sensitive, but JSON data and URLs are not.
2. It is not allowed to have multiple space characters between each term, like "httpc  help   get ". Starting with space is invalid, it must starts with "httpc" without any exception.
3. URL has to be wrapped by a pair of apostrophes, like 'http://httpbin.org/post'.
4. This program has assumed Content-Type to be application/json in any situation, changing Content-Type using POST command has no effect.


## Examples
All the available command lines are listed here.

Helper
1. httpc help
2. httpc help get
3. httpc help post

GET
1. httpc get url
-httpc get 'http://httpbin.org/get?course=networking&assignment=1'
2. httpc get -v url
-httpc get -v 'http://httpbin.org/get?course=networking&assignment=1'
3. httpc get -h key:value url
-httpc get -h key1:value1 key2:value2 'http://httpbin.org/get?course=networking&assignment=1'

POST
1. httpc post url
-httpc post 'http://httpbin.org/post'
2. httpc post -h key:value url
-httpc post -h key1:value1 key2:value2 'http://httpbin.org/post'
3. httpc post -h key:value -d "inline data" url
-httpc post -h key1:value1 key2:value2 -d '{"Assignment": 1}' 'http://httpbin.org/post'
4. httpc post -h key:value -f "file name" url
-httpc post -h key1:value1 key2:value2 -f Data.json 'http://httpbin.org/post'
5. httpc post -v url
-httpc post -v 'http://httpbin.org/post'
6. httpc post -v -h key:value url
-httpc post -v -h key1:value1 key2:value2 'http://httpbin.org/post'
7. httpc post -v -h key:value -d "inline data" url
-httpc post -v -h key1:value1 key2:value2 -d '{"Assignment": 1}' 'http://httpbin.org/post'
8. httpc post -v -h key:value -f "file name" url
-httpc post -v -h key1:value1 key2:value2 -f Data.json 'http://httpbin.org/post'


## Detail
1. On macOS, if user gives the txt file an empty name using command: "httpc -v 'http://httpbin.org/get?course=networking&assignment=1' -o .txt", the output txt file may become a hidden file. It is necessary to press shift + command + . to show and access hidden files. 
