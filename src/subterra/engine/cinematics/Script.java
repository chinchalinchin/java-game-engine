package subterra.engine.cinematics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import subterra.geometry.Point;

public class Script {

	//SCRIPTS ARE ENCODED ACCORDING TO THE KEY IN THE SOURCE FOLDER. IF THE SCRIPTS FOR 
	//CUTSCENES ARE NOT WRITTEN PRECISELY, THEY WILL NOT COMPILE.
	private static final String commentString = "***";
	private static final String cdir = System.getProperty("user.dir"),
								sep = System.getProperty("file.separator"),
								scriptpath = cdir + sep + "cutscenes" + sep + "scene_";
	//Script Actions
	public static final String actor = "a", movement = "m", speech = "s", performance = "p", interaction = "c";
	//Actors Map Index Key
	public static final int VIL_INDEX = 0, NPC_INDEX = 1;
	//Movement Keys
	public static final int CURRENT_POSITION = -1;
	
	//Script Encoding Indices
	private static final int CONTROL_IND = 0, ACTOR_IND = 1,
							 MOVE_SX_IND = 2, MOVE_SY_IND = 3, MOVE_EX_IND = 4, MOVE_EY_IND = 5,
							 CHOICE_MSG_IND = 1, CHOICE_PHASE_IND = 2;
	
	private ArrayList<String> direction;
		//needs to be encoded in strings. 
	private Map<Integer, ArrayList<Integer>> actors;
	private int scene;
	private String line;
	private int lineCounter, lines;
	
	public Script(int thisScene){
		scene = thisScene;
		init();
		loadScene();
		lineCounter = 0;
		lines = direction.size();
	}
	
	private void init(){
		actors = new HashMap<>();
		ArrayList<Integer> npcs = new ArrayList<Integer>();
		actors.put(Integer.valueOf(NPC_INDEX), npcs);
		ArrayList<Integer> vils = new ArrayList<Integer>();
		actors.put(Integer.valueOf(VIL_INDEX), vils);
	}
	private void loadScene(){
		ArrayList<String> info = new ArrayList<String>();
		String filepath = scriptpath + scene + ".txt", line = null;
		try{
			BufferedReader bf = new BufferedReader(new FileReader(filepath));
			while((line = bf.readLine()) != null){
				if(!line.split("\\s")[0].equals(commentString)){ info.add(line);}
			}
			bf.close();
		}catch(IOException ie){  System.out.println("Error loading script:" + filepath); }
		
		for(String bit: info){
			String[] results = bit.split("\\s");
			String control = results[0];
			if(!control.equals(commentString) && control != null){
				if(control.equals(movement)||control.equals(speech)||
						control.equals(performance)||control.equals(interaction)){ direction.add(bit); }
				else if(control.equals(actor)){
					int type = Integer.parseInt(results[1]);
					int ident = Integer.parseInt(results[2]);
					if(type == VIL_INDEX){
						ArrayList<Integer> temp = actors.get(VIL_INDEX);
						temp.add(Integer.valueOf(ident));
						actors.put(Integer.valueOf(VIL_INDEX),temp);
					}
					else if(type == NPC_INDEX){
						ArrayList<Integer> temp = actors.get(NPC_INDEX);
						temp.add(Integer.valueOf(ident));
						actors.put(Integer.valueOf(NPC_INDEX), temp);
					}
				}
			}
		}
	
	}
	
	public boolean hasMoreLines(){
		if(lineCounter <=lines) { return true; }
		else return false;
	}
	
	//RETURNS CONTROL CODE! Statically saved in this class.
	public String getNextLine() {
		line = direction.get(lineCounter);
		String holder = line.split("\\s")[CONTROL_IND];
		lineCounter++;
		return holder;
	}
	
	//need to ensure this method is only called if the current line
	//in the script is movement. will only be called from Cutscene
	//class.
	public Point getStartingCoordinates(){
		String[] results = line.split("\\s");
		double x = Double.parseDouble(results[MOVE_SX_IND]);
		double y = Double.parseDouble(results[MOVE_SY_IND]);
		Point p = new Point(x, y);
		return p;
	}
	
	//need to ensure this method is only called if the current line
	//in the script is movement. will only be called from Cutscene
	//class.
	public Point getEndingCoordinates(){
		String[] results = line.split("\\s");
		double x = Double.parseDouble(results[MOVE_EX_IND]);
		double y = Double.parseDouble(results[MOVE_EY_IND]);
		Point p = new Point(x,y);
		return p;
	}
	
	//need to ensure this method is only called if the current line
	//in the script is movement or performance. will only be called from Cutscene
	//class.
	public int getPerformerIdentity(){
		String[] results = line.split("\\s");
		int i = Integer.parseInt(results[ACTOR_IND]);
		return i;
	}
	
	//need to ensure this method is only called if the current line
	//in the script is choice. will only be called from Cutscene
	//class.
	public int[] getPhaseChoices(){
		String[] results = line.split("\\s");
		int choices = (results.length - 1)/2;
		int[] phasechoices = new int[choices];
		for(int i = 0; i < choices; i++){ phasechoices[i] = Integer.parseInt(results[CHOICE_PHASE_IND+2*i]); }
		return phasechoices;
	}
	
	//need to ensure this method is only called if the current line
	//in the script is choice. will only be called from Cutscene
	//class.
	public int[] getMessageReferences(){
		String[] results = line.split("\\s");
		int choices = (results.length -1)/2;
		int[] messagerefs = new int[choices];
		for(int i = 0; i < choices; i++){ messagerefs[i] = Integer.parseInt(results[CHOICE_MSG_IND+2*i]); }
		return messagerefs;
	}
	
	//separate by villain and NPC.
	public Map<Integer, ArrayList<Integer>> getActors(){
		return actors;
	}
}
