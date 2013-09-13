package com.jpc.nlp.lda.data;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jpc.nlp.util.FileUtils;

/**
 * 数据预处理
 * 
 * @author Jia
 * 
 */
public class DataSet {

	private TIntObjectMap<String> index2Word;
	private Map<String, Integer> word2Index;

	private TIntObjectMap<String> index2Doc;
	private Map<String, Integer> doc2Index;
	
	private List<TIntArrayList> docs;
	
	private String input;
	
	public DataSet(String data) {
		
		input = data;

		index2Word = new TIntObjectHashMap<String>();
		word2Index = new HashMap<String, Integer>();

		index2Doc = new TIntObjectHashMap<String>();
		doc2Index = new HashMap<String, Integer>();
		
	}

	public TIntObjectMap<String> getIndex2Word() {
		return index2Word;
	}

	public Map<String, Integer> getWord2Index() {
		return word2Index;
	}

	public TIntObjectMap<String> getIndex2Doc() {
		return index2Doc;
	}

	public Map<String, Integer> getDoc2Index() {
		return doc2Index;
	}

	public List<TIntArrayList> document2Index() {
		
		docs = new ArrayList<TIntArrayList>();

		List<String> files = FileUtils.fileList(input, ".txt");

		int i = 0;
		
		int j = 0;
		
		for (String f : files) {
			
			if (!doc2Index.containsKey(f)) {
				doc2Index.put(f, i);
				index2Doc.put(i, f);
			}
			
			TIntArrayList index = new TIntArrayList();

			List<String> lines = FileUtils.ReadbyLine(f, "utf-8");
			
			for (String line : lines) {
				
				line = line.trim();
				if(line.length() == 0)
					continue;
				
				String[] words = line.split(" ");
				
				for(String word:words){
					
					if (!word2Index.containsKey(word)) {

						word2Index.put(word, j);

						index2Word.put(j, word);

						j++;
					}
					
					index.add(word2Index.get(word));
				}
			}
			
			docs.add(index);
			
			i++;
		}
		
		return docs;
	}
}
