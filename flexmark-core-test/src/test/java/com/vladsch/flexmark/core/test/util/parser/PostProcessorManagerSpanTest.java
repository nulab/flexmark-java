package com.vladsch.flexmark.core.test.util.parser;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.DocumentPostProcessor;
import com.vladsch.flexmark.parser.block.DocumentPostProcessorFactory;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.testing.junit4.OpenTelemetryRule;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the per-processor span wraps {@code PostProcessorFactory.apply(...)}, so time spent
 * constructing a post processor is attributed to that processor's span.
 */
final public class PostProcessorManagerSpanTest {
    private static final AttributeKey<String> CLASS_ATTRIBUTE = AttributeKey.stringKey("markdown.post_processor.class");

    @Rule
    public final OpenTelemetryRule openTelemetry = OpenTelemetryRule.create();

    @Before
    public void installAsGlobal() {
        // PostProcessorManager obtains its tracer from GlobalOpenTelemetry, which the rule does not register.
        GlobalOpenTelemetry.resetForTest();
        GlobalOpenTelemetry.set(openTelemetry.getOpenTelemetry());
    }

    @After
    public void resetGlobal() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    public void nodePostProcessorFactoryIsAppliedInsideItsSpan() {
        RecordingNodePostProcessorFactory factory = new RecordingNodePostProcessorFactory();
        Parser.builder().postProcessorFactory(factory).build().parse("some text");

        assertFactoryAppliedInsideSpan(factory.spanCurrentDuringApply, RecordingNodePostProcessor.class);
    }

    @Test
    public void documentPostProcessorFactoryIsAppliedInsideItsSpan() {
        RecordingDocumentPostProcessorFactory factory = new RecordingDocumentPostProcessorFactory();
        Parser.builder().postProcessorFactory(factory).build().parse("some text");

        assertFactoryAppliedInsideSpan(factory.spanCurrentDuringApply, RecordingDocumentPostProcessor.class);
    }

    private void assertFactoryAppliedInsideSpan(Span spanCurrentDuringApply, Class<?> expectedProcessorClass) {
        assertNotNull("factory was not applied", spanCurrentDuringApply);
        List<SpanData> spans = openTelemetry.getSpans();
        assertEquals(1, spans.size());
        SpanData span = spans.get(0);
        assertEquals("markdown.parse.post_processor", span.getName());
        assertEquals("factory was applied outside the post_processor span", span.getSpanId(), spanCurrentDuringApply.getSpanContext().getSpanId());
        assertEquals(expectedProcessorClass.getName(), span.getAttributes().get(CLASS_ATTRIBUTE));
        assertTrue(span.hasEnded());
    }

    static class RecordingNodePostProcessorFactory extends NodePostProcessorFactory {
        Span spanCurrentDuringApply;

        RecordingNodePostProcessorFactory() {
            super(false);
            addNodes(Text.class);
        }

        @NotNull
        @Override
        public NodePostProcessor apply(@NotNull Document document) {
            spanCurrentDuringApply = Span.current();
            return new RecordingNodePostProcessor();
        }
    }

    static class RecordingNodePostProcessor extends NodePostProcessor {
        @Override
        public void process(@NotNull NodeTracker state, @NotNull Node node) {
        }
    }

    static class RecordingDocumentPostProcessorFactory extends DocumentPostProcessorFactory {
        Span spanCurrentDuringApply;

        @NotNull
        @Override
        public DocumentPostProcessor apply(@NotNull Document document) {
            spanCurrentDuringApply = Span.current();
            return new RecordingDocumentPostProcessor();
        }
    }

    static class RecordingDocumentPostProcessor extends DocumentPostProcessor {
        @NotNull
        @Override
        public Document processDocument(@NotNull Document document) {
            return document;
        }
    }
}
