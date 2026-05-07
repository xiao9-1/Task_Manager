package com.example.task_manager.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.task_manager.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, есть ли уже пользователи
        if ((userRepository.findAll()).size() > 0) {
            System.out.println("=== Тестовые данные уже есть, пропускаем инициализацию ===");
            return;
        }
        
        Thread.sleep(3000);
        
        String[] commands = {
            // ===== ПОЛЬЗОВАТЕЛИ =====
            "curl -X POST http://localhost:8080/users -H 'Content-Type: application/json' -d '{\"name\":\"Алексей\",\"email\":\"alex@test.com\"}'",
            "curl -X POST http://localhost:8080/users -H 'Content-Type: application/json' -d '{\"name\":\"Мария\",\"email\":\"maria@test.com\"}'",
            "curl -X POST http://localhost:8080/users -H 'Content-Type: application/json' -d '{\"name\":\"Иван\",\"email\":\"ivan@test.com\"}'",
            "curl -X POST http://localhost:8080/users -H 'Content-Type: application/json' -d '{\"name\":\"Елена\",\"email\":\"elena@test.com\"}'",
            "curl -X POST http://localhost:8080/users -H 'Content-Type: application/json' -d '{\"name\":\"Пётр\",\"email\":\"petr@test.com\"}'",

            // ===== ЗАДАЧИ ДЛЯ АЛЕКСЕЯ (userId=1) =====
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Купить молоко\",\"dueTime\":\"2026-06-01T23:00:00\",\"userId\":1,\"rating\":1.0}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Выбросить мусор\",\"dueTime\":\"2026-07-01T23:00:00\",\"userId\":1,\"rating\":0.5}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Сделать домашку\",\"dueTime\":\"2026-05-10T23:00:00\",\"userId\":1,\"rating\":0.3}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Позвонить маме\",\"dueTime\":\"2026-05-15T23:00:00\",\"userId\":1,\"rating\":0.2}'",

            // ===== ЗАДАЧИ ДЛЯ МАРИИ (userId=2) =====
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Сходить в спортзал\",\"dueTime\":\"2026-05-20T23:00:00\",\"userId\":2,\"rating\":0.8}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Прочитать книгу\",\"dueTime\":\"2026-06-10T23:00:00\",\"userId\":2,\"rating\":0.4}'",
            
            // ===== ЗАДАЧИ ДЛЯ ИВАНА (userId=3) =====
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Закончить проект\",\"dueTime\":\"2026-05-25T23:00:00\",\"userId\":3,\"rating\":0.9}'",
            
            // ===== ЗАДАЧИ ДЛЯ ЕЛЕНЫ (userId=4) =====
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Купить подарок\",\"dueTime\":\"2026-05-12T23:00:00\",\"userId\":4,\"rating\":0.6}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Записаться к врачу\",\"dueTime\":\"2026-05-18T23:00:00\",\"userId\":4,\"rating\":0.3}'",
            
            // ===== ПРОСРОЧЕННЫЕ ЗАДАЧИ (для проверки статусов) =====
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Сдать отчёт\",\"dueTime\":\"2026-04-01T23:00:00\",\"userId\":1,\"rating\":0.0}'",
            "curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{\"title\":\"Оплатить налоги\",\"dueTime\":\"2026-03-15T23:00:00\",\"userId\":2,\"rating\":0.0}'"
             
        };
        
        for (String cmd : commands) {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        }
        
        System.out.println("=== Начальные данные загружены ===");
    }
}