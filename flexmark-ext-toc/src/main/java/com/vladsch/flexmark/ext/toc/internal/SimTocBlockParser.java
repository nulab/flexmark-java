package com.vladsch.flexmark.ext.toc.internal;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.util.Parsing;
import com.vladsch.flexmark.ext.toc.SimTocBlock;
import com.vladsch.flexmark.ext.toc.SimTocContent;
import com.vladsch.flexmark.ext.toc.SimTocOption;
import com.vladsch.flexmark.ext.toc.SimTocOptionList;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.misc.Pair;
import com.vladsch.flexmark.util.options.ParsedOption;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vladsch.flexmark.ext.toc.TocExtension.CASE_SENSITIVE_TOC_TAG;
import static com.vladsch.flexmark.parser.block.BlockStart.none;

public class SimTocBlockParser extends AbstractBlockParser {
    static class TocParsing extends Parsing {
        final Pattern TOC_BLOCK_START_PREFIX;
        //private static Pattern TOC_BLOCK_CONTINUE = Pattern.compile("^.+$");

        public TocParsing(DataHolder options) {
            super(options);
            if (CASE_SENSITIVE_TOC_TAG.get(options)) {
                this.TOC_BLOCK_START_PREFIX = Pattern.compile("^\\[TOC(?:\\s+([^\\]]+))?]:\\s*#");
            } else {
                this.TOC_BLOCK_START_PREFIX = Pattern.compile("^\\[(?i:TOC)(?:\\s+([^\\]]+))?]:\\s*#");
            }
        }
    }

    static int HAVE_HTML = 1;
    static int HAVE_HEADING = 2;
    static int HAVE_LIST = 4;
    static int HAVE_BLANK_LINE = 8;

    final private SimTocBlock block;
    final private TocOptions options;
    private int haveChildren = 0;
    private BasedSequence blankLineSpacer = BasedSequence.NULL;

    SimTocBlockParser(DataHolder options, BasedSequence tocChars, BasedSequence styleChars, BasedSequence titleChars) {
        this.options = new TocOptions(options, true);
        block = new SimTocBlock(tocChars, styleChars, titleChars);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // we stop on a blank line if blank line spacer is not enabled or we already had one
        if ((!options.isBlankLineSpacer || haveChildren != 0) && state.isBlank()) {
            return BlockContinue.none();
        } else {
            if (state.isBlank()) {
                haveChildren |= HAVE_BLANK_LINE;
                blankLineSpacer = state.getLine();
            }
            return BlockContinue.atIndex(state.getIndex());
        }
    }

    @Override
    public boolean canContain(ParserState state, BlockParser blockParser, Block block) {
        if (block instanceof HtmlBlock) {
            if ((haveChildren & ~HAVE_BLANK_LINE) == 0) {
                haveChildren |= HAVE_HTML;
                return true;
            }
        } else if (block instanceof Heading) {
            if ((haveChildren & ~HAVE_BLANK_LINE) == 0) {
                haveChildren |= HAVE_HEADING;
                return true;
            }
        } else if (block instanceof ListBlock) {
            if ((haveChildren & (HAVE_HTML | HAVE_LIST)) == 0) {
                haveChildren |= HAVE_LIST;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public void addLine(ParserState state, BasedSequence line) {

    }

    @Override
    public void closeBlock(ParserState state) {
        if (block.hasChildren()) {
            // move the children to a SimTocContent node
            SimTocContent tocContent = new SimTocContent();
            tocContent.takeChildren(block);
            tocContent.setCharsFromContent();

            if (blankLineSpacer.isNotNull()) {
                // need to extend the content node start to include the blank line
                tocContent.setChars(Node.spanningChars(blankLineSpacer, tocContent.getChars()));
            }

            block.appendChild(tocContent);
            block.setCharsFromContent();
            state.blockAddedWithChildren(tocContent);
        }

        // now add the options list and options with their text

        if (options.isAstAddOptions && !block.getStyle().isEmpty()) {
            SimTocOptionsParser optionsParser = new SimTocOptionsParser();
            Pair<TocOptions, List<ParsedOption<TocOptions>>> pair = optionsParser.parseOption(block.getStyle(), TocOptions.DEFAULT, null);
            List<ParsedOption<TocOptions>> options = pair.getSecond();
            if (!options.isEmpty()) {
                // add these
                SimTocOptionList optionsNode = new SimTocOptionList();
                for (ParsedOption<TocOptions> option : options) {
                    SimTocOption optionNode = new SimTocOption(option.getSource());
                    optionsNode.appendChild(optionNode);
                }

                optionsNode.setCharsFromContent();
                block.prependChild(optionsNode);
            }
        }

        block.setCharsFromContent();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
    }

    public static class Factory implements CustomBlockParserFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
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
        final private TocParsing myParsing;

        BlockFactory(DataHolder options) {
            super(options);
            this.myParsing = new TocParsing(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            BasedSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
            Matcher matcher = myParsing.TOC_BLOCK_START_PREFIX.matcher(line);
            if (matcher.find() && matcher.start() == 0) {
                BasedSequence tocChars = state.getLineWithEOL();
                BasedSequence styleChars = null;
                BasedSequence titleChars = null;
                if (matcher.start(1) != -1) {
                    styleChars = trySequence.subSequence(matcher.start(1) - nextNonSpace, matcher.end(1) - nextNonSpace);
                }

                int afterHash = matcher.end();
                int titleStart = Parsing.skipWhitespace(line, afterHash);
                if (titleStart < line.length()) {
                    if (titleStart == afterHash) {
                        // no whitespace between # and title — reject to preserve original behaviour
                        return none();
                    }
                    titleChars = Parsing.parseLinkTitle(line, titleStart);
                    if (titleChars == null || !line.subSequence(titleStart + titleChars.length(), line.length()).trim().isEmpty()) {
                        return none();
                    }
                    // adjust titleChars to be relative to trySequence for downstream consistency
                    titleChars = trySequence.subSequence(titleStart - nextNonSpace, titleStart - nextNonSpace + titleChars.length());
                } else if (!line.subSequence(afterHash, line.length()).trim().isEmpty()) {
                    return none();
                }

                SimTocBlockParser simTocBlockParser = new SimTocBlockParser(state.getProperties(), tocChars, styleChars, titleChars);
                return BlockStart.of(simTocBlockParser)
                        .atIndex(state.getLineEndIndex() + state.getLineEolLength())
                        //.replaceActiveBlockParser()
                        ;
            }
            return none();
        }
    }
}
