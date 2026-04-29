<#
.SYNOPSIS
Captures ClickDungeon Play Console screenshots from an attached Android phone.

.DESCRIPTION
This guided workflow verifies that adb is available, verifies that exactly one
authorized Android phone is connected, creates docs/screenshots/output,
then captures screenshots using the file names defined in
docs/screenshots/SCREENSHOT_SPEC.md.

The script does not navigate gameplay for you. Prepare each requested screen on
a release build, then press Enter when prompted.

.PARAMETER DeviceSerial
ADB device serial to use when more than one phone is connected.

.PARAMETER IncludeOptional
Also capture the optional boss combat and class selection screenshots.

.PARAMETER Force
Overwrite existing files in docs/screenshots/output.

.PARAMETER NoPrompt
Capture each screenshot immediately without waiting for Enter.

.PARAMETER SettleSeconds
Seconds to wait after each prompt before capturing. Defaults to 1.

.PARAMETER Help
Print usage examples and exit without checking adb.

.EXAMPLE
.\scripts\capture_play_screenshots.ps1

Captures the two required Play Console screenshots.

.EXAMPLE
.\scripts\capture_play_screenshots.ps1 -IncludeOptional -Force

Captures all four specified screenshots, replacing existing output files.
#>

[CmdletBinding()]
param(
    [string] $DeviceSerial,
    [switch] $IncludeOptional,
    [switch] $Force,
    [switch] $NoPrompt,
    [ValidateRange(0, 30)]
    [int] $SettleSeconds = 1,
    [switch] $Help
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Show-Usage {
    Write-Host "ClickDungeon Play screenshot capture"
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\scripts\capture_play_screenshots.ps1 [-DeviceSerial SERIAL] [-IncludeOptional] [-Force] [-NoPrompt]"
    Write-Host ""
    Write-Host "Output:"
    Write-Host "  docs/screenshots/output/screenshot_01_main_menu.png"
    Write-Host "  docs/screenshots/output/screenshot_02_dungeon_grid.png"
    Write-Host "  docs/screenshots/output/screenshot_03_boss_combat.png     (-IncludeOptional)"
    Write-Host "  docs/screenshots/output/screenshot_04_class_selection.png (-IncludeOptional)"
    Write-Host ""
    Write-Host "Before running:"
    Write-Host "  1. Install Android SDK Platform-Tools so adb.exe is on PATH."
    Write-Host "  2. Connect one authorized Android phone."
    Write-Host "  3. Install and launch the release build of ClickDungeon."
    Write-Host "  4. Follow docs/screenshots/SCREENSHOT_SPEC.md for screen setup."
}

if ($Help) {
    Show-Usage
    exit 0
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$outputDir = Join-Path $repoRoot "docs/screenshots/output"
$remotePath = "/sdcard/clickdungeon_play_screenshot.png"

$screens = @(
    [pscustomobject]@{
        Title = "Main Menu"
        FileName = "screenshot_01_main_menu.png"
        Required = $true
        Setup = "Show MainMenuActivity with an existing save, Continue enabled, no dialog open."
    },
    [pscustomobject]@{
        Title = "Active Dungeon"
        FileName = "screenshot_02_dungeon_grid.png"
        Required = $true
        Setup = "Show GameActivity mid-run as Knight with mixed revealed tiles, HUD visible, and no combat dialog."
    },
    [pscustomobject]@{
        Title = "Boss Combat"
        FileName = "screenshot_03_boss_combat.png"
        Required = $false
        Setup = "Show CombatDialogFragment on a boss floor with boss health, combat log, Attack/Flee, and HUD behind it."
    },
    [pscustomobject]@{
        Title = "Class Selection"
        FileName = "screenshot_04_class_selection.png"
        Required = $false
        Setup = "Show ClassSelectionActivity initial state with all four class buttons visible and no class selected."
    }
)

function Resolve-AdbPath {
    $adbCommand = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT) | Where-Object { $_ }
    foreach ($sdkRoot in $sdkRoots) {
        $candidate = Join-Path $sdkRoot "platform-tools/adb.exe"
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw @"
adb.exe was not found.

Install Android SDK Platform-Tools, then either add platform-tools to PATH or
set ANDROID_HOME / ANDROID_SDK_ROOT. In Android Studio, SDK Manager can install
Platform-Tools. Verify with:
  adb devices
"@
}

function Invoke-Adb {
    param(
        [Parameter(Mandatory = $true)]
        [string] $AdbPath,
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    $output = & $AdbPath @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb $($Arguments -join ' ') failed: $($output -join [Environment]::NewLine)"
    }

    return $output
}

function Get-AuthorizedDevice {
    param(
        [Parameter(Mandatory = $true)]
        [string] $AdbPath,
        [string] $RequestedSerial
    )

    $rows = Invoke-Adb -AdbPath $AdbPath -Arguments @("devices")
    $devices = @()
    $blocked = @()

    foreach ($row in $rows) {
        if ($row -match "^(\S+)\s+(\S+)$" -and $Matches[1] -ne "List") {
            $entry = [pscustomobject]@{
                Serial = $Matches[1]
                State = $Matches[2]
            }

            if ($entry.State -eq "device") {
                $devices += $entry
            } else {
                $blocked += $entry
            }
        }
    }

    if ($RequestedSerial) {
        $match = $devices | Where-Object { $_.Serial -eq $RequestedSerial } | Select-Object -First 1
        if ($match) {
            return $match.Serial
        }

        throw @"
Requested adb device '$RequestedSerial' is not connected and authorized.

Run 'adb devices', then either start/connect that device or pass a connected
serial with -DeviceSerial.
"@
    }

    if ($devices.Count -eq 1) {
        return $devices[0].Serial
    }

    if ($devices.Count -gt 1) {
        $serials = ($devices | ForEach-Object { "  $($_.Serial)" }) -join [Environment]::NewLine
        throw @"
Multiple authorized adb devices are connected.

Re-run with one of these serials:
$serials

Example:
  .\scripts\capture_play_screenshots.ps1 -DeviceSerial SERIAL
"@
    }

    $blockedText = ""
    if ($blocked.Count -gt 0) {
        $blockedText = [Environment]::NewLine + "Detected but unavailable:" + [Environment]::NewLine +
            (($blocked | ForEach-Object { "  $($_.Serial) $($_.State)" }) -join [Environment]::NewLine)
    }

    throw @"
No authorized adb phone is connected.$blockedText

Connect a phone with USB debugging enabled, accept the RSA authorization prompt,
then verify:
  adb devices

After one device appears with state 'device', re-run this script.
"@
}

function Capture-Screenshot {
    param(
        [Parameter(Mandatory = $true)]
        [string] $AdbPath,
        [Parameter(Mandatory = $true)]
        [string] $Serial,
        [Parameter(Mandatory = $true)]
        [string] $TargetPath
    )

    Invoke-Adb -AdbPath $AdbPath -Arguments @("-s", $Serial, "shell", "screencap", "-p", $remotePath) | Out-Null
    Invoke-Adb -AdbPath $AdbPath -Arguments @("-s", $Serial, "pull", $remotePath, $TargetPath) | Out-Null
    Invoke-Adb -AdbPath $AdbPath -Arguments @("-s", $Serial, "shell", "rm", "-f", $remotePath) | Out-Null

    if (-not (Test-Path $TargetPath)) {
        throw "adb reported success, but the screenshot was not created: $TargetPath"
    }

    if ((Get-Item $TargetPath).Length -le 0) {
        throw "Captured screenshot is empty: $TargetPath"
    }
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$adbPath = Resolve-AdbPath
$serial = Get-AuthorizedDevice -AdbPath $adbPath -RequestedSerial $DeviceSerial
$selectedScreens = $screens | Where-Object { $_.Required -or $IncludeOptional }

Write-Host "Using adb: $adbPath"
Write-Host "Using device: $serial"
Write-Host "Writing screenshots to: $outputDir"
Write-Host ""

foreach ($screen in $selectedScreens) {
    $targetPath = Join-Path $outputDir $screen.FileName

    if ((Test-Path $targetPath) -and -not $Force) {
        throw "Output already exists: $targetPath. Re-run with -Force to overwrite."
    }

    Write-Host "Next: $($screen.Title)"
    Write-Host "Setup: $($screen.Setup)"

    if (-not $NoPrompt) {
        Read-Host "Press Enter when the device is ready to capture"
    }

    if ($SettleSeconds -gt 0) {
        Start-Sleep -Seconds $SettleSeconds
    }

    Capture-Screenshot -AdbPath $adbPath -Serial $serial -TargetPath $targetPath
    Write-Host "Captured: $targetPath"
    Write-Host ""
}

Write-Host "Done. Review output against docs/screenshots/SCREENSHOT_SPEC.md before uploading to Play Console."
