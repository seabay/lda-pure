package com.jpc.nlp.lda.gibbs;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.jpc.nlp.lda.data.DataSet;
import com.jpc.nlp.lda.entry.Main;
import com.jpc.nlp.util.Utils;
import com.jpc.nlp.util.Utils.Pair;

/**
 * Symmetric implementation
 * 
 * @author Jia
 * 
 */
public class GibbsSampler {

	private final int burnInPeriod = 200; // // 迭代次数小于这个值的不考虑
	private final int thinInterval = 50; // // 使用这个范围内的参数值的平均值作为一次参数训练结果

	private static int saveStep = 500;
	private static int iterations = 3000;

	private int topWordCount = 50;

	private static double alpha;
	private static double beta = 0.01;

	private double[][] theta; // ///
	private double[][] phi;

	/**
	 * nm(k) refers to the number of times that topic k has been observed with a
	 * word of document m
	 */
	private int[][] topicCount4Doc; // / M*K
	private int[] topicCount4DocSum; // / M

	/**
	 * nk(t) denote the number of times that term t has been observed with topic
	 * k
	 */
	private int[][] wordCount4Topic; // / K*V
	private int[] wordCount4TopicSum; // // K 同 total number of words in
										// document i, size M

	private TIntObjectMap<String> index2Word;

	private List<TIntArrayList> docs;

	private int topicCount;
	private int docCount;
	private int vocabularyCount;

	private int[] docLength;

	private int[][] topic4WordInDoc; // // 每个文档中的word属于哪个topic, M*N, 对应 z

	private DataSet dataset;

	private Utils u;

	public GibbsSampler(DataSet d, List<TIntArrayList> ds, int k, Main m) {

		u = new Utils();

		this.initParams(m, k);

		this.initStorage(ds, k, d);
	}

	/**
	 * 保存训练过程的中间结果和最终参数结果
	 * 
	 * @param ds
	 * @param k
	 * @param p
	 */
	private void initStorage(List<TIntArrayList> ds, int k, DataSet d) {

		dataset = d;

		docs = ds;

		topicCount = k;

		docCount = docs.size(); // /// m

		docLength = new int[docCount];

		topicCount4Doc = new int[docCount][topicCount];
		topicCount4DocSum = new int[docCount];

		index2Word = dataset.getIndex2Word();

		vocabularyCount = dataset.getWord2Index().size(); // // t

		wordCount4Topic = new int[topicCount][vocabularyCount];
		wordCount4TopicSum = new int[topicCount];

		theta = new double[docCount][topicCount];
		phi = new double[topicCount][vocabularyCount];
	}

	/**
	 * 训练前需要设置的一些可选参数
	 * 
	 * @param m
	 * @param k
	 */
	private void initParams(Main m, int k) {

		if (m.isAlphaFlag())
			alpha = m.getAlpha();
		else
			alpha = 50.0 / (double) k;

		if (m.isBetaFlag())
			beta = m.getBeta();

		if (m.isIterationCountFlag())
			iterations = m.getIterationCount();

		if (m.isSaveStepFlag())
			saveStep = m.getSaveStep();

		if (m.isTopWordFlag())
			this.topWordCount = m.getTopicCount();

	}

	/**
	 * for all documents m in[1; M] do for all wordsn2[1; Nm] in document m do
	 * sample topic index zm;n=k~Mult(1=K) increment document–topic count: n(k)m
	 * +=1 increment document–topic sum: nm+=1 increment topic–term count:n(t)k
	 * +=1 increment topic–term sum:n k +=1
	 */
	private void GibbsInit() {

		topic4WordInDoc = new int[docCount][]; // /M*N

		for (int docIndex = 0; docIndex < docCount; docIndex++) { // i = doc

			TIntArrayList doc = docs.get(docIndex);

			int size = doc.size(); // // 一个document的word的数目

			docLength[docIndex] = size;

			topic4WordInDoc[docIndex] = new int[size];

			for (int word = 0; word < size; word++) { // word 代表一个词在一个文档中的位置

				int wordIndex = doc.get(word); // wordIndex 代表这个词在Vocabulary中的下标

				int topic = (int) (Math.random() * topicCount);
				topic4WordInDoc[docIndex][word] = topic;

				this.topicCount4DocIncrement(docIndex, topic);
				this.wordCount4TopicInrement(topic, wordIndex);
				this.topicCount4DocSumIncrement(docIndex);
				this.wordCount4TopicSumIncrement(topic);
			}
		}
	}

	private void topicCount4DocIncrement(int doc, int topic) {

		topicCount4Doc[doc][topic] += 1;
	}

	private void topicCount4DocDecrement(int doc, int topic) {

		topicCount4Doc[doc][topic] -= 1;
	}

	private void wordCount4TopicInrement(int topic, int word) {

		wordCount4Topic[topic][word] += 1;
	}

	private void wordCount4TopicDecrement(int topic, int word) {

		wordCount4Topic[topic][word] -= 1;
	}

	private void topicCount4DocSumIncrement(int doc) {

		topicCount4DocSum[doc] += 1;
	}

	private void topicCount4DocSumDecrement(int doc) {

		topicCount4DocSum[doc] -= 1;
	}

	private void wordCount4TopicSumIncrement(int topic) {

		wordCount4TopicSum[topic] += 1;
	}

	private void wordCount4TopicSumDecrement(int topic) {

		wordCount4TopicSum[topic] -= 1;
	}

	private int countInParams = 0;
	private int intervalEnd = 0;
	private int intervalStart = 0;
	private int sampleLag = 5; // // 50 - 5 - 50 - 5 - 50

	private void sampling() {

		System.out.println("Sampling " + iterations + " iterations!");

		int curStep = 0;

		intervalStart = burnInPeriod;
		intervalEnd = burnInPeriod + thinInterval + sampleLag;

		for (; curStep < iterations; curStep++) {

			System.out.println("Iteration " + curStep + " .......");

			for (int docIndex = 0; docIndex < docCount; docIndex++) {

				TIntArrayList doc = docs.get(docIndex);

				int size = doc.size(); // // 一个document的word的数目

				for (int wordIndex = 0; wordIndex < size; wordIndex++) { // /
																			// 这里的
																			// wordIndex
																			// 是在
																			// document中的位置

					int topic = this.sampleFullConditional(docIndex, wordIndex);
					topic4WordInDoc[docIndex][wordIndex] = topic;
				}
			}

			if (curStep < burnInPeriod) { // // burn in period
				continue;
			}

			if (curStep > burnInPeriod && (intervalStart <= curStep)
					&& (intervalEnd > curStep)) { // // 每一个 interval 区间内求参数的和

				countInParams++;
				this.updateParams();
			}

			if (intervalEnd == curStep) {

				intervalStart = intervalEnd + sampleLag;
				intervalEnd += thinInterval + sampleLag;
			}

			if (curStep > 0 && saveStep > 0 && (curStep % saveStep == 0)) {

				System.out.println("Saving the model at iteration " + curStep
						+ " ........");

				double[][] tmpPhi = this.computePhi();

				double[][] tmpTheta = this.computeTheta();

				this.saveModel(curStep, tmpPhi, tmpTheta);
			}
		}

		System.out.println("Saving the model at iteration " + curStep
				+ " ........");

		double[][] tmpPhi = this.computePhi();

		double[][] tmpTheta = this.computeTheta();

		this.saveModel(curStep, tmpPhi, tmpTheta);

	}

	private int sampleFullConditional(int doc, int word) {

		// remove z_i from the count variables
		int topic = topic4WordInDoc[doc][word];

		int wordIndex = docs.get(doc).get(word);

		this.topicCount4DocDecrement(doc, topic);
		this.wordCount4TopicDecrement(topic, wordIndex);
		this.topicCount4DocSumDecrement(doc);
		this.wordCount4TopicSumDecrement(topic);

		// do multinomial sampling via cumulative method:
		double[] posteriorTopic = new double[this.topicCount];
		for (int z = 0; z < topicCount; z++) {

			posteriorTopic[z] = (wordCount4Topic[z][wordIndex] + beta)
					/ (wordCount4TopicSum[z] + vocabularyCount * beta)
					* (topicCount4Doc[doc][z] + alpha)
					/ (topicCount4DocSum[doc] + topicCount * alpha);

		}

		// cumulate multinomial parameters
		for (int z = 1; z < posteriorTopic.length; z++) {
			posteriorTopic[z] += posteriorTopic[z - 1];
		}

		// scaled sample because of unnormalised p[]
		double u = Math.random() * posteriorTopic[topicCount - 1];
		for (topic = 0; topic < posteriorTopic.length; topic++) {
			if (u < posteriorTopic[topic])
				break;
		}

		// add newly estimated z_i to count variables
		this.topicCount4DocIncrement(doc, topic);
		this.wordCount4TopicInrement(topic, wordIndex);
		this.topicCount4DocSumIncrement(doc);
		this.wordCount4TopicSumIncrement(topic);

		return topic;
	}

	private void updateParams() {

		this.updateTheta();
		this.updatePhi();
	}

	private void updateTheta() {

		for (int docIndex = 0; docIndex < this.docCount; docIndex++) {

			for (int topicIndex = 0; topicIndex < this.topicCount; topicIndex++) {

				theta[docIndex][topicIndex] += (this.topicCount4Doc[docIndex][topicIndex] + beta)
						/ (this.topicCount4DocSum[docIndex] + this.topicCount
								* beta);
			}
		}
	}

	private void updatePhi() {

		for (int topicIndex = 0; topicIndex < this.topicCount; topicIndex++) {

			for (int wordIndex = 0; wordIndex < this.vocabularyCount; wordIndex++) {

				phi[topicIndex][wordIndex] += (this.wordCount4Topic[topicIndex][wordIndex] + alpha)
						/ (this.wordCount4TopicSum[topicIndex] + this.vocabularyCount
								* alpha);
			}
		}
	}

	private double[][] computeTheta() {

		double[][] tmpTheta = new double[docCount][topicCount];

		for (int docIndex = 0; docIndex < this.docCount; docIndex++) {

			for (int topicIndex = 0; topicIndex < this.topicCount; topicIndex++) {

				tmpTheta[docIndex][topicIndex] = theta[docIndex][topicIndex]
						/ countInParams;
			}
		}

		return tmpTheta;
	}

	private double[][] computePhi() {

		double[][] tmpPhi = new double[topicCount][vocabularyCount];

		for (int topicIndex = 0; topicIndex < this.topicCount; topicIndex++) {

			for (int wordIndex = 0; wordIndex < this.vocabularyCount; wordIndex++) {

				tmpPhi[topicIndex][wordIndex] = phi[topicIndex][wordIndex]
						/ countInParams;
			}
		}

		return tmpPhi;
	}

	public void train() {

		this.GibbsInit();

		this.sampling();
	}

	private void saveWordTopic(int ver, double[][] tmpPhi) {

		File f = new File("output/word_topic" + ver + ".txt");

		try {

			FileWriter fw = new FileWriter(f);

			for (int topicIndex = 0; topicIndex < this.topicCount; topicIndex++) {

				fw.write("Topic	" + topicIndex);
				fw.write("\n");

				List<Map.Entry<Integer, Double>> lst = u
						.sortArray2(tmpPhi[topicIndex]);

				int i = 1;

				for (Map.Entry<Integer, Double> e : lst) {

					if (i == topWordCount)
						break;

					fw.write("\t\t" + index2Word.get(e.getKey()));
					fw.write(":");
					fw.write("" + e.getValue());
					fw.write("\t");

					i++;

					fw.write("\n");
				}
			}

			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void saveTopicDoc(int ver, double[][] tmpTheta) {

		File f = new File("output/topic_document" + ver + ".txt");

		try {
			Writer fw = new FileWriter(f);

			for (int docIndex = 0; docIndex < this.docCount; docIndex++) {

				fw.write("Document	" + dataset.getIndex2Doc().get(docIndex));
				fw.write("\n");

				List<Pair> lst = u.sortArray(tmpTheta[docIndex]);

				for (Pair p : lst) {

					fw.write("\t\t" + p.getD1());
					fw.write(":");
					fw.write("" + p.getD2());
					fw.write("\t");

					fw.write("\n");
				}
			}

			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void saveModel(int ver, double[][] tmpPhi, double[][] tmpTheta) {

		this.saveWordTopic(ver, tmpPhi);

		this.saveTopicDoc(ver, tmpTheta);
	}

}
