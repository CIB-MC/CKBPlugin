package net.cibmc.spigot.ckb;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandExe implements CommandExecutor {
    PluginMain plugin;

    public CommandExe(PluginMain plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("[CKB] this command only can execute from player.");
            return true;
        }
        Player player = (Player)sender;
        switch(cmd.getName()) {
            case "ckbadmin":
                if(!player.hasPermission("ckb.admin")){
                    player.sendMessage(ChatColor.RED + "[CKB] 管理コマンドを発行する権限がありません。");
                    return true;
                }
                this.commandCkbAdmin(player, args);
                break;
        }
        return true;
    }

    private void commandCkbAdmin(Player player, String[] args){
        if (args.length == 0){
            player.sendMessage(ChatColor.RED + "[CKB] このコマンドには引数が必要です。");
            return;
        }

        switch(args[0]){
            case "mode":
                Boolean modeFlag = this.plugin.editModeAdminMap.get(player);
                if (modeFlag == null || !modeFlag.booleanValue()) {
                    this.plugin.editModeAdminMap.put(player, Boolean.TRUE);
                    player.sendMessage(ChatColor.GREEN + "[CKB] 編集モードを有効にしました。");
                } else {
                    this.plugin.editModeAdminMap.put(player, Boolean.FALSE);
                    player.sendMessage(ChatColor.GREEN + "[CKB] 編集モードを無効にしました。");
                }
                break;
            case "create":
                World world = player.getWorld();
                Entity entity = world.spawnEntity(player.getLocation(), EntityType.HORSE);
                if (entity == null || !(entity instanceof Horse)) {
                    player.sendMessage(ChatColor.RED + "[CKB] 馬の生成に失敗しました。");
                    return;
                }
                Horse horse = (Horse)entity;
                horse.setInvulnerable(true);
                horse.setRemoveWhenFarAway(false);
                horse.setAI(false);
                horse.setTamed(true);
                horse.setCustomName("CKB Station");
                if (!horse.isAdult()) {
                    horse.setAdult();
                }
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
                break;
            case "remove":
                Entity vehicle = player.getVehicle();
                if (vehicle == null || !(vehicle instanceof Horse)) {
                    player.sendMessage(ChatColor.RED + "[CKB] 除去対象の馬に乗ってください。");
                    return;
                }
                vehicle.eject();
                vehicle.remove();
                player.sendMessage(ChatColor.GREEN + "[CKB] 対象の馬をデスポーンさせました。");
                break;
            default:
                player.sendMessage(ChatColor.RED + "[CKB] 無効な引数です。");
                break;
        }
    }
}