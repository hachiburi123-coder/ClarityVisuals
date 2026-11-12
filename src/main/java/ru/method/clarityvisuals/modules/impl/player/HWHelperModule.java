package ru.method.clarityvisuals.modules.impl.player;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2675;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.api.event.PacketEvent;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render3D;

public class HWHelperModule
extends Module {
    private static final double EXPLOSIVE_WIDTH = 6.0;
    private static final double EXPLOSIVE_HEIGHT = 4.0;
    private static final long PARTICLE_WINDOW_MS = 2800L;
    private static final long STUN_SAMPLE_WINDOW_MS = 1700L;
    private static final long STUN_BURST_WINDOW_MS = 700L;
    private static final long STUN_DETECT_INTERVAL_MS = 120L;
    private static final long STUN_LIFETIME_MS = 4500L;
    /** Окно линейного затухания контура стана (как в старом клиенте: remaining/4000). */
    private static final long STUN_FADE_WINDOW_MS = 4000L;
    private static final float STUN_MIN_WEIGHT = 55.0f;
    private static final float STUN_MIN_BURST = 20.0f;
    private static final double STUN_MIN_SPAN = 12.0;
    private static final double STUN_MAX_SPAN = 56.0;
    private static final double STUN_MIN_HEIGHT = 6.0;
    private static final double STUN_MAX_HEIGHT = 90.0;
    private static final double STUN_CELL = 4.0;
    private static final double STUN_MERGE_PADDING = 4.0;
    private static final double STUN_ANCHOR_EXPAND = 3.0;
    private static final double STUN_SAMPLE_DISTANCE_SQ = 14400.0;
    private static final double STUN_RENDER_DISTANCE_SQ = 9216.0;
    private static final int STUN_HIGHLIGHT_LIMIT = 6;
    private static final int PARTICLE_POINT_LIMIT = 16000;
    private static final int PARTICLE_SAMPLES_PER_PACKET = 20;
    private static final int SNOW_MAX_TICKS = 80;
    private static final double SNOW_SPEED = 1.5;
    private static final double SNOW_GRAVITY = 0.03;
    private static final double SNOW_DRAG = 0.99;
    private static final double SNOW_EXPAND = 0.45;
    private static final long PREDICT_HOLD_MS = 130L;
    private static final Object STUN_LOCK = new Object();
    private static final double[] STUN_SAMPLE_GAUSS = new double[40];

    private final BooleanSetting trapPreview = new BooleanSetting("modules.settings.hwhelper.trap_preview", true);
    private final ColorSetting trapColorOutside = new ColorSetting("modules.settings.hwhelper.trap_outside", new Color(255, 0, 0, 255));
    private final ColorSetting trapColorInside = new ColorSetting("modules.settings.hwhelper.trap_inside", new Color(0, 255, 0, 255));
    private final ColorSetting explosiveColorOutside = new ColorSetting("modules.settings.hwhelper.explosive_outside", new Color(255, 0, 0, 255));
    private final ColorSetting explosiveColorInside = new ColorSetting("modules.settings.hwhelper.explosive_inside", new Color(0, 255, 0, 255));
    private final SliderSetting trapTransparency = new SliderSetting("modules.settings.hwhelper.trap_alpha", 0.3f, 0.0f, 1.0f, 0.01f);
    private final SliderSetting trapLineWidth = new SliderSetting("modules.settings.hwhelper.trap_width", 2.0f, 0.5f, 5.0f, 0.1f);
    private final BooleanSetting snowballPreview = new BooleanSetting("modules.settings.hwhelper.snowball", true);
    private final BooleanSetting chamsMode = new BooleanSetting("modules.settings.hwhelper.chams", false);
    private final ColorSetting snowballColor = new ColorSetting("modules.settings.hwhelper.snowball_color", new Color(0, 255, 0, 255));
    private final ColorSetting snowballOutline = new ColorSetting("modules.settings.hwhelper.snowball_outline", new Color(0, 180, 0, 255));
    private final SliderSetting snowballTransparency = new SliderSetting("modules.settings.hwhelper.snowball_alpha", 0.3f, 0.0f, 1.0f, 0.01f, this.snowballPreview::jT);
    private final SliderSetting snowballLineWidth = new SliderSetting("modules.settings.hwhelper.snowball_width", 2.0f, 0.5f, 5.0f, 0.1f, this.snowballPreview::jT);
    private final SliderSetting snowballDuration = new SliderSetting("modules.settings.hwhelper.snowball_duration", 2000.0f, 250.0f, 10000.0f, 50.0f, () -> Boolean.FALSE);
    private final BooleanSetting stunHighlight = new BooleanSetting("modules.settings.hwhelper.stun", false);
    private final SliderSetting stunWidth = new SliderSetting("modules.settings.hwhelper.stun_width", 4.0f, 1.0f, 60.0f, 0.5f);

    private final List<ParticlePoint> particleWindow = new ArrayList<ParticlePoint>();
    private final List<StunHighlight> stunHighlights = new ArrayList<StunHighlight>();
    private final Map<Integer, Long> predictedTargets = new HashMap<Integer, Long>();
    private long lastStunDetect;

    static {
        Random random = new Random(9175641L);
        for (int i = 0; i < STUN_SAMPLE_GAUSS.length; ++i) {
            double gaussian = random.nextGaussian();
            STUN_SAMPLE_GAUSS[i] = Math.max(-2.5, Math.min(2.5, gaussian));
        }
    }

    public HWHelperModule() {
        super("HWHelper", ModuleCategory.qm);
        this.getSettings().add(this.trapPreview);
        this.getSettings().add(this.trapColorOutside);
        this.getSettings().add(this.trapColorInside);
        this.getSettings().add(this.explosiveColorOutside);
        this.getSettings().add(this.explosiveColorInside);
        this.getSettings().add(this.trapTransparency);
        this.getSettings().add(this.trapLineWidth);
        this.getSettings().add(this.snowballPreview);
        this.getSettings().add(this.chamsMode);
        this.getSettings().add(this.snowballColor);
        this.getSettings().add(this.snowballOutline);
        this.getSettings().add(this.snowballTransparency);
        this.getSettings().add(this.snowballLineWidth);
        this.getSettings().add(this.snowballDuration);
        this.getSettings().add(this.stunHighlight);
        this.getSettings().add(this.stunWidth);
    }

    @Override
    public void onDisable() {
        synchronized (STUN_LOCK) {
            this.particleWindow.clear();
            this.stunHighlights.clear();
        }
        this.predictedTargets.clear();
        super.onDisable();
    }

    private boolean isHolding(class_1792 item) {
        return HWHelperModule.aiG.field_1724 != null && (HWHelperModule.aiG.field_1724.method_6047().method_31574(item) || HWHelperModule.aiG.field_1724.method_6079().method_31574(item));
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive receive) {
        if (HWHelperModule.fullNullCheck()) {
            return;
        }
        class_2596<?> packet = receive.i();
        if (packet instanceof class_2675 && ((Boolean)this.stunHighlight.jT()).booleanValue()) {
            this.onParticle((class_2675)packet);
        }
    }

    private void onParticle(class_2675 packet) {
        class_2394 parameters = packet.method_11551();
        if (parameters == null) {
            return;
        }
        Object type = parameters.method_10295();
        if (type == null) {
            return;
        }
        long now = System.currentTimeMillis();
        double x = packet.method_11544();
        double y = packet.method_11547();
        double z = packet.method_11546();
        double offsetX = packet.method_11548();
        double offsetY = packet.method_11549();
        double offsetZ = packet.method_11550();
        int count = Math.max(1, packet.method_11545());
        int samples = Math.min(20, count);
        float weight = samples > 1 ? (float)count / (float)samples : Math.max(1.0f, (float)count);
        synchronized (STUN_LOCK) {
            this.pruneParticleWindow(now);
            if (samples <= 1) {
                this.particleWindow.add(new ParticlePoint(x, y, z, now, weight, type));
            } else {
                for (int i = 0; i < samples; ++i) {
                    this.particleWindow.add(new ParticlePoint(
                        x + offsetX * STUN_SAMPLE_GAUSS[i % STUN_SAMPLE_GAUSS.length],
                        y + offsetY * STUN_SAMPLE_GAUSS[(i * 7 + 3) % STUN_SAMPLE_GAUSS.length],
                        z + offsetZ * STUN_SAMPLE_GAUSS[(i * 13 + 11) % STUN_SAMPLE_GAUSS.length],
                        now, weight, type));
                }
            }
        }
    }

    private void pruneParticleWindow(long now) {
        if (this.particleWindow.isEmpty()) {
            return;
        }
        this.particleWindow.removeIf(point -> now - point.time() > PARTICLE_WINDOW_MS);
        int overflow = this.particleWindow.size() - PARTICLE_POINT_LIMIT;
        if (overflow > 0) {
            this.particleWindow.subList(0, Math.min(overflow, this.particleWindow.size())).clear();
        }
    }

    /**
     * Same-type spatial clustering with burst gates: the real stun cloud is a
     * large, dense, short burst surrounding a player. Ambient spawn particles
     * are sparse, tiny and continuous, so they never pass these gates.
     */
    private void detectStunClusters(long now) {
        synchronized (STUN_LOCK) {
            if (now - this.lastStunDetect < STUN_DETECT_INTERVAL_MS) {
                return;
            }
            this.lastStunDetect = now;
            this.pruneParticleWindow(now);
            if (this.particleWindow.isEmpty()) {
                return;
            }
            class_1657 self = HWHelperModule.aiG.field_1724;
            long cutoff = now - STUN_SAMPLE_WINDOW_MS;
            HashMap<Object, List<ParticlePoint>> byType = new HashMap<Object, List<ParticlePoint>>();
            for (ParticlePoint point : this.particleWindow) {
                if (point.time() < cutoff) {
                    continue;
                }
                if (self != null) {
                    double dx = point.x() - self.method_23317();
                    double dy = point.y() - self.method_23318();
                    double dz = point.z() - self.method_23321();
                    if (dx * dx + dy * dy + dz * dz > STUN_SAMPLE_DISTANCE_SQ) {
                        continue;
                    }
                }
                List<ParticlePoint> bucket = byType.get(point.type());
                if (bucket == null) {
                    bucket = new ArrayList<ParticlePoint>();
                    byType.put(point.type(), bucket);
                }
                bucket.add(point);
            }
            for (List<ParticlePoint> bucket : byType.values()) {
                this.clusterTypeBucket(bucket, now);
            }
        }
    }

    private void clusterTypeBucket(List<ParticlePoint> bucket, long now) {
        if (bucket.size() < 12) {
            return;
        }
        HashMap<Long, List<ParticlePoint>> grid = new HashMap<Long, List<ParticlePoint>>();
        for (ParticlePoint point : bucket) {
            long key = cellKey(floorCell(point.x()), floorCell(point.y()), floorCell(point.z()));
            List<ParticlePoint> cell = grid.get(key);
            if (cell == null) {
                cell = new ArrayList<ParticlePoint>();
                grid.put(key, cell);
            }
            cell.add(point);
        }
        HashSet<Long> visited = new HashSet<Long>();
        for (Long key : grid.keySet()) {
            if (visited.contains(key)) {
                continue;
            }
            List<ParticlePoint> cluster = new ArrayList<ParticlePoint>();
            Deque<long[]> queue = new ArrayDeque<long[]>();
            visited.add(key);
            queue.add(decodeCell(key));
            while (!queue.isEmpty()) {
                long[] coord = queue.poll();
                List<ParticlePoint> cell = grid.get(encodeCell(coord[0], coord[1], coord[2]));
                if (cell != null) {
                    cluster.addAll(cell);
                }
                for (long dx = -1L; dx <= 1L; ++dx) {
                    for (long dy = -1L; dy <= 1L; ++dy) {
                        for (long dz = -1L; dz <= 1L; ++dz) {
                            if (dx == 0L && dy == 0L && dz == 0L) {
                                continue;
                            }
                            long neighbour = encodeCell(coord[0] + dx, coord[1] + dy, coord[2] + dz);
                            if (visited.contains(neighbour) || !grid.containsKey(neighbour)) {
                                continue;
                            }
                            visited.add(neighbour);
                            queue.add(new long[]{coord[0] + dx, coord[1] + dy, coord[2] + dz});
                        }
                    }
                }
            }
            this.evaluateCluster(cluster, now);
        }
    }

    private static long floorCell(double value) {
        return (long)Math.floor(value / STUN_CELL);
    }

    private static long cellKey(long x, long y, long z) {
        return (x & 0x1FFFFFL) | ((y & 0x1FFFFFL) << 21) | ((z & 0x1FFFFFL) << 42);
    }

    private static long encodeCell(long x, long y, long z) {
        return cellKey(x, y, z);
    }

    private static long[] decodeCell(long key) {
        long x = key & 0x1FFFFFL;
        key >>>= 21;
        long y = key & 0x1FFFFFL;
        key >>>= 21;
        long z = key & 0x1FFFFFL;
        if (x >= 0x100000L) {
            x -= 0x200000L;
        }
        if (y >= 0x100000L) {
            y -= 0x200000L;
        }
        if (z >= 0x100000L) {
            z -= 0x200000L;
        }
        return new long[]{x, y, z};
    }

    private void evaluateCluster(List<ParticlePoint> cluster, long now) {
        if (cluster.isEmpty()) {
            return;
        }
        double minX = 1.7976931348623157E308;
        double minY = 1.7976931348623157E308;
        double minZ = 1.7976931348623157E308;
        double maxX = -1.7976931348623157E308;
        double maxY = -1.7976931348623157E308;
        double maxZ = -1.7976931348623157E308;
        float totalWeight = 0.0f;
        float burstWeight = 0.0f;
        long burstCutoff = now - STUN_BURST_WINDOW_MS;
        for (ParticlePoint point : cluster) {
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            minZ = Math.min(minZ, point.z());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
            maxZ = Math.max(maxZ, point.z());
            totalWeight += point.weight();
            if (point.time() >= burstCutoff) {
                burstWeight += point.weight();
            }
        }
        if (totalWeight < STUN_MIN_WEIGHT || burstWeight < STUN_MIN_BURST) {
            return;
        }
        double spanX = maxX - minX;
        double spanY = maxY - minY;
        double spanZ = maxZ - minZ;
        if (spanX < STUN_MIN_SPAN || spanZ < STUN_MIN_SPAN || spanY < STUN_MIN_HEIGHT) {
            return;
        }
        if (spanX > STUN_MAX_SPAN || spanZ > STUN_MAX_SPAN || spanY > STUN_MAX_HEIGHT) {
            return;
        }
        minX -= STUN_CELL / 2.0;
        minY -= STUN_CELL / 2.0;
        minZ -= STUN_CELL / 2.0;
        maxX += STUN_CELL / 2.0;
        maxY += STUN_CELL / 2.0;
        maxZ += STUN_CELL / 2.0;
        if (!this.hasPlayerInside(minX, minY, minZ, maxX, maxY, maxZ)) {
            return;
        }
        class_238 box = new class_238(minX - 0.5, minY - 0.5, minZ - 0.5, maxX + 0.5, maxY + 0.5, maxZ + 0.5);
        class_243 center = box.method_1005();
        StunHighlight target = null;
        double best = 1.7976931348623157E308;
        for (StunHighlight highlight : this.stunHighlights) {
            if (!highlight.touches(box, STUN_MERGE_PADDING)) {
                continue;
            }
            double distance = highlight.centerDistanceSq(center.field_1352, center.field_1351, center.field_1350);
            if (distance >= best) {
                continue;
            }
            best = distance;
            target = highlight;
        }
        if (target == null) {
            if (this.stunHighlights.size() >= STUN_HIGHLIGHT_LIMIT) {
                return;
            }
            this.stunHighlights.add(new StunHighlight(box, now + STUN_LIFETIME_MS));
        } else {
            target.refresh(box, now + STUN_LIFETIME_MS);
        }
    }

    private boolean hasPlayerInside(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        class_238 anchor = new class_238(minX - STUN_ANCHOR_EXPAND, minY - STUN_ANCHOR_EXPAND, minZ - STUN_ANCHOR_EXPAND, maxX + STUN_ANCHOR_EXPAND, maxY + STUN_ANCHOR_EXPAND, maxZ + STUN_ANCHOR_EXPAND);
        for (class_1657 player : HWHelperModule.aiG.field_1687.method_18456()) {
            if (player == null || !player.method_5805() || player.method_29504()) {
                continue;
            }
            if (player.method_5829() == null || !anchor.method_994(player.method_5829())) {
                continue;
            }
            return true;
        }
        return false;
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (HWHelperModule.fullNullCheck()) {
            return;
        }
        class_4587 stack = matrixStackStage.l();
        float partialTicks = matrixStackStage.k().method_60637(true);
        if (((Boolean)this.trapPreview.jT()).booleanValue()) {
            if (this.isHolding(class_1802.field_8882)) {
                this.renderTrapPreview(stack, partialTicks);
            }
            if (this.isHolding(class_1802.field_8662)) {
                this.renderExplosiveTrapPreview(stack, partialTicks);
            }
        }
        long now = System.currentTimeMillis();
        if (((Boolean)this.snowballPreview.jT()).booleanValue()) {
            this.renderSnowballPrediction(stack, partialTicks, now);
        }
        if (((Boolean)this.stunHighlight.jT()).booleanValue()) {
            this.detectStunClusters(now);
            this.renderStunHighlights(stack, now);
        }
    }

    private void renderTrapPreview(class_4587 stack, float partialTicks) {
        class_1657 player = HWHelperModule.aiG.field_1724;
        double x = class_3532.method_16436(partialTicks, player.field_6014, player.method_23317());
        double y = class_3532.method_16436(partialTicks, player.field_6036, player.method_23318());
        double z = class_3532.method_16436(partialTicks, player.field_5969, player.method_23321());
        class_238 innerBox = new class_238(x - 1.0, y, z - 1.0, x + 1.0, y + 3.0, z + 1.0);
        boolean hasEnemy = false;
        for (class_1657 other : HWHelperModule.aiG.field_1687.method_18456()) {
            if (other == player || other.method_29504() || !other.method_5805() || !innerBox.method_994(other.method_5829())) {
                continue;
            }
            hasEnemy = true;
            break;
        }
        Color color = (Color)(hasEnemy ? this.trapColorInside.jT() : this.trapColorOutside.jT());
        class_238 box = new class_238(x - 1.5, y, z - 1.5, x + 1.5, y + 3.0, z + 1.5);
        this.drawGlowBox(stack, box, color, ((Float)this.trapLineWidth.jT()).floatValue(), Math.round(((Float)this.trapTransparency.jT()).floatValue() * 255.0f));
    }

    private void renderExplosiveTrapPreview(class_4587 stack, float partialTicks) {
        class_1657 player = HWHelperModule.aiG.field_1724;
        double x = class_3532.method_16436(partialTicks, player.field_6014, player.method_23317());
        double y = class_3532.method_16436(partialTicks, player.field_6036, player.method_23318());
        double z = class_3532.method_16436(partialTicks, player.field_5969, player.method_23321());
        class_238 box = new class_238(x - 3.0, y, z - 3.5, x + 3.0, y + EXPLOSIVE_HEIGHT, z + 3.5);
        class_238 innerBox = new class_238(x - 3.0 + 1.0, y + 1.0, z - 3.5 + 1.0, x + 3.0 - 1.0, y + EXPLOSIVE_HEIGHT - 1.0, z + 3.5 - 1.0);
        boolean hasEnemy = false;
        for (class_1657 other : HWHelperModule.aiG.field_1687.method_18456()) {
            if (other == player || other.method_29504() || !other.method_5805() || !innerBox.method_994(other.method_5829())) {
                continue;
            }
            hasEnemy = true;
            break;
        }
        Color color = (Color)(hasEnemy ? this.explosiveColorInside.jT() : this.explosiveColorOutside.jT());
        this.drawGlowBox(stack, box, color, ((Float)this.trapLineWidth.jT()).floatValue(), Math.round(((Float)this.trapTransparency.jT()).floatValue() * 255.0f));
    }

    /**
     * Точная копия старого drawGlowBox: 4 слоя, все без depth-теста (как в старом клиенте):
     * 1) заливка (внутри drawBox умножается на 0.1) + контур с прозрачностью из настроек,
     * 2) наружное свечение 3x с альфой 48,
     * 3) внутреннее свечение 2x с альфой 96,
     * 4) ядро с полной альфой цвета.
     */
    private void drawGlowBox(class_4587 stack, class_238 box, Color color, float lineWidth, int fillAlpha) {
        int rgb = color.getRGB() & 0xFFFFFF;
        Render3D.drawBox(stack, box, HWHelperModule.withAlpha(rgb, fillAlpha), lineWidth, true, true, false);
        Render3D.drawBox(stack, box, HWHelperModule.withAlpha(rgb, 48), lineWidth * 3.0f, true, false, false);
        Render3D.drawBox(stack, box, HWHelperModule.withAlpha(rgb, 96), lineWidth * 2.0f, true, false, false);
        Render3D.drawBox(stack, box, color.getRGB(), lineWidth, true, false, false);
    }

    /**
     * While a snowball is held, simulates the throw every frame and lights up
     * the hitboxes of every player this throw is guaranteed to hit.
     */
    private void renderSnowballPrediction(class_4587 stack, float partialTicks, long now) {
        class_1657 player = HWHelperModule.aiG.field_1724;
        if (player == null || HWHelperModule.aiG.field_1687 == null) {
            return;
        }
        if (!this.isHolding(class_1802.field_8543)) {
            this.predictedTargets.clear();
            return;
        }
        for (class_1657 target : this.predictSnowballTargets(player, partialTicks)) {
            this.predictedTargets.put(target.method_5628(), now);
        }
        this.predictedTargets.values().removeIf(time -> now - time > PREDICT_HOLD_MS);
        if (this.predictedTargets.isEmpty()) {
            return;
        }
        Color fillColor = (Color)this.snowballColor.jT();
        Color outlineColor = (Color)this.snowballOutline.jT();
        float transparency = class_3532.method_15363(((Float)this.snowballTransparency.jT()).floatValue(), 0.0f, 1.0f);
        float lineWidth = Math.max(1.0f, ((Float)this.snowballLineWidth.jT()).floatValue());
        for (Integer id : this.predictedTargets.keySet()) {
            class_1297 entity = HWHelperModule.aiG.field_1687.method_8469(id.intValue());
            if (!(entity instanceof class_1657) || entity == player || !entity.method_5805()) {
                continue;
            }
            class_243 offset = entity.method_30950(partialTicks).method_1020(entity.method_19538());
            class_238 box = entity.method_5829().method_997(offset);
            // Как в старом клиенте: заливка без depth-теста + контур, без пульса и неона.
            int fillRgb = fillColor.getRGB() & 0xFFFFFF;
            int fillAlpha = Math.round((float)fillColor.getAlpha() * transparency);
            if (fillAlpha > 0) {
                Render3D.drawBox(stack, box, HWHelperModule.withAlpha(fillRgb, fillAlpha), lineWidth, false, true, false);
            }
            if (outlineColor.getAlpha() <= 0) {
                continue;
            }
            Render3D.drawBox(stack, box, outlineColor.getRGB(), lineWidth, true, false, false);
        }
    }

    private List<class_1657> predictSnowballTargets(class_1657 self, float partialTicks) {
        ArrayList<class_1657> result = new ArrayList<class_1657>();
        double x = class_3532.method_16436(partialTicks, self.field_6014, self.method_23317());
        double y = class_3532.method_16436(partialTicks, self.field_6036, self.method_23318());
        double z = class_3532.method_16436(partialTicks, self.field_5969, self.method_23321());
        y += (double)self.method_18381(self.method_18376()) - 0.1;
        float yaw = class_3532.method_17821(partialTicks, self.field_5982, self.method_36454());
        float pitch = class_3532.method_16439(partialTicks, self.field_6004, self.method_36455());
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double vX = -Math.sin(yawRad) * Math.cos(pitchRad) * SNOW_SPEED;
        double vY = -Math.sin(pitchRad) * SNOW_SPEED;
        double vZ = Math.cos(yawRad) * Math.cos(pitchRad) * SNOW_SPEED;
        if (vX * vX + vY * vY + vZ * vZ < 1.0E-6) {
            return result;
        }
        HashSet<Integer> found = new HashSet<Integer>();
        for (int tick = 0; tick < SNOW_MAX_TICKS; ++tick) {
            double nextX = x + vX;
            double nextY = y + vY;
            double nextZ = z + vZ;
            for (int sub = 1; sub <= 2; ++sub) {
                double fraction = 0.5 * (double)sub;
                double fx = x + vX * fraction;
                double fy = y + vY * fraction;
                double fz = z + vZ * fraction;
                if (this.markEntitiesAt(self, fx, fy, fz, found, result)) {
                    return result;
                }
            }
            x = nextX;
            y = nextY;
            z = nextZ;
            class_3965 hit = HWHelperModule.aiG.field_1687.method_17742(new class_3959(new class_243(x - vX, y - vY, z - vZ), new class_243(x, y, z), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, self));
            if (hit != null && hit.method_17783() == class_239.class_240.field_1332) {
                break;
            }
            vX *= SNOW_DRAG;
            vY *= SNOW_DRAG;
            vZ *= SNOW_DRAG;
            vY -= SNOW_GRAVITY;
            if (y <= -64.0) {
                break;
            }
        }
        return result;
    }

    private boolean markEntitiesAt(class_1657 self, double x, double y, double z, Set<Integer> found, List<class_1657> result) {
        boolean hitSomething = false;
        class_238 pointBox = new class_238(x - 0.0625, y - 0.0625, z - 0.0625, x + 0.0625, y + 0.0625, z + 0.0625);
        for (class_1657 other : HWHelperModule.aiG.field_1687.method_18456()) {
            if (other == self || other.method_29504() || !other.method_5805()) {
                continue;
            }
            class_238 hitbox = other.method_5829();
            if (hitbox == null) {
                continue;
            }
            class_238 expanded = new class_238(hitbox.field_1323 - SNOW_EXPAND, hitbox.field_1322 - SNOW_EXPAND, hitbox.field_1321 - SNOW_EXPAND, hitbox.field_1320 + SNOW_EXPAND, hitbox.field_1325 + SNOW_EXPAND, hitbox.field_1324 + SNOW_EXPAND);
            if (!expanded.method_994(pointBox)) {
                continue;
            }
            int id = other.method_5628();
            if (found.add(id)) {
                result.add(other);
                hitSomething = true;
            }
        }
        return hitSomething;
    }

    private void renderStunHighlights(class_4587 stack, long now) {
        List<StunHighlight> snapshot;
        synchronized (STUN_LOCK) {
            this.stunHighlights.removeIf(highlight -> highlight.end <= now);
            snapshot = this.stunHighlights.isEmpty() ? List.of() : new ArrayList<StunHighlight>(this.stunHighlights);
        }
        if (snapshot.isEmpty()) {
            return;
        }
        class_243 camera = HWHelperModule.aiG.field_1773.method_19418().method_19326();
        int rgb = ColorUtils.getGlobalColor().getRGB() & 0xFFFFFF;
        float stunWidth = ((Float)this.stunWidth.jT()).floatValue();
        for (StunHighlight highlight : snapshot) {
            class_243 center = highlight.box().method_1005();
            double dx = center.field_1352 - camera.field_1352;
            double dy = center.field_1351 - camera.field_1351;
            double dz = center.field_1350 - camera.field_1350;
            if (dx * dx + dy * dy + dz * dz > STUN_RENDER_DISTANCE_SQ) {
                continue;
            }
            long remaining = highlight.end - now;
            if (remaining <= 0L) {
                continue;
            }
            float fade = Math.min(1.0f, (float)remaining / (float)STUN_FADE_WINDOW_MS);
            float pulse = 1.0f + 0.06f * (float)Math.sin((double)now / 400.0);
            // Толщина задаётся пикселями через геометрические риббоны (drawNeonBox),
            // поэтому настройка ширины реально влияет на неон (glLineWidth обрезаются
            // многими драйверами до 1px — старый drawBox на них не реагировал).
            // Внутренняя белая сердцевина ~55% ширины + внешние цветные слои дают старый вид.
            float baseWidth = Math.max(1.0f, stunWidth * fade * pulse);
            Render3D.drawNeonBox(stack, highlight.box(), HWHelperModule.blendWhiteTo(rgb, 0.0f), baseWidth * 0.55f, fade, true);
            Render3D.drawNeonBox(stack, highlight.box(), HWHelperModule.blendWhiteTo(rgb, 1.0f), baseWidth, fade * 0.9f, true);
            Render3D.drawNeonBox(stack, highlight.box(), rgb, baseWidth * 2.0f, fade * 0.35f, true);
        }
    }

    /**
     * Линейно смешивает белый с цветом: t=0 -> белый, t=1 -> заданный цвет.
     */
    private static int blendWhiteTo(int rgb, float t) {
        int r = Math.round(t * (float)(rgb >> 16 & 0xFF) + (1.0f - t) * 255.0f);
        int g = Math.round(t * (float)(rgb >> 8 & 0xFF) + (1.0f - t) * 255.0f);
        int b = Math.round(t * (float)(rgb & 0xFF) + (1.0f - t) * 255.0f);
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static int withAlpha(int rgb, int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        return clamped << 24 | rgb & 0xFFFFFF;
    }

    private static final class StunHighlight {
        double minX;
        double minY;
        double minZ;
        double maxX;
        double maxY;
        double maxZ;
        long end;

        StunHighlight(class_238 box, long end) {
            this.minX = box.field_1323;
            this.minY = box.field_1322;
            this.minZ = box.field_1321;
            this.maxX = box.field_1320;
            this.maxY = box.field_1325;
            this.maxZ = box.field_1324;
            this.end = end;
        }

        class_238 box() {
            return new class_238(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        }

        boolean touches(class_238 box, double pad) {
            return box.field_1323 <= this.maxX + pad && box.field_1320 >= this.minX - pad
                && box.field_1322 <= this.maxY + pad && box.field_1325 >= this.minY - pad
                && box.field_1321 <= this.maxZ + pad && box.field_1324 >= this.minZ - pad;
        }

        double centerDistanceSq(double x, double y, double z) {
            double cx = (this.minX + this.maxX) * 0.5 - x;
            double cy = (this.minY + this.maxY) * 0.5 - y;
            double cz = (this.minZ + this.maxZ) * 0.5 - z;
            return cx * cx + cy * cy + cz * cz;
        }

        void refresh(class_238 box, long end) {
            this.minX += (box.field_1323 - this.minX) * 0.3;
            this.minY += (box.field_1322 - this.minY) * 0.3;
            this.minZ += (box.field_1321 - this.minZ) * 0.3;
            this.maxX += (box.field_1320 - this.maxX) * 0.3;
            this.maxY += (box.field_1325 - this.maxY) * 0.3;
            this.maxZ += (box.field_1324 - this.maxZ) * 0.3;
            this.end = end;
        }
    }

    private record ParticlePoint(double x, double y, double z, long time, float weight, Object type) {
    }
}
