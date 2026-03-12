package entity;

/**
 * This class represents the address to be included in different orders.
 */
public class Address {

    // Enum to store different regions (states in Malaysian context)
    public enum State {
        KEDAH, PERLIS, KELANTAN, TERENGGANU, PENANG,
        PAHANG, PERAK, SELANGOR, NEGERI_SEMBILAN, MELAKA,
        JOHOR, SABAH, SARAWAK, WP_KL, WP_PUTRAJAYA, WP_LABUAN
    }

    // Enum to store different regions
    public enum Region {
        EAST_MALAYSIA, NORTHERN_REGION, CENTRAL_REGION, SOUTHERN_REGION, EASTERN_REGION
    }

    // Information about address
    private final String lineOne;
    private final String city;
    private final State state;
    private final Region region;

    // Constructor for address
    public Address(String lineOne, String city, State state) {
        this.lineOne = lineOne;
        this.city = city;
        this.state = state;
        this.region = switch (state) {
            case SABAH, SARAWAK, WP_LABUAN                      -> Region.EAST_MALAYSIA;
            case PERLIS, KEDAH, PENANG, PERAK                   -> Region.NORTHERN_REGION;
            case SELANGOR, WP_KL, WP_PUTRAJAYA, NEGERI_SEMBILAN -> Region.CENTRAL_REGION;
            case PAHANG, TERENGGANU, KELANTAN                   -> Region.SOUTHERN_REGION;
            case MELAKA, JOHOR                                  -> Region.EASTERN_REGION;
        };
    }

    // Getter for address
    public State getState() {
        return state;
    }

    // Getter for region
    public Region getRegion() {
        return region;
    }

    // Method to output address
    @Override
    public String toString() {
        return lineOne + " " + city + " " + state;
    }

    // It is assumed that no edit will take place, so setters are not declared
}
