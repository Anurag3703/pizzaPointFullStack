package com.example.fullstack.config;
import com.example.fullstack.database.repository.CartRepository;
import com.stripe.model.tax.Registration;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {
    private final CartRepository cartRepository;
    public SessionListener(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSessionListener.super.sessionDestroyed(se);String sessionId = se.getSession().getId();
        cartRepository.deleteBySessionId(sessionId); // Delete guest cart when session expires
        System.out.println("Session expired. Deleted cart for session: " + sessionId);
    }
}
