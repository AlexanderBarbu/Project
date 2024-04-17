package Utility;

import java.io.*;
import java.nio.file.*;

public class FileReader {

    public static String read(String filepath) throws FileNotFoundException {
        Path pathObject = Paths.get(filepath);
        if (!Files.exists(pathObject)) {
            throw new FileNotFoundException("FileReader: " + filepath + " not found.");
        }
        try {
            return new String(Files.readAllBytes(pathObject));
        } catch (IOException e) {
            return "";
        }
    }

}
