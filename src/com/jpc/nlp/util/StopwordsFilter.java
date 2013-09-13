package com.jpc.nlp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class StopwordsFilter {

	private Set<String> stopwords;
	
	private static StopwordsFilter swordsFilter;
	
	public static StopwordsFilter getFilter(){
		
		if(swordsFilter == null){
			
			swordsFilter = new StopwordsFilter();
		}
		
		return swordsFilter;
	}
	
	private StopwordsFilter(){
		
		stopwords = new HashSet<String>();
		
		this.loadStopwords("stop.txt");
		
		if(stopwords.size() > 1)
			System.out.println("Load stop words list successfully...");
	}
	
	private void loadStopwords(String path){
		
		File f = new File(path);
		
		try {
			Scanner sc = new Scanner(f);
			
			while(sc.hasNextLine()){
				
				String s = sc.nextLine().trim();
				
				stopwords.add(s);
			}
			
			sc.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isStopwords(String word){
		
		return stopwords.contains(word);
	}
	
	public static void main(String[] args){
		
		System.out.println(StopwordsFilter.getFilter().isStopwords("nbsp"));
	}
}
