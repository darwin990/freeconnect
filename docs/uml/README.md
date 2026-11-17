# Diagramas UML

Esta carpeta reúne dos generaciones de diagramas:

1. **Mermaid (documentación de sistema)** — legibles directamente en GitHub/GitLab, versionables y sin binarios. Es la referencia para entender actores, dominio, secuencias, componentes, despliegue y estados.
2. **PlantUML (diagramas previos de arquitectura)** — se conservan como material histórico y se renderizan con la skill `uml-generator`.

## Diagramas Mermaid

| Archivo | Tipo | Pregunta que responde |
|---------|------|-----------------------|
| [`casos-de-uso.md`](./casos-de-uso.md) | `flowchart` | ¿Qué puede hacer cada rol y cómo se protege el acceso? Incluye la restricción de ADMIN sobre secciones de cliente. |
| [`clases-dominio.md`](./clases-dominio.md) | `classDiagram` | ¿Qué entidades existen, qué campos tienen y cómo se relacionan (Usuario, Proyecto, Propuesta, Contrato, Hito, Pago, Reseña, etc.)? |
| [`secuencia-flujo-principal.md`](./secuencia-flujo-principal.md) | `sequenceDiagram` ×3 | ¿Cómo fluye el ciclo E2E completo, desde el registro hasta el pago y la reseña? |
| [`secuencia-autenticacion.md`](./secuencia-autenticacion.md) | `sequenceDiagram` ×5 | ¿Cómo se registra, inicia sesión, refresca el token, cierra sesión y autoriza por rol? |
| [`secuencia-admin.md`](./secuencia-admin.md) | `sequenceDiagram` ×3 | ¿Cómo funcionan reportes, moderación, suspensión y auditoría bajo `hasRole('ADMIN')`? |
| [`componentes.md`](./componentes.md) | `flowchart` | ¿Qué contenedores y dependencias existen y cuáles están operativos (Redis 503, Docker sin verificar)? |
| [`despliegue.md`](./despliegue.md) | `flowchart` | ¿Cómo se despliega en local y cómo sería el objetivo con Docker + nginx? |
| [`estados-hito.md`](./estados-hito.md) | `stateDiagram-v2` ×3 | ¿Cuál es la máquina de estados del hito, del entregable y del contrato? Incluye la limitación de reenvío. |

### Convenciones

- Si un dato no se puede inferir del código, aparece como `%% TODO: confirmar`.
- Los endpoints y roles provienen de los controladores y de `SecurityConfig`, no de suposiciones.
- Los diagramas de secuencia usan `autonumber`, `alt`, `opt` y `loop` para reflejar errores y caminos alternativos.
- La información funcional detallada vive en [`frontend/README.md`](../../frontend/README.md); aquí no se duplica.

### Render

GitHub y GitLab renderizan Mermaid automáticamente. Para previsualizar en local:

```bash
# Opción 1: extensión Mermaid de VS Code
# Opción 2: CLI
npx @mermaid-js/mermaid-cli -i docs/uml/casos-de-uso.md -o casos-de-uso.png
```

> Los archivos `.png` y `.svg` de `docs/uml/` están ignorados por git para no versionar binarios.

## Diagramas PlantUML previos

| Archivo | Pregunta que responde |
|---------|-----------------------|
| `01-contexto-contenedores.puml` | ¿Qué actores, contenedores e infraestructura componen el sistema? |
| `02-modulos-dependencias.puml` | ¿Cómo se relacionan los módulos del monolito? |
| `03-dominio-contratos.puml` | ¿Cómo se modela el dominio de contratos y qué patrones lo sostienen? |
| `04-secuencia-aceptar-propuesta.puml` | ¿Cómo fluye RF-05/RF-06 desde HTTP hasta eventos y persistencia? |

### Render PlantUML

```powershell
# Validación estructural (sin dependencias)
powershell -ExecutionPolicy Bypass -File ../../.opencode/skills/uml-generator/scripts/validate-uml.ps1

# Validación + SVG con PlantUML
$env:PLANTUML_JAR = "C:\tools\plantuml.jar"
powershell -ExecutionPolicy Bypass -File ../../.opencode/skills/uml-generator/scripts/validate-uml.ps1 -Render
```

Los SVG se generan junto a cada `.puml` y están ignorados por git.
