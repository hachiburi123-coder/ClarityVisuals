package ru.method.clarityvisuals.manager;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.orbit.EventHandler;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.render.NotificationRenderer;

public class NotificationManager
implements MinecraftClientAccess {
    private static final float nn = 58.0f;
    private static final float no = 17.0f;
    private final List<NotificationRenderer> np = new ArrayList<NotificationRenderer>();

    public NotificationManager() {
        ClarityVisuals.getInstance().getEventHandler().subscribe((Object)this);
    }

    public void a(NotificationRenderer notificationRenderer) {
        this.np.add(notificationRenderer);
    }

    @EventHandler
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (Module.fullNullCheck() || ClarityVisuals.getInstance().isPanic()) {
            return;
        }
        if (this.np.isEmpty()) {
            return;
        }
        if (this.np.size() > 10) {
            this.np.removeFirst();
        }
        this.np.removeIf(NotificationRenderer::sc);
        ArrayList arrayList = Lists.newArrayList(this.np);
        float f = (float)aiG.method_22683().method_4502() - 58.0f;
        for (int i = 0; i < arrayList.size(); ++i) {
            int n = arrayList.size() - 1 - i;
            ((NotificationRenderer)arrayList.get(i)).a(hudRenderEvent, f - (float)n * 17.0f);
        }
    }
}

