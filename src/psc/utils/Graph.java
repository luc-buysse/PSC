package psc.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import psc.Settings;

public class Graph {
	public static HashMap<String, ArrayList<Linear>> caracteristics = new HashMap<String, ArrayList<Linear>>();
	
	public static class Linear {
		String name;
		ArrayList<String> values = new ArrayList<String>(); 
	}
	
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
	
	public static void average(String car, String name, ArrayList<Integer> data, int graphSpan) {
		Linear graph = new Linear();
		graph.name = name;
		Integer avg = 0;
	
		for(int i = 0 ; i < graphSpan; i++) {
			avg +=data.get(i);
		}
		
		for(int i = graphSpan ; i < data.size(); i++) {
			graph.values.add(String.valueOf(avg));
			
			if(i < data.size() - graphSpan - 1) {
				avg -= data.get(i - graphSpan);
				avg += data.get(i);
			}
		}
		
		if(!caracteristics.containsKey(car)) {
			caracteristics.put(car, new ArrayList<Linear>());
		}
		caracteristics.get(car).add(graph);
	}
	
	public static void averageF(String car, String name, ArrayList<Float> data, int graphSpan) {
		Linear graph = new Linear();
		graph.name = name;
		Float avg = 0.f;
	
		for(int i = 0 ; i < graphSpan; i++) {
			avg +=data.get(i);
		}
		
		for(int i = graphSpan ; i < data.size(); i++) {
			graph.values.add(String.valueOf(avg));
			
			if(i < data.size() - graphSpan - 1) {
				avg -= data.get(i - graphSpan);
				avg += data.get(i);
			}
		}
		
		if(!caracteristics.containsKey(car)) {
			caracteristics.put(car, new ArrayList<Linear>());
		}
		caracteristics.get(car).add(graph);
	}
	
	public static void writeGraphs() {
		try {
			
			for(String name : caracteristics.keySet()) {
				FileWriter fw = new FileWriter(new File(name + ".csv"));
				
				ArrayList<Linear> graphs = caracteristics.get(name);
				int num_graphs = graphs.size();
				
				// Names
				for(int i = 0 ; i < num_graphs ; i++) {
					fw.write(graphs.get(i).name);
					fw.write(",");
				}
				fw.write("\n");
				
				// Columns
				int remaining = num_graphs;
				int line = 0;
				while(remaining > 0) {
					for(int i = 0 ; i < num_graphs ; i++) {
						if(line == graphs.get(i).values.size()) {
							remaining--;
						}
						
						if(line < graphs.get(i).values.size()) {
							fw.write(graphs.get(i).values.get(line));
						}
						fw.write(",");
					}
					fw.write("\n");
					line++;
				}
				
				fw.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
