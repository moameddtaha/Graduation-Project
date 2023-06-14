package com.example.allsafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;




public class gmail_x extends AppCompatActivity{

    String spamOrNot;
    Switch aSwitch;
    TextView textView3;
    TextView textView6;
    private Cursor cursor;
    private SearchView searchView;

    private ListView listView;

    users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_x);

        user = new users();

        aSwitch = findViewById(R.id.switch1);
        textView3 = findViewById(R.id.textView3);
        textView6 = findViewById(R.id.textView6);
        textView6.setGravity(Gravity.CENTER);
        textView6.setBackgroundResource(R.drawable.spam_textview);

        listView = findViewById(R.id.listView);

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus(); // Because in lower version the app can add the hint text.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

    }

    public void onSwitchClick(View view) {

        if(aSwitch.isChecked()){
            Toast.makeText(getApplicationContext(), "ON", Toast.LENGTH_SHORT).show();

            /*
            List<Message> messages = new ArrayList<>();
            String ID = user.getPersonId();

            // Print the labels in the user's account.
            String user = "me";
            ListLabelsResponse listResponse = service.users().labels().list(user).execute();
            List<Label> labels = listResponse.getLabels();
            if (labels.isEmpty()) {
                System.out.println("No labels found.");
            } else {
                System.out.println("Labels:");
                for (Label label : labels) {
                    System.out.printf("- %s\n", label.getName());
                }
            }

             */


        }else{
            Toast.makeText(getApplicationContext(), "OFF", Toast.LENGTH_SHORT).show();

        }

    }
}