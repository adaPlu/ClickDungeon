param(
    [string]$ApkPath = "app/build/outputs/apk/debug/app-debug.apk",
    [string]$PackageName = "com.adaplu.clickdungeon",
    [string]$LaunchActivity = ".MainMenuActivity",
    [string]$DeviceSerial = "",
    [switch]$SkipInstall,
    [switch]$SkipClear,
    [switch]$Help
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Show-Help {
    Write-Host "ClickDungeon release smoke setup helper"
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\scripts\release_smoke_check.ps1"
    Write-Host "  .\scripts\release_smoke_check.ps1 -ApkPath app\build\outputs\apk\release\app-release.apk"
    Write-Host "  .\scripts\release_smoke_check.ps1 -DeviceSerial <adb-serial>"
    Write-Host "  .\scripts\release_smoke_check.ps1 -SkipInstall"
    Write-Host ""
    Write-Host "Actions:"
    Write-Host "  1. Confirms adb is available and an authorized Android phone is connected."
    Write-Host "  2. Installs the APK unless -SkipInstall is supplied."
    Write-Host "  3. Clears app data unless -SkipClear is supplied."
    Write-Host "  4. Launches the app's main menu activity."
    Write-Host ""
    Write-Host "Evidence:"
    Write-Host "  Record results in docs/LAUNCH_PLAN.md under the release smoke template."
}

function Get-AdbArgs {
    param([string[]]$CommandArgs)

    $adbArgs = @()
    if (-not [string]::IsNullOrWhiteSpace($DeviceSerial)) {
        $adbArgs += @("-s", $DeviceSerial)
    }
    $adbArgs += $CommandArgs
    return $adbArgs
}

function Invoke-Adb {
    param([string[]]$CommandArgs)

    $adbArgs = Get-AdbArgs -CommandArgs $CommandArgs
    Write-Host "adb $($adbArgs -join ' ')"
    & $script:AdbPath @adbArgs
    if ($LASTEXITCODE -ne 0) {
        throw "adb command failed with exit code $LASTEXITCODE"
    }
}

if ($Help) {
    Show-Help
    exit 0
}

if (-not $SkipInstall -and -not (Test-Path -LiteralPath $ApkPath)) {
    throw "APK not found: $ApkPath. Build it first, pass -ApkPath, or use -SkipInstall when the app is already installed."
}

$adb = Get-Command adb -ErrorAction SilentlyContinue
if ($null -eq $adb) {
    throw "adb was not found on PATH. Install Android platform-tools or add adb to PATH."
}
$script:AdbPath = $adb.Source

$deviceLines = @(& $script:AdbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" })
if ($deviceLines.Count -eq 0) {
    throw "No authorized adb device is connected. Connect and unlock an Android phone, authorize USB debugging, then verify with 'adb devices'."
}
if ($deviceLines.Count -gt 1 -and [string]::IsNullOrWhiteSpace($DeviceSerial)) {
    Write-Host "Connected devices:"
    $deviceLines | ForEach-Object { Write-Host "  $_" }
    throw "Multiple adb devices are connected. Re-run with -DeviceSerial <serial>."
}
if (-not [string]::IsNullOrWhiteSpace($DeviceSerial)) {
    $serialPattern = "^$([regex]::Escape($DeviceSerial))\s+device$"
    if (-not ($deviceLines | Where-Object { $_ -match $serialPattern })) {
        throw "Requested adb device '$DeviceSerial' is not connected and authorized. Run 'adb devices' and pass a listed serial."
    }
}

if (-not $SkipInstall) {
    $resolvedApk = Resolve-Path -LiteralPath $ApkPath -ErrorAction Stop
    Invoke-Adb -CommandArgs @("install", "-r", $resolvedApk.Path)
}

if (-not $SkipClear) {
    Invoke-Adb -CommandArgs @("shell", "pm", "clear", $PackageName)
}

Invoke-Adb -CommandArgs @("shell", "am", "start", "-n", "$PackageName/$LaunchActivity")

Write-Host ""
Write-Host "App launched. Continue with the manual smoke checklist in docs/LAUNCH_PLAN.md."
