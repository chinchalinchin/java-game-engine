package subterra.engine;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import javax.swing.*;

import subterra.engine.filehandling.FileHandler;
import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.engine.imagehandling.menu.StartMenu;
import subterra.geometry.Point;

public class Cradle extends JFrame{
	
		/**
		 * @Cradle
		 * 
		 * @author chinchalinchin
		 * 
		 * @field viewer: View. The View is where the game is actually rendered.
		 * 
		 * @field world: GameWorld. This holds all of the information about the 
		 *  game.
		 *  
		 * @field handler: FileHandler. This will control all interaction with the 
		 * configuration files on the deployed computer. 
		 * 
		 * @field loader: SpriteImageLoader. This is a repository of all SpriteImages
		 * in-game, with the exception of in-game menus.
		 * 
		 * @description
		 * this class acts as a foundation for the rest of the program. The main 
		 * creates itself and then initializes the menu. After user makes menu 
		 * selection, the program either loads the world or iterates through 
		 * menu until world is loaded. Once world file has been selected 
		 * (new or loaded), the cradle creates a FileHandler. The FileHandler
		 * parses saved data and populates ArrayList<Sprite>'s with world objects.
		 * 
		 *  Once the data is loaded, the populated lists are passed to the GameWorld
		 *  object. The initialized GameWorld object is passed into a View object. 
		 *  The game thread inside of View is invoked. The Cradle remains idle until
		 *  the user selects save from the in-game menus or dies.
		 *  
		 *  The Cradle is an extension of the JFrame class. It is the container
		 *  the View is placed in. The View is an extension of the JPanel class.
		 *  See View documentation for more details.
		 *  
		 */
	
		private static final long serialVersionUID = 1L;
		public static final String newGame = "new_world", 
							   fileOne = "world_1",
							   fileTwo = "world_2",
							   fileThree = "world_3",
							   worldpath = "world"+System.getProperty("file.separator")+"world_";
		
		private boolean debug = false;
		private String worldfile;
		private View viewer;
		private GameWorld world;
		private FileHandler handler;
		private SpriteImageLoader loader;
		private Controller pad;
		
		public Cradle(int w, int h){
			super();
			if(debug){ System.out.println("Making Cradle..."); }
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setSize(w, h);
			world = new GameWorld();
		}
		
		private void addView(){ 
			if(debug) { System.out.println("Making Controller...");}
			pad = new Controller();
			this.addKeyListener(pad);
			viewer = new View(world, pad, loader); 
			if(debug) { System.out.println("Adding View...");}
			this.add(viewer);
			this.setVisible(true);
		}
		
		/**@Method getMenuSelection
		 * 
		 * @description creates the main menu and waits for user to make selecton.
		 * selection is then formatted to the file path the user has chosen.
		 * 
		 * @return String : represents the file path of the game to be loaded.
		 */
		private String getMenuSelection(){
			if(debug){ System.out.println("Initializing Start Menu..."); }
			StartMenu sm = new StartMenu();
			this.addKeyListener(sm);
			this.add(sm);
			this.setVisible(true);
			sm.makeMenu();
			if(debug){ System.out.println("Waiting for User Selection..."); }
			while(!sm.isSelected()){ 
				Thread.yield();
				sm.paintMenu();	
			}
			int select = sm.getSelection();
			String userSelection;
			int subselect = sm.getFileSelection();
			switch(subselect){
				case StartMenu.file1:
					worldfile = fileOne;
					break;
				case StartMenu.file2:
					worldfile = fileTwo;
					break;
				case StartMenu.file3:
					worldfile = fileThree;
					break;
			}
			switch(select){
				case StartMenu.newG:
					userSelection = newGame;
						break;
				case StartMenu.loadG:
					switch(subselect){
						case StartMenu.file1:
							userSelection = fileOne;
							break;
						case StartMenu.file2:
							userSelection = fileTwo;
							break;
						case StartMenu.file3:
							userSelection = fileThree;
							break;
						default:
							userSelection = fileOne;
							break;
						}
					break;
				default:
					userSelection = newGame;
					break;
				}
			this.removeKeyListener(sm);
			this.setVisible(false);
			if(debug) { 
				String newOrLoad;
				if(userSelection == newGame) { newOrLoad = "New Game"; }
				else { newOrLoad = "Load Game"; }
				System.out.println("User Selection: " + newOrLoad); }
			if(debug) { System.out.println("File Selection: " + worldfile); }
			return userSelection;
		}
		
		public void getWorld(String choice){
			if(debug) { System.out.println("Initializing File..."); }
			handler = new FileHandler(choice, debug);
			if(debug) { System.out.println("Current World Frame Set: " + handler.getWorldFrame()); }
			if(debug) { System.out.println("Hero loaded at X: " + handler.getHero().getMin().getX() +
														 " Y: "  + handler.getHero().getMin().getY()); }
			if(debug) { System.out.println("Loading Sprite Images..."); }
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
			loader = new SpriteImageLoader(handler.getWorldFrame(), handler.getLayers(), gc, debug);
			world = new GameWorld();
			world.setDimensions(loader.getWorldDim());
			if(debug) { System.out.println("Populating GameWorld..."); }
			infoToWorld();
		}
		
		private void infoToWorld(){			
			if(handler.getHero() != null) { world.setHero(handler.getHero()); }
			if(handler.getVillains() != null) { world.setVillains(handler.getVillains()); }
			if(handler.getNPCs() != null) { world.setNPCs(handler.getNPCs()); }
			if(handler.getDoors() != null) { world.setDoors(handler.getDoors()); }
			if(handler.getPortals() != null) { world.setPortals(handler.getPortals()); }
			if(handler.getHitboxes() != null) { world.setHitboxes(handler.getHitboxes()); }
			if(handler.getHackboxes() != null) { world.setHackboxes(handler.getHackboxes()); }
			if(handler.getItemPouch() != null) { world.setItemPouch(handler.getItemPouch()); }
			if(handler.getTreasures() != null) { world.setTreasures(handler.getTreasures()); }
			if(handler.getShovelTiles() != null) { world.setShovelTiles(handler.getShovelTiles()); }
			if(handler.getLights() != null) { world.setLights(handler.getLights()); }
			if(handler.getEquipmentTriggers() != null) { world.setEquipmentTriggers(handler.getEquipmentTriggers()); }
			if(handler.getSwitches()!= null) { world.setSwitches(handler.getSwitches()); }
			if(handler.getPressurePlates() != null) { world.setPressurePlates(handler.getPressurePlates()); }
			if(handler.getGates() != null) { world.setGates(handler.getGates()); }
			if(handler.getBarrels() != null) { world.setBarrels(handler.getBarrels()); }
			if(handler.getCrates() != null) { world.setCrates(handler.getCrates()); }
			if(handler.getBooks() != null) { world.setBooks(handler.getBooks()); }
			if(handler.getSigns() != null) { world.setSigns(handler.getSigns()); }
			if(handler.getSaveCrystals() != null) { world.setSaveCrystals(handler.getSaveCrystals()); }
			if(handler.getEffects() != null) { world.setEffects(handler.getEffects()); }
			if(handler.getDialogue()!=null) { world.setDialogue(handler.getDialogue()); }
			if(handler.getScenes()!=null) { world.setScenes(handler.getScenes()); }
			world.setLayer(handler.getLayer());
		}
		
		private void infoToHandler(){
			if(world.getHitboxes() != null) { handler.setHitboxes(world.getHitboxes()); }
			if(world.getHackboxes() != null) { handler.setHackboxes(world.getHackboxes()); }
			if(world.getNPCs() != null) { handler.setNPCs(world.getNPCs()); }
			if(world.getDoors() != null) { handler.setDoors(world.getDoors()); }
			if(world.getPortals() != null) { handler.setPortals(world.getPortals()); }
			if(world.getHero() != null) { handler.setHero(world.getHero()); }
			if(world.getVillains() != null){ handler.setVillains(world.getVillains()); }
			if(world.getItemPouch() != null) { handler.setItemPouch(world.getItemPouch()); }
			if(world.getTreasures() != null) { handler.setTreasures(world.getTreasures()); }
			if(world.getPressurePlates() != null) { handler.setPressurePlates(world.getPressurePlates()); }
			if(world.getGates() != null) { handler.setGates(world.getGates()); }
			if(world.getShovelTiles() != null) { handler.setShovelTiles(world.getShovelTiles()); }
			if(world.getEquipmentTriggers() != null) { handler.setEquipmentTriggers(world.getEquipmentTriggers()); }
			if(world.getLights() != null) { handler.setLights(world.getLights()); }
			if(world.getSwitches() != null){ handler.setSwitches(world.getSwitches()); }
			if(world.getBarrels() != null) { handler.setBarrels(world.getBarrels()); }
			if(world.getBooks() != null) { handler.setBooks(world.getBooks()); }
			if(world.getCrates() != null) { handler.setCrates(world.getCrates()); }
			if(world.getSigns() != null) { handler.setSigns(world.getSigns()); }
			if(world.getSaveCrystals() != null) { handler.setSaveCrystals(world.getSaveCrystals()); }
			if(world.getEffects() != null) { handler.setEffects(world.getEffects()); }
			if(world.getDialogue() != null){ handler.setDialogue(world.getDialogue()); }
			if(world.getScenes() != null) {handler.setScenes(world.getScenes()); }
			handler.setLayer(world.getLayer());
		}
	
		private void startGame(){
			if(debug) { System.out.println("Starting Game...."); } 
			Thread t = new Thread(viewer);
			t.start();
			while(t.isAlive()){
				
				if(!viewer.isSaving() && !viewer.isPortaled()){
					Thread.yield();
				}
					//TODO: And if viewer hasn't triggered cutscenes
				
				//END STATE: If user selects save from menu.
				if(viewer.isSaving()){
					if(debug){ System.out.println("Saving to file: " + worldfile); } 
					world = viewer.getWorld();
					infoToHandler();
					handler.setSavePath(worldfile);
					handler.saveWorld();
					viewer.flushSaving();
				}
				
				//END STATE: If user triggers cutscene.
				//if(viewer.isPlotting()){
					//remove view
					//retrieve scene number from View
						//and plot phase from GameWorld's Dialogue
					//make new controller for cutscene
					//make cutscene. pass gameworld, spriteimageloader, controller
					//add cutscene
					//start cutscene
					//wait for cutscene to end
					//retrieve NEW plot phase, if applicable
						//reinitialize dialogue
						//set plot phase in file handler
					//add view 
					//flush plotting
				
				//END STATE: If user is changing world frames
				if(viewer.isPortaled()){
					String choice = viewer.getPortalChoice();
					Point insert = viewer.getNextInsertionPoint();
					int layer = viewer.getNextInsertionLayer();
					if(debug) { System.out.println("Portalling to: " + choice); } 
					if(debug) { System.out.println("Inserting Hero at X: " + insert.getX() + 
												   " Y: " + insert.getY());}
					world = viewer.getWorld();
					infoToHandler(); //needed for equipment triggers, hero and item pouch!!1
					handler.setConfigPath(worldpath+choice);
					handler.portalInit();
					handler.parseWorld();
					infoToWorld();
					loader.setLayers(handler.getLayers());
					loader.setWorldFrame(choice);
					world.setDimensions(loader.getWorldDim());
					world.getHero().setMin(insert);
					world.setLayer(layer);
					viewer.setLoader(loader);
					viewer.setWorld(world);
					viewer.flushPortaled();
				}
				
				//END STATE: If user dies.
				//if(!world.getHero().isAlive()){ 
					//handler.setPath(worldfile);
					//handler.parseWorld();
					//infoToWorld();
					//startGame();
				//}
					//TODO: What if user dies without saving from new game? 
					
				//END STATE: 
			}
			
			
		}
		
		/**@Method main
		 * 
		 * @description short and sweet. where all the magic happens. the Cradle
		 * creates itself and then initiates the main menu. After user makes a 
		 * choice, the Cradle adds a View to itself.
		 * 
		 * @param args
		 */
		//START: Main Thread
		public static void main(String args[]){
			//TODO: Parse command line input into frame dimensions
			Cradle c = new Cradle(400, 400);
			String choice = c.getMenuSelection();
			c.getWorld(choice);
			c.addView();	
			c.startGame();
		}
		//END: Main Thread
}
