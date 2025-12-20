/**
 * Virtual filesystem helpers for the console.
 *
 * <p>Wraps host filesystem access behind virtual paths, encapsulated in {@link
 * io.github.protasm.lpc2j.console.fs.VirtualFileServer} and {@link
 * io.github.protasm.lpc2j.console.fs.FSSourceFile}. Provides safe path resolution, file listing, and
 * reading/writing compiled artifacts relative to a configured base directory.</p>
 *
 * <p>Assumes callers validate user input paths at the console layer; this package prevents path
 * traversal outside the base directory but does not handle concurrency or locking.</p>
 */
package io.github.protasm.lpc2j.console.fs;
