package ru.method.clarityvisuals.render;

import java.util.Set;
import net.minecraft.class_10151;
import net.minecraft.class_276;
import net.minecraft.class_279;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_9909;
import net.minecraft.class_9922;
import net.minecraft.class_9925;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.SaturationModule;

public class SaturationRenderer {
    private static final class_2960 EFFECT_ID = class_2960.method_60655((String)"clarityvisuals", (String)"saturation");
    private static final Set<class_2960> EXTERNAL_TARGETS = Set.of(class_279.field_53104);
    private static class_279 processor;
    private static boolean logFailure;

    public static void apply(class_9922 allocator) {
        SaturationModule mod = null;
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals != null && clarityVisuals.getModuleManager() != null) {
            mod = clarityVisuals.getModuleManager().getModule(SaturationModule.class);
        }
        if (mod == null || !mod.isToggled()) {
            return;
        }
        float saturation = mod.getStrength();
        if (saturation == 1.0f) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
        }
        class_276 mainBuffer = mc.method_1522();
        if (mainBuffer == null) {
            return;
        }
        int width = mainBuffer.field_1482;
        int height = mainBuffer.field_1481;
        if (width <= 0 || height <= 0) {
            return;
        }
        try {
            if (processor == null) {
                class_10151 shaderLoader = mc.method_62887();
                if (shaderLoader == null) {
                    return;
                }
                processor = shaderLoader.method_62941(EFFECT_ID, EXTERNAL_TARGETS);
            }
            if (processor == null) {
                return;
            }
            processor.method_57799("Saturation", saturation);
            class_9909 builder = new class_9909();
            class_9925<class_276> mainHandle = builder.method_61914("main_frame", mainBuffer);
            SaturationRenderer.MapFramebufferSet fbSet = new SaturationRenderer.MapFramebufferSet();
            fbSet.put(class_279.field_53104, mainHandle);
            processor.method_62234(builder, width, height, (class_279.class_9961)fbSet);
            builder.method_61909(allocator);
        }
        catch (Exception e) {
            if (logFailure) {
                e.printStackTrace();
                logFailure = false;
            }
        }
    }

    public static void reset() {
        processor = null;
        logFailure = true;
    }

    static {
        logFailure = true;
    }

    static final class MapFramebufferSet
    implements class_279.class_9961 {
        private final java.util.Map<class_2960, class_9925<class_276>> map = new java.util.HashMap<class_2960, class_9925<class_276>>();

        public void put(class_2960 id, class_9925<class_276> handle) {
            this.method_62225(id, handle);
        }

        @Override
        public void method_62225(class_2960 id, class_9925<class_276> handle) {
            this.map.put(id, handle);
        }

        @Override
        public class_9925<class_276> method_62224(class_2960 id) {
            return this.map.get(id);
        }
    }
}
