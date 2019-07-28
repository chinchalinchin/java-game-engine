package subterra.engine.imagehandling;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import subterra.geometry.Point;
import subterra.library.Effect;
import subterra.library.Hero;
import subterra.library.LightSource;
import subterra.library.NPC;
import subterra.library.Villain;

/**
 * 
 * @author chinchalinchin
 *
 *@description
 *
 
 */
public class SpriteImageLoader {

		//Binary Sprite Indices
	public static final int ShovelTile_INDEX = 0, TreasureChest_INDEX = 1, Switch_INDEX = 2, 
							Hack_INDEX = 3, Plate_INDEX = 4, Vrain_INDEX = 5, Gate_INDEX = 6,
							BIN_OBJECTS = 7; 
		//Singleton Sprite Indices
	public static final int Sign_INDEX = 0, Book_INDEX = 1, Crystal_INDEX = 2, Barrel_INDEX = 3,
							Crate_INDEX = 4, Lantern_INDEX= 5,
						    SIN_OBJECTS = 6;
		//Projectile Indices
	public static final int Bolt_INDEX = 0;
	
	
	private static final String commentString = "***";
	@SuppressWarnings("unused")
	private static final String cdir = System.getProperty("user.dir"),
								sep = System.getProperty("file.separator"),
								spc = " ",
								imgpath = cdir + sep + "imgs" + sep, //file locations
								imgconfigpath = cdir + sep + "sprite_img_config" + sep,
								objectpath = imgpath + "objects" + sep,
								lightpath = imgpath + "lightsources" + sep,
								effectpath = imgpath + "effects" + sep,
								worldpath = imgpath + "world" + sep,
								menupath = imgpath + "menu" + sep,
								sheetpath = imgpath + "spritesheets" + sep + "output" + sep,
								herotag = "hero", npctag = "npc", viltag = "vil",
								typetag = "_type", statetag = "_state", frametag = "_frame",
								lightsource = "ls_", effect = "ef_",
								layeroneframe = "_W1.png", layeroneoverframe = "_W1_over.png",
								layertwoframe = "_W2.png", layertwooverframe = "_W2_over.png",
								layerthreeframe = "_W3.png", layerthreeoverframe = "_W3_over.png",
								layerfourframe = "_W4.png", layerfouroverframe = "_W4_over.png",
								layerfiveframe = "_W5.png", layerfiveoverframe = "W5_over.png",
								BINARY_IMG_PATHS = imgconfigpath + "binary_sprite_img_config.txt",
								SINGLETON_IMG_PATHS = imgconfigpath + "singleton_sprite_img_config.txt",
								PROJECTILE_IMG_PATHS = imgconfigpath + "projectile_img_config.txt",
								LIGHT_IMG_PATHS = imgconfigpath + "lightsource_img_config.txt",
								EFFECT_IMG_PATHS = imgconfigpath+ "effect_img_config.txt";
	private final static int LS_STATIONARY_INDEX = 0, LS_ANIMATED_INDEX = 1;
	
			//first map indexed by type
				//second map indexed by state
					//arraylist indexed by frame
	private Map<Integer, Map<Integer, ArrayList<BufferedImage>>> heroRepository, //first map indexed by type
																 npcRepository, //second map indexed by state
																 villainRepository; //arraylist indexed by frame
	private Map<Integer, ArrayList<BufferedImage>> lightRepo; //map index by stationary and animated, arraylists index by type and frame
	private ArrayList<BufferedImage> binaryRepo; //indexed by type and state
	private ArrayList<BufferedImage> singleRepo; //indexed by type
	private ArrayList<BufferedImage> worldRepo; //index by world, layer and over		
	private ArrayList<BufferedImage> effectRepo; //indexed by type and frame
	private ArrayList<BufferedImage> projectRepo; //indexed by type and direction
	private SpriteSheetManager sheet_manager;
	private BufferedImage msg; //Msg has to be separate since it needs reloaded after being drawn on.
	private GraphicsConfiguration graphics_config;
	private String worldframe;
	private int layers;
	private boolean debug;
	
	public SpriteImageLoader(String wf, int lyrs, GraphicsConfiguration gc, boolean db){
		worldframe = wf; layers = lyrs; graphics_config = gc; debug = db;
		sheet_manager = new SpriteSheetManager(graphics_config, debug, pollUser());
		loadSheetSprites();
		loadSingletonObjects(); loadBinaryObjects();
		loadLightSources(); loadEffects(); loadProjectiles();
		loadWorld();	
	}
		
	public String pollUser(){
		String choice;
		if(debug){
			System.out.println("Reinitialize sheet sprites? Y or N");
			Scanner in = new Scanner(System.in);
			choice = in.next().toLowerCase();
			in.close();
		}
		else{ choice = "n"; }
		return choice;
	}
	
	public void loadMessage(){
		BufferedImage bf = null;
		try{ 
			bf = ImageIO.read(new File(menupath+"dialogue_box.png"));
			BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																		Transparency.BITMASK);
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { msg = copy; }
		} 
		catch(IOException ie) { System.out.println("Error loading: " + menupath + "dialogue_box.png"); }
	}
	
	//load in order: layer one, layer one over, layer two, layer two over
					//layer three, layer three over, layer four, layer four over,
					//layer five, layer five over
	private void loadWorld(){
		BufferedImage bf = null;
		worldRepo = new ArrayList<BufferedImage>();
		String filepath;
		for(int i = 1; i <= layers; i++){
			switch(i){
				case 1:
					filepath = worldpath + worldframe + layeroneframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					filepath = worldpath + worldframe + layeroneoverframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.TRANSLUCENT);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					break;
				case 2:
					filepath = worldpath + worldframe + layertwoframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					filepath = worldpath + worldframe + layertwooverframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.TRANSLUCENT);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					break;
				case 3:
					filepath = worldpath + worldframe + layerthreeframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					filepath = worldpath + worldframe + layerthreeoverframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.TRANSLUCENT);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					break;
				case 4:
					filepath = worldpath + worldframe + layerfourframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					filepath = worldpath + worldframe + layerfouroverframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.TRANSLUCENT);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					break;
				case 5:
					filepath = worldpath + worldframe + layerfiveframe;
					try{ 
						bf = ImageIO.read(new File(filepath));
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					filepath = worldpath + worldframe + layerfiveoverframe;
					try{ 
						bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.TRANSLUCENT);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { worldRepo.add(copy); }
					} 
					catch(IOException ie){ System.out.println("Error loading: " + filepath); }
					break;
			}
		}
	}
	
	//up, down, right, left, corresponds to Hero/Villain direction indices
	private void loadProjectiles(){
		projectRepo = new ArrayList<BufferedImage>();
		String filepath = objectpath + "bolt_up.png";
		try{
			BufferedImage bf = ImageIO.read(new File(filepath));
			BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																		Transparency.BITMASK);
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { projectRepo.add(copy); }
		}catch(IOException ie) { System.out.println("Error loading : " + filepath); }
		filepath = objectpath + "bolt_down.png";
		try{
			BufferedImage bf = ImageIO.read(new File(filepath));
			BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																		Transparency.BITMASK);
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { projectRepo.add(copy); }
		}catch(IOException ie) { System.out.println("Error loading : " + filepath); }
		filepath = objectpath + "bolt_right.png";
		try{
			BufferedImage bf = ImageIO.read(new File(filepath));
			BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																		Transparency.BITMASK);
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { projectRepo.add(copy); }
		}catch(IOException ie) { System.out.println("Error loading : " + filepath); }
		filepath = objectpath + "bolt_left.png";
		try{
			BufferedImage bf = ImageIO.read(new File(filepath));
			BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																		Transparency.BITMASK);
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { projectRepo.add(copy); }
		}catch(IOException ie) { System.out.println("Error loading : " + filepath); }
	}
	
	private void loadSheetSprites(){
			//clear spritesheet folder
		if(debug) { System.out.println("... Loading Types and States ..."); }
			//Hero Creation
		heroRepository = new HashMap<>();
		for(int i = 0; i < Hero.types; i++){
			Map<Integer, ArrayList<BufferedImage>> stateMap = new HashMap<>();
			for(int j = 0; j < Hero.states; j++){
				ArrayList<BufferedImage> state_animation = new ArrayList<BufferedImage>();
				int frames = sheet_manager.getHeroFrames(j);
				for(int k = 0; k < frames; k ++){
					String filepath = sheetpath + herotag + typetag + i + statetag + j + frametag + k + ".png";
					try{
						BufferedImage bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null); g2.dispose();
						if(copy != null) { state_animation.add(copy); }
					}
					catch(IOException ie) { System.out.println("Error loading sheet sprite: " + filepath); }
				}
				stateMap.put(j, state_animation);
				if(debug) { System.out.println("... Loading Hero Type: " + i + "  State: " + j + " ..."); }
			}
			heroRepository.put(i, stateMap);
		}
		
		//NPC Creation
		npcRepository = new HashMap<>();
		for(int i = 0; i < NPC.types; i++){
			Map<Integer, ArrayList<BufferedImage>> stateMap = new HashMap<>();
			for(int j = 0; j < NPC.states; j ++){
				ArrayList<BufferedImage> state_animation = new ArrayList<BufferedImage>();
				int frames = sheet_manager.getNPCFrames(j);
				for(int k = 0; k < frames; k++){
					String filepath = sheetpath + npctag + typetag + i + statetag + j + frametag + k + ".png";
					try{
						BufferedImage bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null); g2.dispose();
						if(copy != null) { state_animation.add(copy); }
					}
					catch(IOException ie) { System.out.println("Error loading sheet sprite: " + filepath); }
				}
				stateMap.put(j, state_animation);
				if(debug) { System.out.println("... Loading NPC Type: " + i + " State: " + j + " ..."); }
			}
			npcRepository.put(i, stateMap);
		}
		villainRepository = new HashMap<>();
		for(int i = 0; i < Villain.types; i++){
			Map<Integer, ArrayList<BufferedImage>> stateMap = new HashMap<>();
			for(int j = 0; j < Villain.states; j++){
				ArrayList<BufferedImage> state_animation = new ArrayList<BufferedImage>();
				int frames = sheet_manager.getVillainFrames(j);
				for(int k = 0; k < frames; k++){
					String filepath = sheetpath + viltag + typetag + i + statetag + j + frametag + k + ".png";
					try{
						BufferedImage bf = ImageIO.read(new File(filepath)); 
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null); g2.dispose();
						if(copy != null) { state_animation.add(copy); }
					}
					catch(IOException ie) { System.out.println("Error loading sheet sprite: " + filepath); }
				}
				stateMap.put(j, state_animation);
				if(debug) { System.out.println("... Loading Villain Type: " + i + " State: " + j +" ..."); }
			}
			villainRepository.put(i, stateMap);
		}
		sheet_manager = null;
	}

	/**
	 * @Method loadBinaryObjects
	 * 
	 * @description: Binary Objects are encoded in order: ShovelTile, TreasureChest, Switch.
	 * Each type has a primary and secondary image. The primary image is
	 * associated with state = 0. The secondary image is associated with
	 * state = 1. This is what is meant by Binary Objects.
	 */
	private void loadBinaryObjects(){
		String[][] binaryPath = new String[BIN_OBJECTS][2];
		String line;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(BINARY_IMG_PATHS));
			int counter = 0;
			while((line = bReader.readLine()) != null){ 
				if(line != null && !line.split("\\s")[0].equals(commentString)) { 
					binaryPath[counter][0] = line.split("\\s")[1];
					binaryPath[counter][1] = line.split("\\s")[2];
					counter++; 
				} 
			}
			bReader.close();
		} catch(IOException ie){ System.out.println("Error loading in paths :" + BINARY_IMG_PATHS);}
		binaryRepo = new ArrayList<BufferedImage>();
		for(int i = 0; i<BIN_OBJECTS; i++){
			String filepath_first = objectpath + binaryPath[i][0];
			String filepath_second = objectpath + binaryPath[i][1];
			BufferedImage bf1, bf2;
			try{
				bf1 = ImageIO.read(new File(filepath_first));
				bf2 = ImageIO.read(new File(filepath_second));
				BufferedImage copy1 = graphics_config.createCompatibleImage(bf1.getWidth(), bf1.getHeight(), 
																			Transparency.BITMASK);
				BufferedImage copy2 = graphics_config.createCompatibleImage(bf2.getWidth(), bf2.getHeight(), 
																			Transparency.BITMASK);
				Graphics2D g21 = (Graphics2D) copy1.getGraphics();
				Graphics2D g22 = (Graphics2D) copy2.getGraphics();
				g21.drawImage(bf1, 0 , 0, null);
				g22.drawImage(bf2, 0, 0, null);
				g21.dispose(); g22.dispose();
				if(copy1 !=null) { binaryRepo.add(copy1); }
				if(copy2 != null) { binaryRepo.add(copy2); }
			}catch(IOException ie){ System.out.println("Error loading in image files from: " 
														+ filepath_first + spc + filepath_second); }
		}
		
	}
	
	private void loadSingletonObjects(){
		String[] singlePaths = new String[SIN_OBJECTS];
		String line;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(SINGLETON_IMG_PATHS));
			int counter = 0;
			while((line = bReader.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){
					singlePaths[counter] = line.split("\\s")[1];
					counter++;
				}
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading in paths: " + SINGLETON_IMG_PATHS);}
		singleRepo = new ArrayList<BufferedImage>();
		for(int i = 0; i < SIN_OBJECTS; i++){
			String filepath = objectpath + singlePaths[i];
			try{ 
				BufferedImage bf = ImageIO.read(new File(filepath));
				BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(), 
																			Transparency.BITMASK);
				Graphics2D g2 = (Graphics2D) copy.getGraphics();
				g2.drawImage(bf, 0 , 0, null);
				g2.dispose();
				if(copy !=null) { singleRepo.add(copy); }
			}
			catch(IOException ie){ System.out.println("Error loading in image file: " 
														+ filepath);}
		}
	}
	
	private void loadEffects(){
		String[] effectPaths = new String[Effect.types];
		String line;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(EFFECT_IMG_PATHS));
			int counter = 0;
			while((line = bReader.readLine()) != null){
				if(line != null && !line.split("\\s")[0].equals(commentString)){
					effectPaths[counter] = line.split("\\s")[1];
					counter++;
				}
			}
			bReader.close();
		} catch(IOException ie){ System.out.println("Error loading: " + EFFECT_IMG_PATHS); }
		effectRepo = new ArrayList<BufferedImage>();
		for(int i = 0; i< Effect.types; i++){
			for(int j = 1; j <= Effect.frames; j++){
				String filepath = effectpath + effectPaths[i] + sep + effect + j + ".png";
				try{
					BufferedImage bf = ImageIO.read(new File(filepath));
					BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																				Transparency.BITMASK);
					Graphics2D g2 = (Graphics2D) copy.getGraphics();
					g2.drawImage(bf, 0 , 0, null);
					g2.dispose();
					if(copy !=null) { effectRepo.add(copy); }
				} catch(IOException ie) { System.out.println("Error loading: " + filepath); }
			}
		}
	}
	
	private void loadLightSources(){
		lightRepo = new HashMap<>();
		String[] lightPaths = new String[LightSource.types];
		String line;
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(LIGHT_IMG_PATHS));
			int counter = 0;
			while((line = bReader.readLine()) != null ){
				if(!line.equals("")){
					if(!line.split("\\s")[0].equals(commentString)){
						lightPaths[counter] = line.split("\\s")[1]; 
						counter++;
					}
				}
			}
			bReader.close();
		} catch(IOException ie){ System.out.println("Error loading: " + LIGHT_IMG_PATHS); }
		ArrayList<BufferedImage> stat, ani;
		stat = new ArrayList<BufferedImage>();
		ani = new ArrayList<BufferedImage>();
		for(int i = 0; i < LightSource.types; i++){
			
			if(i<LightSource.STATIONARY_TYPES){
				String filepath = lightpath + lightPaths[i];
				try{
					BufferedImage bf = ImageIO.read(new File(filepath));
					BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																				Transparency.BITMASK);
					Graphics2D g2 = (Graphics2D) copy.getGraphics();
					g2.drawImage(bf, 0 , 0, null);
					g2.dispose();
					if(copy !=null) { stat.add(copy); }
				}catch(IOException ie){ }
			}
			else{
				for(int j = 1; j <= LightSource.frames; j++){
					String filepath = lightpath + lightPaths[i] + sep + lightsource + j + ".png";
					try{
						BufferedImage bf = ImageIO.read(new File(filepath));
						BufferedImage copy = graphics_config.createCompatibleImage(bf.getWidth(), bf.getHeight(),
																					Transparency.BITMASK);
						Graphics2D g2 = (Graphics2D) copy.getGraphics();
						g2.drawImage(bf, 0 , 0, null);
						g2.dispose();
						if(copy !=null) { ani.add(copy); }
					}catch(IOException ie){ }
				}
			}
		}
		lightRepo.put(Integer.valueOf(LS_STATIONARY_INDEX), stat);
		lightRepo.put(Integer.valueOf(LS_ANIMATED_INDEX), ani);
		
	}
	
	public BufferedImage getVillainFrame(int type, int state, int frame){
		return villainRepository.get(type).get(state).get(frame);
	}
	
	public BufferedImage getNPCFrame(int type, int state, int frame){
		return npcRepository.get(type).get(state).get(frame);
	}
	
	public BufferedImage getHeroFrame(int type, int state, int frame){ return heroRepository.get(type).get(state).get(frame); }
	
	public BufferedImage getBinaryFrame(int type, int state){
		int index = type*2 + state;
		BufferedImage bf = binaryRepo.get(index);
		return bf;
	}
	
	public BufferedImage getSingletonFrame(int type){ return singleRepo.get(type); }
	
	public BufferedImage getLightSource(int type, int frame, boolean stat){
		//TODO: Fix Light Source Indexation Method. Surely there is a better way to
			//separate the stationary from the animated light sources?
		int index, animate; 
		if(stat){
			animate = LS_STATIONARY_INDEX;
			index = type;
		}
		else {
			animate = LS_ANIMATED_INDEX;
			index = LightSource.frames*(type - LightSource.STATIONARY_TYPES) + frame;
		}
		ArrayList<BufferedImage> repo = lightRepo.get(Integer.valueOf(animate));
		BufferedImage bf = repo.get(index);
		return bf;
	}
	
	public BufferedImage getEffectFrame(int type, int frame){
		int index = type * Effect.frames + frame;
		BufferedImage bf = effectRepo.get(index);
		return bf;
	}
	
	public BufferedImage getProjectileFrame(int type, int direction){
		return projectRepo.get(type+direction);
	}
	
	//Only returns message box currently!
		//Index the rest of the gui components and put in repo.
	public BufferedImage getMessageFrame(){
		return msg;
	}
	
	public BufferedImage getWorldFrame(int layer, boolean over){
		int index = 2*layer;
		if(over){ index ++; }
		return worldRepo.get(index);
	}
	
	public int getHeroFrames(int type, int state){ return heroRepository.get(type).get(state).size(); }
	
	public Point getHeroDimensions(){
		return new Point (heroRepository.get(0).get(0).get(0).getWidth(),
						  heroRepository.get(0).get(0).get(0).getHeight());
	}
	
	public int getVillainFrames(int type, int state){ return villainRepository.get(type).get(state).size(); }
	
	public Point getVillainDimensions(){
		return new Point (villainRepository.get(0).get(0).get(0).getWidth(),
						 villainRepository.get(0).get(0).get(0).getHeight());
	}
	
	public int getNPCFrames(int type, int state){ return npcRepository.get(type).get(state).size(); }
	
	public Point getNPCDimensions(){
		return new Point (npcRepository.get(0).get(0).get(0).getWidth(),
						  npcRepository.get(0).get(0).get(0).getHeight());
	}
	
	public Point getWorldDim(){
		Point p = new Point(worldRepo.get(0).getWidth(), worldRepo.get(0).getHeight());
		return p;
	}
	
	public void setWorldFrame(String choice){ worldframe = choice; loadWorld(); }
	
	public void setLayers(int lyrs) { layers = lyrs; }
	
}
	
	