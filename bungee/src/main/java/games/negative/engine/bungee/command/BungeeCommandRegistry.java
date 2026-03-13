package games.negative.engine.bungee.command;

import games.negative.engine.bungee.BungeePlugin;
import games.negative.engine.command.CloudArgument;
import games.negative.moss.spring.Enableable;
import games.negative.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bungee.BungeeCommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ParserRegistry;

import java.util.logging.Logger;

@SpringComponent
@RequiredArgsConstructor
public class BungeeCommandRegistry implements Enableable {

    private static final Logger LOGGER = Logger.getLogger(BungeeCommandRegistry.class.getName());

    private final BungeePlugin plugin;

    @Override
    public void onEnable() {
        LOGGER.info("Registering commands");

        BungeeCommandManager<CommandSender> commands = new BungeeCommandManager<>(
                plugin,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        ParserRegistry<CommandSender> parser = commands.parserRegistry();
        plugin.fetchBeans(CloudArgument.class, argument -> {
            String name = argument.name();
            if (name == null) {
                parser.registerParser(ParserDescriptor.of(argument, argument.getType()));
            } else {
                parser.registerNamedParser(name, ParserDescriptor.of(argument, argument.getType()));
            }
            LOGGER.info("Parsed argument: " + argument.getClass().getSimpleName());
        }, (argument, e) -> {
            LOGGER.severe("Failed to parse argument: " + argument.getClass().getSimpleName());
            LOGGER.severe(e.getMessage());
        });

        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(commands, CommandSender.class);

        plugin.invokeBeans(BungeeCommand.class, command -> {
            command.onRegister(commands);
            annotationParser.parse(command);

            LOGGER.info("Registered command: " + command.getClass().getSimpleName());
        }, (command, e) -> {
            LOGGER.severe("Failed to register command: " + command.getClass().getSimpleName());
            LOGGER.severe(e.getMessage());
        });
    }
}
