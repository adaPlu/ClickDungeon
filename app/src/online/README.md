Online test source directory

Place online-only unit tests under `app/src/online/java` so they are compiled
with the normal unit test classpath. The project config includes this folder in
`test` source set to allow `:app:testDebugUnitTest` to run them.

If you need a separate Gradle task (e.g., `testOnlineDebugUnitTest`), update
`app/build.gradle.kts` to create a custom source set and tasks for the new
variant.
