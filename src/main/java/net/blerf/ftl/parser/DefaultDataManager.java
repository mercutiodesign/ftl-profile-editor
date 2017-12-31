package net.blerf.ftl.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;

import org.jdom2.JDOMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.vhati.ftldat.AbstractPack;
import net.vhati.ftldat.FolderPack;
import net.vhati.ftldat.FTLPack;
import net.vhati.ftldat.PackContainer;

import net.blerf.ftl.model.ShipLayout;
import net.blerf.ftl.parser.DatParser;
import net.blerf.ftl.xml.Achievement;
import net.blerf.ftl.xml.AugBlueprint;
import net.blerf.ftl.xml.BackgroundImageList;
import net.blerf.ftl.xml.Blueprints;
import net.blerf.ftl.xml.CrewBlueprint;
import net.blerf.ftl.xml.CrewNameList;
import net.blerf.ftl.xml.DroneBlueprint;
import net.blerf.ftl.xml.Encounters;
import net.blerf.ftl.xml.FTLEvent;
import net.blerf.ftl.xml.FTLEventList;
import net.blerf.ftl.xml.SectorData;
import net.blerf.ftl.xml.SectorDescription;
import net.blerf.ftl.xml.SectorType;
import net.blerf.ftl.xml.ShipBlueprint;
import net.blerf.ftl.xml.ShipEvent;
import net.blerf.ftl.xml.ShipChassis;
import net.blerf.ftl.xml.SystemBlueprint;
import net.blerf.ftl.xml.WeaponBlueprint;


public class DefaultDataManager extends DataManager {

	private static final Logger log = LoggerFactory.getLogger( DefaultDataManager.class );

	private List<String> stdBlueprintsFileNames;
	private List<String> dlcBlueprintsFileNames;
	private List<String> stdEventsFileNames;
	private List<String> dlcEventsFileNames;
	private List<String> stdPlayerShipBaseIds;
	private List<String> dlcPlayerShipBaseIds;
	private List<String> stdPlayerShipIds;
	private List<String> dlcPlayerShipIds;

	private Map<String, BackgroundImageList> backgroundImageLists;

	private Map<String, Blueprints> allBlueprints;
	private Map<String, Blueprints> stdBlueprints;
	private Map<String, Blueprints> dlcBlueprints;

	private Map<String, Encounters> allEvents;
	private Map<String, Encounters> stdEvents;
	private Map<String, Encounters> dlcEvents;

	private Map<String, AugBlueprint> stdAugmentIdMap;
	private Map<String, AugBlueprint> dlcAugmentIdMap;

	private Map<String, CrewBlueprint> stdCrewIdMap;
	private Map<String, CrewBlueprint> dlcCrewIdMap;

	private Map<String, DroneBlueprint> stdDroneIdMap;
	private Map<String, DroneBlueprint> dlcDroneIdMap;

	private Map<String, SystemBlueprint> stdSystemIdMap;
	private Map<String, SystemBlueprint> dlcSystemIdMap;

	private Map<String, WeaponBlueprint> stdWeaponIdMap;
	private Map<String, WeaponBlueprint> dlcWeaponIdMap;

	private Map<String, ShipBlueprint> stdShipIdMap;
	private Map<String, ShipBlueprint> dlcShipIdMap;

	private Map<String, List<ShipBlueprint>> stdPlayerShipVariantsMap;
	private Map<String, List<ShipBlueprint>> dlcPlayerShipVariantsMap;
	private Map<String, ShipBlueprint> stdPlayerShipIdMap;
	private Map<String, ShipBlueprint> dlcPlayerShipIdMap;
	private Map<String, ShipBlueprint> stdAutoShipIdMap;
	private Map<String, ShipBlueprint> dlcAutoShipIdMap;

	private Map<String, ShipEvent> stdShipEventIdMap;
	private Map<String, ShipEvent> dlcShipEventIdMap;

	private Map<String, Achievement> achievementIdMap;
	private Map<ShipBlueprint, List<Achievement>> stdShipAchievementIdMap;
	private Map<ShipBlueprint, List<Achievement>> dlcShipAchievementIdMap;
	private List<Achievement> generalAchievements;

	private Map<String, ShipLayout> shipLayouts;
	private Map<String, ShipChassis> shipChassisMap;
	private List<CrewNameList.CrewName> crewNamesMale;
	private List<CrewNameList.CrewName> crewNamesFemale;

	private Map<String, SectorDescription> sectorDescriptionIdMap;
	private Map<String, SectorType> stdSectorTypeIdMap;
	private Map<String, SectorType> dlcSectorTypeIdMap;

	private PackContainer packContainer = null;
	private	DatParser datParser = null;


	public DefaultDataManager( File datsDir ) throws IOException, JAXBException, JDOMException {

		boolean meltdown = false;
		List<InputStream> streams = new ArrayList<InputStream>();

		try {
			File dataDatFile = new File( datsDir, "data.dat" );
			File resourceDatFile = new File( datsDir, "resource.dat" );

			packContainer = new PackContainer();
			if ( dataDatFile.exists() && resourceDatFile.exists() ) {  // FTL 1.01-1.5.13.
				AbstractPack dataPack = new FTLPack( dataDatFile, "r" );
				packContainer.setPackFor( "data/", dataPack );

				AbstractPack resourcePack = new FTLPack( resourceDatFile, "r" );
				packContainer.setPackFor( "audio/", resourcePack );
				packContainer.setPackFor( "fonts/", resourcePack );
				packContainer.setPackFor( "img/", resourcePack );
			}
			else {
				throw new IOException( String.format( "Could not find both \"%s\" and \"%s\"", dataDatFile.getName(), resourceDatFile.getName() ) );
			}

			datParser = new DatParser();

			log.info( "Reading Achievements..." );
			log.debug( "Reading \"data/achievements.xml\"..." );
			InputStream achStream = getResourceInputStream( "data/achievements.xml" );
			streams.add( achStream );
			List<Achievement> achievements = datParser.readAchievements( achStream, "achievements.xml" );

			log.info( "Reading Blueprints..." );
			stdBlueprintsFileNames = new ArrayList<String>();
			stdBlueprintsFileNames.add( "blueprints.xml" );
			stdBlueprintsFileNames.add( "autoBlueprints.xml" );
			stdBlueprintsFileNames.add( "bosses.xml" );  // FTL 1.5.4+

			dlcBlueprintsFileNames = new ArrayList<String>();
			dlcBlueprintsFileNames.add( "dlcBlueprints.xml" );
			dlcBlueprintsFileNames.add( "dlcBlueprintsOverwrite.xml" );
			dlcBlueprintsFileNames.add( "dlcPirateBlueprints.xml" );

			allBlueprints = new LinkedHashMap<String, Blueprints>();
			for ( String blueprintsFileName : stdBlueprintsFileNames ) {
				if ( !hasResourceInputStream( "data/"+ blueprintsFileName ) ) continue;

				log.debug( String.format( "Reading \"data/%s\"...", blueprintsFileName ) );
				InputStream tmpStream = getResourceInputStream( "data/"+ blueprintsFileName );
				streams.add( tmpStream );
				Blueprints tmpBlueprints = datParser.readBlueprints( tmpStream, blueprintsFileName );
				allBlueprints.put( blueprintsFileName, tmpBlueprints );
			}

			for ( String blueprintsFileName : dlcBlueprintsFileNames ) {
				if ( !hasResourceInputStream( "data/"+ blueprintsFileName ) ) continue;

				log.debug( String.format( "Reading \"data/%s\"...", blueprintsFileName ) );
				InputStream tmpStream = getResourceInputStream( "data/"+ blueprintsFileName );
				streams.add( tmpStream );
				Blueprints tmpBlueprints = datParser.readBlueprints( tmpStream, blueprintsFileName );
				allBlueprints.put( blueprintsFileName, tmpBlueprints );
			}

			log.info( "Reading Events..." );
			stdEventsFileNames = new ArrayList<String>();
			stdEventsFileNames.add( "events.xml" );
			stdEventsFileNames.add( "newEvents.xml" );
			stdEventsFileNames.add( "events_crystal.xml" );
			stdEventsFileNames.add( "events_engi.xml" );
			stdEventsFileNames.add( "events_mantis.xml" );
			stdEventsFileNames.add( "events_rock.xml" );
			stdEventsFileNames.add( "events_slug.xml" );
			stdEventsFileNames.add( "events_zoltan.xml" );
			stdEventsFileNames.add( "events_nebula.xml" );
			stdEventsFileNames.add( "events_pirate.xml" );
			stdEventsFileNames.add( "events_rebel.xml" );
			stdEventsFileNames.add( "nameEvents.xml" );
			stdEventsFileNames.add( "events_fuel.xml" );
			stdEventsFileNames.add( "events_boss.xml" );
			stdEventsFileNames.add( "events_ships.xml" );

			dlcEventsFileNames = new ArrayList<String>();
			dlcEventsFileNames.add( "dlcEvents.xml" );
			dlcEventsFileNames.add( "dlcEvents_anaerobic.xml" );

			allEvents = new LinkedHashMap<String, Encounters>();
			for ( String eventsFileName : stdEventsFileNames ) {
				log.debug( String.format( "Reading \"data/%s\"...", eventsFileName ) );
				InputStream tmpStream = getResourceInputStream( "data/"+ eventsFileName );
				streams.add( tmpStream );
				Encounters tmpEncounters = datParser.readEvents( tmpStream, eventsFileName );
				allEvents.put( eventsFileName, tmpEncounters );
			}

			for ( String eventsFileName : dlcEventsFileNames ) {
				if ( !hasResourceInputStream( "data/"+ eventsFileName ) ) continue;

				log.debug( String.format( "Reading \"data/%s\"...", eventsFileName ) );
				InputStream tmpStream = getResourceInputStream( "data/"+ eventsFileName );
				streams.add( tmpStream );
				Encounters tmpEncounters = datParser.readEvents( tmpStream, eventsFileName );
				allEvents.put( eventsFileName, tmpEncounters );
			}

			log.info( "Reading Crew Names..." );
			log.debug( "Reading \"data/names.xml\"..." );
			InputStream crewNamesStream = getResourceInputStream( "data/names.xml" );
			streams.add( crewNamesStream );
			List<CrewNameList> crewNameLists = datParser.readCrewNames( crewNamesStream, "names.xml" );

			log.info( "Reading Sector Data..." );
			log.debug( "Reading \"data/sector_data.xml\"..." );
			InputStream sectorDataStream = getResourceInputStream( "data/sector_data.xml" );
			streams.add( sectorDataStream );
			SectorData tmpSectorData = datParser.readSectorData( sectorDataStream, "sector_data.xml" );
			sectorDescriptionIdMap = new LinkedHashMap<String, SectorDescription>();
			for ( SectorDescription tmpDesc : tmpSectorData.getSectorDescriptions() ) {
				sectorDescriptionIdMap.put( tmpDesc.getId(), tmpDesc );
			}
			stdSectorTypeIdMap = new LinkedHashMap<String, SectorType>();
			dlcSectorTypeIdMap = new LinkedHashMap<String, SectorType>();
			Pattern sectorOverridePtn = Pattern.compile( "^OVERRIDE_(.*)" );

			for ( SectorType tmpType : tmpSectorData.getSectorTypes() ) {
				if ( sectorOverridePtn.matcher( tmpType.getId() ).matches() ) continue;
				stdSectorTypeIdMap.put( tmpType.getId(), tmpType );
				dlcSectorTypeIdMap.put( tmpType.getId(), tmpType );
			}
			for ( SectorType tmpType : tmpSectorData.getSectorTypes() ) {
				Matcher m = sectorOverridePtn.matcher( tmpType.getId() );
				if ( !m.matches() ) continue;
				String baseId = m.group( 1 );
				dlcSectorTypeIdMap.put( baseId, tmpType );
			}

			log.info( "Reading Background Image Lists..." );
			log.debug( "Reading \"data/events_imageList.xml\"..." );
			InputStream imageListsStream = getResourceInputStream( "data/events_imageList.xml" );
			streams.add( imageListsStream );
			List<BackgroundImageList> imageLists = datParser.readImageLists( imageListsStream, "events_imageList.xml" );

			log.info( "Finished reading FTL resources." );

			achievementIdMap = new LinkedHashMap<String, Achievement>();
			for( Achievement ach : achievements ) {
				achievementIdMap.put( ach.getId(), ach );
			}

			// Add hardcoded ship Quest and Victory achievements. (FTL 1.5.4+)
			// TODO: Magic strings.

			Map<String, String> questAchIds = new LinkedHashMap<String, String>();
			// No Kestrel quest.
			questAchIds.put( "PLAYER_SHIP_STEALTH", "PLAYER_SHIP_STEALTH_QUEST" );
			questAchIds.put( "PLAYER_SHIP_MANTIS", "PLAYER_SHIP_MANTIS_QUEST" );
			// No Engi quest.
			questAchIds.put( "PLAYER_SHIP_FED", "PLAYER_SHIP_FED_QUEST" );
			questAchIds.put( "PLAYER_SHIP_JELLY", "PLAYER_SHIP_JELLY_QUEST" );
			questAchIds.put( "PLAYER_SHIP_ROCK", "PLAYER_SHIP_ROCK_QUEST" );
			questAchIds.put( "PLAYER_SHIP_ENERGY", "PLAYER_SHIP_ENERGY_QUEST" );
			questAchIds.put( "PLAYER_SHIP_CRYSTAL", "PLAYER_SHIP_CRYSTAL_QUEST" );
			// No Anaerobic quest.

			for ( Map.Entry<String, String> entry : questAchIds.entrySet() ) {
				Achievement questAch = achievementIdMap.get( entry.getKey() );
				if ( questAch == null ) {
					questAch = new Achievement();
					questAch.setId( entry.getValue() );
					questAch.setName( entry.getValue() );
					questAch.setDescription( "Dummy quest achievement." );
					questAch.setImagePath( null );
					questAch.setShipId( entry.getKey() );
					achievementIdMap.put( questAch.getId(), questAch );
				}
				questAch.setQuest( true );
			}

			Map<String, String> victoryAchIds = new LinkedHashMap<String, String>();
			victoryAchIds.put( "PLAYER_SHIP_HARD", "PLAYER_SHIP_HARD_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_STEALTH", "PLAYER_SHIP_STEALTH_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_MANTIS", "PLAYER_SHIP_MANTIS_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_CIRCLE", "PLAYER_SHIP_CIRCLE_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_FED", "PLAYER_SHIP_FED_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_JELLY", "PLAYER_SHIP_JELLY_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_ROCK", "PLAYER_SHIP_ROCK_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_ENERGY", "PLAYER_SHIP_ENERGY_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_CRYSTAL", "PLAYER_SHIP_CRYSTAL_VICTORY" );
			victoryAchIds.put( "PLAYER_SHIP_ANAEROBIC", "PLAYER_SHIP_ANAEROBIC_VICTORY" );

			for ( Map.Entry<String, String> entry : victoryAchIds.entrySet() ) {
				Achievement victoryAch = achievementIdMap.get( entry.getKey() );
				if ( victoryAch == null ) {
					victoryAch = new Achievement();
					victoryAch.setId( entry.getValue() );
					victoryAch.setName( entry.getValue() );
					victoryAch.setDescription( "Dummy victory achievement." );
					victoryAch.setImagePath( null );
					victoryAch.setShipId( entry.getKey() );
					achievementIdMap.put( victoryAch.getId(), victoryAch );
				}
				victoryAch.setVictory( true );
			}

			generalAchievements = new ArrayList<Achievement>();
			for( Achievement ach : achievementIdMap.values() ) {
				if ( ach.getShipId() == null ) {
					generalAchievements.add(ach);
				}
			}

			stdBlueprints = new LinkedHashMap<String, Blueprints>( stdBlueprintsFileNames.size() );
			dlcBlueprints = new LinkedHashMap<String, Blueprints>( dlcBlueprintsFileNames.size() + stdBlueprintsFileNames.size() );
			for ( String blueprintsFileName : stdBlueprintsFileNames ) {
				Blueprints blueprints = allBlueprints.get( blueprintsFileName );
				if ( blueprints == null ) continue;
				stdBlueprints.put( blueprintsFileName, blueprints );
				dlcBlueprints.put( blueprintsFileName, blueprints );
			}
			for ( String blueprintsFileName : dlcBlueprintsFileNames ) {
				Blueprints blueprints = allBlueprints.get( blueprintsFileName );
				if ( blueprints == null ) continue;
				dlcBlueprints.put( blueprintsFileName, blueprints );
			}

			stdAugmentIdMap = new LinkedHashMap<String, AugBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<AugBlueprint> augBlueprintList = blueprints.getAugBlueprint();
				if ( augBlueprintList == null ) continue;
				for ( AugBlueprint augment : augBlueprintList ) {
					stdAugmentIdMap.put( augment.getId(), augment );
				}
			}
			dlcAugmentIdMap = new LinkedHashMap<String, AugBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<AugBlueprint> augBlueprintList = blueprints.getAugBlueprint();
				if ( augBlueprintList == null ) continue;
				for ( AugBlueprint augment : augBlueprintList ) {
					dlcAugmentIdMap.put( augment.getId(), augment );
				}
			}

			stdCrewIdMap = new LinkedHashMap<String, CrewBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<CrewBlueprint> crewBlueprintList = blueprints.getCrewBlueprint();
				if ( crewBlueprintList == null ) continue;
				for ( CrewBlueprint crew : crewBlueprintList ) {
					stdCrewIdMap.put( crew.getId(), crew );
				}
			}
			dlcCrewIdMap = new LinkedHashMap<String, CrewBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<CrewBlueprint> crewBlueprintList = blueprints.getCrewBlueprint();
				if ( crewBlueprintList == null ) continue;
				for ( CrewBlueprint crew : crewBlueprintList ) {
					dlcCrewIdMap.put( crew.getId(), crew );
				}
			}

			stdDroneIdMap = new LinkedHashMap<String, DroneBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<DroneBlueprint> droneBlueprintList = blueprints.getDroneBlueprint();
				if ( droneBlueprintList == null ) continue;
				for ( DroneBlueprint drone : droneBlueprintList ) {
					stdDroneIdMap.put( drone.getId(), drone );
				}
			}
			dlcDroneIdMap = new LinkedHashMap<String, DroneBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<DroneBlueprint> droneBlueprintList = blueprints.getDroneBlueprint();
				if ( droneBlueprintList == null ) continue;
				for ( DroneBlueprint drone : droneBlueprintList ) {
					dlcDroneIdMap.put( drone.getId(), drone );
				}
			}

			stdSystemIdMap = new LinkedHashMap<String, SystemBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<SystemBlueprint> systemBlueprintList = blueprints.getSystemBlueprint();
				if ( systemBlueprintList == null ) continue;
				for ( SystemBlueprint system : systemBlueprintList ) {
					stdSystemIdMap.put( system.getId(), system );
				}
			}
			dlcSystemIdMap = new LinkedHashMap<String, SystemBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<SystemBlueprint> systemBlueprintList = blueprints.getSystemBlueprint();
				if ( systemBlueprintList == null ) continue;
				for ( SystemBlueprint system : systemBlueprintList ) {
					dlcSystemIdMap.put( system.getId(), system );
				}
			}

			stdWeaponIdMap = new LinkedHashMap<String, WeaponBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<WeaponBlueprint> weaponBlueprintList = blueprints.getWeaponBlueprint();
				if ( weaponBlueprintList == null ) continue;
				for ( WeaponBlueprint weapon : weaponBlueprintList ) {
					stdWeaponIdMap.put( weapon.getId(), weapon );
				}
			}
			dlcWeaponIdMap = new LinkedHashMap<String, WeaponBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<WeaponBlueprint> weaponBlueprintList = blueprints.getWeaponBlueprint();
				if ( weaponBlueprintList == null ) continue;
				for ( WeaponBlueprint weapon : weaponBlueprintList ) {
					dlcWeaponIdMap.put( weapon.getId(), weapon );
				}
			}

			stdShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : stdBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<ShipBlueprint> shipBlueprintList = blueprints.getShipBlueprint();
				if ( shipBlueprintList == null ) continue;
				for ( ShipBlueprint ship : shipBlueprintList ) {
					stdShipIdMap.put( ship.getId(), ship );
				}
			}
			dlcShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( Map.Entry<String, Blueprints> entry : dlcBlueprints.entrySet() ) {
				Blueprints blueprints = entry.getValue();
				List<ShipBlueprint> shipBlueprintList = blueprints.getShipBlueprint();
				if ( shipBlueprintList == null ) continue;
				for ( ShipBlueprint ship : shipBlueprintList ) {
					dlcShipIdMap.put( ship.getId(), ship );
				}
			}

			stdPlayerShipBaseIds = new ArrayList<String>();     // TODO: Magic strings.
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_HARD" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_STEALTH" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_MANTIS" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_CIRCLE" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_FED" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_JELLY" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_ROCK" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_ENERGY" );
			stdPlayerShipBaseIds.add( "PLAYER_SHIP_CRYSTAL" );

			dlcPlayerShipBaseIds = new ArrayList<String>();
			dlcPlayerShipBaseIds.addAll( stdPlayerShipBaseIds );
			dlcPlayerShipBaseIds.add( "PLAYER_SHIP_ANAEROBIC" );

			stdPlayerShipIds = new ArrayList<String>();
			stdPlayerShipVariantsMap = new LinkedHashMap<String, List<ShipBlueprint>>( stdPlayerShipBaseIds.size() );
			for ( String baseId : stdPlayerShipBaseIds ) {
				stdPlayerShipIds.add( baseId );

				List<ShipBlueprint> variantList = new ArrayList<ShipBlueprint>(2);
				stdPlayerShipVariantsMap.put( baseId, variantList );
				variantList.add( stdShipIdMap.get( baseId ) );

				// All ships have a Type-B layout.
				String variantId = String.format("%s_%d", baseId, 2);
				stdPlayerShipIds.add( variantId );
				variantList.add( stdShipIdMap.get( variantId ) );
			}
			dlcPlayerShipIds = new ArrayList<String>();
			dlcPlayerShipVariantsMap = new LinkedHashMap<String, List<ShipBlueprint>>( dlcPlayerShipBaseIds.size() );
			for ( String baseId : dlcPlayerShipBaseIds ) {
				dlcPlayerShipIds.add( baseId );

				List<ShipBlueprint> variantList = new ArrayList<ShipBlueprint>(3);
				dlcPlayerShipVariantsMap.put( baseId, variantList );
				variantList.add( dlcShipIdMap.get( baseId ) );

				// All ships have a Type-B layout.
				String variantId = String.format("%s_%d", baseId, 2);
				dlcPlayerShipIds.add( variantId );
				variantList.add( dlcShipIdMap.get( variantId ) );

				// Most ships have a Type-C layout.
				if ( !baseId.equals("PLAYER_SHIP_CRYSTAL") && !baseId.equals("PLAYER_SHIP_ANAEROBIC") ) {
					variantId = String.format("%s_%d", baseId, 3);
					dlcPlayerShipIds.add( variantId );
					variantList.add( dlcShipIdMap.get( variantId ) );
				} else {
					variantList.add( null );
				}
			}

			stdPlayerShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( String playerShipId : stdPlayerShipIds ) {
				ShipBlueprint ship = stdShipIdMap.get( playerShipId );
				if ( ship == null ) continue;
				stdPlayerShipIdMap.put( playerShipId, ship );
			}
			dlcPlayerShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( String playerShipId : dlcPlayerShipIds ) {
				ShipBlueprint ship = dlcShipIdMap.get( playerShipId );
				if ( ship == null ) continue;
				dlcPlayerShipIdMap.put( playerShipId, ship );
			}

			stdAutoShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( Map.Entry<String, ShipBlueprint> entry : stdShipIdMap.entrySet() ) {
				if ( !stdPlayerShipIdMap.containsKey( entry.getKey() ) ) {
					stdAutoShipIdMap.put( entry.getKey(), entry.getValue() );
				}
			}
			dlcAutoShipIdMap = new LinkedHashMap<String, ShipBlueprint>();
			for ( Map.Entry<String, ShipBlueprint> entry : dlcShipIdMap.entrySet() ) {
				if ( !dlcPlayerShipIdMap.containsKey( entry.getKey() ) ) {
					dlcAutoShipIdMap.put( entry.getKey(), entry.getValue() );
				}
			}

			// Ship achievements are only tied to "Type A" variants.
			stdShipAchievementIdMap = new HashMap<ShipBlueprint, List<Achievement>>();
			for ( Map.Entry<String, ShipBlueprint> entry : stdPlayerShipIdMap.entrySet() ) {
				List<Achievement> shipAchs = new ArrayList<Achievement>();
				for ( Achievement ach : achievementIdMap.values() )
					if ( entry.getKey().equals( ach.getShipId() ) )
						shipAchs.add(ach);
				stdShipAchievementIdMap.put( entry.getValue(), shipAchs );
			}
			dlcShipAchievementIdMap = new HashMap<ShipBlueprint, List<Achievement>>();
			for ( Map.Entry<String, ShipBlueprint> entry : dlcPlayerShipIdMap.entrySet() ) {
				List<Achievement> shipAchs = new ArrayList<Achievement>();
				for ( Achievement ach : achievementIdMap.values() )
					if ( entry.getKey().equals( ach.getShipId() ) )
						shipAchs.add(ach);
				dlcShipAchievementIdMap.put( entry.getValue(), shipAchs );
			}

			// These'll populate as files are requested.
			shipLayouts = new HashMap<String, ShipLayout>();
			shipChassisMap = new HashMap<String, ShipChassis>();

			crewNamesMale = new ArrayList<CrewNameList.CrewName>();
			crewNamesFemale = new ArrayList<CrewNameList.CrewName>();
			for ( CrewNameList crewNameList : crewNameLists ) {
				if ( "male".equals( crewNameList.getSex() ) )
					crewNamesMale.addAll( crewNameList.getNames() );
				else
					crewNamesFemale.addAll( crewNameList.getNames() );
			}

			backgroundImageLists = new LinkedHashMap<String, BackgroundImageList>();
			for ( BackgroundImageList imageList : imageLists )
				backgroundImageLists.put( imageList.getId(), imageList );

			stdEvents = new LinkedHashMap<String, Encounters>( stdEventsFileNames.size() );
			dlcEvents = new LinkedHashMap<String, Encounters>( dlcEventsFileNames.size() + stdEventsFileNames.size() );
			for ( String eventsFileName : stdEventsFileNames ) {
				Encounters tmpEncounters = allEvents.get( eventsFileName );
				if ( tmpEncounters == null ) continue;
				stdEvents.put( eventsFileName, tmpEncounters );
				dlcEvents.put( eventsFileName, tmpEncounters );
			}
			for ( String eventsFileName : dlcEventsFileNames ) {
				Encounters tmpEncounters = allEvents.get( eventsFileName );
				if ( tmpEncounters == null ) continue;
				dlcEvents.put( eventsFileName, tmpEncounters );
			}

			stdShipEventIdMap = new LinkedHashMap<String, ShipEvent>();
			for ( Map.Entry<String, Encounters> entry : stdEvents.entrySet() ) {
				Encounters tmpEncounters = entry.getValue();
				List<ShipEvent> shipEventList = tmpEncounters.getShipEvents();
				if ( shipEventList == null ) continue;
				for ( ShipEvent shipEvent : shipEventList ) {
					stdShipEventIdMap.put( shipEvent.getId(), shipEvent );
				}
			}
			dlcShipEventIdMap = new LinkedHashMap<String, ShipEvent>();
			for ( Map.Entry<String, Encounters> entry : dlcEvents.entrySet() ) {
				Encounters tmpEncounters = entry.getValue();
				List<ShipEvent> shipEventList = tmpEncounters.getShipEvents();
				if ( shipEventList == null ) continue;
				for ( ShipEvent shipEvent : shipEventList ) {
					dlcShipEventIdMap.put( shipEvent.getId(), shipEvent );
				}
			}
		}
		catch ( JDOMException e ) {
			meltdown = true;
			throw e;
		}
		catch ( JAXBException e ) {
			meltdown = true;
			throw e;
		}
		catch ( IOException e ) {
			meltdown = true;
			throw e;
		}
		finally {
			for ( InputStream stream : streams ) {
				try {if ( stream != null ) stream.close();}
				catch ( IOException f ) {}
			}

			if ( meltdown ) this.close();
		}
	}

	@Override
	public void close() {
		if ( packContainer != null ) {
			for ( AbstractPack pack : packContainer.getPacks() ) {
				try {pack.close();}
				catch ( IOException e ) {}
			}
		}
	}

	@Override
	public boolean hasResourceInputStream( String innerPath ) {
		AbstractPack pack = packContainer.getPackFor( innerPath );
		if ( pack != null && pack.contains( innerPath ) ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public InputStream getResourceInputStream( String innerPath ) throws IOException {
		AbstractPack pack = packContainer.getPackFor( innerPath );
		if ( pack != null ) {
			return pack.getInputStream( innerPath );
		}
		else {
			throw new IOException( String.format( "Unexpected innerPath: %s", innerPath ) );
		}
	}

	public void extractResources( File extractDir ) throws IOException {
		AbstractPack dstPack = null;
		InputStream is = null;

		try {
			if ( !extractDir.exists() ) extractDir.mkdirs();

			dstPack = new FolderPack( extractDir );

			for ( AbstractPack srcPack : packContainer.getPacks() ) {
				log.info( String.format( "Extracting \"%s\" into \"%s\"", srcPack.getName(), extractDir.getPath() ) );
				List<String> innerPaths = srcPack.list();

				for ( String innerPath : innerPaths ) {
					if ( dstPack.contains( innerPath ) ) {
						log.info( "While extracting resources, this file was overwritten: "+ innerPath );
						dstPack.remove( innerPath );
					}
					is = srcPack.getInputStream( innerPath );
					dstPack.add( innerPath, is );
				}
				srcPack.close();
			}
		}
		finally {
			try {if ( is != null ) is.close();}
			catch ( IOException e ) {}

			try {if ( dstPack != null ) dstPack.close();}
			catch ( IOException e ) {}

			// DataMagager's still using the resources, so son't close them.
		}
	}

	@Override
	public Achievement getAchievement( String id ) {
		Achievement result = achievementIdMap.get( id );
		if ( result == null )
			log.error( "No Achievement found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, Achievement> getAchievements() {
		return achievementIdMap;
	}

	@Override
	public AugBlueprint getAugment( String id, boolean dlcEnabled ) {
		Map<String, AugBlueprint> augments = null;
		if ( dlcEnabled ) {
			augments = dlcAugmentIdMap;
		} else {
			augments = stdAugmentIdMap;
		}

		AugBlueprint result = augments.get(id);
		if ( result == null )
			log.error( "No AugBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, AugBlueprint> getAugments( boolean dlcEnabled ) {
		Map<String, AugBlueprint> augments = null;
		if ( dlcEnabled ) {
			augments = dlcAugmentIdMap;
		} else {
			augments = stdAugmentIdMap;
		}

		return augments;
	}

	@Override
	public CrewBlueprint getCrew( String id, boolean dlcEnabled ) {
		Map<String, CrewBlueprint> crews = null;
		if ( dlcEnabled ) {
			crews = dlcCrewIdMap;
		} else {
			crews = stdCrewIdMap;
		}

		CrewBlueprint result = crews.get(id);
		if ( result == null )
			log.error( "No CrewBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, CrewBlueprint> getCrews( boolean dlcEnabled ) {
		Map<String, CrewBlueprint> crews = null;
		if ( dlcEnabled ) {
			crews = dlcCrewIdMap;
		} else {
			crews = stdCrewIdMap;
		}

		return crews;
	}

	@Override
	public DroneBlueprint getDrone( String id, boolean dlcEnabled ) {
		Map<String, DroneBlueprint> drones = null;
		if ( dlcEnabled ) {
			drones = dlcDroneIdMap;
		} else {
			drones = stdDroneIdMap;
		}

		DroneBlueprint result = drones.get(id);
		if ( result == null )
			log.error( "No DroneBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, DroneBlueprint> getDrones( boolean dlcEnabled ) {
		Map<String, DroneBlueprint> drones = null;
		if ( dlcEnabled ) {
			drones = dlcDroneIdMap;
		} else {
			drones = stdDroneIdMap;
		}

		return drones;
	}

	@Override
	public SystemBlueprint getSystem( String id, boolean dlcEnabled ) {
		Map<String, SystemBlueprint> systems = null;
		if ( dlcEnabled ) {
			systems = dlcSystemIdMap;
		} else {
			systems = stdSystemIdMap;
		}

		SystemBlueprint result = systems.get(id);
		if ( result == null )
			log.error( "No SystemBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public WeaponBlueprint getWeapon( String id, boolean dlcEnabled ) {
		Map<String, WeaponBlueprint> weapons = null;
		if ( dlcEnabled ) {
			weapons = dlcWeaponIdMap;
		} else {
			weapons = stdWeaponIdMap;
		}

		WeaponBlueprint result = weapons.get(id);
		if ( result == null )
			log.error( "No WeaponBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, WeaponBlueprint> getWeapons( boolean dlcEnabled ) {
		Map<String, WeaponBlueprint> weapons = null;
		if ( dlcEnabled ) {
			weapons = dlcWeaponIdMap;
		} else {
			weapons = stdWeaponIdMap;
		}

		return weapons;
	}

	@Override
	public ShipBlueprint getShip( String id, boolean dlcEnabled ) {
		Map<String, ShipBlueprint> ships = null;
		if ( dlcEnabled ) {
			ships = dlcShipIdMap;
		} else {
			ships = stdShipIdMap;
		}

		ShipBlueprint result = ships.get(id);
		if ( result == null )
			log.error( "No ShipBlueprint found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, ShipBlueprint> getShips( boolean dlcEnabled ) {
		Map<String, ShipBlueprint> ships = null;
		if ( dlcEnabled ) {
			ships = dlcShipIdMap;
		} else {
			ships = stdShipIdMap;
		}

		return ships;
	}

	@Override
	public Map<String, ShipBlueprint> getAutoShips( boolean dlcEnabled ) {
		Map<String, ShipBlueprint> autoShips = null;
		if ( dlcEnabled ) {
			autoShips = dlcAutoShipIdMap;
		} else {
			autoShips = stdAutoShipIdMap;
		}

		return autoShips;
	}

	@Override
	public Map<String, ShipBlueprint> getPlayerShips( boolean dlcEnabled ) {
		Map<String, ShipBlueprint> playerShips = null;
		if ( dlcEnabled ) {
			playerShips = dlcPlayerShipIdMap;
		} else {
			playerShips = stdPlayerShipIdMap;
		}

		return playerShips;
	}

	/**
	 * Returns a list of ShipBlueprint ids of all Type-A player ships.
	 */
	@Override
	public List<String> getPlayerShipBaseIds( boolean dlcEnabled ) {
		List<String> playerShipBaseIds = null;
		if ( dlcEnabled ) {
			playerShipBaseIds = dlcPlayerShipBaseIds;
		} else {
			playerShipBaseIds = stdPlayerShipBaseIds;
		}

		return playerShipBaseIds;
	}

	/**
	 * Returns the nth Type-ABC variant of a player ship.
	 *
	 * @param baseId the ShipBlueprint id of a Type-A player ship.
	 * @param n 0=Type-A, 1=Type-B, 2=Type-C
	 * @param dlcEnabled true to include DLC content, false otherwise
	 * @return the ship, or null
	 */
	@Override
	public ShipBlueprint getPlayerShipVariant( String baseId, int n, boolean dlcEnabled ) {
		Map<String, List<ShipBlueprint>> variantsMap = null;
		if ( dlcEnabled ) {
			variantsMap = dlcPlayerShipVariantsMap;
		} else {
			variantsMap = stdPlayerShipVariantsMap;
		}

		List<ShipBlueprint> variantList = variantsMap.get( baseId );
		if ( variantList == null || n < 0 || n >= variantList.size() ) return null;
		return variantList.get( n );
	}

	@Override
	public List<Achievement> getShipAchievements( ShipBlueprint ship, boolean dlcEnabled ) {
		Map<ShipBlueprint, List<Achievement>> shipAchievements = null;
		if ( dlcEnabled ) {
			shipAchievements = dlcShipAchievementIdMap;
		} else {
			shipAchievements = stdShipAchievementIdMap;
		}

		return shipAchievements.get( ship );
	}

	@Override
	public List<Achievement> getGeneralAchievements() {
		return generalAchievements;
	}

	@Override
	public ShipLayout getShipLayout( String id ) {
		ShipLayout result = shipLayouts.get(id);

		if ( result == null ) {  // Wasn't cached; try parsing it.
			InputStream in = null;
			try {
				in = getResourceInputStream("data/"+ id +".txt");
				result = datParser.readLayout(in, id +".txt");
				shipLayouts.put( id, result );
			}
			catch ( FileNotFoundException e ) {
				log.error( "No ShipLayout found for id: "+ id );
			}
			catch ( IOException e ) {
				log.error( "An error occurred while parsing ShipLayout: "+ id, e );
			}
			finally {
				try {if ( in != null ) in.close();}
				catch ( IOException f ) {}
			}
		}

		return result;
	}

	@Override
	public ShipChassis getShipChassis( String id ) {
		ShipChassis result = shipChassisMap.get(id);

		if ( result == null ) {  // Wasn't cached; try parsing it.
			InputStream in = null;
			try {
				log.debug( String.format( "Reading ship chassis (data/%s.xml)...", id ) );
				in = getResourceInputStream( "data/"+ id +".xml" );
				result = datParser.readChassis(in, id +".xml");
				shipChassisMap.put( id, result );
			}
			catch ( JDOMException e ) {
				log.error( "Parsing XML failed for ShipChassis id: "+ id, e );
			}
			catch ( JAXBException e ) {
				log.error( "Parsing XML failed for ShipChassis id: "+ id, e );
			}
			catch ( FileNotFoundException e ) {
				log.error( "No ShipChassis found for id: "+ id );
			}
			catch ( IOException e ) {
				log.error( "An error occurred while parsing ShipChassis: "+ id, e );
			}
			finally {
				try {if ( in != null ) in.close();}
				catch ( IOException f ) {}
			}
		}

		return result;
	}

	/**
	 * Returns an Event with a given id.
	 * All event xml files are searched.
	 *
	 * Events and EventLists share a namespace,
	 * so an id could belong to either.
	 */
	@Override
	public FTLEvent getEventById( String id, boolean dlcEnabled ) {
		Map<String, Encounters> events = null;
		if ( dlcEnabled ) {
			events = dlcEvents;
		} else {
			events = stdEvents;
		}

		FTLEvent result = null;
		for ( Map.Entry<String, Encounters> entry : events.entrySet() ) {
			FTLEvent tmpEvent = entry.getValue().getEventById(id);
			if ( tmpEvent != null ) result = tmpEvent;
		}
		return result;
	}

	/**
	 * Returns an EventList with a given id.
	 * All event xml files are searched.
	 *
	 * Events and EventLists share a namespace,
	 * so an id could belong to either.
	 */
	@Override
	public FTLEventList getEventListById( String id, boolean dlcEnabled ) {
		Map<String, Encounters> events = null;
		if ( dlcEnabled ) {
			events = dlcEvents;
		} else {
			events = stdEvents;
		}

		FTLEventList result = null;
		for ( Map.Entry<String, Encounters> entry : events.entrySet() ) {
			FTLEventList tmpEventList = entry.getValue().getEventListById(id);
			if ( tmpEventList != null ) result = tmpEventList;
		}
		return result;
	}

	/**
	 * Returns all Encounters objects, mapped to xml file names.
	 *
	 * Each can be queried for its FTLEvent or FTLEventList members.
	 */
	@Override
	public Map<String, Encounters> getEncounters( boolean dlcEnabled ) {
		Map<String, Encounters> events = null;
		if ( dlcEnabled ) {
			events = dlcEvents;
		} else {
			events = stdEvents;
		}

		return events;
	}

	@Override
	public ShipEvent getShipEventById( String id, boolean dlcEnabled ) {
		Map<String, ShipEvent> shipEvents = null;
		if ( dlcEnabled ) {
			shipEvents = dlcShipEventIdMap;
		} else {
			shipEvents = stdShipEventIdMap;
		}

		ShipEvent result = shipEvents.get(id);
		if ( result == null )
			log.error( "No ShipEvent found for id: "+ id );
		return result;
	}

	@Override
	public Map<String, ShipEvent> getShipEvents( boolean dlcEnabled ) {
		Map<String, ShipEvent> shipEvents = null;
		if ( dlcEnabled ) {
			shipEvents = dlcShipEventIdMap;
		} else {
			shipEvents = stdShipEventIdMap;
		}

		return shipEvents;
	}

	/**
	 * Returns true (male) or false (female).
	 * All possible names have equal
	 * probability, which will skew the
	 * male-to-female ratio.
	 */
	@Override
	public boolean getCrewSex() {
		int n = (int)(Math.random()*(crewNamesMale.size()+crewNamesFemale.size()));
		boolean result = (n < crewNamesMale.size());
		return result;
	}

	/**
	 * Returns a random name for a given sex.
	 */
	@Override
	public String getCrewName( boolean isMale ) {
		List<CrewNameList.CrewName> crewNames = (isMale ? crewNamesMale : crewNamesFemale);
		int n = (int)(Math.random()*crewNames.size());
		return crewNames.get(n).name;
	}

	@Override
	public SectorType getSectorTypeById( String id, boolean dlcEnabled ) {
		Map<String, SectorType> sectorTypes = null;
		if ( dlcEnabled ) {
			sectorTypes = dlcSectorTypeIdMap;
		} else {
			sectorTypes = stdSectorTypeIdMap;
		}

		SectorType result = sectorTypes.get(id);
		if ( result == null )
			log.error( "No SectorType found for id: "+ id );
		return result;
	}

	/**
	 * Returns a SectorDescription with a given id.
	 */
	@Override
	public SectorDescription getSectorDescriptionById( String id ) {
		SectorDescription result = sectorDescriptionIdMap.get( id );
		if ( result == null )
			log.error( "No SectorDescription found for id: "+ id );
		return result;
	}

	/**
	 * Returns all BackgroundImageList objects, mapped to ids.
	 *
	 * When unspecified, images are randomly chosen from the
	 * "PLANET" and "BACKGROUND" lists for sprites and backgrounds.
	 */
	@Override
	public Map<String, BackgroundImageList> getBackgroundImageLists() {
		return backgroundImageLists;
	}
}
