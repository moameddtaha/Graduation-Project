package com.example.allsafe;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class SMS extends AppCompatActivity{

    String spamOrNot;
    TextView textView6;
    private Cursor cursor;
    private SearchView searchView;

    private ListView listView;

    // Content observer to listen for changes in SMS content provider
    private final ContentObserver smsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Retrieve new SMS messages
            READ_SMS();
            if (cursor != null && cursor.moveToFirst()) {
                String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                // Check if the message is spam or not
                Detection apiCall = new Detection(getApplicationContext());
                apiCall.performApiCall(message)
                        .thenApply(result -> {
                            onApiResult(result);
                            return result;
                        })
                        .thenAccept(result -> notification_message())
                        .exceptionally(e -> {
                            e.printStackTrace();
                            // Handle exception if needed
                            return null;
                        });
                notification_message();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        textView6 = findViewById(R.id.textView6);
        textView6.setGravity(Gravity.CENTER);
        textView6.setBackgroundResource(R.drawable.spam_textview);

        listView = findViewById(R.id.listView);
        listView.setVisibility(View.GONE);

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus(); // Because in lower version the app can add the hint text.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    listView.setVisibility(View.GONE); // Hide the ListView when the query is empty
                } else {
                    listView.setVisibility(View.VISIBLE); // Show the ListView when the user performs a search
                    filterMessages(newText);
                }
                return true;
            }
        });


        READ_SMS();

        // Register the content observer to listen for changes in SMS content provider
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);

    }

    private void filterMessages(String query) {
        if (cursor != null) {
            if (query.isEmpty()) {
                listView.setVisibility(View.GONE); // Hide the ListView if query is empty
            } else {
                listView.setVisibility(View.VISIBLE); // Show the ListView

                // Retrieve the filtered addresses
                Cursor filteredCursor = getContentResolver().query(
                        Uri.parse("content://sms"),
                        new String[]{"address"},
                        "address LIKE ?",
                        new String[]{"%" + query + "%"},
                        "date DESC"
                );

                if (filteredCursor != null) {
                    // Create a list to store the addresses
                    List<String> addresses = new ArrayList<>();

                    // Iterate through the filtered addresses cursor and add each address to the list
                    while (filteredCursor.moveToNext()) {
                        String address = filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("address"));
                        addresses.add(address);
                    }

                    // Close the cursor after retrieving the addresses
                    filteredCursor.close();

                    // Create an ArrayAdapter to display the addresses in the ListView
                    ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            addresses
                    );

                    listView.setAdapter(addressAdapter);

                    // Set item click listener for the ListView
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String selectedAddress = addresses.get(position);

                            displayMessagesForAddress(selectedAddress);

                        }
                    });

                }
            }
        }
    }

    private void displayMessagesForAddress(String address) {

        Cursor messageCursor = getContentResolver().query(
                Uri.parse("content://sms"),
                null,
                "address = ?",
                new String[]{address},
                "date DESC"
        );

        if (messageCursor != null) {
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1, // Use a layout with a single TextView
                    messageCursor,
                    new String[]{"body"}, // Use only the "body" column
                    new int[]{android.R.id.text1}, // Use the single TextView in the layout
                    0
            );

            listView.setAdapter(cursorAdapter);

            // Set item click listener for the ListView
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor selectedCursor = (Cursor) listView.getItemAtPosition(position);
                    if (selectedCursor != null) {
                        String message = selectedCursor.getString(selectedCursor.getColumnIndexOrThrow("body"));

                        // Check if the message is spam or not
                        Detection apiCall = new Detection(getApplicationContext());
                        apiCall.performApiCall(message)
                                .thenApply(result -> {
                                    onApiResult(result);
                                    return result;
                                })
                                .thenAccept(result -> notification_message())
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    // Handle exception if needed
                                    return null;
                                });

                        // Display the selected message
                        textView6.setText(spamOrNot);
                        textView6.setBackgroundResource(R.drawable.spam_textview);
                        if("spam".equals(spamOrNot)){
                            textView6.setBackgroundResource(R.drawable.spam_textview);
                            textView6.setTextColor(Color.RED);
                        }else{
                            textView6.setBackgroundResource(R.drawable.spam_textview);
                            textView6.setTextColor(Color.GREEN);
                        }
                    }
                }
            });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the content observer when the activity is destroyed
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    public void READ_SMS(){
        cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, "date DESC");
    }

    public void onApiResult(String result) {
        // Update UI with API response
        textView6.setText(result);
        spamOrNot = result;
    }
    private void notification_message(){
        // Check if the result indicates spam
        if (spamOrNot.equals("spam")) {
            // Create a notification channel
            String channelId = "spam_notifications";
            String channelName = "Spam Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            // Create the notification
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.splash_removebg_preview)
                    .setContentTitle("Spam Alert")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            // Show the notification
            notificationManager.notify(1, builder.build());
        }
    }

}