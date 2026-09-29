package com.verxus.utrade;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UTrade extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, TradeSession> activeTrades = new HashMap<>();
    private Economy econ = null;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found or Economy plugin missing!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getCommand("trade").setExecutor(this);
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);
        getLogger().info("UTrade enabled securely with Vault Economy.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Usage: /trade <player> or /trade accept");
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            Player requester = null;
            for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
                if (entry.getValue().equals(p.getUniqueId())) {
                    requester = Bukkit.getPlayer(entry.getKey());
                    break;
                }
            }

            if (requester == null || !requester.isOnline()) {
                p.sendMessage(ChatColor.RED + "You have no pending trade requests.");
                return true;
            }

            pendingRequests.remove(requester.getUniqueId());
            TradeSession session = new TradeSession(this, requester, p);
            activeTrades.put(requester.getUniqueId(), session);
            activeTrades.put(p.getUniqueId(), session);
            session.open();
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || target.equals(p)) {
            p.sendMessage(ChatColor.RED + "Player not found or invalid.");
            return true;
        }

        pendingRequests.put(p.getUniqueId(), target.getUniqueId());
        p.sendMessage(ChatColor.GREEN + "Trade request sent to " + target.getName());
        
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        TextComponent message = new TextComponent(ChatColor.GOLD + p.getName() + " wants to trade. ");
        TextComponent acceptBtn = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[CLICK TO ACCEPT]");
        acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept"));
        acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder("Click to accept trade from " + p.getName())
                .color(net.md_5.bungee.api.ChatColor.GREEN).create()));
        
        message.addExtra(acceptBtn);
        target.spigot().sendMessage(message);

        return true;
    }

    public void removeTradeSession(UUID uuid) {
        activeTrades.remove(uuid);
    }

    public TradeSession getTradeSession(UUID uuid) {
        return activeTrades.get(uuid);
    }

    public void logTrade(String p1Name, String p2Name, String p1Offer, String p2Offer) {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdir();
        
        File logFile = new File(dataFolder, "trades.log");
        try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            out.println("[" + timestamp + "] TRADE COMPLETED:");
            out.println("  " + p1Name + " gave: " + p1Offer);
            out.println("  " + p2Name + " gave: " + p2Offer);
            out.println("--------------------------------------------------");
        } catch (IOException e) {
            getLogger().warning("Failed to write to trades.log!");
        }
    }
}