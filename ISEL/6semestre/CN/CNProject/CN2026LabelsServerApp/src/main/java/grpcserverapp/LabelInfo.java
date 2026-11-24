package grpcserverapp;

public class LabelInfo {
    private String original;
    private String translated;
    private double score;

    public LabelInfo() {}

    public LabelInfo(String original, String translated, double score) {
        this.original = original;
        this.translated= translated;
        this.score=score;
    }

    public String getOriginal() {
        return original;
    }

    public String getTranslated() {
        return translated;
    }

    public double getScore() {
        return score;
    }

    public void setOriginal(String original) {
        this.original = original;
    }
    public void setTranslated(String translated) {this.translated = translated;}
    public void setScore(double score) {
        this.score = score;
    }
}
