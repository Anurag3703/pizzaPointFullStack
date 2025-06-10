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
            userDTO.setId(cart.getUser().getId()); // or getId() depending on your User entity
            userDTO.setName(cart.getUser().getName()); // or getFirstName() + getLastName() if separate
            userDTO.setAddress(cart.getUser().getAddress());
            userDTO.setPhone(cart.getUser().getPhone()); // or getPhoneNumber() depending on your User entity
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
        return cartDTO;
    }

}


