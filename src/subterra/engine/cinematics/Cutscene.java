package subterra.engine.cinematics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import subterra.engine.Controller;
import subterra.engine.GameWorld;
import subterra.engine.imagehandling.SpriteImageLoader;
import subterra.engine.imagehandling.RenderUtilities;
import subterra.geometry.Point;
import subterra.interfaces.Sprite;
import subterra.library.Effect;
import subterra.library.Hero;
import subterra.library.LightSource;
import subterra.library.ShovelTile;
import subterra.library.Switch;
import subterra.library.TreasureChest;
import subterra.library.Villain;

public class Cutscene extends JPanel implements Runnable {

		//CUTSCENE WILL FUNCTION LIKE VIEW, BUT WITH READSCRIPT INSTEAD OF PARSEINPUT
		//AND DIRECT INSTEAD OF UPDATE. 
	
		//WHEN USER ENCOUNTER MESSAGE OR CHOICE, WILL WAIT FOR INPUT. NEEDS CONTROLLER.
	
		//WHEN DIRECTED TO MOVE OR PERFORM, WILL ALTER GAME WORLD. NEEDS GAMEWORLD.
	
		//NEEDS SPRITEIMAGELOADER

		//WHEN USER ENCOUNTER CHOICES, CHOICES WILL CORRESPOND TO PLOT PHASES
		//EACH CHOICE WILL BE TAGGED TO MESSAGE IN DIALOGUE. IF USER SELECTS THAT
		//CHOICE, THEN PLOT PHASE OBJECT WILL ALTER. NEEDS TO HAVE PLOT PHASE RETURN METHOD
	private static final long serialVersionUID = 1L;
	private static final long REFRESH = 20;
	//Refresh rate measured in milliseconds
	private static final int NO_DELAYS_PER_YIELD = 5;
	private static final int MAX_FRAME_SKIPS = 2;
	
	@SuppressWarnings("unused")
	private int frames, updates;
	private GraphicsConfiguration gc;
	private BufferedImage buffer;
	@SuppressWarnings("unused")
	private Controller pad;
	private GameWorld world;
	private SpriteImageLoader loader;
	private ArrayList<Sprite> actors;
	private Script book;
	private boolean live;
	private int phase;
	
	public Cutscene(int scene, int plotphase, GameWorld thisWorld, Controller thisPad, SpriteImageLoader thisLoader){
		book = new Script(scene);
		phase = plotphase;
		world = thisWorld; pad = thisPad; loader = thisLoader;
		configure(); makeActors();
	}
	
	private void configure(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		
		if(world.getHero().getCurrentFrame() == null) { world.getHero().animate(loader); } //ensure hero is initialized.
		ArrayList<Sprite> initSprites = world.getTreasures();
		for(Sprite s : initSprites){ ((TreasureChest) s).init(loader); } //ensure treasures are initialized
		initSprites = world.getSwitches();
		for(Sprite s : initSprites) { ((Switch) s).init(loader); } //ensure switches are initialized
		initSprites = world.getShovelTiles();
		for(Sprite s : initSprites) { ((ShovelTile) s).init(loader); } //ensure shoveltiles are initialized
		initSprites = world.getLights();
		for(Sprite s : initSprites) { ((LightSource) s).randomizeFrame(); ((LightSource) s).init(loader); }
	}
	
	private void makeActors(){
		ArrayList<Sprite> actors = new ArrayList<Sprite>();
		ArrayList<Integer> villainIdentities = book.getActors().get(Script.VIL_INDEX);
			//contains villain identities
		ArrayList<Integer> npcIdentities = book.getActors().get(Script.NPC_INDEX);
			//contains npc identities
		
		ArrayList<Sprite> vils = world.getVillains();
		for(Integer i : villainIdentities){
			int iv = i.intValue();
			for(Sprite vil : vils){
				if(iv == vil.getIdentity()){
					actors.add(vil);
				}
			}
		}
		
		ArrayList<Sprite> npcs = world.getNPCs();
		for(Integer i : npcIdentities){
			int iv = i.intValue();
			for(Sprite np : npcs){
				if(iv == np.getIdentity()){
					actors.add(np);
				}
			}
		}
	}

	public void run() {
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime, excess; 
		int noDelays;
		
		//Initialize thread variables
		overSleepTime = 0L; noDelays = 0; excess = 0L;
		beforeTime = System.nanoTime()/1000000L;
			//Calculate time of Thread Start
		frames = 0;
		updates = 0;
		live = true;
		
		while(live){
			readScript();
			render();
			paintScreen();
	
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
				int skips = 0;
				while(( excess > REFRESH) && (skips < MAX_FRAME_SKIPS)){
					//If thread is too much ahead of schedule, update
					//without a render or paint. Decrement excess by
					//refresh rate. 
					excess -= REFRESH;
					readScript();
					skips++;
				}
			
			}
			beforeTime = System.nanoTime()/1000000L;
				//Recalculate time before game loop iteration
		}
		
	}
	
	//if next line is movement
		//get coordinates
	//if next line is performace
		//get actionPerformed
	//if next line is reading
		//get controller state
		//get message index
	//if next line is choice
		//get controller state
		//get phase choices
		//get message references
		//pass message references to message box?
		//NOTE: phases will be coded into script choices. 
	@SuppressWarnings("unused")
	private void readScript(){
			//IF NOT PE
		String line = book.getNextLine();
		switch(line){
			case Script.movement:
				Point p1 = book.getStartingCoordinates();
				Point p2 = book.getEndingCoordinates();
				int performerIdentity = book.getPerformerIdentity();
				Sprite thespian;
				for(Sprite a : actors){ if(a.getIdentity() == performerIdentity){ thespian = a; } }
					//check if sprite is at starting coordinates
						//if not, animate to coordinates.
						//if so, begin animation to ending coordinates
				break;
			case Script.performance:
				break;
			case Script.speech:
				break;
			case Script.interaction:
				break;	
		}
		
		if(book.hasMoreLines()){ live = false; }
	}
	
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
		int layer = world.getLayer();
		
		//START: Paint background
		RenderUtilities.paintLevel(g2, loader.getWorldFrame(world.getLayer(), false), heroPosition, screenDim);
		//END: Paint background
	
		//START: Paint Under Effects
		for(Sprite ef : effs){
			if(((Effect) ef).getOverUnder() == Effect.UNDER){
				if( ef.getLayer() == layer){
					Point p1 = ef.getMin();
					Point p2 = ef.getMax();
					if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
						RenderUtilities.paintSprite(g2, ef.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
				}
			}
		}
		//END: Paint Under Effects
		//START: Paint Treasure Chests
		for(Sprite tr : tcs){
			if(tr.getLayer() == layer){
				Point p1 = tr.getMin();
				Point p2 = tr.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					RenderUtilities.paintSprite(g2, tr.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
				}
			}
		}
		//END: Paint Treasure Chests
		
		//STATE: Paint Shovel Tiles
		for(Sprite s : st){
			if(s.getLayer() == layer){
				Point p1 = s.getMin();
				Point p2 = s.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					RenderUtilities.paintSprite(g2, s.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
				}
			}
		}
		//END: Paint Shovel Tiles
		
		//START: Paint Light Sprites (NOT SOURCES!)
			//LightSources that require animation are animated in this loop.
		for(Sprite l : lits){
			if(l.getLayer() == layer){
				Point p1 = l.getMin();
				Point p2 = l.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					LightSource ls = (LightSource) l;
					if(ls.getType() == LightSource.campfire ||
							ls.getType() == LightSource.vertical_lava ||
							ls.getType() == LightSource.horizontal_lava ||
							ls.getType() == LightSource.lava_bubble ||
							ls.getType() == LightSource.stove) { l.animate(loader); }
					RenderUtilities.paintSprite(g2, l.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
				}
			}
		}
		//END: Paint Light Sprites (NOT SOURCES!)
		
		//START: Paint Switches
		for(Sprite swit : swits){
			if(swit.getLayer() == layer){
				Point p1 = swit.getMin();
				Point p2 = swit.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					RenderUtilities.paintSprite(g2, swit.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
				}
			}
		}
		//END: Paint Switches
		
		//START: Paint NPCs
		for(Sprite npc : npcs){
			if(npc.getLayer() == layer){
				Point p1 = npc.getMin();
				Point p2 = npc.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					RenderUtilities.paintSprite(g2, npc.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
				}
			}
		}
		//END: Paint NPCs
				
		//START: Paint Villains
		for(Sprite vil : vils){
			if(vil.getLayer() == layer){
				Point p1 = vil.getMin(); Point p2 = vil.getMax();
				if(RenderUtilities.onScreen(heroPosition, p1, p2, screenDim, worldDim)){
					Villain v = (Villain) vil;
					if(!v.isStunned()){
						RenderUtilities.paintSprite(g2, vil.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
					}
					else { 
						int totalFade = v.getStun();
						double inc = 0.8/totalFade;
						for(int i = 0; i< totalFade; i++){
							float thisFade = (float)(inc*i + 0.2);
							if(v.getStunCounter()%v.getStun()==i){
								g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,thisFade));
								RenderUtilities.paintSprite(g2, vil.getCurrentFrame(), heroPosition, p1, screenDim, worldDim);
							}
						}
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
					}
				}
			}
		}
		//END: Paint Villains
		
		//START: Paint Over Effects
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
		//END: Paint Over Effects
		
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
		
		//START: Paint Over Frame
		RenderUtilities.paintLevel(g2, loader.getWorldFrame(world.getLayer(), true), heroPosition, screenDim); 
		//END: Paint Over Frame
				
		//START: Paint Light Sources
		BufferedImage lightDisplay1 = gc.createCompatibleImage(buffer.getWidth(), buffer.getHeight());
		BufferedImage lightDisplay2 = gc.createCompatibleImage(buffer.getWidth(), buffer.getHeight());
		Graphics2D g2l2 = (Graphics2D) lightDisplay2.getGraphics(); 
		Graphics2D g2l1 = (Graphics2D) lightDisplay1.getGraphics();
		Area clipArea1 = new Area(), clipArea2 = new Area();
		for(Sprite light : lits){
			LightSource ls = (LightSource) light;
			if(ls.getState() == LightSource.on
					&& ls.getLayer() == layer){
				Point screenMod = new Point(ls.getLightRadius()+screenDim.getX(), ls.getLightRadius() + screenDim.getY());
				if(RenderUtilities.onScreen(heroPosition, light.getMin(), light.getMax(), screenMod, worldDim)){
					double renderX = RenderUtilities.getRenderX(heroPosition, light.getMin(), screenDim, worldDim);
					double renderY = RenderUtilities.getRenderY(heroPosition, light.getMin(), screenDim, worldDim);
					Ellipse2D aurora1 = new Ellipse2D.Double(renderX - ls.getLightRadius()/2, renderY - ls.getLightRadius()/2, 
														ls.getLightRadius(), ls.getLightRadius());
					Ellipse2D aurora2 = new Ellipse2D.Double(renderX - ls.getInnerLightRadius()/2, renderY - ls.getInnerLightRadius()/2,
														ls.getInnerLightRadius(), ls.getInnerLightRadius());
					Area newArea = new Area(aurora1); Area otherArea = new Area(aurora2);
					clipArea1.add(newArea); clipArea2.add(otherArea);
						//accumulate clip area of onscreen lights 
				}
			}
		}
		if(h.getLanterning()){
			double renderX = RenderUtilities.getHeroRenderX(heroPosition, screenDim, worldDim);
			double renderY = RenderUtilities.getHeroRenderY(heroPosition, screenDim, worldDim);
			Ellipse2D hAur1 = new Ellipse2D.Double(renderX - h.getOuterLanternRad()/2, renderY -h.getOuterLanternRad()/2,
												   h.getOuterLanternRad(), h.getOuterLanternRad());
			Ellipse2D hAur2 = new Ellipse2D.Double(renderX - h.getInnerLanternRad()/2, renderY - h.getInnerLanternRad()/2,
												   h.getInnerLanternRad(), h.getInnerLanternRad());
			Area hArea1 = new Area(hAur1); Area hArea2 = new Area(hAur2);
			clipArea1.add(hArea1); clipArea2.add(hArea2);
		}
		g2l2.clip(clipArea2);  g2l2.drawImage(buffer, 0, 0, null); // clip & draw inner radii
		g2l1.clip(clipArea1); //clip outer radii
		g2l1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.50f));
		g2l1.drawImage(buffer, 0, 0, null); //draw outer radii
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.65f));
		g2.setColor(Color.BLACK); g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2.drawImage(lightDisplay1, 0, 0, null); // transfer outer radii
		g2.drawImage(lightDisplay2, 0, 0, null); // transfer inner radii
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
		//END: Paint Light Sources
		
		//START: Submerge Hero in Water
		//TODO:Water Submerge
		//FOR ALL EFFECTS
			//IF EFFECT IS ON SCREEN
			//GET EFFECT TYPE
			//IF EFFECT IS WATER
				//IF EFFECT INTERSECTS VILLAIN
				//SUBMERGE VILLAIN
		//END: Submerger Hero in Water
		g2.dispose();
	}

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

	public int getNextPhase(){
		return phase;
	}
}
