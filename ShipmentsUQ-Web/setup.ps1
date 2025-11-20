# 🚀 Configurando Frontend de ShipmentsUQ Web...

Write-Host "🚀 Configurando Frontend de ShipmentsUQ Web..." -ForegroundColor Cyan
Write-Host ""

# Verificar Node.js
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js $nodeVersion encontrado" -ForegroundColor Green
    
    $npmVersion = npm --version
    Write-Host "✅ npm $npmVersion encontrado" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Node.js no está instalado. Por favor instala Node.js 18+ desde https://nodejs.org/" -ForegroundColor Red
    exit 1
}

# Crear proyecto React con Vite
Write-Host "📦 Creando proyecto React con Vite..." -ForegroundColor Yellow
Set-Location frontend
npm create vite@latest . -- --template react-ts

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al crear el proyecto con Vite" -ForegroundColor Red
    exit 1
}

# Instalar dependencias principales
Write-Host "📦 Instalando dependencias..." -ForegroundColor Yellow
npm install

# Instalar bibliotecas adicionales
Write-Host "📦 Instalando bibliotecas adicionales..." -ForegroundColor Yellow
npm install axios react-router-dom
npm install -D tailwindcss postcss autoprefixer
npm install @headlessui/react @heroicons/react lucide-react

# Inicializar Tailwind CSS
Write-Host "🎨 Configurando Tailwind CSS..." -ForegroundColor Yellow
npx tailwindcss init -p

Write-Host ""
Write-Host "✅ ¡Configuración completada!" -ForegroundColor Green
Write-Host ""
Write-Host "Para iniciar el servidor de desarrollo:" -ForegroundColor Cyan
Write-Host "  cd frontend" -ForegroundColor White
Write-Host "  npm run dev" -ForegroundColor White
Write-Host ""
Write-Host "Para construir para producción:" -ForegroundColor Cyan
Write-Host "  cd frontend" -ForegroundColor White
Write-Host "  npm run build" -ForegroundColor White
Write-Host ""
Write-Host "El frontend estará disponible en: http://localhost:5173" -ForegroundColor Magenta
