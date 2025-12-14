package io.github.protasm.lpc2j;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.token.TokenList;

class LPC2JPipelineTest {
    @Test
    void scanParseCompileFromString() {
        String source = "int square(int n) { return n*n; }\n" +
                "int foo() { int r = square(7); return r; }";

        TokenList tokens = LPC2J.scan(source);
        assertNotNull(tokens);
        assertTrue(tokens.size() > 0);

        ASTObject ast = LPC2J.parse(tokens);
        assertNotNull(ast);

        byte[] bytes = LPC2J.compile(ast);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }
}
