package tese;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
//import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

public class ReadCqis {

	public ReadCqis() {
		// TODO Auto-generated constructor stub
	}

	public ReadCqis(int number, ArrayList<String> cqi_events, String cqi_event_file_directory) { //
		// TODO Auto-generated method stub

		String txtFile = cqi_event_file_directory+number+".txt";
        BufferedReader br = null;
        String line = "";

        
        try {

            br = new BufferedReader(new FileReader(txtFile));
            //PrintWriter writer = new PrintWriter("C:/Users/alunos/Desktop/cqis-events/cqi_event-"+k+".txt", "UTF-8");
            
            while ((line = br.readLine()) != null) {
            	cqi_events.add(line);
            }
            
            //writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	
	/*public ReadCqis(int number, ArrayList<String> cqi_events) { //
		// TODO Auto-generated method stub

		String csvFile = "/Users/alunos/Desktop/cqis3/Ue-"+number+".csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        BigDecimal[] previous_line = new BigDecimal[2];

        int aux=1;
        
        try {

            br = new BufferedReader(new FileReader(csvFile));
            //PrintWriter writer = new PrintWriter("C:/Users/alunos/Desktop/cqis-events/cqi_event-"+k+".txt", "UTF-8");
           
            BigDecimal[] line_splitted = new BigDecimal[2];
            BigDecimal aux1 = new BigDecimal("0.005");
            
            while ((line = br.readLine()) != null) {
            	if(aux==1){
            		if((line = br.readLine()) != null){
            	        previous_line[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
            	        previous_line[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
            	        cqi_events.add("0 "+ (int)previous_line[1].floatValue());
            	        //writer.println("0 "+ previous_line[1]);
            			aux=0;
            			if((line = br.readLine()) != null){
            				line_splitted[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
            				line_splitted[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
                	        //System.out.println(previous_line[1]);
                			aux=0;
                		}
            		}
            	}
            		
                String[] line_splitter = line.split(cvsSplitBy);
  
                line_splitted[0]=new BigDecimal(line_splitter[0]);
                line_splitted[1]=new BigDecimal(line_splitter[1]);
                
                if(line_splitted[1].intValue()!=previous_line[1].intValue()){
                   // System.out.println("Time:" + line_splitter[0] + " , CQI=" + line_splitter[1]);

                	BigDecimal a = previous_line[0].remainder(aux1);
                	BigDecimal x0 = previous_line[0].subtract(a);
                	 	
                	BigDecimal b= line_splitted[0].remainder(aux1);
                	BigDecimal x1 = line_splitted[0].subtract(b);              	
                	BigDecimal sub1=x1.subtract(x0);
                	
                	BigDecimal y0= previous_line[1];
                	BigDecimal y1=line_splitted[1];   	
                	BigDecimal sub2=y1.subtract(y0);
                	
                	if(sub1.doubleValue()>0){
                	BigDecimal declive= sub2.divide(sub1, MathContext.DECIMAL32);
                	
                	int i=sub1.divide(aux1,MathContext.DECIMAL32).intValueExact();
                	
                	for(int j=0;j<i;j++){

                		BigDecimal intermedium_point0=previous_line[0].subtract(previous_line[0].remainder(aux1)).add(aux1);

                    	BigDecimal final_result=declive.multiply(aux1, MathContext.DECIMAL32).add(previous_line[1]);
                    	                    	
                    	BigDecimal intermedium_point1=final_result;

                    	float v= Math.round(intermedium_point1.floatValue());
                    	float n= Math.round(previous_line[1].floatValue());
                    	if(v!=n){
                    		previous_line[0]=new BigDecimal(intermedium_point0.toString());
                            previous_line[1]=new BigDecimal((int) Math.round(intermedium_point1.doubleValue()));
                            cqi_events.add(intermedium_point0 + " "+ (int)v);
                        	//writer.println(intermedium_point0 + " "+ v);
                    	}
                		previous_line[0]=new BigDecimal(intermedium_point0.toString());
                        previous_line[1]=new BigDecimal(intermedium_point1.toString());
                    	
                	}
                   // previous_line[0]=new BigDecimal(newX.toString());
                    //previous_line[1]=new BigDecimal(final_result.toString());

                }
                }else{
                previous_line[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
    	        previous_line[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
                }
            }
            
            //writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}*/
	public static void eventsCqis(int number) {
		// TODO Auto-generated method stub

		for(int k=1;k<=200;k++){
		String csvFile = "/Users/alunos/Desktop/cqis3/Ue-"+k+".csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        BigDecimal[] previous_line = new BigDecimal[2];

        int aux=1;
        
        try {
        	

            br = new BufferedReader(new FileReader(csvFile));
            PrintWriter writer = new PrintWriter("C:/Users/alunos/Desktop/cqis-events/cqi_event-"+k+".txt", "UTF-8");
           
            BigDecimal[] line_splitted = new BigDecimal[2];
            BigDecimal aux1 = new BigDecimal("0.005");
            
            while ((line = br.readLine()) != null) {
            	if(aux==1){
            		if((line = br.readLine()) != null){
            	        previous_line[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
            	        previous_line[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
            	        //cqi_events.add("0 "+ (int)previous_line[1].floatValue());
            	        writer.println("0 "+ (int)previous_line[1].floatValue());
            			aux=0;
            			if((line = br.readLine()) != null){
            				line_splitted[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
            				line_splitted[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
                	        //System.out.println(previous_line[1]);
                			aux=0;
                		}
            		}
            	}
            		
                String[] line_splitter = line.split(cvsSplitBy);
  
                line_splitted[0]=new BigDecimal(line_splitter[0]);
                line_splitted[1]=new BigDecimal(line_splitter[1]);
                
                if(line_splitted[1].intValue()!=previous_line[1].intValue()){
                   // System.out.println("Time:" + line_splitter[0] + " , CQI=" + line_splitter[1]);

                	BigDecimal a = previous_line[0].remainder(aux1);
                	BigDecimal x0 = previous_line[0].subtract(a);
                	 	
                	BigDecimal b= line_splitted[0].remainder(aux1);
                	BigDecimal x1 = line_splitted[0].subtract(b);              	
                	BigDecimal sub1=x1.subtract(x0);
                	
                	BigDecimal y0= previous_line[1];
                	BigDecimal y1=line_splitted[1];   	
                	BigDecimal sub2=y1.subtract(y0);
                	
                	if(sub1.doubleValue()>0){
                	BigDecimal declive= sub2.divide(sub1, MathContext.DECIMAL32);
                	
                	int i=sub1.divide(aux1,MathContext.DECIMAL32).intValueExact();
                	
                	for(int j=0;j<i;j++){

                		BigDecimal intermedium_point0=previous_line[0].subtract(previous_line[0].remainder(aux1)).add(aux1);

                    	BigDecimal final_result=declive.multiply(aux1, MathContext.DECIMAL32).add(previous_line[1]);
                    	                    	
                    	BigDecimal intermedium_point1=final_result;

                    	float v= Math.round(intermedium_point1.floatValue());
                    	float n= Math.round(previous_line[1].floatValue());
                    	if(v!=n){
                    		previous_line[0]=new BigDecimal(intermedium_point0.toString());
                            previous_line[1]=new BigDecimal((int) Math.round(intermedium_point1.doubleValue()));
                            //cqi_events.add(intermedium_point0 + " "+ (int)v);
                        	writer.println(intermedium_point0 + " "+ (int)v);
                    	}
                		previous_line[0]=new BigDecimal(intermedium_point0.toString());
                        previous_line[1]=new BigDecimal(intermedium_point1.toString());
                    	
                	}
                   // previous_line[0]=new BigDecimal(newX.toString());
                    //previous_line[1]=new BigDecimal(final_result.toString());

                }
                }else{
                previous_line[0]=new BigDecimal(line.split(cvsSplitBy)[0]);
    	        previous_line[1]=new BigDecimal(line.split(cvsSplitBy)[1]);
                }
            }
            
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		}
	}
	
	public static void main(String[] args){
		eventsCqis(1);
	}
}


