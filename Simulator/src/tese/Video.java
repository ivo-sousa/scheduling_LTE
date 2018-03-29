package tese;


public class Video {
	int[] segmentLenght={1000,2000,4000,6000,10000,15000}; // [miliseconds]
	int segment_lenght;
	double[] representationBitRate={0.2,0.25,0.3,0.4,0.5,0.6,0.7,0.9,1.2,1.5,2,2.5,3,4}; // [Mbps] //,5,6,8
	double video_requested_BitRate;
	int requested_quality;
	int number_bits_per_segment=0;
	double l_best;
	int l_best_index=0;
	public Video( int segment_lenght) {
		this.segment_lenght=segment_lenght;
		// TODO Auto-generated constructor stub
		requested_quality=0;
		number_bits_per_segment=(int) (representationBitRate[requested_quality]*1000000);
		video_requested_BitRate=representationBitRate[requested_quality];
	}
	
	public void calculate_l_best(double bw_estimated){
		double l_best=representationBitRate[0]*1000000;
		l_best_index=0;
		for(int i=0;i<representationBitRate.length;i++){
			
			if(bw_estimated>=(representationBitRate[i]*1000000)){
				//System.out.println(bw_estimated+" "+representationBitRate[i]*1000000);
				//System.out.println(bw_estimated);
				l_best=representationBitRate[i]*1000000;
				l_best_index=i;
				
			}
		}
		//System.out.println(bw_estimated+" "+l_best);
		this.l_best=l_best/1000000;
	}

	public void update_requested_video_quality(double new_requested_video_bitrate, int index){
		this.video_requested_BitRate=new_requested_video_bitrate;
		
		requested_quality=index;
		this.number_bits_per_segment=(int) (representationBitRate[index]*1000000);
	}
	public void request_first_video_quality(){
		requested_quality=0;
		number_bits_per_segment=(int) (representationBitRate[requested_quality]*1000000*(segment_lenght/1000));
		//System.out.println(number_bits_per_segment);
	}
	
	public void request_better_video_quality(){
		if(requested_quality+1<16){
			requested_quality+=1;
			video_requested_BitRate=representationBitRate[requested_quality];
		}
	}
	
	public void request_worse_video_quality(){
		if(requested_quality-1>0){
			requested_quality-=1;
			video_requested_BitRate=representationBitRate[requested_quality];
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] representationBitRate={0.2,0.25,0.3,0.4,0.5,0.6,0.7,0.9,1.2,1.5,2,2.5,3,4}; // [Mbps]
		System.out.println(representationBitRate.length);
		System.out.println(Math.ceil(2.42));
	}

}
