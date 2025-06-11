package com.quasarbyte.llm.codereview.sdk.service.util;

import com.quasarbyte.llm.codereview.sdk.exception.LlmTokensQuotaException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmTokensQuota;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LlmTokensQuotaValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(LlmTokensQuotaValidator.class);

    /**
     * Validates token usage against the configured quota limits.
     * Throws LlmTokensQuotaException if any quota is exceeded.
     * 
     * @param usage The actual token usage from LLM response
     * @param quota The configured token quota limits
     * @throws LlmTokensQuotaException if any quota limit is exceeded
     */
    public static void validateTokenUsage(ReviewedCompletionUsage usage, LlmTokensQuota quota) {
        if (quota == null) {
            logger.debug("No LlmTokensQuota configured, skipping validation");
            return;
        }

        if (usage == null) {
            logger.debug("No token usage data available, skipping validation");
            return;
        }

        // Safely get values with null protection
        Long usageCompletionTokens = usage.getCompletionTokens();
        Long usagePromptTokens = usage.getPromptTokens();
        Long usageTotalTokens = usage.getTotalTokens();
        Long quotaCompletionTokens = quota.getCompletionTokens();
        Long quotaPromptTokens = quota.getPromptTokens();
        Long quotaTotalTokens = quota.getTotalTokens();

        logger.debug("Validating token usage: completion={}, prompt={}, total={} against quota: completion={}, prompt={}, total={}", 
                usageCompletionTokens, usagePromptTokens, usageTotalTokens,
                quotaCompletionTokens, quotaPromptTokens, quotaTotalTokens);

        // Check completion tokens quota
        if (quotaCompletionTokens != null && usageCompletionTokens != null) {
            if (usageCompletionTokens > quotaCompletionTokens) {
                String message = String.format(
                    "Completion tokens quota exceeded. Used: %d, Quota: %d", 
                    usageCompletionTokens, quotaCompletionTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        // Check prompt tokens quota
        if (quotaPromptTokens != null && usagePromptTokens != null) {
            if (usagePromptTokens > quotaPromptTokens) {
                String message = String.format(
                    "Prompt tokens quota exceeded. Used: %d, Quota: %d", 
                    usagePromptTokens, quotaPromptTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        // Check total tokens quota
        if (quotaTotalTokens != null && usageTotalTokens != null) {
            if (usageTotalTokens > quotaTotalTokens) {
                String message = String.format(
                    "Total tokens quota exceeded. Used: %d, Quota: %d", 
                    usageTotalTokens, quotaTotalTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        logger.debug("Token usage validation passed");
    }

    /**
     * Validates if the estimated token usage would exceed quota limits.
     * This method can be used for pre-flight validation before making LLM calls.
     * 
     * @param estimatedUsage The estimated token usage before LLM call
     * @param quota The configured token quota limits
     * @throws LlmTokensQuotaException if estimated usage would exceed any quota limit
     */
    public static void validateEstimatedTokenUsage(ReviewedCompletionUsage estimatedUsage, LlmTokensQuota quota) {
        if (quota == null) {
            logger.debug("No LlmTokensQuota configured, skipping estimated validation");
            return;
        }

        if (estimatedUsage == null) {
            logger.debug("No estimated token usage data available, skipping validation");
            return;
        }

        // Safely get values with null protection
        Long estimatedCompletionTokens = estimatedUsage.getCompletionTokens();
        Long estimatedPromptTokens = estimatedUsage.getPromptTokens();
        Long estimatedTotalTokens = estimatedUsage.getTotalTokens();
        Long quotaCompletionTokens = quota.getCompletionTokens();
        Long quotaPromptTokens = quota.getPromptTokens();
        Long quotaTotalTokens = quota.getTotalTokens();

        logger.debug("Validating estimated token usage: completion={}, prompt={}, total={} against quota: completion={}, prompt={}, total={}", 
                estimatedCompletionTokens, estimatedPromptTokens, estimatedTotalTokens,
                quotaCompletionTokens, quotaPromptTokens, quotaTotalTokens);

        // Check estimated completion tokens against quota
        if (quotaCompletionTokens != null && estimatedCompletionTokens != null) {
            if (estimatedCompletionTokens > quotaCompletionTokens) {
                String message = String.format(
                    "Estimated completion tokens would exceed quota. Estimated: %d, Quota: %d", 
                    estimatedCompletionTokens, quotaCompletionTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        // Check estimated prompt tokens against quota
        if (quotaPromptTokens != null && estimatedPromptTokens != null) {
            if (estimatedPromptTokens > quotaPromptTokens) {
                String message = String.format(
                    "Estimated prompt tokens would exceed quota. Estimated: %d, Quota: %d", 
                    estimatedPromptTokens, quotaPromptTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        // Check estimated total tokens against quota
        if (quotaTotalTokens != null && estimatedTotalTokens != null) {
            if (estimatedTotalTokens > quotaTotalTokens) {
                String message = String.format(
                    "Estimated total tokens would exceed quota. Estimated: %d, Quota: %d", 
                    estimatedTotalTokens, quotaTotalTokens);
                logger.error(message);
                throw new LlmTokensQuotaException(message);
            }
        }

        logger.debug("Estimated token usage validation passed");
    }
}
