package com.bingosrs.notifiers;

import com.bingosrs.BingOSRSConfig;
import com.bingosrs.BingoInfoManager;
import com.bingosrs.api.BingOSRSService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.http.api.loottracker.LootRecordType;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class LootNotifier {

    public static final Set<Integer> SPECIAL_LOOT_NPC_IDS = Set.of(
            NpcID.WHISPERER, NpcID.WHISPERER_MELEE, NpcID.WHISPERER_QUEST, NpcID.WHISPERER_MELEE_QUEST,
            NpcID.ARAXXOR, NpcID.ARAXXOR_DEAD, NpcID.RT_FIRE_QUEEN_INACTIVE, NpcID.RT_ICE_KING_INACTIVE
    );
    public static final Set<String> SPECIAL_LOOT_NPC_NAMES = Set.of("The Whisperer", "Araxxor", "Branda the Fire Queen", "Eldric the Ice King");

    @Inject
    private BingOSRSConfig bingOSRSConfig;

    @Inject
    private BingoInfoManager bingoInfoManager;

    @Inject
    private BingOSRSService bingOSRSService;

    @Inject
    private Client client;

    @Inject
    private DrawManager drawManager;

    public void onServerNpcLoot(ServerNpcLoot event) {
        var comp = event.getComposition();
        this.handleNotify(event.getItems(), comp.getName(), LootRecordType.NPC, comp.getId());
    }

    public void onNpcLootReceived(NpcLootReceived event) {
        NPC npc = event.getNpc();
        int id = npc.getId();
        if (SPECIAL_LOOT_NPC_IDS.contains(id)) {
            // LootReceived is fired for certain NPCs rather than NpcLootReceived, but return here just in case upstream changes their implementation.
            return;
        }

        this.handleNotify(event.getItems(), npc.getName(), LootRecordType.NPC, id);
    }

    public void onLootReceived(LootReceived lootReceived) {
        // only consider non-NPC and non-PK loot
        if (lootReceived.getType() == LootRecordType.EVENT || lootReceived.getType() == LootRecordType.PICKPOCKET) {
            this.handleNotify(lootReceived.getItems(), lootReceived.getName(), lootReceived.getType(), null);
        } else if (lootReceived.getType() == LootRecordType.NPC && SPECIAL_LOOT_NPC_NAMES.contains(lootReceived.getName())) {
            // Special case: upstream fires LootReceived for certain NPCs, but not NpcLootReceived
            this.handleNotify(lootReceived.getItems(), lootReceived.getName(), lootReceived.getType(), null);
        }
    }

    private void handleNotify(Collection<ItemStack> items, String dropper, LootRecordType type, Integer npcId) {
        if (npcId == null && (type == LootRecordType.NPC || type == LootRecordType.PICKPOCKET)) {
            npcId = client.getTopLevelWorldView().npcs().stream()
                    .filter(npc -> dropper.equals(npc.getName()))
                    .findAny()
                    .map(NPC::getId)
                    .orElse(null);
        }

        for (ItemStack item : items) {
            Integer finalNpcId = npcId;

            if (bingoInfoManager.isRequiredDrop(item.getId(), npcId)) {
                log.debug("Submitting drop to bingo: " + item.getId());
                drawManager.requestNextFrameListener(image ->
                {
                    BufferedImage bufferedImage = (BufferedImage) image;
                    byte[] imageBytes;
                    try
                    {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                        imageBytes = byteArrayOutputStream.toByteArray();
                        bingOSRSService.submitDropAsync(bingOSRSConfig.bingoId(), imageBytes, client.getLocalPlayer().getName(), item.getId(), finalNpcId)
                                .whenComplete((result, throwable) -> bingoInfoManager.triggerUpdateRequiredDrops());
                    }
                    catch (IOException e)
                    {
                        log.debug("Error submitting drop", e);
                    }
                });
            }
        }
    }

}
