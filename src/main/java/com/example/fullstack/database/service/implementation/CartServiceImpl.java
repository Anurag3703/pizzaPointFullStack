package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.*;
import com.example.fullstack.database.repository.*;
import com.example.fullstack.database.service.CartService;
import com.example.fullstack.database.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public CartServiceImpl(CartRepository cartRepository, ExtraRepository extraRepository,
                           UserService userService, MenuItemRepository menuItemRepository,CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.extraRepository = extraRepository;
        this.userService = userService;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity, List<String> extraItemId) {
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
        //Extras
        List<Extra> extras = extraRepository.findAllById(extraItemId);

        // Cart Item Deletion Addition Logic
        CartItem cartItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(quantity);
            cartItem.setExtras(extras);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        // Cart Item Total Price
        BigDecimal itemBasePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal itemExtrasPrice = extras.stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal totalItemPrice = itemBasePrice.add(itemExtrasPrice);

        cartItem.setTotalPrice(totalItemPrice);
        cartItemRepository.save(cartItem);

        // Adds new cart item from existing it makes a bit slower as it calculates again and again
        cart.getCartItems().add(cartItem);
        BigDecimal cartTotal = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Total Price in cart
        cart.setTotalPrice(cartTotal);
        //Save the cart
        cartRepository.save(cart);
    }

    @Override
    public void updateItemQuantity(Long cartItemId, Long quantity) {
        Logger logger = LoggerFactory.getLogger(getClass());
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            logger.debug("Cart item with ID {} removed from the database", cartItemId);
            return;
        }

        //If the item is increased or decreased
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(cartItem.getMenuItem().getPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(cartItem);

        // Update the cart total
        Cart cart = cartItem.getCart();
        BigDecimal cartTotal = cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(cartTotal);
        cartRepository.save(cart);

    }

    @Override
    public Optional<Cart> getCart(Long cartId) {
        return Optional.empty();
    }

    @Override
    public BigDecimal getCartTotalPrice(HttpSession session) {
        return null;
    }

    @Override
    public List<Cart> getAllCartItems() {
        return List.of();
    }

    @Override
    public List<Cart> getCartByUserEmail(String email) {
        return cartRepository.findByUserEmail(email);
    }
}
