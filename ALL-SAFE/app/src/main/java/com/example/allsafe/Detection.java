package com.example.allsafe;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class Detection{

    private Context context;

    public Detection(Context context) {
        this.context = context;
    }

    public CompletableFuture<String> performApiCall(String message) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Start the VPN connection
        Intent vpnIntent = new Intent(context, ProtonVpnService.class);
        context.startService(vpnIntent);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String url = "https://api-function-111.azurewebsites.net/api/resume";
                JSONObject jsonPayload = new JSONObject();
                try {
                    jsonPayload.put("message", message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonPayload,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String value = response.getString("is_spam");
                                    System.out.println("Value associated with 'is_spam': " + value);

                                    // Disconnect from the VPN
                                    Intent vpnIntent = new Intent(context, ProtonVpnService.class);
                                    context.stopService(vpnIntent);

                                    future.complete(value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    future.completeExceptionally(e);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                future.completeExceptionally(error);
                            }
                        });

                // Set the timeout value for the request.
                int timeoutMilliseconds = 100000;
                request.setRetryPolicy(new DefaultRetryPolicy(
                        timeoutMilliseconds,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // Get the RequestQueue instance
                RequestQueue requestQueue = Volley.newRequestQueue(context);

                // Add the request to the RequestQueue
                requestQueue.add(request);
            }
        }, 2000); // Delay execution by 2 seconds to allow VPN connection

        return future;
    }
}
