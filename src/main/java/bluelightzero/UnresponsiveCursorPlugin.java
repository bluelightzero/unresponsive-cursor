package bluelightzero;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;

import java.awt.*;
import java.util.Date;

@Slf4j
@PluginDescriptor(
	name = "Unresponsive Cursor"
)
public class UnresponsiveCursorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientUI clientUI;

	private boolean wasLagging = false;
	private long lastTickTime;

	@Inject
	private UnresponsiveCursorConfig config;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
		if(wasLagging) {
			wasLagging = false;
			clientUI.resetCursor();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if(gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			if(wasLagging) {
				wasLagging = false;
				clientUI.resetCursor();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Date date = new Date();
		lastTickTime = date.getTime();
	}

	@Subscribe
	public void onClientTick(ClientTick tick) {
		Date date = new Date();
		long now = date.getTime();

		long diff = now-lastTickTime;

		boolean isLagging = diff > (600 + config.lagThreshold());

		boolean isDefaultCursor = (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR);

		if(isLagging && !wasLagging && isDefaultCursor) {
			clientUI.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			wasLagging = true;
		} else if(!isLagging && wasLagging) {
			clientUI.resetCursor();
			wasLagging = false;
		}
	}

	@Provides
    UnresponsiveCursorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnresponsiveCursorConfig.class);
	}
}
