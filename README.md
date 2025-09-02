LPC2J is an [LPC90](https://protasm.github.io/LPC90)-compatible compiler for creating Java classfiles from LPC (i.e. LPMud) source code.

## Scanner usage

When scanning source from an in-memory string that contains relative `#include`
directives, supply a synthetic source path so that the preprocessor searches the
correct directory:

```java
Scanner scanner = new Scanner();
TokenList tokens = scanner.scan(
    sourceText,
    "/usr/local/include",         // system include path
    "/path/to/source/dir",        // quote include path
    Path.of("/path/to/source.lpc") // pretend file location
);
```

