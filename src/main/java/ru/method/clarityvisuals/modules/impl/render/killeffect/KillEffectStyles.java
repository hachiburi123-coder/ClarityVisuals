package ru.method.clarityvisuals.modules.impl.render.killeffect;

/** Routes a catalogue entry to the matching procedural effect. */
final class KillEffectStyles {
    private KillEffectStyles() {
    }

    static void render(KillEffectRenderer.Ctx c) {
        switch (c.effect) {
            case TETRIS_SMASH -> KillEffectStylesA.tetris(c);
            case BODY_BLOOM -> KillEffectStylesB.bodyBloom(c);
        }
    }
}
