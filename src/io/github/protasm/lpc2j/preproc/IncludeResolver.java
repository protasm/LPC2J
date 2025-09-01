package io.github.protasm.lpc2j.preproc;

import java.io.IOException;
import java.nio.file.Path;

public interface IncludeResolver {
	/**
	 * Resolve an include path to source text.
	 *
	 * @param includingFile absolute or virtual path of the including file (may be
	 *                      null for roots)
	 * @param includePath   the raw string inside #include "..." or <...>
	 * @param system        true for <...>, false for "..."
	 * @return the loaded source text
	 * @throws IOException if not found or unreadable
	 */
	String resolve(Path includingFile, String includePath, boolean system) throws IOException;
}
