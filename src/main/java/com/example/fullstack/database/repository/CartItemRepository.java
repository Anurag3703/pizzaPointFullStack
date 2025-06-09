package com.example.fullstack.database.repository;

import com.example.fullstack.database.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {



    List<CartItem> findByCart_User(User currentUser);

     Optional<CartItem> findByCartAndMenuItemAndExtrasAndInstruction(Cart cart, MenuItem menuItem, List<Extra> extras, String instruction);
    List<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);

}
