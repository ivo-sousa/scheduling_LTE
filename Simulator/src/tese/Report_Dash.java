package tese;

public class Report_Dash {

	float average_thorughput;
	float initial_playout_delay;
	float buffer_level;
	float device_information;
	
	
	public Report_Dash() {
		this.average_thorughput = 0;
		this.initial_playout_delay = 0;
		this.buffer_level = 0;
		this.device_information = 0;
	}


	public Report_Dash(float average_thorughput, float initial_playout_delay, float buffer_level,
			float device_information) {
		super();
		this.average_thorughput = average_thorughput;
		this.initial_playout_delay = initial_playout_delay;
		this.buffer_level = buffer_level;
		this.device_information = device_information;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
