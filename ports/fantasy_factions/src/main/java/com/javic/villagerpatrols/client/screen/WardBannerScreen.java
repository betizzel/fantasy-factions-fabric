package com.javic.villagerpatrols.client.screen;

import com.javic.villagerpatrols.network.ClientboundOpenWardScreenMessage;
import com.javic.villagerpatrols.network.ModNetworking;
import com.javic.villagerpatrols.network.ServerboundWardSettingsActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class WardBannerScreen extends Screen {
    private final BlockPos pos;
    private boolean muted;
    private boolean showCrystal;
    private boolean showParticles;
    private Button soundButton;
    private Button crystalButton;
    private Button particlesButton;

    public WardBannerScreen(BlockPos pos, boolean muted, boolean showCrystal, boolean showParticles) {
        super(Component.translatable("gui.villagerpatrols.ward.title"));
        this.pos = pos;
        this.muted = muted;
        this.showCrystal = showCrystal;
        this.showParticles = showParticles;
    }

    public static void open(ClientboundOpenWardScreenMessage message) {
        Minecraft.getInstance().setScreen(new WardBannerScreen(message.pos(), message.muted(), message.showCrystal(), message.showParticles()));
    }

    @Override
    protected void init() {
        super.init();
        int left = width / 2 - 75;
        int top = height / 2 - 54;
        soundButton = addRenderableWidget(Button.builder(soundLabel(), button -> {
            muted = !muted;
            soundButton.setMessage(soundLabel());
            send(ServerboundWardSettingsActionMessage.Action.SET_MUTED, muted);
        }).bounds(left, top, 150, 20).build());
        crystalButton = addRenderableWidget(Button.builder(crystalLabel(), button -> {
            showCrystal = !showCrystal;
            crystalButton.setMessage(crystalLabel());
            send(ServerboundWardSettingsActionMessage.Action.SET_SHOW_CRYSTAL, showCrystal);
        }).bounds(left, top + 24, 150, 20).build());
        particlesButton = addRenderableWidget(Button.builder(particlesLabel(), button -> {
            showParticles = !showParticles;
            particlesButton.setMessage(particlesLabel());
            send(ServerboundWardSettingsActionMessage.Action.SET_SHOW_PARTICLES, showParticles);
        }).bounds(left, top + 48, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.villagerpatrols.ward.remove_mobs"), button -> openRemoveMobsConfirm()).bounds(left, top + 72, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.villagerpatrols.ward.done"), button -> onClose()).bounds(left, top + 104, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 72, 0xffffff);
    }

    private void send(ServerboundWardSettingsActionMessage.Action action, boolean value) {
        ModNetworking.sendToServer(new ServerboundWardSettingsActionMessage(pos, action, value));
    }

    private void openRemoveMobsConfirm() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) send(ServerboundWardSettingsActionMessage.Action.REMOVE_MOBS, true);
            minecraft.setScreen(this);
        }, Component.translatable("gui.villagerpatrols.ward.remove_mobs_confirm.title"),
            Component.translatable("gui.villagerpatrols.ward.remove_mobs_confirm.message"),
            Component.translatable("gui.villagerpatrols.ward.remove_mobs_confirm.confirm"),
            Component.translatable("gui.villagerpatrols.ward.remove_mobs_confirm.cancel")));
    }

    private Component soundLabel() { return Component.translatable(muted ? "gui.villagerpatrols.ward.sound_off" : "gui.villagerpatrols.ward.sound_on"); }
    private Component crystalLabel() { return Component.translatable(showCrystal ? "gui.villagerpatrols.ward.crystal_on" : "gui.villagerpatrols.ward.crystal_off"); }
    private Component particlesLabel() { return Component.translatable(showParticles ? "gui.villagerpatrols.ward.particles_on" : "gui.villagerpatrols.ward.particles_off"); }
}
