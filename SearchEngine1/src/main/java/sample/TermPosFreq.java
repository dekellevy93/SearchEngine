package sample;

public class TermPosFreq {
    /**
     * info about a term in a doc.
     * pos : 0 if the term first found in the first half of the text, 1 else.
     * freqInDoc: frequency of the term in the doc/
     */
    private byte pos; // 0 if in start, else 1
    private int freqInDoc;

    public TermPosFreq(byte pos, int freqInDoc) {
        this.pos = pos;
        this.freqInDoc = freqInDoc;
    }

    public Byte getPos() {
        return pos;
    }

    /**
     *
     * @param pos
     * sets the position of the term in  a doc
     */
    public void setPos(byte pos) {
        this.pos = pos;
    }

    /**
     *
     * @return
     * get the term frequency in a specific doc
     */
    public int getFreqInDoc() {
        return freqInDoc;
    }

    /**
     * increase the term frequency by one
     */
    public void add1ToFreqInDoc() {
        this.freqInDoc ++;
    }

    /**
     *
     * @param freqInDoc
     * sets the term frequency by one
     */
    public void setFreqInDoc(int freqInDoc) {
        this.freqInDoc = freqInDoc;
    }
}
