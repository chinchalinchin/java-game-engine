package subterra.engine;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

import subterra.engine.imagehandling.MessageBox;
import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.engine.imagehandling.RenderUtilities;
import subterra.engine.imagehandling.menu.GameMenu;
import subterra.geometry.Functions;
import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;
import subterra.library.Book;
import subterra.library.Door;
import subterra.library.Effect;
import subterra.library.Gate;
import subterra.library.Hackbox;
import subterra.library.Hero;
import subterra.library.Item;
import subterra.library.LightSource;
import subterra.library.NPC;
import subterra.library.Portal;
import subterra.library.PressurePlate;
import subterra.library.Projectile;
import subterra.library.ShovelTile;
import subterra.library.Sign;
import subterra.library.Switch;
import subterra.library.TreasureChest;
import subterra.library.Villain;


@SuppressWarnings("unused")
public class View extends JPanel 
				  implements Runnable
				   { 
	

/**@View
 *
 * @description  this is where the game thread lives. As such, the View class implements the Runnable
 *  interface. The View class interacts with the the GameWorld as a proxy for the user. User input is 
 *  processed with the Controller. The Controller listens for user input events and alters its fields 
 *  accordingly. While the game thread is running, View polls the state of the Controller and updates 
 *  the GameWorld object. 
 *  
 *  The View also serves as a canvas for the game to be rendered upon. View extends the JPanel class 
 *  and circumvents the repaint method of its parent by double buffering. A BufferedImage collects all 
 *  the onscreen objects and then is painted directly onto the View object once a single frame has been 
 *  collated. This is to reduce the flickering resulting from instantenous updates to the GameWorld object 
 *  in the game thread and the separate thread that delegates repaint. 
 *  
 *  Game thread ends if the user selects save from the menus rendered in-game. At that point, the thread
 *  terminates and the parent thread in the Cradle class takes over. The Cradle will then call the View
 *  once last time to get back the GameWorld object for file handling purposes. Once that is accomplished,
 *  View may be reinitialized in Cradle class if user selects new or load game from parent thread.
 *  
 *  @field gc: 
 *  
 *  @field buffer:
 *  
 *  @field equipMap: int[]. Holds the equipment currently equipped by user. Can be edited through 
 *  equipmenu sub GameMenu. Affects equipsub GameMenu's current display. Note, equipsub is the 
 *  transculent menu in the bottom left, whereas the equipment sub GameMenu is the equipment menu
 *  brought up through the main menu GameMenu. Passed into the equipsub and equipment menu whenever 
 *  they need to render user's equipment selections.
 *  
 *  @field pad: 
 *  
 *  @field world: GameWorld. contains all the objects the user interacts with throughout his journey. also
 *  contains the user's avatar. See GameWorld documentation for more details. 
 *  
 *  @field menu: GameMenu. Holds the main menu GameMenu.
 *  
 *  @field sub: GameMenu. Holds the current sub GameMenu selected from the Main Menu or brought up
 *  through other means.
 *  
 *  @field equipsub: GameMenu. the equip sub menu. holds all special items the user can employ through his 
 *  journey.
 *  
 *  @field msg: MessageBox. holds messages the user encounters through his journey. holds image files
 *  associated with the appearance of the message. also contains a text file that has been parsed 
 *  internally.
 *  
 *  @field live: boolean. determines whether or not game thread is alive. Initialized as true and only 
 *  turned to false is user selects save from in-game menus.
 *  
 *  @field paused: boolean. determines whether or not game is paused. Set as true is user opens menu or pauses
 *  game. Prevents game thread from allowing Controller to update GameWorld. Effectively freezes user input from
 *  GameWorld while still allowing user to interact with View object via rendered menus. Paused can either signify
 *  a pause, in which case nothing addition is to be rendered, or it could signify the user has brought up the menu.
 *  In the latter case, a GameMenu has been created and is ready to be render.
 *  
 *  @field reading: boolean. similar to paused. thrown if the user encounters a MessageBox. if reading is true,
 *  a MessageBox has been created in game thread and ready to be rendered.
 *  
 *  @field saving: boolean. triggered if the user selects save from the game menus. terminates game thread at the
 *  end of its next iteration.
 *  
 *  @field subbing: boolean. triggered if user activates a sub menu. if true, there is a GameMenu ready to be rendered.
 *  
 *  @field viewing: boolean. triggered if user activates a cutscene. if true, the game thread terminates at the end 
 *  of next iteration.
 *  
 */

	private static final long serialVersionUID = 1L;
	private static final long REFRESH = 45;
		//Refresh rate measured in milliseconds
	private static final int NO_DELAYS_PER_YIELD = 5;
	private static final int MAX_FRAME_SKIPS = 2;
	private static final boolean NPC_SAVE = true;
	public static final int equipMap1 = 0, equipMap2 = 1, equipMap3 = 2;
	
	private int frames, updates; 
	private GraphicsConfiguration gc;
	private BufferedImage buffer;
	private int[] equipMap; 
	private Controller pad;
	private GameWorld world;
	private SpriteImageLoader loader;
	private GameMenu menu, sub, equipsub;
	private MessageBox msg;
	private String portalchoice, scenechoice;
	private Point nextInsertion;
	private int nextLayer;
	private boolean live, paused, reading, 
					saving, subbing, scened, portaled;
	
	public View(GameWorld thisWorld, Controller thisPad, SpriteImageLoader thisLoader){     
		super();
		world = thisWorld;
		loader = thisLoader;
		equipMap = new int[GameMenu.equipsubselections];
		for(int i = 0; i < GameMenu.equipsubselections; i++){
			equipMap[i] = GameMenu.equipmentselections + 1; //ensuring nothing is equipped
		}
		pad = thisPad;
		configure();
	}
	
	private void configure(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
			//INITIALIZE HERO
		if(world.getHero().getCurrentFrame() == null) { 
			world.getHero().animate(loader); 
			((Hero) world.getHero()).configureDimensions(loader);
		} 
			//INITIALIZE VILLAINS
		ArrayList<Sprite> initSprites = world.getVillains();
		if(initSprites.size() > 0){ for(Sprite s : initSprites){
				s.animate(loader);
				((Villain) s).configureDimensions(loader);
			}
		}
			//INITIALIZE NPCS
		initSprites = world.getNPCs();
		if(initSprites.size() > 0) { for(Sprite s: initSprites) { 
				s.animate(loader);
				((NPC) s).configureDimensions(loader);
			}
		}
			//INITIALIZE BINARY OBJECTS
		initSprites = world.getTreasures();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites){ ((TreasureChest) s).init(loader); } } //ensure treasures are initialized
		initSprites = world.getSwitches();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites) { ((Switch) s).init(loader); } } //ensure switches are initialized
		initSprites = world.getShovelTiles();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites) { ((ShovelTile) s).init(loader); } }//ensure shoveltiles are initialized
		initSprites = world.getHackboxes();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites) { ((Hackbox) s).init(loader); } }
		initSprites = world.getPressurePlates();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites){ ((PressurePlate) s).init(loader); } }
		initSprites = world.getGates();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites) { ((Gate) s).init(loader); } }
			//INITIALIZE SINGLETON OBJECTS
		initSprites = world.getBarrels();
		if(initSprites.size() > 0) { for(Sprite s : initSprites) { s.animate(loader); } }
		initSprites = world.getBooks();
		if(initSprites.size() > 0) { for(Sprite s : initSprites) { s.animate(loader); } }
		initSprites = world.getCrates();
		if(initSprites.size() > 0) { for(Sprite s : initSprites) { s.animate(loader); } }
		initSprites = world.getSigns();
		if(initSprites.size() > 0) { for(Sprite s: initSprites) { s.animate(loader); } }
		initSprites = world.getSaveCrystals();
		if(initSprites.size() > 0) { for(Sprite s: initSprites) { s.animate(loader); } }
			//INITIALIZE 0THER SPRITES
		initSprites = world.getLights();
		if(initSprites.size() > 0 ){ for(Sprite s : initSprites) { ((LightSource) s).randomizeFrame(); ((LightSource) s).init(loader); } }
		initSprites = world.getEffects();
		if(initSprites.size() > 0 ) { for(Sprite s : initSprites) { ((Effect) s).randomizeFrame(); ((Effect) s).init(loader); } }
	}
	
	private void makeMenu(boolean npcSaving){ 
		double w = this.getWidth();
		double h = this.getHeight();
		double leftoverW = w - GameMenu.defaultWidth;
		double leftoverH = h - GameMenu.defaultHeight;
		menu = new GameMenu(new Point(7*leftoverW/8, leftoverH/8), GameMenu.mainmenu);
		menu.initMain(npcSaving);
	}

	private void makeSubMenu(int userSelection){
		double w = this.getWidth();
		double h = this.getHeight();
		double leftoverW = w - GameMenu.defaultWidth;
		double leftoverH = h - GameMenu.defaultHeight;
		switch(userSelection){
			case GameMenu.statusmenu:
				sub = new GameMenu(new Point(7*leftoverW/8 - GameMenu.defaultStatusWidth, leftoverH/8), 
									  GameMenu.statusmenu);
				sub.formatStatus((Hero) world.getHero());
					//TODO: Pass in plot object to format status
				break;
			case GameMenu.itemmenu:
				double leftOver = GameMenu.defaultEquipSubWidth - GameMenu.defaultItemWidth;
				sub = new GameMenu(new Point(20 + leftOver/2, h - GameMenu.defaultItemHeight - GameMenu.defaultEquipSubHeight -20), 
						  GameMenu.itemmenu);
				sub.formatItems(world.getItemPouch());
				break;
			case GameMenu.equipmenu:
				sub = new GameMenu(new Point(7*leftoverW/8 - GameMenu.defaultEquipWidth, leftoverH/8),
									GameMenu.equipmenu);
				sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
				break;
			case GameMenu.mapmenu:
				break;
			case GameMenu.savemenu:
				break;
		}
	}
	
	private void initializeEffectAt(int type, Point p, int anchor){
		Effect e = new Effect(p,type, Effect.IN_GAME);
		e.setAnchorIdentity(anchor);
		e.setLayer(world.getLayer());
		ArrayList<Sprite> effects;
		if(world.getEffects() == null) { effects = new ArrayList<Sprite>(); }
		else{ effects = world.getEffects(); }
		effects.add(e);
		if(world.getEffects() == null) { world.setEffects(effects); }
	}
	
	/**
	 * @Method run: the implemented method of the Runnable interface. Enables threads.
	 * 
	 * @description: the basic structure of the game loop is pseudo-coded as follows:
	 * 		while game thread is alive
	 * 			parse user input
	 * 			check if game is paused or if user is reading a message
	 * 					if neither obtain, update GameWorld
	 * 			render to buffer
	 * 			paint to Screen
	 * 			calculated frame and update rates
	 * 		
	 */
	//START: Game Thread
	public void run() {
		//Thread variables
			//beforeTime: Time before game loop iteration
			//afterTime: Time after game loop iteration
			//timeDiff: Time taken for game loop
			//sleepTime: Excess time left until next refresh
			//overSleepTime: Excess time spent yielding to catch up to refresh
			//noDelays:
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime, excess; 
		int noDelays;
		
		//Initialize thread variables
		overSleepTime = 0L; noDelays = 0; excess = 0L;
		beforeTime = System.nanoTime()/1000000L;
			//Calculate time of Thread Start
		frames = 0;
		updates = 0;
		paused = false;
		reading = false;
		saving = false;
		scened = false;
		portaled = false;
		subbing = false;
		live = true;
		
		if(equipsub == null){ equipsub = new GameMenu(new Point(10, 
																this.getHeight() - GameMenu.defaultEquipSubHeight - 10), 
														GameMenu.equipsubmenu);}
		equipsub.formatSubEquipment(equipMap);	
		
		while(live){
			if(!saving && !portaled && !scened){
				//START: Game Loop iteration
				parseInput();
				if(!paused) { updateAll(); }
				if(!portaled){ render(); } //can be changed in updateAll()
				paintScreen();
				//END: Game Loop iteration
			
				afterTime = System.nanoTime()/1000000L;
					//Calculate time after Game Loop Iteration
				timeDiff = afterTime - beforeTime;
				sleepTime = (REFRESH - timeDiff) - overSleepTime;
					//Calculate time needed to catch up to refresh rate
			
				if(sleepTime>0){
					try { Thread.sleep(sleepTime); }
					catch (InterruptedException e){ }
					overSleepTime = System.nanoTime()/1000000L - afterTime - sleepTime;
						//Calculate time overslept
				}
				else{
					excess -= sleepTime;
						//Thread ahead of schedule.
					overSleepTime = 0;
					if(++noDelays >= NO_DELAYS_PER_YIELD){
						//If a delay has happened before, yield to let everything else
						//catch up.
						Thread.yield();
						noDelays = 0;
					}
						
				}
				beforeTime = System.nanoTime()/1000000L;
					//Recalculate time before game loop iteration
			
				if(!paused && !reading){
					int skips = 0;
					while(( excess > REFRESH) && (skips < MAX_FRAME_SKIPS)){
						//If thread is too much ahead of schedule, update
						//without a render or paint. Decrement excess by
						//refresh rate. 
						excess -= REFRESH;
						updateAll();
						skips++;
					}
				}
			}
			Thread.yield();
		}
		
	}
	//END: Game Thread
	
	/**
	 * @Method parseInput:
	 * 
	 * @description parseInput polls the Controller object and passes the 
	 * information into the GameWorld. This method is the only point of contact
	 * between the View and Controller classes. The Controller is always insulated
	 * from the GameWorld through the View. 
	 * 
	 * parseInput passes Controller state to Hero. 
	 * parseInput handles oversees animation of the user's Hero by calling the 
	 * animate method on the Hero class. 
	 * 
	 * Pseudo-code of parseInput
	 * 
	 * if game thread is alive
	 * 		if game is not paused
	 * 			get Controller state
	 * 			if hero is not attacking and not shovelling and not powered up and not stunned
	 * 				switch on Controller state
	 * 					if not reading
	 * 						-walking: set Hero's state to walking
	 * 								  move Hero
	 * 								  animate Hero
	 * 						-interact: if Hero intersects Door/NPC/TreasureChest
	 * 								   perform interaction
	 * 						-menu:
	 * 						-item:
	 * 						-attack:
	 * 						-pause:
	 * 						-engage:
	 * 						-equip1/2/3:
	 * 					else
	 * 						-walking: move through message
	 * 						-interact: 
	 * 						-menu:
	 * 						-item:
	 * 						-attack:
	 * 						-pause:
	 * 						-engage:
	 * 						-equip1/2/3:
	 * 			if hero is attacking and not shovelling
	 * 				animate hero
	 * 				determine if attacking animation has concluded
	 * 			if hero is shovelling and not attacking
	 * 				dig through onscreen and intersected shoveltiles
	 * 				determine if shovelling animation has concluded
	 * 		else if game is paused
	 * 			get Controller state
	 * 			if no sub menus exist (main menu exists)
	 * 				switch on Controller state
	 * 					-walking: change main menu selection
	 * 							  flush Controller
	 * 					-interact/attack: create submenu
	 * 									  flush Controller
	 * 					-menu/engage: destroy main menu
	 * 								  set paused to false			
	 * 			else sub menus exist
	 * 				if main menu exists (coming from main menu)
	 * 					get main menu selection
	 * 					get Controller state
	 * 						switch on main menu selection
	 * 							-statusmenu:
	 * 							-itemmenu: 
	 * 							-equipmenu: switch on Controller state
	 * 											equip1/2/3: Assign EquipMap.
	 * 							-savemenu
	 * 							-mapmenu
	 * 							switch on Controller state
	 * 				else main menu does not exist (coming from item menu)
	 * 							
	 */
	private void parseInput() {  
		//START: Conditional on game looping
		if(live){
			//START: Conditional on game not paused
			if(!paused){
				int input = pad.getState();
				int index, dir;
				Hero h = (Hero) world.getHero();
				ArrayList<Sprite> npcs = world.getNPCs();
				ArrayList<Sprite> doors = world.getDoors();
				ArrayList<Sprite> lights = world.getLights();
				ArrayList<Sprite> barrels = world.getBarrels();
				ArrayList<Sprite> crates = world.getCrates();
				ArrayList<Sprite> signs = world.getSigns();
				ArrayList<Sprite> books = world.getBooks();
				ArrayList<Sprite> crystals = world.getSaveCrystals();
				ArrayList<Sprite> treasures = world.getTreasures();
				ArrayList<Sprite> switches = world.getSwitches();
				ArrayList<Sprite> st = world.getShovelTiles();
				Point wD = world.getDimensions();
				Point sD = new Point(this.getWidth(), this.getHeight());
				
				//START: Conditional: Hero: Nothing
				// *** DEFAULT INPUT LOOP PASS *** //
				if(!h.getAttacking() && !h.getShovelling() && !h.getPowered() && 
						!h.getStunned() && !h.getShielding() && !h.getBowing()){
					
					//START: Switch on input
					switch(input){
				
							//START: WalkUp Assignment
						case Controller.walkup:
							if(!reading){
								if(h.getState() != Hero.wlk_up){  h.resetState(); h.setState(Hero.wlk_up); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveY(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Crates
								for(Sprite c: crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveY(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b: barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveY(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								
							}
							else{ 
								//if reading, MessageBox exists
								msg.previousLine(loader); 
								pad.flushControls(); 
							}
							break;
							//END: WalkUp Assignment
						
							//START: WalkDown Assignment
						case Controller.walkdown:
							if(!reading){
								if(h.getState() != Hero.wlk_down){ h.resetState(); h.setState(Hero.wlk_down); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveY(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveY(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveY(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
							}
							else{ 
								msg.nextLine(loader); 
								pad.flushControls(); 
							}
							break;
							//END: WalkDown assignment	
						
							//START: WalkLeft assignment
						case Controller.walkleft:
							if(!reading){
								if(h.getState() != Hero.wlk_left){ h.resetState(); h.setState(Hero.wlk_left); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(-((double)h.getWlkSPD())/100);
											}
										}
									}
								}
							}
							break;
							//END: WalkLeft assignment	
						
						
							//START: WalkUpLeft assignment
						case Controller.walkupleft:
							if(!reading){
								if(h.getState() != Hero.wlk_up_left){ h.resetState(); h.setState(Hero.wlk_up_left); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												n.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												c.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												b.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								
							}
							break;
							//END: WalkUpLeft assignment	
						
							//START: WalkDownLeft assignment
						case Controller.walkdownleft:
							if(!reading){
								if(h.getState() != Hero.wlk_down_left){ h.resetState(); h.setState(Hero.wlk_down_left); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												n.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												c.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												b.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
							}
							break;
							//END: WalkDownLeft assignment
						
							//START: WalkRight assignment
						case Controller.walkright:
							if(!reading){
								if(h.getState() != Hero.wlk_right){ h.resetState(); h.setState(Hero.wlk_right); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(((double)h.getWlkSPD())/100);
											}
										}
									}
								}
							}
							break;
							//END: WalkRight assignment	
						
							//START: WalkUpRight assignment	
						case Controller.walkupright:
							if(!reading){
								if(h.getState() != Hero.wlk_up_right){ h.resetState(); h.setState(Hero.wlk_up_right); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												n.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates) {
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												c.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b : barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												b.moveY(-((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
							}
							break;
							//END: WalkUpRight assignment	
						
							//START: WalkDownRight assignment
						case Controller.walkdownright:
							if(!reading){
								if(h.getState() != Hero.wlk_down_right){ h.resetState(); h.setState(Hero.wlk_down_right); }
								h.animate(loader);
								//Push NPCS
								for(Sprite n: npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(n.getCollisionBox())){
												n.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												n.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Crates
								for(Sprite c : crates){
									if(c.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), c.getMin(), c.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(c.getCollisionBox())){
												c.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												c.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
								//Push Barrels
								for(Sprite b: barrels){
									if(b.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), b.getMin(), b.getMax(), sD, wD)){
											if(h.getCollisionBox().intersectsQuad(b.getCollisionBox())){
												b.moveX(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
												b.moveY(((double)h.getWlkSPD())/(Math.sqrt(2)*100));
											}
										}
									}
								}
							}
							break;
							//END: WalkDownRight assignment	
						
							//START: Interact assignment
						case Controller.interact:
							//START: Conditional on Not Reading
							if(!reading){
	
								boolean doored = false, npced = false;
								//Determine if NPC'ed
								for(Sprite n : npcs){
									if((n.getLayer() == world.getLayer())){
										if(RenderUtilities.onScreen(h.getMin(), n.getMin(), n.getMax(), sD, wD)){
											if(h.getFrameBox().intersectsQuad(n.getCollisionBox())){ npced = true; }
										}
									}
								}
								
								//START: HERO DOOR DETECTION
								if(!npced){
									for(Sprite door : doors){
										if(RenderUtilities.onScreen(h.getMin(), door.getMin(), door.getMax(), sD, wD)){
											if(door.getLayer() == world.getLayer()){
												if(!doored){
													if(h.getCollisionBox().intersectsQuad(door.getCollisionBox())){ 
														doored = true;
														world.setLayer(((Door) door).getConnectionLayer());
														h.setMin(((Door) door).getConnectionInsert());
													}	
												}
											}
										}
									}
								}
								//END: HERO DOOR DETECTION
								
								//START:DIALOGUE & SAVE DETECTION
								if(!doored){
									for(Sprite npc: npcs){
										if(npc.getLayer() == world.getLayer()){
											if(RenderUtilities.onScreen(h.getMin(), npc.getMin(), npc.getMax(), sD, wD)){
												if(h.getFrameBox().intersectsQuad(npc.getCollisionBox())){
													NPC n = (NPC) npc;
													if(n.getMsgLookUp() != Sprite.NO_MSG){
														String m = world.getDialogue().findDialogue(n.getMsgLookUp());
														double y = this.getHeight()/4 + MessageBox.defaultHeight/2;
														msg = new MessageBox(new Point(40,y), m);
														msg.init(loader); 
														reading = true;
													}
												}
											}
										}
									}
									
									for(Sprite s : signs){
										if(s.getLayer() == world.getLayer()){
											if(RenderUtilities.onScreen(h.getMin(), s.getMin(), s.getMax(), sD, wD)){
												if(h.getFrameBox().intersectsQuad(s.getCollisionBox())){
													Sign si = (Sign) s;
													if(si.getMsgLookUp() != Sprite.NO_MSG){
														String m = world.getDialogue().findDialogue(si.getMsgLookUp());
														double y = this.getHeight()/4 + MessageBox.defaultHeight/2;
														msg = new MessageBox(new Point(40,y), m);
														msg.init(loader); 
														reading = true;
													}
												}
											}
										}
										
									}
									for(Sprite bo : books){
										if(bo.getLayer() == world.getLayer()){
											if(RenderUtilities.onScreen(h.getMin(), bo.getMin(), bo.getMax(), sD, wD)){
												if(h.getFrameBox().intersectsQuad(bo.getCollisionBox())){
													Book book = (Book) bo;
													if(book.getMsgLookUp() != Sprite.NO_MSG){
														String m = world.getDialogue().findDialogue(book.getMsgLookUp());
														double y = this.getHeight()/4 + MessageBox.defaultHeight/2;
														msg = new MessageBox(new Point(40,y), m);
														msg.init(loader); 
														reading = true;
													}
												}
											}
										}
										
									}
									for(Sprite sv : crystals){
										if(sv.getLayer() == world.getLayer()){
											if(RenderUtilities.onScreen(h.getMin(), sv.getMin(), sv.getMax(), sD, wD)){
												if(h.getFrameBox().intersectsQuad(sv.getCollisionBox())){
													paused = true; makeMenu(NPC_SAVE);
												}
											}
										}
										
									}
								}
								//END: DIALOGUE & SAVE DETECTION
						
								//START: TREASURE CHEST DETECTION
								for(Sprite treasure : treasures){
									if( treasure.getLayer() == world.getLayer()){
										if(RenderUtilities.onScreen(h.getMin(), treasure.getMin(), treasure.getMax(), sD, wD)){
											if(h.getFrameBox().intersectsQuad(treasure.getCollisionBox())){
												if(treasure.getState() == TreasureChest.closed){
													TreasureChest tc = (TreasureChest) treasure;
													int[][] i = world.getItemPouch();
													i[tc.getContents()][1]++;
													world.setItemPouch(i);
													tc.animate(loader);
													double y = this.getHeight()/4 + MessageBox.defaultHeight/2;
													String m = "Got " + Item.getText(tc.getContents()) + "!";
													msg = new MessageBox(new Point(20,y), m);
													reading = true;
												}
											}
										}
										 
									}
								}	
								//END: TREASURE CHEST DETECTION
								
								//START: Switch Detection
								for(Sprite swit : switches){
									if( swit.getLayer() == world.getLayer() ) {
										if(RenderUtilities.onScreen(h.getMin(), swit.getMin(), swit.getMax(), sD, wD)){
											if(h.getFrameBox().intersectsQuad(swit.getCollisionBox())){
												Switch s = (Switch) swit;
												swit.animate(loader);
											}
										}
											
									}
									
								}
								//END: Switch Detection
							}
							//END: Conditional on Not Reading
							//START: Conditional on Reading
							else { 
								if(!msg.isFinished()){ msg.nextLine(loader);}
								if(msg.isFinished()){ reading = false; msg = null;
									//TODO: Check is msg is a message SceneTrigger
									//scened = true;
									//scenechoice = SceneTriggers get scene number
								}
							} 
							//END: Conditional on Reading
							pad.flushInteract(); 
							break;
							//END: Interact assignment 
						
							//START: Menu Assignment
						case Controller.menu:
							paused = true;
							makeMenu(!NPC_SAVE);
							pad.flushMenu();
							break;
							//END: Menu Assignment
							
							//START: Item Assignment
						case Controller.item:
							paused = true;
							subbing = true;
							makeSubMenu(GameMenu.itemmenu);
							pad.flushItem();
							break;
							//END: Item Assignment
							
							//START: Attack Assignment
						case Controller.attack:
							if(reading) { reading = false; msg = null; }
							else{
								dir = h.getDirection();
								h.resetState(); 
								h.setAttacking(true);
								switch(dir){
									case Hero.DIR_down: h.setState(Hero.atk_down);
										break;
									case Hero.DIR_up: h.setState(Hero.atk_up);
										break;
									case Hero.DIR_right: h.setState(Hero.atk_right);
										break;
									case Hero.DIR_left: h.setState(Hero.atk_left);
										break;
								}
							}
							pad.flushAttack();
							break;
							//END: Attack Assignment
							
							//START: Engage Assignment
						case Controller.engage:
							if(reading){ reading = false; msg = null; }
							pad.flushEngage();
							break;
							//END: Engage Assignment
							
							//START: Equip1 Assignment
						case Controller.equip1:
							index = equipMap[equipMap1];
							switch(index){
								case GameMenu.lantern:
									h.setLanterning(!h.getLanterning());
									pad.flushEquipment();
									break;
								case GameMenu.shovel:
									dir = h.getDirection();
									h.resetState();
									h.setShovelling(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shv_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shv_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shv_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shv_left);
											break;
									}
									pad.flushEquipment();
									break;
								case GameMenu.shield: 
									dir = h.getDirection();
									h.setShielding(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shd_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shd_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shd_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shd_left);
											break;
									}
									h.animate(loader);
									break;
								case GameMenu.crossbow:
									dir = h.getDirection();
									h.resetState();
									h.setBowing(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.bow_up);
											break;
										case Hero.DIR_down: h.setState(Hero.bow_down);
											break;
										case Hero.DIR_right: h.setState(Hero.bow_right);
											break;
										case Hero.DIR_left: h.setState(Hero.bow_left);
											break;
									}
									//TODO: check for bolts in item pouch
									Projectile pr = new Projectile(new Point(h.getCenter().getX(), h.getCenter().getY()), 
																	Projectile.bolt, 
																	h.getDirection(), 
																	Projectile.FRIEND);
									pr.setLayer(world.getLayer());
									pr.animate(loader);
									world.getProjectiles().add(pr);
									//decrement bolt in item pouch
									pad.flushEquipment();
									break;
								case GameMenu.hammer:
									break;
								case GameMenu.rocket_skates:
									break;
								case GameMenu.visor:
									break;
							}
							break;
							//END: Equip1 Assignment
							
							//START: Equip2 Assignment
						case Controller.equip2:
							index = equipMap[equipMap2];
							switch(index){
								case GameMenu.lantern:
									h.setLanterning(!h.getLanterning());
									pad.flushEquipment();
									break;
								case GameMenu.shovel:
									dir = h.getDirection();
									h.resetState(); //Get direction before resetting state!
									h.setShovelling(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shv_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shv_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shv_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shv_left);
											break;
									}
									pad.flushEquipment();
									break;
								case GameMenu.shield: 
									dir = h.getDirection();
									h.setShielding(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shd_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shd_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shd_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shd_left);
											break;
									}
									h.animate(loader);
									break;
								case GameMenu.crossbow:
									dir = h.getDirection();
									h.resetState();
									h.setBowing(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.bow_up);
											break;
										case Hero.DIR_down: h.setState(Hero.bow_down);
											break;
										case Hero.DIR_right: h.setState(Hero.bow_right);
											break;
										case Hero.DIR_left: h.setState(Hero.bow_left);
											break;
									}
									//TODO: check for bolts in item pouch
									Projectile pr = new Projectile(new Point(h.getCenter().getX(), h.getCenter().getY()), 
																	Projectile.bolt, 
																	h.getDirection(), 
																	Projectile.FRIEND);
									pr.setLayer(world.getLayer());
									pr.animate(loader);
									world.getProjectiles().add(pr);
									pad.flushEquipment();
									//decrement bolt in item pouch
									break;
								case GameMenu.hammer:
									break;
								case GameMenu.rocket_skates:
									break;
								case GameMenu.visor:
									break;
							}
							break;
							//END: Equip2 Assignment
							
							//START: Equip3 Assignment
						case Controller.equip3:
							index = equipMap[equipMap3];
							switch(index){
								case GameMenu.lantern:
									h.setLanterning(!h.getLanterning());
									pad.flushEquipment();
									break;
								case GameMenu.shovel:
									dir = h.getDirection();
									h.resetState(); //Get direction before resetting state!
									h.setShovelling(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shv_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shv_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shv_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shv_left);
											break;
									}
									pad.flushEquipment();
									break;
								case GameMenu.shield: 
									dir = h.getDirection();
									h.setShielding(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.shd_up);
											break;
										case Hero.DIR_down: h.setState(Hero.shd_down);
											break;
										case Hero.DIR_right: h.setState(Hero.shd_right);
											break;
										case Hero.DIR_left: h.setState(Hero.shd_left);
											break;
									}
									h.animate(loader);
									break;
								case GameMenu.crossbow:
									dir = h.getDirection();
									h.resetState();
									h.setBowing(true);
									switch(dir){
										case Hero.DIR_up: h.setState(Hero.bow_up);
											break;
										case Hero.DIR_down: h.setState(Hero.bow_down);
											break;
										case Hero.DIR_right: h.setState(Hero.bow_right);
											break;
										case Hero.DIR_left: h.setState(Hero.bow_left);
											break;
									}
									//TODO: check for bolts in item pouch
									Projectile pr = new Projectile(new Point(h.getCenter().getX(), h.getCenter().getY()), 
																	Projectile.bolt, 
																	h.getDirection(), 
																	Projectile.FRIEND);
									pr.setLayer(world.getLayer());
									pr.animate(loader);
									world.getProjectiles().add(pr);
									//decrement bolt in item pouch
									pad.flushEquipment();
									break;
								case GameMenu.hammer:
									break;
								case GameMenu.rocket_skates:
									break;
								case GameMenu.visor:
									break;
							}
							break;
							//END: Equip3 Assignment
						
							//START: Default Assignment
						default:
							pad.flushControls();
							break;
							//END: Default Assignment
						}
					//END: Switch On Input
				}
				// *** DEFAULT INPUT LOOP PASS *** //
				//END Conditional on: Hero: No Flags
				
				//START Conditional on Hero: Attacking
				// *** SPECIAL INPUT LOOP PASS *** //
				else if(h.getAttacking() && !h.getShovelling() && !h.getPowered() && 
						!h.getStunned() && !h.getBowing()){
					//if attacking
						//override attack if input is shield!
					h.animate(loader);
						//TODO: if hero is done attacking
						//and still pressing attack, go into power up mode.
						//need to get state from pad and switch on input
						//to make sure hero moves while powere up.
						/*
						 * if hero is not attacking and is pressing attack
						 * 		if hero is not powered up, already determined
						 * 			reset heroState
						 * 			set hero state to powered up
						 */
					if(!h.getAttacking() && input == Controller.attack){ }
						//h.animate(loader); //determines whether or not hero keeps holding sword after attack
				}
				// *** SPECIAL INPUT LOOP PASS *** //
				//END Conditional on Hero: Attacking
				
				//START Conditional on Hero: Shovelling
				// *** SPECIAL INPUT LOOP PASS *** //
				else if(!h.getAttacking() && h.getShovelling() && !h.getPowered() && 
						!h.getStunned() && !h.getShielding() && !h.getBowing()){
					h.animate(loader);
					Point hMin = h.getMin();
					for(Sprite s : st){
						Point sMin = s.getMin(); Point sMax = s.getMax();
						if(RenderUtilities.onScreen(hMin, sMin, sMax, sD, wD)){
							if(h.getCollisionBox().intersectsQuad(s.getCollisionBox())){
									if(s.getState() == ShovelTile.cover){
										s.animate(loader);
									}
							}
						}
						//END Conditional shoveltile on Screen
					}
					//END shoveltile iteration
					if(!h.getShovelling()) { h.animate(loader);  }
					if(input == Controller.interact || input == Controller.engage){
						if(reading){ reading = false;}
						if(input == Controller.interact) { pad.flushInteract(); }
						if(input == Controller.engage) { pad.flushEngage(); }
					}
				}
				// *** SPECIAL INPUT LOOP PASS *** //
				//END Conditional on Hero: not (attacking) and (SHOVELING) and not (powered) and not (stunned) 
										//and not (bowing) and not (skating) and not (shielding)
				
				//START Conditional on Hero: Powered
				// *** SPECIAL INPUT LOOP PASS *** //
				else if(!h.getAttacking() && !h.getShovelling() && h.getPowered() && 
						!h.getStunned() && !h.getShielding() && !h.getBowing()){
					
				}
				// *** SPECIAL INPUT LOOP PASS *** //
				//END: Conditional on Hero: Powered
			
				//START: Conditional on Hero: Shielding
				// *** SPECIAL INPUT LOOP PASS *** //
				else if(!h.getAttacking() && !h.getShovelling() && !h.getPowered() && 
						!h.getStunned() && h.getShielding() && !h.getBowing()){
					//TODO: Have to disallow allow equipment being mapped to multiple slots
						//this method for shields allows user to switch out of shielding if
						//the shield is mapped to more than one spot.
					if(equipMap[equipMap1] == GameMenu.shield){
						if(input != Controller.equip1){ 
							h.setShielding(false);
							h.animate(loader);
						}
					}
					if(equipMap[equipMap2] == GameMenu.shield){
						if(input != Controller.equip2){ 
							h.setShielding(false);
							h.animate(loader);
						}
					}
					if(equipMap[equipMap3] == GameMenu.shield){
						if(input != Controller.equip3){ 
							h.setShielding(false);
							h.animate(loader);
						}
					}
				}
				// *** SPECIAL INPUT LOOP PASS *** //
				//END: Conditional on Hero: Shielding
				
				//START Conditional on Hero: Bowing Only
				// *** SPECIAL INPUT LOOP PASS *** //
				else if(!h.getAttacking() && !h.getShovelling() && !h.getPowered() && 
						!h.getStunned() && !h.getShielding() && h.getBowing()){
					h.animate(loader);
					
				}
				// *** SPECIAL INPUT LOOP PASS *** //
				//END Conditional on Hero: Bowing Only
		
				//TODO: MORE Equipment Combinations!!!
				
				//START: Conditional on Hero: Stunned
				// *** SPECIAL INPUT LOOP PASS *** //
				else if (h.getStunned()){ h.animate(loader); }
				// *** SPECIAL INPUT LOOP PASS *** //
				//END: Conditional on Hero: Stunned
			} 
			//END: Conditional on game not paused
			//	
			//START: Conditional on game paused
			else{ //if game is paused, then menu has been made; need to check for sub menus, as well.
				//START: Conditional on Sub Menu Does Not Exist
				if(!subbing){
					int input = pad.getState();
					int selection;
					if(menu == null){ paused = false;}
					//START: Conditional on Main Menu Exists
					else{
					//MAIN-MENU CONTROL CODE 
						//
						//START: Switch on Input
						switch(input){
							case Controller.walkup:
								menu.setState(menu.getState() - 1);
								pad.flushControls();
								//Terminate button press and see if any buttons are still being pushed
								break;
							
							case Controller.walkdown:
								menu.setState(menu.getState() + 1);
								pad.flushControls();
								//Terminate button press and see if any buttons are still being pushed
								break;
							
							case Controller.interact:
								selection = menu.getState();
								//START: Switch on (menu selection)
								switch(selection){
									case GameMenu.status:
										makeSubMenu(GameMenu.statusmenu);
										subbing = true;
										break;
									case GameMenu.items:
										makeSubMenu(GameMenu.itemmenu);
										subbing = true;
										break;
									case GameMenu.equip:
										makeSubMenu(GameMenu.equipmenu);
										subbing = true;
										break;
									case GameMenu.map:
										//TODO: Menu choice map
										//MakeSubMenu(GameMenu.mapmenu);
										//subbing = true;
										break;
									case GameMenu.save:
										saving = true;
										paused = false;
										menu = null; 
											//TODO: display saved message
										break;
									case GameMenu.goback:
										paused = false;
										menu = null;
										break;
								}
								//END: Switch on (menu selection)
								pad.flushInteract();
								break;
						
							case Controller.attack:
								selection = menu.getState();
								//START: Switch on (menu selection)
								switch(selection){
									case GameMenu.status:
										makeSubMenu(GameMenu.statusmenu);
										subbing = true;
										break;
									case GameMenu.items:
										makeSubMenu(GameMenu.itemmenu);
										subbing = true;
										break;
									case GameMenu.equip:
										makeSubMenu(GameMenu.equipmenu);
										subbing = true;
										break;
									case GameMenu.map:
										//TODO: Menu choice map
										//MakeSubMenu(GameMenu.mapmenu);
										//subbing = true;
										break;
									case GameMenu.save:
										saving = true;
										paused = false;
										menu = null; 
											//get rid of above and replace with
											//makeSubMenu(GameMenu.savemenu);
											//Don't set saved until user selects file 
											// from save menu.
											//TODO: Display saved message
										break;
									case GameMenu.goback:
										paused = false;
										menu = null;
										break;
								}
								//END: Switch on (menu selection)
								pad.flushAttack();
								break;
						
							case Controller.menu:
								paused = false;
								menu = null;
								pad.flushMenu();
								//Terminate menu and see if any buttons are still being pushed.
								break;
							
							case Controller.engage:
								paused = false;
								menu = null;
								pad.flushEngage();
								break;
						
							default:
								pad.flushControls();
								break;
						}
						//END: Switch on Input
					}
					//END: Conditional on Main Menu Exists
				
				}
				//END: Conditional on Sub Menu Does Not Exist
				//START: Conditional on Sub Menu Exists
				else{
					//START Conditional on Main Menu Exists
					if(menu != null){
						//if coming from main menu
						int selection = menu.getState();
						int input = pad.getState();
						//START: Switch on Main Menu Selection
						switch(selection){
							//START: Status Menu Control
							case GameMenu.status:
								//START: Switch on Input
								switch(input){
									case Controller.engage:
										subbing = false; sub = null;
										pad.flushEngage();
										break;
									
									case Controller.menu:
										subbing = false; sub = null;
										pad.flushMenu();
										break;
									
									default:
										pad.flushControls();
										break;
								}
								//END: Switch on Input
								break;
								//END: Status Menu Control
							
								//START: Item Menu Control
							case GameMenu.items:
								//START: Switch on Input
								switch(input){
									case Controller.engage:
										subbing = false;
										sub = null;
										pad.flushEngage();
										break;
							
									case Controller.menu:
										subbing = false;
										sub = null;
										pad.flushMenu();
										break;
								
									case Controller.walkdown:
										sub.setState(sub.getState()+1);
										sub.formatItems(world.getItemPouch());
										pad.flushControls();
										break;
								
									case Controller.walkup:
										sub.setState(sub.getState()-1);
										sub.formatItems(world.getItemPouch());
										pad.flushControls();
										break;
										
									case Controller.interact:
										//TODO: Use item!
										break;
								
									default:
										pad.flushControls();
										break;
							
								}
								//END: Switch on Input
								break;
								//END: Item Menu Control
						
								//START: Equipment Menu Control
							case GameMenu.equip:
								//START: Switch on (Input)
								switch(input){
									case Controller.engage:
										subbing = false;
										sub = null;
										pad.flushEngage();
										break;
							
									case Controller.menu:
										subbing = false;
										sub = null;
										pad.flushMenu();
										break;
								
									case Controller.walkright:
										sub.setEquipment(sub.getState()+1, world.getEquipmentTriggers());
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										pad.flushControls();
										break;
								
									case Controller.walkleft:
										sub.setEquipment(sub.getState()-1, world.getEquipmentTriggers());
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										pad.flushControls();
										break;
									
									case Controller.walkdown:
										sub.setEquipment(sub.getState()+4,world.getEquipmentTriggers());
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										pad.flushControls();
										break;
									
									case Controller.walkup:
										sub.setEquipment(sub.getState()-4, world.getEquipmentTriggers());
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										pad.flushControls();
										break;
									
									case Controller.equip1:
										equipMap[equipMap1] = sub.getState();
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										equipsub.formatSubEquipment(equipMap);
										pad.flushEquipment();
										break;
								
									case Controller.equip2:
										equipMap[equipMap2] = sub.getState();
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										equipsub.formatSubEquipment(equipMap);
										pad.flushEquipment();
										break;
									
									case Controller.equip3:
										equipMap[equipMap3] = sub.getState();
										sub.formatEquipment(world.getEquipmentTriggers(), equipMap);
										equipsub.formatSubEquipment(equipMap);
										pad.flushEquipment();
										break;
									
									default:
										pad.flushControls();
										break;
							
								}
								//END: Switch on Input
								break;
								//END: Equipment Menu Control
							
							case GameMenu.save:
								//TODO: Implement Save Menu where user can authorize overwrite!
								break;
							
							case GameMenu.map:
								//TODO: Implement map based on current world frame set.
								break;
							}
							//END: Switch On Main Menu Selection
					}
					//END Conditional On Main Menu Exists	
					//START Conditional on Only Sub Menu Exists
					else{ //if coming from item menu
						int input = pad.getState();
						//START: Switch on Input
						switch(input){
							case Controller.engage:
								subbing = false; paused = false; sub = null;
								pad.flushEngage();
								break;
				
							case Controller.menu:
								subbing = false; sub = null;
								makeMenu(!NPC_SAVE);
								pad.flushMenu();
								break;
					
							case Controller.walkdown:
								sub.setState(sub.getState()+1);
								sub.formatItems(world.getItemPouch());
								pad.flushControls();
								break;
					
							case Controller.walkup:
								sub.setState(sub.getState()-1);
								sub.formatItems(world.getItemPouch());
								pad.flushControls();
								break;
							
							case Controller.item:
								subbing = false;
								sub = null;
								pad.flushItem();
								break;
					
							default:
								pad.flushControls();
								break;
				
						}
						//END: Switch on Input
					}
					//END: Conditional on Sub Menu Only Exists
				}
				//END: Conditional on Sub Menu Exists
		
		}
		    //END: Conditional on (Game Paused)
	}
		//END: Conditional on (Game Looping)
}
	//END: User input
	
	/**
	 * @Method updateAll:
	 * 
	 * @description: updateAll simply calls the heroUpdate, NPCUpdate and VillainUpdate methods
	 * 	and then iterates the updates field to keep track of how many updates have been applied 
	 *  since the game thread started. 
	 */
	private void updateAll(){  
		heroUpdate();
		NPCUpdate();
		VillainUpdate();
		MiscUpdate();
		updates++;
	}

	/**
	 * @Method heroUpdate: 
	 * 
	 * @description: this class contains the physics pertaining to the user's avatar. Properties
	 * contained in the user's Hero data profile are polled and checked against world objects to
	 * determine if the user's avatar has triggered a movement not directly prescribed by user 
	 * input.
	 * 
	 * For example, if the user walks into the hitbox of an object, such as a wall or enemy. To prevent
	 * the user from walking through the object, the in-game coordinates of the user's avatar are altered 
	 * to stop the impact.
	 * 
	 * This method also will check to see if a villain's attack has connected.
	 * 
	 * This method also increments the Hero's lantern counter.
	 */
	private void heroUpdate(){  
		Hero hero = (Hero) world.getHero();
		Point screenDim = new Point(this.getWidth(), this.getHeight());
		Point worldDim = world.getDimensions();
		ArrayList<Sprite> pts = world.getPortals();
		ArrayList<Sprite> efs = world.getEffects();
		ArrayList<Sprite> hck = world.getHackboxes();
		ArrayList<Sprite> hbxs = world.getHitboxes();
		ArrayList<Sprite> plts = world.getPressurePlates();
		ArrayList<Sprite> gates = world.getGates();
		ArrayList<Sprite> vs = world.getVillains();
		ArrayList<Line> wlines = world.getBounds();
		Point heroxy = hero.getMin();
		int layer = world.getLayer();
		
		//START: World Boundary Collision Detection
		if(hero.getCollisionBox().intersectsLine(wlines.get(GameWorld.BOTTOM_INDEX))){
			hero.moveY(-1*(hero.getCollisionBox().getMaxY()- wlines.get(GameWorld.BOTTOM_INDEX).getP1().getY()));
		}
		if(hero.getCollisionBox().intersectsLine(wlines.get(GameWorld.TOP_INDEX))){
			hero.moveY(wlines.get(GameWorld.TOP_INDEX).getP1().getY() - hero.getCollisionBox().getMinY());
		}
		if(hero.getCollisionBox().intersectsLine(wlines.get(GameWorld.RIGHT_INDEX))){
			hero.moveX(-1*(hero.getCollisionBox().getMaxX() - wlines.get(GameWorld.RIGHT_INDEX).getP1().getX()));
		}
		if(hero.getCollisionBox().intersectsLine(wlines.get(GameWorld.LEFT_INDEX))){
			hero.moveX(wlines.get(GameWorld.LEFT_INDEX).getP1().getX() - hero.getCollisionBox().getMinX());
		}
		//END: World Boundary Collision Detection
		
		//START: Hero-to-Sprite Collision Detection
			//start with general Hitbox sprite and
			//iterate through NPC, TreasureChest,
			//Villain Sprites. Only types of sprites
			//that the hero can bounce off of.
		boolean flag = true, 
				vFlag = true, lFlag = true, hFlag = true, gFlag = true; 
					//needed for object specific checks
		int counter = 0;
		while(flag){
			for(Sprite box : hbxs){
				Point min, max;
				min = box.getMin();
				max = box.getMax();
				//START: Conditional on Sprite in same world, same side
				if(box.getLayer() == layer){
					//START: Conditional on Sprite On Screen
					if(RenderUtilities.onScreen(heroxy, min, max, screenDim, worldDim)){
						//START: Conditional on Sprite intersecting Hero
						if(counter == 3) { vFlag = false;}
						if(counter == 4) { lFlag = false; }
						if(counter == 6) { hFlag = false; }
						if(counter == 7) { gFlag = false; }
						if(vFlag && lFlag && hFlag && gFlag){
							if(box.getCollisionBox().intersectsQuad(hero.getCollisionBox())){  
								Physics.spriteCollision(hero, box.getCollisionBox()); 
							}
						}
						else if (!vFlag){
							Villain v = (Villain) box;
							if(!v.isDead()){
								if(box.getCollisionBox().intersectsQuad(hero.getCollisionBox())){ 
									Physics.spriteCollision(hero, box.getCollisionBox()); 
								}
							}
						}
						else if (!lFlag){
							//TODO: Equipment modification allows user to cross lava.
						}
						else if (!hFlag){
							if(box.getState() == Hackbox.unhacked){
								if(box.getCollisionBox().intersectsQuad(hero.getCollisionBox())){
									Physics.spriteCollision(hero, box.getCollisionBox()); 
								}
							}
						}
						else if(!gFlag){
							if(box.getState() == Gate.impassable){
								if(box.getCollisionBox().intersectsQuad(hero.getCollisionBox())){
									Physics.spriteCollision(hero, box.getCollisionBox()); 
								}
							}
							
						}
						
						if(counter == 3) { vFlag = true; }
						if(counter == 4) { lFlag = true; }
						if(counter == 6) { hFlag = true; }
						if(counter == 7) { gFlag = true; }
						//END: Conditional on Sprite intersecting Hero
					}
					//END: Conditional on Sprite On Screen
				}
				//END: Conditional on sprite in same world, same side
			}
			
			if(counter == 0) { hbxs = world.getNPCs();}
			else if(counter == 1) { hbxs = world.getTreasures(); }
			else if(counter == 2) { hbxs = world.getVillains(); }
			else if(counter == 3) { hbxs = world.getLights(); }
			else if(counter == 4) { hbxs = world.getSwitches(); }
			else if(counter == 5) { hbxs = world.getHackboxes(); }
			else if(counter == 6) { hbxs = world.getGates(); }
			else if(counter == 7) { hbxs = world.getCrates(); }
			else if(counter == 8) { flag = false; }
				//TODO: Add Barrels, Signs and Save Crystals to iteration.
			counter++;
		}
		//END: Hero-to-Sprite Collision detection
		
		//HERO LANTERN INCREMENT
		Hero he = (Hero) hero;
		if(he.getLanterning()){ he.incrementLantern(); }
		
		//HERO-VILLAIN ATTACK DETECTION
		for(Sprite v : vs){
			if(v.getLayer() == layer){
				Point min = v.getMin();
				Point max = v.getMax();
				if(RenderUtilities.onScreen(heroxy, min, max, screenDim, worldDim)){
					Villain vil = (Villain) v;
					if(vil.isAttacking()){
						if(vil.getAttackBox().intersectsQuad(hero.getCollisionBox())){
							if(!he.getShielding()){
								he.recoil(vil.getDirection());
								he.stun();
								he.damage(vil.getAtk()/he.getDEF());
								initializeEffectAt(Effect.knockback, he.getCenter(), Effect.NO_ANCHOR);
							}
							else{
								vil.recoil(vil.getOppositeDirection());
								vil.stun();
								initializeEffectAt(Effect.knockback, he.getCenter(), Effect.NO_ANCHOR);
							}
						}
					}
				}
			}
		}
		
		//HERO PORTAL DETECTION
		for(Sprite port : pts){
			if(port.getLayer() == world.getLayer()){
				Point min = port.getMin(); Point max = port.getMax();
				if(RenderUtilities.onScreen(heroxy, min, max, screenDim, worldDim)){
					if(hero.getCollisionBox().intersectsQuad(port.getCollisionBox())){
						portaled = true;
						Portal portal = (Portal) port;
						portalchoice = portal.getConnection();
						nextInsertion = portal.getNextInsertion();
						nextLayer = portal.getNextLayer();
					}
				}
			}
		}
		
		//HERO SPLASH
		boolean splashed = false;
		for(Sprite ef : efs){
			Point min, max;
			min = ef.getMin(); max = ef.getMax();
			if(((Effect) ef).getType() == Effect.waistwater){
				if(RenderUtilities.onScreen(heroxy, min, max, screenDim, worldDim)){
					if(hero.getCollisionBox().intersectsQuad(ef.getCollisionBox())){
							if(!hero.getSubmerged()){ 
								hero.setSubmerged(true);
								splashed = true;
							}
					}
				}	
			}
		}

		//HACKBOX HACK DETECTION
		for(Sprite hk : hck){
			Point min, max;
			min = hk.getMin(); max = hk.getMax();
			if(RenderUtilities.onScreen(heroxy, min, max, screenDim, worldDim)){
				if(hero.getAttacking()){ 
					if(hk.getState() == Hackbox.unhacked){
						if(hero.getAttackBox().intersectsQuad(hk.getCollisionBox())){
							hk.animate(loader);
							Point p = new Point(hk.getMinX() + 10, hk.getCenter().getY() -15);
							initializeEffectAt(Effect.hackcloud, p, Effect.NO_ANCHOR);
						}
					}
				}
			}
		}
		
		//HERO PRESSURE PLATE DETECTION
		for(Sprite pp : plts){
			if(pp.getLayer() == hero.getLayer()){
				if(hero.getCollisionBox().intersectsQuad(pp.getCollisionBox())){
					if(pp.getState() == PressurePlate.unpressed) {
						pp.animate(loader);
						((PressurePlate) pp).setIdentity(Hero.HERO_IDENTITY);
						int plateanchor = ((PressurePlate) pp).getAnchor();
						for(Sprite g: gates){ if(((Gate) g).getAnchor() == plateanchor){ g.animate(loader); } }
					}
				}
			}
		}
		if(splashed) { 
			initializeEffectAt(Effect.splash, hero.getCollisionBox().getCenter(), Sprite.HERO_IDENTITY); 
		}
		
		//TODO: HERO LOCATION SCENETRIGGER DETECTION
	}
		
	private void NPCUpdate(){ 
		ArrayList<Sprite> npcs = world.getNPCs();
		ArrayList<Sprite> hbxs = world.getHitboxes();
		ArrayList<Line> wlines = world.getBounds();
		ArrayList<Sprite> treasures = world.getTreasures();
		ArrayList<Sprite> plates = world.getPressurePlates();
		ArrayList<Sprite> gates = world.getGates();
		ArrayList<Sprite> lights = world.getLights();
		ArrayList<Sprite> hacks = world.getHackboxes();
		Hero h = (Hero) world.getHero();
		
		for(Sprite npc : npcs){
			NPC n = (NPC) npc;
			npc.animate(loader);
			
				//NPC WORLD BOUNDARY COLLISION DETECTION
				if(n.getCollisionBox().intersectsLine(wlines.get(GameWorld.BOTTOM_INDEX))){ 
					n.moveY(-1*(n.getCollisionBox().getMaxY()- wlines.get(GameWorld.BOTTOM_INDEX).getP1().getY() + 1));					
				}
				if(n.getCollisionBox().intersectsLine(wlines.get(GameWorld.TOP_INDEX))){
					n.moveY(wlines.get(GameWorld.TOP_INDEX).getP1().getY() - n.getCollisionBox().getMinY() + 1);
				}
				if(n.getCollisionBox().intersectsLine(wlines.get(GameWorld.RIGHT_INDEX))){
					n.moveX(-1*(n.getCollisionBox().getMaxX() - wlines.get(GameWorld.RIGHT_INDEX).getP1().getX() + 1));
				}
				if(n.getCollisionBox().intersectsLine(wlines.get(GameWorld.LEFT_INDEX))){
					n.moveX(wlines.get(GameWorld.LEFT_INDEX).getP1().getX() - n.getCollisionBox().getMinX() + 1);
				}
				//NPC HITBOX COLLISION DETECTION
				for(Sprite hbx : hbxs ){
					if( hbx.getLayer() == n.getLayer()){
						if(n.getCollisionBox().intersectsQuad(hbx.getCollisionBox())){
							Physics.spriteCollision(n, hbx.getCollisionBox());
							if(n.isMoving()) { n.reverseDirection(); }
						}
					}
				}
				//NPC TREASURE CHEST COLLISION DETECTION
				for(Sprite tc : treasures){
					if( tc.getLayer() == n.getLayer()){
						if(n.getCollisionBox().intersectsQuad(tc.getCollisionBox())){
							Physics.spriteCollision(n, tc.getCollisionBox());
							if(n.isMoving()) {n.reverseDirection();}
						}
					}
				}
				//NPC LIGHT SOURCE COLLISION DETECTION
				for(Sprite l : lights){
					if( l.getLayer() == n.getLayer()){
						if(n.getCollisionBox().intersectsQuad(l.getCollisionBox())){
							Physics.spriteCollision(n, l.getCollisionBox());
							if(n.isMoving()) { n.reverseDirection(); }
						}
					}
				}
				//NPC PRESSURE PLATE DETECTION
				for(Sprite pp : plates){
					if(pp.getLayer() == n.getLayer()){
						if(n.getCollisionBox().intersectsQuad(pp.getCollisionBox())){
							if(pp.getState() == PressurePlate.unpressed) {
								pp.animate(loader);
								((PressurePlate) pp).setIdentity(n.getIdentity());
								int plateanchor = ((PressurePlate) pp).getAnchor();
								for(Sprite g: gates){ if(((Gate) g).getAnchor() == plateanchor){ g.animate(loader); } } }
						}
					}
				}
				//NPC GATE DETECTION
				for(Sprite g : gates){
					if(g.getLayer() == n.getLayer()){
						if(g.getState() == Gate.impassable){
							if(n.getCollisionBox().intersectsQuad(g.getCollisionBox())){
								Physics.spriteCollision(n, g.getCollisionBox()); 
							}
						}
					}
				}
				
				//NPC HACKBOX COLLISION DETECTION
				for(Sprite hk : hacks){
					if(hk.getLayer() == n.getLayer()){
						if(hk.getState() == Hackbox.unhacked){
							if(n.getCollisionBox().intersectsQuad(hk.getCollisionBox())){
								Physics.spriteCollision(n, hk.getCollisionBox());
							}
						}
					}
				}
				
				//NPC BARREL COLLISION DETECTION
				
				//NPC CRATE COLLISION DETECTION
				
				//NPC SIGN COLLISION DETECTION
				
				//NPC SAVE CRYSTAL COLLISION DETECTION
				
				//NPC TO NPC COLLISION DETECTION
				for(Sprite np : npcs){
					if(!np.equals(n)){
						if( np.getLayer() == n.getLayer()){
							if(n.getCollisionBox().intersectsQuad(np.getCollisionBox())){
								Physics.spriteCollision(n, np.getCollisionBox());
								if(n.isMoving()) { n.reverseDirection(); }
							}
						}
					}
				}
			
				if(h.getFrameBox().intersectsQuad(n.getCollisionBox())) { n.setMoving(false); }
				else{ n.setMoving(true); }
			}
		
	}
	
	/**
	 * @VillainUpdate
	 * 
	 * @description this method contains the behavior scripts for the Villain class.
	 * 
	 * pseudo code for Villain behavior
	 * 
	 * for every Villain
	 * 		animate Villain
	 * 		inform Villain of Hero's whereabouts (internal check to see if action 
	 * 		needs to be taken)
	 * 	
	 * 	VILLAIN HIT DETECTION
	 * 		if villain is onscreen
	 * 			if hero is attacking
	 * 				get hero's attack box
	 * 				if attack box intersects Villain
	 * 					switch on (Villain's type):
	 * 						switch on (Hero's direction): moveX or Y
	 * 
	 * 	VILLAIN COLLISION DETECTION
	 * 		VILLAIN WORLD BOUND COLLISION DETECTION
	 * 
	 * 		VILLAIN HITBOX COLLISION DETECTION
	 * 
	 */
	private void VillainUpdate(){
		ArrayList<Sprite> vils = world.getVillains();
		Hero h = (Hero) world.getHero();
		Point screenDim = new Point(this.getWidth(), this.getHeight());
		Point worldDim = world.getDimensions(); 
		ArrayList<Line> wlines = world.getBounds();
		ArrayList<Sprite> hbxs = world.getHitboxes();
		ArrayList<Sprite> lights = world.getLights();
		ArrayList<Sprite> treasures = world.getTreasures();
		ArrayList<Sprite> switches = world.getSwitches();
		ArrayList<Sprite> npcs = world.getNPCs();
		ArrayList<Sprite> proj = world.getProjectiles();
		ArrayList<Sprite> plates = world.getPressurePlates();
		ArrayList<Sprite> hacks = world.getHackboxes();
		ArrayList<Sprite> gates = world.getGates();
		//START Villain iteration
		for(Sprite vil : vils){
			Villain v = (Villain) vil;
			if(updates%v.getSynchDelay() == 0){
				vil.animate(loader);
				//START Conditional on Villain is Not Dying or Dead
				if(!v.isDying() && !v.isDead()){ 
					Point Vmin = v.getMin();
					Point Vmax = v.getMax();
					Point hmin = h.getMin();
					//VILLAIN-HERO INTERACTIONS
					//
					//START Conditional on Same World, Same Side
					if(world.getLayer() == v.getLayer()){
							v.inform(h.getCenter());
							//START HERO->VILLAIN LANTERN FRIGHTEN
							if(h.getLanterning() && v.isScareable()){
								v.frighten(h.getMin(), h.getOuterLanternRad()/2 );
							} 
							else { 
								v.setScared(false);
									//only if no other light sources are frightening the villain, though!
							}
							//END HERO -> VILLAIN LANTERN FRIGTHEN
							//START HERO->VILLAIN HIT DETECTION
							if(RenderUtilities.onScreen(hmin, Vmin, Vmax, screenDim, worldDim)){
								if(h.isAttacking()){
									Quad atk = h.getAttackBox();
									if(atk.intersectsQuad(v.getCollisionBox())){
										v.damage(h.getATK());
										initializeEffectAt(Effect.knockback, v.getCenter(), Effect.NO_ANCHOR);
										if(!v.isStunned() && !v.isDying()) { v.stun(); } 
										v.recoil(h.getDirection());
									}
								}
							}
							//END HERO->VILLAIN HIT DETECTION
					}
					//END Conditional on Same World, Same Side
				
					//START VILLAIN PROJECTILE HIT DETECTION
					if(!v.isDying() && !v.isDead()){
						for(Sprite p : proj){
							if(p.getLayer() == vil.getLayer()){
								Projectile pr = (Projectile) p;
								if(pr.isFriendOrFoe() == Projectile.FRIEND){
									if(p.getCollisionBox().intersectsQuad(vil.getCollisionBox())){
										if(!v.isStunned()) { v.stun(); } 
										initializeEffectAt(Effect.knockback, v.getCenter(), Effect.NO_ANCHOR);
										v.recoil(pr.getDirection());
										p.setState(Projectile.DEAD);
										v.damage(pr.getDamage());
									}
								}
							}
						}
					}
					//END VILLAIN PROJECTILE HIT DETECTION
				
					if(!v.isDying() && !v.isDead()){
						//VILLAIN WORLD BOUNDARY COLLISION DETECTION
						if(vil.getCollisionBox().intersectsLine(wlines.get(GameWorld.BOTTOM_INDEX))){ 
							vil.moveY(-1*(vil.getCollisionBox().getMaxY()- wlines.get(GameWorld.BOTTOM_INDEX).getP1().getY()));
							if(!v.isScared()) { v.reverseWalkDirection(); }
						}
						else if(vil.getCollisionBox().intersectsLine(wlines.get(GameWorld.TOP_INDEX))){
							vil.moveY(wlines.get(GameWorld.TOP_INDEX).getP1().getY() - vil.getCollisionBox().getMinY());
							if(!v.isScared()) { v.reverseWalkDirection(); }
						}
						else if(vil.getCollisionBox().intersectsLine(wlines.get(GameWorld.RIGHT_INDEX))){
							vil.moveX(-1*(vil.getCollisionBox().getMaxX() - wlines.get(GameWorld.RIGHT_INDEX).getP1().getX()));
							if(!v.isScared()) { v.reverseWalkDirection(); }
						}
						else if(vil.getCollisionBox().intersectsLine(wlines.get(GameWorld.LEFT_INDEX))){
							vil.moveX(wlines.get(GameWorld.LEFT_INDEX).getP1().getX() - vil.getCollisionBox().getMinX());
							if(!v.isScared()) { v.reverseWalkDirection(); }
						}
						//VILLAIN HITBOX COLLISION DETECTION
						for(Sprite hbx : hbxs ){
							if( hbx.getLayer() == vil.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(hbx.getCollisionBox())){
									Physics.spriteCollision(v, hbx.getCollisionBox());
									if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); }
									else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
								}
							}
						}
						//VILLAIN LIGHTSOURCE INTERACTION
						for(Sprite l : lights){
							//VILLAIN LIGHTSOURCE COLLISION DETECTION
							if( l.getLayer() == vil.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(l.getCollisionBox())){
									Physics.spriteCollision(vil, l.getCollisionBox());
									if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); }
									else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
								}
							}
							//VILLAIN LIGHTSOURCE FRIGHTEN
							if( l.getLayer() == vil.getLayer()){
								if(l.getState() == LightSource.on
										&& v.isScareable()){
										if(!v.isScared() && !v.isAware()) {
											v.frighten( l.getMin(), ((LightSource) l).getInnerLightRadius()/2);
										}
								}
								else{
									//if villain is scared and no other light sources are frightening him, 
									//set villain to not scared.
								}
							}
						}
				
						//VILLAIN TREASURE COLLISION DETECTION
						for(Sprite t : treasures){
							if( t.getLayer() == vil.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(t.getCollisionBox())){
									Physics.spriteCollision(vil, t.getCollisionBox());
									if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); }
									else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
								}
							}
						}
				
						//VILLAIN GATE COLLISION DETECTION
						for(Sprite g : gates){
							if(g.getLayer() == vil.getLayer()){
								if(g.getState() == Gate.impassable){
									if(vil.getCollisionBox().intersectsQuad(g.getCollisionBox())){
										Physics.spriteCollision(vil, g.getCollisionBox()); 
										if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); }
										else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
									}
								}
							}
						}
				
						//VILLAIN SWITCH COLLISION DETECTION
						for(Sprite sw : switches){
							if( sw.getLayer() == vil.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(sw.getCollisionBox())){
									Physics.spriteCollision(vil, sw.getCollisionBox());
									if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); }
									else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
								}
							}
						}
			
						//VILLAIN NPC COLLISION DETECTION
						for(Sprite n : npcs){
							if( n.getLayer() == vil.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(n.getCollisionBox())){
									Physics.spriteCollision(vil, n.getCollisionBox());
									if(!v.isScared() && !v.isAware()){ v.reverseWalkDirection(); }
									else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
								}
							}
						}
						//VILLAIN PRESSURE PLATE DETECTION
						for(Sprite pp : plates){
							if(pp.getLayer() == v.getLayer()){
								if(vil.getCollisionBox().intersectsQuad(pp.getCollisionBox())){
									if(pp.getState() == PressurePlate.unpressed) {
										pp.animate(loader);
										((PressurePlate) pp).setIdentity(vil.getIdentity());
										int plateanchor = ((PressurePlate) pp).getAnchor();
										for(Sprite g: gates){ if(((Gate) g).getAnchor() == plateanchor){ g.animate(loader); } }
									}
								}
							}
						}
				
						//VILLAIN BARREL COLLISION DETECTION
				
						//VILLAIN CRATE COLLISION DETECTION
				
						//VILLAIN SAVE CRYSTAL COLLISION DETECTION
				
						//VILLAIN SIGN COLLISION DETECTION
			
						//VILLAIN-VILLAIN COLLISION DETECTION
						for(Sprite v2 : vils){
							if(!v2.equals(vil)){ 
								if( v2.getLayer() == vil.getLayer()){
									if(vil.getCollisionBox().intersectsQuad(v2.getCollisionBox())){
										Physics.spriteCollision(vil, v2.getCollisionBox());
										if(!v.isScared() && !v.isAware()) { v.reverseWalkDirection(); } 
										else if (v.isAware() && !v.isScared()) { v.pathCorrect(); }
									}
								}
							}
						}
					}
			
				}
				//END:Conditional on Villain Dying or Is Dead
			}
		}
		//END Villain Iteration	
	}

	private void MiscUpdate(){
		ArrayList<Line> wlines = world.getBounds();
		ArrayList<Sprite> hbxs = world.getHitboxes();
		//Effect Update
		if(world.getEffects().size() > 0){
			Hero h = (Hero) world.getHero();
			ArrayList<Sprite> effects = world.getEffects();
			ArrayList<Integer> removal = new ArrayList<Integer>();
			for(Sprite e : effects){
				e.animate(loader);
				if(e.getState() == Effect.inert) { removal.add(effects.indexOf(e)); }
				Effect ef = (Effect) e;
				if(ef.getType() == Effect.splash){
					if(ef.getIdentity() == Sprite.HERO_IDENTITY && !h.getCollisionBox().intersectsQuad(e.getCollisionBox())){
						h.setSubmerged(false); e.setState(Effect.inert);
						removal.add(effects.indexOf(e));
					}
				}
			}
			int indexMod = 0; //removing an element shifts all elements down by an integer
			for(Integer i : removal){
				effects.remove(effects.get(i - indexMod));
				indexMod++;
			}
		}
		
		//Projectile Update
		if(world.getProjectiles().size() > 0){
			ArrayList<Integer> removal = new ArrayList<Integer>();
			ArrayList<Sprite> proj = world.getProjectiles();
			for(Sprite p : proj){
				if(p.getState() == Projectile.ALIVE) { 
					p.animate(loader); 
					((Projectile) p).determineAlive(world.getBounds(), world.getHitboxes());
						//separate out hitbox detection into view class to initialize effect at spot of hit.
				}
				else { removal.add(proj.indexOf(p)); }
			}
			int indexMod = 0;
			for(Integer i : removal){
				proj.remove(proj.get(i - indexMod));
				indexMod++;
			}
		}
		
		//Crate Update
		if(world.getCrates().size() > 0){
			ArrayList<Sprite> crates = world.getCrates();
			for(Sprite c : crates){
				//Crate Plate Detection
				if(world.getPressurePlates().size() > 0){
					ArrayList<Sprite> plates = world.getPressurePlates();
					for(Sprite p : plates){
						if(c.getLayer() == p.getLayer()){
							if(c.getCollisionBox().intersectsQuad(p.getCollisionBox())){
								if(p.getState() == PressurePlate.unpressed){
									p.animate(loader);
									((PressurePlate) p).setIdentity(c.getIdentity());
									int anchor = ((PressurePlate) p).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g : gates){
											if(((Gate) g).getAnchor() == anchor) {
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//Crate World Bounds Collision Detection
				//Crate Hitbox Collision Detection
				for(Sprite hb : hbxs){
					if( hb.getLayer() == c.getLayer()){
						if(c.getCollisionBox().intersectsQuad(hb.getCollisionBox())){
							Physics.spriteCollision(c, hb.getCollisionBox());
						}
					}
				}
				//Crate NPC Collision Detection
				//Crate Villain Collision Detection
				//Crate Gate Collision
				//Crate Light Source Collision Detection
				//Hackbox Collision Detection
				//Sign Collision Detection
				//Save Crystal Collision Dtection
			}
		}
		
		//Barrel Update
		if(world.getBarrels().size() > 0){
			ArrayList<Sprite> barrels = world.getBarrels();
			for(Sprite b : barrels){
				if(world.getPressurePlates().size() > 0){
					ArrayList<Sprite> plates = world.getPressurePlates();
					for(Sprite p : plates){
						if(b.getLayer() == p.getLayer()){
							if(b.getCollisionBox().intersectsQuad(p.getCollisionBox())){
								if(p.getState() == PressurePlate.unpressed){
									p.animate(loader);
									((PressurePlate) p).setIdentity(b.getIdentity());
									int anchor = ((PressurePlate) p).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g : gates){
											if(((Gate) g).getAnchor() == anchor) {
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//Crate World Bounds Collision Detection
				//Crate Hitbox Collision Detection
				//Crate NPC Collision Detection
				//Crate Gate Collision
				//Crate Light Source Collision Detection
				//Hackbox Collision Detection
				//Sign Collision Detection
				//Save Crystal Collision Dtection
			}
		}
		
		//Pressure Plate Update
		if(world.getPressurePlates().size() > 0){
			ArrayList<Sprite> plates = world.getPressurePlates();
			Hero h = (Hero) world.getHero();
			for(Sprite pl : plates){
				//npc
				if(world.getNPCs().size() > 0){
					ArrayList<Sprite> npcs = world.getNPCs();
					for(Sprite n : npcs){
						if(n.getIdentity() == pl.getIdentity()){
							if(!n.getCollisionBox().intersectsQuad(pl.getCollisionBox())){
								if(pl.getState() == PressurePlate.pressed){
									pl.animate(loader);
									((PressurePlate) pl).setIdentity(PressurePlate.NO_IDENTITY);
									int plateanchor = ((PressurePlate) pl).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g: gates){
											if(((Gate) g).getAnchor() == plateanchor){
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//villain
				if(world.getVillains().size() > 0){
					ArrayList<Sprite> vils = world.getVillains();
					for(Sprite vil : vils){
						if(vil.getIdentity() == pl.getIdentity()){
							if(!vil.getCollisionBox().intersectsQuad(pl.getCollisionBox())){
								if(pl.getState() == PressurePlate.pressed){
									pl.animate(loader);
									((PressurePlate) pl).setIdentity(PressurePlate.NO_IDENTITY);
									int plateanchor = ((PressurePlate) pl).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g: gates){
											if(((Gate) g).getAnchor() == plateanchor){
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//barrel 
				if(world.getBarrels().size() > 0){
					ArrayList<Sprite> barrels = world.getBarrels();
					for(Sprite b : barrels){
						if(b.getIdentity() == pl.getIdentity()){
							if(!b.getCollisionBox().intersectsQuad(pl.getCollisionBox())){
								if(pl.getState() == PressurePlate.pressed){
									pl.animate(loader);
									((PressurePlate) pl).setIdentity(PressurePlate.NO_IDENTITY);
									int plateanchor = ((PressurePlate) pl).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g: gates){
											if(((Gate) g).getAnchor() == plateanchor){
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//crate
				if(world.getCrates().size() > 0){
					ArrayList<Sprite> crates = world.getCrates();
					for(Sprite c : crates){
						if(c.getIdentity() == pl.getIdentity()){
							if(!c.getCollisionBox().intersectsQuad(pl.getCollisionBox())){
								if(pl.getState() == PressurePlate.pressed){
									pl.animate(loader);
									((PressurePlate) pl).setIdentity(PressurePlate.NO_IDENTITY);
									int plateanchor = ((PressurePlate) pl).getAnchor();
									if(world.getGates().size() > 0){
										ArrayList<Sprite> gates = world.getGates();
										for(Sprite g: gates){
											if(((Gate) g).getAnchor() == plateanchor){
												g.animate(loader);
											}
										}
									}
								}
							}
						}
					}
				}
				//hero
				if(pl.getIdentity() == Hero.HERO_IDENTITY){
					if(!h.getCollisionBox().intersectsQuad(pl.getCollisionBox())){
						if(pl.getState() == PressurePlate.pressed){
							pl.animate(loader);
							((PressurePlate) pl).setIdentity(PressurePlate.NO_IDENTITY);
							int plateanchor = ((PressurePlate) pl).getAnchor();
							if(world.getGates().size() > 0){
								ArrayList<Sprite> gates = world.getGates();
								for(Sprite g: gates){
									if(((Gate) g).getAnchor() == plateanchor){
										g.animate(loader);
									}
								}
							}
						}
					}
				}
			}
		}
		//Light Source Update
		if(world.getLights().size() > 0){
			ArrayList<Sprite> lights = world.getLights();
			if(world.getSwitches().size() > 0){
				ArrayList<Sprite> swit = world.getSwitches();
				for(Sprite l : lights){
					LightSource ls = (LightSource) l;
					if(ls.isLava() || ls.getType() == LightSource.stove || ls.getType() == LightSource.campfire) 
						{ l.animate(loader); }
					int lightanchor = ls.getAnchor();
					for(Sprite sw : swit){
						int switchanchor = ((Switch) sw).getAnchor();
						if(lightanchor == switchanchor){
							if(sw.getState() == Switch.on){
								if(l.getState() != LightSource.on) { l.setState(LightSource.on); }
							}
							else if( sw.getState() == Switch.off){
								if(l.getState() != LightSource.off) { l.setState(LightSource.off); }
							}
								
						}
					}
				}
			}
		}
	}
	
	/**
	 * @render
	 * 
	 * @description render is where all of the Sprites from GameWorld are done onto the
	 * buffer. Most of the renderings are done via the following pseudo-coded process:
	 * 
	 * 	iterate over ArrayList containing Sprites
	 * 		if Sprite is in same world and side as Hero
	 * 			if Sprite is on screen
	 * 				render Sprite
	 * 
	 * the conditional checks are done in order of complexity. the world of a Sprite is 
	 * contained in a boolean. The first conditional check is a simple comparison.
	 * If the first conditional is passed, the second conditional is a slightly more 
	 * complicated mathematical computation check to see if the Sprite is onscreen.
	 * If the Sprite passes both checks, it is then rendered onto the buffer image
	 * to await rendering on screen. 
	 * 
	 * Some Sprite rendering loops contain slight deviations specific to the type of Sprite
	 * being iterated over; the biggest deviation is contained in the loop that processes 
	 * the rendering of LightSources. As the loop iterates over LightSource Sprites, it 
	 * accumulates the area of onscreen light sources and their radius of light. This area
	 * is then used to clip the buffer image to give the appearance of darkness and light
	 * sources. 
	 * 
	 * TODO: Condense all sprite paint loops into one loop that iterates over sprite types
	 */
	private void render(){ 
		if(buffer == null) { buffer = gc.createCompatibleImage(this.getWidth(), this.getHeight()); }
		Graphics2D g2 = (Graphics2D) buffer.getGraphics(); 
		Point screenDim = new Point( this.getWidth(), this.getHeight());
		Point worldDim = world.getDimensions();
		Point heroPosition = world.getHero().getMin();
		ArrayList<Sprite> npcs = world.getNPCs();
		ArrayList<Sprite> tcs = world.getTreasures();
		ArrayList<Sprite> st = world.getShovelTiles();
		ArrayList<Sprite> vils = world.getVillains();
		ArrayList<Sprite> lits = world.getLights();
		ArrayList<Sprite> swits = world.getSwitches();
		ArrayList<Sprite> effs = world.getEffects();
		ArrayList<Sprite> proj = world.getProjectiles();
		ArrayList<Sprite> hkbx = world.getHackboxes();
		ArrayList<Sprite> plates = world.getPressurePlates();
		ArrayList<Sprite> gates = world.getGates();
		ArrayList<Sprite> crates = world.getCrates();
		ArrayList<Sprite> barrels = world.getBarrels();
		ArrayList<Sprite> signs = world.getSigns();
		ArrayList<Sprite> books = world.getBooks();
		ArrayList<Sprite> crystals = world.getSaveCrystals();
		int layer = world.getLayer();
		
		//START: Paint background
		RenderUtilities.paintLevel(g2, loader.getWorldFrame(layer, false), heroPosition, screenDim);
		//END: Paint background
	
		//START: Paint Under Effects
		if(effs.size() > 0){
			for(Sprite ef : effs){
				if(((Effect) ef).getOverUnder() == Effect.UNDER){
					if( ef.getLayer() == layer){
						Point p1 = ef.getMin(); Point p2 = ef.getMax();
						if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
							RenderUtilities.paintSprite(g2, ef.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
						}
					}
				}
			}
		}
		//END: Paint Under Effects
		
		//START: Paint Books
		if(books.size() > 0){
			for(Sprite bo : books){
				if(bo.getLayer() == layer){
					Point p1 = bo.getMin(); Point p2 = bo.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, bo.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Books
		
		//START: Paint Signs
		if(signs.size() > 0){
			for(Sprite si : signs){
				if(si.getLayer() == layer){
					Point p1 = si.getMin(); Point p2 = si.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, si.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Signs
		
		//START: Paint Save Crystals
		if(crystals.size() > 0){
			for(Sprite sv : crystals){
				if(sv.getLayer() == layer){
					Point p1 = sv.getMin(); Point p2 = sv.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, sv.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Save Crystals
		
		//START: Paint Gates
		if(gates.size() > 0) {
			for(Sprite g: gates){
				if(g.getLayer() == layer){
					Point p1 = g.getMin(); Point p2 = g.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, g.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Gates
				
		//START: Paint Treasure Chests
		if(tcs.size() > 0) {
			for(Sprite tr : tcs){
				if(tr.getLayer() == layer){
					Point p1 = tr.getMin(); Point p2 = tr.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, tr.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Treasure Chests
		
		//STATE: Paint Shovel Tiles
		if(st.size() > 0){
			for(Sprite s : st){
				if(s.getLayer() == layer){
					Point p1 = s.getMin(); Point p2 = s.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, s.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Shovel Tiles
		
		//START: Paint Light Sprites (NOT SOURCES!)
		if(lits.size() > 0){
			for(Sprite l : lits){
				if(l.getLayer() == layer){
					Point p1 = l.getMin();
					Point p2 = l.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, l.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Light Sprites (NOT SOURCES!)
		
		//START: Paint Switches
		if(swits.size() > 0 ){
			for(Sprite swit : swits){
				if(swit.getLayer() == layer){
					Point p1 = swit.getMin(); Point p2 = swit.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, swit.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Switches
		
		//START: Paint PressurePlates
		if(plates.size() > 0){
			for(Sprite pp: plates){
				if(pp.getLayer() == layer){
					Point p1 = pp.getMin(); Point p2 = pp.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, pp.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		
		//START: Paint Hackboxes
		if(hkbx.size() > 0){
			for(Sprite hk : hkbx){
				if(hk.getLayer() == layer){
					Point p1 = hk.getMin(); Point p2 = hk.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, hk.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Hackboxes
		
		//START: Paint Crates
		if(crates.size() >0) {
			for(Sprite c: crates) {
				if(c.getLayer() == layer){
					Point p1 = c.getMin(); Point p2 = c.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, c.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Crates
		
		//START: Paint Barrels
		if(barrels.size() > 0){
			for(Sprite b : barrels){
				if(b.getLayer() == layer){
					Point p1 = b.getMin(); Point p2 = b.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, b.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Barrels
		
		//START: Paint NPCs
		if(npcs.size() > 0){
			for(Sprite npc : npcs){
				if(npc.getLayer() == layer){
					Point p1 = npc.getMin(); Point p2 = npc.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, npc.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint NPCs
				
		//START: Paint Villains
		if(vils.size() > 0){
			for(Sprite vil : vils){
				if(vil.getLayer() == layer){
					Point p1 = vil.getMin(); Point p2 = vil.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						Villain v = (Villain) vil;
						if(!v.isStunned()){ RenderUtilities.paintSprite(g2, vil.getCurrentFrame(), heroPosition, p1, screenDim, worldDim); }
						else {  RenderUtilities.paintStunnedSprite(g2, vil, vil.getCurrentFrame(), heroPosition, screenDim, worldDim, v.getStun(), v.getStunCounter()); }
					}
				}
			}
		}
		//END: Paint Villains
		
		//START: Paint Hero
		Hero h = (Hero) world.getHero();
		if(!h.getStunned()){
			RenderUtilities.paintHero(g2, h.getCurrentFrame(), heroPosition, screenDim, worldDim); 
			
		}
		else{
			int totalFade = h.getStunCounter();
			double inc = 0.8/totalFade;
			for(int i = 0; i< totalFade; i++){
				float thisFade = (float)(inc*i + 0.2);
				if(h.getStunCount()%h.getStunCounter()==i){
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,thisFade));
					RenderUtilities.paintHero(g2, h.getCurrentFrame(), heroPosition, screenDim, worldDim);
				}
			}
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
		}
		if(h.getLanterning()){
			Point p = new Point( heroPosition.getX() + h.getWidth()/2, heroPosition.getY() + h.getHeight()/2);
			RenderUtilities.paintSprite(g2, loader.getSingletonFrame(SpriteImageLoader.Lantern_INDEX), heroPosition, p, screenDim, worldDim);
		}
		//END: Paint Hero
		
		//START: Paint Over Effects
		if(effs.size() > 0){
			for(Sprite ef : effs){
				if(((Effect) ef).getOverUnder() == Effect.OVER){
					if(ef.getLayer() == layer){
						Point p1 = ef.getMin(); Point p2 = ef.getMax();
						if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
							RenderUtilities.paintSprite(g2, ef.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
						}
					}
				}
			}
		}
		//END: Paint Over Effects
		
		//STATE: Paint Projectiles
		if(proj.size() > 0){
			for(Sprite p : proj){
				Point p1 = p.getMin(); Point p2 = p.getMax();
				if(p.getLayer() == layer){
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, p.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Projectiles
		
		//START: Paint Over Frame
		RenderUtilities.paintLevel(g2, loader.getWorldFrame(world.getLayer(), true), heroPosition, screenDim); 
		//END: Paint Over Frame
				
		//START: Paint Light Sources
		RenderUtilities.clipLightSources(g2, gc, buffer, lits, ((Hero) world.getHero()), layer, screenDim, worldDim);
		//END: Paint Light Sources
		
		//START: Paint Message
		if(reading){ g2.drawImage(msg.getCurrentFrame(), (int) msg.getMinX(), (int) msg.getMinY(), null); }
		//END: Paint Message
		
		//START: Paint Menu
		if(paused){
			if(menu!=null){ g2.drawImage(menu.getCurrentFrame(), (int) menu.getMinX(), (int) menu.getMinY(), null); }
			//START: Paint Sub Menu
			if(subbing) { if(sub != null){ g2.drawImage(sub.getCurrentFrame(), (int) sub.getMinX(), 
									   (int) sub.getMinY(), null); } }
			//END: Paint Sub Menu
		}
		//END: Paint Menu
		
		//START: Paint Equip Sub Menu
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.35f));
		g2.drawImage(equipsub.getCurrentFrame(), (int) equipsub.getMinX(),
					 (int) equipsub.getMinY(), null);
		//End: Paint Equip Sub Menu
		g2.dispose();
	}
	
	/**
	 * @Method paintScreen:
	 * 
	 * @description: paints the buffer directly to the JPanel. 
	 */
	private void paintScreen(){
		Graphics g;
		try{
			g = this.getGraphics();
			if(g!=null && buffer != null) { g.drawImage(buffer, 0, 0, null); }
			Toolkit.getDefaultToolkit().sync();
			g.dispose();
			frames++;
		}
		catch(Exception e){	}
	}
	
	
	public boolean isSaving(){ return saving; }
	
	public boolean isScened() { return scened; }
	
	public boolean isPortaled() { return portaled; }
	
	public void flushPortaled() { portaled = false; }
	
	public void flushSaving(){ saving = false; }
	
	public void flushScened() { scened = false; }
	
	public void setWorld(GameWorld w) { world = w; configure(); }
	
	public void setLoader(SpriteImageLoader l) { loader = l; }
	
	public GameWorld getWorld(){ return world; }
	
	public String getPortalChoice() { return portalchoice; }
	
	public Point getNextInsertionPoint() { return nextInsertion; }
	
	public int getNextInsertionLayer() { return nextLayer; }
	
	public String getSceneChoice() { return scenechoice; }
	
}
