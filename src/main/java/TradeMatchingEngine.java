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

    public TradeMatchingEngine() {
        // Min heap for sell orders based on price (lowest price priority)
        sellOrders = new PriorityQueue<>((a, b) -> a.price == b.price ? Long.compare(a.timestamp, b.timestamp) : Double.compare(a.price, b.price));
        
        // Max heap for buy orders based on price (highest price priority)
        buyOrders = new PriorityQueue<>((a, b) -> a.price == b.price ? Long.compare(a.timestamp, b.timestamp) : Double.compare(b.price, a.price));

        orderMap = new HashMap<String, Order>(); 
    }

    private void executeTrade(Order newOrder, Order currentOrder, double tradePrice, int tradeVolume){ 
        newOrder.volume -= tradeVolume; 
        currentOrder.volume -= tradeVolume; 

        if (currentOrder.volume <= 0) { 
            if (currentOrder.getSide().equals("buy")) {
                buyOrders.poll(); 
            } else { 
                sellOrders.poll(); 
            }
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

        if (order.side.equals("buy")) {
            while (sellOrders.size() > 0 && sellOrders.peek().price <= order.getPrice()) { 
                Order sellOrder = sellOrders.peek(); 
                double tradePrice = sellOrder.getPrice(); 
                int tradeVolume = Math.min(order.getVolume(), sellOrder.getVolume()); 

                executeTrade(order, sellOrder, tradePrice, tradeVolume); 

                if (order.getVolume() == 0) {
                    break;
                }
            }  
            if (order.volume > 0) { 
                orderMap.put(order.orderId, order); 
                buyOrders.add(order); 
            }
        } 

        if (order.side.equals("sell")) {
            while (buyOrders.size() > 0 && buyOrders.peek().price >= order.getPrice()) { 
                Order buyOrder = buyOrders.peek(); 
                double tradePrice = buyOrder.getPrice(); 
                int tradeVolume = Math.min(order.getVolume(), buyOrder.getVolume()); 

                executeTrade(order, buyOrder, tradePrice, tradeVolume); 
                if (order.getVolume() == 0) {
                    break;
                }
            }  
            if (order.volume > 0) { 
                orderMap.put(order.orderId, order); 
                sellOrders.add(order); 
            }
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
        Order order = orderMap.get(orderId); 
        if (order == null) return; 
        if (order.side.equals("buy")) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        orderMap.remove(orderId); 



    }

    // Method to get the market depth/demand
    public Map<Double, Integer> getMarketDepth(String side) {
        // TODO: Implement logic to get the range of buy/sell prices and total volume of pending orders at each price
        return new HashMap<>();
    }

    // Method to handle trade logic
    private void executeTrade(Order newOrder, Order existingOrder) {
        // TODO: Implement logic for executing a trade, including partial and multiple level trades
    }

    public static void main(String[] args) {
        // Example usage of TradeMatchingEngine
        TradeMatchingEngine engine = new TradeMatchingEngine();
        
        // Example order additions (for testing purposes)
        engine.addOrder(new Order("1", "buy", 100.5, 10, System.currentTimeMillis()));
        engine.addOrder(new Order("2", "sell", 100.0, 5, System.currentTimeMillis()));
    }

    public PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }
}