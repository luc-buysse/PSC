package psc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TextCleaner {
	public static void parse(String file_path, String output_dir) {
		String fis_name = file_path.substring(file_path.lastIndexOf('/')+1, file_path.length());
		String filename = fis_name.substring(0, fis_name.lastIndexOf('.'));
		String extension = fis_name.substring(fis_name.lastIndexOf('.') + 1, fis_name.length());
		String output_path = output_dir + "/" + filename + ".txt";
		output_path = output_path.replaceAll("\\s", "_");
		
		System.out.println("Creating the file : " + output_path);
		
		if(extension.equals("xml") ){
			String finalTxt = clean(xmlToString(file_path));
			
			writeTo(output_path, finalTxt);
		}
		else if(extension.equals("txt")) {
			String finalTxt = "";
			
			try {
				finalTxt = clean(new String(Files.readAllBytes(Paths.get(file_path))));
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			writeTo(output_path, finalTxt);
		}
		else {
			System.out.println("Erreur : type de fichier non pris en charge");
		}
	}
	
	private static void writeTo(String output_path, String txt) {
		try {
			FileWriter fw = new FileWriter(new File(output_path));
			
			fw.write(txt);
			
			fw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String xmlToString(String file_path) {
		String res = "";
		
		try {
			FileInputStream fis = new FileInputStream(new File(file_path));
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(fis);
			
			NodeList para = doc.getElementsByTagName("p");
			
			for(int i = 0 ; i < para.getLength() ; i++) {
				NodeList words = para.item(i).getChildNodes();
				for(int j = 0 ; j < words.getLength() ; j++) {
					Node wel = words.item(j);
							
					if(wel != null) {
						NamedNodeMap attrs = wel.getAttributes();
						if(attrs != null) {
							Node w = attrs.getNamedItem("word");
							if(w != null) {
								String sw = w.getTextContent();
								if(!sw.matches("[,.!?]")) {
									res += " ";
								}
								res += sw;
							}
						}
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return res;
	}
	
	static String clean(String txt) {
		txt = txt.replaceAll("[\\[\\]]", "");
		txt = txt.replaceAll("\\r|\\n", " ");
		txt = txt.replaceAll("(?<!\\s\\w)[.?!]\\s", "$0\n");
		
		return txt;
	}
}
