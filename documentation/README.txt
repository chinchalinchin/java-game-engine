DESCRIPTION
APPENDIX.0: Image Configuration
APPENDIX.1A: World Generation Syntax
	.1B: Sprite Type Generation Syntax
	.1C: Sprite State Generation Syntax
APPENDIX.2A: Villain Game Configuration
	.2B: Villain Type Configuration 
	.2C: Villain State Configuration
APPENDIX.3A: NPC Game Configuration
	.3B: NPC Type Configuration
	.3C: NPC State Configuration
APPENDIX.4A: Hero Game Configuration
	.4B: Hero Type Configuration
	.4C: Hero State Configuration
APPENDIX.5: Sheet Sprite Configuration
APPENDIX.6: Binary, Singleton, LightSource and Effect Sprite World Configuration
APPENDIX.7: Sheet Sprite World Configuration
	.7A: Villain World Configuration
	.7B: NPC World Configuration
	.7C: Hero World Configuration
APPENDIX 8: Collision and Attack Box Configuration
APPENDIX.9: Object/Sprite Hierarchies
APPENDIX.10: Engine Class Diagram
APPENDIX.11: SpriteImageLoader indexation methods
APPENDIX.12: World Map

__________________________________________________________
__________________________________________________________
__________________________________________________________
DESCRIPTION
__________________________________________________________
	A: Overview
	B: World Frames
	C: In-Game Objects
__________________________________________________________
	A: Overview
__________________________________________________________
Subterra is a 2D top-down game engine that manipulates in-game objects in the GameWorld. An object in the GameWorld is a called a Sprite. The sprites defined within the GameWorld are as follows:

	***Sheet Sprites
	Hero
	Villain
	NPC
	***Configuration Sprites
	Door
	Portal
	Hitbox
	***Binary Sprites
	Switch
	ShovelTile
	TreasuseChest
	PressurePlate
	Gate
	Hackbox
	***Singleton Sprites
	Crate
	Barrel
	Book
	Sign
	SaveCrystal
	***Other Sprites
	Effect
	LightSource
 
The GameWorld can be populated with sprites by altering the world generation files found in

	/engine/world_config/world/*
	/engine/saves/*

The prior holds the master generation files. The latter holds saved user generation files that are accessed by loading a game. The file 'new_world.txt' determines the configuration of in-game objects when the user selects a new game. The files 'world_1.txt', 'world_2.txt' and 'world_3.txt' are saved files that contain the configuration of in-game objects as of the last time the user saved. APPENDIX.1 of this README contains a syntax guide for editing the new world and configuration world generation files. The master configuration files are never altered by the saving procedure and are referenced as the player traverses the world frames. 
 
The engine is further calibrated by a series of properties hard-coded into it and by several configuration files. The configuration files of in-game objects are found in

	/engine/world_config/villain_config.txt
	/engine/world_config/npc_config.txt
	/engine/world_config/binary_config.txt
	/enginge/world_config/singleton_config.txt	
	/engine/world_config/hero_immutable_config.txt
	/engine/world_config/hero_variable_config.txt
	/engine/world_config/light_source_config.txt
	/engine/world_config/effect_config.txt

These files determine the in-game properties of objects. These properties control how features like collision detection, shading and drawing-order are implemented by the rendering engine.

The image files for all in-game objects can be found in

	/engine/imgs/

Image configuration files contain the filepaths to images relative to the engine image directory. Effect, LightSource, Singleton Sprites(Crates, Barrels, Signs, Books, SaveCrystals), Binary Sprites (Switches, TreasureChests, ShovelTiles, PressurePlates, Gates) and Sheet Spritesimages are configured by text files found in
	
	/engine/sprite_img_config/singleton_object_img_config.txt	
	/engine/sprite_img_config/binary_object_imgconfig.txt
	/engine/sprite_img_config/effect_img_config.txt
	/engine/sprite_img_config/light_source_img_config.txt
	/engine/sprite_img_config/sprite_sheet_img_config.txt
	/engine/sprite_img_config/sprite_sheet_properties.txt

Image loading for singleton and binary sprites is straight forward. Sheet Sprites are slightly more complex; Animations are created from sheets by combining apparel, equipment and weapon sheets with a skin sheet and then specifying a row from the sheet. Appendix.0 contains more information about how this is done, but for the purposes of this introduction, it is enough to note, the sprite animation creation configuration files that define sprite types and states for NPCs, Villains and the Hero class can be found in
	
	/engine/sprite_state_config/hero_sprite_state_config.txt
	/engine/sprite_state_config/npc_sprite_state_config.txt
	/engine/sprite_state_config/villain_sprite_state_config.txt
	/engine/sprite_type_config/hero_sprite_type_config.txt
	/engine/sprite_type_config/npc_sprite_type_config.txt
	/engine/sprite_type_config/villain_sprite_type_config.txt
__________________________________________________________
	B: World Frames
__________________________________________________________

The GameWorld is a 2 dimensional plane arranged in a grid made up of world frame sets whose size is determined by the size of the images found in 

	/engine/imgs/world/ 

All world frame sets in this folder must be the same size, or else the game engine will exhibit strange behavior. There are five hard coded game world layers: Layer 1, Layer 2, Layer 3, Layer 4 & Layer 5. In order for a frame set to be used in game, the frame set needs at least one layer; Layers 2 - 5 are optional. If they are not used, they should not be referenced by game objects like doors or portals. The number of layers in a given world frame set is specified in the world configuration file for that particular frame set. It goes without saying, the number of layers specified in the world configuration file should always be equal to the number of layer image files in the engine/img/world/ folder  These frames are accompanied by over-frames. These are images that are painted on top of everything, to give the appearance of depth. In other words, each world frame set has ten frames, arranged in the engine in this order:

	layer1, frame file name: *frame set*_W1.png
	        overframe file name: *frame set*_W1_over.png
	layer2, frame file name: *frame set*_W2.png
		overframe file name: *frame set*_W2_over.png
	layer3, frame file name: *frame set*_W3.png
		overframe file name: *frame set*_W3_over.png
	layer4, frame file name: *frame set*_W4.png
		overframe file name: *frame set*_W4_over.png
	layer5, frame file name: *frame set*_W5.png
		overframe file name: *frame set*_W5_over.png

Each frame set is interconnected by in-game Doors. The player can traverse the layers in a world frame set by entering doors. Each frame set is interconnected by portals. The player can traverse world frame sets by portals. Portals trigger the loading of a new world frame set, while Doors do not. World frame sets are held in their entirety during gameplay; the frame set is switched out whenever a Portal is entered. This is the major difference between Portals and Doors. Portals connect frame sets and thus signal a loading event; Doors connect frames within a set and thus do not signal a loading event. 

APPENDIX.11 contains a map that shows the configurations of in-game world frame sets, arranged by portal connections. There is no inherent reason Portals should be set up in a grid; this is mainly done for clarity's sake in the design process. The position of Portals in a given world frame set can be altered through the world generation file for each frame set given in the folder,

	/engine/world_config/world/*

World frame sets are populated by sprites through the save or configuration files, depending on if the user is loading a game file or portaling through world frame sets. 

Frame sets comprise the canvas of the GameWorld. The GameWorld is further populated by varieties of Sprites. For example, the boundaries inside a world frame set are demarcated by Hitboxes. A Hitbox is a certain flavor of sprite. See next section for more info.

__________________________________________________________
	C: In-Game Objects
__________________________________________________________

There are several main hiearchies of sprites in-game. There are Singletons Sprites, Binary Sprites and Sheet Sprites.

SINGLETON Sprites:

Sprite Members: Crate, Barrel, Book, Sign, SaveCrystal

Singleton Sprites have a single state. This state is never altered in the course of game play. These sprites serve a singular purpose in gameplay.

BINARY SPRITES:

Sprite Members: Switch, Gate, PressurePlate, TreasureChest, ShovelTile, Hackbox

Binary Sprites have two distinct states that are entered into and out of throughout the course of game play. As a result, Binary Sprites hold two images. Strictly speaking, the SpriteImageLoader holds two images, since there can be a duplication of Sprites on each world frame set and it would be wasteful for each duplicate to contain its own separate image. See the SpriteImageLoader framework and indexation methods in Appendix 11 for more detail.

SHEET SPRITES:

Sprite Members: NPC, Villain, Hero

Sheet sprites are broken up into: skins, apparel, weapon and equipment sheets. By combining these sheets in various ways, animations can be created for use in game. The NPC, Villain and Hero sprites all take advantage of this framework for generating their animation. For this reason, these types of sprites are called Sheet Sprites. 

Sheet Sprites are defined by the following hierarchy,

	Type -> State -> Frame

Types are generated by skins and apparel sheets. States are generated by superimposing weapon or equipment sheets on top of the specified type. Frame refers to which frame the Sprite is currently using in its animation. 

__________________________________________________________
__________________________________________________________
__________________________________________________________
APPENDIX.1: WORLD GENERATION SYNTAX
__________________________________________________________
__________________________________________________________
__________________________________________________________
__________________________________________________________
APPENDIX.2A: VILLAIN CONFIGURATION
__________________________________________________________

The Villains are divided into different types. Each type has an index associated with it. This index is hard-coded into the Villain class to tie it to the configuration files explained in the INTRODUCTION. There are 15 types. The Villain types are 

		index         type
                __________________
		00:          golem
		01:	
		02:
		03:
		04:
		05:
		06:
		07:
		08:
		09:
		10:
		11:
		12:
		13:
		14:
		15:

Each type is modified by its attributes. These attributes determine how a type behaves in-game. A type's behavior can be modifed through the configuration files explained in the INTRODUCTION. Villain type attributes are:

	type width height attack_radius aware_radius health perimeter walk_speed run_speed attack_bounce stun_counter synch_delay 			collision_width_modifier collision_height_modifier atk def atk_trigger atk_length atk_mod

The first row is an identifier. The subsequent rows affect the following attributes of Villains,

	1. width:
	2. height:
	3. attack_radius:
	4. aware_radius:
	5. health:
	6. perimeter: 
	7. walk_speed:
	8. run_speed:
	9. attack_bounce:
	10. stun_counter:
	11. synch_delay:
	12. collision_width_modifier: 
	13. collision_height_modifier
	14. atk:
	15. def:
	16. atk_trigger:
	17. atk_length:
	18. atk_mod:

The Villains have states. These states correspond to different animations in-game. There are 9 states. Each animation in-game has 20 frames. Therefore, each type has 180 frames. 


__________________________________________________________
__________________________________________________________
__________________________________________________________
APPENDIX.6: WORLD MAP
__________________________________________________________




__________________________________________________________
__________________________________________________________
__________________________________________________________
APPENDIX.7: Object Hiearchies
__________________________________________________________
	Note: CHANGES FREQUENTLY, IN DESIGN!

The in-game Object hierarchy, with the exception of Doors and Portals, is given by the following diagram,

	Type -> State -> Frame

		Note: NPC and LightSources have special subtypes of non-movers that only have 
			one frame associated with them. This discrepancy is handled by hard-coding 
			in the file handling processes. These types are marked by a star and 
			account for any accounting errors when calculating the Total Images Held.
		Note: Hitboxes are technically an in-game object, but they are invisible in-game. 
			In other words, Hitboxes are only used to mark collision boundaries on the 
			background world frame set images. They do not hold any images.
		Note: Images Held refers to the number of images held internally by a central image
			repository. The Objects themselves only contain references to the central 
			image repository. This is done to eliminate redundacy of image loading.

Along with determining the scope of gameplay, this hiearchy determines the number of image frames held by the image repository for each unique flavor of in-game Object. Generally speaking, a state is associated with an animation. An animation contains a set number of frames. Each type of Object enters into derivative states and uses the frames called from the central image repository to animate the Object's Sprite on-screen. Objects, for all intents and purposes, are synomnous with Sprites (this is not technically the whole truth; the in-game menus are created through a variety of Sprite, but their configuration is hard-coded into the game loop, so they are largely ignored for the purposes of this README.)

The following table diagrams the hierachies,

	Hero
		Types   States 	Frames
		    1	    13	    10
			Types: Hero
			States: WalkUp, WalkDown, WalkRight, WalkLeft
				AtkUp, AtkDown, AtkRight, AtkLeft,
				ShvUp, ShvDown, ShvRight, ShvLeft
				Die
			Total Images Held: 130

	NPC
		Types	States	Frames
 	  	 3	   4	    9

			Types: Sign*, SaveCrystal* Ducont, Automaton, Cyrus
			States: WalkUp, WalkDown, WalkRight, WalkLeft
			Total Images Held: 

	Villain
		Types   States   Frames 
		    1       9        20
			Types: Golem
			States: WalkUp, WalkDown, WalkRight, WalkLeft
				AtkUp, AtkDown, AtkRight, AtkLeft,
				Die
			Total Images Held: 180

	LightSource
		Types   States   Frames	
	    	8	    2	      5
			Types: LampPost*, Candle*, Beacon*, LanternTree*, VerticalLava, HorizontalLava, LavaBubble, Stove
			States: On, Off
			Total Images Held: 24

	Effect
		Types   States   Frames 
	    	7       2        6	
			Types: Concussion, Clock, KnockBack, RunningWater, WaterFall, WaistWater, Splash
			States: Reactive, Inert
			Total Images Held: 84

A certain sub-flavor of in-game Object occurs frequently enough that is treated apart by the file-handling procedures. These are Binary Objects, Objects that have an on and off state. Binary Objects are Switches, TreasureChests, ShovelTiles, PressurePlates and Gates,	

	Switch
		Types   States   Frames 
		    1	    2	      2
			Types: 1 
			States: On, Off
			Total Images Held: 4
	TreasureChest
		Types   States   Frames  
		    1	    2         2 
			Types: 1
			States: On, Off
			Total Images Held: 4
	ShovelTile
		Types   States   Frames 
                    1       2         2  
			Types: 1
			States: On, Off
			Total Images Held: 4	
						TOTAL IMAGES: 430

__________________________________________________________
__________________________________________________________
__________________________________________________________
APPENDIX.7: Engine Class Diagram
__________________________________________________________

			START
		      __________
		     |          |
 __________          | 		|	     __________
|          |	     |          |	    |          |	
| World	   |	     | Cradle   | --- -->   | Sprite   |
|  File    |	     |		|	    |  Image   |
|   Builder|	     |	     	|	    |   Loader |	
 __________	      __________ _	     __________
   		  	  ||       _		||	
			  \/         _          \/
 __________           __________        _>    __________
|          |  	     |          |  	    |          |  
| File     | --- --> |GameWorld |  --- -->  | View     |
|  Handler |  	     |          |     	    |          |
 __________ 	      __________      	     __________

