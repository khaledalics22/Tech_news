package com.example.techapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * class QueryUtils
 * is responsible for retrieving reports,
 * parsing response,creating, and returning a List of objects
 *
 * @author Khaled Ali
 * @version 1 01 Dec. 2019
 */
public class QueryUtils {
    public static long pageCount = 1;

    private QueryUtils() {
    }

    public static URL createUrl(String url) {
        if (url != null && url.length() == 0) {
            return null;
        }
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlObj;
    }
    /** parse jsonString response into JSON objects and store data in list of reports*/
    public static List<TechReportClass> extractFeatures(Context context, String jsonString) throws IOException {
        if (jsonString == null) return null;
        List<TechReportClass> list = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONObject response = root.getJSONObject("response");
            pageCount = response.getLong("pages");
            JSONArray results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                Bitmap image ;
                JSONObject element = results.getJSONObject(i);
                JSONObject fields = element.optJSONObject("fields");
                if (fields != null) {
                    String imUrl = fields.getString("thumbnail");
                    image = extractImage(imUrl);
                } else {
                    image = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image_png_935205);
                }
                JSONArray tag = element.optJSONArray("tags");
                String authors = null;
                for (int k = 0; tag != null && k < tag.length(); k++) {
                    JSONObject authorInTag = tag.getJSONObject(k);
                    authors = authorInTag.optString("firstName");
                    authors += " " + authorInTag.optString("lastName") + ", ";
                }
                list.add(new TechReportClass(
                        element.getString("webTitle"),
                        element.getString("type"),
                        element.getString("webUrl"),
                        element.optString("webPublicationDate"),
                        element.getString("sectionName"),
                        image,
                        authors
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** fetch image of certain report using thumbnail url */
    public static Bitmap extractImage(String urlString) throws IOException {
        HttpURLConnection urlConnection ;
        InputStream inputStream ;
        Bitmap image = null;
        URL url = createUrl(urlString);
        if (url != null) {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200 /* status is ok */ ) {
                inputStream = urlConnection.getInputStream();
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }
            urlConnection.disconnect();
        }
        return image;
    }

    public static String changePage(String url , int current)
    {
        if(current==5/* load if max 20 page */)
            return null;
        String [] splitter= url.split("page=",2);
        String [] split2 = splitter[1].split("&",2);
        url=splitter[0]+"page="+current+"&"+split2[1];
        return url ;
    }

    /** fetches all data of an url */
    public static List<TechReportClass> makeHttpConnection(Context context, String urlString) throws IOException {
        List<TechReportClass> list= new ArrayList<>();
        for (int i =1 ; i<=pageCount;i ++) {
            // keep fetching if find pages

            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            String response = null;
            URL url = createUrl(urlString);
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200 /* status is ok */) {
                    inputStream = urlConnection.getInputStream();
                    response = getResponseString(inputStream);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null)
                    inputStream.close();
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            List<TechReportClass> temp = extractFeatures(context, response);
            if(temp!=null){
                list.addAll(temp);
                urlString = changePage(urlString,i);
                if(urlString==null /* 5 pages are loaded */ ) break;}
            else break;

        }
        return list;
    }

    public static String getResponseString(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            try {
                line = bufferedReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
