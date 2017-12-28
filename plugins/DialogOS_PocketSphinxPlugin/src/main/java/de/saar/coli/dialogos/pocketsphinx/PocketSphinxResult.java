package de.saar.coli.dialogos.pocketsphinx;

import com.clt.speech.recognition.AbstractRecognitionResult;
import com.clt.speech.recognition.Utterance;
import edu.cmu.pocketsphinx.NBest;
import edu.cmu.pocketsphinx.NBestIterator;
import edu.cmu.pocketsphinx.NBestList;
import java.util.ArrayList;
import java.util.List;

public class PocketSphinxResult extends AbstractRecognitionResult {

    private List<PocketSphinxUtterance> utt;
    private static final int MAX_N = 10;

    public PocketSphinxResult(NBestList nBestList) {
        utt = new ArrayList<>();
        NBestIterator it = nBestList.iterator();

        for (int i = 0; i < 10 && it.hasNext(); i++) {
            NBest nbestHyp = it.next();
            utt.add(new PocketSphinxUtterance(nbestHyp.getHypstr(), nbestHyp.getScore()));
        }
    }

    @Override
    public int numAlternatives() {
        return utt.size();
    }

    @Override
    public Utterance getAlternative(int index) {
        return utt.get(index);
    }
}
