package subterra.engine;

import java.util.ArrayList;


import subterra.engine.cinematics.SceneTriggers;
import subterra.engine.filehandling.Dialogue;
import subterra.geometry.*;
import subterra.interfaces.*;

public class GameWorld {
	
	/** 
	 * @GameWorld
	 * 
	 * @field Hero : Sprite.
	 * 
	 * See Hero class documentation for more information..
	 * 
	 * @field villains : ArrayList<Sprite>
	 * 
	 * See Villain class documentation for more information.
	 * 
	 * @field npcs : Arraylist<Sprite>.
	 * 
	 * See NPC class documentation for more information.
	 * 
	 * @field doors : Arraylist<Sprite>. Determines where the user can pass
	 * through the inside-outside connections between world one and world 
	 * two. See Door class documentation for more information.
	 * 
	 * @field hitboxes : Arraylist<Sprite>. Determines the invisible boundaries of 
	 * game. Contains information about walls and barriers the player cannot pass 
	 * through.
	 * 
	 * @field treasures: Arraylist<Sprite>.
	 * 
	 * @field shoveltiles: Arraylist<Sprite>.
	 * 
	 * @field itempouch: int[][].
	 * 
	 * @field equipmenttriggers: boolean[].
	 * 
	 * @field bounds: Arraylist<Line>.
	 * 
	 * @field OutOrIn: boolean. Controls whether the inside or outside
	 * of a given world returned when current frame is retrieved
	 * via getter methods in the  View class.
	 * 
	 * @field OneOrTwo: boolean. Controls whether world one or world two
	 * panes are returned when current frame is retrieved via getter 
	 * methods in the View class.
	 *  
	 */

		//World Frame Layer Indices
	public static final int LAYER1_INDEX = 0, LAYER2_INDEX = 1, LAYER3_INDEX = 2, 
							LAYER4_INDEX = 3, LAYER5_INDEX =4;
		//World Bound Indices
	public static final int TOP_INDEX = 0, BOTTOM_INDEX = 1, RIGHT_INDEX = 2, LEFT_INDEX = 3;
	
	private Sprite player;
	private ArrayList<Sprite> villains, npcs, 
							  doors, portals,
							  hitboxes, hackboxes,
							  treasures, shoveltiles, switches, 
							  pressureplates, gates,
							  lights,
							  effects, projectiles,
							  books, signs,
							  crystals, barrels,
							  crates;
	private int[][] itempouch;
	private boolean [] equipmenttriggers;
	private Point dimensions;
	private SceneTriggers scenes; 
	private Dialogue words;
	private ArrayList<Line> bounds;
	private int layer;
	public GameWorld() { projectiles = new ArrayList<Sprite>(); }
		
	private void makeBounds(){  
		bounds = new ArrayList<Line>();
		Point vertex1 = new Point(0,0);
		Point vertex2 = new Point(0,dimensions.getY());
		Point vertex3=  new Point(dimensions.getX(), 
								  dimensions.getY());
		Point vertex4 = new Point(dimensions.getX(), 0);
		Line top = new Line(vertex1, vertex4);
		Line bottom = new Line(vertex2, vertex3);
		Line right = new Line(vertex4, vertex3);
		Line left = new Line(vertex1, vertex2);
		bounds.add(top); bounds.add(bottom);
		bounds.add(right); bounds.add(left);
	}
	
	public void setDimensions(Point d){ dimensions = d; makeBounds(); }
	
	public void setHero(Sprite h) { player = h; }
	
	public void setVillains(ArrayList<Sprite> v){ villains = v; }
	
	public void setHitboxes(ArrayList<Sprite> hb) { hitboxes = hb; }
	
	public void setHackboxes(ArrayList<Sprite> hk) { hackboxes = hk; }
	
	public void setNPCs(ArrayList<Sprite> n) { npcs = n; }
	
	public void setPortals(ArrayList<Sprite> p) { portals = p; }
	
	public void setDoors(ArrayList<Sprite> d) { doors = d;}
	
	public void setTreasures(ArrayList<Sprite> t) { treasures = t; }
	
	public void setShovelTiles(ArrayList<Sprite> s) {shoveltiles = s; }
	
	public void setLights(ArrayList<Sprite> l ) { lights = l; }
	
	public void setSwitches(ArrayList<Sprite> s) { switches = s; }
	
	public void setPressurePlates(ArrayList<Sprite> pp) { pressureplates = pp; }
	
	public void setGates(ArrayList<Sprite> g) { gates = g; }
	
	public void setBarrels(ArrayList<Sprite> b) { barrels = b ; }
	
	public void setCrates(ArrayList<Sprite> c) { crates = c; }
	
	public void setBooks(ArrayList<Sprite> bo) { books = bo; }
	
	public void setSigns(ArrayList<Sprite> s) { signs = s; }
	
	public void setSaveCrystals(ArrayList<Sprite> sc) { crystals = sc; }
	
	public void setEffects(ArrayList<Sprite> e ) { effects = e; }
	
	public void setProjectiles(ArrayList<Sprite> p) { projectiles = p ; }
	
	public void setScenes(SceneTriggers st){ scenes = st; }
	
	public void setDialogue(Dialogue d) { words = d; }
	
	public void setItemPouch(int[][] i) { itempouch = i; }
	
	public void setEquipmentTriggers(boolean[] e) { equipmenttriggers = e; }
	
	public void setLayer(int whichLayer) { layer = whichLayer; }
	
	public Sprite getHero() { return player; }
	
	public ArrayList<Sprite> getVillains() { return villains; }
	
	public ArrayList<Sprite> getHitboxes() { return hitboxes; }
	
	public ArrayList<Sprite> getHackboxes() { return hackboxes; }

	public ArrayList<Sprite> getNPCs() { return npcs; }
	
	public ArrayList<Sprite> getPortals() { return portals; }
	
	public ArrayList<Sprite> getDoors() { return doors; }
	
	public ArrayList<Sprite> getTreasures() { return treasures; }
	
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
	
	public ArrayList<Sprite> getProjectiles() { return projectiles; }
	
	public SceneTriggers getScenes() { return scenes; }
	
	public Dialogue getDialogue() { return words; }
	
	public int[][] getItemPouch() { return itempouch; }
	
	public boolean[] getEquipmentTriggers() { return equipmenttriggers; }
	
	public int getLayer() { return layer; }
	
	public ArrayList<Line> getBounds() { 
		if( bounds == null) { makeBounds(); }
		return bounds; }
	
	public Point getDimensions() { return dimensions; }
	
}
