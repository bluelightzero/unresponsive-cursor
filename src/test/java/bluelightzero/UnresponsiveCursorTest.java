package bluelightzero;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class UnresponsiveCursorTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(UnresponsiveCursorPlugin.class);
		RuneLite.main(args);
	}
}