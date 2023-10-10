package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import ai.NeuralNetwork;

public class Calibrator {
	public void calibrate(ArrayList<Entry<String, Integer>> input, NeuralNetwork nn) {
		nn.addLayer(12, true);
		nn.addLayer(32, false);
		nn.addLayer(1, false);
		
//		try {
//			nn.load("weights.txt");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		System.out.println("making training data");
		double[][][] trainingData = new double[input.size()][][];

		for (int i = 0; i < trainingData.length; i++) {
			String path = input.get(i).getKey();
			int grade = input.get(i).getValue();

			double[] in = DifficultyCalculator.getDifficultySimple(path, false, 100);

			trainingData[i] = new double[2][];

			trainingData[i][0] = new double[12];
			for (int j = 0; j < trainingData[i][0].length; j++) {
				trainingData[i][0][j] = in[j];
			}

			trainingData[i][1] = new double[] { grade / 30f };
		}

		int numIters = 2500;
		for (int i = 0; i < numIters; i++) {
			System.out.print("iter: " + i + "/" + numIters + " ");
			nn.train(trainingData, trainingData.length);
		}
	}
}
