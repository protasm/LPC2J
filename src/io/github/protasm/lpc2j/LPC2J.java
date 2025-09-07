package io.github.protasm.lpc2j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.token.TokenBuffer;
import io.github.protasm.lpc2j.token.TokenList;

public class LPC2J {
	public static ASTObject compile(String vPathStr) {
		FSSourceFile sf = parse(vPathStr, basePath);

		if (sf == null)
			return null;

		try {
			Compiler compiler = new Compiler("java/lang/Object");
			byte[] bytes = compiler.compile(sf.astObject());

			sf.setBytes(bytes);

			boolean success = basePath.write(sf);

			if (!success)
				throw new IllegalArgumentException();

			return sf;
		} catch (IllegalArgumentException e) {
			System.out.println("Error compiling fileName: " + vPathStr);

			return null;
		}
	}

	private static FSSourceFile parse(String vPathStr, VirtualFileServer basePath) {
		FSSourceFile sf = scan(vPathStr, basePath);

		if (sf == null)
			return null;

		try {
			Parser parser = new Parser();
			ASTObject astObject = parser.parse(sf.slashName(), sf.tokens());

			sf.setASTObject(astObject);

			return sf;
		} catch (ParseException | IllegalArgumentException e) {
			System.out.println("Error parsing fileName: " + vPathStr);
			System.out.println(e);

			return null;
		}
	}

	private static TokenBuffer scan(String source) {
		try {
			Path resolved = basePath.fileAt(vPathStr);

			if (resolved == null)
				throw new IllegalArgumentException();

			FSSourceFile sf = new FSSourceFile(resolved);

			boolean success = basePath.read(sf);

			if (!success)
				throw new IllegalArgumentException();

            Scanner scanner = new Scanner();
            Path absResolved = basePath.basePath().resolve(resolved);
            TokenBuffer tokens = scanner.scan(
        		source,
                basePath.basePath().toString(),
                absResolved.getParent().toString(),
                absResolved
            );
		} catch (IllegalArgumentException e) {
			System.out.println("Error scanning fileName: " + vPathStr);

			return null;
		}
	}
}