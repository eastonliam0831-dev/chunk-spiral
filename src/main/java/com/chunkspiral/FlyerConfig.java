package com.chunkspiral;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FlyerConfig {
    public enum Mode { CREATIVE, ELYTRA }

    public Mode mode = Mode.CREATIVE;
    /** 0 = world height limit. */
    public int height = 0;
    public boolean autoSpacing = true;
    /** Blocks between rings when autoSpacing is off. */
    public int spacing = 256;
    public int startRadius = 64;
    /** 0 = unlimited, rings keep growing. */
    public int maxRadius = 0;
    /** Blocks per tick (creative mode). */
    public double speed = 1.0;
    public boolean clockwise = true;
    public boolean stopOnDamage = true;
    public boolean showStatus = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FlyerConfig instance;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("chunkspiral.json");
    }

    public static FlyerConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        FlyerConfig loaded = null;
        try {
            Path f = file();
            if (Files.exists(f)) loaded = GSON.fromJson(Files.readString(f), FlyerConfig.class);
        } catch (Exception ignored) { }
        instance = loaded != null ? loaded : new FlyerConfig();
        instance.sanitize();
    }

    public static void save() {
        try {
            Files.writeString(file(), GSON.toJson(get()));
        } catch (IOException ignored) { }
    }

    private void sanitize() {
        if (mode == null) mode = Mode.CREATIVE;
        height = Math.max(0, height);
        spacing = Math.max(16, spacing);
        startRadius = Math.max(8, startRadius);
        maxRadius = Math.max(0, maxRadius);
        speed = Math.max(0.2, Math.min(3.0, speed));
    }
}
