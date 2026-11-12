package ru.method.clarityvisuals.modules.impl.render.cosmetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticDefinition;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticEffectType;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticModelAnchor;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticSlot;

public final class CosmeticCatalog {
    private static final Map<String, CosmeticDefinition> cf = new LinkedHashMap<String, CosmeticDefinition>();
    private static final Map<CosmeticSlot, List<CosmeticDefinition>> cg = new EnumMap<CosmeticSlot, List<CosmeticDefinition>>(CosmeticSlot.class);

    private CosmeticCatalog() {
    }

    private static void a(CosmeticDefinition cosmeticDefinition) {
        cf.put(cosmeticDefinition.aA(), cosmeticDefinition);
        cg.computeIfAbsent(cosmeticDefinition.aB(), cosmeticSlot -> new ArrayList()).add(cosmeticDefinition);
    }

    public static Optional<CosmeticDefinition> ag(String string) {
        if (string == null || string.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cf.get(string.trim().toLowerCase(Locale.ROOT)));
    }

    public static CosmeticDefinition a(String string, CosmeticSlot cosmeticSlot) {
        return CosmeticCatalog.ag(string).filter(cosmeticDefinition -> cosmeticDefinition.aB() == cosmeticSlot).orElseGet(() -> CosmeticCatalog.a(cosmeticSlot));
    }

    public static CosmeticDefinition a(CosmeticSlot cosmeticSlot) {
        List<CosmeticDefinition> list = cg.get((Object)cosmeticSlot);
        return list == null || list.isEmpty() ? CosmeticDefinition.a(cosmeticSlot) : list.getFirst();
    }

    public static List<CosmeticDefinition> b(CosmeticSlot cosmeticSlot) {
        List<CosmeticDefinition> list = cg.get((Object)cosmeticSlot);
        return list == null ? List.of(CosmeticCatalog.a(cosmeticSlot)) : Collections.unmodifiableList(list);
    }

    public static boolean b(String string, CosmeticSlot cosmeticSlot) {
        if (string == null || string.isBlank() || "none".equals(string.trim().toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (cosmeticSlot == CosmeticSlot.dr && string.startsWith("laby:")) {
            return true;
        }
        return CosmeticCatalog.ag(string).map(cosmeticDefinition -> cosmeticDefinition.aB() == cosmeticSlot).orElse(false);
    }

    static {
        for (CosmeticSlot cosmeticSlot2 : CosmeticSlot.values()) {
            cg.computeIfAbsent(cosmeticSlot2, cosmeticSlot -> new ArrayList()).add(CosmeticDefinition.a(cosmeticSlot2));
        }
        
        // Capes
        CosmeticCatalog.a(CosmeticDefinition.a("crystall_signature", "cosmetics.cape.clarity", "cosmetics/capes/crystall_signature.png"));
        CosmeticCatalog.a(CosmeticDefinition.a("crystall_dark", "cosmetics.cape.dark", "cosmetics/capes/crystall_signature.png"));
        CosmeticCatalog.a(CosmeticDefinition.a("crystall_neon", "cosmetics.cape.neon", "cosmetics/capes/crystall_signature.png"));
        CosmeticCatalog.a(CosmeticDefinition.a("crystall_fire", "cosmetics.cape.fire", "cosmetics/capes/crystall_signature.png"));
        
        // Hats
        CosmeticCatalog.a(CosmeticDefinition.a("hat_crown", CosmeticSlot.ds, "cosmetics.hat.crown", "cosmetics/icons/crown.png", "cosmetics/models/hat_crown.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("hat_horns", CosmeticSlot.ds, "cosmetics.hat.horns", "cosmetics/icons/horns.png", "cosmetics/models/hat_horns.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("hat_cap", CosmeticSlot.ds, "cosmetics.hat.cap", "cosmetics/icons/cap.png", "cosmetics/models/hat_cap.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("hat_halo", CosmeticSlot.ds, "cosmetics.hat.halo", "cosmetics/icons/crown.png", "cosmetics/models/hat_halo.json", CosmeticModelAnchor.cY));
        
        // Wings
        CosmeticCatalog.a(CosmeticDefinition.a("wings_angel", CosmeticSlot.dt, "cosmetics.wings.angel", "cosmetics/icons/angel.png", "cosmetics/models/wings_angel.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("wings_demon", CosmeticSlot.dt, "cosmetics.wings.demon", "cosmetics/icons/demon.png", "cosmetics/models/wings_demon.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("wings_crystal", CosmeticSlot.dt, "cosmetics.wings.crystal", "cosmetics/icons/angel.png", "cosmetics/models/wings_angel.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("wings_dragon", CosmeticSlot.dt, "cosmetics.wings.dragon", "cosmetics/icons/demon.png", "cosmetics/models/wings_demon.json", CosmeticModelAnchor.cZ));

        // Aura (auravisual) hats & wings
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_1", CosmeticSlot.ds, "cosmetics.aura.hats_1", "cosmetics/icons/aura_hats_1.png", "cosmetics/models/aura_hats_1.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_2", CosmeticSlot.ds, "cosmetics.aura.hats_2", "cosmetics/icons/aura_hats_2.png", "cosmetics/models/aura_hats_2.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_3", CosmeticSlot.ds, "cosmetics.aura.hats_3", "cosmetics/icons/aura_hats_3.png", "cosmetics/models/aura_hats_3.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_5", CosmeticSlot.ds, "cosmetics.aura.hats_5", "cosmetics/icons/aura_hats_5.png", "cosmetics/models/aura_hats_5.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_6", CosmeticSlot.ds, "cosmetics.aura.hats_6", "cosmetics/icons/aura_hats_6.png", "cosmetics/models/aura_hats_6.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_13", CosmeticSlot.ds, "cosmetics.aura.hats_13", "cosmetics/icons/aura_hats_13.png", "cosmetics/models/aura_hats_13.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_15", CosmeticSlot.ds, "cosmetics.aura.hats_15", "cosmetics/icons/aura_hats_15.png", "cosmetics/models/aura_hats_15.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_16", CosmeticSlot.ds, "cosmetics.aura.hats_16", "cosmetics/icons/aura_hats_16.png", "cosmetics/models/aura_hats_16.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_17", CosmeticSlot.ds, "cosmetics.aura.hats_17", "cosmetics/icons/aura_hats_17.png", "cosmetics/models/aura_hats_17.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_20", CosmeticSlot.ds, "cosmetics.aura.hats_20", "cosmetics/icons/aura_hats_20.png", "cosmetics/models/aura_hats_20.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_21", CosmeticSlot.ds, "cosmetics.aura.hats_21", "cosmetics/icons/aura_hats_21.png", "cosmetics/models/aura_hats_21.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_22", CosmeticSlot.ds, "cosmetics.aura.hats_22", "cosmetics/icons/aura_hats_22.png", "cosmetics/models/aura_hats_22.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_hats_23", CosmeticSlot.ds, "cosmetics.aura.hats_23", "cosmetics/icons/aura_hats_23.png", "cosmetics/models/aura_hats_23.json", CosmeticModelAnchor.cY));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_1", CosmeticSlot.dt, "cosmetics.aura.wings_1", "cosmetics/icons/aura_wings_1.png", "cosmetics/models/aura_wings_1.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_3", CosmeticSlot.dt, "cosmetics.aura.wings_3", "cosmetics/icons/aura_wings_3.png", "cosmetics/models/aura_wings_3.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_4", CosmeticSlot.dt, "cosmetics.aura.wings_4", "cosmetics/icons/aura_wings_4.png", "cosmetics/models/aura_wings_4.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_7", CosmeticSlot.dt, "cosmetics.aura.wings_7", "cosmetics/icons/aura_wings_7.png", "cosmetics/models/aura_wings_7.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_8", CosmeticSlot.dt, "cosmetics.aura.wings_8", "cosmetics/icons/aura_wings_8.png", "cosmetics/models/aura_wings_8.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_9", CosmeticSlot.dt, "cosmetics.aura.wings_9", "cosmetics/icons/aura_wings_9.png", "cosmetics/models/aura_wings_9.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_10", CosmeticSlot.dt, "cosmetics.aura.wings_10", "cosmetics/icons/aura_wings_10.png", "cosmetics/models/aura_wings_10.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_11", CosmeticSlot.dt, "cosmetics.aura.wings_11", "cosmetics/icons/aura_wings_11.png", "cosmetics/models/aura_wings_11.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_12", CosmeticSlot.dt, "cosmetics.aura.wings_12", "cosmetics/icons/aura_wings_12.png", "cosmetics/models/aura_wings_12.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_13", CosmeticSlot.dt, "cosmetics.aura.wings_13", "cosmetics/icons/aura_wings_13.png", "cosmetics/models/aura_wings_13.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_14", CosmeticSlot.dt, "cosmetics.aura.wings_14", "cosmetics/icons/aura_wings_14.png", "cosmetics/models/aura_wings_14.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_15", CosmeticSlot.dt, "cosmetics.aura.wings_15", "cosmetics/icons/aura_wings_15.png", "cosmetics/models/aura_wings_15.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_20", CosmeticSlot.dt, "cosmetics.aura.wings_20", "cosmetics/icons/aura_wings_20.png", "cosmetics/models/aura_wings_20.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_21", CosmeticSlot.dt, "cosmetics.aura.wings_21", "cosmetics/icons/aura_wings_21.png", "cosmetics/models/aura_wings_21.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_22", CosmeticSlot.dt, "cosmetics.aura.wings_22", "cosmetics/icons/aura_wings_22.png", "cosmetics/models/aura_wings_22.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_23", CosmeticSlot.dt, "cosmetics.aura.wings_23", "cosmetics/icons/aura_wings_23.png", "cosmetics/models/aura_wings_23.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_24", CosmeticSlot.dt, "cosmetics.aura.wings_24", "cosmetics/icons/aura_wings_24.png", "cosmetics/models/aura_wings_24.json", CosmeticModelAnchor.cZ));
        CosmeticCatalog.a(CosmeticDefinition.a("aura_wings_25", CosmeticSlot.dt, "cosmetics.aura.wings_25", "cosmetics/icons/aura_wings_25.png", "cosmetics/models/aura_wings_25.json", CosmeticModelAnchor.cZ));

        // Effects & Auras
        CosmeticCatalog.a(CosmeticDefinition.a("effect_spheres", "cosmetics.effect.spheres", "cosmetics/icons/effect_spheres.png", CosmeticEffectType.dl));
        CosmeticCatalog.a(CosmeticDefinition.a("effect_fire_aura", "cosmetics.effect.fire", "cosmetics/icons/effect_spheres.png", CosmeticEffectType.dj));
        CosmeticCatalog.a(CosmeticDefinition.a("effect_frost_aura", "cosmetics.effect.frost", "cosmetics/icons/effect_spheres.png", CosmeticEffectType.dk));
        CosmeticCatalog.a(CosmeticDefinition.a("effect_stars_aura", "cosmetics.effect.stars", "cosmetics/icons/effect_spheres.png", CosmeticEffectType.dm));
    }
}
