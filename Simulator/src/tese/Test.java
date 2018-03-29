package tese;

import java.util.Comparator;

public class Test implements Comparable{
	double buffer=0;
	public Test(double buffer) {
		// TODO Auto-generated constructor stub
		this.buffer=buffer;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		float x=1;
		float num=30;

			 System.out.println((double)7/8*(Math.log((x/num))/6+1));
	
		
	}
	
	@Override
	public int compareTo(Object arg0) {
		return 0;
	}
	
	public static Comparator<Test> Comparator_Buffer = new Comparator<Test>() {

		public int compare(Test u1, Test u2) {

		   double buffer1 = u1.buffer;
		   double buffer2 = u2.buffer;

		   /*For ascending order*/
		   if (buffer1 < buffer2) return -1;
	        if (buffer1 > buffer2) return 1;
	        return 0;
	   }};
	   
	   
	
}
