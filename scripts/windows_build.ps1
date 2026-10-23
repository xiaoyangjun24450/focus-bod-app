param([switch]$BuildOnly, [string]$Serial)
$ErrorActionPreference = 'Stop'
try {
    . "$PSScriptRoot\windows_env.ps1"
    Initialize-BuildEnvironment
    Push-Location $ProjectRoot
    try {
        $gradle = Get-ProjectGradle
        & $gradle :app:assembleDebug
        if ($LASTEXITCODE -ne 0) { throw "Build failed ($LASTEXITCODE)." }
        if (-not $BuildOnly) {
            $adb = Find-Adb
            $deviceArgs = @()
            if ($Serial) { $deviceArgs = @('-s', $Serial) }
            & $adb @deviceArgs install -r -t "$ProjectRoot\app\build\outputs\apk\debug\app-debug.apk"
            if ($LASTEXITCODE -ne 0) { throw "Installation failed ($LASTEXITCODE). Check adb devices and USB authorization." }
        }
    } finally { Pop-Location }
} catch { Write-Error $_ -ErrorAction Continue; exit 1 }


