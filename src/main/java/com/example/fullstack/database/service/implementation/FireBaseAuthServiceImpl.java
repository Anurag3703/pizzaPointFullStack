package com.example.fullstack.database.service.implementation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;


@Service
public class FireBaseAuthServiceImpl  {

    public boolean verifyOtp(String idToken) throws FirebaseAuthException {
        // Verify the ID token
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken != null; // Return true if the token is valid
    }
}
