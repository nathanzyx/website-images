package com.example.util;

import java.io.*;
import java.sql.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;

public class FileReadWriter {

    static public void saveCanvas(String name, String content, String canvas_password) {
        // NEW VERSION (MYSQL STORAGE SYSTEM)
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String user = "root";
        String password = "root";

//        String Cpassword = canvas_password != null ? "'" + canvas_password + "'" : "NULL";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);

            System.out.println("Connected to mySQL: " + url);

//            String query = "INSERT INTO data (id, content) VALUES ('nameHere', 'contentHere')";
            String query = "INSERT INTO data (id, content, password) VALUES ('" + name + "', '" + content + "', '" + canvas_password + "') ON DUPLICATE KEY UPDATE content = VALUES(content)";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);


        } catch (ClassNotFoundException e){
            System.out.println("Driver class not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL error.");
            e.printStackTrace();
//            throw new RuntimeException(e);
        }





//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
////        String url = "jdbc:mysql://localhost:3306/canvas";
//
//        String user = "root";
//        String password = "root";
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            try (Connection connection = DriverManager.getConnection(url, user, password)) {
//                System.out.println("Connection successful!");
//            }
//            System.out.println("Connection successful!");
//        } catch (ClassNotFoundException e) {
//            System.out.println("Driver class not found.");
//            e.printStackTrace();
//        } catch (SQLException e) {
//            System.out.println("SQL error.");
//            e.printStackTrace();
//        }



    }

    static public void saveFile(File dir, String name, String content) throws FileNotFoundException {

//        // NEW VERSION (MYSQL STORAGE SYSTEM)
//        String url = "jdbc:mysql://localhost:3306/canvas_storage";
//        String user = "root";
//        String password = "NATH-SEP9-2004-0001";
//
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(url, user, password);
//
//            System.out.println("Connected to mySQL: " + url);
//
//            //Insert data into mySQL database
//            String query = "insert into canvas_data values(\"nameHere\", \"contentHere\")";
//            Statement statement = connection.createStatement();
//            statement.executeQuery(query);
//
//        } catch (ClassNotFoundException e){
//            e.printStackTrace();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }


//         OLD VERSION (FILE STORAGE SYSTEM)
        File myFile = null;
        try {
            myFile = new File(dir, name);
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getPath());
            } else {
                System.out.println("File already exists. " + myFile.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (myFile != null){
            PrintWriter output = new PrintWriter(myFile);
            output.print(content);
            output.close();
        }
    }

    // Return whether the file exists or not
    static public boolean checkFile(File dir, String name) {
        boolean exists = new File(dir, name).exists();
        return exists;
    }

    // Reads and returns the specified file
    static public String readFile(File dir, String name) throws FileNotFoundException {





//         OLD VERSION (FILE RETRIEVAL SYSTEM)
        File myFile = null;
        try {
            myFile = new File(dir, name);
            if (myFile.createNewFile()) {
                return null;
            } else {

                FileReader fileInput = new FileReader(myFile);
                BufferedReader input = new BufferedReader(fileInput);

                // Read content
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = input.readLine()) != null) {
                    buffer.append(line);
                }
                String content = buffer.toString();

                return content;

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
