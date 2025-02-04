package com.example.mirror;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import fi.iki.elonen.NanoHTTPD;

public class MjpegServer extends NanoHTTPD {

    private final View rootView;

    public MjpegServer(int port, View rootView) {
        super(port);
        this.rootView = rootView;
    }

    @Override
    public Response serve(IHTTPSession session) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = captureScreen();

        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        }

        return newChunkedResponse(Response.Status.OK, "image/jpeg", new ByteArrayInputStream(stream.toByteArray()));
    }

    private Bitmap captureScreen() {
        if (rootView == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }
}
