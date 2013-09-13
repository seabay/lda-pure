package com.jpc.nlp.lda.entry;

import gnu.trove.list.array.TIntArrayList;

import java.util.Date;
import java.util.List;

import com.jpc.nlp.lda.data.DataSet;
import com.jpc.nlp.lda.gibbs.GibbsSampler;

public class Main {

	private DataSet dataset;
	private GibbsSampler gibbs;

	public Main(String[] args) {

		this.init(args);

	}

	private static double alpha = 0;
	private static boolean alphaFlag = false;
	private static double beta = 0;
	private static boolean betaFlag = false;
	private static int topicCount = 0;
	private static int iterationCount = 0;
	private static boolean iterationCountFlag = false;
	private static int saveStep = 0;
	private static boolean saveStepFlag = false;
	private static int topWord = 0;
	private static boolean topWordFlag = false;
	private static String input = "process_data/";

	public boolean isSaveStepFlag() {
		return saveStepFlag;
	}

	public boolean isAlphaFlag() {
		return alphaFlag;
	}

	public boolean isBetaFlag() {
		return betaFlag;
	}

	public boolean isIterationCountFlag() {
		return iterationCountFlag;
	}

	public boolean isTopWordFlag() {
		return topWordFlag;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	public int getTopicCount() {
		return topicCount;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public int getSaveStep() {
		return saveStep;
	}

	public int getTopWord() {
		return topWord;
	}

	private void help() {

		String help = "java -jar lda.jar [-alpha <double>] [-beta <double>] -ntopic <int> [-niters <int>] [-savestep <int>] [-twords <int>] -input <String>";

		System.out.println(help);
	}

	private void init(String[] args) {

		int size = args.length;

		if (size == 0) {
			this.help();
			return;
		}

		for (int i = 0; i < size; i++) {

			String cmd = args[i];

			if (cmd.equalsIgnoreCase("-alpha")) {

				alpha = Double.parseDouble(args[i + 1]);

				alphaFlag = true;

				i++;
			}

			if (cmd.equalsIgnoreCase("-beta")) {

				beta = Double.parseDouble(args[i + 1]);
				betaFlag = true;

				i++;
			}

			if (cmd.equalsIgnoreCase("-ntopic")) {

				topicCount = Integer.parseInt(args[i + 1]);
				i++;
			}

			if (cmd.equalsIgnoreCase("-niters")) {

				iterationCount = Integer.parseInt(args[i + 1]);
				iterationCountFlag = true;
				i++;
			}

			if (cmd.equalsIgnoreCase("-savestep")) {

				saveStep = Integer.parseInt(args[i + 1]);
				saveStepFlag = true;
				i++;
			}

			if (cmd.equalsIgnoreCase("-twords")) {

				topWord = Integer.parseInt(args[i + 1]);
				topWordFlag = true;
				i++;
			}

			if (cmd.equalsIgnoreCase("-input")) {

				input = args[i + 1];

				i++;
			}
		}
	}

	public void start() {

		System.out.println("Start at:" + new Date());

		if (topicCount == 0) {
			System.out.println("Invalid topic count.....");
			return;
		}

		if (input == null || input.length() == 0) {
			System.out.println("Invalid input path.....");
			return;
		}

		dataset = new DataSet(input);

		List<TIntArrayList> r = dataset.document2Index();
		
		System.out.println("Dataset size: "+r.size()+",\tVocabulary size:"+dataset.getWord2Index().size());

		gibbs = new GibbsSampler(dataset, r, topicCount, this);

		gibbs.train();
		
		System.out.println("Finish at:" + new Date());
	}

	public static void main(String[] args) {

		Main m = new Main(args);

		m.start();
	}
}
