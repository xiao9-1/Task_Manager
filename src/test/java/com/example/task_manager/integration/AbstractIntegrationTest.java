package com.example.task_manager.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("taskmanager-test")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    //.withInitScript("db/src.sql"
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("docker/db/1_create_ddl.sql"),
                            "/docker-entrypoint-initdb.d/1_create_ddl.sql"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("docker/db/3_alter_table_v0_0_2.sql"),
                            "/docker-entrypoint-initdb.d/3_alter_table_v0_0_2.sql"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("docker/db/4_alter_table_tasks_v0_0_4.sql"),
                            "/docker-entrypoint-initdb.d/4_alter_table_tasks_v0_0_4.sql"
                    )
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("docker/db/5_create_table_v_0_0_4.sql"),
                            "/docker-entrypoint-initdb.d/5_create_table_v_0_0_4.sql"
                    );

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
