package io.github.protasm.lpc2j.exec;

import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.SearchPathIncludeResolver;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Configuration options for the host-facing LPC execution runtime. */
public final class LpcRuntimeConfig {
    private final Path baseIncludePath;
    private final List<Path> includeSearchPaths;
    private final String parentInternalName;
    private final ClassLoader parentClassLoader;
    private final IncludeResolver includeResolver;

    private LpcRuntimeConfig(Builder builder) {
        this.baseIncludePath = (builder.baseIncludePath != null)
                ? builder.baseIncludePath.toAbsolutePath().normalize()
                : null;
        this.includeSearchPaths = List.copyOf(builder.includeSearchPaths);
        this.parentInternalName = Objects.requireNonNull(builder.parentInternalName, "parentInternalName");
        this.parentClassLoader = Objects.requireNonNull(builder.parentClassLoader, "parentClassLoader");
        this.includeResolver = builder.includeResolver;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Path baseIncludePath() {
        return baseIncludePath;
    }

    public List<Path> includeSearchPaths() {
        return includeSearchPaths;
    }

    public String parentInternalName() {
        return parentInternalName;
    }

    public ClassLoader parentClassLoader() {
        return parentClassLoader;
    }

    public IncludeResolver includeResolver() {
        return includeResolver;
    }

    IncludeResolver resolveIncludeResolver() {
        if (includeResolver != null) {
            return includeResolver;
        }

        return new SearchPathIncludeResolver(baseIncludePath, includeSearchPaths);
    }

    public static final class Builder {
        private Path baseIncludePath = Path.of(".");
        private List<Path> includeSearchPaths = List.of();
        private String parentInternalName = "java/lang/Object";
        private ClassLoader parentClassLoader = LpcRuntimeConfig.class.getClassLoader();
        private IncludeResolver includeResolver;

        public Builder baseIncludePath(Path baseIncludePath) {
            this.baseIncludePath = baseIncludePath;
            return this;
        }

        public Builder includeSearchPaths(List<Path> includeSearchPaths) {
            this.includeSearchPaths = (includeSearchPaths != null) ? includeSearchPaths : List.of();
            return this;
        }

        public Builder parentInternalName(String parentInternalName) {
            if (parentInternalName != null) {
                this.parentInternalName = parentInternalName;
            }
            return this;
        }

        public Builder parentClassLoader(ClassLoader parentClassLoader) {
            if (parentClassLoader != null) {
                this.parentClassLoader = parentClassLoader;
            }
            return this;
        }

        public Builder includeResolver(IncludeResolver includeResolver) {
            this.includeResolver = includeResolver;
            return this;
        }

        public LpcRuntimeConfig build() {
            return new LpcRuntimeConfig(this);
        }
    }
}
