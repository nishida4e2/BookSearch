package com.example.nishida.booksearch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

public class AccessService {

    public static String getXmlStringFromUrl_GET(String urlStr) throws Exception {

        final StringBuilder result = new StringBuilder();
        final URL url = new URL(urlStr);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.connect();
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                final InputStream in = con.getInputStream();
                final InputStreamReader inReader = new InputStreamReader(in);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                while((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) con.disconnect();
        }
        return result.toString();
    }
}
