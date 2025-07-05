package com.example.markdowneditorv2;

import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import com.example.markdowneditorv2.parser.TableParser;

@RunWith(RobolectricTestRunner.class)
public class TableParserTest {

    @Test
    public void testSingleRowTable() {
        List<String> tableLines = Arrays.asList(
                "| Header1 | Header2 |",
                "|---|---|"
        );
        TableParser parser = new TableParser(RuntimeEnvironment.getApplication());
        View result = parser.parseTable(tableLines);

        assertTrue(result instanceof HorizontalScrollView);
        TableLayout table = (TableLayout) ((HorizontalScrollView) result).getChildAt(0);
        assertEquals(1, table.getChildCount());

        TableRow headerRow = (TableRow) table.getChildAt(0);
        assertEquals(2, headerRow.getChildCount());
        TextView headerCell = (TextView) headerRow.getChildAt(0);
        assertEquals("Header1", headerCell.getText().toString());
    }

    @Test
    public void testTableWithExtraSpaces() {
        List<String> tableLines = Arrays.asList(
                "|  Header1  | Header2   |",
                "|---|---|",
                "| Cell1   |    Cell2 |"
        );
        TableParser parser = new TableParser(RuntimeEnvironment.getApplication());
        View result = parser.parseTable(tableLines);

        TableLayout table = (TableLayout) ((HorizontalScrollView) result).getChildAt(0);
        assertEquals(2, table.getChildCount());

        TableRow headerRow = (TableRow) table.getChildAt(0);
        TextView headerCell = (TextView) headerRow.getChildAt(0);
        assertEquals("Header1", headerCell.getText().toString());

        TableRow dataRow = (TableRow) table.getChildAt(1);
        TextView dataCell = (TextView) dataRow.getChildAt(0);
        assertEquals("Cell1", dataCell.getText().toString());
    }

    @Test
    public void testTextAlignment() {
        List<String> tableLines = Arrays.asList(
                "| Header |",
                "|---|",
                "| Data |"
        );
        TableParser parser = new TableParser(RuntimeEnvironment.getApplication());
        View result = parser.parseTable(tableLines);

        TableLayout table = (TableLayout) ((HorizontalScrollView) result).getChildAt(0);
        TableRow headerRow = (TableRow) table.getChildAt(0);
        TextView headerCell = (TextView) headerRow.getChildAt(0);
        assertEquals(Gravity.CENTER, headerCell.getGravity());

        TableRow dataRow = (TableRow) table.getChildAt(1);
        TextView dataCell = (TextView) dataRow.getChildAt(0);
        assertEquals(Gravity.CENTER, dataCell.getGravity());
    }

    @Test
    public void testNonStandardSeparator() {
        List<String> tableLines = Arrays.asList(
                "| Header1 | Header2 |",
                "|-|-|",
                "| Cell1 | Cell2 |"
        );
        TableParser parser = new TableParser(RuntimeEnvironment.getApplication());
        View result = parser.parseTable(tableLines);

        TableLayout table = (TableLayout) ((HorizontalScrollView) result).getChildAt(0);
        assertEquals(2, table.getChildCount());
    }

    @Test
    public void testRowHeight() {
        List<String> tableLines = Arrays.asList(
                "| Header |",
                "|---|",
                "| Data |"
        );
        TableParser parser = new TableParser(RuntimeEnvironment.getApplication());
        View result = parser.parseTable(tableLines);

        TableLayout table = (TableLayout) ((HorizontalScrollView) result).getChildAt(0);
        TableRow headerRow = (TableRow) table.getChildAt(0);

        TextView sample = new TextView(RuntimeEnvironment.getApplication());
        sample.setTextSize(14);
        sample.setPadding(16, 8, 16, 8);
        sample.measure(0, 0);
        int expectedHeight = sample.getMeasuredHeight() + 20;

        assertEquals(expectedHeight, headerRow.getLayoutParams().height);
    }
}