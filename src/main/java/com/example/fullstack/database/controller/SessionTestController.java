package com.example.fullstack.database.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-test")
public class SessionTestController {

    @GetMapping
    public ResponseEntity<String> testSession(HttpSession session) {
        Integer visits = (Integer) session.getAttribute("visits");
        if (visits == null) {
            visits = 0;
        }
        session.setAttribute("visits", ++visits);
        return ResponseEntity.ok("Session is working with Redis! Session ID: " + session.getId() + ", Visits: " + visits);
    }
}