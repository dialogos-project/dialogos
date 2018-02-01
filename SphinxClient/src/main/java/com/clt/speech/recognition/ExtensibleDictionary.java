package com.clt.speech.recognition;

import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.linguist.dictionary.TextDictionary;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by timo on 18.11.17.
 */
public class ExtensibleDictionary extends TextDictionary {

    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            if (!"factory already defined".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    public void loadExceptions(List<G2PEntry> g2pList) throws IOException {
        if (!g2pList.isEmpty()) {
            StringBuilder sb = new StringBuilder("data:");
            for (G2PEntry e : g2pList) {
                sb.append(e.getGraphemes());
                sb.append(" ");
                sb.append(e.getPhonemes());
                sb.append("\n");
            }
            addendaUrlList.add(new URL(sb.toString()));
        }
    }

}
