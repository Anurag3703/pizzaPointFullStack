package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.dto.OrderDTO;
import com.example.fullstack.database.dto.OrderItemDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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

        StringBuilder orderItemsHtml = new StringBuilder();
        List<OrderItemDTO> orderItems = order.getOrderItems();
        if (orderItems != null) {
            for (OrderItemDTO item : orderItems) {
                // Get the actual menu item name instead of ID
                String itemName = item.getOrderMenuItemName() != null ? item.getOrderMenuItemName() : "Unknown Item";
                String itemText = String.format("%dx %s - Ft %.2f", item.getQuantity(), itemName, item.getPricePerItem());
                orderItemsHtml.append("<li>").append(itemText).append("</li>");
            }
        }
        String orderItemsSection = orderItemsHtml.toString();

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String deliveryDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Delivery Address]", order.getAddress() != null ?
                        (order.getAddress().getStreet() + ", " + order.getAddress().getBuildingName()) : "N/A")
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Delivery Fee]", df.format(order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO))
                .replace("[Service Fee]", df.format(order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO))
                .replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee() != null ? order.getBottleDepositFee() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO))
                .replace("[Delivery DateTime]", deliveryDateTime)
                .replace("[Support Link]", "https://pizzapoint.com/support")
                .replace("[Company Address]", "123 Pizza Lane, Food City")
                .replace("[Company Email]", "support@pizzapoint.com")
                .replace("[Company Phone]", "+1-800-PIZZA-99");

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

        StringBuilder orderItemsHtml = new StringBuilder();
        List<OrderItemDTO> orderItems = order.getOrderItems();
        if (orderItems != null) {
            for (OrderItemDTO item : orderItems) {
                String itemName = item.getOrderMenuItemName() != null ? item.getOrderMenuItemName() : "Unknown Item";
                String itemText = String.format("%dx %s - Ft %.2f", item.getQuantity(), itemName, item.getPricePerItem());
                orderItemsHtml.append("<li>").append(itemText).append("</li>");
            }
        }
        String orderItemsSection = orderItemsHtml.toString();

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String cancellationDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Cancellation DateTime]", cancellationDateTime)
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Delivery Fee]", df.format(order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO))
                .replace("[Service Fee]", df.format(order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO))
                .replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee() != null ? order.getBottleDepositFee() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO));

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

        StringBuilder orderItemsHtml = new StringBuilder();
        List<OrderItemDTO> orderItems = order.getOrderItems();
        if (orderItems != null) {
            for (OrderItemDTO item : orderItems) {
                // Get the actual menu item name instead of ID
                String itemName = item.getOrderMenuItemName() != null ? item.getOrderMenuItemName() : "Unknown Item";
                String itemText = String.format("%dx %s - Ft %.2f", item.getQuantity(), itemName, item.getPricePerItem());
                orderItemsHtml.append("<li>").append(itemText).append("</li>");
            }
        }
        String orderItemsSection = orderItemsHtml.toString();

        DecimalFormat df = new DecimalFormat("#.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy, hh:mm a");

        String completionDateTime = order.getUpdatedAt() != null ? order.getUpdatedAt().format(dtf) : "N/A";

        String finalContent = htmlContent.toString()
                .replace("[Customer Name]", order.getUser() != null ? order.getUser().getName() : "Customer")
                .replace("[Order ID]", order.getOrderId() != null ? order.getOrderId() : "N/A")
                .replace("[Completion DateTime]", completionDateTime)
                .replace("[Order Items]", orderItemsSection)
                .replace("[Total Cart Amount]", df.format(order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO))
                .replace("[Service Fee]", df.format(order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO))
                .replace("[Bottle Deposit Fee]", df.format(order.getBottleDepositFee() != null ? order.getBottleDepositFee() : BigDecimal.ZERO))
                .replace("[Total Price]", df.format(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO));

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("PIZZA POINT - Your Order is Ready for Pickup!");
        helper.setText(finalContent, true);

        mailSender.send(message);
    }

}