import net.neoforged.moddevgradle.dsl.ModModel
import net.neoforged.moddevgradle.dsl.RunModel
import org.jetbrains.kotlin.gradle.idea.proto.com.google.protobuf.mixin
import org.slf4j.event.Level


plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("java")
    id("idea")
    id("net.neoforged.moddev") version "2.0.140"
    id("com.diffplug.spotless") version ("6.19.0")
}

val modId: String by project
val modVersion: String by project
val modGroupId: String by project
val modName: String by project
val modLicense: String by project
val modAuthors: String by project
val modDescription: String by project
val mcVersion: String by project
val mcVersionRange: String by project
val neoForgeVersion: String by project
val neoForgeVersionRange: String by project
val loaderVersionRange: String by project
val kffVersion: String by project
val fzzyConfigVersion: String by project
val tabVersion: String by project
val parchmentMappingsVersion: String by project
val parchmentMCVersion: String by project

version = modVersion
group = modGroupId

base {
    archivesName.set(modName)
}


tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}



java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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

    maven("https://jitpack.io")
    maven { url = uri("https://api.modrinth.com/maven") }
    mavenCentral()
}

neoForge {
    version = neoForgeVersion
    parchment {
        mappingsVersion = parchmentMappingsVersion
        minecraftVersion = parchmentMCVersion
    }


    runs {

        val client : RunModel by creating {
            client()
            programArguments.addAll("--username=ScaredRabbitNL", "--uuid=67e129a0-7954-4ad0-bc39-d2ecf97e7a1a")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        val client2 : RunModel by creating {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            programArguments.addAll("--username=ScaredRabbitNL2")
        }

        val server : RunModel by creating {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
        val gameTestServer : RunModel by creating {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
        val data : RunModel by creating {
            data()
            programArguments.addAll("--mod", modId, "--all", "--output", file("src/generated/resources/").getAbsolutePath(), "--existing", file("src/main/resources/").getAbsolutePath())
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG
        }

    }
    mods {
        modId.let {
            val sourceSet : ModModel by creating {
                sourceSet(sourceSets.main.get())
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
    implementation("thedarkcolour:kotlinforforge-neoforge:${kffVersion}")
    //annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    implementation("curse.maven:tacz-1-21-1-1353462:8547439-sources-8547440")
    compileOnly("top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api")
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1")

    implementation("software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.2")
    implementation("curse.maven:superb-warfare-1218165:8104860-sources-8104863")
    implementation("com.github.NEZNAMY:TAB-API:${tabVersion}")
    compileOnly("net.luckperms:api:5.5")

    implementation("me.fzzyhmstrs:fzzy_config:$fzzyConfigVersion+1.21+neoforge")
}

tasks {
    withType<ProcessResources> {
        val replaceProperties = mapOf(
            "minecraft_version" to mcVersion,
            "minecraft_version_range" to mcVersionRange,
            "neo_version" to neoForgeVersion,
            "neo_version_range" to neoForgeVersionRange,
            "loader_version_range" to loaderVersionRange,
            "mod_id" to modId,
            "mod_name" to modName,
            "mod_license" to modLicense,
            "mod_version" to modVersion,
            "mod_authors" to modAuthors,
            "mod_description" to modDescription
        )

        inputs.properties(replaceProperties)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(replaceProperties)
        }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
    }
    withType<Jar> {
        manifest.attributes(
            "MixinConfigs" to "scaredsfactions.mixins.json"
        )
        archiveFileName = "${modName}-${modVersion}-mc${mcVersion}.jar"
    }
}

spotless {
    java {
        targetExclude("src/main/java/io/github/scaredsmods/scaredsfactions/common/compat/luckperms/LuckPermsAPICompat.java",)
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


