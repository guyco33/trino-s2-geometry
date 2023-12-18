package io.trino.s2.geometry.functions;

import io.trino.sql.query.QueryAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(PER_CLASS)
@Execution(CONCURRENT)
public class TestS2Functions
{
    private QueryAssertions assertions;

    public TestS2Functions()
    {
        assertions = new QueryAssertions();
        assertions.addPlugin(new S2GeometryFunctionsPlugin());
    }

    @AfterAll
    public void teardown()
    {
        assertions.close();
    }

    @Test
    public void testS2Functions()
    {
        assertThat(assertions.expression(
                "s2_cell(32.15091, 34.848075)"))
                .isEqualTo("151d4816371ba05b");

        assertThat(assertions.expression(
                "s2_cell(32.15091, 34.848075, 15)"))
                .isEqualTo("151d48164");

        assertThat(assertions.expression(
                "s2_level(s2_cell(32.15091, 34.848075))"))
                .isEqualTo(30);

        assertThat(assertions.expression(
                "s2_level('f76')"))
                .isEqualTo(-1);

        assertThat(assertions.expression(
                "s2_childs('14e64ad5')"))
                .isEqualTo(ImmutableList.of("14e64ad44", "14e64ad4c", "14e64ad54", "14e64ad5c"));
    }

}