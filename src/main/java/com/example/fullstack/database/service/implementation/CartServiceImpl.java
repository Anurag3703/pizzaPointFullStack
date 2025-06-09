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
import java.util.Optional;
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ExtraRepository extraRepository;
    private final UserService userService;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ExtraRepository extraRepository,
                           UserService userService, MenuItemRepository menuItemRepository,CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.extraRepository = extraRepository;
        this.userService = userService;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // Modified addItemToCart method
    @Override
    @Transactional
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity, List<String> extraItemId, String instructions) {
        User currentUser = userService.getCurrentUser();
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item Not Found"));

        // Find or create cart (unchanged)
        Cart cart = cartRepository.findByUser(currentUser)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        List<Extra> extras = extraRepository.findAllById(extraItemId);

        // NEW: Check for existing cart items with same config
        List<CartItem> existingItems = cartItemRepository.findByCartAndMenuItem(cart, menuItem);

        Optional<CartItem> matchingItem = existingItems.stream()
                .filter(ci ->
                        ci.getExtras().size() == extras.size() &&
                                ci.getExtras().containsAll(extras) &&
                                ci.getInstruction().equals(instructions))
                .findFirst();

        CartItem cartItem;
        if (matchingItem.isPresent()) {
            cartItem = matchingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(quantity);
            cartItem.setExtras(extras);
            cartItem.setInstruction(instructions);
        }

        // Calculate prices (unchanged)
        BigDecimal itemBasePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal itemExtrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        cartItem.setTotalPrice(itemBasePrice.add(itemExtrasPrice));
        cartItemRepository.save(cartItem);

        // Update cart total (FIXED: removed erroneous +400)
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

            //Case 1 : When Quantity is zero
            if (quantity <= 0) {
                cart.getCartItems().remove(cartItem);  // Removed from the List of cart item in the Cart
                cartItemRepository.delete(cartItem);   //Removed from CartItem table

                BigDecimal cartTotal = cart.getCartItems().stream() //Total price in the cart after removing the cartItem
                        .map(CartItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                cart.setTotalPrice(cartTotal);
                cartRepository.save(cart);  // saved cart

                logger.debug("Cart item with ID {} removed from the database", cartItemId);
                return;
            }

            //Case 2 : Addition or Subtraction of cartItem in cart
            cartItem.setQuantity(quantity);
            cartItem.setTotalPrice(cartItem.getMenuItem().getPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItemRepository.save(cartItem);

            BigDecimal cartTotal = cart.getCartItems().stream() // Total price in Case 2
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotalPrice(cartTotal);
            cartRepository.save(cart);  //save cart

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
        return null;
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

        // Fixed delivery fee
        return cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public Cart getCurrentUserCart() {
        User currentUser = userService.getCurrentUser();
        return cartRepository.findByUser(currentUser)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart not found for the current user"));
    }
}

//    @Override
//    public Cart getCartByUser(User user) {
//        return null;
//    }


