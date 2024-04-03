package com.affixstudio.chatopen.GetData;

import static com.affixstudio.chatopen.ai.StartActivity.apiKeys;
import static com.affixstudio.chatopen.ai.StartActivity.rapidChatUrl;
import static com.affixstudio.chatopen.ai.StartActivity.sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.affixstudio.chatopen.ai.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class HttpRequest {

    ApiResponse apiResponse;
    public static HttpURLConnection connection;

    public HttpRequest(ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public void sendImage(Uri data, Context c, ApiResponse fileName)
    {
        try{

            Log.i("upload", "Upload started");

            OkHttpClient client = new OkHttpClient();

            InputStream imageStream = c.getContentResolver().openInputStream(data);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault());
            String filename = "IMG_" + sdf.format(new Date()) + ".png";

            fileName.response(filename); // send the file to show to user on ui


            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", filename,RequestBody.create(bitmapdata, MediaType.parse("image/png")))

                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(c.getString(R.string.imageRecognizarEndpoint))
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e)
                {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    try {


                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        } else {
                            assert response.body() != null;
                            String responseBody = response.body().string();
                            if (!responseBody.equals(c.getString(R.string.uploadImageErorrCode))) {
                                // Do something with the response.
                                Log.i("Image upload", "Image uploaded");
                                Log.i("Image upload", "response.body().string() " + responseBody);

                                apiResponse.response(responseBody);
                            } else {
                                // Handle error
                            }

                        }

                    }finally {
                        if (!Objects.isNull(response.body())) {
                            response.body().close();
                        }
                    }
                }
            });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteUploadedImage(String url)
    {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String json = "{\"imageUrl\": \"" + url + "\"}";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url("Delete uploaded image end point")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // Handle successful response
                    System.out.println("Image deleted");
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public  void Start(String urlString)
    {


      task task=new task();
      task.urlString=urlString;
      task.apiResponse=apiResponse;
      task.execute();


    }

    public  void StartPost(String urlString,String parameter)
    {


        taskPost task=new taskPost();
        task.parameter=parameter;
        task.urlString=urlString;
        task.apiResponse=apiResponse;
        task.execute();


    }


    static class taskPost extends AsyncTask<String, String, String>{

        StringBuffer content = new StringBuffer();
        String urlString;
        String parameter;
        ApiResponse apiResponse;


        @Override
        protected String doInBackground(String... strings) {
            try {

                // Set up the URL and the POST parameters
                URL url = new URL(urlString);


                // Open the connection and set up the request
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json; utf-8");

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(300 * 1000);
                conn.setReadTimeout(300 * 1000);

                connection=conn;
                // Write the POST parameters to the request body
                OutputStream os = conn.getOutputStream();
                os.write(parameter.getBytes());
                os.flush();
                os.close();

                // Read the response from the server
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                int responseCode = conn.getResponseCode();

                System.out.println("Response Code : " + responseCode);
                br.close();

                // Disconnect the connection
                conn.disconnect();


            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            apiResponse.response(content.toString());
            super.onPostExecute(s);
        }
    }


    static class task extends AsyncTask<String, String, String>{

        StringBuffer content = new StringBuffer();
        String urlString;
        ApiResponse apiResponse;


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(urlString.replace(" ","%20"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");


                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            apiResponse.response(content.toString());
            super.onPostExecute(s);
        }
    }


    @SuppressLint("StaticFieldLeak")
    public  void StartRapid(String query)
    {


        task1 task=new task1();
        task.query=query;
        task.apiResponse=apiResponse;
        task.execute();


    }
    static class task1 extends AsyncTask<String, String, String>{

        StringBuffer content = new StringBuffer();
        String query;
        ApiResponse apiResponse;


        @Override
        protected String doInBackground(String... strings) {
            try {
                String value = "{\r\n    \"query\": \""+query+"\"\r\n}";
                if (!sp.getString("conID","").isEmpty())
                {
                    value = "{\r\n  \"conversationId\":\""+sp.getString("conID","")+"\" , \"query\": \""+query+"\"\r\n}";
                }
                String urlString = "https://gpt-chatbotapi.p.rapidapi.com/ask";


                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("content-type", "application/json");
                con.setRequestProperty("X-RapidAPI-Key", apiKeys.get(0)/*"bf9db13786msh824b7b844d03656p16f5b2jsned587baf3ac2"*/);
                con.setRequestProperty("X-RapidAPI-Host", rapidChatUrl.get(0));

                // Set connection and read timeouts (30 seconds)
                con.setConnectTimeout(30 * 1000);
                con.setReadTimeout(30 * 1000);

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(value);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                // StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            apiResponse.response(content.toString());
            super.onPostExecute(s);
        }
    }
    public void postAssistantMessage(String json)
    {
        OkHttpClient client = getUnsafeOkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //String json = "{\"assistantMessage\":\"" + assistantMessage + "\"}";

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("http://192.101.68.44:5000")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    listenForMessages();
                    // do something with the response
                    apiResponse.response(response.message());
                    System.out.println(response.body().string());
                }
            }
        });
    }


    public  void listenForMessages() {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //String json = "{\"assistantMessage\":\"" + assistantMessage + "\"}";

       // RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("http://192.101.68.44:5000")
                //.post(body)
                .build();




        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                System.out.println("Stream opened: " + response);
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                System.out.println("Received event: " + data);

            }

            @Override
            public void onClosed(EventSource eventSource) {
                System.out.println("Stream closed.");
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                t.printStackTrace();
            }
        };

        EventSource eventSource = EventSources.createFactory(client).newEventSource(request, listener);

    }
    OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void phpCallForFastResponse(String json,String url)
    {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //String json = "{\"assistantMessage\":\"" + assistantMessage + "\"}";

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();




        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                System.out.println("Stream opened: " + response);
                System.out.println("Response code: " + response.code());
                try {
                    System.out.println("Response body: " + response.body().string());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                System.out.println("Received event: " + data);

                if (data.equals("100") || data.equals("200"))
                {
                    apiResponse.response(data);
                    return;
                }
                if (!isValidJson(data))
                {
                    return; // Skip processing if not valid JSON
                }
                apiResponse.response(data.replace("\\\\n","\n"));

            }

            @Override
            public void onClosed(EventSource eventSource) {
                System.out.println("Stream closed.");
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {

                t.printStackTrace();
            }
        };

        EventSource eventSource = EventSources.createFactory(client).newEventSource(request, listener);


    }

    // Check if a string is valid JSON
    private boolean isValidJson(String jsonString) {
        try {
            new JSONObject(jsonString);
            return true;
        } catch (JSONException e) {
            try {
                new JSONArray(jsonString);
                return true;
            } catch (JSONException ex) {
                return false;
            }
        }
    }



}
