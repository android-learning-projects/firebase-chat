package com.example.chatapp.utils;


import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by "Kathir" on 3/16/2017.
 */


public class EncodeBased64Binary {

    public static String encodeFileToBase64Binary(File fileName) throws IOException {


        String encoded = Base64.encodeToString(EncodeBased64Binary.loadFile(fileName), Base64.DEFAULT);
//        String encoded = Base64.encodeToString(pdfToByte(fileName), Base64.DEFAULT);
        String encodedString = new String(encoded);
        return encodedString;
    }

    public static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    public static byte[] pdfToByte(File file) {

//        File file = new File(filePath);
        FileInputStream fileInputStream;
        byte[] data = null;
        byte[] finalData = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            data = new byte[(int) file.length()];
            finalData = new byte[(int) file.length()];
            byteArrayOutputStream = new ByteArrayOutputStream();

            fileInputStream.read(data);
            byteArrayOutputStream.write(data);
            finalData = byteArrayOutputStream.toByteArray();

            fileInputStream.close();

        } catch (FileNotFoundException e) {
            Log.i("File not found", e.getMessage());
        } catch (IOException e) {
            Log.i("IO exception", e.getMessage());
        }

        return finalData;

    }
}
