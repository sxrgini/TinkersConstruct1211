# Building the Fabric 1.21.1 port

The repository root is still the original Forge 1.20.1 build (Gradle 8.1.1, which cannot run on JDK 21). **Do not run `gradlew` in the repository root** for the Fabric port.

Requirements: **JDK 21** and internet access. Mantle (`mantle-fabric`) is built as part of `tconstruct-fabric` and embedded in its jar, so there is only one jar to install.

```
cd tconstruct-fabric
./gradlew build             # Windows: .\gradlew.bat build ; jar ends up in build/libs
./gradlew runClient         # dev client
./gradlew runServer         # dev server
./gradlew runDatagen        # regenerates src/generated/resources
```

Use `TConstruct-Fabric-1.21.1-<version>.jar` (not the `-sources` jar). Shortcut from the repository root: `build-fabric.bat` / `./build-fabric.sh`.

If Gradle picks the wrong JDK, set `JAVA_HOME` to a JDK 21 install, or run `./gradlew -Dorg.gradle.java.home=<path> ...`.
