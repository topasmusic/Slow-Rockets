# Slow Rockets — Work Log (Feb 10, 2026)

## Goal
Server‑side Fabric mod for MC 1.21.11 that **only** nerfs Elytra **firework boost**, leaving normal glide speed intact.
Target: ~80% slower boost (20% of vanilla effect) and a funny action‑bar message with cooldown.

## Current Status
Last code change: added **debug mode** logging and kept redirect on `PlayerMoveC2SPacket.getX/Y/Z` in `onPlayerMove`. Debug logs show boost ticks and movement scaling once per second per player when enabled. Built **1.0.7** jar. Local `runServer` test failed (connection refused), so user will test on Nitrado tomorrow.

## What Was Tried (and Why It Failed)
1. **LivingEntity mixin (shadow method)**  
   - Server crash: `@Shadow method method_18798 not found`. Removed.

2. **FireworkRocketEntity modify constants (1.5 / 0.1) + notify**  
   - Works in single‑player (client integrated), not on dedicated server.  
   - Likely server movement authoritative path not affected.

3. **PlayerEntity tick clamp**  
   - Server‑side clamp based on `SlowRocketsState` window.  
   - Did not reduce speed on dedicated server.

4. **ServerWorld tick clamp**  
   - Cap velocity after boost each tick (`player.setVelocity` + `velocityDirty = true`).  
   - No observed effect on dedicated server.

5. **ServerPlayNetworkHandler movement scaling — attempts**  
   - ModifyArg on `ServerPlayerEntity.move(...)` (Vec3d) — no effect.  
   - Redirect `updatePositionAndAngles(...)` — no effect.  
   - **Axis enum inside mixin** caused `IllegalClassLoadError`. Removed.

## Current Approach (NEW)
**ServerPlayNetworkHandlerMixin** redirects **all** calls to:
`PlayerMoveC2SPacket.getX/Y/Z(double)` inside `onPlayerMove` and scales the desired coordinates when boost is active.

Rationale:  
Server handles movement from these packet values. Scaling them at source should prevent full boost even if client predicts faster motion.

## Key Files
- `src/main/java/com/example/slowrockets/mixin/FireworkRocketEntityMixin.java`  
  - Scales rocket boost constants in `tick`.
  - Records boost activation in `SlowRocketsState` and sends fun messages.
- `src/main/java/com/example/slowrockets/SlowRocketsState.java`  
  - Tracks recent boost activation (`lastTick`) and base speed at first boost tick.
- `src/main/java/com/example/slowrockets/mixin/ServerPlayNetworkHandlerMixin.java`  
  - **Latest change:** redirects `PlayerMoveC2SPacket.getX/Y/Z` to scale movement during boost.
- `src/main/resources/slowrockets.mixins.json`  
  - Mixins list must include `FireworkRocketEntityMixin` + `ServerPlayNetworkHandlerMixin`.
- `gradle.properties`  
  - `mod_version` bumped each build.

## Versions / Builds
- 1.0.0 → base mod + messages  
- 1.0.1 → attempt: packet scaling (failed)  
- 1.0.2 → move(Vec3d) scaling (failed)  
- 1.0.3 → ServerWorld tick clamp (failed)  
- 1.0.4 → ModifyVariable scaling (failed + enum crash)  
- 1.0.5 → Enum removed (still failed)  
- **1.0.6** → Redirect getX/Y/Z in `onPlayerMove`
- **1.0.7** → Debug logs + redirect with log throttling

## Next Steps
1. Test **1.0.7** on Nitrado:
   - Replace server jar with `build/libs/slowrockets-1.0.7+1.21.11.jar`
   - Full server restart
2. Enable debug if possible:
   - JVM arg: `-Dslowrockets.debug=true`
   - or env var: `SLOWROCKETS_DEBUG=1`
3. Collect logs:
   - `[SlowRockets DEBUG] Boost tick: ...`
   - `[SlowRockets DEBUG] Move scale: ...`
4. If still no effect:
   - Build “always‑debug” jar (logging without flags)
   - Consider alternate hook if redirects are not firing

