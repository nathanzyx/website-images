package com.example.canvasapi;

import com.example.util.FileReadWriter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Path("/canvas")
public class CanvasLogResource {

    @GET
    @Path("/{canvasID}")
    @Produces("application/json")
    public Response getCanvas(@PathParam("canvasID") String canvasID) {
        URL url = this.getClass().getClassLoader().getResource("/canvas");
        String history = "";
        File mainDir = null;

        try {
            mainDir = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // load the file content into history
        try {
            history = FileReadWriter.readFile(mainDir, canvasID + ".json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Response myResp = Response.status(200) // success
                .header("Content-Type", "application/json")
                .entity(history) // adding the json data
                .build();
        return myResp;
    }

    @POST
    @Path("/{canvasID}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response saveCanvasInAPI(@PathParam("canvasID") String canvasID, String content) {

        // Parse content to JSONObject
        JSONObject mapper = new JSONObject(content);

        // Get the canvas name
        String filename = mapper.getString("room");

        // Get the canvas' password
        String password = mapper.getString("password");

        // get the canvas object which holds the canvas pixel data
        JSONObject logObject = mapper.getJSONObject("canvas");

        // Convert the canvas object to string
        String contentAsString = "{\"password\":\"" + password + "\", \"canvas\":" + logObject.toString() + "}";

        URL url = this.getClass().getClassLoader().getResource("/canvas");

        // Create a File object for the chat history folder
        File data = null;
        try {
            data = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            // Save the canvas to the canvasID.json file
            FileReadWriter.saveFile(data, canvasID + ".json", contentAsString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return a success response
        Response myResp = Response.status(200)
                .header("Content-Type", "application/json")
                .build();
        return myResp;
    }
}

