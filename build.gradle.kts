plugins {
    alias(libs.plugins.mdg)
}

group = "one.devos.nautical"
version = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .orElse("99999")
    .map { "1.0.$it" }
    .get()

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

repositories {
    exclusiveContent {
        forRepositories(maven("https://maven.createmod.net")).filter {
            includeGroup("com.simibubi.create")
            includeGroup("dev.engine-room.flywheel")
            includeGroup("net.createmod.ponder")
        }

        forRepositories(maven("https://maven.ithundxr.dev/snapshots")).filter {
            includeModule("com.tterrag.registrate", "Registrate")
        }

        forRepositories(maven("https://maven.ryanhcode.dev/releases")).filter {
            includeGroup("dev.ryanhcode.sable-companion")
            includeGroup("dev.ryanhcode.sable")
            includeGroup("dev.ryanhcode.offroad")
            includeGroup("dev.simulated_team.simulated")
            includeGroup("dev.eriksonn.aeronautics")
        }

        forRepositories(maven("https://maven.blamejared.com")).filter {
            includeGroup("foundry.veil")
            includeModule("gg.moonflower", "molang-compiler")
            includeModule("io.github.ocelot", "glsl-processor")
        }

        forRepositories(maven("https://api.modrinth.com/maven")).filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    implementation(libs.create) {
        isTransitive = false
    }

    implementation(libs.ponder)
    implementation(libs.registrate)

    compileOnly(libs.flywheel.api)
    runtimeOnly(libs.flywheel)

    compileOnly(libs.bundles.sable.libs)
    implementation(libs.bundles.simulated.suite) {
        isTransitive = false
    }

    implementation(libs.bundles.functional.storage)
}

neoForge {
    version = libs.versions.neoforge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }

    mods.register("shenanigans") {
        sourceSet(sourceSets.main.get())
    }

    runs {
        register("client") {
            client()
        }

        register("server") {
            server()
            gameDirectory = file("run/server")
        }

        configureEach {
            jvmArgument("-Dmixin.debug.export=true")
        }
    }
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to libs.versions.minecraft.get(),
        "neo_version" to libs.versions.neoforge.get(),
    )

    inputs.properties(properties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }
}
