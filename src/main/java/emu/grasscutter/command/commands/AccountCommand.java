package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.Player;

import java.util.List;
import emu.grasscutter.utils.FileUtils;
import java.io.*;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "account", usage = "account <create|delete> <username> [uid]", description = "修改用户账号")
public final class AccountCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (sender != null) {
            CommandHandler.sendMessage(sender, translate("commands.generic.console_execute_error"));
            return;
        }

        if (args.size() < 2) {
            CommandHandler.sendMessage(null, translate("commands.account.command_usage"));
            return;
        }

        String action = args.get(0);
        String username = args.get(1);

        switch (action) {
            default:
                CommandHandler.sendMessage(null, translate("commands.account.command_usage"));
                return;
            case "create":
                int uid = 0;
                if (args.size() > 2) {
                    try {
                        uid = Integer.parseInt(args.get(2));
                    } catch (NumberFormatException ignored) {
                        CommandHandler.sendMessage(null, translate("commands.account.invalid"));
                        return;
                    }
                }

                emu.grasscutter.game.Account account = DatabaseHelper.createAccountWithId(username, uid);
                if (account == null) {
                    CommandHandler.sendMessage(null, translate("commands.account.exists"));
                    return;
                } else {
                    account.addPermission("*");
                    account.save(); // Save account to database.
                    File new_account_file_command = new File("auth/passwords/" + username + ".leekpassword");
                    try {
                        new_account_file_command.createNewFile();
                    } catch (IOException e) {
                        Grasscutter.getLogger().info(e.getMessage());
                    }
                    Grasscutter.getLogger().info(String.format("创建并向%s写入文件",new String("auth/passwords/" + username + ".leekpassword")));
                    FileUtils.write("auth/passwords/" + username + ".leekpassword", "123".getBytes());

                    CommandHandler.sendMessage(null, translate("commands.account.create", Integer.toString(account.getPlayerUid())));
                }
                return;
            case "delete":
                if (DatabaseHelper.deleteAccount(username)) {
                    CommandHandler.sendMessage(null, translate("commands.account.delete"));
                } else {
                    CommandHandler.sendMessage(null, translate("commands.account.no_account"));
                }
        }
    }
}
