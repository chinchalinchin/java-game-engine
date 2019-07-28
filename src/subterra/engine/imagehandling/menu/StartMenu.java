package subterra.engine.imagehandling.menu;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import subterra.geometry.Point;

public class StartMenu extends JPanel
					   implements KeyListener{

	/**
	 * 
	 * @description
	 * 
	 * The StartMenu is made before the View or Controller. Therefore, StartMenu 
	 * implements the KeyListener interface to deal with user input before the 
	 * Controller is rigged to the Cradle.
	 * 
	 * StartMenu holds the selection as an integer. When the Cradle calls the selection
	 * for use in loading a new or saved game, the integer is translated into the file 
	 * path of the user's choice. 
	 * 
	 * The StartMenu is constructed and configured via the Cradle. The Cradle yields and 
	 * paints the menu while there is no selection made by the user. The StartMenu listens
	 * for user input until the selected 
	 */
	private static final long serialVersionUID = 1L;
	private static final String menupath = System.getProperty("user.dir") + System.getProperty("file.separator") + "imgs" +  
										   System.getProperty("file.separator") + System.getProperty("file.separator") + "menu" + 
										   System.getProperty("file.separator") + "menu_back.png";
	
	public static final int nothing = 0, newG = 1,  loadG =2,
							file1 = 3, file2 = 4, file3 = 5;
	
	private MainButton newGame,loadGame,
					   fileOne, fileTwo, fileThree;
	private GraphicsConfiguration gc;
	private BufferedImage background;
	private BufferedImage buffer;
	private int currentSelection, subSelection;
	private boolean selected, subbing;
	
	public StartMenu(){
		super();
		configure(); loadFrame();
		selected = false; subbing = false;
		currentSelection = nothing;
		subSelection = nothing;
	}
	
	private void configure(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
	}
	
	private void loadFrame(){  
		BufferedImage bf = null;
		try { 
			bf = ImageIO.read(new File(menupath)); 
			BufferedImage copy = gc.createCompatibleImage(bf.getWidth(), bf.getHeight());
			Graphics2D g2 = (Graphics2D) copy.getGraphics();
			g2.drawImage(bf, 0 , 0, null);
			g2.dispose();
			if(copy !=null) { background = copy; } 
		}
		catch(IOException ie) { System.out.println("Error loading: " + menupath);}
	}
	
	private void makeSubMenu(){
		double spaceX = (this.getWidth() - 3 * MainButton.defaultWidth)/4;
		double spaceY = 5*(this.getHeight() - MainButton.defaultHeight)/6;
		fileOne = new MainButton(new Point(spaceX, spaceY), "File 1");
		fileTwo = new MainButton(new Point(2*spaceX + MainButton.defaultWidth, spaceY), "File 2");
		fileThree = new MainButton(new Point(3*spaceX + 2*MainButton.defaultWidth, spaceY), "File 3");
	}
	
	public void makeMenu(){ 
		double spaceX = (this.getWidth() - 2*MainButton.defaultWidth)/3;
		double spaceY = 2*(this.getHeight() - MainButton.defaultHeight)/3;
		newGame = new MainButton(new Point(spaceX , spaceY), "New");
		loadGame = new MainButton(new Point(2*spaceX + MainButton.defaultWidth, spaceY), "Load");
	}
	
	public void paintMenu(){ 
		if( buffer == null) { buffer = gc.createCompatibleImage(this.getWidth(), this.getHeight()); }
		Graphics2D g2d = (Graphics2D) buffer.getGraphics();
		g2d.setColor(Color.lightGray);
		g2d.fillRect(0,0, this.getWidth(), this.getHeight());
		g2d.drawImage(background, 0, 0 , this.getWidth(), this.getHeight(), 0, 0, background.getWidth(), background.getHeight(), null);
		g2d.drawImage(newGame.getCurrentFrame(), (int) newGame.getMinX(), (int) newGame.getMinY(), null);
		g2d.drawImage(loadGame.getCurrentFrame(), (int) loadGame.getMinX(), (int) loadGame.getMinY(), null);
		if(subbing){
			if(fileOne.getCurrentFrame() != null){
				g2d.drawImage(fileOne.getCurrentFrame(), (int) fileOne.getMinX(), (int) fileOne.getMinY(), null);
			}
			if(fileTwo.getCurrentFrame() != null){
				g2d.drawImage(fileTwo.getCurrentFrame(), (int) fileTwo.getMinX(), (int) fileTwo.getMinY(), null);
			}	
			if(fileThree.getCurrentFrame() != null){
				g2d.drawImage(fileThree.getCurrentFrame(), (int) fileThree.getMinX(), (int) fileThree.getMinY(), null);
			}
		}
		paintScreen();
		g2d.dispose();
	}
	
	public void paintScreen(){
		Graphics g;
		try{
			g = this.getGraphics();
			if(g!=null && buffer != null) { g.drawImage(buffer, 0, 0, null); }
			Toolkit.getDefaultToolkit().sync();
			g.dispose();
		}
		catch(Exception e){	}
	}
	
	public boolean isSelected() { return selected; }
	
	public int getSelection() { return currentSelection; }
	
	public int getFileSelection() { return subSelection; }
	
	public void keyPressed(KeyEvent ke) { 
		int ch = ke.getKeyCode();
		switch(ch){
			case KeyEvent.VK_RIGHT:
				if(!subbing){
					switch(currentSelection){
						case nothing:
							currentSelection = newG;
							newGame.setState(MainButton.active);
							loadGame.setState(MainButton.inactive);
							break;
						case newG:
							currentSelection = loadG;
							loadGame.setState(MainButton.active);
							newGame.setState(MainButton.inactive);
							break;
						case loadG:
							currentSelection = nothing;
							loadGame.setState(MainButton.inactive); 
							newGame.setState(MainButton.inactive); 
							break;
					}
				}
				else {
					switch(subSelection){
						case nothing:
							subSelection = file1;
							fileOne.setState(MainButton.active);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.inactive);
							break;
						case file1:
							subSelection = file2;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.active);
							fileThree.setState(MainButton.inactive);
							break;
						case file2:
							subSelection = file3;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.active);
							break;
						case file3:
							subSelection = nothing;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.inactive);
							break;
					}
				}
				break;
			case KeyEvent.VK_LEFT:
				if(!subbing){
					switch(currentSelection){
						case nothing:
							currentSelection = loadG;
							loadGame.setState(MainButton.active);
							newGame.setState(MainButton.inactive);
							break;
						case newG:
							currentSelection = nothing;
							newGame.setState(MainButton.inactive); 
							loadGame.setState(MainButton.inactive); 
							break;
						case loadG:
							currentSelection = newG;
							newGame.setState(MainButton.active);
							loadGame.setState(MainButton.inactive); 
							break;
					}
				}
				else{
					switch(subSelection){
						case nothing:
							subSelection = file3;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.active);
							break;
						case file1:
							subSelection = nothing;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.inactive);
							break;
						case file2:
							subSelection = file1;
							fileOne.setState(MainButton.active);
							fileTwo.setState(MainButton.inactive);
							fileThree.setState(MainButton.inactive);
							break;
						case file3:
							subSelection = file2;
							fileOne.setState(MainButton.inactive);
							fileTwo.setState(MainButton.active);
							fileThree.setState(MainButton.inactive);
							break;	
					}
				}
				break;
				
			case KeyEvent.VK_SPACE:
				if(currentSelection == newG){ 
					if(!subbing){
						makeSubMenu();
						subbing = true;
					}
					else{ if(subSelection != nothing ) { selected = true; } }
				}
				if(currentSelection == loadG) { 
					if(!subbing){
						makeSubMenu();
						subbing = true;
					}
					else{ if(subSelection != nothing ) { selected = true; } }
				}
				break;
				
			case KeyEvent.VK_SHIFT:
				if(subbing) { subbing = false; }
				fileOne = null;
				fileTwo = null;
				fileThree = null;
				subSelection = nothing;
				break;
		}
	}
	
	public void keyReleased(KeyEvent ke) {	}

	public void keyTyped(KeyEvent arg0) { }
	
}
