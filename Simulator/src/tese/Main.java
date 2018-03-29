package tese;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.SystemOutLogger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Desktop;

public class Main {
	public static String directory="C:/Users/alunos/Desktop/";
	public static String result_file_directory= directory+"Results.xls";
	public static String cqi_event_file_directory=directory+"cqis-events/cqi_event-";
	public static int simulation=-1;
	static Scenario scenario;
	public static int initial_delay=5000;
	public static int numMillisecondToSimulate=180000;
	//public int numUEs=100;
	//public int numDashUEs=0;
	public static int numRB=100;
	public static int numAntennas=1;
	public static int[] cqi_numbers;
	public static double[] period_above_mos_limit;
	public static double[] avg_mos;
	public static double[] mos_above_limit_during_more_than_x_percent_of_time;
	public static double mos_limit=3;
	public static double time_mos_limit=80; //percentage time that mos is above limit
	public static double percetage_users_w_mos_greater_than_limit=0;
	public static double number_users_w_mos_lower_than_Z_limit=0;
	
	public static int buffer_l=0;
	
	public static int segment_size=1000;
	/*dash mode*/
	public static String dash_mode="QAAD"; //default,QDASH,QAAD
	public static double u_marginal_buffer_lenght=10000; //10 seconds
	public static double w_weight_factor=0.875;
	public static double teta_estimation_period=3;
	public static double sigma=3000; // 3 seconds
	public static int last_served=-1;
	public static int number_segments_initially_requested=5;
	public static int number_segments_requested_during_rebuffering=5;
	public static int initial_buffer_amount_to_start_draining=5000;
	public static int recover_stall_buffer_amount_to_start_draining=5000;
	
	public static int a=0,b=0,c=0;
	
	/**/	
	/*granularity*/
	public static int granularity_mos_export_excel=60;
	public static int granularity_buffer_export_excel=60;
	public static int granularity_cqi_export_excel=60;
	public static int granularity_throughput_export_excel=60;
	
	public static int granularity_buffer_write=1;
	public static int granularity_cqi_write=5;
	public static int granularity_throughput_write=1;
	public static int granularity_mos_write=1;
	
	
	public static int granularity_Video_quality_change=segment_size;
	public static int granularity_Cqis_update=5;
	public static int granularity_buffer_drain=1;
	

	/**/
	public static int buffer_size=120000;
	public static int interrupt_allocating=1500;
	public static int rebuffering_period=5000;
	public static int[] bits_per_RB_per_CQI={0,20,30,48,76,111,149,187,242,304,345,419,492,570,645,700};
	public static boolean[] sending_data;

	public static int minimum_period_to_request=5;
	
	/*algorithm inputs*/
	int strategy;
	static double U_2=3; // QoE limite 3, 3.5 ou 4
	static double Y_3=0.7; //% users with QoE>U (0.7, 0.8 ou 0.9)
	static double U_3=3; // QoE limite 3, 3.5 ou 4
	static double Z_limit=2; //minimun QoE for the remaining users (2)
	/**/
	
	static double[][] buffers;
	static int[][] cqis;
	static double[][] throughputs;
	static double[][] mos;

	static double[] average_throughputs;
	static double[] user_metrics;
	public static double avg_mos_of_all_users=0;

	

	public static double[] calculate_average_throughputs(int i){
		
			for(int j=0;j<scenario.numUEs;j++){
				if(i==0){
					average_throughputs[j]=scenario.UEs.get(j).bitRate.throughput;
				}else{
					average_throughputs[j]=scenario.UEs.get(j).calculate_average_throughput(i,average_throughputs[j]);
				}
			}
			return average_throughputs;
	}

	public static void openCSV(File a){
		try {
            Desktop.getDesktop().open(a);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void drain_users_buffers(double qt, int i, int granularity){
		if(i%granularity==0){
			for(int j=0;j<scenario.numUEs;j++){
				if(!scenario.UEs.get(j).initial_buffer_filling && !scenario.UEs.get(j).recovering_stall){
				//if((scenario.UEs.get(j).requesting_initial_segments==false && scenario.UEs.get(j).rebuffering==false)){
					scenario.UEs.get(j).drain_buffer(i,qt,rebuffering_period,number_segments_requested_during_rebuffering); // por cada segundo (1000 iterações/alocações/ms)
				}
			}
		}
	}

	public static void allocate_equal_nbr_RB1(){
		for(int i=0; i<scenario.numUEs;i++){
			scenario.UEs.get(i).numAllocatedRB=0;
		}
		for(int i=0; i<numRB;i++){
			
			if(last_served==scenario.numUEs-1){
				if(sending_data[0]){
					scenario.UEs.get(0).numAllocatedRB++;
					last_served=0;
				}
			}else{
				if(sending_data[last_served+1]){
					scenario.UEs.get(last_served+1).numAllocatedRB++;
					last_served++;
				}else{
					System.out.println("OOOOOOOOOOOKAASKASAOOOOOOOOOOOOOOSASASAS");
				}
			}
			
			
			
		}
	}
	public static void allocate_equal_nbr_RB(){
		int user_w_highest_metric=0;
		for(int i=0; i<scenario.numUEs;i++){
			scenario.UEs.get(i).numAllocatedRB=0;
		}
		
			
		for (int j = 0; j < user_metrics.length; j++) {
			user_metrics[j]=1/(scenario.UEs.get(j).number_times_served+1);
			//System.out.println(scenario.UEs.get(j).number_times_served+1);
		}
		
		user_w_highest_metric=find_USER_w_highest_metric(0);
		scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		//System.out.println(user_w_highest_metric+"#");
		scenario.UEs.get(user_w_highest_metric).number_times_served++;
		
	}
	
	public static void allocate_PFBF(int i){
		int user_w_highest_metric=0;
		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			boolean bol=false;
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
			}
			calculate_average_throughputs(i);
			int fmin=330;
			double denominador=0;
			calculate_percent_rebuf(i);
				for (int j = 0; j < scenario.numUEs; j++) {
					denominador=denominador+scenario.UEs.get(j).percent_rebuf;
				}
				for (int j = 0; j < user_metrics.length; j++) {
					double v=1;
					if(denominador>0){
						double frac=scenario.numUEs*scenario.UEs.get(j).percent_rebuf/denominador;
						v=1+frac;
						//System.out.println(v);
					}else{
						v=1;
					}
					
					
					double throughput= scenario.numRB*1000*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]*scenario.UEs.get(j).numAntennas;
					//scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality]
					
					user_metrics[j]=v*((Math.exp(fmin-(scenario.UEs.get(j).buffer))*throughput/scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality])+(throughput*Math.pow(10, 6)/average_throughputs[j]));
					
				}			
			
			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}
	
	
	/*public static void allocate_modified_PF(int i){
		int user_w_highest_metric=0;
		double alfa=1;
		double beta=1;
		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			boolean bol=false;
			double total_throughput=0;
			int x=0;
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
				user_metrics[j]=0;
				//total_throughput+=average_throughputs[j];
				
				if(scenario.UEs.get(j).buffer<300 && !scenario.UEs.get(j).requesting_initial_segments){
					bol=true;
					x++;
				}
			}
			//System.out.println(x);
			calculate_average_throughputs(i-1);
			if(bol){
				//System.out.println("ai");
				for (int j = 0; j < user_metrics.length; j++) {
					
					user_metrics[j]=(double)1/scenario.UEs.get(j).buffer;	
				}
				
			}else{
				//(throughput*scenario.numUEs/total_throughput)
				//System.out.println("olee");
				for (int j = 0; j < user_metrics.length; j++) {
					double throughput= scenario.numRB*1000*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]*scenario.UEs.get(j).numAntennas;
					double qt=calculate_qt(j);
					//System.out.println(qt);
					//user_metrics[j]=qt/(scenario.UEs.get(j).buffer/i);
					user_metrics[j]=1/average_throughputs[j];
				}
			}

			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}*/
	
	public static double calculate_nbr_RB_needed(int index){
		double[] nbr_rb={0,31.26,20.3,12.63,7.91,5.43,4.05,3.22,2.49,1.98,1.74,1.43,1.22,1.05,0.93,0.85};
				
			return nbr_rb[scenario.UEs.get(index).cqi.cqi];
	}
	
	public static void allocate_modified_PF(int i){
		int user_w_highest_metric=0;

		int aux=numRB;

		for (int j = 0; j < user_metrics.length; j++) {
			scenario.UEs.get(j).numAllocatedRB=0;
			user_metrics[j]=0;
		}
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{

			boolean bol=false;
			boolean bol2=false;
			
			for (int j = 0; j < user_metrics.length; j++) {
				//scenario.UEs.get(j).numAllocatedRB=0;
				user_metrics[j]=0;
			}

			calculate_average_throughputs(i-1);
			for (int j = 0; j < user_metrics.length; j++) {	

				if(scenario.UEs.get(j).requested_video.requested_quality<8.5){
					bol=true;
				}
				if(scenario.UEs.get(j).buffer<buffer_l){
					
					bol2=true;
				}
				
			}
			
			if(bol2==true){
				for (int j = 0; j < user_metrics.length; j++) {
					scenario.UEs.get(j).avg_served_q_when_mos_equal_2=0;
					user_metrics[j]=1/scenario.UEs.get(j).buffer;
				}
				user_w_highest_metric=find_USER_w_highest_metric(0);
				scenario.UEs.get(user_w_highest_metric).numAllocatedRB=aux;
				a++;
			}else if(bol==true){
				for (int j = 0; j < user_metrics.length; j++) {
					scenario.UEs.get(j).avg_served_q_when_mos_equal_2=0;
					scenario.UEs.get(j).total_rb=0;
					user_metrics[j]=1/average_throughputs[j];
					
				}
				user_w_highest_metric=find_USER_w_highest_metric(0);
				scenario.UEs.get(user_w_highest_metric).numAllocatedRB=aux;
				b++;
			}else{
				//if(c==0)System.out.println(i);
				c++;
				//System.out.println(" c "+i);
				for (int j = 0; j < user_metrics.length; j++) {	
					
					scenario.UEs.get(j).calculate_avg_quality();
					if(scenario.UEs.get(j).requested_video.requested_quality>15.0){
						//System.out.println(i);
						if(scenario.UEs.get(j).avg_served_q_when_mos_equal_2==0){
							scenario.UEs.get(j).avg_served_q_when_mos_equal_2=scenario.UEs.get(j).calculate_avg_quality();
							//scenario.UEs.get(j).avg_served_q_when_mos_equal_2=scenario.UEs.get(j).requested_video.requested_quality-2;
							scenario.UEs.get(j).i=i;

							if(aux>=(int) scenario.UEs.get(j).rb_quality_cqi[(int)Math.ceil(scenario.UEs.get(j).avg_served_q_when_mos_equal_2)][scenario.UEs.get(j).cqi.cqi]){
								scenario.UEs.get(j).numAllocatedRB=(int) scenario.UEs.get(j).rb_quality_cqi[(int)Math.ceil(scenario.UEs.get(j).avg_served_q_when_mos_equal_2)][scenario.UEs.get(j).cqi.cqi];
								scenario.UEs.get(j).total_rb+=(int) scenario.UEs.get(j).rb_quality_cqi[(int)Math.ceil(scenario.UEs.get(j).avg_served_q_when_mos_equal_2)][scenario.UEs.get(j).cqi.cqi];
								aux-=(int) scenario.UEs.get(j).rb_quality_cqi[(int)Math.ceil(scenario.UEs.get(j).avg_served_q_when_mos_equal_2)][scenario.UEs.get(j).cqi.cqi];
								//System.out.println(j+":"+scenario.UEs.get(j).numAllocatedRB);
							}else{
								System.out.println("Not enough 1");
								scenario.UEs.get(j).numAllocatedRB=aux;
								scenario.UEs.get(j).total_rb+=aux;
								aux=0;
							}
							
						}else{
		
							//para baixo
							if(scenario.UEs.get(j).total_rb/(i-scenario.UEs.get(j).i)> scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]){
								if(aux>=(int) scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]){
									scenario.UEs.get(j).numAllocatedRB=(int) scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi];
									scenario.UEs.get(j).total_rb+=(int) scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi];
									aux-=(int) scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi];
									//System.out.println((int) scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]);
								}else{
									System.out.println("Not enough 2");
									scenario.UEs.get(j).numAllocatedRB=aux;
									scenario.UEs.get(j).total_rb+=aux;
									aux=0;
								}
							}else{//arredonda para cima
								if(aux>=(int) Math.ceil(scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi])){
									scenario.UEs.get(j).numAllocatedRB=(int) Math.ceil(scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]);
									//System.out.println(scenario.UEs.get(j).numAllocatedRB);
									scenario.UEs.get(j).total_rb+=(int) Math.ceil(scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]);
									aux-=(int) Math.ceil(scenario.UEs.get(j).rb_quality_cqi[(int)scenario.UEs.get(j).avg_served_q_when_mos_equal_2][scenario.UEs.get(j).cqi.cqi]);
								}else{
									System.out.println("Not enough 3");
									scenario.UEs.get(j).numAllocatedRB=aux;
									scenario.UEs.get(j).total_rb+=aux;
									aux=0;
								}
							}
						}
						
						
						
					}
					
				}
			}
		}
	}
	
	public static void allocate_modified_PF2(int i){
		int user_w_highest_metric=0;

		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			boolean bol=false;

			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
				user_metrics[j]=0;

				
				if(scenario.UEs.get(j).buffer<300 && !scenario.UEs.get(j).requesting_initial_segments){
					bol=true;
				}
			}

			calculate_average_throughputs(i-1);
			if(bol){
				for (int j = 0; j < user_metrics.length; j++) {				
					user_metrics[j]=(double)1/scenario.UEs.get(j).buffer;	
				}
				
			}else{
				
				for (int j = 0; j < user_metrics.length; j++) {
					double qt=calculate_qt(j);
					
					user_metrics[j]=qt/(scenario.UEs.get(j).buffer);
				}
			}

			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}
	
	
	public static void allocate_modified_PF3(int i){
		int user_w_highest_metric=0;

		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			boolean bol=false;

			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
				user_metrics[j]=0;

				
				if(scenario.UEs.get(j).buffer<300 && !scenario.UEs.get(j).requesting_initial_segments){
					bol=true;
				}
			}

			calculate_average_throughputs(i-1);
			if(bol){
				for (int j = 0; j < user_metrics.length; j++) {			
	
					user_metrics[j]=(double)1/scenario.UEs.get(j).buffer;	
				}
				
			}else{
				
				for (int j = 0; j < user_metrics.length; j++) {
					double qt=calculate_qt(j);
					scenario.UEs.get(j).calculate_avg_quality();
					user_metrics[j]=qt*scenario.UEs.get(j).requested_video.video_requested_BitRate/scenario.UEs.get(j).avg_served_quality/scenario.UEs.get(j).buffer;
				}
			}

			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}
	
	public static void allocate_by_PF(int i){
		int user_w_highest_metric=0;
		double alfa=1;
		double beta=1;
		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
			}
			calculate_average_throughputs(i-1);
			for (int j = 0; j < user_metrics.length; j++) {
				double throughput= scenario.numRB*1000*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]*scenario.UEs.get(j).numAntennas;
				user_metrics[j]=throughput/average_throughputs[j];
			}
			
			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}

	
	public static void allocate_by_RAGA(int i){
		int user_w_highest_metric=0;
		double beta=((double) 1)/((double) (i+1));
		//System.out.println(beta);
		int aux=numRB;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
			}
			double[] throughputs_last=new double[scenario.numUEs];
			
			//throughputs_last=calculate_average_throughputs(i);
			//System.out.println("last: "+throughputs_last[1]);
			//System.out.println(average_throughputs[1]);

			double h_r_t=0;
			
			for(int k=0; k<scenario.numUEs;k++){
				h_r_t=h_r_t+Math.log(average_throughputs[k]);
			}
			
			
			for (int j = 0; j < user_metrics.length; j++) {
				
				double throughput= scenario.numRB*1000*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]*scenario.UEs.get(j).numAntennas;

				user_metrics[j]=Math.log((average_throughputs[j]*(i-1)+throughput)/i);
			}
			double[] aux_metrics=user_metrics;

			for(int j=0; j<scenario.numUEs;j++){
				for(int k=0;k<scenario.numUEs;k++){
					if(k!=j)
					user_metrics[j]=user_metrics[j]+Math.log(aux_metrics[k]);

				}
						
			}
			
			for (int j = 0; j < user_metrics.length; j++) {	
				double throughput= scenario.numRB*1000*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]*scenario.UEs.get(j).numAntennas;
				double buffer_difference=0;
				double b_steady_tresh=2000;
				double delta=1;
				
				double fi=0.1;
				double a=1+fi*Math.max((b_steady_tresh-scenario.UEs.get(j).buffer)/b_steady_tresh, 0.0);
				//System.out.println(a);
				if(i==0 || i==1){
					buffer_difference=0;
				}else{
					buffer_difference=scenario.UEs.get(j).calculate_buffer_difference(buffers[(i/granularity_buffer_write)-2][j]);
					//System.out.println(buffer_difference);
				}
				double w=Math.max(scenario.UEs.get(j).w_minus_1+(delta-buffer_difference),0.0);
				w=1;
				scenario.UEs.get(j).w_minus_1=w;
				//System.out.println(w);
				user_metrics[j]=Math.exp(a*w)*(user_metrics[j]-h_r_t)*throughput;
				//System.out.println(Math.exp(a*w));
			}
			
			if(i==4000){
				for(int j=0;j<scenario.numUEs;j++){
					System.out.println(user_metrics[j]);
				}
			}
			
			user_w_highest_metric=find_USER_w_highest_metric(0);
			scenario.UEs.get(user_w_highest_metric).numAllocatedRB=scenario.numRB;
		}
	}
	
	public static void allocate_RB_by_avg_throughput(int i){
		int user_w_highest_metric=0;
		int aux=numRB;
		boolean some_to_allocate=false;
		int user_to_allocate=0;
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
				if(some_to_allocate==false){
					if(scenario.UEs.get(j).to_allocate==true){
						some_to_allocate=true;
						user_to_allocate=j;
					} 
				}
				
				if(scenario.UEs.get(j).buffer<buffer_size && scenario.UEs.get(j).buffer>buffer_size-1000){
					scenario.UEs.get(j).to_allocate=false;						
				}
				if(scenario.UEs.get(j).to_allocate==false){
					scenario.UEs.get(j).interrupt_allocating--;
					if(scenario.UEs.get(j).interrupt_allocating==0){
						scenario.UEs.get(j).to_allocate=true;
						scenario.UEs.get(j).interrupt_allocating=interrupt_allocating;
					}
				}
			}
			
			
				calculate_average_throughputs(i-1);
		        
		    
			for(int rb=0;rb<numRB;rb++){
				for (int j = 0; j < user_metrics.length; j++) {
					//calculate_average_throughputs(throughputs, i-1);
			        user_metrics[j]=1/average_throughputs[j];
			    }
				
				if(some_to_allocate==true){
					user_w_highest_metric=find_USER_w_highest_metric(user_to_allocate);
					scenario.UEs.get(user_w_highest_metric).numAllocatedRB++;
				}
				
				
			}
		}
		
	}
	
	public static void allocate_by_buffer(int i){
		int buffer_target=10000;
		int aux=numRB;
		int buffer_limit_to_be_in_risk_toStall=100;
		
		for(int j=0; j<scenario.numUEs;j++){
			scenario.UEs.get(j).numAllocatedRB=0;
		}
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			ArrayList<User> Users_low_buffer= new ArrayList<User>();
			ArrayList<User> Users_in_risk_to_Stall= new ArrayList<User>();
			
			for(int j=0;j<scenario.numUEs;j++){
				if(scenario.UEs.get(j).buffer<buffer_target){
					Users_low_buffer.add(scenario.UEs.get(j));
				}

				if(scenario.UEs.get(j).buffer<buffer_limit_to_be_in_risk_toStall && !scenario.UEs.get(j).rebuffering  && !scenario.UEs.get(j).requesting_initial_segments){ // vai haver stall se nada for alocado
					Users_in_risk_to_Stall.add(scenario.UEs.get(j));
				}
			}
			
			
			
			Collections.sort(Users_low_buffer, User.Comparator_Buffer_Ascendent);
			
			while(aux>0){
				for(User u:Users_low_buffer){

					if(u.buffer>buffer_target){
						
					}else{
						if(aux>0){
							u.numAllocatedRB++;
							aux--;
						}
					}
				}
				
				if(aux>0 && Users_in_risk_to_Stall.size()!=0){
					for(User u:Users_in_risk_to_Stall){
	
						if(u.buffer>buffer_limit_to_be_in_risk_toStall){
							
						}else{
							if(aux>0){
								u.numAllocatedRB++;
								aux--;	
							}
						}
					}
				}
				
				if(aux>0){
					int k=find_user_w_highest_cqi(scenario.UEs);
					scenario.UEs.get(k).numAllocatedRB+=aux;
					aux=0;
				}
				
			}
		}
	}
	
	public static void allocate_by_metric_x(int i) throws IOException{
		/*for(int j=0; j<numUEs;j++){
			scenario.UEs.get(j).numAllocatedRB=0;
		}
		calculate_avg_qualities();
		
		for(int rb=0;rb<numRB;rb++){
			for (int j = 0; j < user_metrics.length; j++) {
				//calculate_average_throughputs(throughputs, i-1);
		        user_metrics[j]=scenario.UEs.get(j).duration_rebuffering_event*Math.exp(5*(double)scenario.UEs.get(j).cqi.cqi_variation)/(scenario.UEs.get(j).buffer*scenario.UEs.get(j).avg_served_quality);
		    }
			
		
				int user_w_highest_metric=find_USER_w_highest_metric(0);
				scenario.UEs.get(user_w_highest_metric).numAllocatedRB++;
			
			
			
		}*/
		ArrayList<User> Users= new ArrayList<User>();
		
		for(int j=0; j<scenario.numUEs;j++){
			Users.add(scenario.UEs.get(j));
		}
		Collections.sort(Users,User.Comparator_Cqi_Ascendent);
		
		
		int aux=numRB;
		for(User u: scenario.UEs){
			
			if(u.cqi.cqi==6){
				if(i%2==0){
					if(aux>=2){
						u.numAllocatedRB=2;
						aux-=2;
					}
				}else{
					if(aux>=1){
						u.numAllocatedRB=1;
						aux--;
					}
				}
			}else if(u.cqi.cqi==7){
				if(i%2==0){
					if(aux>=1){
						u.numAllocatedRB=1;
						aux--;
					}
				}else{
					if(aux>=2){
						u.numAllocatedRB=2;
						aux-=2;
					}
				}
			}
		}
		
		Collections.sort(Users,User.Comparator_Cqi_Descendent);
		/*while(aux>0){
			for(User u: scenario.UEs){
				if(aux>0){
					u.numAllocatedRB++;
					aux--;
				}
			}
		}*/
		if(aux>0){
			for(int a=0; a<aux;a++){
				
				if(last_served==scenario.numUEs-1){
					if(sending_data[0]){
						scenario.UEs.get(0).numAllocatedRB++;
						last_served=0;
					}
				}else{
					if(sending_data[last_served+1]){
						scenario.UEs.get(last_served+1).numAllocatedRB++;
						last_served++;
					}
				}
			
			
			
			}
		}
		/*if(aux>0){
			for(User u: scenario.UEs){
				if(aux>0){
					u.numAllocatedRB++;
					aux--;
				}
			}
		}*/
	}
	
	public static void allocate_to_solve(int i,Workbook wb, int granularity) throws IOException{
		int aux=numRB;
		double buffer_limit_to_be_in_risk_toStall=1;
		ArrayList<User> Users_Rebuffering= new ArrayList<User>();
		ArrayList<User> Users_in_risk_to_Stall= new ArrayList<User>();
		ArrayList<User> Users_not_Rebuffering_neither_in_risk_to_Stall=new ArrayList<User>();
		ArrayList<User> Users_Requesting_first_segments= new ArrayList<User>();

		for(int j=0; j<scenario.numUEs;j++){
			scenario.UEs.get(j).numAllocatedRB=0;
		}
		
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
				for(int j=0;j<scenario.numUEs;j++){
					scenario.UEs.get(j).numAllocatedRB=0;
					if(scenario.UEs.get(j).buffer<buffer_limit_to_be_in_risk_toStall && !scenario.UEs.get(j).rebuffering  && !scenario.UEs.get(j).requesting_initial_segments){ // vai haver stall se nada for alocado
						Users_in_risk_to_Stall.add(scenario.UEs.get(j));
					}
					if(scenario.UEs.get(j).rebuffering && !scenario.UEs.get(j).requesting_initial_segments){
						Users_Rebuffering.add(scenario.UEs.get(j));
					}
					if(!scenario.UEs.get(j).rebuffering && !scenario.UEs.get(j).requesting_initial_segments && scenario.UEs.get(j).buffer>=buffer_limit_to_be_in_risk_toStall){
						Users_not_Rebuffering_neither_in_risk_to_Stall.add(scenario.UEs.get(j));
					}
					
					if(scenario.UEs.get(j).requesting_initial_segments && !scenario.UEs.get(j).rebuffering){
						Users_Requesting_first_segments.add(scenario.UEs.get(j));
					}
					
				}
				Collections.sort(Users_Requesting_first_segments, User.Comparator_Cqi_Descendent);
				Collections.sort(Users_in_risk_to_Stall, User.Comparator_Buffer_Ascendent);
				Collections.sort(Users_Rebuffering, User.Comparator_Buffer_Ascendent);
				Collections.sort(Users_not_Rebuffering_neither_in_risk_to_Stall, User.Comparator_Cqi_Ascendent);
				int size[]={Users_Requesting_first_segments.size(),Users_in_risk_to_Stall.size(),Users_Rebuffering.size(),Users_not_Rebuffering_neither_in_risk_to_Stall.size()};
				export_sizes_to_Excel(wb, i, granularity,size);
				
				
				if(Users_not_Rebuffering_neither_in_risk_to_Stall.size()!=0){
					if(numRB>=scenario.numUEs){
						for(User u:scenario.UEs){
							u.numAllocatedRB++;
							aux--;
						}
					}
				}
				
				
				if(Users_Requesting_first_segments.size()!=0){
					
					
					while(aux>0){
						for(User u:Users_Requesting_first_segments){
							if(aux>0){
								u.numAllocatedRB++;
								aux--;
							}else{
								if(Users_not_Rebuffering_neither_in_risk_to_Stall.size()!=0){
									int k=find_user_w_highest_buffer(Users_not_Rebuffering_neither_in_risk_to_Stall);
									if(Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB>0){
										Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB--;	
										u.numAllocatedRB++;
									}
								}
							}
						}
					}
				}
				

					//if(Users_in_risk_to_Stall.size()!=0){
						
							while(aux>0 && Users_in_risk_to_Stall.size()!=0){
								for(Iterator<User> it = Users_in_risk_to_Stall.iterator(); it.hasNext();){
									User u=it.next();
									if(aux>0){
										u.numAllocatedRB++;
										aux--;
										/*if(u.verify_if_user_will_be_removed_from_users_in_risk_to_stall(bits_per_RB_per_CQI, buffer_limit_to_be_in_risk_toStall)){
											it.remove();
											Users_not_Rebuffering_neither_in_risk_to_Stall.add(u);
										}*/
									}else{
										if(Users_not_Rebuffering_neither_in_risk_to_Stall.size()!=0){
										int k=find_user_w_highest_buffer(Users_not_Rebuffering_neither_in_risk_to_Stall);
										if(Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB>0){
											Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB--;	
											u.numAllocatedRB++;
										}
										}
										
									}
								}
							}
						
							
					
					//if(Users_Rebuffering.size()!=0){
						while(aux>0 && Users_Rebuffering.size()!=0){
							for(Iterator<User> it = Users_Rebuffering.iterator(); it.hasNext();){
								User u=it.next();
								
									//int num_RB=(int) (number_segments_requested_during_rebuffering*segment_size*u.requested_video.video_requested_BitRate*Math.pow(10, 6)/1000/bits_per_RB_per_CQI[u.cqi.cqi]);
								if(aux>0){
									u.numAllocatedRB++;
									aux--;
									/*if(u.verify_if_user_will_be_removed_from_users_rebuffering(bits_per_RB_per_CQI, buffer_limit_to_be_in_risk_toStall, segment_size)){
										it.remove();
										Users_not_Rebuffering_neither_in_risk_to_Stall.add(u);
									} */
								
								}else{
									if(Users_not_Rebuffering_neither_in_risk_to_Stall.size()!=0){
									int k=find_user_w_highest_buffer(Users_not_Rebuffering_neither_in_risk_to_Stall);
									if(Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB>0){
										Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB--;	
										u.numAllocatedRB++;
									}
									}
								}
									
								
							}
						
					}
						
						
						if(Users_not_Rebuffering_neither_in_risk_to_Stall.size()!=0){
							/*int k= find_user_lost_more_cqi(Users_not_Rebuffering_neither_in_risk_to_Stall);
							Users_not_Rebuffering_neither_in_risk_to_Stall.get(k).numAllocatedRB+=aux;
							aux=0;*/
							Collections.sort(Users_not_Rebuffering_neither_in_risk_to_Stall, User.Comparator_Cqi_Ascendent);
							while(aux>0){
								for(User u:Users_not_Rebuffering_neither_in_risk_to_Stall){
									if(aux>0){
										u.numAllocatedRB++;
										aux--;
									}
								}
							}
						}			
				
				
		}
	}
	
	public static void allocate_RB_by_metric(int i){
		int user_w_highest_metric=0;
		int aux=numRB;
		boolean some_to_allocate=false;
		int user_to_allocate=0;
		
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
				if(some_to_allocate==false){
					if(scenario.UEs.get(j).to_allocate==true){
						some_to_allocate=true;
						user_to_allocate=j;
					} 
				}
				
				if(scenario.UEs.get(j).buffer<buffer_size && scenario.UEs.get(j).buffer>buffer_size-1000){
					scenario.UEs.get(j).to_allocate=false;						
				}
				if(scenario.UEs.get(j).to_allocate==false){
					scenario.UEs.get(j).interrupt_allocating--;
					if(scenario.UEs.get(j).interrupt_allocating==0){
						scenario.UEs.get(j).to_allocate=true;
						scenario.UEs.get(j).interrupt_allocating=interrupt_allocating;
					}
				}
			}
			
			
				calculate_average_throughputs(i-1);
		        
		    
			for(int rb=0;rb<numRB;rb++){
				for (int j = 0; j < user_metrics.length; j++) {
					//calculate_average_throughputs(throughputs, i-1);
			        user_metrics[j]=(buffer_size-scenario.UEs.get(j).buffer)/buffer_size*scenario.UEs.get(j).cqi.cqi/average_throughputs[j];
			    }
				
				if(some_to_allocate==true){
					user_w_highest_metric=find_USER_w_highest_metric(user_to_allocate);
					scenario.UEs.get(user_w_highest_metric).numAllocatedRB++;
				}
				
				
			}
		}
		
	}
	
	public static void allocate_RB_by_buffer(int i){
		int user_w_highest_metric=0;
		int aux=numRB;
		
		if(i==0){
			while(aux>0){
				for(int k=0;k<scenario.numUEs;k++){
					if(aux>0){
						scenario.UEs.get(k).numAllocatedRB++;
						aux--;
					}
				}
			}
		}else{
			for (int j = 0; j < user_metrics.length; j++) {
				scenario.UEs.get(j).numAllocatedRB=0;
			}
			for(int rb=0;rb<numRB;rb++){
				calculate_user_metrics();
				user_w_highest_metric=find_USER_w_highest_metric(0);
				scenario.UEs.get(user_w_highest_metric).numAllocatedRB++;
			}
		}
		
		
		
	}
	
	private static int find_USER_w_highest_metric(int j) {

		   double maxValue = user_metrics[j];
		   int max_index=j;
		   for (int i = 0; i < user_metrics.length; i++) {
			   //if(scenario.UEs.get(i).to_allocate==true){
		        if (user_metrics[i] > maxValue) {
		            maxValue = user_metrics[i];
		            max_index=i;  
		        }
			  // }
		   }
		   return max_index;
		    	
	}

	public static void calculate_user_metrics(){
		for (int i = 0; i < user_metrics.length; i++) {
	        user_metrics[i]=buffer_size-scenario.UEs.get(i).buffer;
	    }
	}
	
	private static double calculate_qt(int j){
		double qt=1000*(1000*scenario.numRB*scenario.UEs.get(j).bitRate.bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]/1000)/(scenario.UEs.get(j).requested_video.video_requested_BitRate*Math.pow(10, 6));
		return qt;
	}
	
	private static int calculate_RB_to_have_qt_equal_1(int j){
		//double RB=1*(scenario.UEs.get(j).requested_video.video_requested_BitRate*Math.pow(10, 6))/(1000*scenario.UEs.get(j).bitRate.bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]);
		double RB=average_throughputs[j]/(1000*scenario.UEs.get(j).bitRate.bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi]);
		return (int) RB;
	}
	
	private static void send_data_to_users(int i) {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.UEs.size();j++){
			if(sending_data[j]){
				
				scenario.UEs.get(j).received_bits=scenario.UEs.get(j).received_bits+scenario.UEs.get(j).numAllocatedRB*bits_per_RB_per_CQI[scenario.UEs.get(j).cqi.cqi];
				double qt=1000*(scenario.UEs.get(j).bitRate.throughput/1000)/(scenario.UEs.get(j).requested_video.video_requested_BitRate*Math.pow(10, 6)); // 1000*(Nºbits/ms) / (Nbits/segmento) =1000* Nºsegmentos/ms =1000*Nºsegundos de video enviados num RB=1000*Nºmilisegundos de video enviados num RB
				scenario.UEs.get(j).fill_buffer(qt);
				//System.out.println(qt);
				if(scenario.UEs.get(j).rebuffering){
					if(scenario.UEs.get(j).buffer>=recover_stall_buffer_amount_to_start_draining){
						scenario.UEs.get(j).recovering_stall=false;
					}else{
						scenario.UEs.get(j).duration_rebuffering_event++;
						
					}
					
					if(scenario.UEs.get(j).received_bits>=number_segments_requested_during_rebuffering*scenario.UEs.get(j).requested_video.number_bits_per_segment){
						scenario.UEs.get(j).rebuffering=false;
						sending_data[j]=false;	
						scenario.UEs.get(j).received_bits=0;
					}
				}else if(scenario.UEs.get(j).requesting_initial_segments){
					if(scenario.UEs.get(j).buffer>=initial_buffer_amount_to_start_draining){
						scenario.UEs.get(j).initial_buffer_filling=false;
					}else{
						scenario.UEs.get(j).duration_rebuffering_event++; // se considerar initial buffering igual a rebuffering
						scenario.UEs.get(j).duration_initial_buffering++;
					}
					
					if(scenario.UEs.get(j).received_bits>=number_segments_initially_requested*scenario.UEs.get(j).requested_video.number_bits_per_segment){
						scenario.UEs.get(j).requesting_initial_segments=false;
						sending_data[j]=false;	
						scenario.UEs.get(j).received_bits=0;
					}
				}else{
					if(scenario.UEs.get(j).received_bits>=scenario.UEs.get(j).requested_video.number_bits_per_segment){
						sending_data[j]=false;
						scenario.UEs.get(j).received_bits=0;
					}
				}
				
				
			}
		
		}
		
		
	}
	
	private static void update_bitrate() {
		// TODO Auto-generated method stub
		for(User user: scenario.UEs){
			user.bitRate.throughput=0;
		}
		for(int j=0;j<scenario.UEs.size();j++){
			if(sending_data[j]){
				scenario.UEs.get(j).update_bitrate();
			}
		}
	}
	
	
	public static void export_MOS_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){
        	Row row_UEs = wb.getSheetAt(6).createRow((short)0);
        	Row row_mos = wb.getSheetAt(6).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UEmos"+j));
        		
        			
        			row_mos.createCell(0).setCellValue(i);
        			row_mos.createCell(j+1).setCellValue(scenario.UEs.get(j).mos);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_mos = wb.getSheetAt(6).createRow((short)(i/granularity)+1);
	        	row_mos.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_mos.createCell(j+1).setCellValue(scenario.UEs.get(j).mos);
	    		}
	        }
        }
	}
	
	
	public static void export_avg_mos_to_Excel(Workbook wb, int simulation, String algorithm) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    Arrays.sort(avg_mos);

	    	if(simulation==0){
	        	Row row_title = wb.getSheetAt(0).createRow((short)0);
	    		row_title.createCell(simulation).setCellValue(createHelper.createRichTextString("Avg.MOS + "+algorithm));
	        	
	        	for(int j=0;j<scenario.UEs.size();j++){
	        		
	        		Row row_mos = wb.getSheetAt(0).createRow((short)j+1);
	        		row_mos.createCell(simulation).setCellValue(avg_mos[j]);
	        		
	    		}		
        	}else{
        		Row row_title = wb.getSheetAt(0).getRow((short)0);
	    		row_title.createCell(simulation).setCellValue(createHelper.createRichTextString("Avg.MOS + "+algorithm));
	        	
	        	for(int j=0;j<scenario.numUEs;j++){
	        		
	        		Row row_mos = wb.getSheetAt(0).getRow((short)j+1);
	        		//System.out.println(avg_mos.length);
	        		row_mos.createCell(simulation).setCellValue(avg_mos[j]);
	        		
	    		}
        	}
        
	}
	
	public static void export_statistical_avg_mos_to_Excel(Workbook wb, int simulation, String algorithm, int collumn, int num_simulations) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    Arrays.sort(avg_mos);
	    
	    double mos_avg=0;
	    for(int j=0; j<avg_mos.length;j++){
	    	mos_avg+=avg_mos[j];
	    }
	    mos_avg=mos_avg/avg_mos.length;
	    
	    calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
	    	if(simulation==0){
	    		if(collumn==1){
	    			Row row_title = wb.getSheetAt(0).createRow((short)0);
		    		row_title.createCell(collumn).setCellValue(createHelper.createRichTextString("% + "+algorithm));
		    		Row row_mos = wb.getSheetAt(0).createRow((short)simulation+1);
	        		row_mos.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	        		row_title.createCell(0).setCellValue(num_simulations);
	    		}else{
	    			Row row_title = wb.getSheetAt(0).getRow((short)0);
		    		row_title.createCell(collumn).setCellValue(createHelper.createRichTextString("% + "+algorithm));
		    		Row row_mos = wb.getSheetAt(0).getRow((short)simulation+1);
	        		row_mos.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	    		}
	        	
	        		
	    			
        	}else{
        		
	        		if(collumn==1){
	        			Row row_mos = wb.getSheetAt(0).createRow((short)simulation+1);
		        		row_mos.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	        		}else{
	        			Row row_mos = wb.getSheetAt(0).getRow((short)simulation+1);
	        			
		        		row_mos.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	        		}
	        		
	        		
	    		
        	}
        
	}
	
	public static void export_mos_evolution_to_Excel(Workbook wb, int collumn, String algorithm,int i, int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
       

       
		
		
		double avg=0;
		for(int j=0; j<scenario.numUEs;j++){
			avg=avg+scenario.UEs.get(j).mos;
		}
		avg=avg/scenario.numUEs;
			
	    	if(simulation==0){
	        	Row row_title = wb.getSheetAt(0).createRow((short)0);
	    		if(i==0){
	    			System.out.println(algorithm+collumn+simulation);
	    			row_title.createCell(collumn).setCellValue(createHelper.createRichTextString("Avg.MOS + "+algorithm));
	    			Row row_mos = wb.getSheetAt(0).createRow((short)1);
	        		row_mos.createCell(0).setCellValue(i);
	        		row_mos.createCell(collumn).setCellValue(avg);
	    		}else{
	    			if(i%granularity==0){
	    				Row row_mos = wb.getSheetAt(0).createRow((short)(i/granularity)+1);
		        		row_mos.createCell(0).setCellValue(i);
		        		row_mos.createCell(collumn).setCellValue(avg);
	    			}
	    		}

        	}else{
        		Row row_title = wb.getSheetAt(0).getRow((short)0);
	    		if(i==0){
	    			System.out.println(algorithm+collumn+simulation);
	    			row_title.createCell(collumn).setCellValue(createHelper.createRichTextString("Avg.MOS + "+algorithm));
	    			Row row_mos = wb.getSheetAt(0).getRow((short)1);
	        		row_mos.createCell(0).setCellValue(i);
	        		row_mos.createCell(collumn).setCellValue(avg);
	    		}else{
	    			if(i%granularity==0){
	    				Row row_mos = wb.getSheetAt(0).getRow((short)(i/granularity)+1);
		        		row_mos.createCell(0).setCellValue(i);
		        		row_mos.createCell(collumn).setCellValue(avg);
	    			}
	    		}
        	}
        
	}
	
	public static void calculate_percent_rebuf(int i){
		for(int j=0;j<scenario.UEs.size();j++){
    		scenario.UEs.get(j).percent_rebuf=scenario.UEs.get(j).duration_rebuffering_event/i*100;    		
		}
		
	}
	
	public static void export_percent_rebuf_to_Excel(Workbook wb, int simulation, String algorithm) throws IOException{
		calculate_percent_rebuf(numMillisecondToSimulate);
		
        CreationHelper createHelper = wb.getCreationHelper();
	    Arrays.sort(avg_mos);

	    	if(simulation==0){
	        	Row row_title = wb.getSheetAt(0).createRow((short)0);
	    		row_title.createCell(simulation).setCellValue(createHelper.createRichTextString("%Rebuf + "+algorithm));
	        	
	        	for(int j=0;j<scenario.UEs.size();j++){
	        		
	        		Row row_mos = wb.getSheetAt(0).createRow((short)j+1);
	        		row_mos.createCell(simulation).setCellValue(scenario.UEs.get(j).percent_rebuf);
	        		
	    		}		
        	}else{
        		Row row_title = wb.getSheetAt(0).getRow((short)0);
	    		row_title.createCell(simulation).setCellValue(createHelper.createRichTextString("%Rebuf + "+algorithm));
	        	
	        	for(int j=0;j<scenario.numUEs;j++){
	        		
	        		Row row_mos = wb.getSheetAt(0).getRow((short)j+1);
	        		//System.out.println(avg_mos.length);
	        		row_mos.createCell(simulation).setCellValue(scenario.UEs.get(j).percent_rebuf);
	        		
	    		}
        	}
        
	}
	
	
	
	public static void export_percentage_users_w_MOS_bigger_than_limit_to_Excel(Workbook wb, int simulation,int collumn, String algorithm) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

	    	if(simulation==0){
	    		if(collumn==1){
		        	Row row_title = wb.getSheetAt(0).createRow((short)0);
		    		row_title.createCell(0).setCellValue(createHelper.createRichTextString("Number Users"));
		    		row_title.createCell(collumn).setCellValue(createHelper.createRichTextString(algorithm));
		    		
		        	Row data=wb.getSheetAt(0).createRow((short)(1));
		        	data.createCell(0).setCellValue(scenario.numUEs);
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
		        	
	    		}else{
	    			Row row_title = wb.getSheetAt(0).getRow((short)0);
	    			row_title.createCell(collumn).setCellValue(createHelper.createRichTextString(algorithm));
		        	Row data=wb.getSheetAt(0).getRow((short)1);
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	    		}
        	}else{
        		if(collumn==1){
	        		Row data=wb.getSheetAt(0).createRow((short)(simulation+1));
	        		data.createCell(0).setCellValue(scenario.numUEs);
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
        		}else{
        			Row data=wb.getSheetAt(0).getRow((short)(simulation+1));
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
        		}
        	}
        
	}
	
	public static void export_percentage_users_w_MOS_bigger_than_limit_to_Excel_to_1_algorithm(Workbook wb, int simulation,int collumn, String algorithm) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    //calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
        calculate_percentage_of_users_w_mos_above_limit_strategy1();

	    	if(simulation==0){
	    		if(collumn==1){
	    			Row row_title = wb.getSheetAt(0).createRow((short)0);
		    		row_title.createCell(collumn).setCellValue(scenario.numUEs);
		    		
		    		Row data=wb.getSheetAt(0).createRow((short)(1));
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	    		}else{
	    			Row row_title = wb.getSheetAt(0).getRow((short)0);
		    		row_title.createCell(collumn).setCellValue(scenario.numUEs);
		    		
		    		Row data=wb.getSheetAt(0).getRow((short)(1));
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
	    		}
	    		
	    		
	        	
        	}else{
        		if(collumn==1){
	        		Row data=wb.getSheetAt(0).createRow((short)(simulation+1));
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
        		}else{
        			Row data=wb.getSheetAt(0).getRow((short)(simulation+1));
		        	data.createCell(collumn).setCellValue(percetage_users_w_mos_greater_than_limit);
        		}
        	}
        
	}
	
	public static void export_number_users_w_MOS_lower_than_Z_limit_to_Excel_to_1_algorithm(Workbook wb, int simulation,int collumn, String algorithm) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    //calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
        //calculate_percentage_of_users_w_mos_above_limit();
        calculate_number_of_users_w_mos_bellow_Z_limit();
        
	    	if(simulation==0){
	    		if(collumn==1){
	    			Row row_title = wb.getSheetAt(0).createRow((short)0);
		    		row_title.createCell(collumn).setCellValue(scenario.numUEs);
		    		
		    		Row data=wb.getSheetAt(0).createRow((short)(1));
		        	data.createCell(collumn).setCellValue(number_users_w_mos_lower_than_Z_limit);
	    		}else{
	    			Row row_title = wb.getSheetAt(0).getRow((short)0);
		    		row_title.createCell(collumn).setCellValue(scenario.numUEs);
		    		
		    		Row data=wb.getSheetAt(0).getRow((short)(1));
		        	data.createCell(collumn).setCellValue(number_users_w_mos_lower_than_Z_limit);
	    		}
	    		
	    		
	        	
        	}else{
        		if(collumn==1){
	        		Row data=wb.getSheetAt(0).createRow((short)(simulation+1));
		        	data.createCell(collumn).setCellValue(number_users_w_mos_lower_than_Z_limit);
        		}else{
        			Row data=wb.getSheetAt(0).getRow((short)(simulation+1));
		        	data.createCell(collumn).setCellValue(number_users_w_mos_lower_than_Z_limit);
        		}
        	}
        
	}
	
	public static void export_buffers_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){
        	Row row_UEs = wb.getSheetAt(0).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(0).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UEbuffer"+j));
        		
        			
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).buffer);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(0).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).buffer);
	    		}
	        }
        }
	}
	
	public static void export_sizes_to_Excel(Workbook wb, int i,int granularity, int[] size) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){
        	Row row_UEs = wb.getSheetAt(5).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(5).createRow((short)1);
        	for(int j=0;j<4;j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("Size"+j));
        		
        			
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(size[j]);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(5).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<4;j++){
	    			row_buffers.createCell(j+1).setCellValue(size[j]);
	    		}
	        }
        }
        //System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
	}
	
	
	public static void export_CQIs_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){

        	Row row_UEs = wb.getSheetAt(1).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(1).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UEcqi"+j));      		
        		
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).cqi.cqi);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(1).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).cqi.cqi);
	    		}
	        }
        }
	}
	
	public static void export_qualities_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){

        	Row row_UEs = wb.getSheetAt(4).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(4).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UEquality"+j));      		
        		
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).requested_video.video_requested_BitRate);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(4).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).requested_video.video_requested_BitRate);
	    		}
	        }
        }
	}
	public static void export_rb_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){

        	Row row_UEs = wb.getSheetAt(3).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(3).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UErb"+j));      		
        		
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).numAllocatedRB);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(3).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).numAllocatedRB);
	    		}
	        }
        }
	}
	
	public static void export_throughputs_to_Excel(Workbook wb, int i,int granularity) throws IOException{

        CreationHelper createHelper = wb.getCreationHelper();
	    

        if(i==0){

        	Row row_UEs = wb.getSheetAt(2).createRow((short)0);
        	Row row_buffers = wb.getSheetAt(2).createRow((short)1);
        	for(int j=0;j<scenario.UEs.size();j++){
        		wb.getSheetAt(2).autoSizeColumn(j);
        		row_UEs.createCell(j+1).setCellValue(createHelper.createRichTextString("UEthroughput"+j));
        		
        				
        			row_buffers.createCell(0).setCellValue(i);
        			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).bitRate.throughput);
        		
    		}
        }else{
        
	        if(i%granularity==0){
	        	Row row_buffers = wb.getSheetAt(2).createRow((short)(i/granularity)+1);
	        	row_buffers.createCell(0).setCellValue(i);
	    		for(int j=0;j<scenario.UEs.size();j++){
	    			row_buffers.createCell(j+1).setCellValue(scenario.UEs.get(j).bitRate.throughput);
	    		}
	        }
        }
	}
		
		private static void update_CQIs(int i,int granularity) {
			// TODO Auto-generated method stub
			if(i%granularity==0){
				for(User user : scenario.UEs){
					user.cqi.update_cqi(i);
				}
			}
		}
		
		private static void update_requested_segments(int i,int granularity, String mode) {
			// TODO Auto-generated method stub

			if(mode.equals("default")){
				for(int j=0;j<scenario.UEs.size();j++){
					if(scenario.UEs.get(j).received_bits>=number_segments_initially_requested*scenario.UEs.get(j).requested_video.number_bits_per_segment & scenario.UEs.get(j).requesting_initial_segments){
						sending_data[j]=false;
						scenario.UEs.get(j).requesting_initial_segments=false;
					}
					if(scenario.UEs.get(j).received_bits>=scenario.UEs.get(j).requested_video.number_bits_per_segment & !scenario.UEs.get(j).requesting_initial_segments){//received an entire segment
						sending_data[j]=false;
						scenario.UEs.get(j).received_bits=0;
							
						if(i-scenario.UEs.get(j).last_request>=minimum_period_to_request){ //5 ms is the minimum time interval between segment requests
							if(average_throughputs[j]>scenario.UEs.get(j).requested_video.video_requested_BitRate*1000000){
								scenario.UEs.get(j).requested_video.request_better_video_quality();
								scenario.UEs.get(j).last_request=i;
								sending_data[j]=true;
							}else{
								scenario.UEs.get(j).requested_video.request_worse_video_quality();
								sending_data[j]=true;
								scenario.UEs.get(j).last_request=i;
							}
						}else{//user have to wait until 5 ms have passed since the last segment request
							sending_data[j]=false;
							scenario.UEs.get(j).received_bits=0;
						}
					}
					
				}
				
				
			}else if(mode.equals("QDASH")){
				
				for(int j=0;j<scenario.UEs.size();j++){				
						
					if((scenario.UEs.get(j).received_bits>=scenario.UEs.get(j).requested_video.number_bits_per_segment && !scenario.UEs.get(j).requesting_initial_segments && scenario.UEs.get(j).rebuffering== false)
						|| (scenario.UEs.get(j).received_bits>=number_segments_initially_requested*scenario.UEs.get(j).requested_video.number_bits_per_segment && scenario.UEs.get(j).requesting_initial_segments && scenario.UEs.get(j).rebuffering==false) 
						|| (scenario.UEs.get(j).rebuffering==true && scenario.UEs.get(j).buffer>number_segments_requested_during_rebuffering*segment_size)){	
					
						scenario.UEs.get(j).received_bits=0;

							if(scenario.UEs.get(j).rebuffering==true && scenario.UEs.get(j).buffer>number_segments_requested_during_rebuffering*segment_size){
								scenario.UEs.get(j).received_bits=scenario.UEs.get(j).bits_already_sent_from_last_segment_before_rebuffering;
								scenario.UEs.get(j).bits_already_sent_from_last_segment_before_rebuffering=0;
							}
							scenario.UEs.get(j).requesting_initial_segments=false;
							scenario.UEs.get(j).rebuffering=false;
							sending_data[j]=false;
							
							if(i-scenario.UEs.get(j).last_request>=minimum_period_to_request){//5 ms is the minimum time interval between segment requests			
								double bw_estimated=Math.round(scenario.UEs.get(j).update_bandwidth_estimated(w_weight_factor, teta_estimation_period,i,throughputs,j));
								scenario.UEs.get(j).requested_video.calculate_l_best(bw_estimated);
								
								if(scenario.UEs.get(j).requested_video.l_best>=scenario.UEs.get(j).requested_video.video_requested_BitRate){
									scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.l_best,scenario.UEs.get(j).requested_video.l_best_index);
									scenario.UEs.get(j).last_request=i;
									sending_data[j]=true;
									
								}else if(scenario.UEs.get(j).requested_video.l_best<scenario.UEs.get(j).requested_video.video_requested_BitRate){									
									
									if(scenario.UEs.get(j).requested_video.l_best_index<(scenario.UEs.get(j).requested_video.requested_quality-1)){
										//System.out.println("c");
										double t_best_plus_1=0;
										t_best_plus_1=((scenario.UEs.get(j).buffer))/(1-(bw_estimated/(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.l_best_index+1]*1000000)));
										double n_best_plus_1=t_best_plus_1*bw_estimated/(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.l_best_index+1]*1000000*segment_size);
										//System.out.println("k"+n_best_plus_1);
										if(n_best_plus_1>=1){
											scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.l_best_index+1],scenario.UEs.get(j).requested_video.l_best_index+1);
											//System.out.println("a");
											scenario.UEs.get(j).last_request=i;
											sending_data[j]=true;
										}else{
											scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.l_best,scenario.UEs.get(j).requested_video.l_best_index);
											//System.out.println("b");
											scenario.UEs.get(j).last_request=i;
											sending_data[j]=true;
										}								
									}else{
										scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.l_best,scenario.UEs.get(j).requested_video.l_best_index);
										scenario.UEs.get(j).last_request=i;
										sending_data[j]=true;			
									}
								}
							}else{//user have to wait until 5 ms have passed since the last segment request
								sending_data[j]=false;
								scenario.UEs.get(j).received_bits=0;	
							}
						}
						
				
					}
			}else if(mode.equals("QAAD")){
				for(int j=0;j<scenario.UEs.size();j++){
				//	if(j==1 && /*scenario.UEs.get(j).received_bits>=scenario.UEs.get(j).requested_video.number_bits_per_segment &&*/ !scenario.UEs.get(j).requesting_initial_segments  && scenario.UEs.get(j).rebuffering== false){System.out.println("OIIIIIIIIIIII   "+i);}
					
					if(sending_data[j]==false){
						
						if(i-scenario.UEs.get(j).last_request>=minimum_period_to_request){//5 ms is the minimum time interval between segment requests
							double bw_estimated=Math.round(scenario.UEs.get(j).update_bandwidth_estimated(w_weight_factor, teta_estimation_period,i,throughputs,j));
							scenario.UEs.get(j).requested_video.calculate_l_best(bw_estimated);
								
							if(scenario.UEs.get(j).requested_video.l_best==scenario.UEs.get(j).requested_video.video_requested_BitRate){					
								scenario.UEs.get(j).last_request=i;
								sending_data[j]=true;
									
							}else if(scenario.UEs.get(j).requested_video.l_best>scenario.UEs.get(j).requested_video.video_requested_BitRate){
								if(scenario.UEs.get(j).buffer>u_marginal_buffer_lenght){
									scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality+1],scenario.UEs.get(j).requested_video.requested_quality+1);
									scenario.UEs.get(j).last_request=i;
									sending_data[j]=true;
									scenario.UEs.get(j).num_quality_changes++;
								}else{
									scenario.UEs.get(j).last_request=i;
									sending_data[j]=true;
								}
							}else{
								int k=0;
								double t=0;
								double n=0;
								do{
									t=(scenario.UEs.get(j).buffer-sigma)/(1-(bw_estimated/(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality-k]*1000000)));	
									n=t*bw_estimated/(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality-k]*1000000*segment_size);
									k=k+1;	
								}while(n<1 && k<(scenario.UEs.get(j).requested_video.requested_quality-1));

								scenario.UEs.get(j).requested_video.update_requested_video_quality(scenario.UEs.get(j).requested_video.representationBitRate[scenario.UEs.get(j).requested_video.requested_quality-k],scenario.UEs.get(j).requested_video.requested_quality-k);
								scenario.UEs.get(j).last_request=i;
								sending_data[j]=true;
									if(k!=0){
										scenario.UEs.get(j).num_quality_changes++;
									}
							}
							

						scenario.UEs.get(j).served_qualities.add((double)(scenario.UEs.get(j).requested_video.requested_quality+1));
						
						}else{//user have to wait until 5 ms have passed since the last segment request
							sending_data[j]=false;
							scenario.UEs.get(j).received_bits=0;
						}
					}

							
						}

					}
			}
		
				
			
		
		
		private static void write_buffers(int i, double[][] buffers,int granularity) {
			for(int j=0;j<scenario.numUEs;j++){
				if(i==0){
					buffers[0][j]=scenario.UEs.get(j).buffer;
				}else{
					if(i%granularity==0){
						buffers[(i/granularity)][j]=scenario.UEs.get(j).buffer;
					}
				}
			}
		}
		
		private static void write_cqis(int i, int[][] cqis,int granularity) {
			if(i%granularity==0){
				for(int j=0;j<scenario.numUEs;j++){
					cqis[(i/granularity)][j]=scenario.UEs.get(j).cqi.cqi;
				}
			}
		}
		
		/*private static void calculate_standard_cqi_desviation(int i, int[][] cqis, int granularity){
			for(int j=0;j<scenario.numUEs;j++){
				scenario.UEs.get(j).cqi.calculate_standard_cqi_desviation(i,cqis,granularity,j);
			}
		}
		*/
		private static void write_throughputs(int i, double[][] throughputs,int granularity) {
			
				for(int j=0;j<scenario.numUEs;j++){
					if(i==0){
						throughputs[0][j]=scenario.UEs.get(j).bitRate.throughput;
					}else{
						if(i%granularity==0){
							throughputs[(i/granularity)][j]=scenario.UEs.get(j).bitRate.throughput;
						}
					}
					
				}
		}
		
		private static void write_mos(int i, double mos[][], int granularity) {
			
			for(int j=0;j<scenario.numUEs;j++){
				if(scenario.UEs.get(j).mos>=mos_limit && i>scenario.UEs.get(j).duration_initial_buffering){
					//System.out.println(scenario.UEs.get(j).mos);
					period_above_mos_limit[j]++;
				}
				/*if(scenario.UEs.get(j).mos>2 && scenario.UEs.get(j).mos<2.1){
					System.out.println(i);
				}*/
				if(i==0){
					mos[0][j]=scenario.UEs.get(j).mos;
				}else{
					if(i%granularity==0){
						mos[(i/granularity)][j]=scenario.UEs.get(j).mos;
					}
				}
				
			}
	}
		
	
		public static int find_user_w_highest_buffer(ArrayList<User> Users){
			double maxValue = Users.get(0).buffer;
			int max_index=0;
			   for (int i = 1; i < Users.size(); i++) {
				   if (Users.get(i).buffer > maxValue) {
			           maxValue = Users.get(i).buffer;
			           max_index=i;  
			       }
			   }
			   return max_index;
		}
		public static int find_user_lost_more_cqi(ArrayList<User> Users){
			double minValue = Users.get(0).cqi.cqi_variation;
			int min_index=0;
			   for (int i = 1; i < Users.size(); i++) {
				   if (Users.get(i).cqi.cqi_variation < minValue) {
			           minValue = Users.get(i).cqi.cqi_variation;
			           min_index=i;  
			       }
			   }
			   return min_index;
		}
		
		public static int find_user_w_lowest_MOS(ArrayList<User> Users){
			double maxValue = Users.get(0).mos;
			int max_index=0;
			   for (int i = 1; i < Users.size(); i++) {
				   if (Users.get(i).mos < maxValue) {
			           maxValue = Users.get(i).mos;
			           max_index=i;  
			       }
			   }
			   return max_index;
		}
		
		public static int find_user_w_highest_cqi(ArrayList<User> Users){
			double maxValue = Users.get(0).cqi.cqi;
			int max_index=0;
			   for (int i = 1; i < Users.size(); i++) {
				   if (Users.get(i).cqi.cqi > maxValue) {
			           maxValue = Users.get(i).cqi.cqi;
			           max_index=i;  
			       }
			   }
			   return max_index;
		}
		
		public static int find_user_w_highest_buffer(double buffers[]){
			double maxValue = buffers[0];
			int max_index=0;
			   for (int i = 1; i < buffers.length; i++) {
				   if (buffers[i] > maxValue) {
			           maxValue = buffers[i];
			           max_index=i;  
			       }
			   }
			   return max_index;
		}
		
		public static int find_user_w_lowest_buffer(ArrayList<User> Users){
			double minValue = scenario.UEs.get(0).buffer;
			int min_index=0;
			   for (int i = 1; i < Users.size(); i++) {
				   if (Users.get(i).buffer < minValue) {
			           minValue = Users.get(i).buffer;
			           min_index=i;  
			       }
			   }
			   return min_index;
		}
		
		public static int find_user_w_lowest_buffer(double buffers[]){
			double minValue = scenario.UEs.get(0).buffer;
			int min_index=0;
			   for (int i = 1; i < buffers.length; i++) {
				   if (buffers[i] < minValue) {
			           minValue = buffers[i];
			           min_index=i;  
			       }
			   }
			   return min_index;
		}
		
		public static void print_general_metrics(double avg_mos_of_all_users){
			double avg_num_rebuffering_events=0;
			double avg_num_quality_changes=0;
			double avg_video_quality_served=0;
			double avg_rebuffering_duration=0;
			double standard_mos_desviation=calculate_standard_mos_desviation(avg_mos_of_all_users);
			
			for(int j=0;j<scenario.numUEs;j++){
				avg_num_rebuffering_events+=scenario.UEs.get(j).numRebufEvents;
				avg_num_quality_changes+=scenario.UEs.get(j).num_quality_changes;
				avg_video_quality_served+=scenario.UEs.get(j).avg_served_quality;
				avg_rebuffering_duration+=scenario.UEs.get(j).avg_duration_rebuffering_event;
			}
			avg_num_rebuffering_events=avg_num_rebuffering_events/scenario.numUEs;
			avg_num_quality_changes=avg_num_quality_changes/scenario.numUEs;
			avg_video_quality_served=(double) (avg_video_quality_served/scenario.numUEs);
			avg_rebuffering_duration=avg_rebuffering_duration/scenario.numUEs;
			
			
			System.out.println("");
			System.out.println("Average number of rebuffering events: "+avg_num_rebuffering_events);
			System.out.println("Average number of video quality changes: "+avg_num_quality_changes);
			System.out.println("Average video quality served: "+avg_video_quality_served + " Mbps");
			System.out.println("Average rebuffering duration : "+avg_rebuffering_duration+ " ms");
			System.out.println("Avg. Mos: "+avg_mos_of_all_users);	
			System.out.println("Standard MOS desviation: "+standard_mos_desviation);
			
			
		}
		
		public static double calculate_standard_mos_desviation(double avg_mos_of_all_users) {
			// TODO Auto-generated method stub
			double result=0;
			for(int j=0; j<scenario.numUEs;j++){
				result=result+Math.pow((scenario.UEs.get(j).avg_mos-avg_mos_of_all_users), 2);
			}
			result= Math.sqrt(result);
			result=result/scenario.numUEs;
			return result;
		}
		
		public static void print_user_metrics(){
			for(int j=0;j<scenario.numUEs;j++){
				System.out.println("User "+j);
				System.out.println(" Rebuf. events: "+scenario.UEs.get(j).numRebufEvents);
				System.out.println(" Video Qual. Changes: "+scenario.UEs.get(j).num_quality_changes);
				System.out.println(" Avg video quality: " +scenario.UEs.get(j).avg_served_quality);
				System.out.println(" % of time mos>"+mos_limit+": "+(double)(period_above_mos_limit[j]/numMillisecondToSimulate*100)+"%");
				if(scenario.UEs.get(j).numRebufEvents!=0){
					//if(j==1)System.out.println(scenario.UEs.get(1).avg_duration_rebuffering_event);
					System.out.println(" Avg rebuffering event duration: "+ scenario.UEs.get(j).avg_duration_rebuffering_event+ " ms");
				}else{
					System.out.println(" Avg rebuffering event duration: 0 ms");
				}
				System.out.println(" Avg. MOS: " +scenario.UEs.get(j).avg_mos);
			}
		}
	
		public static void export_to_Excel(Workbook wb, int i) throws IOException{
			export_buffers_to_Excel(wb,i, granularity_buffer_export_excel);
			export_CQIs_to_Excel(wb,i, granularity_cqi_export_excel);
			export_rb_to_Excel(wb,i, granularity_cqi_export_excel);
			export_throughputs_to_Excel(wb,i,granularity_throughput_export_excel);
			export_qualities_to_Excel(wb, i, granularity_throughput_export_excel);
			
			
			calculate_avg_duration_rebuf_events();
			calculate_Fis(i);
			calculate_avg_qualities();
			calculate_standard_quality_desviations();
			calculate_users_MOS(i);
			//System.out.println(scenario.UEs.get(2).mos);
			export_MOS_to_Excel(wb,i, granularity_mos_export_excel);
		}
		
		public static void generate_cqi_file_numbers(int nbrUEs){
			
			cqi_numbers=new int[nbrUEs];
			Random rand = new Random();
			for(int i=0;i<cqi_numbers.length;i++){
				cqi_numbers[i]=rand.nextInt(200)+1;
			}
		}
		
		
	public static void allocate_RB(String allocation_algorithm,int i,Workbook wb,int granularity) throws IOException{
		if(allocation_algorithm.equals("BET")){
			allocate_RB_by_avg_throughput(i);
		}else if(allocation_algorithm.equals("RoundRobin")){
			allocate_equal_nbr_RB1();
		}else if(allocation_algorithm.equals("ByBuffer")){
			allocate_RB_by_buffer(i);
		}else if(allocation_algorithm.equals("MyAlgorithm")){
			allocate_RB_by_metric(i);
		}else if(allocation_algorithm.equals("AllocateToSolve")){
			allocate_to_solve(i,wb,granularity);
		}else if(allocation_algorithm.equals("BufferTarget")){
			allocate_by_buffer(granularity);
		}else if(allocation_algorithm.equals("MetricX")){
		 allocate_by_metric_x(i);
		}else if(allocation_algorithm.equals("PF")){
			 allocate_by_PF(i);
		}else if(allocation_algorithm.equals("RAGA")){
			allocate_by_RAGA(i);
		}else if(allocation_algorithm.equals("MPF")){
			allocate_modified_PF(i);
		}else if(allocation_algorithm.equals("PFBF")){
			allocate_PFBF(i);
			//allocate_modified2_PF(i);
		}else if(allocation_algorithm.equals("MPF2")){
			allocate_modified_PF2(i);
		}else if(allocation_algorithm.equals("MPF3")){
			allocate_equal_nbr_RB();
		}
		//scenario.UEs.get(0).cqi.cqi=3;
		//scenario.UEs.get(1).cqi.cqi=8;
		//scenario.UEs.get(2).cqi.cqi=9;
		/*scenario.UEs.get(3).cqi.cqi=15;
		scenario.UEs.get(4).cqi.cqi=15;
		scenario.UEs.get(5).cqi.cqi=14;
		scenario.UEs.get(6).cqi.cqi=15;
		scenario.UEs.get(7).cqi.cqi=13;
		scenario.UEs.get(8).cqi.cqi=12;
		scenario.UEs.get(9).cqi.cqi=10;*/
		
	}
	

	
	private static void print_user_mos() {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			System.out.println("Avg. MOS User "+j+": "+scenario.UEs.get(j).avg_mos);
		}
	}

	private static void calculate_avg_duration_rebuf_events(){
		for(int j=0;j<scenario.numUEs;j++){
			if(scenario.UEs.get(j).numRebufEvents!=0){
				scenario.UEs.get(j).avg_duration_rebuffering_event=(double)((double)scenario.UEs.get(j).duration_rebuffering_event/(double)scenario.UEs.get(j).numRebufEvents);
			}else{
				scenario.UEs.get(j).avg_duration_rebuffering_event=(double)scenario.UEs.get(j).duration_rebuffering_event;
			}
		}
	
	}

	private static double calculate_avg_MOS() {
		double avg_MOS=0;
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			avg_MOS=avg_MOS+scenario.UEs.get(j).avg_mos;
		}
		avg_MOS=avg_MOS/scenario.numUEs;
		avg_mos_of_all_users=avg_MOS;
		return avg_MOS;
	}
	
	private static void calculate_avg_MOS_of_each_user(int i) {
		
		if(i==0){
			for(int j=0;j<scenario.numUEs;j++){
				avg_mos[j]=0;
			}
		}else if(i==1){
			for(int j=0;j<scenario.numUEs;j++){
				avg_mos[j]=scenario.UEs.get(j).mos;
			}
		}else{
			for(int j=0;j<scenario.numUEs;j++){
				avg_mos[j]=(avg_mos[j]*(i-1)+scenario.UEs.get(j).mos)/i;
			}
		}
		
		for(int j=0;j<scenario.numUEs;j++){
			scenario.UEs.get(j).avg_mos=avg_mos[j];
		}
		
		/*for(int j=0;j<scenario.numUEs;j++){
			double mos_avg=0;
			for(int i=0;i<numMillisecondToSimulate;i++){
				mos_avg+=mos[i][j];
			}
			mos_avg=mos_avg/numMillisecondToSimulate;
			scenario.UEs.get(j).avg_mos=mos_avg;
		}*/
		
	}

	private static void calculate_users_MOS(int i) {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			scenario.UEs.get(j).calculate_MOS();
		}
	}

	private static void calculate_standard_quality_desviations() {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			scenario.UEs.get(j).calculate_standard_quality_desviation();
		}
	}

	private static void calculate_avg_qualities() {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			scenario.UEs.get(j).calculate_avg_quality();
		}
	}

	private static void calculate_Fis(int i) {
		// TODO Auto-generated method stub
		double num_seconds_to_simulate= (double) ((double)i/1000);
		for(int j=0;j<scenario.numUEs;j++){
			scenario.UEs.get(j).calculate_Fi(num_seconds_to_simulate);
		}
	}
	
	public static double simulation(String allocation_algorithm, int numUEs_to_simulate,int collumn, int mum_simulations) throws IOException {
		simulation++;
		scenario= new Scenario(numUEs_to_simulate,numUEs_to_simulate,numRB,numAntennas,interrupt_allocating,rebuffering_period,cqi_event_file_directory,segment_size,number_segments_requested_during_rebuffering,cqi_numbers,number_segments_initially_requested);
		scenario.numUEs=numUEs_to_simulate;
		last_served=-1;
		buffers= new  double[numMillisecondToSimulate][scenario.numUEs];
		cqis= new  int[numMillisecondToSimulate][scenario.numUEs];
		mos=new double[numMillisecondToSimulate][scenario.numUEs];
		throughputs= new  double[numMillisecondToSimulate][scenario.numUEs];
		average_throughputs= new  double[scenario.numUEs];
		sending_data=new boolean[scenario.numUEs];
		period_above_mos_limit=new double[scenario.numUEs];
		avg_mos=new double[scenario.numUEs];
		mos_above_limit_during_more_than_x_percent_of_time=new double[scenario.numUEs];
		
		
		for(int j=0;j<scenario.numUEs;j++){
			sending_data[j]=false;
		}
		user_metrics= new double[scenario.numUEs];
		
		
		File f= new File(directory+"Rebuf_cdf"+".xls");
		

		Workbook wb_Rebuf_cdf;
		if(f.exists() && simulation!=0){
			FileInputStream is = new FileInputStream(f);
			wb_Rebuf_cdf=new XSSFWorkbook(is);
		}else{
			wb_Rebuf_cdf=new XSSFWorkbook();
		    wb_Rebuf_cdf.createSheet("Rebuf_cdf");
		}
		
		
		File f2= new File(directory+"PercentUsersMOS"+".xls");
		

		Workbook wb_percent;	

		if(simulation==0 && collumn==1 && !f2.exists()){
			wb_percent=new XSSFWorkbook();
		    wb_percent.createSheet("%Users with MOS>=x");
			
		}else{
			if(f2.exists()){
			FileInputStream is2 = new FileInputStream(f2);
			wb_percent=new XSSFWorkbook(is2);
			}else{
				wb_percent=new XSSFWorkbook();
			    wb_percent.createSheet("%Users with MOS>=x");
			}
		}
		
		
		File f3= new File(directory+"AvgMOS_cdf"+".xls");
		

		Workbook wb_AvgMOS_cdf;	
		if(f3.exists() && simulation!=0){
			FileInputStream is3 = new FileInputStream(f3);
			wb_AvgMOS_cdf=new XSSFWorkbook(is3);
		}else{
			wb_AvgMOS_cdf=new XSSFWorkbook();
			wb_AvgMOS_cdf.createSheet("%Users with MOS<=x");
		}
		
		
		File f4= new File(directory+"MOS_evolution"+".xls");
		

		Workbook wb_MOS_evolution;	
		if(f4.exists() && simulation!=0){
			FileInputStream is4 = new FileInputStream(f4);
			wb_MOS_evolution=new XSSFWorkbook(is4);
		}else{
			wb_MOS_evolution=new XSSFWorkbook();
			wb_MOS_evolution.createSheet("MOS_evolution");
		}
		
		
		File f5= new File(directory+"Statistics_mos"+".xls");
		
		Workbook wb_MOS_stats;	
		/*System.out.println(simulation+" "+collumn+f5.exists());
		if(simulation==0 && collumn==1 && !f5.exists()){
			
			wb_MOS_stats=new XSSFWorkbook();
			wb_MOS_stats.createSheet("MOS_Stats");
			
		}else{
			FileInputStream is5 = new FileInputStream(f5);
			wb_MOS_stats=new XSSFWorkbook(is5);
		}*/
		
		File f6= new File(directory+"Perc_UE_MOS_1Algorithm_"+allocation_algorithm+".xls");
		
		Workbook wb_perc_1Alg;	
		
		if(simulation==0 && collumn==1){
			if(!f6.exists()){
				wb_perc_1Alg=new XSSFWorkbook();
				wb_perc_1Alg.createSheet("%Users with MOS>=x");
			}else{
				FileInputStream is6 = new FileInputStream(f6);
				wb_perc_1Alg=new XSSFWorkbook(is6);
			}
		}else{
			if(f6.exists()){
				FileInputStream is6 = new FileInputStream(f6);
				wb_perc_1Alg=new XSSFWorkbook(is6);
			}else{
				wb_perc_1Alg=new XSSFWorkbook();
				wb_perc_1Alg.createSheet("%Users with MOS>=x");
			}
			
		}
		
		File f7= new File(directory+"Num_UE_belowZ_1Algorithm_"+allocation_algorithm+".xls");
		Workbook wb_users_below_limitZ;	
		
		if(simulation==0 && collumn==1){
			if(!f7.exists()){
				wb_users_below_limitZ=new XSSFWorkbook();
				wb_users_below_limitZ.createSheet("%Users with MOS>=x");
			}else{
				FileInputStream is7 = new FileInputStream(f7);
				wb_users_below_limitZ=new XSSFWorkbook(is7);
			}
		}else{
			if(f7.exists()){
				FileInputStream is7 = new FileInputStream(f7);
				wb_users_below_limitZ=new XSSFWorkbook(is7);
			}else{
				wb_users_below_limitZ=new XSSFWorkbook();
				wb_users_below_limitZ.createSheet("%Users with MOS>=x");
			}
			
		}
		
		Workbook wb = new XSSFWorkbook();  // or new XSSFWorkbook();	
	    wb.createSheet("Buffers");
	    wb.createSheet("CQIs");
	    wb.createSheet("Throughputs");
	    wb.createSheet("RB");
	    wb.createSheet("RequestedQualities");
	    wb.createSheet("Sizes");
	    wb.createSheet("MOS");

	    
		for(int i=0;i<=numMillisecondToSimulate-1;i++){ // simular 10 00 alocações, ou seja 1 segundos
			
			calculate_average_throughputs(i);
			
			if(i==0){
				for(int j=0;j<scenario.UEs.size();j++){
					scenario.UEs.get(j).requested_video.request_first_video_quality();
					sending_data[j]=true;
					//scenario.UEs.get(j).avg_served_quality+=scenario.UEs.get(j).requested_video.video_requested_BitRate;
				}
			}else{
				update_requested_segments(i,5,dash_mode); /*users request a new segment if the last have already been received*/
			}
			
			
			allocate_RB(allocation_algorithm,i, wb, granularity_buffer_export_excel);
			int aux=0;
			for(int h=0;h<scenario.numUEs;h++){
				aux=aux+scenario.UEs.get(h).numAllocatedRB;
			}
			if(aux>100){
				System.out.println("ERRORRRRRR:!!!!!!");
				System.out.println(aux);
			}
			
			update_CQIs(i,granularity_Cqis_update);
			update_bitrate();	
			send_data_to_users(i);
			drain_users_buffers(1,i,granularity_buffer_drain); // drain if the user has already received the initially requested segments
			
			
			calculate_avg_duration_rebuf_events();
			calculate_Fis(i);
			calculate_avg_qualities();
			calculate_standard_quality_desviations();
			calculate_users_MOS(i);
			
			/*if(i>8000){
				scenario.UEs.get(0).cqi.cqi=6;
			}
			if(i<6000){
				scenario.UEs.get(1).cqi.cqi=12;
			}
			else if(i>6000 && i<=8000){
				scenario.UEs.get(1).cqi.cqi=8;
			}else if(i>8000 && i<=10000){
				scenario.UEs.get(1).cqi.cqi=12;
			}else if(i>10000 && i<=12000){
				scenario.UEs.get(1).cqi.cqi=8;
			}else if(i>12000 && i<=14000){
				scenario.UEs.get(1).cqi.cqi=12;
			}else if(i>14000 && i<=16000){
				scenario.UEs.get(1).cqi.cqi=8;
			}else if(i>16000 && i<=18000){
				scenario.UEs.get(1).cqi.cqi=12;
			}else if(i>18000 && i<=20000){
				scenario.UEs.get(1).cqi.cqi=8;
			}else if(i>20000 && i<=22000){
				scenario.UEs.get(1).cqi.cqi=12;
			}else{
				scenario.UEs.get(1).cqi.cqi=12;
			}
			*/
			
			write_buffers(i,buffers, granularity_buffer_write);
			write_cqis(i,cqis,granularity_cqi_write);
			write_throughputs(i,throughputs,granularity_throughput_write);
			write_mos(i,mos, granularity_mos_write);
			
			export_to_Excel(wb, i);		

			//export_mos_evolution_to_Excel(wb_MOS_evolution,collumn,allocation_algorithm,i,granularity_buffer_export_excel);
			calculate_avg_MOS_of_each_user(i);
		}
		//calculate_avg_MOS_of_each_user(i);
		double avg_mos_of_all_users=calculate_avg_MOS();
		
		write_avg_mos(avg_mos);

		//export_avg_mos_to_Excel(wb_AvgMOS_cdf, simulation, allocation_algorithm);

		//export_percentage_users_w_MOS_bigger_than_limit_to_Excel(wb_percent, simulation,collumn,allocation_algorithm);
		//export_percent_rebuf_to_Excel(wb_Rebuf_cdf, simulation, allocation_algorithm);
		//export_statistical_avg_mos_to_Excel(wb_MOS_stats, simulation, allocation_algorithm, collumn,mum_simulations);
		export_percentage_users_w_MOS_bigger_than_limit_to_Excel_to_1_algorithm(wb_perc_1Alg, simulation,collumn,allocation_algorithm);
		//export_number_users_w_MOS_lower_than_Z_limit_to_Excel_to_1_algorithm(wb_users_below_limitZ, simulation,collumn,allocation_algorithm);
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+allocation_algorithm+".xls");
			wb.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"AvgMOS_cdf"+".xls");
			wb_AvgMOS_cdf.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"PercentUsersMOS"+".xls");
			wb_percent.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"Perc_UE_MOS_1Algorithm_"+allocation_algorithm+".xls");
			wb_perc_1Alg.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"Rebuf_cdf"+".xls");
			wb_Rebuf_cdf.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"MOS_evolution"+".xls");
			wb_MOS_evolution.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			FileOutputStream fileOut = new FileOutputStream(directory+"Statistics_mos"+".xls");
			wb_MOS_stats.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			FileOutputStream fileOut = new FileOutputStream(directory+"Num_UE_belowZ_1Algorithm_"+allocation_algorithm+".xls");
			wb_users_below_limitZ.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//calculate_users_MOS(i, granularity_mos_write);

		
		//print_user_metrics();
		System.out.println("->>>"+allocation_algorithm+":");
		System.out.println("NumUEs: "+scenario.numUEs);
		System.out.println("Avg. Mos: "+avg_mos_of_all_users);
		//print_general_metrics(avg_mos_of_all_users);
		//calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
		//print_user_mos();
			
		//System.out.println(a);
		//System.out.println(b);
		//System.out.println(c);
		//System.out.println("Terminated succssefully.\n\n");
		
		return avg_mos_of_all_users;
	}




	private static void write_avg_mos(double[] avg_mos) {
		// TODO Auto-generated method stub
		for(int j=0;j<scenario.numUEs;j++){
			//System.out.println(scenario.UEs.get(j).avg_mos);
			avg_mos[j]=scenario.UEs.get(j).avg_mos;
		}
	}

	private static Object calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time() {
		// TODO Auto-generated method stub
		double aux=0;
		
		for(int j=0;j<scenario.numUEs;j++){
			mos_above_limit_during_more_than_x_percent_of_time[j]=(double)(period_above_mos_limit[j]/numMillisecondToSimulate*100);
			if(mos_above_limit_during_more_than_x_percent_of_time[j]>time_mos_limit){
				aux++;
			}
		}
		percetage_users_w_mos_greater_than_limit=(double)(aux/scenario.numUEs*100);
		//System.out.println("% of users with mos bellow "+ mos_limit+ " during " + time_mos_limit+ "% of time: "+percetage_users_w_mos_greater_than_limit+"%");
		return null;
	}

	private static Object calculate_percentage_of_users_w_mos_above_limit_strategy1() {
		// TODO Auto-generated method stub
		double aux=0;
		
		for(int j=0;j<scenario.numUEs;j++){
			if(scenario.UEs.get(j).mos>=mos_limit){
				aux++;
			}
		}
		
		percetage_users_w_mos_greater_than_limit=(double)(aux/scenario.numUEs*100);
		System.out.println("%>3  "+percetage_users_w_mos_greater_than_limit);
		return null;
	}
	

	
	private static Object calculate_number_of_users_w_mos_bellow_Z_limit() {
		// TODO Auto-generated method stub
		double aux=0;
		
		for(int j=0;j<scenario.numUEs;j++){
			if(avg_mos[j]<Z_limit){
				aux++;
			}
		}
		number_users_w_mos_lower_than_Z_limit=aux;
		
		return null;
	}
	public static void main(String args[]) throws IOException{
		//int numUE=65;
		//int num_simulations=50;
		//generate_cqi_file_numbers(numUE);
		
		for(int i=50;i<=95;i+=5){
			int collumn=(i/5);
			for(int j=0;j<1;j++){
				generate_cqi_file_numbers(i);
				/*simulation("RoundRobin",i,collumn,1);
				simulation--;
				calculate_percentage_of_users_w_mos_above_limit_strategy1();
				System.out.println("#>3: "+percetage_users_w_mos_greater_than_limit);*/
				//simulation("BET",i,collumn,1);
				//simulation--;
				//simulation("PF",i,collumn,1);
				//simulation--;
				//simulation("PFBF",i,collumn,1);
				//simulation--;
				//simulation("MPF2",i,collumn,1);
				//simulation--;
				simulation("MPF3",i,collumn,1);
				calculate_percentage_of_users_w_mos_above_limit_strategy1();
				System.out.println("#>3: "+percetage_users_w_mos_greater_than_limit);
				simulation--;
				simulation("MPF2",i,collumn,1);
				calculate_percentage_of_users_w_mos_above_limit_strategy1();
				System.out.println("#>3: "+percetage_users_w_mos_greater_than_limit);
				/*calculate_percentage_of_users_w_mos_above_limit();
				System.out.println("#>3: "+percetage_users_w_mos_greater_than_limit);
				
				calculate_number_of_users_w_mos_bellow_Z_limit();
				System.out.println("bellow 2: "+number_users_w_mos_lower_than_Z_limit);
				
				calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
				System.out.println("#>3 80%time: "+percetage_users_w_mos_greater_than_limit);
				
				//simulation("PFBF",i,collumn,1);
				//simulation--;*/
				/*simulation("MPF3",i,collumn,1);
				simulation--;
				
				calculate_percentage_of_users_w_mos_above_limit();
				System.out.println("#>3:"+percetage_users_w_mos_greater_than_limit);
				
				calculate_number_of_users_w_mos_bellow_Z_limit();
				System.out.println("bellow 2: "+number_users_w_mos_lower_than_Z_limit);
				
				calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
				System.out.println("#>3 80%time: "+percetage_users_w_mos_greater_than_limit);*/
				
				
				
				/*simulation("PFBF",i,collumn,1);
				calculate_percentage_of_users_w_mos_above_limit();
				System.out.println("#>3:"+percetage_users_w_mos_greater_than_limit);
				
				calculate_number_of_users_w_mos_bellow_Z_limit();
				System.out.println("bellow 2: "+number_users_w_mos_lower_than_Z_limit);
				
				calculate_percentage_of_users_w_mos_above_limit_during_more_than_x_time();
				System.out.println("#>3 80%time: "+percetage_users_w_mos_greater_than_limit);*/
				
				//simulation--;
			}
			
			simulation=-1;
			
		}
		
		
		//simulation("RoundRobin",i,1);
		//simulation--;
		//simulation("PF",i,2);
		//simulation--;
		//simulation("BET",i,3);
		//simulation--;
		//simulation("PFBF",i,4);
		//simulation--;
		//simulation("MPF",i,5);
		//simulation--;
		
		
		//simulation("RoundRobin",numUE,1,1);
		//simulation("PF",numUE,2,1);
		//simulation("BET",numUE,3,1);
		//simulation("PFBF",numUE,4,1);

		/*buffer_l=200;
		simulation("MPF",numUE,5,1);
		*/
		/*simulation("MPF2",numUE,1,1);
		calculate_percentage_of_users_w_mos_above_limit();
		System.out.println("#"+percetage_users_w_mos_greater_than_limit);
		simulation--;
		simulation("MPF3",numUE,6,1);
		calculate_percentage_of_users_w_mos_above_limit();
		System.out.println("#"+percetage_users_w_mos_greater_than_limit);
		simulation("PFBF",numUE,6,1);
		calculate_percentage_of_users_w_mos_above_limit();
		System.out.println("#"+percetage_users_w_mos_greater_than_limit);*/
		

		//simulation("MPF2",numUE,2,1);
		
		/*double media_mos=0;
		for(int h=0;h<50;h++){
			generate_cqi_file_numbers(numUE);
			media_mos+=simulation("MPF",numUE,5,1);
		}

		System.out.println("Avg:"+media_mos);
		*/
		//simulation("MPF",numUE,5,1);
		//generate_cqi_file_numbers(numUE);
		//simulation("MPF",numUE,5,1);
		//generate_cqi_file_numbers(numUE);
		//simulation("MPF",numUE,5,1);
		//generate_cqi_file_numbers(numUE);
		//simulation("MPF",numUE,5,1);
		//generate_cqi_file_numbers(numUE);
		//simulation("MPF",numUE,5,1);
		
		//simulation("MPF2",numUE,1,1);
		

		/*for(int i=0;i<num_simulations;i++){
			generate_cqi_file_numbers(numUE);
			simulation("MPF2",numUE,1,num_simulations);
			//simulation--;
			//simulation("PF",numUE,2,num_simulations);
		}*/
		
		/*for (int j = 0; j < user_metrics.length; j++) {		
			
			System.out.println(scenario.UEs.get(j).mos);
			
		}*/
	}




	

}
