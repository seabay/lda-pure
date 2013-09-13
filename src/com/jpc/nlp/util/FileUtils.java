package com.jpc.nlp.util;

import java.io.*;
import java.util.*;

public class FileUtils {

	/***
	 * 返回文件夹path中所有以suffix为后缀的文件名
	 * 
	 * */
	public static ArrayList<String> fileList(String path, String suffix) {
		LinkedList<File> list = new LinkedList<File>();
		ArrayList<String> files = new ArrayList<String>();
		File dir = new File(path);
		if (!dir.exists()) {
			System.err.println("the dir doesn't exist: " + path);
			return null;
		}
		File file[] = dir.listFiles();
		if (file == null) {
			System.err.println("no files in the dir: " + path);
			return null;
		}
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory())
				list.add(file[i]);
			else {
				String curpath = file[i].getAbsolutePath();
				if (curpath.endsWith(suffix))
					files.add(curpath);
			}

		}
		File tmp;
		while (!list.isEmpty()) {
			tmp = list.removeFirst();
			if (tmp.isDirectory()) {
				file = tmp.listFiles();
				if (file == null)
					continue;
				for (int i = 0; i < file.length; i++) {
					if (file[i].isDirectory())
						list.add(file[i]);
					else {
						String curpath = file[i].getAbsolutePath();
						if (curpath.endsWith(suffix))
							files.add(curpath);
					}
				}
			} else {
				String curpath = tmp.getAbsolutePath();
				if (curpath.endsWith(suffix))
					files.add(curpath);
			}
		}
		dir = null;
		list = null;
		return files;
	}

	/**
	 * Read file according encode
	 * 
	 * @param file
	 *            target file
	 * @param encoding
	 *            encoding of file
	 * @return content of file
	 */

	public static String ReadFilebyChar(File file, String encoding) {
		if (!file.exists())
			return null;

		int len = (int) file.length();// bug
		if (len <= 0)
			return null;
		BufferedReader reader = null;
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file), encoding);
			reader = new BufferedReader(read);

			char[] fcontent = new char[len];
			reader.read(fcontent, 0, len);
			return new String(fcontent);
		} catch (IOException e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
	}

	/**
	 * Read file according encode
	 * 
	 * @param fileFullPath
	 *            full file path
	 * @param encoding
	 *            encoding of file
	 * @return content of file
	 */
	public static String ReadFile(String fileFullPath, String encoding) {
		File file = new File(fileFullPath);

		return ReadFilebyChar(file, encoding);
	}

	/*****
	 * 返回文件file中所有行
	 * */
	public static ArrayList<String> ReadbyLine(File file, String encoding) {
		if (!file.exists())
			return null;

		BufferedReader reader = null;

		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file), encoding);
			reader = new BufferedReader(read);
			ArrayList<String> lines = new ArrayList<String>(2000);

			String tempString = null;

			while ((tempString = reader.readLine()) != null) {
				lines.add(tempString);
			}

			return lines;
			
		} catch (IOException e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
	}

	/****
	 * 返回文件fileFullPath中所有的行
	 * */
	public static ArrayList<String> ReadbyLine(String fileFullPath,
			String encoding) {
		File file = new File(fileFullPath);

		return ReadbyLine(file, encoding);
	}

	/**
	 * Write content to file
	 * 
	 * @param fileFullPath
	 *            full file path
	 * @param newLine
	 *            content to write
	 */
	public static void WriteFile(String fileFullPath, String newLine) {
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileFullPath, true), "utf-8"));

			bw.write(newLine);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/****
	 * content中的内容按行写入fileFullPath中
	 * */
	public static void WriteFilebyLine(String fileFullPath,
			ArrayList<String> content, String encoding) {
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileFullPath, true), encoding));

			Iterator<String> it = content.iterator();
			while (it.hasNext()) {
				String curline = it.next();
				bw.write(curline);
				bw.newLine();
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/****
	 * 清理词典：去掉前后的空格 返回清理后的词的个数
	 * */
	public static int cleanDict(String inpath, String inencoding,
			String outpath, String outencoding) throws Exception {
		if (outpath.equals(inpath))
			return 0;

		ArrayList<String> wds = ReadbyLine(inpath, inencoding);

		if (wds == null)
			return 0;

		int cnt = 0;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outpath, true), outencoding));
		for (String w : wds) {
			w = w.trim();
			if (w.length() > 0) {
				bw.write(w + "\n");
				cnt++;
			}
		}
		bw.close();
		return cnt;

	}
}
