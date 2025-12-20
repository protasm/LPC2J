package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.compiler.CompileException;
import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.ir.IRLowerer;
import io.github.protasm.lpc2j.ir.IRLoweringResult;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.semantic.SemanticAnalysisResult;
import io.github.protasm.lpc2j.semantic.SemanticAnalyzer;
import io.github.protasm.lpc2j.scanner.ScanException;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.token.TokenList;

public class LPC2J {
  private static final String DEFAULT_PARENT = "java/lang/Object";

  public static TokenList scan(String source) {
    Scanner scanner = new Scanner();

    try {
      return scanner.scan(source);
    } catch (ScanException e) {
      System.out.println("Error scanning source");
      System.out.println(e);

      return null;
    }
  }

  public static ASTObject parse(TokenList tokens) {
    return parse(tokens, ParserOptions.defaults());
  }

  public static ASTObject parse(TokenList tokens, ParserOptions parserOptions) {
    if (tokens == null) return null;

    try {
      Parser parser = new Parser(parserOptions);

      return parser.parse("<input>", tokens);
    } catch (ParseException | IllegalArgumentException e) {
      System.out.println("Error parsing tokens");
      System.out.println(e);

      return null;
    }
  }

  public static byte[] compile(ASTObject astObject) {
    if (astObject == null) return null;

    try {
      SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
      if (!analysis.succeeded()) {
        for (CompilationProblem problem : analysis.problems()) {
          System.out.println(problem.getMessage());
          if (problem.getThrowable() != null) {
            System.out.println(problem.getThrowable());
          }
        }
        return null;
      }

      IRLowerer lowerer = new IRLowerer(DEFAULT_PARENT);
      IRLoweringResult loweringResult = lowerer.lower(analysis.semanticModel());
      if (!loweringResult.succeeded()) {
        for (CompilationProblem problem : loweringResult.problems()) {
          System.out.println(problem.getMessage());
          if (problem.getThrowable() != null) {
            System.out.println(problem.getThrowable());
          }
        }
        return null;
      }

      Compiler compiler = new Compiler(DEFAULT_PARENT);

      return compiler.compile(loweringResult.typedIr());
    } catch (CompileException | IllegalArgumentException e) {
      System.out.println("Error compiling ASTObject");

      System.out.println(e);

      return null;
    }
  }

  public static byte[] compile(String source) {
    return compile(source, ParserOptions.defaults());
  }

  public static byte[] compile(String source, ParserOptions parserOptions) {
    CompilationResult result = compileWithDiagnostics(source, parserOptions);
    if (!result.succeeded()) {
      printProblems(result);
      return null;
    }

    return result.getBytecode();
  }

  public static CompilationResult compileWithDiagnostics(String source) {
    return compileWithDiagnostics(source, ParserOptions.defaults());
  }

  public static CompilationResult compileWithDiagnostics(
      String source, ParserOptions parserOptions) {
    CompilationPipeline pipeline = new CompilationPipeline(DEFAULT_PARENT);
    return pipeline.run(source, parserOptions);
  }

  private static void printProblems(CompilationResult result) {
    for (CompilationProblem problem : result.getProblems()) {
      System.out.println(problem.getMessage());
      if (problem.getThrowable() != null) {
        System.out.println(problem.getThrowable());
      }
    }
  }
}
