package com.chunkspiral;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public class IntSlider extends AbstractSliderButton {
    private String label;
    private int min, max, step;
    private IntConsumer setter;
    private IntFunction<String> fmt;

    public IntSlider(int x, int y, int w, int h, String label, int min, int max, int step,
                     int current, IntConsumer setter, IntFunction<String> fmt) {
        super(x, y, w, h, Component.empty(),
                Math.max(0.0, Math.min(1.0, (double) (current - min) / (max - min))));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.setter = setter;
        this.fmt = fmt;
        updateMessage();
    }

    private int current() {
        int v = min + (int) Math.round(this.value * (max - min) / step) * step;
        return Math.max(min, Math.min(max, v));
    }

    @Override
    protected void updateMessage() {
        if (label == null) return;
        setMessage(Component.literal(label + ": " + fmt.apply(current())));
    }

    @Override
    protected void applyValue() {
        setter.accept(current());
    }
}
