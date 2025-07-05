package com.example.markdowneditorv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.markdowneditorv2.utils.FileUtils;

import java.io.IOException;

public class MarkdownEditActivity extends AppCompatActivity {
    private EditText editText;
    private Uri fileUri;
    private String sourceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MarkdownEditActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        editText = findViewById(R.id.editText);
        Button btnSave = findViewById(R.id.btn_save);

        String markdownContent = getIntent().getStringExtra("MARKDOWN_CONTENT");
        fileUri = getIntent().getParcelableExtra("FILE_URI");
        sourceUrl = getIntent().getStringExtra("SOURCE_URL");

        editText.setText(markdownContent);

        btnSave.setOnClickListener(v -> {
            String editedContent = editText.getText().toString();
            saveContent(editedContent);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("EDITED_CONTENT", editedContent);
            resultIntent.putExtra("FILE_URI", fileUri);
            resultIntent.putExtra("SOURCE_URL", sourceUrl);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void saveContent(String content) {
        try {
            if (fileUri != null) {
                FileUtils.saveTextToUri(this, fileUri, content);
            } else if (sourceUrl != null) {
                SharedPreferences prefs = getSharedPreferences("md_cache", MODE_PRIVATE);
                prefs.edit().putString(sourceUrl, content).apply();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    public void formatBoldAction(View view) {
        insertAroundSelection("**", "**");
    }
    public void formatItalicAction(View view) {
        insertAroundSelection("*", "*");
    }
    public void formatStrikethroughAction(View view) {
        insertAroundSelection("~~", "~~");
    }
    public void formatHeaderAction(View view) {
        int start = editText.getSelectionStart();
        editText.getText().insert(start, "# ");
        editText.setSelection(start + 2);
    }

    public void formatLinkAction(View view) {
        insertAroundSelection("[", "](url)");
    }

    public void formatImageAction(View view) {
        insertAroundSelection("![", "](https://example.com/image.jpg)");
        int pos = editText.getSelectionStart();
        editText.setSelection(pos - 18, pos - 6);
    }

    private void insertAroundSelection(String prefix, String suffix) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Editable editable = editText.getText();
        editable.insert(end, suffix);
        editable.insert(start, prefix);

        if (start == end) {
            editText.setSelection(start + prefix.length());
        } else {
            editText.setSelection(end + prefix.length() + suffix.length());
        }
    }
}