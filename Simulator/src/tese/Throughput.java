package tese;

public class Throughput {
	double efficiency [] ={0,0.1523,0.2344,0.3770,0.6016,0.8770,1.1758,1.4766,1.9141,2.4063,2.7305,3.3223,3.9023,4.5234,5.1152,5.5547	};
	public  int[] bits_per_RB_per_CQI={0,20,30,48,76,111,149,187,242,304,345,419,492,570,645,700};
	double throughput;
	
	public Throughput() {
		// TODO Auto-generated constructor stub
		throughput =   0;
	}
	
	public void update_bitRate(int cqi, int numAntennas, int numAllocatedRB){
			//throughput=numAllocatedRB*1000*12*7*2*efficiency[cqi]*numAntennas*0.75; // bits/s
		throughput=numAllocatedRB*1000*bits_per_RB_per_CQI[cqi]*numAntennas; // bits/s
	}

}
