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
import java.util.Objects;
import java.util.function.Supplier;

public class LPCConsole {
  private final VirtualFileServer vfs;
  private final RuntimeContext runtimeContext;
  private final CompilationPipeline pipeline;
  private Path vPath;
  private final ParserOptions parserOptions;
  private final ConsoleConfig config;

  private final ConsoleLineReader lineReader;

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

  public LPCConsole(ConsoleConfig config) {
    this(config, ParserOptions.defaults());
  }

  public LPCConsole(ConsoleConfig config, ParserOptions parserOptions) {
    this.config = Objects.requireNonNull(config, "config");
    this.vfs = new VirtualFileServer(config.basePath().toString());
    this.parserOptions = (parserOptions == null) ? ParserOptions.defaults() : parserOptions;
    IncludeResolver includeResolver =
        new SearchPathIncludeResolver(vfs.basePath(), config.includeDirs());
    this.runtimeContext = new RuntimeContext(includeResolver);
    this.pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);
    this.vPath = Path.of("/");

    lineReader = new ConsoleLineReader(System.in, System.out);

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
      String line = lineReader.readLine(pwdShort() + " % ");
      if (line == null) {
        break;
      }

      line = line.trim();

      if (line.isEmpty()) {
        continue;
      }

      lineReader.recordHistory(line);

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

  public CallResult call(String className, String methodName, Object[] callArgs) {
    return withRuntimeContext(() -> doCall(className, methodName, callArgs));
  }

  public FSSourceFile compile(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (!result.succeeded()) {
      printProblems(sf, result.getProblems());
      return null;
    }

    return sf;
  }

  public FSSourceFile parse(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.astObject() == null) {
      printProblems(sf, result.getProblems());
      return null;
    }

    return sf;
  }

  public FSSourceFile scan(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.tokens() == null) {
      printProblems(sf, result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(sf, result.getProblems());
    }

    return sf;
  }

  public FSSourceFile analyze(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.semanticModel() == null) {
      printProblems(sf, result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(sf, result.getProblems());
    }

    return sf;
  }

  public FSSourceFile ir(String vPathStr) {
    FSSourceFile sf = prepareSourceFile(vPathStr);
    if (sf == null) return null;

    CompilationResult result = runPipeline(sf);

    if (sf.typedIr() == null) {
      printProblems(sf, result.getProblems());
      return null;
    }

    if (!result.getProblems().isEmpty()) {
      printProblems(sf, result.getProblems());
    }

    return sf;
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Error: missing configuration path.");
      printUsage();

      System.exit(-1);
    }

    String configPathArg = args[0];

    ParserOptions parserOptions = ParserOptions.defaults();
    try {
      ConsoleConfig config = ConsoleConfig.load(Path.of(configPathArg));
      LPCConsole console = new LPCConsole(config, parserOptions);

      System.out.println("LPC2J Console\nType 'help' for help.");
      console.repl();
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      System.exit(-1);
    }
  }

  private static void printUsage() {
    System.out.println("Usage: LPCConsole <config path>");
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
    String displayPath = Path.of("/").resolve(sf.vPath()).normalize().toString();

    CompilationResult result =
        pipeline.run(sourcePath, sf.source(), sf.slashName(), displayPath, parserOptions);

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

  private void printProblems(FSSourceFile sf, List<CompilationProblem> problems) {
    Path displayPath = (sf != null) ? Path.of("/").resolve(sf.vPath()).normalize() : null;

    for (CompilationProblem problem : problems) {
      StringBuilder prefix = new StringBuilder();
      if (displayPath != null) {
        prefix.append(displayPath);
        if (problem.getLine() != null && problem.getLine() > 0) {
          prefix.append(":").append(problem.getLine());
        }
        prefix.append(": ");
      } else if (problem.getLine() != null && problem.getLine() > 0) {
        prefix.append("line ").append(problem.getLine()).append(": ");
      }

      String prefixStr = prefix.toString();
      System.out.println(prefixStr + problem.getStage() + ": " + problem.getMessage());
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

  private CallResult doCall(String className, String methodName, Object[] callArgs) {
    if (callArgs == null) {
      callArgs = new Object[0];
    }

    Object obj = runtimeContext.getObject(className);

    if (obj == null) {
      System.out.println("Error: Object '" + className + "' not loaded.");

      return CallResult.error();
    }

    Method target = findMethod(obj.getClass(), methodName, callArgs);
    if (target == null) {
      System.out.println(
          "Error: Object '" + className + "' has no method '" + methodName + "' matching args "
              + callArgs.length
              + ".");
      return CallResult.error();
    }

    try {
      return CallResult.success(target.invoke(obj, callArgs));
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof NoSuchMethodException) {
        System.out.println(
            "Error: '" + className + "." + methodName + "' failed: " + cause.getMessage());
      } else if (cause != null) {
        System.out.println(
            "Error: '" + className + "." + methodName + "' threw "
                + cause.getClass().getSimpleName()
                + ": "
                + cause.getMessage());
        cause.printStackTrace();
      } else {
        System.out.println("Error: '" + className + "." + methodName + "' failed: " + e);
      }
    } catch (IllegalAccessException e) {
      System.out.println(e.toString());
    } catch (IllegalArgumentException e) {
      System.out.println(e.toString());
    }

    return CallResult.error();
  }

  private Method findMethod(Class<?> clazz, String methodName, Object[] callArgs) {
    Method fallback = null;
    for (Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName)) {
        continue;
      }

      if (method.getParameterCount() == callArgs.length
          && parametersMatch(method.getParameterTypes(), callArgs)) {
        return method;
      }

      if (fallback == null) {
        fallback = method;
      }
    }

    return fallback;
  }

  private boolean parametersMatch(Class<?>[] parameterTypes, Object[] callArgs) {
    if (parameterTypes.length != callArgs.length) {
      return false;
    }

    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> expectedType = parameterTypes[i];
      Object arg = callArgs[i];

      if (arg == null) {
        if (expectedType.isPrimitive()) {
          return false;
        }
        continue;
      }

      Class<?> wrappedExpected = toWrapper(expectedType);
      if (!wrappedExpected.isAssignableFrom(arg.getClass())) {
        return false;
      }
    }

    return true;
  }

  private Class<?> toWrapper(Class<?> clazz) {
    if (!clazz.isPrimitive()) {
      return clazz;
    }

    if (clazz == boolean.class) return Boolean.class;
    if (clazz == byte.class) return Byte.class;
    if (clazz == short.class) return Short.class;
    if (clazz == int.class) return Integer.class;
    if (clazz == long.class) return Long.class;
    if (clazz == char.class) return Character.class;
    if (clazz == float.class) return Float.class;
    if (clazz == double.class) return Double.class;

    return clazz;
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

  public static final class CallResult {
    private final boolean success;
    private final Object value;

    private CallResult(boolean success, Object value) {
      this.success = success;
      this.value = value;
    }

    public static CallResult success(Object value) {
      return new CallResult(true, value);
    }

    public static CallResult error() {
      return new CallResult(false, null);
    }

    public boolean succeeded() {
      return success;
    }

    public Object value() {
      return value;
    }
  }
}
