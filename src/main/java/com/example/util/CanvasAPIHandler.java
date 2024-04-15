package com.example.util;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CanvasAPIHandler {

    public static void saveCanvas(String canvasID, String canvasData, String password) throws IOException {
        String uriAPI = "http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/canvas/" + canvasID;
        URL url = new URL(uriAPI);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        // Construct canvas data for the canvas' file
        String jsonInputString = "{\"room\":\"" + canvasID + "\", \"password\":\"" + password + "\", \"canvas\":" + canvasData + "}";

        // Send canvas' data to the API
        System.out.println(jsonInputString);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("UTF-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get response from API
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Gets canvas data (coordinate-colors) and canvas password from the API as a HashMap
    public static HashMap<String, String> getCanvas(String roomID) throws IOException {
        String uriAPI = "http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/canvas/" + roomID;
        URL url = new URL(uriAPI);

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(false);
        con.setDoInput(true);

        InputStream inStream = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String jsonData = buffer.toString();

        System.out.println("load the data");
        System.out.println(jsonData);

        // Convert canvas file data into JSON object
        JSONObject jsonObject = new JSONObject(jsonData);

        // Get Specifically "canvas" from the JSON object since that has the color-grid data (ex: log:{"0,0":"#ffffff","0,1":"#ffffff"})
        JSONObject canvasObject = jsonObject.getJSONObject("canvas");

        String canvasString = jsonObject.getString("password");

        HashMap<String, String> map = new HashMap<>();

        // Put all "canvas" objects from the file into a hashmap (this is how the server keeps the grid-color data)
        for(String key : canvasObject.keySet()) {
            String v = canvasObject.getString(key);
            map.put(key, v);
        }
        map.put("password", canvasString);

        return map;
    }

    // checkCanvasExists returns as a json string whether the canvas being requested exists in the API
    public static boolean checkCanvasExists(String canvasID) throws IOException {
        String uriAPI = "http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/check_canvas/" + canvasID;
        URL url = new URL(uriAPI);

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(false);
        con.setDoInput(true);

        // Getting data
        InputStream inStream = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String jsonData = buffer.toString();

        System.out.println(jsonData);

        JSONObject data = new JSONObject(jsonData);
        Map<String, Object> mapData = data.toMap();
        String content = (String) mapData.get("exists");

        if(content.equals("true")) {
            return true;
        }

        return false;
    }
}
