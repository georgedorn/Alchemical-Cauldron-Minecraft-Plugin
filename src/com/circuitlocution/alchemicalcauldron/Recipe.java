package com.circuitlocution.alchemicalcauldron;
import java.util.logging.Logger;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.util.config.ConfigurationNode;


public class Recipe extends Object {
	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");

	protected Material reagent1;
	protected byte reagent1_data = -1; //cast to byte in advance, for speed
	protected Material reagent2;
	protected byte reagent2_data = -1; //cast to byte in advance, for speed
	protected Material reagent3;
	protected byte reagent3_data = -1; //cast to byte in advance, for speed

	protected String product_type; //todo: enum?
	protected String product; //probbly could be a generic data type, but I dunno how to do that

	public Recipe(ConfigurationNode recipe){ //whatever type we can get for this
		
		log.info("Got data: " + recipe);
		
		product_type = recipe.getString("type", "block");
		product = recipe.getString("product", "ERROR");

		reagent1 = Material.matchMaterial(recipe.getString("reagent1", "ERROR"));
		reagent2 = Material.matchMaterial(recipe.getString("reagent2", "ERROR"));
		reagent3 = Material.matchMaterial(recipe.getString("reagent3", "ERROR"));

		String r1_data = recipe.getString("reagent1_data", null);
		String r2_data = recipe.getString("reagent2_data", null);
		String r3_data = recipe.getString("reagent3_data", null);
		
		if (r1_data != null){
			if (reagent1 == Material.INK_SACK){
				reagent1_data = (byte) (15 - DyeColor.valueOf(r1_data).getData());
			} else if (reagent1 == Material.WOOL){
				reagent1_data = (byte) (15 - DyeColor.valueOf(r1_data).getData());
			}
			
		}
		if (r2_data != null){
			if (reagent2 == Material.INK_SACK){
				reagent2_data = (byte) (15 - DyeColor.valueOf(r2_data).getData());
			} else if (reagent2 == Material.WOOL){
				reagent2_data = (byte) (15 - DyeColor.valueOf(r2_data).getData());
			}
			
		}
		if (r3_data != null){
			if (reagent3 == Material.INK_SACK){
				reagent3_data = (byte) (15 - DyeColor.valueOf(r3_data).getData());
			} else if (reagent3 == Material.WOOL){
				reagent3_data = (byte) (15 - DyeColor.valueOf(r3_data).getData());
			}
			
		}
		
		return;
	}
	
	public String toString(){
		return "" + reagent1.name() + ":" + reagent1_data +
			   "_" + reagent2.name() + ":" + reagent2_data +
			   "_" + reagent3.name() + ":" + reagent3_data;
		
	}

}




