package com.jpc.nlp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

	public class Pair {

		private int d1;
		private double d2;

		public int getD1() {
			return d1;
		}

		public double getD2() {
			return d2;
		}

		public Pair(int pd1, double pd2) {

			d1 = pd1;
			d2 = pd2;
		}
	}

	public List<Pair> sortArray(double[] array) {

		List<Pair> ps = new ArrayList<Pair>();

		int rowSize = array.length;

		for (int j = 0; j < rowSize; j++) {

			Pair p = new Pair(j, array[j]);

			ps.add(p);
		}

		quickSort(ps, 0, ps.size()-1);

		return ps;
	}

	private void quickSort(List<Pair> ps, int left, int right) {

		int l_hold, r_hold;

		l_hold = left;
		r_hold = right;
		int pivotidx = left;

		Pair pivot = ps.get(pivotidx);

		while (left < right) {

			while (ps.get(right).getD2() <= pivot.getD2() && left < right) {
				right--;
			}

			if (left != right) {

				ps.set(left, ps.get(right));
				left++;
			}

			while (ps.get(left).getD2() >= pivot.getD2() && left < right) {
				left++;
			}
			
			if (left != right) {

				ps.set(right, ps.get(left));
				right--;
			}
		}

		ps.set(left, pivot);
		pivotidx = left;
		left = l_hold;
		right = r_hold;

		if (left < pivotidx) {
			quickSort(ps, left, pivotidx - 1);
		}
		if (right > pivotidx) {
			quickSort(ps, pivotidx + 1, right);
		}
	}
	
	
	public List<Map.Entry<Integer, Double>> sortArray2(double[] array) {

		Map<Integer, Double> mp = new HashMap<Integer, Double>();

		int rowSize = array.length;

		for (int j = 0; j < rowSize; j++) {

			mp.put(j, array[j]);
		}


		List<Map.Entry<Integer, Double>> ret = this.getSortedHashMapByValue(mp);
		
		return ret;
	}
	
	public List<Map.Entry<Integer, Double>> getSortedHashMapByValue(
			Map<Integer, Double> similarity) {

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		List<Map.Entry<Integer, Double>> l = new ArrayList<Map.Entry<Integer, Double>>(
				similarity.entrySet());

		Collections.sort(l, new Comparator<Map.Entry<Integer, Double>>() {
			
			public int compare(Map.Entry<Integer, Double> o1,
					Map.Entry<Integer, Double> o2) {
				
				if(o2.getValue().compareTo(o1.getValue()) > 0)
					return 1;
				else if(o2.getValue().compareTo(o1.getValue()) == 0)
					return 0;
				else
					return -1;
			}
		});

		return l;
	}

}
