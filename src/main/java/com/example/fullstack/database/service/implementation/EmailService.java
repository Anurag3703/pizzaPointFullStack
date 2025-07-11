package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.AddressDTO;
import com.example.fullstack.database.dto.ExtraDTO;
import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.dto.OrderItemDTO;
import com.example.fullstack.database.model.OrderType;
import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.repository.SecurityUserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service

public class EmailService {
    private final JavaMailSender mailSender;
    private final SecurityUserRepository securityUserRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.emails}")
    private String adminEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender,SecurityUserRepository securityUserRepository) {
        this.mailSender = mailSender;
        this.securityUserRepository = securityUserRepository;
    }

    private String buildOrderItemsHtml(List<OrderItemDTO> orderItems) {
        StringBuilder orderItemsHtml = new StringBuilder();
        if (orderItems != null) {
            for (OrderItemDTO item : orderItems) {
                String itemName = item.getOrderMenuItemName() != null ? item.getOrderMenuItemName() : "Unknown Item";
                StringBuilder itemText = new StringBuilder();
                itemText.append(String.format("%dx %s - Ft %.2f", item.getQuantity(), itemName, item.getPricePerItem()));

                // Add extras if present
                if (item.getExtras() != null && !item.getExtras().isEmpty()) {
                    itemText.append(" (");
                    List<ExtraDTO> extras = item.getExtras();
                    for (int i = 0; i < extras.size(); i++) {
                        ExtraDTO extra = extras.get(i);
                        itemText.append(extra.getName());
                        if (extra.getPrice() != null && extra.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                            itemText.append(" +Ft ").append(String.format("%.2f", extra.getPrice()));
                        }
                        if (i < extras.size() - 1) {
                            itemText.append(", ");
                        }
                    }
                    itemText.append(")");
                }

                orderItemsHtml.append("<li>").append(itemText.toString()).append("</li>");
            }
        }
        return orderItemsHtml.toString();
    }

    private String buildOrderItemsHtmlAdmin(List<OrderItemDTO> orderItems) {
        StringBuilder orderItemsHtml = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.00");
        int totalItems = 0;

        if (orderItems != null) {
            totalItems = (int) orderItems.stream()
                    .filter(item -> item.getQuantity() != null)
                    .mapToLong(OrderItemDTO::getQuantity)
                    .sum();

            for (OrderItemDTO item : orderItems) {
                String itemName = item.getOrderMenuItemName() != null ? item.getOrderMenuItemName() : "Unknown Item";
                BigDecimal itemTotal = item.getPricePerItem() != null ?
                        item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0)) : BigDecimal.ZERO;

                StringBuilder itemText = new StringBuilder();
                itemText.append(String.format("%dx %s - Ft %.2f", item.getQuantity() != null ? item.getQuantity() : 0, itemName, itemTotal));

                // Add extras if present
                if (item.getExtras() != null && !item.getExtras().isEmpty()) {
                    itemText.append(" (");
                    List<ExtraDTO> extras = item.getExtras();
                    for (int i = 0; i < extras.size(); i++) {
                        ExtraDTO extra = extras.get(i);
                        itemText.append(extra.getName());
                        if (extra.getPrice() != null && extra.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                            itemText.append(" +Ft ").append(String.format("%.2f", extra.getPrice()));
                        }
                        if (i < extras.size() - 1) {
                            itemText.append(", ");
                        }
                    }
                    itemText.append(")");
                }

                orderItemsHtml.append("<li><span class=\"item-details\">").append(itemText.toString())
                        .append("</span><span class=\"item-price\">Ft ").append(df.format(itemTotal)).append("</span></li>");
            }
        }
        return orderItemsHtml.toString();
    }

    public void sendDeliveryEmail(String toEmail, OrderDTO order) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        ClassPathResource resource = new ClassPathResource("templates/email/delivery-email-template.html");
        StringBuilder htmlContent = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
        }

        String orderItemsSection = buildOrderItemsHtml(order.getOrderItems());

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String deliveryDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null && order.getUser().getName() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Delivery Address]", order.getAddress() != null ?
                        (order.getAddress().getStreet() != null ? order.getAddress().getStreet() : "") +
                                (order.getAddress().getBuildingName() != null ? ", " + order.getAddress().getBuildingName() : "") : "N/A")
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO))
                .replace("[Delivery DateTime]", deliveryDateTime)
                .replace("[Support Link]", "https://pizzapoint.com/support")
                .replace("[Company Address]", "123 Pizza Lane, Food City")
                .replace("[Company Email]", "support@pizzapoint.com")
                .replace("[Company Phone]", "+1-800-PIZZA-99");

        // Conditionally add fees only if they're greater than zero
        finalContent = conditionallyAddFees(finalContent, order);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("PIZZA POINT - Thank you for your order");
        helper.setText(finalContent, true);

        mailSender.send(message);
    }

    public void sendCancellationEmail(String toEmail, OrderDTO order) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        ClassPathResource resource = new ClassPathResource("templates/email/cancellation-email-template.html");
        StringBuilder htmlContent = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
        }

        String orderItemsSection = buildOrderItemsHtml(order.getOrderItems());

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String cancellationDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null && order.getUser().getName() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Cancellation DateTime]", cancellationDateTime)
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO));

        // Conditionally add fees only if they're greater than zero
        finalContent = conditionallyAddFees(finalContent, order);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("PIZZA POINT - Order Cancellation Notice");
        helper.setText(finalContent, true);

        mailSender.send(message);
    }

    public void sendPickupCompletionEmail(String toEmail, OrderDTO order) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        ClassPathResource resource = new ClassPathResource("templates/email/pickup-completion-email-template.html");
        StringBuilder htmlContent = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
        }

        String orderItemsSection = buildOrderItemsHtml(order.getOrderItems());

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String completionDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null && order.getUser().getName() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Completion DateTime]", completionDateTime)
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO));

        // Conditionally add fees only if they're greater than zero (excluding delivery fee for pickup)
        finalContent = conditionallyAddFeesPickup(finalContent, order);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("PIZZA POINT - Your Order is Ready for Pickup!");
        helper.setText(finalContent, true);

        mailSender.send(message);
    }

    public void sendNewOrderAdminEmail(OrderDTO order) throws Exception {
        List<String> adminEmails = getAllAdminEmails();

        if (adminEmails.isEmpty()) {
            return; // No admins to notify
        }

        ClassPathResource resource = new ClassPathResource("templates/email/admin-new-order-template.html");
        StringBuilder htmlContent = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String orderItemsSection = buildOrderItemsHtmlAdmin(order.getOrderItems());

        int totalItems = 0;
        if (order.getOrderItems() != null) {
            totalItems = (int) order.getOrderItems().stream()
                    .filter(item -> item.getQuantity() != null)
                    .mapToLong(OrderItemDTO::getQuantity)
                    .sum();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String orderDateTime = order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "N/A";
        StringBuilder deliveryAddress = new StringBuilder();
        String specialInstructions = "None";
        String intercom = "N/A";
        String floor = "N/A";
        String apartmentNo = "N/A";
        String googleMapsLink = "N/A";

        if (order.getOrderType() == OrderType.DELIVERY && order.getAddress() != null) {
            AddressDTO address = order.getAddress();
            if (address.getStreet() != null && !address.getStreet().trim().isEmpty()) {
                deliveryAddress.append(address.getStreet());
            }
            if (address.getBuildingName() != null && !address.getBuildingName().trim().isEmpty()) {
                if (!deliveryAddress.isEmpty()) deliveryAddress.append(", ");
                deliveryAddress.append(address.getBuildingName());
            }
            if (address.getOtherInstructions() != null && !address.getOtherInstructions().trim().isEmpty()) {
                specialInstructions = address.getOtherInstructions();
            }
            if (address.getIntercom() != null && !address.getIntercom().trim().isEmpty()) {
                intercom = address.getIntercom();
            }
            if (address.getFloor() != null && !address.getFloor().trim().isEmpty()) {
                floor = address.getFloor();
            }
            if (address.getApartmentNo() != null && !address.getApartmentNo().trim().isEmpty()) {
                apartmentNo = address.getApartmentNo();
            }
            googleMapsLink = generateGoogleMapsLink(address);
        }
        if (deliveryAddress.isEmpty()) {
            deliveryAddress.append("N/A");
        }

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null && order.getUser().getName() != null ? order.getUser().getName() : "Customer")
                .replace("[Customer Email]", order.getUser() != null && order.getUser().getEmail() != null ? order.getUser().getEmail() : "N/A")
                .replace("[Customer Phone]", order.getUser() != null && order.getUser().getPhone() != null ? order.getUser().getPhone() : "N/A")
                .replace("[Order Type]", order.getOrderType() != null ? order.getOrderType().toString() : "N/A")
                .replace("[Payment Method]", order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "N/A")
                .replace("[Delivery Address]", deliveryAddress.toString())
                .replace("[Special Instructions]", specialInstructions)
                .replace("[Intercom]", intercom)
                .replace("[Floor]", floor)
                .replace("[Apartment No]", apartmentNo)
                .replace("[Google Maps Link]", googleMapsLink != null ? googleMapsLink : "N/A")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Order Sequence]", order.getOrderSequence() != null ? order.getOrderSequence() : "N/A")
                .replace("[Order DateTime]", orderDateTime)
                .replace("[Total Items]", String.valueOf(totalItems))
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO))
                .replace("[Admin Dashboard URL]", "https://www.pizzapoint.hu/admin")
                .replace("[Customer Contact URL]", order.getUser() != null && order.getUser().getEmail() != null ? "mailto:" + order.getUser().getEmail() : "#")
                .replace("[Support URL]", "https://pizzapoint.com/support")
                .replace("[Settings URL]", "https://pizzapoint.com/admin/settings")
                .replace("[Estimated Prep Time]", "30");

        // Conditionally add fees and discount only if they're greater than zero
        finalContent = conditionallyAddFeesAdmin(finalContent, order);

        for (String adminEmail : adminEmails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(adminEmail);
                helper.setSubject("PIZZA POINT - Urgent: New Order #" + (order.getOrderSequence() != null ? order.getOrderSequence() : "N/A"));
                helper.setText(finalContent, true);

                mailSender.send(message);
            } catch (Exception e) {
                // Log error but continue with other admins
                System.err.println("Failed to send email to admin: " + adminEmail);
                e.printStackTrace();
            }
        }
    }

    private String conditionallyAddFees(String content, OrderDTO order) {
        DecimalFormat df = new DecimalFormat("#.00");

        // Add delivery fee if greater than zero
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Delivery Fee]", df.format(order.getDeliveryFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Delivery Fee\\].*(\r?\n)?", "");
        }

        // Add service fee if greater than zero
        if (order.getServiceFee() != null && order.getServiceFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Service Fee]", df.format(order.getServiceFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Service Fee\\].*(\r?\n)?", "");
        }

        // Add packaging fee if greater than zero
        if (order.getPackagingFee() != null && order.getPackagingFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Packaging Fee]", df.format(order.getPackagingFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Packaging Fee\\].*(\r?\n)?", "");
        }

        // Add bottle deposit fee if greater than zero
        if (order.getBottleDepositFee() != null && order.getBottleDepositFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Bottle Deposit Fee\\].*(\r?\n)?", "");
        }

        return content;
    }

    private String conditionallyAddFeesPickup(String content, OrderDTO order) {
        DecimalFormat df = new DecimalFormat("#.00");

        // Add service fee if greater than zero
        if (order.getServiceFee() != null && order.getServiceFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Service Fee]", df.format(order.getServiceFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Service Fee\\].*(\r?\n)?", "");
        }

        // Add packaging fee if greater than zero
        if (order.getPackagingFee() != null && order.getPackagingFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Packaging Fee]", df.format(order.getPackagingFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Packaging Fee\\].*(\r?\n)?", "");
        }

        // Add bottle deposit fee if greater than zero
        if (order.getBottleDepositFee() != null && order.getBottleDepositFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Bottle Deposit Fee\\].*(\r?\n)?", "");
        }

        return content;
    }

    private String conditionallyAddFeesAdmin(String content, OrderDTO order) {
        DecimalFormat df = new DecimalFormat("#.00");

        // Add delivery fee if greater than zero
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Delivery Fee]", df.format(order.getDeliveryFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Delivery Fee\\].*(\r?\n)?", "");
        }

        // Add service fee if greater than zero
        if (order.getServiceFee() != null && order.getServiceFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Service Fee]", df.format(order.getServiceFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Service Fee\\].*(\r?\n)?", "");
        }

        // Add packaging fee if greater than zero
        if (order.getPackagingFee() != null && order.getPackagingFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Packaging Fee]", df.format(order.getPackagingFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Packaging Fee\\].*(\r?\n)?", "");
        }

        // Add bottle deposit fee if greater than zero
        if (order.getBottleDepositFee() != null && order.getBottleDepositFee().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee()));
        } else {
            content = content.replaceAll("(?i).*\\[Bottle Deposit Fee\\].*(\r?\n)?", "");
        }

        // Add discount amount if greater than zero
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            content = content.replace("[Discount Amount]", df.format(order.getDiscountAmount()));
        } else {
            content = content.replaceAll("(?i).*\\[Discount Amount\\].*(\r?\n)?", "");
        }

        return content;
    }

    private String generateGoogleMapsLink(AddressDTO address) {
        if (address == null) return null;

        StringBuilder addressBuilder = new StringBuilder();
        if (address.getStreet() != null && !address.getStreet().trim().isEmpty()) {
            addressBuilder.append(address.getStreet().trim());
        }
        if (address.getBuildingName() != null && !address.getBuildingName().trim().isEmpty()) {
            if (!addressBuilder.isEmpty()) addressBuilder.append(", ");
            addressBuilder.append(address.getBuildingName().trim());
        }

        if (addressBuilder.isEmpty()) {
            return null;
        }

        try {
            String encodedAddress = java.net.URLEncoder.encode(addressBuilder.toString(), StandardCharsets.UTF_8);
            return "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getAllAdminEmails(){
        return securityUserRepository.findEmailsByRole("ADMIN");
    }
}