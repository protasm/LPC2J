/**
 * Entry point utilities that tie together scanning, parsing, and compilation for LPC source.
 *
 * <p>The {@link io.github.protasm.lpc2j.LPC2J} facade offers simple static helpers for running the
 * compiler pipeline against strings, handling diagnostic printing, and applying a default Java
 * superclass when emitting bytecode.</p>
 *
 * <p>Assumes downstream components ({@code scanner}, {@code parser}, {@code compiler}) enforce their
 * own validation rules; this layer simply orchestrates them and returns {@code null} on failure.</p>
 *
 * <p>Not responsible for command-line UX or file management; the console packages wrap these helpers
 * for interactive use.</p>
 */
package io.github.protasm.lpc2j;
