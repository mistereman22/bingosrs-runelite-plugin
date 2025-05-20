package com.bingosrs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BingOSRSPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BingOSRSPlugin.class);
		RuneLite.main(args);
	}
}