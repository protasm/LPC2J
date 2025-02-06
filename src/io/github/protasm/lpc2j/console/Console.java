package io.github.protasm.lpc2j.console;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.console.cmd.*;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.runtime.LPCObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Tokens;

public class Console {
	private final String basePath;
	private Map<String, LPCObject> objects;
	private final java.util.Scanner inputScanner;

	private static final String RUNTIMEOBJ = "io/github/protasm/lpc2j/runtime/LPCObject";
	private static Map<String, Command> commands = new TreeMap<>();
	
	static {
		commands.put("c", new CmdCompile());
		commands.put("call", new CmdCall());
		commands.put("cd", new CmdDirChange());
		commands.put("h", new CmdHelp());
		commands.put("l", new CmdLoad());
		commands.put("ls", new CmdDirList());
		commands.put("o", new CmdListObjects());
		commands.put("p", new CmdParse());
		commands.put("pwd", new CmdDirShow());
		commands.put("s", new CmdScan());
		commands.put("x", new CmdQuit());
	}

	public Console(String basePath) {
		this.basePath = basePath;

		objects = new HashMap<>();
		inputScanner = new java.util.Scanner(System.in);
		
		new CmdLoad().execute(this, "obj/weapon/sword.lpc");
	}

	public Map<String, LPCObject> objects() {
		return objects;
	}

	public static Map<String, Command> commands() {
		return commands;
	}

	public void repl() {
		while (true) {
			System.out.print("> ");

			String line = inputScanner.nextLine().trim();

			if (line.isEmpty())
				continue;

			String[] parts = line.split("\\s+");
			String command = parts[0];

			if (Console.commands.containsKey(command)) {
				Command cmd = Console.commands.get(command);
				parts = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

				boolean finished = cmd.execute(this, parts);

				if (finished)
					break;
			} else {
				System.out.println("Unrecognized command: '" + command + "'.");

				continue;
			}
		}

		inputScanner.close();
	}

	public FSSourceFile load(String filePath) {
		FSSourceFile sf = compile(filePath);

		if (sf == null)
			return null;

		// Define the class dynamically from the bytecode
		Class<?> clazz = new ClassLoader() {
			public Class<?> defineClass(byte[] bytecode) {
				return defineClass(null, bytecode, 0, bytecode.length);
			}
		}.defineClass(sf.bytes());

		// Ensure the generated class extends LPCObject
		if (!LPCObject.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Generated class must extend LPCObject.");
		}

		// Instantiate the class using reflection
		try {
			Constructor<?> constructor = clazz.getConstructor(); // Assumes a no-arg constructor
			LPCObject instance = (LPCObject) constructor.newInstance();

			sf.setLPCObject(instance);

			return sf;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InstantiationException e) {
			return null;
		}
	}

	public void call(String className, String methodName, Object[] callArgs) {
		LPCObject obj = objects.get(className);
		
		if (obj == null) {
			System.out.println("Error: Object '" + className + "' not loaded.");

			return;
		}

		try {
			Object result = obj.dispatch(methodName, callArgs);

			System.out.println("Method result: " + result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public FSSourceFile compile(String filePath) {
		FSSourceFile sf = parse(filePath);

		if (sf == null)
			return null;

		byte[] bytes = new Compiler(RUNTIMEOBJ).compile(sf.astObject());

		sf.setBytes(bytes);
		sf.write(basePath);

		return sf;
	}

	public FSSourceFile parse(String filePath) {
		FSSourceFile sf = scan(filePath);

		if (sf == null)
			return null;

		ASTObject astObject = new Parser().parse(sf.slashName(), sf.tokens());

		sf.setASTObject(astObject);

		return sf;
	}

	public FSSourceFile scan(String filePath) {
		try {
			FSSourceFile sf = new FSSourceFile(basePath, filePath);

			Tokens tokens = new Scanner().scan(sf.source());

			sf.setTokens(tokens);

			return sf;
		} catch (IllegalArgumentException e) {
			System.out.println(e.getLocalizedMessage());

			return null;
		} catch (IOException e) {
			System.out.println("Could not read file '" + filePath + "'.");

			return null;
		}
	}
}
