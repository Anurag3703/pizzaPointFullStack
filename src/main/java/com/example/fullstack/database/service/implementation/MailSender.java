//Dummy Class for Mail Sending
package com.example.fullstack.database.service.implementation;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailSender extends JavaMailSenderImpl {
    JavaMailSenderImpl javaMailSender;
    public MailSender() {
        super();
    }

}
