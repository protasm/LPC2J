package io.github.protasm.lpc2j.sourcepos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mapping implementation that tracks contiguous segments of generated text back to original {@link
 * LineMap}s.
 */
public final class SegmentedSourceMapper implements SourceMapper {
    private final List<Segment> segments;
    private final int generatedLength;

    private SegmentedSourceMapper(List<Segment> segments, int generatedLength) {
        this.segments = segments;
        this.generatedLength = generatedLength;
    }

    @Override
    public SourcePos originalPos(int generatedOffset) {
        int offset = clamp(generatedOffset);
        Segment seg = segmentFor(offset);

        if (seg == null)
            return new SourcePos("<generated>", 1, 1, offset);

        return seg.posAt(offset);
    }

    @Override
    public SourceSpan originalSpan(int generatedStart, int generatedEnd) {
        int start = clamp(generatedStart);
        int end = clamp(generatedEnd);

        if (end < start)
            end = start;

        Segment startSeg = segmentFor(start);
        Segment endSeg = segmentFor(Math.max(start, end - 1));

        if ((startSeg == null) || (endSeg == null)) {
            SourcePos startPos = new SourcePos("<generated>", 1, 1, start);

            return new SourceSpan(startPos, startPos);
        }

        if (startSeg == endSeg) {
            return startSeg.spanFor(start, end);
        }

        if (startSeg.canBridgeTo(endSeg, start, end)) {
            return startSeg.bridgeTo(endSeg, start, end);
        }

        SourcePos startPos = startSeg.posAt(start);
        SourcePos endPos = endSeg.posAt(end);

        if (startPos.fileName().equals(endPos.fileName()))
            return new SourceSpan(startPos, endPos);

        return new SourceSpan(startPos, startPos);
    }

    private int clamp(int offset) {
        if (offset < 0)
            return 0;

        if (offset > generatedLength)
            return generatedLength;

        return offset;
    }

    private Segment segmentFor(int generatedOffset) {
        int lo = 0;
        int hi = segments.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            Segment s = segments.get(mid);

            if (generatedOffset < s.generatedStart)
                hi = mid - 1;
            else if (generatedOffset >= s.generatedEnd)
                lo = mid + 1;
            else
                return s;
        }

        return null;
    }

    public static final class Builder {
        private final List<Segment> segments = new ArrayList<>();
        private int generatedCursor = 0;

        public void append(String text, LineMap map, int originalStartOffset, int originalEndOffset) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(map, "map");

            if (text.isEmpty())
                return;

            int originalLength = Math.max(0, originalEndOffset - originalStartOffset);
            Segment next = new Segment(generatedCursor, text.length(), map, originalStartOffset, originalLength);

            maybeMerge(next);

            generatedCursor += text.length();
        }

        private void maybeMerge(Segment next) {
            if (segments.isEmpty()) {
                segments.add(next);
                return;
            }

            Segment last = segments.get(segments.size() - 1);

            if (last.canMerge(next)) {
                last.extend(next);
                return;
            }

            segments.add(next);
        }

        public SourceMapper build() {
            return new SegmentedSourceMapper(List.copyOf(segments), generatedCursor);
        }
    }

    private static final class Segment {
        private final int generatedStart;
        private int generatedEnd;
        private final LineMap map;
        private final int originalStartOffset;
        private final int originalLength;

        Segment(int generatedStart, int generatedLength, LineMap map, int originalStartOffset, int originalLength) {
            this.generatedStart = generatedStart;
            this.generatedEnd = generatedStart + generatedLength;
            this.map = map;
            this.originalStartOffset = originalStartOffset;
            this.originalLength = originalLength;
        }

        boolean canMerge(Segment other) {
            if (originalLength == 0 || other.originalLength == 0)
                return false;

            return (map == other.map)
                    && (generatedEnd == other.generatedStart)
                    && ((originalStartOffset + originalLength) == other.originalStartOffset);
        }

        void extend(Segment other) {
            this.generatedEnd = other.generatedEnd;
        }

        boolean canBridgeTo(Segment other, int start, int end) {
            return (map == other.map)
                    && contains(start)
                    && other.contains(Math.max(start, end - 1));
        }

        boolean contains(int generatedOffset) {
            return (generatedOffset >= generatedStart) && (generatedOffset < generatedEnd);
        }

        SourcePos posAt(int generatedOffset) {
            int delta = Math.max(0, Math.min(generatedOffset - generatedStart, mappedOriginalLength()));
            int originalOffset = originalStartOffset + delta;

            return map.posAt(originalOffset);
        }

        SourceSpan spanFor(int generatedStartOffset, int generatedEndOffset) {
            SourcePos start = posAt(generatedStartOffset);
            SourcePos end = posAt(generatedEndOffset);

            return new SourceSpan(start, end);
        }

        SourceSpan bridgeTo(Segment other, int generatedStartOffset, int generatedEndOffset) {
            SourcePos start = posAt(generatedStartOffset);
            SourcePos end = other.posAt(generatedEndOffset);

            return new SourceSpan(start, end);
        }

        private int mappedOriginalLength() {
            if (originalLength == 0)
                return 0;

            return Math.min(originalLength, generatedLength());
        }

        private int generatedLength() {
            return generatedEnd - generatedStart;
        }
    }
}
