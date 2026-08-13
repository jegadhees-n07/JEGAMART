@echo off
REM Simple HTTP Server for Frontend
REM Requires Node.js or Python installed

cd /d "%~dp0"

REM Try Node.js first
where /q npm
if %ERRORLEVEL% EQU 0 (
    echo Starting frontend server with Node.js...
    cd frontend
    npx -y http-server -p 3000 -g
    exit /b
)

REM Try Python next
python --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Starting frontend server with Python...
    cd frontend
    python -m http.server 3000
    exit /b
)

REM Fallback - use PowerShell
echo Starting frontend server with PowerShell...
powershell -Command "cd 'c:\Users\acer\Desktop\Test\frontend'; & { $HttpListener = [System.Net.HttpListener]::new(); $HttpListener.Prefixes.Add('http://localhost:3000/'); $HttpListener.Start(); Write-Host 'Frontend server running on http://localhost:3000'; while ($HttpListener.IsListening) { $Context = $HttpListener.GetContext(); $Response = $Context.Response; $RequestedPath = $Context.Request.Url.LocalPath; if ($RequestedPath -eq '/') { $RequestedPath = '/login.html' }; $FullPath = Join-Path (Get-Location) $RequestedPath.TrimStart('/'); if (Test-Path $FullPath) { $Response.StatusCode = 200; $Content = [System.IO.File]::ReadAllBytes($FullPath); $Response.ContentLength64 = $Content.Length; $Response.OutputStream.Write($Content, 0, $Content.Length) } else { $Response.StatusCode = 404; $Response.StatusCode = 404; } $Response.Close() } }"
