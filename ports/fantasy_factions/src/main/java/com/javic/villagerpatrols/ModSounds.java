package com.javic.villagerpatrols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final Map<String, Supplier<SoundEvent>> REGISTERED = new HashMap<>();
    public static final Supplier<SoundEvent> LUTE_BARD_1 = register("item.lute.bard_1");
    public static final Supplier<SoundEvent> LUTE_BARD_2 = register("item.lute.bard_2");
    public static final Supplier<SoundEvent> LUTE_BARD_3 = register("item.lute.bard_3");
    public static final Supplier<SoundEvent> LUTE_BARD_4 = register("item.lute.bard_4");
    public static final Supplier<SoundEvent> LUTE_BARD_5 = register("item.lute.bard_5");
    public static final Supplier<SoundEvent> LUTE_BARD_6 = register("item.lute.bard_6");
    public static final Supplier<SoundEvent> LUTE_BARD_7 = register("item.lute.bard_7");
    public static final Supplier<SoundEvent> LUTE_BARD_8 = register("item.lute.bard_8");
    public static final Supplier<SoundEvent> LUTE_BARD_9 = register("item.lute.bard_9");
    public static final Supplier<SoundEvent> LUTE_BARD_10 = register("item.lute.bard_10");
    private static final List<Supplier<SoundEvent>> LUTE_BARDS = List.of(LUTE_BARD_1, LUTE_BARD_2,
            LUTE_BARD_3, LUTE_BARD_4, LUTE_BARD_5, LUTE_BARD_6, LUTE_BARD_7, LUTE_BARD_8,
            LUTE_BARD_9, LUTE_BARD_10);
    private static final String[] GENDERS = {"male", "female"};
    private static final String[] AMBIENT_HURT_DEATH_FOLDERS = {
            "villager", "knight", "knight_shield", "knight_leader", "village_guard",
            "village_guard_leader", "elf", "dwarf", "orc", "evil_knight", "evil_knight_shield",
            "evil_knight_leader", "bandit", "shadow", "evil_wizard", "wizard", "human_pillager",
            "human_vindicator", "human_evoker", "vampire"};
    private static final String[] INTERACT_FOLDERS = {"villager", "knight", "knight_shield", "knight_leader",
            "village_guard", "village_guard_leader", "elf", "dwarf", "wizard", "vampire"};
    private static final String[] POSITIVE_NEGATIVE_FOLDERS = {"vampire", "wizard", "elf", "dwarf"};

    static {
        for (String folder : AMBIENT_HURT_DEATH_FOLDERS) for (String gender : GENDERS) {
            register(soundKey(folder, gender, "ambient"));
            register(soundKey(folder, gender, "hurt"));
            register(soundKey(folder, gender, "death"));
        }
        for (String folder : INTERACT_FOLDERS) for (String gender : GENDERS)
            register(soundKey(folder, gender, "interact"));
        for (String folder : POSITIVE_NEGATIVE_FOLDERS) for (String gender : GENDERS) {
            register(soundKey(folder, gender, "positive"));
            register(soundKey(folder, gender, "negative"));
        }
    }

    private ModSounds() {}
    public static void init() { LUTE_BARD_1.get(); }
    public static SoundEvent getAmbient(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "ambient")); }
    public static SoundEvent getHurt(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "hurt")); }
    public static SoundEvent getDeath(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "death")); }
    public static SoundEvent getInteract(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "interact")); }
    public static SoundEvent getPositive(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "positive")); }
    public static SoundEvent getNegative(String folder, boolean female) { return get(soundKey(folder, female ? "female" : "male", "negative")); }
    public static SoundEvent getLuteBard(int trackIndex) { return LUTE_BARDS.get(Math.floorMod(trackIndex, LUTE_BARDS.size())).get(); }

    private static Supplier<SoundEvent> register(String path) {
        Supplier<SoundEvent> sound = NativeRegistry.register(BuiltInRegistries.SOUND_EVENT, path,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillagerPatrols.MODID, path)));
        REGISTERED.put(path, sound);
        return sound;
    }
    private static SoundEvent get(String path) { return REGISTERED.get(path).get(); }
    private static String soundKey(String folder, String gender, String type) { return "entity." + folder + "." + gender + "." + type; }
}
