package subterra.library;

public class Item {

	public final static int key = 0, potion = 1, hipotion = 2, 
							antidote = 3, barrier = 4, bomb = 5, 
							bolt = 6;
	public final static int items = 7; 
	
	private final static String[] itemText = {"Key", "Potion", "Hi-Potion", 
											   "Antidote", "Barrier", "Bomb",
												"Bolt"};
	
	public static String getText(int type){ return itemText[type];}
	
}
