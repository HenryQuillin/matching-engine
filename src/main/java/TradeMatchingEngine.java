// package src.main.java;

import java.util.*;

public class TradeMatchingEngine {

    // Represents an order with all necessary details
    public static class Order {
        String orderId;
        String side; // "buy" or "sell"
        double price;
        int volume;
        long timestamp;

        // Constructor for the Order
        public Order(String orderId, String side, double price, int volume, long timestamp) {
            this.orderId = orderId;
            this.side = side;
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
        }

        // Getters for Order properties
        public String getOrderId() {
            return orderId;
        }

        public String getSide() {
            return side;
        }

        public double getPrice() {
            return price;
        }

        public int getVolume() {
            return volume;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    // Data structures for buy and sell orders
    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;
    private Map<String, Order> orderMap; 
    private Map<Double, Integer> buyVolumeMap; 
    private Map<Double, Integer> sellVolumeMap; 

    public TradeMatchingEngine() {
        // Min heap for sell orders based on price (lowest price priority)
        sellOrders = new PriorityQueue<>((a, b) -> a.price == b.price ? Long.compare(a.timestamp, b.timestamp) : Double.compare(a.price, b.price));
        
        // Max heap for buy orders based on price (highest price priority)
        buyOrders = new PriorityQueue<>((a, b) -> a.price == b.price ? Long.compare(a.timestamp, b.timestamp) : Double.compare(b.price, a.price));

        orderMap = new HashMap<String, Order>(); 
        buyVolumeMap = new TreeMap<Double, Integer>(); 
        sellVolumeMap = new TreeMap<Double, Integer>(); 

    }



    private void executeTrade(Order newOrder, Order currentOrder, double tradePrice, int tradeVolume){ 
        newOrder.volume -= tradeVolume;
        currentOrder.volume -= tradeVolume;
        // updateMarketDepth(newOrder, -tradeVolume) ;
        updateMarketDepth(currentOrder, -tradeVolume);


        if (currentOrder.volume <= 0) { 
            deleteOrder(currentOrder.orderId);
            // if (currentOrder.getSide().equals("buy")) {
            //     buyOrders.poll(); 
            // } else { 
            //     sellOrders.poll(); 
            // }
            // orderMap.remove(currentOrder.orderId); 

        } 


    }

    // Method to add an order to the engine
    public void addOrder(Order order) {
        // TODO: Implement logic to add the order to the order book, match trades, and return a Trade if applicable

        /* 
        Time complexity: 
        No order filled: O(1) 
        Order filled: O(logn)
        If there are multiple matches, this polling process continues until the order is filled or no more matches are found. In the worst case, this involves O(m log n) where m is the number of matches and n is the number of elements in the heap
         */
        PriorityQueue<Order> oppOrders = order.side.equals("sell") ? buyOrders : sellOrders; 
        PriorityQueue<Order> orders = order.side.equals("buy") ? buyOrders : sellOrders; 

        while (!oppOrders.isEmpty() && 
            ((order.side.equals("buy") && oppOrders.peek().price <= order.getPrice()) || 
                (order.side.equals("sell") && oppOrders.peek().price >= order.getPrice()))) {
            
            Order oppOrder = oppOrders.peek(); 
            double tradePrice = oppOrder.getPrice(); 
            int tradeVolume = Math.min(order.getVolume(), oppOrder.getVolume()); 

            executeTrade(order, oppOrder, tradePrice, tradeVolume); 

            if (order.getVolume() == 0) {
                break;
            }
        }  
        if (order.volume > 0) { 
            orderMap.put(order.orderId, order); 
            orders.add(order); 
            updateMarketDepth(order, order.getVolume());
        }
            
    }

    // Method to delete an existing order
    public void deleteOrder(String orderId) {
        // TODO: Implement logic to delete a pending order by its ID

        
        /*
         * Time complexity: 
         * Removing from map O(1)
         * Removing from PQ O(logn)
         */
        Order order = orderMap.getOrDefault(orderId, null); 
        if (order == null) return; 
        if (order.side.equals("buy")) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        orderMap.remove(orderId); 
        updateMarketDepth(order, -order.getVolume());



    }

    private void updateMarketDepth(Order order, Integer volumeDiff) {
        Map<Double, Integer> volumeMap = order.getSide().equals("buy") ? buyVolumeMap: sellVolumeMap; 

        double price = order.getPrice(); 
        
        int newVolume = volumeMap.getOrDefault(price,0) + volumeDiff; 

        if (newVolume <= 0) { 
            volumeMap.remove(price); 
        } else { 
            volumeMap.put(price, newVolume); 
        }

    }


    // Method to get the market depth/demand
    public Map<Double, Integer> getMarketDepth(String side) {
        /* 
         O(n) approach, where n is length of order book 
         // use tree map if the map needs to be sorted by price 
        Map<Double, Integer> depthMap = new HashMap<>(); 
         PriorityQueue<Order> orders = side.equals("buy") ? buyOrders : sellOrders; 
 
         for (Order order : orders) {
             depthMap.put(order.getPrice(), depthMap.getOrDefault(order.getPrice(),0) + order.getVolume()); 
         }
         return depthMap; 
         */
        return side.equals("buy") ? buyVolumeMap : sellVolumeMap;
    }

    public Map<Double, Integer> getMarketDepthFromRange(String side, Double startPrice, Double endPrice) {
        /*
         two liner 
        NavigableMap<Double, Integer> volumeMap = side.equals("buy") ?  (NavigableMap ) buyVolumeMap : (NavigableMap ) sellVolumeMap;
        return new HashMap<>(volumeMap.subMap(startPrice, true, endPrice, true));
         */
        Map<Double, Integer> depthMap = new HashMap<>();
        Map<Double, Integer> volumeMap = side.equals("buy") ? buyVolumeMap : sellVolumeMap;

        // Iterate through the volumeMap starting from startPrice
        for (Map.Entry<Double, Integer> entry : volumeMap.entrySet()) {
            double price = entry.getKey();
            if (price > endPrice) {
                break; // Stop if the price exceeds endPrice
            }
            if (price >= startPrice) {
                depthMap.put(price, entry.getValue());
            }
        }
        return depthMap;




    }

    public static void main(String[] args) {
        // Example usage of TradeMatchingEngine
        TradeMatchingEngine engine = new TradeMatchingEngine();
            
        engine.addOrder(new Order("1", "buy", 100.0, 10, System.currentTimeMillis()));
        engine.addOrder(new Order("2", "buy", 101.0, 15, System.currentTimeMillis()));
        engine.addOrder(new Order("3", "sell", 102.0, 20, System.currentTimeMillis()));
        engine.addOrder(new Order("4", "sell", 100.0, 5, System.currentTimeMillis()));

        System.out.println("Market Depth for Buy Orders:");
        Map<Double, Integer> buyDepth = engine.getMarketDepth("buy");
        for (Map.Entry<Double, Integer> entry : buyDepth.entrySet()) {
            System.out.println("Price: " + entry.getKey() + " | Volume: " + entry.getValue());
        }

        System.out.println("\nMarket Depth for Sell Orders:");
        Map<Double, Integer> sellDepth = engine.getMarketDepth("sell");
        for (Map.Entry<Double, Integer> entry : sellDepth.entrySet()) {
            System.out.println("Price: " + entry.getKey() + " | Volume: " + entry.getValue());
        }

        System.out.println("\nMarket Depth for Buy Orders in Range 100.0 to 101.0:");
        Map<Double, Integer> buyDepthRange = engine.getMarketDepthFromRange("buy", 100.0, 101.0);
        for (Map.Entry<Double, Integer> entry : buyDepthRange.entrySet()) {
            System.out.println("Price: " + entry.getKey() + " | Volume: " + entry.getValue());
        }
    }

    public PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }
}