package com.vladsch.flexmark.ext.toc;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimTocParserTest {
    @Test
    public void unterminatedJsonLikeTitleDoesNotStackOverflow() {
        String jsonish = ("\\\"recovery_seconds\\\":18353,\\\"start_date\\\":\\\"2026-03-28 23:36:11\\\",\\\"active\\\":1,").repeat(80);
        String markdown = "[TOC]: # \"" + jsonish + "\n";

        Parser parser = Parser.builder().extensions(Collections.singleton(SimTocExtension.create())).build();
        assertEquals(markdown, parser.parse(markdown).getChars().toString());
    }

    @Test
    public void validQuotedTitleStillRenders() {
        Parser parser = Parser.builder().extensions(Collections.singleton(SimTocExtension.create())).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Collections.singleton(SimTocExtension.create())).build();

        String markdown = "[TOC]: # \"title\\\"s\"\n\n# Heading\n";
        Node document = parser.parse(markdown);
        String html = renderer.render(document);

        assertEquals(markdown, document.getChars().toString());
        assertTrue(html.contains("Heading"));
    }
}
