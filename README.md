# Chat-Room
***
## Project Information

### Group Members:

**- Nathan Tandory**

**- Ebrahim Shaikh**

**- Sanjith Krishnamoorthy**

[//]: # (**- Shem Jagroop**)

### Description:

**This project uses web sockets to establish a live connection between a client and the 
server, allowing the client to chat in real time with other users. Along with the ability 
to create chat-rooms, a user has access to server commands within the chat-room for various 
real-world uses.**

### Chat-Room Demo Video (Click Link): [Demo Video](https://drive.google.com/file/d/1g3LkxhpK9xm9uOiwylukjoiesB4y1NNN/view?usp=sharing)

***
# Improvements

### Chat-Room Commands:
**Users in a chat-room have the ability to use chat commands, which are indicated by a forward slash '/' at the 
beginning of a message followed by the specified command. Commands can be seen on the right side of the chat-room 
web page. Steps have been taken to ensure improperly formatted commands are ignored.**

- `/users`
**This command returns all users currently in a chat-room. If a user in the chat-room has 
not entered a name for themselves (ie: user has not sent a message yet) they will be 
included in the count of "Anonymous" users.**
  - Example usage of */users* command:
    > */users*


- `/whisper`
**This command only sends a message to the user specified in the command. If a user whispers to a username
which does not exist in the chat-room, the user whispering will be notified that the user being whispered
to does not exist.**
  - Example usage of */whisper* command:
    > */whisper user1 hello how are you*
    
    In this case *"user1"* will receive the message *"hello how are you"*.


- `/rand`
  **This command returns to all users a random number between two numbers provided in the message.**
  - Examples usage of */rand* command:
    > */rand 1 10*

    > */rand 50 20*
    
    In these cases ***all*** users in the chatroom will receive a message containing the random number generated between 
    the provided numbers.

### Refresh Chat-Room List Button:
**A small improvement made to the web applications UI is the addition of a "↻" button which refreshes the
list of chat-rooms.**

### Send Chat Button:
**A small improvement made to the web applications UI is the addition of a "Send" button which sends a users
message.**

***
## How To Run

### Clone project in IntelliJ IDEA
- Navigate to: *__File__* -> *__New__* -> *__Project from Version Control...__*
  - Use URL: https://github.com/OntarioTech-CS-program/w24-csci2020u-assignment02-shaikh-louie-tandory-jagroop
  - Click: *__Clone__*
### Run Project
- Ensure GlassFish is Installed
- Right click *__pom.xml__* file in *__w24-csci2020u-assignment02-shaikh-louie-tandory-jagroop__* folder -> Click *__Add as Maven Project__* 
(If *__Add as Maven Project__* does not appear, this step has already been completed.)
- Navigate to *__File__* -> *__Settings...__* -> *__Build, Execution, Deployment__* -> *__Application Servers__*
  - Ensure GlassFish is added as an Application Server
- Navigate to *__Run__* -> *__Edit Configurations...__* -> Click the *__+__* symbol -> Click *__Local__* under *__GlassFish Server__*
  - use URL: http://localhost:8080/WSChatServer-1.0-SNAPSHOT/
  - use Server Domain: *domain1*
  - Click on *__Deployment__* -> Click the *__+__* symbol -> Click *__Artifact...__* -> Click *__WSChatServer:war exploded__*
  - Click *__Apply__* then click *__Ok__*
- Then click the play button in the top middle/right while GlassFish is selected (Wait approx. 1 min for the server to run)
- (If index.html does not automatically run) While server is running, right-click the *__index.html__* file in *__webapp__* folder and click *__Run 'index.html'__*
- To stop running the server, Click the red stop button

***
## Resources

### Gson library:
**The Gson library developed by Google was used in ChatRoomList.java class to convert a HashSet of room codes 
from ChatServlet.java class into JSON format for the purpose of displaying a list of rooms codes in the web app.**

-  **MVN Repository for Gson:**
  https://mvnrepository.com/artifact/com.google.code.gson/gson

***
## Contributions

**Nathan Tandory: (50%)**
- *Back-End:*
  - ChatServer.java
  - ChatRoomList.java
  - Chat-Room Commands
  - main.js

- *Other:*
  - README.md

**Ebrahim Shaikh: (35%)**
- *Front-End:*
  - index.html
  - about.html
  - style.css

**Jaydon Louie: (15%)**
- *Front-End:*
  - style.css
  - index.html
- *Back-End:*
  - main.js

**Shem Jagroop: (0%)**
