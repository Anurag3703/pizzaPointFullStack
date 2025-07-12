package com.example.fullstack.database.service;

import com.example.fullstack.database.model.*;

import java.util.List;

public interface OrdersService {
    void addOrder(Orders order);
    void addAllOrders(List<Orders> orders);
    void updateOrder(Orders order);
    void updateOrderStatus(String orderId, Status status);
    Orders  processCheckoutWithDelivery();
    List<Orders> getAllOrders();
    List<Orders> getOrdersByUser(String userId);
    Orders processCheckoutWithPickup();
    Orders retryPayment(String orderId, String cardToken);
    Orders  getOrderById(String orderId);
    Orders confirmCheckout(String orderId, PaymentMethod paymentMethod, OrderType orderType, String cardToken);
    void deletePendingOrders();
    List<Orders> getAlLPlacedOrders();
    List<Orders> getAllDeliveredOrders();
    List<Orders> getAllCancelledOrders();
    List<Orders> getAllDeliveredAndCompletedOrders();
    Orders confirmCheckoutWithDiscount(String orderId, PaymentMethod paymentMethod, OrderType orderType, String cardToken,String discountCode);
    void validateDiscountCode(String discountCode);



}
