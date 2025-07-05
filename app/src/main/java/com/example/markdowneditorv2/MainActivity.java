package com.example.markdowneditorv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.markdowneditorv2.utils.FileUtils;
import com.example.markdowneditorv2.utils.NetworkUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int FILE_PICK_REQUEST_CODE = 1001;
    private static final int VIEW_REQUEST_CODE = 1002;

    private EditText etUrl;
    private ProgressBar progressBar;

    private ListView savedFilesList;
    private ArrayAdapter<String> filesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        initializeUI();
        handleIntent(getIntent());

        savedFilesList = findViewById(R.id.saved_files_list);
        refreshFilesList();

        savedFilesList.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = filesAdapter.getItem(position);
            openSavedFile(fileName);
        });
    }

    private void refreshFilesList() {
        String[] files = fileList();
        List<String> mdFiles = new ArrayList<>();

        for (String file : files) {
            if (file.endsWith(".md")) {
                mdFiles.add(file);
            }
        }

        filesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mdFiles
        );

        savedFilesList.setAdapter(filesAdapter);
    }

    private void openSavedFile(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }

            br.close();
            isr.close();
            fis.close();

            Intent intent = new Intent(MainActivity.this, MarkdownViewActivity.class);
            intent.putExtra("MARKDOWN_CONTENT", content.toString());
            intent.putExtra("FILE_URI", (Uri) null);
            intent.putExtra("SOURCE_URL", "file://" + getFilesDir() + "/" + fileName);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка открытия файла: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeUI() {
        Button btnSelectFile = findViewById(R.id.btn_select_file);
        Button btnDownload = findViewById(R.id.btn_download);
        etUrl = findViewById(R.id.et_url);
        progressBar = findViewById(R.id.progress_bar);

        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnDownload.setOnClickListener(v -> downloadFromUrl());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && isMarkdownFile(uri)) {
                loadFromUrl(uri.toString());
            }
        }
    }

    private boolean isMarkdownFile(Uri uri) {
        String path = uri.getPath();
        return path != null && path.toLowerCase().endsWith(".md");
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE);
    }

    private void downloadFromUrl() {
        String url = etUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Введите URL файла", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        new DownloadTask().execute(url);
    }

    private void loadFromUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        new DownloadTask().execute(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            processSelectedFile(data.getData());
        } else if (requestCode == VIEW_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            handleEditResult(data);
        }
    }

    private void processSelectedFile(Uri fileUri) {
        try {
            String content = FileUtils.readTextFromUri(this, fileUri);
            startMarkdownViewActivity(content, fileUri, null);
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка чтения файла", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleEditResult(Intent data) {
        String editedContent = data.getStringExtra("EDITED_CONTENT");
        Uri fileUri = data.getParcelableExtra("FILE_URI");
        String sourceUrl = data.getStringExtra("SOURCE_URL");
        startMarkdownViewActivity(editedContent, fileUri, sourceUrl);
    }

    private void startMarkdownViewActivity(String content, Uri fileUri, String sourceUrl) {
        Intent intent = new Intent(MainActivity.this, MarkdownViewActivity.class);
        intent.putExtra("MARKDOWN_CONTENT", content);
        intent.putExtra("FILE_URI", fileUri);
        intent.putExtra("SOURCE_URL", sourceUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        private String url;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0 || urls[0] == null) return null;

            this.url = urls[0];
            try {
                return NetworkUtils.downloadText(url);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                String displayUrl = url.replace("https://", "").replace("http://", "");
                etUrl.setText(displayUrl);

                saveToLocalStorage(result, url);
                startMarkdownViewActivity(result, null, url);
            } else {
                Toast.makeText(
                        MainActivity.this,
                        "Ошибка загрузки. Проверьте URL и подключение",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void saveToLocalStorage(String content, String url) {
        try {
            String fileName = generateFileName(url);
            String filePath = getFilesDir() + "/" + fileName;
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();

            refreshFilesList();
            Toast.makeText(this,
                    "Файл сохранен:\n" + fileName +
                            "\nПуть: " + filePath,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String generateFileName(String url) {
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < url.length() - 1) {
            String name = url.substring(lastSlash + 1);
            int queryStart = name.indexOf('?');
            if (queryStart != -1) {
                name = name.substring(0, queryStart);
            }

            if (!name.endsWith(".md")) {
                name += ".md";
            }
            return name;
        }

        return "downloaded_" + System.currentTimeMillis() + ".md";
    }
}