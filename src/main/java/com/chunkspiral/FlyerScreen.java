package com.chunkspiral;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FlyerScreen extends Screen {
    private final Screen parent;
    private StringWidget status;

    public FlyerScreen(Screen parent) {
        super(Component.literal("Chunk Spiral"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean v) {
        return Component.literal(name + ": ")
                .append(Component.literal(v ? "ON" : "OFF").withStyle(v ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static Component statusText() {
        return Component.literal(SpiralFlyer.statusLine());
    }

    @Override
    protected void init() {
        FlyerConfig c = FlyerConfig.get();
        int colW = 150, gap = 8, total = colW * 2 + gap;
        int left = this.width / 2 - total / 2;
        int right = left + colW + gap;
        int y = this.height / 2 - 105;

        StringWidget title = new StringWidget(left, y, total, 12,
                Component.literal("\u2708 CHUNK SPIRAL \u2708").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), this.font);
        center(title);
        addRenderableWidget(title);
        y += 14;

        status = new StringWidget(left, y, total, 10, statusText(), this.font);
        center(status);
        addRenderableWidget(status);
        y += 16;

        // Start / stop
        boolean running = SpiralFlyer.isRunning();
        addRenderableWidget(Button.builder(
                Component.literal(running ? "\u25a0 STOP SPIRAL" : "\u25b6 START SPIRAL")
                        .withStyle(running ? ChatFormatting.RED : ChatFormatting.GREEN, ChatFormatting.BOLD),
                b -> {
                    FlyerConfig.save();
                    var mc = this.minecraft;
                    if (SpiralFlyer.isRunning()) SpiralFlyer.stop(mc, "\u00a7eChunk Spiral stopped.");
                    else SpiralFlyer.start(mc);
                    onClose();
                }).bounds(left, y, total, 22).build());
        y += 28;

        // Row 1: mode | speed
        addRenderableWidget(Button.builder(modeText(c), b -> {
            c.mode = c.mode == FlyerConfig.Mode.CREATIVE ? FlyerConfig.Mode.ELYTRA : FlyerConfig.Mode.CREATIVE;
            b.setMessage(modeText(c));
        }).bounds(left, y, colW, 20).build());
        addRenderableWidget(new IntSlider(right, y, colW, 20, "Speed (creative)", 3, 30, 1,
                (int) Math.round(c.speed * 10), v -> c.speed = v / 10.0, v -> (v * 2) + " b/s"));
        y += 24;

        // Row 2: height | direction
        addRenderableWidget(new IntSlider(left, y, colW, 20, "Height", 0, 640, 8, c.height,
                v -> c.height = v, v -> v == 0 ? "World max" : String.valueOf(v)));
        addRenderableWidget(Button.builder(dirText(c), b -> {
            c.clockwise = !c.clockwise;
            b.setMessage(dirText(c));
        }).bounds(right, y, colW, 20).build());
        y += 24;

        // Row 3: auto spacing | spacing
        IntSlider spacing = new IntSlider(right, y, colW, 20, "Ring gap", 32, 1024, 16, c.spacing,
                v -> c.spacing = v, v -> v + " blocks");
        spacing.active = !c.autoSpacing;
        addRenderableWidget(Button.builder(onOff("Auto ring gap", c.autoSpacing), b -> {
            c.autoSpacing = !c.autoSpacing;
            b.setMessage(onOff("Auto ring gap", c.autoSpacing));
            spacing.active = !c.autoSpacing;
        }).bounds(left, y, colW, 20).build());
        addRenderableWidget(spacing);
        y += 24;

        // Row 4: start radius | max radius
        addRenderableWidget(new IntSlider(left, y, colW, 20, "Start radius", 16, 1024, 16, c.startRadius,
                v -> c.startRadius = v, v -> v + " blocks"));
        addRenderableWidget(new IntSlider(right, y, colW, 20, "Max radius", 0, 100000, 1000, c.maxRadius,
                v -> c.maxRadius = v, v -> v == 0 ? "Unlimited" : v + " blocks"));
        y += 24;

        // Row 5: toggles
        addRenderableWidget(Button.builder(onOff("Stop on damage", c.stopOnDamage), b -> {
            c.stopOnDamage = !c.stopOnDamage;
            b.setMessage(onOff("Stop on damage", c.stopOnDamage));
        }).bounds(left, y, colW, 20).build());
        addRenderableWidget(Button.builder(onOff("Status bar", c.showStatus), b -> {
            c.showStatus = !c.showStatus;
            b.setMessage(onOff("Status bar", c.showStatus));
        }).bounds(right, y, colW, 20).build());
        y += 28;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left, y, total, 20).build());
    }

    private void center(StringWidget w) {
        int mid = this.width / 2;
        w.setWidth(this.font.width(w.getMessage()));
        w.setX(mid - w.getWidth() / 2);
    }

    private static Component modeText(FlyerConfig c) {
        return Component.literal("Mode: ").append(Component.literal(
                c.mode == FlyerConfig.Mode.CREATIVE ? "Creative flight" : "Elytra + fireworks")
                .withStyle(ChatFormatting.YELLOW));
    }

    private static Component dirText(FlyerConfig c) {
        return Component.literal("Direction: " + (c.clockwise ? "Clockwise" : "Counter-clockwise"));
    }

    @Override
    public void tick() {
        super.tick();
        if (status != null) { status.setMessage(statusText()); center(status); }
    }

    @Override
    public void onClose() {
        FlyerConfig.save();
        this.minecraft.gui.setScreen(parent);
    }
}
