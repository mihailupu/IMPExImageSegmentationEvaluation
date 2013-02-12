/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import com.sun.media.jai.codec.JPEGEncodeParam;
import java.io.FileOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * @TODO Should harmonize the size of the images
 * 
 * @author mihailupu
 */
public class tiff2jpg {

    public static String convert(String absoluteFilename) {

        try {
//read the file

            RenderedOp img = JAI.create("FileLoad", absoluteFilename);

            
            
//open the output file stream

            FileOutputStream ostream = new FileOutputStream(absoluteFilename+".jpg");

//wants to store as JPEG
            JPEGEncodeParam encParam = new JPEGEncodeParam();
//set JPEG saving quality option, but optional
            encParam.setQuality(0.75F);

//save to the file as JPEG

            JAI.create("encode", img, ostream, "JPEG", encParam);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return absoluteFilename+".jpg";

    }
}
