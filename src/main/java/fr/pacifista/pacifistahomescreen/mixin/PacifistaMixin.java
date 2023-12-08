package fr.pacifista.pacifistahomescreen.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class PacifistaMixin extends Screen {

    protected PacifistaMixin() {
        super(Text.literal("PacifistaMC"));
    }

    @Override
    public void init() {
        if (this.client == null) throw new NullPointerException("Client is null");
        ButtonWidget joinButton = ButtonWidget.builder(Text.literal("Rejoindre Pacifista"), button -> {
                    ServerInfo info = new ServerInfo("Pacifista", "play.pacifista.fr", ServerInfo.ServerType.LAN);

                    ConnectScreen.connect(
                            new MultiplayerScreen(new TitleScreen()),
                            MinecraftClient.getInstance(),
                            ServerAddress.parse(info.address),
                            info,
                            false);
                })
                .dimensions(this.width / 2 - 100, this.height / 2 - 12, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Cliquez pour rejoindre directement Pacifista !")))
                .build();

        ButtonWidget settingsButton = ButtonWidget.builder(Text.literal("Paramètres"), button ->
                    this.client.setScreen(new OptionsScreen(this, this.client.options))
                )
                .dimensions(this.width / 2 - 100, this.height / 2 + 12, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Paramètres Minecraft")))
                .build();

        ButtonWidget quitButton = ButtonWidget.builder(Text.literal("Quitter le jeu"), button ->
                    this.client.scheduleStop()
                )
                .dimensions(this.width / 2 - 100, this.height / 2 + 36, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Quitter le jeu")))
                .build();

        addDrawableChild(joinButton);
        addDrawableChild(settingsButton);
        addDrawableChild(quitButton);
    }
}
