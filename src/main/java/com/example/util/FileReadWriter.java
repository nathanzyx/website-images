package com.example.util;

import java.io.*;

public class FileReadWriter {

    static public void saveFile(File dir, String name, String content) throws FileNotFoundException {
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
