package com.kiwi.moon.coinz;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadFileTask extends AsyncTask<String, Void, String> {

    public DownloadCompleteRunner delegate = null;

    @Override
    protected String doInBackground(String... urls) {
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            return "Unable to load content. Check your network connection";
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    //Given a string representation of a URL, sets up a connection and gets an input stream
    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);   //10 seconds
        conn.setConnectTimeout(15000);   //15 seconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream) throws IOException {
        //Read input from stream, build result as a string
        return convertStreamToString(stream);
    }

    private String convertStreamToString(InputStream stream) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream), 4096);
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
        } catch (IOException e) {
            //Auto-generated catch block
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        delegate.downloadComplete(result);
    }
 }
