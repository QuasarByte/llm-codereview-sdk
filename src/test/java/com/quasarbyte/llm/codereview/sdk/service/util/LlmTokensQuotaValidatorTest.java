package com.quasarbyte.llm.codereview.sdk.service.util;

import com.quasarbyte.llm.codereview.sdk.exception.LlmTokensQuotaException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmTokensQuota;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LlmTokensQuotaValidatorTest {

    @Test
    public void testValidateTokenUsage_NoQuota_ShouldNotThrow() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateTokenUsage(usage, null));
    }

    @Test
    public void testValidateTokenUsage_NoUsage_ShouldNotThrow() {
        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateTokenUsage(null, quota));
    }

    @Test
    public void testValidateTokenUsage_WithinQuota_ShouldNotThrow() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(100L)
                .setTotalTokens(150L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
    }

    @Test
    public void testValidateTokenUsage_CompletionTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(150L)
                .setPromptTokens(50L)
                .setTotalTokens(200L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
        assertTrue(exception.getMessage().contains("Completion tokens quota exceeded"));
        assertTrue(exception.getMessage().contains("Used: 150"));
        assertTrue(exception.getMessage().contains("Quota: 100"));
    }

    @Test
    public void testValidateTokenUsage_PromptTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(250L)
                .setTotalTokens(300L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(400L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
        assertTrue(exception.getMessage().contains("Prompt tokens quota exceeded"));
        assertTrue(exception.getMessage().contains("Used: 250"));
        assertTrue(exception.getMessage().contains("Quota: 200"));
    }

    @Test
    public void testValidateTokenUsage_TotalTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(100L)
                .setTotalTokens(350L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
        assertTrue(exception.getMessage().contains("Total tokens quota exceeded"));
        assertTrue(exception.getMessage().contains("Used: 350"));
        assertTrue(exception.getMessage().contains("Quota: 300"));
    }

    @Test
    public void testValidateTokenUsage_PartialQuotaLimits_ShouldOnlyValidateSet() {
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(150L)  // This exceeds quota
                .setPromptTokens(50L)       // No quota set
                .setTotalTokens(200L);      // No quota set

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L); // Only completion tokens quota set

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
        assertTrue(exception.getMessage().contains("Completion tokens quota exceeded"));
    }

    @Test
    public void testValidateEstimatedTokenUsage_NoQuota_ShouldNotThrow() {
        ReviewedCompletionUsage estimatedUsage = new ReviewedCompletionUsage()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(estimatedUsage, null));
    }

    @Test
    public void testValidateEstimatedTokenUsage_NoUsage_ShouldNotThrow() {
        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(null, quota));
    }

    @Test
    public void testValidateEstimatedTokenUsage_WithinQuota_ShouldNotThrow() {
        ReviewedCompletionUsage estimatedUsage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(100L)
                .setTotalTokens(150L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(estimatedUsage, quota));
    }

    @Test
    public void testValidateEstimatedTokenUsage_EstimatedCompletionTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage estimatedUsage = new ReviewedCompletionUsage()
                .setCompletionTokens(150L)
                .setPromptTokens(50L)
                .setTotalTokens(200L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(estimatedUsage, quota));
        assertTrue(exception.getMessage().contains("Estimated completion tokens would exceed quota"));
        assertTrue(exception.getMessage().contains("Estimated: 150"));
        assertTrue(exception.getMessage().contains("Quota: 100"));
    }

    @Test
    public void testValidateEstimatedTokenUsage_EstimatedPromptTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage estimatedUsage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(250L)
                .setTotalTokens(300L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(400L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(estimatedUsage, quota));
        assertTrue(exception.getMessage().contains("Estimated prompt tokens would exceed quota"));
        assertTrue(exception.getMessage().contains("Estimated: 250"));
        assertTrue(exception.getMessage().contains("Quota: 200"));
    }

    @Test
    public void testValidateEstimatedTokenUsage_EstimatedTotalTokensExceeded_ShouldThrow() {
        ReviewedCompletionUsage estimatedUsage = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(100L)
                .setTotalTokens(350L);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        LlmTokensQuotaException exception = assertThrows(LlmTokensQuotaException.class, 
                () -> LlmTokensQuotaValidator.validateEstimatedTokenUsage(estimatedUsage, quota));
        assertTrue(exception.getMessage().contains("Estimated total tokens would exceed quota"));
        assertTrue(exception.getMessage().contains("Estimated: 350"));
        assertTrue(exception.getMessage().contains("Quota: 300"));
    }

    @Test
    public void testValidateTokenUsage_NullSafetyInQuotaGetters() {
        // Test with usage having null values
        ReviewedCompletionUsage usage = new ReviewedCompletionUsage()
                .setCompletionTokens(null)
                .setPromptTokens(null)
                .setTotalTokens(null);

        LlmTokensQuota quota = new LlmTokensQuota()
                .setCompletionTokens(100L)
                .setPromptTokens(200L)
                .setTotalTokens(300L);

        // Should not throw since usage values are null
        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateTokenUsage(usage, quota));
        
        // Test with quota having null values
        ReviewedCompletionUsage usage2 = new ReviewedCompletionUsage()
                .setCompletionTokens(50L)
                .setPromptTokens(100L)
                .setTotalTokens(150L);

        LlmTokensQuota quota2 = new LlmTokensQuota()
                .setCompletionTokens(null)
                .setPromptTokens(null)
                .setTotalTokens(null);

        // Should not throw since quota values are null
        assertDoesNotThrow(() -> LlmTokensQuotaValidator.validateTokenUsage(usage2, quota2));
    }
}
