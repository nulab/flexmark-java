package com.vladsch.flexmark.ext.admonition;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.mappers.SpecialLeadInHandler;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

final public class AdmonitionParserTest {
    String escape(String input, Parser parser) {
        BasedSequence baseSeq = BasedSequence.of(input);
        List<SpecialLeadInHandler> handlers = Parser.SPECIAL_LEAD_IN_HANDLERS.get(parser.getOptions());
        StringBuilder sb = new StringBuilder();

        for (SpecialLeadInHandler handler : handlers) {
            if (handler.escape(baseSeq, null, sb::append)) return sb.toString();
        }
        return input;
    }

    String unEscape(String input, Parser parser) {
        BasedSequence baseSeq = BasedSequence.of(input);
        List<SpecialLeadInHandler> handlers = Parser.SPECIAL_LEAD_IN_HANDLERS.get(parser.getOptions());
        StringBuilder sb = new StringBuilder();

        for (SpecialLeadInHandler handler : handlers) {
            if (handler.unEscape(baseSeq, null, sb::append)) return sb.toString();
        }
        return input;
    }

    @Test
    public void test_escape() {
        Parser parser = Parser.builder().extensions(Collections.singleton(AdmonitionExtension.create())).build();

        assertEquals("abc", escape("abc", parser));
        assertEquals("!", escape("!", parser));
        assertEquals("!!", escape("!!", parser));
        assertEquals("!!!!", escape("!!!!", parser));
        assertEquals("!!!-", escape("!!!-", parser));
        assertEquals("?", escape("?", parser));
        assertEquals("??", escape("??", parser));
        assertEquals("????", escape("????", parser));
        assertEquals("???-", escape("???-", parser));

        assertEquals("\\!!!", escape("!!!", parser));
        assertEquals("\\!!!+", escape("!!!+", parser));
        assertEquals("\\???", escape("???", parser));
        assertEquals("\\???+", escape("???+", parser));

        assertEquals("\\abc", unEscape("\\abc", parser));
        assertEquals("\\!", unEscape("\\!", parser));
        assertEquals("\\!!", unEscape("\\!!", parser));
        assertEquals("\\!!!!", unEscape("\\!!!!", parser));
        assertEquals("\\!!!-", unEscape("\\!!!-", parser));
        assertEquals("\\?", unEscape("\\?", parser));
        assertEquals("\\??", unEscape("\\??", parser));
        assertEquals("\\????", unEscape("\\????", parser));
        assertEquals("\\???-", unEscape("\\???-", parser));

        assertEquals("!!!", unEscape("\\!!!", parser));
        assertEquals("!!!+", unEscape("\\!!!+", parser));
        assertEquals("???", unEscape("\\???", parser));
        assertEquals("???+", unEscape("\\???+", parser));
    }

    @Test
    public void unterminatedJsonLikeTitleDoesNotStackOverflow() {
        String jsonish = ("\\\"recovery_seconds\\\":18353,\\\"start_date\\\":\\\"2026-03-28 23:36:11\\\",\\\"active\\\":1,").repeat(80);
        String markdown = "??? note \"" + jsonish + "\nbody\n";

        Parser parser = Parser.builder().extensions(Collections.singleton(AdmonitionExtension.create())).build();
        assertEquals(markdown, parser.parse(markdown).getChars().toString());
    }

    @Test
    public void validQuotedTitleStillRenders() {
        Parser parser = Parser.builder().extensions(Collections.singleton(AdmonitionExtension.create())).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Collections.singleton(AdmonitionExtension.create())).build();

        String markdown = "??? note \"title\\\"s\"\nbody\n";
        Node document = parser.parse(markdown);
        String html = renderer.render(document);

        assertEquals(markdown, document.getChars().toString());
        assertEquals(true, html.contains("body"));
        assertEquals(true, html.contains("title"));
    }
}
