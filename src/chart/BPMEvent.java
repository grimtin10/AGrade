package chart;

public class BPMEvent {
	float tick;
	float seconds;
	float bpm;

	public BPMEvent(float tick, float bpm) {
		this.tick = tick;
		this.bpm = bpm;
	}
}