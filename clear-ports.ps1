# Script para limpiar puertos ocupados por la aplicación ShipmentsUQ
# Ejecutar con: powershell -ExecutionPolicy Bypass -File clear-ports.ps1

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Limpieza de Puertos - ShipmentsUQ" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Puerto a limpiar
$port = 8080

Write-Host "[1/3] Buscando procesos en puerto $port..." -ForegroundColor Yellow
$netstatOutput = netstat -ano | Select-String ":$port"

if ($netstatOutput) {
    Write-Host "[ENCONTRADO] Procesos usando puerto $port" -ForegroundColor Green
    Write-Host $netstatOutput -ForegroundColor Gray
    
    # Extraer PIDs
    $pids = @()
    foreach ($line in $netstatOutput) {
        if ($line -match '\s+(\d+)\s*$') {
            $processId = $matches[1]
            if ($processId -and $processId -ne "0" -and $pids -notcontains $processId) {
                $pids += $processId
            }
        }
    }
    
    if ($pids.Count -gt 0) {
        Write-Host ""
        Write-Host "[2/3] PIDs a terminar: $($pids -join ', ')" -ForegroundColor Yellow
        
        foreach ($processId in $pids) {
            try {
                $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
                if ($process) {
                    Write-Host "  Terminando: $($process.Name) (PID: $processId)" -ForegroundColor Cyan
                    Stop-Process -Id $processId -Force -ErrorAction Stop
                    Write-Host "  [OK] Proceso terminado" -ForegroundColor Green
                }
            }
            catch {
                Write-Host "  [ERROR] No se pudo terminar PID $pid : $_" -ForegroundColor Red
            }
        }
        
        Write-Host ""
        Write-Host "[3/3] Esperando a que el puerto se libere..." -ForegroundColor Yellow
        Start-Sleep -Seconds 2
        
        # Verificar si se liberó
        $verification = netstat -ano | Select-String ":$port"
        if ($verification) {
            Write-Host "[ADVERTENCIA] Puerto $port aún en uso" -ForegroundColor Yellow
            Write-Host $verification -ForegroundColor Gray
        } else {
            Write-Host "[EXITO] Puerto $port liberado correctamente" -ForegroundColor Green
        }
    }
} else {
    Write-Host "[INFO] Puerto $port ya está libre" -ForegroundColor Green
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Limpieza completada" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona Enter para cerrar..." -ForegroundColor Gray
$null = Read-Host
