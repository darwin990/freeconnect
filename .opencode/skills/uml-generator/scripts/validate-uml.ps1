param(
    [string]$Path = "",
    [switch]$Render
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Path)) {
    $repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")
    $Path = Join-Path $repoRoot "docs\uml"
}

if (-not (Test-Path $Path)) {
    Write-Error "No existe la ruta de diagramas: $Path"
    exit 1
}

$files = @(Get-ChildItem -Path $Path -Filter *.puml -File -Recurse)
if ($files.Count -eq 0) {
    Write-Error "No se encontraron archivos .puml en $Path"
    exit 1
}

$errors = New-Object System.Collections.Generic.List[string]
$plantUmlJar = $env:PLANTUML_JAR

foreach ($file in $files) {
    $content = Get-Content -Raw -LiteralPath $file.FullName
    $relative = $file.FullName

    if ([string]::IsNullOrWhiteSpace($content)) {
        $errors.Add("$relative : archivo vacio")
        continue
    }

    $startCount = ([regex]::Matches($content, '(?m)^\s*@startuml\b')).Count
    $endCount = ([regex]::Matches($content, '(?m)^\s*@enduml\b')).Count
    if ($startCount -ne 1 -or $endCount -ne 1) {
        $errors.Add("$relative : se esperaba exactamente un @startuml y un @enduml (encontrados $startCount/$endCount)")
    }

    $trimmed = $content.TrimStart()
    if (-not $trimmed.StartsWith("@startuml")) {
        $errors.Add("$relative : el archivo debe comenzar con @startuml")
    }
    if (-not $content.TrimEnd().EndsWith("@enduml")) {
        $errors.Add("$relative : el archivo debe terminar con @enduml")
    }

    $open = ([regex]::Matches($content, '\{')).Count
    $close = ([regex]::Matches($content, '\}')).Count
    if ($open -ne $close) {
        $errors.Add("$relative : llaves desbalanceadas ($open abiertas, $close cerradas)")
    }

    if ($content -match '!include\s+https?://') {
        $errors.Add("$relative : no se permiten includes remotos")
    }

    if (-not [string]::IsNullOrWhiteSpace($plantUmlJar) -and (Test-Path $plantUmlJar)) {
        $javaArgs = @("-jar", $plantUmlJar, "-checkonly", $file.FullName)
        & java @javaArgs
        if ($LASTEXITCODE -ne 0) {
            $errors.Add("$relative : PlantUML reporto errores de sintaxis")
            continue
        }
        if ($Render) {
            $javaArgs = @("-jar", $plantUmlJar, "-tsvg", $file.FullName)
            & java @javaArgs
            if ($LASTEXITCODE -ne 0) {
                $errors.Add("$relative : no se pudo renderizar el SVG")
            }
        }
    }
    elseif ($Render) {
        Write-Warning "PLANTUML_JAR no esta definido; se omite el render de $($file.Name)"
    }
}

if ($errors.Count -gt 0) {
    Write-Host ""
    Write-Host "Errores encontrados:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
}

Write-Host "OK: $($files.Count) diagramas validados en $Path" -ForegroundColor Green
