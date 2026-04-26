Write-Host "?? Stopping any existing processes..."
Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

Write-Host "?? Starting Backend (Spring Boot)..."
$backendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c mvnw.cmd spring-boot:run" -WorkingDirectory ".\backend" -PassThru

Write-Host "?? Starting Frontend (SvelteKit)..."
Set-Location ".\frontend"
npm install
$frontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev -- --open" -WorkingDirectory "." -PassThru
Set-Location ".."

Write-Host "? Waiting for services to initialize..."
Start-Sleep -Seconds 5

Write-Host "?? Opening Application..."
$url = "http://localhost:5173"
Start-Process $url

Write-Host "? System is running! (Frontend PID: $($frontendProcess.Id) | Backend PID: $($backendProcess.Id))"
Write-Host "Press [Ctrl+C] to stop both services."

try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
}
finally {
    Write-Host "`nTerminating processes..."
    Stop-Process -Id $frontendProcess.Id -Force -ErrorAction SilentlyContinue
    Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
    Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
    Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
    Write-Host "Done."
}