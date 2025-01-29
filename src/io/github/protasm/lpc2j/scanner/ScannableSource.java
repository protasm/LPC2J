package io.github.protasm.lpc2j.scanner;

public class ScannableSource {
    private static final char EOL = '\n';
    private static final char NULL_CHAR = '\0';
    private String source;
    private int head, tail;
    private int line;

    public ScannableSource(String source) {
	this.source = source;

	reset();
    }

    public void advance() {

	if (peek() == EOL)
	    line++;

	head++;
    }

    public boolean advancePast(char c) {
	advanceTo(c);

	advance();

	return atEnd();
    }

    public boolean advanceTo(char c) {
	while (peek() != c && !atEnd())
	    advance();

	return !atEnd();
    }

    public boolean atEnd() {
	return head >= source.length();
    }

    public boolean atStart() {
	return head == 0;
    }

    public char consumeOneChar() {
	char c = peek();

	advance();

	return c;
    }

    public int head() {
	return head;
    }

    public int line() {
	return line;
    }

    public boolean match(char expected) {
	if (peek() != expected)
	    return false;

	advance();

	return true;
    }

    public char nextCharOnLine() {
	int scout = head;
	char c;

	do
	    c = source.charAt(scout++);
	while (Character.isWhitespace(c));

	return c;
    }

    public char peek() {
	if (atEnd())
	    return NULL_CHAR;

	return source.charAt(head);
    }

    public char peekNext() {
	if (head + 1 >= source.length())
	    return NULL_CHAR;

	return source.charAt(head + 1);
    }

    public char peekPrev() {
	if (atStart() || atEnd())
	    return NULL_CHAR;

	return source.charAt(head - 1);
    }

    public String read() {
	return source.substring(tail, head);
    }

    public String readTrimmed() {
	return source.substring(tail + 1, head - 1);
    }

    public void reset() {
	head = 0;
	tail = 0;
	line = 1;
    }

    public String source() {
	return source;
    }

    public void syncTailHead() {
	tail = head;
    }

    public int tail() {
	return tail;
    }

    @Override
    public String toString() {
	return source;
    }
}
