package bluelightzero;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("example")
public interface UnresponsiveCursorConfig extends Config {
	@Range(
			min = 0
	)
	@ConfigItem(
			keyName = "delay",
			name = "Delay (ms)",
			description = "How long to wait after lag before changing cursor in milliseconds.",
			position = 1
	)
	default int lagThreshold() { return 100; }
}