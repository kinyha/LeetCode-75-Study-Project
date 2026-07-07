param(
    [Parameter(Mandatory = $true)]
    [string]$File,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ProgramArgs
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if ($null -eq $ProgramArgs) {
    $ProgramArgs = @()
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

if (-not (Test-Path -LiteralPath $File)) {
    $projectRelativeFile = Join-Path $root $File
    if (Test-Path -LiteralPath $projectRelativeFile) {
        $File = $projectRelativeFile
    } else {
        Write-Error "File not found: $File"
        exit 1
    }
}

$resolvedFile = Resolve-Path -LiteralPath $File
$extension = [System.IO.Path]::GetExtension($resolvedFile).ToLowerInvariant()
if ($extension -ne ".java" -and $extension -ne ".kt") {
    Write-Error "Current file is not a Java/Kotlin source file: $resolvedFile"
    exit 1
}

& .\gradlew.bat classes
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$content = Get-Content -Raw -LiteralPath $resolvedFile
$packageMatch = [regex]::Match($content, '(?m)^\s*package\s+([A-Za-z_][\w]*(?:\.[A-Za-z_][\w]*)*)\s*;?')
$packageName = ""
if ($packageMatch.Success) {
    $packageName = $packageMatch.Groups[1].Value
}

$stem = [System.IO.Path]::GetFileNameWithoutExtension($resolvedFile)
$classNames = New-Object System.Collections.Generic.List[string]

if ($extension -eq ".kt") {
    # Top-level Kotlin main is compiled to FileNameKt.
    $classNames.Add($stem + "Kt")
}

# Java usually requires the public class name to match the file name.
# Kotlin often does not, so also try class/object names declared in the file.
$classNames.Add($stem)
$typeMatches = [regex]::Matches($content, '\b(?:class|object)\s+([A-Za-z_][\w]*)')
foreach ($match in $typeMatches) {
    $classNames.Add($match.Groups[1].Value)
}

$candidates = $classNames |
    Select-Object -Unique |
    ForEach-Object {
        if ([string]::IsNullOrWhiteSpace($packageName)) { $_ } else { "$packageName.$_" }
    }

$classPath = "build/classes/java/main;build/classes/kotlin/main"
$lastExitCode = 1

foreach ($mainClass in $candidates) {
    Write-Host "Running $mainClass ..."
    & java -cp $classPath $mainClass @ProgramArgs
    $javaSucceeded = $?
    $lastExitCode = $LASTEXITCODE

    if ($javaSucceeded) {
        exit 0
    }
}

Write-Error "Could not run current file. Make sure it contains a public/static main entry point. Tried: $($candidates -join ', ')"
exit $lastExitCode
