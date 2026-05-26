package com.example.task_manager.service;

import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.exception.TaskNotFoundException;
import com.example.task_manager.exception.UserNotFoundException;
import com.example.task_manager.model.Project;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.Task;
import com.example.task_manager.model.User;
import com.example.task_manager.repository.ProjectRepository;
import com.example.task_manager.repository.TaskRepository;
import com.example.task_manager.repository.UserRepository;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.utils.RatingValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final StatusService statusService;
    private final UserService userService;


    public TaskService(TaskRepository taskRepository, UserRepository userRepository, StatusService statusService, UserService userService, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.statusService = statusService;
        this.userService = userService;
    }

    // GET все задачи

    // POST Создать новую задачу Method updated 
    // Если не указывается ID автора задачи, то ID присваивается текущему пользователю
    public Task createTask(TaskRequest request, Long userId) {

        log.info("Запрос на создание задачи: title='{}', dueTime={}, userId={}, rating={}",
                request.title(), request.dueTime(), request.userId(), request.rating());
        
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        RatingValidator.validateRating(request.rating());

        User creator = userService.getUserById(userId);

        Long ownerId;

        if (creator.getRole() == Role.ADMIN) {
            ownerId = (request.userId() != null) ? request.userId() : creator.getId();
        }
        else {
            if (request.userId() != null && !Objects.equals(request.userId(), creator.getId())) {
                throw new AccessDeniedException("Пользователь не может создавать задачи другим пользователям");
            }

            ownerId = creator.getId();
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Владелец задачи не найден"));
        
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));
        }        

        Task task = new Task(request.title(), request.dueTime(), ownerId);

        task.setProject(project);

        task.setCreatedAt(LocalDateTime.now());
        task.setCreatedBy(creator.getId());
        task.setRating(request.rating() != null ? request.rating() : 0.0);
        task.setStatus(statusService.getCurrentStatus(task));

        Task saved = taskRepository.save(task);

        owner.setTaskCount(owner.getTaskCount() + 1);
        userRepository.save(owner);
        userService.updateTopStatus(ownerId);

        return saved;

    }

    // method updated 
    public Task updateTask(Long taskId, TaskRequest request, Long requesterId, Role role) {
        log.info("Обновление задачи ID={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с ID " + taskId + " не найдена"));

        boolean isOwner = Objects.equals(task.getUserId(), requesterId);

        if (role != Role.ADMIN && !isOwner) {
                throw new AccessDeniedException(
                        "Пользователь может обновлять только свои задачи"
                );
        }

        if (request.title() != null && !request.title().trim().isEmpty()) {
            task.setTitle(request.title());
        }

        if (request.dueTime() != null) {
            task.setDueTime(request.dueTime());
        }

        // Обновляем поля
        task.setTitle(request.title());
        task.setDueTime(request.dueTime());
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(requesterId);

        // Обновляем рейтинг только если он передан
        if (request.rating() != null) {
            RatingValidator.validateRating(request.rating());
            task.setRating(request.rating());
        }

        task.setStatus(statusService.getCurrentStatus(task));

        Task savedTask = taskRepository.save(task);

        log.info("Задача ID={} обновлена: dueTime={}, rating={}",
                taskId, savedTask.getDueTime(), savedTask.getRating());

        // Обновляем TOP статус пользователя
        userService.updateTopStatus(task.getUserId());

        return savedTask;
    }

    // DELETE Удалить задачу
    public void deleteTask(Long taskId, Long requesterId, Role role) {
        log.info("Запрос на удаление задачи с ID={}", taskId);

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            log.warn("Попытка удалить несуществующую задачу с ID={}", taskId);
            throw new TaskNotFoundException("Задача не найдена");
        }

        boolean isOwner = Objects.equals(task.getUserId(), requesterId);

        if (role != Role.ADMIN && !isOwner) {
                throw new AccessDeniedException(
                        "Пользователь может удалять только свои задачи"
                );
        }

        Long userId = task.getUserId();
        
        taskRepository.delete(task);

        userRepository.findById(userId).ifPresent(user -> {
            user.setTaskCount(Math.max(0, user.getTaskCount() - 1));
            userRepository.save(user);
        });

        userService.updateTopStatus(userId);

        log.info("Задача ID={} ('{}') успешно удалена", taskId, task.getTitle());

    }

    public Task completeTask(Long taskId, Long userId, Role role) {
        
        log.info("Завершение задачи ID={}", taskId);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        boolean isOwner = Objects.equals(task.getUserId(), userId);

        if (role != Role.ADMIN && !isOwner) {
            throw new AccessDeniedException("Пользователь может завершать только свои задачи");
        }    

        if (task.getCompletedAt() != null) {
            log.warn("Попытка повторного завершения задачи ID={}", taskId);
            return task;
        }

        task.setCompletedAt(LocalDateTime.now());
        task.setStatus(statusService.getCurrentStatus(task));

        Task savedTask = taskRepository.save(task);

        userService.updateTopStatus(task.getUserId());

        log.info("Задача ID={} завершена, статус={}", taskId, savedTask.getStatus());

        return savedTask;
    }

    // new method for GET /tasks
    public List<Task> getAllTasksForUser(Long userId, Role role) {
        if (role == Role.ADMIN) {
            return taskRepository.findAll();
        }
        return taskRepository.findAllByUserId(userId);
    }

    // new method for GET tasks/{id}
    public Task getTaskByIdForUser(Long taskId, Long userId, Role role) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));

        if (role == Role.ADMIN) {
            return task;
        }

        if (!Objects.equals(task.getUserId(), userId)) {
            throw new AccessDeniedException("Пользователь не может смотреть чужие задачи");
        }
        return task;

    }

    // new method for GET tasks/users/{id}
    public List<Task> getAllTasksByUserIdForUser(Long targetUserId, Long requesterId, Role role) {

        if (!userRepository.existsById(targetUserId)) {
            throw new UserNotFoundException("Пользователь с ID " + targetUserId + " не найден");
        }

        if (role == Role.ADMIN) {
            return taskRepository.findAllByUserId(targetUserId);
        }

        if (!Objects.equals(targetUserId, requesterId)) {
            throw new AccessDeniedException("Пользователь не может смотреть чужие задачи");
        }
        return taskRepository.findAllByUserId(requesterId);
    }

    public List<UserProjectTaskReport> getReport(Long userId, Role role) {
        if (role == Role.ADMIN) {
            return taskRepository.getUserProjectTaskReport(userId);
        } else {
            throw new AccessDeniedException("Пользователь не может смотреть отчет");
        }
        
    }
}

