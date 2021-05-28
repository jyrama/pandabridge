package fi.jrd.pandabridgemod.matrix;

public class Homeserver {
    private String address;

    public Homeserver(String address) {
        if (address == null) {
            throw new NullPointerException("Homeserver address was null!");
        }

        this.address = address;
    }

    public Room getRoom(String id) {
        return new Room(id, this);
    }

    public String getAddress() {
        return address;
    }
}
