package ru.method.clarityvisuals.screen.mainmenu;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_526;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.render.ColorMath;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.screen.AccountManagerScreen;
import ru.method.clarityvisuals.screen.MultiplayerScreen;
import ru.method.clarityvisuals.screen.SettingsScreen;
import ru.method.clarityvisuals.screen.mainmenu.MenuChrome;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.utils.render.fonts.Instance;

public final class CustomMainMenuScreen
extends class_437
implements MinecraftClientAccess {
    private static final float DESIGN_HEIGHT = 1080.0f;
    private float uiScale = 1.0f;
    private int vw = 1;
    private int vh = 1;
    public static Color ACCENT;
    private static final Color TEXT;
    private static final Color TEXT2;
    private static final Color TEXT3;
    private static final Color TEXT4;
    private static final Color MUTED;
    private static final Color MUTED2;
    private static final Color MUTED4;
    private static final Color INK;
    private static final Color DANGER;
    private static final String IC_GLOBE;
    private static final String IC_PERSON;
    private static final String IC_GEAR;
    private static final String IC_POWER;
    private final Animation hNet = CustomMainMenuScreen.anim();
    private final Animation hSingle = CustomMainMenuScreen.anim();
    private final Animation hSettings = CustomMainMenuScreen.anim();
    private final Animation hAlts = CustomMainMenuScreen.anim();
    private final Animation hExit = CustomMainMenuScreen.anim();
    private final Animation hCover = CustomMainMenuScreen.anim();
    private final float[] rNet = new float[4];
    private final float[] rSingle = new float[4];
    private final float[] rSettings = new float[4];
    private final float[] rAlts = new float[4];
    private final float[] rExit = new float[4];
    private final float[] rCover = new float[4];
    private long exitArmedUntil = 0L;
    private float parX;
    private float parY;
    private long welcomeEndMs = 0L;

    private static Animation anim() {
        return MenuChrome.anim();
    }

    public CustomMainMenuScreen() {
        super((class_2561)class_2561.method_43470((String)"Clarity Visuals"));
    }

    private boolean recomputeScale() {
        if (aiG == null || aiG.method_22683() == null) {
            return false;
        }
        float f = Math.max(0.05f, (float)aiG.method_22683().method_4502() / 1080.0f);
        int n = Math.max(1, Math.round((float)aiG.method_22683().method_4486() / f));
        int n2 = Math.max(1, Math.round((float)aiG.method_22683().method_4502() / f));
        if (n == this.vw && n2 == this.vh && Math.abs(f - this.uiScale) < 5.0E-4f) {
            return false;
        }
        this.uiScale = f;
        this.vw = n;
        this.vh = n2;
        return true;
    }

    public void method_25394(class_332 class_3322, int n, int n2, float f) {
        this.recomputeScale();
        class_4587 class_45872 = class_3322.method_51448();
        class_45872.method_22903();
        class_45872.method_22905(this.uiScale, this.uiScale, 1.0f);
        int n3 = Math.round((float)n / this.uiScale);
        int n4 = Math.round((float)n2 / this.uiScale);
        float f2 = class_3532.method_15363((float)(((float)n3 - (float)this.vw / 2.0f) / ((float)this.vw / 2.0f)), (float)-1.0f, (float)1.0f);
        float f3 = class_3532.method_15363((float)(((float)n4 - (float)this.vh / 2.0f) / ((float)this.vh / 2.0f)), (float)-1.0f, (float)1.0f);
        this.parX += (f2 - this.parX) * 0.08f;
        this.parY += (f3 - this.parY) * 0.08f;
        this.drawBackground(class_45872);
        boolean bl = ColorMath.rz();
        if (!bl) {
            MenuChrome.autoStartOnce();
        }
        MenuChrome.tickMusic();
        if (bl) {
            this.welcomeEndMs = 0L;
            class_45872.method_22909();
            this.renderLottie(class_3322);
            return;
        }
        if (this.welcomeEndMs == 0L) {
            this.welcomeEndMs = System.currentTimeMillis();
        }
        float f4 = (float)Math.min(1.0, (double)(System.currentTimeMillis() - this.welcomeEndMs) / 450.0);
        f4 = 1.0f - (1.0f - f4) * (1.0f - f4) * (1.0f - f4);
        class_45872.method_22903();
        class_45872.method_46416(0.0f, (1.0f - f4) * 22.0f, 0.0f);
        MenuChrome.drawLogo(class_45872, this.vw);
        MenuChrome.drawClock(class_45872, this.vw);
        this.drawWelcomeBlock(class_45872, n3, n4, f);
        this.drawDisclaimer(class_45872);
        MenuChrome.drawMusic(class_45872, this.vw, this.vh, n3, n4, this.hCover, this.rCover);
        this.drawAltsButton(class_45872, n3, n4);
        class_45872.method_22909();
        if (f4 < 0.999f) {
            Render2D.drawRoundedRect(class_45872, 0.0f, 0.0f, this.vw, this.vh, 0.0f, new Color(6, 9, 8, Math.round(200.0f * (1.0f - f4))));
        }
        class_45872.method_22909();
    }

    private void drawBackground(class_4587 class_45872) {
        MenuChrome.drawBackgroundArt(class_45872, this.vw, this.vh, this.parX, this.parY);
        this.drawScrim(class_45872);
    }

    private void drawScrim(class_4587 class_45872) {
        Render2D.drawHorizontalGradient(class_45872, 0.0f, 0.0f, this.vw, this.vh, new Color(6, 9, 8, 128), new Color(6, 9, 8, 0));
    }

    private void drawWelcomeBlock(class_4587 class_45872, int n, int n2, float f) {
        float f2 = 56.0f;
        float f3 = 300.0f;
        float f4 = 340.0f;
        String string = aiG != null && aiG.method_1548() != null ? aiG.method_1548().method_1676() : "player";
        Instance instance = Fonts.UNBOUNDED_L.getFont(10.0f);
        this.drawSpaced(class_45872, instance, "\u0414\u0410\u0412\u041d\u041e \u041d\u0415 \u0412\u0418\u0414\u0415\u041b\u0418\u0421\u042c", f2, f3, ACCENT, 3.0f);
        float f5 = f3 + instance.getHeight() + 14.0f;
        Instance instance2 = Fonts.GOLOS_SEMI.getFont(38.0f);
        float f6 = 43.7f;
        Render2D.drawFont(class_45872, instance2, "\u0414\u043e\u0431\u0440\u043e \u043f\u043e\u0436\u0430\u043b\u043e\u0432\u0430\u0442\u044c", f2, f5, TEXT);
        Render2D.drawFont(class_45872, instance2, "\u0432\u043d\u043e\u0432\u044c, " + string, f2, f5 + f6, TEXT);
        Instance instance3 = Fonts.GOLOS.getFont(14.0f);
        Render2D.drawFont(class_45872, instance3, "\u0413\u043e\u0442\u043e\u0432 \u043f\u0440\u044b\u0433\u0430\u0442\u044c \u043f\u043e \u0431\u043b\u043e\u043a\u0430\u043c?", f2, f5 += f6 * 2.0f + 8.0f, MUTED2);
        float f7 = 38.0f;
        float f8 = f5 += instance3.getHeight() + 28.0f;
        Render2D.drawRoundedVerticalGradient(class_45872, f2, f8, f7, f7, f7 / 2.0f, new Color(42, 56, 48), new Color(16, 22, 19));
        Instance instance4 = Fonts.UNBOUNDED.getFont(12.0f);
        String string2 = string.isEmpty() ? "?" : string.substring(0, 1).toUpperCase(Locale.ROOT);
        Render2D.drawFont(class_45872, instance4, string2, f2 + (f7 - instance4.getWidth(string2)) / 2.0f, f8 + (f7 - instance4.getHeight()) / 2.0f, ACCENT);
        float f9 = f2 + f7 + 12.0f;
        Instance instance5 = Fonts.GOLOS.getFont(10.5f);
        String string3 = this.licenseWord();
        Render2D.drawFont(class_45872, Fonts.ICONS2.getFont(9.0f), "A", f9, f8 + 1.0f, MUTED2);
        this.drawSpaced(class_45872, instance5, " \u0442\u0435\u043a\u0443\u0449\u0438\u0439 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u00b7 " + string3, f9 + 10.0f, f8 + 1.0f, MUTED2, 0.84f);
        Instance instance6 = Fonts.UNBOUNDED.getFont(13.0f);
        Render2D.drawFont(class_45872, instance6, string, f9, f8 + 18.0f, TEXT);
        f5 = f8 + f7 + 18.0f;
        float f14 = 50.0f;
        float f15 = 9.0f;
        boolean bl2 = this.hit(n, n2, f2, f5, f4, f14);
        this.drawButton(class_45872, f2, f5, f4, f14, true, "C", "\u0421\u0435\u0442\u0435\u0432\u0430\u044f \u0438\u0433\u0440\u0430", INK, this.upd(this.hNet, bl2), false, false);
        this.set(this.rNet, f2, f5, f4, f14);
        boolean bl3 = this.hit(n, n2, f2, f5 += f14 + f15, f4, f14);
        this.drawButton(class_45872, f2, f5, f4, f14, false, "A", "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430", TEXT2, this.upd(this.hSingle, bl3), false, false);
        this.set(this.rSingle, f2, f5, f4, f14);
        float f16 = 108.0f;
        float f17 = f4 - f16 - f15;
        boolean bl4 = this.hit(n, n2, f2, f5 += f14 + f15, f17, f14);
        this.drawButton(class_45872, f2, f5, f17, f14, false, "B", "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", TEXT2, this.upd(this.hSettings, bl4), false, false);
        this.set(this.rSettings, f2, f5, f17, f14);
        float f18 = f2 + f17 + f15;
        boolean bl5 = System.currentTimeMillis() < this.exitArmedUntil;
        boolean bl6 = this.hit(n, n2, f18, f5, f16, f14);
        this.drawButton(class_45872, f18, f5, f16, f14, false, "D", bl5 ? "\u0422\u043e\u0447\u043d\u043e?" : "\u0412\u044b\u0445\u043e\u0434", bl5 ? DANGER : TEXT4, this.upd(this.hExit, bl6), true, true);
        this.set(this.rExit, f18, f5, f16, f14);
    }

    private void drawButton(class_4587 class_45872, float f, float f2, float f3, float f4, boolean bl, String string, String string2, Color color, float f5, boolean bl2, boolean bl3) {
        MenuChrome.drawButton(class_45872, f, f2, f3, f4, bl, string, string2, color, f5, bl2, bl3);
    }

    private void drawAltsButton(class_4587 class_45872, int n, int n2) {
        float fW = 300.0f;
        float fH = 50.0f;
        float fX = (float)this.vw - 56.0f - fW;
        float fY = (float)this.vh - 56.0f - fH;
        boolean bl = this.hit(n, n2, fX, fY, fW, fH);
        this.drawButton(class_45872, fX, fY, fW, fH, false, "A", "Аккаунты", TEXT2, this.upd(this.hAlts, bl), false, false);
        this.set(this.rAlts, fX, fY, fW, fH);
    }

    private void drawDisclaimer(class_4587 class_45872) {        float f2 = 56.0f;
        Instance instance = Fonts.GOLOS.getFont(11.0f);
        String[] stringArray = new String[]{"Clarity Visuals \u043d\u0435 \u0441\u0432\u044f\u0437\u0430\u043d \u0441 Mojang \u0438 Microsoft \u0438 \u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f", "\u043e\u0444\u0438\u0446\u0438\u0430\u043b\u044c\u043d\u044b\u043c \u043f\u0440\u043e\u0434\u0443\u043a\u0442\u043e\u043c Minecraft. \u0412\u0435\u0440\u0441\u0438\u044f 1.21.4, \u0441\u0431\u043e\u0440\u043a\u0430 2.0."};
        float f3 = instance.getHeight() * 1.5f;
        float f4 = (float)this.vh - 44.0f - f3 * (float)stringArray.length;
        for (int i = 0; i < stringArray.length; ++i) {
            Render2D.drawFont(class_45872, instance, stringArray[i], f2, f4 + (float)i * f3, MUTED4);
        }
    }

    public boolean method_25402(double d, double d2, int n) {
        if (ColorMath.rz()) {
            return true;
        }
        if (n != 0) {
            return super.method_25402(d, d2, n);
        }
        float f = (float)(d / (double)this.uiScale);
        float f2 = (float)(d2 / (double)this.uiScale);
        if (this.in(this.rNet, f, f2)) {
            MenuChrome.keepMusicAcrossSwap();
            aiG.method_1507((class_437)new MultiplayerScreen(this));
            return true;
        }
        if (this.in(this.rSingle, f, f2)) {
            aiG.method_1507((class_437)new class_526((class_437)this));
            return true;
        }
        if (this.in(this.rSettings, f, f2)) {
            MenuChrome.keepMusicAcrossSwap();
            aiG.method_1507((class_437)new SettingsScreen(this));
            return true;
        }
        if (this.in(this.rAlts, f, f2)) {
            MenuChrome.keepMusicAcrossSwap();
            aiG.method_1507((class_437)new AccountManagerScreen(this));
            return true;
        }
        if (this.in(this.rExit, f, f2)) {
            if (System.currentTimeMillis() < this.exitArmedUntil) {
                aiG.method_1592();
            } else {
                this.exitArmedUntil = System.currentTimeMillis() + 3000L;
            }
            return true;
        }
        if (this.in(this.rCover, f, f2)) {
            this.toggleMusic();
            return true;
        }
        return super.method_25402(d, d2, n);
    }

    private void drawSpaced(class_4587 class_45872, Instance instance, String string, float f, float f2, Color color, float f3) {
        MenuChrome.drawSpaced(class_45872, instance, string, f, f2, color, f3);
    }

    private boolean hit(int n, int n2, float f, float f2, float f3, float f4) {
        return MenuChrome.hit(n, n2, f, f2, f3, f4);
    }

    private boolean in(float[] fArray, float f, float f2) {
        return MenuChrome.in(fArray, f, f2);
    }

    private void set(float[] fArray, float f, float f2, float f3, float f4) {
        MenuChrome.set(fArray, f, f2, f3, f4);
    }

    private float upd(Animation animation, boolean bl) {
        return MenuChrome.upd(animation, bl);
    }

    private Color lerp(Color color, Color color2, float f) {
        return MenuChrome.lerp(color, color2, f);
    }

    private String licenseWord() {
        return MenuChrome.licenseWord();
    }

    private void renderLottie(class_332 class_3322) {
        class_4587 class_45872 = class_3322.method_51448();
        class_45872.method_22903();
        class_45872.method_22905(this.uiScale, this.uiScale, 1.0f);
        ColorMath.d(class_3322, this.vw, this.vh);
        class_45872.method_22909();
    }

    private void toggleMusic() {
        MenuChrome.toggleMusic();
    }

    public void method_25432() {
        MenuChrome.onMenuScreenRemoved();
        super.method_25432();
    }

    public boolean method_25422() {
        return false;
    }

    public void method_25420(class_332 class_3322, int n, int n2, float f) {
    }

    static {
        IC_GLOBE = "C";
        IC_PERSON = "A";
        IC_GEAR = "B";
        IC_POWER = "D";
        ACCENT = new Color(155, 163, 175);
        TEXT = MenuChrome.TEXT;
        TEXT2 = MenuChrome.TEXT2;
        TEXT3 = MenuChrome.TEXT3;
        TEXT4 = MenuChrome.TEXT4;
        MUTED = MenuChrome.MUTED;
        MUTED2 = MenuChrome.MUTED2;
        MUTED4 = MenuChrome.MUTED4;
        INK = MenuChrome.INK;
        DANGER = MenuChrome.DANGER;
    }
}
