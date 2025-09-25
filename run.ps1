# Executa compilação incremental e roda a aplicação
param(
    [switch]$Clean,
    [string]$MainClass = "br.com.gestao.Main"
)

if ($Clean -and (Test-Path out)) { Write-Host "[CLEAN] Removendo pasta out"; Remove-Item -Recurse -Force out }
if (!(Test-Path out)) { New-Item -ItemType Directory out | Out-Null }

$cp = "gson-2.10.1.jar"
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }

Write-Host "[COMPILANDO]" ($files.Count) "arquivos..."
javac -encoding UTF-8 -classpath $cp -d out $files
if ($LASTEXITCODE -ne 0) { Write-Error "Falha na compilação"; exit 1 }

Write-Host "[EXECUTANDO] $MainClass"
java -cp "out;$cp" $MainClass
