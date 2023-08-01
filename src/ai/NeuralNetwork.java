package ai;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import main.Main;

public class NeuralNetwork {
	public ArrayList<Layer> layers;
	
	public double eps = 0.0001f;
	public double step = 0.0001f;

	public NeuralNetwork() {
		layers = new ArrayList<Layer>();
	}

	public NeuralNetwork clone() {
		NeuralNetwork nn = new NeuralNetwork();

		nn.layers = new ArrayList<Layer>();

		for (int i = 0; i < layers.size(); i++) {
			nn.layers.add(layers.get(i).clone());
		}

		return nn;
	}

	public void addLayer(int size, boolean isInput) {
		if (isInput) {
			layers.add(new Layer(size));
		} else {
			layers.add(new Layer(size, layers.get(layers.size() - 1)));
		}
	}

	public void setIn(double[] in) {
		Layer firstLayer = layers.get(0);
		for (int i = 0; i < firstLayer.neurons.length; i++) {
			firstLayer.neurons[i].value = in[i];
		}
	}

	public void calc() {
		for (int i = 1; i < layers.size(); i++) {
			layers.get(i).calc(layers.get(i - 1));
		}
	}

	public double[] getOut() {
		Layer lastLayer = layers.get(layers.size() - 1);

		double[] out = new double[lastLayer.neurons.length];

		for (int i = 0; i < out.length; i++) {
			out[i] = lastLayer.neurons[i].value;
		}

		return out;
	}

	public void save(String file) throws FileNotFoundException {
		ArrayList<String> text = new ArrayList<String>();
		text.add(layers.size() + ""); // layer count
		for (int i = 0; i < layers.size(); i++) {
			text.add(layers.get(i).neurons.length + ""); // neuron count
			if (i > 0) {
				for (int j = 0; j < layers.get(i).neurons.length; j++) {
					text.add(layers.get(i).neurons[j].weights.length + ""); // weight count
					for (int k = 0; k < layers.get(i).neurons[j].weights.length; k++) {
						text.add(layers.get(i).neurons[j].weights[k] + ""); // weight
					}
					text.add(layers.get(i).neurons[j].bias + ""); // bias
				}
			}
		}

		PrintWriter out = new PrintWriter("weights.txt");
		for (int i = 0; i < text.size(); i++) {
			out.println(text.get(i));
		}
		out.close();
		System.out.println("saving network...");
	}

	public void load(String file) throws IOException {
		String[] text = Main.loadStrings(file);

		int offset = 0;

		layers.clear();

		int layerCount = Integer.parseInt(text[offset++]); // layer count
		for (int i = 0; i < layerCount; i++) {
			addLayer(Integer.parseInt(text[offset++]), i == 0); // neuron count
			if (i > 0) {
				Layer layer = layers.get(i);
				for (int j = 0; j < layer.neurons.length; j++) {
					Neuron neuron = layer.neurons[j];
					neuron.weights = new double[Integer.parseInt(text[offset++])]; // weight count
					for (int k = 0; k < neuron.weights.length; k++) {
						neuron.weights[k] = Double.parseDouble(text[offset++]); // weight
					}
					neuron.bias = Double.parseDouble(text[offset++]); // bias
				}
			}
		}
	}

	public double evaluate(double[][][] data) {
		double loss = 0;

		for (int i = 0; i < data.length; i++) {
			setIn(data[i][0]);
			calc();

			double[] out = getOut();
			for (int j = 0; j < out.length; j++) {
				double diff = out[j] - data[i][1][j];
				loss += (diff * diff) / out.length;
			}
		}

		return loss;// / data.length;
	}

	public double evaluate(double[][][] data, int batchSize, int offset) {
		double loss = 0;

		for (int i = 0; i < Math.min(batchSize, data.length); i++) {
			setIn(data[i + offset][0]);
			calc();

			double[] out = getOut();
			for (int j = 0; j < out.length; j++) {
				double diff = out[j] - data[i + offset][1][j];
				loss += (diff * diff) / out.length;
			}
		}

		return loss / batchSize;
	}

	public void train(double[][][] data, int batchSize) {
		int offset = Math.max((int)(Main.random(0, data.length - (batchSize - 0.9999f))), 0);

		double initLoss = evaluate(data, batchSize, offset);

		double batchEps = eps;// min(batchSize, data.length)/(double)data.length*eps;

		double[][][] weightGradients = new double[layers.size() - 1][][];
		double[][] biasGradients = new double[layers.size() - 1][];

		for (int i = 1; i < layers.size(); i++) {
			Layer layer = layers.get(i);

			weightGradients[i - 1] = new double[layer.neurons.length][];
			biasGradients[i - 1] = new double[layer.neurons.length];
			for (int j = 0; j < layer.neurons.length; j++) {
				Neuron neuron = layer.neurons[j];

				weightGradients[i - 1][j] = new double[neuron.weights.length];

				for (int k = 0; k < neuron.weights.length; k++) {
					double origValue = neuron.weights[k];

					neuron.weights[k] += batchEps;

					double newLoss = evaluate(data, batchSize, offset);

					double deltaLoss = (newLoss - initLoss) / batchEps;

					neuron.weights[k] = origValue;

					weightGradients[i - 1][j][k] = -deltaLoss * step;
					
//					System.out.println(deltaLoss + " " + deltaLoss * step);
				}

				double origValue = neuron.bias;

				neuron.bias += batchEps;

				double newLoss = evaluate(data, batchSize, offset);

				double deltaLoss = (newLoss - initLoss) / batchEps;

				neuron.bias = origValue;

				biasGradients[i - 1][j] = -deltaLoss * step;
			}
		}

		for (int i = 1; i < layers.size(); i++) {
			Layer layer = layers.get(i);

			for (int j = 0; j < layer.neurons.length; j++) {
				Neuron neuron = layer.neurons[j];

				for (int k = 0; k < neuron.weights.length; k++) {
//					System.out.print(neuron.weights[k] + " ");
					neuron.weights[k] += weightGradients[i - 1][j][k];
//					System.out.println(weightGradients[i - 1][j][k] + " " + neuron.weights[k]);
				}
				neuron.bias += biasGradients[i - 1][j];
			}
		}
		
		System.out.println("loss: " + initLoss);
	}
}