package com.circuitlocution.alchemicalcauldron;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;

/**
 * 
 * @author georgedorn
 */
public class AlchemicalCauldron extends JavaPlugin
{
	private final AlchemicalCauldronPlayerListener playerListener = new AlchemicalCauldronPlayerListener(this);
	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");
	
	private HashMap<String, Recipe> RecipeBook = new HashMap<String, Recipe>();
	
	
	public AlchemicalCauldron(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		folder.mkdirs();

	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
		createConfigIfNotExists();
		setLogLevel();
		buildRecipeBook();
		registerPlugin();
	}

	private void registerPlugin() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
	}

	private void setLogLevel() {
		String log_level = getConfiguration().getString("log_level", "debug").toString().toUpperCase();
		log.info("Setting AlchemicalCauldron logging to " + log_level);
		log.setLevel(Level.parse(log_level));
	}

	private void createConfigIfNotExists() {
		//reload config
		File yml = new File(getDataFolder(), "config.yml");
		if (!yml.exists())
		{
			try
			{
				log.log(Level.INFO, "Creating new log file for " + getDescription().getName());
				yml.createNewFile();
			}
			catch (IOException ex)
			{
				log.log(Level.SEVERE, "Log file could not be created!");
			}
		}
	}

	/**
	 * Parses recipes from the plugin's configuration
	 * @param list
	 * @return
	 */
	private List<Recipe> loadRecipes(List<ConfigurationNode> list){
		log.log(Level.INFO, "Got recipes:", list);
		ArrayList<Recipe> recipe_list = new ArrayList<Recipe>();
		for (ConfigurationNode current_recipe : list) {
			log.log(Level.INFO, "Looking at recipe: " + current_recipe);
			recipe_list.add(new Recipe(current_recipe));
		}
		
		return recipe_list;
		
	}
	
	private void buildRecipeBook(){
		List<Recipe> recipes = loadRecipes();
		RecipeBook = new HashMap<String, Recipe>();
		for (Recipe recipe : recipes) {
			RecipeBook.put(recipe.toString(), recipe);
		}
	}
	
	
	private List<Recipe> loadRecipes() {
		return loadRecipes(getConfiguration().getNodeList("recipes", null));
	}

	private Recipe findRecipe(Block reagent1, Block reagent2, ItemStack reagent3){
		//because the reagents are Blocks, not Materials, they will also contain
		//their data
		String recipe_key = "" + reagent1.getType().name() + ":" + reagent1.getData() +
		                    "_" + reagent2.getType().name() + ":" + reagent2.getData() +
		                    "_" + reagent3.getType().name() + ":" + reagent3.getData();
		
		return RecipeBook.get(recipe_key);
		
	}
	
	
	protected void process_event(PlayerItemEvent event){
		Block reagent2 = event.getBlockClicked();
		World world = reagent2.getWorld();
		Location loc = new Location(world, reagent2.getX(), reagent2.getY(), reagent2.getZ());
		//check to see if the block was just placed in a cauldron
		if (!is_on_cauldron(loc)){
			return;
		}
		Block reagent1 = world.getBlockAt(reagent2.getX(), reagent2.getY()-1, reagent2.getZ());
		ItemStack reagent3 = event.getItem();
		Player p = event.getPlayer();
		
		Recipe r = findRecipe(reagent1, reagent2, reagent3);
		if (r == null){
			p.sendMessage("Invalid recipe.");
			return;
		}
		
		p.sendMessage("You invoked a recipe that should produce a product: " + r.product);

	}
	
	protected boolean is_on_cauldron(Location loc){
		/**
		 * Checks to see if the location is one block above an obsidian cauldron.
		 * Cauldrons look like this:
		 * Top:  Mid:  Bottom:
		 * ***   ooo   ooo
		 * *2*   o1o   ooo
		 * ***   ooo   ooo
		 * 
		 * Key:	* = anything, probably air
		 * 		2 = reagent 2, a placed block
		 * 		1 = reagent 1, a placed block
		 * 		o = obsidian
		 */
		int y = loc.getBlockY(); // y is used for height in minecraft
		if (y <2 ){
			return false;  //can't place a cauldron inside bedrock; also crashing is bad
		}
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
	
		World world = loc.getWorld();
		
		//begin ugly hard-coded checks.
		//maybe there's a more graceful way to do this, but this should be fast
		
		//two blocks under; this should be the fastest disqualifier in most cases
		if (world.getBlockAt(x, y-2, z).getType() != Material.OBSIDIAN){
			return false;
		}
		
		//around the ring for tier2
		if (world.getBlockAt(x-1, y-1, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x, y-1, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-1, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x-1, y-1, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x, y-1, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-1, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
	
		//We don't check x,y-1,z, because that should be the location of reagent 1
		if (world.getBlockAt(x-1, y-1, z).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-1, z).getType() != Material.OBSIDIAN){
			return false;
		}
		
		//Bottom tier
		if (world.getBlockAt(x-1, y-2, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x, y-2, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-2, z-1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x-1, y-2, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x, y-2, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-2, z+1).getType() != Material.OBSIDIAN){
			return false;
		}
	
		//we don't check x,y-2,z because we already did that
		
		if (world.getBlockAt(x-1, y-2, z).getType() != Material.OBSIDIAN){
			return false;
		}
		if (world.getBlockAt(x+1, y-2, z).getType() != Material.OBSIDIAN){
			return false;
		}
		
		//if we got here, it's a cauldron
		return true;
	}


}
