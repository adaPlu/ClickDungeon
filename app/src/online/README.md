Online test source directory

Place online-only unit tests under `app/src/online/java` so they are compiled
with the normal unit test classpath. The project config includes this folder in
the `test` source set, so `:app:testDebugUnitTest` runs them automatically.

For compatibility with older scripts and docs, `:app:testOnlineDebugUnitTest`
is also available as an alias and currently redirects to the same standard unit-test suite.

This folder is test/support scaffolding for connected-services work; it is not a separate app flavor.
