package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.Cart;
import com.example.fullstack.database.model.CartItem;
import com.example.fullstack.database.model.MenuItem;
import com.example.fullstack.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartAndMenuItem(Cart cart, MenuItem menuItem);

    List<CartItem> findByCart_User(User currentUser);
}
