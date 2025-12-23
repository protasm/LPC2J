package io.github.protasm.lpc2j.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

public final class ConsoleLineReader {
  private static final int ESC = 27;

  private final PushbackInputStream in;
  private final PrintStream out;
  private final List<String> history = new ArrayList<>();
  private int historyCursor = 0;

  public ConsoleLineReader(InputStream in, PrintStream out) {
    this.in = new PushbackInputStream(in, 3);
    this.out = out;
  }

  public String readLine(String prompt) {
    try {
      return readLineInternal(prompt);
    } catch (IOException e) {
      return null;
    }
  }

  public void recordHistory(String line) {
    if (line == null || line.isEmpty()) {
      return;
    }

    history.add(line);
  }

  private String readLineInternal(String prompt) throws IOException {
    StringBuilder buffer = new StringBuilder();
    historyCursor = history.size();

    out.print(prompt);
    out.flush();

    while (true) {
      int raw = in.read();
      if (raw == -1) {
        return null;
      }

      char ch = (char) raw;
      if (ch == '\r' || ch == '\n') {
        consumeTrailingNewline(ch);
        out.print(System.lineSeparator());
        out.flush();
        return buffer.toString();
      }

      if (ch == 127 || ch == '\b') {
        handleBackspace(buffer);
        continue;
      }

      if (ch == ESC) {
        if (handleEscapeSequence(buffer, prompt)) {
          continue;
        }
      }

      buffer.append(ch);
      out.print(ch);
      out.flush();
    }
  }

  private void consumeTrailingNewline(char ch) throws IOException {
    if (ch != '\r') {
      return;
    }

    int maybeLf = in.read();
    if (maybeLf != '\n' && maybeLf != -1) {
      in.unread(maybeLf);
    }
  }

  private void handleBackspace(StringBuilder buffer) {
    if (buffer.length() == 0) {
      return;
    }

    buffer.setLength(buffer.length() - 1);
    out.print("\b \b");
    out.flush();
  }

  private boolean handleEscapeSequence(StringBuilder buffer, String prompt) throws IOException {
    int next = in.read();
    if (next != '[') {
      if (next != -1) {
        in.unread(next);
      }
      return false;
    }

    int command = in.read();
    if (command == -1) {
      return false;
    }

    if (command == 'A') {
      recallHistory(buffer, prompt, -1);
      return true;
    }

    if (command == 'B') {
      recallHistory(buffer, prompt, 1);
      return true;
    }

    return false;
  }

  private void recallHistory(StringBuilder buffer, String prompt, int delta) {
    int newCursor = historyCursor + delta;
    if (newCursor < 0 || newCursor > history.size()) {
      return;
    }

    int previousLength = buffer.length();
    historyCursor = newCursor;

    String replacement = (historyCursor == history.size()) ? "" : history.get(historyCursor);
    buffer.setLength(0);
    buffer.append(replacement);

    redrawLine(prompt, buffer, previousLength);
  }

  private void redrawLine(String prompt, StringBuilder buffer, int previousLength) {
    out.print("\r");
    out.print(prompt);
    out.print(buffer);

    int clearCount = previousLength - buffer.length();
    if (clearCount > 0) {
      out.print(" ".repeat(clearCount));
      out.print("\r");
      out.print(prompt);
      out.print(buffer);
    }

    out.flush();
  }
}
