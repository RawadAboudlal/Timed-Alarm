package application;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class AppController {
	
	private static final String DEFAULT_SLEEP_TIME = "10:00";
	
	private static final String[] SOUND_FILE_EXTENSIONS = {
			"*.wav"
	};
	
	private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(1);
	
	@FXML private Label countdownTimer;
	@FXML private TextField textFieldAlarmTime;
	@FXML private Label labelSoundFile;
	@FXML private Button buttonChooseFile;
	
	private FileChooser fileChooser;
	
	private TextFormatter<Long> textFormatter;
	
	private File soundFile;
	
	private Clip clip;
	
	private long requestedSleepTime;
	private long currentSleptTime;
	
	private boolean loadSoundRequested;
	private boolean playSoundRequested;
	
	public AppController() {
		setLoadSoundRequested(false);
		setPlaySoundRequested(false);
	}
	
	@FXML
	public void initialize() {
		
		Timer countdown = new Timer("Countdown Thread", true);
		countdown.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				
				long passedTime = SLEEP_TIME;
				
				if(requestedSleepTime > 0) {
					
					if(passedTime <= currentSleptTime) {
						currentSleptTime -= passedTime;
					} else {
						currentSleptTime = 0;
					}
					
					final long finalSleptTime = currentSleptTime;
					
					Platform.runLater(() -> {
						countdownTimer.setText(textFormatter.getValueConverter().toString(finalSleptTime));
					});
					
					if(currentSleptTime <= 0) {
						currentSleptTime = requestedSleepTime;
						
						setPlaySoundRequested(true);
						
					}
					
				}
					
			}
		}, 0, SLEEP_TIME);
		
		Thread soundThread = new Thread(() -> {
			while(true) {
				
				if(isLoadSoundRequested()) {
					
					try {
						System.out.println("Loading sound...");
						AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
						clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						System.out.println("Loaded sound successfully.");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					setLoadSoundRequested(false);
					
				}
				
				if(isPlaySoundRequested()) {
					
					if(clip != null) {
						System.out.println("Playing sound...");
						if(clip.isRunning()) clip.stop();
						clip.setFramePosition(0);
						clip.start();
					}
					
					setPlaySoundRequested(false);
					
				}
				
			}
		}, "Sound Thread");
		soundThread.setDaemon(true);
		soundThread.start();
		
		textFormatter = new TextFormatter<Long>(new StringConverter<Long>() {
			
			@Override
			public Long fromString(String string) {
				
				String[] tokens = string.split(":");
				
				long minutes = 0;
				long seconds = 0;
				
				try {
					minutes = Long.parseLong(tokens[0]);
				} catch(Exception ex) {
					System.out.println("Invalid minutes.");
				}
				
				try {
					seconds = Long.parseLong(tokens[1]);
				} catch(Exception ex) {
					System.out.println("Invalid seconds.");
					seconds = minutes;// Only a single number entered, assume seconds.
					minutes = 0;
				}
				
				return TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);
				
			}
			
			@Override
			public String toString(Long timeInMillis) {
				
				if(timeInMillis == null) return DEFAULT_SLEEP_TIME;
				
				long timeInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis);
				
				long minutes = timeInSeconds / TimeUnit.MINUTES.toSeconds(1);
				long seconds = timeInSeconds % TimeUnit.MINUTES.toSeconds(1);
				
				return minutes + ":" + seconds;
				
			}
			
		});
		
		textFieldAlarmTime.setTextFormatter(textFormatter);
		
		textFieldAlarmTime.setOnAction(e -> {
			setSleepTime(textFieldAlarmTime.getText());
		});
		
		textFieldAlarmTime.setText(DEFAULT_SLEEP_TIME);
		setSleepTime(DEFAULT_SLEEP_TIME);
		
		countdownTimer.setText(DEFAULT_SLEEP_TIME);
		
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Sound File", SOUND_FILE_EXTENSIONS));
		
		soundFile = new File("res/defaultSound.wav");
		labelSoundFile.setText(this.soundFile.getName());
		
		setLoadSoundRequested(true);
		
		buttonChooseFile.setOnAction(e -> {
			
			fileChooser.setInitialDirectory(soundFile.isDirectory()? soundFile:soundFile.getParentFile());
			fileChooser.setInitialFileName(soundFile.getName());
			File soundFile = fileChooser.showOpenDialog(buttonChooseFile.getScene().getWindow());
			
			if(soundFile != null) {
				
				this.soundFile = soundFile;
				
				setLoadSoundRequested(true);
				
			}
			
			labelSoundFile.setText(this.soundFile.getName());
			
		});
		
	}
	
	private void setSleepTime(String sleepTime) {
		requestedSleepTime = textFormatter.getValueConverter().fromString(sleepTime);
		currentSleptTime = requestedSleepTime;
	}
	
	public synchronized void setLoadSoundRequested(boolean loadSoundRequested) {
		this.loadSoundRequested = loadSoundRequested;
	}
	
	public synchronized boolean isLoadSoundRequested() {
		return loadSoundRequested;
	}
	
	public synchronized void setPlaySoundRequested(boolean playSoundRequested) {
		this.playSoundRequested = playSoundRequested;
	}
	
	public synchronized boolean isPlaySoundRequested() {
		return playSoundRequested;
	}
	
}
