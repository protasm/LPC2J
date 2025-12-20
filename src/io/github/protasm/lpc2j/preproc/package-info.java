/**
 * Minimal LPC preprocessor.
 *
 * <p>Handles line splicing, macro expansion, conditional directives, and include resolution while
 * preserving mappings back to original files via {@link io.github.protasm.lpc2j.sourcepos.LineMap}
 * and {@link io.github.protasm.lpc2j.sourcepos.SourceSpan} data.</p>
 *
 * <p>Exposes pluggable {@link io.github.protasm.lpc2j.preproc.IncludeResolver} strategies so callers
 * control filesystem access. Assumes callers manage include search paths and guard against untrusted
 * input when providing resolvers.</p>
 *
 * <p>Not responsible for tokenization or parsing; produces expanded source strings and positional
 * mappings for the scanner.</p>
 */
package io.github.protasm.lpc2j.preproc;
