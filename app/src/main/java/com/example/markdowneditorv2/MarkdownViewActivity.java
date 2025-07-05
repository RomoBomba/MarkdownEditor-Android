package com.example.markdowneditorv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.markdowneditorv2.parser.MarkdownParser;
import java.util.List;

public class MarkdownViewActivity extends AppCompatActivity {
    private static final int EDIT_REQUEST_CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MarkdownViewActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        String markdownContent = getIntent().getStringExtra("MARKDOWN_CONTENT");
        final Uri fileUri = getIntent().getParcelableExtra("FILE_URI");
        final String sourceUrl = getIntent().getStringExtra("SOURCE_URL");

        LinearLayout container = findViewById(R.id.container);
        container.setPadding(16, 8, 16, 8);
        container.removeAllViews();

        MarkdownParser parser = new MarkdownParser(this);
        List<View> views = parser.parse(markdownContent);

        for (View view : views) {
            container.addView(view);
        }

        Button editButton = new Button(this);
        editButton.setText("Редактировать");
        editButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editButton.setOnClickListener(v -> {
            startEditActivity(markdownContent, fileUri, sourceUrl);
        });

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 32));

        container.addView(spacer);
        container.addView(editButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String editedContent = data.getStringExtra("EDITED_CONTENT");
            Uri fileUri = data.getParcelableExtra("FILE_URI");
            String sourceUrl = data.getStringExtra("SOURCE_URL");

            Intent refresh = new Intent(this, MarkdownViewActivity.class);
            refresh.putExtra("MARKDOWN_CONTENT", editedContent);
            refresh.putExtra("FILE_URI", fileUri);
            refresh.putExtra("SOURCE_URL", sourceUrl);
            refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(refresh);
            finish();
        }
    }

    private void startEditActivity(String content, Uri fileUri, String sourceUrl) {
        Intent editIntent = new Intent(this, MarkdownEditActivity.class);
        editIntent.putExtra("MARKDOWN_CONTENT", content);
        editIntent.putExtra("FILE_URI", fileUri);
        editIntent.putExtra("SOURCE_URL", sourceUrl);
        startActivityForResult(editIntent, EDIT_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}