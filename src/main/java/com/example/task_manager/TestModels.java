package com.example.task_manager;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.Status;
import java.time.LocalDateTime;

public class TestModels {
    public static void main(String[] args) {

        // 1 Проверка создания задачи
        Task task = new Task("Сделать что-то", LocalDateTime.now().plusDays(1));
        task.setId(1L);
        task.setCreatedAt(LocalDateTime.now());

        System.out.println("Задача создана: ");
        System.out.println("ID: " + task.getId());
        System.out.println("Название: " + task.getTitle());
        System.out.println("Стаус: " + task.getStatus());
        System.out.println("Дедлайн: " + task.getDueTime());

        // 2 Проверка смены статуса
        task.setStatus(Status.COMPLETED_ON_TIME);
        System.out.println("\n Статус изменент на " + task.getStatus());

        // 3 Проверка валидации заголовка
        try {
            task.setTitle("");
            System.out.println("\n Ошибка! Пустой заголовок!");

        } catch (IllegalArgumentException e) {
            System.out.println("\n Валидация работает: " + e.getMessage());
        }

        System.out.println("Все статусы: ");
        for (Status s: Status.values()) {
            System.out.println((" - " + s + s.getRusName()));
        }

    }

}
