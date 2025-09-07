package io.github.protasm.lpc2j.sourcepos;

/** Fast line lookup by maintaining offsets of line starts. */
public final class LineMap {
	private final String fileName;
	private final CharSequence source;
	private final int[] lineStarts; // lineStarts[i] = offset of line (i+1)

	public LineMap(String sourceFileName, CharSequence source) {
		if ((sourceFileName == null) || (source == null))
			throw new IllegalArgumentException();

		this.fileName = sourceFileName;
		this.source = source;

		this.lineStarts = buildLineStarts(source);
	}

	private static int[] buildLineStarts(CharSequence s) {
		int n = 1; // at least one line

		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\n')
				n++;

		int[] starts = new int[n];
		int li = 0;

		starts[li++] = 0;

		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\n')
				starts[li++] = i + 1;

		return starts;
	}

	public int sourceLength() {
		return source.length();
	}

	public char charAt(int i) {
		return ((i >= 0) && (i < source.length())) ? source.charAt(i) : '\0';
	}

	public SourcePos posAt(int offset) {
		if (offset < 0) offset = 0;

		if (offset > source.length()) offset = source.length();

		int lo = 0, hi = lineStarts.length - 1;

		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			int start = lineStarts[mid];

			if (start <= offset) {
				if ((mid == (lineStarts.length - 1)) || (lineStarts[mid + 1] > offset)) {
					int line = mid + 1;
					int col = (offset - start) + 1;

					return new SourcePos(fileName, line, col);
				}

				lo = mid + 1;
			} else
				hi = mid - 1;
		}

		return new SourcePos(fileName, 1, 1); // fallback
	}
}
