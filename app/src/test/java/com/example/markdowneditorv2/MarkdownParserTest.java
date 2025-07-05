package com.example.markdowneditorv2;

import android.text.SpannableStringBuilder;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.example.markdowneditorv2.parser.MarkdownParser;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MarkdownParserTest {

    @Test
    public void testBoldFormatting_withDoubleAsterisk() {
        MarkdownParser parser = new MarkdownParser(null);
        SpannableStringBuilder result = parser.applyTextStyles("This is **bold** text");

        StyleSpan[] spans = result.getSpans(0, result.length(), StyleSpan.class);
        Assert.assertEquals(1, spans.length);
        Assert.assertEquals(Typeface.BOLD, spans[0].getStyle());

        int start = result.getSpanStart(spans[0]);
        int end = result.getSpanEnd(spans[0]);
        Assert.assertEquals("bold", result.subSequence(start, end).toString());
    }

    @Test
    public void testItalicFormatting_withAsterisk() {
        MarkdownParser parser = new MarkdownParser(null);
        SpannableStringBuilder result = parser.applyTextStyles("This is *italic* text");

        StyleSpan[] spans = result.getSpans(0, result.length(), StyleSpan.class);
        Assert.assertEquals(1, spans.length);
        Assert.assertEquals(Typeface.ITALIC, spans[0].getStyle());

        int start = result.getSpanStart(spans[0]);
        int end = result.getSpanEnd(spans[0]);
        Assert.assertEquals("italic", result.subSequence(start, end).toString());
    }

    @Test
    public void testStrikethroughFormatting() {
        MarkdownParser parser = new MarkdownParser(null);
        SpannableStringBuilder result = parser.applyTextStyles("This is ~~strike~~ text");

        StrikethroughSpan[] spans = result.getSpans(0, result.length(), StrikethroughSpan.class);
        Assert.assertEquals(1, spans.length);

        int start = result.getSpanStart(spans[0]);
        int end = result.getSpanEnd(spans[0]);
        Assert.assertEquals("strike", result.subSequence(start, end).toString());
    }
}
