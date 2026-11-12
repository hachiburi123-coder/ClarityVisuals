package ru.method.clarityvisuals.screen.clickgui.legion;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.api.setting.BindSetting;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ButtonSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.MultiSelectSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.Setting;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.api.setting.StringSetting;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.manager.ConfigManager;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.screen.clickgui.redesign.ClickGuiIcons;
import ru.method.clarityvisuals.ui.ClientTheme;
import ru.method.clarityvisuals.ui.UiThemeController;
import ru.method.clarityvisuals.utils.client.web.HwEventsApi;
import ru.method.clarityvisuals.utils.render.ClickGuiRenderContext;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.LiquidGlassReflectionCache;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

/**
 * ClickGUI РІ СЃС‚РёР»Рµ Legion (1.21.11 -> РїРѕСЂС‚РёСЂРѕРІР°РЅРѕ РЅР° РЅР°С€ 1.21.4 API).
 *
 * РЎРѕРѕС‚РІРµС‚СЃС‚РІРёРµ РѕСЂРёРіРёРЅР°Р»Сѓ:
 *   BG_WIDTH 400, BG_HEIGHT 320, CORNER_RADIUS 14, CARD_RADIUS 12
 *   Р»РµРІР°СЏ РєРѕР»РѕРЅРєР° 25% С€РёСЂРёРЅС‹, РєР°С‚РµРіРѕСЂРёРё РїРѕ 22px СЃ Р°РЅРёРјРёСЂРѕРІР°РЅРЅС‹Рј selBar
 *   РєР°СЂС‚РѕС‡РєРё РјРѕРґСѓР»РµР№ РІ 2 РєРѕР»РѕРЅРєРё, С‚СѓРјР±Р»РµСЂС‹-РїРёР»СЋР»Рё 16x9 СЃ squish-Р°РЅРёРјР°С†РёРµР№
 *   РїР°РЅРµР»СЊ РЅР°СЃС‚СЂРѕРµРє 155px С€РёСЂРёРЅРѕР№, blur + tint 0xFF0A0A0A
 *   pullAnim РїСЂРё РѕС‚РєСЂС‹С‚РёРё/Р·Р°РєСЂС‹С‚РёРё 0.12
 *
 * РћС‚Р»РёС‡РёСЏ РѕС‚ РѕСЂРёРіРёРЅР°Р»Р° (РѕСЃРѕР·РЅР°РЅРЅС‹Рµ):
 *   - Р°РІС‚РѕР·Р°РєСѓРїРєР° (AutoBuy), РјР°РєСЂРѕСЃС‹ Рё РєРѕРЅС„РёРіРё Legion РЅРµ РїРµСЂРµРЅРµСЃРµРЅС‹
 *   - РёС… ModuleStructure/Setting -> РЅР°С€Рё Module/Setting<?>
 *   - РёС… Render2D/Fonts -> РЅР°С€Рё, РЅРѕ LEGION_ICONS/LEGION_LOGO СѓР¶Рµ Р±С‹Р»Рё РІ РїСЂРѕРµРєС‚Рµ
 */
public class LegionClickGui
extends class_437
implements MinecraftClientAccess {

    private static final float BG_WIDTH = 400.0f;
    private static final float BG_HEIGHT = 320.0f;
    private static final float CORNER_RADIUS = 14.0f;
    private static final float CARD_RADIUS = 12.0f;
    private static final float LEFT_W = BG_WIDTH * 0.25f;
    private static final float CAT_ROW_H = 22.0f;
    private static final float MOD_ROW_H = 20.0f;
    private static final float SETTINGS_W = 155.0f;
    private static final float SET_ROW_H = 15.0f;
    private static final float SLIDER_ROW_H = 26.0f;
    private static final float THEME_MENU_W = 136.0f;
    private static final float THEME_ROW_H = 14.0f;
    private static final float THEME_HEADER_H = 18.0f;
    private static final long OPEN_ANIMATION_DURATION = 450L;
    /** Иконка клиента — та же, что слева в ватермарке. */
    private static final class_2960 CLIENT_ICON = ClarityVisuals.id("textures/gui/clarity_icon.png");

    private static LegionClickGui INSTANCE;

    // РїРѕР·РёС†РёСЏ/Р°РЅРёРјР°С†РёРё
    private float x;
    private float y;
    private float baseY;
    private float pullAnim;
    private boolean closing;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private float fixedW;
    private float fixedH;
    /** Масштаб отрисовки: дизайн-координаты GUI умножаются на него. */
    private float uiScale = 1.0f;
    /** Размер экрана, для которого последний раз считались позиция и масштаб. */
    private float lastW;
    private float lastH;
    /** true, когда позиция уже выставлена программно (не перетащена игроком). */
    private boolean centered;
    /** Один раз логируем геометрию при открытии — удобно для диагностики. */
    private boolean loggedFirstFrame;

    // РІС‹Р±СЂР°РЅРЅР°СЏ РєР°С‚РµРіРѕСЂРёСЏ
    private ModuleCategory selectedCategory;
    private ModuleCategory lastCategory;
    private float selBarY;
    private float moduleFade;
    private float scrollOffset;
    private float scrollTarget;

    // Р°РЅРёРјР°С†РёРё Р·РЅР°С‡РµРЅРёР№
    private final Map<String, Float> toggleAnims = new HashMap<>();
    private final Map<String, Float> boolSettingAnims = new HashMap<>();
    private final Map<String, Float> sliderProgressAnims = new HashMap<>();
    private final Map<String, Float> sliderDragAnims = new HashMap<>();

    // РѕС‚РєСЂС‹С‚Р°СЏ РїР°РЅРµР»СЊ РЅР°СЃС‚СЂРѕРµРє
    private String editingModule;
    private String closingModule;
    private float settingsAnim;
    private float settingsOffX;
    private float settingsOffY;
    private boolean settingsDragging;
    private float settingsDragOffX;
    private float settingsDragOffY;
    private float editingModuleX;
    private float editingModuleY;
    private float closingModuleX;
    private float closingModuleY;
    /** Внутренний скролл панели настроек (для модулей с большим числом строк). */
    private float settingsScroll;
    private float settingsScrollTarget;

    // РІС‹РїР°РґР°СЋС‰РёР№ СЃРїРёСЃРѕРє enum
    private String editingSelect;
    private String closingSelect;
    private float selectAnim;
    private float editingSelectX;
    private float editingSelectY;

    // РїРµСЂРµС‚Р°СЃРєРёРІР°РµРјС‹Р№ СЃР»Р°Р№РґРµСЂ
    private String draggingSlider;
    private String editingSliderText;
    private StringBuilder sliderTextInput;

    // Р±РёРЅРґС‹
    private String editingBind;
    private String bindModuleName;
    private String bindSettingName;

    // редактирование строковых настроек
    private String editingString;
    private String stringModuleName;
    private StringBuilder stringTextInput;

    // палитра цветов для ColorSetting в панели настроек
    private final LegionColorPicker colorPicker = new LegionColorPicker();
    private int stringTextLimit = 64;

    // меню выбора темы
    private boolean themeMenuOpen;
    private float themeMenuAnim;

    // РїРѕРёСЃРє
    private boolean searchFocused;
    private final StringBuilder searchInput = new StringBuilder();

    // ==================== HW Events (порт из clarity) ====================

    /** Виртуальная вкладка «HW Events»: не категория модулей, а живой список ивентов. */
    private boolean hwView;
    private static final long HW_FETCH_INTERVAL = 30000L;
    private static final long HW_SERVER_INTERVAL = 60000L;
    private static final float HW_CARD_H = 40.0f;
    private static final float HW_CARD_GAP = 5.0f;
    private static final float HW_CHIP_H = 15.0f;
    private static final float HW_CHIP_GAP = 4.0f;
    private final List<HwEventsApi.HwEvent> hwEvents = new ArrayList<>();
    private final Map<String, String> hwServers = new LinkedHashMap<>();
    private String hwSelectedServer = "";
    private boolean hwLoading;
    private long hwFetchTime;
    private long hwServersFetchTime;
    private float hwRefreshAnim;
    /** Фильтры: "" означают «показывать всё». */
    private String hwRarityFilter = "";
    private String hwEventFilter = "";
    /** Раскладка чипов, посчитанная рендером — клик-тест использует последний кадр. */
    private List<float[]> hwRarityChipRects = List.of();
    private List<String> hwRarityChipLabels = List.of();
    private List<float[]> hwEventChipRects = List.of();
    private List<String> hwEventChipLabels = List.of();
    private float hwFiltersBottom;
    private float hwRefreshX;
    private float hwRefreshY;

    // ==================== панель конфигов ====================

    private boolean configPanelOpen;
    private float configPanelAnim;
    private boolean configNameFocused;
    private final StringBuilder configNameInput = new StringBuilder();
    private float configScroll;
    private float configScrollTarget;
    private List<String> configNamesCache = List.of();
    private boolean configNamesDirty = true;
    private static final float CONFIG_PANEL_W = 168.0f;
    private static final float CONFIG_PANEL_H = 196.0f;
    private static final float CONFIG_ROW_H = 17.0f;
    /** Прямоугольник последнего кадра поля ввода и кнопки сохранения. */
    private float[] configFieldRect = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
    private float[] configSaveRect = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    private final Animation openAnimation = new Animation(OPEN_ANIMATION_DURATION, 1.0, true, Easing.aiT);

    public LegionClickGui() {
        super(class_2561.method_30163("clarityvisuals-legion-gui"));
        LegionClickGui.INSTANCE = this;
        ModuleCategory first = null;
        for (ModuleCategory category : ClarityVisuals.getInstance().getModuleManager().getCategories()) {
            if (category == ModuleCategory.qo) {
                continue;
            }
            if (first == null) {
                first = category;
            }
        }
        this.selectedCategory = first == null ? ModuleCategory.qj : first;
        this.lastCategory = this.selectedCategory;
        this.selBarY = 0.0f;
    }

    public static LegionClickGui getInstance() {
        return INSTANCE;
    }

    // ==================== helpers ====================

    private static int softTextColor(int alpha) {
        return ColorUtils.alpha(ColorUtils.getTextColor(), Math.max(0, Math.min(255, alpha))).getRGB();
    }

    private static int themeMain() {
        Color c = ColorUtils.getPrimaryColor();
        return c.getRGB();
    }

    /** Y верхнего края текста размера size, центрированного по вертикали в области высотой h от y. */
    private static float vCenter(float y, float h, float size) {
        return y + (h - Fonts.LEGION_INTER.getHeight(size)) / 2.0f;
    }

    private static int withAlpha(int rgb, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    private static int mixChannel(int a, int b, float t) {
        int ca = (a >> 16) & 0xFF, cb = (b >> 16) & 0xFF;
        int r = Math.round(ca + (cb - ca) * t);
        int na = (a >> 8) & 0xFF, nb = (b >> 8) & 0xFF;
        int g = Math.round(na + (nb - na) * t);
        int da = a & 0xFF, db = b & 0xFF;
        int bl = Math.round(da + (db - da) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float approach(float current, float target, float rate) {
        float v = current + (target - current) * rate;
        return Math.abs(target - v) < 0.0001f ? target : v;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Переводит ключ на текущий язык. Если перевода нет, возвращает исходную
     * строку, поэтому обычные человекочитаемые названия не ломаются.
     */
    private static String localize(String key, Object... args) {
        if (key == null) {
            return "";
        }
        String translated = class_1074.method_4662(key, args);
        return translated == null || translated.equals(key) ? key : translated;
    }

    private static boolean inside(double mx, double my, float rx, float ry, float rw, float rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private static String fmt(float value, float step) {
        if (step >= 1.0f) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.2f", value);
    }

    private static String keyName(int key) {
        if (key <= 0) {
            return "none";
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) {
            return name.toUpperCase();
        }
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BKSP";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            default -> "KEY_" + key;
        };
    }

    private static String bindName(KeyBind bind) {
        if (bind == null || bind.isEmpty()) {
            return localize("settings.none");
        }
        int key = bind.c();
        if (bind.jW()) {
            return switch (key) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                case KeyBind.Kg -> "WHEEL+";
                case KeyBind.Kh -> "WHEEL-";
                default -> "MB" + (key + 1);
            };
        }
        return keyName(key).replace("_", " ");
    }

    private static void saveClientConfig() {
        ClarityVisuals client = ClarityVisuals.getInstance();
        if (client == null || client.isConfigLoading() || client.getConfigManager() == null) {
            return;
        }
        client.getConfigManager().dU();
    }

    private List<Module> categoryModules() {
        List<Module> list = new ArrayList<>();
        if (ClarityVisuals.getInstance() == null || this.selectedCategory == null) {
            return list;
        }
        for (Module m : ClarityVisuals.getInstance().getModuleManager().getModules()) {
            if (m.isServerBlocked()) continue;
            if (m.getCategory() == this.selectedCategory && m.getCategory() != ModuleCategory.qo) {
                list.add(m);
            }
        }
        return list;
    }

    private Module findModule(String name) {
        if (name == null || ClarityVisuals.getInstance() == null
            || ClarityVisuals.getInstance().getModuleManager() == null) {
            return null;
        }
        // Ищем по всем категориям: панель должна корректно доигрывать
        // анимацию закрытия даже после смены вкладки.
        for (Module m : ClarityVisuals.getInstance().getModuleManager().getModules()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private boolean matchesSearch(Module m) {
        if (this.searchInput.length() == 0) {
            return true;
        }
        return m.getDisplayName().toLowerCase().contains(this.searchInput.toString().toLowerCase());
    }

    private static List<Setting<?>> visibleSettings(Module m) {
        List<Setting<?>> list = new ArrayList<>();
        for (Setting<?> s : m.getSettings()) {
            if (s.jS()) {
                list.add(s);
            }
        }
        return list;
    }

    private static boolean isSliderRow(Setting<?> s) {
        return s instanceof SliderSetting;
    }

    // ==================== lifecycle ====================

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        if (aiG == null || LegionClickGui.aiG.field_1755 != this) {
            return;
        }
        // Размеры берём в координатах GUI, а не фреймбуфера: мышь приходит
        // именно в них, иначе окно «прыгает» при перетаскивании и стоит не по центру.
        this.fixedW = this.field_22789;
        this.fixedH = this.field_22790;

        this.updateUiScale();
        this.recenterIfNeeded();

        if (!this.loggedFirstFrame) {
            this.loggedFirstFrame = true;
            System.out.println("[LegionGUI] screen=" + this.fixedW + "x" + this.fixedH
                + " uiScale=" + this.uiScale + " pos=" + this.x + "," + this.y
                + " panel=" + (BG_WIDTH * this.uiScale) + "x" + (BG_HEIGHT * this.uiScale));
        }

        if (this.dragging) {
            float maxX = Math.max(0.0f, this.designW() - BG_WIDTH);
            float maxY = Math.max(0.0f, this.designH() - BG_HEIGHT);
            this.x = clamp(mouseX / this.uiScale - this.dragOffsetX, 0.0f, maxX);
            this.baseY = clamp(mouseY / this.uiScale - this.dragOffsetY, 0.0f, maxY) + this.pullAnim;
        }

        // pull-Р°РЅРёРјР°С†РёСЏ РѕС‚РєСЂС‹С‚РёСЏ/Р·Р°РєСЂС‹С‚РёСЏ, РєР°Рє РІ РѕСЂРёРіРёРЅР°Р»Рµ (0.12)
        float target = this.designH() / 2.0f + BG_HEIGHT / 2.0f;
        if (!this.closing) {
            this.pullAnim = approach(this.pullAnim, target, 0.12f);
        } else {
            this.pullAnim = approach(this.pullAnim, 0.0f, 0.12f);
            if (this.pullAnim < 1.0f) {
                this.closing = false;
                aiG.method_1507(null);
                return;
            }
        }
        this.y = this.baseY - this.pullAnim;

        float progress = Math.min(1.0f, this.pullAnim / Math.max(1.0f, target));
        float mainAlpha = Math.min(1.0f, progress * 1.5f + 0.3f);
        if (mainAlpha <= 0.001f) {
            return;
        }

        if (this.selectedCategory != this.lastCategory) {
            this.moduleFade = 0.0f;
            this.lastCategory = this.selectedCategory;
            this.closeSettings();
        }
        this.moduleFade = approach(this.moduleFade, 1.0f, 0.12f);

        int a = (int) (0xFF * mainAlpha);
        float contentAlpha = Math.min(1.0f, (progress - 0.2f) * 2.5f);
        int ca = (int) (0xFF * Math.min(mainAlpha, contentAlpha));

        // РјР°СЃС€С‚Р°Р± РїРѕРґ СЂР°Р·РјРµСЂ РѕРєРЅР°
        int[] mouse = this.transformMouse(mouseX, mouseY);

        ClickGuiRenderContext.begin(mainAlpha, this.closing);
        try {
            Render2D.prepareHudFrame(ctx);
            ctx.method_51448().method_22903();
            // Дизайн-координаты GUI -> экран: просто множитель, потому что
            // начало координат у обоих систем общее (левый верхний угол).
            ctx.method_51448().method_22905(this.uiScale, this.uiScale, 1.0f);

            this.renderPanel(ctx, a, ca);
            this.renderCategories(ctx, ca, mouse[0], mouse[1]);
            this.renderHeader(ctx, ca, mouse[0], mouse[1]);
            if (this.hwView) {
                this.renderHwEvents(ctx, ca, mouse[0], mouse[1]);
            } else {
                this.renderModules(ctx, ca, mouse[0], mouse[1]);
            }
            this.updateSettingsDrag(mouse[0], mouse[1]);
            this.renderSettings(ctx, mouse[0], mouse[1], ca);
            this.renderSelectDropdown(ctx, ca);
            this.renderThemeMenu(ctx, ca);
            this.renderConfigPanel(ctx, ca, mouse[0], mouse[1]);

            ctx.method_51448().method_22909();
        } finally {
            ClickGuiRenderContext.end();
        }
    }

    /** Ширина/высота экрана в дизайн-координатах GUI (после масштаба). */
    private float designW() {
        return this.uiScale <= 0.0f ? this.fixedW : this.fixedW / this.uiScale;
    }

    private float designH() {
        return this.uiScale <= 0.0f ? this.fixedH : this.fixedH / this.uiScale;
    }

    /** Окно полностью выехало из нижнего края и готово к перетаскиванию. */
    private boolean isFullyOpen() {
        float target = this.designH() / 2.0f + BG_HEIGHT / 2.0f;
        return !this.closing && this.pullAnim >= target - 1.0f;
    }

    /**
     * Подбирает масштаб так, чтобы панель занимала большую часть экрана,
     * но не вылезала за его пределы. Раньше здесь считались пиксели
     * фреймбуфера, из-за чего окно выглядело мелким.
     */
    private void updateUiScale() {
        if (this.fixedW <= 0.0f || this.fixedH <= 0.0f) {
            this.uiScale = 1.0f;
            return;
        }
        float byH = (this.fixedH * 0.62f) / BG_HEIGHT;
        float byW = (this.fixedW * 0.55f) / BG_WIDTH;
        float s = Math.min(byW, byH);
        if (!Float.isFinite(s) || s <= 0.0f) {
            s = 1.0f;
        }
        this.uiScale = Math.max(0.75f, Math.min(1.35f, s));
    }

    /**
     * Обрезка рисования в дизайн-координатах. Scissor в DrawContext сам
     * применяет текущую матрицу, поэтому умножать на uiScale здесь нельзя:
     * иначе область обрезки уезжает вправо/вниз и срезает начало строк.
     */
    private void clip(class_332 ctx, float x, float y, float w, float h) {
        Render2D.startScissor(ctx, x, y, w, h);
    }

    /** Держит окно по центру, пока пользователь сам его не перетащил. */
    private void recenterIfNeeded() {
        boolean resized = this.fixedW != this.lastW || this.fixedH != this.lastH;
        this.lastW = this.fixedW;
        this.lastH = this.fixedH;
        if (!resized && this.centered) {
            return;
        }
        this.recenter();
    }

    private void recenter() {
        float maxX = Math.max(0.0f, this.designW() - BG_WIDTH);
        this.x = Math.max(0.0f, Math.min(maxX, (this.designW() - BG_WIDTH) / 2.0f));
        this.baseY = this.designH();
        this.centered = true;
    }

    /** Экранные координаты мыши -> дизайн-координаты GUI. */
    private int[] transformMouse(double mx, double my) {
        float s = this.uiScale <= 0.0f ? 1.0f : this.uiScale;
        return new int[]{
            Math.round((float) mx / s),
            Math.round((float) my / s)
        };
    }

    // ==================== С„РѕРЅ РїР°РЅРµР»Рё ====================

    private void renderPanel(class_332 ctx, int a, int ca) {
        int bgTint = mixChannel(0x0F0F0F, themeMain(), 0.08f);
        rblur(ctx,  this.x, this.y, BG_WIDTH, BG_HEIGHT, 12.0f, withAlpha(bgTint, (int) (a * 0.86f)));
        rrect(ctx, this.x, this.y, LEFT_W, BG_HEIGHT, CORNER_RADIUS, withAlpha(0x0A0A0A, (int) (a * 0.17f)));
    }

    // ==================== РєР°С‚РµРіРѕСЂРёРё ====================

    private List<ModuleCategory> visibleCategories() {
        List<ModuleCategory> list = new ArrayList<>();
        for (ModuleCategory c : ClarityVisuals.getInstance().getModuleManager().getCategories()) {
            if (c == ModuleCategory.qo) {
                continue;
            }
            list.add(c);
        }
        return list;
    }

    private static String categoryName(ModuleCategory c) {
        return ru.method.clarityvisuals.ui.ModuleTab.labelFor(c);
    }

    /** Иконки категорий как в Legion (у Render своя гарнитура). */
    private static Font categoryIconFont(ModuleCategory c) {
        return c == ModuleCategory.ql ? Fonts.LEGION_ICONZ : Fonts.LEGION_ICONS;
    }

    private static String categoryIcon(ModuleCategory c) {
        if (c == ModuleCategory.qk) {
            return "S"; // компас — движение
        }
        if (c == ModuleCategory.ql) {
            return "J"; // кисть — визуалы
        }
        if (c == ModuleCategory.qm) {
            return "L"; // силуэт игрока
        }
        if (c == ModuleCategory.qn) {
            return "z"; // сетка — прочее
        }
        return "V"; // скрещённые мечи — бой (и запасной вариант)
    }

    /** Кнопка «Конфиги» в левом нижнем углу (под всеми категориями). */
    private float[] configButtonRect() {
        return new float[]{this.x + 8.0f, this.y + BG_HEIGHT - 26.0f, LEFT_W - 16.0f, 20.0f};
    }

    private void renderCategories(class_332 ctx, int ca, int mx, int my) {
        List<ModuleCategory> cats = visibleCategories();
        float catY = this.y + 55.0f;

        // С†РµР»РµРІР°СЏ РїРѕР·РёС†РёСЏ РїРѕР»РѕСЃРєРё РІС‹Р±РѕСЂР°; HW-строка идёт сразу после категорий
        float selTarget = catY;
        if (this.hwView) {
            selTarget += cats.size() * CAT_ROW_H;
        } else {
            for (ModuleCategory c : cats) {
                if (c == this.selectedCategory) {
                    break;
                }
                selTarget += CAT_ROW_H;
            }
        }
        this.selBarY = approach(this.selBarY, selTarget, 0.12f);
        rrect(ctx, this.x + 8.0f, this.selBarY, LEFT_W - 16.0f, CAT_ROW_H, 8.0f, withAlpha(0xFFFFFF, (int) (ca * 0.05f)));

        for (ModuleCategory c : cats) {
            boolean active = c == this.selectedCategory;
            int color = active ? softTextColor(ca) : softTextColor((int) (ca * 0.45f));
            float textY = catY + (CAT_ROW_H - Fonts.LEGION_INTER.getHeight(8.0f)) / 2.0f;
            float iconY = catY + (CAT_ROW_H - this.categoryIconFont(c).getHeight(8.0f)) / 2.0f;
            Render2D.drawFont(ctx.method_51448(), this.categoryIconFont(c).getFont(8.0f), this.categoryIcon(c), this.x + 12.0f, iconY, toColor(color));
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), categoryName(c),
                this.x + 12.0f + 12.0f, textY, toColor(color));
            catY += CAT_ROW_H;
        }

        // строка «HW Events» — виртуальная вкладка с живым списком ивентов.
        // Иконка — календарь (legion_icons 'j'), в стиле остальных категорий.
        boolean hwActive = this.hwView;
        int hwColor = hwActive ? softTextColor(ca) : softTextColor((int) (ca * 0.45f));
        float hwTextY = catY + (CAT_ROW_H - Fonts.LEGION_INTER.getHeight(8.0f)) / 2.0f;
        float hwIconY = catY + (CAT_ROW_H - Fonts.LEGION_ICONS.getHeight(8.0f)) / 2.0f;
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(8.0f), "j",
            this.x + 12.0f, hwIconY, toColor(hwColor));
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), "HW Events",
            this.x + 12.0f + 12.0f, hwTextY, toColor(hwColor));

        // кнопка «Конфиги» — левый нижний угол
        float[] btn = this.configButtonRect();
        boolean btnHover = inside(mx, my, btn[0], btn[1], btn[2], btn[3]);
        if (btnHover || this.configPanelOpen) {
            rrect(ctx, btn[0], btn[1], btn[2], btn[3], 10.0f,
                withAlpha(themeMain(), (int) (ca * (this.configPanelOpen ? 0.16f : 0.10f))));
        }
        int cfgColor = softTextColor((int) (ca * (btnHover || this.configPanelOpen ? 0.9f : 0.55f)));
        // иконка-облако (legion_icons 'p') в начале кнопки
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(9.0f), "p",
            btn[0] + 8.0f, btn[1] + (btn[3] - Fonts.LEGION_ICONS.getHeight(9.0f)) / 2.0f, toColor(cfgColor));
        float cfgLabelW = Fonts.LEGION_INTER.getWidth("Конфиги", 7.5f);
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.5f), "Конфиги",
            btn[0] + 24.0f + (btn[2] - 24.0f - cfgLabelW) / 2.0f, vCenter(btn[1], btn[3], 7.5f), toColor(cfgColor));
    }

    private void renderHeader(class_332 ctx, int ca, int mx, int my) {
        // Иконка клиента по центру левой колонки (как слева в ватермарке).
        int main = themeMain();
        float logoSize = 22.0f;
        Render2D.drawTexture(ctx.method_51448(),
            this.x + (LEFT_W - logoSize) / 2.0f, this.y + 3.0f,
            logoSize, logoSize, 0.0f, CLIENT_ICON,
            toColor(withAlpha(main, (int) (ca * 0.70f))));

        rrect(ctx, this.x + 8.0f, this.y + 34.5f, LEFT_W - 16.0f, 0.5f, 0.5f, withAlpha(0xFFFFFF, (int) (ca * 0.06f)));
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), "Main",
            this.x + 10.0f, this.y + 41.0f, toColor(softTextColor((int) (ca * 0.45f))));

        // Р·Р°РіРѕР»РѕРІРѕРє РІС‹Р±СЂР°РЅРЅРѕР№ РєР°С‚РµРіРѕСЂРёРё СЃРїСЂР°РІР°
        String name = this.hwView ? "HW Events"
            : (this.selectedCategory == null ? "" : categoryName(this.selectedCategory));
        float topCenterY = this.y + 17.25f;
        int rightColor = softTextColor((int) (ca * 0.45f));
        Font hdrIconFont = this.hwView ? Fonts.LEGION_ICONS : this.categoryIconFont(this.selectedCategory);
        String hdrIcon = this.hwView ? "j" : this.categoryIcon(this.selectedCategory);
        float hdrIconY = topCenterY - hdrIconFont.getHeight(8.0f) / 2.0f;
        float rightTextY = topCenterY - Fonts.LEGION_INTER.getHeight(8.0f) / 2.0f;
        Render2D.drawFont(ctx.method_51448(), hdrIconFont.getFont(8.0f), hdrIcon,
            this.x + LEFT_W + 10.0f, hdrIconY, toColor(rightColor));
        float sepX = this.x + LEFT_W + 10.0f + 12.0f + 5.0f;
        rrect(ctx, sepX, topCenterY - 6.0f, 0.5f, 12.0f, 0.5f, withAlpha(0xFFFFFF, (int) (ca * 0.06f)));
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), name, sepX + 5.0f, rightTextY, toColor(rightColor));

        // Кнопка настроек темы (шестерёнка) рядом с поиском.
        float themeButtonX = this.x + BG_WIDTH - 12.0f - 18.0f;
        float themeButtonY = this.y + 7.0f;
        boolean themeHover = inside(mx, my, themeButtonX, themeButtonY, 18.0f, 18.0f);
        if (themeHover || this.themeMenuOpen) {
            rrect(ctx, themeButtonX, themeButtonY, 18.0f, 18.0f, 8.0f,
                withAlpha(themeMain(), (int) (ca * (this.themeMenuOpen ? 0.18f : 0.10f))));
        }
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(11.0f), "%",
            themeButtonX + (18.0f - Fonts.LEGION_ICONS.getWidth("%", 11.0f)) / 2.0f,
            themeButtonY + (18.0f - Fonts.LEGION_ICONS.getHeight(11.0f)) / 2.0f,
            toColor(withAlpha(main, (int) (ca * (themeHover || this.themeMenuOpen ? 0.95f : 0.45f)))));

        rrect(ctx, this.x + LEFT_W + 8.0f, this.y + 34.5f, BG_WIDTH - LEFT_W - 16.0f, 0.5f, 0.5f, withAlpha(0xFFFFFF, (int) (ca * 0.06f)));

        // РїРѕРёСЃРє — только в обычном режиме; в HW Events он не работает
        if (!this.hwView) {
            float searchW = 90.0f;
            float searchX = this.x + BG_WIDTH - 36.0f - searchW;
            float searchY = this.y + 8.0f;
            float searchH = 19.0f;
            rrect(ctx, searchX, searchY, searchW, searchH, 8.0f, withAlpha(0xFFFFFF, (int) (ca * 0.05f)));
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(9.0f), "G",
                searchX + 5.0f, searchY + (searchH - Fonts.LEGION_ICONS.getHeight(9.0f)) / 2.0f, toColor(withAlpha(main, (int) (ca * 0.45f))));
            String query = this.searchInput.toString();
            if (query.isEmpty() && !this.searchFocused) {
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), "Search...",
                    searchX + 16.0f, vCenter(searchY, searchH, 7.0f), toColor(softTextColor((int) (ca * 0.30f))));
            } else {
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), query,
                    searchX + 16.0f, vCenter(searchY, searchH, 7.0f), toColor(softTextColor((int) (ca * 0.70f))));
                if (this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
                    float cx = searchX + 16.0f + Fonts.LEGION_INTER.getWidth(query, 7.0f);
                    rrect(ctx, cx, searchY + 5.0f, 0.5f, searchH - 10.0f,
                        softTextColor(ca), 0.5f);
                }
            }
        }
    }

    // ==================== РјРѕРґСѓР»Рё ====================

    /** Высота карточек без учёта текущего скролла. */
    private float modulesContentHeight(List<Module> modules) {
        Map<String, List<Module>> groups = this.buildGroups(modules, this.selectedCategory);
        float[] colY = {0.0f, 0.0f};
        int colIdx = 0;
        for (List<Module> gm : groups.values()) {
            if (gm.isEmpty()) {
                continue;
            }
            float cardH = 8.0f + gm.size() * MOD_ROW_H;
            colY[colIdx] += 10.0f + cardH + 6.0f;
            colIdx = colY[0] <= colY[1] ? 0 : 1;
        }
        // Последний отступ в 6 px не относится к контенту.
        return Math.max(0.0f, Math.max(colY[0], colY[1]) - 6.0f);
    }

    private void renderModules(class_332 ctx, int ca, int mx, int my) {
        List<Module> modules = new ArrayList<>();
        for (Module m : categoryModules()) {
            if (matchesSearch(m)) {
                modules.add(m);
            }
        }
        float viewH = BG_HEIGHT - 44.0f;
        if (modules.isEmpty()) {
            this.contentHeight = 0.0f;
            this.viewHeight = viewH;
            this.scrollTarget = 0.0f;
            this.scrollOffset = 0.0f;
            return;
        }

        float contentH = this.modulesContentHeight(modules);
        float maxScroll = Math.max(0.0f, contentH - viewH);
        if (maxScroll <= 0.0f) {
            this.scrollTarget = 0.0f;
            this.scrollOffset = 0.0f;
        } else {
            this.scrollTarget = clamp(this.scrollTarget, 0.0f, maxScroll);
            this.scrollOffset = clamp(this.scrollOffset, 0.0f, maxScroll);
        }
        this.contentHeight = Math.max(0.0f, contentH);
        this.viewHeight = viewH;
        this.scrollOffset = approach(this.scrollOffset, this.scrollTarget, 0.18f);
        this.scrollOffset = clamp(this.scrollOffset, 0.0f, maxScroll);

        float rightX = this.x + LEFT_W + 8.0f;
        float rightW = BG_WIDTH - LEFT_W - 16.0f;
        float colW = (rightW - 6.0f) / 2.0f;
        float startY = this.y + 42.0f - this.scrollOffset;

        // РѕРґРёРЅ РєРѕРЅС‚РµР№РЅРµСЂ РЅР° РІСЃРµ РіСЂСѓРїРїС‹

        Map<String, List<Module>> groups = this.buildGroups(modules, this.selectedCategory);

        float[] colY = {startY, startY};
        float[] colX = {rightX, rightX + colW + 6.0f};
        int headerColor = softTextColor((int) (ca * 0.45f * this.moduleFade));
        int colIdx = 0;
        float contentTop = this.y + 36.0f;
        float panelBottom = this.y + BG_HEIGHT - 3.0f;

        this.clip(ctx, this.x + LEFT_W + 1.0f, contentTop, BG_WIDTH - LEFT_W - 2.0f, panelBottom - contentTop);
        for (Map.Entry<String, List<Module>> entry : groups.entrySet()) {
            List<Module> gm = entry.getValue();
            if (gm.isEmpty()) {
                continue;
            }
            float cardX = colX[colIdx];
            float cardH = 8.0f + gm.size() * MOD_ROW_H;
            float gy = colY[colIdx];
            float cardTop = gy + 10.0f;
            float cardBottom = cardTop + cardH;

            // Карточка рисуется в своих настоящих координатах: при прокрутке
            // её край уходит за границу списка и обрезается, а не прилипает
            // к верху, оставляя пустоту между рамкой и первой строкой.
            if (cardBottom > contentTop && cardTop < panelBottom) {
                int bgTintCard = mixChannel(0x0F0F0F, themeMain(), 0.08f);
                int cardBg = withAlpha(mixChannel(bgTintCard, 0x000000, 0.20f), (int) (ca * 0.86f * this.moduleFade));

                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.5f), entry.getKey(),
                    cardX + 7.0f, gy - 1.0f, toColor(headerColor));

                rrect(ctx, cardX - 0.5f, cardTop - 0.5f, colW + 1.0f, cardH + 1.0f, CARD_RADIUS, withAlpha(0xFFFFFF, (int) (ca * 0.06f * this.moduleFade)));
                rrect(ctx, cardX, cardTop, colW, cardH, CARD_RADIUS, cardBg);
            }

            float modY = gy + 18.0f;
            for (int mi = 0; mi < gm.size(); ++mi) {
                Module module = gm.get(mi);
                boolean visible = modY + MOD_ROW_H > contentTop && modY < panelBottom;

                if (visible) {
                    int modColor = module.isToggle()
                        ? softTextColor((int) (ca * this.moduleFade))
                        : softTextColor((int) (ca * 0.45f * this.moduleFade));
                    Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), module.getDisplayName(),
                        cardX + 8.0f, modY + 1.0f, toColor(modColor));
                }

                // С‚СѓРјР±Р»РµСЂ
                float anim = this.toggleAnims.getOrDefault(module.getName(), module.isToggle() ? 1.0f : 0.0f);
                anim = approach(anim, module.isToggle() ? 1.0f : 0.0f, 0.08f);
                this.toggleAnims.put(module.getName(), anim);

                float pillW = 16.0f;
                float pillH = 9.0f;
                float pillX = cardX + colW - 8.0f - pillW;
                float pillY = modY + 1.0f;

                if (this.editingModule != null && this.editingModule.equals(module.getName())) {
                    this.editingModuleX = this.x + BG_WIDTH - SETTINGS_W - 14.0f;
                    this.editingModuleY = modY + 1.0f;
                } else if (this.closingModule != null && this.closingModule.equals(module.getName())) {
                    this.closingModuleX = this.x + BG_WIDTH - SETTINGS_W - 14.0f;
                    this.closingModuleY = modY + 1.0f;
                }

                if (visible) {
                    int themeCol = themeMain();
                    int tr = Math.round(((themeCol >> 16) & 0xFF) * anim + 0xFF * (1.0f - anim));
                    int tg = Math.round(((themeCol >> 8) & 0xFF) * anim + 0xFF * (1.0f - anim));
                    int tb = Math.round((themeCol & 0xFF) * anim + 0xFF * (1.0f - anim));
                    int ta = (int) (ca * (0.12f + 0.46f * anim) * this.moduleFade);
                    rrect(ctx, pillX, pillY, pillW, pillH,
                        withAlpha((tr << 16) | (tg << 8) | tb, ta), pillH / 2.0f);

                    float thumbDiam = 7.0f;
                    float squish = 1.0f + (1.0f - Math.abs(anim * 2.0f - 1.0f)) * 0.35f;
                    float thumbW = thumbDiam * squish;
                    float thumbH = thumbDiam / squish;
                    float thumbCenter = pillX + 1.0f + anim * (pillW - 2.0f - thumbDiam) + thumbDiam / 2.0f;
                    rrect(ctx, thumbCenter - thumbW / 2.0f, pillY + (pillH - thumbH) / 2.0f,
                        thumbW, thumbH, withAlpha(0xFFFFFF, (int) (ca * this.moduleFade)), thumbH / 2.0f);

                    if (!visibleSettings(module).isEmpty()) {
                        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(8.0f), "%",
                            pillX - 12.0f, modY + 1.5f,
                            toColor(withAlpha(themeCol, (int) (ca * 0.45f * this.moduleFade))));
                    }
                }

                modY += MOD_ROW_H;
                if (mi < gm.size() - 1 && visible) {
                    rrect(ctx, cardX + 8.0f, modY - 5.0f, colW - 16.0f, 0.5f, 0.5f, withAlpha(0xFFFFFF, (int) (ca * 0.04f * this.moduleFade)));
                }
            }
            colY[colIdx] += 10.0f + cardH + 6.0f;
            colIdx = colY[0] <= colY[1] ? 0 : 1;
        }
        Render2D.stopScissor(ctx);
    }

    private float contentHeight;
    private float viewHeight;

    private static final java.util.Set<String> VIS_HIGHLIGHTS = java.util.Set.of(
        "BlockOverlay", "CustomHitBox", "HitColor", "HitRange", "TargetEsp");
    private static final java.util.Set<String> VIS_WORLD = java.util.Set.of(
        "AspectRatio", "Aurora", "FogBlur", "Fullbright", "LowFog", "NoRender", "Rain", "Sakura",
        "Sandstorm", "ShaderFog", "SuperResolution", "World", "MotionBlur");
    private static final java.util.Set<String> VIS_HUD = java.util.Set.of(
        "Crosshair", "CustomVitals", "Saturation", "SelfNametags");
    private static final java.util.Set<String> VIS_ANIM = java.util.Set.of(
        "Hands", "ItemPhysics", "ItemRadius", "SwingAnimation", "ViewModel",
        "Trajectories", "Freelook", "Zoom");

    private static final java.util.Set<String> PLAYER_AUTO = java.util.Set.of(
        "AutoAccept", "AutoNear", "AutoReply", "AutoSpec");
    private static final java.util.Set<String> PLAYER_ITEMS = java.util.Set.of(
        "ItemScroller", "LockSlot");
    private static final java.util.Set<String> PLAYER_UTILS = java.util.Set.of(
        "ChatHelper", "CMDDelay", "CoordInvite", "DeathCoords",
        "WayPoints");

    /** Имя карточки (группы), в которую попадает модуль. */
    private String groupOf(Module m) {
        String name = m.getName();
        if (m.getCategory() == ModuleCategory.ql) {
            if (VIS_HIGHLIGHTS.contains(name)) return "Highlights";
            if (VIS_WORLD.contains(name)) return "World";
            if (VIS_HUD.contains(name)) return "HUD";
            if (VIS_ANIM.contains(name)) return "Animations";
            return "Visuals";
        }
        if (m.getCategory() == ModuleCategory.qm) {
            if (PLAYER_AUTO.contains(name)) return "Auto";
            if (PLAYER_ITEMS.contains(name)) return "Items";
            if (PLAYER_UTILS.contains(name)) return "Utils";
            return "Info";
        }
        if (m.getCategory() == ModuleCategory.qk) {
            return "Movement";
        }
        if (m.getCategory() == ModuleCategory.qj) {
            return "Combat";
        }
        return "Other";
    }

    /** Порядок карточек — от него зависит раскладка по двум колонкам. */
    private List<String> groupOrder(ModuleCategory c) {
        if (c == ModuleCategory.ql) {
            return Arrays.asList("Highlights", "Animations", "World", "Visuals", "HUD");
        }
        if (c == ModuleCategory.qm) {
            return Arrays.asList("Auto", "Info", "Utils", "Items");
        }
        if (c == ModuleCategory.qk) {
            return List.of("Movement");
        }
        if (c == ModuleCategory.qj) {
            return List.of("Combat");
        }
        return List.of("Other");
    }

    private Map<String, List<Module>> buildGroups(List<Module> modules, ModuleCategory c) {
        Map<String, List<Module>> groups = new LinkedHashMap<>();
        for (String g : this.groupOrder(c)) {
            groups.put(g, new ArrayList<>());
        }
        for (Module m : modules) {
            groups.computeIfAbsent(this.groupOf(m), k -> new ArrayList<>()).add(m);
        }
        groups.values().removeIf(List::isEmpty);
        return groups;
    }

    // ==================== РїР°РЅРµР»СЊ РЅР°СЃС‚СЂРѕРµРє ====================

    /** Полная высота содержимого панели настроек. */
    private float settingsContentHeight(Module module) {
        float totalH = 0.0f;
        for (Setting<?> s : visibleSettings(module)) {
            totalH += isSliderRow(s) ? SLIDER_ROW_H : SET_ROW_H;
        }
        // Первая строка панели — бинд модуля.
        totalH += SET_ROW_H;
        return 9.0f + totalH + 5.0f;
    }

    /** Высота панели с учётом экрана: длинные списки прокручиваются внутри. */
    private float settingsPanelHeight(Module module) {
        float maxH = Math.max(40.0f, this.designH() - 8.0f);
        return Math.min(this.settingsContentHeight(module), maxH);
    }

    /** Позиция панели настроек с учётом границ экрана (общая для рендера и кликов). */
    private float[] settingsPanelPos(Module module, float modX, float modY) {
        float panelH = this.settingsPanelHeight(module);
        float maxX = Math.max(4.0f, this.designW() - SETTINGS_W - 4.0f);
        float maxY = Math.max(4.0f, this.designH() - panelH - 4.0f);
        float px = clamp(modX + this.settingsOffX, 4.0f, maxX);
        float py = clamp(modY + this.settingsOffY, 4.0f, maxY);
        return new float[]{px, py, panelH};
    }

    /** Перетаскивание панели настроек за её верхнюю полоску. */
    private void updateSettingsDrag(float mx, float my) {
        if (!this.settingsDragging || this.editingModule == null) {
            return;
        }
        Module module = this.findModule(this.editingModule);
        if (module == null) {
            return;
        }
        float panelH = this.settingsPanelHeight(module);
        float maxX = Math.max(4.0f, this.designW() - SETTINGS_W - 4.0f);
        float maxY = Math.max(4.0f, this.designH() - panelH - 4.0f);
        float px = clamp(mx - this.settingsDragOffX, 4.0f, maxX);
        float py = clamp(my - this.settingsDragOffY, 4.0f, maxY);
        this.settingsOffX = px - this.editingModuleX;
        this.settingsOffY = py - this.editingModuleY;
    }

    private void renderSettings(class_332 ctx, float mx, float my, int ca) {
        String active = this.editingModule != null ? this.editingModule : this.closingModule;
        if (active == null) {
            this.settingsAnim = approach(this.settingsAnim, 0.0f, 0.12f);
            this.settingsScroll = 0.0f;
            this.settingsScrollTarget = 0.0f;
            return;
        }
        Module module = this.findModule(active);
        if (module == null) {
            this.closeSettings();
            this.closingModule = null;
            return;
        }
        boolean closing = this.editingModule == null;
        if (closing) {
            this.settingsAnim = approach(this.settingsAnim, 0.0f, 0.12f);
            if (this.settingsAnim < 0.001f) {
                this.settingsAnim = 0.0f;
                this.closingModule = null;
                return;
            }
        } else {
            this.settingsAnim = approach(this.settingsAnim, 1.0f, 0.12f);
        }

        float modX = closing ? this.closingModuleX : this.editingModuleX;
        float modY = closing ? this.closingModuleY : this.editingModuleY;
        List<Setting<?>> settings = visibleSettings(module);
        float contentH = this.settingsContentHeight(module);
        float[] pos = this.settingsPanelPos(module, modX, modY);
        float panelX = pos[0];
        float panelY = pos[1];
        float panelH = pos[2];

        float maxScroll = Math.max(0.0f, contentH - panelH);
        if (maxScroll <= 0.0f) {
            this.settingsScrollTarget = 0.0f;
            this.settingsScroll = 0.0f;
        } else {
            this.settingsScrollTarget = clamp(this.settingsScrollTarget, 0.0f, maxScroll);
            this.settingsScroll = clamp(this.settingsScroll, 0.0f, maxScroll);
        }
        this.settingsScroll = approach(this.settingsScroll, this.settingsScrollTarget, 0.18f);
        this.settingsScroll = clamp(this.settingsScroll, 0.0f, maxScroll);

        float animPanelH = panelH * this.settingsAnim;
        animPanelH = Math.min(animPanelH, this.designH() - panelY - 4.0f);
        int alpha = (int) (ca * this.settingsAnim);
        float gcr = CORNER_RADIUS - 2.0f;

        rblur(ctx,  panelX, panelY, SETTINGS_W, animPanelH, 10.0f, withAlpha(0x000000, (int) (alpha * 0.45f)));
        rrect(ctx, panelX, panelY, SETTINGS_W, animPanelH, gcr, withAlpha(0x0A0A0A, (int) (alpha * 0.62f)));

        int textColor = softTextColor((int) (alpha * 0.85f));
        int valColor = softTextColor((int) (alpha * 0.60f));

        float contentTop = panelY + 9.0f;
        float contentBottom = panelY + animPanelH - 5.0f;
        float bindRowY = contentTop - this.settingsScroll;
        float setY = bindRowY + SET_ROW_H;
        float valX = panelX + SETTINGS_W - 8.0f;

        if (contentBottom > contentTop) {
            this.clip(ctx, panelX + 1.0f, contentTop, SETTINGS_W - 2.0f, contentBottom - contentTop);
            try {
                if (bindRowY + SET_ROW_H > contentTop && bindRowY < contentBottom) {
                    Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f),
                        localize("settings.bind"), panelX + 8.0f, bindRowY + 1.0f, toColor(textColor));
                    boolean bindEditing = this.editingBind != null
                        && this.bindSettingName == null
                        && module.getName().equals(this.bindModuleName);
                    this.drawValue(ctx, bindEditing ? "..." : bindName(module.getBind()), alpha,
                        valX, bindRowY, bindEditing ? softTextColor(alpha) : valColor,
                        panelX, module, "bind");
                }

                for (Setting<?> s : settings) {
                    String rawName = s.getName();
                    String name = localize(rawName);
                    boolean sliderRow = isSliderRow(s);
                    float rowH = sliderRow ? SLIDER_ROW_H : SET_ROW_H;
                    boolean stringEditing = s instanceof StringSetting ss && this.isEditingString(module, ss);
                    if (setY + rowH > contentTop && setY < contentBottom) {
                        if (!stringEditing) {
                            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f),
                                fit(name, 7.0f, SETTINGS_W - 72.0f),
                                panelX + 8.0f, setY + (sliderRow ? 4.0f : 1.0f), toColor(textColor));
                        }

                        if (s instanceof BooleanSetting bs) {
                            this.drawBoolean(ctx, alpha, valX, setY, rowH, module.getName() + "|" + rawName,
                                Boolean.TRUE.equals(bs.jT()));
                        } else if (s instanceof SliderSetting sl) {
                            this.drawSlider(ctx, sl, alpha, valX, setY, rowH, rawName, panelX);
                        } else if (s instanceof EnumSetting<?> es) {
                            String val = localize(es.jZ());
                            this.drawValue(ctx, val, alpha, valX, setY, valColor, panelX, module, rawName);
                        } else if (s instanceof MultiSelectSetting ms) {
                            int count = ms.ka().size();
                            this.drawValue(ctx, localize("settings.selected", count), alpha, valX, setY,
                                valColor, panelX, module, rawName);
                        } else if (s instanceof BindSetting bs) {
                            boolean bindEditing = this.bindSettingName != null
                                && this.bindSettingName.equals(rawName)
                                && module.getName().equals(this.bindModuleName);
                            this.drawValue(ctx, bindEditing ? "..." : bindName(bs.jT()), alpha, valX, setY,
                                bindEditing ? softTextColor(alpha) : valColor, panelX, module, rawName);
                        } else if (s instanceof ButtonSetting btn) {
                            String btnName = btn.jX() != null ? localize(btn.jX()) : name;
                            this.drawValue(ctx, btnName, alpha, valX, setY,
                                softTextColor((int) (alpha * 0.70f)), panelX, module, rawName);
                        } else if (s instanceof ColorSetting cs) {
                            float size = 8.0f;
                            Color c = (Color) cs.jT();
                            rrect(ctx, valX - size, setY + (SET_ROW_H - size) / 2.0f, size, size,
                                c == null ? Color.WHITE.getRGB() : c.getRGB(), 1.5f);
                            // Подсветка свотча, для которого открыта палитра.
                            if (this.colorPicker.isActive() && this.colorPicker.getSetting() == cs) {
                                Render2D.drawBorder(ctx.method_51448(),
                                    valX - size - 2.5f, setY + (SET_ROW_H - size) / 2.0f - 2.5f,
                                    size + 5.0f, size + 5.0f, 2.5f, 0.8f,
                                    new ColorRGBA(new Color(withAlpha(themeMain(), (int) (alpha * 0.85f)), true)));
                            }
                        } else if (s instanceof StringSetting ss) {
                            this.drawStringSetting(ctx, ss, alpha, valX, setY, panelX, module);
                        }
                    }
                    setY += rowH;
                }
            } finally {
                Render2D.stopScissor(ctx);
            }
        }

        // Полоса прокрутки, чтобы было видно, что в панели есть ещё строки.
        if (maxScroll > 0.5f && alpha > 0) {
            float trackX = panelX + SETTINGS_W - 3.0f;
            float trackY = panelY + 9.0f;
            float trackH = Math.max(0.0f, panelH - 14.0f);
            if (trackH > 6.0f) {
                float thumbH = Math.max(10.0f, trackH * panelH / contentH);
                float thumbY = trackY + (trackH - thumbH) * (this.settingsScroll / maxScroll);
                rrect(ctx, trackX, thumbY, 1.5f, thumbH, 0.75f,
                    withAlpha(0xFFFFFF, (int) (alpha * 0.22f)));
            }
        }

        // Палитра рисуется поверх панели настроек в её дизайн-координатах.
        if (this.editingModule != null && this.colorPicker.isActive()) {
            this.colorPicker.render(ctx, mx, my, ca, themeMain());
        }
    }

    private void drawValue(class_332 ctx, String val, int alpha, float valX, float setY, int color,
                            float panelX, Module module, String name) {
        if (val == null) {
            return;
        }
        // Значение может быть длинным: обрезаем хвост, а не начало,
        // иначе на экране остаются огрызки вида "ing...".
        String shown = fit(val, 7.0f, 62.0f);
        float w = Fonts.LEGION_INTER.getWidth(shown, 7.0f);
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), shown,
            valX - w, setY + 2.0f, toColor(color));
    }

    private void drawStringSetting(class_332 ctx, StringSetting setting, int alpha, float valX,
                                   float setY, float panelX, Module module) {
        if (this.isEditingString(module, setting)) {
            // В режиме ввода показываем поле на всю ширину строки, чтобы
            // длинный текст не обрезался и было видно, что именно печатается.
            rrect(ctx, panelX + 5.0f, setY + 1.0f, SETTINGS_W - 10.0f, SET_ROW_H - 2.0f,
                4.0f, withAlpha(themeMain(), (int) (alpha * 0.55f)));
            rrect(ctx, panelX + 6.0f, setY + 2.0f, SETTINGS_W - 12.0f, SET_ROW_H - 4.0f,
                3.0f, withAlpha(0x0A0A0A, (int) (alpha * 0.92f)));
            String buffer = this.stringTextInput == null ? "" : this.stringTextInput.toString();
            String cursor = System.currentTimeMillis() / 500L % 2L == 0L ? "|" : "";
            String shown = fitEnd(buffer + cursor, 7.0f, SETTINGS_W - 20.0f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), shown,
                panelX + 9.0f, setY + 4.0f, toColor(softTextColor(alpha)));
            return;
        }
        String value = setting.jT() == null ? "" : setting.jT();
        this.drawValue(ctx, value.isEmpty() ? "..." : value, alpha, valX, setY,
            softTextColor((int) (alpha * 0.60f)), panelX, module, setting.getName());
    }

    /** Обрезает строку по ширине, оставляя видимым конец (для ввода текста). */
    private static String fitEnd(String text, float size, float maxW) {
        if (text == null) {
            return "";
        }
        if (Fonts.LEGION_INTER.getWidth(text, size) <= maxW) {
            return text;
        }
        String s = text;
        while (s.length() > 1 && Fonts.LEGION_INTER.getWidth(".." + s, size) > maxW) {
            s = s.substring(1);
        }
        return ".." + s;
    }

    /** Обрезает строку по ширине, добавляя ".." в конце. */
    private static String fit(String text, float size, float maxW) {
        if (text == null) {
            return "";
        }
        if (Fonts.LEGION_INTER.getWidth(text, size) <= maxW) {
            return text;
        }
        String s = text;
        while (s.length() > 1 && Fonts.LEGION_INTER.getWidth(s + "..", size) > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "..";
    }

    private void drawBoolean(class_332 ctx, int alpha, float valX, float setY, float rowH,
                             String key, boolean on) {
        float anim = this.boolSettingAnims.getOrDefault(key, on ? 1.0f : 0.0f);
        anim = approach(anim, on ? 1.0f : 0.0f, 0.08f);
        this.boolSettingAnims.put(key, anim);

        float pillW = 12.0f;
        float pillH = 7.0f;
        float pillX = valX - pillW;
        float pillY = setY + (rowH - pillH) / 2.0f;

        int themeCol = themeMain();
        int tr = Math.round(((themeCol >> 16) & 0xFF) * anim + 0xFF * (1.0f - anim));
        int tg = Math.round(((themeCol >> 8) & 0xFF) * anim + 0xFF * (1.0f - anim));
        int tb = Math.round((themeCol & 0xFF) * anim + 0xFF * (1.0f - anim));
        int ta = (int) (alpha * (0.12f + 0.46f * anim));
        rrect(ctx, pillX, pillY, pillW, pillH, withAlpha((tr << 16) | (tg << 8) | tb, ta), pillH / 2.0f);

        float thumbDiam = 5.0f;
        float squish = 1.0f + (1.0f - Math.abs(anim * 2.0f - 1.0f)) * 0.35f;
        float thumbW = thumbDiam * squish;
        float thumbH = thumbDiam / squish;
        float thumbCenter = pillX + 1.0f + anim * (pillW - 2.0f - thumbDiam) + thumbDiam / 2.0f;
        rrect(ctx, thumbCenter - thumbW / 2.0f, pillY + (pillH - thumbH) / 2.0f, thumbW, thumbH,
            withAlpha(0xFFFFFF, alpha), thumbH / 2.0f);
    }

    private void drawSlider(class_332 ctx, SliderSetting sl, int alpha, float valX, float setY,
                            float rowH, String name, float panelX) {
        boolean editing = this.editingSliderText != null && this.editingSliderText.equals(name);
        float value = sl.jT() == null ? sl.kb() : sl.jT();
        String displayVal = fmt(value, sl.kd());
        String valStr = editing
            ? this.sliderTextInput.toString() + (System.currentTimeMillis() / 500L % 2L == 0L ? "|" : " ")
            : displayVal;
        String shown = fit(valStr, 7.0f, 54.0f);
        float valW = Fonts.LEGION_INTER.getWidth(shown, 7.0f);
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), shown, valX - valW, setY + 4.0f,
            toColor(editing ? softTextColor(alpha) : softTextColor((int) (alpha * 0.60f))));

        float sliderW = SETTINGS_W - 20.0f;
        float sliderX = panelX + 10.0f;
        float sliderY = setY + rowH - 10.0f;
        float range = sl.kc() - sl.kb();
        float raw = range == 0.0f ? 0.0f : (value - sl.kb()) / range;
        raw = Math.max(0.0f, Math.min(1.0f, raw));

        float anim = this.sliderProgressAnims.getOrDefault(name, raw);
        anim = approach(anim, raw, 0.12f);
        this.sliderProgressAnims.put(name, anim);

        rrect(ctx, sliderX, sliderY + 2.0f, sliderW, 2.0f, 1.0f, withAlpha(0xFFFFFF, (int) (alpha * 0.08f)));
        rrect(ctx, sliderX, sliderY + 2.0f, sliderW * anim, 2.0f, 1.0f, withAlpha(themeMain(), (int) (alpha * 0.52f)));

        float dragAnim = this.sliderDragAnims.getOrDefault(name, 0.0f);
        dragAnim = approach(dragAnim, this.draggingSlider != null && this.draggingSlider.equals(name) ? 1.0f : 0.0f, 0.12f);
        this.sliderDragAnims.put(name, dragAnim);
        float squish = 1.0f + dragAnim * 0.35f;
        float thumbDiam = 5.0f;
        float thumbCX = sliderX + sliderW * anim;
        float thumbCY = sliderY + 3.0f;
        float thumbW = thumbDiam * squish;
        float thumbH = thumbDiam / squish;
        rrect(ctx, thumbCX - thumbW / 2.0f, thumbCY - thumbH / 2.0f, thumbW, thumbH,
            withAlpha(0xFFFFFF, alpha), Math.min(thumbW, thumbH) / 2.0f);
    }

    private void renderSelectDropdown(class_332 ctx, int ca) {
        String active = this.editingSelect != null ? this.editingSelect : this.closingSelect;
        if (active == null) {
            this.selectAnim = approach(this.selectAnim, 0.0f, 0.12f);
            return;
        }
        if (this.editingSelect != null) {
            this.selectAnim = approach(this.selectAnim, 1.0f, 0.12f);
        } else {
            this.selectAnim = approach(this.selectAnim, 0.0f, 0.12f);
            if (this.selectAnim < 0.0001f) {
                this.closingSelect = null;
                return;
            }
        }
        Module module = this.findModule(this.editingModule != null ? this.editingModule : this.closingModule);
        if (module == null) {
            this.editingSelect = null;
            this.closingSelect = null;
            return;
        }
        EnumSetting<?> target = null;
        for (Setting<?> s : visibleSettings(module)) {
            if (s.getName().equals(active) && s instanceof EnumSetting<?> es) {
                target = es;
                break;
            }
        }
        if (target == null) {
            this.editingSelect = null;
            this.closingSelect = null;
            return;
        }

        float dropItemH = 14.0f;
        Enum<?>[] values = target.jT().getDeclaringClass().getEnumConstants();
        float[] dropPos = this.selectDropdownPos(target);
        float dropX = dropPos[0];
        float dropY = dropPos[1];
        float dropW = dropPos[2];
        float dropH = dropPos[3];
        float animDropH = dropH * this.selectAnim;
        float gcr = CORNER_RADIUS - 2.0f;
        int dropAlpha = (int) (ca * this.selectAnim);

        int bgTint = mixChannel(0x0F0F0F, themeMain(), 0.08f);
        rblur(ctx,  dropX, dropY, dropW, animDropH, 10.0f, withAlpha(0x000000, (int) (dropAlpha * 0.14f)));
        rrect(ctx, dropX, dropY, dropW, animDropH, gcr, withAlpha(bgTint, (int) (dropAlpha * 0.55f)));

        float iy = dropY + 3.0f;
        for (Enum<?> opt : values) {
            boolean isOn = opt == target.jT();
            int itemColor = isOn ? softTextColor(dropAlpha) : softTextColor((int) (dropAlpha * 0.35f));
            float textY = vCenter(iy, dropItemH, 7.0f);
            float textStartX = dropX + 6.0f + 12.0f + 4.0f;
            if (isOn) {
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(8.0f), "p",
                    dropX + 6.0f, iy + (dropItemH - Fonts.LEGION_ICONS.getHeight(8.0f)) / 2.0f,
                    toColor(withAlpha(themeMain(), dropAlpha)));
            }
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f),
                this.optionName(opt), textStartX, textY, toColor(itemColor));
                iy += dropItemH;
        }
    }

    /** Позиция выпадающего списка, не выходящая за пределы экрана. */
    private float[] selectDropdownPos(EnumSetting<?> target) {
        float dropW = 130.0f;
        float dropH = target.jT().getDeclaringClass().getEnumConstants().length * 14.0f + 6.0f;
        float dropX = clamp(this.editingSelectX, 4.0f, Math.max(4.0f, this.designW() - dropW - 4.0f));
        float dropY = clamp(this.editingSelectY + 12.0f, 4.0f, Math.max(4.0f, this.designH() - dropH - 4.0f));
        return new float[]{dropX, dropY, dropW, dropH};
    }

    /** Отображаемое имя опции: NamedOption.getName(), иначе enum-имя. */
    private static String optionName(Enum<?> opt) {
        if (opt instanceof NamedOption no) {
            return localize(no.getName());
        }
        return localize(opt.name());
    }

    private float[] themeMenuPos() {
        ClientTheme[] themes = ClientTheme.values();
        float menuH = THEME_HEADER_H + themes.length * THEME_ROW_H + 5.0f;
        float menuX = clamp(this.x + BG_WIDTH - 12.0f - THEME_MENU_W, 4.0f,
            Math.max(4.0f, this.designW() - THEME_MENU_W - 4.0f));
        float menuY = clamp(this.y + 30.0f, 4.0f,
            Math.max(4.0f, this.designH() - menuH - 4.0f));
        return new float[]{menuX, menuY, THEME_MENU_W, menuH};
    }

    private void renderThemeMenu(class_332 ctx, int ca) {
        if (!this.themeMenuOpen && this.themeMenuAnim <= 0.0001f) {
            this.themeMenuAnim = 0.0f;
            return;
        }
        this.themeMenuAnim = approach(this.themeMenuAnim, this.themeMenuOpen ? 1.0f : 0.0f, 0.14f);
        if (this.themeMenuAnim <= 0.0001f) {
            return;
        }
        float[] pos = this.themeMenuPos();
        float menuX = pos[0];
        float menuY = pos[1];
        float menuW = pos[2];
        float fullH = pos[3];
        float menuH = fullH * this.themeMenuAnim;
        int alpha = (int) (ca * this.themeMenuAnim);

        int bgTint = mixChannel(0x0F0F0F, themeMain(), 0.08f);
        rblur(ctx, menuX, menuY, menuW, menuH, 10.0f, withAlpha(0x000000, (int) (alpha * 0.35f)));
        rrect(ctx, menuX, menuY, menuW, menuH, 10.0f, withAlpha(bgTint, (int) (alpha * 0.94f)));
        this.clip(ctx, menuX + 1.0f, menuY + 1.0f, menuW - 2.0f, Math.max(0.0f, menuH - 2.0f));
        try {
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f),
                localize("settings.theme"), menuX + 9.0f, menuY + 6.0f,
                toColor(softTextColor((int) (alpha * 0.55f))));
            ClientTheme current = UiThemeController.oX();
            float rowY = menuY + THEME_HEADER_H;
            for (ClientTheme theme : ClientTheme.values()) {
                if (rowY >= menuY + menuH - 2.0f) {
                    break;
                }
                if (theme == current) {
                    rrect(ctx, menuX + 4.0f, rowY + 1.0f, menuW - 8.0f, THEME_ROW_H - 2.0f,
                        6.0f, withAlpha(themeMain(), (int) (alpha * 0.14f)));
                }
                Color accent = theme.pe();
                rrect(ctx, menuX + 9.0f, rowY + (THEME_ROW_H - 7.0f) / 2.0f, 7.0f, 7.0f,
                    3.5f, withAlpha(accent.getRGB(), (int) (alpha * 0.95f)));
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f),
                    theme.getName(), menuX + 21.0f, rowY + 4.0f,
                    toColor(theme == current ? softTextColor(alpha) : softTextColor((int) (alpha * 0.58f))));
                if (theme == current) {
                    Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_ICONS.getFont(8.0f), "p",
                        menuX + menuW - 15.0f, rowY + 3.0f,
                        toColor(withAlpha(themeMain(), alpha)));
                }
                rowY += THEME_ROW_H;
            }
        } finally {
            Render2D.stopScissor(ctx);
        }
    }

    private boolean clickThemeMenu(float mx, float my) {
        if (!this.themeMenuOpen) {
            return false;
        }
        float[] pos = this.themeMenuPos();
        if (inside(mx, my, pos[0], pos[1], pos[2], pos[3])) {
            int index = (int) ((my - pos[1] - THEME_HEADER_H) / THEME_ROW_H);
            ClientTheme[] themes = ClientTheme.values();
            if (index >= 0 && index < themes.length) {
                UiThemeController.c(themes[index]);
                this.themeMenuOpen = false;
            }
            return true;
        }
        this.themeMenuOpen = false;
        return true;
    }

    private boolean clickThemeButton(float mx, float my) {
        float buttonX = this.x + BG_WIDTH - 12.0f - 18.0f;
        float buttonY = this.y + 7.0f;
        if (inside(mx, my, buttonX, buttonY, 18.0f, 18.0f)) {
            this.themeMenuOpen = !this.themeMenuOpen;
            this.searchFocused = false;
            if (this.themeMenuOpen) {
                this.editingSelect = null;
                this.closingSelect = null;
            }
            return true;
        }
        return false;
    }

    private boolean isEditingString(Module module, StringSetting setting) {
        return this.editingString != null
            && this.editingString.equals(setting.getName())
            && module.getName().equals(this.stringModuleName);
    }

    private void startStringEdit(Module module, StringSetting setting) {
        this.commitStringEdit(false);
        this.editingString = setting.getName();
        this.stringModuleName = module.getName();
        this.stringTextInput = new StringBuilder(setting.jT() == null ? "" : setting.jT());
        this.stringTextLimit = Math.max(1, setting.kf());
        this.editingBind = null;
        this.bindModuleName = null;
        this.bindSettingName = null;
        this.editingSliderText = null;
        this.draggingSlider = null;
        this.searchFocused = false;
    }

    private void applyStringInput() {
        if (this.editingString == null || this.stringTextInput == null) {
            return;
        }
        Module module = this.findModule(this.stringModuleName);
        if (module == null) {
            return;
        }
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof StringSetting ss && s.getName().equals(this.editingString)) {
                ss.bI(this.stringTextInput.toString());
                break;
            }
        }
    }

    private void commitStringEdit(boolean save) {
        if (this.editingString == null) {
            return;
        }
        this.applyStringInput();
        this.editingString = null;
        this.stringModuleName = null;
        this.stringTextInput = null;
        if (save) {
            saveClientConfig();
        }
    }

    private static Color toColor(int argb) {
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }

    /**
 * Обёртки под наш Render2D: его сигнатура и порядок аргументов отличаются
 * от Legion. Принимаем цвет последним, чтобы не путать его с радиусом.
 */
    /**
 * Обёртка под наш Render2D. Его сигнатура отличается от Legion, а в коде
 * порядок «радиус/цвет» плавает, поэтому последние два аргумента
 * разбираются здесь по типу: Float — радиус, Integer — цвет ARGB.
 */
    private static void rrect(class_332 ctx, float x, float y, float w, float h,
                              Object a, Object b) {
        float radius = a instanceof Float ? (Float) a : (Float) b;
        int argb = a instanceof Integer ? (Integer) a : (Integer) b;
        Render2D.drawRect(ctx.method_51448(), x, y, w, h, radius,
            new ru.method.clarityvisuals.utils.render.ColorRGBA(toColor(argb)));
    }

    private static void rblur(class_332 ctx, float x, float y, float w, float h,
                              Object a, Object b) {
        float radius = a instanceof Float ? (Float) a : (Float) b;
        int argb = a instanceof Integer ? (Integer) a : (Integer) b;
        Render2D.drawBlurredRect(ctx.method_51448(), x, y, w, h, radius, radius, toColor(argb));
    }

    // ==================== input ====================

    @Override
    public boolean method_25402(double mx, double my, int button) {
        if (this.closing || aiG == null || LegionClickGui.aiG.field_1755 != this) {
            return false;
        }
        int[] m = this.transformMouse(mx, my);
        float fmx = m[0];
        float fmy = m[1];

        // Клик мышью во время ожидания бинда сразу назначает кнопку.
        if (this.editingBind != null) {
            this.applyMouseBind(button);
            return true;
        }

        // Строка ввода сохраняется при клике в любом другом месте.
        if (this.editingString != null) {
            this.commitStringEdit(true);
        }

        // Открытая палитра перехватывает клики внутри себя; клик мимо закрывает её.
        if (this.colorPicker.isActive()) {
            if (this.colorPicker.mouseClicked(fmx, fmy, button)) {
                return true;
            }
            this.colorPicker.close();
        }
        if (this.clickThemeMenu(fmx, fmy)) {
            return true;
        }
        if (this.clickThemeButton(fmx, fmy)) {
            return true;
        }
        if (this.clickConfigButton(fmx, fmy)) {
            return true;
        }
        if (this.configPanelOpen && this.clickConfigPanel(fmx, fmy, button)) {
            return true;
        }
        // Ветка «HW Events»: свои контролы, обычные модули не задействованы.
        if (this.hwView) {
            if (this.clickHwControls(fmx, fmy, button)) {
                return true;
            }
            if (inside(fmx, fmy, this.x + LEFT_W, this.y + 36.0f, BG_WIDTH - LEFT_W, BG_HEIGHT - 36.0f)) {
                return true;
            }
            this.clickCategories(fmx, fmy);
            if (button == 0 && this.isFullyOpen()
                && inside(fmx, fmy, this.x + LEFT_W, this.y, BG_WIDTH - LEFT_W, 34.0f)) {
                this.dragging = true;
                this.dragOffsetX = fmx - this.x;
                this.dragOffsetY = fmy - this.y;
                return true;
            }
            return super.method_25402(mx, my, button);
        }

        // Панель настроек рисуется поверх остального и получает клики первой.
        if (this.editingModule != null) {
            Module em = this.findModule(this.editingModule);
            if (em != null) {
                float[] pos = this.settingsPanelPos(em, this.editingModuleX, this.editingModuleY);
                if (button == 0 && inside(fmx, fmy, pos[0], pos[1], SETTINGS_W, 9.0f)) {
                    this.settingsDragging = true;
                    this.settingsDragOffX = fmx - pos[0];
                    this.settingsDragOffY = fmy - pos[1];
                    return true;
                }
                if (this.clickEnumDropdown(fmx, fmy)) {
                    return true;
                }
                if (inside(fmx, fmy, pos[0], pos[1], SETTINGS_W, pos[2])) {
                    // Клик по самой панели не должен проваливаться к модулям под ней.
                    this.clickSettingsRow(fmx, fmy, button);
                    return true;
                }
                // Клик мимо панели закрывает её, но не блокирует клик по GUI.
                if (button == 0) {
                    this.closeSettings();
                }
            }
        } else if (this.clickEnumDropdown(fmx, fmy)) {
            return true;
        }

        if (this.clickSearch(fmx, fmy)) {
            return true;
        }
        if (this.clickModules(fmx, fmy, button)) {
            return true;
        }
        this.clickCategories(fmx, fmy);

        // Перетаскивание окна — только за свободную часть шапки и только
        // после окончания анимации открытия, иначе окно «прыгает» при клике.
        if (button == 0 && this.editingSelect == null && this.isFullyOpen()
            && inside(fmx, fmy, this.x + LEFT_W, this.y, BG_WIDTH - LEFT_W, 34.0f)) {
            this.dragging = true;
            this.dragOffsetX = fmx - this.x;
            this.dragOffsetY = fmy - this.y;
            return true;
        }
        return super.method_25402(mx, my, button);
    }

    private void clickCategories(float mx, float my) {
        float catY = this.y + 55.0f;
        for (ModuleCategory c : visibleCategories()) {
            if (inside(mx, my, this.x + 8.0f, catY, LEFT_W - 16.0f, CAT_ROW_H)) {
                if (this.selectedCategory != c || this.hwView) {
                    this.scrollTarget = 0.0f;
                    this.scrollOffset = 0.0f;
                    this.hwView = false;
                }
                this.selectedCategory = c;
                this.closeSettings();
                return;
            }
            catY += CAT_ROW_H;
        }
        // строка «HW Events» — после всех категорий
        if (inside(mx, my, this.x + 8.0f, catY, LEFT_W - 16.0f, CAT_ROW_H)) {
            if (!this.hwView) {
                this.hwView = true;
                this.scrollTarget = 0.0f;
                this.scrollOffset = 0.0f;
            }
            this.closeSettings();
        }
    }

    private boolean clickSearch(float mx, float my) {
        float searchW = 90.0f;
        float searchX = this.x + BG_WIDTH - 36.0f - searchW;
        if (inside(mx, my, searchX, this.y + 8.0f, searchW, 19.0f)) {
            this.searchFocused = true;
            return true;
        }
        return false;
    }

    private boolean clickModules(float mx, float my, int button) {
        List<Module> modules = new ArrayList<>();
        for (Module m : categoryModules()) {
            if (matchesSearch(m)) {
                modules.add(m);
            }
        }
        if (modules.isEmpty()) {
            return false;
        }
        float rightX = this.x + LEFT_W + 8.0f;
        float rightW = BG_WIDTH - LEFT_W - 16.0f;
        float colW = (rightW - 6.0f) / 2.0f;
        float startY = this.y + 42.0f - this.scrollOffset;
        float[] colY = {startY, startY};
        float[] colX = {rightX, rightX + colW + 6.0f};
        int colIdx = 0;

        Map<String, List<Module>> groups = this.buildGroups(modules, this.selectedCategory);
        for (Map.Entry<String, List<Module>> entry : groups.entrySet()) {
            List<Module> gm = entry.getValue();
            if (gm.isEmpty()) {
                continue;
            }
            float cardX = colX[colIdx];
            float cardH = 8.0f + gm.size() * MOD_ROW_H;
            float gy = colY[colIdx];
            float modY = gy + 18.0f;
            for (Module module : gm) {
                // Хит-тест повторяет частичную видимость из рендера: полностью
                // прижатый к низу последний ряд уходит за panelBottom на 1px
                // и раньше выпадал из проверки — его нельзя было переключить.
                if (modY + MOD_ROW_H > this.y + 36.0f && modY < this.y + BG_HEIGHT - 3.0f) {
                    if (inside(mx, my, cardX, modY, colW, MOD_ROW_H)) {
                        float pillW = 16.0f, pillH = 9.0f;
                        float pillX = cardX + colW - 8.0f - pillW;
                        float pillY = modY + 1.0f;
                        // Правая кнопка по строке — настройки модуля.
                        if (button == 1) {
                            if (!visibleSettings(module).isEmpty()) {
                                this.openSettings(module);
                            }
                            return true;
                        }
                        if (button == 0) {
                            // Шестерёнка слева от тумблера — настройки,
                            // сам тумблер и остальная строка — вкл/выкл.
                            if (inside(mx, my, pillX - 14.0f, pillY - 2.0f, 12.0f, pillH + 4.0f)) {
                                if (!visibleSettings(module).isEmpty()) {
                                    this.openSettings(module);
                                }
                                return true;
                            }
                            module.toggle();
                            return true;
                        }
                    }
                }
                modY += MOD_ROW_H;
            }
            colY[colIdx] += 10.0f + cardH + 6.0f;
            colIdx = colY[0] <= colY[1] ? 0 : 1;
        }
        return false;
    }

    private void openSettings(Module m) {
        this.commitStringEdit(true);
        this.colorPicker.closeNow();
        this.editingModule = m.getName();
        this.closingModule = null;
        this.settingsAnim = Math.min(this.settingsAnim, 0.2f);
        // Новый модуль всегда открывается на стандартном месте, а не там,
        // куда игрок перетащил панель предыдущего.
        this.settingsOffX = 0.0f;
        this.settingsOffY = 0.0f;
        this.settingsScroll = 0.0f;
        this.settingsScrollTarget = 0.0f;
    }

    private void closeSettings() {
        this.commitStringEdit(true);
        this.colorPicker.closeNow();
        if (this.editingModule != null) {
            this.closingModule = this.editingModule;
        }
        this.editingModule = null;
        this.editingSelect = null;
        this.closingSelect = null;
        this.editingBind = null;
        this.bindModuleName = null;
        this.bindSettingName = null;
        this.draggingSlider = null;
        this.editingSliderText = null;
        this.settingsDragging = false;
    }

    private boolean clickEnumDropdown(float mx, float my) {
        if (this.editingSelect == null) {
            return false;
        }
        Module module = this.findModule(this.editingModule);
        if (module == null) {
            this.editingSelect = null;
            return false;
        }
        EnumSetting<?> target = null;
        for (Setting<?> s : visibleSettings(module)) {
            if (s.getName().equals(this.editingSelect) && s instanceof EnumSetting<?> es) {
                target = es;
                break;
            }
        }
        if (target == null) {
            this.editingSelect = null;
            return false;
        }
        Enum<?>[] values = target.jT().getDeclaringClass().getEnumConstants();
        float dropItemH = 14.0f;
        float[] dropPos = this.selectDropdownPos(target);
        float dropX = dropPos[0];
        float dropY = dropPos[1];
        float iy = dropY + 3.0f;
        for (Enum<?> opt : values) {
            if (inside(mx, my, dropX, iy, dropPos[2], dropItemH)) {
                // bS() сам сопоставляет константу по имени и дженерики не ломает.
                target.bS(opt.name());
                this.closingSelect = this.editingSelect;
                this.editingSelect = null;
                return true;
            }
            iy += dropItemH;
        }
        this.closingSelect = this.editingSelect;
        this.editingSelect = null;
        return true;
    }

    private boolean clickSettingsRow(float mx, float my, int button) {
        if (this.editingModule == null) {
            return false;
        }
        Module module = this.findModule(this.editingModule);
        if (module == null) {
            return false;
        }
        float[] pos = this.settingsPanelPos(module, this.editingModuleX, this.editingModuleY);
        float panelX = pos[0];
        float panelY = pos[1];
        float panelH = pos[2];
        if (!inside(mx, my, panelX, panelY, SETTINGS_W, panelH)) {
            return false;
        }
        float valX = panelX + SETTINGS_W - 8.0f;
        float contentTop = panelY + 9.0f;
        float contentBottom = panelY + panelH - 5.0f;
        float bindRowY = contentTop - this.settingsScroll;
        if (button == 0 && bindRowY >= contentTop - 0.5f
            && bindRowY + SET_ROW_H <= contentBottom + 0.5f
            && inside(mx, my, panelX + 8.0f, bindRowY, SETTINGS_W - 16.0f, SET_ROW_H)) {
            this.editingBind = module.getName();
            this.bindModuleName = module.getName();
            this.bindSettingName = null;
            this.editingString = null;
            return true;
        }
        float setY = bindRowY + SET_ROW_H;

        for (Setting<?> s : visibleSettings(module)) {
            boolean sliderRow = isSliderRow(s);
            float rowH = sliderRow ? SLIDER_ROW_H : SET_ROW_H;
            boolean rowVisible = setY >= contentTop - 0.5f && setY + rowH <= contentBottom + 0.5f;

            if (rowVisible && button == 0) {
                if (s instanceof SliderSetting sl) {
                    float sliderW = SETTINGS_W - 20.0f;
                    float sliderX = panelX + 10.0f;
                    float sliderY = setY + rowH - 10.0f;
                    if (inside(mx, my, sliderX - 4.0f, sliderY - 3.0f, sliderW + 8.0f, 9.0f)) {
                        this.draggingSlider = s.getName();
                        this.updateSlider(sl, sliderX, sliderW, mx);
                        return true;
                    }
                } else if (s instanceof BooleanSetting bs) {
                    float pillW = 12.0f, pillH = 7.0f;
                    float pillX = valX - pillW;
                    float pillY = setY + (rowH - pillH) / 2.0f;
                    if (inside(mx, my, pillX - 2.0f, pillY - 2.0f, pillW + 4.0f, pillH + 4.0f)) {
                        bs.a(!Boolean.TRUE.equals(bs.jT()));
                        return true;
                    }
                } else if (s instanceof EnumSetting<?> es) {
                    if (inside(mx, my, panelX + 8.0f, setY, SETTINGS_W - 16.0f, rowH)) {
                        this.editingSelect = s.getName();
                        this.editingSelectX = valX - 60.0f;
                        this.editingSelectY = setY;
                        this.selectAnim = 0.0f;
                        return true;
                    }
                } else if (s instanceof ButtonSetting btn) {
                    if (inside(mx, my, panelX + 8.0f, setY, SETTINGS_W - 16.0f, rowH)) {
                        btn.run();
                        return true;
                    }
                } else if (s instanceof BindSetting) {
                    if (inside(mx, my, panelX + 8.0f, setY, SETTINGS_W - 16.0f, rowH)) {
                        this.editingBind = s.getName();
                        this.bindModuleName = module.getName();
                        this.bindSettingName = s.getName();
                        this.editingString = null;
                        return true;
                    }
                } else if (s instanceof StringSetting ss) {
                    if (inside(mx, my, panelX + 8.0f, setY, SETTINGS_W - 16.0f, rowH)) {
                        this.startStringEdit(module, ss);
                        return true;
                    }
                } else if (s instanceof ColorSetting cs) {
                    // Свотч 8x8 справа в строке; расширенная зона клика.
                    float size = 8.0f;
                    float swX = valX - size;
                    float swY = setY + (rowH - size) / 2.0f;
                    if (inside(mx, my, swX - 3.0f, swY - 3.0f, size + 6.0f, size + 6.0f)) {
                        if (this.colorPicker.isActive() && this.colorPicker.getSetting() == cs) {
                            // Повторный клик по тому же свотчу закрывает палитру.
                            this.colorPicker.close();
                        } else {
                            this.searchFocused = false;
                            this.editingString = null;
                            this.colorPicker.open(cs, panelX, SETTINGS_W,
                                setY + SET_ROW_H / 2.0f, this.designW(), this.designH());
                        }
                        return true;
                    }
                }
            }
            setY += rowH;
        }
        // Клик по пустому месту панели поглощаем, чтобы он не задевал модули под ней.
        return true;
    }

    private void updateSlider(SliderSetting sl, float sliderX, float sliderW, float mx) {
        float t = (mx - sliderX) / sliderW;
        t = Math.max(0.0f, Math.min(1.0f, t));
        float range = sl.kc() - sl.kb();
        float step = sl.kd();
        float raw = sl.kb() + range * t;
        float snapped = step > 0.0f ? Math.round(raw / step) * step : raw;
        snapped = Math.max(sl.kb(), Math.min(sl.kc(), snapped));
        sl.a(snapped);
    }

    @Override
    public boolean method_25406(double mx, double my, int button) {
        this.colorPicker.mouseReleased();
        if (this.dragging) {
            this.dragging = false;
            return true;
        }
        if (this.settingsDragging) {
            this.settingsDragging = false;
            return true;
        }
        if (this.draggingSlider != null) {
            this.draggingSlider = null;
            return true;
        }
        return super.method_25406(mx, my, button);
    }

    @Override
    public boolean method_25401(double mx, double my, double dh, double dv) {
        if (this.closing) {
            return false;
        }
        if (this.editingBind != null) {
            if (dv != 0.0) {
                this.applyBindValue(dv > 0.0 ? KeyBind.Kg : KeyBind.Kh, true);
            }
            return true;
        }
        int[] m = this.transformMouse(mx, my);
        // Колесо над палитрой не должно прокручивать панель настроек под ней.
        if (this.colorPicker.isActive() && this.colorPicker.isInside(m[0], m[1])) {
            return true;
        }
        if (this.draggingSlider != null) {
            Module module = this.findModule(this.editingModule);
            if (module != null) {
                for (Setting<?> s : visibleSettings(module)) {
                    if (s instanceof SliderSetting sl && s.getName().equals(this.draggingSlider)) {
                        float[] pos = this.settingsPanelPos(module, this.editingModuleX, this.editingModuleY);
                        this.updateSlider(sl, pos[0] + 10.0f, SETTINGS_W - 20.0f, m[0]);
                        return true;
                    }
                }
            }
            return true;
        }
        // Колесо над панелью конфигов прокручивает список конфигов.
        if (this.configPanelOpen) {
            float[] panel = this.configPanelRect();
            if (inside(m[0], m[1], panel[0], panel[1], panel[2], panel[3])) {
                float maxCfgScroll = Math.max(0.0f, this.configListHeight() - this.configListAreaH());
                if (maxCfgScroll > 0.0f) {
                    float deltaCfg = (float) Math.max(-1.0, Math.min(1.0, dv));
                    this.configScrollTarget = clamp(this.configScrollTarget - deltaCfg * 12.0f, 0.0f, maxCfgScroll);
                }
                return true;
            }
        }
        // Колесо над панелью настроек прокручивает её содержимое.
        if (this.editingModule != null) {
            Module module = this.findModule(this.editingModule);
            if (module != null) {
                float[] pos = this.settingsPanelPos(module, this.editingModuleX, this.editingModuleY);
                if (inside(m[0], m[1], pos[0], pos[1], SETTINGS_W, pos[2])) {
                    float maxScroll = Math.max(0.0f, this.settingsContentHeight(module) - pos[2]);
                    if (maxScroll > 0.0f) {
                        float delta = (float) Math.max(-1.0, Math.min(1.0, dv));
                        this.settingsScrollTarget = clamp(this.settingsScrollTarget - delta * 12.0f,
                            0.0f, maxScroll);
                    }
                    return true;
                }
            }
        }
        if (inside(m[0], m[1], this.x + LEFT_W, this.y + 36.0f, BG_WIDTH - LEFT_W, BG_HEIGHT - 36.0f)) {
            float delta = (float) Math.max(-1.0, Math.min(1.0, dv));
            if (this.contentHeight > this.viewHeight) {
                float maxScroll = this.contentHeight - this.viewHeight;
                this.scrollTarget = clamp(this.scrollTarget - delta * 12.0f, 0.0f, maxScroll);
            }
            return true;
        }
        return super.method_25401(mx, my, dh, dv);
    }

    @Override
    public boolean method_25404(int key, int scancode, int mods) {
        if (this.closing) {
            return false;
        }
        if (this.editingBind != null) {
            this.applyBind(key);
            return true;
        }
        if (this.colorPicker.keyPressed(key)) {
            return true;
        }
        if (this.editingString != null) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.stringTextInput != null && this.stringTextInput.length() > 0) {
                    this.stringTextInput.deleteCharAt(this.stringTextInput.length() - 1);
                    this.applyStringInput();
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_DELETE) {
                if (this.stringTextInput != null) {
                    this.stringTextInput.setLength(0);
                    this.applyStringInput();
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER
                || key == GLFW.GLFW_KEY_ESCAPE) {
                this.commitStringEdit(true);
                return true;
            }
            if (key == GLFW.GLFW_KEY_V && class_437.method_25441() && aiG != null
                && aiG.method_22683() != null) {
                String clipboard = GLFW.glfwGetClipboardString(aiG.method_22683().method_4490());
                if (clipboard != null && this.stringTextInput != null) {
                    for (int i = 0; i < clipboard.length()
                        && this.stringTextInput.length() < this.stringTextLimit; i++) {
                        char c = clipboard.charAt(i);
                        if (c >= ' ' && c != 127 && !Character.isISOControl(c)) {
                            this.stringTextInput.append(c);
                        }
                    }
                    this.applyStringInput();
                }
                return true;
            }
            return true;
        }
        if (this.configNameFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.configNameFocused = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                this.saveNewConfig();
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.configNameInput.length() > 0) {
                    this.configNameInput.deleteCharAt(this.configNameInput.length() - 1);
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_DELETE) {
                this.configNameInput.setLength(0);
                return true;
            }
            if (key == GLFW.GLFW_KEY_V && class_437.method_25441() && aiG != null
                && aiG.method_22683() != null) {
                String cfgClipboard = GLFW.glfwGetClipboardString(aiG.method_22683().method_4490());
                if (cfgClipboard != null) {
                    for (int ci = 0; ci < cfgClipboard.length()
                        && this.configNameInput.length() < 24; ci++) {
                        char cc = cfgClipboard.charAt(ci);
                        if (cc >= ' ' && cc != 127 && !Character.isISOControl(cc)) {
                            this.configNameInput.append(cc);
                        }
                    }
                }
                return true;
            }
            return true;
        }
        if (this.searchFocused && key == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.searchInput.length() > 0) {
                this.searchInput.deleteCharAt(this.searchInput.length() - 1);
            }
            return true;
        }
        if (this.editingSliderText != null) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.sliderTextInput.length() > 0) {
                    this.sliderTextInput.deleteCharAt(this.sliderTextInput.length() - 1);
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                this.commitSliderText();
                return true;
            }
            return true;
        }
        if (this.searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.searchFocused = false;
                this.searchInput.setLength(0);
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.searchInput.length() > 0) {
                    this.searchInput.deleteCharAt(this.searchInput.length() - 1);
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                this.searchFocused = false;
                return true;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.beginClose();
            return true;
        }
        return super.method_25404(key, scancode, mods);
    }

    private void applyBind(int key) {
        this.applyBindValue(key, false);
    }

    private void applyMouseBind(int button) {
        this.applyBindValue(button, true);
    }

    private void applyBindValue(int key, boolean mouse) {
        Module module = this.findModule(this.bindModuleName);
        if (module != null) {
            KeyBind bind = this.resolveBind(module);
            if (bind != null) {
                if (!mouse && (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE)) {
                    bind.ah(-1);
                    bind.u(false);
                } else {
                    bind.ah(key);
                    bind.u(mouse);
                }
                saveClientConfig();
            }
        }
        this.editingBind = null;
        this.bindModuleName = null;
        this.bindSettingName = null;
    }

    private KeyBind resolveBind(Module module) {
        if (this.bindSettingName == null) {
            return module.getBind();
        }
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BindSetting bs && s.getName().equals(this.bindSettingName)) {
                return bs.jT();
            }
        }
        return null;
    }

    private void commitSliderText() {
        if (this.editingSliderText == null) {
            return;
        }
        Module module = this.findModule(this.editingModule);
        String name = this.editingSliderText;
        this.editingSliderText = null;
        if (module == null) {
            return;
        }
        try {
            float parsed = Float.parseFloat(this.sliderTextInput.toString().trim());
            for (Setting<?> s : visibleSettings(module)) {
                if (s instanceof SliderSetting sl && s.getName().equals(name)) {
                    sl.a(Math.max(sl.kb(), Math.min(sl.kc(), parsed)));
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
        this.sliderTextInput = null;
    }

    @Override
    public boolean method_25400(char c, int mods) {
        if (this.colorPicker.charTyped(c)) {
            return true;
        }
        if (this.editingString != null) {
            if (c >= ' ' && c != 127 && !Character.isISOControl(c)
                && this.stringTextInput != null
                && this.stringTextInput.length() < this.stringTextLimit) {
                this.stringTextInput.append(c);
                this.applyStringInput();
            }
            return true;
        }
        if (this.configNameFocused) {
            if (c >= ' ' && c != 127 && !Character.isISOControl(c)
                && this.configNameInput.length() < 24) {
                this.configNameInput.append(c);
            }
            return true;
        }
        if (this.searchFocused && !this.editingSliderText0()) {
            if (c >= ' ' && this.searchInput.length() < 32) {
                this.searchInput.append(c);
            }
            return true;
        }
        if (this.editingSliderText != null) {
            if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
                this.sliderTextInput.append(c);
                return true;
            }
        }
        return super.method_25400(c, mods);
    }

    private boolean editingSliderText0() {
        return false;
    }

    @Override
    public void method_25426() {
        this.prepareForOpen();
    }

    @Override
    public void method_25393() {
    }

    @Override
    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.closeSettings();
        this.searchFocused = false;
        this.editingBind = null;
        this.bindModuleName = null;
        this.bindSettingName = null;
        this.editingString = null;
        this.stringModuleName = null;
        this.stringTextInput = null;
        this.themeMenuOpen = false;
        this.themeMenuAnim = 0.0f;
        this.configPanelOpen = false;
        this.configNameFocused = false;
        this.configPanelAnim = 0.0f;
        this.hwRarityFilter = "";
        this.hwEventFilter = "";
        LiquidGlassReflectionCache.invalidate(LiquidGlassReflectionCache.ReflectionSource.CLICK_GUI);
    }

    @Override
    public void method_25432() {
        this.cancelClose();
        super.method_25432();
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    public boolean isOpen() {
        return aiG != null && LegionClickGui.aiG.field_1755 == this && !this.closing;
    }

    public boolean isDisplayed() {
        return aiG != null && LegionClickGui.aiG.field_1755 == this;
    }

    public boolean isClosing() {
        return this.closing;
    }

    /** true, пока GUI перехватывает клавиши для бинда или текстового поля. */
    public boolean hasActiveBinding() {
        return this.editingBind != null || this.editingString != null || this.editingSliderText != null
            || this.configNameFocused;
    }

    // ==================== HW Events (порт из clarity) ====================

    private static int hwRarityRank(String rarity) {
        if (rarity == null) {
            return 7;
        }
        return switch (rarity.toLowerCase()) {
            case "легендарная" -> 0;
            case "эпическая" -> 1;
            case "роскошная" -> 2;
            case "зажиточная" -> 3;
            case "редкая" -> 4;
            case "особая" -> 5;
            case "смертельная", "взрывоопасная", "мирная" -> 6;
            default -> 7;
        };
    }

    private static int hwRarityColor(String rarity) {
        if (rarity == null) {
            return -7035976;
        }
        return switch (rarity.toLowerCase()) {
            case "обычная" -> -7035976;
            case "редкая" -> -10443270;
            case "эпическая" -> -5796870;
            case "легендарная" -> -278748;
            case "роскошная" -> -13773633;
            case "зажиточная" -> -49782;
            case "мирная" -> -11870592;
            case "смертельная" -> -1096636;
            case "взрывоопасная" -> -290244;
            case "особая" -> -495247;
            default -> -7035976;
        };
    }

    private static String hwPluralEvents(int n) {
        int d = n % 10;
        int dd = n % 100;
        if (dd >= 11 && dd <= 14) {
            return "ивентов";
        }
        if (d == 1) {
            return "ивент";
        }
        if (d >= 2 && d <= 4) {
            return "ивента";
        }
        return "ивентов";
    }

    private static String hwCountdown(long ms) {
        long totalSec = Math.max(0L, (ms + 999L) / 1000L);
        long h = totalSec / 3600L;
        long m = totalSec % 3600L / 60L;
        long s = totalSec % 60L;
        return h > 0L ? String.format("%d:%02d:%02d", h, m, s) : String.format("%d:%02d", m, s);
    }

    private static String hwServerNumber(String id) {
        if (id == null) {
            return id;
        }
        int idx = id.lastIndexOf(95);
        if (idx < 0 || idx == id.length() - 1) {
            return id;
        }
        try {
            Integer.parseInt(id.substring(idx + 1));
            return id.substring(0, idx);
        } catch (NumberFormatException e) {
            return id;
        }
    }

    private String hwServerLabel(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        return this.hwServers.getOrDefault(id, id);
    }

    private void refreshHwServers() {
        long now = System.currentTimeMillis();
        if (now - this.hwServersFetchTime < HW_SERVER_INTERVAL) {
            return;
        }
        this.hwServersFetchTime = now;
        new Thread(() -> {
            Map<String, String> fetched;
            try {
                fetched = HwEventsApi.fetchServers();
            } catch (Exception e) {
                fetched = Collections.emptyMap();
            }
            Map<String, String> result = fetched;
            class_310.method_1551().execute(() -> this.hwServers.putAll(result));
        }).start();
    }

    private void refreshHwEvents() {
        long now = System.currentTimeMillis();
        if (now - this.hwFetchTime < HW_FETCH_INTERVAL || this.hwLoading) {
            return;
        }
        this.hwFetchTime = now;
        this.hwLoading = true;
        new Thread(() -> {
            List<HwEventsApi.HwEvent> fetched;
            try {
                fetched = HwEventsApi.fetchEvents(this.hwSelectedServer);
            } catch (Exception e) {
                fetched = Collections.emptyList();
            }
            List<HwEventsApi.HwEvent> result = fetched;
            class_310.method_1551().execute(() -> {
                this.hwEvents.clear();
                this.hwEvents.addAll(result);
                this.hwEvents.sort(Comparator.comparingInt(e -> hwRarityRank(e.displayRarity())));
                this.hwLoading = false;
            });
        }).start();
    }

    /** Список ивентов с применением двух фильтров и сортировкой по редкости. */
    private List<HwEventsApi.HwEvent> hwVisibleEvents() {
        List<HwEventsApi.HwEvent> list = new ArrayList<>(this.hwEvents);
        if (!this.hwRarityFilter.isEmpty()) {
            list.removeIf(e -> !e.displayRarity().equals(this.hwRarityFilter));
        }
        if (!this.hwEventFilter.isEmpty()) {
            list.removeIf(e -> !e.displayName().equals(this.hwEventFilter));
        }
        list.sort(Comparator.comparingInt(e -> hwRarityRank(e.displayRarity())));
        return list;
    }

    /** «Все» + все редкости, реально встречающиеся в списке, по возрастанию ранга. */
    private List<String> hwRarityOptions() {
        List<String> out = new ArrayList<>();
        out.add("Все");
        List<String> seen = new ArrayList<>();
        for (HwEventsApi.HwEvent e : this.hwEvents) {
            String r = e.displayRarity();
            if (r == null || r.isEmpty()) {
                continue;
            }
            // «Мирная», «Взрывоопасная», «Смертельная» не показываем в чипах-фильтрах —
            // сами ивенты с этими редкостями остаются в списке и в фильтре «Все».
            String low = r.toLowerCase();
            if (low.contains("мирная") || low.contains("взрывоопасная") || low.contains("смертельная")) {
                continue;
            }
            if (!seen.contains(r)) {
                seen.add(r);
            }
        }
        seen.sort(Comparator.comparingInt(LegionClickGui::hwRarityRank));
        out.addAll(seen);
        return out;
    }

    /** «Все ивенты» + все названия ивентов, реально встречающиеся в списке. */
    private List<String> hwEventOptions() {
        List<String> out = new ArrayList<>();
        out.add("Все ивенты");
        for (HwEventsApi.HwEvent e : this.hwEvents) {
            String n = e.displayName();
            if (n != null && !n.isEmpty() && !out.contains(n)) {
                out.add(n);
            }
        }
        return out;
    }

    /**
     * Раскладывает чипы с переносом строк. Возвращает список прямоугольников
     * {x, y, w} и заполняет итоговую высоту через outRowBottom[0].
     */
    private List<float[]> hwLayoutChips(List<String> labels, float xStart, float y, float maxW) {
        List<float[]> rects = new ArrayList<>();
        float cx = xStart;
        float cy = y;
        for (String label : labels) {
            float w = Fonts.LEGION_INTER.getWidth(label, 7.0f) + 14.0f;
            if (cx + w > xStart + maxW && cx > xStart) {
                cx = xStart;
                cy += HW_CHIP_H + HW_CHIP_GAP;
            }
            rects.add(new float[]{cx, cy, w});
            cx += w + HW_CHIP_GAP;
        }
        this.hwFiltersBottom = cy + HW_CHIP_H;
        return rects;
    }

    private void hwDrawChip(class_332 ctx, int ca, float mx, float my, List<String> labels,
                            List<float[]> rects, String selected, int activeColor) {
        for (int i = 0; i < rects.size(); ++i) {
            float[] r = rects.get(i);
            String label = labels.get(i);
            boolean active = label.equals(selected) || (selected.isEmpty() && i == 0);
            boolean hov = inside(mx, my, r[0], r[1], r[2], HW_CHIP_H);
            int chipColor = active ? activeColor : -1;
            rrect(ctx, r[0], r[1], r[2], HW_CHIP_H, HW_CHIP_H / 2.0f,
                withAlpha(chipColor, (int) (ca * (active ? 0.22f : (hov ? 0.10f : 0.05f)))));
            int textColor = active ? softTextColor((int) (ca * 0.95f)) : softTextColor((int) (ca * (hov ? 0.65f : 0.45f)));
            float tw = Fonts.LEGION_INTER.getWidth(label, 7.0f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), label,
                r[0] + (r[2] - tw) / 2.0f, vCenter(r[1], HW_CHIP_H, 7.0f), toColor(textColor));
        }
    }

    private void renderHwEvents(class_332 ctx, int ca, int mx, int my) {
        this.refreshHwServers();
        this.refreshHwEvents();

        float rightX = this.x + LEFT_W + 8.0f;
        float rightW = BG_WIDTH - LEFT_W - 16.0f;
        float panelBottom = this.y + BG_HEIGHT - 3.0f;
        float rowY = this.y + 41.0f;
        float chipMaxW = rightW - 34.0f;

        // кнопка обновления (правый верх Content-зоны)
        this.hwRefreshX = rightX + rightW - 30.0f;
        this.hwRefreshY = rowY - 1.0f;
        boolean rfHov = inside(mx, my, this.hwRefreshX, this.hwRefreshY, 26.0f, 26.0f);
        this.hwRefreshAnim = approach(this.hwRefreshAnim, rfHov ? 1.0f : 0.0f, 0.2f);

        // строка 1 — фильтр по редкости, строка 2 — фильтр по самим ивентам
        this.hwRarityChipLabels = this.hwRarityOptions();
        this.hwRarityChipRects = this.hwLayoutChips(this.hwRarityChipLabels, rightX, rowY, chipMaxW);
        float eventRowY = this.hwFiltersBottom + 4.0f;
        this.hwEventChipLabels = this.hwEventOptions();
        this.hwEventChipRects = this.hwLayoutChips(this.hwEventChipLabels, rightX, eventRowY, chipMaxW);
        this.hwFiltersBottom += 4.0f;
        float cardsStart = this.hwFiltersBottom + 8.0f;

        List<HwEventsApi.HwEvent> visible = this.hwVisibleEvents();
        boolean sameServer = true;
        if (!visible.isEmpty()) {
            String first = this.hwServerLabel(visible.get(0).server());
            for (HwEventsApi.HwEvent e : visible) {
                if (!this.hwServerLabel(e.server()).equals(first)) {
                    sameServer = false;
                    break;
                }
            }
        }
        float contentH = visible.size() * (HW_CARD_H + HW_CARD_GAP) - HW_CARD_GAP;
        if (contentH < 0.0f) {
            contentH = 0.0f;
        }
        float viewH = panelBottom - cardsStart;
        float maxScroll = Math.max(0.0f, contentH - viewH);
        this.scrollTarget = clamp(this.scrollTarget, 0.0f, maxScroll);
        this.scrollOffset = clamp(this.scrollTarget, 0.0f, maxScroll);
        this.scrollOffset = approach(this.scrollOffset, this.scrollTarget, 0.18f);
        this.scrollOffset = clamp(this.scrollOffset, 0.0f, maxScroll);
        this.contentHeight = contentH;
        this.viewHeight = Math.max(0.0f, viewH);

        // чипы фильтров и кнопка обновления — фиксированные, вне скролл-клипа

        // строка 1 — фильтр по редкости (чипы в цветах редкостей)
        for (int i = 0; i < this.hwRarityChipRects.size(); ++i) {
            String label = this.hwRarityChipLabels.get(i);
            float[] r = this.hwRarityChipRects.get(i);
            boolean active = i == 0 ? this.hwRarityFilter.isEmpty() : this.hwRarityFilter.equals(label);
            boolean hov = inside(mx, my, r[0], r[1], r[2], HW_CHIP_H);
            int chipColor = i == 0 ? (active ? themeMain() : -1) : hwRarityColor(label);
            rrect(ctx, r[0], r[1], r[2], HW_CHIP_H, HW_CHIP_H / 2.0f,
                withAlpha(chipColor, (int) (ca * (active ? 0.26f : (hov ? 0.10f : 0.05f)))));
            int textColor = active ? softTextColor((int) (ca * 0.95f)) : softTextColor((int) (ca * (hov ? 0.65f : 0.45f)));
            float tw = Fonts.LEGION_INTER.getWidth(label, 7.0f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), label,
                r[0] + (r[2] - tw) / 2.0f, vCenter(r[1], HW_CHIP_H, 7.0f), toColor(textColor));
        }
        // строка 2 — фильтр по самим ивентам
        for (int i = 0; i < this.hwEventChipRects.size(); ++i) {
            String label = this.hwEventChipLabels.get(i);
            float[] r = this.hwEventChipRects.get(i);
            boolean active = i == 0 ? this.hwEventFilter.isEmpty() : this.hwEventFilter.equals(label);
            boolean hov = inside(mx, my, r[0], r[1], r[2], HW_CHIP_H);
            int chipColor = active ? themeMain() : -1;
            rrect(ctx, r[0], r[1], r[2], HW_CHIP_H, HW_CHIP_H / 2.0f,
                withAlpha(chipColor, (int) (ca * (active ? 0.26f : (hov ? 0.10f : 0.05f)))));
            int textColor = active ? softTextColor((int) (ca * 0.95f)) : softTextColor((int) (ca * (hov ? 0.65f : 0.45f)));
            float tw = Fonts.LEGION_INTER.getWidth(label, 7.0f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), label,
                r[0] + (r[2] - tw) / 2.0f, vCenter(r[1], HW_CHIP_H, 7.0f), toColor(textColor));
        }

        // кнопка обновления: фон + иконка, при загрузке — приглушена
        rrect(ctx, this.hwRefreshX, this.hwRefreshY, 26.0f, 26.0f, 9.0f,
            withAlpha(themeMain(), (int) (ca * (0.08f + 0.10f * this.hwRefreshAnim))));
        int rfIconColor = softTextColor((int) (ca * (this.hwLoading ? 0.35f : 0.75f)));
        ClickGuiIcons.refresh(ctx.method_51448(), this.hwRefreshX + 13.0f, this.hwRefreshY + 13.0f, 10.0f, toColor(rfIconColor));

        // карточки — клип начинается ниже фильтров, чтобы при скролле
        // карточки не наезжали на чипы
        this.clip(ctx, rightX - 1.0f, cardsStart, rightW + 2.0f, Math.max(0.0f, panelBottom - cardsStart));

        // карточки ивентов
        if (visible.isEmpty()) {
            String meta = this.hwLoading ? "Загрузка..." : "Сейчас нет активных ивентов";
            float mw = Fonts.LEGION_INTER.getWidth(meta, 7.5f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.5f), meta,
                rightX + (rightW - mw) / 2.0f, cardsStart + 30.0f, toColor(softTextColor((int) (ca * 0.30f))));
        } else {
            long nowMs = System.currentTimeMillis();
            for (int i = 0; i < visible.size(); ++i) {
                HwEventsApi.HwEvent event = visible.get(i);
                float gy = cardsStart + (float) i * (HW_CARD_H + HW_CARD_GAP) - this.scrollOffset;
                if (gy + HW_CARD_H < cardsStart - HW_CARD_H || gy > panelBottom) {
                    continue;
                }
                this.renderHwCard(ctx, ca, mx, my, event, rightX, gy, rightW, nowMs, sameServer);
            }
        }
        Render2D.stopScissor(ctx);

        // полоса прокрутки списка карточек
        if (maxScroll > 0.5f && ca > 0) {
            float trackX = rightX + rightW - 2.5f;
            float trackH = Math.max(0.0f, panelBottom - cardsStart);
            if (trackH > 8.0f) {
                float thumbH = Math.max(10.0f, trackH * viewH / contentH);
                float thumbY = cardsStart + (trackH - thumbH) * (this.scrollOffset / maxScroll);
                rrect(ctx, trackX - 0.5f, thumbY, 1.5f, thumbH, 0.75f,
                    withAlpha(0xFFFFFF, (int) (ca * 0.22f)));
            }
        }
    }

    private void renderHwCard(class_332 ctx, int ca, int mx, int my, HwEventsApi.HwEvent event,
                              float cardX, float gy, float cardW, long nowMs, boolean sameServer) {
        boolean hovered = inside(mx, my, cardX, gy, cardW, HW_CARD_H);
        int rarityColor = hwRarityColor(event.displayRarity());

        int bgTintCard = mixChannel(0x0F0F0F, themeMain(), 0.08f);
        int cardBg = withAlpha(mixChannel(bgTintCard, 0x000000, 0.20f), (int) (ca * 0.86f * this.moduleFade));
        rrect(ctx, cardX, gy, cardW, HW_CARD_H, 10.0f, cardBg);
        Render2D.drawBorder(ctx.method_51448(), cardX, gy, cardW, HW_CARD_H, 10.0f, 1.0f, 1.2f,
            toColor(withAlpha(rarityColor, (int) (ca * (hovered ? 0.95f : 0.65f) * this.moduleFade))));

        // бейдж редкости справа
        String rarityLabel = event.displayRarity();
        float badgeW = Fonts.LEGION_INTER.getWidth(rarityLabel, 7.0f) + 12.0f;
        float badgeH = 14.0f;
        float badgeX = cardX + cardW - 8.0f - badgeW;
        float badgeY = gy + (HW_CARD_H - badgeH) / 2.0f;
        rrect(ctx, badgeX, badgeY, badgeW, badgeH, badgeH / 2.0f,
            withAlpha(rarityColor, (int) (ca * 0.25f * this.moduleFade)));
        float rw = Fonts.LEGION_INTER.getWidth(rarityLabel, 7.0f);
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), rarityLabel,
            badgeX + (badgeW - rw) / 2.0f, vCenter(badgeY, badgeH, 7.0f),
            toColor(withAlpha(rarityColor, (int) (ca * 0.9f * this.moduleFade))));

        long leftMs = event.voteEndsAt() - nowMs;
        boolean votingNow = event.voting() || (event.voteEndsAt() > 0L && leftMs <= 0L);
        boolean countdownNow = event.voteEndsAt() > 0L && leftMs > 0L;
        float textX = cardX + 10.0f;
        float textMaxW = badgeX - textX - 6.0f;
        String mainText = event.displayName();
        int mainColor = -1;
        if (votingNow) {
            mainText = "/vote";
            mainColor = -29696;
        } else if (countdownNow) {
            mainText = "Голосование через " + hwCountdown(leftMs);
            mainColor = -18611;
        }
        // Если все ивенты на одном сервере, имя сервера на каждой карточке
        // не несёт информации — показываем его только при разных серверах.
        String serverLabel = this.hwServerLabel(event.server());
        String secondText;
        if (votingNow || countdownNow) {
            secondText = sameServer ? event.displayName()
                : (serverLabel.isEmpty() ? event.displayName() : serverLabel + " · " + event.displayName());
        } else {
            secondText = sameServer ? null : serverLabel;
        }
        // заголовок и вторая строка — единый блок, центрированный по карточке
        float hTitle = Fonts.LEGION_INTER.getHeight(8.0f);
        float hSecond = Fonts.LEGION_INTER.getHeight(7.0f);
        boolean hasSecond = secondText != null && !secondText.isEmpty();
        float blockH = hasSecond ? hTitle + 4.0f + hSecond : hTitle;
        float titleY = gy + (HW_CARD_H - blockH) / 2.0f;
        float secondY = titleY + hTitle + 4.0f;

        this.clip(ctx, textX, gy + 5.0f, textMaxW, HW_CARD_H - 10.0f);
        if (secondText != null && !secondText.isEmpty()) {
            String second = secondText;
            while (Fonts.LEGION_INTER.getWidth(second + "..", 7.0f) > textMaxW && second.length() > 1) {
                second = second.substring(0, second.length() - 1);
            }
            if (Fonts.LEGION_INTER.getWidth(second, 7.0f) > textMaxW) {
                second = second + "..";
            }
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), second,
                textX, secondY, toColor(softTextColor((int) (ca * 0.55f * this.moduleFade))));
        }
        String main = mainText;
        while (Fonts.LEGION_INTER.getWidth(main + "..", 8.0f) > textMaxW && main.length() > 1) {
            main = main.substring(0, main.length() - 1);
        }
        if (Fonts.LEGION_INTER.getWidth(main, 8.0f) > textMaxW) {
            main = main + "..";
        }
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), main,
            textX, titleY, toColor(withAlpha(mainColor, (int) (ca * 0.95f * this.moduleFade))));
        Render2D.stopScissor(ctx);
    }

    private boolean clickHwControls(float mx, float my, int button) {
        if (button != 0) {
            return false;
        }
        // кнопка обновления
        if (inside(mx, my, this.hwRefreshX, this.hwRefreshY, 26.0f, 26.0f)) {
            this.hwFetchTime = 0L;
            this.hwServersFetchTime = 0L;
            this.refreshHwEvents();
            return true;
        }
        // чип редкости
        for (int i = 0; i < this.hwRarityChipRects.size(); ++i) {
            float[] r = this.hwRarityChipRects.get(i);
            if (inside(mx, my, r[0], r[1], r[2], HW_CHIP_H)) {
                this.hwRarityFilter = i == 0 ? "" : this.hwRarityChipLabels.get(i);
                this.scrollTarget = 0.0f;
                this.scrollOffset = 0.0f;
                return true;
            }
        }
        // чип ивента
        for (int i = 0; i < this.hwEventChipRects.size(); ++i) {
            float[] r = this.hwEventChipRects.get(i);
            if (inside(mx, my, r[0], r[1], r[2], HW_CHIP_H)) {
                this.hwEventFilter = i == 0 ? "" : this.hwEventChipLabels.get(i);
                this.scrollTarget = 0.0f;
                this.scrollOffset = 0.0f;
                return true;
            }
        }
        return false;
    }

    // ==================== панель конфигов ====================

    private ConfigManager configManager() {
        ClarityVisuals client = ClarityVisuals.getInstance();
        return client == null ? null : client.getConfigManager();
    }

    private List<String> configNames() {
        if (this.configNamesDirty) {
            ConfigManager m = this.configManager();
            this.configNamesCache = m == null ? List.of() : m.dW();
            this.configNamesDirty = false;
        }
        return this.configNamesCache;
    }

    /** Высота списка конфигов без учёта текущего скролла. */
    private float configListHeight() {
        return this.configNames().size() * (CONFIG_ROW_H + 3.0f);
    }

    /** Высота области списка внутри панели. */
    private float configListAreaH() {
        return CONFIG_PANEL_H - 92.0f;
    }

    private float[] configPanelRect() {
        float[] btn = this.configButtonRect();
        float px = clamp(this.x + LEFT_W + 6.0f, 4.0f, Math.max(4.0f, this.designW() - CONFIG_PANEL_W - 4.0f));
        float py = clamp(btn[1] + btn[3] - CONFIG_PANEL_H, 4.0f, Math.max(4.0f, this.designH() - CONFIG_PANEL_H - 4.0f));
        return new float[]{px, py, CONFIG_PANEL_W, CONFIG_PANEL_H};
    }

    private void renderConfigPanel(class_332 ctx, int ca, double mx, double my) {
        if (!this.configPanelOpen && this.configPanelAnim <= 0.0001f) {
            this.configPanelAnim = 0.0f;
            return;
        }
        this.configPanelAnim = approach(this.configPanelAnim, this.configPanelOpen ? 1.0f : 0.0f, 0.14f);
        if (this.configPanelAnim <= 0.0001f) {
            return;
        }
        float[] pos = this.configPanelRect();
        float px = pos[0];
        float py = pos[1];
        float pw = pos[2];
        float fullH = pos[3];
        float ph = fullH * this.configPanelAnim;
        int alpha = (int) (ca * this.configPanelAnim);

        int bgTint = mixChannel(0x0F0F0F, themeMain(), 0.08f);
        rblur(ctx, px, py, pw, ph, 10.0f, withAlpha(0x000000, (int) (alpha * 0.35f)));
        rrect(ctx, px, py, pw, ph, 10.0f, withAlpha(bgTint, (int) (alpha * 0.94f)));

        ConfigManager m = this.configManager();
        String current = m == null ? "" : m.dQ();

        // заголовок
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(8.0f), "Конфиги",
            px + 9.0f, py + 7.0f, toColor(softTextColor((int) (alpha * 0.9f))));
        String activeLine = "Активный: " + current;
        float activeW = Fonts.LEGION_INTER.getWidth(activeLine, 7.0f);
        if (activeW > pw - 18.0f) {
            while (activeLine.length() > 1 && Fonts.LEGION_INTER.getWidth(activeLine + "..", 7.0f) > pw - 18.0f) {
                activeLine = activeLine.substring(0, activeLine.length() - 1);
            }
            activeLine = activeLine + "..";
        }
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), activeLine,
            px + 9.0f, py + 20.0f, toColor(softTextColor((int) (alpha * 0.4f))));

        // поле ввода имени + кнопка «Сохранить»
        float fieldX = px + 8.0f;
        float fieldY = py + 34.0f;
        float fieldH = 18.0f;
        float fieldW = pw - 16.0f - 56.0f;
        this.configFieldRect = new float[]{fieldX, fieldY, fieldW, fieldH};
        rrect(ctx, fieldX, fieldY, fieldW, fieldH, 6.0f,
            withAlpha(0x0A0A0A, (int) (alpha * 0.55f)));
        Render2D.drawBorder(ctx.method_51448(), fieldX, fieldY, fieldW, fieldH, 6.0f, 1.0f, 0.8f,
            toColor(withAlpha(themeMain(), (int) (alpha * (this.configNameFocused ? 0.65f : 0.22f)))));
        String buffer = this.configNameInput.toString();
        if (buffer.isEmpty() && !this.configNameFocused) {
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), "Название...",
                fieldX + 6.0f, vCenter(fieldY, fieldH, 7.0f), toColor(softTextColor((int) (alpha * 0.28f))));
        } else {
            String shown = buffer;
            String cursor = this.configNameFocused && System.currentTimeMillis() / 500L % 2L == 0L ? "|" : "";
            while (Fonts.LEGION_INTER.getWidth(shown + cursor, 7.0f) > fieldW - 10.0f && shown.length() > 1) {
                shown = shown.substring(1);
            }
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), shown + cursor,
                fieldX + 6.0f, vCenter(fieldY, fieldH, 7.0f), toColor(softTextColor((int) (alpha * 0.85f))));
        }

        boolean canSave = m != null && !buffer.trim().isEmpty();
        float saveX = px + pw - 8.0f - 50.0f;
        float saveY = fieldY;
        float saveW = 50.0f;
        this.configSaveRect = new float[]{saveX, saveY, saveW, fieldH};
        float saveHover = inside(mx, my, saveX, saveY, saveW, fieldH) && canSave ? 1.0f : 0.0f;
        rrect(ctx, saveX + 0.5f, saveY + 0.5f, saveW - 1.0f, fieldH - 1.0f, 6.0f,
            withAlpha(themeMain(), (int) (alpha * (canSave ? 0.55f + 0.25f * saveHover : 0.16f))));
        String saveText = "Сохранить";
        float saveTW = Fonts.LEGION_INTER.getWidth(saveText, 7.0f);
        Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), saveText,
            saveX + (saveW - saveTW) / 2.0f, vCenter(saveY, fieldH, 7.0f),
            toColor(canSave ? softTextColor((int) (alpha * 0.95f)) : softTextColor((int) (alpha * 0.30f))));

        // список конфигов
        float listTop = fieldY + fieldH + 6.0f;
        float listH = Math.max(0.0f, py + ph - listTop - 4.0f);
        List<String> names = this.configNames();
        float maxCfgScroll = Math.max(0.0f, this.configListHeight() - listH);
        this.configScrollTarget = clamp(this.configScrollTarget, 0.0f, maxCfgScroll);
        this.configScroll = approach(this.configScroll, this.configScrollTarget, 0.18f);
        this.configScroll = clamp(this.configScroll, 0.0f, maxCfgScroll);

        float rowX = px + 5.0f;
        float rowW = pw - 10.0f;
        this.clip(ctx, px + 1.0f, listTop, pw - 2.0f, listH);
        if (names.isEmpty()) {
            String empty = "Конфигов пока нет";
            float ew = Fonts.LEGION_INTER.getWidth(empty, 7.0f);
            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), empty,
                px + (pw - ew) / 2.0f, listTop + 12.0f, toColor(softTextColor((int) (alpha * 0.3f))));
        } else {
            for (int i = 0; i < names.size(); ++i) {
                String name = names.get(i);
                float ry = listTop + i * (CONFIG_ROW_H + 3.0f) - this.configScroll;
                if (ry + CONFIG_ROW_H < listTop || ry > listTop + listH) {
                    continue;
                }
                boolean isActive = name.equalsIgnoreCase(current);
                boolean hov = inside(mx, my, rowX, ry, rowW, CONFIG_ROW_H);
                rrect(ctx, rowX, ry, rowW, CONFIG_ROW_H, 6.0f,
                    withAlpha(isActive ? themeMain() : 0xFFFFFF,
                        (int) (alpha * (isActive ? 0.14f : (hov ? 0.07f : 0.03f)))));
                // имя конфига
                float nameMaxW = rowW - (isActive ? 18.0f : 42.0f);
                String shown = name;
                while (Fonts.LEGION_INTER.getWidth(shown + "..", 7.0f) > nameMaxW && shown.length() > 1) {
                    shown = shown.substring(0, shown.length() - 1);
                }
                if (Fonts.LEGION_INTER.getWidth(shown, 7.0f) > nameMaxW) {
                    shown = shown + "..";
                }
                Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), shown,
                    rowX + 8.0f, vCenter(ry, CONFIG_ROW_H, 7.0f),
                    toColor(softTextColor((int) (alpha * (isActive ? 0.95f : (hov ? 0.8f : 0.6f))))));
                // автор (если есть место и не активный)
                if (!isActive && m != null) {
                    String author = m.bj(name);
                    if (author != null && !author.isBlank()) {
                        String authorLine = "by " + author;
                        float aw = Fonts.LEGION_INTER.getWidth(authorLine, 7.0f);
                        if (rowX + 8.0f + Fonts.LEGION_INTER.getWidth(shown, 7.0f) + 8.0f + aw < rowX + rowW - 20.0f) {
                            Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), authorLine,
                                rowX + rowW - aw - 19.0f, vCenter(ry, CONFIG_ROW_H, 7.0f),
                                toColor(softTextColor((int) (alpha * 0.3f))));
                        }
                    }
                }
                // корзина для удаления (не для «default» и не для активного)
                boolean deletable = !isActive && !"default".equalsIgnoreCase(name);
                if (deletable) {
                    float trashX = rowX + rowW - 14.0f;
                    float trashY = ry + (CONFIG_ROW_H - 11.0f) / 2.0f;
                    boolean trashHov = inside(mx, my, trashX - 2.0f, trashY, 15.0f, 14.0f);
                    int trashColor = softTextColor((int) (alpha * (trashHov ? 0.85f : 0.4f)));
                    ClickGuiIcons.trash(ctx.method_51448(), trashX + 5.0f, ry + CONFIG_ROW_H / 2.0f, 9.0f, toColor(trashColor));
                }
                if (isActive) {
                    Render2D.drawFont(ctx.method_51448(), Fonts.LEGION_INTER.getFont(7.0f), "активен",
                        rowX + rowW - Fonts.LEGION_INTER.getWidth("активен", 7.0f) - 8.0f,
                        vCenter(ry, CONFIG_ROW_H, 7.0f), toColor(withAlpha(themeMain(), (int) (alpha * 0.95f))));
                }
            }
        }
        Render2D.stopScissor(ctx);

        // полоса прокрутки списка
        if (maxCfgScroll > 0.5f && alpha > 0) {
            float trackX = px + pw - 3.5f;
            float trackH = listH;
            if (trackH > 8.0f) {
                float thumbH = Math.max(10.0f, trackH * listH / (listH + maxCfgScroll));
                float thumbY = listTop + (trackH - thumbH) * (this.configScroll / maxCfgScroll);
                rrect(ctx, trackX, thumbY, 1.5f, thumbH, 0.75f,
                    withAlpha(0xFFFFFF, (int) (alpha * 0.22f)));
            }
        }
    }

    private boolean clickConfigButton(float mx, float my) {
        float[] btn = this.configButtonRect();
        if (inside(mx, my, btn[0], btn[1], btn[2], btn[3])) {
            this.configPanelOpen = !this.configPanelOpen;
            this.configNamesDirty = true;
            if (this.configPanelOpen) {
                this.themeMenuOpen = false;
                this.editingSelect = null;
                this.closingSelect = null;
            }
            this.searchFocused = false;
            return true;
        }
        return false;
    }

    private boolean clickConfigPanel(float mx, float my, int button) {
        float[] pos = this.configPanelRect();
        if (!inside(mx, my, pos[0], pos[1], pos[2], pos[3])) {
            this.configPanelOpen = false;
            this.configNameFocused = false;
            return true;
        }
        if (button != 0) {
            return true;
        }
        // поле ввода
        float[] f = this.configFieldRect;
        if (inside(mx, my, f[0], f[1], f[2], f[3])) {
            this.configNameFocused = true;
            return true;
        }
        this.configNameFocused = false;
        // кнопка сохранения
        float[] s = this.configSaveRect;
        if (inside(mx, my, s[0], s[1], s[2], s[3])) {
            this.saveNewConfig();
            return true;
        }
        // строки списка: загрузка кликом по имени, удаление по корзине
        ConfigManager m = this.configManager();
        if (m == null) {
            return true;
        }
        List<String> names = this.configNames();
        String current = m.dQ();
        float listTop = f[1] + f[3] + 6.0f;
        float listH = Math.max(0.0f, pos[1] + pos[3] - listTop - 4.0f);
        float rowX = pos[0] + 5.0f;
        float rowW = pos[2] - 10.0f;
        for (int i = 0; i < names.size(); ++i) {
            String name = names.get(i);
            float ry = listTop + i * (CONFIG_ROW_H + 3.0f) - this.configScroll;
            if (ry + CONFIG_ROW_H < listTop || ry > listTop + listH) {
                continue;
            }
            if (!inside(mx, my, rowX, ry, rowW, CONFIG_ROW_H)) {
                continue;
            }
            boolean isActive = name.equalsIgnoreCase(current);
            float trashX = rowX + rowW - 14.0f;
            if (!isActive && !"default".equalsIgnoreCase(name)
                && inside(mx, my, trashX - 2.0f, ry, 17.0f, CONFIG_ROW_H)) {
                if (m.bk(name)) {
                    this.configNamesDirty = true;
                }
                return true;
            }
            if (!isActive) {
                // Загрузка переключает модули (onDisable/onEnable трогают рендер),
                // поэтому строго на render-потоке — как это делает LinkGuiScreen.
                try {
                    m.bi(name);
                    this.configNamesDirty = true;
                } catch (Exception e) {
                    ClarityVisuals.LOGGER.error("[LegionGUI] Load config failed: " + name, e);
                }
                return true;
            }
            return true;
        }
        return true;
    }

    private void saveNewConfig() {
        ConfigManager m = this.configManager();
        if (m == null) {
            return;
        }
        String raw = this.configNameInput.toString().trim();
        if (raw.isEmpty()) {
            return;
        }
        String name = m.bl(raw);
        if (name.isBlank()) {
            return;
        }
        try {
            m.bh(name);
            this.configNameInput.setLength(0);
            this.configNameFocused = false;
            this.configNamesDirty = true;
        } catch (Exception e) {
            ClarityVisuals.LOGGER.error("[LegionGUI] Save config failed: " + name, e);
        }
    }

    public void open() {
        if (!UI.isInPlayWorld() || aiG == null) {
            return;
        }
        this.prepareForOpen();
        if (LegionClickGui.aiG.field_1755 == this) {
            return;
        }
        aiG.method_1507(this);
    }

    public void beginClose() {
        if (aiG == null || LegionClickGui.aiG.field_1755 != this) {
            this.cancelClose();
            return;
        }
        this.method_25419();
    }

    private void prepareForOpen() {
        this.cancelClose();
        this.openAnimation.w(OPEN_ANIMATION_DURATION);
        this.openAnimation.ae(true);
        this.searchInput.setLength(0);
        this.searchFocused = false;
        this.editingBind = null;
        this.bindModuleName = null;
        this.bindSettingName = null;
        this.editingString = null;
        this.stringModuleName = null;
        this.stringTextInput = null;
        this.colorPicker.closeNow();
        this.themeMenuOpen = false;
        this.themeMenuAnim = 0.0f;
        if (aiG != null && aiG.method_22683() != null) {
            // Те же единицы, что и в рендере, иначе позиция уедет.
            this.fixedW = this.field_22789;
            this.fixedH = this.field_22790;
        }
        this.updateUiScale();
        // Каждое открытие — по центру экрана.
        this.recenter();
        this.loggedFirstFrame = false;
    }

    private void cancelClose() {
        this.closing = false;
        this.openAnimation.ae(true);
    }
}
