# Gera um JAR executável simples (não modular) usando Manifest
param(
  [string]$MainClass = "br.com.gestao.Main",
  [string]$JarName = "app.jar"
)

$manifest = @(
  "Main-Class: $MainClass",
  "Class-Path: gson-2.10.1.jar",
  ""
) # linha em branco final obrigatória
$manifestPath = "manifest.mf"
$manifest | Set-Content -Encoding Ascii $manifestPath

if (Test-Path build) { Remove-Item -Recurse -Force build }
New-Item -ItemType Directory build | Out-Null

$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
Write-Host "[COMPILANDO]" ($files.Count) "arquivos..."
javac -encoding UTF-8 -classpath gson-2.10.1.jar -d build $files
if ($LASTEXITCODE -ne 0) { Write-Error "Falha na compilação"; exit 1 }

if (Test-Path $JarName) { Remove-Item $JarName }
jar cfm $JarName $manifestPath -C build .
Write-Host "[OK] JAR gerado: $JarName"
Write-Host "Execute com: java -jar $JarName"
