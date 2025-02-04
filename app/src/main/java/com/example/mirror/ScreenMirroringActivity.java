package com.example.mirror;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Collections;

public class ScreenMirroringActivity extends Activity {
    private static final int REQUEST_CODE = 100;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private MjpegServer server;
    private int httpPort;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        tvStatus = findViewById(R.id.tv_status);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        btnStart.setOnClickListener(view -> {
            tvStatus.setText("Iniciando mirroring...");
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            startScreenMirroring();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScreenMirroring() {
        View rootView = getWindow().getDecorView().getRootView();
        httpPort = findOpenPort(8080);
        server = new MjpegServer(httpPort, rootView);
        try {
            server.start();
            String ip = getIPAddress();
            tvStatus.setText("Transmisión en: http://" + ip + ":" + httpPort + "/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getIPAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) { }
        return "127.0.0.1";
    }

    private int findOpenPort(int startPort) {
        for (int port = startPort; port <= startPort + 20; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) { }
        }
        return startPort;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
    }
}
