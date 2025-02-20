package io.github.protasm.lpc2j.console;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.console.cmd.CmdCall;
import io.github.protasm.lpc2j.console.cmd.CmdCompile;
import io.github.protasm.lpc2j.console.cmd.CmdDirChange;
import io.github.protasm.lpc2j.console.cmd.CmdDirList;
import io.github.protasm.lpc2j.console.cmd.CmdDirShow;
import io.github.protasm.lpc2j.console.cmd.CmdFileCat;
import io.github.protasm.lpc2j.console.cmd.CmdHelp;
import io.github.protasm.lpc2j.console.cmd.CmdListObjects;
import io.github.protasm.lpc2j.console.cmd.CmdLoad;
import io.github.protasm.lpc2j.console.cmd.CmdParse;
import io.github.protasm.lpc2j.console.cmd.CmdQuit;
import io.github.protasm.lpc2j.console.cmd.CmdScan;
import io.github.protasm.lpc2j.console.cmd.Command;
import io.github.protasm.lpc2j.console.fs.FSBasePath;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Tokens;

public class Console {
    private final FSBasePath basePath;
    private Path vPath;

    private final Map<String, Object> objects;
    private final java.util.Scanner inputScanner;

    private static Map<String, Command> commands = new TreeMap<>();

    static {
	commands.put("c", new CmdCompile());
	commands.put("cat", new CmdFileCat());
	commands.put("compile", new CmdCompile());
	commands.put("call", new CmdCall());
	commands.put("cd", new CmdDirChange());
	commands.put("h", new CmdHelp());
	commands.put("help", new CmdHelp());
	commands.put("l", new CmdLoad());
	commands.put("load", new CmdLoad());
	commands.put("ls", new CmdDirList());
	commands.put("o", new CmdListObjects());
	commands.put("objects", new CmdListObjects());
	commands.put("p", new CmdParse());
	commands.put("parse", new CmdParse());
	commands.put("pwd", new CmdDirShow());
	commands.put("q", new CmdQuit());
	commands.put("quit", new CmdQuit());
	commands.put("s", new CmdScan());
	commands.put("scan", new CmdScan());
    }

    public Console(String basePathStr) {
	basePath = new FSBasePath(basePathStr);
	vPath = Path.of("/");

	objects = new HashMap<>();
	inputScanner = new java.util.Scanner(System.in);

//	new CmdLoad().execute(this, "obj/weapon/sword.lpc");
//	System.out.print("\n");
//	new CmdLoad().execute(this, "obj/weapon/axe.lpc");
//	System.out.print("\n");
//	new CmdLoad().execute(this, "obj/armor/armor.lpc");
    }

    public FSBasePath basePath() {
	return basePath;
    }

    public Path vPath() {
	return vPath;
    }

    public void setVPath(Path vPath) {
	this.vPath = vPath.normalize();
    }

    public String pwd() {
	if (vPath.getNameCount() == 0)
	    return "/";

	return "/" + vPath.toString();
    }

    public String pwdShort() {
	if (vPath.getNameCount() == 0)
	    return "/";

	return vPath.getFileName().toString();
    }

    public Map<String, Object> objects() {
	return objects;
    }

    public static Map<String, Command> commands() {
	return commands;
    }

    public void repl() {
	while (true) {
	    System.out.print(pwdShort() + " % ");

	    String line = inputScanner.nextLine().trim();

	    if (line.isEmpty())
		continue;

	    String[] parts = line.split("\\s+");
	    String command = parts[0];

	    if (Console.commands.containsKey(command)) {
		Command cmd = Console.commands.get(command);
		parts = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

		if (!cmd.execute(this, parts))
		    break;
	    } else {
		System.out.println("Unrecognized command: '" + command + "'.");

		continue;
	    }
	}

	inputScanner.close();
    }

    public FSSourceFile load(String vPathStr) {
	FSSourceFile sf = compile(vPathStr);

	if (sf == null)
	    return null;

	// Define the class dynamically from the bytecode
	Class<?> clazz = new ClassLoader() {
	    public Class<?> defineClass(byte[] bytecode) {
		return defineClass(null, bytecode, 0, bytecode.length);
	    }
	}.defineClass(sf.bytes());

	// Instantiate the class using reflection
	try {
	    // Assume a no-arg constructor
	    Constructor<?> constructor = clazz.getConstructor();
	    Object instance = constructor.newInstance();

	    sf.setLPCObject(instance);

	    return sf;
	} catch (NoSuchMethodException
		| InvocationTargetException
		| IllegalAccessException
		| InstantiationException e) {
	    System.out.println(e.toString());

	    return null;
	}
    }

    public void call(String className, String methodName, Object[] callArgs) {
	Object obj = objects.get(className);

	if (obj == null) {
	    System.out.println("Error: Object '" + className + "' not loaded.");

	    return;
	}

	try {
	    Method[] methods = obj.getClass().getMethods();

	    for (Method method : methods)
		if (method.getName().equals(methodName)) { // && matchParameters(method.getParameterTypes(), argTypes))
		    Object result = method.invoke(obj, callArgs);

		    break;
		}
	} catch (InvocationTargetException e) {
	    System.out.println(e.toString());
	} catch (IllegalAccessException e) {
	    System.out.println(e.toString());
	}
    }

    public FSSourceFile compile(String vPathStr) {
	FSSourceFile sf = parse(vPathStr);

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

    public FSSourceFile parse(String vPathStr) {
	FSSourceFile sf = scan(vPathStr);

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

    public FSSourceFile scan(String vPathStr) {
	try {
	    Path resolved = basePath.fileAt(vPathStr);

	    if (resolved == null)
		throw new IllegalArgumentException();

	    FSSourceFile sf = new FSSourceFile(resolved);

	    boolean success = basePath.read(sf);

	    if (!success)
		throw new IllegalArgumentException();

	    Scanner scanner = new Scanner();
	    Tokens tokens = scanner.scan(sf.source());

	    sf.setTokens(tokens);

	    return sf;
	} catch (IllegalArgumentException e) {
	    System.out.println("Error scanning file: " + vPathStr);

	    return null;
	}
    }
}
