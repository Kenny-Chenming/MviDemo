pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyMviProject"
include(":app")

// PRD-307: Google agents-cli Enterprise GCP Agent Deployment Governance SDK
include(
    ":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-core",
    ":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-orchestration",
    ":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-iam",
    ":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-audit",
    ":feature:prd-307-Google-agents-cli-Enterprise-SDK:agents-cli-sdk-ci"
)
