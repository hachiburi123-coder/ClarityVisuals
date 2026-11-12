package ru.method.clarityvisuals.modules.impl.render.killeffect;

import ru.method.clarityvisuals.api.setting.NamedOption;

/**
 * Catalogue of the kill effects exposed to the settings selector. Entries keep
 * their original pack ids where they had one; the procedural ones use their own.
 */
public final class KillEffectCatalog {
    private KillEffectCatalog() {
    }

    public enum Effect implements NamedOption {
        TETRIS_SMASH("tertis_smash", "Tetris Smash", 3.6f, 1.0f, 0x59C3FF, 0xFF5C5C, 0xFFD23F),
        BODY_BLOOM("body_bloom", "Body Bloom", 3.8f, 1.0f, 0xFF8AD8, 0x6C5BFF, 0x8AF5FF);

        private final String id;
        private final String displayName;
        private final float duration;
        private final float scale;
        private final int primary;
        private final int secondary;
        private final int accent;

        Effect(String id, String displayName, float duration, float scale, int primary, int secondary, int accent) {
            this.id = id;
            this.displayName = displayName;
            this.duration = duration;
            this.scale = scale;
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
        }

        @Override
        public String getName() {
            return "settings.killeffect.mode." + this.id;
        }

        public String id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public float duration() {
            return this.duration;
        }

        public float scale() {
            return this.scale;
        }

        public int primary() {
            return this.primary;
        }

        public int secondary() {
            return this.secondary;
        }

        public int accent() {
            return this.accent;
        }
    }

    public static Effect byId(String id) {
        if (id == null) {
            return null;
        }
        for (Effect effect : Effect.values()) {
            if (effect.id().equalsIgnoreCase(id)) {
                return effect;
            }
        }
        return null;
    }
}
