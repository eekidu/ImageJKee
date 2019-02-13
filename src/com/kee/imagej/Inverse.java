package com.kee.imagej;

import com.kee.imagej.filters.Fast_Filters;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.*;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.filter.RankFilters;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;

import java.io.File;

public class Inverse {

    public static void main(String args[]) {
        loadImage();
    }

    private static void Demo() {
        OpenDialog wo = new OpenDialog("");
        System.out.println("你打开的图像的路径是：");
        String we = wo.getPath();
        System.out.println(we);
//--------图像显示
        ImagePlus ming = new ImagePlus(we);
        ming.setTitle("原图像");
        ming.show();

        ImageProcessor ip1 = ming.getProcessor();
        ImageProcessor ip2 = ip1.duplicate();
        ip2.invert();
        ImagePlus ming2 = new ImagePlus("平滑后的图像", ip2);//图像有标题
        ming2.show();
    }


    private static void loadImage() {
        String fileName = "308DJ0016235-DJ20190122-190125-000813-816x519-830-15000ms-P127-noTag-1-83026.raw";
        String directory = "E:\\201901-Xi'an2\\308DJ0016235\\190125\\DJ20190122\\images\\image";

        FileInfo fi = new FileInfo();
        fi.fileType = FileInfo.GRAY16_UNSIGNED;
        fi.width = 816;
        fi.height = 519;
        fi.intelByteOrder = true;
        fi.fileFormat = fi.RAW;
        fi.fileName = fileName;
        if (!(directory.endsWith(File.separator) || directory.endsWith("/")))
            directory += "/";
        fi.directory = directory;

        FileOpener fo = new FileOpener(fi);
        ImagePlus imp = fo.openImage();
        short[] pixels = (short[]) imp.getProcessor().getPixels();
        for (short pixel : pixels) {
            System.out.print(pixel+",");
        }
        imp.show();

        FileSaver fileSaver = new FileSaver(imp);
        String filePath = "D:\\RAW\\" + fileName.replace("raw","tif");
        fileSaver.saveAsTiff(filePath);



//        lineMedian (radius, cache, pixels, writeFrom, writeTo, pixel0, pointInc, vLo, vHi);


//
        Fast_Filters fast_filters=new Fast_Filters();
//        fast_filters.setup(null,imp);
//        fast_filters.showDialog(imp,null,new PlugInFilterRunner());

//        String filePath = fi.directory+fi.fileName;
//        Recorder.recordCall(fi.getCode()+"imp = Raw.open(\""+filePath+"\", fi);");
//        if (imp!=null) {
//            imp.show();
//            int n = imp.getStackSize();
//            if (n>1) {
//                imp.setSlice(n/2);
//                ImageProcessor ip = imp.getProcessor();
//                ip.resetMinAndMax();
//                imp.setDisplayRange(ip.getMin(),ip.getMax());
//            }
//    }
    }

    // Median filter of a line; at the image borders it does not give extra weight to the border pixels.
    private static void lineMedian (int radius, float[] cache, float[] pixels, int writeFrom, int writeTo,
                                    int pixel0, int pointInc, float[] vHi, float[] vLo) {
        int length = cache.length;
        float median = Float.isNaN(cache[writeFrom]) ? 0 : cache[writeFrom]; //a first guess
        for (int i=writeFrom, iMinus=i-radius, iPlus=i+radius, p=pixel0;
             i<writeTo; i++, iMinus++,iPlus++,p+=pointInc) {
            int nHi = 0, nLo = 0;
            int iStart = (iMinus>=0) ? iMinus : 0;
            int iStop = (iPlus<length) ? iPlus : length-1;
            int nEqual = 0;
            for (int iRead=iStart; iRead <= iStop; iRead++) {
                float v = cache[iRead];
                if (v > median) vHi[nHi++] = v;
                else if (v < median) vLo[nLo++] = v;
                else if (v==v) nEqual++;    //if (!isNaN(v))
            }
            int nPoints = nHi + nLo + nEqual;
            if (nPoints == 0) {
                pixels[p] = Float.NaN;
            } else {
                if (nPoints%2 == 0) {  //avoid an even number of points: in case of doubt, leave it closer to original value
                    float v = cache[i];
                    if (v > median) vHi[nHi++] = v;
                    else if (v < median) vLo[nLo++] = v;
                }
                int half = nPoints/2;//>>1; //(nHi+nLo)/2, but faster
                if (nHi>half)
                    median = RankFilters.findNthLowestNumber(vHi, nHi, nHi-half-1);
                else if (nLo>half)
                    median = RankFilters.findNthLowestNumber(vLo, nLo, half);
                pixels[p] = median;
            }
        }
    }

}
