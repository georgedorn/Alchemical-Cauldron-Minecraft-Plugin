package com.circuitlocution.alchemicalcauldron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author georgedorn
 */
public class AlchemicalCauldron extends JavaPlugin implements Listener
{

//	private final Logger log = getLogger();
	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");
	private HashMap<String, Recipe> RecipeBook = new HashMap<String, Recipe>();
	private ArrayList<Recipe> recipe_list = new ArrayList<Recipe>();
	public static Permission Permissions;	
	public Boolean use_permissions = false;

	
	private ConfigurationSection mapToConfig(Map<String, Object> map){
		MemoryConfiguration temp = new MemoryConfiguration();
		temp.createSection("monkey", map);
		return temp.getConfigurationSection("monkey");
	}
	
	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	
	public void onEnable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
		setLogLevel();
		saveDefaultConfig(); //this is safe; if the file already exists it won't be overwritten
		buildRecipeBook();
		registerPlugin();
	}

	private void registerPlugin() {
        getServer().getPluginManager().registerEvents(this, this);
	}

	private void setLogLevel() {
		String log_level = getConfig().getString("log_level", "warning").toString().toUpperCase();
		log.setLevel(Level.parse(log_level));
	}

	/**
	 * Parses recipes from the plugin's configuration
	 * @param list
	 * @return
	 */
	private List<Recipe> loadRecipes(List<?> list){
		recipe_list = new ArrayList<Recipe>();
		for (Object current_recipe : list) {
			ConfigurationSection temp_config = mapToConfig((Map<String, Object>) current_recipe);
			Recipe new_recipe = new Recipe(temp_config);
			if (new_recipe.isValid()){
				recipe_list.add(new_recipe);
				log.info("Added recipe: " + new_recipe.toString());
			} else {
				log.info("Invalid recipe for " + new_recipe + ": " + new_recipe.toStringKey());
			}
		}
		log.info("Found " + recipe_list.size() + " alchemy recipes.");
		return recipe_list;
		
	}
	
	private void buildRecipeBook(){
		List<Recipe> recipes = loadRecipes();
		RecipeBook = new HashMap<String, Recipe>();
		for (Recipe recipe : recipes) {
			RecipeBook.put(recipe.toStringKey(), recipe);
		}
		log.info("All RecipeBook keys: " + RecipeBook.keySet().toString());
	}
	
	
	private List<Recipe> loadRecipes() {
		//ConfigurationSection recipe_section = getConfig().getConfigurationSection("recipes");
		//return loadRecipes(recipe_section);
		//return loadRecipes(getConfig().getMapList("recipes"));
		return loadRecipes(getConfig().getList("recipes"));
	}

	private String makeLookupString(Block reagent1, Block reagent2, ItemStack reagent3){
		String str = "" + reagent1.getType().name();
		if (reagent1.getType() == Material.WOOL || reagent1.getType() == Material.INK_SACK){
			str += reagent1.getData();
		}
		str += "_" + reagent2.getType().name();
		if (reagent2.getType() == Material.WOOL || reagent2.getType() == Material.INK_SACK){
			str += reagent2.getData();
		}
		str += "_" + reagent3.getType().name();
		if (reagent3.getType() == Material.WOOL || reagent3.getType() == Material.INK_SACK){
			str += reagent3.getDurability();
		}
		
		return str;
	}
	
	private Recipe findRecipe(Block reagent1, Block reagent2, ItemStack reagent3){
		//because the reagents are Blocks, not Materials, they will also contain
		//their data
		String recipe_key = makeLookupString(reagent1, reagent2, reagent3);
		log.info("Trying to find recipe for key: " + recipe_key);
		return RecipeBook.get(recipe_key);
	}

	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){

		log.info("AlchemicalCauldronPlayerListener.onPlayerItem() called with event: " + event.toString());
		if (event.isCancelled()){
			return;
		}

		Block block = event.getClickedBlock();
		if (block == null){
			return;
		}

		process_event(event);
	}

	
	
	protected void process_event(PlayerInteractEvent event){
		Player p = event.getPlayer();
		if (p == null){
			log.warning("Got event " + event.toString() + " with a null player?");
			return;
		}
		if (!p.hasPermission("alchemicalcauldron.use")){
			return; //player doesn't have permission
		}
		Block reagent2 = event.getClickedBlock();
		World world = reagent2.getWorld();
		Location loc = new Location(world, reagent2.getX(), reagent2.getY(), reagent2.getZ());
		//check to see if the block was just placed in a cauldron
		if (!is_on_cauldron(loc)){
			return;
		}
		Block reagent1 = world.getBlockAt(reagent2.getX(), reagent2.getY()-1, reagent2.getZ());
		ItemStack reagent3 = event.getItem();
		
		Recipe r = findRecipe(reagent1, reagent2, reagent3);
		if (r == null){
			p.sendMessage("Invalid recipe: " + reagent1.getType().toString() + " + " + reagent2.getType().toString() + " + " + reagent3.getType().toString());
			return;
		}
		
		if (r.reagent3_quantity > 1 && reagent3.getAmount() < r.reagent3_quantity){
			p.sendMessage("You need " + r.reagent3_quantity + " " + r.reagent3.name() + " to make " + r.product.name());
			return;
		}

		p.sendMessage("You invoked the recipe to make " + r.product);
		reagent2.setType(Material.AIR);
		reagent1.setType(Material.AIR);
		if (r.product_type.equals("item")){
			if (r.product_data > -1){
				world.dropItemNaturally(loc, new ItemStack(r.product, r.product_quantity, (byte) r.product_data));
			} else {
				world.dropItemNaturally(loc, new ItemStack(r.product, r.product_quantity));
			}
		} else if (r.product_type.equals("block")){
			//drop the block where reagent1 was
			reagent1.setType(r.product);
			if (r.product.equals(Material.MOB_SPAWNER)){
				log.info("Trying to create a mob spawner....");
				CreatureSpawner spawner = (CreatureSpawner)reagent1.getState();
				spawner.setCreatureTypeByName(r.product_mob.getName());
				spawner.update(true);
			}
			else if (r.product_data > -1){
				reagent1.setData(r.product_data);
			}
		} else if (r.product_type.equals("mob")){
			for (int i = 0; i < r.product_quantity; i++) {
				p.getWorld().spawnEntity(loc, r.product_mob);
			}
		}
		if (r.reagent3_consumed){
			log.info("Consuming item in hand");
			if (reagent3.getAmount() > r.reagent3_quantity){
				reagent3.setAmount(reagent3.getAmount() - r.reagent3_quantity);
			} else {
				p.setItemInHand(null);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (!cmd.getName().equalsIgnoreCase("recipes")){
			return false;
		}

		sender.sendMessage("Alchemy Recipes:");
		if (!( sender.hasPermission("alchemicalcauldron.use") ) ){
			return false; //player doesn't have permission
		}
		for (Recipe r: recipe_list){
			if (r.secret == false)
				sender.sendMessage(r.toString());
		}
		return true;
		
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
