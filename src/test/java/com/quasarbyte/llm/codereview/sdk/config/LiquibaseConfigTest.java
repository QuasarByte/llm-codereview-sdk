package com.quasarbyte.llm.codereview.sdk.config;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiquibaseConfigTest {

    @Test
    void shouldCreateConfigWithDefaultValues() {
        // Given & When
        LiquibaseConfig config = LiquibaseConfig.builder().build();
        
        // Then
        assertEquals(LiquibaseConfig.DEFAULT_CHANGELOG_PATH, config.getChangeLogPath());
        assertNull(config.getContexts());
        assertNull(config.getLabels());
        assertNull(config.getResourceAccessor());
    }

    @Test
    void shouldCreateConfigWithCustomValues() {
        // Given
        String customPath = "custom/changelog.xml";
        String contexts = "test,development";
        String labels = "version1,hotfix";
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        
        // When
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath(customPath)
                .contexts(contexts)
                .labels(labels)
                .resourceAccessor(resourceAccessor)
                .build();
        
        // Then
        assertEquals(customPath, config.getChangeLogPath());
        assertEquals(contexts, config.getContexts());
        assertEquals(labels, config.getLabels());
        assertEquals(resourceAccessor, config.getResourceAccessor());
    }

    @Test
    void shouldUseDefaultChangelogPathWhenNull() {
        // Given & When
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath(null)
                .build();
        
        // Then
        assertEquals(LiquibaseConfig.DEFAULT_CHANGELOG_PATH, config.getChangeLogPath());
    }

    @Test
    void shouldCreateBuilderMultipleTimes() {
        // Given & When
        LiquibaseConfig config1 = LiquibaseConfig.builder()
                .changeLogPath("path1.xml")
                .build();
        
        LiquibaseConfig config2 = LiquibaseConfig.builder()
                .changeLogPath("path2.xml")
                .contexts("prod")
                .build();
        
        // Then
        assertEquals("path1.xml", config1.getChangeLogPath());
        assertNull(config1.getContexts());
        
        assertEquals("path2.xml", config2.getChangeLogPath());
        assertEquals("prod", config2.getContexts());
    }

    @Test
    void shouldSupportBuilderChaining() {
        // Given & When
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath("test.xml")
                .contexts("test")
                .labels("v1")
                .resourceAccessor(new ClassLoaderResourceAccessor())
                .build();
        
        // Then
        assertNotNull(config);
        assertEquals("test.xml", config.getChangeLogPath());
        assertEquals("test", config.getContexts());
        assertEquals("v1", config.getLabels());
        assertNotNull(config.getResourceAccessor());
    }
}
