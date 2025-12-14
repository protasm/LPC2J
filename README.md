LPC2J is an [LPC90](https://protasm.github.io/LPC90)-compatible compiler for creating Java classfiles from LPC (i.e. LPMud) source code.

## Scanner usage

When scanning source from an in-memory string, use the simplified pipeline:

```java
TokenList tokens = LPC2J.scan(sourceText);
ASTObject ast = LPC2J.parse(tokens);
byte[] bytecode = LPC2J.compile(ast);
```

