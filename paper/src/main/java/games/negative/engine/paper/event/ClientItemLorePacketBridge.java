package games.negative.engine.paper.event;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PacketEvents bridge that fires {@link ClientItemLoreEvent} for outgoing item packets.
 */
@Slf4j
public final class ClientItemLorePacketBridge extends PacketListenerAbstract {

    private static final int CARRIED_ITEM_SLOT = -1;

    private final Plugin plugin;
    private PacketListenerCommon listenerHandle;

    public ClientItemLorePacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (listenerHandle != null) return;
        if (PacketEvents.getAPI() == null) {
            log.warn("PacketEvents API is unavailable; client item lore bridge was not registered");
            return;
        }

        this.listenerHandle = PacketEvents.getAPI().getEventManager().registerListener(this);
        log.info("Enabled PacketEvents client item lore bridge");
    }

    public void disable() {
        if (listenerHandle == null) return;
        if (PacketEvents.getAPI() == null) {
            this.listenerHandle = null;
            return;
        }

        PacketEvents.getAPI().getEventManager().unregisterListener(listenerHandle);
        this.listenerHandle = null;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (ClientItemLoreEvent.getHandlerList().getRegisteredListeners().length == 0) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            handleSetSlot(event, player);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            handleWindowItems(event, player);
        }
    }

    private void handleSetSlot(PacketSendEvent event, Player player) {
        WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
        com.github.retrooper.packetevents.protocol.item.ItemStack updatedItem = applyLore(
                player,
                wrapper.getWindowId(),
                wrapper.getSlot(),
                wrapper.getItem()
        );

        if (updatedItem != null) {
            wrapper.setItem(updatedItem);
        }
    }

    private void handleWindowItems(PacketSendEvent event, Player player) {
        WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event);

        List<com.github.retrooper.packetevents.protocol.item.ItemStack> items = wrapper.getItems();
        List<com.github.retrooper.packetevents.protocol.item.ItemStack> modifiedItems = new ArrayList<>(items.size());
        for (int slot = 0; slot < items.size(); slot++) {
            com.github.retrooper.packetevents.protocol.item.ItemStack originalItem = items.get(slot);
            com.github.retrooper.packetevents.protocol.item.ItemStack updatedItem = applyLore(
                    player,
                    wrapper.getWindowId(),
                    slot,
                    originalItem
            );
            modifiedItems.add(updatedItem == null ? originalItem : updatedItem);
        }
        wrapper.setItems(modifiedItems);

        Optional<com.github.retrooper.packetevents.protocol.item.ItemStack> carriedItem = wrapper.getCarriedItem();
        if (carriedItem.isPresent()) {
            com.github.retrooper.packetevents.protocol.item.ItemStack updatedCarriedItem = applyLore(
                    player,
                    wrapper.getWindowId(),
                    CARRIED_ITEM_SLOT,
                    carriedItem.get()
            );
            if (updatedCarriedItem != null) {
                wrapper.setCarriedItem(updatedCarriedItem);
            }
        }
    }

    private com.github.retrooper.packetevents.protocol.item.ItemStack applyLore(
            Player player,
            int windowId,
            int slot,
            com.github.retrooper.packetevents.protocol.item.ItemStack packetItem
    ) {
        if (packetItem == null) return null;

        ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(packetItem);
        if (bukkitItem == null || bukkitItem.getType().isAir()) return null;

        List<Component> originalLore = readLore(bukkitItem);
        ClientItemLoreEvent event = new ClientItemLoreEvent(player, windowId, slot, bukkitItem);
        plugin.getServer().getPluginManager().callEvent(event);

        List<Component> lore = event.getLore();
        if (lore.equals(originalLore)) {
            return packetItem;
        }

        ItemStack clientItem = bukkitItem.clone();
        ItemMeta meta = clientItem.getItemMeta();
        if (meta == null) {
            log.warn("Unable to apply client-side lore to item without item meta: {}", clientItem.getType());
            return packetItem;
        }

        meta.lore(lore);
        clientItem.setItemMeta(meta);
        return SpigotConversionUtil.fromBukkitItemStack(clientItem);
    }

    private List<Component> readLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return List.of();
        }

        List<Component> lore = meta.lore();
        return lore == null ? List.of() : List.copyOf(lore);
    }
}
