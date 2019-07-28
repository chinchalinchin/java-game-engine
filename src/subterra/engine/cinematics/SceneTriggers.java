package subterra.engine.cinematics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import subterra.geometry.Point;

public class SceneTriggers {

		//continues methods that determine whether an action in game has
		//triggered a cutscene.
	
		//will contain each trigger as a boolean flag that has either
		//been flipped or not
	
		//View will modify the state of the boolean flags based on the 
		//results of the various methods contained in this class.
	
		public static final String cdir = System.getProperty("user.dir"),
									sep = System.getProperty("file.separator");
		private static final String scenepath = cdir + sep + "cutscenes" + sep,
									triggerpath = scenepath + "scene_triggers_plot_phase_";
		private static final String commentString = "***", 
									locTrig = "lt",
									msgTrig = "mt";
		
		public Map<Integer, Integer> messagetriggers;
		public Map<Integer, Point> locationtriggers;
		public boolean[] messaged;
		public boolean[] locationed;
		public int plotphase;
		
		public SceneTriggers(int phase){
			plotphase = phase;
			loadTriggers();
		}
		
		private void loadTriggers(){
			String filepath = triggerpath + plotphase + ".txt";
			ArrayList<String> info = new ArrayList<String>();
			String line;
			try{
				BufferedReader bf  = new BufferedReader(new FileReader(filepath));
				while(((line = bf.readLine()) != null)){ 
					if(!(line.split("\\s")[0].equals(commentString))){
						info.add(line); 
					}
				}
				bf.close();
			} catch (IOException ie){ System.out.println("Error at: " + filepath); }
			
			messagetriggers = new HashMap<>();
			locationtriggers = new HashMap<>();
			for(String bit : info){
				String[] results = bit.split("\\s");
				String control = results[0];
				if(control != null && !control.equals(commentString)){
					switch(control){
						case msgTrig:
							//TODO add message trigger to Map
							break;
							//TODO add location trigger to Map
						case locTrig:
							break;
					}
				}
			}
			
			messaged = new boolean[messagetriggers.size()];
			locationed = new boolean[locationtriggers.size()];
		}
		
		public boolean locationTrigger(Point p){
			return false;
		}
		
		public boolean messageTrigger(int msgLookUp){
			return false;
		}
}
