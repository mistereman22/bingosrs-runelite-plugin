package com.bingosrs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bingosrs")
public interface BingOSRSConfig extends Config
{
	@ConfigItem(
		keyName = "bingoId",
		name = "Bingo ID",
		description = "ID of the bingo "
	)
	default String bingoId()
	{
		return "";
	}

	@ConfigItem(
			keyName = "playerToken",
			name = "Player Token",
			description = "Player token for the bingo. Required to submit drops.",
			secret = true
	)
	default String playerToken()
	{
		return "";
	}
}
