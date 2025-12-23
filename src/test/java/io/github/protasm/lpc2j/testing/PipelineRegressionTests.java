package io.github.protasm.lpc2j.testing;

import io.github.protasm.lpc2j.console.ConsoleConfig;
import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;
import io.github.protasm.lpc2j.ir.IRBlock;
import io.github.protasm.lpc2j.ir.IRBinaryOperation;
import io.github.protasm.lpc2j.ir.IRLowerer;
import io.github.protasm.lpc2j.ir.IRLoweringResult;
import io.github.protasm.lpc2j.ir.IRMethod;
import io.github.protasm.lpc2j.ir.IRReturn;
import io.github.protasm.lpc2j.parser.ParserNew;
import io.github.protasm.lpc2j.parser.ParserOptions;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.pipeline.CompilationPipeline;
import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationResult;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import io.github.protasm.lpc2j.preproc.IncludeResolution;
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
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.console.ConsoleLineReader;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
                new TestCase("semantic defers inherit validation to analysis", PipelineRegressionTests::semanticFlagsInvalidInheritancePlacement),
                new TestCase("semantic reports definition without declaration", PipelineRegressionTests::semanticReportsDefinitionWithoutDeclaration),
                new TestCase("parser accepts typed and untyped functions", PipelineRegressionTests::parserAcceptsTypedAndUntypedFunctions),
                new TestCase("parser concatenates adjacent string literals", PipelineRegressionTests::parserConcatenatesAdjacentStringLiterals),
                new TestCase("semantic normalizes untyped functions", PipelineRegressionTests::semanticDefaultsUntypedFunctionsToMixed),
                new TestCase(
                        "semantic resolves parent calls to inherited methods",
                        PipelineRegressionTests::semanticResolvesParentCallsToParentMethods),
                new TestCase(
                        "semantic rejects parent calls without a parent",
                        PipelineRegressionTests::semanticRejectsParentCallsWhenNoParent),
                new TestCase(
                        "semantic rejects parent calls when parent omits method",
                        PipelineRegressionTests::semanticRejectsParentCallsWhenMethodMissing),
                new TestCase("IR lowering preserves arithmetic", PipelineRegressionTests::irLoweringBuildsBinaryReturn),
                new TestCase("codegen produces invokable bytecode", PipelineRegressionTests::codegenRoundTripProducesWorkingClass),
                new TestCase("instance calls honor declared parameter types", PipelineRegressionTests::instanceCallsHonorDeclaredParameterTypes),
                new TestCase("dynamic invoke results coerce to integers", PipelineRegressionTests::dynamicInvokeResultsCoerceToIntegers),
                new TestCase(
                        "inheritance lowers to Java extends and super dispatch",
                        PipelineRegressionTests::inheritanceLowersToJavaSuperCalls),
                new TestCase(
                        "inheritance samples exercise pipeline end-to-end",
                        PipelineRegressionTests::inheritanceSamplesExercisePipeline),
                new TestCase("field initializers run in constructor", PipelineRegressionTests::fieldInitializersExecute),
                new TestCase("truthiness and logical negation follow LPC rules", PipelineRegressionTests::truthinessAndLogicalNegationFollowLpcRules),
                new TestCase("arrays parse and execute basic operations", PipelineRegressionTests::arraysBehave),
                new TestCase("mappings parse and execute basic operations", PipelineRegressionTests::mappingsBehave),
                new TestCase("console loads system include directories from config", PipelineRegressionTests::consoleConfigLoadsSystemIncludes),
                new TestCase("console rejects missing base path", PipelineRegressionTests::consoleRejectsMissingBasePath),
                new TestCase("console recalls previous commands with arrow keys", PipelineRegressionTests::consoleReadsHistoryWithArrows),
                new TestCase("console down arrow returns to a blank entry", PipelineRegressionTests::consoleReturnsToEmptyHistorySlot),
                new TestCase("console load resolves inherited objects", PipelineRegressionTests::consoleLoadHandlesInheritance));

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
                (includingFile, includePath, system) ->
                    switch (includePath) {
                      case "defs.h" ->
                          new IncludeResolution(
                              "#define VALUE 7\nint inc = VALUE;\n", Path.of("defs.h"), "defs.h");
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
                (includingFile, includePath, system) ->
                    switch (includePath) {
                      case "defs.h" ->
                          new IncludeResolution(
                              "#define VALUE 7\nint inc = VALUE;\n", Path.of("defs.h"), "defs.h");
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
        ParserNew parser = new ParserNew();
        ASTObject astObject = parser.parse("SemanticSample", tokens);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);

        assertTrue(!analysis.problems().isEmpty(), "semantic analysis should surface problems");
        assertTrue(
                analysis.problems().stream().anyMatch(p -> p.getMessage().contains("Return type mismatch")),
                "return type mismatch should be reported");
    }

    private static void semanticFlagsInvalidInheritancePlacement() {
        String source = ""
                + "inherit \"/std/base1\";\n"
                + "int value;\n"
                + "inherit \"/std/base2\";\n";
        TokenList tokens = new Scanner().scan(source);
        ParserNew parser = new ParserNew();
        ASTObject astObject = parser.parse("InheritanceSample", tokens);

        assertEquals(2, astObject.inherits().size(), "parser should record all inherit directives");
        int firstPropertyLine = astObject.fields().iterator().next().line();
        List<Integer> inheritLines = astObject.inherits().stream().map(inherit -> inherit.line()).toList();

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
        String problemSummary = describeProblems(analysis.problems());

        assertTrue(!analysis.succeeded(), "semantic analysis should fail for invalid inheritance layout");
        assertTrue(
                analysis.problems().stream().anyMatch(p -> p.getMessage().contains("Only one inherit")),
                "semantic analysis should reject multiple inherit statements (" + problemSummary + ")");
        assertTrue(
                analysis.problems().stream()
                        .anyMatch(p -> p.getMessage().contains("inherit statements must appear before any variable or function declarations.")),
                "semantic analysis should enforce inherit ordering (" + problemSummary + ", inheritLines=" + inheritLines
                        + ", firstPropertyLine=" + firstPropertyLine + ")");
    }

    private static void semanticReportsDefinitionWithoutDeclaration() {
        ASTObject astObject = new ASTObject(1, "OrphanedDefinitions");

        Symbol fieldSymbol = new Symbol("int", "ghost_field");
        ASTField orphanField = new ASTField(2, astObject.name(), fieldSymbol, false);
        orphanField.markDefined();
        astObject.fields().put(fieldSymbol.name(), orphanField);

        Symbol methodSymbol = new Symbol("void", "ghost_method");
        ASTMethod orphanMethod = new ASTMethod(3, astObject.name(), methodSymbol, false);
        orphanMethod.markDefined();
        astObject.methods().put(methodSymbol.name(), orphanMethod);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
        String problemSummary = describeProblems(analysis.problems());

        assertTrue(
                analysis.problems().stream()
                        .anyMatch(p -> p.getMessage().contains("Field 'ghost_field' is defined without a prior declaration.")),
                "semantic analysis should flag orphaned field definitions (" + problemSummary + ")");
        assertTrue(
                analysis.problems().stream()
                        .anyMatch(p -> p.getMessage().contains("Method 'ghost_method' is defined without a prior declaration.")),
                "semantic analysis should flag orphaned method definitions (" + problemSummary + ")");
    }

    private static void parserAcceptsTypedAndUntypedFunctions() {
        String source = "foo(bar) { return bar; }\nint typed(string name) { return 1; }\n";

        TokenList tokens = new Scanner().scan(source);
        ParserNew parser = new ParserNew();
        ASTObject astObject = parser.parse("ParsingSample", tokens);

        assertEquals(2, astObject.methods().size(), "both methods should be parsed");
        assertTrue(astObject.methods().get("foo") != null, "untyped method should exist");
        assertTrue(astObject.methods().get("typed") != null, "typed method should exist");
    }

    private static void parserConcatenatesAdjacentStringLiterals() {
        String source = ""
                + "#define LONG_DESC \"First line.\\n\" \\\n"
                + "\"Second line.\\n\"\n"
                + "string description = LONG_DESC;\n";

        TokenList tokens = new Scanner().scan(Path.of("/obj/room.c"), source, "/obj/room.c");
        ParserNew parser = new ParserNew();
        ASTObject astObject = parser.parse("RoomSample", tokens);

        ASTField field = astObject.fields().get("description");
        assertTrue(field != null, "field should be present after parsing");

        ASTExpression initializer = field.initializer();
        assertTrue(initializer instanceof ASTExprLiteralString, "initializer should be a string literal");
        assertEquals("First line.\\nSecond line.\\n", ((ASTExprLiteralString) initializer).value(), "string literals should concatenate");
    }

    private static void semanticDefaultsUntypedFunctionsToMixed() {
        String source = "legacy(x) { x = 5; }\n";
        TokenList tokens = new Scanner().scan(source);
        ParserNew parser = new ParserNew();
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

    private static void semanticResolvesParentCallsToParentMethods() {
        String parentSource = "int shout() { return 1; }\n";
        Scanner scanner = new Scanner();
        ParserNew parser = new ParserNew();

        TokenList parentTokens = scanner.scan(parentSource);
        ASTObject parentAst = parser.parse("ParentObject", parentTokens);
        CompilationUnit parentUnit = new CompilationUnit(null, "ParentObject", null, parentSource);
        parentUnit.setTokens(parentTokens);
        parentUnit.setASTObject(parentAst);
        SemanticAnalysisResult parentAnalysis = new SemanticAnalyzer().analyze(parentUnit);
        parentUnit.setSemanticModel(parentAnalysis.semanticModel());
        if (!parentAnalysis.succeeded())
            throw new AssertionError("parent analysis should succeed: " + describeProblems(parentAnalysis.problems()));

        String childSource = "inherit \"/parent\";\nint call_parent() { return ::shout(); }\n";
        TokenList childTokens = scanner.scan(childSource);
        ASTObject childAst = parser.parse("ChildObject", childTokens);
        CompilationUnit childUnit = new CompilationUnit(null, "ChildObject", null, childSource);
        childUnit.setTokens(childTokens);
        childUnit.setASTObject(childAst);
        childUnit.setParentUnit(parentUnit);

        SemanticAnalysisResult childAnalysis = new SemanticAnalyzer().analyze(childUnit);
        if (!childAnalysis.succeeded())
            throw new AssertionError("child analysis should succeed: " + describeProblems(childAnalysis.problems()));

        ASTMethod callParent = childAst.methods().get("call_parent");
        ASTMethod parentMethod = parentAst.methods().get("shout");
        ASTStmtReturn ret = (ASTStmtReturn) callParent.body().statements().get(0);
        ASTExprCallMethod call = (ASTExprCallMethod) ret.returnValue();

        assertTrue(call.isParentDispatch(), "call should be marked as parent dispatch");
        assertTrue(call.method() == parentMethod, "call should resolve to parent method definition");
    }

    private static void semanticRejectsParentCallsWhenNoParent() {
        String source = "int lonely() { return ::missing(); }\n";
        TokenList tokens = new Scanner().scan(source);
        ParserNew parser = new ParserNew();
        ASTObject astObject = parser.parse("LonelyChild", tokens);

        SemanticAnalysisResult analysis = new SemanticAnalyzer().analyze(astObject);
        String problems = describeProblems(analysis.problems());

        assertTrue(!analysis.succeeded(), "semantic analysis should fail without parent");
        assertTrue(
                analysis.problems().stream()
                        .anyMatch(p -> p.getMessage().contains("without a parent object")),
                "error should mention missing parent (" + problems + ")");
    }

    private static void semanticRejectsParentCallsWhenMethodMissing() {
        String parentSource = "int stub() { return 0; }\n";
        Scanner scanner = new Scanner();
        ParserNew parser = new ParserNew();

        TokenList parentTokens = scanner.scan(parentSource);
        ASTObject parentAst = parser.parse("BareParent", parentTokens);
        CompilationUnit parentUnit = new CompilationUnit(null, "BareParent", null, parentSource);
        parentUnit.setTokens(parentTokens);
        parentUnit.setASTObject(parentAst);
        SemanticAnalysisResult parentAnalysis = new SemanticAnalyzer().analyze(parentUnit);
        parentUnit.setSemanticModel(parentAnalysis.semanticModel());
        if (!parentAnalysis.succeeded())
            throw new AssertionError("parent analysis should succeed: " + describeProblems(parentAnalysis.problems()));

        String childSource = "inherit \"/parent\";\nint child() { return ::missing(); }\n";
        TokenList childTokens = scanner.scan(childSource);
        ASTObject childAst = parser.parse("BareChild", childTokens);
        CompilationUnit childUnit = new CompilationUnit(null, "BareChild", null, childSource);
        childUnit.setTokens(childTokens);
        childUnit.setASTObject(childAst);
        childUnit.setParentUnit(parentUnit);

        SemanticAnalysisResult childAnalysis = new SemanticAnalyzer().analyze(childUnit);
        String problems = describeProblems(childAnalysis.problems());

        assertTrue(!childAnalysis.succeeded(), "semantic analysis should fail when parent method is missing");
        assertTrue(
                childAnalysis.problems().stream()
                        .anyMatch(p -> p.getMessage().contains("not defined in the parent object")),
                "error should mention missing parent method (" + problems + ")");
    }

    private static void irLoweringBuildsBinaryReturn() {
        String source = "int add(int a, int b) { return a + b; }\n";
        TokenList tokens = new Scanner().scan(source);
        ParserNew parser = new ParserNew();
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
        CompilationResult result = pipeline.run(null, source, "regression/Add", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
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

    private static void instanceCallsHonorDeclaredParameterTypes() throws Exception {
        String source = ""
                + "status stored;\n"
                + "void set_flag(status flag) { stored = flag; }\n"
                + "status query_flag() { return stored; }\n"
                + "void create() { set_flag(1); }\n";

        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/FlagHolder", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.FlagHolder";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method create = clazz.getMethod("create");
        Method queryFlag = clazz.getMethod("query_flag");

        create.invoke(instance);
        Object flag = queryFlag.invoke(instance);

        assertEquals(Boolean.TRUE, flag, "status arguments should coerce int literals to booleans when invoking methods");
    }

    private static void dynamicInvokeResultsCoerceToIntegers() throws Exception {
        String source = ""
                + "int strength;\n"
                + "int compare_strength(object other) {\n"
                + "  if (!other) return 0;\n"
                + "  return strength - other->query_strength();\n"
                + "}\n";

        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/OrcLike", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.OrcLike";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();

        Field strengthField = clazz.getDeclaredField("strength");
        strengthField.setAccessible(true);
        strengthField.setInt(instance, 10);

        Object rival = new StrengthProbe(7);

        Method compare = clazz.getMethod("compare_strength", Object.class);
        int difference = ((Number) compare.invoke(instance, rival)).intValue();
        int nullCase = ((Number) compare.invoke(instance, new Object[] {null})).intValue();

        assertEquals(3, difference, "dynamic invocation results should be coerced to integers");
        assertEquals(0, nullCase, "compare_strength should handle null objects");
    }

    private static void inheritanceLowersToJavaSuperCalls() throws Exception {
        String parentSource = "int shout() { return 1; }\n";
        IncludeResolver resolver = (includingFile, includePath, system) ->
                new IncludeResolution(parentSource, Path.of("parent.c"), "regression/Parent");

        RuntimeContext runtimeContext = new RuntimeContext(resolver);
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);

        CompilationResult parentResult =
                pipeline.run(null, parentSource, "regression/Parent", null, ParserOptions.defaults());
        if (!parentResult.succeeded())
            throw new AssertionError("parent compilation should succeed: " + describeProblems(parentResult.getProblems()));

        String childSource = ""
                + "inherit \"/parent\";\n"
                + "int shout() { return 2; }\n"
                + "int call_parent() { return ::shout(); }\n"
                + "int call_self() { return shout(); }\n";

        CompilationResult childResult =
                pipeline.run(null, childSource, "regression/Child", null, ParserOptions.defaults());

        if (!childResult.succeeded()) {
            throw new AssertionError("child compilation should succeed: " + describeProblems(childResult.getProblems()));
        }

        byte[] parentBytecode = parentResult.getBytecode();
        byte[] childBytecode = childResult.getBytecode();

        class ByteArrayLoader extends ClassLoader {
            Class<?> defineBytes(String binaryName, byte[] bytecode) {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }

        ByteArrayLoader loader = new ByteArrayLoader();
        Class<?> parentClass = loader.defineBytes("regression.Parent", parentBytecode);
        Class<?> childClass = loader.defineBytes("regression.Child", childBytecode);

        assertEquals(
                parentClass, childClass.getSuperclass(), "child class should extend the resolved parent class");

        Object child = childClass.getDeclaredConstructor().newInstance();
        assertEquals(
                1,
                ((Number) childClass.getMethod("call_parent").invoke(child)).intValue(),
                "parent dispatch should target the parent implementation");
        assertEquals(
                2,
                ((Number) childClass.getMethod("call_self").invoke(child)).intValue(),
                "virtual dispatch should target the override");
    }

    private static void inheritanceSamplesExercisePipeline() throws Exception {
        Path sampleRoot = Path.of("lpc2j/sample");
        Path sampleDir = sampleRoot.resolve("inheritance");
        IncludeResolver resolver = new SearchPathIncludeResolver(sampleRoot, List.of());

        RuntimeContext runtimeContext = new RuntimeContext(resolver);
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", runtimeContext);
        ByteArrayLoader loader = new ByteArrayLoader();

        Path standalonePath = sampleDir.resolve("standalone_basic.c");
        String standaloneDisplay = sampleDisplayName(sampleRoot, standalonePath);
        CompilationResult standaloneResult =
                pipeline.run(standalonePath, Files.readString(standalonePath), standaloneDisplay, standaloneDisplay, ParserOptions.defaults());
        assertTrue(standaloneResult.succeeded(), "standalone sample should compile cleanly (" + describeProblems(standaloneResult.getProblems()) + ")");
        Class<?> standaloneClass = loader.define(binaryName(standaloneDisplay), standaloneResult.getBytecode());
        Object standaloneInstance = standaloneClass.getDeclaredConstructor().newInstance();
        assertEquals("standalone", standaloneClass.getMethod("describe").invoke(standaloneInstance), "standalone method dispatch should work");
        int incremented = ((Number) standaloneClass.getMethod("increment", int.class).invoke(standaloneInstance, 3)).intValue();
        assertEquals(5, incremented, "standalone field mutation should persist");
        int squared = ((Number) standaloneClass.getMethod("squared_counter").invoke(standaloneInstance)).intValue();
        assertEquals(25, squared, "standalone methods should observe updated fields");

        Path parentPath = sampleDir.resolve("inherit_parent.c");
        String parentDisplay = sampleDisplayName(sampleRoot, parentPath);
        CompilationResult parentResult =
                pipeline.run(parentPath, Files.readString(parentPath), parentDisplay, parentDisplay, ParserOptions.defaults());
        assertTrue(parentResult.succeeded(), "parent sample should compile cleanly (" + describeProblems(parentResult.getProblems()) + ")");
        Class<?> parentClass = loader.define(binaryName(parentDisplay), parentResult.getBytecode());
        Object parentInstance = parentClass.getDeclaredConstructor().newInstance();
        assertEquals(101, ((Number) parentClass.getMethod("shout").invoke(parentInstance)).intValue(), "parent method should use its own field");
        assertEquals(25, ((Number) parentClass.getMethod("parent_sum").invoke(parentInstance)).intValue(), "parent initializer should run before methods");
        assertEquals(-1, ((Number) parentClass.getMethod("manual_marker").invoke(parentInstance)).intValue(), "driver should not invoke user hooks");

        Path childPath = sampleDir.resolve("inherit_child.c");
        String childDisplay = sampleDisplayName(sampleRoot, childPath);
        CompilationResult childResult =
                pipeline.run(childPath, Files.readString(childPath), childDisplay, childDisplay, ParserOptions.defaults());
        assertTrue(childResult.succeeded(), "child sample should compile cleanly (" + describeProblems(childResult.getProblems()) + ")");
        Class<?> childClass = loader.define(binaryName(childDisplay), childResult.getBytecode());
        assertEquals(parentClass, childClass.getSuperclass(), "child should extend resolved parent class");
        Object childInstance = childClass.getDeclaredConstructor().newInstance();

        assertEquals(
                205, ((Number) childClass.getMethod("call_self_shout").invoke(childInstance)).intValue(), "virtual dispatch should target override");
        assertEquals(
                101, ((Number) childClass.getMethod("call_parent_shout").invoke(childInstance)).intValue(), "explicit parent dispatch should bypass override");
        assertEquals(6, ((Number) childClass.getMethod("combined_shadow").invoke(childInstance)).intValue(), "shadowed fields should not collide across classes");
        assertEquals(36, ((Number) childClass.getMethod("totals").invoke(childInstance)).intValue(), "inherited fields should be accessible in child code");

        // Driver-managed initialization should run exactly once per instance and chain parent then child.
        int shadowed = readIntField(childClass, childInstance, "shadowed_field");
        int parentField = readIntField(parentClass, childInstance, "parent_field");
        int childOnly = readIntField(childClass, childInstance, "child_only");
        int childInitOrder = readIntField(childClass, childInstance, "child_init_order");
        assertEquals(5, shadowed, "child shadowed_field should retain child value");
        assertEquals(10, parentField, "parent_field should be initialized in parent class");
        assertEquals(6, childOnly, "child-only field should initialize from child shadowed_field");
        assertEquals(20, childInitOrder, "child initialization should see parent-initialized values");
        Method childInit = childClass.getDeclaredMethod("$lpc$init");
        childInit.setAccessible(true);
        childInit.invoke(childInstance); // second invocation should be a no-op due to guard
        assertEquals(10, readIntField(parentClass, childInstance, "parent_field"), "init guard should prevent double initialization");
        assertEquals(6, readIntField(childClass, childInstance, "child_only"), "init guard should avoid rerunning child initializers");

        // Field layout: parent fields remain on parent, child fields stay on child, names stay unique.
        ensureDeclaredFieldsUnique(childClass);
        assertTrue(!hasDeclaredField(childClass, "parent_field"), "parent_field should not be declared on the child class");
        assertTrue(hasDeclaredField(parentClass, "parent_field"), "parent_field should be declared on the parent class");
        assertTrue(hasDeclaredField(parentClass, "shadowed_field"), "shadowed_field should exist on parent class");
        assertTrue(hasDeclaredField(childClass, "shadowed_field"), "shadowed_field should exist on child class");

        // Parent dispatch should mutate parent storage only.
        int bumped = ((Number) childClass.getMethod("parent_field_via_method", int.class).invoke(childInstance, 3)).intValue();
        assertEquals(13, bumped, "parent bump should update parent field");
        assertEquals(13, readIntField(parentClass, childInstance, "parent_field"), "parent_field should reflect bump");
        assertEquals(5, readIntField(childClass, childInstance, "shadowed_field"), "child shadowed_field should remain unchanged");

        // No user-defined methods should be auto-invoked by driver-managed lifecycle.
        assertEquals(0, ((Number) childClass.getMethod("manual_check").invoke(childInstance)).intValue(), "driver should not call user-defined setup");
        childClass.getMethod("run_manual_setup").invoke(childInstance);
        assertEquals(16, ((Number) childClass.getMethod("manual_check").invoke(childInstance)).intValue(), "manual setup should run only when explicitly invoked");

        Path simplePath = sampleDir.resolve("no_inherit_simple.c");
        String simpleDisplay = sampleDisplayName(sampleRoot, simplePath);
        CompilationResult simpleResult =
                pipeline.run(simplePath, Files.readString(simplePath), simpleDisplay, simpleDisplay, ParserOptions.defaults());
        assertTrue(simpleResult.succeeded(), "non-inheritance sample should compile cleanly (" + describeProblems(simpleResult.getProblems()) + ")");
        Class<?> simpleClass = loader.define(binaryName(simpleDisplay), simpleResult.getBytecode());
        assertEquals(42, ((Number) simpleClass.getMethod("get_value").invoke(simpleClass.getDeclaredConstructor().newInstance())).intValue(), "simple object should initialize and dispatch");

        Path invalidPath = sampleDir.resolve("invalid_duplicate_field.c");
        String invalidDisplay = sampleDisplayName(sampleRoot, invalidPath);
        CompilationResult invalidResult =
                pipeline.run(invalidPath, Files.readString(invalidPath), invalidDisplay, invalidDisplay, ParserOptions.defaults());
        assertTrue(!invalidResult.succeeded(), "invalid sample should fail compilation");
        assertTrue(
                invalidResult.getProblems().stream().anyMatch(p -> p.getMessage().contains("Duplicate field")),
                "duplicate field should surface as a semantic error");
    }

    private static void fieldInitializersExecute() throws Exception {
        String source = "string short_desc = \"a rusty sword\";\nshort() { return short_desc; }\n";
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/Sword", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.Sword";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method shortMethod = clazz.getMethod("short");
        Object value = shortMethod.invoke(instance);

        assertEquals("a rusty sword", value, "field initializer should populate short_desc");
    }

    private static void truthinessAndLogicalNegationFollowLpcRules() throws Exception {
        String source = ""
                + "int notIntZero() { return !0; }\n"
                + "int notIntNonZero() { return !42; }\n"
                + "int notStringEmpty() { string s = \"\"; return !s; }\n"
                + "int notStringNonEmpty() { return !\"abc\"; }\n"
                + "int notObject(object o) { return !o; }\n"
                + "int notMixed(mixed x) { return !x; }\n"
                + "int ifOnString() { string s = \"\"; if (s) return 1; return 0; }\n";

        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/Truthiness", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.Truthiness";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();

        assertEquals(1, ((Number) clazz.getMethod("notIntZero").invoke(instance)).intValue(), "!0 should be truthy");
        assertEquals(0, ((Number) clazz.getMethod("notIntNonZero").invoke(instance)).intValue(), "!non-zero should be false");
        assertEquals(0, ((Number) clazz.getMethod("notStringEmpty").invoke(instance)).intValue(), "empty string is truthy");
        assertEquals(0, ((Number) clazz.getMethod("notStringNonEmpty").invoke(instance)).intValue(), "non-empty string is truthy");

        Object nullObject = null;
        Object someObject = new Object();
        assertEquals(1, ((Number) clazz.getMethod("notObject", Object.class).invoke(instance, nullObject)).intValue(), "null object is falsey");
        assertEquals(0, ((Number) clazz.getMethod("notObject", Object.class).invoke(instance, someObject)).intValue(), "non-null object is truthy");

        assertEquals(1, ((Number) clazz.getMethod("notMixed", Object.class).invoke(instance, 0)).intValue(), "mixed zero is falsey");
        assertEquals(0, ((Number) clazz.getMethod("notMixed", Object.class).invoke(instance, "value")).intValue(), "mixed non-zero/non-null is truthy");

        assertEquals(1, ((Number) clazz.getMethod("ifOnString").invoke(instance)).intValue(), "strings participate in truthiness within conditionals");
    }

    private static void arraysBehave() throws Exception {
        String source = ""
                + "string *fruits;\n"
                + "void create() { fruits = ({ \"apple\", \"banana\", \"cherry\" }); }\n"
                + "string second() { return fruits[1]; }\n"
                + "string update_and_get() { fruits[2] = \"orange\"; return fruits[2]; }\n"
                + "string *combined() { return ({ \"apple\" }) + ({ \"banana\", \"cherry\" }); }\n";

        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result = pipeline.run(null, source, "regression/ArraySample", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.ArraySample";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("create").invoke(instance);

        Object second = clazz.getMethod("second").invoke(instance);
        assertEquals("banana", second, "array indexing should read elements");

        Object updated = clazz.getMethod("update_and_get").invoke(instance);
        assertEquals("orange", updated, "array assignment should write elements");

        @SuppressWarnings("unchecked")
        java.util.List<Object> combined = (java.util.List<Object>) clazz.getMethod("combined").invoke(instance);
        assertEquals(3, combined.size(), "array concatenation should merge elements");
        assertEquals("apple", combined.get(0), "first element should be preserved");
        assertEquals("banana", combined.get(1), "second element should be preserved");
        assertEquals("cherry", combined.get(2), "third element should be preserved");
    }

    private static void mappingsBehave() throws Exception {
        String source = ""
                + "mapping metals = ([ ]);\n"
                + "void create() { metals = ([ \"gold\" : 10, \"silver\" : 20 ]); metals += ([ \"copper\" : 30 ]); }\n"
                + "int silver() { return metals[\"silver\"]; }\n"
                + "mixed nested_array() { return ([ \"coins\" : ({ 1, 2, 3 }) ])[\"coins\"][1]; }\n"
                + "mapping combine(mapping other) { return metals + other; }\n";

        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");
        CompilationResult result =
                pipeline.run(null, source, "regression/MappingSample", null, ParserOptions.defaults());

        if (!result.succeeded()) {
            throw new AssertionError("Compilation pipeline failed: " + describeProblems(result.getProblems()));
        }

        byte[] bytecode = result.getBytecode();
        String binaryName = "regression.MappingSample";

        Class<?> clazz = new ClassLoader() {
            Class<?> define() {
                return defineClass(binaryName, bytecode, 0, bytecode.length);
            }
        }.define();

        Object instance = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("create").invoke(instance);

        Object silver = clazz.getMethod("silver").invoke(instance);
        assertEquals(20, silver, "mapping index should retrieve stored value");

        Object nested = clazz.getMethod("nested_array").invoke(instance);
        assertEquals(2, nested, "mapping values should support nested arrays");

        @SuppressWarnings("unchecked")
        java.util.Map<Object, Object> combined = (java.util.Map<Object, Object>)
                clazz.getMethod("combine", java.util.Map.class)
                        .invoke(instance, java.util.Map.of("silver", 25, "iron", 40));

        assertEquals(4, combined.size(), "merged mapping should contain original and new keys");
        assertEquals(25, combined.get("silver"), "new mapping should override duplicate keys with right-hand side");
        assertEquals(10, combined.get("gold"), "original entries should be preserved when not overridden");
        assertEquals(30, combined.get("copper"), "existing entries should persist after merging");
        assertEquals(40, combined.get("iron"), "right-hand entries should be added");
    }

    private static void consoleConfigLoadsSystemIncludes() {
        try {
            Path base = Files.createTempDirectory("lpc2j-config");
            Path incA = Files.createDirectories(base.resolve("incA"));
            Path incB = Files.createDirectories(base.resolve("nested/incB"));
            Path absInc = Files.createTempDirectory("lpc2j-abs-inc");

            Path cfg = base.resolve("sample.cfg");
            Files.writeString(cfg,
                    "mudlib directory : .\n"
                            + "system include directories : incA:nested/incB:"
                            + absInc.toString());

            ConsoleConfig config = ConsoleConfig.load(cfg);
            List<Path> dirs = config.includeDirs();

            assertEquals(3, dirs.size(), "should preserve configured include count");
            assertEquals(base.normalize(), config.basePath(), "base path should resolve relative to config");
            assertEquals(incA.normalize(), dirs.get(0), "first include should preserve order");
            assertEquals(incB.normalize(), dirs.get(1), "second include should preserve order");
            assertEquals(absInc.normalize(), dirs.get(2), "absolute include should be used as-is");
        } catch (Exception e) {
            throw new AssertionError("Console config should load system include directories", e);
        }
    }

    private static void consoleRejectsMissingBasePath() {
        String missingPath = Path.of(System.getProperty("java.io.tmpdir"), "lpc2j-missing-base-" + System.nanoTime())
                .toAbsolutePath()
                .toString();
        boolean threw = false;

        try {
            Path cfgDir = Files.createTempDirectory("lpc2j-missing-base-cfg");
            Path cfg = cfgDir.resolve("cfg");
            Files.writeString(cfg, "mudlib directory : " + missingPath);
            ConsoleConfig.load(cfg);
        } catch (IllegalArgumentException e) {
            threw = true;
            assertTrue(e.getMessage().contains("Mudlib directory"), "error should mention mudlib directory");
        } catch (Exception e) {
            throw new AssertionError("Unexpected error setting up console config", e);
        }

        assertTrue(threw, "console config should reject nonexistent base path");
    }

    private static void consoleReadsHistoryWithArrows() {
        byte[] input = ("\u001b[A\n").getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConsoleLineReader reader =
                new ConsoleLineReader(new ByteArrayInputStream(input), new PrintStream(out, true, StandardCharsets.UTF_8));
        reader.recordHistory("first");
        reader.recordHistory("second");

        String line = reader.readLine("> ");

        assertEquals("second", line, "up arrow should recall the most recent command");
        String rendered = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(rendered.contains("> second"), "rendered line should show recalled command");
    }

    private static void consoleReturnsToEmptyHistorySlot() {
        byte[] input = ("\u001b[A\u001b[B\n").getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConsoleLineReader reader =
                new ConsoleLineReader(new ByteArrayInputStream(input), new PrintStream(out, true, StandardCharsets.UTF_8));
        reader.recordHistory("look");

        String line = reader.readLine("$ ");

        assertEquals("", line, "down arrow should return to a fresh entry after navigating history");
    }

    private static void consoleLoadHandlesInheritance() throws Exception {
        Path cfgPath = Path.of("lpc2j/sample/sample.cfg");
        ConsoleConfig config = ConsoleConfig.load(cfgPath);
        LPCConsole console = new LPCConsole(config);

        FSSourceFile child = console.load("inheritance/inherit_child.c");
        if (child == null || child.lpcObject() == null) {
            throw new AssertionError("console load should produce an instantiated object");
        }

        Object instance = child.lpcObject();
        Class<?> childClass = instance.getClass();

        assertEquals(
                "inheritance.inherit_parent",
                childClass.getSuperclass().getName(),
                "loaded child should extend the inherited parent");
        int self = ((Number) childClass.getMethod("call_self_shout").invoke(instance)).intValue();
        assertEquals(205, self, "child override should dispatch correctly");
        int parent = ((Number) childClass.getMethod("call_parent_shout").invoke(instance)).intValue();
        assertEquals(101, parent, "explicit parent dispatch should reach the parent implementation");
    }

    private static String sampleDisplayName(Path root, Path file) {
        Path rel = root.relativize(file);
        String stem = stripExtension(rel.getFileName().toString());
        Path display = (rel.getParent() == null) ? Path.of(stem) : rel.getParent().resolve(stem);
        return "samples/" + display.toString().replace('\\', '/');
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }

    private static String binaryName(String displayName) {
        return displayName.replace('/', '.');
    }

    private static int readIntField(Class<?> owner, Object target, String fieldName) throws Exception {
        Field f = owner.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.getInt(target);
    }

    private static boolean hasDeclaredField(Class<?> owner, String fieldName) {
        try {
            owner.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static void ensureDeclaredFieldsUnique(Class<?> owner) {
        Set<String> names = new HashSet<>();
        for (Field field : owner.getDeclaredFields()) {
            if (!names.add(field.getName()))
                throw new AssertionError("Duplicate Java field name emitted: " + field.getName());
        }
    }

    private static final class ByteArrayLoader extends ClassLoader {
        Class<?> define(String binaryName, byte[] bytecode) {
            return defineClass(binaryName, bytecode, 0, bytecode.length);
        }
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

    private static String describeProblems(List<CompilationProblem> problems) {
        StringBuilder sb = new StringBuilder();
        for (CompilationProblem problem : problems) {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append(problem.getStage())
                    .append(": ")
                    .append(problem.getMessage());
            if (problem.getLine() != null)
                sb.append(" @").append(problem.getLine());
        }
        return sb.toString();
    }

    public static final class StrengthProbe {
        private final int strength;

        StrengthProbe(int strength) {
            this.strength = strength;
        }

        @SuppressWarnings("unused")
        public int query_strength() {
            return strength;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record TestCase(String name, ThrowingRunnable runnable) {}
}
