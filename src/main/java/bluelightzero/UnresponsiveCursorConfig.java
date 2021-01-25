package bluelightzero;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("example")
public interface UnresponsiveCursorConfig extends Config {
	@ConfigSection(
			name = "Server Freeze Style",
			description = "Settings for when the server freezes. (This is when things stop for everyone.)",
			position = 2
	)
	String serverFreezeSection = "Server Freeze Style";

	@ConfigSection(
			name = "Connection Lost Style",
			description = "Settings for when you lose connection. (This is when things stop for you, but then catch up afterwards.)",
			position = 4
	)
	String connectionLostSection = "Connection Lost Style";

	@Range(
			min = 50
	)
	@ConfigItem(
			keyName = "delay",
			name = "Delay (ms)",
			description = "How long to wait after lag before changing cursor in milliseconds. Low values may cause cursor to flicker.",
			position = 1
	)
	default int lagThreshold() { return 100; }

	@ConfigItem(
			keyName = "serverLagText",
			name = "Text",
			description = "The text to use when server freezes. (This is when things stop for everyone.)",
			position = 2,
			section = serverFreezeSection
	)
	default String serverLagText() { return "Server Freeze!"; }

	@ConfigItem(
			keyName = "serverLagTextColor",
			name = "Text Color",
			description = "The text color to use when server freezes. (This is when things stop for everyone.)",
			position = 3,
			section = serverFreezeSection
	)
	default Color serverLagTextColor() { return new Color(255, 141, 60); }

	@ConfigItem(
			keyName = "connectionLostText",
			name = "Text",
			description = "The text to use when you lose connection. (This is when things stop for you, but then catch up afterwards.)",
			position = 4,
			section = connectionLostSection
	)
	default String connectionLostText() { return "Connection Lost!"; }

	@ConfigItem(
			keyName = "connectionLostTextColor",
			name = "Text Color",
			description = "The text color to use when you lose connection. (This is when things stop for you, but then catch up afterwards.)",
			position = 5,
			section = connectionLostSection
	)
	default Color connectionLostTextColor() { return new Color(255, 57, 57); }

}