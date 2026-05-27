package com.example.task_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_manager.dto.DirectionRequest;
import com.example.task_manager.dto.DirectionResponse;
import com.example.task_manager.model.Direction;
import com.example.task_manager.repository.DirectionRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("\n =======DirectionService Unit Tests======= \n")
public class DirectionServiceTest {

    @Mock
    private DirectionRepository directionRepository;

    @InjectMocks
    private DirectionService directionService;

    @Test
    @DisplayName("createDirection() -> создает направление")
    public void createDirection_CreateNewDirection() {

        DirectionRequest request = new DirectionRequest("new dir");

        Direction savedDirection = new Direction();
        savedDirection.setId(1L);
        savedDirection.setName("new dir");

        when(directionRepository.save(any(Direction.class)))
                .thenReturn(savedDirection);

        Direction result = directionService.createDirection(request);

        assertEquals("new dir", result.getName());
        assertEquals(1L, result.getId());

        verify(directionRepository).save(any(Direction.class));
    }

    @Test
    @DisplayName("createDirection() -> создает направление")
    public void createDirectionWithEmptyTitle_ReturnsException() {

        DirectionRequest request = new DirectionRequest("");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> directionService.createDirection(request)
        );

        assertEquals("Имя направления не может быть пустым", ex.getMessage());

    }

    @Test
    @DisplayName("createDirection() -> создает направление")
    public void createDirectionWithNullTitle_ReturnsException() {

        DirectionRequest request = new DirectionRequest(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> directionService.createDirection(request)
        );

        assertEquals("Имя направления не может быть пустым", ex.getMessage());

    }
    

    @Test
    @DisplayName("createDirection() -> дубликат имени вызывает исключение")
    void createDirectionWithDuplicateName_ThrowsException() {

        DirectionRequest request = new DirectionRequest("same name");

        when(directionRepository.existsByName("same name"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> directionService.createDirection(request)
        );

        assertEquals("Направление с таким названием уже существует.", ex.getMessage());

        verify(directionRepository).existsByName("same name");
        verify(directionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllDirections() -> вызывает все направления")
    public void getAllDirections_ReturnsAllDirections() {

        Direction savedDirection1 = new Direction();
        savedDirection1.setId(1L);
        savedDirection1.setName("new dir");

        Direction savedDirection2 = new Direction();
        savedDirection2.setId(2L);
        savedDirection2.setName("new dir");

        List<Direction> dirs = new ArrayList<>();

        dirs.add(savedDirection1);
        dirs.add(savedDirection2);

        when(directionRepository.findAll()).thenReturn(dirs);

        List<Direction> result = directionService.getAllDirections();

        assertEquals(2, result.size());

    }
}
