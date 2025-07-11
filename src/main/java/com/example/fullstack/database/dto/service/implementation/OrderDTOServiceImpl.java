package com.example.fullstack.database.dto.service.implementation;

import com.example.fullstack.database.dto.*;
import com.example.fullstack.database.dto.service.OrderDTOService;
import com.example.fullstack.database.model.Orders;
import com.example.fullstack.database.service.implementation.OrdersServiceImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        dto.setPackagingFee(order.getPackageFee());

        List<OrderItemDTO> orderItemDTOList = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO dtoItem = new OrderItemDTO();
                    dtoItem.setId(item.getId());
                    dtoItem.setOrderMenuItemName(item.getMenuItem().getName());
                    dtoItem.setQuantity(item.getQuantity());
                    dtoItem.setPricePerItem(item.getPricePerItem());
                    dtoItem.setOrderId(item.getOrder().getOrderId());
                    dtoItem.setMenuItemId(item.getMenuItem().getId());
                    dtoItem.setExtras(item.getExtras()!=null ? item.getExtras().stream()
                            .map(extra ->{
                                ExtraDTO extraDto = new ExtraDTO();
                                extraDto.setId(extra.getId());
                                extraDto.setName(extra.getName());
                                extraDto.setPrice(extra.getPrice());
                                return extraDto;
                            }).toList():List.of());
                    return dtoItem;
                }).toList();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(order.getUser().getId());
        userDTO.setName(order.getUser().getName());
        userDTO.setEmail(order.getUser().getEmail());
        userDTO.setPhone(order.getUser().getPhone());
        dto.setUser(userDTO);

        AddressDTO addressDTO = null;
        if (order.getAddress() != null) {
            addressDTO = getAddressDTO(order);
        }
        dto.setAddress(addressDTO);
        
        dto.setOrderItems(orderItemDTOList);
        return dto;


    }

    @Override
    public OrderDTO convertToDTOAdmin(Orders order) {
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

        UserDTO userDTO = new UserDTO();
        userDTO.setName(order.getUser().getName());
        userDTO.setPhone(order.getUser().getPhone());
        dto.setUser(userDTO);


        AddressDTO addressDTO = null;
        if (order.getAddress() != null) {
            addressDTO = getAddressDTO(order);
        }
        dto.setAddress(addressDTO);

        List<OrderItemDTO> orderItemlist = order.getOrderItems().stream().map(
                item -> {
                    OrderItemDTO dtoItem = new OrderItemDTO();
                    dtoItem.setOrderMenuItemName(item.getMenuItem().getName());
                    dtoItem.setQuantity(item.getQuantity());
                    dtoItem.setExtras(item.getExtras()!=null ? item.getExtras().stream()
                            .map(extra -> {
                                ExtraDTO extraDTO = new ExtraDTO();
                                extraDTO.setName(extra.getName());
                                return extraDTO;
                            }).toList():List.of());
                    return dtoItem;
                }).toList();


        dto.setOrderItems(orderItemlist);
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
}
