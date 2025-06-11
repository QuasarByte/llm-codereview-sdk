package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunnerFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiquibaseInnerRunnerFactoryTest {

    private LiquibaseInnerRunnerFactory liquibaseInnerRunnerFactory;

    @BeforeEach
    void setUp() {
        liquibaseInnerRunnerFactory = new LiquibaseInnerRunnerFactoryImpl();
    }

    @Test
    void shouldCreateServiceWithDefaultConfig() {
        // Given & When
        LiquibaseInnerRunner service = liquibaseInnerRunnerFactory.create();
        
        // Then
        assertNotNull(service);
        assertInstanceOf(LiquibaseInnerRunnerImpl.class, service);
    }

    @Test
    void shouldCreateServiceWithCustomResourceAccessor() {
        // Given
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        
        // When
        LiquibaseInnerRunner service = liquibaseInnerRunnerFactory.create(resourceAccessor);
        
        // Then
        assertNotNull(service);
        assertInstanceOf(LiquibaseInnerRunnerImpl.class, service);
    }

    @Test
    void shouldCreateServiceWithCustomConfig() {
        // Given
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath("custom/changelog.xml")
                .contexts("test")
                .build();
        
        // When
        LiquibaseInnerRunner service = liquibaseInnerRunnerFactory.create(config);
        
        // Then
        assertNotNull(service);
        assertInstanceOf(LiquibaseInnerRunnerImpl.class, service);
    }

    @Test
    void shouldCreateServiceWithCustomChangelogPath() {
        // Given
        String changelogPath = "custom/test-changelog.xml";
        
        // When
        LiquibaseInnerRunner service = liquibaseInnerRunnerFactory.createWithChangelogPath(changelogPath);
        
        // Then
        assertNotNull(service);
        assertInstanceOf(LiquibaseInnerRunnerImpl.class, service);
    }

    @Test
    void shouldCreateMultipleDistinctInstances() {
        // Given & When
        LiquibaseInnerRunner service1 = liquibaseInnerRunnerFactory.create();
        LiquibaseInnerRunner service2 = liquibaseInnerRunnerFactory.create();
        
        // Then
        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2);
    }
}
