package io.github.protasm.lpc2j.pipeline;

import io.github.protasm.lpc2j.compiler.CompileException;
import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.ScanException;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.ir.IRLowerer;
import io.github.protasm.lpc2j.ir.IRLoweringResult;
import io.github.protasm.lpc2j.ir.TypedIR;
import io.github.protasm.lpc2j.semantic.SemanticAnalysisResult;
import io.github.protasm.lpc2j.semantic.SemanticAnalyzer;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import io.github.protasm.lpc2j.token.TokenList;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.preproc.IncludeResolution;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CompilationPipeline {
    private final String parentInternalName;
    private final RuntimeContext runtimeContext;

    public CompilationPipeline(String parentInternalName) {
        this(parentInternalName, new RuntimeContext(null));
    }

    public CompilationPipeline(String parentInternalName, RuntimeContext runtimeContext) {
        this.parentInternalName = parentInternalName;
        this.runtimeContext = (runtimeContext != null) ? runtimeContext : new RuntimeContext(null);
    }

    public CompilationResult run(String source) {
        return run(source, ParserOptions.defaults());
    }

    public CompilationResult run(String source, ParserOptions parserOptions) {
        return run(null, source, null, null, parserOptions);
    }

    public CompilationResult run(Path sourcePath, String source, ParserOptions parserOptions) {
        return run(sourcePath, source, null, null, parserOptions);
    }

    public CompilationResult run(
            Path sourcePath, String source, String sourceName, String displayPath, ParserOptions parserOptions) {
        ParserOptions options = Objects.requireNonNull(parserOptions, "parserOptions");
        CompilationUnit unit = new CompilationUnit(sourcePath, sourceName, displayPath, source);
        Set<String> inheritanceStack = new HashSet<>();
        inheritanceStack.add(inheritanceKey(unit));
        return run(unit, options, true, inheritanceStack);
    }

    private CompilationResult run(
            CompilationUnit unit, ParserOptions parserOptions, boolean includeCodegen, Set<String> inheritanceStack) {
        List<CompilationProblem> problems = new ArrayList<>();
        TokenList tokens = null;
        ASTObject astObject = null;
        SemanticModel semanticModel = null;
        TypedIR typedIr = null;
        byte[] bytecode = null;

        Scanner scanner = new Scanner(runtimeContext.newPreprocessor());
        try {
            tokens = scanner.scan(unit.sourcePath(), unit.source(), unit.displayPath());
            unit.setTokens(tokens);
        } catch (ScanException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.SCAN, "Error scanning source", e));
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        Parser parser = new Parser(runtimeContext, parserOptions);
        try {
            astObject = parser.parse(unit.parseName(), tokens);
            unit.setASTObject(astObject);
            String inheritedPath = normalizeInheritedPath(astObject.parentName());
            unit.setInheritedPath(inheritedPath);
            astObject.setParentName(inheritedPath);
        } catch (ParseException | IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE, "Error parsing tokens", e));
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        if (unit.inheritedPath() != null) {
            CompilationUnit parentUnit =
                    resolveAndAnalyzeParent(unit, parserOptions, inheritanceStack, problems);
            unit.setParentUnit(parentUnit);
            if (!problems.isEmpty()) {
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
            }

            if (astObject != null && parentUnit != null && parentUnit.astObject() != null)
                astObject.setParentName(parentUnit.astObject().name());
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(runtimeContext);
        try {
            SemanticAnalysisResult analysisResult = analyzer.analyze(unit);
            semanticModel = analysisResult.semanticModel();
            unit.setSemanticModel(semanticModel);
            problems.addAll(analysisResult.problems());

            if (!analysisResult.succeeded())
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        } catch (IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE, "Error analyzing ASTObject", e));
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        if (!includeCodegen)
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);

        IRLowerer lowerer = new IRLowerer(parentInternalName);
        try {
            IRLoweringResult loweringResult = lowerer.lower(semanticModel);
            typedIr = loweringResult.typedIr();
            problems.addAll(loweringResult.problems());

            if (!loweringResult.succeeded())
                return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        } catch (IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER, "Error lowering semantic model", e));
            return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        Compiler compiler = new Compiler(parentInternalName);
        try {
            bytecode = compiler.compile(typedIr);
        } catch (CompileException | IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.COMPILE, "Error compiling typed IR", e));
        }

        return new CompilationResult(unit, tokens, astObject, semanticModel, typedIr, bytecode, problems);
    }

    private CompilationUnit resolveAndAnalyzeParent(
            CompilationUnit childUnit,
            ParserOptions parserOptions,
            Set<String> inheritanceStack,
            List<CompilationProblem> problems) {
        IncludeResolver resolver = runtimeContext.includeResolver();
        IncludeResolution resolution;
        String inheritedPath = normalizeInheritedPath(childUnit.inheritedPath());

        try {
            resolution = resolver.resolve(childUnit.sourcePath(), inheritedPath, false);
        } catch (Exception e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE,
                            "Cannot inherit '" + childUnit.inheritedPath() + "': " + e.getMessage(),
                            e));
            return null;
        }

        CompilationUnit parentUnit =
                new CompilationUnit(
                        resolution.resolvedPath(),
                        resolvedParentSourceName(childUnit, inheritedPath, resolution),
                        resolution.displayPath(),
                        resolution.source());

        String parentKey = inheritanceKey(parentUnit);
        if (inheritanceStack.contains(parentKey)) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE,
                            "Circular inherit detected for '" + parentUnit.parseName() + "'",
                            (Throwable) null));
            return null;
        }

        Set<String> nextStack = new HashSet<>(inheritanceStack);
        nextStack.add(parentKey);

        CompilationResult parentResult = run(parentUnit, parserOptions, false, nextStack);
        problems.addAll(parentResult.getProblems());

        if (parentResult.getSemanticModel() == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Failed to analyze inherited object '" + childUnit.inheritedPath() + "'",
                            (Throwable) null));
        }

        return parentUnit;
    }

    private String inheritanceKey(CompilationUnit unit) {
        if (unit.sourcePath() != null)
            return unit.sourcePath().normalize().toString();

        if (unit.displayPath() != null)
            return unit.displayPath();

        return unit.parseName();
    }

    private String normalizeInheritedPath(String inheritedPath) {
        if (inheritedPath == null)
            return null;

        String trimmed = inheritedPath.trim();

        if (trimmed.isEmpty())
            return null;

        if ((trimmed.length() >= 2) && trimmed.startsWith("\"") && trimmed.endsWith("\""))
            trimmed = trimmed.substring(1, trimmed.length() - 1);

        while (trimmed.startsWith("/"))
            trimmed = trimmed.substring(1);

        if (trimmed.isEmpty())
            return null;

        return trimmed;
    }

    private String resolvedParentSourceName(
            CompilationUnit childUnit, String inheritedPath, IncludeResolution resolution) {
        String normalized =
                normalizeInheritedPath((resolution.displayPath() != null) ? resolution.displayPath() : inheritedPath);
        if ((normalized == null) || normalized.isEmpty())
            return null;

        Path path = Path.of(normalized);
        String stem = stripExtension(path.getFileName().toString());
        Path withoutExt = (path.getParent() == null) ? Path.of(stem) : path.getParent().resolve(stem);
        Path normalizedPath = withoutExt.normalize();

        if ((childUnit.sourceName() != null) && !normalizedPath.isAbsolute()) {
            Path childPath = Path.of(childUnit.sourceName());
            if (childPath.getNameCount() > 0) {
                Path childPrefix = childPath.getName(0);
                if ((normalizedPath.getNameCount() == 0) || !normalizedPath.getName(0).equals(childPrefix))
                    normalizedPath = childPrefix.resolve(normalizedPath).normalize();
            }
        }

        String normalizedName = normalizedPath.toString();

        normalizedName = normalizedName.replace('\\', '/');

        while (normalizedName.startsWith("/"))
            normalizedName = normalizedName.substring(1);

        return normalizedName;
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }
}
