# Guía de estilo PlantUML

Convenciones para todos los diagramas de `docs/uml`.

## Cabecera

```
@startuml
title <Modulo o caso de uso> - <Pregunta que responde>

skinparam shadowing false
skinparam monochrome true
' opcional: left to right direction
```

- Un título claro en español.
- `monochrome true` para que el diagrama se lea igual en impresión y en modo
  oscuro.
- `shadowing false` reduce ruido visual.

## Nombres

- Alias cortos y estables (`CT`, `Contract`, `EP`), no alias generados.
- Nombres de clases, métodos y endpoints **idénticos** al código.
- Paquetes con el nombre real (`contracts.domain`, `shared`).
- Evita acentos en alias y rutas; en títulos y notas sí se permiten.

## Colores y estereotipos

- No uses colores por defecto de PlantUML; el modo monocromo ya diferencia.
- Estereotipos permitidos: `<<value object>>`, `<<aggregate>>`,
  `<<adapter>>`, `<<port>>`, `<<enum>>`.
- Para enums usa `enum`, no una clase con constantes.

## Relaciones

| Relación | Uso |
|----------|-----|
| `-->` | dependencia directa / llamada |
| `..>` | dependencia débil, traducción o evento |
| `*--` | composición (el hijo no existe sin el padre) |
| `o--` | agregación (el hijo puede existir aparte) |
| `..|>` | implementación de interfaz |
| `--|>` | herencia de clase |

- Etiqueta las relaciones cuando el rol no sea obvio (`: hitos`,
  `: calcula comision`).
- No dibujes getters/setters; sí métodos de negocio.

## Diagramas de contexto

- Actores a la izquierda, contenedores en el centro, infraestructura a la
  derecha.
- Cada contenedor debe tener responsabilidad y tecnología.
- Una `legend` final aclara qué es simulado o externo.

## Diagramas de módulos

- Un paquete por módulo.
- Las dependencias hacia `shared` pueden agruparse para no saturar.
- Los eventos se dibujan como flechas discontinuas hacia `notifications` y
  `audit`, con una nota que recuerde que es Observer, no acoplamiento directo.

## Diagramas de clases

- Sólo el agregado y sus colaboradores directos.
- Incluye estados/interfaces que expliquen el patrón (State, Strategy,
  Command).
- Métodos con firma abreviada (`approve(UUID)`, `progress(): int`).
- Si un tipo genérico falla al renderizar, usa la forma `List~T~`.

## Diagramas de secuencia

- `autonumber` siempre.
- Participantes en orden: actor → controlador → caso de uso → dominio →
  repositorio → eventos.
- El primer mensaje es la petición HTTP real; el último, la respuesta con
  código.
- Usa `alt`/`opt` para reglas condicionales (por ejemplo, completar contrato
  cuando todos los hitos están aprobados).
- No muestres detalles de SQL; el repositorio es suficiente.

## Validación

```powershell
powershell -ExecutionPolicy Bypass -File .opencode/skills/uml-generator/scripts/validate-uml.ps1
```

Con PlantUML instalado:

```powershell
$env:PLANTUML_JAR = "C:\tools\plantuml.jar"
powershell -ExecutionPolicy Bypass -File .opencode/skills/uml-generator/scripts/validate-uml.ps1 -Render
```
