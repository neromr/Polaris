# La Bendicion de Nanook (Forge 1.20.1)

Mod que anade domesticacion de osos polares vanilla. Contenido:

- Domesticar osos polares con bacalao crudo: **5% de probabilidad** por
  bacalao (el loro, el mob mas dificil de domesticar en vanilla, tiene
  10%; esto es el doble de dificil).
- Logro oculto **"La Bendicion Nanook"** al domesticar el primer oso.
- Receta oculta de la **Lanza de Nanook** (mas daño que netherite),
  desbloqueada automaticamente por el logro.
- Bendicion permanente: ningun oso polar volvera a atacarte (ni cerca
  de una cria, ni si tu lo atacas primero) una vez la obtienes.
- El oso domesticado te sigue, te defiende de quien te ataque, ataca lo
  que tu ataques, y puedes ordenarle Seguir / Quedarse quieto / Deambular
  (agachado + click).
- Se puede montar entre el dueno y un segundo jugador.

## Compilacion automatica con GitHub Actions

Este repositorio ya viene listo para compilarse solo. No hace falta que
instales nada en tu maquina.

1. Sube TODO este contenido a un repositorio nuevo en GitHub (puede ser
   arrastrando los archivos en la web de GitHub, subiendolos con GitHub
   Desktop, o con `git push` si usas la terminal).
2. En cuanto el push termine, ve a la pestana **Actions** de tu
   repositorio. Va a aparecer un workflow llamado "Build" corriendo
   automaticamente (tarda entre 5 y 10 minutos la primera vez, porque
   Gradle tiene que descargar y descomprimir Minecraft).
3. Cuando termine (icono verde ✅), entra a esa ejecucion y baja hasta
   **Artifacts**. Ahi vas a encontrar `nanookmod.zip`, que contiene el
   `.jar` del mod.
4. Descarga ese zip, sacale el `.jar` de adentro, y ponlo en la carpeta
   `mods` de tu instalacion de Forge 1.20.1.

Si el icono sale rojo ❌ (fallo), entra a la ejecucion y abre el paso
"Compilar el mod" para ver el error exacto — normalmente son cosas
puntuales (ver la seccion de abajo).

Tambien podes lanzar la compilacion a mano sin hacer push: pestana
**Actions** → "Build" (menu izquierdo) → botón **Run workflow**.

## Arquitectura

- `NanookBearData`: guarda si el oso esta domesticado, quien es el
  dueno y su orden actual (Seguir/Quieto/Deambular) usando
  `getPersistentData()` (el NBT propio de cada entidad, ya se guarda
  solo). La Bendicion del jugador se guarda igual, en el jugador.
- `GoalUtil` + `NanookBearGoals`: como PolarBear ya existe en vanilla y
  no se puede heredar de una entidad ya registrada, usamos reflexion
  (`ObfuscationReflectionHelper`) para acceder a los `goalSelector` /
  `targetSelector` protegidos de `Mob`, quitamos solo los goals internos
  de PolarBear (panico, atacar jugadores cerca de una cria) sin tocar
  los genericos compartidos (`MeleeAttackGoal`, mirar al jugador...), y
  anadimos los nuestros (quedarse quieto, seguir, deambular, defender,
  asistir).
- `NanookEventHandler`: un unico manejador con la interaccion (domesticar
  / alimentar / ordenar / montar), la reaplicacion de IA al cargar chunks,
  el bloqueo de ataques via `LivingChangeTargetEvent` (la Bendicion), y el
  movimiento al ser montado.

No se usa Mixin en ningun lado, a proposito: todo el mod se apoya en
reflexion + eventos normales de Forge para que `gradle build` funcione
sin plugins ni configuracion extra.

## Sobre el montado (la parte mas experimental)

PolarBear no es montable en vanilla, asi que no responde al WASD del
jinete por si sola. Lo resolvi con eventos puros de Forge:

- El **dueno** monta con el sistema de pasajero normal (`startRiding`).
- Cada tick, `onBearTick` lee `rider.zza` / `rider.xxa` (sincronizados
  del cliente al servidor por el propio juego mientras el jugador esta
  montado) y mueve al oso a mano con `bear.move(...)`.
- El **segundo jinete** monta "sobre" el dueno (que a su vez monta al
  oso), en vez de ser un segundo pasajero real del oso. Es la parte
  menos probada: el punto de asiento exacto del segundo jugador depende
  de la logica por defecto de Entity y puede verse un poco descolocado.

Ajusta la velocidad (`0.32` en `onBearTick`) si hace falta despues de
probarlo en el juego.

## Si el build de GitHub Actions falla

- **"could not resolve plugin net.minecraftforge.gradle"**: reintenta
  (`Re-run jobs`); a veces el maven de Forge tarda en responder.
- **Error de reflexion sobre "goalSelector" o "targetSelector" al
  arrancar el juego** (esto pasaria jugando, no en el build): significa
  que esos nombres de campo cambiaron en tus mappings. Abre
  `GoalUtil.java` y ajusta el string.
- **Falta memoria durante el build**: subi `org.gradle.jvmargs` en
  `gradle.properties` (por ejemplo a `-Xmx4G`); los runners gratuitos de
  GitHub Actions traen 7 GB de RAM asi que no deberia hacer falta.

## Otros detalles a revisar

- La textura `assets/nanookmod/textures/item/nanook_spear.png` es un
  marcador de posicion generado por codigo (16x16, silueta simple).
  Sustituyela por arte real cuando quieras (no rompe el build dejarla).
- El icono del logro usa `minecraft:polar_bear_spawn_egg`; cambialo si
  quieres un item propio.
- `forge_version=47.4.10` en `gradle.properties` es la build
  "Recommended" de Forge para 1.20.1 en este momento. Si en el futuro
  sale una mas nueva y quieres actualizar, solo cambia ese numero.
