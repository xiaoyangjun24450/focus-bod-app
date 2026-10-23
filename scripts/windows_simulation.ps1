param([string]$Serial, [string]$Package = 'com.xyj.focuspod')
$ErrorActionPreference = 'Stop'
try {
    . "$PSScriptRoot\windows_env.ps1"
    $adb = Find-Adb
    $deviceArgs = @()
    if ($Serial) { $deviceArgs = @('-s', $Serial) }
    $processId = & $adb @deviceArgs shell pidof -s $Package
    if ($LASTEXITCODE -ne 0 -or -not $processId) { throw "Process not found: $Package. Open the app on the phone first; check adb devices." }
    $processId = "$processId".Trim()
    if ($processId -notmatch '^\d+$') { throw "Unexpected process ID: $processId" }
    & $adb @deviceArgs logcat "--pid=$processId" '*:I'
    if ($LASTEXITCODE -ne 0) { throw "logcat failed ($LASTEXITCODE)." }
} catch { Write-Error $_ -ErrorAction Continue; exit 1 }
