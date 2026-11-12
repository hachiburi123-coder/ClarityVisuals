package ru.method.clarityvisuals.modules.impl.render;

import java.util.function.Supplier;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_408;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.event.MouseScrollEvent;
import ru.method.clarityvisuals.api.events.impl.EventMouse;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.util.MathUtils;

public class ViewModelModule
extends Module {
    private final BooleanSetting In = new BooleanSetting("settings.viewmodel.punch_mode", false);
    private final Supplier<Boolean> Io = () -> (Boolean)this.In.jT() == false;
    public SliderSetting Ip = new SliderSetting("settings.viewmodel.main_x", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Iq = new SliderSetting("settings.viewmodel.main_y", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Ir = new SliderSetting("settings.viewmodel.main_z", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Is = new SliderSetting("settings.viewmodel.main_scale_x", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);
    public SliderSetting It = new SliderSetting("settings.viewmodel.main_scale_y", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);
    public SliderSetting Iu = new SliderSetting("settings.viewmodel.main_scale_z", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);
    public SliderSetting Iv = new SliderSetting("settings.viewmodel.off_x", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Iw = new SliderSetting("settings.viewmodel.off_y", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Ix = new SliderSetting("settings.viewmodel.off_z", 0.0f, -2.0f, 2.0f, 0.05f, this.Io);
    public SliderSetting Iy = new SliderSetting("settings.viewmodel.off_scale_x", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);
    public SliderSetting Iz = new SliderSetting("settings.viewmodel.off_scale_y", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);
    public SliderSetting IA = new SliderSetting("settings.viewmodel.off_scale_z", 1.0f, 0.0f, 3.0f, 0.05f, this.Io);

    // --- Interactive drag state (порт kimiko ViewModel: дабл-клик в чате + скролл) ---
    private static final long DOUBLE_CLICK_MS = 300L;
    private static final float DRAG_UNITS_PER_PIXEL = 3.2f;
    private static final float SCALE_STEP = 0.22f;
    private static final float FOLLOW = 0.45f;

    private class_1268 activeHand;
    private boolean holding;
    private long lastClickMs;
    private float grabCursorX;
    private float grabCursorY;
    private float grabOffsetX;
    private float grabOffsetY;
    private float mainSmoothX;
    private float mainSmoothY;
    private float offSmoothX;
    private float offSmoothY;
    private boolean smoothInit;
    private class_1306 lastMainArm;

    public ViewModelModule() {
        super("ViewModel", ModuleCategory.ql);
    }

    public boolean jN() {
        return (Boolean)this.In.jT();
    }

    public static ViewModelModule getInstance() {
        ClarityVisuals cv = ClarityVisuals.getInstance();
        if (cv == null || cv.getModuleManager() == null) {
            return null;
        }
        return cv.getModuleManager().getModule(ViewModelModule.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.smoothInit = false;
        this.holding = false;
        this.activeHand = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.holding = false;
        this.activeHand = null;
    }

    private static boolean inChat() {
        return aiG != null && aiG.field_1755 instanceof class_408;
    }

    private static float screenW() {
        if (aiG == null || aiG.method_22683() == null) {
            return 1.0f;
        }
        return (float) aiG.method_22683().method_4486();
    }

    private static float screenH() {
        if (aiG == null || aiG.method_22683() == null) {
            return 1.0f;
        }
        return (float) aiG.method_22683().method_4502();
    }

    private static float mouseX() {
        if (aiG == null || aiG.field_1729 == null || aiG.method_22683() == null) {
            return 0.0f;
        }
        double raw = aiG.field_1729.method_1603();
        double w = (double) aiG.method_22683().method_4480();
        if (w < 1.0) {
            return 0.0f;
        }
        return (float) (raw * (double) screenW() / w);
    }

    private static float mouseY() {
        if (aiG == null || aiG.field_1729 == null || aiG.method_22683() == null) {
            return 0.0f;
        }
        double raw = aiG.field_1729.method_1604();
        double h = (double) aiG.method_22683().method_4507();
        if (h < 1.0) {
            return 0.0f;
        }
        return (float) (raw * (double) screenH() / h);
    }

    private static float clamp(float v, float min, float max) {
        return MathUtils.k(v, min, max);
    }

    private void handleMainHandSwap() {
        if (aiG == null || aiG.field_1724 == null) {
            return;
        }
        class_1306 arm = aiG.field_1724.method_6068();
        if (this.lastMainArm == null) {
            this.lastMainArm = arm;
            return;
        }
        if (arm == this.lastMainArm) {
            return;
        }
        this.lastMainArm = arm;
        // Зеркалим X как в оригинале, чтобы руки не прыгали при смене main arm.
        this.Ip.a(Float.valueOf(-((Float) this.Ip.jT()).floatValue()));
        this.Iv.a(Float.valueOf(-((Float) this.Iv.jT()).floatValue()));
        this.mainSmoothX = -this.mainSmoothX;
        this.offSmoothX = -this.offSmoothX;
    }

    private class_1268 pickGrabbedHand(float mx) {
        boolean rightSide = mx >= screenW() * 0.5f;
        class_1306 mainArm = null;
        if (aiG != null && aiG.field_1724 != null) {
            mainArm = aiG.field_1724.method_6068();
        }
        boolean mainIsRight = mainArm == null || mainArm == class_1306.field_6183;
        // Если клик справа и main рука правая -> это MAIN_HAND, иначе OFF_HAND.
        return rightSide == mainIsRight ? class_1268.field_5808 : class_1268.field_5810;
    }

    @EventHandler
    private void onTick(ClientTickEvent event) {
        if (fullNullCheck()) {
            return;
        }
        this.handleMainHandSwap();
        if (!this.isToggled()) {
            this.holding = false;
            this.activeHand = null;
            return;
        }
        if (!this.smoothInit) {
            this.mainSmoothX = ((Float) this.Ip.jT()).floatValue();
            this.mainSmoothY = ((Float) this.Iq.jT()).floatValue();
            this.offSmoothX = ((Float) this.Iv.jT()).floatValue();
            this.offSmoothY = ((Float) this.Iw.jT()).floatValue();
            this.smoothInit = true;
        }
        boolean inChat = inChat();
        if (!inChat) {
            this.activeHand = null;
            this.holding = false;
        }
        if (inChat && this.holding && this.activeHand != null) {
            float mx = mouseX();
            float my = mouseY();
            float k = DRAG_UNITS_PER_PIXEL / Math.max(1.0f, screenH());
            float nx = this.grabOffsetX + (mx - this.grabCursorX) * k;
            float ny = this.grabOffsetY - (my - this.grabCursorY) * k;
            nx = clamp(nx, -2.0f, 2.0f);
            ny = clamp(ny, -2.0f, 2.0f);
            if (this.activeHand == class_1268.field_5808) {
                this.Ip.a(Float.valueOf(nx));
                this.Iq.a(Float.valueOf(ny));
            } else {
                this.Iv.a(Float.valueOf(nx));
                this.Iw.a(Float.valueOf(ny));
            }
        }
        boolean holdingMain = inChat && this.holding && this.activeHand == class_1268.field_5808;
        boolean holdingOff = inChat && this.holding && this.activeHand == class_1268.field_5810;
        float targetMainX = ((Float) this.Ip.jT()).floatValue();
        float targetMainY = ((Float) this.Iq.jT()).floatValue();
        float targetOffX = ((Float) this.Iv.jT()).floatValue();
        float targetOffY = ((Float) this.Iw.jT()).floatValue();
        if (holdingMain) {
            this.mainSmoothX = targetMainX;
            this.mainSmoothY = targetMainY;
        } else {
            this.mainSmoothX += (targetMainX - this.mainSmoothX) * FOLLOW;
            this.mainSmoothY += (targetMainY - this.mainSmoothY) * FOLLOW;
        }
        if (holdingOff) {
            this.offSmoothX = targetOffX;
            this.offSmoothY = targetOffY;
        } else {
            this.offSmoothX += (targetOffX - this.offSmoothX) * FOLLOW;
            this.offSmoothY += (targetOffY - this.offSmoothY) * FOLLOW;
        }
    }

    @EventHandler
    private void onMouse(EventMouse event) {
        if (!this.isToggled() || !inChat()) {
            return;
        }
        if (event.getButton() != 0) {
            return;
        }
        // EventMouse: action 1 = press, 0 = release (см. HudElement).
        if (event.getAction() == 1) {
            long now = System.currentTimeMillis();
            boolean dbl = now - this.lastClickMs <= DOUBLE_CLICK_MS;
            this.lastClickMs = now;
            if (!dbl) {
                return;
            }
            float mx = mouseX();
            float my = mouseY();
            this.activeHand = this.pickGrabbedHand(mx);
            this.holding = true;
            this.grabCursorX = mx;
            this.grabCursorY = my;
            boolean main = this.activeHand == class_1268.field_5808;
            this.grabOffsetX = main ? ((Float) this.Ip.jT()).floatValue() : ((Float) this.Iv.jT()).floatValue();
            this.grabOffsetY = main ? ((Float) this.Iq.jT()).floatValue() : ((Float) this.Iw.jT()).floatValue();
            event.consume();
        } else if (event.getAction() == 0) {
            this.holding = false;
        }
    }

    @EventHandler
    private void onScroll(MouseScrollEvent event) {
        if (!this.isToggled() || !inChat() || this.activeHand == null || !this.holding) {
            return;
        }
        double vertical = event.h();
        if (vertical == 0.0) {
            return;
        }
        float step = vertical > 0.0 ? SCALE_STEP : -SCALE_STEP;
        if (this.activeHand == class_1268.field_5808) {
            float cur = ((Float) this.Is.jT()).floatValue();
            float next = clamp(cur + step, 0.0f, 3.0f);
            this.Is.a(Float.valueOf(next));
            this.It.a(Float.valueOf(next));
            this.Iu.a(Float.valueOf(next));
        } else {
            float cur = ((Float) this.Iy.jT()).floatValue();
            float next = clamp(cur + step, 0.0f, 3.0f);
            this.Iy.a(Float.valueOf(next));
            this.Iz.a(Float.valueOf(next));
            this.IA.a(Float.valueOf(next));
        }
        event.consume();
    }

    // --- Геттеры для миксина (сглаженные значения, как mainOffsetX в оригинале) ---
    public float renderMainX() {
        return this.smoothInit ? this.mainSmoothX : ((Float) this.Ip.jT()).floatValue();
    }

    public float renderMainY() {
        return this.smoothInit ? this.mainSmoothY : ((Float) this.Iq.jT()).floatValue();
    }

    public float renderOffX() {
        return this.smoothInit ? this.offSmoothX : ((Float) this.Iv.jT()).floatValue();
    }

    public float renderOffY() {
        return this.smoothInit ? this.offSmoothY : ((Float) this.Iw.jT()).floatValue();
    }

    public boolean isDragging() {
        return this.holding && this.activeHand != null && inChat() && this.isToggled();
    }

    public class_1268 draggedHand() {
        return this.activeHand;
    }
}
