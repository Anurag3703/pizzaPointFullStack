package com.example.fullstack.database.dto.service.implementation;

import com.example.fullstack.database.dto.*;
import com.example.fullstack.database.dto.service.OrderDTOService;
import com.example.fullstack.database.model.Orders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderDTOServiceImpl implements OrderDTOService {
    @Override
    public OrderDTO convertToDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderSequence(order.getFormattedOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setDate(order.getDate());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setOrderType(order.getOrderType());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setServiceFee(order.getServiceFee());
        dto.setBottleDepositFee(order.getTotalBottleDepositFee());
        dto.setTotalCartAmount(order.getTotalCartAmount());

        // Initialize separate lists for OrderItems and CustomMeals
        List<OrderItemDTO> orderItemDTOList = new ArrayList<>();
        List<CustomMealDTO> customMealDTOList = new ArrayList<>();

        // Process OrderItems
        order.getOrderItems().forEach(item -> {
            if (item.getMenuItem() != null) {
                // MenuItem-based OrderItem
                OrderItemDTO dtoItem = new OrderItemDTO();
                dtoItem.setId(item.getId());
                dtoItem.setQuantity(item.getQuantity());
                dtoItem.setPricePerItem(item.getPricePerItem());
                dtoItem.setOrderId(item.getOrder().getOrderId());
                dtoItem.setItemType("MENU_ITEM");
                dtoItem.setOrderMenuItemName(item.getMenuItem().getName());
                dtoItem.setMenuItemId(item.getMenuItem().getId());
                dtoItem.setExtras(item.getExtras() != null ? item.getExtras().stream()
                        .map(extra -> {
                            ExtraDTO extraDto = new ExtraDTO();
                            extraDto.setId(extra.getId());
                            extraDto.setName(extra.getName());
                            extraDto.setPrice(extra.getPrice());
                            return extraDto;
                        }).toList() : List.of());
                dtoItem.setCustomMeal(null);
                orderItemDTOList.add(dtoItem);
            } else if (item.getCustomMeal() != null) {
                // CustomMeal-based OrderItem
                CustomMealDTO customMealDTO = convertCustomMealToDTO(item.getCustomMeal());
                customMealDTOList.add(customMealDTO);
            }
        });

        // Set the lists in the DTO
        dto.setOrderItems(orderItemDTOList);
        dto.setCustomMeals(customMealDTOList);

        // User DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(order.getUser().getId());
        userDTO.setName(order.getUser().getName());
        userDTO.setEmail(order.getUser().getEmail());
        userDTO.setPhone(order.getUser().getPhone());
        dto.setUser(userDTO);

        // Address DTO
        AddressDTO addressDTO = getAddressDTO(order);
        dto.setAddress(addressDTO);

        return dto;
    }

    private static AddressDTO getAddressDTO(Orders order) {
        if (order.getAddress() == null) {
            return null;
        }
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(order.getAddress().getAddressId());
        addressDTO.setFloor(order.getAddress().getFloor());
        addressDTO.setStreet(order.getAddress().getStreet());
        addressDTO.setBuildingName(order.getAddress().getBuildingName());
        addressDTO.setIntercom(order.getAddress().getIntercom());
        addressDTO.setOtherInstructions(order.getAddress().getOtherInstructions());
        addressDTO.setApartmentNo(order.getAddress().getApartmentNo());
        addressDTO.setSelected(order.getAddress().isSelected());
        addressDTO.setRecent(order.getAddress().isRecent());
        addressDTO.setUserId(order.getUser().getId());
        return addressDTO;
    }

    private CustomMealDTO convertCustomMealToDTO(com.example.fullstack.database.model.CustomMeal customMeal) {
        if (customMeal == null) {
            return null;
        }

        CustomMealDTO dto = new CustomMealDTO();
        dto.setId(customMeal.getId());
        dto.setTotalPrice(customMeal.getTotalPrice());

        // Convert MealTemplate
        if (customMeal.getTemplate() != null) {
            dto.setTemplate(convertMealTemplateToDTO(customMeal.getTemplate()));
        }

        // Convert SelectedMealItems
        if (customMeal.getSelectedItems() != null) {
            dto.setSelectedItems(customMeal.getSelectedItems().stream()
                    .map(this::convertSelectedMealItemToDTO)
                    .toList());
        }

        return dto;
    }

    private MealTemplateDTO convertMealTemplateToDTO(com.example.fullstack.database.model.MealTemplate template) {
        if (template == null) {
            return null;
        }

        MealTemplateDTO dto = new MealTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setPrice(template.getPrice());
        dto.setDescription(template.getDescription());
        dto.setImageUrl(template.getImageUrl());
        dto.setActive(template.isActive());
        dto.setCategory(template.getCategory());

        // Convert MenuItems
        if (template.getMenuItems() != null) {
            dto.setMenuItems(template.getMenuItems().stream()
                    .map(this::convertMenuItemToDTO)
                    .toList());
        }

        // Convert MealComponents
        if (template.getComponents() != null) {
            dto.setComponents(template.getComponents().stream()
                    .map(this::convertMealComponentToDTO)
                    .toList());
        }

        return dto;
    }

    private MenuItemDTO convertMenuItemToDTO(com.example.fullstack.database.model.MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setPrice(menuItem.getPrice());
        dto.setDescription(menuItem.getDescription());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setIsAvailable(menuItem.getIsAvailable());
        dto.setCategory(menuItem.getCategory());
        dto.setSize(menuItem.getSize());
        return dto;
    }

    private MealComponentDTO convertMealComponentToDTO(com.example.fullstack.database.model.MealComponent component) {
        if (component == null) {
            return null;
        }

        MealComponentDTO dto = new MealComponentDTO();
        dto.setId(component.getId());
        dto.setType(component.getType());
        dto.setMinSelection(component.getMinSelection());
        dto.setMaxSelection(component.getMaxSelection());

        if (component.getAvailableItems() != null) {
            dto.setAvailableItems(component.getAvailableItems().stream()
                    .map(this::convertMenuItemToDTO)
                    .toList());
        }

        return dto;
    }

    private SelectedMealItemDTO convertSelectedMealItemToDTO(com.example.fullstack.database.model.SelectedMealItem selectedItem) {
        if (selectedItem == null) {
            return null;
        }

        SelectedMealItemDTO dto = new SelectedMealItemDTO();
        dto.setMenuItemId(selectedItem.getMenuItem() != null ? selectedItem.getMenuItem().getId() : null);
        dto.setQuantity(selectedItem.getQuantity());
        dto.setSpecialInstructions(selectedItem.getSpecialInstructions());

        return dto;
    }
}