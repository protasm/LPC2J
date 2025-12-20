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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CompilationPipeline {
    private final String parentInternalName;

    public CompilationPipeline(String parentInternalName) {
        this.parentInternalName = parentInternalName;
    }

    public CompilationResult run(String source) {
        return run(source, ParserOptions.defaults());
    }

    public CompilationResult run(String source, ParserOptions parserOptions) {
        ParserOptions options = Objects.requireNonNull(parserOptions, "parserOptions");
        List<CompilationProblem> problems = new ArrayList<>();
        TokenList tokens = null;
        ASTObject astObject = null;
        SemanticModel semanticModel = null;
        TypedIR typedIr = null;
        byte[] bytecode = null;

        Scanner scanner = new Scanner();
        try {
            tokens = scanner.scan(source);
        } catch (ScanException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.SCAN, "Error scanning source", e));
            return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        Parser parser = new Parser(options);
        try {
            astObject = parser.parse("<input>", tokens);
        } catch (ParseException | IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.PARSE, "Error parsing tokens", e));
            return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        try {
            SemanticAnalysisResult analysisResult = analyzer.analyze(astObject);
            semanticModel = analysisResult.semanticModel();
            problems.addAll(analysisResult.problems());

            if (!analysisResult.succeeded())
                return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        } catch (IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE, "Error analyzing ASTObject", e));
            return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        IRLowerer lowerer = new IRLowerer(parentInternalName);
        try {
            IRLoweringResult loweringResult = lowerer.lower(semanticModel);
            typedIr = loweringResult.typedIr();
            problems.addAll(loweringResult.problems());

            if (!loweringResult.succeeded())
                return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        } catch (IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER, "Error lowering semantic model", e));
            return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
        }

        Compiler compiler = new Compiler(parentInternalName);
        try {
            bytecode = compiler.compile(astObject);
        } catch (CompileException | IllegalArgumentException e) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.COMPILE, "Error compiling ASTObject", e));
        }

        return new CompilationResult(tokens, astObject, semanticModel, typedIr, bytecode, problems);
    }
}
