package subterra.engine.imagehandling;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D; 
import java.awt.GraphicsConfiguration;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import subterra.geometry.Point;
import subterra.interfaces.Sprite;
import subterra.library.Hero;
import subterra.library.LightSource;

/**
 *  
 * @author chinchalinchin
 *
 * @description contains static methods used to render sprites on screen.
 */
public class RenderUtilities {
	
	/**
	 * @Method onScreen
	 * 
	 * @param HeroMin :The top left corner of the hero's hitbox.
	 * @param spriteMin :The top left corner of the Sprite's hitbox.
	 * @param spriteMax :The bottom right corner of the Sprite's hitbox
	 * @param screenDim :The dimensions of the View screen.
	 * 
	 * @description
	 * Determines if a given sprite is on screen. The current coordinates 
	 * of the screen in the GameWorld are calculated based on the hero's 
	 * position and the screen's dimensions. Since the screen moves over the 
	 * GameWorld, the hero is always centered, unless the hero approaches the 
	 * edges of the GameWorld. In which case, the screen stays in the corner of 
	 * the GameWorld. Then, the Sprite is checked against the fixed coordinates 
	 * of the corner to determine if it is on screen. 
	 * 
	 * @return Method returns true if Sprite is on screen and false if not. 
	 * 
	 * @note
	 * All coordinates are given in java space, where the top right is defined as (0,0).
	 * Down is defined as the positive direction in the vertical dimension. 
	 */
	public static boolean onScreen(Point heroMin, Point spriteMin, Point spriteMax, Point screenDim, Point worldDim){
		double startX, startY, endX, endY;
		double screenW = screenDim.getX();
		double screenH = screenDim.getY();
		double levelW = worldDim.getX();
		double levelH = worldDim.getY();
		if((heroMin.getX() > screenW/2 || heroMin.getX() == screenW/2)
				&& (heroMin.getX() < levelW - screenW/2)){
			startX = heroMin.getX() - screenW/2;
			endX = heroMin.getX() + screenW/2;
		} 
		else if(heroMin.getX() > levelW - screenW/2 || heroMin.getX() == levelW - screenW/2){
			startX = levelW - screenW;
			endX = levelW;
		}
		else {
			startX = 0; endX = screenW;
		}
		if((heroMin.getY() > screenH/2 || heroMin.getY() == screenH/2)
				&& (heroMin.getY() < levelH - screenH/2)){
			startY = heroMin.getY() - screenH/2;
			endY = heroMin.getY() + screenH/2;
		}
		else if (heroMin.getY() > levelH - screenH/2 || heroMin.getY() == levelH - screenH/2){
			startY = levelH - screenH;
			endY = levelH;
		}
		else {
			startY = 0; endY = screenH;
		}
		boolean topcheck, bottomcheck, rightcheck, leftcheck;
		leftcheck = spriteMax.getX() > startX || spriteMax.getX() == startX;
		rightcheck = spriteMin.getX() < endX;
		topcheck = spriteMax.getY() > startY || spriteMax.getY() == startY;
		bottomcheck = spriteMin.getY() < endY;
		
		boolean totalcheck = topcheck && bottomcheck && rightcheck && leftcheck;
		return totalcheck;
	}

	
	public static int getRenderX(Point heroMin, Point spriteMin, Point screenDim, Point worldDim){
		int x = (int) spriteMin.getX();
		int hx = (int) heroMin.getX();
		int lw = (int) worldDim.getX();
		int w = (int) screenDim.getX();
			//initialize java space coordinate
		int drawX;
			//if hero is past w/2, half the width of screen,
			//in game space, screen will be centered 
			//on hero in game space, unless the hero
			//approaches the level boundary, lw. Likewise,
			//screen will center vertically on hero if hero 
			//is past h/2, half the height of the screen, unless
			//the hero approaches the level boundary, lh.
				//transform horizontal coordinates from game space
				//into java space.
		if( (hx > w/2 || hx == w/2) && (hx < lw - w/2)) { drawX = w/2 + x - hx; }
		else if (hx > lw - w/2 || hx == lw - w/2) { drawX = x - lw + w; }
		else { drawX = x; }
		return drawX;
	}
	
	public static int getRenderY(Point heroMin, Point spriteMin, Point screenDim, Point worldDim){
		int y = (int) spriteMin.getY();
		int hy = (int) heroMin.getY();
		int lh = (int) worldDim.getY();
		int h = (int) screenDim.getY();
			//initialize java space coordinate
		int drawY;
			//if hero is past w/2, half the width of screen,
			//in game space, screen will be centered 
			//on hero in game space, unless the hero
			//approaches the level boundary, lw. Likewise,
			//screen will center vertically on hero if hero 
			//is past h/2, half the height of the screen, unless
			//the hero approaches the level boundary, lh.
				//transform vertical coordinates from game space
				//into jave space.
		if( (hy > h/2 || hy == h/2) && (hy < lh  - h/2)) { drawY = h/2 + y - hy; }
		else if (hy > lh - h/2 || hy == lh - h/2) { drawY = y - lh + h; }
		else { drawY = y; }
		return drawY;
	}

	public static int getHeroRenderX(Point heroMin, Point screenDim, Point worldDim){
		int drawX;
		int hx = (int) heroMin.getX();
		int w = (int) screenDim.getX();
		int lw = (int) worldDim.getX();
		if( (hx > w/2 || hx == w/2) && (hx < lw - w/2)) { drawX = w/2; }
		else if (hx > lw - w/2 || hx == lw - w/2) { drawX = w - lw + hx; }
		else { drawX = hx; } 
		return drawX;
	}
	
	public static int getHeroRenderY(Point heroMin, Point screenDim, Point worldDim){
		int drawY;
		int hy = (int) heroMin.getY();
		int h = (int) screenDim.getY();
		int lh = (int) worldDim.getY(); 
		if( (hy > h/2 || hy == h/2) && (hy < lh - h/2)) { drawY = h/2; }
		else if ( hy > lh - h/2 || hy == lh - h/2) { drawY = h - lh + hy; }
		else { drawY = hy; }
		return drawY;
	}

	public static void paintStunnedSprite(Graphics2D g2, Sprite v, BufferedImage frame, Point heroMin, Point screenDim, Point worldDim, int totalStun, int currentStun){
		double inc = 0.8/totalStun;
		for(int i = 0; i< totalStun; i++){
			float thisFade = (float)(inc*i + 0.2);
			if(currentStun%totalStun==i){
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,thisFade));
				RenderUtilities.paintSprite(g2, v.getCurrentFrame(), heroMin, v.getMin(), screenDim, worldDim);
			}
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
	}
	
	/**
	 * @Method paintSprite
	 * 
	 * @param g2 :Graphics2D object upon which the method paints
	 * @param frame :BufferedImage of the current frame of the Sprite to be painted.
	 * @param heroMin :The hero's top left corner
	 * @param spriteMin :The painted sprite's top left corner
	 * @param screenDim :The dimensions of the screen
	 * @param worldDim :The dimensions of the GameWorld.
	 * 
	 * @description: Paints a given Sprite onto a graphics object. The position of the Sprite
	 * on the Graphics Object is based on calculations involving the current position of the
	 * hero. If the hero is approaching an edge, the screen is no longer centered and his 
	 * 
	 * @note
	 * All coordinates are given in java space, where the top right is defined as (0,0).
	 * Down is defined as the positive direction in the vertical dimension. 
	 */
	public static void paintSprite(Graphics2D g2, BufferedImage frame, Point heroMin, 
							Point spriteMin, Point screenDim, Point worldDim) {
		//java space: where the image is rendered. screen size.
		//game space: where the level background and sprites live. image size, usually
		//bigger than screen size, but not necessarily so.
		//Graphic2D drawImage: s(x,y) - game (image) space
		//			 		   d(x,y) - java space
				//Retrieve image if not already loaded.
				//get game space coordinates 
			int x = (int) spriteMin.getX();
			int y = (int) spriteMin.getY();
			int hx = (int) heroMin.getX();
			int hy = (int) heroMin.getY();
			int lw = (int) worldDim.getX();
			int lh = (int) worldDim.getY();
			int w = (int) screenDim.getX();
			int h = (int) screenDim.getY();
				//initialize java space coordinate
			int drawX, drawY;
				//if hero is past w/2, half the width of screen,
				//in game space, screen will be centered 
				//on hero in game space, unless the hero
				//approaches the level boundary, lw. Likewise,
				//screen will center vertically on hero if hero 
				//is past h/2, half the height of the screen, unless
				//the hero approaches the level boundary, lh.
					//transform horizontal coordinates from game space
					//into java space.
			if( (hx > w/2 || hx == w/2) && (hx < lw - w/2)) { drawX = w/2 + x - hx; }
			else if (hx > lw - w/2 || hx == lw - w/2) { drawX = x - lw + w; }
			else { drawX = x; }
					//transform vertical coordinates from game space
					//into jave space.
			if( (hy > h/2 || hy == h/2) && (hy < lh  - h/2)) { drawY = h/2 + y - hy; }
			else if (hy > lh - h/2 || hy == lh - h/2) { drawY = y - lh + h; }
			else { drawY = y; }
			
			if( ((drawX > 0 || drawX == 0) && drawX < w)  &&
			    ((drawY > 0 || drawY == 0) && drawY < h) )
			{ g2.drawImage(frame, drawX, drawY, null); }
			else{
				//image is only partially displayed
				double boxW = frame.getWidth();
				double boxH = frame.getHeight();
					//java space
				double dx1; double dy1;
				double dx2; double dy2;
					//game space
				double sx1; double sy1;
				double sx2; double sy2;
				boolean case1 = (drawX < 0 && drawX > -boxW) && (drawY > 0 || drawY == 0);
				boolean case2 = (drawX > 0 || drawX == 0) && (drawY < 0 && drawY > -boxH); 
				boolean case3 = (drawX < 0 && drawX > -boxW) && (drawY < 0 && drawY > -boxH);
				if(case1){
						//game space
					sx1 = -drawX; sy1 = boxH;
					sx2 = boxW; sy2 = 0;
						//java space
					dx1 = 0; dy1 = drawY + boxH;;
					dx2 = boxW + drawX; dy2 = drawY;
					g2.drawImage(frame, 
									//java space
							 	(int) dx1, (int) dy1, (int) dx2, (int) dy2, 
							 		//game space
							 	(int) sx1, (int) sy1, (int) sx2, (int) sy2, 
							 	null);
				}
				if(case2){
						//game space
					sx1 = 0; sy1 = -drawY;
					sx2 = boxW; sy2 = boxH;
						//java space
					dx1 = drawX; dy1 = 0;
					dx2 = boxW + drawX; dy2 = boxH + drawY;
					g2.drawImage(frame, 
									//java space
							 	(int) dx1, (int) dy1, (int) dx2, (int) dy2, 
							 		//game space
							 	(int) sx1, (int) sy1, (int) sx2, (int) sy2, 
							 	null);
				}
				if(case3){
						//game space
					sx1 = -drawX; sy1 = -drawY;
					sx2 = boxW; sy2 = boxH;
						//java space
					dx1 = 0; dy1 = 0;
					dx2 = boxW + drawX; dy2 = drawY + boxH;
					g2.drawImage(frame, 
									//java space
							 	(int) dx1, (int) dy1, (int) dx2, (int) dy2, 
							 		//game space
							 	(int) sx1, (int) sy1, (int) sx2, (int) sy2, 
							 	null);
				}
			}
		}

	public static void paintHero(Graphics2D g2, BufferedImage frame, Point heroMin, Point screenDim, Point worldDim) { 
		int drawX, drawY;
		int hx = (int) heroMin.getX();
		int hy = (int) heroMin.getY();
		int w = (int) screenDim.getX();
		int h = (int) screenDim.getY();
		int lw = (int) worldDim.getX();
		int lh = (int) worldDim.getY();
		if( (hx > w/2 || hx == w/2) && (hx < lw - w/2)) { drawX = w/2; }
		else if (hx > lw - w/2 || hx == lw - w/2) { drawX = w - lw + hx; }
		else { drawX = hx; } 
		if( (hy > h/2 || hy == h/2) && (hy < lh - h/2)) { drawY = h/2; }
		else if ( hy > lh - h/2 || hy == lh - h/2) { drawY = h - lh + hy; }
		else { drawY = hy; }
		g2.drawImage(frame, drawX, drawY, null);
	}
	
	/**
	 * @Method paintLevel
	 * 
	 * @param g2 : Graphics2D context for the buffer image that collates a single frame of 
	 * animation. 
	 * 
	 * @param level : BufferedImage. Image containing the entire level.
	 * 
	 * @param hero : Point. Position of hero's top left corner.
	 * 
	 * @param screenDim : Point. Dimensions of the panel the View renders animation on.
	 */
	public static void paintLevel(Graphics2D g2, BufferedImage level, Point hero, Point screenDim){
			//&& background != null
			double sx1, sy1;
			double sx2, sy2;
			double hx = hero.getX();
			double hy = hero.getY();
			double w = screenDim.getX();
			double h = screenDim.getY();
			double bw = level.getWidth();
			double bh = level.getHeight();
			
			if( (hx > w/2 || hx == w/2) &&
				(hx < bw - w/2)	){
				sx1 = hx - w/2; sx2 = hx + w/2;
			} 
			else if ( hx > bw - w/2 || hx == bw - w/2){
				sx1 = bw - w;sx2 = bw;
			}
			else{ sx1 = 0; sx2 = w; }
			if( (hy > h/2 || hy == h/2) &&
					(hy < bh - h/2)){
				sy1 = hy - h/2; sy2 = hy + h/2;
			} 
			else if (hy > bh - h/2 || hy == bh - h/2){
				sy1 = bh - h; sy2 = bh;
			} 
			else { sy1 = 0; sy2 = h;}
			
			g2.drawImage(level, 
							//java space
						 (int) 0, (int) 0, (int) w, (int) h, 
						 	//image space
						 (int) sx1, (int) sy1, (int) sx2, (int) sy2, 
						 null);
	}

	public static BufferedImage mirrorImage(BufferedImage img){
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage mirror = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < h; y++){
			for(int lx = 0, rx = w -1;lx<w;lx++, rx--){
				int p = img.getRGB(lx, y);
				mirror.setRGB(rx, y, p);
			}
		}
		return mirror;
	}
	
	/**@Method paintMirroredHero
	 * 
	 * @note this method is essential jury-rigged to make animation appear better in-game. 
	 * jave renders images from the top left. this led to problems when rendering animations
	 * that involved the hero facing the leftward direction. to circumvent this problem,
	 * this method was created. the buffer from the View is mirrored and then passed into this method,
	 * where an unaltered Hero sprite is painted onto the mirror at the calculated render point.
	 * The image will then be unmirrored back in the View class to give the appearance of leftward 
	 * facing animation.
	 * 
	 * @param g2 : Graphics2D context of the mirrored image to be rendered upon. Represents the 
	 * panel of the View after gamespace has been transformed into imagespace.
	 * 
	 * @param frame : BufferedImage of the Hero. Will paint unaltered image of hero on g2 
	 * attacking/shovelling to the right and then mirror it back to create appearance of 
	 * attacking/shovelling to the left.
	 *  
	 * @param heroMin : position of Hero in game space.
	 * @param screenDim : dimensions of the panel View is rendering upon.
	 * @param worldDim : dimensions of game space.
	 * @param heroWidth : width of Hero Sprite. 
	 */
	public static void paintMirroredHero(Graphics2D g2, BufferedImage frame, Point heroMin, Point screenDim, Point worldDim, double heroWidth){
		double hx = heroMin.getX();
		double hy = heroMin.getY();
		double w = screenDim.getX();
		double h = screenDim.getY();
		double lw = worldDim.getX();
		double lh = worldDim.getY();
		double drawX, drawY;
		int renderX, renderY;
		
		if( (hx > w/2 || hx == w/2) && (hx < lw - w/2)) { drawX = w/2; }
		else if (hx > lw - w/2 || hx == lw - w/2) { drawX = w - lw + hx; }
		else { drawX = hx; }
		if( (hy > h/2 || hy == h/2) && (hy < lh - h/2)) { drawY = h/2; }
		else if ( hy > lh - h/2 || hy == lh - h/2) { drawY = h - lh + hy; }
		else { drawY = hy; }
		
		renderX = (int)(w - drawX - heroWidth);
		renderY = (int) drawY;
		g2.drawImage(frame, renderX, renderY, null);
	}

	public static void clipLightSources(Graphics2D g2, GraphicsConfiguration gc, BufferedImage frame, ArrayList<Sprite> lights, Hero h, int layer, Point screenDim, Point worldDim){
		BufferedImage lightDisplay1 = gc.createCompatibleImage(frame.getWidth(), frame.getHeight());
		BufferedImage lightDisplay2 = gc.createCompatibleImage(frame.getWidth(), frame.getHeight());
		Graphics2D g2l2 = (Graphics2D) lightDisplay2.getGraphics(); 
		Graphics2D g2l1 = (Graphics2D) lightDisplay1.getGraphics();
		Area clipArea1 = new Area(), clipArea2 = new Area();
		Point heroPosition = h.getMin();
		for(Sprite light : lights){
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
		g2l2.clip(clipArea2);  g2l2.drawImage(frame, 0, 0, null); // clip & draw inner radii
		g2l1.clip(clipArea1); //clip outer radii
		g2l1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.50f));
		g2l1.drawImage(frame, 0, 0, null); //draw outer radii
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.65f));
		g2.setColor(Color.BLACK); g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		g2.drawImage(lightDisplay1, 0, 0, null); // transfer outer radii
		g2.drawImage(lightDisplay2, 0, 0, null); // transfer inner radii
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
		g2l1.dispose(); g2l2.dispose();
	}

}
