package de.jeffclan.AngelChest;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AngelChest {

	final ItemStack[] armorInv;
	final ItemStack[] storageInv;
	final ItemStack[] extraInv;
	final Inventory overflowInv;
	Block block;
	UUID owner;
	Hologram hologram;
	boolean isProtected;
	long configDuration;
	long taskStart;
	
	
	public AngelChest(UUID owner,Block block, PlayerInventory playerItems, AngelChestPlugin plugin) {
		
		this.owner=owner;
		this.block=block;
		this.isProtected = plugin.getServer().getPlayer(owner).hasPermission("angelchest.protect");

		String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getPlayer(owner).getName());
		overflowInv = Bukkit.createInventory(null, 54, inventoryName);
		createChest(block);
		createHologram(block, plugin);

		// Remove curse of vanishing equipment
		for(ItemStack i : playerItems.getContents()) {
			if(!Utils.isEmpty(i)) {
				if(i.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
					playerItems.remove(i);
				}
			}
		}

		armorInv = playerItems.getArmorContents();
		storageInv = playerItems.getStorageContents();
		extraInv = playerItems.getExtraContents();
		
		AngelChest me = this;
		
		configDuration = plugin.getConfig().getLong("angelchest-duration");
		taskStart = System.currentTimeMillis();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if(plugin.isAngelChest(block)) {
					Utils.destroyAngelChest(block, me, plugin);
					Player player = plugin.getServer().getPlayer(owner);
					if(player != null) {
						player.sendMessage(plugin.messages.MSG_ANGELCHEST_DISAPPEARED);
					}
				}
			}
		}, configDuration * 20);
	}
	
	private void createChest(Block block) {
		block.setType(Material.CHEST);
	}
	
	public void unlock() {
		this.isProtected = false;
	}
	
	public void saveToFile() {
		
	}
	
	public long secondsRemaining() {
		return configDuration - ((System.currentTimeMillis() - taskStart) / 1000);
	}

	public void createHologram(Block block, AngelChestPlugin plugin) {
		String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
		hologram = new Hologram(block, hologramText, plugin);
	}

	public void destroyHologram(AngelChestPlugin plugin) {
		Entity holo;
		for (UUID uuid : hologram.armorStandUUIDs) {
			holo = plugin.getServer().getEntity(uuid);
			if(holo != null)
				holo.remove();
		}
		
		for(ArmorStand armorStand : hologram.armorStands) {
			
			armorStand.remove();
		}
		
		hologram.destroy();
	}
}