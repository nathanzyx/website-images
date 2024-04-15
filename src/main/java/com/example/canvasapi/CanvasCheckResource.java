package com.example.canvasapi;

import com.example.util.FileReadWriter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@Path("/check_canvas")
public class CanvasCheckResource {

    // This function returns as a json object either true or false depending on if the file exists
    @GET
    @Path("/{canvasID}")
    @Produces("application/json")
    public Response checkCanvas(@PathParam("canvasID") String canvasID) {

        URL url = this.getClass().getClassLoader().getResource("/canvas");
        File mainDir = null;

        try {
            mainDir = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // See if file exists
        boolean exists = FileReadWriter.checkFile(mainDir, canvasID + ".json");

        JSONObject mapper = new JSONObject();
        mapper.put("room", canvasID);

        if(exists) {
            mapper.put("exists", "true");
        } else {
            mapper.put("exists", "false");
        }

        Response myResp = Response.status(200)
                .header("Content-Type", "application/json")
                .entity(mapper.toString())
                .build();
        return myResp;


    }

}
