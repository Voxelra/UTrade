package com.verxus.utrade;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TradeListener implements Listener {
    private final UTrade plugin;

    public TradeListener(UTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        TradeSession session = plugin.getTradeSession(p.getUniqueId());
        if (session == null) return;

        if (e.isShiftClick() && e.getRawSlot() >= session.getInventory().getSize()) {
            e.setCancelled(true);
            return;
        }

        int rawSlot = e.getRawSlot();
        boolean isTopInventory = rawSlot < session.getInventory().getSize();

        if (isTopInventory) {
            
            if (rawSlot == 36 || rawSlot == 44) {
                e.setCancelled(true);
                if (rawSlot == 36 && p.equals(session.getP1())) {
                    double amount = getMoneyAmountFromClick(e);
                    if (amount != 0) session.modifyMoney(p, amount);
                } else if (rawSlot == 44 && p.equals(session.getP2())) {
                    double amount = getMoneyAmountFromClick(e);
                    if (amount != 0) session.modifyMoney(p, amount);
                }
                return;
            }

            if (rawSlot == 37 || rawSlot == 43) {
                e.setCancelled(true);
                if (rawSlot == 37 && p.equals(session.getP1())) {
                    int amount = getXpAmountFromClick(e);
                    if (amount != 0) session.modifyXp(p, amount);
                } else if (rawSlot == 43 && p.equals(session.getP2())) {
                    int amount = getXpAmountFromClick(e);
                    if (amount != 0) session.modifyXp(p, amount);
                }
                return;
            }

            if (rawSlot == 45 && p.equals(session.getP1())) {
                e.setCancelled(true);
                session.toggleReady(p);
                return;
            }
            if (rawSlot == 53 && p.equals(session.getP2())) {
                e.setCancelled(true);
                session.toggleReady(p);
                return;
            }

            if (p.equals(session.getP1()) && !session.P1_SLOTS.contains(rawSlot)) {
                e.setCancelled(true);
            } else if (p.equals(session.getP2()) && !session.P2_SLOTS.contains(rawSlot)) {
                e.setCancelled(true);
            } else {
                session.resetReady();
            }
        }
    }

    private double getMoneyAmountFromClick(InventoryClickEvent e) {
        if (e.isShiftClick() && e.isLeftClick()) return 1000.0;
        if (e.isShiftClick() && e.isRightClick()) return -1000.0;
        if (e.isLeftClick()) return 100.0;
        if (e.isRightClick()) return -100.0;
        return 0.0;
    }

    private int getXpAmountFromClick(InventoryClickEvent e) {
        if (e.isShiftClick() && e.isLeftClick()) return 10;
        if (e.isShiftClick() && e.isRightClick()) return -10;
        if (e.isLeftClick()) return 1;
        if (e.isRightClick()) return -1;
        return 0;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        TradeSession session = plugin.getTradeSession(p.getUniqueId());
        if (session == null) return;

        for (int slot : e.getRawSlots()) {
            if (slot < session.getInventory().getSize()) {
                if (p.equals(session.getP1()) && !session.P1_SLOTS.contains(slot)) {
                    e.setCancelled(true);
                    return;
                }
                if (p.equals(session.getP2()) && !session.P2_SLOTS.contains(slot)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        session.resetReady();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        TradeSession session = plugin.getTradeSession(p.getUniqueId());
        if (session != null) session.abort(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TradeSession session = plugin.getTradeSession(e.getPlayer().getUniqueId());
        if (session != null) session.abort(e.getPlayer());
    }
}