package com.example.markdowneditorv2.parser;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.markdowneditorv2.R;

import java.util.ArrayList;
import java.util.List;

public class TableParser {
    private final Context context;

    public TableParser(Context context) {
        this.context = context;
    }

    public View parseTable(List<String> tableLines) {
        List<String[]> allRows = parseAllRows(tableLines);
        if (allRows.isEmpty()) {
            return new View(context);
        }

        int columnCount = getMaxColumnCount(allRows);

        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        scrollView.setHorizontalScrollBarEnabled(true);

        TableLayout table = new TableLayout(context);
        table.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        table.setPadding(0, 16, 0, 24);

        int rowHeight = calculateRowHeight();

        for (int i = 0; i < allRows.size(); i++) {
            String[] rowCells = allRows.get(i);
            TableRow row = createTableRow(rowCells, columnCount, i == 0, rowHeight);
            table.addView(row);
        }

        scrollView.addView(table);
        return scrollView;
    }

    private List<String[]> parseAllRows(List<String> tableLines) {
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableLines.size(); i++) {
            String line = tableLines.get(i).trim();
            String[] cells = parseTableRow(line);

            if (i == 1 && isSeparatorRow(cells)) continue;

            rows.add(cells);
        }
        return rows;
    }

    private int getMaxColumnCount(List<String[]> rows) {
        int maxColumns = 0;
        for (String[] row : rows) {
            if (row.length > maxColumns) {
                maxColumns = row.length;
            }
        }
        return maxColumns;
    }

    private int calculateRowHeight() {
        TextView sample = new TextView(context);
        sample.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sample.setPadding(16, 8, 16, 8);
        sample.measure(0, 0);
        return sample.getMeasuredHeight() + 20;
    }

    private String[] parseTableRow(String row) {
        String[] cells = row.split("\\|", -1);
        List<String> cleaned = new ArrayList<>();
        for (String cell : cells) {
            String trimmed = cell.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("---")) {
                cleaned.add(trimmed);
            }
        }
        return cleaned.toArray(new String[0]);
    }

    private boolean isSeparatorRow(String[] cells) {
        for (String cell : cells) {
            if (!cell.matches("^[-:]+$")) {
                return false;
            }
        }
        return true;
    }

    private TableRow createTableRow(String[] cells, int columnCount, boolean isHeader, int rowHeight) {
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, rowHeight));

        for (int i = 0; i < columnCount; i++) {
            TextView cell = new TextView(context);
            cell.setPadding(16, 8, 16, 8);
            cell.setGravity(Gravity.CENTER);
            cell.setSingleLine(false);

            if (i < cells.length) {
                cell.setText(cells[i]);
            } else {
                cell.setText("");
            }

            if (isHeader) {
                cell.setTypeface(null, Typeface.BOLD);
                cell.setBackgroundColor(0x22000000);
                cell.setTextSize(16);
            } else {
                cell.setBackgroundResource(R.drawable.table_cell_border);
                cell.setTextSize(14);
            }

            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            cell.setLayoutParams(params);

            row.addView(cell);
        }

        return row;
    }
}