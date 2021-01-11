/**
 * Created by guycohen on 18/05/2017.
 */
package io.trino.s2.geometry.functions;

import io.trino.spi.Plugin;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class S2GeometryFunctionsPlugin
        implements Plugin
{
    @Override
    public Set<Class<?>> getFunctions()
    {
        return ImmutableSet.<Class<?>>builder()
                .add(S2GeometryFunctions.class)
                .build();
    }
}
