/**
 * Command implementations for the LPC console REPL.
 *
 * <p>Each {@link io.github.protasm.lpc2j.console.cmd.Command} handles a single user command (scan,
 * parse, compile, file navigation, etc.), using the shared {@link io.github.protasm.lpc2j.console.LPCConsole}
 * context for state. Commands are small, stateful objects registered with aliases by the console.</p>
 *
 * <p>Assumes inputs come from trusted interactive users; error handling favors user feedback over strict
 * exceptions. This package is not intended for headless automation.</p>
 */
package io.github.protasm.lpc2j.console.cmd;
