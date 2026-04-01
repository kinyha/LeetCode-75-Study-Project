Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$tasksPath = "C:/Users/ulad.bratchykau/AppData/Local/Zed/extensions/installed/java/languages/java/tasks.json"
$gitSh = "C:/Program Files/Git/usr/bin/sh.exe"

$tasks = Get-Content -Raw -LiteralPath $tasksPath | ConvertFrom-Json

foreach ($task in $tasks) {
    if ($task.shell -and $task.shell.with_arguments) {
        $task.shell.with_arguments.program = $gitSh
    }
}

$projectRunner = 'if [ -f scripts/run-current.ps1 ]; then powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/run-current.ps1 "$ZED_FILE"; else '
$currentRunCommand = [string]$tasks[0].command
if (-not $currentRunCommand.StartsWith('if [ -f scripts/run-current.ps1 ]; then')) {
    $tasks[0].command = $projectRunner + $currentRunCommand + ' fi;'
}

$tasks | ConvertTo-Json -Depth 20 | Set-Content -NoNewline -LiteralPath $tasksPath
