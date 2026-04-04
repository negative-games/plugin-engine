package games.negative.engine.paper.event;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Optional loader for PacketEvents-backed client item lore handling.
 */
@Slf4j
public final class ClientItemLorePacketBridge {

    private static final String[] PACKET_EVENTS_PLUGIN_NAMES = {"packetevents", "PacketEvents"};

    private final Plugin plugin;
    private ClientItemLoreBridge delegate;

    public ClientItemLorePacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (delegate != null) return;
        if (!isPacketEventsPresent()) {
            log.info("PacketEvents not present; client item lore bridge will remain disabled");
            return;
        }

        try {
            ClientItemLoreBridge bridge = new PacketEventsClientItemLorePacketBridge(plugin);
            bridge.enable();
            this.delegate = bridge;
        } catch (NoClassDefFoundError exception) {
            log.warn("Failed to initialize optional PacketEvents client item lore bridge; runtime classes are unavailable", exception);
        } catch (LinkageError exception) {
            log.warn("Failed to initialize PacketEvents client item lore bridge due to a linkage problem", exception);
        }
    }

    public void disable() {
        if (delegate == null) return;

        delegate.disable();
        delegate = null;
    }

    private boolean isPacketEventsPresent() {
        return getPacketEventsPlugin() != null;
    }

    private Plugin getPacketEventsPlugin() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (String pluginName : PACKET_EVENTS_PLUGIN_NAMES) {
            Plugin packetEventsPlugin = pluginManager.getPlugin(pluginName);
            if (packetEventsPlugin != null) {
                return packetEventsPlugin;
            }
        }
        return null;
    }
}
