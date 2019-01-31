package edu.cmu.lti.dialogos.sphinx.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DataURLHelper {

    static String encodeData(String data) {
        String s = null;
        data = data.replaceAll("\\+", "%2B");
        try {
            s = "data:" + URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            s = "data:" + data; // let's try without encoding then...
        }
        return s;
    }
}
