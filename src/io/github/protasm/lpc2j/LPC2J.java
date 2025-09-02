package io.github.protasm.lpc2j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.fs.FSBasePath;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.token.TokenList;

public class LPC2J {
	public static void main(String... args) {
		if (args.length != 1)
			die(2, "Usage: lpc2j <path>/file.lpc");

		Path in = Paths.get(args[0]).toAbsolutePath();
		FSBasePath basePath = new FSBasePath("/");

		if (!Files.isRegularFile(in))
			die(2, "Not found: " + in);

		if (!in.getFileName().toString().endsWith(".lpc"))
			die(2, "Expected a .lpc file: " + in);

		try {
			FSSourceFile sf = compile(in.toString(), basePath);

			if (sf != null)
				System.out.println(sf.classPath());

			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);

			die(1, "Compilation failed: " + ex.getMessage());
		}
	}

	public static FSSourceFile compile(String vPathStr, FSBasePath basePath) {
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
			System.out.println("Error compiling file: " + vPathStr);

			return null;
		}
	}

	private static FSSourceFile parse(String vPathStr, FSBasePath basePath) {
		FSSourceFile sf = scan(vPathStr, basePath);

		if (sf == null)
			return null;

		try {
			Parser parser = new Parser();
			ASTObject astObject = parser.parse(sf.slashName(), sf.tokens());

			sf.setASTObject(astObject);

			return sf;
		} catch (ParseException | IllegalArgumentException e) {
			System.out.println("Error parsing file: " + vPathStr);
			System.out.println(e);

			return null;
		}
	}

	private static FSSourceFile scan(String vPathStr, FSBasePath basePath) {
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
                        TokenList tokens = scanner.scan(sf.source(),
                                        basePath.basePath().toString(),
                                        absResolved.getParent().toString(),
                                        absResolved);

			sf.setTokens(tokens);

			return sf;
		} catch (IllegalArgumentException e) {
			System.out.println("Error scanning file: " + vPathStr);

			return null;
		}
	}

	private static void die(int code, String msg) {
		System.err.println(msg);

		System.exit(code);
	}
}