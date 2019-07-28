package subterra.engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Controller implements KeyListener {

	
	
	//TODO: Solve the triple button press. Solved in vertical directions, ie. LUR and LDR, but not DLU or DLR.
	//TODO: Read in control mappings so user can edit controls via in-game menu.
	
	/**
	 * @Controller: 
	 * 
	 * @note:
	 *  IN-GAME CONTROLS:
	 *  INTERACT = W    
	 *  ENGAGE = SHIFT      
	 *  ATTACK = SPACE       
	 *  MENU = E
	 *  EQUIPMENT1/2/3 = A/S/D   
	 *  ITEMMENU = Q
	 *  MOVE = DIRECTIONAL ARROWS
	 *   
	 *  @field: controls. the number of possible states that can be reached by combinations of user-input and 
	 *  GameWorld events.
	 *   
	 *  @field: active. boolean array. holds all active states on the Controller.
	 *  
	 *  @field: state. integer that represents current state. see description for more information
	 *  on hierarchy of states and how states map to user input and GameWorld events. 
	 *  
	 *  @description the Controller class serves as an implementation of the KeyListener interface. A Controller
	 * has a state that is polled by the game thread in View. The state is determined the combination of keys 
	 * pressed by the user. One key being pressed is called a primary state. A combination of keys being pressed
	 * is called a secondary state.
	 * 
	 * The Controller converts the key presses into an array of booleans. When the state is polled by View,
	 * the state returned is determined by the booleans currently set as true. This way, as long as the user
	 * holds down the key or combination of keys, the Controller will be in the determined state, either until 
	 * the key is released or the state is changed by in-game events. 
	 * 
	 * There is a hierarchy of states. Certain states supersede other states. If the user is walking and attacking 
	 * at the same time, the attacking state is always polled first.
	 * 
	 * The user input is not directly polled, but instead stored in a boolean array. This proxy acts as an 
	 * intermediary to the GameWorld and the user. Some events in-game affect how the user input is expressed
	 * in-game. These events do not correspond to events triggered by the user, but by the GameWorld itself. For
	 * this reason, the Controller class contains flush methods that can alter the state of the boolean input
	 * array. 
	 * 
	 * For instance, attacking in-game is a finite animation and effect. When the user enters into a state of 
	 * attacking, the game treats this as a state that automatically ends after a set number of iterations in the 
	 * game thread, in the case when the user releases the attack key or keeps it held down. In other words, the 
	 * user has to press the button twice to attack twice. If it is the case the user is holding down attack key,
	 * then this would, in the absence of intervention, case the Controller to keep reporting that the current  
	 * state is attack, even though in the View object the animation for attack would have ended. 
	 * 
	 * In order to circumvent problems such as these, the Controller class contains flush methods that can be called
	 * publicly to override the boolean array containing the map of the user input. In other words, in-game events
	 * can supersede user input in certain cases, as in the one detailed above.
	 * 
	 * @hierarchy_of_states: [menu]>[item]>[interact]>[attack]>[equip1]>[equip2]>[equip3]>[walk*]>[run*]
	 * 
	 * @primary_states: primary states have a one-to-one relationship with the keys pressed by the user. 
	 * 
	 * @secondary_states: 
	 * 
	 */
		
		//primary states
	public static final int nothing = 0, walkup = 1, walkdown = 2,
							walkright = 3, walkleft = 4, interact = 5, 
							engage = 6, menu = 7, attack = 8, item = 9,
							equip1 = 10, equip2 = 11, equip3 = 12;
	
		//secondary states
	public static final int walkupright = 13, walkupleft = 14, walkdownright = 15,
						    walkdownleft = 16, runup = 17, rundown = 18,
						    runright = 19, runleft = 20, runupright = 21,
						    rundownright = 22, rundownleft = 23, runupleft = 24;

	
	private static final int controls = 13;
	private boolean[] active;
	
	private int state;
	
	public Controller(){ state = nothing; flushControls(); }
	
	public void flushControls(){ 
		active = new boolean[controls];
		for(int i = 0; i < controls; i ++){ active[i] = false; } 
		determineState();
	}
	
	public void flushInteract(){ active[interact] = false; determineState(); }
	
	public void flushMenu(){ active[menu] = false; determineState();	}
	
	public void flushEngage() { active[engage] = false; determineState();}
	
	public void flushAttack() { active[attack] = false; determineState(); }
	
	public void flushItem() { active[item] = false; determineState(); }
	
	public void flushEquipment() { active[equip1] = false; active[equip2] = false; active[equip3] = false; determineState(); }
	
	private void determineState(){
		if(active[menu]) { state = menu; }
		else if(active[item]) { state = item; }
		else if(active[interact]){ state = interact; }
		else if(active[attack]) { state = attack; }
		else if(active[equip1]) { state = equip1; }
		else if(active[equip2]) { state = equip2; }
		else if(active[equip3]) { state = equip3; }
		else{
			if(!active[engage]){
				if(!active[walkup]&&!active[walkdown]&&!active[walkright]&&!active[walkleft]) {
					state = nothing;
				}
				if( (active[walkup]&&!active[walkdown]&&!active[walkright]&&!active[walkleft]) ||
						(active[walkup]&&!active[walkdown]&&active[walkright]&&active[walkleft]) ){
					state = walkup;
				}
				if( (!active[walkup]&&active[walkdown]&&!active[walkright]&&!active[walkleft]) ||
						(!active[walkup]&&active[walkdown]&&active[walkright]&&active[walkleft])){
					state = walkdown;
				}
				if( (!active[walkup]&&!active[walkdown]&&active[walkright]&&!active[walkleft]) ||
						(active[walkup]&&active[walkdown]&&active[walkright]&&!active[walkleft])){
					state = walkright;
				}
				if( (!active[walkup]&&!active[walkdown]&&!active[walkright]&&active[walkleft]) ||
						(active[walkup]&&active[walkdown]&&!active[walkright]&&active[walkleft])){
					state = walkleft;
				}
				if(active[walkup]&&!active[walkdown]&&active[walkright]&&!active[walkleft]){
					state = walkupright;
				}
				if(active[walkup]&&!active[walkdown]&&!active[walkright]&&active[walkleft]){
					state = walkupleft;
				}
				if(!active[walkup]&&active[walkdown]&&!active[walkright]&&active[walkleft]){
					state = walkdownleft;
				}
				if(!active[walkup]&&active[walkdown]&&active[walkright]&&!active[walkleft]){
					state = walkdownright;
				}
			}
			else{
				if(!active[walkup]&&!active[walkdown]&&!active[walkright]&&!active[walkleft]) {
					state = engage;
				}
				if( (active[walkup]&&!active[walkdown]&&!active[walkright]&&!active[walkleft]) ||
						(active[walkup]&&!active[walkdown]&&active[walkright]&&active[walkleft]) ){
					state = runup;
				}
				if( (!active[walkup]&&active[walkdown]&&!active[walkright]&&!active[walkleft]) ||
						(!active[walkup]&&active[walkdown]&&active[walkright]&&active[walkleft])){
					state = rundown;
				}
				if( (!active[walkup]&&!active[walkdown]&&active[walkright]&&!active[walkleft]) ||
						(active[walkup]&&active[walkdown]&&active[walkright]&&!active[walkleft])){
					state = runright;
				}
				if( (!active[walkup]&&!active[walkdown]&&!active[walkright]&&active[walkleft]) ||
						(active[walkup]&&active[walkdown]&&!active[walkright]&&active[walkleft])){
					state = runleft;
				}
				if(active[walkup]&&!active[walkdown]&&active[walkright]&&!active[walkleft]){
					state = runupright;
				}
				if(active[walkup]&&!active[walkdown]&&!active[walkright]&&active[walkleft]){
					state = runupleft;
				}
				if(!active[walkup]&&active[walkdown]&&!active[walkright]&&active[walkleft]){
					state = rundownleft;
				}
				if(!active[walkup]&&active[walkdown]&&active[walkright]&&!active[walkleft]){
					state = rundownright;
				}
			}
		}
	}
	
	public void keyPressed(KeyEvent ke) {
		int ch = ke.getKeyCode();
		switch(ch){
			case KeyEvent.VK_UP:
				active[walkup] = true;
				break;
			case KeyEvent.VK_DOWN:
				active[walkdown] = true;
				break;
			case KeyEvent.VK_RIGHT:
				active[walkright] = true;
				break;
			case KeyEvent.VK_LEFT:
				active[walkleft] = true;
				break; 
			case KeyEvent.VK_W:
				active[interact] = true;
				break;
			case KeyEvent.VK_SHIFT:
				active[engage] = true;
				break;
			case KeyEvent.VK_SPACE:
				active[attack] = true;
				break;
			case KeyEvent.VK_E:
				active[menu] = true;
				break;
			case KeyEvent.VK_Q:
				active[item] = true;
				break;
			case KeyEvent.VK_A:
				active[equip1] = true;
				break;
			case KeyEvent.VK_S:
				active[equip2] = true;
				break;
			case KeyEvent.VK_D:
				active[equip3] = true;
				break;
			}
		determineState();
	}

	public void keyReleased(KeyEvent ke) { 
		int ch = ke.getKeyCode();
		switch(ch){
			case KeyEvent.VK_UP:
				active[walkup] = false;
				break;
			case KeyEvent.VK_DOWN:
				active[walkdown] = false;
				break;
			case KeyEvent.VK_RIGHT:
				active[walkright] = false;
				break;
			case KeyEvent.VK_LEFT:
				active[walkleft] = false;
				break; 
			case KeyEvent.VK_W:
				active[interact] = false;
				break;
			case KeyEvent.VK_SHIFT:
				active[engage] = false;
				break;
			case KeyEvent.VK_SPACE:
				active[attack] = false;
				break;
			case KeyEvent.VK_E:
				active[menu] = false;
				break;
			case KeyEvent.VK_Q:
				active[item] = false;
				break;
			case KeyEvent.VK_A:
				active[equip1] = false;
				break;
			case KeyEvent.VK_S:
				active[equip2] = false;
				break;
			case KeyEvent.VK_D:
				active[equip3] = false;
				break;
 			}
			determineState();
		}
 
	public void keyTyped(KeyEvent ke) { 	}
	
	public int getState() { return state; }

}
