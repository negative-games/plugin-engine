package games.negative.engine.paper.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Fired when a client-bound item packet is about to be sent to a player.
 * Changes made through this event only affect the client view of the item lore.
 */
public final class ClientItemLoreEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int windowId;
    private final int slot;
    private final ItemStack originalItem;
    private List<Component> lore;

    public ClientItemLoreEvent(Player player, int windowId, int slot, ItemStack item) {
        super(player);
        this.windowId = windowId;
        this.slot = slot;
        this.originalItem = item.clone();

        ItemMeta meta = item.getItemMeta();
        List<Component> currentLore = meta == null ? List.of() : meta.lore();
        this.lore = currentLore == null ? new ArrayList<>() : new ArrayList<>(currentLore);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getOriginalItem() {
        return originalItem.clone();
    }

    public List<Component> getLore() {
        return lore;
    }

    public void setLore(List<Component> lore) {
        this.lore = lore == null ? new ArrayList<>() : new ArrayList<>(lore);
    }

    public void clearLore() {
        this.lore.clear();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
