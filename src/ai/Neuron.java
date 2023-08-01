package ai;

import main.Main;

public class Neuron {
	public double value;

	public double[] weights;
	public double bias;

	public Neuron(Layer prevLayer) {
		if (prevLayer != null) {
			weights = new double[prevLayer.neurons.length];

			double s = Math.sqrt(prevLayer.neurons.length);

			for (int i = 0; i < weights.length; i++) {
				weights[i] = Main.random(-1, 1) / s;
			}
			bias = 0;// random(-0.25, 0.25);
		}
	}

	public Neuron clone() {
		Neuron neuron = new Neuron(null);

		if (weights != null) {
			neuron.weights = new double[weights.length];
			for (int i = 0; i < weights.length; i++) {
				neuron.weights[i] = weights[i];
			}
			neuron.bias = bias;
		}

		return neuron;
	}

	public void calc(Layer prevLayer) {
		value = bias;
		for (int i = 0; i < prevLayer.neurons.length; i++) {
			value += prevLayer.neurons[i].value * weights[i];
//      System.out.println(weights[i] + " " + bias + " " + prevLayer.neurons[i].value);

			if (value != value) {
				System.out.println(weights[i] + " " + bias + " " + prevLayer.neurons[i].value);
				System.exit(0);
			}
		}

		// println(value, index);
		value = Main.relu(value);
	}
}