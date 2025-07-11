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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final ExtraRepository extraRepository;
    private final UserService userService;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;

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
        validateInputs(menuItemId, quantity, extraItemIds);

        User currentUser = userService.getCurrentUser();
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item Not Found: " + menuItemId));

        Cart cart = getOrCreateCart(currentUser);
        List<Extra> extras = validateAndGetExtras(extraItemIds);

        CartItem cartItem = findMatchingCartItem(cart, menuItem, extras, instructions)
                .map(existingItem -> updateExistingCartItem(existingItem, quantity))
                .orElseGet(() -> createNewCartItem(cart, menuItem, quantity, extras, instructions));

        updateCartAndSave(cart, cartItem);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long cartItemId, Long quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

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
        logger.debug("Updated cart item {} with quantity {}", cartItemId, quantity);
    }

    @Override
    public Optional<Cart> getCart(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public BigDecimal getCartTotalPrice(HttpSession session) {
        return getOrCreateCart(userService.getCurrentUser()).getTotalPrice();
    }

    @Override
    public List<Cart> getAllCartItems() {
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        return cartRepository.findByUser(user);
    }

    @Override
    public List<Cart> getCartByUserEmail(String email) {
        return cartRepository.findByUserEmail(email);
    }

    @Override
    public void transferGuestCartToUser(HttpSession session, User user) {
        // Implementation pending based on requirements
    }

    @Override
    public BigDecimal calculateCartItemTotalPrice(CartItem cartItem) {
        BigDecimal basePrice = cartItem.getMenuItem().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal extraPrice = cartItem.getExtras().stream()
                .map(Extra::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return basePrice.add(extraPrice);
    }

    @Override
    public BigDecimal calculateCartTotalPrice(Cart cart) {
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
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    private void validateInputs(String menuItemId, Long quantity, List<String> extraItemIds) {
        if (menuItemId == null || menuItemId.isBlank()) {
            throw new IllegalArgumentException("Menu item ID cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (extraItemIds == null) {
            throw new IllegalArgumentException("Extra items list cannot be null");
        }
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    private List<Extra> validateAndGetExtras(List<String> extraItemIds) {
        List<Extra> extras = extraRepository.findAllById(extraItemIds);
        if (extras.size() != extraItemIds.size()) {
            throw new RuntimeException("One or more extra items not found");
        }
        return extras;
    }

    private Optional<CartItem> findMatchingCartItem(Cart cart, MenuItem menuItem, List<Extra> extras, String instructions) {
        return cart.getCartItems().stream()
                .filter(item -> item.getMenuItem().equals(menuItem) &&
                        hasSameExtras(item.getExtras(), extras) &&
                        Objects.equals(item.getInstruction(), instructions))
                .findFirst();
    }

    private CartItem updateExistingCartItem(CartItem existingItem, Long quantity) {
        existingItem.setQuantity(existingItem.getQuantity() + quantity);
        return existingItem;
    }

    private CartItem createNewCartItem(Cart cart, MenuItem menuItem, Long quantity, List<Extra> extras, String instructions) {
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setMenuItem(menuItem);
        newItem.setQuantity(quantity);
        newItem.setExtras(extras);
        newItem.setInstruction(instructions);
        cart.getCartItems().add(newItem);
        return newItem;
    }

    private void updateCartAndSave(Cart cart, CartItem cartItem) {
        cartItem.setTotalPrice(calculateCartItemTotalPrice(cartItem));
        cartItemRepository.save(cartItem);
        cart.setTotalPrice(calculateCartTotalPrice(cart));
        cartRepository.save(cart);
    }

    private boolean hasSameExtras(List<Extra> extras1, List<Extra> extras2) {
        if (extras1 == null && extras2 == null) return true;
        if (extras1 == null || extras2 == null) return false;
        if (extras1.size() != extras2.size()) return false;

        List<String> ids1 = extras1.stream().map(Extra::getId).sorted().toList();
        List<String> ids2 = extras2.stream().map(Extra::getId).sorted().toList();
        return ids1.equals(ids2);
    }
}