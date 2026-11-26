# Chunk Spiral

Client-side Fabric mod for Minecraft Java 26.3. Flies you to the height limit, then spirals outward
forever so every chunk beneath you loads onto your map (Xaero's, JourneyMap, etc.).

- **J** opens the menu (also reachable from Mod Menu), **K** starts/stops.
- Modes: Creative flight, or Elytra + fireworks (start gliding first, keep rockets in your hotbar).
- Ring gap defaults to your render distance so rings overlap slightly. Max radius 0 = unlimited.
- Safety stops: damage, stuck, out of fireworks, flight lost, disconnect.

## Build
Push to GitHub, open the **Actions** tab, and download the jar from the finished run's artifacts.
Locally: install Java 25 + Gradle 9, run `gradle build`; jar is in `build/libs/`.

If the build complains about a missing version, copy the right values for 26.3 from
https://fabricmc.net/develop/ into `gradle.properties`.
