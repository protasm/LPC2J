package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class CmdCompile extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("Compile");

        if (args.length < 1) {
            System.out.println("Error: No fileName specified.");

            return true;
        }

        String vPathStr = pathStrOfArg(console.vPath(), args[0]);
        FSSourceFile sf = console.compile(vPathStr);

        if (sf == null) {
            return true;
        }

        System.out.println("Success!  Compiled to " + sf.dotName());
        emitDisassembly(console, sf);

        return true;
    }

    @Override
    public String toString() {
        return "Compile <source fileName>";
    }

    private void emitDisassembly(LPCConsole console, FSSourceFile sf) {
        if (sf.bytes() == null) {
            System.out.println("No bytecode generated; skipping javap output.");
            return;
        }

        if (!console.vfs().write(sf)) {
            System.out.println("Unable to write class file for javap.");
            return;
        }

        Path classFile = console.vfs().basePath().resolve(sf.classPath()).normalize();
        String displayClassPath = Path.of("/").resolve(sf.classPath()).normalize().toString();

        try {
            Process process =
                    new ProcessBuilder("javap", "-classpath", console.vfs().basePath().toString(), "-c", sf.dotName())
                            .redirectErrorStream(true)
                            .start();
            String output;
            try (InputStream stream = process.getInputStream()) {
                output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exit = process.waitFor();

            System.out.println("javap -c " + displayClassPath + ":");
            System.out.println(output.replace(console.vfs().basePath().toString(), "/"));
            if (exit != 0) {
                System.out.println("javap exited with code " + exit);
            }
        } catch (IOException e) {
            System.out.println("Error running javap: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("javap interrupted: " + e.getMessage());
        }
    }
}
