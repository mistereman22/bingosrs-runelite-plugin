package com.bingosrs;

import com.bingosrs.api.BingOSRSService;
import com.bingosrs.notifiers.LootNotifier;
import com.bingosrs.panel.BingOSRSPanel;
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
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

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

	@Inject
	private ClientToolbar clientToolbar;

	private BingOSRSPanel panel;
	private NavigationButton navButton;

	private final AtomicReference<GameState> gameState = new AtomicReference<>();

	@Override
	protected void startUp() {
		panel = injector.getInstance(BingOSRSPanel.class);

		navButton = NavigationButton.builder()
				.tooltip("BingOSRS")
				.icon(ImageUtil.loadImageResource(getClass(), "/panel_icon.png"))
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		this.bingOSRSService.triggerAuth(false);
		this.bingoInfoManager.startUp();

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

        bingOSRSService.triggerAuth(false);
		if (event.getKey().equals("bingoId")) {
			bingoInfoManager.triggerUpdateData(false);
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

	public void updatePanel() {
		this.panel.update();
	}
}
