# Pixelated Canvas Editor
***
## Project Information

### Group Members:

**- Nathan Tandory**

**- Ebrahim Shaikh**

**- Sanjith Krishnamoorthy**

### Description:

**This project uses web sockets to establish a live connection between a client and the 
server, allowing the client to draw on a pixelated canvas in real time with other users. Along
with the ability to draw on a live canvas, the server saves the canvas' data and password in a 
JSON file when all users have exited the live canvas room.**

### Canvas-Editor Demo Video (Click Link): [Demo Video](https://drive.google.com/file/d/1g3LkxhpK9xm9uOiwylukjoiesB4y1NNN/view?usp=sharing)

***
# Notes

### Loading a Canvas:

**Upon entering a name in the *__Name__* field of the main page, one of the three will occur:**

  - An existing canvas will be loaded in from the API *(if the canvas exists in the API)*


  - The user will join the live canvas *(if the canvas is live on the server)*


  - The server will create a new blank canvas *(if the canvas exists neither in the API nor the live server)* 

### Canvas Passwords:

**A canvas can be changed by anyone if it has no password, if it does have a password the user must enter it before editing.**

  - If a canvas has no password, you can set a password by using the *__Password__* field then either pressing
  *__Enter Password__* or using the *__Enter__* button on their keyboard.


  - When a canvas has a password, users who have not yet entered the password will be counted as *__Viewers__*
  and users who have entered the correct password and can edit the canvas are counted as *__Editors__*. You can see
  the number of viewers and editors on the right side of the canvas page along with the name of the canvas you are
  editing.


  - The canvas' password will be saved as a JSON object in the API when the canvas is closed.


  - A user who has not entered a canvas' password (i.e. a viewer), will be able to draw on the canvas, but no changes
  will be logged to either the server or other users.

### Editing the Canvas:

**You can select a color to draw with by clicking on the *__Color__* box below the canvas.**

    
***
## How To Run

### Clone project in IntelliJ IDEA
- Navigate to: *__File__* -> *__New__* -> *__Project from Version Control...__*
  - Use URL: https://github.com/OntarioTech-CS-program/w24-csci2020u-final-project-shaikh-tandory-krishnamoorthy-jagroop
  - Click: *__Clone__*
### Run Project
- Ensure GlassFish is Installed
- Right click *__pom.xml__* file in *__w24-csci2020u-assignment02-shaikh-louie-tandory-jagroop__* folder -> Click *__Add as Maven Project__* 
(If *__Add as Maven Project__* does not appear, this step has already been completed.)
- Navigate to *__File__* -> *__Settings...__* -> *__Build, Execution, Deployment__* -> *__Application Servers__*
  - Ensure GlassFish is added as an Application Server
- Navigate to *__Run__* -> *__Edit Configurations...__* -> Click the *__+__* symbol -> Click *__Local__* under *__GlassFish Server__*
  - use URL: http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/
  - use Server Domain: *domain1*
  - Click on *__Deployment__* -> Click the *__+__* symbol -> Click *__Artifact...__* -> Click *__WSChatServer:war exploded__*
  - Click *__Apply__* then click *__Ok__*
- Then click the play button in the top middle/right while GlassFish is selected (Wait approx. 1 min for the server to run)
- (If index.html does not automatically run) While server is running, right-click the *__login.html__* file in *__webapp__* folder and click *__Run 'login.html'__*
- To stop running the server, Click the red stop button

***
## Resources

### Gson library:
**The Gson library developed by Google was used in the 'close' function in CanvasServer.java class to convert a HashSet of canvas data 
into JSON format for the purpose of saving canvas data to the API when the canvas closes.**

-  **MVN Repository for Gson:**
  https://mvnrepository.com/artifact/com.google.code.gson/gson

***
## Contributions

**Nathan Tandory: (40%)**
- *Back-End:*
  - CanvasApplication.java
  - CanvasCheckResource.java
  - CanvasLogResource.java
  - CanvasAPIHandler.java
  - Canvas.java
  - CanvasServer.java
  - main.js

- *Front-End:*
  - index.html
  - style.css

- *Other:*
  - README.md

**Ebrahim Shaikh: (40%)**
- *Front-End:*
  - index.html
  - login.html
  - login.css
  - about.html
  - style.css

**Sanjith Krishnamoorthy: (20%)**
- *Front-End:*
  - style.css
  - login.html
