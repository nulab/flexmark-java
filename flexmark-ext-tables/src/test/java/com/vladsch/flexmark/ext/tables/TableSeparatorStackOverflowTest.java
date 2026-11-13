package com.vladsch.flexmark.ext.tables;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TableSeparatorStackOverflowTest {
    // The default thread stack size differs per platform, so the tests pin one to stay deterministic.
    private static final long STACK_SIZE_BYTES = 512 * 1024;
    private static final int COLUMNS = 5000;

    private static String renderOnSmallStack(String markdown, int minSeparatorDashes) throws InterruptedException {
        DataHolder options = new MutableDataSet()
                .set(TablesExtension.MIN_SEPARATOR_DASHES, minSeparatorDashes)
                .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()))
                .toImmutable();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        AtomicReference<String> html = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                html.set(renderer.render(parser.parse(markdown)));
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "table-separator-stack-overflow-test", STACK_SIZE_BYTES);
        thread.start();
        thread.join();

        assertNull("rendering must not fail: " + failure.get(), failure.get());
        return html.get();
    }

    private static String wideTable(String separatorCell) {
        StringBuilder header = new StringBuilder("|");
        StringBuilder separator = new StringBuilder("|");
        StringBuilder body = new StringBuilder("|");
        for (int i = 0; i < COLUMNS; i++) {
            header.append("h|");
            separator.append(separatorCell).append('|');
            body.append("v|");
        }
        return header + "\n" + separator + "\n" + body + "\n";
    }

    private static int count(String text, String needle) {
        int count = 0;
        for (int i = text.indexOf(needle); i >= 0; i = text.indexOf(needle, i + needle.length())) count++;
        return count;
    }

    @Test
    public void wideTable_plainSeparator() throws InterruptedException {
        String html = renderOnSmallStack(wideTable("---"), 3);
        assertEquals(COLUMNS, count(html, "<th>"));
        assertEquals(COLUMNS, count(html, "<td>"));
    }

    @Test
    public void wideTable_leftAlignedSeparator() throws InterruptedException {
        String html = renderOnSmallStack(wideTable(":-"), 1);
        assertEquals(COLUMNS, count(html, "<th align=\"left\">"));
    }

    @Test
    public void wideTable_rightAlignedSeparator() throws InterruptedException {
        String html = renderOnSmallStack(wideTable("-:"), 1);
        assertEquals(COLUMNS, count(html, "<th align=\"right\">"));
    }

    @Test
    public void wideTable_centerAlignedSeparator() throws InterruptedException {
        String html = renderOnSmallStack(wideTable(" :-: "), 1);
        assertEquals(COLUMNS, count(html, "<th align=\"center\">"));
    }

    @Test
    public void mixedAlignment_isKept() throws InterruptedException {
        String html = renderOnSmallStack("" +
                "a|b|c|d\n" +
                ":--|:-:|--:|---\n" +
                "1|2|3|4\n", 3);
        assertTrue(html, html.contains("<th align=\"left\">a</th>"));
        assertTrue(html, html.contains("<th align=\"center\">b</th>"));
        assertTrue(html, html.contains("<th align=\"right\">c</th>"));
        assertTrue(html, html.contains("<th>d</th>"));
    }
}
