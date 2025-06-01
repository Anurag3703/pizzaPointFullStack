package com.example.fullstack.database.service.implementation;


import com.example.fullstack.database.model.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber; // Changed import
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;



@Service
public class WhatsAppService {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @Value("${restaurant.owner.whatsapp}")
    private String ownerWhatsApp;

    @Value("${whatsapp.notifications.enabled:false}")
    private boolean notificationsEnabled;

    @PostConstruct
    public void init() {
        if (notificationsEnabled) {
            try {
                Twilio.init(accountSid, authToken);
                logger.info("WhatsApp service initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize WhatsApp service: {}", e.getMessage());
            }
        }
    }

    public void sendNewOrderNotification(Orders order) {
        if (!notificationsEnabled) {
            logger.info("WhatsApp notifications are disabled");
            return;
        }

        try {
            String messageBody = formatNewOrderMessage(order);

            Message message = Message.creator(
                    new PhoneNumber(ownerWhatsApp),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            logger.info("WhatsApp order notification sent successfully. SID: {}", message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp order notification: {}", e.getMessage());
        }
    }

    public void sendOrderStatusUpdate(Orders order, Status newStatus) {
        if (!notificationsEnabled) return;

        try {
            String messageBody = formatStatusUpdateMessage(order, newStatus);

            Message message = Message.creator(
                    new PhoneNumber(ownerWhatsApp),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            logger.info("WhatsApp status update sent successfully. SID: {}", message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp status update: {}", e.getMessage());
        }
    }

    private String formatNewOrderMessage(Orders order) {
        StringBuilder message = new StringBuilder();

        message.append("🍽️ *NEW ORDER RECEIVED!* 🍽️\n\n");
        message.append("📋 *Order ID:* ").append(order.getOrderSequence()).append("\n");
        message.append("👤 *Customer:* ").append(order.getUser().getName()).append(("\n"));
        message.append("📞 *Phone:* ").append(order.getUser().getPhone()).append("\n");
        message.append("📧 *Email:* ").append(order.getUser().getEmail()).append("\n");

        // Address information
        if (order.getAddress() != null) {
            Address address = order.getAddress();
            message.append("📍 *DELIVERY ADDRESS:*\n");
            message.append("─────────────────\n");

            // Street address
            if (address.getStreet() != null && !address.getStreet().trim().isEmpty()) {
                message.append("🏠 *Street:* ").append(address.getStreet()).append("\n");
            }

            // Building name
            if (address.getBuildingName() != null && !address.getBuildingName().trim().isEmpty()) {
                message.append("🏢 *Building:* ").append(address.getBuildingName()).append("\n");
            }

            // Floor information
            if (address.getFloor() != null && !address.getFloor().trim().isEmpty()) {
                message.append("🏗️ *Floor:* ").append(address.getFloor()).append("\n");
            }

            // Apartment number
            if (address.getApartmentNo() != null && !address.getApartmentNo().trim().isEmpty()) {
                message.append("🚪 *Apartment:* ").append(address.getApartmentNo()).append("\n");
            }

            // Intercom code
            if (address.getIntercom() != null && !address.getIntercom().trim().isEmpty()) {
                message.append("🔔 *Intercom Code:* ").append(address.getIntercom()).append("\n");
            }

            // Special delivery instructions
            if (address.getOtherInstructions() != null && !address.getOtherInstructions().trim().isEmpty()) {
                message.append("📝 *Delivery Instructions:*\n");
                message.append("   ").append(address.getOtherInstructions()).append("\n");
            }

            // Google Maps navigation link
            String mapsLink = generateGoogleMapsLink(address);
            if (mapsLink != null) {
                message.append("🗺️ *Navigation:* ").append(mapsLink).append("\n");
                message.append("   👆 *Tap to open in Google Maps*\n");
            }

            message.append("\n");
        }

        message.append("🚚 *Order Type:* ").append(getOrderTypeEmoji(order.getOrderType()))
                .append(" ").append(order.getOrderType().toString()).append("\n");
        message.append("💳 *Payment:* ").append(getPaymentMethodEmoji(order.getPaymentMethod()))
                .append(" ").append(order.getPaymentMethod().toString()).append("\n\n");

        // Order items
        message.append("🛒 *ITEMS ORDERED:*\n");
        message.append("─────────────────\n");

        for (OrderItem item : order.getOrderItems()) {
            message.append("• *").append(item.getMenuItem().getName()).append("*\n");
            message.append("  Qty: ").append(item.getQuantity());
            message.append(" × ft").append(item.getPricePerItem());
            message.append(" = ft").append(item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity()))).append("\n");

            // Add extras if any
            if (item.getExtras() != null && !item.getExtras().isEmpty()) {
                message.append("  *Extras:* ");
                for (int i = 0; i < item.getExtras().size(); i++) {
                    Extra extra = item.getExtras().get(i);
                    message.append(extra.getName());
                    if (i < item.getExtras().size() - 1) {
                        message.append(", ");
                    }
                }
                message.append("\n");
            }
            message.append("\n");
        }

        // Pricing breakdown
        message.append("💰 *PRICE BREAKDOWN:*\n");
        message.append("─────────────────\n");
        message.append("Food Total: ft").append(order.getTotalCartAmount()).append("\n");

        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            message.append("Delivery Fee: ft").append(order.getDeliveryFee()).append("\n");
        }

        if (order.getServiceFee() != null && order.getServiceFee().compareTo(BigDecimal.ZERO) > 0) {
            message.append("Service Fee: ft").append(order.getServiceFee()).append("\n");
        }

        if (order.getBottleDepositFee() != null && order.getBottleDepositFee().compareTo(BigDecimal.ZERO) > 0) {
            message.append("Bottle Deposit: ft").append(order.getBottleDepositFee()).append("\n");
        }

        message.append("*TOTAL: ft").append(order.getTotalPrice()).append("*\n\n");

        // Order time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        message.append("⏰ *Order Time:* ").append(order.getCreatedAt().format(formatter)).append("\n");
        message.append("🆔 *Order Status:* ").append(order.getStatus()).append("\n\n");

        message.append("⚡ *ACTION REQUIRED: Please confirm this order!*");

        return message.toString();
    }

    private String formatStatusUpdateMessage(Orders order, Status newStatus) {
        StringBuilder message = new StringBuilder();

        message.append("📋 *ORDER STATUS UPDATE*\n\n");
        message.append("🆔 *Order:* ").append(order.getOrderSequence()).append("\n");
        message.append("👤 *Customer:* ").append(order.getUser().getName()).append("\n");

        message.append("📱 *Status:* ").append(getStatusEmoji(newStatus))
                .append(" *").append(newStatus.toString()).append("*\n");
        message.append("💰 *Total:* $").append(order.getTotalPrice()).append("\n\n");

        message.append(getStatusDescription(newStatus));

        return message.toString();
    }

    private String getOrderTypeEmoji(OrderType orderType) {
        if (orderType == null) return "📦";
        return switch (orderType) {
            case DELIVERY -> "🚚";
            case PICKUP -> "🏪";
            case DINE_IN -> "🍽️";
            default -> "📦";
        };
    }

    private String getPaymentMethodEmoji(PaymentMethod paymentMethod) {
        if (paymentMethod == null) return "💳";
        return switch (paymentMethod) {
            case CASH -> "💵";

            default -> "💳";
        };
    }

    private String getStatusEmoji(Status status) {
        return switch (status) {
            case PENDING -> "⏳";
            case PLACED -> "✅";
            case PREPARING -> "👨‍🍳";
            case READY_FOR_PICKUP -> "📦";
            case OUT_FOR_DELIVERY -> "🚚";
            case DELIVERED -> "✅";
            case CANCELLED -> "❌";
            default -> "📋";
        };
    }

    private String getStatusDescription(Status status) {
        return switch (status) {
            case PENDING -> "Order is pending confirmation.";
            case PLACED -> "Order has been placed and confirmed!";
            case PREPARING -> "Kitchen is preparing the order.";
            case READY_FOR_PICKUP -> "Order is ready for pickup.";
            case OUT_FOR_DELIVERY -> "Order is on the way to customer.";
            case DELIVERED -> "Order has been delivered successfully!";
            case CANCELLED -> "Order has been cancelled.";
            default -> "Order status updated.";
        };
    }

    private String generateGoogleMapsLink(Address address) {
        if (address == null) return null;

        // Build the address string from available components
        StringBuilder addressBuilder = new StringBuilder();

        if (address.getStreet() != null && !address.getStreet().trim().isEmpty()) {
            addressBuilder.append(address.getStreet().trim());
        }

        if (address.getBuildingName() != null && !address.getBuildingName().trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(address.getBuildingName().trim());
        }

        // Only create a link if we have some address information
        if (addressBuilder.length() == 0) {
            return null;
        }

        try {
            // URL encode the address for Google Maps
            String encodedAddress = java.net.URLEncoder.encode(addressBuilder.toString(), "UTF-8");
            return "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;
        } catch (Exception e) {
            logger.warn("Failed to generate Google Maps link for address: {}", e.getMessage());
            return null;
        }
    }
}