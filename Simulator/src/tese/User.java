package tese;

import java.util.ArrayList;
import java.util.Comparator;

public class User implements Comparable{
	
	double w_minus_1=0;
	double nbr_rb_needed=0;
	CQI cqi;
	Report_Dash report_dash;
	Boolean dash=false;
	double buffer=0; // [ms]
	int numAllocatedRB=0;
	int numAntennas=0;
	Throughput bitRate;
	Video requested_video;
	boolean greedy;
	public int numRebufEvents=0;// se considerar initial buffering igual a rebuffering=1
	public boolean flag_request_better_quality=false;
	public boolean flag_request_worse_quality=false;
	public int time_request_better_quality=0;
	public int time_request_worse_quality=0;
	public boolean rebuffering=false; // when a stall happens, it is true until number_segments_requested_during_rebuffering have been requested and received
	public int rebuffering_period=0;
	public int number_segments_requested_during_rebuffering;
	public double bandwidth_estimated;
	public int last_request=0;
	public int interrupt_allocating;
	public boolean requesting_initial_segments=true; // true until the number_segments_initially_requested has been requested and received
	public boolean to_allocate=true;
	public int received_bits=0;
	public int num_quality_changes=0;
	public double avg_served_quality=0;
	public double avg_duration_rebuffering_event=0;
	public int time_of_rebuffering_start=0;
	public int bits_already_sent_from_last_segment_before_rebuffering=0;
	public boolean initial_buffer_filling=true; //true until buffer amount equals initial_buffer_amount_to_start_draining
	public boolean recovering_stall=false; //when a stall happens, it is true until buffer amount equals recover_stall_buffer_amount_to_start_draining 
	public double fi;
	public int num_requested_segments;
	public double standard_quality_desviation=0;
	public double avg_mos=0;
	public ArrayList<Double> served_qualities;
	public double mos=0;
	public boolean segment_finished=false;
	public double duration_rebuffering_event=0;
	public double duration_initial_buffering=0;
	public double percent_rebuf=0;
	public double avg_served_q_when_mos_equal_2=0;
	public double rb_quality_cqi[][];
	public double number_times_served=0;
	public double total_rb=0;
	public double i;
	
	
	public User(boolean dash, int numAntennas, int interrupt_allocating, int rebuffering_period, String cqi_event_file_directory, int segment_lenght, int number_segments_requested_during_rebuffering, int cqi_number, int number_segments_initially_requested) {
		this.buffer=0;
		this.interrupt_allocating=interrupt_allocating;
		cqi=new CQI(cqi_event_file_directory,cqi_number);
		report_dash=new Report_Dash();
		this.dash=dash;
		bitRate=new Throughput();
		this.numAntennas=numAntennas;
		requested_video= new Video(segment_lenght);
		this.rebuffering_period=rebuffering_period;
		bandwidth_estimated=0;
		
		served_qualities=new ArrayList<Double>();
		for(int i=0;i<number_segments_initially_requested;i++){
			served_qualities.add(1.0);
		}
		
		rb_quality_cqi=new double[this.requested_video.representationBitRate.length][this.bitRate.bits_per_RB_per_CQI.length];
		for(int i=0;i<this.requested_video.representationBitRate.length;i++){
			for(int j = 0;j<this.bitRate.bits_per_RB_per_CQI.length;j++){
				rb_quality_cqi[i][j]=this.requested_video.representationBitRate[i]*Math.pow(10, 3)/this.bitRate.bits_per_RB_per_CQI[j];
				//System.out.println(i+" "+j+" "+rb_quality_cqi[i][j]);
			}
		}
		
	}
	
	public boolean verify_if_segment_will_be_completed(int[] bits_per_RB_per_CQI){
		if ((this.received_bits+ (this.numAllocatedRB*bits_per_RB_per_CQI[this.cqi.cqi]))>=this.requested_video.number_bits_per_segment){
			return true;// with  num_RB the segment will be totally sent
		}else{
			return false;
		}
	}
	
	public boolean verify_if_user_will_be_removed_from_users_in_risk_to_stall(int[] bits_per_RB_per_CQI, double buffer_limit_to_be_in_risk_toStall){
		
		double qt=1000*(numAllocatedRB*1000*bits_per_RB_per_CQI[this.cqi.cqi]*numAntennas/1000)/(this.requested_video.video_requested_BitRate*Math.pow(10, 6));
		if (this.buffer+qt> buffer_limit_to_be_in_risk_toStall){
			return true;// with  num_RB the segment will be totally sent
		}else{
			return false;
		}
	}
	
	public boolean verify_if_user_will_be_removed_from_users_rebuffering(int[] bits_per_RB_per_CQI, double buffer_limit_to_be_in_risk_toStall, int segment_size){
		double qt=1000*(numAllocatedRB*1000*bits_per_RB_per_CQI[this.cqi.cqi]*numAntennas/1000)/(this.requested_video.video_requested_BitRate*Math.pow(10, 6));
		if(this.buffer+qt>number_segments_requested_during_rebuffering*segment_size){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean is_mos_above_min_target_mos(double min_target_mos){
		if(this.mos>=min_target_mos){
			return true;
		}else{
			return false;
		}
			
	}
	
	public boolean is_mos_below_max_target_mos(double max_target_mos){
		if(this.mos<=max_target_mos){
			return true;
		}else{
			return false;
		}
			
	}
	
	
	public double calculate_bw_sample(double teta, int i, double [][] throughputs, int j){
		double bw_sample=0;
		if(i<=teta*1000){
			for(int k=0;k<i;k++){
				bw_sample=bw_sample+throughputs[k][j];
			}
			bw_sample=bw_sample/(teta*1000);
		}else{
			for(int k=(int)(i-(teta*1000));k<i;k++){
				bw_sample=bw_sample+throughputs[k][j];
			}
			bw_sample=bw_sample/(teta*1000);
			//System.out.println(bw_sample);
		}
		return bw_sample;
	}
	
	public double update_bandwidth_estimated(double w, double teta, int i, double [][] throughputs, int j){
		double bw_sample=calculate_bw_sample(teta, i, throughputs, j);
		if(bandwidth_estimated==0){
			bandwidth_estimated=bw_sample;
		}
		bandwidth_estimated=w*bandwidth_estimated+((1-w)*bw_sample);
		//System.out.println(bandwidth_estimated);
		return this.bandwidth_estimated;
	}

	public double calculate_average_throughput(int i, double average_throughput) {
		double average=0;
		
			average= average_throughput*(i-1)+this.bitRate.throughput;
			//System.out.println(this.bitRate.throughput);
		average=average/i;
		return average;
	}
	
	public void update_bitrate(){
		this.bitRate.update_bitRate(this.cqi.cqi,this.numAntennas,this.numAllocatedRB);
	}
	
	public void fill_buffer(double qt){
		this.buffer+=qt;
	}
	
	public void drain_buffer(int i,double qt,int rebuffering_period, int number_segments_requested_during_rebuffering){
		if(this.buffer<0){
			System.out.println("ATENTION!!!!!!!!!!!!!!!!!!!!!!!");
		}
		if(this.buffer-qt>0){
			if(rebuffering==false && requesting_initial_segments==false){
				
				this.buffer=buffer-qt;
				
			}else{
				/*this.rebuffering_period--;				
				if(this.rebuffering_period==0){
					rebuffering=false;
					this.rebuffering_period=rebuffering_period;
				}*/

				/*if(this.number_segments_requested_during_rebuffering==0){
					this.rebuffering=false;
					this.number_segments_requested_during_rebuffering=number_segments_requested_during_rebuffering;
				}*/
			}
		}else{
			if(rebuffering==false && requesting_initial_segments==false){

				this.time_of_rebuffering_start=i;
				recovering_stall=true;
				rebuffering=true;
				this.requested_video.requested_quality=0;
				this.requested_video.video_requested_BitRate=this.requested_video.representationBitRate[0];
				numRebufEvents++;
				for(int i1=0;i1<number_segments_requested_during_rebuffering;i1++){
					served_qualities.add(1.0);
				}
				this.number_segments_requested_during_rebuffering=number_segments_requested_during_rebuffering;
				bits_already_sent_from_last_segment_before_rebuffering=this.received_bits;
			}
		}
	}
	
	public void request_better_video_quality(){
		this.requested_video.request_better_video_quality();
	}



	@Override
	public int compareTo(Object arg0) {
		return 0;
	}
	
	public static Comparator<User> Comparator_Cqi_Descendent = new Comparator<User>() {

		public int compare(User u1, User u2) {

		   double cqi1 = u1.cqi.cqi;
		   double cqi2 = u2.cqi.cqi;

		   /*For ascending order*/
		   if (cqi1 > cqi2) return -1;
	        if (cqi1 < cqi2) return 1;
	        return 0;
	   }};
	   
	   public static Comparator<User> Comparator_MOS_Ascendent = new Comparator<User>() {

			public int compare(User u1, User u2) {

			   double mos1 = u1.mos;
			   double mos2 = u2.mos;

			   /*For ascending order*/
			   if (mos1 < mos2) return -1;
		        if (mos1 > mos2) return 1;
		        return 0;
		   }};
	   
	   public static Comparator<User> Comparator_Cqi_Ascendent = new Comparator<User>() {

			public int compare(User u1, User u2) {

			   double cqi1 = u1.cqi.cqi;
			   double cqi2 = u2.cqi.cqi;

			   /*For ascending order*/
			   if (cqi1 < cqi2) return -1;
		        if (cqi1 > cqi2) return 1;
		        return 0;
		   }};
	   
		public static Comparator<User> Comparator_Buffer_Ascendent = new Comparator<User>() {
	
			public int compare(User u1, User u2) {
	
			   double buffer1 = u1.buffer;
			   double buffer2 = u2.buffer;
	
			   /*For ascending order*/
			   if (buffer1 < buffer2) return -1;
		        if (buffer1 > buffer2) return 1;
		        return 0;
		   }};
	   
		public static Comparator<User> Comparator_Buffer_Descendent = new Comparator<User>() {

			public int compare(User u1, User u2) {

			   double buffer1 = u1.buffer;
			   double buffer2 = u2.buffer;

			   /*For ascending order*/
			   if (buffer1 > buffer2) return -1;
		        if (buffer1 < buffer2) return 1;
		        return 0;
		   }};


		public void calculate_Fi(double i) {
			double fi=0;
			double stall_frequency = (double) ((double)numRebufEvents/((double) i/1000));
			//System.out.println("stall f: "+stall_frequency);
			double avg_duration_rebuffering_event= (double) ((double)this.avg_duration_rebuffering_event/(double)1000);
			double min=0;
			double max=0;
			
			if(avg_duration_rebuffering_event<=15){
				min=avg_duration_rebuffering_event;
			}else{
				min=15;
			}
			if(this.mos==0)
			if(Math.log(stall_frequency)/6+1>0){
				
				max=Math.log(stall_frequency)/6+1;
			}else{
				
				max=0;
			}
			//System.out.println("max: "+max+" min: "+min);
			//System.out.println(this.avg_duration_rebuffering_event);
			fi=((double)7/8*max)+((double)1/8*min/15);
			this.fi=fi;
		}



		public double calculate_avg_quality() { //kbps
			// TODO Auto-generated method stub
			double avg_q=0;
			for(int i=0;i<served_qualities.size();i++){
				//System.out.println(served_qualities.get(i));
				avg_q+=served_qualities.get(i);
			}
			//System.out.println(avg_q+"++++++++++++++++++++++");
			avg_q=(double)avg_q/(double)served_qualities.size();
			//this.avg_served_quality=avg_q*1000;
			this.avg_served_quality=avg_q;
			return  avg_q;
		}



		public void calculate_standard_quality_desviation() {
			// TODO Auto-generated method stub
			double result=0;
			for(int i=0; i<served_qualities.size();i++){
				result=result+Math.pow((served_qualities.get(i)-avg_served_quality), 2);
			}
			result= Math.sqrt(result);
			result=result/served_qualities.size();
			this.standard_quality_desviation=result;
		}
		

	
		public double calculate_MOS(){
			//System.out.println(avg_served_quality+"|||"+standard_quality_desviation+"|||||"+fi);
			this.mos=5.67*avg_served_quality/((double)this.requested_video.representationBitRate.length- (double)1)-(6.72*standard_quality_desviation/((double)this.requested_video.representationBitRate.length-(double)1))+0.17-(4.95*this.fi);
			/*this.mos=(double)0.81*avg_served_quality-(double)0.95*standard_quality_desviation-(double)4.95*this.fi+0.17;
			if(this.mos<0)this.mos=0;*/
			if(this.mos<0)this.mos=0;
			if(this.mos>5)this.mos=5;
			return this.mos;
		}

		public double calculate_buffer_difference(double last_buffer){
			double current_buffer= this.buffer;
			double buffer_difference=0;
			return (current_buffer-last_buffer);
		}
		

}

