# Key-Value-Store-With-History

It's a project to design and implement a key-value store with history.

## Introduction

The goal of this project is to design and implement a key-value store with history. Like a traditional key-value store, this store should support the following APIs:

1. *get(key)*: returns all values associated with the key, if present. 
2. *put(key, value):* adds or updates the key with the value.
3. *del(key)*: deletes the key from the store.
4. *del(key, value)*: deletes the specified value from the key.

In addition, this store should also support the following APIs:

3. *get(key, time)*: returns all values associated with the key up to the specified time.
4. *diff(key, time1, time2)*: returns the difference in value associated with the key between time1 and time2. *time1 <= time2*.

Besides, *get(key, time)* and *diff(key, time1, time2)* should retain the order in which the values are added to the given key.

For example:

| Time  | APIs | result |
| :------------- | :------------- | :------------- |
| 0       | put("A","c")       |     
| 1       | put("B","d")       |
| 2       | get("A")       | ["c"] |
| 3       | put("A","e")       |
| 4       | get("A")       | ["c","e"] |
| 5       | get("A", 2)       | ["c"] |
| 6       | del("A")       |
| 7       | get("A")       | [] |
| 8       | get("A", 5)       | ["c","e"] |
| 9       | put("B", "f")       |
| 10       | del("B", "d")       |
| 11      | get("B")       | ["f"] |
| 12      | diff("A", 1, 2)       | [] |
| 13      | diff("A', 3, 5)       | [] |
| 14      | diff("A", 1, 4)       | ["e"] |
| 15      | diff("B", 0, 1)       | ["d"] |

## Basic Idea
I assume the number of operations will be in range of integer. I use a Friend class to record friend info including name and created time, then use a users map to record all added friends info with created time and associate user-friend pair, a deleted map to record those friends who are deleted, and current friends map to record friends users have right now.

I use socket in Java to realize connection between Server and Client. 

## Files

1. *Server.java*: java file for Server
2. *Client.java*: java file for Client
3. *Service.java*: a Service class to realize those APIs
4. *Friend.java*: wrapping class of friends info
5. *ResultType.java*: my own class for better info transportation between server and client
6. *input.txt*: test input with above example

## Environment

I use MacOS, and bash.

## How to run

1. Open terminal to compile `Server.java` and `Client.java` respectively.
```
javac Server.java
javac Client.java
```

2. Run `Server` in server terminal first
```
java Server
```
When you see:

> Server starting at: [Date]

then it means server runs correctly

3. When server is running (you can check info on terminal), open **another terminal** to run `Client`:
```
java Client
```
When you see

> Connected server!

on *client terminal*, and

> Client connected now, server listening on port 5000

on *server terminal*, it means you run client correctly and they're connected.

4. Now you can type commands on **client** terminal to get and check results. Please follow a specific format to enter your commands. For example:
```
put A c # add c as friend of user A
get A # get all friends of A
get A 5 # get all friends of A before time 5
del A # delete all A's friends and A
del A c # delete c, a A's friend
diff A 1 4 # return the difference of friends associate user A between time 1 and time4
quit # quit program
```
