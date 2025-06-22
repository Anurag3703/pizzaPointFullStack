package com.example.fullstack.database.dto.service.implementation;

import com.example.fullstack.database.dto.*;
import com.example.fullstack.database.dto.service.CartDTOService;
import com.example.fullstack.database.model.Cart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartDTOServiceImpl implements CartDTOService {
    @Override
    public CartDTO convertToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setTotalPrice(cart.getTotalPrice());
        cartDTO.setSessionId(cart.getSessionId());

        if (cart.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(cart.getUser().getId());
            userDTO.setName(cart.getUser().getName());
            userDTO.setAddress(cart.getUser().getAddress());
            userDTO.setPhone(cart.getUser().getPhone());
            userDTO.setEmail(cart.getUser().getEmail());
            cartDTO.setUser(userDTO);
        }

        // Map cart items with full details
        List<CartItemDTO> itemsDTO = cart.getCartItems().stream().map(item -> {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setCartItemId(item.getCartItemId());
            itemDTO.setCartId(cart.getCartId());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setTotalPrice(item.getTotalPrice());
            itemDTO.setInstruction(item.getInstruction());

            // Include full MenuItem details
            MenuItemDTO menuItemDTO = new MenuItemDTO();
            menuItemDTO.setId(item.getMenuItem().getId());
            menuItemDTO.setName(item.getMenuItem().getName());
            menuItemDTO.setDescription(item.getMenuItem().getDescription());
            menuItemDTO.setPrice(item.getMenuItem().getPrice());
            menuItemDTO.setCategory(item.getMenuItem().getCategory());
            menuItemDTO.setSize(item.getMenuItem().getSize());
            menuItemDTO.setIsAvailable(item.getMenuItem().getIsAvailable());
            menuItemDTO.setImageUrl(item.getMenuItem().getImageUrl());
            itemDTO.setMenuItem(menuItemDTO);

            // Include full Extra details
            List<ExtraDTO> extraDTOs = item.getExtras().stream()
                    .map(extra -> {
                        ExtraDTO extraDTO = new ExtraDTO();
                        extraDTO.setId(extra.getId());
                        extraDTO.setName(extra.getName());
                        extraDTO.setPrice(extra.getPrice());
                        return extraDTO;
                    })
                    .collect(Collectors.toList());
            itemDTO.setExtras(extraDTOs);

            return itemDTO;
        }).collect(Collectors.toList());

        cartDTO.setCartItems(itemsDTO);

        // Map custom meals
        List<CustomMealDTO> customMealDTOs = cart.getCustomMeals().stream().map(customMeal -> {
            CustomMealDTO customMealDTO = new CustomMealDTO();
            customMealDTO.setId(customMeal.getId());
            customMealDTO.setTotalPrice(customMeal.getTotalPrice());

            // Map meal template
            if (customMeal.getTemplate() != null) {
                MealTemplateDTO templateDTO = new MealTemplateDTO();
                templateDTO.setId(customMeal.getTemplate().getId());
                templateDTO.setName(customMeal.getTemplate().getName());
                templateDTO.setPrice(customMeal.getTemplate().getPrice());
                templateDTO.setDescription(customMeal.getTemplate().getDescription());
                templateDTO.setImageUrl(customMeal.getTemplate().getImageUrl());
                templateDTO.setActive(customMeal.getTemplate().isActive());
                templateDTO.setCategory(customMeal.getTemplate().getCategory());

                // Map template menu items
                if (customMeal.getTemplate().getMenuItems() != null) {
                    List<MenuItemDTO> menuItemDTOs = customMeal.getTemplate().getMenuItems().stream()
                            .map(menuItem -> {
                                MenuItemDTO menuItemDTO = new MenuItemDTO();
                                menuItemDTO.setId(menuItem.getId());
                                menuItemDTO.setName(menuItem.getName());
                                menuItemDTO.setDescription(menuItem.getDescription());
                                menuItemDTO.setPrice(menuItem.getPrice());
                                menuItemDTO.setCategory(menuItem.getCategory());
                                menuItemDTO.setSize(menuItem.getSize());
                                menuItemDTO.setIsAvailable(menuItem.getIsAvailable());
                                menuItemDTO.setImageUrl(menuItem.getImageUrl());
                                return menuItemDTO;
                            })
                            .collect(Collectors.toList());
                    templateDTO.setMenuItems(menuItemDTOs);
                }

                // Map meal components
                if (customMeal.getTemplate().getComponents() != null) {
                    List<MealComponentDTO> componentDTOs = customMeal.getTemplate().getComponents().stream()
                            .map(component -> {
                                MealComponentDTO componentDTO = new MealComponentDTO();
                                componentDTO.setId(component.getId());
                                componentDTO.setType(component.getType());
                                componentDTO.setMinSelection(component.getMinSelection());
                                componentDTO.setMaxSelection(component.getMaxSelection());

                                // Map available items for each component
                                if (component.getAvailableItems() != null) {
                                    List<MenuItemDTO> availableItemDTOs = component.getAvailableItems().stream()
                                            .map(menuItem -> {
                                                MenuItemDTO menuItemDTO = new MenuItemDTO();
                                                menuItemDTO.setId(menuItem.getId());
                                                menuItemDTO.setName(menuItem.getName());
                                                menuItemDTO.setDescription(menuItem.getDescription());
                                                menuItemDTO.setPrice(menuItem.getPrice());
                                                menuItemDTO.setCategory(menuItem.getCategory());
                                                menuItemDTO.setSize(menuItem.getSize());
                                                menuItemDTO.setIsAvailable(menuItem.getIsAvailable());
                                                menuItemDTO.setImageUrl(menuItem.getImageUrl());
                                                return menuItemDTO;
                                            })
                                            .collect(Collectors.toList());
                                    componentDTO.setAvailableItems(availableItemDTOs);
                                }

                                return componentDTO;
                            })
                            .collect(Collectors.toList());
                    templateDTO.setComponents(componentDTOs);
                }

                customMealDTO.setTemplate(templateDTO);
            }

            // Map selected meal items
            if (customMeal.getSelectedItems() != null) {
                List<SelectedMealItemDTO> selectedItemDTOs = customMeal.getSelectedItems().stream()
                        .map(selectedItem -> {
                            SelectedMealItemDTO selectedItemDTO = new SelectedMealItemDTO();
                            selectedItemDTO.setQuantity(selectedItem.getQuantity());
                            selectedItemDTO.setMenuItemId(selectedItem.getId());
                            selectedItemDTO.setSpecialInstructions(selectedItem.getSpecialInstructions());

                            // Map the menu item
                            if (selectedItem.getMenuItem() != null) {
                                MenuItemDTO menuItemDTO = new MenuItemDTO();
                                menuItemDTO.setId(selectedItem.getMenuItem().getId());
                                menuItemDTO.setName(selectedItem.getMenuItem().getName());
                                menuItemDTO.setDescription(selectedItem.getMenuItem().getDescription());
                                menuItemDTO.setPrice(selectedItem.getMenuItem().getPrice());
                                menuItemDTO.setCategory(selectedItem.getMenuItem().getCategory());
                                menuItemDTO.setSize(selectedItem.getMenuItem().getSize());
                                menuItemDTO.setIsAvailable(selectedItem.getMenuItem().getIsAvailable());
                                menuItemDTO.setImageUrl(selectedItem.getMenuItem().getImageUrl());
                                selectedItemDTO.setMenuItemId(menuItemDTO.getId());
                            }

                            return selectedItemDTO;
                        })
                        .collect(Collectors.toList());
                customMealDTO.setSelectedItems(selectedItemDTOs);
            }

            return customMealDTO;
        }).collect(Collectors.toList());

        cartDTO.setCustomMeals(customMealDTOs);

        return cartDTO;
    }
}