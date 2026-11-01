package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.client.gui.VillagerPatrolsConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class VillagerPatrolsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return VillagerPatrolsConfigScreen::new;
    }
}
