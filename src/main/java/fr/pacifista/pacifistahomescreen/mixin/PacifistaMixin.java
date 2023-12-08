package fr.pacifista.pacifistahomescreen.mixin;

import com.mojang.authlib.minecraft.BanDetails;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class PacifistaMixin extends Screen {
    private static final Text COPYRIGHT = Text.translatable("title.credits");

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
                .dimensions(this.width / 2 - 100, this.height / 4 + 24, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Cliquez pour rejoindre directement Pacifista !")))
                .build();

        this.addDrawableChild(joinButton);
        this.addDefaultButtons();
    }

    private void addDefaultButtons() {
        assert this.client != null;
        int i = this.textRenderer.getWidth(COPYRIGHT);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;

        this.addDrawableChild(AccessibilityOnboardingButtons.createLanguageButton(20, button ->
            this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()))
        , true))
        .setPosition(this.width / 2 - 124, l + 72 + 12);

        this.addDrawableChild(AccessibilityOnboardingButtons.createAccessibilityButton(20, button ->
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options))
        , true))
        .setPosition(this.width / 2 + 104, l + 72 + 12);

        this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, button ->
            this.client.setScreen(new CreditsAndAttributionScreen(this))
        , this.textRenderer));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), button ->
            this.client.setScreen(new OptionsScreen(this, this.client.options))
        ).dimensions(this.width / 2 - 100, l + 72 + 12, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), button ->
            this.client.scheduleStop()
        ).dimensions(this.width / 2 + 2, l + 72 + 12, 98, 20).build());

        this.initWidgetsNormal(l);
    }

    private void initWidgetsNormal(int y) {
        assert this.client != null;
        int spacingY = 24;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), button ->
            this.client.setScreen(new SelectWorldScreen(this))
        ).dimensions(this.width / 2 - 100, y, 200, 20).build());
        Text text = this.getMultiplayerDisabledText();
        boolean bl = text == null;
        Tooltip tooltip = text != null ? Tooltip.of(text) : null;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), button -> {
            Screen screen = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
            this.client.setScreen(screen);
        }).dimensions(this.width / 2 - 100, y + spacingY, 200, 20).tooltip(tooltip).build()).active = bl;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.online"), button ->
            this.client.setScreen(new RealmsMainScreen(this))
        ).dimensions(this.width / 2 - 100, y + spacingY * 2, 200, 20).tooltip(tooltip).build()).active = bl;
    }

    @Nullable
    private Text getMultiplayerDisabledText() {
        assert this.client != null;
        if (this.client.isMultiplayerEnabled()) {
            return null;
        } else if (this.client.isUsernameBanned()) {
            return Text.translatable("title.multiplayer.disabled.banned.name");
        } else {
            BanDetails banDetails = this.client.getMultiplayerBanDetails();
            if (banDetails != null) {
                return banDetails.expires() != null ? Text.translatable("title.multiplayer.disabled.banned.temporary") : Text.translatable("title.multiplayer.disabled.banned.permanent");
            } else {
                return Text.translatable("title.multiplayer.disabled");
            }
        }
    }
}