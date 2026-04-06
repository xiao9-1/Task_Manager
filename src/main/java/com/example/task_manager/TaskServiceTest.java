package com.example.task_manager;

import com.example.task_manager.model.Task;
import com.example.task_manager.model.TaskRequest;
import com.example.task_manager.service.TaskService;
import java.time.LocalDateTime;

public class TaskServiceTest {
    public static void main(String[] args) {
        TaskService service = new TaskService();

        // 1. Создаём задачи
        System.out.println("=== СОЗДАЁМ ЗАДАЧИ ===");
        TaskRequest request1 = new TaskRequest("Купить хлеб", LocalDateTime.now().plusDays(1));
        Task task1 = service.createTask(request1);
        System.out.println("Создана задача: ID=" + task1.getId() + ", " + task1.getTitle());

        TaskRequest request2 = new TaskRequest("Выучить Java", LocalDateTime.now().plusDays(7));
        Task task2 = service.createTask(request2);
        System.out.println("Создана задача: ID=" + task2.getId() + ", " + task2.getTitle());

        // 2. Получаем все задачи
        System.out.println("\n=== ВСЕ ЗАДАЧИ ===");
        service.getAllTasks().forEach(t ->
                System.out.println("ID=" + t.getId() + ": " + t.getTitle())
        );

        // 3. Получаем задачу по ID
        System.out.println("\n=== ПОЛУЧАЕМ ЗАДАЧУ ID=1 ===");
        Task found = service.getTaskById(1L);
        System.out.println("Найдено: " + found.getTitle());

        // 4. Обновляем задачу
        System.out.println("\n=== ОБНОВЛЯЕМ ЗАДАЧУ ===");
        TaskRequest updateRequest = new TaskRequest("Купить хлеб и молоко", LocalDateTime.now().plusDays(2));
        Task updated = service.updateTask(1L, updateRequest);
        System.out.println("Обновлено: " + updated.getTitle());

        // 5. Удаляем задачу
        System.out.println("\n=== УДАЛЯЕМ ЗАДАЧУ ID=2 ===");
        boolean deleted = service.deleteTask(2L);
        System.out.println("Удалено: " + deleted);

        // 6. Проверяем, что осталось
        System.out.println("\n=== ОСТАВШИЕСЯ ЗАДАЧИ ===");
        service.getAllTasks().forEach(t ->
                System.out.println("ID=" + t.getId() + ": " + t.getTitle())
        );
    }
}