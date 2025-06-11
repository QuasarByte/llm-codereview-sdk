package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.sdk.repository.*;
import com.quasarbyte.llm.codereview.sdk.repository.impl.FileRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.PromptRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.ResolvedFileRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.RuleRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.*;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;
import com.quasarbyte.llm.codereview.sdk.service.mapper.impl.ReviewPromptJsonMapperFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewPromptCombinerFactoryImpl implements ReviewPromptCombinerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPromptCombinerFactoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ReviewRunContext reviewRunContext;

    public ReviewPromptCombinerFactoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate, ObjectMapper objectMapper, ReviewRunContext reviewRunContext) {
        this.dbPojoJsonConvertor = dbPojoJsonConvertor;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.reviewRunContext = reviewRunContext;
    }

    @Override
    public ReviewPromptCombiner create() {
        logger.debug("Instantiating ReviewPromptCombiner");

        FileRepositoryFactory fileRepositoryFactory = new FileRepositoryFactoryImpl(jdbcTemplate);
        PromptRepositoryFactory promptRepositoryFactory = new PromptRepositoryFactoryImpl(dbPojoJsonConvertor, jdbcTemplate);
        ResolvedFileRepositoryFactory resolvedFileRepositoryFactory = new ResolvedFileRepositoryFactoryImpl(jdbcTemplate);
        RuleRepositoryFactory ruleRepositoryFactory = new RuleRepositoryFactoryImpl(jdbcTemplate);
        RuleRepository ruleRepository = ruleRepositoryFactory.create();

        RuleServiceFactory ruleServiceFactory = new RuleServiceFactoryImpl(ruleRepository);

        RuleService ruleService = ruleServiceFactory.create();

        SourceFileReader sourceFileReader = new SourceFileReaderFactoryImpl().create();
        SourceFileServiceFactory sourceFileServiceFactory = new SourceFileServiceFactoryImpl(sourceFileReader);

        ReviewPromptJsonMapper reviewPromptJsonMapper = new ReviewPromptJsonMapperFactoryImpl().create();

        ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper = new ResolvedFilePathToPromptMapperImpl(
                fileRepositoryFactory.create(),
                objectMapper,
                promptRepositoryFactory.create(),
                resolvedFileRepositoryFactory.create(),
                reviewPromptJsonMapper,
                reviewRunContext,
                ruleService,
                sourceFileServiceFactory.create()
        );
        return new ReviewPromptCombiner(resolvedFilePathToPromptMapper);
    }
}
