package subterra.engine.filehandling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Dialogue {

	public static final String cdir = System.getProperty("user.dir"),
								sep = System.getProperty("file.separator");
	
	private static final String wordpath = cdir + sep + "words" + sep,
						        dialoguepath = wordpath + "dialogue_plot_phase_";
	private static final String START_CHAR = "++", END_CHAR = "--", COMMENT_STRING = "***";
	
	private int plotphase;
	private String path;
	private ArrayList<String> dialogue;
	
	public Dialogue(int phase){
		plotphase = phase;
		path = dialoguepath + plotphase + ".txt";
		loadDialogue();
	}
	
	private void loadDialogue(){
		String line; dialogue = new ArrayList<String>();
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(path));
			while((line = bReader.readLine()) != null){ 
				String control[] = line.split("\\s");
				if(!control[0].equals(COMMENT_STRING)){  dialogue.add(line); }
			}
			bReader.close();
		}
		catch(IOException ie) { System.out.println("Error loading: " + path); }
	}
	
	public String findDialogue(int code){
		int index = -1;
		String message = null;
		for(String speech : dialogue){
			if(speech.split("\\s")[0].equals(START_CHAR)){
				if(Integer.parseInt(speech.split("\\s")[1])==code){
					index = dialogue.indexOf(speech);
					break;
				}
			}
		}
		boolean flag = true;
		int counter = 1;
		if(index>=0){
			do{
				if(message == null) { message = dialogue.get(index+counter); }
				else { message = message + dialogue.get(index+counter); }
				counter++;
				if(dialogue.get(index+counter).split("\\s")[0].equals(END_CHAR)) { flag = false; }
			}
			while(flag);
		}
		return message;
	}
	
	public int getPhase() { return plotphase; }
}
