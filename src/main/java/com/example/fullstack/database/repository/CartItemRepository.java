package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartAndMenuItem(Cart cart, MenuItem menuItem);

}
