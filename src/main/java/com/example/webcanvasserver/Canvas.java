package com.example.webcanvasserver;

import java.util.HashMap;
import java.util.Map;

public class Canvas {
    private String code;

    private String password;

    // First string is coordinates, second string is color
    private Map<String, String> canvas = new HashMap<String, String>();

    // Map of users and their ability to edit the canvas
    private Map<String, Boolean> users = new HashMap<String, Boolean>();

    private int dimension;

    public Canvas(String code, String user, int dimension){
        this.code = code;
        this.dimension = dimension;

        // Initial canvas has no password
        this.password = "";

        // initializes the canvas (always of size 20)
        this.canvas = blankCanvas();

        // Users by default cannot edit, hence false
        this.users.put(user, false);
    }

    // Makes and returns a blank canvas (of length & width of grid)
    public HashMap<String, String> blankCanvas() {
        HashMap<String, String> newCanvas = new HashMap<>();
        String coord;
        String color = "#ffffff";

        for(int x = 0; x < this.dimension; x++) {
            for(int y = 0; y < this.dimension; y++) {
                coord = x + "," + y;
                newCanvas.put(coord, color);
            }
        }
        return newCanvas;
    }

    public void updatePixel(String x, String y, String color) {
        if(Integer.parseInt(x) >= this.dimension || Integer.parseInt(y) >= this.dimension || Integer.parseInt(x) < 0 || Integer.parseInt(y) < 0) {
            return;
        }

        this.canvas.put(x+","+y, color);
    }

    public void setGrid(HashMap<String, String> grid) {
        for(Map.Entry<String, String> entry : grid.entrySet()) {
            if(entry.getKey().equals("password")) {
                this.password = entry.getValue();
            } else {
                this.canvas.put(entry.getKey(), entry.getValue());
            }
        }

    }

    public Map<String, String> getCanvasData() {
        return this.canvas;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Boolean> getUsers() {
        return users;
    }

    public boolean tryPassword(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Returns the count of editors
    public int getEditorCount() {
        int count = 0;
        // If canvas has no password (password = ""), then all users are able to edit (hence all users have privilege)
        if(this.password.equals("")) {
            return getUsers().size();
        }

        // If canvas has a password, then only return users able to edit
        for(Map.Entry<String, Boolean> entry : getUsers().entrySet()) {
            if(entry.getValue()) {
                count++;
            }
        }

        return count;
    }

    // Returns the canvas' password
    public String getPassword() {
        return this.password;
    }

    // Returns the count of viewers
    public int getViewerCount() {
        int count = 0;
        // If canvas has no password, then all users are able to edit (hence all users have privilege)
        if(this.password.equals("")) {
            return count;
        }

        // If canvas has a password, then only return users able to edit
        for(Map.Entry<String, Boolean> entry : getUsers().entrySet()) {
            if(!entry.getValue()) {
                count++;
            }
        }

        return count;
    }

    // Removes user from list of users
    public void removeUser(String userID){
        if(this.users.containsKey(userID)){
            this.users.remove(userID);
        }
    }

    public void addUser(String userID){
        this.users.put(userID, false);
    }

    // Updates the users privileges if the password given matches the canvas' password
    public boolean setUserPrivilege(String userID, String password){
        if(tryPassword(password)) {
            this.users.put(userID, true);
            return true;
        }
        return false;
    }

    // Returns true if the user can edit the canvas
    public boolean hasUserPrivilege(String userID) {
        return users.get(userID);
    }

    public boolean inRoom(String userID){
        return users.containsKey(userID);
    }
}
