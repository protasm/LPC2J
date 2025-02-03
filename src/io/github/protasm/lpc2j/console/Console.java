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

    Map<String, Command> commands = Map.of("c", new CmdCompile(), "l", new CmdLoad(), "p", new CmdParse(), "s",
	    new CmdScan());

    public Console() {
	objects = new HashMap<>();
	inputScanner = new java.util.Scanner(System.in);
    }

    public void repl() {
	while (true) {
	    System.out.print("> ");

	    String line = inputScanner.nextLine().trim();

	    if (line.isEmpty())
		continue;

	    String[] parts = line.split("\\s+");
	    String command = parts[0];

	    if ("x".equals(command))
		break;

	    if (commands.containsKey(command)) {
		Command cmd = commands.get(command);
		parts = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

		cmd.execute(this, parts);
	    } else {
		System.out.println("Unrecognized command: '" + command + "'.");

		continue;
	    }

//	    switch (command) {
//	    case "o": // List Loaded Objects
//		System.out.println(objects.keySet());
//
//		break;
//
//	    case "s": // Scan
//		if (parts.length < 2) {
//		    System.out.println("Error: No file specified.");
//		    
//		    break;
//		}
//		
//		Tokens tokens = scan(parts[1]);
//		
//		if (tokens != null)
//		    System.out.println(tokens);
//		
//		break;
//	    case "p": // Parse
//		if (parts.length < 2) {
//		    System.out.println("Error: No file specified.");
//		    
//		    break;
//		}
//		
//		ASTObject ast = parse(parts[1]);
//		
//		if (ast != null)
//		    System.out.println(ast);
//		
//		break;
//	    case "c": // Compile
//		if (parts.length < 2) {
//		    System.out.println("Error: No file specified.");
//		    
//		    break;
//		}
//		
//		compile(parts[1]);
//		
//		break;
//
//	    case "l": // Load
//		if (parts.length < 2) {
//		    System.out.println("Error: No file specified.");
//		
//		    break;
//		}
//		
//		load(parts[1]);
//		
//		break;
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

//	inputScanner.close();
    }

    public void load(String filePath) {
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
	FSClassFile cf = classFileFrom(filePath);
	ASTObject ast = parse(filePath);
	Compiler compiler = new Compiler("io/github/protasm.lpc2j/runtime/LPCObject");

	byte[] bytes = compiler.compile(ast);

	if (bytes != null) {
	    cf.write(bytes);

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
