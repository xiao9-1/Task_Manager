package com.example.task_manager.service;

import com.example.task_manager.dto.TaskDto;
import com.example.task_manager.dto.TaskRequest;
import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.exception.AccessDeniedException;
import com.example.task_manager.exception.ResourceNotFoundException;
import com.example.task_manager.exception.ResourceNotFoundException;
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

    private void validateTaskRequest(TaskRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок не может быть пустым");
        }

        RatingValidator.validateRating(request.rating());
    }

    private Long resolveOwnerId(TaskRequest request, User creator) {

        if (creator.getRole() == Role.ADMIN) {
            return request.userId() != null ? request.userId() : creator.getId();
        }

        if (request.userId() != null &&
            !Objects.equals(request.userId(), creator.getId())) {
            throw new AccessDeniedException("Нельзя создавать задачи другим пользователям");
        }

        return creator.getId();
    }

    private void checkAccess(Task task, Long requesterId, Role role) {

        boolean isOwner = Objects.equals(task.getUserId(), requesterId);

        if (role != Role.ADMIN && !isOwner) {
            throw new AccessDeniedException("Нет доступа к задаче");
        }
    }

    private void applyProject(Task task, Long projectId) {
        if (projectId == null) return;

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));

        task.setProject(project);
    }

    private void applyRating(Task task, Double rating) {
        double finalRating = (rating != null) ? rating : 0.0;

        RatingValidator.validateRating(finalRating);
        task.setRating(finalRating);
    }

    private void applyStatus(Task task) {
        task.setStatus(statusService.getCurrentStatus(task));
    }

    private void applyBasicFields(Task task, TaskRequest request) {

        if (request.title() != null && !request.title().trim().isEmpty()) {
            task.setTitle(request.title());
        }

        if (request.dueTime() != null) {
            task.setDueTime(request.dueTime());
        }
    }

    private void applyCreateAudit(Task task, Long userId) {

        LocalDateTime now = LocalDateTime.now();

        task.setCreatedAt(now);
        task.setCreatedBy(userId);

        task.setUpdatedAt(now);
        task.setUpdatedBy(userId);
    }

    private void applyUpdateAudit(Task task, Long userId) {

        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(userId);
    }

    // GET все задачи

    // POST Создать новую задачу Method updated 
    // Если не указывается ID автора задачи, то ID присваивается текущему пользователю
    public Task createTask(TaskRequest request, Long userId) {

        log.info("Создание задачи: title='{}'", request.title());

        validateTaskRequest(request);

        User creator = userService.getUserById(userId);

        Long ownerId = resolveOwnerId(request, creator);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Владелец задачи не найден"));

        Task task = new Task(request.title(), request.dueTime(), ownerId);

        applyProject(task, request.projectId());
        applyCreateAudit(task, creator.getId());
        applyRating(task, request.rating());
        applyStatus(task);

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
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));

        checkAccess(task, requesterId, role);

        applyBasicFields(task, request);
        applyProject(task, request.projectId());

        if (request.userId() != null) {

            if (role != Role.ADMIN) {
                throw new AccessDeniedException("Только админ может менять владельца задачи");
            }

            User newOwner = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

            task.setUserId(newOwner.getId());
        }

        applyUpdateAudit(task, requesterId);
        applyRating(task, request.rating());
        applyStatus(task);

        Task saved = taskRepository.save(task);

        userService.updateTopStatus(task.getUserId());

        return saved;
    }

    // DELETE Удалить задачу
    public void deleteTask(Long taskId, Long requesterId, Role role) {
        log.info("Запрос на удаление задачи с ID={}", taskId);

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            log.warn("Попытка удалить несуществующую задачу с ID={}", taskId);
            throw new ResourceNotFoundException("Задача не найдена");
        }

        checkAccess(task, requesterId, role);

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
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        checkAccess(task, userId, role);  

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
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));

        checkAccess(task, userId, role);
        return task;

    }

    // new method for GET tasks/users/{id}
    public List<Task> getAllTasksByUserIdForUser(Long targetUserId, Long requesterId, Role role) {

        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + targetUserId + " не найден");
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

