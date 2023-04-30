import org.gradle.kotlin.dsl.application

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(19))
	}
}

plugins {
	application
	java
	id("org.springframework.boot") version "3.0.6"
	id("io.spring.dependency-management") version "1.1.0"
	id("com.google.protobuf") version "0.9.2"
	kotlin("jvm") version "1.8.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

tasks {
	compileJava {
		options.compilerArgs.addAll(arrayOf("--add-modules", "jdk.incubator.concurrent", "--enable-preview"))
	}
}

configurations {
	all {
		exclude(group = "org.slf4j", module = "log4j-to-slf4j")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.slf4j:slf4j-api:2.0.7")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
	implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
	implementation("org.msgpack:jackson-dataformat-msgpack:0.9.3")
	implementation("com.google.protobuf:protobuf-java:3.22.3")
	implementation("org.apache.avro:avro:1.11.1")
	//implementation(kotlin("stdlib-jdk8"))
}

application {
	mainClass.set("com.example.demo.Main")
}
/*
tasks.withType<Test> {
	useJUnitPlatform()
}*/
/*
kotlin {
	jvmToolchain(11)
}
 */