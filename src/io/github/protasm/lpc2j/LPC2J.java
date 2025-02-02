package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.runtime.LPCObject;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Tokens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LPC2J {
    private static final Map<String, byte[]> loadedClasses = new HashMap<>();

    public static void main(String[] args) throws IOException {
        java.util.Scanner inputScanner = new java.util.Scanner(System.in);

        while (true) {
            System.out.print("> ");

            String line = inputScanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(" ", 3);
            String command = parts[0];

            if ("x".equals(command)) break;

            if (!command.matches("[clopsy]")) {
                System.out.println("Unknown command: " + command);
                continue;
            }

            switch (command) {
                case "o": // List Loaded Objects
                    System.out.println(loadedClasses.keySet());
                    break;

                case "s": // Scan
                    if (parts.length < 2) {
                        System.out.println("Error: No file specified.");
                        break;
                    }
                    Tokens tokens = scan(parts[1]);
                    if (tokens != null) System.out.println(tokens);
                    break;

                case "p": // Parse
                    if (parts.length < 2) {
                        System.out.println("Error: No file specified.");
                        break;
                    }
                    ASTObject ast = parse(parts[1]);
                    if (ast != null) System.out.println(ast);
                    break;

                case "c": // Compile
                    if (parts.length < 2) {
                        System.out.println("Error: No file specified.");
                        break;
                    }
                    compile(parts[1]);
                    break;

                case "l": // Load
                    if (parts.length < 2) {
                        System.out.println("Error: No file specified.");
                        break;
                    }
                    load(parts[1]);
                    break;
                
                case "y": // Call Method
                    if (parts.length < 3) {
                        System.out.println("Error: Insufficient arguments for call command.");
                        break;
                    }
                    
                    String[] callArgs = parts[2].split(" ");
                    call(parts[1], callArgs);
                    break;
            }
        }
    }

    private static void load(String filePath) {
        String classFilePath = filePath.replace(".lpc", ".class");
        Path classPath = Path.of("/Users/jonathan/brainjar", classFilePath);

        if (!Files.exists(classPath)) {
            System.out.println("Compiled class file not found, compiling...");
            compile(filePath);
        }

        try {
            byte[] classBytes = Files.readAllBytes(classPath);
            String fullyQualifiedName = filePath.replace('/', '.').replace(".lpc", "");
            loadedClasses.put(fullyQualifiedName, classBytes);
            System.out.println("Loaded: " + fullyQualifiedName);
        } catch (IOException e) {
            System.out.println("Failed to read class file: " + classPath + ".");
        }
    }

    private static void call(String className, String[] args) {
        if (!loadedClasses.containsKey(className)) {
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

    private static SourceFile sf(String filePath) {
        if (filePath == null) {
            System.out.println("Error: No file specified.");
            return null;
        }
        return new SourceFile("/Users/jonathan/brainjar", filePath);
    }

    private static void compile(String filePath) {
        SourceFile sf = sf(filePath);
        ASTObject ast = parse(filePath);
        Compiler compiler = new Compiler("io/github/protasm.lpc2j/runtime/LPCObject");
        byte[] bytes = compiler.compile(ast);

        if (bytes != null) {
            sf.write(bytes);
            System.out.println("Compilation successful.");
        } else {
            System.out.println("Compilation failed.");
        }
    }

    private static ASTObject parse(String filePath) {
        SourceFile sf = sf(filePath);
        Tokens tokens = scan(filePath);
        Parser parser = new Parser();
        return parser.parse(sf.slashName(), tokens);
    }

    private static Tokens scan(String filePath) {
        SourceFile sf = sf(filePath);
        return new Scanner().scan(sf.source());
    }
}