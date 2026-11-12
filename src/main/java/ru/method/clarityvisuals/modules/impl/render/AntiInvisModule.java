package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;

/**
 * Анти-инвиз: подсветка невидимых игроков ванильными средствами.
 * Пустой тумблер без настроек, вся работа делается миксинами:
 * <ul>
 *   <li>{@code EntityRenderDispatcherMixin} — ванильная тень над инвизками
 *       (@ModifyExpressionValue на чтение {@code EntityRenderState.invisible});</li>
 *   <li>{@code LivingEntityRendererMixin#antiInvisShowLabel} — форс ванильного
 *       hasLabel (method_4055) для невидимых игроков: ванильный конвейер сам
 *       рисует ник над головой с обычным depth-тестом (сквозь стены НЕ виден);</li>
 *   <li>{@code LivingEntityRendererMixin#antiInvisUnsneakLabel} — трюк из
 *       SelfNametags: ваниль не рисует ТЕКСТ неймтега у приседающих
 *       ({@code EntityRenderState.sneaking}), поэтому для инвизок флаг снику
 *       сбрасывается на render state — ник виден и в приседе.</li>
 * </ul>
 */
public class AntiInvisModule
extends Module {
    public AntiInvisModule() {
        super("AntiInvis", ModuleCategory.ql);
    }
}
