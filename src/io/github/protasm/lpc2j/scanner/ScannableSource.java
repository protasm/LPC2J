package io.github.protasm.lpc2j.scanner;

import io.github.protasm.lpc2j.sourcepos.LineMap;
import io.github.protasm.lpc2j.sourcepos.SourcePos;
import io.github.protasm.lpc2j.sourcepos.SourceSpan;

/**
 * Lightweight wrapper around source text used by {@link Scanner}.
 * 
 * It maintains a current head index and a tail index marking the start of the
 * current lexeme. The line/column information is provided via {@link LineMap}
 * and exposed as {@link SourcePos} objects.
 */
class ScannableSource {
    private final String source;
    private final LineMap map;
    private int head;
    private int tail;

    ScannableSource(String fileName, String source) {
        this.source = source;

        this.map = new LineMap(fileName, source);
        this.head = 0;
        this.tail = 0;
    }

    boolean atEnd() {
        return head >= source.length();
    }

    void syncTailHead() {
        tail = head;
    }

    char consumeOneChar() {
        return atEnd() ? '\0' : source.charAt(head++);
    }

    boolean match(char expected) {
        if (peek() != expected)
            return false;

        head++;

        return true;
    }

    char peek() {
        return atEnd() ? '\0' : source.charAt(head);
    }

    char peekNext() {
        return ((head + 1) < source.length()) ? source.charAt(head + 1) : '\0';
    }

    char peekPrev() {
        return ((head - 1) >= 0) ? source.charAt(head - 1) : '\0';
    }

    void advance() {
        if (!atEnd())
            head++;
    }

    boolean advanceTo(char ch) {
        while (!atEnd() && (peek() != ch))
            head++;

        return !atEnd();
    }

    void advancePast(char ch) {
        if (advanceTo(ch))
            advance();
    }

    String read() {
        return source.substring(tail, Math.min(head, source.length()));
    }

    String readTrimmed() {
        int start = Math.min(tail + 1, source.length());
        int end = Math.max(start, Math.min(head - 1, source.length()));

        return source.substring(start, end);
    }

    SourcePos pos() {
        return map.posAt(tail);
    }

    SourceSpan span() {
        return SourceSpan.from(map, tail, head);
    }
}
