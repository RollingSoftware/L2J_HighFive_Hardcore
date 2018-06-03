package ai.individual.Zaken;

import java.util.Arrays;
import java.util.List;

public class AdjacentCandles {

    private final int leftCandleId;
    private final int upCandleId;
    private final int downCandleId;
    private final int rightCandleId;

    public AdjacentCandles(int leftCandleId, int upCandleId, int downCandleId, int rightCandleId) {
        this.leftCandleId = leftCandleId;
        this.upCandleId = upCandleId;
        this.downCandleId = downCandleId;
        this.rightCandleId = rightCandleId;
    }

    public int getLeftCandleId() {
        return leftCandleId;
    }

    public int getRightCandleId() {
        return rightCandleId;
    }

    public int getUpCandleId() {
        return upCandleId;
    }

    public int getDownCandleId() {
        return downCandleId;
    }

    public List<Integer> getCandleIds() {
        return Arrays.asList(leftCandleId, upCandleId, downCandleId, rightCandleId);
    }

    public static AdjacentCandles none() {
        return new AdjacentCandles(-1, -1, -1, -1);
    }

}
