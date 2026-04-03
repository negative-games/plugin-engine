package games.negative.engine.paper.event;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Direct PacketEvents bridge that fires {@link ClientItemLoreEvent} for outgoing item packets.
 */
@Slf4j
public final class ClientItemLorePacketBridge extends PacketListenerAbstract {

    private static final int CARRIED_ITEM_SLOT = -1;

    private final Plugin plugin;
    private PacketListenerCommon registeredListener;

    public ClientItemLorePacketBridge(Plugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void enable() {
        if (registeredListener != null) return;
        if (PacketEvents.getAPI() == null) {
            log.warn("PacketEvents API is unavailable; client item lore bridge was not registered");
            return;
        }

        this.registeredListener = PacketEvents.getAPI().getEventManager().registerListener(this);
        log.info("Enabled PacketEvents client item lore bridge");
    }

    public void disable() {
        if (registeredListener == null) return;
        if (PacketEvents.getAPI() == null) {
            this.registeredListener = null;
            return;
        }

        PacketEvents.getAPI().getEventManager().unregisterListener(registeredListener);
        this.registeredListener = null;
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
        ItemStack updatedItem = applyLore(
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

        List<ItemStack> items = wrapper.getItems();
        List<ItemStack> modifiedItems = new ArrayList<>(items.size());
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack originalItem = items.get(slot);
            ItemStack updatedItem = applyLore(
                    player,
                    wrapper.getWindowId(),
                    slot,
                    originalItem
            );
            modifiedItems.add(updatedItem == null ? originalItem : updatedItem);
        }
        wrapper.setItems(modifiedItems);

        Optional<ItemStack> carriedItem = wrapper.getCarriedItem();
        if (carriedItem.isPresent()) {
            ItemStack updatedCarriedItem = applyLore(
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

    private ItemStack applyLore(
            Player player,
            int windowId,
            int slot,
            ItemStack packetItem
    ) {
        if (packetItem == null) return null;

        org.bukkit.inventory.ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(packetItem);
        if (bukkitItem == null || bukkitItem.getType().isAir()) return null;

        List<Component> originalLore = readLore(bukkitItem);
        ClientItemLoreEvent event = new ClientItemLoreEvent(player, windowId, slot, bukkitItem);
        plugin.getServer().getPluginManager().callEvent(event);

        List<Component> lore = event.getLore();
        if (lore.equals(originalLore)) {
            return null;
        }

        org.bukkit.inventory.ItemStack clientItem = bukkitItem.clone();
        ItemMeta meta = clientItem.getItemMeta();
        if (meta == null) {
            log.warn("Unable to apply client-side lore to item without item meta: {}", clientItem.getType());
            return null;
        }

        meta.lore(lore);
        clientItem.setItemMeta(meta);
        return SpigotConversionUtil.fromBukkitItemStack(clientItem);
    }

    private List<Component> readLore(org.bukkit.inventory.ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return List.of();
        }

        List<Component> lore = meta.lore();
        return lore == null ? List.of() : List.copyOf(lore);
    }
}
