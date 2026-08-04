import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
	alias(libs.plugins.java.library)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.jacoco)
	alias(libs.plugins.freefair.lombok)
}

group = "pe.ask"
version = "1.0.4"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

jacoco {
	toolVersion = "0.8.12"
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {

	api(platform(libs.spring.boot.dependencies))
	api(libs.spring.boot.starter.data.r2dbc)

	implementation(libs.jackson.databind)
	implementation(libs.ask.core)
	implementation(libs.ask.exception.core)
	implementation(libs.r2dbc.postgresql)

	annotationProcessor(libs.spring.boot.configuration.processor)
	annotationProcessor(libs.spring.boot.autoconfigure.processor)

	testRuntimeOnly(libs.junit.platform.launcher)
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				name.set("SQL Persistence Core")
				description.set("Core library for reactive SQL persistence, providing generic adapters, automatic mapping, and pagination helpers for Spring WebFlux.")
			}
		}
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.withType<Javadoc>().configureEach {
	isFailOnError = false

	(options as StandardJavadocDocletOptions).apply {
		addStringOption("Xdoclint:none", "-quiet")
		encoding = "UTF-8"
		charSet = "UTF-8"
		docEncoding = "UTF-8"
	}
}