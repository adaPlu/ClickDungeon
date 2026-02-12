Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "Running debug unit tests..."
& "$PSScriptRoot\\..\\gradlew.bat" testDebugUnitTest
