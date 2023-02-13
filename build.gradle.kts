import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import java.io.FileOutputStream

plugins {
    id("java-library")
    id("java")
    id("io.freefair.lombok") version "6.6.1"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("maven-publish")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    `kotlin-dsl` version ("2.1.7")
    `kotlin-dsl-precompiled-script-plugins`
    id("dev.s7a.gradle.minecraft.server") version "2.0.0"
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("io.freefair.gradle:lombok-plugin:6.3.0")
        classpath("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    }
}

var pluginVersion = "1.2.0-PRE"

group = "net.endrealm"
version = pluginVersion

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Gerolmed/BuildRealm")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

repositories {

    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.infernalsuite.com/repository/maven-releases/")
    maven("https://repo.rapture.pw/repository/maven-releases/")

    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.rapture.pw/repository/maven-releases/")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.22")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT")
    compileOnly("com.infernalsuite.aswm:api:1.19.3-R0.1-SNAPSHOT")

    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("fr.minuskube.inv:smart-invs:1.2.7")
    implementation("net.endrealm:realm-drive:1.3.2-RELEASE")
    implementation("org.mongodb:mongo-java-driver:3.11.2")
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.jetbrains:annotations:23.0.0")

    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")
}


tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

java {
    toolchain {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}


tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        relocate("fr.minuskube.inv", "net.endrealm.buildrealm.depends")
    }
}


// Configure plugin.yml generation
bukkit {
    name = "BuildRealm"
    main = "net.endrealm.buildrealm.BuildRealm"
    apiVersion = "1.19"
    authors = listOf("Gerolmed")
    depend = listOf("SlimeWorldManager")
    softDepend = listOf("WorldEdit")
    version = pluginVersion

    commands {
        register("draft") {
            description = "Base command for drafts"
            usage = "/draft create/list/leave"
        }
        register("group") {
            description = "Base command for groups"
            usage = "/group create/list/open"
        }
    }
}


task<LaunchMinecraftServerTask>("runServer") {
    dependsOn("shadowJar")

    doFirst {
        val slimeJarFileOutput = buildDir.resolve("MinecraftServer/plugins/AdvancedSlimeManager.jar")
        uri(ASWM.plugin("42175f090baf00494c0fb25588f1e22ad4d9558f", "1.19.3-R0.1"))
            .toURL().openStream().use { it.copyTo(FileOutputStream(slimeJarFileOutput)) }
        copy {
            val file = tasks.named<AbstractArchiveTask>("shadowJar").flatMap { shadow -> shadow.archiveFile }.get().asFile;
            from(file)
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(ASWM.server("42175f090baf00494c0fb25588f1e22ad4d9558f", "1.19.3-R0.1"))
    agreeEula.set(true)
}

class ASWM {
    companion object {
        fun server(build: String, version: String) : String {
            return "https://dl.rapture.pw/IS/ASP/main/${build}/slimeworldmanager-paperclip-${version}-SNAPSHOT-reobf.jar";
        }
        fun plugin(build: String, version: String) : String {
            return "https://dl.rapture.pw/IS/ASP/main/${build}/plugin-${version}-SNAPSHOT.jar";
        }
    }
}
