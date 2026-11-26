package com.chunkspiral;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class SpiralFlyer {
    public enum Phase { IDLE, CLIMB, SPIRAL }

    private static final double TAU = Math.PI * 2.0;

    public static Phase phase = Phase.IDLE;
    private static double cx, cz, theta, targetY, curRadius;
    private static float lastHealth;
    private static int tickCount, stuckTimer, rocketCooldown;
    private static Vec3 stuckPos;

    private SpiralFlyer() { }

    public static boolean isRunning() { return phase != Phase.IDLE; }

    public static void reset() { phase = Phase.IDLE; }

    public static void toggle(Minecraft mc) {
        if (isRunning()) stop(mc, "\u00a7eChunk Spiral stopped.");
        else start(mc);
    }

    public static void start(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        FlyerConfig c = FlyerConfig.get();

        if (c.mode == FlyerConfig.Mode.CREATIVE && !p.getAbilities().mayfly) {
            msg(p, "\u00a7cCreative flight isn't available here. Switch to Elytra mode in the menu.");
            return;
        }
        if (c.mode == FlyerConfig.Mode.ELYTRA && !p.isFallFlying()) {
            msg(p, "\u00a7cStart gliding with your elytra first, then press the key again.");
            return;
        }

        int maxY = mc.level.getMaxY();
        targetY = c.height > 0 ? Math.min(c.height, maxY) : maxY;
        theta = 0;
        curRadius = c.startRadius;
        tickCount = 0;
        stuckTimer = 0;
        rocketCooldown = 0;
        stuckPos = p.position();
        lastHealth = p.getHealth();
        phase = p.getY() >= targetY - 1.0 ? Phase.SPIRAL : Phase.CLIMB;
        if (phase == Phase.SPIRAL) { cx = p.getX(); cz = p.getZ(); }
        msg(p, "\u00a7bChunk Spiral started \u00a77(target height " + (int) targetY + ")");
    }

    public static void stop(Minecraft mc, String message) {
        phase = Phase.IDLE;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (FlyerConfig.get().mode == FlyerConfig.Mode.CREATIVE) p.setDeltaMovement(Vec3.ZERO);
        if (message != null) msg(p, message);
    }

    private static void msg(LocalPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), false);
    }

    public static String statusLine() {
        if (!isRunning()) return "\u00a77Idle";
        if (phase == Phase.CLIMB) return "\u00a7eClimbing to height limit...";
        return "\u00a7aRing " + (int) (theta / TAU + 1) + " \u00a77| \u00a7fradius " + (int) curRadius + " blocks";
    }

    private static double spacing(Minecraft mc, FlyerConfig c) {
        if (!c.autoSpacing) return c.spacing;
        int rd = Math.max(2, mc.options.getEffectiveRenderDistance());
        return Math.max(32, (rd * 2 - 2) * 16);
    }

    public static void tick(Minecraft mc) {
        if (phase == Phase.IDLE) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || !p.isAlive()) { reset(); return; }
        FlyerConfig c = FlyerConfig.get();
        boolean creative = c.mode == FlyerConfig.Mode.CREATIVE;
        tickCount++;
        if (rocketCooldown > 0) rocketCooldown--;

        if (c.stopOnDamage && p.getHealth() < lastHealth - 0.01f) {
            stop(mc, "\u00a7cTook damage - Chunk Spiral stopped.");
            return;
        }
        lastHealth = p.getHealth();

        if (++stuckTimer >= 60) {
            if (stuckPos != null && p.position().distanceTo(stuckPos) < 3.0) {
                stop(mc, "\u00a7cLooks like you're stuck - Chunk Spiral stopped.");
                return;
            }
            stuckPos = p.position();
            stuckTimer = 0;
        }

        if (creative) {
            if (!p.getAbilities().mayfly) { stop(mc, "\u00a7cFlight lost - stopped."); return; }
            if (!p.getAbilities().flying) {
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
            }
        } else if (!p.isFallFlying()) {
            stop(mc, "\u00a7cGliding ended - Chunk Spiral stopped.");
            return;
        }

        if (phase == Phase.CLIMB) {
            if (p.getY() >= targetY - 1.0) {
                cx = p.getX();
                cz = p.getZ();
                phase = Phase.SPIRAL;
            } else if (creative) {
                p.setDeltaMovement(0, Math.min(c.speed, targetY - p.getY()), 0);
            } else {
                p.setXRot(-55f);
                if (!rocket(mc, p)) return;
            }
            status(p, c);
            return;
        }

        // ---- SPIRAL ----
        double sp = spacing(mc, c);
        curRadius = c.startRadius + sp * theta / TAU;
        if (c.maxRadius > 0 && curRadius > c.maxRadius) {
            stop(mc, "\u00a7aReached max radius - Chunk Spiral finished.");
            return;
        }

        double dir = c.clockwise ? 1.0 : -1.0;
        double tx = cx + curRadius * Math.cos(dir * theta);
        double tz = cz + curRadius * Math.sin(dir * theta);
        double px = p.getX(), pz = p.getZ();
        double dx = tx - px, dz = tz - pz;
        double dist = Math.hypot(dx, dz);

        Vec3 vel = p.getDeltaMovement();
        double hSpeed = Math.hypot(vel.x, vel.z);
        double look = creative ? Math.max(8.0, c.speed * 10.0) : 40.0;
        double v = creative ? c.speed : Math.max(0.8, hSpeed);
        // "Carrot on a stick": the target only moves ahead while you're close to it.
        if (dist < look) theta += 1.3 * v / Math.max(curRadius, 1.0);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        if (creative) {
            double vx = dist > 0.01 ? dx / dist * c.speed : 0;
            double vz = dist > 0.01 ? dz / dist * c.speed : 0;
            double vy = Math.max(-0.5, Math.min(0.5, (targetY - p.getY()) * 0.3));
            p.setDeltaMovement(vx, vy, vz);
            p.setYRot(yaw);
            p.setXRot(10f);
        } else {
            double dy = targetY - p.getY();
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(dist, 1.0)));
            pitch = Math.max(-45f, Math.min(30f, pitch));
            p.setYRot(yaw);
            p.setXRot(pitch);
            if (!rocket(mc, p)) return;
        }
        status(p, c);
    }

    /** Uses a firework when speed is low. Returns false if the run was stopped. */
    private static boolean rocket(Minecraft mc, LocalPlayer p) {
        double spd = p.getDeltaMovement().length();
        if (spd >= 1.1 || rocketCooldown > 0) return true;

        InteractionHand hand = null;
        if (p.getOffhandItem().is(Items.FIREWORK_ROCKET)) hand = InteractionHand.OFF_HAND;
        else if (p.getMainHandItem().is(Items.FIREWORK_ROCKET)) hand = InteractionHand.MAIN_HAND;
        else {
            for (int i = 0; i < 9; i++) {
                if (p.getInventory().getItem(i).is(Items.FIREWORK_ROCKET)) {
                    p.getInventory().setSelectedSlot(i);
                    hand = InteractionHand.MAIN_HAND;
                    break;
                }
            }
        }
        if (hand == null) {
            if (spd < 0.6) {
                stop(mc, "\u00a7cOut of fireworks - Chunk Spiral stopped.");
                return false;
            }
            return true;
        }
        if (mc.gameMode != null) mc.gameMode.useItem(p, hand);
        rocketCooldown = 25;
        return true;
    }

    private static void status(LocalPlayer p, FlyerConfig c) {
        if (c.showStatus && tickCount % 10 == 0) {
            p.displayClientMessage(Component.literal("\u00a7b\u00a7lChunk Spiral \u00a78| " + statusLine()
                    + " \u00a78| \u00a7fY " + (int) p.getY()), true);
        }
    }
}
