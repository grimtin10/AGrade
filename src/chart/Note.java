package chart;

public class Note {
	public float tick;
	public float seconds;
	
	public boolean[] frets = new boolean[5];
	public int len;
	
	public int mainFret = -1;
	public int highestFret;
	public int fretCount;
	
	public int index;
	
	public boolean isHopo;
	public boolean isTap;

	public Note(float tick, boolean[] frets, int len) {
		this.tick = tick;
		this.len = len;
		isHopo = false;
		isTap = false;
	}

	public void finishNote() {
		for (int i = 0; i < frets.length; i++) {
			if (frets[i]) {
				fretCount++;
				highestFret = i;
				if (mainFret == -1) {
					mainFret = i;
				}
			}
		}
	}

	public void addFret(int fret) {
		if (fret < frets.length) {
			frets[fret] = true;
		} else if (fret == 5) {
			isHopo = true;
		} else if (fret == 6) {
			isTap = true;
		}
	}
}