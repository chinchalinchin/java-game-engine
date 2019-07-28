package subterra.engine.filehandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import subterra.engine.GameWorld;
import subterra.engine.cinematics.SceneTriggers;
import subterra.engine.imagehandling.menu.GameMenu;
import subterra.geometry.*;
import subterra.interfaces.*;
import subterra.library.*;

public class FileHandler {
	
	/**@FileHandler
	 * 
	 * @author chinchalinchin
	 * 
	 * @description FileHandler contains the relevant keys for parsing world data.
	 * The world data key can be found in the /subterra/world/ source folder.
	 * Essentially, FileHandler reads in a markup language and populates
	 * ArrayList<Sprite>'s and other arrays with information regarding the 
	 * state of the world.  
	 * 
	 * This is where all in-game objects are instantiated. All constructor calls
	 * to Sprite objects is made in this class, with the exception of projectiles
	 * which are created in-game due to user interaction.
	 * 
	 * Object properties are either hard-coded, such as LightSource, ShovelTile, 
	 * TreasureChest and Switch, or read-in through a configuration file, such as
	 * Villain and NPC. 
	 * 
	 * @field outsideOne/insideOne/outsideTwo/insideTwo : BufferedImage. Frames
	 * that represent the different possible backgrounds to be rendered.
	 * Frames are determined in game depending on the user's combination of OneOrTwo 
	 * and InOrOut boolean triggers.
	 * 
	 * @field villains/nps/doors/treasures/hitboxes/shoveltiles: ArrayList<Sprite>.
	 * Arraylists containing all the information regarding in-game objects. See
	 * GameWorld documentation for more details.
	 * 
	 * @field itempouch : int[][].
	 * 
	 * @field equipmenttriggers : boolean[]. Boolean array that represents the
	 * types of equipment the user has encountered on his journey.
	 * 
	 * @field dialogue : ArrayList<String>. Array of all the dialogue in-game,
	 * organized into lines. The key for parsing data can be found in the 
	 *  /subterra/words/ source folder.
	 */
	
		//World Generation Key
	private static final String commentString = "***",
								worldframe = "wf", worldlayer = "wl",
								numlayers = "ly", plotphase = "pp",
								l1box = "w1", l2box = "w2", 
								l3box = "w3", l4box = "w4", l5box = "w5",
								portal = "po", door = "do", 
								newhero = "ho", npc = "np", 
								villain = "vi", light = "li",
								treasure = "tc", heroitem = "hi",
								equipment = "eq", shovel_tile = "st",
								pressure_plate = "pl", gate = "gt",
								switch_sprite = "sw", hack_box = "hb",
								barrel = "ba", crate = "cr",
								save_crystal = "sv", book = "bo",
								sign = "si",
								effect = "ef";
		//Singleton Object Attributes
	@SuppressWarnings("unused")
	private static final int Sign_INDEX = 0, Book_INDEX = 1, Crystal_INDEX = 2,
							 Crate_INDEX = 3, Barrel_INDEX = 4, Lantern_INDEX = 5,
							 SIN_SPRITES = 6,
							 SIN_WIDTH_INDEX = 0, SIN_HEIGHT_INDEX = 1, 
							 SIN_COL_WID_INDEX = 2, SIN_COL_HGT_INDEX = 3,
							 SIN_ATTRIBUTES= 4;		 
		//Binary Object Attribute Indices
	@SuppressWarnings("unused")
	private static final int ShovelTile_INDEX = 0, TreasureChest_INDEX = 1, Switch_INDEX = 2, 
							 Hackbox_INDEX = 3, PressurePlate_INDEX = 4, Vrain_INDEX = 5, 
							 Gate_INDEX = 6,
							 BIN_SPRITES = 7,
							 BIN_WIDTH_INDEX = 0, BIN_HEIGHT_INDEX = 1,
							 BIN_ATTRIBUTES = 2;
	//File paths and characters
	private static final String cdir = System.getProperty("user.dir"),
							   sep = System.getProperty("file.separator"),
							   world_config_path = cdir + sep + "world_config",
							   collision_config_path = world_config_path + sep + "sprite_collision_config" + sep,
							   attribute_config_path = world_config_path + sep + "sprite_attribute_config" + sep,
							   save_path = cdir + sep + "saves",
						 	   villain_config_path = attribute_config_path + sep + "villain_attribute_config.txt",
						 	   npc_config_path = attribute_config_path + sep + "npc_attribute_config.txt",
						 	   hero_immutable_config_path = attribute_config_path + sep + "hero_immutable_attribute_config.txt",
						 	   hero_variable_config_path = attribute_config_path + sep + "hero_variable_attribute_config.txt",
						 	   bin_sprite_config_path = attribute_config_path + sep + "binary_sprite_attribute_config.txt",
						 	   sin_sprite_config_path = attribute_config_path + sep + "singleton_sprite_attribute_config.txt",
						 	   light_config_path = attribute_config_path + sep +"light_source_attribute_config.txt",
						 	   effect_config_path = attribute_config_path + sep + "effect_attribute_config.txt",
						 	   sprite_sheet_collision_path = collision_config_path + "sheet_sprite_collision_box_config.txt",
							   bin_sprite_collision_path = collision_config_path + "binary_sprite_collision_box_config.txt",
						 	   sprite_sheet_attack_path = collision_config_path + "sheet_sprite_attack_box_config.txt", 
						 	   spc = " ";
	//Class fields
	private ArrayList<Sprite> villains, npcs, 
							  doors, portals, hitboxes, 
							  hackboxes, shoveltiles, switches, treasures, 
							  	pressureplates, gates,
							  lights, effects,
							  books, signs, crystals, barrels, crates;
	private int[][] itempouch;
	private boolean[] equipmenttriggers;
	private SceneTriggers scenes;
	private Sprite hero;
	private Dialogue words;
	private int wlayer, layers;
	private String filepath, wframe;
	private boolean debug;
	private int phase;
	private int[] heroImmutableAttributes, heroVariableAttributes;
	private int[][] singletonAttributes, binaryAttributes,
					lightAttributes, effectAttributes,
					npcAttributes, villainAttributes;
	private int[] sheetCollisions;
	private int[][] binaryCollisions;
	private int[][] sheetCombat;
	
	public FileHandler(String world, boolean db){   
		filepath = save_path+sep+world+".txt";
		debug = db;
		init();
		configureSpriteAttributes();
		configureSpriteCollisions();
		configureCombat();
		parseWorld();
	}
	
	private void configureCombat(){
		sheetCombat = new int[Sprite.DIRECTIONS][Sprite.ATTACK_ATTRIBUTES];
		if(debug) { System.out.println("Configuring Sheet Sprite Combat..."); }
		String line; int counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(sprite_sheet_attack_path));
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					sheetCombat[counter][Sprite.X_ATK] = Integer.parseInt(line.split("\\s")[Sprite.X_ATK+1]);
					sheetCombat[counter][Sprite.Y_ATK] = Integer.parseInt(line.split("\\s")[Sprite.Y_ATK+1]);
					sheetCombat[counter][Sprite.ATK_WIDTH] = Integer.parseInt(line.split("\\s")[Sprite.ATK_WIDTH+1]);
					sheetCombat[counter][Sprite.ATK_HEIGHT] = Integer.parseInt(line.split("\\s")[Sprite.ATK_HEIGHT+1]);
					counter++;
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading : " + sprite_sheet_attack_path); }
	}
	
	private void configureSpriteCollisions(){
		sheetCollisions = new int[Sprite.COLLISION_ATTRIBUTES];
		binaryCollisions = new int[BIN_SPRITES][Sprite.COLLISION_ATTRIBUTES];
		if(debug) { System.out.println("Configuring Sheet Sprite Collisions..."); }
		String line;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(sprite_sheet_collision_path));
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					sheetCollisions[Sprite.OFFSET_X] = Integer.parseInt(line.split("\\s")[Sprite.OFFSET_X]);
					sheetCollisions[Sprite.OFFSET_Y] = Integer.parseInt(line.split("\\s")[Sprite.OFFSET_Y]);
					sheetCollisions[Sprite.COLLISION_WIDTH] = Integer.parseInt(line.split("\\s")[Sprite.COLLISION_WIDTH]);
					sheetCollisions[Sprite.COLLISION_HEIGHT] = Integer.parseInt(line.split("\\s")[Sprite.COLLISION_HEIGHT]);
				}
			}
			bReader.close(); 
		}
		catch(IOException ie) { System.out.println("Error loading: " + sprite_sheet_collision_path); }
		try{
			int counter = 0;
			BufferedReader bReader = new BufferedReader(new FileReader(bin_sprite_collision_path));
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					binaryCollisions[counter][Sprite.OFFSET_X] = Integer.parseInt(line.split("\\s")[Sprite.OFFSET_X]);
					binaryCollisions[counter][Sprite.OFFSET_Y] = Integer.parseInt(line.split("\\s")[Sprite.OFFSET_Y]);
					binaryCollisions[counter][Sprite.COLLISION_WIDTH] = Integer.parseInt(line.split("\\s")[Sprite.COLLISION_WIDTH]);
					binaryCollisions[counter][Sprite.COLLISION_HEIGHT] = Integer.parseInt(line.split("\\s")[Sprite.COLLISION_HEIGHT]);
					counter++;
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + bin_sprite_collision_path); }
	}
	
	private void configureSpriteAttributes(){
		
		//VILLAIN CONFIGURATION
		villainAttributes = new int[Villain.types][Villain.attributes];
		if(debug) { System.out.println("Configuring Villain attributes..."); }
		String line; int counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(villain_config_path));
			while((line=bReader.readLine()) != null) { 
				if(line != null) { 	
					if(!line.split("\\s")[0].equals(commentString)) { 
						for(int i = 0; i < Villain.attributes; i++){
							villainAttributes[counter][i] = Integer.parseInt(line.split("\\s")[i+1]);
						}
						counter++;
					}
				}
			}
			bReader.close();
		} 
		catch(IOException ie){ System.out.println("Error loading:" + villain_config_path); }
	
		//NPC CONFIGURATION
		npcAttributes = new int[NPC.types][NPC.attributes];
		if(debug) { System.out.println("Configuring NPC attributes..."); }
		line = null; counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(npc_config_path)); 
			while((line = bReader.readLine()) != null){
				if(line != null) { 
					if(!line.split("\\s")[0].equals(commentString)){ 
						for(int i = 0; i < NPC.attributes; i++){
							npcAttributes[counter][i] = Integer.parseInt(line.split("\\s")[i+1]);
						}
						counter++;
					}
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + npc_config_path);}
	
		//HERO CONFIGURATION
		heroImmutableAttributes = new int[Hero.immutable_attributes];
		if(debug) { System.out.println("Configuring Hero Immutable Attributes..."); }
		line = null;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(hero_immutable_config_path)); 
			while((line=bReader.readLine())!= null){
				if(line != null) { 
					if(!line.split("\\s")[0].equals(commentString)) { 
						for(int i = 0; i < Hero.immutable_attributes; i++){
							heroImmutableAttributes[i] = Integer.parseInt(line.split("\\s")[i]);
						}
					}
				}
			}
			bReader.close();
		} 
		catch(IOException ie) { System.out.println("Error loading: " + hero_immutable_config_path); }
		heroVariableAttributes = new int[Hero.variable_attributes];
		if(debug) { System.out.println("Configuring Hero Variable Attributes..."); }
		line = null;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(hero_variable_config_path));
			while((line = bReader.readLine()) != null){
				if(line!= null) { 
					if(!line.split("\\s")[0].equals(commentString)) { 
						for(int i = 0; i < Hero.variable_attributes; i++){
							heroVariableAttributes[i] = Integer.parseInt(line.split("\\s")[i]);
						}
					} 
				}
			}
			bReader.close();
		} 
		catch(IOException ie) { System.out.println("Error loading: " + hero_variable_config_path); }
		
		//SINGLETON OBJECT CONFIGURATION
		singletonAttributes = new int[SIN_SPRITES][SIN_ATTRIBUTES];
		if(debug){ System.out.println("Configuring Singleton Attributes..."); }
		line = null; counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(sin_sprite_config_path));
			while((line = bReader.readLine()) != null){
				if(line != null){
					if(!line.split("\\s")[0].equals(commentString)){ 
						for(int i = 0; i < SIN_ATTRIBUTES; i++){
							singletonAttributes[counter][i] = Integer.parseInt(line.split("\\s")[i]);
						}
						counter++;
					}
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + sin_sprite_config_path); }
		
		//BINARY OBJECT CONFIGURATION
		binaryAttributes = new int[BIN_SPRITES][BIN_ATTRIBUTES];
		if(debug) { System.out.println("Configuring Binary Attributes..."); }
		line = null; counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(bin_sprite_config_path));
			while((line = bReader.readLine()) != null){ 
				if(line != null) { 
					if(!line.split("\\s")[0].equals(commentString)){ 
						for(int i = 0; i < BIN_ATTRIBUTES; i++){
							binaryAttributes[counter][i] = Integer.parseInt(line.split("\\s")[i]);
						}
						counter++;
					} 
				} 
			}
			bReader.close();
		} 
		catch(IOException ie){ System.out.println("Error loading: " + bin_sprite_config_path); }
		
		//LIGHTSOURCE CONFIGURATION
		lightAttributes = new int[LightSource.types][LightSource.attributes];
		if(debug) { System.out.println("Configuring Light Source Attributes..."); }
		ArrayList<String> info = new ArrayList<String>(); line = null;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(light_config_path));
			while((line = bReader.readLine()) != null){
				if(line != null) { if(!line.split("\\s")[0].equals(commentString)) { info.add(line); } }
			}
			bReader.close();
		} 
		catch(IOException ie){ System.out.println("Error loading :" + light_config_path); }
		counter = 0;
		for(String bit : info){
			if(bit != null && !(bit.split("\\s")[0].equals(commentString))){
				String[] control = bit.split("\\s");
				for(int j = 1; j < control.length; j++){
					lightAttributes[Integer.parseInt(control[0])][j-1] = Integer.parseInt(control[j]);
				}
			}
		}
		
		//EFFECT CONFIGURATION
		effectAttributes = new int[Effect.types][Effect.attributes];
		if(debug) { System.out.println("Configuring Effect Attributes..."); }
		info = new ArrayList<String>(); line = null; counter = 0;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(effect_config_path));
			while((line = bReader.readLine()) != null){
				if(line != null ){ if(!line.split("\\s")[0].equals(commentString)){ info.add(line);}}
			}
			bReader.close();
		} 
		catch(IOException ie) { System.out.println("Error loading :" + effect_config_path); }
		for(String bit : info){
			if(bit != null && !(bit.split("\\s")[0].equals(commentString))){
				String[] control = bit.split("\\s");
				effectAttributes[counter][Effect.WIDTH_INDEX] = Integer.parseInt(control[0]);
				effectAttributes[counter][Effect.HEIGHT_INDEX] = Integer.parseInt(control[1]);
				effectAttributes[counter][Effect.SUB_DEP_INDEX] = Integer.parseInt(control[2]);
				counter++;
			}
		}
	}
	
	private void init(){  
			//Sheet Sprites
		npcs = new ArrayList<Sprite>();
		villains = new ArrayList<Sprite>();
			//Configuration Sprites
		doors = new ArrayList<Sprite>();
		hitboxes = new ArrayList<Sprite>();
		portals = new ArrayList<Sprite>();
			//Singleton Sprites
		barrels = new ArrayList<Sprite>();
		crates = new ArrayList<Sprite> ();
		books = new ArrayList<Sprite>();
		signs = new ArrayList<Sprite>();
		crystals = new ArrayList<Sprite>();
			//Binary Sprites
		hackboxes = new ArrayList<Sprite>();
		treasures = new ArrayList<Sprite>();
		shoveltiles = new ArrayList<Sprite>();
		pressureplates = new ArrayList<Sprite>();
		gates = new ArrayList<Sprite>();
		switches = new ArrayList<Sprite>();
			//Other
		lights = new ArrayList<Sprite>();
		effects = new ArrayList<Sprite>();
		itempouch = new int[Item.items][2];
		equipmenttriggers = new boolean[GameMenu.equipmentselections];
		for(int i = 0; i < Item.items; i++) { itempouch[i][0]= i; itempouch[i][1] = 0; }
		for(int i = 0; i < GameMenu.equipmentselections; i++){ equipmenttriggers[i] = false; }
	}
	
	public void portalInit(){
			//Sheet Sprites
		villains = new ArrayList<Sprite>();
		npcs = new ArrayList<Sprite>();
			//Configuration Sprites
		hitboxes = new ArrayList<Sprite>();
		portals = new ArrayList<Sprite>();
		doors = new ArrayList<Sprite>();
			//Singleton Sprites
		barrels = new ArrayList<Sprite>();
		crates = new ArrayList<Sprite> ();
		books = new ArrayList<Sprite>();
		signs = new ArrayList<Sprite>();
		crystals = new ArrayList<Sprite>();
			//Binary Sprites
		hackboxes = new ArrayList<Sprite>();
		treasures = new ArrayList<Sprite>();
		shoveltiles = new ArrayList<Sprite>();
		pressureplates = new ArrayList<Sprite>();
		gates = new ArrayList<Sprite>();
		switches = new ArrayList<Sprite>();
			//Other
		lights = new ArrayList<Sprite>();
		effects = new ArrayList<Sprite>();
	}
	
	public void parseWorld(){ 
		String line = null;
		Boolean flag = true;
		ArrayList<String> info = new ArrayList<String>();
		
		//START: Reading in GameWorld file
		if(debug) { System.out.println("Parsing File..."); }
		do{
			try{
				BufferedReader bReader = new BufferedReader(new FileReader(filepath));
				while((line = bReader.readLine())!=null){ 
					if(line != null) {  if(!line.substring(0,3).equals(commentString)) { info.add(line); } }
				}
				bReader.close();
				flag = false; 
			}
			catch(IOException ie){ }
		}while(flag);
		//END: Reading in GameWorld file
		
		//START: Iterate over Gameworld file
		for(String datum : info){
			String control = null;
			String[] results;
			if(!datum.isEmpty()) {  control = datum.substring(0,2);}
			//START: Conditional On (control)
			if(control != null && !(control.equals(commentString))){
				//START: Switch On (control)
				results = datum.substring(3).split("\\s");
				switch(control){
					case worldframe:
						wframe = results[0];
						break;
						
					case worldlayer:
						wlayer = Integer.parseInt(results[0]);
						break;
						
					case numlayers:
						layers = Integer.parseInt(results[0]);
						break;
						
					case plotphase:
						phase = Integer.parseInt(results[0]);
						words = new Dialogue(Integer.parseInt(results[0]));
						scenes = new SceneTriggers(Integer.parseInt(results[0]));
						break;
						
					case newhero:
						hero = new Hero(new Point(Double.parseDouble(results[0]), 
												  Double.parseDouble(results[1])));
						Hero he = (Hero) hero;
						he.configureImmutable( heroImmutableAttributes[Hero.I_WLKSPD_INDEX], 
												heroImmutableAttributes[Hero.I_RUNSPD_INDEX], 
												heroImmutableAttributes[Hero.I_LANT_INDEX], 
												heroImmutableAttributes[Hero.I_STUN_INDEX],
												heroImmutableAttributes[Hero.I_ATK_BOUNCE_INDEX],
												heroImmutableAttributes[Hero.I_ATK_TRIGGER_INDEX]);
						he.configureVariable(heroVariableAttributes[Hero.V_TOTHP_INDEX], 
												heroVariableAttributes[Hero.V_CURHP_INDEX], 
												heroVariableAttributes[Hero.V_ATK_INDEX], 
												heroVariableAttributes[Hero.V_DEF_INDEX], 
												heroVariableAttributes[Hero.V_LINNERRAD_INDEX],
												heroVariableAttributes[Hero.V_LOUTERRAD_INDEX]);
						he.configureCollisions(sheetCollisions[Sprite.OFFSET_X], 
												sheetCollisions[Sprite.OFFSET_Y], 
												sheetCollisions[Sprite.COLLISION_WIDTH],
												sheetCollisions[Sprite.COLLISION_HEIGHT]);
						he.configureCombat(sheetCombat[Sprite.DIR_left], 
											sheetCombat[Sprite.DIR_right], 
											sheetCombat[Sprite.DIR_up], 
											sheetCombat[Sprite.DIR_down]);
						he.setType(heroVariableAttributes[Hero.V_TYPE_INDEX]);
						break;
						
					case heroitem:
						itempouch[Integer.parseInt(results[0])][1]= Integer.parseInt(results[1]);
						break;
					
					case equipment:
						if(Integer.parseInt(results[0]) == 0) { equipmenttriggers[GameMenu.lantern] = false; }
						else if(Integer.parseInt(results[0]) ==1) { equipmenttriggers[GameMenu.lantern] = true; }
						
						if(Integer.parseInt(results[1]) == 0) { equipmenttriggers[GameMenu.shovel] = false; }
						else if(Integer.parseInt(results[1])==1) { equipmenttriggers[GameMenu.shovel] = true; }
						
						if(Integer.parseInt(results[2]) == 0) { equipmenttriggers[GameMenu.crossbow] = false; }
						else if (Integer.parseInt(results[2]) == 1) { equipmenttriggers[GameMenu.crossbow] = true;}
						
						if(Integer.parseInt(results[3]) == 0) { equipmenttriggers[GameMenu.shield] = false; }
						else if (Integer.parseInt(results[3]) == 1) { equipmenttriggers[GameMenu.shield] = true;}
						
						if(Integer.parseInt(results[4]) == 0) { equipmenttriggers[GameMenu.hammer] = false; }
						else if (Integer.parseInt(results[4]) == 1) { equipmenttriggers[GameMenu.hammer] = true; }
						
						if(Integer.parseInt(results[5]) == 0) { equipmenttriggers[GameMenu.rocket_skates] = false; }
						else if (Integer.parseInt(results[5]) == 1) { equipmenttriggers[GameMenu.rocket_skates] = true; }
						
						if(Integer.parseInt(results[6]) == 0) { equipmenttriggers[GameMenu.visor] = false; }
						else if (Integer.parseInt(results[6]) == 1) { equipmenttriggers[GameMenu.visor] = true; }
						
						if(Integer.parseInt(results[7]) == 0) { equipmenttriggers[GameMenu.axe] = false; }
						else if (Integer.parseInt(results[7]) == 1) { equipmenttriggers[GameMenu.axe] = true; }
						
						if (Integer.parseInt(results[8]) == 0) { equipmenttriggers[GameMenu.blank1] = false; }
						else if (Integer.parseInt(results[8]) == 1){ equipmenttriggers[GameMenu.blank1] = true; }
						
						if (Integer.parseInt(results[9])== 0) { equipmenttriggers[GameMenu.blank2] = false;}
						else if (Integer.parseInt(results[9])== 1) {equipmenttriggers[GameMenu.blank2] = true; }
						
						if (Integer.parseInt(results[10]) == 0) { equipmenttriggers[GameMenu.blank3] = false;}
						else if (Integer.parseInt(results[10]) == 1) { equipmenttriggers[GameMenu.blank3] = true; }
						
						if(Integer.parseInt(results[11]) == 0) { equipmenttriggers[GameMenu.pike] = false; }
						else if (Integer.parseInt(results[11]) == 1) { equipmenttriggers[GameMenu.pike] = true; }
						break;
						
					case l1box:
						Hitbox hone = new Hitbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
													new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])), 
													Hitbox.regular);
						hone.setLayer(GameWorld.LAYER1_INDEX);
						hitboxes.add(hone);
						break;
						
					case l2box:
						Hitbox htwo = new Hitbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
												   new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])), 
												   Hitbox.regular);
						htwo.setLayer(GameWorld.LAYER2_INDEX);
						hitboxes.add(htwo);
						break;
						
					case l3box:
						Hitbox hthree =new Hitbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
												   new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])), 
												   Hitbox.regular);
						hthree.setLayer(GameWorld.LAYER3_INDEX);
						hitboxes.add(hthree);
						break;
						
					case l4box:
						Hitbox hfour = new Hitbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
													   new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])), 
													   Hitbox.regular);
						hfour.setLayer(GameWorld.LAYER4_INDEX);
						hitboxes.add(hfour);
						break;
						
					case l5box:
						Hitbox hfive = new Hitbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
													   new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])), 
													   Hitbox.regular);
						hfive.setLayer(GameWorld.LAYER5_INDEX);
						hitboxes.add(hfive);
						break;
						
					case door:
						Door d = new Door(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
										  new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])));
						d.setLayer(Integer.parseInt(results[4]));
						d.setConnectionLayer(Integer.parseInt(results[5]));
						d.setConnectionInsert(new Point(Double.parseDouble(results[6]), Double.parseDouble(results[7])));
						doors.add(d);
						break; 
						
					case portal:
						Portal p = new Portal(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
											new Point(Double.parseDouble(results[2]), Double.parseDouble(results[3])),
											new Point(Double.parseDouble(results[5]), Double.parseDouble(results[6])),
											results[4],
											Integer.parseInt(results[8]));
						p.setLayer(Integer.parseInt(results[7]));
						portals.add(p);
						break;
						
					case switch_sprite:
						Switch s = new Switch(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
											  Integer.parseInt(results[2]), 
											  Integer.parseInt(results[4]));
						s.configureAttributes(binaryAttributes[Switch_INDEX][BIN_WIDTH_INDEX], 
									 			binaryAttributes[Switch_INDEX][BIN_HEIGHT_INDEX]);
						s.configureCollisions(binaryCollisions[Switch_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[Switch_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[Switch_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[Switch_INDEX][Sprite.COLLISION_HEIGHT]);
						s.setLayer(Integer.parseInt(results[3]));
						switches.add(s);
						break;
						
					case light:
						LightSource l = new LightSource(new Point(Double.parseDouble(results[0]),Double.parseDouble(results[1])), 
														Integer.parseInt(results[2]),
														Integer.parseInt(results[3]), 
														Integer.parseInt(results[5]));
						l.configure(lightAttributes[Integer.parseInt(results[2])][LightSource.WIDTH_INDEX], 
						             lightAttributes[Integer.parseInt(results[2])][LightSource.HEIGHT_INDEX], 
						             lightAttributes[Integer.parseInt(results[2])][LightSource.OUTERRAD_INDEX],
						             lightAttributes[Integer.parseInt(results[2])][LightSource.INNERRAD_INDEX],
						             lightAttributes[Integer.parseInt(results[2])][LightSource.COLLISION_WIDTH_INDEX],
						             lightAttributes[Integer.parseInt(results[2])][LightSource.COLLISION_HEIGHT_INDEX]);
						l.setLayer(Integer.parseInt(results[4]));
						lights.add(l);
						break;
						
					case npc:
						NPC n = new NPC(new Point(Double.parseDouble(results[0]),Double.parseDouble(results[1])),  
										Integer.parseInt(results[2]), 
										Integer.parseInt(results[3]),
										Integer.parseInt(results[5]));
						n.configure( npcAttributes[Integer.parseInt(results[2])][NPC.SPD_INDEX], 
									 npcAttributes[Integer.parseInt(results[2])][NPC.PER_INDEX],
									 npcAttributes[Integer.parseInt(results[2])][NPC.WEIGHT_INDEX]);
						n.configureCollisions(sheetCollisions[Sprite.OFFSET_X], 
												sheetCollisions[Sprite.OFFSET_Y], 
												sheetCollisions[Sprite.COLLISION_WIDTH],
												sheetCollisions[Sprite.COLLISION_HEIGHT]);
						n.setLayer(Integer.parseInt(results[4]));
						npcs.add(n);
						break;
					
					case villain:
						Villain v = new Villain(new Point(Double.parseDouble(results[0]),Double.parseDouble(results[1])), 
												Integer.parseInt(results[2]), Integer.parseInt(results[4]));
						v.configureAttributes( villainAttributes[Integer.parseInt(results[2])][Villain.ATKRAD_INDEX], 
												villainAttributes[Integer.parseInt(results[2])][Villain.AWARERAD_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.HEALTH_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.PERIMETER_INDEX], 
												villainAttributes[Integer.parseInt(results[2])][Villain.WALKSPEED_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.RUNSPEED_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.ATKBOUNCE_INDEX], 
												villainAttributes[Integer.parseInt(results[2])][Villain.STUN_INDEX], 
												villainAttributes[Integer.parseInt(results[2])][Villain.SYNCH_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.ATK_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.DEF_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.ATK_TRIGGER_INDEX],
												villainAttributes[Integer.parseInt(results[2])][Villain.ATK_LENGTH_INDEX]);
						v.configureCollisions(sheetCollisions[Sprite.OFFSET_X], 
												sheetCollisions[Sprite.OFFSET_Y], 
												sheetCollisions[Sprite.COLLISION_WIDTH],
												sheetCollisions[Sprite.COLLISION_HEIGHT]);
						v.configureCombat(sheetCombat[Sprite.DIR_left], 
												sheetCombat[Sprite.DIR_right], 
												sheetCombat[Sprite.DIR_up], 
												sheetCombat[Sprite.DIR_down]);
						v.setLayer(Integer.parseInt(results[3]));
						villains.add(v);
						break;
						
					case treasure:
						TreasureChest trs = new TreasureChest( new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
															   Integer.parseInt(results[2]), Integer.parseInt(results[4])); 
						trs.configureAttributes(binaryAttributes[TreasureChest_INDEX][BIN_WIDTH_INDEX], 
									   			binaryAttributes[TreasureChest_INDEX][BIN_HEIGHT_INDEX]);
						trs.configureCollisions(binaryCollisions[TreasureChest_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[TreasureChest_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[TreasureChest_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[TreasureChest_INDEX][Sprite.COLLISION_HEIGHT]);
						trs.setLayer(Integer.parseInt(results[3]));
						treasures.add(trs);
						break;
						
					case shovel_tile:
						ShovelTile sh = new ShovelTile(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])), 
													Integer.parseInt(results[2]), 
													Integer.parseInt(results[4]));
						sh.configureAttributes(binaryAttributes[ShovelTile_INDEX][BIN_WIDTH_INDEX], 
												binaryAttributes[ShovelTile_INDEX][BIN_HEIGHT_INDEX]);
						sh.configureCollisions(binaryCollisions[ShovelTile_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[ShovelTile_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[ShovelTile_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[ShovelTile_INDEX][Sprite.COLLISION_HEIGHT]);
						sh.setLayer(Integer.parseInt(results[3]));
						shoveltiles.add(sh);
						break;
						
					case hack_box:
						Hackbox h = new Hackbox(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
												Integer.parseInt(results[2]));
						h.setLayer(Integer.parseInt(results[3]));
						h.configureAttributes(binaryAttributes[Hackbox_INDEX][BIN_WIDTH_INDEX], 
									  			binaryAttributes[Hackbox_INDEX][BIN_HEIGHT_INDEX]);
						h.configureCollisions(binaryCollisions[Hackbox_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[Hackbox_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[Hackbox_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[Hackbox_INDEX][Sprite.COLLISION_HEIGHT]);
						hackboxes.add(h);
						break;
						
					case pressure_plate:
						PressurePlate pp = new PressurePlate(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
																Integer.parseInt(results[3]));
						pp.setLayer(Integer.parseInt(results[2]));
						pp.configureAttributes(binaryAttributes[PressurePlate_INDEX][BIN_WIDTH_INDEX], 
												binaryAttributes[PressurePlate_INDEX][BIN_HEIGHT_INDEX]);

						pp.configureCollisions(binaryCollisions[PressurePlate_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[PressurePlate_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[PressurePlate_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[PressurePlate_INDEX][Sprite.COLLISION_HEIGHT]);
						pressureplates.add(pp);
						break;
						
					case gate:
						Gate g = new Gate(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
											Integer.parseInt(results[3]));
						g.setLayer(Integer.parseInt(results[2]));
						g.configureAttributes(binaryAttributes[Gate_INDEX][BIN_WIDTH_INDEX], 
												binaryAttributes[Gate_INDEX][BIN_HEIGHT_INDEX]);
						g.configureCollisions(binaryCollisions[Gate_INDEX][Sprite.OFFSET_X], 
												binaryCollisions[Gate_INDEX][Sprite.OFFSET_Y], 
												binaryCollisions[Gate_INDEX][Sprite.COLLISION_WIDTH], 
												binaryCollisions[Gate_INDEX][Sprite.COLLISION_HEIGHT]);
						gates.add(g);
						break;
						
					case barrel:
						Barrel b = new Barrel(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
												Integer.parseInt(results[3]));
						b.setLayer(Integer.parseInt(results[2]));
						b.configure(singletonAttributes[Barrel_INDEX][SIN_WIDTH_INDEX], 
									singletonAttributes[Barrel_INDEX][SIN_HEIGHT_INDEX], 
									singletonAttributes[Barrel_INDEX][SIN_COL_WID_INDEX],
									singletonAttributes[Barrel_INDEX][SIN_COL_HGT_INDEX]);
						
						barrels.add(b);
						break;
						
					case crate:
						Crate c = new Crate(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
											Integer.parseInt(results[3]));
						c.setLayer(Integer.parseInt(results[2]));
						c.configure(singletonAttributes[Crate_INDEX][SIN_WIDTH_INDEX], 
									singletonAttributes[Crate_INDEX][SIN_HEIGHT_INDEX], 
									singletonAttributes[Crate_INDEX][SIN_COL_WID_INDEX],
									singletonAttributes[Crate_INDEX][SIN_COL_HGT_INDEX]);
						crates.add(c);
						break;
						
					case book: 
						Book bo = new Book(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
											Integer.parseInt(results[3]));
						bo.setLayer(Integer.parseInt(results[2]));
						bo.configure(singletonAttributes[Book_INDEX][SIN_WIDTH_INDEX], 
									 singletonAttributes[Book_INDEX][SIN_HEIGHT_INDEX], 
									 singletonAttributes[Book_INDEX][SIN_COL_WID_INDEX],
									 singletonAttributes[Book_INDEX][SIN_COL_HGT_INDEX]);
						books.add(bo);
						break;
						
					case sign:
						Sign si = new Sign(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
											Integer.parseInt(results[3]));
						si.setLayer(Integer.parseInt(results[2]));
						si.configure(singletonAttributes[Sign_INDEX][SIN_WIDTH_INDEX], 
									 singletonAttributes[Sign_INDEX][SIN_HEIGHT_INDEX], 
									 singletonAttributes[Sign_INDEX][SIN_COL_WID_INDEX],
									 singletonAttributes[Sign_INDEX][SIN_COL_HGT_INDEX]);
						signs.add(si);
						break;
						
					case save_crystal:
						SaveCrystal save = new SaveCrystal(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])));
						save.setLayer(Integer.parseInt(results[2]));
						save.configure(singletonAttributes[Crystal_INDEX][SIN_WIDTH_INDEX], 
									   singletonAttributes[Crystal_INDEX][SIN_HEIGHT_INDEX], 
									   singletonAttributes[Crystal_INDEX][SIN_COL_WID_INDEX],
									   singletonAttributes[Crystal_INDEX][SIN_COL_HGT_INDEX]);
						crystals.add(save);
						break;
						
					case effect:
						Effect e = new Effect(new Point(Double.parseDouble(results[0]), Double.parseDouble(results[1])),
												Integer.parseInt(results[2]),
												Effect.FILE);
						e.handlerConfigure(effectAttributes[Integer.parseInt(results[2])][Effect.WIDTH_INDEX],
								            effectAttributes[Integer.parseInt(results[2])][Effect.HEIGHT_INDEX],
								            effectAttributes[Integer.parseInt(results[2])][Effect.SUB_DEP_INDEX]);
						e.setLayer(Integer.parseInt(results[3]));
						effects.add(e); 
						break;
					} 
					//END: Switch On (control)
				}
			//END: Conditional On (control)
		}
		//END: Iterating over (info)
	}
	
	public void saveWorld(){
		ArrayList<String> info = new ArrayList<String>();
		String line;
		
		//WORLD GENERATION SAVE FILE
		info.add("***");info.add("***");
		info.add("*** note: see world_generation_key.txt for info on syntax!");
		info.add("*** WORLD PROPERTIES");
		info.add("*** world layer");
		line = worldlayer + spc + wlayer;
		info.add(line);
		info.add("*** plot phase");
		line = plotphase + spc + phase;
		info.add(line);
		info.add("*** world frame");
		line = worldframe + spc + wframe;
		info.add(line);
		info.add("*** layer number");
		line = numlayers + spc + layers;
		info.add(line);
		info.add("*** ");
		info.add("*** ");
		info.add("*** OBJECTS");
		info.add("*** hero");
		line = newhero + spc + hero.getMinX() + spc + hero.getMinY();
		info.add(line);
		
		info.add("*** hero equipment");
		line = equipment + spc;
		for(int i = 0; i < equipmenttriggers.length; i++){
			if(equipmenttriggers[i]){ line = line + "1" + spc; }
			else { line = line + "0" + spc; }
		}
		info.add(line);
		
		//TODO: Save items 
		//info.add("*** items");
		//for(Item i : itempouch){
			//	
		//}
		
		info.add("*** "); info.add("***");
		info.add("*** HITBOXES");
		for(Sprite hb : hitboxes) {
			String control = null;
			switch(hb.getLayer()){
				case GameWorld.LAYER1_INDEX: control = l1box;
					break;
				case GameWorld.LAYER2_INDEX: control = l2box;
					break;
				case GameWorld.LAYER3_INDEX: control = l3box;
					break;
				case GameWorld.LAYER4_INDEX: control = l4box;
					break;
			}
			control = control + spc + hb.getMinX() + spc + hb.getMinY() + spc +
						  hb.getWidth() + spc + hb.getHeight();
			
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** DOORS");
		for(Sprite d : doors){
			String control = door + spc + d.getMinX() + spc + d.getMinY() + spc +
							 d.getWidth() + spc + d.getHeight() + spc + d.getLayer() + spc +
							 ((Door) d).getConnectionLayer() + spc + ((Door) d).getConnectionInsert().getX() +
							 spc + ((Door) d).getConnectionInsert().getY();
			info.add(control);
		}
		info.add("***"); info.add("***");
		info.add("*** PORTALS");
		for(Sprite p : portals){
			String control = portal + spc + p.getMinX() + spc + p.getMinY() + spc + 
							p.getWidth() + spc + p.getHeight();
			Portal port = (Portal) p;
			control = control + spc + port.getConnection() + spc + port.getNextInsertion().getX()
					 + spc + port.getNextInsertion().getY() + spc + port.getLayer() + spc + port.getNextLayer();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** VILLAINS");
		for(Sprite v : villains){
			String control = villain + spc + v.getMinX() + spc + v.getMinY() + spc + ((Villain) v).getType() + spc +
							v.getLayer() + spc + v.getIdentity();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** NPCS");
		for(Sprite n : npcs){
			String control = npc + spc + n.getMinX() + spc + n.getMinY() + spc + ((NPC) n).getType() + spc +
								((NPC) n).getMsgLookUp() + spc + n.getLayer() + spc + n.getIdentity();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** TREASURE CHESTS");
		for(Sprite t: treasures){
			String control = treasure + spc + t.getMinX() + spc + t.getMinY() + spc + 
							((TreasureChest) t).getContents() + spc + t.getLayer() + spc + t.getState();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** LIGHT SOURCES");
		for(Sprite l : lights){
			String control = light + spc + l.getMinX() + spc + l.getMinY() + spc +
							((LightSource)l).getType() + spc + l.getState() + spc
							+ l.getLayer() + spc + ((LightSource)l).getAnchor();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** SWITCHES");
		for(Sprite sw : switches) {
			String control = switch_sprite + spc + sw.getMinX() + spc + sw.getMinY() + spc +
							 sw.getState() + spc + sw.getLayer() + spc + ((Switch) sw).getAnchor();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** SHOVEL TILES");
		for(Sprite sh : shoveltiles){
			String control = shovel_tile + spc + sh.getMinX() + spc + sh.getMinY() + spc + 
							sh.getState() + spc + sh.getLayer() + spc + ((ShovelTile) sh).getContents();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** HACKBOXES");
		for(Sprite hk  : hackboxes){
			String control = hack_box + spc + hk.getMinX() + spc + hk.getMinY() + spc +
							 hk.getState() + spc + hk.getLayer();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** PRESSURE PLATES");
		for(Sprite pp : pressureplates){
			String control = pressure_plate + spc + pp.getMinX() + spc + pp.getMinY() + spc +
							 pp.getLayer() + spc + ((PressurePlate) pp).getAnchor();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** GATES");
		for(Sprite g: gates){
			String control = gate + spc + g.getMinX() + spc + g.getMinY() + spc +
							g.getLayer() + spc + ((Gate) g).getAnchor();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** BARRELS");
		for(Sprite b : barrels){
			String control = barrel + spc + b.getMinX() + spc + b.getMinY() + spc +
							b.getLayer() + spc + b.getIdentity();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** BOOKS");
		for(Sprite bo : books){
			String control = book + spc + bo.getMinX() + spc + bo.getMinY() + spc +
							bo.getLayer() + spc + ((Book) bo).getMsgLookUp();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** CRATES");
		for(Sprite c : crates){
			String control = crate + spc + c.getMinX() + spc + c.getMinY() + spc +
							c.getLayer() + spc + c.getIdentity();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** SIGNS");
		for(Sprite s : signs){
			String control = sign + spc + s.getMinX() + spc + s.getMinY() + spc +
							s.getLayer() + spc + ((Sign)s).getMsgLookUp();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** SAVE CRYSTALS");
		for(Sprite sc : crystals){
			String control = save_crystal + spc + sc.getMinX() + spc + sc.getMinY() + spc +
							sc.getLayer();
			info.add(control);
		}
		info.add("*** "); info.add("***");
		info.add("*** EFFECTS");
		for(Sprite ef : effects){
			String control = effect + spc + ef.getMinX() + spc + ef.getMinY() + spc + ((Effect) ef).getType() + spc +
								ef.getLayer();
			info.add(control);
		}
		
		//HERO VARIABLE ATTRIBUTES SAVE FILE
		ArrayList<String> heroinfo = new ArrayList<String>();
		String varConfig = ((Hero) hero).getType() + spc +((Hero) hero).getTotalHP() + spc + ((Hero) hero).getCurrentHP() 
							+ spc + ((Hero) hero).getATK() + spc + ((Hero) hero).getDEF() + spc +
							((Hero) hero).getInnerLanternRad() + spc + ((Hero) hero).getOuterLanternRad();
		heroinfo.add("*** HERO'S VARIABE ATTRIBUTES."); 
		heroinfo.add("*** "); heroinfo.add("*** "); 
		heroinfo.add("*** these characteristics can be altered by in-game play.");
		heroinfo.add("*** this file will be overwritten on save each time.");
		heroinfo.add("*** total_hp current_hp attack defense lantern_inner_rad lantern_outer_rad."); 
		heroinfo.add(varConfig);
		
		//WRTIE TO FILE
		try{
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(hero_variable_config_path));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(filepath));
			for(String bit : info){ bw2.write(bit); bw2.newLine();}
			for(String bit : heroinfo){ bw1.write(bit); bw1.newLine(); }
			bw1.close(); bw2.close();
		}
		catch(IOException ie){ }
	}
	
	public Sprite getHero() { return hero; }
	
	public ArrayList<Sprite> getVillains() { return villains; }
	
	public ArrayList<Sprite> getNPCs() { return npcs; }
	
	public ArrayList<Sprite> getDoors() { return doors; }
	
	public ArrayList<Sprite> getPortals() { return portals; }
	
	public ArrayList<Sprite> getTreasures() { return treasures; }
	
	public ArrayList<Sprite> getHitboxes() { return hitboxes; }
	
	public ArrayList<Sprite> getHackboxes() { return hackboxes; }
	
	public ArrayList<Sprite> getShovelTiles() { return shoveltiles; }
	
	public ArrayList<Sprite> getLights() { return lights; }
	
	public ArrayList<Sprite> getSwitches() { return switches; }
	
	public ArrayList<Sprite> getPressurePlates() { return pressureplates; }
	
	public ArrayList<Sprite> getGates() { return gates; } 
	
	public ArrayList<Sprite> getBarrels() { return barrels; }
	
	public ArrayList<Sprite> getCrates() { return crates; }

	public ArrayList<Sprite> getBooks() { return books; }
	
	public ArrayList<Sprite> getSigns() { return signs; }
	
	public ArrayList<Sprite> getSaveCrystals() { return crystals; }
	
	public ArrayList<Sprite> getEffects() { return effects; }
	
	public int getPlotPhase() { return phase; }
	
	public String getWorldFrame() { return wframe; }
	
	public SceneTriggers getScenes() { return scenes; }
	
	public Dialogue getDialogue() { return words; }
	
	public int[][] getItemPouch() { return itempouch; }
	
	public boolean[] getEquipmentTriggers() { return equipmenttriggers; }
	
	public int getLayer() { return wlayer; }
	
	public int getLayers() { return layers; }
	
	public void setHero(Sprite h) { hero = h; }
	
	public void setVillains(ArrayList<Sprite> v){ villains = v; }
	
	public void setHitboxes(ArrayList<Sprite> hb) { hitboxes = hb;	}
	
	public void setHackboxes(ArrayList<Sprite> hk) { hackboxes = hk; }
	
	public void setNPCs(ArrayList<Sprite> np) { npcs = np; }
	
	public void setPortals(ArrayList<Sprite> p) { portals = p; }
	
	public void setDoors(ArrayList<Sprite> d) { doors = d; }
	
	public void setTreasures(ArrayList<Sprite> t) { treasures = t; }
	
	public void setShovelTiles(ArrayList<Sprite> s) { shoveltiles = s; }
	
	public void setLights(ArrayList<Sprite> l){ lights = l; }
	
	public void setSwitches(ArrayList<Sprite> s) { switches = s; }
	
	public void setPressurePlates(ArrayList<Sprite> pp) { pressureplates = pp; }
	
	public void setGates(ArrayList<Sprite> g) { gates = g; }
	
	public void setBarrels(ArrayList<Sprite> b) { barrels = b ; }
	
	public void setCrates(ArrayList<Sprite> c) { crates = c; }
	
	public void setBooks(ArrayList<Sprite> bo) { books = bo; }
	
	public void setSigns(ArrayList<Sprite> s) { signs = s; }
	
	public void setSaveCrystals(ArrayList<Sprite> sc) { crystals = sc; }
	
	public void setEffects(ArrayList<Sprite> e) { effects = e; }
	
	public void setWorldFrame(String wf) { wframe = wf; }
	
	public void setScenes(SceneTriggers st) { scenes = st; }
	
	public void setDialogue(Dialogue d) { words = d; }
	
	public void setPlotPhase(int thisPhase) { phase = thisPhase; }
	
	public void setItemPouch(int[][] i) { itempouch = i; }
	
	public void setEquipmentTriggers(boolean[] e) { equipmenttriggers = e; }
	
	public void setLayer(int whichLayer) { wlayer = whichLayer; }
	
	public void setSavePath(String path){  filepath = save_path + sep + path + ".txt"; }
	
	public void setConfigPath(String path){ filepath = world_config_path + sep + path + ".txt"; }
}
