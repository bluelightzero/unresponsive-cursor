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
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ExecutorServiceExceptionLogger;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

	@Inject
	private WorldService worldService;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoOverlay infoOverlay;

	private static final int TICK_TIME = 600;
	private static final int PING_SLOW_INTERVAL = 6000;
	private static final int PING_FAST_INTERVAL = 200;

	private boolean wasLagging = false;
	private long lastTickTime;

	// All used for pinging
	private ScheduledExecutorService scheduledExecutorService;
	private static final int TIMEOUT = 1000;
	private static final int PORT = 43594;
	private long lastPingTime;
	private LinkedList<Long> recentPings = new LinkedList<>();
	private long lastPongTime;
	private long lastPingLatency;

	@Inject
	private UnresponsiveCursorConfig config;

	@Override
	protected void startUp() throws Exception
	{
		scheduledExecutorService = new ExecutorServiceExceptionLogger(Executors.newSingleThreadScheduledExecutor());
		overlayManager.add(infoOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		scheduledExecutorService.shutdown();
		scheduledExecutorService = null;
		overlayManager.remove(infoOverlay);

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
		lastTickTime = new Date().getTime();
	}

	private void pingCurrentWorld() {
		try {
			WorldResult worldResult = worldService.getWorlds();
			if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) {
				return;
			}

			final World currentWorld = worldResult.findWorld(client.getWorld());
			if (currentWorld == null) {
				return;
			}

			String address = currentWorld.getAddress();

			Socket socket = new Socket();
			InetAddress inetAddress = InetAddress.getByName(address);
			long before = new Date().getTime();
			socket.connect(new InetSocketAddress(inetAddress, PORT), TIMEOUT);
			long after = new Date().getTime();
			lastPongTime = after;
			lastPingLatency = after - before;

			client.addChatMessage(ChatMessageType.CONSOLE, "ping-log", "Latency = "+lastPingLatency, "Unresponsive Cursor");
		} catch (IOException exception) {
			client.addChatMessage(ChatMessageType.CONSOLE, "ping-log", "Latency = 1000+", "Unresponsive Cursor");
		}
	}

	private boolean hasBeenRecentPing() {
		long now = new Date().getTime();
		for (Long pingTime : recentPings) {
			long timeSinceRecentPing = now-pingTime;
			if(timeSinceRecentPing < PING_SLOW_INTERVAL && timeSinceRecentPing > lastPingLatency) {
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onClientTick(ClientTick tick) {
		long now = new Date().getTime();

		long timeSinceLastTick = now-lastTickTime;

		boolean isLagging = timeSinceLastTick > (600 + config.lagThreshold());

		if(isLagging && hasBeenRecentPing()) {
			long timeSincePong = now-lastPongTime;
			if (timeSincePong > PING_FAST_INTERVAL + lastPingLatency) {
				infoOverlay.setText(config.connectionLostText());
				infoOverlay.setColor(config.connectionLostTextColor());
			} else {
				infoOverlay.setText(config.serverLagText());
				infoOverlay.setColor(config.serverLagTextColor());
			}
		} else {
			infoOverlay.clearText();
		}

		boolean isDefaultCursor = (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR);

		if(isLagging && !wasLagging && isDefaultCursor) {
			clientUI.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			wasLagging = true;
		} else if(!isLagging && wasLagging) {
			clientUI.resetCursor();
			wasLagging = false;
		}

		long timeSincePing = now-lastPingTime;
		if(isLagging) {
			if(timeSincePing > PING_FAST_INTERVAL) {
				scheduledExecutorService.execute(this::pingCurrentWorld);
				lastPingTime = now;
				recentPings.add(now);
			}
		} else {
			if(timeSincePing > PING_SLOW_INTERVAL) {
				scheduledExecutorService.execute(this::pingCurrentWorld);
				lastPingTime = now;
				recentPings.add(now);
			}
		}

		if(recentPings.size() > 10) {
			recentPings.remove();
		}
	}

	@Provides
    UnresponsiveCursorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnresponsiveCursorConfig.class);
	}
}
