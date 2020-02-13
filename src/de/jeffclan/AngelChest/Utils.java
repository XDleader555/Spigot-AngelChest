package de.jeffclan.AngelChest;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Utils {

	public static boolean isEmpty(Inventory inv) {

		if (inv.getContents() == null)
			return true;
		if (inv.getContents().length == 0)
			return true;

		for (ItemStack itemStack : inv.getContents()) {
			if (itemStack == null)
				continue;
			if (itemStack.getAmount() == 0)
				continue;
			if (itemStack.getType().equals(Material.AIR))
				continue;
			return false;
		}

		return true;

	}

	public static boolean isEmpty(ItemStack itemStack) {
		if (itemStack == null)
			return true;
		if (itemStack.getAmount() == 0)
			return true;
		if (itemStack.getType().equals(Material.AIR))
			return true;
		return false;
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public static boolean isWorldEnabled(World world, AngelChestPlugin plugin) {
		
		for(String worldName : plugin.disabledWorlds) {
			if(world.getName().equalsIgnoreCase(worldName)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static Block findSafeBlock(Block playerLoc, AngelChestPlugin plugin) {
		Block fixedAngelChestBlock = playerLoc;
		
		//System.out.println(plugin.getConfig().getInt("max-radius"));
		

		if (!playerLoc.getType().equals(Material.AIR)) {
			List<Block> blocksNearby = Utils.getPossibleChestLocations(playerLoc.getLocation(),
					plugin.getConfig().getInt("max-radius"));
					
			if (blocksNearby.size() > 0) {
				Collections.sort(blocksNearby, new Comparator<Block>() {
					public int compare(Block b1, Block b2) {
						double dist1 = b1.getLocation().distance(playerLoc.getLocation());
						double dist2 = b2.getLocation().distance(playerLoc.getLocation());
						if (dist1 > dist2)
							return 1;
						if (dist2 > dist1)
							return -1;
						return 0;
					}
				});
				
				fixedAngelChestBlock = blocksNearby.get(0);
			}
		}

		return fixedAngelChestBlock;
	}

	/**
	 * Puts everything from source into destination.
	 * 
	 * @param source
	 * @param dest
	 * @return true if everything could be stored, otherwise false
	 */
	public static boolean tryToMergeInventories(AngelChest source, PlayerInventory dest) {
		if(!isEmpty(source.overflowInv))
			return false; // Already applied inventory

		ArrayList<ItemStack> overflow = new ArrayList<ItemStack>();
		ItemStack[] armor_merged = dest.getArmorContents();
		ItemStack[] storage_merged = dest.getStorageContents();
		ItemStack[] extra_merged = dest.getExtraContents();

		// Try to auto-equip armor
		for(int i = 0; i < armor_merged.length; i ++) {
			if(isEmpty(armor_merged[i])) {
				armor_merged[i] = source.armorInv[i];
			} else if(!isEmpty(source.armorInv[i])) {
				overflow.add(source.armorInv[i]);
			}
			source.armorInv[i] = null;
		}

		// Try keep storage layout
		for(int i = 0; i < storage_merged.length; i ++) {
			if(isEmpty(storage_merged[i])) {
				storage_merged[i] = source.storageInv[i];
			} else if(!isEmpty(source.storageInv[i])) {
				overflow.add(source.storageInv[i]);
			}
			source.storageInv[i] = null;
		}

		// Try to merge extra (offhand?)
		for(int i = 0; i < extra_merged.length; i ++) {
			if(isEmpty(extra_merged[i])) {
				extra_merged[i] = source.extraInv[i];
			} else if(!isEmpty(source.extraInv[i])) {
				overflow.add(source.extraInv[i]);
			}
			source.extraInv[i] = null;
		}

		// Apply merged inventories
		dest.setArmorContents(armor_merged);
		dest.setStorageContents(storage_merged);
		dest.setExtraContents(extra_merged);

		// Try to place overflow items into empty storage slots
		HashMap<Integer, ItemStack> unstorable = dest
			.addItem(overflow.toArray(new ItemStack[overflow.size()]));
		source.overflowInv.clear();

		if (unstorable.size() == 0) {
			return true;
		}

		source.overflowInv.addItem(unstorable.values()
			.toArray(new ItemStack[unstorable.size()]));

		return false;
	}

	public static void dropItems(Block block, ItemStack[] invContent) {
		//plugin.getLogger().info("Dropped AngelChest's contents at " + block.getLocation().toString());
		for (ItemStack itemStack : invContent) {
			if (Utils.isEmpty(itemStack))
				continue;
			block.getWorld().dropItem(block.getLocation(), itemStack);
		}
	}

	public static void dropItems(Block block, Inventory inv) {
		dropItems(block, inv.getContents());
		inv.clear();
	}

	public static void destroyAngelChest(Block block, AngelChest angelChest, AngelChestPlugin plugin) {
		

		if (!plugin.isAngelChest(block))
			return;

		block.setType(Material.AIR);
		angelChest.destroyHologram(plugin);

		// drop contents
		dropItems(block, angelChest.armorInv);
		dropItems(block, angelChest.storageInv);
		dropItems(block, angelChest.extraInv);
		dropItems(block, angelChest.overflowInv);

		plugin.angelChests.remove(block);
		block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
	}

	public static void sendDelayedMessage(Player p, String message, long delay, AngelChestPlugin plugin) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (p == null)
					return;
				if (!(p instanceof Player))
					return;
				if (!p.isOnline())
					return;
				p.sendMessage(message);
			}
		}, delay);
	}

	public static ArrayList<AngelChest> getAllAngelChestsFromPlayer(Player p, AngelChestPlugin plugin) {
		ArrayList<AngelChest> angelChests = new ArrayList<AngelChest>();
		for (AngelChest angelChest : plugin.angelChests.values()) {
			if (!angelChest.owner.equals(p.getUniqueId()))
				continue;
			angelChests.add(angelChest);
		}
		return angelChests;
	}
	
	 public static List<Block> getNearbyBlocks(Location location, int radius) {
	        List<Block> blocks = new ArrayList<Block>();
	        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
	            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
	                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
	                   blocks.add(location.getWorld().getBlockAt(x, y, z));
	                }
	            }
	        }
	        return blocks;
	    }
	
	// Only allow chests to spawn in these types
	public static List<Material> safeBlockTypes = Arrays.asList(Material.AIR);

	// Prevent chests from spawning ontop of these types
	public static List<Material> notSolidTypes = Arrays.asList(Material.AIR, Material.GRASS_PATH);

	 public static List<Block> getPossibleChestLocations(Location location, int radius) {
	        List<Block> blocks = new ArrayList<Block>();
	        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
	            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
	                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
						Block block = location.getWorld().getBlockAt(x,y,z);
						Block oneBelow = location.getWorld().getBlockAt(x,y - 1,z);
						if(safeBlockTypes.contains(block.getType())
								&& !notSolidTypes.contains(oneBelow.getType())
								&& y > 0) {
	                		blocks.add(block);
	                	}
	                }
	            }
	        }
	        return blocks;
	    }

	public static String locationToString(Block block) {
		if(block == null) {
			return "<none>";
		}
		return String.format("%d, %d, %d (%s)", block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	}
	
	// from sk89q
	public static String getCardinalDirection(Player player) {
		double rotation = player.getLocation().getYaw() % 360;
		if (rotation < 0) {
			rotation += 360.0;
		}
		 if (0 <= rotation && rotation < 22.5) {
			return "S";
		} else if (22.5 <= rotation && rotation < 67.5) {
			return "SW";
		} else if (67.5 <= rotation && rotation < 112.5) {
			return "W";
		} else if (112.5 <= rotation && rotation < 157.5) {
			return "NW";
		} else if (157.5 <= rotation && rotation < 202.5) {
			return "N";
		} else if (202.5 <= rotation && rotation < 247.5) {
			return "NE";
		} else if (247.5 <= rotation && rotation < 292.5) {
			return "E";
		} else if (292.5 <= rotation && rotation < 337.5) {
			return "SE";
		} else if (337.5 <= rotation && rotation < 360.0) {
			return "S";
		} else {
			return null;
		}
	}
}