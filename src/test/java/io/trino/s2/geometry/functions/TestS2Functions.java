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
                "s2_cell(-61.326853510565,0)"))
                .isEqualTo("b760000000000001");

        assertThat(assertions.expression(
                "s2_level(s2_cell(32.15091, 34.848075))"))
                .isEqualTo(30);

        assertThat(assertions.expression(
                "s2_level('f76')"))
                .isEqualTo(4);

        assertThat(assertions.expression(
                "s2_distance('151d48164', 32.15000, 34.848000)"))
                .isEqualTo(168.9677806132969);

        assertThat(assertions.expression(
                "s2_childs('14e64ad5')"))
                .isEqualTo(ImmutableList.of("14e64ad44", "14e64ad4c", "14e64ad54", "14e64ad5c"));

        assertThat(assertions.expression(
                "s2_polygon_cover('POLYGON((79.353781784841460000000000000 21.230046625568562000000000000,79.353781020772290000000000000 21.230050485350244000000000000,79.350476393767820000000000000 21.232411200843462000000000000,79.346512273403100000000000000 21.233700093104580000000000000,79.345482325776330000000000000 21.234339905743090000000000000,79.345332800121070000000000000 21.234425918233110000000000000,79.345332800121070000000000000 21.234425918233107000000000000,79.345305096687370000000000000 21.234441854236270000000000000,79.345218280237650000000000000 21.234543697986535000000000000,79.345571448126920000000000000 21.234129399285870000000000000,79.345966626609370000000000000 21.233683216103213000000000000,79.346984082534620000000000000 21.233037569985580000000000000,79.346984082534630000000000000 21.233037569985576000000000000,79.346559654340280000000000000 21.233306899014080000000000000,79.350279187296660000000000000 21.232063012839653000000000000,79.350902850694780000000000000 21.231617137600860000000000000,79.353781784841460000000000000 21.230046625568562000000000000))',18)"))
                .isEqualTo(ImmutableList.of("3bd4cb618c", "3bd4cb6191", "3bd4cb6199", "3bd4cb619b", "3bd4cb61a1", "3bd4cb61a3", "3bd4cb61f5", "3bd4cb620b", "3bd4cb620d", "3bd4cb620f", "3bd4cb6211", "3bd4cb6213", "3bd4cb6217", "3bd4cb6219", "3bd4cb621b", "3bd4cb621f", "3bd4cb6221", "3bd4cb626d", "3bd4cb6274", "3bd4cb6279", "3bd4cb627f", "3bd4cb6281", "3bd4cc82b3", "3bd4cc82b5", "3bd4cc82b7", "3bd4cc82c3", "3bd4cc82c5", "3bd4cc82c7", "3bd4cc82c9", "3bd4cc82cb", "3bd4cc82cf", "3bd4cc82dd", "3bd4cc9d43", "3bd4cc9d45", "3bd4cc9d47", "3bd4cc9d4c", "3bd4cc9d5b", "3bd4cc9d5d", "3bd4cc9d63", "3bd4cc9d65", "3bd4cc9d67", "3bd4cc9d69", "3bd4cc9d6f", "3bd4cc9d7c", "3bd4cc9d81", "3bd4cc9d87", "3bd4cc9d89"));
    }

}