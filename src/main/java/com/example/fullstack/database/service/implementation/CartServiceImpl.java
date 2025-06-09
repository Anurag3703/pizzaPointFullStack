package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.CartService;
import com.example.fullstack.database.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ExtraRepository extraRepository;
    private final UserService userService;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ExtraRepository extraRepository,
                           UserService userService, MenuItemRepository menuItemRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.extraRepository = extraRepository;
        this.userService = userService;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity, List<String> extraItemId, String instructions) {
        User currentUser = userService.getCurrentUser();
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item Not Found"));

        // Find or create cart
        Cart cart = cartRepository.findByUser(currentUser)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        // Get extras
        List<Extra> extras = extraRepository.findAllById(extraItemId);

        // Check if same item with same extras and instructions already exists
        CartItem existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenuItem().equals(menuItem) &&
                        hasSameExtras(item.getExtras(), extras) &&
                        Objects.equals(item.getInstruction(), instructions))
                .findFirst()
                .orElse(null);

        CartItem cartItem;
        if (existingCartItem != null) {
            // Update existing cart item
            cartItem = existingCartItem;
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(quantity);
            cartItem.setExtras(extras);
            cartItem.setInstruction(instructions);
            cart.getCartItems().add(cartItem); // Add to cart's item list only for new items
        }

        // Recalculate cart item total price using the helper method
        cartItem.setTotalPrice(calculateCartItemTotalPrice(cartItem));
        cartItemRepository.save(cartItem);

        // Recalculate cart total price
        cart.setTotalPrice(calculateCartTotalPrice(cart));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long cartItemId, Long quantity) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            // Fetch the cart item only once
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            Cart cart = cartItem.getCart();

            // Case 1: When quantity is zero or negative - remove item
            if (quantity <= 0) {
                cart.getCartItems().remove(cartItem);  // Remove from the List of cart item in the Cart
                cartItemRepository.delete(cartItem);   // Remove from CartItem table

                // Recalculate cart total using helper method
                cart.setTotalPrice(calculateCartTotalPrice(cart));
                cartRepository.save(cart);  // Save cart

                logger.debug("Cart item with ID {} removed from the database", cartItemId);
                return;
            }

            // Case 2: Update quantity
            cartItem.setQuantity(quantity);
            // Use the helper method to calculate total price including extras
            cartItem.setTotalPrice(calculateCartItemTotalPrice(cartItem));
            cartItemRepository.save(cartItem);

            // Recalculate cart total using helper method
            cart.setTotalPrice(calculateCartTotalPrice(cart));
            cartRepository.save(cart);  // Save cart

        } catch (RuntimeException e) {
            logger.error("Error updating cart item: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Cart> getCart(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public BigDecimal getCartTotalPrice(HttpSession session) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        Cart cart = cartRepository.findByUser(currentUser)
                .stream()
                .findFirst()
                .orElse(null);

        return cart != null ? cart.getTotalPrice() : BigDecimal.ZERO;
    }

    @Override
    public List<Cart> getAllCartItems() {
        User user = userService.getCurrentUser(); // Get the current user
        if (user == null) {
            throw new RuntimeException("User not authenticated"); // Handle case where user is not logged in
        }
        return cartRepository.findByUser(user);
    }

    @Override
    public List<Cart> getCartByUserEmail(String email) {
        return cartRepository.findByUserEmail(email);
    }

    @Override
    public void transferGuestCartToUser(HttpSession session, User user) {
        // Implementation for transferring guest cart to authenticated user
        // This can be implemented based on your specific requirements
    }

    @Override
    public BigDecimal calculateCartItemTotalPrice(CartItem cartItem) {
        BigDecimal basePrice = cartItem.getMenuItem().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal extraPrice = cartItem.getExtras().stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return basePrice.add(extraPrice);
    }

    @Override
    public BigDecimal calculateCartTotalPrice(Cart cart) {
        // Calculate total of all cart items
        BigDecimal itemsTotal = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add delivery fee if needed (uncomment and modify as per your business logic)
        // BigDecimal deliveryFee = BigDecimal.valueOf(400);
        // return itemsTotal.add(deliveryFee);

        return itemsTotal;
    }

    @Override
    public Cart getCurrentUserCart() {
        User currentUser = userService.getCurrentUser();
        return cartRepository.findByUser(currentUser)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart not found for the current user"));
    }

    // Helper method to check if two lists of extras are the same
    private boolean hasSameExtras(List<Extra> extras1, List<Extra> extras2) {
        if (extras1 == null && extras2 == null) {
            return true;
        }
        if (extras1 == null || extras2 == null) {
            return false;
        }
        if (extras1.size() != extras2.size()) {
            return false;
        }

        // Sort both lists by ID and compare
        List<String> ids1 = extras1.stream()
                .map(Extra::getId)
                .sorted()
                .collect(Collectors.toList());
        List<String> ids2 = extras2.stream()
                .map(Extra::getId)
                .sorted()
                .collect(Collectors.toList());

        return ids1.equals(ids2);
    }
}

//    @Override
//    public Cart getCartByUser(User user) {
//        return null;
//    }