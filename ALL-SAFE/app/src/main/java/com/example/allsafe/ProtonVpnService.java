package com.example.allsafe;

import android.app.Notification;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class ProtonVpnService extends VpnService {
    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startVpn();
        return START_STICKY;
    }

    private void startVpn() {
        try {
            // Create a VPN interface
            Builder builder = new Builder();
            builder.setSession(getString(R.string.app_name));
            builder.addAddress("10.0.0.2", 32);
            builder.addDnsServer("8.8.8.8");
            builder.addRoute("0.0.0.0", 0);
            vpnInterface = builder.establish();

            // Start the VPN connection
            if (vpnInterface != null) {
                startForeground(1, createNotification());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification() {
        return null; // Replace with your implementation
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }

    private void stopVpn() {
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
