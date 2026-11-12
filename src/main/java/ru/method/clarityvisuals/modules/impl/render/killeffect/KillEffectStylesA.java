package ru.method.clarityvisuals.modules.impl.render.killeffect;

import static ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectRenderer.*;

/**
 * Tetris smash: tetromino blocks rain down around the fallen body and slam
 * into the ground with an impact ring and dust puffs.
 */
final class KillEffectStylesA {
    private static final int[][] TETRIS_PIECES = {
            {0, 0, 1, 0, 2, 0, 3, 0},
            {0, 0, 1, 0, 0, 1, 1, 1},
            {0, 0, 1, 0, 2, 0, 1, 1},
            {0, 0, 0, 1, 0, 2, 1, 2},
            {0, 0, 1, 0, 1, 1, 2, 1},
            {1, 0, 2, 0, 0, 1, 1, 1},
            {1, 0, 0, 1, 1, 1, 1, 2}
    };
    private static final int[] TETRIS_COLORS = {
            0x59C3FF, 0xFFD23F, 0xC77DFF, 0xFF9F43, 0x5BE37D, 0xFF5C7A, 0x4FE0E0
    };

    private KillEffectStylesA() {
    }

    static void tetris(Ctx c) {
        float fade = env(c);
        // Pure Tetris smash: no floor glow, no walls, no scan flash — just
        // tetromino blocks raining down and shattering into the ground.
        for (int piece = 0; piece < TETRIS_PIECES.length; piece++) {
            float angle = piece / (float) TETRIS_PIECES.length * TAU + 0.4f;
            float ox = (float) Math.cos(angle) * 1.45f;
            float oz = (float) Math.sin(angle) * 1.45f;
            float local = clamp((c.t - piece * 0.055f) / 0.55f);
            double drop = 4.2 * (1.0 - overshoot(local));
            double baseY = 0.24 + Math.max(drop, -0.22);
            int color = TETRIS_COLORS[piece % TETRIS_COLORS.length];
            int[] pieceData = TETRIS_PIECES[piece];
            for (int block = 0; block < pieceData.length; block += 2) {
                float bx = ox + (pieceData[block] - 1.5f) * 0.3f;
                float bz = oz + (pieceData[block + 1] - 1.0f) * 0.3f;
                box(c, bx, baseY, bz, 0.14f, 0.14f, 0.14f, col(color, 0.92f * fade), true);
            }
            if (local > 0.92f) {
                float puff = clamp((local - 0.92f) / 0.08f);
                ringAt(c, 0.03, 0.32f + 0.85f * easeOut(puff), col(color, (1.0f - puff) * 0.4f), true);
                for (int i = 0; i < 5; i++) {
                    sprite(c, ox + (rnd(c, piece * 5 + i, 1) - 0.5f) * 0.9f, 0.12,
                            oz + (rnd(c, piece * 5 + i, 2) - 0.5f) * 0.9f,
                            0.32f + 0.18f * rnd(c, piece * 5 + i, 3), MIST,
                            col(0xFFFFFF, fade * puff * 0.3f), true, false);
                }
            }
        }
    }
}
