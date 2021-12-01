package net.cibmc.spigot.ckb;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    private PluginMain plugin;
    public EventListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Horse)) return;
        Horse horse = (Horse)holder;
        if (!EventListener.isRentalHorse(horse)) return;
        HumanEntity entity = event.getPlayer();
        if (!(entity instanceof Player)) return;
        Player player = (Player)entity;
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "[CKB]" + ChatColor.WHITE + " インベントリはのぞけません");
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        InventoryHolder holder = event.getSource().getHolder();
        if (!(holder instanceof Player)) return;
        Player player = (Player)holder;
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof Horse)) return;
        Horse horse = (Horse)vehicle;
        if (!EventListener.isRentalHorse(horse)) return;

        ItemStack stack = event.getItem();
        if(stack == null) return;
        Material material = stack.getType();

        switch(material) {
            default:
                return;
            case SADDLE:
            case LEATHER_HORSE_ARMOR:
            case IRON_HORSE_ARMOR:
            case DIAMOND_HORSE_ARMOR:
                break;
        }
        plugin.getLogger().info("hello4");
        
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "[CKB]" + ChatColor.WHITE + " 馬のレンタル中は馬装備の編集はできません");
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();
        if (!(damager instanceof Player)) return;
        if (!(damaged instanceof Horse)) return;
        Horse horse = (Horse)damaged;
        if (!EventListener.isStationHorse(horse)) return;

        Player player = (Player)damager;
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "[CKB]" + ChatColor.WHITE + " なぐってもこうかがないよ");
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        if (!(e instanceof Horse)) return;

        Horse horse = (Horse)e;
        if (!(
            EventListener.isStationHorse(horse) ||
            EventListener.isRentalHorse(horse)
        )) return;

        Player player = event.getPlayer();
        ItemStack itemMain = player.getInventory().getItemInMainHand();
        switch(itemMain.getType()) {
            default:
                break;
            case GOLDEN_CARROT:
            case GOLDEN_APPLE:
            case NAME_TAG:
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[CKB]" + ChatColor.WHITE + " つかってもこうかがないよ");
                return;
        }

        ItemStack itemOff = player.getInventory().getItemInOffHand();
        switch(itemOff.getType()) {
            default:
                break;
            case GOLDEN_CARROT:
            case GOLDEN_APPLE:
            case NAME_TAG:
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[CKB]" + ChatColor.WHITE + " つかってもこうかがないよ");
                return;
        }

        if (!EventListener.isStationHorse(horse)) return;

        Boolean editModeFlag = this.plugin.editModeAdminMap.get(player);
        if (editModeFlag != null && editModeFlag.booleanValue()) {
            player.sendMessage(ChatColor.GREEN + "[CKB] 編集モードが有効です。馬を移動してください。");
            return;
        }

        event.setCancelled(true);

        Location loc = player.getLocation();
        Entity se = player.getWorld().spawnEntity(loc, EntityType.HORSE);
        if (se == null || !(se instanceof Horse)) {
            return;
        }

        Horse pHorse = (Horse)se;
        pHorse.setCustomName("CKB Rental");
        pHorse.setAI(false);
        pHorse.setAdult();
        pHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
        pHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
        pHorse.setHealth(30.0);
        pHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.45);
        pHorse.setOwner(player);
        pHorse.addPassenger(player);
        player.sendMessage(ChatColor.GREEN + "[CKB]" + ChatColor.WHITE + " ご利用ありがとうございます。どうぞご安全に！");
    }

    @EventHandler
    public void onVehicleExitEvent(VehicleExitEvent event) {
        Entity e = event.getExited();
        if (!(e instanceof Player)) {
            return;
        }
        Player player = (Player)e;

        Vehicle v = event.getVehicle();
        if (!(v instanceof Horse)) {
            return;
        }
        Horse horse = (Horse)v;
        if (!EventListener.isRentalHorse(horse)) return;

        horse.remove();
        player.sendMessage(ChatColor.GREEN + "[CKB]" + ChatColor.WHITE + " ご利用ありがとうございました。この後もご安全に！");
    }

    @EventHandler
    public void onPlayerExitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Entity e = player.getVehicle();
        if (e == null || !(e instanceof Horse)) {
            return;
        }

        Horse horse = (Horse)e;
        if (!EventListener.isRentalHorse(horse)) return;

        horse.remove();
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Entity e = player.getVehicle();
        if (e == null || !(e instanceof Horse)) {
            return;
        }

        Horse horse = (Horse)e;
        if (!EventListener.isRentalHorse(horse)) return;

        horse.remove();
    }

    @EventHandler
    public void onVehicleEnterEvent(EntityDamageEvent event) {;
        Entity e = event.getEntity();
        if (!(e instanceof Horse)) {
            return;
        }
        Horse horse = (Horse)e;

        if (!EventListener.isStationHorse(horse)) return;

        event.setCancelled(true);
    }

    public static boolean isStationHorse(Horse horse) {
        if (horse.hasAI()) {
            return false;
        }

        String cName = horse.getCustomName();
        if((cName == null) || !(cName.equals("CKB Station"))) {
            return false;
        }

        return true;
    }

    public static boolean isRentalHorse(Horse horse) {
        if (horse.hasAI()) {
            return false;
        }

        String cName = horse.getCustomName();
        if((cName == null) || !(cName.equals("CKB Rental"))) {
            return false;
        }

        return true;
    }
}