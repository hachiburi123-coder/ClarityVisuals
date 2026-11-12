package ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_630;
import net.minecraft.class_638;
import net.minecraft.class_745;
import net.minecraft.class_746;
import net.minecraft.class_10055;
import net.minecraft.class_10142;
import net.minecraft.class_10156;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.modules.impl.render.EmotionsModule;
import ru.method.clarityvisuals.utils.client.web.EmotionNetwork;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.Render2D;

/**
 * Мигрировано с auravisual (модуль Emotions / EmotionWheelManager, исходник на 1.21.11).
 * Круг выбора эмоций: держишь бинд — открывается колесо, отпускаешь — эмоция ставится
 * поверх текущей модельной позы (BipedEntityModel.setAngles), плюс синхронизация через websocket.
 */
public final class EmotionWheelManager {
    private static final int PREVIEW_ENTITY_ID = -784231;
    private static final float FADE_IN_MS = 280.0f;
    private static final float FADE_OUT_MS = 320.0f;
    private static final EmotionType[] EMOTIONS = EmotionType.values();
    public static final EmotionWheelManager INSTANCE = new EmotionWheelManager();
    private static final class_310 aiG = class_310.method_1551();
    private final Map<UUID, ActiveEmotion> active = new ConcurrentHashMap<UUID, ActiveEmotion>();
    private final Map<String, ActiveEmotion> activeByName = new ConcurrentHashMap<String, ActiveEmotion>();
    private final float[] slotHover = new float[EMOTIONS.length];
    private boolean wheelOpen;
    private boolean closingVisual;
    private float closeProgress;
    private boolean keyWasDown;
    private float wheelAnim;
    private long lastAnimMs;
    private double cursorX;
    private double cursorY;
    private int stickySlot = -1;
    private EmotionType hovered;
    private EmotionType previewEmotion = EmotionType.HELLO;
    private long previewStartedAtMs;
    private StevePreviewEntity stevePreview;
    private class_638 stevePreviewWorld;
    private EmotionType stevePreviewEmotion = EmotionType.HELLO;
    private boolean lookFrozen;
    private float savedYaw;
    private float savedPitch;
    private float savedYaw2;
    private float savedPitch2;
    private float savedBody;
    private float savedYawHead;
    private float savedLev;
    private float savedLev2;

    private EmotionWheelManager() {
    }

    public void tick(EmotionsModule emotionsModule) {
        if (emotionsModule == null) {
            return;
        }
        if (aiG.field_1724 != null && aiG.field_1687 != null) {
            if (aiG.field_1755 != null && this.wheelOpen) {
                this.closeWheel(true);
            }
            boolean bl = emotionsModule.isWheelKeyHeld();
            if (bl && !this.keyWasDown && aiG.field_1755 == null) {
                this.openWheel();
            }
            if (!bl && this.keyWasDown && this.wheelOpen) {
                EmotionType emotionType = this.hovered;
                this.closeWheel(false);
                if (emotionType != null) {
                    this.play((class_1657)aiG.field_1724, emotionType, true);
                }
            }
            this.keyWasDown = bl;
            if (this.wheelOpen) {
                this.readCursor();
                this.updateHover();
            }
            Iterator<Map.Entry<UUID, ActiveEmotion>> iterator = this.active.entrySet().iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().getValue().expired()) continue;
                iterator.remove();
            }
            Iterator<Map.Entry<String, ActiveEmotion>> iterator2 = this.activeByName.entrySet().iterator();
            while (iterator2.hasNext()) {
                if (!iterator2.next().getValue().expired()) continue;
                iterator2.remove();
            }
        } else {            this.closeWheel(true);
            this.active.clear();
            this.activeByName.clear();
        }
        EmotionNetwork.INSTANCE.tick();
    }

    public void playRemoteEmotion(String string, String string2) {
        if (string == null || string2 == null) {
            return;
        }
        String string3 = string2.trim();
        EmotionType emotionType = null;
        for (EmotionType emotionType2 : EMOTIONS) {
            if (!(emotionType2.name().equalsIgnoreCase(string3) || emotionType2.title().equalsIgnoreCase(string3) || emotionType2.name().replace("_", "").equalsIgnoreCase(string3.replace("_", "")))) continue;
            emotionType = emotionType2;
            break;
        }
        if (emotionType == null) {
            return;
        }
        ActiveEmotion activeEmotion = new ActiveEmotion(emotionType, System.currentTimeMillis(), false);
        this.activeByName.put(string.trim().toLowerCase(Locale.ROOT), activeEmotion);
        if (aiG.field_1687 == null) {
            return;
        }
        for (class_1657 class_16572 : aiG.field_1687.method_18456()) {
            String string4 = class_16572.method_7334() != null && class_16572.method_7334().getName() != null ? class_16572.method_7334().getName() : "";
            String string5 = class_16572.method_5476() != null ? class_16572.method_5476().getString() : "";
            if (!(string4.equalsIgnoreCase(string) || string5.equalsIgnoreCase(string))) continue;
            if (aiG.field_1724 != null && class_16572.method_7334() != null && aiG.field_1724.method_7334() != null && class_16572.method_7334().getId().equals(aiG.field_1724.method_7334().getId())) {
                return;
            }
            if (class_16572.method_7334() == null) {
                continue;
            }
            this.active.put(class_16572.method_7334().getId(), activeEmotion);
            return;
        }
    }

    public void play(class_1657 class_16572, EmotionType emotionType, boolean bl) {
        ActiveEmotion activeEmotion = new ActiveEmotion(emotionType, System.currentTimeMillis(), bl);
        if (class_16572.method_7334() != null) {
            this.active.put(class_16572.method_7334().getId(), activeEmotion);
        }
        if (class_16572.method_5476() != null) {
            this.activeByName.put(class_16572.method_5476().getString().trim().toLowerCase(Locale.ROOT), activeEmotion);
        }
        if (class_16572.method_7334() != null && class_16572.method_7334().getName() != null) {
            this.activeByName.put(class_16572.method_7334().getName().trim().toLowerCase(Locale.ROOT), activeEmotion);
        }
        if (bl) {
            EmotionNetwork.INSTANCE.sendEmotion(emotionType.name());
        }
    }

    public void render(class_332 class_3322) {
        if (aiG.field_1724 == null || aiG.field_1687 == null || class_3322 == null) {
            return;
        }
        long l = System.currentTimeMillis();
        float f = this.lastAnimMs == 0L ? 0.016f : class_3532.method_15363((float)(l - this.lastAnimMs) / 1000.0f, 0.001f, 0.05f);
        this.lastAnimMs = l;
        this.wheelAnim = EmotionWheelManager.approach(this.wheelAnim, this.wheelOpen ? 1.0f : 0.0f, f, 10.0f);
        if (this.closingVisual) {
            this.closeProgress = Math.min(1.0f, this.closeProgress + f / 0.5f);
            if (this.closeProgress >= 1.0f) {
                this.closingVisual = false;
                this.wheelAnim = 0.0f;
                return;
            }
        }
        if (this.wheelAnim <= 0.001f && !this.wheelOpen && !this.closingVisual) {
            this.closingVisual = false;
            return;
        }
        float f2 = EmotionWheelManager.easeOutCubic(class_3532.method_15363(this.wheelAnim, 0.0f, 1.0f));
        float f3 = this.closingVisual ? 1.0f - EmotionWheelManager.smoothStep(class_3532.method_15363(this.closeProgress / 0.72f, 0.0f, 1.0f)) : 1.0f;
        float f4 = this.closingVisual ? f3 : f2;
        class_4587 class_45872 = class_3322.method_51448();
        int n = aiG.method_22683().method_4486();
        int n2 = aiG.method_22683().method_4502();
        float f5 = (float)n / 2.0f;
        float f6 = (float)n2 / 2.0f;
        if (this.wheelOpen) {
            this.readCursor();
            this.updateHover();
        }
        Render2D.drawRoundedRect(class_45872, 0.0f, 0.0f, (float)n, (float)n2, 0.0f, EmotionWheelManager.alphaColor(0, (int)(86.0f * f4)));
        float f7 = this.closingVisual ? 1.0f : 0.76f + 0.24f * f2;
        float f8 = 142.0f * f7;
        float f9 = 48.0f * f7;
        float f10 = 96.0f * f7;
        float f11 = this.closingVisual ? EmotionWheelManager.smoothStep(this.closeProgress) * 42.0f : 0.0f;
        Render2D.drawBlurredRect(class_45872, f5 - f8, f6 - f8, f8 * 2.0f, f8 * 2.0f, f8, 22.0f, EmotionWheelManager.alphaColor(0xFFFFFF, (int)(205.0f * f4)));
        for (int i = 0; i < EMOTIONS.length; ++i) {
            float f12 = this.stickySlot == i ? 1.0f : 0.0f;
            this.slotHover[i] = EmotionWheelManager.approach(this.slotHover[i], f12, f, 14.0f);
            double d = Math.toRadians(-90.0 + (double)(i * 60));
            float f13 = (float)Math.cos(d) * f11;
            float f14 = (float)Math.sin(d) * f11;
            float f15 = f5 + f13;
            float f16 = f6 + f14;
            float f17 = -120.0f + (float)(i * 60);
            int n3 = EmotionWheelManager.lerpColor(592397, 1908516, this.slotHover[i]);
            EmotionWheelManager.drawSector(class_45872, f15, f16, f9, f8, f17, 60.0f, EmotionWheelManager.alphaColor(n3, (int)((228.0f + 15.0f * this.slotHover[i]) * f4)));
            double d2 = Math.toRadians(f17);
            float f18 = f15 + (float)Math.cos(d2) * (f9 + 1.0f);
            float f19 = f16 + (float)Math.sin(d2) * (f9 + 1.0f);
            float f20 = f15 + (float)Math.cos(d2) * (f8 - 1.0f);
            float f21 = f16 + (float)Math.sin(d2) * (f8 - 1.0f);
            Render2D.drawLine(class_45872, f18, f19, f20, f21, 0.72f, new ColorRGBA(EmotionWheelManager.alphaColor(6909043, (int)(72.0f * f4))));
            float f22 = f15 + (float)Math.cos(d) * f10;
            float f23 = f16 + (float)Math.sin(d) * f10;
            int n4 = Math.max(2, Math.round((39.0f + 4.0f * this.slotHover[i]) * f7));
            int n5 = class_3532.method_15340(Math.round(255.0f * f4), 0, 255);
            boolean bl = this.wheelOpen && this.stickySlot == i && this.slotHover[i] > 0.28f && aiG.field_1724 != null;
            int n6 = bl ? class_3532.method_15340(Math.round((float)n5 * (1.0f - this.slotHover[i])), 0, 255) : n5;
            if (n6 > 2) {
                Render2D.drawTexture(class_45872, f22 - (float)n4 / 2.0f, f23 - (float)n4 / 2.0f, (float)n4, (float)n4, 0.0f, EMOTIONS[i].texture(), EmotionWheelManager.alphaColor(0xFFFFFF, n6));
            }
            if (bl) {
                this.renderEmotionPreview(class_3322, Math.round(f22), Math.round(f23), EMOTIONS[i]);
            }
        }
        float f24 = this.closingVisual ? 1.0f - 0.5f * EmotionWheelManager.smoothStep(class_3532.method_15363(this.closeProgress / 0.72f, 0.0f, 1.0f)) : 1.0f;
        float f25 = (f9 - 1.2f) * f24;
        if (f25 > 0.25f) {
            Render2D.drawCircle(class_45872, f5, f6, f25, EmotionWheelManager.alphaColor(395018, (int)(247.0f * f4)));
            Render2D.drawRing(class_45872, f5, f6, f25, 1.3f, EmotionWheelManager.alphaColor(6448752, (int)(92.0f * f4)));
        }
        Color color = EmotionWheelManager.alphaColor(this.hovered == null ? 14211550 : 7830147, (int)(215.0f * f4));
        float f26 = 6.5f * f7 * f24;
        if (f26 > 0.25f) {
            Render2D.drawLine(class_45872, f5 - f26, f6 - f26, f5 + f26, f6 + f26, 1.45f, new ColorRGBA(color));
            Render2D.drawLine(class_45872, f5 + f26, f6 - f26, f5 - f26, f6 + f26, 1.45f, new ColorRGBA(color));
        }
    }

    /**
     * Позирование модели после ванильного setAngles: мозги эмоции, применённые к стейту игрока.
     * Вызывается из BipedEntityModelMixin. Модели превью задаёт собственная эмоция.
     */
    public void applyToModel(class_10055 class_100552, class_630 class_6302, class_630 class_6303, class_630 class_6304, class_630 class_6305, class_630 class_6306, class_630 class_6307, class_630 class_6308) {
        if (aiG.field_1687 == null) {
            return;
        }
        boolean bl = class_100552.field_53528 == PREVIEW_ENTITY_ID && this.stevePreview != null;
        class_1297 class_12972 = bl ? this.stevePreview : aiG.field_1687.method_8469(class_100552.field_53528);
        ActiveEmotion activeEmotion = null;
        class_1657 class_16572 = class_12972 instanceof class_1657 ? (class_1657)((Object)class_12972) : null;
        if (class_16572 != null) {
            if (class_16572.method_7334() != null) {
                activeEmotion = this.active.get(class_16572.method_7334().getId());
            }
            if (activeEmotion == null && class_16572.method_5476() != null) {
                activeEmotion = this.activeByName.get(class_16572.method_5476().getString().trim().toLowerCase(Locale.ROOT));
            }
            if (activeEmotion == null && class_16572.method_7334() != null && class_16572.method_7334().getName() != null) {
                activeEmotion = this.activeByName.get(class_16572.method_7334().getName().trim().toLowerCase(Locale.ROOT));
            }
        }
        if (activeEmotion == null && class_100552.field_53525 != null) {
            activeEmotion = this.activeByName.get(class_100552.field_53525.getString().trim().toLowerCase(Locale.ROOT));
        }
        EmotionType emotionType = null;
        float f = 0.0f;
        long l = 0L;
        if (bl) {
            emotionType = this.stevePreviewEmotion;
            l = this.previewStartedAtMs;
            float f2 = class_3532.method_15363((float)(System.currentTimeMillis() - this.previewStartedAtMs) / 520.0f, 0.0f, 1.0f);
            f = EmotionWheelManager.smoothStep(f2);
        } else if (activeEmotion != null) {
            emotionType = activeEmotion.type;
            f = activeEmotion.blend();
            l = activeEmotion.startedAt;
        }
        if (emotionType == null || f <= 0.001f) {
            return;
        }
        long l2 = Math.max(0L, System.currentTimeMillis() - l);
        boolean bl2 = bl || class_16572 != null && class_16572 != aiG.field_1724 || !aiG.field_1690.method_31044().method_31034();
        switch (emotionType) {
            case DEB: {
                this.poseDeb(f, bl2, class_6302, class_6305, class_6306);
                break;
            }
            case FLOSS: {
                this.poseFloss(f, l2, bl2, class_6304, class_6305, class_6306, class_6307, class_6308);
                break;
            }
            case MASTURBATE: {
                this.poseMasturbate(f, l2, bl2, class_16572, class_6304, class_6305, class_6306, class_6307, class_6308);
                break;
            }
            case HELLO: {
                this.poseHello(f, l2, bl2, class_6305);
                break;
            }
            case GET_GRIDDY: {
                this.poseGriddy(f, l2, bl2, class_6302, class_6304, class_6305, class_6306, class_6307, class_6308);
                break;
            }
            default: {
                this.poseHappy(f, bl2, class_6305, class_6306);
            }
        }
        if (bl) {
            class_6302.field_3654 = 0.0f;
            class_6302.field_3675 = 0.0f;
            class_6302.field_3674 = 0.0f;
        }
        class_6303.field_3654 = class_6302.field_3654;
        class_6303.field_3675 = class_6302.field_3675;
        class_6303.field_3674 = class_6302.field_3674;
    }

    public void clearActive() {
        this.active.clear();
        this.activeByName.clear();
    }

    public void closeWheel(boolean bl) {
        boolean bl2 = this.wheelOpen;
        this.wheelOpen = false;
        if (bl2 && !bl) {
            this.closingVisual = true;
            this.closeProgress = 0.0f;
        }
        if (bl) {
            this.hovered = null;
            this.stickySlot = -1;
        }
        if (bl2 && aiG.field_1755 == null && aiG.method_22683() != null) {
            aiG.field_1729.method_1612();
        }
    }

    public boolean isWheelOpen() {
        return this.wheelOpen;
    }

    void openWheel() {
        this.wheelOpen = true;
        this.closingVisual = false;
        this.closeProgress = 0.0f;
        this.cursorY = 0.0;
        this.cursorX = 0.0;
        this.hovered = null;
        this.stickySlot = -1;
        this.previewEmotion = EmotionType.HELLO;
        this.previewStartedAtMs = System.currentTimeMillis();
        aiG.field_1729.method_1610();
        GLFW.glfwSetCursorPos(aiG.method_22683().method_4490(), (double)aiG.method_22683().method_4480() * 0.5, (double)aiG.method_22683().method_4507() * 0.5);
    }

    /**
     * Захват камеры пока колесо открыто (вызывается из MouseMixin вокруг updateMouse,
     * паттерн FreeLookModule.a/b).
     */
    public void captureLook(class_746 class_7462) {
        if (class_7462 == null || !this.wheelOpen) {
            this.lookFrozen = false;
            return;
        }
        this.lookFrozen = true;
        this.savedYaw = class_7462.method_36454();
        this.savedPitch = class_7462.method_36455();
        this.savedYaw2 = class_7462.field_5982;
        this.savedPitch2 = class_7462.field_6004;
        this.savedBody = class_7462.field_6283;
        this.savedYawHead = class_7462.field_6220;
        this.savedLev = class_7462.field_6241;
        this.savedLev2 = class_7462.field_6259;
    }

    public void restoreLook(class_746 class_7462) {
        if (class_7462 == null || !this.lookFrozen) {
            return;
        }
        this.lookFrozen = false;
        class_7462.method_36456(this.savedYaw);
        class_7462.method_36457(this.savedPitch);
        class_7462.field_5982 = this.savedYaw2;
        class_7462.field_6004 = this.savedPitch2;
        class_7462.field_6283 = this.savedBody;
        class_7462.field_6220 = this.savedYawHead;
        class_7462.field_6241 = this.savedLev;
        class_7462.field_6259 = this.savedLev2;
    }

    void updateHover() {
        float f = 0.76f + 0.24f * EmotionWheelManager.easeOutCubic(class_3532.method_15363(this.wheelAnim, 0.0f, 1.0f));
        double d = Math.hypot(this.cursorX, this.cursorY);
        if (d < 48.0 * (double)f) {
            this.hovered = null;
            this.stickySlot = -1;
            return;
        }
        double d2 = Math.toDegrees(Math.atan2(this.cursorY, this.cursorX));
        double d3 = (d2 + 120.0) % 360.0;
        if (d3 < 0.0) {
            d3 += 360.0;
        }
        this.stickySlot = class_3532.method_15340((int)Math.floor(d3 / 60.0), 0, EMOTIONS.length - 1);
        this.hovered = EMOTIONS[this.stickySlot];
        if (this.previewEmotion != this.hovered) {
            this.previewEmotion = this.hovered;
            this.previewStartedAtMs = System.currentTimeMillis();
        }
    }

    void readCursor() {
        double d = aiG.field_1729.method_1603() / (double)aiG.method_22683().method_4495();
        double d2 = aiG.field_1729.method_1604() / (double)aiG.method_22683().method_4495();
        this.cursorX = d - (double)aiG.method_22683().method_4486() * 0.5;
        this.cursorY = d2 - (double)aiG.method_22683().method_4502() * 0.5;
    }

    private void renderEmotionPreview(class_332 class_3322, int n, int n2, EmotionType emotionType) {
        if (aiG.field_1687 == null) {
            return;
        }
        if (this.stevePreview == null || this.stevePreviewWorld != aiG.field_1687) {
            this.stevePreviewWorld = aiG.field_1687;
            GameProfile gameProfile = new GameProfile(new UUID(0L, 0L), "Steve");
            this.stevePreview = new StevePreviewEntity(aiG.field_1687, gameProfile);
            this.stevePreview.method_5838(PREVIEW_ENTITY_ID);
        }
        this.stevePreviewEmotion = emotionType;
        try {
            class_490.method_2486(class_3322, n - 25, n2 - 28, n + 25, n2 + 29, 23, 0.0625f, 6.0f, 0.0f, this.stevePreview);
        }
        catch (Throwable throwable) {
            // Скины с нулевым UUID иногда роняют оверлей — превью не критичен.
        }
    }

    private static void drawSector(class_4587 class_45872, float f, float f2, float f3, float f4, float f5, float f6, Color color) {
        if (color.getAlpha() <= 0 || f6 <= 0.0f || f4 <= 0.0f || f3 >= f4) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        class_287 class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27380, class_290.field_1576);
        int n = 24;
        for (int i = 0; i <= n; ++i) {
            double d = Math.toRadians(f5 + f6 * (double)((float)i / (float)n));
            float f7 = (float)Math.cos(d);
            float f8 = (float)Math.sin(d);
            class_2872.method_22918(matrix4f, f + f7 * f4, f2 + f8 * f4, 0.0f).method_39415(color.getRGB());
            class_2872.method_22918(matrix4f, f + f7 * f3, f2 + f8 * f3, 0.0f).method_39415(color.getRGB());
        }
        RenderSystem.setShader((class_10156)class_10142.field_53876);
        class_286.method_43433((class_9801)class_2872.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    void poseDeb(float f, boolean bl, class_630 class_6302, class_630 class_6303, class_630 class_6304) {
        if (bl) {
            EmotionWheelManager.armXZ(class_6303, f, -0.7f, -2.0f);
            EmotionWheelManager.armXZ(class_6304, f, -0.7f, -2.0f);
        }
        class_6302.field_3654 = class_3532.method_16439(f, class_6302.field_3654, 0.7f);
        class_6302.field_3675 = class_3532.method_16439(f, class_6302.field_3675, 0.5f);
        class_6302.field_3674 = class_3532.method_16439(f, class_6302.field_3674, 0.0f);
    }

    void poseFloss(float f, long l, boolean bl, class_630 class_6302, class_630 class_6303, class_630 class_6304, class_630 class_6305, class_630 class_6306) {
        float f2 = EmotionWheelManager.tri(l, 250L);
        if (bl) {
            EmotionWheelManager.armXZ(class_6303, f, 0.5f - EmotionWheelManager.tri(l, 500L), 1.0f - 2.0f * f2);
            EmotionWheelManager.armXZ(class_6304, f, 0.5f - EmotionWheelManager.tri(l + 250L, 500L), 1.5f - 2.0f * f2);
        }
        class_6302.field_3674 = class_3532.method_16439(f, class_6302.field_3674, 0.3f * f2 - 0.15f);
        class_6305.field_3657 = class_3532.method_16439(f, class_6305.field_3657, -0.5f - 3.0f * f2);
        class_6306.field_3657 = class_3532.method_16439(f, class_6306.field_3657, 3.0f - 2.5f * f2);
    }

    void poseHello(float f, long l, boolean bl, class_630 class_6302) {
        if (bl) {
            EmotionWheelManager.armXZ(class_6302, f, -0.2f, 2.75f - 0.25f * EmotionWheelManager.tri(l, 250L));
        }
    }

    void poseHappy(float f, boolean bl, class_630 class_6302, class_630 class_6303) {
        if (bl) {
            EmotionWheelManager.armXZ(class_6302, f, 0.0f, 2.75f);
            EmotionWheelManager.armXZ(class_6303, f, 0.0f, -2.75f);
        }
    }

    void poseMasturbate(float f, long l, boolean bl, class_1657 class_16572, class_630 class_6302, class_630 class_6303, class_630 class_6304, class_630 class_6305, class_630 class_6306) {
        float f2 = EmotionWheelManager.tri(l, 100L);
        if (class_16572 != null && class_16572.method_5715()) {
            if (bl) {
                EmotionWheelManager.armXZ(class_6303, f, 0.7f - 0.7f * f2, -0.55f);
            }
            return;
        }
        if (bl) {
            EmotionWheelManager.armXZ(class_6303, f, -0.3f - 0.7f * f2, -0.55f);
            EmotionWheelManager.armXZ(class_6304, f, 0.3f, 1.0f);
        }
        class_6302.field_3654 = class_3532.method_16439(f, class_6302.field_3654, -0.1f);
        class_6305.field_3655 = class_3532.method_16439(f, class_6305.field_3655, -1.0f);
        class_6306.field_3655 = class_3532.method_16439(f, class_6306.field_3655, -1.0f);
    }

    void poseGriddy(float f, long l, boolean bl, class_630 class_6302, class_630 class_6303, class_630 class_6304, class_630 class_6305, class_630 class_6306, class_630 class_6307) {
        float f2 = EmotionWheelManager.tri(l, 350L);
        float f3 = EmotionWheelManager.griddyEye(l);
        if (bl) {
            float f4 = 1.0f - 1.5f * f2 - 3.5f * f3;
            EmotionWheelManager.armXZ(class_6304, f, f4, 0.5f - 0.75f * f2);
            EmotionWheelManager.armXZ(class_6305, f, f4, -0.5f + 0.75f * f2);
        }
        class_6303.field_3654 = class_3532.method_16439(f, class_6303.field_3654, 0.2f - 0.05f * f2);
        class_6306.field_3655 = class_3532.method_16439(f, class_6306.field_3655, 2.0f - 0.5f * f2);
        class_6307.field_3655 = class_3532.method_16439(f, class_6307.field_3655, 2.0f - 0.5f * f2);
        class_6306.field_3654 = class_3532.method_16439(f, class_6306.field_3654, 0.3f * EmotionWheelManager.tri(l, 300L) - 0.3f);
        class_6307.field_3654 = class_3532.method_16439(f, class_6307.field_3654, -0.3f * EmotionWheelManager.tri(l, 300L) - 0.1f);
        class_6302.field_3675 = class_3532.method_16439(f, class_6302.field_3675, -0.4f + 0.4f * f3);
        class_6302.field_3654 = class_3532.method_16439(f, class_6302.field_3654, -0.3f * f3);
    }

    static void armXZ(class_630 class_6302, float f, float f2, float f3) {
        class_6302.field_3654 = class_3532.method_16439(f, class_6302.field_3654, f2);
        class_6302.field_3674 = class_3532.method_16439(f, class_6302.field_3674, f3);
    }

    static float tri(long l, long l2) {
        long l3 = l2 * 2L;
        long l4 = l % l3;
        return l4 < l2 ? (float)l4 / (float)l2 : 2.0f - (float)l4 / (float)l2;
    }

    static float griddyEye(long l) {
        long l2 = l % 3100L;
        return l2 < 2100L ? 0.0f : EmotionWheelManager.tri(l2 - 2100L, 500L);
    }

    static float approach(float f, float f2, float f3, float f4) {
        float f5 = 1.0f - (float)Math.exp(-f4 * f3);
        return f + (f2 - f) * f5;
    }

    static float easeOutCubic(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }

    static float smoothStep(float f) {
        float f2 = class_3532.method_15363(f, 0.0f, 1.0f);
        return f2 * f2 * (3.0f - 2.0f * f2);
    }

    static Color alphaColor(int n, int n2) {
        int n3 = class_3532.method_15340(n2, 0, 255);
        return new Color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, n3);
    }

    static int lerpColor(int n, int n2, float f) {
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        int n6 = n2 >> 16 & 0xFF;
        int n7 = n2 >> 8 & 0xFF;
        int n8 = n2 & 0xFF;
        int n9 = (int)((float)n3 + ((float)n6 - (float)n3) * f);
        int n10 = (int)((float)n4 + ((float)n7 - (float)n4) * f);
        int n11 = (int)((float)n5 + ((float)n8 - (float)n5) * f);
        return n9 << 16 | n10 << 8 | n11;
    }

    /**
     * Активная эмоция конкретного игрока: тип, старт, blend-фаза входа/выхода.
     */
    private static final class ActiveEmotion {
        private final EmotionType type;
        private final long startedAt;
        private final boolean local;

        ActiveEmotion(EmotionType emotionType, long l, boolean bl) {
            this.type = emotionType;
            this.startedAt = l;
            this.local = bl;
        }

        private boolean expired() {
            return System.currentTimeMillis() - this.startedAt >= this.type.durationMs();
        }

        private float blend() {
            long l = System.currentTimeMillis() - this.startedAt;
            long l2 = Math.max(0L, this.type.durationMs() - l);
            float f = class_3532.method_15363((float)l / FADE_IN_MS, 0.0f, 1.0f);
            float f2 = class_3532.method_15363((float)l2 / FADE_OUT_MS, 0.0f, 1.0f);
            float f3 = Math.min(f, f2);
            return f3 * f3 * (3.0f - 2.0f * f3);
        }
    }
}
