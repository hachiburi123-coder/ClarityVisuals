package ru.method.clarityvisuals.utils.render.fonts;

import ru.method.clarityvisuals.api.render.msdf.MsdfFont;

public record Instance(MsdfFont font, float size) {
    public float getWidth(String string) {
        return this.font.getWidth(string, this.size);
    }

    public float getHeight() {
        return this.font.getHeight(this.size);
    }
}

