package com.example.markdowneditorv2.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.markdowneditorv2.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {
    private final LruCache<String, Bitmap> imageCache;

    public ImageLoader() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void loadImage(final String imageUrl, final ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            showErrorImage(imageView);
            return;
        }

        Bitmap cachedBitmap = imageCache.get(imageUrl);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        new Thread(() -> {
            try {
                String finalUrl = imageUrl;
                if (imageUrl.contains("github.com") && !imageUrl.contains("raw.githubusercontent.com")) {
                    finalUrl = imageUrl
                            .replace("github.com", "raw.githubusercontent.com")
                            .replace("/blob/", "/");
                }

                URL url = new URL(finalUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("ImageLoader", "HTTP error: " + connection.getResponseCode() + " for " + finalUrl);
                    showErrorImage(imageView);
                    return;
                }

                InputStream input = connection.getInputStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null) {
                    imageCache.put(imageUrl, bitmap);
                    imageView.post(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    });
                } else {
                    Log.e("ImageLoader", "Bitmap is null for: " + finalUrl);
                    showErrorImage(imageView);
                }
            } catch (Exception e) {
                Log.e("ImageLoader", "Error loading image: " + imageUrl, e);
                showErrorImage(imageView);
            }
        }).start();
    }

    private void showErrorImage(ImageView imageView) {
        imageView.post(() -> {
            imageView.setBackgroundColor(0xFFEEEEEE);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageResource(R.drawable.ic_broken_image);
        });
    }
}