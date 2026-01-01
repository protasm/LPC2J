/**
 * Lexical analysis for LPC source text.
 *
 * <p>Converts raw or preprocessed source strings into {@link io.github.protasm.lpc2j.token.Token}
 * streams while preserving {@link io.github.protasm.lpc2j.sourcepos.SourcePos} information via
 * {@link io.github.protasm.lpc2j.scanner.ScannableSource}.</p>
 *
 * <p>Responsible for coordinating preprocessing, recognizing reserved words and LPC types, and
 * rejecting malformed input with {@link io.github.protasm.lpc2j.scanner.ScanException}.</p>
 *
 * <p>Assumes higher layers will interpret token sequences grammatically; it does not perform parsing
 * or semantic validation beyond token boundaries.</p>
 */
package io.github.protasm.lpc2j.scanner;
