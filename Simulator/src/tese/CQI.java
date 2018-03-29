package tese;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

public class CQI {
	 int cqi;
	 int user_cqi_number;
	 int cqi_pointer=0;
	 public ReadCqis readCqis;
	 ArrayList<String> cqi_events=new ArrayList<String>();
	// public double standard_cqi_desviation=0;
	 public int cqi_variation=0;
	
	public CQI(String cqi_event_file_directory,int cqi_number) {
		Random rand = new Random();
		//user_cqi_number=rand.nextInt(200)+1;
		user_cqi_number=cqi_number;
		readCqis= new ReadCqis(user_cqi_number, cqi_events, cqi_event_file_directory);
		cqi=Integer.parseInt(cqi_events.get(cqi_pointer).split(" ")[1]);
		cqi_pointer++;
	}

	public CQI(int cqi) {
		super();
		this.cqi = cqi;
	}
	
	public void update_cqi(int currenttime){
		int last_cqi=this.cqi;
		BigDecimal i=new BigDecimal(currenttime);
		i=i.divide(new BigDecimal("1000"));
		if(cqi_events.size()-1>=cqi_pointer){
			String[] a=cqi_events.get(cqi_pointer).split(" ");
			BigDecimal j=new BigDecimal(a[0]);
			if(i.equals(j)){
				cqi=Integer.parseInt(cqi_events.get(cqi_pointer).split(" ")[1]);
				if(Math.abs(this.cqi-last_cqi)<Math.abs(cqi_variation)){
					cqi_variation=this.cqi-last_cqi;
				}
				cqi_pointer++;
			}
		}
	}
	
	/*public double calculate_average_cqi(int i, int[][] cqi,int granularity,int j){
		double avg_cqi=0.0;
		for(int j=0;j<cqi.length;j++){
			avg_cqi+=cqi[j];
		}
		
		for(int j=0;j<scenario.numUEs;j++){
			cqis[(i/granularity)][j]=scenario.UEs.get(j).cqi.cqi;
		}
		avg_cqi=avg_cqi/((double)cqi.length);
		return avg_cqi;
		
	}
	public void calculate_standard_cqi_desviation(int i, int[][] cqis, int granularity, int j){
		// TODO Auto-generated method stub
		double result=0;
		double avg_cqi=calculate_average_cqi(cqis,j);
		
		for(int i=0; i<cqis.length;i++){
			result=result+Math.pow((cqis[i]-avg_cqi), 2);
		}
		result= Math.sqrt(result);
		result=result/cqis.length;
		this.standard_cqi_desviation=result;
	}*/
	
	public static void main(String args[]){
		int a=2;
		
		int b=a;
		
		a=3;
		
		System.out.println(a);
	}
	
}
