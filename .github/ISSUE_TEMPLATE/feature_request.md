---
name: Petición de Funcionalidad
about: Sugiere una nueva característica o mejora para la librería.
title: '[FEATURE] '
labels: enhancement
assignees: ''
---

## Contexto del Problema
¿Esta petición está relacionada con un problema específico o una limitación actual de la librería? Descríbelo de forma clara y concisa (ej. "Actualmente es muy repetitivo hacer X..." o "Me resulta frustrante no poder integrar Y...").

## Solución Propuesta
Describe claramente cómo debería funcionar la nueva característica o mejora. ¿Cuál es el comportamiento esperado o la arquitectura que sugieres?

## Casos de Uso
Describe en qué escenarios o arquitecturas (ej. microservicios, lambdas) esta funcionalidad sería útil. ¿Qué valor aporta al resto de desarrolladores que usan la librería?

## Alternativas Consideradas
Menciona otras soluciones, enfoques temporales (workarounds) u otras librerías que hayas evaluado o que estés utilizando actualmente para suplir esta necesidad.

## Diseño de API o Ejemplo de Uso (Recomendado)
Muestra cómo te gustaría que los desarrolladores interactúen con esta nueva funcionalidad en el código fuente.

```java
// Muestra la firma de los métodos o cómo se instanciaría
throw NewException.builder()
        .codigoError("ERR-123")
        .detalles("Datos adicionales")
        .build();