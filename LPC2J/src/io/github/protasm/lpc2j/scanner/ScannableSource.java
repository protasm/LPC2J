package io.github.protasm.lpc2j.scanner;

public class ScannableSource {
  private static final char EOL = '\n';
  private static final char NULL_CHAR = '\0';
  private String source;
  private int head, tail;
  private int line;

  // ScannableSource(String)
  public ScannableSource(String source) {
    this.source = source;

    reset();
  }

  // advance()
  public void advance() {
    // This should be the only place head is incremented,
    // to ensure that line is also incremented as needed.
    if (peek() == EOL)
      line++;

    head++;
  }

  // advancePast(char)
  public boolean advancePast(char c) {
    advanceTo(c);

    advance();

    return atEnd();
  }

  // advanceTo(char)
  public boolean advanceTo(char c) {
    while (peek() != c && !atEnd())
      advance();

    return !atEnd();
  }

  // atEnd()
  public boolean atEnd() {
    return head >= source.length();
  }

  // atStart()
  public boolean atStart() {
    return head == 0;
  }

  // consumeOneChar()
  public char consumeOneChar() {
    char c = peek();

    advance();

    return c;
  }

  // head()
  public int head() {
    return head;
  }

  // line()
  public int line() {
    return line;
  }

  // match()
  public boolean match(char expected) {
    if (peek() != expected)
      return false;

    advance();

    return true;
  }

  // nextCharOnLine()
  public char nextCharOnLine() {
    int scout = head;
    char c;

    do {
      c = source.charAt(scout++);
    } while (Character.isWhitespace(c));

    return c;
  }

  // peek()
  public char peek() {
    if (atEnd())
      return NULL_CHAR;

    return source.charAt(head);
  }

  // peekNext()
  public char peekNext() {
    if (head + 1 >= source.length())
      return NULL_CHAR;

    return source.charAt(head + 1);
  }

  // peekPrev()
  public char peekPrev() {
    if (atStart() || atEnd())
      return NULL_CHAR;

    return source.charAt(head - 1);
  }

  // read()
  public String read() {
    // Read string from tail (inclusive) through
    // head (exclusive);
    // E.g. "foobar".substring(2, 5) == "oba".
    // If (tail == head) then this will return a
    // zero-length string.
    return source.substring(tail, head);
  }

  // readTrimmed()
  public String readTrimmed() {
    return source.substring(tail + 1, head - 1);
  }

  // reset()
  public void reset() {
    head = 0;
    tail = 0;
    line = 1;
  }

  // source()
  public String source() {
    return source;
  }

  // syncTailHead()
  public void syncTailHead() {
    tail = head;
  }

  // tail()
  public int tail() {
    return tail;
  }


  //toString()
  @Override
  public String toString() {
    return source;
  }
}
