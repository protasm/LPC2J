package io.github.protasm.lpc2j.console;

import io.github.protasm.lpc2j.console.cmd.*;
import io.github.protasm.lpc2j.console.efuns.*;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;
import io.github.protasm.lpc2j.console.ConsoleConfig;
import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.runtime.RuntimeContextHolder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LPCConsole {
  private final VirtualFileServer vfs;
  private final RuntimeContext runtimeContext;
  private final CompilationPipeline pipeline;
  private Path vPath;
  private final ParserOptions parserOptions;
  private final ConsoleConfig config;

  private final java.util.Scanner inputScanner;

  private static Map<Command, List<String>> commands = new LinkedHashMap<>();

  static {
    commands.put(new CmdHelp(), List.of("h", "help"));
    commands.put(new CmdPreprocess(), List.of("pp", "preprocess"));
    commands.put(new CmdScan(), List.of("s", "scan"));
    commands.put(new CmdParse(), List.of("p", "parse"));
    commands.put(new CmdAnalyze(), List.of("a", "analyze"));
    commands.put(new CmdIR(), List.of("ir"));
    commands.put(new CmdCompile(), List.of("c", "compile"));
    commands.put(new CmdLoad(), List.of("l", "load"));
    commands.put(new CmdListObjects(), List.of("o", "objects"));
    commands.put(new CmdInspect(), List.of("i", "inspect"));
    commands.put(new CmdCall(), List.of("call"));
    commands.put(new CmdDirShow(), List.of("pwd"));
    commands.put(new CmdDirList(), List.of("ls"));
    commands.put(new CmdDirChange(), List.of("cd"));
    commands.put(new CmdFileCat(), List.of("cat"));
    commands.put(new CmdQuit(), List.of("q", "quit"));
  }

  public LPCConsole(String basePathStr) {
    this(basePathStr, ParserOptions.defaults());
  }

  public LPCConsole(String basePathStr, ParserOptions parserOptions) {
    this.vfs = new VirtualFileServer(basePathStr);
    this.parserOptions = (parserOptions == null) ? ParserOptions.defaults() : parserOptions;
    this.config = ConsoleConfig.load(vfs.basePath());
    IncludeResolver includeResolver =
        new SearchPathIncludeResolver(vfs.basePath(), config.includeDirs());
    this.runtimeContext = new RuntimeContext(includeResolver);
    this.pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);
    this.vPath = Path.of("/");

    inputScanner = new java.util.Scanner(System.in);

    // Register Efuns
    runtimeContext.registerEfun(EfunAddAction.INSTANCE);
    runtimeContext.registerEfun(EfunAddVerb.INSTANCE);
    runtimeContext.registerEfun(EfunCallOther.INSTANCE);
    runtimeContext.registerEfun(EfunDestruct.INSTANCE);
    runtimeContext.registerEfun(EfunFoo.INSTANCE);
    runtimeContext.registerEfun(EfunEnvironment.INSTANCE);
    runtimeContext.registerEfun(EfunRandom.INSTANCE);
    runtimeContext.registerEfun(EfunThisPlayer.INSTANCE);
    runtimeContext.registerEfun(EfunThisObject.INSTANCE);
    runtimeContext.registerEfun(EfunSetHeartBeat.INSTANCE);
    runtimeContext.registerEfun(EfunSetLight.INSTANCE);
    runtimeContext.registerEfun(EfunSay.INSTANCE);
    runtimeContext.registerEfun(EfunWrite.INSTANCE);
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
    return runtimeContext.objectsView();
  }

  public void registerObject(String name, Object object) {
    runtimeContext.registerObject(name, object);
  }

  public Object getObject(String name) {
    return runtimeContext.getObject(name);
  }

  public boolean hasObject(String name) {
    return runtimeContext.objects().containsKey(name);
  }

  public IncludeResolver includeResolver() {
    return runtimeContext.includeResolver();
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
    return withRuntimeContext(() -> doLoad(vPathStr));
  }

  public Object call(String className, String methodName, Object[] callArgs) {
    return withRuntimeContext(() -> doCall(className, methodName, callArgs));
  }

  public FSSourceFile compile(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (!result.succeeded()) {
      printProblems(result.getProblems());
      return null;
    }

    return sf;
  }

  public FSSourceFile parse(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.astObject() == null) {
      printProblems(result.getProblems());
      return null;
    }

    return sf;
  }

  public FSSourceFile scan(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.tokens() == null) {
      printProblems(result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(result.getProblems());
    }

    return sf;
  }

  public FSSourceFile analyze(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.semanticModel() == null) {
      printProblems(result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(result.getProblems());
    }

    return sf;
  }

  public FSSourceFile ir(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.typedIr() == null) {
      printProblems(result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(result.getProblems());
    }

    return sf;
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
    try {
      LPCConsole console = new LPCConsole(basePathArg, parserOptions);

      System.out.println("LPC2J Console\nType 'help' for help.");
      console.repl();
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      System.exit(-1);
    }
  }

  private static void printUsage() {
    System.out.println("Usage: LPCConsole <base path>");
  }

  private FSSourceFile prepareSourceFile(String vPathStr) {
    try {
      Path resolved = vfs.fileAt(vPathStr);

      if (resolved == null) throw new IllegalArgumentException();

      FSSourceFile sf = new FSSourceFile(resolved);

      boolean success = vfs.read(sf);

      if (!success) throw new IllegalArgumentException();

      return sf;
    } catch (IllegalArgumentException e) {
      System.out.println("Error: cannot read file '" + vPathStr + "'.");
      return null;
    }
  }

  private CompilationResult runPipeline(FSSourceFile sf) {
    Path sourcePath = vfs.basePath().resolve(sf.vPath()).normalize();

    CompilationResult result = pipeline.run(sourcePath, sf.source(), sf.slashName(), parserOptions);

    if (result.getTokens() != null) {
      sf.setTokens(result.getTokens());
    }

    if (result.getAstObject() != null) {
      sf.setASTObject(result.getAstObject());
    }

    if (result.getSemanticModel() != null) {
      sf.setSemanticModel(result.getSemanticModel());
    }

    if (result.getTypedIr() != null) {
      sf.setTypedIr(result.getTypedIr());
    }

    if (result.getBytecode() != null) {
      sf.setBytes(result.getBytecode());
    }

    return result;
  }

  private void printProblems(List<CompilationProblem> problems) {
    for (CompilationProblem problem : problems) {
      System.out.println(problem.getStage() + ": " + problem.getMessage());
      if (problem.getThrowable() != null) {
        System.out.println(problem.getThrowable());
      }
    }
  }

  private FSSourceFile doLoad(String vPathStr) {
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

  private Object doCall(String className, String methodName, Object[] callArgs) {
    Object obj = runtimeContext.getObject(className);

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

  private <T> T withRuntimeContext(Supplier<T> supplier) {
    RuntimeContext previous = RuntimeContextHolder.current();
    RuntimeContextHolder.setCurrent(runtimeContext);
    try {
      return supplier.get();
    } finally {
      RuntimeContextHolder.setCurrent(previous);
    }
  }
}
