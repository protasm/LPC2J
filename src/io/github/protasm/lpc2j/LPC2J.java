package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
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

            String[] parts = line.split(" ", 2);
            String command = parts[0];
            String filePath = parts.length > 1 ? parts[1] : null;

            if ("x".equals(command))
              break;

            if (!command.matches("[clops]")) {
                System.out.println("Unknown command: " + command);
              
                continue;
            }

            switch (command) {
            	case "o": // List Loaded Objects
            		System.out.println(loadedClasses.keySet());
            		
            		break;
                case "s": // Scan
                    Tokens tokens = scan(filePath);
                    
                    if (tokens != null)
                    	System.out.println(tokens);
                
                    break;
                case "p": // Parse
                    ASTObject ast = parse(filePath);

                    if (ast != null)
                    	System.out.println(ast);
                
                    break;
                case "c": // Compile
                    compile(filePath);
                
                    break;
                case "l": // Load
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
	                    
	                    break;
                    } catch (IOException e) {
                    	System.out.println("Failed to read class file: " + classPath + ".");
                    	
                    	break;
                    }
            } // switch (command)
        } // while (true)
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
        } else
        	System.out.println("Compilation failed.");
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
