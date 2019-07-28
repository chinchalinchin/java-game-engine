package subterra.engine.imagehandling;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class SpriteSheetManager {
	
	private static final String commentString = "***";
	private static final String cdir = System.getProperty("user.dir"),
								sep = System.getProperty("file.separator"),
								IN = "INPUT_", OUT = "OUTPUT_",
								img_config_path = cdir + sep + "sprite_img_config" + sep,
								type_config_path = cdir + sep + "sprite_type_config" + sep,
								state_config_path = cdir + sep + "sprite_state_config" + sep,
								sheet_img_path = img_config_path + "sprite_sheet_img_config.txt",
								sheet_prop_path = img_config_path + "sprite_sheet_properties.txt",
								npc_type_config_path = type_config_path + "npc_sprite_type_config.txt", 
								npc_state_config_path = state_config_path + IN + "npc_sprite_state_config.txt",
								npc_state_frame_map_path = state_config_path + OUT + "npc_state_frame_map.txt",
								villain_type_config_path = type_config_path + "villain_sprite_type_config.txt",
								villain_state_config_path = state_config_path + IN + "villain_sprite_state_config.txt",
								villain_state_frame_map_path = state_config_path + OUT + "villain_state_frame_map.txt",
								hero_type_config_path = type_config_path + "hero_sprite_type_config.txt",
								hero_state_config_path = state_config_path + IN + "hero_sprite_state_config.txt",
								hero_state_frame_map_path = state_config_path + OUT + "hero_state_frame_map.txt",
								img_path = cdir + sep + "imgs"+ sep + "spritesheets" + sep,
								sheet_path = img_path + "output" + sep,
								skin_path = img_path + "skins" + sep,
								premade_path = skin_path + "premade" + sep,
								apparel_path = img_path + "apparel" + sep,
								weapon_path = img_path + "weapons" + sep;
	
	private final static int pre = -1, none = -1;
	//Order of Animations
	public final static int magic = 0, thrust = 1, slash = 2, bow = 3, die = 4;
	//Order of Rows
	public final static int magic_up = 0, magic_left = 1, magic_down = 2, magic_right = 3,
						    thrust_up = 4, thrust_left = 5, thrust_down = 6, thrust_right = 7,
						    walk_up = 8, walk_left = 9, walk_down = 10, walk_right = 11,
						    slash_up = 12, slash_left = 13, slash_down = 14, slash_right = 15,
						    bow_up = 16, bow_left = 17, bow_down = 18, bow_right = 19,
						    die_state = 20,
						    ROWS = 21;
	
		//Recipe Properties Indices
	private final static int TYPE_INDEX = 0, TYPE_SKIN_INDEX = 1, TYPE_APPAREL_INDEX = 2,
						     STATE_INDEX =0, STATE_WEAPON_INDEX = 1, STATE_ROW_INDEX = 2;
		//Sheet Properties Parsing Order
	private final static int SKIN = 0, PREMADE = 1, APPAREL = 2, WEAPON = 3, 
							DIMENSIONS = 4,
					        MAGIC = 5, THRUST = 6, WALK = 7, SLASH = 8, BOW = 9, DIE = 10;
	
		//First Map Indexed by Type on Integer
			//ArrayList Indexed by Skin and Apparels
	private Map<Integer, ArrayList<Integer>> heroTypeRecipes, 
											 npcTypeRecipes, 
											 vilTypeRecipes;
	//First Map Indexed by Type on Integer
		//ArrayList Indexed by Weapon and Equipments
	private Map<Integer, ArrayList<Integer>> heroStateRecipes,
											 npcStateRecipes, 
											 vilStateRecipes;
	private ArrayList<Integer> heroStateFrameMap,
							   npcStateFrameMap,
							   vilStateFrameMap;
	private int magic_frames, thrust_frames, walk_frames, slash_frames, bow_frames, die_frames;
	private int width_per_frame, height_per_frame;
	private ArrayList<BufferedImage> skins, apparel, weapons, premade;
	private BufferedImage skin_palette, weapon_palette;
	private GraphicsConfiguration graphics_config;
	private ArrayList<BufferedImage> apparel_palette;
	private String[] skin_paths, apparel_paths, weapon_paths, premade_paths;
	private boolean debug;
	
	public SpriteSheetManager(GraphicsConfiguration gc, boolean db, String init){
		//get configuration properties from file
		graphics_config = gc; debug = db;
		configure();
	
		if(init.equals("y")){
			loadSheets();
			makeAnimations();
		}
		else{
			loadStateFrameMaps();
		}
		
		
		
	}
	
	private void configure(){
		//initialize temporary sheet property variables
			//will eventually get subsumed into the size variable of the sheet arrays
		int skins_cnt = 0, app_cnt = 0, weap_cnt = 0, pre_cnt = 0;
		//read in sheet properties
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(sheet_prop_path));
			String line; int counter = 0;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					if(counter == SKIN) { skins_cnt = Integer.parseInt(line.split("\\s")[0]); }
					else if(counter == PREMADE ) { pre_cnt = Integer.parseInt(line.split("\\s")[0]); }
					else if(counter == APPAREL) { app_cnt = Integer.parseInt(line.split("\\s")[0]);}
					else if(counter == WEAPON){ weap_cnt = Integer.parseInt(line.split("\\s")[0]); }
					else if(counter == DIMENSIONS) { 
						width_per_frame = Integer.parseInt(line.split("\\s")[0]);
						height_per_frame = Integer.parseInt(line.split("\\s")[1]);
					}
					else if(counter == MAGIC) { magic_frames = Integer.parseInt(line.split("\\s")[1]);}
					else if(counter == THRUST) { thrust_frames = Integer.parseInt(line.split("\\s")[1]); }
					else if(counter == WALK) { walk_frames = Integer.parseInt(line.split("\\s")[1]);}
					else if(counter == SLASH) { slash_frames = Integer.parseInt(line.split("\\s")[1]); }
					else if(counter == BOW) { bow_frames = Integer.parseInt(line.split("\\s")[1]); }
					else if(counter == DIE) { die_frames = Integer.parseInt(line.split("\\s")[1]); }
					counter++;
				}
			}
			bReader.close();
		} 
		catch(IOException ie){ System.out.println("Error loading: " + sheet_prop_path); }
		
		//initialize string arrays
		skin_paths = new String[skins_cnt]; premade_paths = new String[pre_cnt];
		apparel_paths = new String[app_cnt];
		weapon_paths = new String[weap_cnt];
		//read in sheet names.
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(sheet_img_path));
			String line; int counter = 1;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					if(counter <= skins_cnt){
						skin_paths[counter-1] = line;
					}
					else if(counter>skins_cnt && counter <= (skins_cnt + pre_cnt)){
						premade_paths[counter-skins_cnt-1] = line;
					}
					else if(counter>(skins_cnt + pre_cnt) && counter<=(skins_cnt + pre_cnt + app_cnt)){
						apparel_paths[counter-skins_cnt-pre_cnt-1] = line;
					}
					else if(counter>(skins_cnt + pre_cnt + app_cnt)){
						weapon_paths[counter-skins_cnt-pre_cnt-app_cnt-1] = line;
					}
					counter++;
				}
			}
			
			bReader.close();
		}
		catch(IOException ie){ System.out.println("Error loading: " + sheet_img_path); }
		
		//initialize maps for type recipes
		heroTypeRecipes = new HashMap<>(); npcTypeRecipes = new HashMap<>(); 
		vilTypeRecipes = new HashMap<>();
		//read in hero type recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(hero_type_config_path));
			String line;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[TYPE_INDEX].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[TYPE_SKIN_INDEX]));
					for(int i = TYPE_APPAREL_INDEX; i < line.split("\\s").length; i++){
						Integer hold = Integer.parseInt(line.split("\\s")[i]);
						recipe.add(hold);
					}
					heroTypeRecipes.put(Integer.parseInt(line.split("\\s")[TYPE_INDEX]), 
									    recipe);
				}
			}
			bReader.close();
		}
		catch(IOException ie){ System.out.println("Error loading: " + hero_type_config_path); }
		//read in npc type recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(npc_type_config_path));
			String line;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[TYPE_SKIN_INDEX]));
					for(int i = TYPE_APPAREL_INDEX; i < line.split("\\s").length; i++){
						Integer hold = Integer.parseInt(line.split("\\s")[i]);
						recipe.add(hold);
					}
					npcTypeRecipes.put(Integer.parseInt(line.split("\\s")[TYPE_INDEX]), 
									    recipe);
				}
			}
			bReader.close();
		}
		catch(IOException ie){ System.out.println("Error loading: " + npc_type_config_path); }
		//read in villain type recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(villain_type_config_path));
			String line;
			while((line = bReader.readLine())!= null){
				if(!line.split("\\s")[0].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[TYPE_SKIN_INDEX]));
					for(int i = TYPE_APPAREL_INDEX; i < line.split("\\s").length; i++){
						Integer hold = Integer.parseInt(line.split("\\s")[i]);
						recipe.add(hold);
					}
					vilTypeRecipes.put(Integer.parseInt(line.split("\\s")[TYPE_INDEX]), 
									   recipe);
				}
				//ELSE IF LINE EQUALS PREMADE FLAG
					//put into ArrayList<Integer> value of premade sheet, then put into vilTypeRecipes
			}
			bReader.close();
		}
		catch(IOException ie){ System.out.println("Error loading: " + villain_type_config_path); }
	
		//initialize maps for state recipes
		heroStateRecipes = new HashMap<>(); npcStateRecipes = new HashMap<>(); 
		vilStateRecipes = new HashMap<>();
		//read in hero state recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(hero_state_config_path));
			String line;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[STATE_INDEX].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_WEAPON_INDEX])); // add weapon index
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_ROW_INDEX])); // add row index
					heroStateRecipes.put(Integer.parseInt(line.split("\\s")[STATE_INDEX]), recipe);
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + hero_state_config_path); }
		//read in npc state recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(npc_state_config_path));
			String line;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[STATE_INDEX].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_WEAPON_INDEX]));
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_ROW_INDEX]));
					npcStateRecipes.put(Integer.parseInt(line.split("\\s")[STATE_INDEX]), recipe);
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + npc_state_config_path); }
		//read in villain state recipes
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(villain_state_config_path));
			String line;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[STATE_INDEX].equals(commentString)){
					ArrayList<Integer> recipe = new ArrayList<Integer>();
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_WEAPON_INDEX]));
					recipe.add(Integer.parseInt(line.split("\\s")[STATE_ROW_INDEX]));
					vilStateRecipes.put(Integer.parseInt(line.split("\\s")[0]), recipe);
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + villain_state_config_path); }
	}
	
	private void loadSheets(){
		skins = new ArrayList<BufferedImage>();
		try{
			for(int i = 0; i < skin_paths.length; i++){
				BufferedImage bf = ImageIO.read(new File(skin_path+skin_paths[i]));
				BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																			Transparency.BITMASK);
				if(debug) { System.out.println("... Loading Sheet : " + skin_paths[i] + " ..."); }
				Graphics2D g2 = (Graphics2D) copy.getGraphics();
				g2.drawImage(bf, 0 , 0, null);
				g2.dispose();
				if(copy != null) { skins.add(copy); }
			}
		}
		catch(IOException ie) { System.out.println("Error loading sheet files. Check filenames match: " 
												    + sheet_img_path ); }
		apparel = new ArrayList<BufferedImage>();
		try{
			for(int i = 0; i < apparel_paths.length; i++){
				BufferedImage bf = ImageIO.read(new File(apparel_path+apparel_paths[i]));
				BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																			Transparency.BITMASK);
				if(debug) { System.out.println("... Loading Sheet : " + apparel_paths[i] + " ..."); }
				Graphics2D g2 = (Graphics2D) copy.getGraphics();
				g2.drawImage(bf, 0 , 0, null);
				g2.dispose();
				if(copy != null) { apparel.add(copy); }
			}
		}
		catch(IOException ie) { System.out.println("Error loading sheet files. Check filenames match: "
													+ sheet_img_path ); }
		weapons = new ArrayList<BufferedImage>();
		try{
			for(int i = 0; i < weapon_paths.length; i++){
				BufferedImage bf = ImageIO.read(new File(weapon_path+weapon_paths[i]));
				BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																			Transparency.BITMASK);
				if(debug) { System.out.println("... Loading Sheet : " + weapon_paths[i] + " ..."); }
				Graphics2D g2 = (Graphics2D) copy.getGraphics();
				g2.drawImage(bf, 0 , 0, null);
				g2.dispose();
				if(copy != null) { weapons.add(copy); }
			}
		}
		catch(IOException ie){ System.out.println("Error loading sheet files. Check filenames match: "
													+ sheet_img_path ); }
		
		premade = new ArrayList<BufferedImage>();
		try{
			for(int i = 0; i < premade_paths.length; i++){
				BufferedImage bf = ImageIO.read(new File(premade_path + premade_paths[i]));
				BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																			Transparency.BITMASK);
				if(debug) { System.out.println("... Loading Sheet : " + premade_paths[i] + " ..."); }
				Graphics2D g2 = (Graphics2D) copy.getGraphics();
				g2.drawImage(bf, 0 , 0, null);
				g2.dispose();
				if(copy != null) { premade.add(copy); }
			}
		}
		catch(IOException ie){ System.out.println("Error loading sheet files. Check filenames match: "
													+ sheet_img_path ); }
	}
	
	private void loadStateFrameMaps(){
		heroStateFrameMap = new ArrayList<Integer>();
		npcStateFrameMap = new ArrayList<Integer>();
		vilStateFrameMap = new ArrayList<Integer>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(hero_state_frame_map_path)); 
			String line;
			while((line = br.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					heroStateFrameMap.add(Integer.parseInt(line.split("\\s")[1]));
				}
			}
			br.close();
			br = new BufferedReader(new FileReader(npc_state_frame_map_path));
			while((line = br.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					npcStateFrameMap.add(Integer.parseInt(line.split("\\s")[1]));
				}
			}
			br.close();
			br = new BufferedReader(new FileReader(villain_state_frame_map_path));
			while((line = br.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					vilStateFrameMap.add(Integer.parseInt(line.split("\\s")[1]));
				}
			}
			br.close();
		}
		catch(IOException ie) { System.out.println("Error loading state frame maps"); }
	}
	
	private void makeAnimations(){
		int row;
		if(debug) { System.out.println("... Composing Sheet Sprite Types and States ..."); }
		heroStateFrameMap = new ArrayList<Integer>();
		for(int i = 0; i < heroTypeRecipes.size(); i++){
			if(heroTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1) != pre){
				apparel_palette = new ArrayList<BufferedImage>();
				int skin = heroTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1);
				int apps = heroTypeRecipes.get(i).size() - 1;
				for(int j = 0; j < apps; j++){ apparel_palette.add(getApparelSheet(heroTypeRecipes.get(i).get(TYPE_APPAREL_INDEX+j-1))); }
				skin_palette = getSkinSheet(skin);
			}
			else{
				int index = heroTypeRecipes.get(i).get(TYPE_APPAREL_INDEX-1);
				skin_palette = getPremadeSheet(index);
			}
			for(int k = 0; k< heroStateRecipes.size(); k++){
				if(heroStateRecipes.get(k).get(STATE_WEAPON_INDEX-1) != none){
					int weapon = heroStateRecipes.get(k).get(STATE_WEAPON_INDEX-1);
					weapon_palette = getWeaponSheet(weapon);
				}
				row = heroStateRecipes.get(k).get(STATE_ROW_INDEX-1);
				BufferedImage palette = graphics_config.createCompatibleImage(skin_palette.getWidth(), skin_palette.getHeight(),
																				Transparency.BITMASK);
				Graphics2D g = (Graphics2D) palette.getGraphics();
				g.drawImage(skin_palette, 0, 0, null);
				if(apparel_palette != null) { for(BufferedImage img : apparel_palette){ g.drawImage(img, 0, 0, null); } }
				if(weapon_palette != null) { g.drawImage(weapon_palette, 0, 0, null); }
				g.dispose();
				int startY = row*height_per_frame;
				int frames = getFrameNumber(row);
				for(int j = 0; j < frames; j++){
					int startX = j*width_per_frame;
					String path = sheet_path+"hero_type"+i+"_state"+k+"_frame" + j + ".png";
					File f = new File(path);
					try{
					ImageIO.write(palette.getSubimage(startX, startY, width_per_frame, height_per_frame), 
										"png", f);
					}catch(IOException ie){ System.out.println("Error writing sprite sheet: " + path); }
				}
				if(debug) { System.out.println("... Composing Hero Type: " + i + " State: " + k + " ..."); }
				heroStateFrameMap.add(frames);
				weapon_palette = null; 
			}
				skin_palette = null; apparel_palette = null;
		}
		
		row = 0; npcStateFrameMap = new ArrayList<Integer>();
		for(int i = 0; i < npcTypeRecipes.size(); i++){
			if(npcTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1) != pre){
				apparel_palette = new ArrayList<BufferedImage>();
				int skin = npcTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1);
				int apps = npcTypeRecipes.get(i).size() - 1;
				for(int j = 0; j < apps; j++){ apparel_palette.add(getApparelSheet(npcTypeRecipes.get(i).get(TYPE_APPAREL_INDEX+j-1))); }
				skin_palette = getSkinSheet(skin);
			}
			else{
				int index = npcTypeRecipes.get(i).get(TYPE_APPAREL_INDEX-1);
				skin_palette = getPremadeSheet(index);
			}
			for(int k = 0; k< npcStateRecipes.size(); k++){
				if(npcStateRecipes.get(k).get(STATE_WEAPON_INDEX-1) != none){
					int weapon = npcStateRecipes.get(k).get(STATE_WEAPON_INDEX-1);
					weapon_palette = getWeaponSheet(weapon);
				}
				row = npcStateRecipes.get(k).get(STATE_ROW_INDEX-1);
				BufferedImage palette = graphics_config.createCompatibleImage(skin_palette.getWidth(), skin_palette.getHeight(),
																				Transparency.BITMASK);
				Graphics2D g = (Graphics2D) palette.getGraphics();
				g.drawImage(skin_palette, 0, 0, null);
				if(apparel_palette != null) { for(BufferedImage img : apparel_palette){ g.drawImage(img, 0, 0, null); } }
				if(weapon_palette != null) { g.drawImage(weapon_palette, 0, 0, null); }
				g.dispose();
				int startY = row*height_per_frame;
				int frames = getFrameNumber(row);
				for(int j = 0; j < frames; j++){
					int startX = j*width_per_frame;
					String path = sheet_path+"npc_type"+i+"_state"+k+"_frame" + j + ".png";
					File f = new File(path);
					try{
					ImageIO.write(palette.getSubimage(startX, startY, width_per_frame, height_per_frame), 
										"png", f);
					}catch(IOException ie){ System.out.println("Error writing sprite sheet: " + path); }
				}
				if(debug) { System.out.println("... Composing NPC Type: " + i + " State: " + k + " ..."); }
				npcStateFrameMap.add(frames);
				weapon_palette = null;
			}
				skin_palette = null; apparel_palette = null;
		}
		
		row = 0; vilStateFrameMap = new ArrayList<Integer>();
		for(int i = 0; i < vilTypeRecipes.size(); i++){
			if(vilTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1) != pre){
				apparel_palette = new ArrayList<BufferedImage>();
				int skin = vilTypeRecipes.get(i).get(TYPE_SKIN_INDEX-1);
				int apps = vilTypeRecipes.get(i).size() - 1;
				for(int j = 0; j < apps; j++){ apparel_palette.add(getApparelSheet(vilTypeRecipes.get(i).get(TYPE_APPAREL_INDEX+j-1))); }
				skin_palette = getSkinSheet(skin);
			}
			else{
				int index = vilTypeRecipes.get(i).get(TYPE_APPAREL_INDEX-1);
				skin_palette = getPremadeSheet(index);
			}
			for(int k = 0; k< vilStateRecipes.size(); k++){
				if(vilStateRecipes.get(k).get(STATE_WEAPON_INDEX-1) != none){
					int weapon = vilStateRecipes.get(k).get(STATE_WEAPON_INDEX-1);
					weapon_palette = getWeaponSheet(weapon);
				}
				row = vilStateRecipes.get(k).get(STATE_ROW_INDEX-1);
				BufferedImage palette = graphics_config.createCompatibleImage(skin_palette.getWidth(), skin_palette.getHeight(),
																				Transparency.BITMASK);
				Graphics2D g = (Graphics2D) palette.getGraphics();
				g.drawImage(skin_palette, 0, 0, null);
				if(apparel_palette != null) { for(BufferedImage img : apparel_palette){ g.drawImage(img, 0, 0, null); } }
				if(weapon_palette != null) { g.drawImage(weapon_palette, 0, 0, null); }
				g.dispose();
				int startY = row*height_per_frame;
				int frames = getFrameNumber(row);
				for(int j = 0; j < frames; j++){
					int startX = j*width_per_frame;
					String path = sheet_path+"vil_type"+i+"_state"+k+"_frame" + j + ".png";
					File f = new File(path);
					try{
					ImageIO.write(palette.getSubimage(startX, startY, width_per_frame, height_per_frame), 
										"png", f);
					}catch(IOException ie){ System.out.println("Error writing sprite sheet: " + path); }
				}
				if(debug) { System.out.println("... Composing Villain Type: " + i + " State: " + k + " ..."); }
				vilStateFrameMap.add(frames);
				weapon_palette = null;
			}
				skin_palette = null; apparel_palette = null;
		}
		//save state-frame maps to file
		try{
			BufferedWriter bw = new BufferedWriter( new FileWriter(npc_state_frame_map_path));
			bw.write("*** state frames" ); bw.newLine();
			int counter = 0;
			for(Integer i : npcStateFrameMap){
				String hold = counter + " " + i.toString();
				bw.write(hold); bw.newLine();
				counter++;
			}
			bw.close();
			bw = new BufferedWriter( new FileWriter(villain_state_frame_map_path));
			bw.write("*** state frames" ); bw.newLine();
			counter = 0;
			for(Integer i : vilStateFrameMap){
				String hold = counter + " " + i.toString();
				bw.write(hold); bw.newLine();
				counter++;
			}
			bw.close();
			bw = new BufferedWriter( new FileWriter(hero_state_frame_map_path));
			bw.write("*** state frames" ); bw.newLine();
			counter = 0;
			for(Integer i : heroStateFrameMap){
				String hold = counter + " " + i.toString();
				bw.write(hold); bw.newLine();
				counter++;
			}
			bw.close();
		} 
		catch(IOException ie) { System.out.println("Error saving state-frame maps"); }
		
		heroTypeRecipes = null; heroStateRecipes = null;
		npcTypeRecipes = null; npcStateRecipes = null;
		vilTypeRecipes = null; vilStateRecipes = null;
		skins = null; apparel = null;
		weapons = null; premade = null;
	}
	
	private BufferedImage getSkinSheet(int sk){ return skins.get(sk); }
	
	private BufferedImage getApparelSheet(int app){ return apparel.get(app); }	
	
	private BufferedImage getWeaponSheet(int wpn){ return weapons.get(wpn);}
	
	private BufferedImage getPremadeSheet(int pre) { return premade.get(pre); }
	
	private int getFrameNumber(int row){
		if(row == magic_up || row == magic_down || row == magic_left || row == magic_right){ 
			return magic_frames;
		}
		else if (row == thrust_up || row == thrust_down || row == thrust_left || row == thrust_right){
			return thrust_frames;
		}
		else if (row == walk_up || row == walk_down || row == walk_left || row == walk_right ){
			return walk_frames;
		}
		else if(row == slash_up || row == slash_down || row == slash_left || row == slash_right){
			return slash_frames;
		}
		else if(row == bow_up || row == bow_down || row == bow_left || row == bow_right){
			return bow_frames;
		}
		else if(row == die_state){
			return die_frames;
		}
		else return 0;
	}

	public int getHeroFrames(int state){  return heroStateFrameMap.get(state); }
	
	public int getNPCFrames(int state) { return npcStateFrameMap.get(state); }
	
	public int getVillainFrames(int state) { return vilStateFrameMap.get(state); }
}
