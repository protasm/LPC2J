package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.token.TokenList;

public class LPC2J {
    private static final String DEFAULT_PARENT = "java/lang/Object";

    public static TokenList scan(String source) {
        Scanner scanner = new Scanner();

        return scanner.scan(source);
    }

    public static ASTObject parse(TokenList tokens) {
        if (tokens == null)
            return null;

        try {
            Parser parser = new Parser();

            return parser.parse("<input>", tokens);
        } catch (ParseException | IllegalArgumentException e) {
            System.out.println("Error parsing tokens");
            System.out.println(e);

            return null;
        }
    }

    public static byte[] compile(ASTObject astObject) {
        if (astObject == null)
            return null;

        try {
            Compiler compiler = new Compiler(DEFAULT_PARENT);

            return compiler.compile(astObject);
        } catch (IllegalArgumentException e) {
            System.out.println("Error compiling ASTObject");

            return null;
        }
    }

    public static byte[] compile(String source) {
        TokenList tokens = scan(source);
        ASTObject astObject = parse(tokens);

        return compile(astObject);
    }
}
