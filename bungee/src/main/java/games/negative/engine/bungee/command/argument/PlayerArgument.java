package games.negative.engine.bungee.command.argument;

import games.negative.engine.command.CloudArgument;
import games.negative.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Locale;

@SpringComponent
@RequiredArgsConstructor
public class PlayerArgument implements CloudArgument<CommandSender, ProxiedPlayer> {

    private final ProxyServer server;

    @Override
    public Class<ProxiedPlayer> getType() {
        return ProxiedPlayer.class;
    }

    @Override
    public @NonNull ArgumentParseResult<ProxiedPlayer> parse(
            @NonNull CommandContext<CommandSender> context,
            @NonNull CommandInput input
    ) {
        String string = input.readString();

        ProxiedPlayer player = server.getPlayer(string);
        return resultOrThrow(player, () -> new NullPointerException("Player \"" + string + "\" not found"));
    }

    @Override
    public @NonNull SuggestionProvider<CommandSender> suggestionProvider() {
        return SuggestionProvider.blockingStrings((context, input) -> {
            String prefix = input.lastRemainingToken().toLowerCase(Locale.ROOT);
            return server.getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        });
    }

}
