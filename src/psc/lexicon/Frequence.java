package psc.lexicon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import psc.Settings;

public class Frequence {
	public static HashSet<String> list = new HashSet<String>();
	
	public static void initalize()  {
		list.clear();
		File dir = new File(Settings.corpus_parsed);
		
		boolean erase_next = true;
		
		if(dir.exists() && dir.isDirectory()) {
			for(File file : dir.listFiles()) {
				String name = file.getName();
				
				System.out.println("Nom du fichier a ajouter : " + name);
				
				String ext = name.substring(name.lastIndexOf('.') + 1, name.length());
				
				if(ext.equals("txt")) {
					
					try {
						Scanner fis = new Scanner(file);
						
						while(fis.hasNext()) {
							String line = fis.nextLine();
							String ws[] = line.split("\t");
							
							if(ws.length < 2) {
								erase_next = true;
								continue;
							}
							if(erase_next) {
								erase_next = false;
								continue;
							}
							
							list.add(ws[1]);
						}
						
						fis.close();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(Settings.dict_path, StandardCharsets.UTF_16));
			String line = "";
			
			while((line=br.readLine())!=null)
			{
				String word = line.split(",")[0];
				
				list.remove(word);
			}
			
			br.close();
			
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> to_remove = new ArrayList<String>();
		for(String word : list) {
			if(Character.isUpperCase(word.charAt(0))) {
				to_remove.add(word);
			}
		}
		for(String word : to_remove) {
			list.remove(word);
		}
		
		try {
			File neo_file = new File(Settings.neo_path);
			FileWriter fw = new FileWriter(neo_file);
			
			for(String word : list) {
				fw.write(word + "\n");
			}
			
			fw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
