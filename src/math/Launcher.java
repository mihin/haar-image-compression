/*
 	Haar wavelets image compression
 	
    Copyright (C) 2014 by Mikhail Prisheltsev <mikhail.prisheltsev@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package math;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import math.dwt.wavelets.*;
import math.utils.FileNamesConst;
import math.utils.Log;

public class Launcher {

	public static void main(String [] args) {
		// StreamHandler sh = new StreamHandler(System.out, new
		// SimpleFormatter());
		// Log.get().addHandler(sh);
		Level logLevel = Level.ALL;
		Log.getInstance().setLevel(logLevel);

		int wtLevel = 1;
		int quantLevels = 32;
//		Class wavelet = HaarClassic.class;
		Class wavelet = HaarAdaptive.class;
		
		TransmormationManager m = new TransmormationManager(wtLevel, quantLevels, wavelet);

		Log.getInstance().log(Level.CONFIG,
				"TransmormationManager launch. Decomposition depth " + wtLevel + ", quatLvl = " + quantLevels
				+ ". wavelet is " + wavelet.getSimpleName());

//		List<String> files = null;
//		String filename = "image1.bmp";
//		if (!m.start(filename))
//			System.err.println("File not found: " + filename);
//		else {
//			files = new ArrayList<String>();
//			files.add(filename);
//		}

		List<String> files = m.start(20);

		if (files != null && files.size() > 0) {
			System.err.println("\n-= End transformation. Begin analyse =-");

			System.out.println("Decompositon level = " + wtLevel + ", quantization levels = " + quantLevels
					+ ", wavelet = " + wavelet.getSimpleName());
			for (String file : files) {
				analyseResults(file);
			}
		} else {
			System.err.println("\n-= End transformation. Analyse failed since file list is empty =-");
		}

		// m.logFile = new File("log.txt");
		// m.loadDecompCoefs();
		// m.reconstructImage();
	}

	private static void analyseResults(final String fileName) {
		File resDir = new File(FileNamesConst.resultsFolder, FileNamesConst.picsFolder);
		if (!resDir.exists()) {
			System.err.println("Results folder doesn't exists");
			return;
		}
		StringBuilder sb = new StringBuilder();
		long origSize = 0;
		{
			File originalImage = new File(FileNamesConst.picsFolder, fileName);
			origSize = originalImage.length();
			BufferedImage bimg;
			try {
				bimg = ImageIO.read(originalImage);
				int width = bimg.getWidth();
				int height = bimg.getHeight();
				sb.append(fileName + " " + width + "x" + height + " " + origSize + " bytes");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final String bareName = fileName.substring(0, fileName.lastIndexOf('.'));
		for (File f : resDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith(bareName + "H") && arg1.endsWith(FileNamesConst.extBIN);
			}
		})) {

			long size = f.length();
			float percent = (float) size / (float) origSize * 100f;
			// System.out.println("File found: " + f.getName() + ", size = " +
			// size);
			sb.append(", ahaar " + size + " bytes (" + percent + "%)\n");
		}

		System.out.println(sb.toString());
	}
}
