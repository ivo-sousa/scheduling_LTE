package tese;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;

public class Scenario {
	int numUEs;
	ArrayList<User> UEs;
	int numDashUEs;
	int numRB;

	
	public Scenario(int numUEs, int numDashUEs, int numRB, int numAntennas, int interrupt_allocating, int rebuffering_period, String cqi_event_file_directory, int segment_lenght, int number_segments_requested_during_rebuffering, int[] cqi_numbers, int number_segments_initially_requested) {
		// TODO Auto-generated constructor stub
		this.numUEs=numUEs;
		System.out.println(numUEs);
		UEs= new ArrayList<User>(numUEs);
		this.numDashUEs=numDashUEs;
		this.numRB=numRB;
		

		for(int i=0 ; i<numUEs;i++){
			UEs.add(new User(true,numAntennas,interrupt_allocating,rebuffering_period, cqi_event_file_directory,segment_lenght,number_segments_requested_during_rebuffering,cqi_numbers[i],number_segments_initially_requested));
		}
		/*for(int i=numUEs-numDashUEs ; i<numUEs;i++){
			UEs.add(new User(false,numAntennas,interrupt_allocating,rebuffering_period, cqi_event_file_directory,segment_lenght,number_segments_requested_during_rebuffering,cqi_numbers[i],number_segments_initially_requested));
		}*/
	}

	
	public static void main(String[] args) {
		double a= 1+(double)1/6;
		System.out.println(a);
	}

}
