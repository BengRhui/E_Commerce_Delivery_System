package utility;

import context.DataManager;
import entity.Address;
import entity.Address.State;
import entity.Item;
import entity.Order;
import entity.Order.PaymentType;
import entity.OrderBin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static context.ThreadManager.ORDER_BIN_SIZE;
import static context.ThreadManager.WRONG_DETAIL_PROBABILITY;

/**
 * A class to initialize different objects and queues.
 */
public class Initializer {

    // Constants
    private static final int MIN_ITEMS_PER_ORDER = 1;
    private static final int MAX_ITEMS_PER_ORDER = 5;
    private static final int MIN_ITEM_QUANTITY = 1;
    private static final int MAX_ITEM_QUANTITY = 5;
    private static final int MIN_INVENTORY_QUANTITY = 300;
    private static final int MAX_INVENTORY_QUANTITY = 2000;

    // Fields to help initialize order details: Payment types
    private static final PaymentType[] paymentTypeList = {
            PaymentType.CARD,
            PaymentType.E_WALLET,
            PaymentType.ONLINE_BANKING,
            PaymentType.CASH_ON_DELIVERY
    };

    // Address list to help initialize orders
    private static final String[] addressLineOneList = {
            "1, Jalan Buaya",
            "19, Jalan Kancil",
            "23, Jalan Kerisa",
            "45, Jalan Harimau",
            "78, Jalan Singa",
            "93, Jalan Monyet"
    };

    // City and regions to help initialize orders
    private static final List<Map.Entry<State, String>> cityAndRegion = List.of(
            Map.entry(State.KEDAH,             "Alor Setar"),
            Map.entry(State.PERLIS,            "Kangar"),
            Map.entry(State.KELANTAN,          "Kota Bharu"),
            Map.entry(State.TERENGGANU,        "Kuala Terengganu"),
            Map.entry(State.PENANG,            "George Town"),
            Map.entry(State.PAHANG,            "Kuantan"),
            Map.entry(State.PERAK,             "Ipoh"),
            Map.entry(State.SELANGOR,          "Puchong"),
            Map.entry(State.NEGERI_SEMBILAN,   "Seremban"),
            Map.entry(State.MELAKA,            "Ayer Keroh"),
            Map.entry(State.JOHOR,             "Muar"),
            Map.entry(State.SABAH,             "Sandakan"),
            Map.entry(State.SARAWAK,           "Miri"),
            Map.entry(State.WP_KL,             "Bukit Jalil"),
            Map.entry(State.WP_PUTRAJAYA,      "Presint 3"),
            Map.entry(State.WP_LABUAN,         "Victoria")
    );

    // List of items to be used in orders
    private static final Item[] itemList = {
            new Item("Wireless Mouse"),
            new Item("Bluetooth Headphones"),
            new Item("Coffee Mug"),
            new Item("Notebook"),
            new Item("LED Desk Lamp"),
            new Item("Phone Charger"),
            new Item("Running Shoes"),
            new Item("Backpack"),
            new Item("Water Bottle"),
            new Item("Fitness Tracker"),
            new Item("Scented Candle"),
            new Item("Smartphone Stand"),
            new Item("Portable Power Bank"),
            new Item("Gaming Keyboard"),
            new Item("T-shirt"),
            new Item("Yoga Mat")
    };

    // Main method to populate everything
    public static void initializeAll() throws InterruptedException {

        // A random object to be used
        ThreadLocalRandom randomObject = ThreadLocalRandom.current();

        // Initialize the inventory list
        for (Item item: itemList) {

            // Generate a random quantity and put it into the list
            int quantity = randomObject.nextInt(MIN_INVENTORY_QUANTITY, MAX_INVENTORY_QUANTITY);
            DataManager.inventoryList.put(item, quantity);
        }

        // Initialize the order bins: Order bins will be circulated but not created by threads
        for (int i = 0; i < ORDER_BIN_SIZE; i++) {
            DataManager.orderBinQueue.put(new OrderBin());
        }
    }

    // A method to create a brand-new order
    public static Order createRandomOrder() {
        return createRandomOrder(true, null);
    }

    // A method to recreate an order with existing ID
    public static Order createRandomOrder(String orderId) {
        return createRandomOrder(false, orderId);
    }

    // A main method to create a new order
    public static Order createRandomOrder(boolean requireNewId, String orderId) {

        // First, check if valid arguments are passed in. Existing orders cannot have empty order ID
        if (!requireNewId && (orderId == null || orderId.isEmpty())) {
            throw new IllegalArgumentException("Order ID must be provided if requireNewId is false.");
        }

        // A random object to be used to generate random details
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Generate payment type
        int paymentIndex = random.nextInt(paymentTypeList.length);
        PaymentType paymentType = paymentTypeList[paymentIndex];

        // Generate error in payment
        if (random.nextDouble() < WRONG_DETAIL_PROBABILITY) paymentType = null;

        // Generate address
        int houseIndex = random.nextInt(addressLineOneList.length);
        int cityIndex = random.nextInt(cityAndRegion.size());
        Address address = new Address(
                addressLineOneList[houseIndex],
                cityAndRegion.get(cityIndex).getValue(),
                cityAndRegion.get(cityIndex).getKey()
        );

        // Generate error in address
        if (random.nextDouble() < WRONG_DETAIL_PROBABILITY) address = null;

        // Generate the size of the item list randomly
        Map<Item, Integer> itemMap = new HashMap<>();
        int itemListSize = random.nextInt(MIN_ITEMS_PER_ORDER, MAX_ITEMS_PER_ORDER);

        // Generate error in item list size
        if (random.nextDouble() < WRONG_DETAIL_PROBABILITY) itemListSize = 0;

        // Populate items based on size
        for (int i = 0; i < itemListSize; i++) {

            // Get random index and quantity
            int itemIndex = random.nextInt(itemList.length);
            int itemQuantity = random.nextInt(MIN_ITEM_QUANTITY, MAX_ITEM_QUANTITY);

            // Add the item to the map
            itemMap.merge(itemList[itemIndex], itemQuantity, Integer::sum);
        }

        // Check if a new ID is required and proceed with the corresponding constructor
        return requireNewId
                ? new Order(paymentType, itemMap, address)
                : new Order(orderId, paymentType, itemMap, address);
    }
}
