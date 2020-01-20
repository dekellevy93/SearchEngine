package sample;

public class DocInFolderData {
    /**
     * information about a certain doc.
     * max term frequency in the doc, how many unique terms are in the doc, length of the text in the doc.
     */

    private int maxTF;
    private int uniquTermsAmount;
    private int textLength;

    public DocInFolderData(int uniquTermsAmount, int textLength) {
        this.uniquTermsAmount = uniquTermsAmount;
        this.textLength = textLength;
        this.maxTF = 0;
    }

    public int getMaxTF() {
        return maxTF;
    }

    public void setMaxTF(int maxTF) {
        this.maxTF = maxTF;
    }

    public int getUniquTermsAmount() {
        return uniquTermsAmount;
    }

    public void setUniquTermsAmount(int uniquTermsAmount) {
        this.uniquTermsAmount = uniquTermsAmount;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    @Override
    public String toString() {
        return "DocInFolderData{" +
                "maxTF=" + maxTF +
                ", uniquTermsAmount=" + uniquTermsAmount +
                ", textLength=" + textLength +
                '}';
    }
}
