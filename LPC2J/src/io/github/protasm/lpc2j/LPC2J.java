package io.github.protasm.lpc2j;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_ASSIGNMENT;
import static io.github.protasm.lpc2j.scanner.TokenType.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Token;

public class LPC2J {
  private static final Map<String, J_Type> javaTypeForLPCType;

  static {
    javaTypeForLPCType = new HashMap<>();

    javaTypeForLPCType.put("int",    J_Type.INT);
    javaTypeForLPCType.put("float",  J_Type.FLOAT);
    javaTypeForLPCType.put("status", J_Type.BOOLEAN);
    javaTypeForLPCType.put("string", J_Type.STRING);
    javaTypeForLPCType.put("void",   J_Type.VOID);
  }

  private Parser parser;
  private C_MethodBuilder builder;
  private ClassWriter cw;

  //parser()
  public Parser parser() {
    return this.parser;
  }

  //compile(String, String, String)
  public byte[] compile(String sourcePath, String sysInclude, String quoteInclude) throws IOException {
    Path path = Paths.get(sourcePath);
    String fileName = path.getFileName().toString();
    String filePrefix = fileName.substring(0, fileName.lastIndexOf(".lpc"));
    String source = Files.readString(path);

    parser = new Parser(this, source, sysInclude, quoteInclude);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

    //advance to the first non-error Token (or EOF)
    parser.advance();

    //loop inherit declarations until exhausted
    //while(parser.match(TOKEN_INHERIT))
      //inherit();

    //loop member declarations until EOF
    while (!parser.match(TOKEN_EOF))
      member();

    cw.visitEnd();

    return cw.toByteArray();
  } //compile(String, String, String)

/*
  //inherit()
  private void inherit() {
    parser.consume(TOKEN_STRING, "Expect inherited object name.");

    int index = emitConstant(parser.previous().literal());

    parser.consume(TOKEN_SEMICOLON, "Expect semicolon after inherited object name.");

//    if (identifiersEqual(classToken, parser.previous()))
//      error("A class can't inherit from itself.");

//    if (currScope.locals().size() >= Props.instance().getInt("MAX_SIGNED_BYTE")) {
//      parser.error("Too many local variables in function.");
//
//      return;
//    }

    //Record existence of local variable.
    //Token token = syntheticToken("super"));
//    currScope.locals().push(new Local(token, -1));

//    defineVariable(0x00);

//    namedVariable(classToken, false);
      
    //emitCode(OP_COMPILE);
    //emitCode(index);

    //emitCode(OP_INHERIT);

//    currentClass.setHasSuperclass(true);
  } //inherit()
*/

  //member()
  private void member() {
    Token typeToken = parseType("Expect member type.");
    Token nameToken = parseVariable("Expect member name.");

    if (!parser.check(TOKEN_LEFT_PAREN)) //field
      fieldDeclaration(typeToken, nameToken);
    else //method
      methodDeclaration(typeToken, nameToken);

    if (parser.panicMode())
      parser.synchronize();
  } //member()

  //declaration()
  private void declaration() {
    if (parser.check(TOKEN_TYPE)) { //variable
      Token typeToken = parser.previous();
      Token nameToken = parseVariable("Expect variable name.");

      varDeclaration(typeToken, nameToken);
    } else
      statement();

    if (parser.panicMode())
      parser.synchronize();
  } //declaration()

  //parseType(String)
  private Token parseType(String errorMessage) {
    //parser.match(TOKEN_STAR); //temp
    parser.consume(TOKEN_TYPE, errorMessage);

    return parser.previous();
  } //parseType(String)

  //parseVariable(String)
  private Token parseVariable(String errorMessage) {
    parser.consume(TOKEN_IDENTIFIER, errorMessage);

    Token token = parser.previous();

    if (
      builder != null &&
      builder.scopeDepth() > 0
    )
      declareLocalVar(token);

    return token;
  } //parseVariable(String)

  //fieldDeclaration(Token, Token)
  private void fieldDeclaration(Token typeToken, Token nameToken) {
    J_Type type = LPC2J.javaTypeForLPCType.get(typeToken.lexeme());
    String name = nameToken.lexeme();
    J_Field field = new J_Field(this.classFile, name, type);

    this.classFile.add(field);

    if (parser.match(TOKEN_EQUAL)) {
      //compile field initialization code into this.<init>
      this.builder = new C_MethodBuilder(this.classFile.initMethod());

      emitLoadThis();

      expression(); //leaves expression value on stack

      char idx = this.classFile.getRef(field);

      emitPutField(idx);
    }

    //handle field declarations of the form:
    //int x = 99, y, z = 1;
    if (parser.match(TOKEN_COMMA)) {
      nameToken = parseVariable("Expect field name.");
      
      fieldDeclaration(typeToken, nameToken);

      return;
    }

    parser.consume(TOKEN_SEMICOLON, "Expect ';' after field declaration(s).");
  } //fieldDeclaration(Token, Token)

  //varDeclaration(Token, Token)
  private void varDeclaration(Token typeToken, Token nameToken) {
    J_Type type = LPC2J.javaTypeForLPCType.get(typeToken.lexeme());
    String name = nameToken.lexeme();

    //this.classFile.cp().addField(name, type);

    if (parser.match(TOKEN_EQUAL)) {
      expression(); //leaves expression value on stack

      //this.classVar.initField(name);
    }

    //handle variable declarations of the form:
    //int x = 99, y, z = 1;
    if (parser.match(TOKEN_COMMA)) {
      nameToken = parseVariable("Expect variable name.");
      
      varDeclaration(typeToken, nameToken);

      return;
    }

    parser.consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration(s).");
  } //varDeclaration(Token, Token)

  //declareLocalVar(Token)
  private void declareLocalVar(Token token) {
    //A local variable is "declared" when it is added to the
    //current scope.
    if (builder.hasLocal(token))
      parser.error("Already a variable with this name in this scope.");

    //Record existence of local variable, with sentinel (-1)
    //scopeDepth to mark it uninitialized
    C_Local local = new C_Local(token, -1);

    builder.addLocal(local);
  } //declareLocalVar(Token)

  //defineVariable()
  private void defineVariable() {
    if (builder.scopeDepth() > 0) //local var
    //A local variable is "defined" and becomes available
    //for use when it is marked initialized.
      builder.markTopLocalInitialized(builder.scopeDepth());

      //No instructions needed to "create a local
      //variable" at runtime; it's just the value
      //on top of the stack.
    //} else if (currScope.compilation().type() == TYPE_SCRIPT) { //global var
      //emitCode(OP_DEF_GLOBAL);
      //emitCode(index);
    //}
  } //defineVariable()

  //expression()
  public void expression() {
    parser.parsePrecedence(PREC_ASSIGNMENT);
  } //expression()

  //methodDeclaration(Token, Token)
  private void methodDeclaration(Token typeToken, Token nameToken) {
    J_Type type = LPC2J.javaTypeForLPCType.get(typeToken.lexeme());
    String name = nameToken.lexeme();

    J_Method method = new J_Method(this.classFile, name, type);
    C_MethodBuilder builder = new C_MethodBuilder(method);
    this.builder = builder;

    //Method declaration's variable is marked "initialized"
    //before compiling the body so that the name can be
    //referenced inside the body without generating an error;
    //e.g., for nested, looping methods
    this.builder.markTopLocalInitialized(0);

    beginScope();

    parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

    if (!parser.check(TOKEN_RIGHT_PAREN))
      do { //multiple parameters
        Token paramTypeToken = parseType("Expect parameter type.");
        parseVariable("Expect parameter name."); //discarded

        J_Type paramType = LPC2J.javaTypeForLPCType.get(paramTypeToken.lexeme());

        method.addParamType(paramType);

        defineVariable(); //mark parameter initialized
      } while (parser.match(TOKEN_COMMA));

    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method parameters.");
    parser.consume(TOKEN_LEFT_BRACE, "Expect '{' before method body.");

    block(); //consumes right brace

    //No call to endScope() needed here; builder will be discarded

    //add an implicit return to every method
    emitReturn(J_Type.VOID);

    this.classFile.add(method);
    this.builder = null;
  } //methodDeclaration(Token, Token)

  //namedVariable(Token, boolean)
  public void namedVariable(Token token, boolean canAssign) {
	if (resolveLocal(token.lexeme()) != -1) { //local
      namedLocal(token, canAssign);
    } else if (this.classFile.hasField(token.lexeme())) { //field
      namedField(token.lexeme(), canAssign);
    } else { //global
//      namedGlobal(token, canAssign);
    }
  } //namedVariable(Token, boolean)

  //resolveLocal(String)
  private int resolveLocal(String name) {
	if (this.builder == null) return -1;

    //traverse locals backward, looking for a match
    for (int i = builder.locals().size() - 1; i >= 0; i--) {
      C_Local local = builder.locals().get(i);

      if (name.equals(local.token().lexeme())) {  //found match
        if (local.scopeDepth() == -1) //"sentinel" depth
          parser.error("Can't read local variable in its own initializer.");

        return i; //runtime stack position of matching local
      }
    }

    //No match; not a local.
    return -1;
  } //resolveLocal(String)

  //namedLocal(Token, boolean)
  private void namedLocal(Token token, boolean canAssign) {
    //TO DO
  } //namedLocal(Token, boolean)

  //namedField(String, boolean)
  private void namedField(String name, boolean canAssign) {
    if (canAssign && parser.match(TOKEN_EQUAL)) { //assignment
      emitLoadThis();

      expression(); //leaves expression value on stack

      J_Field field = this.classFile.getField(name);
      char idx = this.classFile.getRef(field);

      emitPutField(idx);
    }

	/*
	    else if (canAssign && parser.match(TOKEN_MINUS_EQUAL))
	      compoundAssignment(getOp, setOp, OP_SUBTRACT, index);
	    else if (canAssign && parser.match(TOKEN_PLUS_EQUAL))
	      compoundAssignment(getOp, setOp, OP_ADD, index);
	    else if (canAssign && parser.match(TOKEN_SLASH_EQUAL))
	      compoundAssignment(getOp, setOp, OP_DIVIDE, index);
	    else if (canAssign && parser.match(TOKEN_STAR_EQUAL))
	      compoundAssignment(getOp, setOp, OP_MULTIPLY, index);
	*/

    else { //retrieval
      emitLoadThis();
      
      J_Field field = this.classFile.getField(name);
      char idx = this.classFile.getRef(field);
      
      emitGetField(idx);
    }
  } //namedField(String, boolean)

/*
  ///argumentList()
  public int argumentList() {
    int argCount = 0;

    if (!parser.check(TOKEN_RIGHT_PAREN))
      do {
        expression();

        argCount++;
      } while (parser.match(TOKEN_COMMA));

    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after arguments.");

    return argCount;
  }
*/

  //block()
  private void block() {
    while (!parser.check(TOKEN_RIGHT_BRACE) && !parser.check(TOKEN_EOF))
      declaration();

    parser.consume(TOKEN_RIGHT_BRACE, "Expect '}' after block.");
  } //block()

/*
  //compoundAssignment(byte, byte, byte, int index)
  private void compoundAssignment(byte getOp, byte setOp, byte assignOp, int index) {
    //emitCode(getOp);
    //emitCode(index);

    expression();

    //emitCode(assignOp);

    //emitCode(setOp);
    //emitCode(index);
  }
*/

  ///////////////////////////////////////////
  // Emitters
  ///////////////////////////////////////////
  //emitOpCode(byte)
  private void emitOpCode(byte opCode) {
    this.builder.method().addOpCode(opCode);
  } //emitOpCode(byte)
  
  //emitInstruction(J_Instruction)
  private void emitInstruction(J_Instruction instr) {
    this.builder.method().addInstruction(instr);
  } //emitInstruction(J_Instruction)
  
  //emitLDC_W(char)
  private void emitLDC_W(char idx) {
    emitInstruction(
      new J_Instruction(
        J_OpCode.OP_LDC_W,
        idx
      )
    );
  } //emitLDC_W(char)
  
  //emitPutField(char)
  private void emitPutField(char idx) {
    emitInstruction(
      new J_Instruction(
        J_OpCode.OP_PUTFIELD,
        idx
      )
    );
  } //emitPutField(char)
  
  //emitGetField(char)
  private void emitGetField(char idx) {
    emitInstruction(
      new J_Instruction(
        J_OpCode.OP_GETFIELD,
        idx
      )
    );
  } //emitGetField(char)
  
  //emitLoadThis()
  private void emitLoadThis() {
    emitOpCode(J_OpCode.OP_ALOAD_0);
  } //emitLoadThis()
  
  //emitPop()
  private void emitPop() {
    emitOpCode(J_OpCode.OP_POP);
  } //emitPop()
  
  //emitReturn(J_Type)
  private void emitReturn(J_Type type) {
	switch(type) {
	  case VOID:
        emitOpCode(J_OpCode.OP_RETURN);
        break;
	  case STRING:
	    emitOpCode(J_OpCode.OP_ARETURN);
	    break;
	  case INT:
	    emitOpCode(J_OpCode.OP_IRETURN);
	    break;
	  case FLOAT:
	    emitOpCode(J_OpCode.OP_FRETURN);
	    break;
      default:
        break;
	} //switch
  } //emitReturn(J_Type)

/*
  //emitCode(byte code)
  public void emitCode(byte code) {
    if (parser.previous() == null) //may be null for "synthetic" operations
      currInstrList().addCode(code);
    else
      currInstrList().addCode(code, parser.previous().line());
  }
*/

/*
  //emitConstant(Object)
  public int emitConstant(Object constant) {
    return currInstrList().add(constant);
  }
*/

/*
  //emitJump(byte)
  public int emitJump(byte code) {
    emitCode(code);
    emitCode(0xFF); //placeholder, later backpatched

    return currInstrList().codes().size() - 1;
  }
*/

/*
  //emitLoop(int)
  private void emitLoop(int loopStart) {
    int offset = currInstrList().codes().size() - loopStart + 2;

    emitCode(OP_LOOP);
    emitCode(offset);
  }
*/

  //beginScope()
  private void beginScope() {
    builder.incScopeDepth();
  } //beginScope()

  //endScope()
  private void endScope() {
    builder.decScopeDepth();

    //pop all locals belonging to the expiring scope
    while (
      !(this.builder.locals().isEmpty()) &&
      this.builder.locals().peek().scopeDepth() > this.builder.scopeDepth()
    ) {
      emitPop();

      this.builder.locals().pop();
    } //while
  } //endScope()

  ///////////////////////////////////////////
  // Parser Callbacks
  ///////////////////////////////////////////

  //literal(Token)
  public void literal(Token token) {
    switch (token.type()) {
      case TOKEN_NIL:
        emitOpCode(J_OpCode.OP_ACONST_NULL);
        break;
      case TOKEN_FALSE:
        emitOpCode(J_OpCode.OP_ICONST_0);
        break;
      case TOKEN_TRUE:
    	emitOpCode(J_OpCode.OP_ICONST_1);
        break;
      default: //Unreachable
        break;
    } //switch
  } //literal(Token)

  //lpcFloat(Float)
  public void lpcFloat(Float value) {
    J_Constant constant = J_Constant.newFloat(value);
    char idx = this.classFile.cp().add(constant);

    emitLDC_W(idx);
  } //lpcFloat(Float)

  //lpcInteger(Integer)
  public void lpcInteger(Integer value) {
    J_Constant constant = J_Constant.newInteger(value);
    char idx = this.classFile.cp().add(constant);

    emitLDC_W(idx);
  } //lpcInteger(Integer)

  //invalidNumber(Object)
  public void invalidNumber(Object value) {
    parser.error("Invalid number value.");
  } //invalidNumber(Object)

  //lpcString(String)
  public void lpcString(String value) {
    J_Constant constant = J_Constant.newUtf8(value);
    char idx = this.classFile.cp().add(constant);

    constant = J_Constant.newString(idx);
    idx = this.classFile.cp().add(constant);

    emitLDC_W(idx);
  } //lpcString(String)

  //expressionStatement()
  private void expressionStatement() {
    expression();

    parser.consume(TOKEN_SEMICOLON, "Expect ';' after expression.");

    //this.emitPop(); //needed?
  } //expressionStatement()

/*
  //forStatement()
  private void forStatement() {
    parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after 'for'.");

    //Initializer clause.
    if (parser.match(TOKEN_SEMICOLON)) {
      // No initializer.
    } else if (parser.match(TOKEN_TYPE)) {
      int index = parseVariable("Expect variable name.");

      varDeclaration(index);
    } else
      expressionStatement();

    int loopStart = currInstrList().codes().size();

     //Condition clause.
    int exitJump = -1;

    if (!parser.match(TOKEN_SEMICOLON)) {
      expression();

      parser.consume(TOKEN_SEMICOLON, "Expect ';' after loop condition.");

      // Jump out of the loop if the condition is false.
      //exitJump = emitJump(OP_JUMP_IF_FALSE);

      //emitCode(OP_POP); // Condition.
    }

    //Increment clause.
    if (!parser.match(TOKEN_RIGHT_PAREN)) {
      int bodyJump = emitJump(OP_JUMP);
      int incrementStart = currInstrList().codes().size();

      expression();

      //emitCode(OP_POP);

      parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after for clauses.");

      //emitLoop(loopStart);

      loopStart = incrementStart;

      patchJump(bodyJump);
    }

    statement();

    //emitLoop(loopStart);

    if (exitJump != -1) {
      patchJump(exitJump);

      //emitCode(OP_POP); // Condition.
    }

    endScope();
  }
*/

/*
  //array()
  public int array() {
    int elementCount = 0;

    if (!parser.check(TOKEN_RIGHT_BRACE))
      do {
        expression();
      
        elementCount++;
      } while (parser.match(TOKEN_COMMA));

    parser.consume(TOKEN_RIGHT_BRACE, "Expect '}' after array elements.");
    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after array.");

    return elementCount;
  }
*/

/*
  //mapping()
  public int mapping() {
    int elementCount = 0;

    if (!parser.check(TOKEN_RIGHT_BRACKET))
      do {
        expression();

        parser.consume(TOKEN_COLON, "Expect ':' after mapping key.");

        expression();

        elementCount++;
      } while (parser.match(TOKEN_COMMA));

    parser.consume(TOKEN_RIGHT_BRACKET, "Expect ']' after mapping entries.");
    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after mapping.");

    return elementCount;
  }
*/

/*
  //ifStatement()
  private void ifStatement() {
    parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after 'if'.");

    expression();

    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition.");

    int thenJump = emitJump(OP_JUMP_IF_FALSE);

    //emitCode(OP_POP);

    statement();

    //int elseJump = emitJump(OP_JUMP);

    patchJump(thenJump);

    //emitCode(OP_POP);

    if (parser.match(TOKEN_ELSE)) statement();

    patchJump(elseJump);
  }
*/

/*
  //patchJump(int)
  public void patchJump(int offset) {
    // -1 to adjust for the jump offset itself.
    int jump = currInstrList().codes().size() - offset - 1;

    currInstrList().codes().set(offset, (byte)jump);
  }
*/

  //returnStatement()
  private void returnStatement() {
    if (this.builder == null)
      parser.error("Can't return from top-level code.");

    if (parser.match(TOKEN_SEMICOLON)) //no return value provided
      if (this.builder.method().type() != J_Type.VOID)
        parser.error("Missing return value.");
      else
        emitReturn(J_Type.VOID);
    else { //handle return value
      expression();

      parser.consume(TOKEN_SEMICOLON, "Expect ';' after return value.");

      emitReturn(this.builder.method().type());
    } //if-else
  } //returnStatement()

  //statement()
  private void statement() {
//    if (parser.match(TOKEN_FOR))
//      forStatement();
//    else if (parser.match(TOKEN_IF))
//      ifStatement();
    if (parser.match(TOKEN_RETURN))
      returnStatement();
//    else if (parser.match(TOKEN_WHILE))
//      whileStatement();
    else if (parser.match(TOKEN_LEFT_BRACE)) {
      beginScope();
      
      block();

      endScope();
    } else
      expressionStatement();
  } //statement()

/*
  //syntheticToken(String)
  public Token syntheticToken(String text) {
    return new Token(text);
  }
*/

/*
  //whileStatement()
  private void whileStatement() {
    int loopStart = currInstrList().codes().size();

    parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after 'while'.");

    expression();

    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition.");

    int exitJump = emitJump(OP_JUMP_IF_FALSE);

    emitCode(OP_POP);

    statement();

    emitLoop(loopStart);

    patchJump(exitJump);

    emitCode(OP_POP);
  }
*/

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: LPC2J <path>");

      return;
    }

    LPC2J compiler = new LPC2J();

    byte[] bytes = compiler.compile(args[0], ".", ".");

    System.out.println(bytes);
  } //main(String[])
} //LPC2J