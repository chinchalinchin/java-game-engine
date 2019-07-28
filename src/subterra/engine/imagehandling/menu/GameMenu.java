package subterra.engine.imagehandling.menu;

import java.awt.Color; 
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import subterra.geometry.Line;
import subterra.geometry.Point;
import subterra.geometry.Quad;
import subterra.interfaces.Sprite;
import subterra.library.Hero;
import subterra.library.Item;
import subterra.engine.View;
import subterra.engine.imagehandling.SpriteImageLoader;

public class GameMenu extends Quad 
					  implements Sprite{

	//TODO: Read in menu configuration file to get paths for 
	/**@GameMenu
	 * 
	 * @description can be instantiated as one of several different types of in-game menus. 
	 * The types can be accessed statically and are stored as integers numbered 0 - 6. 
	 * The type is referenced statically by the View class and then passed into the 
	 * constructor of the GameMenu. The types and their purpose in-game are detailed
	 * as follows:
	 * 
	 * 		0 - mainmenu:
	 * 		1 - statusmenu:
	 * 		2 - itemmenu:
	 * 		3 - equipmenu:
	 * 		4 - savemenu:
	 * 		5 - mapmenu:
	 * 		6 - equipsubmenu:
	 * 
	 * GameMenus are traversed by iterating the state field.
	 * 
	 * GameMenus hold their own images internally for formatting purposes. They make no
	 * calls to the SpriteImageLoader. GameMenus are the only Sprites that hold their
	 * own images. This is because there can be only one instance of a GameMenu in-game
	 * duplication is impossible. Moreover, GameMenu contains special formatting methods
	 * that draw on the menu based on the user's selections, passed in from the View.
	 * 
	 * @note the equipsubmenu is the transuclent menu displayed at all times 
	 * on the bottom left of the screen. the equipmenu is the equipment 
	 * menu selected through the main in-game menu.
	 */
	//FILEPATHS
	private static final String menupath = imgpath + "menu" + sep + "game_menu" + sep;
	private static final String subpath = imgpath + "menu" + sep;

	//STATES
		//Main Game Menu States
	public static final int status = 0, items = 1, equip = 2,
							map = 3, save = 4, goback = 5;
		//Item Menu States
	public static final int key = 0, potion = 1, hipotion = 2, 
							bomb = 3, bolt = 4;
		//Equip Menu States
	public static final int lantern = 0, shovel = 1, crossbow = 2, shield = 3,
							hammer = 4, rocket_skates = 5, visor = 6, axe = 7, 
							blank1 = 8, blank2 = 9, blank3 = 10, pike = 11; 
	
	//TYPES
	public static final int mainmenu = 0, statusmenu = 1, itemmenu = 2,
							equipmenu = 3, savemenu = 4, mapmenu = 5,
							equipsubmenu = 6; 
	
	//PROPERTIES
		//Main Menu Properties
	public static final double defaultHeight = 157, defaultWidth = 95,
							   topBorder = 13, betweenBorders = 7, sideBorderWidth = 5,
							   itemWidth = 57, itemHeight = 17, iconWidth = 18, iconHeight = 17;
	private static final int   menuselections = 6;
	private static final String[] menuSelect = {"Status", "Items", "Equip", "Map", "Save", "Return" };
	
		//Status Menu Properties
	public static final double defaultStatusHeight = 99, defaultStatusWidth = 159;
	private static final int statusselections = 0;
	
		//Item Menu Properties
	public static final double defaultItemHeight = 99, defaultItemWidth = 159;
	private static final int itemselections = 7;
	
		//Equip Menu Properties
	public static final double defaultEquipHeight = 130, defaultEquipWidth = 162,
							   squareSide = 32, squareBorder = 2, outerBorder =16;
	public static final int equipmentselections = 12;
	
		//Equip Sub Menu Properties
	public static final double defaultEquipSubHeight = 66, defaultEquipSubWidth = 128;
	public static final int equipsubselections = 3;
	
	private BufferedImage menuFrame, inactiveIcon, activeIcon;
	private BufferedImage activeEquipBorder, equip1Border, equip2Border, equip3Border;
	private ArrayList<BufferedImage> equipment;
	private boolean saved;
		//determines if save item on main menu should be grayed out!
	private int state, type, selections;
	
	public GameMenu(Point p, int thisType) {
		super(p.getX(), p.getY(), defaultWidth, defaultHeight);
		type = thisType;
		if(type == mainmenu) { selections = menuselections; }
		if(type == statusmenu) { 
			selections = statusselections; 
			super.setWidth(defaultStatusWidth);
			super.setHeight(defaultStatusHeight);
			loadFrame();
		}
		if(type == itemmenu) {
			selections = itemselections;
			super.setWidth(defaultItemWidth);
			super.setHeight(defaultItemHeight);
			loadFrame();
		}
		if(type == equipmenu){
			equipment = new ArrayList<BufferedImage>();
			selections = equipmentselections;
			super.setWidth(defaultEquipWidth);
			super.setHeight(defaultEquipHeight);
			loadFrame();
		}
		if(type == equipsubmenu){
			equipment = new ArrayList<BufferedImage>();
			selections = equipsubselections;
			super.setWidth(defaultEquipSubWidth);
			super.setHeight(defaultEquipSubHeight);
			loadFrame();
		}
		state = 0;
	}
	
	public void initMain(boolean saving){
		saved = saving; loadFrame();
	}

	private void loadFrame(){ 
		switch(type){
			case mainmenu:
				BufferedImage bf = null, bi = null, ba = null;
				try{ 
					bf = ImageIO.read(new File(menupath + "menu.png")); 
					bi = ImageIO.read(new File(menupath + "inactive_icon.png"));
					ba = ImageIO.read(new File(menupath + "active_icon.png"));
				}
				catch(IOException ie) { System.out.println("Error loading: " + menupath + "bippity"); }
				if(bf != null) { menuFrame = bf; }
				if(bi != null) { inactiveIcon = bi; }
				if(ba != null) { activeIcon = ba; }
				Graphics2D g2  = (Graphics2D) menuFrame.getGraphics();
					//Write menu items on menuFrame 
				Color temp = g2.getColor();
				for(int i = 0; i < menuselections; i++){
					int w = g2.getFontMetrics().stringWidth(menuSelect[i]);
					int leftover = (int) (itemWidth - w)/2;
					int startX = (int) (2*sideBorderWidth + iconWidth);
					int startY = (int) (topBorder + i*betweenBorders + (i+1)*itemHeight-5);
					if(i == 4 && !saved) { g2.setColor(Color.lightGray); }
					g2.drawString(menuSelect[i], startX + leftover, startY);
					if(i == 4 && !saved) { g2.setColor(temp);}
				}
				g2.dispose();
				setActiveSelection();
				break;
			case statusmenu:
				BufferedImage bs = null;
				try{ bs = ImageIO.read(new File(subpath + "dialogue_box.png")); }
				catch(IOException ie) {	System.out.println("Error loading: " + subpath + "dialogue_box.png");}
				if(bs != null) { menuFrame = bs; }
				break;
			case itemmenu:
				BufferedImage bim = null;
				try{ bim = ImageIO.read(new File(subpath + "dialogue_box.png")); }
				catch(IOException ie) {	System.out.println("Error loading: " + subpath + "dialogue_box.png"); }
				if(bim != null) { menuFrame = bim; }
				break;
			case equipmenu:
				BufferedImage be = null, beq = null, bae = null, e1 = null, e2 = null, e3 = null;
				try{ 
					be = ImageIO.read(new File(menupath + "equipmenu_blank.png")); 
					bae = ImageIO.read(new File(menupath + "equipmenu_square_active_border.png"));
					e1 = ImageIO.read(new File(menupath + "equipmenu_square_equip1_border.png"));
					e2 = ImageIO.read(new File(menupath + "equipmenu_square_equip2_border.png"));
					e3 = ImageIO.read(new File(menupath + "equipmenu_square_equip3_border.png"));
					}
				catch(IOException ie) {	System.out.println("Error loading: " + menupath + "equipmenu_*");}
				if(be != null) { menuFrame = be;} if(bae != null) {activeEquipBorder = bae;}
				if(e1 != null){	equip1Border = e1; } if(e2 != null) { equip2Border = e2;}
				if (e3 != null) {equip3Border = e3;}
				for(int i = 0; i < selections; i++){
					String filepath = menupath;
					switch(i){
						case lantern: filepath = filepath + "equipmenu_square_lantern.png";
							break;
						case shovel: filepath = filepath + "equipmenu_square_shovel.png"; 
							break;
						case crossbow: filepath = filepath + "equipmenu_square_crossbow.png";
							break;
						case hammer: filepath = filepath + "equipmenu_square_hammer.png";
							break;
						case rocket_skates: filepath = filepath + "equipmenu_square_rocket_skates.png";
							break;
						case visor: filepath = filepath + "equipmenu_square_visor.png";
							break;
						case shield: filepath = filepath + "equipmenu_square_shield.png"; 
							break;
						case axe: filepath = filepath + "equipmenu_square_axe.png";
							break;
						case pike: filepath = filepath + "equipmenu_square_pike.png"; 
							break;
						}
					try{ beq = ImageIO.read(new File(filepath));}
					catch(IOException ie) { System.out.println("Error loading: " + filepath); }
					if(beq != null) { equipment.add(beq); } 
				}
				break;
				
			case equipsubmenu:
				BufferedImage esm = null, seq = null;
				try { esm = ImageIO.read(new File(menupath + "equipsubmenu.png")); }
				catch(IOException ie) { System.out.println("Error loading: " + menupath + "equipsubmenu.png"); }
				if(esm != null) { menuFrame = esm; }
				for(int i = 0; i < equipmentselections; i++){
					String filepath = menupath;
					switch(i){
						case lantern: filepath = filepath + "equipmenu_square_lantern.png";
							break;
						case shovel: filepath = filepath + "equipmenu_square_shovel.png"; 
							break;
						case crossbow: filepath = filepath + "equipmenu_square_crossbow.png";
							break;
						case hammer: filepath = filepath + "equipmenu_square_hammer.png";
							break;
						case rocket_skates: filepath = filepath + "equipmenu_square_rocket_skates.png";
							break;
						case visor: filepath = filepath + "equipmenu_square_visor.png";
							break;
						case shield: filepath = filepath + "equipmenu_square_shield.png"; 
							break;
						case axe: filepath = filepath + "equipmenu_square_axe.png";
							break;
						case pike: filepath = filepath + "equipmenu_square_pike.png"; 
							break;
						}
					try{ seq = ImageIO.read(new File(filepath));}
					catch(IOException ie) { System.out.println("Error loading: " + filepath); }
					equipment.add(seq);
				}
				break;
			}
	}
	
	private void setActiveSelection(){
		Graphics2D g2 = (Graphics2D) menuFrame.getGraphics();
		for(int i =0; i<menuselections; i++){
				int startY = (int) (topBorder + i*betweenBorders + i*itemHeight + 
								   (iconHeight-activeIcon.getHeight())/2);
				int startX = (int) (sideBorderWidth + (iconWidth - activeIcon.getWidth())/2);
				if(i == state) { g2.drawImage(activeIcon, startX, startY, null); }
				else { g2.drawImage(inactiveIcon, startX, startY, null); }
		}
		g2.dispose();
	}
	
	/**
	 * @Method formatSubEquipment
	 * 
	 * @description alters the menuFrame by receiving the user's equipment 
	 * selection and drawing on the Graphics of the image.
	 * 
	 * @param equipSelected map of user's current equipment selections. Passed
	 * in through the View to determine which equipment icons to draw on the 
	 * equipment sub menu.
	 */
	public void formatSubEquipment(int[] equipSelected){
		Graphics2D g2 = (Graphics2D) menuFrame.getGraphics();
		for(int i = 0; i<selections; i++){
			if(equipSelected[i] < equipmentselections+1){
				equipment.get(equipSelected[i]);
				int x = (int)(outerBorder + i*squareSide), y = (int) outerBorder;
				g2.drawImage(equipment.get(equipSelected[i]), x, y, null);
			}
		}
		g2.dispose();
	}
	
	/**
	 * @Method formatEquipment
	 * 
	 * @description 
	 * 
	 * @param triggers : boolean[]. Represents equipment the user has enabled
	 * throughout his journey. Passed in through the View. 
	 * 
	 * @param equipSelected
	 * 
	 *
	 */
	public void formatEquipment(boolean[] triggers, int[] equipSelected){
		Graphics2D g2 = (Graphics2D) menuFrame.getGraphics();
		for(int i = 0; i < selections; i++){
			switch(i){
				case lantern:
					if(triggers[lantern]){ g2.drawImage(equipment.get(lantern), (int)outerBorder, 
														(int)outerBorder, null); }
					if(state == lantern){ g2.drawImage(activeEquipBorder, (int)outerBorder,
													  (int)outerBorder, null); }
					else if(equipSelected[View.equipMap1] == lantern){ g2.drawImage(equip1Border, (int)outerBorder,
							  														(int)outerBorder, null); }
					else if(equipSelected[View.equipMap2] == lantern){ g2.drawImage(equip2Border, (int)outerBorder,
							  													    (int)outerBorder, null);}
					else if(equipSelected[View.equipMap3] == lantern){ g2.drawImage(equip3Border, (int)outerBorder,
							  													   (int)outerBorder, null);	}
					break;
				case shovel:
					if(triggers[shovel]){ g2.drawImage(equipment.get(shovel), (int) (outerBorder+squareSide),
													  (int)outerBorder, null); }
					if(state == shovel){ g2.drawImage(activeEquipBorder, (int)(outerBorder+squareSide),
													 (int)outerBorder, null); }
					else if(equipSelected[View.equipMap1] == shovel){ g2.drawImage(equip1Border, (int)(outerBorder+squareSide),
							 													 (int)outerBorder, null); }
					else if(equipSelected[View.equipMap2] == shovel){ g2.drawImage(equip2Border, (int)(outerBorder+squareSide),
																				  (int)outerBorder, null); }
					else if(equipSelected[View.equipMap3] == shovel){ g2.drawImage(equip2Border, (int)(outerBorder+squareSide),
							 													   (int)outerBorder, null);}
					break;
				case crossbow:
					if(triggers[crossbow]){ g2.drawImage(equipment.get(crossbow),(int) (outerBorder + 2*squareSide),
							     		   (int)outerBorder, null); }
					if(state == crossbow) { g2.drawImage(activeEquipBorder, (int)(outerBorder+2*squareSide),
									                     (int)outerBorder, null); }
					else if(equipSelected[View.equipMap1] == crossbow){ g2.drawImage(equip1Border, (int)(outerBorder+2*squareSide),
		                     														(int)outerBorder, null);}
					else if(equipSelected[View.equipMap2] == crossbow){ g2.drawImage(equip2Border, (int)(outerBorder+2*squareSide),
		                     														(int)outerBorder, null);}
					else if(equipSelected[View.equipMap3] == crossbow){ g2.drawImage(equip3Border, (int)(outerBorder+2*squareSide),
		                     														(int)outerBorder, null);}
					break;
				case hammer:
					if(triggers[hammer]){ g2.drawImage(equipment.get(hammer), (int)outerBorder,
													   (int)(outerBorder+squareSide), null); }
					if(state == hammer){ g2.drawImage(activeEquipBorder, (int)outerBorder,
													  (int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap1] == hammer){ g2.drawImage(equip1Border, (int)outerBorder,
																				  (int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap2] == hammer){ g2.drawImage(equip2Border, (int)outerBorder,
							  													  (int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap3] == hammer){ g2.drawImage(equip3Border, (int)outerBorder,
							  													  (int)(outerBorder+squareSide), null); }
					break;
				case rocket_skates:
					if(triggers[rocket_skates]){	g2.drawImage(equipment.get(rocket_skates), (int)(outerBorder+squareSide),
														    (int)(outerBorder+squareSide), null); }
					if(state == rocket_skates){ g2.drawImage(activeEquipBorder, (int)(outerBorder+squareSide),	
														  (int)(outerBorder + squareSide), null); }
					else if(equipSelected[View.equipMap1] == rocket_skates){ g2.drawImage(equip1Border, (int)(outerBorder+squareSide),	
							  														  (int)(outerBorder + squareSide), null); }
					else if(equipSelected[View.equipMap2] == rocket_skates){ g2.drawImage(equip2Border, (int)(outerBorder+squareSide),	
							  														  (int)(outerBorder + squareSide), null); }
					else if(equipSelected[View.equipMap3] == rocket_skates){ g2.drawImage(equip3Border, (int)(outerBorder+squareSide),	
							  														  (int)(outerBorder + squareSide), null); }
					break;
				case visor:
					if(triggers[visor]){	g2.drawImage(equipment.get(visor), (int)(outerBorder+2*squareSide),
								 						 (int)(outerBorder+squareSide), null); }
					if(state == visor){ g2.drawImage(activeEquipBorder, (int)(outerBorder + 2*squareSide),
													   (int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap1] == visor){ g2.drawImage(equip1Border, (int)(outerBorder + 2*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap2] == visor){ g2.drawImage(equip2Border, (int)(outerBorder + 2*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap3] == visor){ g2.drawImage(equip3Border, (int)(outerBorder + 2*squareSide),
							   (int)(outerBorder+squareSide), null); }
					break;
				case shield:
					if(triggers[shield]){ g2.drawImage(equipment.get(shield), (int)(outerBorder+3*squareSide),
													 (int)outerBorder, null); }
					if(state == shield) { g2.drawImage(activeEquipBorder, (int)(outerBorder + 3*squareSide),
													  (int)outerBorder, null); }
					else if(equipSelected[View.equipMap1] == shield){ g2.drawImage(equip1Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap2] == shield){ g2.drawImage(equip2Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap3] == shield){ g2.drawImage(equip3Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					break;
				case axe:
					if(triggers[axe]){ g2.drawImage(equipment.get(axe), (int)(outerBorder+3*squareSide),
													(int)(outerBorder+squareSide), null); }
					if(state == axe){ g2.drawImage(activeEquipBorder, (int)(outerBorder+3*squareSide),
												  (int)(outerBorder+squareSide),null); }
					else if(equipSelected[View.equipMap1] == axe){ g2.drawImage(equip1Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap2] == axe){ g2.drawImage(equip2Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap3] == axe){ g2.drawImage(equip3Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					break;
				case pike:
					if(triggers[pike]){	g2.drawImage(equipment.get(pike), (int)(outerBorder + 3*squareSide),
													 (int)(outerBorder+2*squareSide), null); }
					if(state == pike){ g2.drawImage(activeEquipBorder, (int)(outerBorder+3*squareSide),
													(int)(outerBorder+2*squareSide), null); }
					else if(equipSelected[View.equipMap1] == shield){ g2.drawImage(equip1Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap2] == shield){ g2.drawImage(equip2Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					else if(equipSelected[View.equipMap3] == shield){ g2.drawImage(equip3Border, (int)(outerBorder + 3*squareSide),
																					(int)(outerBorder+squareSide), null); }
					break;
			}
		}
		g2.dispose();
	}
	
	public void formatStatus(Hero h){
		String health = "Health: " + h.getCurrentHP() + " / " + h.getTotalHP();
		String attack = "Attack: " + h.getATK();
		String defense = "Defense: " + h.getDEF();
		Graphics2D g2 = (Graphics2D) menuFrame.getGraphics();
		int wordHeight = g2.getFontMetrics().getHeight();
		int buff = 5;
		g2.drawString(health, buff, buff+wordHeight);
		g2.drawString(attack, buff, 2*(buff+wordHeight));
		g2.drawString(defense, buff, 3*(buff+wordHeight));
		g2.dispose();
	}
	
	public void formatItems(int[][] itempouch){ 
		loadFrame();
		Graphics2D g2 = (Graphics2D) menuFrame.getGraphics();
		int wordHeight = g2.getFontMetrics().getHeight();
		int rows = (int) Math.floor(defaultItemHeight / (wordHeight+5));
		int columnwidth = (int) defaultItemWidth/2;
		for(int i = 0; i < Item.items; i++){
			String text = Item.getText(itempouch[i][0]);
			int quant = itempouch[i][1];
			int w = g2.getFontMetrics().stringWidth(text);
			int leftover = columnwidth - g2.getFontMetrics().stringWidth(text+quant)-5;
			if(i<rows){ 
				if(quant>0){
					g2.setColor(Color.WHITE);
					if(i==state){
						g2.fillRect(5, i*(wordHeight+5) + 10,
									columnwidth, wordHeight);
						g2.setColor(Color.BLACK);
						
					}
					g2.drawString(text, 6, (i+1)*(wordHeight+5));
					g2.drawString(""+quant, 5 + w + leftover , (i+1)*(wordHeight+5));
				}
			}
			else{ 
				int newI = i - rows;
				if(quant>0){
					g2.setColor(Color.WHITE);
					if(i==state){
						g2.fillRect(10 + columnwidth, newI*(wordHeight+5) + 10,
									columnwidth-15, wordHeight);
						g2.setColor(Color.BLACK);
					}
					g2.drawString(text, 10 + columnwidth,  (newI+1)*(wordHeight+5));
					g2.drawString(""+quant, w+leftover+columnwidth, (newI+1)*(wordHeight+5));
					}
				}
			}
		g2.dispose();
	}
	

	public BufferedImage getCurrentFrame() { return menuFrame; }
	
	public int getState() {	return state; }
	
	public void setState(int newState) {  
		if(type == mainmenu || type == itemmenu || type == equipmenu || type == savemenu){
			int holder = state;
			state = newState;
			if(state >= selections) { state = 0; }
			if(state < 0) { state = selections - 1; }
			if(type == mainmenu) { 
				if(!saved && state == 4) { 
					if(holder<state){ state++; }
					if(holder>state) { state--; }
				} //skip over save if not applicable
				setActiveSelection(); 
			}
		}
	}
	
	public void setEquipment(int newState, boolean[] triggers){
		int oldState = state;
		setState(newState);
		boolean flag = true;
		while(flag){
			if(!triggers[state]){
				oldState = state;
				if(state > oldState){ setState(state+1); }
				else{ setState(state-1); } //skip over inactive equipment. 
			}
			else{ flag = false;}
		}
	}
	
	//Unimplemented methods
	public int getIdentity(){ return 0; }
	public void animate(SpriteImageLoader loader) { }
	public void resetState() { }
	public void moveX(double delta) {	}
	public void moveY(double delta) { }
	public boolean intersectsSprite(Sprite s) { return false; }
	public ArrayList<Line> getBounds() { return null; }
	public Quad getArea() { return null; }
	public Quad getCollisionBox() { return null; }
	public void setLayer(int newLayer) { }
	public int getLayer() { return 0; }

}
