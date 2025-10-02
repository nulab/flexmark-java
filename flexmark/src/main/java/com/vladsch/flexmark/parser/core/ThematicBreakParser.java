package com.vladsch.flexmark.parser.core;

import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThematicBreakParser extends AbstractBlockParser {

    /**
     * Checks if the given input matches the thematic break pattern without using regex
     * to avoid StackOverflowError with very long sequences.
     *
     * Pattern: ^(?:(?:\*[ \t]*){3,}|(?:_[ \t]*){3,}|(?:-[ \t]*){3,})[ \t]*$
     * - At least 3 occurrences of *, _, or - characters
     * - Characters can be separated by spaces or tabs
     * - Only one type of character allowed per line
     * - Line can end with spaces or tabs
     */
    private static boolean matchesThematicBreak(BasedSequence input) {
        int length = input.length();
        if (length == 0) return false;

        // Skip leading whitespace
        int pos = 0;
        while (pos < length && isWhitespace(input.charAt(pos))) {
            pos++;
        }

        if (pos >= length) return false;

        char patternChar = input.charAt(pos);
        if (patternChar != '*' && patternChar != '_' && patternChar != '-') {
            return false;
        }

        int charCount = 0;
        while (pos < length) {
            char c = input.charAt(pos);
            if (c == patternChar) {
                charCount++;
            } else if (isWhitespace(c)) {
                // Whitespace is allowed between pattern characters
            } else {
                // Invalid character found
                return false;
            }
            pos++;
        }

        return charCount >= 3;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    final private ThematicBreak block = new ThematicBreak();

    public ThematicBreakParser(BasedSequence line) {
        block.setChars(line);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public void closeBlock(ParserState state) {
        block.setCharsFromContent();
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // a horizontal rule can never container > 1 line, so fail to match
        return BlockContinue.none();
    }

    public static class Factory implements CustomBlockParserFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return new HashSet<>(Arrays.asList(
                    BlockQuoteParser.Factory.class,
                    HeadingParser.Factory.class,
                    FencedCodeBlockParser.Factory.class,
                    HtmlBlockParser.Factory.class
                    //ThematicBreakParser.Factory.class,
                    //ListBlockParser.Factory.class,
                    //IndentedCodeBlockParser.Factory.class
            ));
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return new HashSet<>(Arrays.asList(
                    //BlockQuoteParser.Factory.class,
                    //HeadingParser.Factory.class,
                    //FencedCodeBlockParser.Factory.class,
                    //HtmlBlockParser.Factory.class,
                    //ThematicBreakParser.Factory.class,
                    ListBlockParser.Factory.class,
                    IndentedCodeBlockParser.Factory.class
            ));
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @NotNull
        @Override
        public BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {
        final private ThematicBreakOptions options;

        BlockFactory(DataHolder options) {
            super(options);
            this.options = new ThematicBreakOptions(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4 || matchedBlockParser.getBlockParser().isParagraphParser() && !options.relaxedStart) {
                return BlockStart.none();
            }
            BasedSequence line = state.getLine();
            final BasedSequence input = line.subSequence(state.getNextNonSpaceIndex(), line.length());
            if (matchesThematicBreak(input)) {
                return BlockStart.of(new ThematicBreakParser(line.subSequence(state.getIndex()))).atIndex(line.length());
            } else {
                return BlockStart.none();
            }
        }
    }

    static class ThematicBreakOptions {
        final boolean relaxedStart;

        public ThematicBreakOptions(DataHolder options) {
            this.relaxedStart = Parser.THEMATIC_BREAK_RELAXED_START.get(options);
        }
    }
}
