package chart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ChartParser {
	public ArrayList<BPMEvent> bpmEvents = new ArrayList<BPMEvent>();
	public ArrayList<Note> notes = new ArrayList<Note>();
	
	public String name = "";

	int resolution = 192;
	
	public void getName(String chartPath) {
		String[] chart = null;
		try {
			chart = loadStrings(chartPath + "\\notes.chart");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean inInfo = false;

		for (int i = 0; i < chart.length; i++) {
			if (chart[i].trim().contains("[Song]")) {
				inInfo = true;
				i += 2;
			}
			
			if (chart[i].equals("}")) {
				return;
			}
			
			if (inInfo) {
				String[] split = chart[i].trim().split(" ");
				String type = split[0].toLowerCase();

				if (type.equals("name")) {
					name = chart[i].split(" = ")[1].replace("\"", "");
				}
			}
		}
	}
	
	public void parse(String chartPath, float speed) {
		String[] chart = null;
		try {
			chart = loadStrings(chartPath + "\\notes.chart");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean inSync = false;
		boolean inNotes = false;
		boolean inInfo = false;

		int lastTick = 0;
		Note lastNote = new Note(0, new boolean[] {}, 0);
		for (int i = 0; i < chart.length; i++) {
			if (chart[i].trim().contains("[SyncTrack]")) {
				inSync = true;
				i += 2;
			}
			if (chart[i].trim().contains("[ExpertSingle]")) {
				inNotes = true;
				i += 2;
			}
			if (chart[i].trim().contains("[Song]")) {
				inInfo = true;
				i += 2;
			}
			
			if (chart[i].equals("}")) {
				inSync = false;
				inNotes = false;
				inInfo = false;
			}

			if (inSync) {
				String[] split = chart[i].trim().split(" ");
				int tick = Integer.parseInt(split[0]);
				String type = split[2];

				if (type.equals("B")) {
					float bpm = ((float) Integer.parseInt(split[3]) * 0.0009999f) * (speed / 100f);

					bpmEvents.add(new BPMEvent(tick, bpm));
				}
			}
			if (inNotes) {
				String[] split = chart[i].trim().split(" ");
				int tick = Integer.parseInt(split[0]);
				String type = split[2];

				if (type.equals("N")) {
					int fret = Integer.parseInt(split[3]);
					int len = Integer.parseInt(split[4]);
					if (lastTick == tick) {
						lastNote.addFret(fret);
						if (lastNote.len == 0) {
							lastNote.len = len;
						}
					} else {
						lastNote.finishNote();
						notes.add(lastNote);
						lastNote = new Note(tick, new boolean[] {}, len);
						lastNote.addFret(fret);
						lastTick = tick;
					}
				}
			}
			if (inInfo) {
				String[] split = chart[i].trim().split(" ");
				String type = split[0].toLowerCase();

				if (type.equals("resolution")) {
					resolution = Integer.parseInt(split[2]);
				}

				if (type.equals("name")) {
					name = chart[i].split(" = ")[1].replace("\"", "");
				}
			}
		}

		notes.add(lastNote);

		float time = 0;
		BPMEvent prevBPM = bpmEvents.get(0);
		prevBPM.seconds = 0;

		for (int i = 0; i < bpmEvents.size(); i++) {
			time += ticksToSeconds(bpmEvents.get(i).tick - prevBPM.tick, prevBPM.bpm);
			bpmEvents.get(i).seconds = time;
			prevBPM = bpmEvents.get(i);
		}

		for (int i = 0; i < notes.size(); i++) {
			notes.get(i).index = i;
			notes.get(i).seconds = ticksToSeconds(notes.get(i).tick);
		}

		processHopos();
	}

	String[] loadStrings(String path) throws IOException {
		ArrayList<String> strings = new ArrayList<String>();

		BufferedReader bf = new BufferedReader(new FileReader(path));

		String line = bf.readLine();

		while (line != null) {
			strings.add(line);
			line = bf.readLine();
		}

		bf.close();

		return strings.toArray(new String[0]);
	}

	float ticksToSeconds(float ticks, float bpm) {
		// Ticks / ticks per beat * minutes per beat
		return (float) ticks / (float) resolution * (60f / bpm);
	}

	float ticksToSeconds(float ticks) {
		int index = getBPMEventBinarySearch(ticks);

		BPMEvent bpm = bpmEvents.get(index);

		if (bpm.tick > ticks && index > 0) {
			bpm = bpmEvents.get(index - 1);
		}

		return bpm.seconds + ticksToSeconds(ticks - bpm.tick, bpm.bpm);
	}

	int getBPMEventBinarySearch(double tick) {
		int lower = 0;
		int upper = bpmEvents.size() - 1;

		int index = 0;

		int mid;

		while (lower <= upper) {
			mid = (lower + upper) >> 1;
			index = mid;

			if (bpmEvents.get(index).tick == (int) tick) {
				break;
			} else {
				if (bpmEvents.get(index).tick < tick) {
					lower = mid + 1;
				} else {
					upper = mid - 1;
				}
			}
		}

		return index;
	}

	void processHopos() {
		for (int i = 0; i < notes.size(); i++) {
			Note curNote = notes.get(i);
			Note lastNote = null;
			if (i > 0) {
				lastNote = notes.get(i - 1);
			}

			if (lastNote != null) {
				boolean prevFret = true;
				for (int x = 0; x < 5; x++) {
					if (curNote.frets[x] != lastNote.frets[x]) {
						prevFret = false;
					}
				}
				
				boolean hopo = (((curNote.tick - lastNote.tick) / (float) resolution) <= 0.35f) && !(curNote.fretCount > 1); // is it a hopo?
				hopo = curNote.isHopo ? !hopo : hopo && !prevFret;
				
				curNote.isHopo = hopo && !curNote.isTap;
			}
		}
	}
}