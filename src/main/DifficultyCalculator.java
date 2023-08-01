package main;

import java.util.ArrayList;

import ai.NeuralNetwork;
import chart.ChartParser;
import chart.Note;

public class DifficultyCalculator {
	static float avgTapSpeed = 0;
	static float maxTapSpeed = 0;
	static int tapSpeedCount = 0;
	static float avgTapTech = 0;
	static float maxTapTech = 0;
	static int tapTechCount = 0;

	static float avgChordSpeed = 0;
	static float maxChordSpeed = 0;
	static int chordSpeedCount = 0;
	static float avgChordTech = 0;
	static float maxChordTech = 0;
	static int chordTechCount = 0;

	static float avgStrumSpeed = 0;
	static float maxStrumSpeed = 0;
	static int strumSpeedCount = 0;
	static float maxStrumTech = 0;

	boolean[] foundNotesTap = new boolean[5];
	boolean[] foundNotesStrum = new boolean[5];

	int techDistTap = 0;
	int techDistStrum = 0;

	public static double[] getDifficultySimple(String path, boolean verbose, float speed) {
		ChartParser chart = new ChartParser();
		chart.parse(path, speed);

//		float tapSpeedWeight = 0.9f;
//		float tapTechWeight = 1.25f; // TODO: pattern detection and actual difficulty
//
//		float chordSpeedWeight = 1.25f;
//		float chordTechWeight = 1f; // TODO: make this ignore 2 chords in a row and prioritize hard chords
//
//		float strumSpeedWeight = 1f;
//		float strumTechWeight = 1.5f; // TODO: make funky rhythms increase this

		avgTapSpeed = 0;
		maxTapSpeed = 0;
		tapSpeedCount = 0;
		avgTapTech = 0;
		maxTapTech = 0;
		tapTechCount = 0;

		avgChordSpeed = 0;
		maxChordSpeed = 0;
		chordSpeedCount = 0;
		avgChordTech = 0;
		maxChordTech = 0;
		chordTechCount = 0;

		avgStrumSpeed = 0;
		maxStrumSpeed = 0;
		strumSpeedCount = 0;
		maxStrumTech = 0;

		ArrayList<Note> notes = chart.notes;
		getMetrics(notes, verbose);

		float lengthWeight = notes.get(notes.size() - 1).seconds / 10f;

		avgTapSpeed /= tapSpeedCount;
		avgTapTech /= tapTechCount;

		avgChordSpeed /= chordSpeedCount;
		avgChordTech /= chordTechCount;

		avgStrumSpeed /= strumSpeedCount;

		if(tapSpeedCount == 0) {
			avgTapSpeed = 0;
		}
		if(tapTechCount == 0) {
			avgTapTech = 0;
		}
		if(chordSpeedCount == 0) {
			avgChordSpeed = 0;
		}
		if(chordTechCount == 0) {
			avgChordTech = 0;
		}
		if(strumSpeedCount == 0) {
			avgStrumSpeed = 0;
		}

		return new double[] { avgTapSpeed, maxTapSpeed, avgTapTech, maxTapTech, avgChordSpeed, maxChordSpeed, avgChordTech, maxChordTech, avgStrumSpeed, maxStrumSpeed, maxStrumTech, lengthWeight };
	}

	public static float getDifficulty(String path, boolean verbose, NeuralNetwork nn, float speed) {
		ChartParser chart = new ChartParser();
		chart.parse(path, speed);

		avgTapSpeed = 0;
		maxTapSpeed = 0;
		tapSpeedCount = 0;
		avgTapTech = 0;
		maxTapTech = 0;
		tapTechCount = 0;

		avgChordSpeed = 0;
		maxChordSpeed = 0;
		chordSpeedCount = 0;
		avgChordTech = 0;
		maxChordTech = 0;
		chordTechCount = 0;

		avgStrumSpeed = 0;
		maxStrumSpeed = 0;
		strumSpeedCount = 0;
		maxStrumTech = 0;

		ArrayList<Note> notes = chart.notes;
		getMetrics(notes, verbose);

		float length = notes.get(notes.size() - 1).seconds / 10f;

		avgTapSpeed /= tapSpeedCount;
		avgTapTech /= tapTechCount;

		avgChordSpeed /= chordSpeedCount;
		avgChordTech /= chordTechCount;

		avgStrumSpeed /= strumSpeedCount;

		if (tapSpeedCount == 0) {
			avgTapSpeed = 0;
		}
		if (tapTechCount == 0) {
			avgTapTech = 0;
		}
		if (chordSpeedCount == 0) {
			avgChordSpeed = 0;
		}
		if (chordTechCount == 0) {
			avgChordTech = 0;
		}
		if (strumSpeedCount == 0) {
			avgStrumSpeed = 0;
		}

		double[] cleanValues = new double[12];
		cleanValues[0] = avgTapSpeed;
		cleanValues[1] = maxTapSpeed;
		cleanValues[2] = avgTapTech;
		cleanValues[3] = maxTapTech;
		cleanValues[4] = avgChordSpeed;
		cleanValues[5] = maxChordSpeed;
		cleanValues[6] = avgChordTech;
		cleanValues[7] = maxChordTech;
		cleanValues[8] = avgStrumSpeed;
		cleanValues[9] = maxStrumSpeed;
		cleanValues[10] = maxStrumTech;
		cleanValues[11] = length;

		float uncleanAvg = 0;
		for (int i = 0; i < cleanValues.length; i++) {
			uncleanAvg += cleanValues[i];
		}
		uncleanAvg /= cleanValues.length;

		double[] cleanWeights = new double[cleanValues.length];
		for (int i = 0; i < cleanValues.length; i++) {
			if (cleanValues[i] > 0) {
				double x = cleanValues[i];
				double c = cleanValue(x, uncleanAvg); // sometimes you dont want smoothing
				cleanWeights[i] = 1;//(x / c + x / Math.max(c / 5, 1)) / 2 / x;
				cleanValues[i] *= cleanWeights[i]; // so i removed it :)))
			} else {
				cleanWeights[i] = 1;
			}
		}

		if (verbose) {
			if(speed != 100) {
				System.out.println("----- " + chart.name.toUpperCase() + " (" + (int)speed + "%) -----");
			} else {
				System.out.println("----- " + chart.name.toUpperCase() + " -----");
			}
			System.out.println();
			System.out.println("length: " + cleanValues[11]);
			System.out.println();
			System.out.println("tap speed: max " + cleanValues[1] + ", avg " + cleanValues[0]);
			System.out.println("tap tech: max " + cleanValues[3] + ", avg " + cleanValues[2]);
			System.out.println();
			System.out.println("chord speed: max " + cleanValues[5] + ", avg " + cleanValues[4]);
			System.out.println("chord tech: max " + cleanValues[7] + ", avg " + cleanValues[6]);
			System.out.println();
			System.out.println("strum speed: max " + cleanValues[9] + ", avg " + cleanValues[8]);
			System.out.println("strum tech: max " + cleanValues[10]);
			System.out.println();
			System.out.println("---------------------------");
		}

		nn.setIn(cleanValues);
		nn.calc();
		float overall = (float) nn.getOut()[0] * 30;

		if (verbose) {
			System.out.println("overall: " + overall);
		}

		return overall;
	}
	
	// do not percieve this
	// it is not very good
	static void getMetrics(ArrayList<Note> notes, boolean verbose) {
		boolean[] foundNotesTap = new boolean[5];
		boolean[] foundNotesStrum = new boolean[5];

		int techDistTap = 0;
		int techDistStrum = 0;

		for (int i = 1; i < notes.size(); i++) {
			float dif = notes.get(i).seconds - notes.get(i - 1).seconds;

			if (dif > 0.005f) {
				if ((notes.get(i).isTap || notes.get(i).isHopo) && (notes.get(i - 1).isTap || notes.get(i - 1).isHopo)) {
					avgTapSpeed += 1f / dif;
					maxTapSpeed = Math.max(1f / dif, maxTapSpeed);
					tapSpeedCount++;

					int minNote = 5;
					int maxNote = 0;
					boolean foundMatch = false;

					for (int j = 0; j < 5; j++) {
						if (notes.get(i).frets[j] && foundNotesTap[j]) {
							for (int k = 0; k < 5; k++) {
								if (foundNotesTap[k]) {
									minNote = Math.min(minNote, k);
									maxNote = Math.max(maxNote, k);
									foundMatch = true;
								}
							}
							break;
						}
					}

					for (int j = 0; j < 5; j++) {
						foundNotesTap[j] = foundNotesTap[j] || notes.get(i).frets[j];
					}

					if (foundMatch) {
						if (dif <= 0.1f) {
							float tech = (maxNote - minNote) * techDistTap * 2;

							avgTapTech += tech;
							maxTapTech = Math.max(tech, maxTapTech);
							tapTechCount++;
						}

						for (int j = 0; j < 5; j++) {
							foundNotesTap[j] = false;
						}
						techDistTap = 0;
					} else {
						techDistTap++;
					}
				} else {
					for (int j = 0; j < 5; j++) {
						foundNotesTap[j] = false;
					}
					techDistTap = 0;
				}

				if (notes.get(i).fretCount > 1 && notes.get(i - 1).fretCount > 1) {
					avgChordSpeed += 1f / dif;
					maxChordSpeed = Math.max(1f / dif, maxChordSpeed);
					chordSpeedCount++;

					float tech = (notes.get(i).highestFret - notes.get(i).mainFret) / (dif * 2) * ((notes.get(i).isTap || notes.get(i).isHopo) ? 0.1f : 1f);
					avgChordTech += tech;
					maxChordTech = Math.max(maxChordTech, tech);
					chordTechCount++;
				}

				if (!(notes.get(i).isTap || notes.get(i).isHopo) && !(notes.get(i - 1).isTap || notes.get(i - 1).isHopo)) {
					avgStrumSpeed += 1f / dif;
					maxStrumSpeed = Math.max(1f / dif, maxStrumSpeed);
					strumSpeedCount++;

					int minNote = 5;
					int maxNote = 0;
					boolean foundMatch = false;

					for (int j = 0; j < 5; j++) {
						if (notes.get(i).frets[j] && foundNotesStrum[j]) {
							for (int k = 0; k < 5; k++) {
								if (foundNotesStrum[k]) {
									minNote = Math.min(minNote, k);
									maxNote = Math.max(maxNote, k);
									foundMatch = true;
								}
							}
							break;
						}
					}

					for (int j = 0; j < 5; j++) {
						foundNotesStrum[j] = foundNotesStrum[j] || notes.get(i).frets[j];
					}

					if (foundMatch) {
						if (dif <= 0.5f) {
							float tech = (maxNote - minNote) * techDistStrum / (dif * 15);

							maxStrumTech = Math.max(tech, maxStrumTech);
						}

						for (int j = 0; j < 5; j++) {
							foundNotesStrum[j] = false;
						}
						techDistStrum = 0;
					} else {
						techDistStrum++;
					}
				} else {
					for (int j = 0; j < 5; j++) {
						foundNotesStrum[j] = false;
					}
					techDistStrum = 0;
				}
			}
		}
	}

	static double cleanValue(double x, double v) {
		double p = Math.abs(x - v) / 25;
		return Math.max(p * p, 1);
	}
}
