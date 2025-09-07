package io.github.protasm.lpc2j.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FSBasePathTest {
    @TempDir
    Path tmp; // absolute temp dir

    Path base;
    VirtualFileServer fs;

    @BeforeEach
    void setup() throws Exception {
        base = tmp.resolve("base");
        Files.createDirectories(base);

        // structure:
        // base/
        //   a.txt
        //   dir1/
        //     b.txt
        //   empty/
        Files.writeString(base.resolve("a.txt"), "A");
        Files.createDirectories(base.resolve("dir1"));
        Files.writeString(base.resolve("dir1").resolve("b.txt"), "B");
        Files.createDirectories(base.resolve("empty"));

        fs = new VirtualFileServer(base.toString());
    }

    @Test
    void constructor_requires_absolute() {
        assertThrows(IllegalArgumentException.class, () -> new VirtualFileServer("relative/path"));
        assertEquals(base.normalize(), fs.basePath());
    }

    @Test
    void dirAt_blank_returns_basePath_object() {
        // Current implementation returns the absolute base Path on blank (not "/")
        assertEquals(fs.basePath(), fs.dirAt(" "));
    }

    @Test
    void dirAt_existing_subdir_returns_relpath() {
        Path rel = fs.dirAt("dir1");
        assertNotNull(rel);
        assertEquals(Path.of("dir1"), rel);
    }

    @Test
    void dirAt_nonexistent_or_escape_returns_null() {
        assertNull(fs.dirAt("nope"));
        assertNull(fs.dirAt("../outside"));
        assertNull(fs.dirAt(null));
    }

    @Test
    void fileAt_existing_file_returns_relpath() {
        Path rel1 = fs.fileAt("a.txt");
        assertNotNull(rel1);
        assertEquals(Path.of("a.txt"), rel1);

        Path rel2 = fs.fileAt("dir1/b.txt");
        assertNotNull(rel2);
        assertEquals(Path.of("dir1", "b.txt"), rel2);
    }

    @Test
    void fileAt_directory_or_bad_returns_null() {
        assertNull(fs.fileAt("dir1"));     // dir, not fileName
        assertNull(fs.fileAt("nope.txt")); // missing
        assertNull(fs.fileAt(""));         // blank
        assertNull(fs.fileAt("../hack.txt")); // escape
    }

    @Test
    void filesIn_lists_dir_contents() {
        // blank → base directory
        File[] baseFiles = fs.filesIn("");
        assertNotNull(baseFiles);
        var names = java.util.Arrays.stream(baseFiles).map(File::getName).toList();
        assertTrue(names.contains("a.txt"));
        assertTrue(names.contains("dir1"));
        assertTrue(names.contains("empty"));

        // specific subdir
        File[] dir1Files = fs.filesIn("dir1");
        assertNotNull(dir1Files);
        var names2 = java.util.Arrays.stream(dir1Files).map(File::getName).toList();
        assertEquals(1, names2.size());
        assertTrue(names2.contains("b.txt"));

        // nonexistent / escape → null (per implementation)
        assertNull(fs.filesIn("nope"));
        assertNull(fs.filesIn("../outside"));
    }
}
