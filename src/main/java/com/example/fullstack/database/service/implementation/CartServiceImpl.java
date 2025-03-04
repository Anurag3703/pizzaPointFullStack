    package com.example.fullstack.database.service.implementation;

    import com.example.fullstack.database.model.Cart;
    import com.example.fullstack.database.model.Extra;
    import com.example.fullstack.database.model.MenuItem;
    import com.example.fullstack.database.model.User;
    import com.example.fullstack.database.repository.CartRepository;
    import com.example.fullstack.database.repository.ExtraRepository;
    import com.example.fullstack.database.repository.MenuItemRepository;
    import com.example.fullstack.database.service.CartService;
    import com.example.fullstack.database.service.UserService;

    import jakarta.servlet.http.HttpSession;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class CartServiceImpl implements CartService {

        private final ExtraRepository extraRepository;
        CartRepository cartRepository;
        MenuItemRepository menuItemRepository;
        UserService userService;
        public CartServiceImpl(CartRepository cartRepository, MenuItemRepository menuItemRepository, UserService userService, ExtraRepository extraRepository) {
            this.cartRepository = cartRepository;
            this.menuItemRepository = menuItemRepository;
            this.userService = userService;
            this.extraRepository = extraRepository;
        }

        @Override
        public void addItemToCart(HttpSession session, String menuItemId, Long quantity,List<String> extraItemId) {

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu Item Not Found"));
            User currentUser = userService.getCurrentUser();
            List<Extra> extras = extraRepository.findAllById(extraItemId);

            Cart cartItem = new Cart();
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(quantity);
            cartItem.setTotalPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUser(currentUser);
            cartItem.setExtras(extras);
            BigDecimal totalPrice = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity))
                    .add(extras.stream()
                            .map(Extra::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .multiply(BigDecimal.valueOf(quantity)));

            cartItem.setTotalPrice(totalPrice);
            cartRepository.save(cartItem);
        }

        @Override
        public void updateItemQuantity(Long cartId, Long quantity) {
            Logger logger = LoggerFactory.getLogger(getClass());
            Cart cartItem = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (quantity <= 0) {
                cartRepository.deleteById(cartId);
                logger.debug("Cart item with ID {} removed from the database", cartId); // Debug log
                return;
            }
            cartItem.setQuantity(quantity);
            cartItem.setTotalPrice(cartItem.getMenuItem().getPrice().multiply(BigDecimal.valueOf(quantity))); // Use menuItem price

            // Save the updated cart item
            cartRepository.save(cartItem);
        }

        @Override
        public Optional<Cart> getCart(Long cartId) {

            return cartRepository.findById(cartId);
        }


        @Override
        public BigDecimal getCartTotalPrice(HttpSession session) {
    User currentUser = userService.getCurrentUser();
    List<Cart> cartItems = cartRepository.findByUser(currentUser);

    if (cartItems.isEmpty()) {
        return BigDecimal.ZERO;
    }

    return cartItems.stream()
            .map(Cart::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

        @Override
        public List<Cart> getAllCartItems() {
            User currentUser = userService.getCurrentUser();
            return cartRepository.findByUser(currentUser);
        }



    }
