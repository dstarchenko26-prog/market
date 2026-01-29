package ua.nulp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    //Тимчасова заглушка
    public void send(String to, String subject, String emailContent) {
        System.out.print(to);
        System.out.print(subject);
        System.out.print(emailContent);
    }
}