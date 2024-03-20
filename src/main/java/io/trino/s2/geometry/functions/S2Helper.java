package io.trino.s2.geometry.functions;

import com.google.common.geometry.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.trino.spi.StandardErrorCode;
import io.trino.spi.TrinoException;

/**
 * Created by guycohen on 14/06/2017.
 */
public class S2Helper {

    public static S2Polygon parseWktPolygon(String polygon) {
        String start_pattern = "^(\\s)*[Pp][Oo][Ll][Yy][Gg][Oo][Nn](\\s)*[(](\\s)*[(]";
        String end_pattern = "[)](\\s)*[)](\\s)*$";

        try {
            if (!Pattern.matches(start_pattern+"[0-9.,\\-\\s\\t]+"+end_pattern, polygon)) {
                return new S2Polygon();
            }

            ArrayList<S2Point> points = new ArrayList<S2Point>();
            String[] items = polygon.replaceAll(start_pattern,"").replaceAll(end_pattern,"").split(",");

            // Remove the last vertex from valid polygon string
            int i = items.length;
            for (String item: items) {
                if (--i == 0) {
                    continue;
                }
                String[] nums = item.trim().split("(\\s)+");
                points.add(S2LatLng.fromDegrees(Double.parseDouble(nums[1]), Double.parseDouble(nums[0])).toPoint());
            }

            S2Loop loop = new S2Loop(points);
            // Normalize the loop and check if direction is CCW (Implicit)
            if (!loop.isNormalized()) {
                loop.normalize();
            }

            S2PolygonBuilder polyBuilder = new S2PolygonBuilder();
            polyBuilder.addLoop(loop);

            if (!loop.isValid())
            { S2Error error = new S2Error();
                loop.findValidationError(error);
                throw new TrinoException(StandardErrorCode.GENERIC_INTERNAL_ERROR,error.text() + " for " + polygon);
            }

            return polyBuilder.assemblePolygon();
        }
        catch (Exception e) {
            return new S2Polygon();
        }
    }

    public static S2CellUnion cover(S2Polygon polygon, int minLevel, int maxLevel) {
        if (polygon == null || polygon.numLoops()==0) return null;
        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMinLevel(minLevel)
                .setMaxLevel(maxLevel)
                .build();
        return coverer.getCovering(polygon);
    }

    public static S2CellUnion cover(S2Polygon polygon, int level) {
        return cover(polygon,level,level);
    }

    public static List<S2CellId> getChilds(S2CellId cellid) {
        List<S2CellId> childs = new ArrayList<S2CellId>();
        for (S2CellId c = cellid.childBegin(); !c.equals(cellid.childEnd()); c = c.next())
            childs.add(c);
        return childs;
    }
}
