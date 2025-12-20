/**
 * Interactive console for experimenting with the LPC compiler.
 *
 * <p>Provides the {@link io.github.protasm.lpc2j.console.LPCConsole} REPL, configuration loading, and
 * plumbing to compile/load LPC sources from a {@link io.github.protasm.lpc2j.console.fs.VirtualFileServer}.</p>
 *
 * <p>Coordinates command registration, efun setup for the session, and include resolution. Assumes the
 * caller supplies a base filesystem path and interacts via standard input/output.</p>
 *
 * <p>Console concerns are limited to developer tooling; it is not a general runtime environment for
 * deployed LPC programs.</p>
 */
package io.github.protasm.lpc2j.console;
