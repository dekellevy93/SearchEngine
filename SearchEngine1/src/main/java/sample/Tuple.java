package sample;

/**
 * class for representation of a term and its frequency in a specific soc
 */
public class Tuple {
    private String term = null;
    private String frequency = null;

    public Tuple() {
    }

    public Tuple(String term, String frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    /**
     *
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     *
     * @param term
     * sets the term in the pair
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     *
     * @return the frequency of a term in a specific doc
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     *
     * @param frequency
     * sets the number of appearances of a term in the specific doc
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
