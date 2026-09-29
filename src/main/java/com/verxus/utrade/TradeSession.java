package com.verxus.utrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TradeSession {
    private final UTrade plugin;
    private final Player p1;
    private final Player p2;
    private final Inventory inventory;

    private boolean p1Ready = false;
    private boolean p2Ready = false;
    private boolean isCompleting = false;

    // Track economy and XP offers
    private double p1MoneyOffer = 0.0;
    private double p2MoneyOffer = 0.0;
    private int p1XpOffer = 0;
    private int p2XpOffer = 0;

    public final Set<Integer> P1_SLOTS = new HashSet<>(Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30));
    public final Set<Integer> P2_SLOTS = new HashSet<>(Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35));

    public TradeSession(UTrade plugin, Player p1, Player p2) {
        this.plugin = plugin;
        this.p1 = p1;
        this.p2 = p2;
        this.inventory = Bukkit.createInventory(null, 54, "Trade: " + p1.getName() + " & " + p2.getName());
        buildGUI();
    }

    private void buildGUI() {
        ItemStack separator = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta sepMeta = separator.getItemMeta();
        sepMeta.setDisplayName(" ");
        separator.setItemMeta(sepMeta);

        for (int i : new int[]{4, 13, 22, 31, 38, 39, 40, 41, 42, 47, 48, 49, 50, 51}) {
            inventory.setItem(i, separator);
        }
        
        // Add Player Heads
        inventory.setItem(46, getPlayerHead(p1));
        inventory.setItem(52, getPlayerHead(p2));
        
        updateButtons();
        updateMoneyButtons();
        updateXpButtons();
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + player.getName() + "'s Side");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Items on this side", 
                ChatColor.GRAY + "belong to " + player.getName()
        ));
        head.setItemMeta(meta);
        return head;
    }

    public void open() {
        p1.openInventory(inventory);
        p2.openInventory(inventory);
    }

    public void updateButtons() {
        ItemStack p1Btn = new ItemStack(Material.WOOL, 1, (short) (p1Ready ? 5 : 14));
        ItemMeta p1Meta = p1Btn.getItemMeta();
        p1Meta.setDisplayName(p1Ready ? ChatColor.GREEN + p1.getName() + " READY" : ChatColor.RED + p1.getName() + " NOT READY");
        p1Btn.setItemMeta(p1Meta);
        inventory.setItem(45, p1Btn);

        ItemStack p2Btn = new ItemStack(Material.WOOL, 1, (short) (p2Ready ? 5 : 14));
        ItemMeta p2Meta = p2Btn.getItemMeta();
        p2Meta.setDisplayName(p2Ready ? ChatColor.GREEN + p2.getName() + " READY" : ChatColor.RED + p2.getName() + " NOT READY");
        p2Btn.setItemMeta(p2Meta);
        inventory.setItem(53, p2Btn);
    }

    public void updateMoneyButtons() {
        inventory.setItem(36, createButton(Material.GOLD_INGOT, ChatColor.GOLD + p1.getName() + "'s Money Offer",
                ChatColor.YELLOW + "Offer: $" + String.format("%.2f", p1MoneyOffer), "+$100", "-$100", "+$1,000", "-$1,000"));
        inventory.setItem(44, createButton(Material.GOLD_INGOT, ChatColor.GOLD + p2.getName() + "'s Money Offer",
                ChatColor.YELLOW + "Offer: $" + String.format("%.2f", p2MoneyOffer), "+$100", "-$100", "+$1,000", "-$1,000"));
    }

    public void updateXpButtons() {
        inventory.setItem(37, createButton(Material.EXP_BOTTLE, ChatColor.AQUA + p1.getName() + "'s XP Offer",
                ChatColor.GREEN + "Offer: " + p1XpOffer + " Levels", "+1 Level", "-1 Level", "+10 Levels", "-10 Levels"));
        inventory.setItem(43, createButton(Material.EXP_BOTTLE, ChatColor.AQUA + p2.getName() + "'s XP Offer",
                ChatColor.GREEN + "Offer: " + p2XpOffer + " Levels", "+1 Level", "-1 Level", "+10 Levels", "-10 Levels"));
    }

    private ItemStack createButton(Material mat, String name, String line1, String L, String R, String SL, String SR) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(line1, ChatColor.GRAY + "Left-Click: " + L, ChatColor.GRAY + "Right-Click: " + R,
                ChatColor.GRAY + "Shift-Left: " + SL, ChatColor.GRAY + "Shift-Right: " + SR));
        item.setItemMeta(meta);
        return item;
    }

    public void modifyMoney(Player player, double amount) {
        if (player.equals(p1)) {
            double newAmount = Math.max(0, p1MoneyOffer + amount);
            if (plugin.getEconomy().getBalance(p1) < newAmount) {
                p1.sendMessage(ChatColor.RED + "You don't have enough money!");
                return;
            }
            p1MoneyOffer = newAmount;
        } else if (player.equals(p2)) {
            double newAmount = Math.max(0, p2MoneyOffer + amount);
            if (plugin.getEconomy().getBalance(p2) < newAmount) {
                p2.sendMessage(ChatColor.RED + "You don't have enough money!");
                return;
            }
            p2MoneyOffer = newAmount;
        }
        updateMoneyButtons();
        resetReady();
    }

    public void modifyXp(Player player, int amount) {
        if (player.equals(p1)) {
            int newAmount = Math.max(0, p1XpOffer + amount);
            if (p1.getLevel() < newAmount) {
                p1.sendMessage(ChatColor.RED + "You don't have enough XP levels!");
                return;
            }
            p1XpOffer = newAmount;
        } else if (player.equals(p2)) {
            int newAmount = Math.max(0, p2XpOffer + amount);
            if (p2.getLevel() < newAmount) {
                p2.sendMessage(ChatColor.RED + "You don't have enough XP levels!");
                return;
            }
            p2XpOffer = newAmount;
        }
        updateXpButtons();
        resetReady();
    }

    public void toggleReady(Player player) {
        if (player.equals(p1)) p1Ready = !p1Ready;
        if (player.equals(p2)) p2Ready = !p2Ready;
        updateButtons();

        if (p1Ready && p2Ready) completeTrade();
    }

    public void resetReady() {
        if (p1Ready || p2Ready) {
            p1Ready = false;
            p2Ready = false;
            updateButtons();
            p1.sendMessage(ChatColor.YELLOW + "Trade contents changed, unreadied.");
            p2.sendMessage(ChatColor.YELLOW + "Trade contents changed, unreadied.");
        }
    }

    private void completeTrade() {
        if (isCompleting) return;

        if (plugin.getEconomy().getBalance(p1) < p1MoneyOffer || plugin.getEconomy().getBalance(p2) < p2MoneyOffer) {
            abortWithMessage("Someone could not afford their money offer.");
            return;
        }
        if (p1.getLevel() < p1XpOffer || p2.getLevel() < p2XpOffer) {
            abortWithMessage("Someone did not have enough XP levels.");
            return;
        }

        isCompleting = true;

        // Process Money
        if (p1MoneyOffer > 0) { plugin.getEconomy().withdrawPlayer(p1, p1MoneyOffer); plugin.getEconomy().depositPlayer(p2, p1MoneyOffer); }
        if (p2MoneyOffer > 0) { plugin.getEconomy().withdrawPlayer(p2, p2MoneyOffer); plugin.getEconomy().depositPlayer(p1, p2MoneyOffer); }

        // Process XP Levels
        if (p1XpOffer > 0) { p1.setLevel(p1.getLevel() - p1XpOffer); p2.setLevel(p2.getLevel() + p1XpOffer); }
        if (p2XpOffer > 0) { p2.setLevel(p2.getLevel() - p2XpOffer); p1.setLevel(p1.getLevel() + p2XpOffer); }

        // Process Items
        for (int slot : P1_SLOTS) giveItemSafely(p2, inventory.getItem(slot));
        for (int slot : P2_SLOTS) giveItemSafely(p1, inventory.getItem(slot));

        inventory.clear(); 
        p1.sendMessage(ChatColor.GREEN + "Trade completed successfully!");
        p2.sendMessage(ChatColor.GREEN + "Trade completed successfully!");
        p1.closeInventory();
        p2.closeInventory();
        cleanup();
    }

    private void abortWithMessage(String reason) {
        p1.sendMessage(ChatColor.RED + "Trade failed: " + reason);
        p2.sendMessage(ChatColor.RED + "Trade failed: " + reason);
        abort(null);
    }

    public void abort(Player canceler) {
        if (isCompleting) return;
        isCompleting = true;
        
        if (canceler != null) {
            Player other = canceler.equals(p1) ? p2 : p1;
            canceler.sendMessage(ChatColor.RED + "You cancelled the trade.");
            other.sendMessage(ChatColor.RED + canceler.getName() + " cancelled the trade.");
        }

        for (int slot : P1_SLOTS) giveItemSafely(p1, inventory.getItem(slot));
        for (int slot : P2_SLOTS) giveItemSafely(p2, inventory.getItem(slot));
        
        inventory.clear();
        p1.closeInventory();
        p2.closeInventory();
        cleanup();
    }

    private void giveItemSafely(Player p, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(item);
        for (ItemStack leftover : overflow.values()) p.getWorld().dropItemNaturally(p.getLocation(), leftover);
    }

    private void cleanup() {
        plugin.removeTradeSession(p1.getUniqueId());
        plugin.removeTradeSession(p2.getUniqueId());
    }

    public Player getP1() { return p1; }
    public Player getP2() { return p2; }
    public Inventory getInventory() { return inventory; }
}