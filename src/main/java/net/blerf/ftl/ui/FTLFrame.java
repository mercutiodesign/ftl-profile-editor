package net.blerf.ftl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.vhati.ftldat.PackUtilities;
import net.vhati.modmanager.core.FTLUtilities;

import net.blerf.ftl.model.Profile;
import net.blerf.ftl.parser.DataManager;
import net.blerf.ftl.parser.MysteryBytes;
import net.blerf.ftl.parser.ProfileParser;
import net.blerf.ftl.parser.SavedGameParser;
import net.blerf.ftl.ui.DumpPanel;
import net.blerf.ftl.ui.ExtensionFileFilter;
import net.blerf.ftl.ui.HTMLEditorTransferHandler;
import net.blerf.ftl.ui.ProfileGeneralAchievementsPanel;
import net.blerf.ftl.ui.ProfileGeneralStatsPanel;
import net.blerf.ftl.ui.ProfileShipStatsPanel;
import net.blerf.ftl.ui.ProfileShipUnlockPanel;
import net.blerf.ftl.ui.SavedGameFloorplanPanel;
import net.blerf.ftl.ui.SavedGameGeneralPanel;
import net.blerf.ftl.ui.SavedGameHangarPanel;
import net.blerf.ftl.ui.SavedGameSectorMapPanel;
import net.blerf.ftl.ui.SavedGameSectorTreePanel;
import net.blerf.ftl.ui.SavedGameStateVarsPanel;
import net.blerf.ftl.ui.Statusbar;
import net.blerf.ftl.ui.StatusbarMouseListener;


public class FTLFrame extends JFrame implements Statusbar {

	private static final Logger log = LoggerFactory.getLogger( FTLFrame.class );

	private static final String PROFILE_SHIP_UNLOCK = "Ship Unlocks & Achievements";
	private static final String PROFILE_GENERAL_ACH = "General Achievements";
	private static final String PROFILE_GENERAL_STATS = "General Stats";
	private static final String PROFILE_SHIP_STATS = "Ship Stats";
	private static final String PROFILE_DUMP = "Dump";

	private static final String SAVE_DUMP = "Dump";
	private static final String SAVE_GENERAL = "General";
	private static final String SAVE_PLAYER_SHIP = "Player Ship";
	private static final String SAVE_NEARBY_SHIP = "Nearby Ship";
	private static final String SAVE_CHANGE_SHIP = "Change Ship";
	private static final String SAVE_SECTOR_MAP = "Sector Map";
	private static final String SAVE_SECTOR_TREE = "Sector Tree";
	private static final String SAVE_STATE_VARS = "State Vars";

	private Profile stockProfile = null;
	private Profile profile = null;
	private SavedGameParser.SavedGameState gameState = null;

	private ImageIcon openIcon = new ImageIcon( ClassLoader.getSystemResource( "open.gif" ) );
	private ImageIcon saveIcon = new ImageIcon( ClassLoader.getSystemResource( "save.gif" ) );
	private ImageIcon unlockIcon = new ImageIcon( ClassLoader.getSystemResource( "unlock.png" ) );
	private ImageIcon aboutIcon = new ImageIcon( ClassLoader.getSystemResource( "about.gif" ) );
	private final ImageIcon updateIcon = new ImageIcon( ClassLoader.getSystemResource( "update.gif" ) );
	private final ImageIcon releaseNotesIcon = new ImageIcon( ClassLoader.getSystemResource( "release-notes.png" ) );

	private URL aboutPageURL = ClassLoader.getSystemResource( "about.html" );
	private URL latestVersionTemplateURL = ClassLoader.getSystemResource( "update.html" );
	private URL releaseNotesTemplateURL = ClassLoader.getSystemResource( "release-notes.html" );

	private final String latestVersionUrl = "https://raw.github.com/Vhati/ftl-profile-editor/master/latest-version.txt";
	private final String versionHistoryUrl = "https://raw.github.com/Vhati/ftl-profile-editor/master/release-notes.txt";
	private String bugReportUrl = "https://github.com/Vhati/ftl-profile-editor/issues/new";
	private String forumThreadUrl = "http://subsetgames.com/forum/viewtopic.php?f=7&t=10959";

	private List<JButton> updatesButtonList = new ArrayList<JButton>();
	private Runnable updatesCallback;

	private JButton profileSaveBtn;
	private JButton profileDumpBtn;
	private JTabbedPane profileTabsPane;
	private ProfileShipUnlockPanel profileShipUnlockPanel;
	private ProfileGeneralAchievementsPanel profileGeneralAchsPanel;
	private ProfileGeneralStatsPanel profileGeneralStatsPanel;
	private ProfileShipStatsPanel profileShipStatsPanel;
	private DumpPanel profileDumpPanel;

	private JButton gameStateSaveBtn;
	private JButton gameStateDumpBtn;
	private JTabbedPane savedGameTabsPane;
	private DumpPanel savedGameDumpPanel;
	private SavedGameGeneralPanel savedGameGeneralPanel;
	private SavedGameFloorplanPanel savedGamePlayerFloorplanPanel;
	private SavedGameFloorplanPanel savedGameNearbyFloorplanPanel;
	private SavedGameHangarPanel savedGameHangarPanel;
	private SavedGameSectorMapPanel savedGameSectorMapPanel;
	private SavedGameSectorTreePanel savedGameSectorTreePanel;
	private SavedGameStateVarsPanel savedGameStateVarsPanel;
	private JLabel statusLbl;
	private final HyperlinkListener linkListener;

	private final String appName;
	private final int appVersion;


	public FTLFrame( String appName, int appVersion ) {
		this.appName = appName;
		this.appVersion = appVersion;

		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
		this.setSize( 800, 700 );
		this.setLocationRelativeTo( null );
		this.setTitle( String.format( "%s v%d", appName, appVersion ) );
		this.setIconImage( unlockIcon.getImage() );

		linkListener = new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate( HyperlinkEvent e ) {
				if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {

					if ( Desktop.isDesktopSupported() ) {
						try {
							Desktop.getDesktop().browse( e.getURL().toURI() );
						}
						catch ( Exception f ) {
							log.error( "Unable to open link in a browser", f );
						}
					}
				}
			}
		};

		initCheckboxIcons();

		JPanel contentPane = new JPanel( new BorderLayout() );
		this.setContentPane( contentPane );

		JTabbedPane tasksPane = new JTabbedPane();
		contentPane.add( tasksPane, BorderLayout.CENTER );

		JPanel profilePane = new JPanel( new BorderLayout() );
		tasksPane.addTab( "Profile", profilePane );

		JToolBar profileToolbar = new JToolBar();
		setupProfileToolbar( profileToolbar );
		profilePane.add( profileToolbar, BorderLayout.NORTH );

		profileTabsPane = new JTabbedPane();
		profilePane.add( profileTabsPane, BorderLayout.CENTER );

		profileShipUnlockPanel = new ProfileShipUnlockPanel( this );
		profileGeneralAchsPanel = new ProfileGeneralAchievementsPanel( this );
		profileGeneralStatsPanel = new ProfileGeneralStatsPanel( this );
		profileShipStatsPanel = new ProfileShipStatsPanel( this );
		profileDumpPanel = new DumpPanel();

		JScrollPane profileShipUnlockScroll = new JScrollPane( profileShipUnlockPanel );
		profileShipUnlockScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileGeneralAchsScroll = new JScrollPane( profileGeneralAchsPanel );
		profileGeneralAchsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileGeneralStatsScroll = new JScrollPane( profileGeneralStatsPanel );
		profileGeneralStatsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileShipStatsScroll = new JScrollPane( profileShipStatsPanel );
		profileShipStatsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		profileTabsPane.addTab( PROFILE_SHIP_UNLOCK, profileShipUnlockScroll );
		profileTabsPane.addTab( PROFILE_GENERAL_ACH, profileGeneralAchsScroll );
		profileTabsPane.addTab( PROFILE_GENERAL_STATS, profileGeneralStatsScroll );
		profileTabsPane.addTab( PROFILE_SHIP_STATS, profileShipStatsScroll );
		profileTabsPane.addTab( PROFILE_DUMP, profileDumpPanel );


		JPanel savedGamePane = new JPanel( new BorderLayout() );
		tasksPane.addTab( "Saved Game", savedGamePane );

		JToolBar savedGameToolbar = new JToolBar();
		setupSavedGameToolbar(savedGameToolbar);
		savedGamePane.add( savedGameToolbar, BorderLayout.NORTH );

		savedGameTabsPane = new JTabbedPane();
		savedGamePane.add( savedGameTabsPane, BorderLayout.CENTER );

		savedGameDumpPanel = new DumpPanel();
		savedGameGeneralPanel = new SavedGameGeneralPanel( this );
		savedGamePlayerFloorplanPanel = new SavedGameFloorplanPanel( this );
		savedGameNearbyFloorplanPanel = new SavedGameFloorplanPanel( this );
		savedGameHangarPanel = new SavedGameHangarPanel( this );
		savedGameSectorMapPanel = new SavedGameSectorMapPanel( this );
		savedGameSectorTreePanel = new SavedGameSectorTreePanel( this );
		savedGameStateVarsPanel = new SavedGameStateVarsPanel( this );

		JScrollPane savedGameGeneralScroll = new JScrollPane( savedGameGeneralPanel );
		savedGameGeneralScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane savedGameSectorTreeScroll = new JScrollPane( savedGameSectorTreePanel );
		savedGameSectorTreeScroll.getVerticalScrollBar().setUnitIncrement( 14 );
		savedGameSectorTreeScroll.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		savedGameTabsPane.addTab( SAVE_DUMP, savedGameDumpPanel );
		savedGameTabsPane.addTab( SAVE_GENERAL, savedGameGeneralScroll );
		savedGameTabsPane.addTab( SAVE_PLAYER_SHIP, savedGamePlayerFloorplanPanel );
		savedGameTabsPane.addTab( SAVE_NEARBY_SHIP, savedGameNearbyFloorplanPanel );
		savedGameTabsPane.addTab( SAVE_CHANGE_SHIP, savedGameHangarPanel );
		savedGameTabsPane.addTab( SAVE_SECTOR_MAP, savedGameSectorMapPanel );
		savedGameTabsPane.addTab( SAVE_SECTOR_TREE, savedGameSectorTreeScroll );
		savedGameTabsPane.addTab( SAVE_STATE_VARS, savedGameStateVarsPanel );

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout( new BoxLayout( statusPanel, BoxLayout.Y_AXIS ) );
		statusPanel.setBorder( BorderFactory.createLoweredBevelBorder() );
		statusLbl = new JLabel( " " );
		//statusLbl.setFont( statusLbl.getFont().deriveFont(Font.PLAIN) );
		statusLbl.setBorder( BorderFactory.createEmptyBorder( 2, 4, 2, 4 ) );
		statusLbl.setAlignmentX( Component.LEFT_ALIGNMENT );
		statusPanel.add( statusLbl );
		contentPane.add( statusPanel, BorderLayout.SOUTH );

		// Load blank profile (sets Kestrel unlock).
		stockProfile = Profile.createEmptyProfile();
		loadProfile( stockProfile );

		loadGameState( null );

		// Check for updates in a seperate thread.
		setStatusText( "Checking for updates..." );
		Thread t = new Thread( "CheckVersion" ) {
			@Override
			public void run() {
				checkForUpdate();
			}
		};
		t.setDaemon( true );
		t.start();
	}

	private void showErrorDialog( String message ) {
		JOptionPane.showMessageDialog( this, message, "Error", JOptionPane.ERROR_MESSAGE );
	}

	private void initCheckboxIcons() {
		InputStream stream = null;
		try {
			stream = DataManager.get().getResourceInputStream( "img/customizeUI/box_lock_on.png" );
			ImageUtilities.setLockImage( ImageIO.read( stream ) );
		}
		catch ( IOException e ) {
			log.error( "Eeading lock image failed" , e );
		}
		finally {
			try {if ( stream != null ) stream.close();}
			catch ( IOException e ) {}
		}
	}

	private void setupProfileToolbar( JToolBar toolbar ) {
		toolbar.setMargin( new Insets( 5, 5, 5, 5 ) );
		toolbar.setFloatable( false );

		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled( false );
		fc.addChoosableFileFilter( new FileFilter() {
			@Override
			public String getDescription() {
				return "FTL Profile (ae_prof.sav; prof.sav)";
			}
			@Override
			public boolean accept(File f) {
				if ( f.isDirectory() ) return true;
				if ( f.getName().equalsIgnoreCase( "ae_prof.sav" ) ) return true;
				if ( f.getName().equalsIgnoreCase( "prof.sav" ) ) return true;
				return false;
			}
		});

		final File candidateAEProfileFile = new File( FTLUtilities.findUserDataDir(), "ae_prof.sav" );
		final File candidateClassicProfileFile = new File( FTLUtilities.findUserDataDir(), "prof.sav" );
		final File userDataDir = FTLUtilities.findUserDataDir();
		if ( candidateAEProfileFile.exists() ) {
			fc.setSelectedFile( candidateAEProfileFile );
		}
		else if ( candidateClassicProfileFile.exists() ) {
			fc.setSelectedFile( candidateClassicProfileFile );
		}
		else {
			fc.setCurrentDirectory( userDataDir );
		}

		fc.setMultiSelectionEnabled( false );

		JButton profileOpenBtn = new JButton( "Open", openIcon );
		profileOpenBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				fc.setDialogTitle( "Open Profile" );
				int chooserResponse = fc.showOpenDialog( FTLFrame.this );
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					List<String> sillyNames = new ArrayList<String>();
					sillyNames.add( "continue.sav" );
					sillyNames.add( "data.dat" );
					sillyNames.add( "resource.dat" );

					if ( sillyNames.contains( chosenFile.getName() ) ) {
						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Profile tab, and you're opening \""+ chosenFile.getName() +"\" instead of \"ae_prof.sav\" or \"prof.sav\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileInputStream in = null;
					StringBuilder hexBuf = new StringBuilder();
					boolean hashFailed = false;
					Exception exception = null;

					try {
						log.info( "Opening profile: "+ chosenFile.getAbsolutePath() );

						in = new FileInputStream( chosenFile );

						// Hash whole file, then go back to the beginning.
						String readHash = PackUtilities.calcStreamMD5( in );
						in.getChannel().position( 0 );

						// Read the content in advance, in case an error ocurs.
						byte[] buf = new byte[4096];
						int len = 0;
						while ( (len = in.read(buf)) >= 0 ) {
							for ( int j=0; j < len; j++ ) {
								hexBuf.append( String.format( "%02x", buf[j] ) );
								if ( (j+1) % 32 == 0 ) {
									hexBuf.append( "\n" );
								}
							}
						}
						in.getChannel().position( 0 );

						// Parse file data.
						ProfileParser parser = new ProfileParser();
						Profile p = parser.readProfile( in );
						log.debug( "Profile read successfully." );

						Profile mockProfile = new Profile( p );
						FTLFrame.this.loadProfile( mockProfile );

						// Perform mock write.
						// The update() incidentally triggers load() of the modified profile.
						ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
						FTLFrame.this.updateProfile( mockProfile );
						parser.writeProfile( mockOut, mockProfile );
						mockOut.close();

						// Hash result.
						ByteArrayInputStream mockIn = new ByteArrayInputStream( mockOut.toByteArray() );
						String writeHash = PackUtilities.calcStreamMD5( mockIn );
						mockIn.close();

						// Compare hashes.
						if ( !writeHash.equals( readHash ) ) {
							log.error( "Hashes did not match after a mock write; editing may not be safe" );
							hashFailed = true;
						}

						// Reload the original unmodified profile.
						FTLFrame.this.loadProfile( p );
					}
					catch( Exception f ) {
						log.error( String.format( "Error reading profile (\"%s\").", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error reading profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
						exception = f;
					}
					finally {
						try {if ( in != null ) in.close();}
						catch ( IOException f ) {}
					}

					if ( hashFailed || exception != null ) {
						if ( hexBuf.length() > 0 ) {
							StringBuilder errBuf = new StringBuilder();

							if ( hashFailed && exception == null ) {
								errBuf.append( "Your profile loaded, but re-saving will not create an identical file.<br/>");
								errBuf.append( "You CAN technically proceed anyway, but there is risk of corruption.<br/>" );
							}
							else {
								errBuf.append( "Your profile could not be interpreted correctly.<br/>" );
							}

							errBuf.append( "<br/>" );
							errBuf.append( "To submit a bug report, you can use <a href='"+ bugReportUrl +"'>GitHub</a>.<br/>" );
							errBuf.append( "Or post to the FTL forums <a href='"+ forumThreadUrl +"'>here</a>.<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "On GitHub, set the issue title as \"Profile Parser Error\".<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "I will fix the problem and release a new version as soon as I can.<br/>" );
							errBuf.append( "<br/><br/>" );
							errBuf.append( "Copy (Ctrl-A, Ctrl-C) the following text, including \"[ code ] tags\"." );
							errBuf.append( "<br/><br/>" );

							StringBuilder reportBuf = new StringBuilder();
							reportBuf.append( "[code]\n" );
							reportBuf.append( "Profile Parser Error\n" );
							reportBuf.append( "\n" );

							if ( hashFailed ) {
								reportBuf.append( "Hashes did not match after a mock write.\n" );
								reportBuf.append( "\n" );
							}

							if ( exception != null ) {
								appendStackTrace( reportBuf, exception );
							}

							reportBuf.append( String.format( "Editor Version: %s\n", appVersion ) );
							reportBuf.append( String.format( "OS: %s %s\n", System.getProperty( "os.name" ), System.getProperty( "os.version" ) ) );
							reportBuf.append( String.format( "VM: %s, %s, %s\n", System.getProperty( "java.vm.name" ), System.getProperty( "java.version" ), System.getProperty( "os.arch" ) ) );
							reportBuf.append( "[/code]\n" );
							reportBuf.append( "\n" );
							reportBuf.append( String.format( "File (\"%s\")...\n", chosenFile.getName() ) );
							reportBuf.append( "[code]\n" );
							reportBuf.append( hexBuf );
							reportBuf.append( "\n[/code]\n" );

							JDialog failDialog = createBugReportDialog( "Profile Parser Error", errBuf.toString(), reportBuf.toString() );
							failDialog.setVisible( true );
						}
					}
				}
			}
		});
		profileOpenBtn.addMouseListener( new StatusbarMouseListener( this, "Open an existing profile." ) );
		toolbar.add( profileOpenBtn );

		profileSaveBtn = new JButton( "Save", saveIcon );
		profileSaveBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				if ( profile == stockProfile ) {
					int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting might be a mistake.\n\nThis is the blank default profile, which the editor uses for eye candy.\nNormally one would OPEN an existing profile first.\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
					if ( sillyResponse != JOptionPane.YES_OPTION ) return;

					fc.setSelectedFile( candidateClassicProfileFile );  // The stock profile is a "prof.sav".
				}

				fc.setDialogTitle( "Save Profile" );
				int chooserResponse = fc.showSaveDialog( FTLFrame.this );
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "continue.sav".equals( chosenFile.getName() ) ) {
						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Profile tab, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}

					if ( !sillyMistake && profile.getFileFormat() == 4 &&
					     "ae_prof.sav".equals( chosenFile.getName() ) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is NOT an AE profile, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}

					if ( !sillyMistake && profile.getFileFormat() == 9 &&
					     "prof.sav".equals( chosenFile.getName() ) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is an AE profile, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileOutputStream out = null;
					try {
						log.info( "Saving profile: "+ chosenFile.getAbsolutePath() );

						if ( chosenFile.exists() ) {
							String bakName = chosenFile.getName() +".bak";
							File bakFile = new File( chosenFile.getParentFile(), bakName );
							boolean bakValid = true;

							if ( bakFile.exists() ) {
								bakValid = bakFile.delete();
								if ( !bakValid ) log.warn( "Profile will be overwritten. Could not delete existing backup: "+ bakName );
							}
							if ( bakValid ) {
								bakValid = chosenFile.renameTo( bakFile );
								if ( !bakValid ) log.warn( "Profile will be overwritten. Could not rename existing file: "+ chosenFile.getName() );
							}

							if ( bakValid ) {
								log.info( "Profile was backed up: "+ bakName );
							}
						}

						out = new FileOutputStream( chosenFile );

						ProfileParser parser = new ProfileParser();
						FTLFrame.this.updateProfile( profile );
						parser.writeProfile( out, profile );
					}
					catch( IOException f ) {
						log.error( String.format( "Error saving profile (\"%s\")", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error saving profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
			}
		});
		profileSaveBtn.addMouseListener( new StatusbarMouseListener( this, "Save the current profile." ) );
		toolbar.add( profileSaveBtn );

		profileDumpBtn = new JButton( "Dump", saveIcon );
		profileDumpBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				if ( profile == stockProfile ) {
					int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting might be a mistake.\n\nThis is the blank default profile, which the editor uses for eye candy.\nNormally one would OPEN an existing profile first.\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
					if ( sillyResponse != JOptionPane.YES_OPTION ) return;

					fc.setSelectedFile( candidateClassicProfileFile );  // The stock profile is a "prof.sav".
				}

				JFileChooser dumpChooser = new JFileChooser();
				dumpChooser.setDialogTitle( "Dump Profile" );
				dumpChooser.setCurrentDirectory( fc.getCurrentDirectory() );
				dumpChooser.setFileHidingEnabled( false );

				ExtensionFileFilter txtFilter = new ExtensionFileFilter( "Text Files (*.txt)", new String[] {".txt"} );
				dumpChooser.addChoosableFileFilter( txtFilter );

				int chooserResponse = dumpChooser.showSaveDialog( FTLFrame.this );
				File chosenFile = dumpChooser.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( !chosenFile.exists() && dumpChooser.getFileFilter() == txtFilter && !txtFilter.accept( chosenFile ) ) {
						chosenFile = new File( chosenFile.getAbsolutePath() + txtFilter.getPrimarySuffix() );
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals( chosenFile.getName() ) ||
					     "prof.sav".equals( chosenFile.getName() ) ||
					     "continue.sav".equals( chosenFile.getName() ) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nYou're dumping a text summary called \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					BufferedWriter out = null;
					try {
						log.info( "Dumping profile: "+ chosenFile.getAbsolutePath() );

						out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( chosenFile ) ) );
						out.write( profile.toString() );
						out.close();
					}
					catch( IOException f ) {
						log.error( String.format( "Error dumping profile (\"%s\")", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error dumping profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
			}
		});
		profileDumpBtn.addMouseListener( new StatusbarMouseListener( this, "Dump unmodified profile info to a text file." ) );
		toolbar.add( profileDumpBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileUnlockShipsBtn = new JButton( "Unlock All Ships", unlockIcon );
		profileUnlockShipsBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.debug( "Unlock all ships button clicked" );
				profileShipUnlockPanel.unlockAllShips();
			}
		});
		profileUnlockShipsBtn.addMouseListener( new StatusbarMouseListener( this, "Unlock All Ships (except Type-B)." ) );
		toolbar.add( profileUnlockShipsBtn );


		JButton profileUnlockShipAchsBtn = new JButton( "Unlock All Ship Achievements", unlockIcon );
		profileUnlockShipAchsBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.debug( "Unlock all ship achievements button clicked" );
				profileShipUnlockPanel.unlockAllShipAchievements();
			}
		});
		profileUnlockShipAchsBtn.addMouseListener( new StatusbarMouseListener( this, "Unlock All Ship Achievements (and Type-B ships)." ) );
		toolbar.add( profileUnlockShipAchsBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileExtractBtn = new JButton( "Extract Dats", saveIcon );
		profileExtractBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.debug( "Extract button clicked" );

				JFileChooser extractChooser = new JFileChooser();
				extractChooser.setDialogTitle( "Choose a dir to extract into" );
				extractChooser.setFileHidingEnabled( false );
				extractChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				extractChooser.setMultiSelectionEnabled( false );

				if ( extractChooser.showSaveDialog( FTLFrame.this ) == JFileChooser.APPROVE_OPTION ) {
					try {
						File extractDir = extractChooser.getSelectedFile();

						JOptionPane.showMessageDialog( FTLFrame.this, "This may take a few seconds.\nClick OK to proceed.", "About to Extract", JOptionPane.PLAIN_MESSAGE );

						DataManager.get().extractResources( extractDir );

						JOptionPane.showMessageDialog( FTLFrame.this, "All dat content extracted successfully.", "Extraction Complete", JOptionPane.PLAIN_MESSAGE );
					}
					catch( IOException f ) {
						log.error( "Extracting dats failed", f );
						showErrorDialog( String.format( "Error extracting dats:\n%s: %s", f.getClass().getSimpleName(), f.getMessage() ) );
					}
				}
			}
		});
		profileExtractBtn.addMouseListener( new StatusbarMouseListener( this, "Extract dat content to a directory." ) );
		toolbar.add( profileExtractBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileAboutBtn = createAboutButton();
		toolbar.add( profileAboutBtn );

		JButton profileUpdatesBtn = createUpdatesButton();
		updatesButtonList.add( profileUpdatesBtn );
		toolbar.add( profileUpdatesBtn );
	}

	private void setupSavedGameToolbar( JToolBar toolbar ) {
		toolbar.setMargin( new Insets( 5, 5, 5, 5 ) );
		toolbar.setFloatable( false );

		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled( false );
		fc.addChoosableFileFilter( new FileFilter() {
			@Override
			public String getDescription() {
				return "FTL Saved Game (continue.sav)";
			}
			@Override
			public boolean accept( File f ) {
				return f.isDirectory() || f.getName().equalsIgnoreCase( "continue.sav" );
			}
		});

		File candidateSaveFile = new File( FTLUtilities.findUserDataDir(), "continue.sav" );
		if ( candidateSaveFile.exists() ) {
			fc.setSelectedFile( candidateSaveFile );
		} else {
			fc.setCurrentDirectory( FTLUtilities.findUserDataDir() );
		}

		fc.setMultiSelectionEnabled( false );

		JButton gameStateOpenBtn = new JButton( "Open", openIcon );
		gameStateOpenBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				fc.setDialogTitle( "Open Saved Game" );
				int chooserResponse = fc.showOpenDialog( FTLFrame.this );
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					List<String> sillyNames = new ArrayList<String>();
					sillyNames.add( "ae_prof.sav" );
					sillyNames.add( "prof.sav" );
					sillyNames.add( "data.dat" );
					sillyNames.add( "resource.dat" );

					if ( sillyNames.contains( chosenFile.getName() ) ) {
						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Saved Game tab, and you're opening \""+ chosenFile.getName() +"\" instead of \"continue.sav\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileInputStream in = null;
					StringBuilder hexBuf = new StringBuilder();
					Exception exception = null;

					try {
						log.info( "Opening game state: "+ chosenFile.getAbsolutePath() );

						in = new FileInputStream( chosenFile );

						// Read the content in advance, in case an error ocurs.
						byte[] buf = new byte[4096];
						int len = 0;
						while ( (len = in.read(buf)) >= 0 ) {
							for ( int j=0; j < len; j++ ) {
								hexBuf.append( String.format( "%02x", buf[j] ) );
								if ( (j+1) % 32 == 0 ) {
									hexBuf.append( "\n" );
								}
							}
						}
						in.getChannel().position( 0 );

						SavedGameParser parser = new SavedGameParser();
						SavedGameParser.SavedGameState gs = parser.readSavedGame( in );
						loadGameState( gs );

						log.debug( "Game state read successfully" );

						if ( gameState.getMysteryList().size() > 0 ) {
							StringBuilder musteryBuf = new StringBuilder();
							musteryBuf.append( "This saved game file contains mystery bytes the developers hadn't anticipated!\n" );
							boolean first = true;
							for ( MysteryBytes m : gameState.getMysteryList() ) {
								if ( first ) { first = false; }
								else { musteryBuf.append( ",\n" ); }
								musteryBuf.append( m.toString().replaceAll( "(^|\n)(.+)", "$1  $2") );
							}
							log.warn( musteryBuf.toString() );
						}
					}
					catch( Exception f ) {
						log.error( String.format( "Reading game state (\"%s\") failed", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error reading game state (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
						exception = f;
					}
					finally {
						try {if ( in != null ) in.close();}
						catch ( IOException f ) {}
					}

					if ( exception != null ) {
						if ( hexBuf.length() > 0 ) {
							StringBuilder errBuf = new StringBuilder();
							errBuf.append( "Your saved game could not be interpreted correctly.<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "To submit a bug report, you can use <a href='"+ bugReportUrl +"'>GitHub</a>.<br/>");
							errBuf.append( "Or post to the FTL forums <a href='"+ forumThreadUrl +"'>here</a>.<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "On GitHub, set the issue title as \"SavedGame Parser Error\".<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "I will fix the problem and release a new version as soon as I can.<br/>" );
							errBuf.append( "<br/><br/>" );
							errBuf.append( "Copy (Ctrl-A, Ctrl-C) the following text, including \"[ code ] tags\"." );
							errBuf.append( "<br/><br/>" );

							StringBuilder reportBuf = new StringBuilder();
							reportBuf.append( "[code]\n" );
							reportBuf.append( "SavedGame Parser Error\n" );
							reportBuf.append( "\n" );

							if ( exception != null ) {
								appendStackTrace( reportBuf, exception );
							}

							reportBuf.append( String.format( "Editor Version: %s\n", appVersion ) );
							reportBuf.append( String.format( "OS: %s %s\n", System.getProperty( "os.name" ), System.getProperty( "os.version" ) ) );
							reportBuf.append( String.format( "VM: %s, %s, %s\n", System.getProperty( "java.vm.name" ), System.getProperty( "java.version" ), System.getProperty( "os.arch" ) ) );
							reportBuf.append( "[/code]\n" );
							reportBuf.append( "\n" );
							reportBuf.append( String.format( "File (\"%s\")...\n", chosenFile.getName() ) );
							reportBuf.append( "[code]\n" );
							reportBuf.append( hexBuf );
							reportBuf.append( "\n[/code]\n" );

							JDialog failDialog = createBugReportDialog( "SavedGame Parser Error", errBuf.toString(), reportBuf.toString() );
							failDialog.setVisible( true );
						}
					}
				}
			}
		});
		gameStateOpenBtn.addMouseListener( new StatusbarMouseListener( this, "Open an existing saved game." ) );
		toolbar.add( gameStateOpenBtn );

		gameStateSaveBtn = new JButton( "Save", saveIcon );
		gameStateSaveBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				if ( gameState == null ) return;

				if ( gameState.getMysteryList().size() > 0 )
					log.warn( "The original saved game file contained mystery bytes, which will be omitted in the new file." );

				fc.setDialogTitle( "Save Game State" );
				int chooserResponse = fc.showSaveDialog( FTLFrame.this );
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals( chosenFile.getName() ) ||
					     "prof.sav".equals( chosenFile.getName() ) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Saved Game tab, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileOutputStream out = null;
					try {
						log.info( "Saving game state: "+ chosenFile.getAbsolutePath() );

						if ( chosenFile.exists() ) {
							String bakName = chosenFile.getName() +".bak";
							File bakFile = new File( chosenFile.getParentFile(), bakName );
							boolean bakValid = true;

							if ( bakFile.exists() ) {
								bakValid = bakFile.delete();
								if ( !bakValid ) log.warn( "Saved game will be overwritten. Could not delete existing backup: "+ bakName );
							}
							if ( bakValid ) {
								bakValid = chosenFile.renameTo( bakFile );
								if ( !bakValid ) log.warn( "Saved game will be overwritten. Could not rename existing file: "+ chosenFile.getName() );
							}

							if ( bakValid ) {
								log.info( "Saved game was backed up: "+ bakName );
							}
						}

						out = new FileOutputStream( chosenFile );

						SavedGameParser parser = new SavedGameParser();
						FTLFrame.this.updateGameState(gameState);
						parser.writeSavedGame( out, gameState );
					}
					catch( IOException f ) {
						log.error( String.format( "Error saving game state (\"%s\").", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error saving game state (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
			}
		});
		gameStateSaveBtn.addMouseListener( new StatusbarMouseListener( this, "Save the current game state." ) );
		toolbar.add( gameStateSaveBtn );

		gameStateDumpBtn = new JButton( "Dump", saveIcon );
		gameStateDumpBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				if ( gameState == null ) return;

				JFileChooser dumpChooser = new JFileChooser();
				dumpChooser.setDialogTitle( "Dump Game State" );
				dumpChooser.setCurrentDirectory( fc.getCurrentDirectory() );
				dumpChooser.setFileHidingEnabled( false );

				ExtensionFileFilter txtFilter = new ExtensionFileFilter( "Text Files (*.txt)", new String[] {".txt"} );
				dumpChooser.addChoosableFileFilter( txtFilter );

				int chooserResponse = dumpChooser.showSaveDialog( FTLFrame.this );
				File chosenFile = dumpChooser.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( !chosenFile.exists() && dumpChooser.getFileFilter() == txtFilter && !txtFilter.accept( chosenFile ) ) {
						chosenFile = new File( chosenFile.getAbsolutePath() + txtFilter.getPrimarySuffix() );
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals( chosenFile.getName() ) ||
					     "prof.sav".equals( chosenFile.getName() ) ||
					     "continue.sav".equals( chosenFile.getName() ) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nYou're dumping a text summary called \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					BufferedWriter out = null;
					try {
						log.info( "Dumping game state: "+ chosenFile.getAbsolutePath() );

						out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( chosenFile ) ) );
						out.write( gameState.toString() );
						out.close();
					}
					catch( IOException f ) {
						log.error( String.format( "Error dumping game state (\"%s\").", chosenFile.getName() ), f );
						showErrorDialog( String.format( "Error dumping game state (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage() ) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
			}
		});
		gameStateDumpBtn.addMouseListener( new StatusbarMouseListener( this, "Dump unmodified game state info to a text file." ) );
		toolbar.add( gameStateDumpBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton gameStateAboutBtn = createAboutButton();
		toolbar.add( gameStateAboutBtn );

		JButton gameStateUpdatesBtn = createUpdatesButton();
		updatesButtonList.add( gameStateUpdatesBtn );
		toolbar.add( gameStateUpdatesBtn );
	}

	public JButton createAboutButton() {
		JButton aboutButton = new JButton( "About", aboutIcon );
		aboutButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				showAboutDialog();
			}
		});
		aboutButton.addMouseListener( new StatusbarMouseListener( this, "View information about this tool and links for information/bug reports" ) );
		return aboutButton;
	}

	public JButton createUpdatesButton() {
		JButton updatesButton = new JButton( "Updates" );
		updatesButton.setEnabled( false );
		updatesButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( updatesCallback != null )
					updatesCallback.run();
			}
		});
		updatesButton.addMouseListener( new StatusbarMouseListener( this, "Update this tool or review past changes." ) );
		return updatesButton;
	}

	/**
	 * Returns string content of a bundled resource url, decoded as UTF-8.
	 */
	private String readResourceText( URL url ) throws IOException {
		BufferedReader in = null;
		String line = null;
		try {
			StringBuilder buf = new StringBuilder();
			in = new BufferedReader( new InputStreamReader( (InputStream)url.getContent(), "UTF-8" ) );
			while ( (line = in.readLine()) != null ) {
				buf.append( line ).append( "\n" );
			}
			in.close();

			return buf.toString();
		}
		finally {
			try {if ( in != null ) in.close();}
			catch( IOException e ) {}
		}
	}

	// TODO: May need to reimplement the http client with nio.
	// System.exit() interrupts daemon threads, but blocked sockets need explicit close().
	// Java's built-in URLConnections don't expose sockets to allow that.

	/**
	 * Returns an InputStream to read an HTTP URL.
	 */
	private InputStream fetchURL( URL url ) throws SocketTimeoutException, UnknownHostException, IOException {
		HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
		httpConn.setConnectTimeout( 5000 );
		httpConn.setReadTimeout( 10000 );
		httpConn.connect();

		int responseCode = httpConn.getResponseCode();
		if ( responseCode != HttpURLConnection.HTTP_OK ) {
			throw new IOException( String.format( "Download request failed for \"%s\": HTTP Code %d (%s)", httpConn.getURL(), responseCode, httpConn.getResponseMessage() ) );
		}

		return httpConn.getInputStream();
	}

	private void checkForUpdate() {

		InputStream is = null;
		BufferedReader in = null;
		try {
			log.debug( "Checking for latest version" );

			is = fetchURL( new URL( latestVersionUrl ) );

			in = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
			int latestVersion = Integer.parseInt( in.readLine() );
			in.close();

			String releaseTemplate;
			int minHistory;
			final String dialogTitle;
			final Color updatesBtnColor;
			final ImageIcon updatesBtnIcon;
			final String statusMessage;

			if ( latestVersion > appVersion ) {
				log.debug( "New version available" );

				releaseTemplate = readResourceText( latestVersionTemplateURL );
				minHistory = appVersion;
				dialogTitle = "Update Available";
				updatesBtnColor = new Color( 0xff, 0xaa, 0xaa );
				updatesBtnIcon = updateIcon;
				statusMessage = "A new version has been released.";
			}
			else {
				log.debug( "Already up-to-date" );

				releaseTemplate = readResourceText( releaseNotesTemplateURL );
				minHistory = 0;
				dialogTitle = "Release Notes";
				updatesBtnColor = UIManager.getColor( "Button.background" );
				updatesBtnIcon = releaseNotesIcon;
				statusMessage = "No new updates.";
			}

			final String historyHtml = getVersionHistoryHtml( releaseTemplate, minHistory );

			final Runnable newCallback = new Runnable() {
				@Override
				public void run() {
					JDialog updatesDlg = new JDialog( FTLFrame.this, dialogTitle, true );

					JEditorPane editor = new JEditorPane( "text/html", historyHtml );
					editor.setEditable( false );
					editor.setCaretPosition( 0 );
					editor.setTransferHandler( new HTMLEditorTransferHandler() );
					editor.addHyperlinkListener( linkListener );
					updatesDlg.setContentPane( new JScrollPane( editor ) );

					//updatesDlg.pack();
					updatesDlg.setSize( 600, 450 );
					updatesDlg.setLocationRelativeTo( FTLFrame.this );
					updatesDlg.setVisible( true );
				}
			};

			// Make changes from the GUI thread.
			Runnable r = new Runnable() {
				@Override
				public void run() {
					updatesCallback = newCallback;
					for ( JButton updatesBtn : updatesButtonList ) {
						updatesBtn.setBackground( updatesBtnColor );
						updatesBtn.setIcon( updatesBtnIcon );
						updatesBtn.setEnabled( true );
					}
					setStatusText( statusMessage );
				}
			};
			SwingUtilities.invokeLater( r );

		}
		catch ( Exception e ) {
			log.error( "Checking for latest version failed", e );
			showErrorDialog( "Error checking for latest version.\n(Use the About window to check the download page manually)\n"+ e.toString() );
		}
		finally {
			try {if ( in != null ) in.close();}
			catch ( IOException e ) {}

			try {if ( is != null ) is.close();}
			catch ( IOException e ) {}
		}
	}

	/**
	 * Returns a Map of versions (in descending order) and their lists of changes.
	 */
	private Map<Integer, List<String>> fetchVersionHistory() throws IOException {
		Map<Integer, List<String>> historyMap = new LinkedHashMap<Integer, List<String>>();

		InputStream is = null;
		BufferedReader in = null;
		String line = null;
		try {
			is = fetchURL( new URL( versionHistoryUrl ) );
			in = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );

			while ( (line = in.readLine()) != null ) {
				int releaseVersion = Integer.parseInt( line );
				List<String> releaseChangeList = new ArrayList<String>();
				historyMap.put(releaseVersion, releaseChangeList );

				while ( (line = in.readLine()) != null && !line.equals( "" ) ) {
					releaseChangeList.add( line );
				}

				// Must've either hit a blank or done.
			}
			in.close();

			return historyMap;
		}
		finally {
			try {if ( in != null ) in.close();}
			catch ( IOException e ) {}

			try {if ( is != null ) is.close();}
			catch ( IOException e ) {}
		}
	}

	/**
	 * Returns an HTML summary of changes since a given version.
	 *
	 * @see #fetchVersionHistory()
	 */
	private String getVersionHistoryHtml( String releaseTemplate, int sinceVersion ) throws IOException {

		// Buffer for presentation-ready html.
		StringBuilder historyBuf = new StringBuilder();

		// Fetch the changelog.
		Map<Integer, List<String>> historyMap = fetchVersionHistory();

		StringBuilder releaseBuf = new StringBuilder();

		for ( Map.Entry<Integer, List<String>> releaseEntry : historyMap.entrySet() ) {
			if (releaseEntry.getKey() <= sinceVersion ) break;

			releaseBuf.setLength( 0 );

			for ( String change : releaseEntry.getValue() ) {
				releaseBuf.append( "<li>" ).append( change ).append( "</li>\n" );
			}

			if ( releaseBuf.length() > 0 ) {
				Map<String, String> placeholderMap = new HashMap<String, String>();
				placeholderMap.put( "{version}", String.format( "v%s", releaseEntry.getKey() ) );
				placeholderMap.put( "{items}", releaseBuf.toString() );

				String releaseDesc = releaseTemplate;
				for ( Map.Entry<String, String> dictEntry : placeholderMap.entrySet() ) {
					releaseDesc = releaseDesc.replaceAll( Pattern.quote( dictEntry.getKey() ), Matcher.quoteReplacement( dictEntry.getValue() ) );
				}
				historyBuf.append( releaseDesc );
			}
		}

		return historyBuf.toString();
	}

	/**
	 * Formats an exception, appending lines to a bug report buffer.
	 */
	private void appendStackTrace( StringBuilder reportBuf, Throwable exception ) {
		reportBuf.append( String.format( "Exception: %s\n", exception.toString() ) );
		reportBuf.append( "\n" );

		reportBuf.append( "Stack Trace...\n" );
		StackTraceElement[] traceElements = exception.getStackTrace();
		int traceDepth = 5;
		for ( int i=0; i < traceDepth && i < traceElements.length; i++ ) {
			reportBuf.append( String.format( "  %s\n", traceElements[i].toString() ) );
		}
/*
		Throwable currentCause = exception;

		// Traditional loggers truncate each cause's trace when a line is
		// already mentioned in the next downstream exception, i.e.,
		// remaining lines are redundant.

		while ( currentCause.getCause() != currentCause && null != (currentCause=currentCause.getCause()) ) {
			reportBuf.append( String.format( "Caused by: %s\n", currentCause.toString() ) );
			StackTraceElement[] causeElements = currentCause.getStackTrace();
			int causeDepth = 3;
			for ( int i=0; i < causeDepth && i < causeElements.length; i++ ) {
				reportBuf.append( String.format( "  %s\n", causeElements[i].toString() ) );
			}
		}
*/
		reportBuf.append( "\n" );
	}

	private JDialog createBugReportDialog( String title, String message, String report ) {

		JDialog dlg = new JDialog( this, title, true );
		JPanel panel = new JPanel( new BorderLayout() );

		Font reportFont = new Font( Font.MONOSPACED, Font.PLAIN, 13 );

		JEditorPane messageEditor = new JEditorPane( "text/html", message );
		messageEditor.setEditable( false );
		messageEditor.setCaretPosition( 0 );
		messageEditor.addHyperlinkListener( linkListener );
		messageEditor.setTransferHandler( new HTMLEditorTransferHandler() );
		panel.add( new JScrollPane( messageEditor ), BorderLayout.NORTH );

		JTextArea reportArea = new JTextArea( report );
		reportArea.setTabSize( 4 );
		reportArea.setFont( reportFont );
		reportArea.setEditable( false );
		reportArea.setCaretPosition( 0 );
		panel.add( new JScrollPane( reportArea ), BorderLayout.CENTER );

		dlg.setContentPane( panel );
		dlg.setSize( 600, 450 );
		dlg.setLocationRelativeTo( this );

		return dlg;
	}

	private void showAboutDialog() {
		try {
			JDialog aboutDlg = new JDialog( FTLFrame.this, "About", true );

			JEditorPane editor = new JEditorPane( aboutPageURL );
			editor.setEditable( false );
			editor.setMargin( new Insets( 0, 48, 25, 48 ) );
			editor.addHyperlinkListener( linkListener );
			aboutDlg.setContentPane( editor );

			//aboutDlg.setSize( 300, 320 );
			aboutDlg.pack();
			aboutDlg.setLocationRelativeTo( FTLFrame.this );
			aboutDlg.setVisible( true );
		}
		catch( IOException f ) {
			log.error( "Failed to show the about dialog", f );
		}
	}

	public void loadProfile( Profile p ) {
		try {
			profileShipUnlockPanel.setProfile( p );
			profileGeneralAchsPanel.setProfile( p );
			profileGeneralStatsPanel.setProfile( p );
			profileShipStatsPanel.setProfile( p );
			profileDumpPanel.setText( (p != null ? p.toString() : "") );

			profileSaveBtn.setEnabled( (p != null) );
			profileDumpBtn.setEnabled( (p != null) );

			profile = p;
		}
		catch ( IOException e ) {
			if ( profile != null && profile != p ) {
				log.info( "Attempting to revert GUI to the previous profile..." );
				showErrorDialog( "Error loading profile.\nAttempting to return to the previous profile..." );
				loadProfile( profile );
			} else {
				showErrorDialog( "Error loading profile.\nThis has left the GUI in an ambiguous state.\nSaving is not recommended until another profile has successfully loaded." );
			}
		}

		this.repaint();
	}

	public void updateProfile( Profile p ) {

		if ( p == null ) {
		}
		else if ( p.getFileFormat() == 4 || p.getFileFormat() == 9 ) {
			profileShipUnlockPanel.updateProfile( p );
			profileGeneralAchsPanel.updateProfile( p );
			profileGeneralStatsPanel.updateProfile( p );
			profileShipStatsPanel.updateProfile( p );
			// profileDumpPanel doesn't modify anything.
		}

		loadProfile( p );
	}

	/**
	 * Returns the currently loaded game state.
	 *
	 * This method should only be called when a panel
	 * needs to pull the state, make a major change,
	 * and reload it.
	 */
	public SavedGameParser.SavedGameState getGameState() {
		return gameState;
	}

	public void loadGameState( SavedGameParser.SavedGameState gs ) {

		if ( gs == null ) {
			savedGameDumpPanel.setText( "" );
			savedGameGeneralPanel.setGameState( null );
			savedGamePlayerFloorplanPanel.setShipState( null, null );
			savedGameNearbyFloorplanPanel.setShipState( null, null );
			savedGameHangarPanel.setGameState( null );
			savedGameSectorMapPanel.setGameState( null );
			savedGameSectorTreePanel.setGameState( null );
			savedGameStateVarsPanel.setGameState( null );

			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_GENERAL ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_PLAYER_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_NEARBY_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_CHANGE_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_MAP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_TREE ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_STATE_VARS ), false );
			savedGameTabsPane.setSelectedIndex( savedGameTabsPane.indexOfTab( SAVE_DUMP ) );
			gameStateSaveBtn.setEnabled( false );
			gameStateDumpBtn.setEnabled( false );

			gameState = null;
		}
		else if ( Arrays.binarySearch( new int[] {2, 7, 8, 9}, gs.getFileFormat() ) >= 0 ) {
			savedGameDumpPanel.setText( gs.toString() );
			savedGameGeneralPanel.setGameState( gs );
			savedGamePlayerFloorplanPanel.setShipState( gs, gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.setShipState( gs, gs.getNearbyShipState() );
			savedGameHangarPanel.setGameState( gs );
			savedGameSectorMapPanel.setGameState( gs );
			savedGameSectorTreePanel.setGameState( gs );
			savedGameStateVarsPanel.setGameState( gs );

			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_GENERAL ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_PLAYER_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_NEARBY_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_CHANGE_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_MAP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_TREE ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_STATE_VARS ), true );
			savedGameTabsPane.setSelectedIndex( savedGameTabsPane.indexOfTab( SAVE_DUMP ) );
			gameStateSaveBtn.setEnabled( true );
			gameStateDumpBtn.setEnabled( true );

			gameState = gs;
		}
		else {
			log.error( "Unsupported game state fileFormat: "+ gs.getFileFormat() );
			showErrorDialog( "Unsupported game state fileFormat: "+ gs.getFileFormat() );

			loadGameState( null );
		}
	}

	public void updateGameState( SavedGameParser.SavedGameState gs ) {

		if ( gs == null ) {
		}
		else if ( Arrays.binarySearch( new int[] {2, 7, 8, 9}, gs.getFileFormat() ) >= 0 ) {
			// savedGameDumpPanel doesn't modify anything.
			savedGameGeneralPanel.updateGameState( gs );
			savedGamePlayerFloorplanPanel.updateShipState( gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.updateShipState( gs.getNearbyShipState() );
			// savedGameHangarPanel doesn't modify anything.
			savedGameSectorMapPanel.updateGameState( gs );
			savedGameSectorTreePanel.updateGameState( gs );
			savedGameStateVarsPanel.updateGameState( gs );

			// Sync session's redundant ship info with player ship.
			gs.setPlayerShipName( gs.getPlayerShipState().getShipName() );
			gs.setPlayerShipBlueprintId( gs.getPlayerShipState().getShipBlueprintId() );
		}

		loadGameState( gs );
	}

	@Override
	public void setStatusText( String text ) {
		if ( text.length() > 0 ) {
			statusLbl.setText( text );
		} else {
			statusLbl.setText( " " );
		}
	}
}
