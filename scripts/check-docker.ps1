# Safe diagnostics for Docker on Windows
# Run in PowerShell (you may need to run as Administrator for some checks).

Write-Host "--- Docker CLI version ---"
try {
    docker version --format "{{json .}}" | ConvertFrom-Json | Format-List -Force
} catch {
    Write-Host "docker version failed: $_"
}

Write-Host "`n--- docker info ---"
try {
    docker info --format "{{json .}}" | ConvertFrom-Json | Format-List -Force
} catch {
    Write-Host "docker info failed: $_"
}

Write-Host "`n--- Docker related Windows services ---"
Get-Service *docker* -ErrorAction SilentlyContinue | Format-Table -AutoSize

Write-Host "`n--- Check for Docker Desktop process ---"
Get-Process -Name '*Docker*' -ErrorAction SilentlyContinue | Select-Object Id,ProcessName,Path | Format-Table -AutoSize

Write-Host "`n--- WSL distros (if WSL2 is used) ---"
try {
    wsl --list --verbose
} catch {
    Write-Host "wsl list failed or WSL not available: $_"
}

Write-Host "`n--- Suggestion ---"
Write-Host "If Server info is missing or the docker commands failed with the pipe error, ensure Docker Desktop is installed and running. Try starting Docker Desktop from Start Menu and wait until it reports 'Docker is running'. If that fails, run PowerShell as Administrator and restart the Docker service or reboot the machine after enabling WSL2/Hyper-V as needed."

Write-Host "`n--- End of script ---"
