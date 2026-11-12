package ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion;

import net.minecraft.class_2960;
import ru.method.clarityvisuals.ClarityVisuals;

/**
 * Catalog of emote wheels migrated from auravisual ("auraApi" -> "emotions").
 * Each emotion maps to an icon under <modid>:emotions/<name>.png (fragment of the wheel),
 * a displayed label and the animation duration on the model.
 */
public enum EmotionType {
    DEB("deb", "\u0414\u0435\u0431", 2500L),
    FLOSS("floss", "\u0424\u043b\u043e\u0441\u0441", 4500L),
    MASTURBATE("masturbate", "\u042d\u043c\u043e\u0446\u0438\u044f", 3500L),
    HELLO("hello", "\u041f\u0440\u0438\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0435", 2500L),
    GET_GRIDDY("get_griddy", "Get Griddy", 5500L),
    HAPPY("happy", "\u0420\u0430\u0434\u043e\u0441\u0442\u044c", 2500L);

    private final String name;
    private final String title;
    private final long durationMs;
    private final class_2960 texture;

    EmotionType(String string, String string2, long l) {
        this.name = string;
        this.title = string2;
        this.durationMs = l;
        this.texture = ClarityVisuals.id("emotions/" + string + ".png");
    }

    public String title() {
        return this.title;
    }

    public long durationMs() {
        return this.durationMs;
    }

    public class_2960 texture() {
        return this.texture;
    }

    public String shortLabel() {
        return this.name.length() <= 2 ? this.name.toUpperCase() : this.name.substring(0, 2).toUpperCase();
    }
}
