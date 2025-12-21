package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.PreprocessedSource;
import io.github.protasm.lpc2j.preproc.PreprocessException;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.sourcepos.SourceMapper;
import io.github.protasm.lpc2j.sourcepos.SourcePos;

public class CmdPreprocess extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        if (args.length == 0) {
            System.out.println("Usage: preprocess <source fileName>");

            return true;
        }

        VirtualFileServer vfs = console.vfs();
        Path root = vfs.basePath();

        try {
            String vPathStr = pathStrOfArg(console.vPath(), args[0]);
            Path vPath = vfs.fileAt(vPathStr);

            if (vPath == null) {
                System.out.println("Invalid fileName: " + args[0]);

                return true;
            }

            Path sourcePath = root.resolve(vPath).normalize();
            String source = vfs.contentsOfFileAt(vPath.toString());

            if (source == null) {
                System.out.println("Unable to read fileName: " + args[0]);

                return true;
            }

            IncludeResolver resolver = console.includeResolver();
            Preprocessor pp = new Preprocessor(resolver);
            String displayPath = Path.of("/").resolve(vPath).normalize().toString();
            PreprocessedSource processed = pp.preprocessWithMapping(sourcePath, source, displayPath);

            System.out.print(withLineNumbers(processed));
        } catch (InvalidPathException e) {
            System.out.println("Error preprocessing fileName: " + args[0]);
            System.out.println(e.toString());
        } catch (PreprocessException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    @Override
    public String toString() {
        return "Preprocess <source fileName>";
    }

    private static String withLineNumbers(PreprocessedSource processed) {
        List<NumberedLine> lines = splitLines(processed.source(), processed.mapper());

        int maxCurrent = lines.isEmpty() ? 1 : lines.get(lines.size() - 1).currentLine();
        int maxOriginal = lines.stream().mapToInt(NumberedLine::originalLine).max().orElse(1);
        int width = Math.max(digits(maxCurrent), digits(maxOriginal));

        StringBuilder numbered = new StringBuilder();

        for (NumberedLine line : lines) {
            numbered
                .append('[')
                .append(paddedNumber(line.currentLine(), width))
                .append(',')
                .append(paddedNumber(line.originalLine(), width))
                .append("] ")
                .append(line.text());

            if (line.hasTrailingNewline())
                numbered.append('\n');
        }

        return numbered.toString();
    }

    private static List<NumberedLine> splitLines(String text, SourceMapper mapper) {
        List<NumberedLine> lines = new ArrayList<>();
        int lineStart = 0;
        int lineNumber = 1;

        for (int i = 0; i <= text.length(); i++) {
            boolean atEnd = (i == text.length()) || (text.charAt(i) == '\n');

            if (atEnd) {
                String lineText = text.substring(lineStart, i);
                boolean hasNewline = (i < text.length());
                int originalLine = representativeOriginalLine(mapper, lineStart, i);

                lines.add(new NumberedLine(lineNumber++, originalLine, lineText, hasNewline));
                lineStart = i + 1;
            }
        }

        return lines;
    }

    private static int representativeOriginalLine(SourceMapper mapper, int startOffset, int endOffset) {
        if (startOffset >= endOffset)
            return mapper.originalPos(startOffset).line();

        Map<OriginalLine, Integer> counts = new LinkedHashMap<>();
        int cursor = startOffset;

        while (cursor < endOffset) {
            SourcePos pos = mapper.originalPos(cursor);
            OriginalLine key = new OriginalLine(pos.fileName(), pos.line());
            int runEnd = cursor + 1;

            while (runEnd < endOffset) {
                SourcePos next = mapper.originalPos(runEnd);

                if (!key.matches(next))
                    break;

                runEnd++;
            }

            counts.merge(key, runEnd - cursor, Integer::sum);
            cursor = runEnd;
        }

        OriginalLine best = null;
        int bestCount = -1;

        for (Map.Entry<OriginalLine, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }

        return (best != null) ? best.line() : mapper.originalPos(startOffset).line();
    }

    private static String paddedNumber(int number, int width) {
        return String.format("%0" + width + "d", number);
    }

    private static int digits(int value) {
        return Integer.toString(Math.max(1, value)).length();
    }

    private record NumberedLine(int currentLine, int originalLine, String text, boolean hasTrailingNewline) {}

    private record OriginalLine(String fileName, int line) {
        boolean matches(SourcePos pos) {
            return fileName.equals(pos.fileName()) && (line == pos.line());
        }
    }
}
