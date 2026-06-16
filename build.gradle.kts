plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    // Cloth Config
    maven("https://maven.shedaniel.me/")
    // Trinkets & ModMenu
    maven("https://maven.terraformersmc.com/releases/")
    // Cardinal Components (Trinkets dependency)
    maven("https://maven.ladysnake.org/releases")
}

loom {
    runs {
        named("client") {
            ideConfigGenerated(true)
        }
    }
}

dependencies {
    // Minecraft & Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Cloth Config — required dependency
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

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }
}
