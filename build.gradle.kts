import org.gradle.kotlin.dsl.application
import com.google.protobuf.gradle.*

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
	//kotlin("jvm") version "1.8.21"
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
protobuf {
	protoc {
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.49.2"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				id("grpc")
			}
		}
	}
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.0.6")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.slf4j:slf4j-api:2.0.7")
	implementation("com.google.protobuf:protobuf-java:3.22.3")
	runtimeOnly("io.grpc:grpc-netty-shaded:1.49.2")
	implementation("io.grpc:grpc-protobuf:1.49.2")
	implementation("io.grpc:grpc-stub:1.49.2")
	implementation("org.jetbrains:annotations:24.0.1")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
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