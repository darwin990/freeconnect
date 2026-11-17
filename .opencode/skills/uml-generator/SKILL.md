---
name: UML Generator
description: Genera, valida y actualiza diagramas UML en PlantUML (contexto, modulos, clases y secuencia) a partir del codigo real del repositorio.
---

# UML Generator

Usa esta habilidad cuando se pida crear, actualizar o revisar diagramas UML de
un proyecto, o cuando haya que documentar arquitectura, dominio o flujos con
PlantUML.

## Principios

1. **El codigo es la fuente de verdad.** No inventes clases, endpoints, módulos
   ni relaciones. Antes de dibujar, lee el código y cita mentalmente el archivo
   que respalda cada elemento.
2. **Un diagrama, una pregunta.** Contexto responde "¿qué contenedores hay?";
   módulos, "¿quién depende de quién?"; clases, "¿cómo se modela el dominio?";
   secuencia, "¿cómo fluye un caso de uso?".
3. **Legibilidad antes que exhaustividad.** Si un diagrama tiene más de ~25
   elementos, divídelo.
4. **Nada de includes externos.** Los `.puml` deben renderizar sin red ni
   dependencias descargadas.

## Flujo de trabajo

1. **Contexto.** Lee `AGENT.md`, `docs/ARQUITECTURA.md`, `docs/PATRONES.md` y la
   estructura de paquetes del proyecto.
2. **Inventario.** Localiza controladores, casos de uso, agregados, puertos y
   adaptadores reales. Para secuencias, identifica el endpoint y sigue la cadena
   controlador → caso de uso → dominio → repositorio → eventos.
3. **Tipo de diagrama.** Elige uno de:
   - Contexto/contenedores (C4 simplificado).
   - Módulos y dependencias.
   - Clases de un agregado o patrón.
   - Secuencia de un caso de uso.
4. **Estilo.** Lee `references/plantuml-style.md` y respétalo.
5. **Ubicación.** Guarda en `docs/uml/` con el patrón
   `NN-nombre-descriptivo.puml` (`01-...`, `02-...`).
6. **Validación.** Ejecuta:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .opencode/skills/uml-generator/scripts/validate-uml.ps1
   ```
   Si defines `PLANTUML_JAR`, el script además hace `-checkonly` con PlantUML.
7. **Render opcional.** Con `PLANTUML_JAR`:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .opencode/skills/uml-generator/scripts/validate-uml.ps1 -Render
   ```
   Genera SVG junto a cada `.puml`. No publiques PNG/SVG si el repositorio los
   ignora.
8. **Enlaces.** Si agregas un diagrama nuevo, enlázalo desde el README o la
   documentación de arquitectura.

## Reglas de contenido

- Nombres de clases, métodos y endpoints **exactos** como en el código.
- En secuencias, el primer mensaje debe ser la petición HTTP real y el último la
  respuesta (código incluido).
- En clases, marca interfaces con `interface`, enums con `enum` y value objects
  con `<<value object>>`.
- En módulos, las flechas discontinuas representan dependencias débiles o
  eventos; las continuas, dependencias directas.
- Añade `legend` o `note` sólo cuando aclaren una decisión; no para repetir el
  título.
- Español para títulos y notas; inglés para identificadores técnicos.

## Plantillas

- `templates/component.puml`
- `templates/class.puml`
- `templates/sequence.puml`

Copia la plantilla y sustitúyela por elementos reales del proyecto.

## Validación mínima (sin PlantUML instalado)

El script comprueba:

- Que el archivo empiece por `@startuml` y termine en `@enduml`.
- Que no haya bloques `@startuml`/`@enduml` desbalanceados.
- Que las llaves `{` y `}` estén balanceadas.
- Que no se usen `!include` remotos.
- Que el archivo no esté vacío.

Si hay PlantUML disponible (`PLANTUML_JAR` o `plantuml` en el PATH), ejecuta
`-checkonly` para una validación sintáctica real.

## Errores comunes

| Síntoma | Causa | Solución |
|---------|-------|----------|
| Flechas cruzadas ilegibles | Demasiados elementos | Dividir el diagrama o usar `left to right direction` |
| Error de sintaxis en clase | Genéricos o llaves sin escapar | Simplificar firmas; usar `List~T~` si el render falla |
| Diagrama no renderiza | `!include` externo o tema remoto | Eliminar includes; usar `skinparam` locales |
| Secuencia incorrecta | Se saltó la capa de aplicación | Volver a trazar controlador → use case → dominio |
