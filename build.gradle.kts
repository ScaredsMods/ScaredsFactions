import com.diffplug.gradle.spotless.JavaExtension
import net.minecraftforge.gradle.common.util.ModConfig
import net.minecraftforge.gradle.common.util.RunConfig

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("java")
    id("idea")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.2.0"
    id("com.diffplug.spotless") version("6.19.0")
}

val modId : String by project
val modVersion : String by project
val modGroupId : String by project
val modName : String by project
val modLicense : String by project
val modAuthors : String by project
val modDescription : String by project
val minecraftVersion : String by project
val minecraftVersionRange : String by project
val forgeVersion : String by project
val forgeVersionRange : String by project
val loaderVersionRange : String by project
val kffVersion : String by project
val fzzyConfigVersion : String by project

version = modVersion
group = modGroupId

base {
    archivesName.set(modId)
}


tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}


// Java 17
java {
    toolchain {
        // Mojang ships Java 21 to end users starting in 1.20.5, so mods should target Java 21.
        languageVersion = JavaLanguageVersion.of(17)
    }
}



repositories {
    // Forge + Maven Central are added automatically
    maven {
        // Add curse maven to repositories
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
    }

    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven { url = uri("https://maven.shedaniel.me/") }
    maven {
        name = "FzzyMaven"
        url = uri("https://maven.fzzyhmstrs.me/")
    }

}


minecraft {
    mappings(
        project.property("mappingChannel") as String,
        project.property("mappingVersion") as String
    )

    copyIdeResources.set(true)

    runs {

        val client : RunConfig by creating {
            client(true)
            args("--username=ScaredRabbitNL", "--uuid=67e129a0-7954-4ad0-bc39-d2ecf97e7a1a")
            property("forge.enabledGameTestNamespaces", modId)
        }

        val client2 : RunConfig by creating {
            client(true)
            workingDirectory(project.file("run2"))
            args("--username=ScaredRabbitNL2")
            property("forge.enabledGameTestNamespaces", modId)
        }

        val server : RunConfig by creating {
            property("forge.enabledGameTestNamespaces", modId)
            args("--nogui")
        }

        val gameTestServer : RunConfig by creating {
            property("forge.enabledGameTestNamespaces", modId)
        }

        val data : RunConfig by creating {
            workingDirectory(project.file("run-data"))

            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/").toString(),
                "--existing", file("src/main/resources/").toString()
            )
        }
        configureEach {
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            mods {
                // define mod <-> source bindings
                // these are used to tell the game which sources are for which mod
                // mostly optional in a single mod project
                // but multi mod projects should define one per mod
                modId.let {
                    val sourceSet : ModConfig by creating {
                        source(sourceSets.main.get())
                    }
                }
            }
        }
    }
}

// Include generated resources
sourceSets {
    getByName("main") {
        resources.srcDir("src/generated/resources")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")
    compileOnly("curse.maven:timeless-and-classics-zero-1028108:7745481-sources-7745491")
    compileOnly(("top.theillusivec4.curios:curios-forge:5.4.2+1.20.1:api"))
    compileOnly("software.bernie.geckolib:geckolib-forge-1.20.1:4.4.6")
    compileOnly("curse.maven:superb-warfare-1218165:7292685-sources-7292686")

    implementation("thedarkcolour:kotlinforforge:${kffVersion}")
    implementation(fg.deobf("me.fzzyhmstrs:fzzy_config:$fzzyConfigVersion+$minecraftVersion+forge"))
}

tasks {
    withType<ProcessResources> {
        val replaceProperties = mapOf(
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to minecraftVersion,
            "forge_version" to forgeVersion,
            "forge_version_range" to forgeVersionRange,
            "loader_version_range" to loaderVersionRange,
            "mod_id" to modId,
            "mod_name" to modName,
            "mod_license" to modLicense,
            "mod_version" to modVersion,
            "mod_authors" to modAuthors,
            "mod_description" to modDescription
        )

        inputs.properties(replaceProperties)

        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
            expand(replaceProperties + mapOf("project" to project))
        }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
    }
}

spotless {
    java {
        licenseHeaderFile(file("HEADER"))
        removeUnusedImports()
        indentWithTabs()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        licenseHeaderFile(file("HEADER"))
        indentWithTabs()
        trimTrailingWhitespace()
        endWithNewline()
    }

}


