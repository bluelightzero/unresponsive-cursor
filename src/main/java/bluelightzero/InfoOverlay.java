package bluelightzero;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.*;

import java.awt.*;

public class InfoOverlay extends Overlay {

    private final Client client;
    private final UnresponsiveCursorPlugin plugin;
    private final UnresponsiveCursorConfig config;

    private String text = "";
    private Color color;

    @Inject
    private InfoOverlay(Client client, UnresponsiveCursorPlugin plugin, UnresponsiveCursorConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOOLTIP);
    }

    public void clearText() {
        this.text = "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!text.isEmpty()) {
            final int textWidth = graphics.getFontMetrics().stringWidth(text);
            final int textHeight = graphics.getFontMetrics().getAscent() - graphics.getFontMetrics().getDescent();

            final Point mousePosition = client.getMouseCanvasPosition();
            final Point textPosition = new Point(mousePosition.getX() + 16, mousePosition.getY() + textHeight / 2);
            OverlayUtil.renderTextLocation(graphics, textPosition, text, color);
        }

        return null;
    }
}
