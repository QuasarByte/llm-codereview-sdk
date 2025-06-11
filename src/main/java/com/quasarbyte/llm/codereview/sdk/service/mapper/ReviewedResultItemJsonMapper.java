package com.quasarbyte.llm.codereview.sdk.service.mapper;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItemJson;

public interface ReviewedResultItemJsonMapper {
    ReviewedResultItemJson toJson(ReviewedResultItem reviewedResultItem);

    ReviewedResultItem fromJson(ReviewedResultItemJson reviewedResultItemJson);
}
