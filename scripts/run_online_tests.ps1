# Run unit tests for the app module and collect reports
# Usage: .\run_online_tests.ps1

param()

Write-Host "Running :app:testDebugUnitTest..."
& .\gradlew.bat :app:testDebugUnitTest --no-daemon | Tee-Object test-run.log

$reportDir = "app\build\test-results\testDebugUnitTest"
if (Test-Path $reportDir) {
    Write-Host "Test reports available: $reportDir"
} else {
    Write-Host "No test reports found. Check the Gradle output (test-run.log)."
}

Write-Host "Done. See test-run.log and app\build\reports\tests\testDebugUnitTest for details."