package io.github.rockerhieu.emojicon.emoji;

public class EmojiArray {

    private int[] codePoints;
    private int resource;
    private int one_codePoints;

    //Full Appt (0)
    public EmojiArray(int[] codePoints, int resource) {
        this.codePoints = codePoints;
        this.resource = resource;
    }

    public EmojiArray(int one_codePoints , int resource){
        this.one_codePoints = one_codePoints;
        this.resource = resource;
    }

    public int[] getCodePoints() {
        return codePoints;
    }

    public void setCodePoints(int[] codePoints) {
        this.codePoints = codePoints;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public int getOne_codePoints() {
        return one_codePoints;
    }

    public void setOne_codePoints(int one_codePoints) {
        this.one_codePoints = one_codePoints;
    }

}

