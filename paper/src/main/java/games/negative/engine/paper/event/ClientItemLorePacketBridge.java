package games.negative.engine.paper.event;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.MethodHandles;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Optional PacketEvents bridge that fires {@link ClientItemLoreEvent} for outgoing item packets.
 */
@Slf4j
public final class ClientItemLorePacketBridge {

    private static final int CARRIED_ITEM_SLOT = -1;

    private final Plugin plugin;

    private Object eventManager;
    private Object listenerHandle;

    public ClientItemLorePacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (listenerHandle != null || !isPacketEventsPresent()) return;

        try {
            ClassLoader classLoader = getPacketEventsClassLoader();

            Class<?> packetEventsClass = Class.forName("com.github.retrooper.packetevents.PacketEvents", false, classLoader);
            Class<?> packetListenerClass = Class.forName("com.github.retrooper.packetevents.event.PacketListener", false, classLoader);
            Class<?> priorityClass = Class.forName("com.github.retrooper.packetevents.event.PacketListenerPriority", false, classLoader);

            Object api = packetEventsClass.getMethod("getAPI").invoke(null);
            this.eventManager = api.getClass().getMethod("getEventManager").invoke(api);

            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return handleObjectMethod(proxy, method.getName(), args);
                }

                if ("onPacketSend".equals(method.getName()) && args != null && args.length == 1) {
                    onPacketSend(args[0]);
                }

                if (method.isDefault()) {
                    return invokeDefaultMethod(proxy, method, args);
                }

                return defaultValue(method.getReturnType());
            };

            Object listener = Proxy.newProxyInstance(packetListenerClass.getClassLoader(), new Class[]{packetListenerClass}, handler);
            Object priority = Enum.valueOf((Class<Enum>) priorityClass.asSubclass(Enum.class), "NORMAL");

            this.listenerHandle = eventManager.getClass()
                    .getMethod("registerListener", packetListenerClass, priorityClass)
                    .invoke(eventManager, listener, priority);

            log.info("Enabled PacketEvents client item lore bridge");
        } catch (ReflectiveOperationException exception) {
            this.eventManager = null;
            this.listenerHandle = null;
            log.warn("Failed to enable PacketEvents client item lore bridge", exception);
        }
    }

    public void disable() {
        if (eventManager == null || listenerHandle == null) return;

        try {
            Class<?> listenerCommonClass = Class.forName(
                    "com.github.retrooper.packetevents.event.PacketListenerCommon",
                    false,
                    getPacketEventsClassLoader()
            );

            eventManager.getClass()
                    .getMethod("unregisterListener", listenerCommonClass)
                    .invoke(eventManager, listenerHandle);
        } catch (ReflectiveOperationException exception) {
            log.warn("Failed to disable PacketEvents client item lore bridge", exception);
        } finally {
            this.eventManager = null;
            this.listenerHandle = null;
        }
    }

    private void onPacketSend(Object packetSendEvent) {
        if (ClientItemLoreEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        try {
            Object packetType = packetSendEvent.getClass().getMethod("getPacketType").invoke(packetSendEvent);
            if (!(packetType instanceof Enum<?> packetTypeEnum)) return;

            Object playerObject = packetSendEvent.getClass().getMethod("getPlayer").invoke(packetSendEvent);
            if (!(playerObject instanceof Player player)) return;

            switch (packetTypeEnum.name()) {
                case "SET_SLOT" -> handleSetSlot(packetSendEvent, player);
                case "WINDOW_ITEMS" -> handleWindowItems(packetSendEvent, player);
            }
        } catch (ReflectiveOperationException exception) {
            log.warn("Failed to process outgoing item packet", exception);
        }
    }

    private void handleSetSlot(Object packetSendEvent, Player player) throws ReflectiveOperationException {
        ClassLoader classLoader = getPacketEventsClassLoader();
        Class<?> packetSendEventClass = Class.forName(
                "com.github.retrooper.packetevents.event.PacketSendEvent",
                false,
                classLoader
        );
        Class<?> wrapperClass = Class.forName(
                "com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot",
                false,
                classLoader
        );
        Class<?> packetItemClass = Class.forName(
                "com.github.retrooper.packetevents.protocol.item.ItemStack",
                false,
                classLoader
        );

        Object wrapper = wrapperClass.getConstructor(packetSendEventClass).newInstance(packetSendEvent);
        int windowId = (int) wrapperClass.getMethod("getWindowId").invoke(wrapper);
        int slot = (int) wrapperClass.getMethod("getSlot").invoke(wrapper);
        Object packetItem = wrapperClass.getMethod("getItem").invoke(wrapper);

        Object updatedItem = applyLore(player, windowId, slot, packetItem);
        if (updatedItem != null) {
            wrapperClass.getMethod("setItem", packetItemClass).invoke(wrapper, updatedItem);
        }
    }

    private void handleWindowItems(Object packetSendEvent, Player player) throws ReflectiveOperationException {
        ClassLoader classLoader = getPacketEventsClassLoader();
        Class<?> packetSendEventClass = Class.forName(
                "com.github.retrooper.packetevents.event.PacketSendEvent",
                false,
                classLoader
        );
        Class<?> wrapperClass = Class.forName(
                "com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems",
                false,
                classLoader
        );
        Class<?> packetItemClass = Class.forName(
                "com.github.retrooper.packetevents.protocol.item.ItemStack",
                false,
                classLoader
        );

        Object wrapper = wrapperClass.getConstructor(packetSendEventClass).newInstance(packetSendEvent);
        int windowId = (int) wrapperClass.getMethod("getWindowId").invoke(wrapper);

        List<?> items = (List<?>) wrapperClass.getMethod("getItems").invoke(wrapper);
        List<Object> modifiedItems = new ArrayList<>(items.size());
        for (int slot = 0; slot < items.size(); slot++) {
            Object updatedItem = applyLore(player, windowId, slot, items.get(slot));
            modifiedItems.add(updatedItem == null ? items.get(slot) : updatedItem);
        }
        wrapperClass.getMethod("setItems", List.class).invoke(wrapper, modifiedItems);

        Optional<?> carriedItem = (Optional<?>) wrapperClass.getMethod("getCarriedItem").invoke(wrapper);
        if (carriedItem.isPresent()) {
            Object updatedCarriedItem = applyLore(player, windowId, CARRIED_ITEM_SLOT, carriedItem.get());
            if (updatedCarriedItem != null) {
                wrapperClass.getMethod("setCarriedItem", packetItemClass).invoke(wrapper, updatedCarriedItem);
            }
        }
    }

    private Object applyLore(Player player, int windowId, int slot, Object packetItem) throws ReflectiveOperationException {
        if (packetItem == null) return null;

        ItemStack bukkitItem = toBukkitItem(packetItem);
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
        if (meta == null) return null;

        meta.lore(lore);
        clientItem.setItemMeta(meta);

        return fromBukkitItem(clientItem);
    }

    private ItemStack toBukkitItem(Object packetItem) throws ReflectiveOperationException {
        Class<?> packetItemClass = Class.forName(
                "com.github.retrooper.packetevents.protocol.item.ItemStack",
                false,
                getPacketEventsClassLoader()
        );

        Class<?> conversionUtilClass = Class.forName(
                "io.github.retrooper.packetevents.util.SpigotConversionUtil",
                false,
                getPacketEventsClassLoader()
        );

        return (ItemStack) conversionUtilClass
                .getMethod("toBukkitItemStack", packetItemClass)
                .invoke(null, packetItem);
    }

    private Object fromBukkitItem(ItemStack itemStack) throws ReflectiveOperationException {
        Class<?> conversionUtilClass = Class.forName(
                "io.github.retrooper.packetevents.util.SpigotConversionUtil",
                false,
                getPacketEventsClassLoader()
        );

        return conversionUtilClass
                .getMethod("fromBukkitItemStack", ItemStack.class)
                .invoke(null, itemStack);
    }

    private boolean isPacketEventsPresent() {
        try {
            ClassLoader classLoader = getPacketEventsClassLoader();
            Class.forName("com.github.retrooper.packetevents.PacketEvents", false, classLoader);
            Class.forName("io.github.retrooper.packetevents.util.SpigotConversionUtil", false, classLoader);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private ClassLoader getPacketEventsClassLoader() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        Plugin packetEventsPlugin = pluginManager.getPlugin("packetevents");
        if (packetEventsPlugin == null) {
            packetEventsPlugin = pluginManager.getPlugin("PacketEvents");
        }

        return packetEventsPlugin == null
                ? plugin.getClass().getClassLoader()
                : packetEventsPlugin.getClass().getClassLoader();
    }

    private List<Component> readLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return List.of();
        }

        List<Component> lore = meta.lore();
        return lore == null ? List.of() : List.copyOf(lore);
    }

    private Object invokeDefaultMethod(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
        return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
                .unreflectSpecial(method, method.getDeclaringClass())
                .bindTo(proxy)
                .invokeWithArguments(args == null ? new Object[0] : args);
    }

    private Object handleObjectMethod(Object proxy, String methodName, Object[] args) {
        return switch (methodName) {
            case "toString" -> getClass().getSimpleName();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
            default -> null;
        };
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }

        if (returnType == boolean.class) return false;
        if (returnType == char.class) return '\0';
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0F;
        if (returnType == double.class) return 0D;
        return null;
    }
}
