package com.example.minicommerce.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);

        // İleride fakrlı detaylar beklersek id bulunamadı döndürürsek mesela
        // farklı fieldlar gerekebilir bu class'a
    }
}
