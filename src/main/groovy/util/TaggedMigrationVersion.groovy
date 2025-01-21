package util

import org.apache.commons.lang3.math.NumberUtils

/**
 * Represents an app version from the perspective of a tagged liquibase changeSet.
 * By convention these should map to directories under the grails-app/migrations folder (Ex: '0.9.x').
 */
class TaggedMigrationVersion implements Comparable<TaggedMigrationVersion> {
    static final int INVALID_VERSION = -1
    static final String LATEST_VERSION_STRING = 'LATEST'

    /**
     * Represents 'whatever version is newest'. Saves us from having to update config values for each new release.
     */
    static final TaggedMigrationVersion LATEST_VERSION = new TaggedMigrationVersion(LATEST_VERSION_STRING)

    int major
    int minor
    int patch

    // Needed so we can easily convert back to the original stringified version.
    String stringVersion

    TaggedMigrationVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null.")
        }
        stringVersion = version

        // We shouldn't be comparing versions for the 'LATEST' version, but set values just in case.
        if (version == LATEST_VERSION_STRING) {
            major = 999
            minor = 999
            patch = 999
            return
        }

        String[] versionSplit = version.split("\\.")
        if (versionSplit.length != 3) {
            throw new IllegalArgumentException("Invalid liquibase version: ${version}. Expected format: 'x.y.z'.")
        }
        major = NumberUtils.toInt(versionSplit[0], INVALID_VERSION)
        minor = NumberUtils.toInt(versionSplit[1], INVALID_VERSION)
        patch = NumberUtils.toInt(versionSplit[2], INVALID_VERSION)  // This is usually 'x', which resolves to -1
    }

    /**
     * 0.9.x is newer than 0.8.x
     * 2.0.x is newer than 1.10.x
     * 0.9.0 is newer than 0.9.x (a weird edge case that we shouldn't ever actually hit)
     */
    @Override
    int compareTo(TaggedMigrationVersion that) {
        if (that == null) {
            return 1
        }
        if (major != that.major) {
            return major > that.major ? 1 : -1
        }
        if (minor != that.minor) {
            return minor > that.minor ? 1 : -1
        }
        if (patch != that.patch) {
            return patch > that.patch ? 1 : -1
        }
        return 0
    }

    @Override
    String toString() {
        return stringVersion
    }
}
