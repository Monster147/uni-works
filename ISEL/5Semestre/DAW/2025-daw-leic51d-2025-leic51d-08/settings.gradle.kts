plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "2025-daw-leic51d-2025-leic51d-08"

include("app")
include("domain")
include("http")
include("service")
include("repo")
include("repo-jdbc")

