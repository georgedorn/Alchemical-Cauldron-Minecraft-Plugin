package com.circuitlocution.alchemicalcauldron;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.material.MaterialData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.material.Dye;


public class Recipe extends Object {
	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");

	protected Material reagent1;
	protected byte reagent1_data; //cast to byte in advance, for speed
	protected Material reagent2;
	protected byte reagent2_data; //cast to byte in advance, for speed
	protected Material reagent3;
	protected byte reagent3_data; //cast to byte in advance, for speed

	protected String product_type; //todo: enum?
	protected String product; //probbly could be a generic data type, but I dunno how to do that

	public Recipe(Map data){ //whatever type we can get for this
		log.info("Got data: " + data);

	}
	
	public String toString(){
		return "" + reagent1.name() + ":" + reagent1_data +
			   "_" + reagent2.name() + ":" + reagent2_data +
			   "_" + reagent3.name() + ":" + reagent3_data;
		
	}

}


