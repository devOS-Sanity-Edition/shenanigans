plugins {
    alias(libs.plugins.mdg)
}

group = "one.devos.nautical"
version = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .orElse("99999")
    .map { "1.0.$it" }
    .get()

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

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
        create("client") {
            client()
        }

        create("server") {
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
