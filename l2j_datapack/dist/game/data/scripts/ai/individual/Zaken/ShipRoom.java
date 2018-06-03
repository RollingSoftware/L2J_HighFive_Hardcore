package ai.individual.Zaken;

import com.l2jserver.gameserver.model.Location;

public class ShipRoom {
    private final int roomNumber;
    private final Location roomCenter;
    private final AdjacentCandles candles;

    public ShipRoom(int roomNumber, Location roomCenter, AdjacentCandles candles) {
        this.roomNumber = roomNumber;
        this.roomCenter = roomCenter;
        this.candles = candles;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public Location getRoomCenter() {
        return roomCenter;
    }

    public AdjacentCandles getCandleIds() {
        return candles;
    }
}
