package ai;

public class Layer {
  public Neuron[] neurons;

  // hidden/output layer
  public Layer(int size, Layer prevLayer) {
    neurons = new Neuron[size];

    for (int i = 0; i < neurons.length; i++) {
      neurons[i] = new Neuron(prevLayer);
    }
  }

  // input layer
  public Layer(int size) {
    neurons = new Neuron[size];

    for (int i = 0; i < neurons.length; i++) {
      neurons[i] = new Neuron(null);
    }
  }

  public void calc(Layer prevLayer) {
    for (int i = 0; i < neurons.length; i++) {
      neurons[i].calc(prevLayer);
    }
  }
  
  public Layer clone() {
    Layer layer = new Layer(neurons.length);
    
    layer.neurons = new Neuron[neurons.length];
    
    for(int i=0;i<neurons.length;i++) {
      layer.neurons[i] = neurons[i].clone();
    }
    
    return layer;
  }
}