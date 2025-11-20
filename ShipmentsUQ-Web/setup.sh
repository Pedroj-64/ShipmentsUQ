#!/bin/bash

echo "🚀 Configurando Frontend de ShipmentsUQ Web..."
echo ""

# Verificar Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js no está instalado. Por favor instala Node.js 18+ desde https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js $(node --version) encontrado"
echo "✅ npm $(npm --version) encontrado"
echo ""

# Crear proyecto React con Vite
echo "📦 Creando proyecto React con Vite..."
cd frontend
npm create vite@latest . -- --template react-ts

# Instalar dependencias principales
echo "📦 Instalando dependencias..."
npm install

# Instalar bibliotecas adicionales
echo "📦 Instalando bibliotecas adicionales..."
npm install axios react-router-dom
npm install -D tailwindcss postcss autoprefixer
npm install @headlessui/react @heroicons/react

# Inicializar Tailwind CSS
echo "🎨 Configurando Tailwind CSS..."
npx tailwindcss init -p

echo ""
echo "✅ ¡Configuración completada!"
echo ""
echo "Para iniciar el servidor de desarrollo:"
echo "  cd frontend"
echo "  npm run dev"
echo ""
echo "Para construir para producción:"
echo "  cd frontend"
echo "  npm run build"
