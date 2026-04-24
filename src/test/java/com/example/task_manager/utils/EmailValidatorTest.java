package com.example.task_manager.utils;

import com.example.task_manager.utils.EmailValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    @DisplayName("Правильная почта")
    void validEmail_Simple() {
        assertTrue(EmailValidator.isValid("ivan@mail.ru"));
    }

    @Test
    @DisplayName("Правильная почта имеет числа")
    void validEmail_ContainsDigits() {
        assertTrue(EmailValidator.isValid("Ivan123@mail.ru"));
    }

    @Test
    @DisplayName("Правильная почта начинается с числа")
    void validEmail_StartsWithDigits() {
        assertTrue(EmailValidator.isValid("123Ivan@mail.ru"));
    }

    @Test
    @DisplayName("Правильная почта c большой буквы")
    void validEmail_FirstLetterUpperCase() {
        assertTrue(EmailValidator.isValid("Ivan@mail.ru"));
    }


    @Test
    @DisplayName("Неправильная почта Почта без собачки")
    void invalidEmail_NoAtSymbol() {
        assertFalse(EmailValidator.isValid("ivanmail.ru"));
    }

    @Test
    @DisplayName("Неправильная почта начинается с точки")
    void invalidEmail_StartsWithDot() {
        assertFalse(EmailValidator.isValid(".ivanmail@mail.ru"));
    }

    @Test
    @DisplayName("Неправильная почта заканчивается с точки")
    void invalidEmail_EndsWithDot() {
        assertFalse(EmailValidator.isValid("ivanmail.@mail.ru."));
    }

    @Test
    @DisplayName("Неправильная почта нет домена")
    void invalidEmail_NoDomain() {
        assertFalse(EmailValidator.isValid("ivanmail@mail"));
    }

    @Test
    @DisplayName("Неправильная почта нет имени")
    void invalidEmail_NoName() {
        assertFalse(EmailValidator.isValid("@mail"));
    }

    @Test
    @DisplayName("Неправильная почта домен содержит числа")
    void invalidEmail_DomainContainsDigits() {
        assertFalse(EmailValidator.isValid("ivanmail@mail.12"));
    }

    @Test
    @DisplayName("Неправильная почта русские буквы")
    void invalidEmail_ContainsRusLetters() {
        assertFalse(EmailValidator.isValid("иван@mail.ru"));
    }

    @Test
    @DisplayName("null - невалидный")
    void invalidEmail_Null() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    @DisplayName("Только пробелы - невалидный")
    void invalidEmail_OnlySpaces() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    @DisplayName("Пустой - невалидный")
    void invalidEmail_Empty() {
        assertFalse(EmailValidator.isValid(""));
    }



    


}
