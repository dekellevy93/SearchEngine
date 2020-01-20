package sample;

public class TermPostingData {
    private double freqInDoc;
    private double howManyDocs;
    private int pos; //0 or 1

    public TermPostingData(double freqInDoc, double howManyDocs, int pos) {
        this.freqInDoc = freqInDoc;
        this.howManyDocs = howManyDocs;
        this.pos = pos;
    }

    public double getFreqInDoc() {
        return freqInDoc;
    }

    public double getHowManyDocs() {
        return howManyDocs;
    }

    public int getPos() {
        return pos;
    }
}
