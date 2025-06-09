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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ExtraRepository extraRepository;
    private final UserService userService;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ExtraRepository extraRepository,
                           UserService userService, MenuItemRepository menuItemRepository,
                           CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.extraRepository = extraRepository;
        this.userService = userService;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public void addItemToCart(HttpSession session, String menuItemId, Long quantity, List<String> extraItemIds, String instructions) {
        User currentUser = userService.getCurrentUser();

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item Not Found"));

        List<Extra> selectedExtras = extraRepository.findAllById(extraItemIds != null ? extraItemIds : new ArrayList<>());

        // Get or create cart
        Cart cart = cartRepository.findByUser(currentUser).stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        // Check if a cart item with same menu item, extras and instructions already exists
        Optional<CartItem> optionalExistingItem = cart.getCartItems().stream()
                .filter(item -> item.getMenuItem().equals(menuItem)
                        && hasSameExtras(item.getExtras(), selectedExtras)
                        && Objects.equals(item.getInstruction(), instructions))
                .findFirst();

        CartItem cartItem;
        if (optionalExistingItem.isPresent()) {
            // Update existing item
            cartItem = optionalExistingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(quantity);
            cartItem.setExtras(selectedExtras);
            cartItem.setInstruction(instructions);

            // Only add new items to cart list
            cart.getCartItems().add(cartItem);
        }

        // Update cart item price and save
        cartItem.setTotalPrice(calculateCartItemTotalPrice(cartItem));
        cartItemRepository.save(cartItem);

        // Update cart total price and save
        cart.setTotalPrice(calculateCartTotalPrice(cart));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long cartItemId, Long quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = cartItem.getCart();

        if (quantity <= 0) {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.setTotalPrice(calculateCartItemTotalPrice(cartItem));
            cartItemRepository.save(cartItem);
        }

        cart.setTotalPrice(calculateCartTotalPrice(cart));
        cartRepository.save(cart);
    }

    @Override
    public Optional<Cart> getCart(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public BigDecimal getCartTotalPrice(HttpSession session) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return BigDecimal.ZERO;

        return cartRepository.findByUser(currentUser).stream()
                .findFirst()
                .map(Cart::getTotalPrice)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<Cart> getAllCartItems() {
        User user = userService.getCurrentUser();
        if (user == null) throw new RuntimeException("User not authenticated");
        return cartRepository.findByUser(user);
    }

    @Override
    public List<Cart> getCartByUserEmail(String email) {
        return cartRepository.findByUserEmail(email);
    }

    @Override
    public void transferGuestCartToUser(HttpSession session, User user) {
        // Not implemented for now
    }

    @Override
    public BigDecimal calculateCartItemTotalPrice(CartItem cartItem) {
        BigDecimal basePrice = cartItem.getMenuItem().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal extrasPrice = cartItem.getExtras().stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return basePrice.add(extrasPrice);
    }

    @Override
    public BigDecimal calculateCartTotalPrice(Cart cart) {
        return cart.getCartItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Cart getCurrentUserCart() {
        User user = userService.getCurrentUser();
        return cartRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    // Helper method: compare extra lists
    private boolean hasSameExtras(List<Extra> list1, List<Extra> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;

        List<String> ids1 = list1.stream().map(Extra::getId).sorted().collect(Collectors.toList());
        List<String> ids2 = list2.stream().map(Extra::getId).sorted().collect(Collectors.toList());
        return ids1.equals(ids2);
    }
}
