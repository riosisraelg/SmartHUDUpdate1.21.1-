plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

val isNeoForge = project.findProperty("neoforge")?.toString()?.toBoolean() == true

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(
        if (isNeoForge) "${project.property("archives_base_name")}-neoforge"
        else project.property("archives_base_name") as String
    )
}

repositories {
    // Cloth Config
    maven("https://maven.shedaniel.me/")
    // Trinkets & ModMenu
    maven("https://maven.terraformersmc.com/releases/")
    // Cardinal Components (Trinkets dependency)
    maven("https://maven.ladysnake.org/releases")
}

sourceSets {
    named("main") {
        if (isNeoForge) {
            java.setSrcDirs(listOf("remappedSrc", "src/main/java/net"))
        }
    }
}

loom {
    runs {
        named("client") {
            ideConfigGenerated(true)
        }
    }
}

dependencies {
    // Minecraft & Mappings
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    if (isNeoForge) {
        mappings(loom.officialMojangMappings())
    } else {
        mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    }
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Cloth Config — required dependency on Fabric, optional on NeoForge
    modApi("me.shedaniel.cloth:cloth-config-fabric:${project.property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    // ModMenu — optional, for config screen integration
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    // Trinkets — optional dependency (compile only)
    modCompileOnly("dev.emi:trinkets:${project.property("trinkets_version")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand("version" to project.version)
        }
    }

    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"
    }

    jar {
        exclude("net/neoforged/**")
        if (isNeoForge) {
            exclude(
                "fabric.mod.json",
                "smarthud.client.mixins.json",
                "*-refmap.json",
                "dev/sapphic/smarthud/compat/**"
            )
        } else {
            exclude(
                "META-INF/neoforge.mods.toml",
                "smarthud.neoforge.mixins.json",
                "dev/sapphic/smarthud/neoforge/**",
                "dev/sapphic/smarthud/mixin/InGameHudMixin.class",
                "dev/sapphic/smarthud/mixin/MinecraftClientMixin.class"
            )
        }
        from("LICENSE") {
            rename { "${it}_${project.property("archives_base_name")}" }
        }
        doLast {
            if (isNeoForge) {
                val outJar = archiveFile.get().asFile
                val libsDir = layout.buildDirectory.dir("libs").get().asFile
                libsDir.mkdirs()
                outJar.copyTo(
                    libsDir.resolve("smarthud-neoforge-${project.version}.jar"),
                    overwrite = true
                )
                val distDir = file("dist")
                distDir.mkdirs()
                outJar.copyTo(
                    distDir.resolve("smarthud-neoforge-${project.property("minecraft_version")}.jar"),
                    overwrite = true
                )
            }
        }
    }

    remapJar {
        doLast {
            if (!isNeoForge) {
                val distDir = file("dist")
                distDir.mkdirs()
                val outJar = archiveFile.get().asFile
                outJar.copyTo(
                    distDir.resolve("smarthud-${project.property("minecraft_version")}.jar"),
                    overwrite = true
                )
                outJar.copyTo(
                    distDir.resolve("smarthud-fabric-${project.property("minecraft_version")}.jar"),
                    overwrite = true
                )
            }
        }
    }
}
