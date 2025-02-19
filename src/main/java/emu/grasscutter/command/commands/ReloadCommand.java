package emu.grasscutter.command.commands;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "reload", usage = "reload", permission = "server.reload", description = "commands.reload.description")
public final class ReloadCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        CommandHandler.sendMessage(sender, translate("commands.reload.reload_start"));
        
        Grasscutter.loadConfig();
        Grasscutter.loadLanguage();
        Grasscutter.getGameServer().getGachaManager().load();
        Grasscutter.getGameServer().getDropManager().load();
        Grasscutter.getGameServer().getShopManager().load();
        Grasscutter.getDispatchServer().loadQueries();
        
        CommandHandler.sendMessage(sender, translate("commands.reload.reload_done"));
    }
}
