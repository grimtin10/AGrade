package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import chart.ChartParser;

public class Main {
	public static String[] calibrationPaths = { "C:\\Songs\\Penger\\Penger", "C:\\Songs\\Jobber (Ode to Cheaters)\\Jobber (Ode to Cheaters)", "C:\\Songs\\Half Jew_s Hell", "C:\\Songs\\Guitar Hero III\\Bonus\\Dragonforce - Through The Fire & Flames", "C:\\Songs\\3 - H-ell\\3 - H-ell", "C:\\Songs\\~Community Track Pack X\\VI. Omega\\ArchWk's Hell", "C:\\Songs\\Prevail", "C:\\Songs\\spac", "C:\\Songs\\Constellation", "C:\\Songs\\TSMB2 - Cosmic Embassy\\Cosmic Embassy", "C:\\Songs\\~Community Track Pack 7\\[CTP7] Tier 8\\Supernovae remaster", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\exiobsi", "C:\\Songs\\~Community Track Pack 7\\[CTP7] Tier 8\\Megalodon", "C:\\Songs\\Community Track Pack 6\\TIER 8\\Triathlon", "C:\\Songs\\The PCPlayer Finals", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\exiamal", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\exiarmb", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\exiepidox",
			"C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\exilzigman", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\ExileLord Songs\\eximinds", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\Schmutz06-Solo-Releases\\Zoidberg the Cowboy by Schmutz06 & gamingfreak3", "C:\\Songs\\TheEruptionOffer - Lockene (Edit)", "C:\\Songs\\Acai's Setlist\\non guitar stuff\\void222x-edtrio", "C:\\Songs\\KOTH Season 4 Qualifying Round\\Destruction Armageddon", "C:\\Songs\\Demons 2", "C:\\Songs\\ties - grimtin10\\ties", "C:\\Songs\\Hyperbolium - Lean_s World", };

	public static int[] calibrationGrades = { 11, 22, 6, 8, 21, 19, 15, 23, 21, 30, 18, 16, 20, 19, 30, 9, 9, 12, 4, 10, 7, 7, 8, 10, 17, 9, 19, };

	public static boolean recalculateWeights = true;

	public static void main(String[] args) throws IOException {
		float[] weights = new float[17];
		
		System.out.println("AGrade v1.2.1");
		
		if (recalculateWeights) {
//			System.out.println("loading all songs");
//			HashMap<String, String> names = new HashMap<String, String>();
//			ChartParser parser = new ChartParser();
//			Object[] paths = Files.find(Path.of("C:\\Songs\\"), 99999, (p, bfa) -> (bfa.isRegularFile() && p.toFile().getName().endsWith("notes.chart"))).toArray();
//
//			for (int i = 0; i < paths.length; i++) {
//				Path p = (Path) paths[i];
//				String path = p.toString();
//				path = path.substring(0, path.lastIndexOf("\\"));
//				parser.getName(path);
//
//				if (!names.containsKey(parser.name)) {
//					names.put(parser.name, path);
//				}
//			}
//			
//			System.out.println(names.size() + " songs");
//
//			System.out.println("loading all grades");
//			ArrayList<Entry<String, Integer>> grades = new ArrayList<Entry<String, Integer>>();
//
//			String[] gradeFile = loadStrings("grades.csv");
//			for (int i = 1; i < gradeFile.length; i++) {
//				String[] split = gradeFile[i].split(",");
//				String name = split[0].replace("\"", "");
//
//				int grade;
//				try {
//					grade = Integer.parseInt(split[3]);
//				} catch (Exception e) {
//					continue;
//				}
//
//				grades.add(new SimpleEntry<String, Integer>(name, grade));
//			}
//
//			System.out.println("getting songs with grades");
			ArrayList<Entry<String, Integer>> toProcess = new ArrayList<Entry<String, Integer>>();
//			for (int i = 0; i < grades.size(); i++) {
//				String name = grades.get(i).getKey();
//				int grade = grades.get(i).getValue();
//
//				if (names.containsKey(name)) {
//					toProcess.add(new SimpleEntry<String, Integer>(names.get(name), grade));
//				}
//			}
			for (int i = 0; i < calibrationPaths.length; i++) {
				toProcess.add(new SimpleEntry<String, Integer>(calibrationPaths[i], calibrationGrades[i]));
			}
			
			System.out.println(toProcess.size() + " songs with grades");

			System.out.println("Calibrating");

			for (int i = 0; i < toProcess.size(); i++) {
				double[] out = DifficultyCalculator.getDifficultySimple(toProcess.get(i).getKey(), false, 100);
				float grade = toProcess.get(i).getValue();

				for (int j = 0; j < out.length; j++) {
					if(out[j] > 0) {
						weights[j] += grade / out[j];
					}
				}
			}

			for (int i = 0; i < weights.length; i++) {
				weights[i] /= toProcess.size();
			}
			weights[12] = 1;
			
			float overallWeight = 0;
			for (int i = 0; i < toProcess.size(); i++) {
				float out = DifficultyCalculator.getDifficulty(toProcess.get(i).getKey(), false, weights, 100);
				float grade = toProcess.get(i).getValue();

				overallWeight += grade / out;
			}
			weights[12] = overallWeight / toProcess.size();
			
			FileWriter file = new FileWriter("weights.txt");
			PrintWriter out = new PrintWriter(file);
			for (int i = 0; i < weights.length; i++) {
				out.println(weights[i]);
			}
			out.flush();
			out.close();
		} else {
			try {
				String[] weightText = loadStrings("weights.txt");

				for (int i = 0; i < weightText.length; i++) {
					weights[i] = Float.parseFloat(weightText[i]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Tap Speed Weight:    [Avg: " + weights[0] + "] [Max: " + weights[1] + "]");
		System.out.println("Tap Tech Weight:     [Avg: " + weights[2] + "] [Max: " + weights[3] + "]");
		System.out.println("Tap Stamina Weight:  [Avg: " + weights[13] + "] [Max: " + weights[14] + "]");
		System.out.println("Chord Speed Weight:  [Avg: " + weights[4] + "] [Max: " + weights[1] + "]");
		System.out.println("Chord Tech Weight:   [Avg: " + weights[6] + "] [Max: " + weights[1] + "]");
		System.out.println("Strum Speed Weight:  [Avg: " + weights[8] + "] [Max: " + weights[1] + "]");
		System.out.println("Strum Tech Weight:   [Max: " + weights[10] + "]");
		System.out.println("Strum Stamina Weight: [Avg: " + weights[15] + "] [Max: " + weights[16] + "]");
		System.out.println("Length Weight:      " + weights[11]);

		System.out.println();

//		float diff = DifficultyCalculator.getDifficulty("C:\\Songs\\The Mighty Rio Grande", true, weights, 100);
		float diff = DifficultyCalculator.getDifficulty("C:\\Songs\\songs\\Crisis City", true, weights, 100);
		float diff2 = diff / weights[12];
		
		if(diff > diff2) {
			System.out.println("Final Estimated Grade: " + Math.round(diff2) + "-" + Math.round(diff));
		} else {
			System.out.println("Final Estimated Grade: " + Math.round(diff) + "-" + Math.round(diff2));
		}
	}

	public static String[] loadStrings(String path) throws IOException {
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

	public static float random(float min, float max) {
		return (float) (min + Math.random() * (max - min));
	}

	public static float ftanh(float x) {
		return x / (1 + Math.abs(x)) * 1.5f;
	}

	public static double relu(double x) {
		return Math.max(x, 0);
	}
}
