package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.UserService;
import com.example.fullstack.security.model.UserSecurity;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final  UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final CartRepository cartRepository;
    private final OrdersRepository ordersRepository;
    private final OrdersServiceImpl ordersServiceImpl;
    private final OrderItemRepository orderItemRepository;



    public UserServiceImpl(UserRepository userRepository, DeletedUserRepository deletedUserRepository,
                           CartRepository cartRepository
                            , OrdersServiceImpl ordersServiceImpl, OrdersRepository ordersRepository, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.deletedUserRepository = deletedUserRepository;
        this.cartRepository = cartRepository;
        this.ordersRepository = ordersRepository;
        this.ordersServiceImpl = ordersServiceImpl;
        this.orderItemRepository = orderItemRepository;

    }



    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> addAllUsers(List<User> users) {
        return userRepository.saveAll(users);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserSecurity) {
            UserSecurity userSecurity = (UserSecurity) principal;
            return getUserFromUserSecurity(userSecurity);
        }
        return null; // User is not authenticated
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        UserSecurity userSecurity = (UserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
        DeletedUser deletedUser = new DeletedUser();
        deletedUser.setPhone(user.getPhone());
        deletedUser.setDeletedAt(LocalDateTime.now());
        user.setName("Anonymous User");
        user.setEmail("anonymous_" + user.getId() + "@example.com");
        user.setPhone("000000000");
        user.setAddress("N/A");
        user.setAnonymous(true);

        if(user.getUserSecurity() != null) {
            user.getUserSecurity().setEmail("anonymous_" + user.getId() + "@example.com");
            user.getUserSecurity().setPhone("000000000");
            user.getUserSecurity().setPassword("anonymous");
            user.getUserSecurity().setMobileVerified(false);
        }

        if(user.getAddresses()!=null) {
            for(Address address : user.getAddresses()) {
                address.setFloor("N/A");
                address.setStreet("Anonymous Street");
                address.setIntercom("N/A");
                address.setApartmentNo("N/A");
                address.setBuildingName("Anonymous Building");
                address.setOtherInstructions("N/A");
            }
        }
        Cart cart = cartRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElse(null);

        if (cart != null) {
            cartRepository.delete(cart);  // Delete the cart from the database (this will also delete CartItems)
        }

        Optional<Orders> optionalOrder = ordersRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, Status.PENDING);
        if (optionalOrder.isPresent()) {
            Orders order = optionalOrder.get();
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItemRepository.delete(orderItem);  // Delete OrderItems
            }
            ordersRepository.delete(order);  // Delete the order after deleting its items
        } else {
            throw new RuntimeException("No pending orders found");
        }


        // Save the anonymized user
        deletedUserRepository.save(deletedUser);
        userRepository.save(user);

    }

    private User getUserFromUserSecurity(UserSecurity userSecurity) {
        // Convert UserSecurity to User
        return userRepository.findByEmail(userSecurity.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    }



