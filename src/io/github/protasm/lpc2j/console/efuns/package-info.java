/**
 * Console-provided efun implementations for interactive sessions.
 *
 * <p>Supplies placeholder or minimal implementations of common LPC efuns used when running compiled
 * objects inside the REPL. Each class typically exposes a singleton instance and registers itself
 * with the console {@link io.github.protasm.lpc2j.runtime.RuntimeContext} during startup.</p>
 *
 * <p>These efuns are geared toward development workflows (printing, environment inspection) rather
 * than full game-engine fidelity. They should not be treated as a comprehensive LPC runtime.</p>
 */
package io.github.protasm.lpc2j.console.efuns;
