package io.github.protasm.lpc2j.testing;

import io.github.protasm.lpc2j.ir.IRBlock;
import io.github.protasm.lpc2j.ir.IRBinaryOperation;
import io.github.protasm.lpc2j.ir.IRLowerer;
import io.github.protasm.lpc2j.ir.IRLoweringResult;
import io.github.protasm.lpc2j.ir.IRMethod;
import io.github.protasm.lpc2j.ir.IRReturn;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.PreprocessedSource;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.semantic.SemanticAnalysisResult;
import io.github.protasm.lpc2j.semantic.SemanticAnalyzer;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.sourcepos.SourceMapper;
import io.github.protasm.lpc2j.sourcepos.SourcePos;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenList;
import io.github.protasm.lpc2j.token.TokenType;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight regression harness that exercises the phase-separated compiler pipeline.
 *
 * <p>These tests intentionally avoid JUnit to keep the build self-contained; they throw when any
 * assertion fails so CI callers can rely on the process exit code.</p>
 */
public final class PipelineRegressionTests {
    public static void main(String[] args) {
        List<TestCase> tests = List.of(
                new TestCase("preprocessor tracks include/macro mapping", PipelineRegressionTests::preprocessorMappingIsPreserved),
                new TestCase("scanner spans honor preprocessor mapping", PipelineRegressionTests::scannerSpansReflectMappedFiles),
                new TestCase("semantic analysis surfaces type mismatches", PipelineRegressionTests::semanticAnalysisReportsReturnMismatch),
                new TestCase("parser accepts typed and untyped functions", PipelineRegressionTests::parserAcceptsTypedAndUntypedFunctions),
                new TestCase("semantic normalizes untyped functions", PipelineRegressionTests::semanticDefaultsUntypedFunctionsToMixed),
                new TestCase("IR lowering preserves arithmetic", PipelineRegressionTests::irLoweringBuildsBinaryReturn),
                new TestCase("codegen produces invokable bytecode", PipelineRegressionTests::codegenRoundTripProducesWorkingClass));

        List<String> failures = new ArrayList<>();

        for (TestCase test : tests) {
            try {
                test.runnable().run();
                System.out.println("[PASS] " + test.name());
            } catch (Throwable t) {
                failures.add(test.name() + ": " + t.getMessage());
                System.out.println("[FAIL] " + test.name());
                t.printStackTrace(System.out);
            }
        }

        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder("Regression tests failed:");
            for (String failure : failures) {
                sb.append(System.lineSeparator()).append(" - ").append(failure);
            }
            throw new AssertionError(sb.toString());
        }
    }

    private static void preprocessorMappingIsPreserved() {
        IncludeResolver resolver =
                (includingFile, includePath, system) -> switch (includePath) {
                    case "defs.h" -> "#define VALUE 7\nint inc = VALUE;\n";
                    default -> throw new IllegalArgumentException("Unexpected include: " + includePath);
                };

        String source = "#include \"defs.h\"\nint x = VALUE;\n";
        Preprocessor preprocessor = new Preprocessor(resolver);
        PreprocessedSource processed = preprocessor.preprocessWithMapping(Path.of("main.c"), source);

        SourceMapper mapper = processed.mapper();
        int firstLiteral = processed.source().indexOf('7');
        int secondLiteral = processed.source().indexOf('7', firstLiteral + 1);

        SourcePos literalPos = mapper.originalPos(firstLiteral);
        SourcePos secondLiteralPos = mapper.originalPos(secondLiteral);
        SourcePos mainPos = mapper.originalPos(processed.source().indexOf("int x"));

        assertEquals("defs.h", literalPos.fileName(), "macro expansion should map to include file");
        assertEquals(1, literalPos.line(), "macro body should retain original line");
        assertEquals(15, literalPos.column(), "macro body should retain original column");

        assertEquals("defs.h", secondLiteralPos.fileName(), "subsequent expansions reuse macro source");
        assertEquals("main.c", mainPos.fileName(), "user source should map to calling file");
    }

    private static void scannerSpansReflectMappedFiles() {
        IncludeResolver resolver =
                (includingFile, includePath, system) -> switch (includePath) {
                    case "defs.h" -> "#define VALUE 7\nint inc = VALUE;\n";
                    default -> throw new IllegalArgumentException("Unexpected include: " + includePath);
                };

        String source = "#include \"defs.h\"\nint x = VALUE;\n";
        Scanner scanner = new Scanner(new Preprocessor(resolver));
        TokenList tokens = scanner.scan(Path.of("main.c"), source);

        Token<?> firstInt = find(tokens, TokenType.T_INT_LITERAL, "7", 0);
        Token<?> secondInt = find(tokens, TokenType.T_INT_LITERAL, "7", tokens.size() / 2);

        assertEquals("defs.h", firstInt.span().fileName(), "first macro literal should point at include");
        assertEquals(1, firstInt.span().startLine(), "first macro literal should keep source line");
        assertEquals(15, firstInt.span().startColumn(), "first macro literal should keep source column");
        assertEquals("defs.h", secondInt.span().fileName(), "second macro literal should point at include");
    }

    private static void semanticAnalysisReportsReturnMismatch() {
        String source = "int bad() { return \"oops\"; }\n";
        TokenList tokens = new Scanner().scan(source);
        Parser parser = new Parser();
        ASTObject astObject = parser.parse("SemanticSample", tokens);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);

        assertTrue(!analysis.problems().isEmpty(), "semantic analysis should surface problems");
        assertTrue(
                analysis.problems().stream().anyMatch(p -> p.getMessage().contains("Return type mismatch")),
                "return type mismatch should be reported");
    }

    private static void parserAcceptsTypedAndUntypedFunctions() {
        String source = "foo(bar) { return bar; }\nint typed(string name) { return 1; }\n";

        TokenList tokens = new Scanner().scan(source);
        Parser parser = new Parser();
        ASTObject astObject = parser.parse("ParsingSample", tokens);

        assertEquals(2, astObject.methods().size(), "both methods should be parsed");
        assertTrue(astObject.methods().get("foo") != null, "untyped method should exist");
        assertTrue(astObject.methods().get("typed") != null, "typed method should exist");
    }

    private static void semanticDefaultsUntypedFunctionsToMixed() {
        String source = "legacy(x) { x = 5; }\n";
        TokenList tokens = new Scanner().scan(source);
        Parser parser = new Parser();
        ASTObject astObject = parser.parse("LegacySample", tokens);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
        assertTrue(analysis.succeeded(), "semantic analysis should succeed for untyped methods");

        ASTMethod method = astObject.methods().get("legacy");
        ASTParameter parameter = method.parameters().get(0);
        ASTStatement lastStatement = method.body().statements().get(method.body().statements().size() - 1);

        assertEquals(LPCType.LPCMIXED, method.symbol().lpcType(), "untyped method should default to mixed return type");
        assertEquals(
                LPCType.LPCMIXED, parameter.symbol().lpcType(), "untyped parameter should default to mixed type");
        assertTrue(lastStatement instanceof ASTStmtReturn, "implicit return should be synthesized");
        assertEquals(
                LPCType.LPCINT,
                ((ASTStmtReturn) lastStatement).returnValue().lpcType(),
                "implicit return should follow legacy 'return 0' semantics");
    }

    private static void irLoweringBuildsBinaryReturn() {
        String source = "int add(int a, int b) { return a + b; }\n";
        TokenList tokens = new Scanner().scan(source);
        Parser parser = new Parser();
        ASTObject astObject = parser.parse("IRSample", tokens);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
        assertTrue(analysis.succeeded(), "semantic analysis should succeed for valid sample");

        IRLoweringResult lowering = new IRLowerer("java/lang/Object").lower(analysis.semanticModel());
        assertTrue(lowering.succeeded(), "lowering should succeed");

        IRMethod method = lowering.typedIr().object().methods().get(0);
        IRBlock entry = method.entryBlock().orElseThrow(() -> new AssertionError("missing entry block"));
        IRReturn returnTerminator = (IRReturn) entry.terminator();
        IRBinaryOperation operation = (IRBinaryOperation) returnTerminator.returnValue();

        assertEquals("add", method.name(), "method name should be preserved");
        assertEquals("BOP_ADD", operation.operator().name(), "binary operation should be addition");
    }

    private static void codegenRoundTripProducesWorkingClass() throws Exception {
        String source = "int add(int a, int b) { return a + b; }\n";
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/Add", ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + result.getProblems());
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.Add";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method add = clazz.getMethod("add", int.class, int.class);
        Object value = add.invoke(instance, 2, 3);

        assertEquals(5, ((Number) value).intValue(), "generated class should add arguments");
    }

    private static Token<?> find(TokenList tokens, TokenType type, String lexeme, int start) {
        for (int i = start; i < tokens.size(); i++) {
            Token<?> token = tokens.get(i);
            if ((token.type() == type) && Objects.equals(token.lexeme(), lexeme))
                return token;
        }

        throw new AssertionError("Token " + type + "(" + lexeme + ") not found in list of size " + tokens.size());
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual))
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record TestCase(String name, ThrowingRunnable runnable) {}
}
