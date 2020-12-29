package bluelightzero;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.CONNECTION_LOST)
		{
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.lagThreshold(), null);
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

		Cursor c;
		if(diff > (600 + config.lagThreshold())) {
			c = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		} else {
			c = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}

		clientUI.setCursor(c);
	}

	@Provides
    UnresponsiveCursorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnresponsiveCursorConfig.class);
	}
}
