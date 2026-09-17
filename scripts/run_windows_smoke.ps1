$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$exeRelative = "build/phase16/windows/ClickDungeon.exe"
$logRelative = "build/phase16/windows-player.log"
$outRelative = "build/phase16/windows-smoke.json"
$exePath = Join-Path $repoRoot $exeRelative
$logPath = Join-Path $repoRoot $logRelative
$outPath = Join-Path $repoRoot $outRelative
$timeoutSeconds = 45
$markers = @(
    "CD_SMOKE_BOOT",
    "CD_SMOKE_MAIN_MENU",
    "CD_SMOKE_START_GAME",
    "CD_SMOKE_DUNGEON_READY",
    "CD_SMOKE_COMPLETE"
)

function Write-SmokeReport([string]$status, [string[]]$findings) {
    $report = [ordered]@{
        status = $status
        exe = $exeRelative
        log = $logRelative
        markers = $markers
        findings = $findings
    }
    $outDir = Split-Path -Parent $outPath
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    $report | ConvertTo-Json -Depth 4 | Set-Content -Path $outPath -Encoding UTF8
}

if (-not (Test-Path -LiteralPath $exePath -PathType Leaf)) {
    Write-SmokeReport "FAIL" @("missing executable: $exeRelative")
    Write-Error "Missing Windows build: $exePath"
    Exit 1
}

if (Test-Path -LiteralPath $logPath) {
    Remove-Item -Force $logPath
}

$arguments = @("-releaseSmoke", "-logFile", $logPath, "-batchmode", "-nographics")
$process = Start-Process -FilePath $exePath -ArgumentList $arguments -PassThru
$finished = $process.WaitForExit($timeoutSeconds * 1000)
if (-not $finished) {
    try { $process.Kill() } catch { }
    Write-SmokeReport "FAIL" @("player timeout after $timeoutSeconds seconds")
    Write-Error "Windows smoke timed out"
    Exit 1
}

if (-not (Test-Path -LiteralPath $logPath -PathType Leaf)) {
    Write-SmokeReport "FAIL" @("player log missing: $logRelative")
    Write-Error "Windows smoke log was not created"
    Exit 1
}

$logText = Get-Content -Raw -LiteralPath $logPath
$lastIndex = -1
$findings = @()
foreach ($marker in $markers) {
    $markerIndex = $logText.IndexOf($marker)
    if ($markerIndex -lt 0) {
        $findings += "missing marker: $marker"
        continue
    }
    if ($markerIndex -le $lastIndex) {
        $findings += "out-of-order marker: $marker"
        continue
    }
    $lastIndex = $markerIndex
}

if ($process.ExitCode -ne 0) {
    $findings += "player exit code: $($process.ExitCode)"
}

if ($findings.Count -gt 0) {
    Write-SmokeReport "FAIL" $findings
    $findings | ForEach-Object { Write-Error $_ }
    Exit 1
}

Write-SmokeReport "PASS" @()
Write-Output "windows built-player smoke: PASS"
Exit 0
