import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LPC2J {
    public static void main(String[] args) {
        if ((args.length == 0) || (args.length > 2)) {
            System.err.println("Usage: LPC2J <source-file> [output-dir]");
            System.err.println("Compiles an LPC source file into a JVM class file.");
            System.exit(64);
        }

        Path sourcePath = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(sourcePath)) {
            System.err.println("Source file not found: " + sourcePath);
            System.exit(66);
        }

        Path outputDir = (args.length == 2)
            ? Path.of(args[1]).toAbsolutePath().normalize()
            : sourcePath.getParent();
        if (outputDir == null) {
            outputDir = Path.of(".").toAbsolutePath().normalize();
        }

        String source;
        try {
            source = Files.readString(sourcePath);
        } catch (IOException e) {
            System.err.println("Failed to read source file: " + e.getMessage());
            System.exit(74);
            return;
        }

        Path baseIncludePath = sourcePath.getParent();
        RuntimeContext runtimeContext =
            new RuntimeContext(new SearchPathIncludeResolver(baseIncludePath, List.of()));
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);
        String sourceName = deriveSourceName(sourcePath, baseIncludePath);
        String displayPath = "/" + sourceName;

        CompilationResult result =
            pipeline.run(sourcePath, source, sourceName, displayPath, ParserOptions.defaults());

        if (!result.getProblems().isEmpty()) {
            reportProblems(result.getProblems());
            System.exit(65);
        }

        byte[] bytecode = result.getBytecode();
        if (bytecode == null) {
            System.err.println("Compilation did not produce bytecode.");
            System.exit(70);
        }

        String internalName = (result.getAstObject() != null)
            ? result.getAstObject().name()
            : sourceName;
        Path outputPath = outputDir.resolve(internalName + ".class");

        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputPath, bytecode);
        } catch (IOException e) {
            System.err.println("Failed to write classfile: " + e.getMessage());
            System.exit(74);
        }

        System.out.println("Wrote classfile: " + outputPath);
    }

    private static void reportProblems(List<CompilationProblem> problems) {
        for (CompilationProblem problem : problems) {
            StringBuilder message = new StringBuilder();
            message.append(problem.getStage()).append(": ").append(problem.getMessage());
            if (problem.getLine() != null) {
                message.append(" (line ").append(problem.getLine()).append(")");
            }
            Throwable throwable = problem.getThrowable();
            if ((throwable != null) && (throwable.getMessage() != null)) {
                message.append(" - ").append(throwable.getMessage());
            }
            System.err.println(message);
        }
    }

    private static String deriveSourceName(Path sourcePath, Path baseIncludePath) {
        Path normalized = sourcePath.normalize();
        Path base = (baseIncludePath != null) ? baseIncludePath.normalize() : null;
        Path relative = (base != null && normalized.startsWith(base))
            ? base.relativize(normalized)
            : normalized;
        Path withoutExtension = stripExtension(relative);
        String normalizedName = withoutExtension.normalize().toString().replace('\\', '/');

        while (normalizedName.startsWith("/")) {
            normalizedName = normalizedName.substring(1);
        }

        return normalizedName;
    }

    private static Path stripExtension(Path path) {
        if (path == null) {
            return Path.of("<input>");
        }

        Path fileName = path.getFileName();
        if (fileName == null) {
            return path;
        }

        String name = fileName.toString();
        int dot = name.lastIndexOf('.');
        String stem = (dot == -1) ? name : name.substring(0, dot);
        Path parent = path.getParent();

        return (parent == null) ? Path.of(stem) : parent.resolve(stem);
    }
}
