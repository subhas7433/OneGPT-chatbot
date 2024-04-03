package com.affixstudio.chatopen.GetData;

import static com.affixstudio.chatopen.ai.StartActivity.apiKeys;
import static com.affixstudio.chatopen.ai.StartActivity.rapidApIkey;
import static com.affixstudio.chatopen.ai.StartActivity.sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RapidApiCall {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public void callBing(String s, ApiResponse apiResponse) {

        HttpRequest httpRequest=new HttpRequest(new ApiResponse() {
            @Override
            public void response(String response) {
                System.out.println(response.toString());
                sendSearchContent(response,apiResponse);
            }
        });
        httpRequest.Start("get search result end points"+s+"&cc="+sp.getString("cc",""));



    }

    @SuppressLint("StaticFieldLeak")
    public void callBing1(String s, ApiResponse apiResponse)
    {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://bing-web-search1.p.rapidapi.com/search?q="+s+"&textDecorations=false&textFormat=Raw")
                .get()
                .addHeader("X-BingApis-SDK", "true")
                .addHeader("X-RapidAPI-Key", "Rapid api key for bing search")
                .addHeader("X-RapidAPI-Host", "bing-web-search1.p.rapidapi.com")
                .build();
        final Response[] response = {null};
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {

                try {
                     response[0] = client.newCall(request).execute();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (response[0].isSuccessful())
                {
                    System.out.println("search Result  response[0] "+response[0]);
                    sendSearchContent(response[0].message(),apiResponse);
                }
            }
        }.execute();

    }

    void sendSearchContent(String json,ApiResponse apiResponse)
    {
        ArrayList<String> link=new ArrayList<>();
        System.out.println("search Result  "+json);
      try {
            JSONObject jsonObject = new JSONObject(json);
           JSONObject webPages = jsonObject.getJSONObject("webPages");
            JSONArray valueArray = webPages.getJSONArray("value");

            StringBuilder stringBuilder=new StringBuilder();
            for (int i = 0; i < valueArray.length(); i++) {
                JSONObject article = valueArray.getJSONObject(i);
               // String articleName = article.getString("name");
                String articleURL = article.getString("url");

                System.out.println("Article URL: " + articleURL);
                link.add(articleURL);
               // System.out.println("Article Description: " + articleDescription);

            }


          totalCalled=1;
          if (link.size()>0)
          {
              getAllSiteData(0,apiResponse,link);

          }

        }
         catch (Exception e)
        {
            apiResponse.response("There is an error try again.");
            e.printStackTrace();
        }

    }

    int totalCalled=1;
    StringBuilder allWebContent=new StringBuilder(); // all the web data which are got from webtomeaning

    public void getAllSiteData(int i, ApiResponse apiResponse,ArrayList<String> urls ) {
        ArrayList<String> aKeys=apiKeys;


        Random rand = new Random();
        int randomNum = rand.nextInt(aKeys.size());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String POST_PARAMS = "{ \"url\": \""+urls.get(i)+"\"\r\n}";
                   // String POST_PARAMS = "{ \"url\": \""+url+"\"\r\n}";
                    URL url = new URL("https://web2meaning.p.rapidapi.com/parse");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    // Headers
                    connection.setRequestProperty("content-type", "application/json");
                    connection.setRequestProperty("X-RapidAPI-Key",aKeys.get(randomNum));// chat gpt is just choosing the first web result
                    connection.setRequestProperty("X-RapidAPI-Host", "web2meaning.p.rapidapi.com");

                    // Sending post request
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(POST_PARAMS.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        in.close();

                        System.out.println(response.toString());


                        try {

                            JSONObject jsonObject = new JSONObject(response.toString());
                            String body = jsonObject.getString("body");

                            allWebContent.append(body).append("\n").append("Source:").append(url).append("\n");

                            if ((urls.size()-1)>i) // have more website url to call
                            {
                                getAllSiteData(i+1,apiResponse,urls);
                            }else {
                                apiResponse.response(allWebContent.toString());
                            }

                        } catch (JSONException e) {

                            throw new RuntimeException(e);
                        }





                    } else {
                        System.out.println("POST request not worked");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (totalCalled>=i) // have more website url to call
                    {
                        apiResponse.response(allWebContent.toString());
                    }

                }
            }
        });

    }



    public void autoSuggestion(String q, ApiResponse apiResponse, Context c)
    {


        executor.execute(new Runnable() {

            @Override
            public void run() {
                try
                {
//                    HttpResponse<String> response = Unirest.get("https://web-typeahead.p.rapidapi.com/search?q=who%20is%20&count=10&cc=us&setLang=en")
//                            .header("X-RapidAPI-Key", "641f07aa9amshdfa52f560d1f531p1cdb4cjsne27418d2761a")
//                            .header("X-RapidAPI-Host", "web-typeahead.p.rapidapi.com")
//                            .asString();
//                    System.out.println(response);


//                      String POST_PARAMS = "{ \"url\": \""+urls.get(i)+"\"\r\n}";
//                     String POST_PARAMS = "{ \"url\": \""+url+"\"\r\n}";

//                    String parms="q="+q+"&count=6&setLang=en";
//
//                    URL url = new URL("https://web-typeahead.p.rapidapi.com/search?"+parms);
//                    // Open a connection on the URL
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                    // Set the request method to GET
//                    connection.setRequestMethod("GET");
//
//                    // Headers
//
//                    connection.setRequestProperty("X-RapidAPI-Key",rapidApIkey/* "641f07aa9amshdfa52f560d1f531p1cdb4cjsne27418d2761a"*/);// chat gpt is just choosing the first web result
//                    connection.setRequestProperty("X-RapidAPI-Host", "web2meaning.p.rapidapi.com");
//
//                    // Get the response code
//                    int responseCode = connection.getResponseCode();
//
//                    // If the response code is 200 (HTTP_OK), fetch the input stream and print the response
//                    if(responseCode == HttpURLConnection.HTTP_OK) {
//                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                        String inputLine;
//                        StringBuffer response = new StringBuffer();
//
//                        while ((inputLine = in.readLine()) != null) {
//                            response.append(inputLine);
//                        }
//                        in.close();
//
//                        apiResponse.response(response.toString());
//                        // Print the response
//                        System.out.println(response.toString());
//                    }
//                    else {
//                        apiResponse.response("403");
//                        System.out.println("GET request did not work "+responseCode);
//                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();

                }
            }
        });
    }


    public void getSiteData(String s, ApiResponse apiResponse) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String POST_PARAMS = "{ \"url\": \""+s+"\"\r\n}";
                    URL url = new URL("https://web2meaning.p.rapidapi.com/parse");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    // Headers
                    connection.setRequestProperty("content-type", "application/json");
                    connection.setRequestProperty("X-RapidAPI-Key", "641f07aa9amshdfa52f560d1f531p1cdb4cjsne27418d2761a");// chat gpt is just choosing the first web result
                    connection.setRequestProperty("X-RapidAPI-Host", "web2meaning.p.rapidapi.com");

                    // Sending post request
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(POST_PARAMS.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        in.close();

                        System.out.println(response.toString());
                        sendSiteContent(response.toString(),apiResponse);
                    } else {
                        System.out.println("POST request not worked");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public  void sendSiteContent(String content,ApiResponse apiResponse)
    {
        System.out.println("content Result  "+content);
        try {

            JSONObject jsonObject = new JSONObject(content);
            String body = jsonObject.getString("body");
            apiResponse.response(body);
        } catch (JSONException e) {
            apiResponse.response("Can't get this article. Try sending other article urls.");
            throw new RuntimeException(e);
        }
    }







}
