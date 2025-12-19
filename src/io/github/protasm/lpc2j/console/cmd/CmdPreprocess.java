package io.github.protasm.lpc2j.console.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.PreprocessException;
import io.github.protasm.lpc2j.preproc.Preprocessor;

public class CmdPreprocess extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        if (args.length == 0) {
            System.out.println("Usage: preprocess <source fileName>");

            return true;
        }

        VirtualFileServer vfs = console.vfs();
        Path root = vfs.basePath();

        try {
            String vPathStr = pathStrOfArg(console.vPath(), args[0]);
            Path vPath = vfs.fileAt(vPathStr);

            if (vPath == null) {
                System.out.println("Invalid fileName: " + args[0]);

                return true;
            }

            Path sourcePath = root.resolve(vPath).normalize();
            String source = vfs.contentsOfFileAt(vPath.toString());

            if (source == null) {
                System.out.println("Unable to read fileName: " + args[0]);

                return true;
            }

            IncludeResolver resolver = includeResolver(root);
            Preprocessor pp = new Preprocessor(resolver);
            String processed = pp.preprocess(sourcePath, source);

            System.out.print(processed);
        } catch (InvalidPathException e) {
            System.out.println("Error preprocessing fileName: " + args[0]);
            System.out.println(e.toString());
        } catch (PreprocessException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    private IncludeResolver includeResolver(Path root) {
        Path normalizedRoot = root.normalize();

        return (includingFile, includePath, system) -> {
            if (!system && (includingFile != null)) {
                Path parent = includingFile.getParent();

                if (parent != null) {
                    Path candidate = parent.resolve(includePath).normalize();

                    if (candidate.startsWith(normalizedRoot) && Files.isRegularFile(candidate))
                        return Files.readString(candidate);
                }
            }

            Path fallback = normalizedRoot.resolve(includePath).normalize();

            if (fallback.startsWith(normalizedRoot) && Files.isRegularFile(fallback))
                return Files.readString(fallback);

            throw new IOException("cannot include '" + includePath + "'");
        };
    }

    @Override
    public String toString() {
        return "Preprocess <source fileName>";
    }
}
