package ru.method.clarityvisuals.modules.impl.render.cosmetic;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_3489;
import net.minecraft.class_9280;
import net.minecraft.class_9334;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.CustomSwordModel;

/**
 * Кастомный меч как косметика: единственная выбранная модель накладывается
 * на все мечи (деревянный, каменный, железный, золотой, алмазный, незеритовый)
 * без настроек. Выбор — вкладка «Меч» в гардеробе (CosmeticsScreen), хранится
 * в косметическом лукауте (слот CosmeticSlot.dw).
 */
public final class CosmeticSwords {
    public static final CosmeticSwords INSTANCE = new CosmeticSwords();

    /** id (в нижнем регистре: display-name и имя константы) -> модель. */
    private static final Map<String, CustomSwordModel> BY_ID = new HashMap<String, CustomSwordModel>();

    static {
        for (CustomSwordModel model : CustomSwordModel.values()) {
            BY_ID.put(model.getName().toLowerCase(Locale.ROOT), model);
            BY_ID.put(model.name().toLowerCase(Locale.ROOT), model);
        }
    }

    private CosmeticSwords() {
    }

    /**
     * Флаг «идёт рендер превью в гардеробе»: превью-стеки уже несут свой компонент
     * модели, подмену на выбранный меч надо отключить, иначе все карточки показывают
     * один и тот же (выбранный) меч. Ставится на время drawItem в CosmeticsScreen.
     */
    public static volatile boolean PREVIEW_RENDER = false;

    /**
     * Invoked from ItemModelResolverMixin for every item render state update.
     */
    public class_1799 getRenderStack(class_1799 class_17992) {
        if (PREVIEW_RENDER || class_17992 == null || class_17992.method_7960()) {
            return class_17992;
        }
        if (!CosmeticSwords.isSword(class_17992)) {
            return class_17992;
        }
        CustomSwordModel customSwordModel = CosmeticSwords.selected();
        if (customSwordModel == null || customSwordModel == CustomSwordModel.NONE) {
            return class_17992;
        }
        return CosmeticSwords.apply(class_17992, customSwordModel);
    }

    /** Модель, выбранная в гардеробе; null/NONE — косметика меча выключена. */
    public static CustomSwordModel selected() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        CosmeticsService cosmeticsService = clarityVisuals == null ? null : clarityVisuals.getCosmeticsService();
        if (cosmeticsService == null || !cosmeticsService.isEnabled()) {
            return null;
        }
        return CosmeticSwords.resolve(cosmeticsService.aL().f(CosmeticSlot.dw));
    }

    /** id хранится в нижнем регистре (CosmeticLoadout.normalize), сверяем без учёта регистра. */
    public static CustomSwordModel resolve(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        if (string2.isEmpty() || "none".equals(string2)) {
            return null;
        }
        return BY_ID.get(string2);
    }

    /** Копия стека с CUSTOM_MODEL_DATA выбранной модели — все мечи получают одну модель. */
    public static class_1799 apply(class_1799 class_17992, CustomSwordModel customSwordModel) {
        if (customSwordModel == null || customSwordModel == CustomSwordModel.NONE || class_17992 == null || class_17992.method_7960()) {
            return class_17992;
        }
        class_1799 class_17993 = class_17992.method_7972();
        class_17993.method_57379(class_9334.field_49637, new class_9280(List.of(), List.of(), List.of(customSwordModel.getName()), List.of()));
        return class_17993;
    }

    /** Кэш стеков для предпоказа в гардеробе: алмазный меч с компонентом модели. */
    private static final Map<CustomSwordModel, class_1799> PREVIEW_CACHE = new EnumMap<CustomSwordModel, class_1799>(CustomSwordModel.class);

    /** Стек для предпоказа в гардеробе: алмазный меч с компонентом модели (кэшируется). */
    public static class_1799 previewStack(CustomSwordModel customSwordModel) {
        return PREVIEW_CACHE.computeIfAbsent(customSwordModel, model -> {
            class_1799 class_17992 = new class_1799(class_1802.field_8802);
            class_17992.method_57379(class_9334.field_49637, new class_9280(List.of(), List.of(), List.of(model.getName()), List.of()));
            return class_17992;
        });
    }

    private static boolean isSword(class_1799 class_17992) {
        return class_17992.method_31573(class_3489.field_42611)
            || class_17992.method_31574(class_1802.field_22022)
            || class_17992.method_31574(class_1802.field_8802)
            || class_17992.method_31574(class_1802.field_8371)
            || class_17992.method_31574(class_1802.field_8845)
            || class_17992.method_31574(class_1802.field_8528)
            || class_17992.method_31574(class_1802.field_8091);
    }
}
