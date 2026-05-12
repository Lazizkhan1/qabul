plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.5"
    id("org.hibernate.orm") version "7.2.12.Final"
}

group = "uz.umft"
version = "0.0.1-SNAPSHOT"
description = "qabul"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.apache.poi:poi-ooxml:5.5.1")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

hibernate {
    enhancement {
        enableAssociationManagement = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Ensure tests use the correct environment by ignoring conflicting environment variables from the host
    environment("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/qabul_test")
    environment("SPRING_DATASOURCE_USERNAME", "postgres")
    environment("SPRING_DATASOURCE_PASSWORD", "123")
    environment("SPRING_PROFILES_ACTIVE", "test")
}
graalvmNative {
    binaries {
        all {
            // native-build-tools reads this to find native-image unless either JAVA_HOME or GRAALVM_HOME are set:
            //  https://github.com/graalvm/native-build-tools/blob/0.9.28/native-gradle-plugin/src/main/java/org/graalvm/buildtools/gradle/tasks/BuildNativeImageTask.java#L211
            //  https://github.com/graalvm/native-build-tools/blob/0.9.28/native-gradle-plugin/src/main/java/org/graalvm/buildtools/gradle/internal/NativeImageExecutableLocator.java#L89
            //  https://github.com/graalvm/native-build-tools/issues/542
            javaLauncher.set(javaToolchains.launcherFor {
                // Compile with native-image from GraalVM for JDK21
                languageVersion.set(JavaLanguageVersion.of(25))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            }
            )
        }
    }
}