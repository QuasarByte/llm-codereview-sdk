package com.quasarbyte.llm.codereview.sdk.model.parameter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancingStrategyTest {

    @Test
    void loadBalancingStrategy_hasExpectedValues() {
        LoadBalancingStrategy[] values = LoadBalancingStrategy.values();
        
        assertEquals(2, values.length);
        assertEquals(LoadBalancingStrategy.ROUND_ROBIN, values[0]);
        assertEquals(LoadBalancingStrategy.RANDOM, values[1]);
    }

    @Test
    void loadBalancingStrategy_valueOf_worksCorrectly() {
        assertEquals(LoadBalancingStrategy.ROUND_ROBIN, LoadBalancingStrategy.valueOf("ROUND_ROBIN"));
        assertEquals(LoadBalancingStrategy.RANDOM, LoadBalancingStrategy.valueOf("RANDOM"));
    }

    @Test
    void parallelExecutionParameter_withLoadBalancingStrategy_worksCorrectly() {
        ParallelExecutionParameter parameter = new ParallelExecutionParameter()
                .setBatchSize(5)
                .setLoadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN);

        assertEquals(Integer.valueOf(5), parameter.getBatchSize());
        assertEquals(LoadBalancingStrategy.ROUND_ROBIN, parameter.getLoadBalancingStrategy());
    }

    @Test
    void parallelExecutionParameter_defaultLoadBalancingStrategy_isNull() {
        ParallelExecutionParameter parameter = new ParallelExecutionParameter()
                .setBatchSize(5);

        assertEquals(Integer.valueOf(5), parameter.getBatchSize());
        assertNull(parameter.getLoadBalancingStrategy());
    }

    @Test
    void parallelExecutionParameter_setLoadBalancingStrategy_allowsChaining() {
        ParallelExecutionParameter parameter = new ParallelExecutionParameter()
                .setBatchSize(10)
                .setLoadBalancingStrategy(LoadBalancingStrategy.RANDOM);

        assertEquals(Integer.valueOf(10), parameter.getBatchSize());
        assertEquals(LoadBalancingStrategy.RANDOM, parameter.getLoadBalancingStrategy());
    }
}
