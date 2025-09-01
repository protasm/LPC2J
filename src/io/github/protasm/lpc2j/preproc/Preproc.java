package io.github.protasm.lpc2j.preproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;

/**
 * Utility class that performs C-style preprocessing on LPC source
 * code.  This wraps the anarres-cpp library and exposes a simple
 * interface for clients.
 */
public final class Preproc {
    private Preproc() {
        // utility class
    }

    /**
     * Preprocesses the provided source string using the supplied system
     * and quote include paths.  The preprocessed source is returned as a
     * single string.
     */
    public static String preprocess(String source, String sysInclPath, String quoteInclPath) throws IOException {
        try (Preprocessor pp = new Preprocessor()) {
            pp.addInput(new StringLexerSource(source, true));
            pp.getSystemIncludePath().add(".");

            List<String> systemPaths = new ArrayList<>();
            systemPaths.add(sysInclPath);
            pp.setSystemIncludePath(systemPaths);

            List<String> quotePaths = new ArrayList<>();
            quotePaths.add(quoteInclPath);
            pp.setQuoteIncludePath(quotePaths);

            try (CppReader reader = new CppReader(pp)) {
                StringBuilder output = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    output.append((char) ch);
                }
                return output.toString();
            }
        }
    }
}
