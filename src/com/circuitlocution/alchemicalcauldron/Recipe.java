package com.circuitlocution.alchemicalcauldron;
import java.util.logging.Logger;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.util.config.ConfigurationNode;


public class Recipe extends Object {
	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");

	protected Material reagent1;
	protected byte reagent1_data = -1; //cast to byte in advance, for speed
	protected Material reagent2;
	protected byte reagent2_data = -1; //cast to byte in advance, for speed
	protected Material reagent3;
	protected byte reagent3_data = -1; //cast to byte in advance, for speed
	protected boolean reagent3_consumed = true; //normally eats one of the thing you're hitting with
	protected boolean secret = false; //true if this recipe shouldn't show up in the book.
	
	protected String product_type; //todo: enum?  one of "item", "block" or "mob"
	protected byte product_data = -1;
	protected Material product = null;
	protected int product_quantity = 1;
	protected int reagent3_quantity = 1;
	protected CreatureType product_mob = null;
	
	protected String recipe_description;
	
	
	public String getProductName(){
		if (product_type.equals("mob")){
			return product_mob.getName();
		} else {
			return product.name();
		}
	}
	
	
	public Recipe(ConfigurationNode recipe){ //whatever type we can get for this
		
		product_type = recipe.getString("type", "block");

		reagent3_consumed = recipe.getBoolean("reagent3_consumed", true);
		secret = recipe.getBoolean("secret", false);
		

/**
 * REAGENT 1
 */
		String r1_string = recipe.getString("reagent1", "");
		reagent1 = Material.matchMaterial(r1_string);
		String r1_data = recipe.getString("reagent1_data", null);
		if (r1_data != null){
			recipe_description += r1_data + " ";
			if (reagent1 == Material.INK_SACK){
				reagent1_data = (byte) (15 - DyeColor.valueOf(r1_data).getData());
			} else if (reagent1 == Material.WOOL){
				reagent1_data = (byte) (DyeColor.valueOf(r1_data).getData());
			}
		}
		recipe_description += r1_string;
		if (reagent1 == null){
			log.warning("In recipe for " + product + ", reagent1 isn't valid: " + r1_string);
		}

		recipe_description += " + ";


/**
 * REAGENT 2
 */
		String r2_string = recipe.getString("reagent2", "");
		reagent2 = Material.matchMaterial(r2_string);
		String r2_data = recipe.getString("reagent2_data", null);
		if (r2_data != null){
			recipe_description += r2_data + " ";
			if (reagent2 == Material.INK_SACK){
				reagent2_data = (byte) (15 - DyeColor.valueOf(r2_data).getData());
			} else if (reagent2 == Material.WOOL){
				reagent2_data = (byte) (DyeColor.valueOf(r2_data).getData());
			}
		}
		recipe_description += r2_string;
		if (reagent2 == null){
			log.warning("In recipe for " + product + ", reagent2 isn't valid: " + r2_string);
		}

		recipe_description += " + ";


/**
 * REAGENT 3
 */
		String r3_string = recipe.getString("reagent3", "");
		reagent3 = Material.matchMaterial(r3_string);
		String r3_data = recipe.getString("reagent3_data", null);
		if (r3_data != null){
			recipe_description += r3_data + " ";
			if (reagent3 == Material.INK_SACK){
				reagent3_data = (byte) (15 - DyeColor.valueOf(r3_data).getData());
			} else if (reagent3 == Material.WOOL){
				reagent3_data = (byte) (DyeColor.valueOf(r3_data).getData());
			}
		}

		recipe_description += r3_string;
		if (reagent3 == null){
			log.warning("In recipe for " + product + ", reagent3 isn't valid: " + r3_string);
		}
		
		reagent3_quantity = recipe.getInt("reagent3_quantity", 1);
		if (reagent3_quantity > 1){
			recipe_description += "(x" + reagent3_quantity + ")";
		}
		

		recipe_description += " = ";

		
/**
 * PRODUCT
 */
		String p_data = recipe.getString("product_data", null);
		String p_string = recipe.getString("product", "ERROR");
		if(product_type.equals("block") || product_type.equals("item")){
			product = Material.matchMaterial(p_string);
			if (product == null){
				log.warning("Product of recipe not found for string: " + recipe.getString("product", "<empty>"));
			}
		} else if (product_type.equals("mob")){
			product_mob = CreatureType.fromName(p_string);
			if (product_mob == null){
				log.warning("Product of recipe not found for string: " + recipe.getString("product", "<empty>"));
			}
		}
	
		if (p_data != null){
			recipe_description += p_data + " ";
			if (product == Material.INK_SACK){
				product_data = (byte) (15 - DyeColor.valueOf(p_data).getData());
			} else if (product == Material.WOOL || product_mob == CreatureType.SHEEP){
				product_data = (byte) (DyeColor.valueOf(p_data).getData());
			} 
		}
		
		recipe_description += p_string;

		product_quantity = recipe.getInt("product_quantity", 1);
		if (product_quantity > 1){
			recipe_description += "(x" + product_quantity + ")";
		}
		
		return;
	}
	
	public String toString(){
		return recipe_description;
	}
	
	public String toStringKey(){
		String str = "" + reagent1.name();
		if (reagent1_data > -1){
			str += reagent1_data;
		}
		str += "_" + reagent2.name();
		if (reagent2_data > -1){
			str += reagent2_data;
		}
		str += "_" + reagent3.name();
		if (reagent3_data > -1){
			str += reagent3_data;
		}
		
		return str;
	}

	public boolean isValid(){
		if (reagent1 == null || reagent2 == null || reagent3 == null){
			return false;
		}
		if (product == null && product_mob == null){
			return false;
		}
		return true;
	}
	
}




