package games.stendhal.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Checks if all sound files can be played on the current system. For example
 * some sounds can only be played on MS Windows but not on Linux based systems.
 * 
 * @author mtotz
 */
public class CheckSounds {
	private static final boolean TESTPLAY_SAMPLES = false;

	private static class TestLineListener implements LineListener {
		public boolean active = true;

		public void update(LineEvent event) {
			if (event.getType() == LineEvent.Type.STOP) {
				active = false;
			}
		}
	}

	private static String getString(String s, int width, char c) {
		while (s.length() < width) {
			s += c;
		}
		return s;
	}

	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		loadSoundProperties(prop);

		Map<String, AudioFormat> formatMap = new TreeMap<String, AudioFormat>();
		Map<String, String> fileFormatMap = new TreeMap<String, String>();
		Mixer defaultMixer = AudioSystem.getMixer(null);

		// get sound library filepath
		String soundBase = prop.getProperty("soundbase", "data/sounds");

		// read all load-permitted sounds listed in properties
		// from soundfile into cache map
		for (Entry<String, String> entry : ((Map<String, String>) (Map) prop).entrySet()) {
			if (isValidEntry(entry.getKey(), entry.getValue())) {
				String name = entry.getKey().substring(4);
				String filename = entry.getValue();
				int pos = filename.indexOf(',');
				if (pos > -1) {
					filename = filename.substring(0, pos);
				}

				try {
					InputStream is = CheckSounds.class.getClassLoader().getResourceAsStream(soundBase + "/" + filename);
					AudioInputStream ais = AudioSystem.getAudioInputStream(is);
					AudioFormat format = ais.getFormat();
					String formatString = format.toString();
	
					if (TESTPLAY_SAMPLES) {
						// testplay the sound
						DataLine.Info info = new DataLine.Info(Clip.class, format);
						if (defaultMixer.isLineSupported(info)) {
							AudioInputStream playStream = ais;
							AudioFormat defaultFormat = new AudioFormat(format.getSampleRate(), 16, 1, false, true);
							if (AudioSystem.isConversionSupported(defaultFormat, format)) {
								playStream = AudioSystem.getAudioInputStream(defaultFormat, ais);
							} else {
								System.out.println("conversion not supported (to " + defaultFormat + ")");
							}
	
							System.out.println("testplaying " + name + " " + playStream.getFormat());
	
							Clip line = (Clip) defaultMixer.getLine(info);
							line.open(playStream);
							line.loop(2);
							TestLineListener testListener = new TestLineListener();
							line.addLineListener(testListener);
							while (testListener.active) {
								Thread.yield();
							}
							line.close();
						}
					}
	
					fileFormatMap.put(name, formatString);
					if (!formatMap.containsKey(formatString)) {
						formatMap.put(formatString, format);
					}
				} catch (UnsupportedAudioFileException e) {
					System.out.println(name + " cannot be read, the file format is not supported");
				}
			}
		}

		Mixer.Info[] mixerList = AudioSystem.getMixerInfo();
		int[] width = new int[mixerList.length];

		System.out.println("\n\n--- Result ---\n");
		System.out.println("installed mixer: ");
		for (int i = 0; i < mixerList.length; i++) {
			Mixer.Info mixer = mixerList[i];
			width[i] = Math.max(mixer.getName().length(), "unsupported".length());
			System.out.println(mixer.getName() + " - " + mixer.getDescription());
		}
		System.out.println("Default: " + AudioSystem.getMixer(null).getMixerInfo().getName());
		System.out.println("\n");

		System.out.println(formatMap.size()+ " audio formats\nThe maximum available lines for the format is in brackets.");
		for (int i = 0; i < mixerList.length; i++) {
			System.out.print(getString(mixerList[i].getName(), width[i], ' ') + " | ");
		}
		System.out.println("Format");
		for (int i = 0; i < mixerList.length; i++) {
			System.out.print(getString("", width[i], '-') + "-+-");
		}
		System.out.println("---------------------");

		for (String key : formatMap.keySet()) {
			DataLine.Info info = new DataLine.Info(Clip.class, formatMap
					.get(key));
			for (int i = 0; i < mixerList.length; i++) {
				Mixer mixer = AudioSystem.getMixer(mixerList[i]);
				boolean supported = mixer.isLineSupported(info);
				System.out.print(getString((supported ? "  " : "un")
						+ "supported (" + mixer.getMaxLines(info) + ")",
						width[i], ' ')
						+ " | ");
			}

			System.out.print(key);
			// line not supported by any mixer
			String files = "";
			for (String file : fileFormatMap.keySet()) {
				if (key.equals(fileFormatMap.get(file))) {
					files += " " + file;
				}
			}
			System.out.print(" (files: " + files + ")");

			System.out.println();
		}
		System.out.println("done");
	}

	// ------------------------------------------------------------------------ 
	//              TODO: clean up this copied code from SoundSystem
	// ------------------------------------------------------------------------ 
	
	/** expected location of the sound definition file (classloader). */
	private static final String STORE_PROPERTYFILE = "data/sounds/stensounds.properties";

	/**
	 * @param prop
	 *            the Property Object to load to
	 * @throws IOException
	 */
	private static void loadSoundProperties(Properties prop) throws IOException {
		InputStream in1;

		in1 = CheckSounds.class.getClassLoader().getResourceAsStream(STORE_PROPERTYFILE);
		prop.load(in1);
		in1.close();
	}

	/**
	 * A key/value pair is assumed valid if 	
	 * <ul>
	 *    <li>key starts with "sfx." <b>and </b></li>
	 *    <li>key does not end with ",x"</li>
	 *    <li>or value contains a "."</li>
	 * </ul>
	 * @param key
	 * @param value
	 * @return true, if it is valid, false otherwise
	 */
	private static boolean isValidEntry(String key, String value) {
		boolean load;
		int pos1;
		if (key.startsWith("sfx.")) {
			if ((pos1 = value.indexOf(',')) > -1) {
				load = value.substring(pos1 + 1).charAt(0) != 'x';
			} else {
				load = true;
			}
			load |= value.indexOf('.') != -1;
			return load;
		} else {
			return false;
		}
	}
	// ------------------------------------------------------------------------ 
	//              copied code end
	// ------------------------------------------------------------------------ 
}
