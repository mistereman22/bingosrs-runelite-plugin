package com.bingosrs;

import com.bingosrs.api.BingOSRSService;
import com.bingosrs.notifiers.LootNotifier;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.*;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@PluginDescriptor(
	name = "BingOSRS"
)
public class BingOSRSPlugin extends Plugin
{
	@Inject
	private BingoInfoManager bingoInfoManager;

	@Inject
	private BingOSRSService bingOSRSService;

	@Inject
	private LootNotifier lootNotifier;

	private final AtomicReference<GameState> gameState = new AtomicReference<>();

	@Override
	protected void startUp() {
		log.debug("Started up BingOSRS");
	}

	@Override
	protected void shutDown() {
		log.debug("Shutting down BingOSRS");
		gameState.lazySet(null);
	}

	@Provides
	BingOSRSConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BingOSRSConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!BingoInfoManager.CONFIG_GROUP.equals(event.getGroup())) {
			return;
		}

        bingOSRSService.triggerAuth();
		if (event.getKey().equals("bingoId")) {
			bingoInfoManager.triggerUpdateRequiredDrops();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		bingoInfoManager.onGameTick(event);
		bingOSRSService.onGameTick(event);
	}

	@Subscribe(priority = 1) // run before the base loot tracker plugin
	public void onServerNpcLoot(ServerNpcLoot event) {
		// temporarily only use new event when needed
		if (event.getComposition().getId() != NpcID.YAMA) {
			return;
		}
		lootNotifier.onServerNpcLoot(event);
	}

	@Subscribe(priority = 1) // run before the base loot tracker plugin
	public void onNpcLootReceived(NpcLootReceived npcLootReceived) {
		if (npcLootReceived.getNpc().getId() == NpcID.YAMA) {
			// handled by ServerNpcLoot, but return just in case
			return;
		}

		lootNotifier.onNpcLootReceived(npcLootReceived);
	}

	@Subscribe
	public void onLootReceived(LootReceived lootReceived) {
		lootNotifier.onLootReceived(lootReceived);
	}
}
