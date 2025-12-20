package io.github.protasm.lpc2j.console;

import io.github.protasm.lpc2j.compiler.CompileException;
import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.console.cmd.*;
import io.github.protasm.lpc2j.console.efuns.*;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;
import io.github.protasm.lpc2j.console.ConsoleConfig;
import io.github.protasm.lpc2j.efun.EfunRegistry;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.ScanException;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.token.TokenList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LPCConsole {
  private final VirtualFileServer vfs;
  private Path vPath;
  private final ParserOptions parserOptions;
  private final IncludeResolver includeResolver;
  private final ConsoleConfig config;

  private final Map<String, Object> objects;
  private final java.util.Scanner inputScanner;

  private static Map<Command, List<String>> commands = new LinkedHashMap<>();

  static {
    commands.put(new CmdHelp(), List.of("?", "help"));
    commands.put(new CmdScan(), List.of("s", "scan"));
    commands.put(new CmdParse(), List.of("p", "parse"));
    commands.put(new CmdCompile(), List.of("c", "compile"));
    commands.put(new CmdLoad(), List.of("l", "load"));
    commands.put(new CmdListObjects(), List.of("o", "objects"));
    commands.put(new CmdInspect(), List.of("i", "inspect"));
    commands.put(new CmdCall(), List.of("call"));
    commands.put(new CmdDirShow(), List.of("pwd"));
    commands.put(new CmdDirList(), List.of("ls"));
    commands.put(new CmdDirChange(), List.of("cd"));
    commands.put(new CmdFileCat(), List.of("cat"));
    commands.put(new CmdPreprocess(), List.of("pp", "preprocess"));
    commands.put(new CmdQuit(), List.of("q", "quit"));
  }

  public LPCConsole(String basePathStr) {
    this(basePathStr, ParserOptions.defaults());
  }

  public LPCConsole(String basePathStr, ParserOptions parserOptions) {
    this.vfs = new VirtualFileServer(basePathStr);
    this.parserOptions = (parserOptions == null) ? ParserOptions.defaults() : parserOptions;
    this.config = ConsoleConfig.load(vfs.basePath());
    this.includeResolver =
        new SearchPathIncludeResolver(vfs.basePath(), config.includeDirs());
    this.vPath = Path.of("/");

    objects = new LinkedHashMap<>();
    inputScanner = new java.util.Scanner(System.in);

    // Register Efuns
    EfunRegistry.register("add_action", EfunAddAction.INSTANCE);
    EfunRegistry.register("add_verb", EfunAddVerb.INSTANCE);
    EfunRegistry.register("call_other", EfunCallOther.INSTANCE);
    EfunRegistry.register("destruct", EfunDestruct.INSTANCE);
    EfunRegistry.register("foo", EfunFoo.INSTANCE);
    EfunRegistry.register("environment", EfunEnvironment.INSTANCE);
    EfunRegistry.register("this_player", EfunThisPlayer.INSTANCE);
    EfunRegistry.register("this_object", EfunThisObject.INSTANCE);
    EfunRegistry.register("set_heart_beat", EfunSetHeartBeat.INSTANCE);
    EfunRegistry.register("set_light", EfunSetLight.INSTANCE);
    EfunRegistry.register("say", EfunSay.INSTANCE);
    EfunRegistry.register("write", EfunWrite.INSTANCE);
  }

  public VirtualFileServer vfs() {
    return vfs;
  }

  public Path vPath() {
    return vPath;
  }

  public void setVPath(Path vPath) {
    this.vPath = vPath.normalize();
  }

  public String pwd() {
    if (vPath.getNameCount() == 0) {
      return "/";
    }

    return "/" + vPath.toString();
  }

  public String pwdShort() {
    if (vPath.getNameCount() == 0) {
      return "/";
    }

    return vPath.getFileName().toString();
  }

  public Map<String, Object> objects() {
    return objects;
  }

  public IncludeResolver includeResolver() {
    return includeResolver;
  }

  public static Map<Command, List<String>> commands() {
    return commands;
  }

  public void repl() {
    while (true) {
      System.out.print(pwdShort() + " % ");
      String line = inputScanner.nextLine().trim();

      if (line.isEmpty()) {
        continue;
      }

      String[] parts = line.split("\\s+");
      String input = parts[0];

      // Lookup command by alias
      Command cmd = LPCConsole.getCommand(input);

      if (cmd != null) {
        parts = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        if (!cmd.execute(this, parts)) {
          break;
        }
      } else {
        System.out.println("Unrecognized command: '" + input + "'.");
      }
    }

    inputScanner.close();
  }

  public static Command getCommand(String alias) {
    for (Map.Entry<Command, List<String>> entry : commands.entrySet()) {
      if (entry.getValue().contains(alias)) {
        return entry.getKey();
      }
    }

    return null;
  }

  public FSSourceFile load(String vPathStr) {
    FSSourceFile sf = compile(vPathStr);

    if (sf == null) {
      return null;
    }

    // Define the class dynamically from the bytecode
    Class<?> clazz =
        new ClassLoader() {
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
        | InstantiationException
        | VerifyError e) {
      System.out.println(e.toString());

      return null;
    }
  }

  public Object call(String className, String methodName, Object[] callArgs) {
    Object obj = objects.get(className);

    if (obj == null) {
      System.out.println("Error: Object '" + className + "' not loaded.");

      return null;
    }

    try {
      Method[] methods = obj.getClass().getMethods();

      for (Method method : methods) {
        if (method
            .getName()
            .equals(methodName)) { // && matchParameters(method.getParameterTypes(), argTypes))
          return method.invoke(obj, callArgs);
        }
      }
    } catch (InvocationTargetException e) {
      System.out.println(e.toString());
      e.getCause().printStackTrace();
    } catch (IllegalAccessException e) {
      System.out.println(e.toString());
    } catch (IllegalArgumentException e) {
      System.out.println(e.toString());
    }

    return null;
  }

  public FSSourceFile compile(String vPathStr) {
    FSSourceFile sf = parse(vPathStr);

    if (sf == null) {
      return null;
    }

    try {
      Compiler compiler = new Compiler("java/lang/Object");
      byte[] bytes = compiler.compile(sf.astObject());

      sf.setBytes(bytes);

      return sf;
    } catch (CompileException | IllegalArgumentException e) {
      System.out.println("Error compiling fileName: " + vPathStr);
      System.out.println(e);

      return null;
    }
  }

  public FSSourceFile parse(String vPathStr) {
    FSSourceFile sf = scan(vPathStr);

    if (sf == null) return null;

    try {
      Parser parser = new Parser(parserOptions);
      ASTObject astObject = parser.parse(sf.slashName(), sf.tokens());

      sf.setASTObject(astObject);

      return sf;
    } catch (ParseException | IllegalArgumentException e) {
      System.out.println("Error parsing fileName: " + vPathStr);
      System.out.println(e);

      return null;
    }
  }

  public FSSourceFile scan(String vPathStr) {
    try {
      Path resolved = vfs.fileAt(vPathStr);

      if (resolved == null) throw new IllegalArgumentException();

      FSSourceFile sf = new FSSourceFile(resolved);

      boolean success = vfs.read(sf);

      if (!success) throw new IllegalArgumentException();

      Preprocessor preprocessor = new Preprocessor(includeResolver);
      Scanner scanner = new Scanner(preprocessor);
      Path sourcePath = vfs.basePath().resolve(resolved).normalize();

      TokenList tokens = scanner.scan(sourcePath, sf.source());

      sf.setTokens(tokens);

      return sf;
    } catch (ScanException | IllegalArgumentException e) {
      System.out.println("Error scanning fileName: " + vPathStr);
      System.out.println(e);

      return null;
    }
  }

  public static void main(String[] args) {
    String basePathArg = null;

    for (String arg : args) {
      if (basePathArg == null) basePathArg = arg;
      else {
        System.out.println("Error: unexpected argument '" + arg + "'.");
        printUsage();

        System.exit(-1);
      }
    }

    if (basePathArg == null) {
      System.out.println("Error: missing base path.");
      printUsage();

      System.exit(-1);
    }

    ParserOptions parserOptions = ParserOptions.defaults();
    LPCConsole console = new LPCConsole(basePathArg, parserOptions);

    console.repl();
  }

  private static void printUsage() {
    System.out.println("Usage: LPCConsole <base path>");
  }
}
