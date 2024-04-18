package com.example.webcanvasserver;


import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;

import java.util.*;

import static com.example.util.CanvasAPIHandler.saveCanvas;
import static com.example.util.CanvasAPIHandler.checkCanvasExists;
import static com.example.util.CanvasAPIHandler.getCanvas;


@ServerEndpoint(value="/ws/{canvasID}")
public class CanvasServer {

    // Static list of live canvas'
    private static List<Canvas> canvas = new ArrayList<Canvas>();


    // canvasID is a unique ID that can only be used on one canvas
    @OnOpen
    public void open(@PathParam("canvasID") String canvasID, Session session) throws IOException, EncodeException {
        String userId = session.getId();

        if(canvasID.strip().isEmpty() || canvasID.contains("/") || canvasID.contains(".")) {
            System.out.println("Ignored open request, due to improperly formatted");
            return;
        }



        // Initially assume canvas is not live
        Canvas liveCanvas = null;
        boolean canvasIsLive = false;

        for(Canvas c : canvas) {
            if(c.getCode().equals(canvasID)) {
                liveCanvas = c;
                canvasIsLive = true;
            }
        }

        // If canvas is live
        if(canvasIsLive) {
            System.out.println("Canvas was LIVE, user joined this canvas room.");

            // Add user to the live canvas
            liveCanvas.addUser(userId);

            // Load canvas for user
            loadCanvas(liveCanvas, session);

            //Send message if canvas is password protected
            if(!liveCanvas.tryPassword("")) {
                sendCannotEditMessage(session);
            }

            // Send updated number of editors and viewers to all users in the canvas
            sendGlobalEditorViewerCount(liveCanvas, session);
        }
        // If canvas is NOT live
        else {

            // If canvas is NOT live, but does exist in the API
            if(checkCanvasExists(canvasID)) {
                System.out.println("Canvas was not LIVE but did exist in the API, created new canvas in the server, and setting its grid.");

                // Make new live canvas
                Canvas newCanvas = new Canvas(canvasID, session.getId(), 20);

                // Add the new canvas to the list of live canvas'
                canvas.add(newCanvas);

                // Load canvas' data from the API
                newCanvas.setGrid(getCanvas(canvasID));

                // Load canvas for user
                loadCanvas(newCanvas, session);

                // Send updated number of editors and viewers to all users in the canvas
                sendGlobalEditorViewerCount(newCanvas, session);

                //Send message if canvas is password protected
                if(!newCanvas.tryPassword("")) {
                    sendCannotEditMessage(session);
                }
            }
            // If canvas is NOT live, and does not exist in the API
            else {
                System.out.println("Canvas was not LIVE and did not exist in the API, created new canvas in the server.");

                // Create new canvas
                Canvas newCanvas = new Canvas(canvasID, session.getId(), 20);

                // Add canvas to list of live canvas'
                canvas.add(newCanvas);

                // Load canvas for user
                loadCanvas(newCanvas, session);

                // Send updated number of editors and viewers to all users in the canvas
                sendGlobalEditorViewerCount(newCanvas, session);

            }
        }
    }

    // Loads canvas to specified user
    public void loadCanvas(Canvas canvas, Session session) throws IOException, EncodeException {
        JSONObject jsonData = new JSONObject(canvas.getCanvasData());
        String jsonString = jsonData.toString();
        session.getBasicRemote().sendText("{\"type\": \"canvasJSON\", \"data\":" + jsonString + "}");

        // Sends canvas' name to the user
        session.getBasicRemote().sendText("{\"type\": \"canvasName\", \"name\":\"" + canvas.getCode() + "\"}");
    }

    // Sends message containing number of editors and number of viewers to specified user
    public void sendEditorViewerCount(Canvas canvas, Session session) throws IOException, EncodeException {
        session.getBasicRemote().sendText("{\"type\": \"userCount\", \"editors\":\"" + canvas.getEditorCount() + "\", \"viewers\":\"" + canvas.getViewerCount() + "\"}");
    }

    public void sendGlobalEditorViewerCount(Canvas canvas, Session session) throws IOException, EncodeException {
        for(Session peer: session.getOpenSessions()) {
            if(canvas.inRoom(peer.getId())){
                peer.getBasicRemote().sendText("{\"type\": \"userCount\", \"editors\":\"" + canvas.getEditorCount() + "\", \"viewers\":\"" + canvas.getViewerCount() + "\"}");
            }
        }
    }

    // Sends message to specified user notifying that they cannot edit the canvas
    public void sendCannotEditMessage(Session session) throws IOException, EncodeException {
        session.getBasicRemote().sendText("{\"type\": \"warning\", \"message\":\"Enter the password to edit!\"}");
    }

    public void sendPixelChangeData(Session session, String message) throws IOException, EncodeException {
        session.getBasicRemote().sendText("{\"type\": \"pixel\", \"message\":\"" + message + " \"}");
    }



    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();

        Iterator<Canvas> iterator = canvas.iterator();
        while (iterator.hasNext()) {
            Canvas room = iterator.next();

            if (room.inRoom(userId)) {
                // Remove user from the canvas
                room.removeUser(userId);


                // Sends number of users in live canvas to all users (if the room is not empty)
                if(!room.getUsers().isEmpty()) {
                    sendGlobalEditorViewerCount(room, session);
                }

                if (room.getEditorCount() <= 0) {
                    System.out.println("Canvas has no more editors, saving canvas.");

                    // Save canvas data (turn canvas pixel data into JSON)
                    Gson gson = new Gson();
                    saveCanvas(room.getCode(), gson.toJson(room.getCanvasData()), room.getPassword());

                    // Use iterator to safely remove the canvas from the list
                    iterator.remove();
                }

            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        // Get users ID
        String userId = session.getId();
        // This will ensure the user exists in at least one room
        boolean inRoom = false;


        // Get all parts of the message sent by the user
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");

//        System.out.println(type + ": " + message);

        Canvas room = null;

        // Get the room the user is chatting in
        for (Canvas iterRoom : canvas) {
            if (iterRoom.inRoom(userId)) {
                room = iterRoom;
                inRoom = true;
            }
        }
        // return function if user does not exist
        if(!inRoom){
            return;
        }


        // Password
        if (type.equals("password")) {

            // If canvas does not have a password
            if(room.getPassword().isEmpty()) {
                System.out.println("Canvas had no password, setting password to: " + message);
                // Set the canvas' password
                room.setPassword(message);

                // Add user privilege to the user who entered the password
                boolean success = room.setUserPrivilege(userId, message);

                // Send message to user who set the password that the password has been set
                session.getBasicRemote().sendText("{\"type\": \"warning\", \"message\":\"Password Set!\"}");

                // Update number of users with editing privileges
                for(Session peer: session.getOpenSessions()) {
                    if(room.getUsers().containsKey(peer.getId())) {

                        // Update all users but the user who set the password that the canvas now has a password
                        if(!peer.getId().equals(userId)) {
                            sendCannotEditMessage(peer);
                        }
                    }
                }
                // Update number of editors and viewers for all users
                sendGlobalEditorViewerCount(room, session);

                // Send message to other users that canvas is now locked behind a password
            }
            // If canvas DOES have a password
            else {
                if(room.hasUserPrivilege(userId)) {
                    session.getBasicRemote().sendText("{\"type\": \"warning\", \"message\":\"You are already an editor!\"}");
                    return;
                }

                // If canvas does have a password, then the user is obviously trying to access the canvas
                boolean success = room.setUserPrivilege(userId, message);
                if(success) {

                    // Reload canvas to user (to erase changes that were not saved while the user was not authorized to edit the canvas)
                    loadCanvas(room, session);

                    // Send successful password entry to user
                    session.getBasicRemote().sendText("{\"type\": \"warning\", \"message\":\"You are now an editor!\"}");


                    // Update number of editors and viewers for all users
                    sendGlobalEditorViewerCount(room, session);

                } else {
                    session.getBasicRemote().sendText("{\"type\": \"warning\", \"message\":\"Incorrect password!\"}");
                }
            }
            return;
        }

        // If user has privileges to change a pixel || if canvas does not have a password
        if ((type.equals("pixel") && room.hasUserPrivilege(userId)) || room.tryPassword("")) {
            for (Session peer : session.getOpenSessions()) {
                if (room.inRoom(peer.getId()) && !peer.getId().equals(userId)) {
                    sendPixelChangeData(peer, message);
                }
            }

            // Add/Change data in canvas at that point
            String[] splitmsg = message.split(",");
            if (splitmsg.length == 3) {
                room.updatePixel(splitmsg[0], splitmsg[1], splitmsg[2]);
            }

            Map<String, String> tempCanvasData = room.getCanvasData();
            if (tempCanvasData != null) {

            }
        }
        // If user does NOT have privileges to change a pixel
        else {
            sendCannotEditMessage(session);
        }
    }
}