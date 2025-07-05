package com.example.markdowneditorv2.parser;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markdowneditorv2.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {
    private final Context context;
    private final TableParser tableParser;
    private final ImageLoader imageLoader;

    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[(.*?)\\]\\(\\s*(.*?)\\s*\\)");
    private static final Pattern TABLE_START_PATTERN = Pattern.compile("^\\|.*\\|$");

    public MarkdownParser(Context context) {
        this.context = context;
        this.tableParser = new TableParser(context);
        this.imageLoader = new ImageLoader();
    }

    public List<View> parse(String markdown) {
        List<View> views = new ArrayList<>();
        String[] lines = markdown.split("\n");
        int i = 0;

        while (i < lines.length) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                i++;
                continue;
            }

            Matcher imageMatcher = IMAGE_PATTERN.matcher(line);
            if (imageMatcher.find()) {
                String altText = imageMatcher.group(1);
                String imageUrl = imageMatcher.group(2);
                views.add(parseImage(imageUrl));
                i++;
                continue;
            }

            Matcher headerMatcher = HEADER_PATTERN.matcher(line);
            if (headerMatcher.find()) {
                views.add(parseHeader(headerMatcher.group(1).length(), headerMatcher.group(2)));
                i++;
                continue;
            }

            if (TABLE_START_PATTERN.matcher(line).matches()) {
                List<String> tableLines = new ArrayList<>();
                while (i < lines.length && TABLE_START_PATTERN.matcher(lines[i].trim()).matches()) {
                    tableLines.add(lines[i].trim());
                    i++;
                }

                if (!tableLines.isEmpty()) {
                    views.add(parseTable(tableLines));
                }
                continue;
            }

            TextView textView = new TextView(context);
            SpannableStringBuilder styledText = applyTextStyles(line);
            textView.setText(styledText);
            textView.setTextSize(16);
            textView.setPadding(0, 0, 0, 16);
            views.add(textView);
            i++;
        }

        return views;
    }

    private View parseHeader(int level, String text) {
        TextView tv = new TextView(context);
        tv.setText(text.trim());

        float[] sizes = {24f, 22f, 20f, 18f, 16f, 14f};
        if (level > 0 && level <= sizes.length) {
            tv.setTextSize(sizes[level - 1]);
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv.setPadding(0, 16, 0, 8);
        return tv;
    }

    private View parseImage(String imageUrl) {
        String decodedUrl = imageUrl.trim().replace(" ", "%20").replace("|", "%7C");

        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 16);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imageLoader.loadImage(decodedUrl, imageView);
        return imageView;
    }

    private View parseTable(List<String> tableLines) {
        TableParser tableParser = new TableParser(context);
        return tableParser.parseTable(tableLines);
    }

    public SpannableStringBuilder applyTextStyles(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        applyFormatting(builder, "\\*\\*(\\S(?:.*?\\S)?)\\*\\*", new StyleSpan(Typeface.BOLD));
        applyFormatting(builder, "__(\\S(?:.*?\\S)?)__", new StyleSpan(Typeface.BOLD));
        applyFormatting(builder, "~~(\\S(?:.*?\\S)?)~~", new StrikethroughSpan());
        applyFormatting(builder, "\\*(\\S(?:.*?\\S)?)\\*", new StyleSpan(Typeface.ITALIC));
        applyFormatting(builder, "_(\\S(?:.*?\\S)?)_", new StyleSpan(Typeface.ITALIC));

        return builder;
    }
    private void applyFormatting(SpannableStringBuilder builder, String regex, Object span) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(builder);

        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);

            builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int markerLength = regex.startsWith("\\*\\*") || regex.startsWith("__") || regex.startsWith("~~") ? 2 : 1;

            builder.delete(end, end + markerLength);
            builder.delete(matcher.start(0), matcher.start(0) + markerLength);

            matcher = pattern.matcher(builder);
        }
    }
}