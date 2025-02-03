package io.github.protasm.lpc2j.console;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.console.cmd.*;
import io.github.protasm.lpc2j.fs.FSClassFile;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.runtime.LPCObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Tokens;

public class Console {
	private Map<String, byte[]> objects;
	private final java.util.Scanner inputScanner;

	Map<String, Command> commands = Map.of(
			"c", new CmdCompile(),
			"h", new CmdHelp(),
			"l", new CmdLoad(),
			"o", new CmdListObjects(),
			"p", new CmdParse(),
			"s", new CmdScan(),
			"x", new CmdQuit() //
	);

	public Console() {
		objects = new HashMap<>();
		inputScanner = new java.util.Scanner(System.in);
	}

	public Map<String, byte[]> objects() {
		return objects;
	}

	public void repl() {
		while (true) {
			System.out.print("> ");

			String line = inputScanner.nextLine().trim();

			if (line.isEmpty())
				continue;

			String[] parts = line.split("\\s+");
			String command = parts[0];

			if (commands.containsKey(command)) {
				Command cmd = commands.get(command);
				parts = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

				boolean finished = cmd.execute(this, parts);

				if (finished)
					break;
			} else {
				System.out.println("Unrecognized command: '" + command + "'.");

				continue;
			}

//	    case "y": // Call Method
//		if (parts.length < 3) {
//		    System.out.println("Error: Insufficient arguments for call command.");
//		    
//		    break;
//		}
//
//		String[] callArgs = parts[2].split(" ");
//		
//		call(parts[1], callArgs);
//		
//		break;
//	    }
		}

		inputScanner.close();
	}

	public void load(String filePath) {
		FSSourceFile sf = sourceFileFrom(filePath);
		String classFilePath = filePath.replace(".lpc", ".class");
		Path classPath = Path.of("/Users/jonathan/brainjar", classFilePath);

		if (!Files.exists(classPath)) {
			System.out.println("Compiled class file not found, compiling...");

			compile(filePath);
		}

		try {
			byte[] classBytes = Files.readAllBytes(classPath);
			String fullyQualifiedName = filePath.replace('/', '.').replace(".lpc", "");

			objects.put(fullyQualifiedName, classBytes);

			System.out.println("Loaded: " + fullyQualifiedName);
		} catch (IOException e) {
			System.out.println("Failed to read class file: " + classPath + ".");
		}
	}

	public void call(String className, String[] args) {
		if (!objects.containsKey(className)) {
			System.out.println("Error: Class not loaded - " + className);

			return;
		}

		try {
			Class<?> clazz = Class.forName(className);
			LPCObject instance = (LPCObject) clazz.getDeclaredConstructor().newInstance();
			String methodName = args[0];
			Object[] methodArgs = new Object[args.length - 1];

			System.arraycopy(args, 1, methodArgs, 0, args.length - 1);

			Object result = instance.dispatch(methodName, methodArgs);

			System.out.println("Method result: " + result);
		} catch (Exception e) {
			System.out.println("Error invoking method: " + e.getMessage());
		}
	}

	public FSSourceFile sourceFileFrom(String filePath) {
		if (filePath == null) {
			System.out.println("Error: No file specified.");

			return null;
		}

		return new FSSourceFile("/Users/jonathan/brainjar", filePath);
	}

	public FSClassFile classFileFrom(String filePath) {
		if (filePath == null) {
			System.out.println("Error: No file specified.");

			return null;
		}

		return new FSClassFile("/Users/jonathan/brainjar", filePath);
	}

	public void compile(String filePath) {
		FSSourceFile sf = sourceFileFrom(filePath);
		ASTObject ast = parse(filePath);
		Compiler compiler = new Compiler("io/github/protasm.lpc2j/runtime/LPCObject");

		byte[] bytes = compiler.compile(ast);

		if (bytes != null) {
			sf.write(bytes);

			System.out.println("Compilation successful.");
		} else
			System.out.println("Compilation failed.");
	}

	public ASTObject parse(String filePath) {
		FSSourceFile sf = sourceFileFrom(filePath);
		Tokens tokens = scan(filePath);
		Parser parser = new Parser();

		return parser.parse(sf.slashName(), tokens);
	}

	public Tokens scan(String filePath) {
		FSSourceFile sf = sourceFileFrom(filePath);

		return new Scanner().scan(sf.source());
	}
}
