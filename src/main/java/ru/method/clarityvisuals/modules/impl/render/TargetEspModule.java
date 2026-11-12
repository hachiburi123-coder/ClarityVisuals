package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.Setting;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.UUID;
import lombok.Generated;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_10142;
import net.minecraft.class_10156;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.render.ProjectionUtils;
import ru.method.clarityvisuals.server.ServerDetector;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.utils.debug.TargetEspProfiler;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.DepthRenderScope;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.Render3D;

public class TargetEspModule
extends Module {
    private static final class_2960 Gu = ClarityVisuals.id("client/xuynya/ghost_bloom.png");
    private static final class_2960 Gv = ClarityVisuals.id("client/xuynya/marker.png");
    private final EnumSetting<TargetStyle> Gw = new EnumSetting<TargetStyle>("settings.targetesp.mode", TargetStyle.GO);
    private final BooleanSetting Gx = new BooleanSetting("settings.targetesp.npc", false);
    private final SliderSetting Gy = new SliderSetting("settings.targetesp.size", 15.0f, 10.0f, 25.0f, 1.0f, () -> this.Gw.jT() == TargetStyle.GO);
    private final SliderSetting Gz = new SliderSetting("settings.targetesp.ghost_speed", 1.0f, 0.2f, 3.0f, 0.05f, () -> {
        TargetStyle targetStyle = (TargetStyle)this.Gw.jT();
        return targetStyle == TargetStyle.GP || targetStyle == TargetStyle.GQ;
    });
    private final SliderSetting GA = new SliderSetting("settings.targetesp.crystals.count", 14.0f, 4.0f, 40.0f, 1.0f, () -> this.Gw.jT() == TargetStyle.GR);
    private final Animation GB = new Animation(300L, 1.0, true, Easing.aiR);
    private class_1309 GC = null;
    private class_1309 GD = null;
    private long kI = 0L;
    private final long GE = 1000L;
    private final double GF = 30.0;
    private static final long GG = 50000000L;
    private double GI = 0.0;
    private double GJ = 0.0;
    private long GK = 0L;
    private class_1309 GL = null;
    private long GM = 0L;
    private boolean GN = false;

    public TargetEspModule() {
        super("TargetEsp", ModuleCategory.ql);
    }

    private Color jr() {
        Color color = ColorUtils.getGlobalColor();
        if (color == null) {
            return Color.WHITE;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private class_1309 js() {
        class_3966 class_39662;
        class_1297 class_12972;
        if (TargetEspModule.fullNullCheck()) {
            return null;
        }
        class_239 class_2392 = TargetEspModule.aiG.field_1765;
        if (class_2392 != null && class_2392.method_17783() == class_239.class_240.field_1331 && (class_12972 = (class_39662 = (class_3966)class_2392).method_17782()) instanceof class_1309 && class_12972 != TargetEspModule.aiG.field_1724) {
            class_1309 class_13092;
            if (this.d((class_1309)class_12972) && !((Boolean)this.Gx.jT()).booleanValue()) {
                return null;
            }
            if (class_12972 instanceof class_1657 && this.d((class_1657)class_12972)) {
                return null;
            }
            if (class_12972 instanceof class_1657 && this.s((class_1657)class_12972)) {
                return null;
            }
            if (!(class_12972 instanceof class_1657) && class_12972.method_5767()) {
                return null;
            }
            if (!(class_12972 instanceof class_1657) && class_12972 instanceof class_1309 && (class_13092 = (class_1309)class_12972).method_6059(class_1294.field_5905)) {
                return null;
            }
            double d = TargetEspModule.aiG.field_1724.method_5739(class_12972);
            if (d <= 30.0) {
                return (class_1309)class_12972;
            }
        }
        return null;
    }

    private boolean c(class_1309 class_13092) {
        if (class_13092 == null) {
            return false;
        }
        if (class_13092.method_31481() || !class_13092.method_5805()) {
            return false;
        }
        if (this.d(class_13092) && !((Boolean)this.Gx.jT()).booleanValue()) {
            return false;
        }
        if (TargetEspModule.aiG.field_1724 == null) {
            return false;
        }
        if (class_13092 instanceof class_1657 && this.d((class_1657)class_13092)) {
            return false;
        }
        if (!(class_13092 instanceof class_1657) && class_13092.method_5767()) {
            return false;
        }
        if (!(class_13092 instanceof class_1657) && class_13092.method_6059(class_1294.field_5905)) {
            return false;
        }
        if (class_13092 instanceof class_1657 && this.s((class_1657)class_13092)) {
            return false;
        }
        return (double)TargetEspModule.aiG.field_1724.method_5739((class_1297)class_13092) <= 30.0;
    }

    private boolean d(class_1657 class_16572) {
        if (class_16572 == null) {
            return true;
        }
        if (class_16572 == TargetEspModule.aiG.field_1724) {
            return false;
        }
        if (!((Boolean)this.Gx.jT()).booleanValue() && this.r(class_16572)) {
            return true;
        }
        boolean bl = class_16572.method_5767() || class_16572.method_6059(class_1294.field_5905);
        return bl && !this.o(class_16572);
    }

    private boolean o(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        if (!class_16572.method_6047().method_7960() || !class_16572.method_6079().method_7960()) {
            return true;
        }
        for (class_1799 class_17992 : class_16572.method_5661()) {
            if (class_17992 == null || class_17992.method_7960()) continue;
            return true;
        }
        return false;
    }

    private boolean d(class_1309 class_13092) {
        if (class_13092 == null) {
            return false;
        }
        if (class_13092 instanceof class_1531) {
            return true;
        }
        if (class_13092 instanceof class_1657) {
            class_1657 class_16572 = (class_1657)class_13092;
            return this.r(class_16572);
        }
        return false;
    }

    private boolean r(class_1657 class_16572) {
        if (class_16572 == null || class_16572 == TargetEspModule.aiG.field_1724 || aiG.method_1562() == null) {
            return false;
        }
        if (ServerDetector.t(class_16572) != 0) {
            return false;
        }
        UUID uUID = class_16572.method_7334() != null ? class_16572.method_7334().getId() : null;
        return this.d(uUID) || this.d(class_16572.method_5667());
    }

    private boolean d(UUID uUID) {
        return uUID != null && uUID.getMostSignificantBits() == 0L && uUID.getLeastSignificantBits() == 0L;
    }

    private boolean s(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getFriendManager() == null) {
            return false;
        }
        GameProfile gameProfile = class_16572.method_7334();
        if (gameProfile == null || gameProfile.getName() == null) {
            return false;
        }
        return clarityVisuals.getFriendManager().av(gameProfile.getName());
    }

    private boolean a(class_1309 class_13092, float f) {
        double d;
        double d2;
        class_243 class_2432;
        if (TargetEspModule.fullNullCheck() || class_13092 == null) {
            return false;
        }
        long l = System.nanoTime();
        if (class_13092 == this.GL && l - this.GM <= 50000000L) {
            return this.GN;
        }
        class_243 class_2433 = TargetEspModule.aiG.field_1724.method_5836(f);
        class_3959 class_39592 = new class_3959(class_2433, class_2432 = class_13092.method_30950(f).method_1031(0.0, (double)class_13092.method_17682() * 0.5, 0.0), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)TargetEspModule.aiG.field_1724);
        class_3965 class_39652 = TargetEspModule.aiG.field_1687.method_17742(class_39592);
        boolean bl = class_39652 == null ? true : (class_39652.method_17783() == class_239.class_240.field_1333 ? true : (class_39652.method_17783() == class_239.class_240.field_1332 ? (d2 = class_39652.method_17784().method_1025(class_2433)) >= (d = class_2432.method_1025(class_2433)) - 0.001 : true));
        this.GL = class_13092;
        this.GM = l;
        this.GN = bl;
        return bl;
    }

    private boolean jt() {
        class_1309 class_13092 = this.js();
        long l = System.currentTimeMillis();
        if (class_13092 != null) {
            this.GC = class_13092;
            this.GD = class_13092;
            this.kI = l;
            return true;
        }
        if (this.GC != null) {
            if (l - this.kI < 1000L && this.c(this.GC)) {
                this.GD = this.GC;
                return true;
            }
            this.GC = null;
        }
        return false;
    }

    private void ju() {
        this.GI = 0.0;
        this.GJ = 0.0;
        this.GK = 0L;
        this.GL = null;
        this.GM = 0L;
        this.GN = false;
    }

    private void a(double d) {
        long l = System.nanoTime();
        if (this.GK == 0L) {
            this.GK = l;
            return;
        }
        double d2 = (double)(l - this.GK) / 1.0E9;
        this.GK = l;
        this.GI = (this.GI + d2 * d * 0.45) % 1.0;
        this.GJ = (this.GJ + d2 * d * 0.7) % 1.0;
        if (this.GI < 0.0) {
            this.GI += 1.0;
        }
        if (this.GJ < 0.0) {
            this.GJ += 1.0;
        }
    }

        @EventHandler
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (TargetEspModule.fullNullCheck()) {
            return;
        }
        if (this.Gw.jT() != TargetStyle.GO) {
            return;
        }
        TargetEspProfiler.beginFrame();
        try {
            long l = TargetEspProfiler.startSection();
            boolean bl = this.jt();
            TargetEspProfiler.endSection("targetRetention", l);
            this.GB.i(bl);
            if (this.GB.rk() <= 0.0f) {
                return;
            }
            if (!bl && this.GD == null) {
                return;
            }
            if (bl && this.GD != null && !this.c(this.GD)) {
                return;
            }
            if (bl && this.GD != null) {
                float f = hudRenderEvent.k().method_60637(true);
                long l2 = TargetEspProfiler.startSection();
                boolean bl2 = this.a(this.GD, f);
                TargetEspProfiler.endSection("visibilityCheck", l2);
                if (!bl2) {
                    return;
                }
            }
            long l3 = TargetEspProfiler.startSection();
            this.e(hudRenderEvent);
            TargetEspProfiler.endSection("renderOriginal", l3);
        }
        finally {
            TargetEspProfiler.endFrame();
        }
    }

        @EventHandler
    public void onRender3D(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (TargetEspModule.fullNullCheck()) {
            this.ju();
            return;
        }
        if (this.Gw.jT() == TargetStyle.GO) {
            this.ju();
            return;
        }
        TargetEspProfiler.beginFrame();
        try {
            long l = TargetEspProfiler.startSection();
            boolean bl = this.jt();
            TargetEspProfiler.endSection("targetRetention", l);
            this.GB.i(bl);
            if (this.GB.rk() <= 0.0f) {
                this.ju();
                return;
            }
            if (!bl && this.GD == null) {
                this.ju();
                return;
            }
            if (bl && this.GD != null && !this.c(this.GD)) {
                this.ju();
                return;
            }
            if (bl && this.GD != null) {
                float f = matrixStackStage.k().method_60637(true);
                long l2 = TargetEspProfiler.startSection();
                boolean bl2 = this.a(this.GD, f);
                TargetEspProfiler.endSection("visibilityCheck", l2);
                if (!bl2) {
                    this.ju();
                    return;
                }
            }
            switch (((TargetStyle)this.Gw.jT()).ordinal()) {
                case 1: {
                    long l3 = TargetEspProfiler.startSection();
                    this.a(matrixStackStage);
                    TargetEspProfiler.endSection("renderGhosts", l3);
                    return;
                }
                case 2: {
                    long l4 = TargetEspProfiler.startSection();
                    this.b(matrixStackStage);
                    TargetEspProfiler.endSection("renderCircles", l4);
                    return;
                }
                case 3: {
                    long l5 = TargetEspProfiler.startSection();
                    this.c(matrixStackStage);
                    TargetEspProfiler.endSection("renderCrystals", l5);
                    return;
                }
                default: {
                    this.ju();
                    return;
                }
            }
        }
        finally {
            TargetEspProfiler.endFrame();
        }
    }

    private void e(HudRenderEvent hudRenderEvent) {
        float f = hudRenderEvent.k().method_60637(true);
        class_243 class_2432 = this.GD.method_19538();
        class_243 class_2433 = new class_243(this.GD.field_6014, this.GD.field_6036, this.GD.field_5969);
        class_243 class_2434 = class_2433.method_35590(class_2432, (double)f);
        double d = class_2434.field_1352 - TargetEspModule.aiG.field_1724.method_23317();
        double d2 = class_2434.field_1350 - TargetEspModule.aiG.field_1724.method_23321();
        double d3 = Math.sqrt(d * d + d2 * d2);
        float f2 = (float)ProjectionUtils.c(class_2434, ((Float)this.Gy.jT()).floatValue() * this.GB.rk());
        float f3 = Math.max(f2 - (float)d3 * 0.1f, ((Float)this.Gy.jT()).floatValue() * 0.3f);
        class_243 class_2435 = ProjectionUtils.o(new class_243(class_2434.field_1352, class_2434.field_1351 + (double)(this.GD.method_17682() / 2.0f), class_2434.field_1350));
        if (!(class_2435.field_1350 > 0.0) || !(class_2435.field_1350 < 1.0)) {
            return;
        }
        double d4 = (double)System.currentTimeMillis() / 1000.0;
        float f4 = (float)(Math.sin(d4) * 180.0);
        Color color = this.jr();
        int n = (int)(255.0f * this.GB.rk() * 0.85f);
        Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
        Color color3 = new Color(255, 0, 0, n);
        float f5 = 0.0f;
        if (this.GD.field_6235 > 0) {
            f5 = (float)Math.abs(Math.sin((double)this.GD.field_6235 * 0.5));
        }
        Color color4 = ColorUtils.fade(color2, color3, f5);
        try (DepthRenderScope depthRenderScope = DepthRenderScope.lequal();){
            hudRenderEvent.j().method_51448().method_22903();
            hudRenderEvent.j().method_51448().method_22904(class_2435.method_10216(), class_2435.method_10214(), -100.0);
            hudRenderEvent.j().method_51448().method_22907(class_7833.field_40718.rotationDegrees(f4));
            Render2D.drawTexture(hudRenderEvent.j().method_51448(), -f3 / 2.0f, -f3 / 2.0f, f3, f3, 0.0f, Gv, color4);
            hudRenderEvent.j().method_51448().method_22909();
        }
    }

    private void a(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (this.GD == null || TargetEspModule.aiG.field_1724 == null) {
            this.ju();
            return;
        }
        float f = matrixStackStage.k().method_60637(true);
        class_243 class_2432 = this.GD.method_30950(f);
        boolean bl = this.a(this.GD, f);
        float f2 = class_3532.method_15363((float)this.GB.rk(), (float)0.0f, (float)1.0f);
        if (f2 <= 0.0f) {
            this.ju();
            return;
        }
        float f3 = class_3532.method_15363((float)(((float)this.GD.field_6235 - f) / 10.0f), (float)0.0f, (float)1.0f);
        float f4 = class_3532.method_15363((float)((Float)this.Gz.jT()).floatValue(), (float)0.2f, (float)3.0f);
        this.a(f4);

        // Два призрачных шара летают по одной общей орбите. Раньше здесь было
        // 60 наложенных спрайтов, из-за чего эффект распадался на две «колбасы».
        float f5 = Math.max(this.GD.method_17681(), 0.55f);
        float f6 = this.GD.method_17682();
        float f7 = (f5 * 0.82f + 0.3f) * f2;
        float f8 = f7 * 0.72f;
        double d = this.GI * Math.PI * 2.0;
        double d2 = this.GJ * Math.PI * 2.0;
        double d3 = class_2432.field_1351 + (double)(f6 * 0.55f);
        Color color = this.jr();
        Color color2 = new Color(255, 64, 64);
        Color color3 = ColorUtils.fade(color, color2, f3);
        int n = 0;

        // Едва заметная орбита связывает шары в единое целое.
        Color color4 = new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), class_3532.method_15340((int)Math.round(12.0f * f2), (int)0, (int)255));
        Color color5 = new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), class_3532.method_15340((int)Math.round(34.0f * f2), (int)0, (int)255));
        for (int i = 0; i < 48; ++i) {
            double d4 = d + (double)i * (Math.PI * 2) / 48.0;
            double d5 = d + (double)(i + 1) * (Math.PI * 2) / 48.0;
            class_243 class_2433 = this.a(class_2432, d3, d2, f7, f8, d4);
            class_243 class_2434 = this.a(class_2432, d3, d2, f7, f8, d5);
            float f9 = 0.5f + 0.5f * (float)Math.cos((d4 - d) * 2.0);
            Color color6 = ColorUtils.fade(color4, color5, f9);
            Render3D.drawLine(matrixStackStage.l(), class_2433, class_2434, color6, 1.15f, false);
        }

        for (int i = 0; i < 2; ++i) {
            double d6 = d + (double)i * Math.PI;
            float f10 = (float)(1.0 + Math.sin(d2 * 2.0 + (double)i) * 0.045);
            float f11 = f7 * 2.15f * f10;

            // Хвост тянется по орбите позади шара и плавно провисает вниз.
            int tailCount = 10;
            for (int t = tailCount; t >= 1; --t) {
                float tailT = (float)t / (float)tailCount;
                double d7 = d6 - (double)t * 0.075;
                class_243 class_2436 = this.a(class_2432, d3, d2, f7, f8, d7);
                double d8 = (double)t * (double)f7 * 0.032;
                double d9 = Math.sin(d2 * 2.0 + (double)i * 1.4 + (double)t * 0.8) * (double)f7 * 0.018 * (double)t;
                class_2436 = class_2436.method_1031(0.0, d9 - d8, 0.0);
                float f12 = f11 * (0.56f - 0.047f * (float)t);
                int n5 = class_3532.method_15340((int)Math.round(82.0f * f2 * Math.pow(1.0f - tailT, 1.5)), (int)0, (int)255);
                Color color10 = ColorUtils.fade(
                    new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), n5),
                    new Color(255, 255, 255, n5),
                    0.22f * (1.0f - tailT)
                );
                Render3D.renderBillboardTexture(class_2436, f12, Gu, color10.getRGB(), bl, true);
                ++n;
            }

            class_243 class_2435 = this.a(class_2432, d3, d2, f7, f8, d6);
            int n2 = class_3532.method_15340((int)Math.round(46.0f * f2), (int)0, (int)255);
            int n3 = class_3532.method_15340((int)Math.round(112.0f * f2), (int)0, (int)255);
            int n4 = class_3532.method_15340((int)Math.round(215.0f * f2), (int)0, (int)255);
            Color color7 = new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), n2);
            Color color8 = ColorUtils.fade(new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), n3), new Color(255, 255, 255, n3), 0.55f);
            Color color9 = ColorUtils.fade(new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), n4), Color.WHITE, 0.78f);
            Render3D.renderBillboardTexture(class_2435, f11, Gu, color7.getRGB(), bl, true);
            Render3D.renderBillboardTexture(class_2435, f11 * 0.52f, Gu, color8.getRGB(), bl, true);
            Render3D.renderBillboardTexture(class_2435, f11 * 0.23f, Gu, color9.getRGB(), bl, true);
            n += 3;
        }
        TargetEspProfiler.incrementCounter("ghostSprites", n);
    }

    private class_243 a(class_243 class_2432, double d, double d2, float f, float f2, double d3) {
        return new class_243(class_2432.field_1352 + Math.cos(d3) * (double)f, d + Math.sin(d3 * 2.0 + d2) * 0.22, class_2432.field_1350 + Math.sin(d3) * (double)f2);
    }

    private void b(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (this.GD == null || TargetEspModule.aiG.field_1724 == null) {
            this.ju();
            return;
        }
        float f = matrixStackStage.k().method_60637(true);
        class_243 class_2432 = this.GD.method_30950(f);
        boolean bl = this.a(this.GD, f);
        float f2 = class_3532.method_15363((float)this.GB.rk(), (float)0.0f, (float)1.0f);
        if (f2 <= 0.0f) {
            this.ju();
            return;
        }
        double d = class_3532.method_15363((float)((Float)this.Gz.jT()).floatValue(), (float)0.2f, (float)3.0f);
        this.a(d);
        float f3 = class_3532.method_15363((float)(((float)this.GD.field_6235 - f) / 10.0f), (float)0.0f, (float)1.0f);
        float f4 = Math.max(this.GD.method_17681() * 0.8f, 0.4f);
        double d2 = this.GI;
        double d3 = this.GJ;
        double d4 = Math.sin(d3 * 6.2831854820251465) * 0.3 * (double)f2;
        double d5 = 0.25 * (double)f2;
        double d6 = class_2432.field_1351 + (double)(this.GD.method_17682() * 0.6f) + d4 + d5;
        Color color = this.jr();
        Color color2 = new Color(255, 64, 64);
        int n = 3;
        int n2 = 6;
        double d7 = 0.2;
        float f5 = Math.max(0.35f * f2, 0.04f);
        int n3 = class_3532.method_15340((int)Math.round(f2 * 240.0f), (int)0, (int)255);
        int n4 = 0;
        for (int i = 0; i < n; ++i) {
            double d8 = (double)i / (double)n;
            for (int j = 0; j < n2; ++j) {
                double d9 = n2 == 1 ? 0.0 : (double)j / (double)(n2 - 1) - 0.5;
                double d10 = d9 * d7;
                double d11 = (d2 + d8 + d10) % 1.0;
                if (d11 < 0.0) {
                    d11 += 1.0;
                }
                double d12 = d11 * 6.2831854820251465;
                double d13 = 1.0 + d9 * 0.25;
                class_243 class_2433 = new class_243(class_2432.field_1352 + Math.cos(d12) * (double)f4 * d13, d6, class_2432.field_1350 + Math.sin(d12) * (double)f4 * d13);
                Color color3 = ColorUtils.fade(new Color(color.getRed(), color.getGreen(), color.getBlue(), n3), new Color(255, 255, 255, n3), 0.25f);
                Color color4 = ColorUtils.fade(color3, new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), n3), f3);
                Render3D.renderBillboardTexture(class_2433, f5, Gu, color4.getRGB(), bl, true);
                ++n4;
            }
        }
        TargetEspProfiler.incrementCounter("circleSprites", n4);
    }

    private void c(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (this.GD == null || TargetEspModule.aiG.field_1724 == null) {
            return;
        }
        float f = matrixStackStage.k().method_60637(true);
        float f2 = this.GB.rk();
        if (f2 <= 0.01f) {
            return;
        }
        float f3 = TargetEspModule.z(f2);
        class_243 class_2432 = this.GD.method_30950(f);
        float f4 = ((float)TargetEspModule.aiG.field_1724.field_6012 + f) * 6.0f;
        float f5 = this.GD.method_17682();
        float f6 = this.GD.method_17681() * 0.5f;
        int n = Math.max(1, Math.round(((Float)this.GA.jT()).floatValue()));
        int n2 = this.jv();
        for (int i = 0; i < n; ++i) {
            float[] fArray = this.a(i, n, f4, f6, f5);
            float f7 = 0.18f * f3;
            int n3 = TargetEspModule.g(n2, f2 * 0.3f);
            Render3D.renderBillboardTexture(class_2432.method_1031((double)fArray[0], (double)fArray[1], (double)fArray[2]), f7 * 3.6f, Gu, n3, true, true);
        }
    }

    private void b(class_4587 class_45872, float f) {
        float f2 = this.GB.rk();
        if (f2 <= 0.01f) {
            return;
        }
        float f3 = TargetEspModule.z(f2);
        class_4184 class_41842 = TargetEspModule.aiG.field_1773.method_19418();
        class_243 class_2432 = this.GD.method_30950(f);
        class_243 class_2433 = class_41842.method_19326();
        class_243 class_2434 = class_2432.method_1020(class_2433);
        float f4 = ((float)TargetEspModule.aiG.field_1724.field_6012 + f) * 6.0f;
        float f5 = this.GD.method_17682();
        float f6 = this.GD.method_17681() * 0.5f;
        int n = Math.max(1, Math.round(((Float)this.GA.jT()).floatValue()));
        int n2 = this.jv();
        class_45872.method_22903();
        class_45872.method_22904(class_2434.field_1352, class_2434.field_1351, class_2434.field_1350);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((class_10156)class_10142.field_53876);
        class_289 class_2892 = class_289.method_1348();
        class_287 class_2872 = class_2892.method_60827(class_293.class_5596.field_27379, class_290.field_1576);
        for (int i = 0; i < n; ++i) {
            float[] fArray = this.a(i, n, f4, f6, f5);
            float f7 = 0.18f * f3;
            this.a(class_2872, class_45872, fArray[0], fArray[1], fArray[2], f7, fArray[3], n2, f2 * 0.7f);
        }
        class_286.method_43433((class_9801)class_2872.method_60800());
        class_45872.method_22909();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private float[] a(int n, int n2, float f, float f2, float f3) {
        float f4 = (float)Math.sin((float)n * 1.7f + 0.3f) * 0.5f + 0.5f;
        float f5 = (float)Math.cos((float)n * 2.3f + 0.7f) * 0.5f + 0.5f;
        float f6 = (float)Math.sin((float)n * 3.1f + 1.1f) * 0.5f + 0.5f;
        float f7 = f + (float)n * (360.0f / (float)n2) + f4 * 12.0f;
        float f8 = f2 + 0.25f + f6 * 0.15f;
        float f9 = f8 * (float)Math.cos(Math.toRadians(f7));
        float f10 = f8 * (float)Math.sin(Math.toRadians(f7));
        float f11 = f5 * f3;
        return new float[]{f9, f11, f10, f7};
    }

    private void a(class_287 class_2872, class_4587 class_45872, float f, float f2, float f3, float f4, float f5, int n, float f6) {
        class_45872.method_22903();
        class_45872.method_46416(f, f2, f3);
        class_45872.method_22907(class_7833.field_40716.rotationDegrees(-f5 + 90.0f));
        class_45872.method_22905(f4, f4, f4);
        class_4587.class_4665 class_46652 = class_45872.method_23760();
        int n2 = n >> 16 & 0xFF;
        int n3 = n >> 8 & 0xFF;
        int n4 = n & 0xFF;
        int n5 = (int)(180.0f * f6);
        int n6 = Math.min(255, (int)((float)n2 * 1.3f));
        int n7 = Math.min(255, (int)((float)n3 * 1.3f));
        int n8 = Math.min(255, (int)((float)n4 * 1.3f));
        int n9 = (int)((float)n2 * 0.6f);
        int n10 = (int)((float)n3 * 0.6f);
        int n11 = (int)((float)n4 * 0.6f);
        float f7 = 0.5f;
        float f8 = 1.0f;
        this.a(class_2872, class_46652, 0.0f, 0.0f, f8, -f7, 0.0f, 0.0f, 0.0f, f7, 0.0f, n6, n7, n8, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, f8, 0.0f, f7, 0.0f, f7, 0.0f, 0.0f, n6, n7, n8, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, f8, f7, 0.0f, 0.0f, 0.0f, -f7, 0.0f, n2, n3, n4, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, f8, 0.0f, -f7, 0.0f, -f7, 0.0f, 0.0f, n2, n3, n4, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, -f8, 0.0f, f7, 0.0f, -f7, 0.0f, 0.0f, n9, n10, n11, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, -f8, f7, 0.0f, 0.0f, 0.0f, f7, 0.0f, n9, n10, n11, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, -f8, 0.0f, -f7, 0.0f, f7, 0.0f, 0.0f, n9, n10, n11, n5);
        this.a(class_2872, class_46652, 0.0f, 0.0f, -f8, -f7, 0.0f, 0.0f, 0.0f, -f7, 0.0f, n9, n10, n11, n5);
        class_45872.method_22909();
    }

    private void a(class_287 class_2872, class_4587.class_4665 class_46652, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int n, int n2, int n3, int n4) {
        class_2872.method_56824(class_46652, f, f2, f3).method_1336(n, n2, n3, n4);
        class_2872.method_56824(class_46652, f4, f5, f6).method_1336(n, n2, n3, n4);
        class_2872.method_56824(class_46652, f7, f8, f9).method_1336(n, n2, n3, n4);
    }

    private static float z(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }

    private int jv() {
        Color color = this.jr();
        return color.getRGB();
    }

    private static int g(int n, float f) {
        int n2 = Math.max(0, Math.min(255, (int)(255.0f * f)));
        return n2 << 24 | n & 0xFFFFFF;
    }

    public static enum TargetStyle
    implements NamedOption  {
        GO("settings.targetesp.mode.original"),
        GP("settings.targetesp.mode.ghosts"),
        GQ("settings.targetesp.mode.circles"),
        GR("settings.targetesp.mode.crystals");
        private final String GS;

@Override
        public String getName() {
            return this.GS;
        }

        @Generated
        private TargetStyle(String string2) {
            this.GS = string2;
        }

}
}

