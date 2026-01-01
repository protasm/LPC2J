package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import io.github.protasm.lpc2j.runtime.RuntimeContext;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

public final class Lpc2jHelloDemo {
    public static void main(String[] args) throws Exception {
        // Hypothetical LPC source (no includes/inherits/fields; single void method).
        String source = "void hello() { }\n";
        Path sourcePath = Path.of("hello.c"); // hypothetical file path
        Path baseIncludePath = sourcePath.getParent() == null ? Path.of(".") : sourcePath.getParent();

        RuntimeContext runtimeContext =
                new RuntimeContext(new SearchPathIncludeResolver(baseIncludePath, List.of()));
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);

        String sourceName = "demo/Hello";     // internal LPC object name
        String displayPath = "/demo/Hello";   // display path for diagnostics

        CompilationResult result =
                pipeline.run(sourcePath, source, sourceName, displayPath, ParserOptions.defaults());

        if (!result.succeeded()) {
            StringBuilder message = new StringBuilder("Compilation failed:\n");
            for (CompilationProblem problem : result.getProblems()) {
                message.append(problem.getStage()).append(": ").append(problem.getMessage()).append("\n");
            }
            throw new IllegalStateException(message.toString());
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "demo.Hello";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method hello = clazz.getMethod("hello");
        hello.invoke(instance);
        System.out.println("hello() invoked successfully.");
    }
}

