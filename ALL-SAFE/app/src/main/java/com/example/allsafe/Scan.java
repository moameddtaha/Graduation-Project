package com.example.allsafe;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Scan extends AppCompatActivity{

    private Button scanButton;
    private ListView numbersListView;
    private ListView predictionsListView;
    private ProgressBar progressBar;
    private List<String> numbersList;
    private List<String> predictionsList;
    private ArrayAdapter<String> numbersAdapter;
    private ArrayAdapter<String> predictionsAdapter;

    private ArrayList<String> numbers = new ArrayList<>();
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanButton = findViewById(R.id.scan);
        numbersListView = findViewById(R.id.listView1);
        predictionsListView = findViewById(R.id.listView2);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        numbersList = new ArrayList<>();
        predictionsList = new ArrayList<>();

        numbersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, numbersList);
        predictionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, predictionsList);

        numbersListView.setAdapter(numbersAdapter);
        predictionsListView.setAdapter(predictionsAdapter);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanMessages();
            }
        });

    }

    private void scanMessages() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            progressBar.setVisibility(View.VISIBLE);

            Detection apiCall = new Detection(getApplicationContext());

            int batchSize = 100;
            int totalMessages = cursor.getCount();
            int processedMessages = 0;

            while (processedMessages < totalMessages) {
                int remainingMessages = totalMessages - processedMessages;
                int currentBatchSize = Math.min(remainingMessages, batchSize);

                // Create a list of CompletableFuture to hold the API call futures
                List<CompletableFuture<String>> apiCallFutures = new ArrayList<>();

                for (int i = 0; i < currentBatchSize; i++) {
                    numbers.add(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                    message = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                    CompletableFuture<String> futureResult = apiCall.performApiCall(message);
                    apiCallFutures.add(futureResult);

                    cursor.moveToNext();
                    processedMessages++;
                }

                CompletableFuture<Void> allApiCallResults = CompletableFuture.allOf(apiCallFutures.toArray(new CompletableFuture[0]));

                allApiCallResults.thenRun(() -> {
                    for (int i = 0; i < apiCallFutures.size(); i++) {
                        int currectNumber = Integer.parseInt(numbers.get(i));

                        CompletableFuture<String> futureResult = apiCallFutures.get(i);
                        String result = futureResult.join(); // Retrieve the API call result
                        runOnUiThread(() -> onApiResult(result, currectNumber)); // Process the result on the UI thread
                    }

                    runOnUiThread(() -> {
                        // Notify the adapters that the data has changed
                        numbersAdapter.notifyDataSetChanged();
                        predictionsAdapter.notifyDataSetChanged();
                    });
                });
            }

            cursor.close();
        }
    }

    public void onApiResult(String result, int Number) {
        System.out.println(result);
        System.out.println(Number);
        if (result.equals("spam")) {
            // Add the number/name and prediction to the respective lists
            numbersList.add(String.valueOf(Number)); // Replace with actual number/name
            predictionsList.add(result);
        }
        progressBar.setVisibility(View.GONE);
    }

}