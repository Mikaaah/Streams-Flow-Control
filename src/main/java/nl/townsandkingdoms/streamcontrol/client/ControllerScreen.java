package nl.townsandkingdoms.streamcontrol.client;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import nl.townsandkingdoms.streamcontrol.ControlNetwork;

/** Compact, shared, server-validated editor for both controller blocks. */
public final class ControllerScreen extends Screen {
    private static final int PANEL_W = 284;
    private static final int PANEL_H = 226;
    private static final int COPPER = 0xFFD39A58;
    private static final int WATER = 0xFF6CCADA;

    private final ControlNetwork.Open snapshot;
    private int radius;
    private double speed;
    private boolean connected;
    private Direction facing;
    private Button connectedButton;
    private Button facingButton;
    private int left, top;

    public static void open(ControlNetwork.Open packet) {
        Minecraft.getInstance().setScreen(new ControllerScreen(packet));
    }

    private ControllerScreen(ControlNetwork.Open packet) {
        super(Component.translatable(packet.still() ? "block.streamcontrol.stillwater_anchor"
            : "block.streamcontrol.current_generator"));
        snapshot = packet;
        radius = packet.radius();
        speed = packet.strength();
        connected = packet.connectedOnly();
        facing = Direction.from3DDataValue(packet.facing());
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;
    }

    @Override protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        addRenderableWidget(new RadiusSlider(left + 16, top + 61, PANEL_W - 32));

        if (!snapshot.still()) {
            addRenderableWidget(new SpeedSlider(left + 16, top + 111, PANEL_W - 32));
            facingButton = addRenderableWidget(Button.builder(directionText(), b -> {
                facing = facing.getClockWise();
                b.setMessage(directionText());
            }).bounds(left + 16, top + 137, PANEL_W - 32, 20).build());
        }
        connectedButton = addRenderableWidget(Button.builder(connectedText(), b -> {
            connected = !connected;
            b.setMessage(connectedText());
        }).bounds(left + 16, top + 162, PANEL_W - 32, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.streamcontrol.apply"), b -> apply())
            .bounds(left + 16, top + 194, 154, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.streamcontrol.cancel"), b -> onClose())
            .bounds(left + 176, top + 194, PANEL_W - 192, 20).build());
    }

    private Component connectedText() {
        return Component.translatable("gui.streamcontrol.connected", Component.translatable(connected
            ? "gui.streamcontrol.on" : "gui.streamcontrol.off"));
    }

    private Component directionText() {
        return Component.translatable("gui.streamcontrol.direction",
            Component.translatable("gui.streamcontrol." + facing.getName()));
    }

    private void apply() {
        PacketDistributor.sendToServer(new ControlNetwork.Configure(snapshot.pos(), radius, speed,
            connected, facing.get3DDataValue()));
        onClose();
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Prevent Screen.render() from applying the deferred vanilla blur over labels.
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // The vanilla background shader is deferred on this Minecraft version:
        // it can blur our panel and labels while the widgets remain sharp.
        // Use a simple dim overlay so every part of this custom GUI stays crisp.
        g.fill(0, 0, width, height, 0xB0101722);
        g.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF4222C36);
        g.fill(left, top, left + PANEL_W, top + 3, snapshot.still() ? WATER : COPPER);
        g.renderOutline(left, top, PANEL_W, PANEL_H, 0xFF64717A);
        g.drawCenteredString(font, title, width / 2, top + 12, 0xFFF7F2E8);
        g.drawCenteredString(font, Component.translatable(snapshot.still()
            ? "gui.streamcontrol.still_description" : "gui.streamcontrol.current_description"),
            width / 2, top + 28, 0xFFB1BCC1);
        g.drawString(font, Component.translatable("gui.streamcontrol.radius_label"), left + 16, top + 49, 0xFFD1DBDF, false);
        g.drawString(font, Component.translatable("gui.streamcontrol.radius_details", radius * 2 + 1,
            snapshot.vertical()), left + 16, top + 86, 0xFFAFBDC3, false);
        if (snapshot.still()) {
            g.drawString(font, Component.translatable("gui.streamcontrol.still_speed"), left + 16, top + 116, WATER, false);
            g.drawString(font, Component.translatable("gui.streamcontrol.still_direction"), left + 16, top + 141,
                0xFFAFBDC3, false);
        } else {
            g.drawString(font, Component.translatable("gui.streamcontrol.speed_label"), left + 16, top + 99, 0xFFD1DBDF, false);
        }
        g.drawString(font, Component.translatable("gui.streamcontrol.connected_hint"),
            left + 16, top + 184, 0xFFAFBDC3, false);
        if (snapshot.powered()) g.drawString(font, Component.translatable("gui.streamcontrol.powered"),
            left + 16, top + 217, 0xFFF0B475, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private final class RadiusSlider extends AbstractSliderButton {
        private RadiusSlider(int x, int y, int w) {
            super(x, y, w, 20, Component.empty(), (double)(radius - 1) / Math.max(1, snapshot.maxRadius() - 1));
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.translatable("gui.streamcontrol.radius", radius));
        }
        @Override protected void applyValue() {
            radius = 1 + (int)Math.round(value * (snapshot.maxRadius() - 1));
            updateMessage();
        }
    }

    private final class SpeedSlider extends AbstractSliderButton {
        private SpeedSlider(int x, int y, int w) {
            super(x, y, w, 20, Component.empty(), (speed - 0.05) / Math.max(0.001, snapshot.maxStrength() - 0.05));
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.translatable("gui.streamcontrol.speed",
                String.format(Locale.ROOT, "%d%%", Math.round(speed * 100.0))));
        }
        @Override protected void applyValue() {
            speed = Math.max(0.05, Math.min(snapshot.maxStrength(),
                Math.round((0.05 + value * (snapshot.maxStrength() - 0.05)) * 20.0) / 20.0));
            updateMessage();
        }
    }
}
