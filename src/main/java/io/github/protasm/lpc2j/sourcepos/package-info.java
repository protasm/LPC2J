/**
 * Source position tracking utilities.
 *
 * <p>Defines {@link io.github.protasm.lpc2j.sourcepos.LineMap}, {@link
 * io.github.protasm.lpc2j.sourcepos.SourcePos}, and {@link io.github.protasm.lpc2j.sourcepos.SourceSpan}
 * to map offsets to human-friendly file/line/column data, even after preprocessing or generated text
 * insertion.</p>
 *
 * <p>{@link io.github.protasm.lpc2j.sourcepos.SegmentedSourceMapper} combines multiple {@link LineMap}
 * instances so diagnostics can refer back to original sources. Callers are expected to supply spans
 * consistently within a single logical file.</p>
 *
 * <p>This package is deliberately agnostic to language semantics; it only provides coordinate systems
 * for other phases.</p>
 */
package io.github.protasm.lpc2j.sourcepos;
