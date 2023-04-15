package psc.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import psc.Settings;

public class Graph {
	public static void histogram(String name, Map<Object, Object> data) {
		try
		{
			System.out.println(Settings.graphs_path + "/" + name + ".txt");
			
			FileWriter fw = new FileWriter(new File(Settings.graphs_path + "/" + name + ".csv"));
			
			for(Object key : data.keySet()) {
				fw.write(key.toString());
				fw.write("\t");
				fw.write(data.get(key).toString());
				fw.write("\n");
			}
			
			fw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void average(String name, ArrayList<Integer> data, int graphSpan) {
		try
		{
			int avg = 0;
			FileWriter fw = new FileWriter(new File(Settings.graphs_path + "/" + name + ".csv"));
		
			for(int i = 0 ; i < graphSpan; i++) {
				avg += data.get(i);
			}
			
			for(int i = graphSpan ; i < data.size(); i++) {
				fw.write(String.valueOf(avg) + "\n");
				
				if(i < data.size() - graphSpan - 1) {
					avg -= data.get(i - graphSpan);
					avg += data.get(i);
				}
			}
			
			fw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
