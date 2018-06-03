package ai.individual.Zaken;

import java.util.List;
import java.util.stream.Collectors;

public class Ship {

    private final List<ShipRoom> shipRooms;

    public Ship(List<ShipRoom> shipRooms) {
        this.shipRooms = shipRooms;
    }

    public ShipRoom findRoom(int roomNumber) {
        return shipRooms.stream().filter(i -> i.getRoomNumber() == roomNumber).findFirst().orElse(ShipRoom.none());
    }

    public List<ShipRoom> findRoomsByCandleId(int candleId) {
        return shipRooms.stream()
            .filter(room -> room.getAdjacentCandles().getCandleIds().contains(candleId))
            .collect(Collectors.toList());
    }

}
