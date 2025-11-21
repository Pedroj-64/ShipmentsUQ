# Script para resetear las coordenadas de los repartidores
# Elimina el archivo de datos para que se regenere con las coordenadas correctas

Write-Host "🔄 Reseteando datos de repartidores..." -ForegroundColor Cyan

$dataFile = ".\data\app_state.dat"
$samedayDataFile = ".\ShipmentsUQ-SameDay\data\app_state.dat"

if (Test-Path $dataFile) {
    Remove-Item $dataFile -Force
    Write-Host "✅ Eliminado: $dataFile" -ForegroundColor Green
} else {
    Write-Host "⚠️  No existe: $dataFile" -ForegroundColor Yellow
}

if (Test-Path $samedayDataFile) {
    Remove-Item $samedayDataFile -Force
    Write-Host "✅ Eliminado: $samedayDataFile" -ForegroundColor Green
} else {
    Write-Host "⚠️  No existe: $samedayDataFile" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "✨ Datos reseteados. Al ejecutar la aplicación:" -ForegroundColor Green
Write-Host "   - Los repartidores se crearán con coordenadas correctas en Armenia" -ForegroundColor White
Write-Host "   - Juan Pérez: Grid (48, 52) → GPS (~4.63°N, -75.58°W)" -ForegroundColor White
Write-Host "   - Ana Gómez: Grid (52, 55) → GPS (~4.78°N, -75.58°W)" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  NOTA: Tendrás que crear nuevos envíos y direcciones" -ForegroundColor Yellow
Write-Host ""
