package lanse.lanses.challenge.modpack;

import lanse.lanses.challenge.modpack.challenges.blizzard.Blizzard;
import lanse.lanses.challenge.modpack.challenges.mobexploder.MobExploder;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
public class InitializedListeners {

    public static void register() {

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (killedEntity != null && (MainControl.modPreset == MainControl.Preset.MOBEXPLODER
                    || MainControl.modPreset == MainControl.Preset.ALL && MainControl.isModEnabled)) {
                MobExploder.onLivingEntityDeath(killedEntity, world);
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (MainControl.modPreset == MainControl.Preset.BLIZZARD && MainControl.isModEnabled) {
                Blizzard.onEntitySpawn(entity, world);
            }
        });
    }
}
