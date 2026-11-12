package ru.method.clarityvisuals.api.setting;

import java.util.function.Supplier;
import ru.method.clarityvisuals.render.Point2f;
import ru.method.clarityvisuals.api.setting.Setting;

public class PositionSetting
extends Setting<Point2f> {
    public PositionSetting(String string, Point2f point2f) {
        super(string, point2f);
    }

    public PositionSetting(String string, Point2f point2f, Supplier<Boolean> supplier) {
        super(string, point2f, supplier);
    }
}

