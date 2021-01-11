package io.trino.s2.geometry.functions;

import com.google.common.geometry.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
            for (String item: items) {
                String[] nums = item.trim().split("(\\s)+");
                points.add(S2LatLng.fromDegrees(Double.parseDouble(nums[1]), Double.parseDouble(nums[0])).toPoint());
            }
            S2Loop loop = new S2Loop(points);
            S2PolygonBuilder polyBuilder = new S2PolygonBuilder();
            polyBuilder.addLoop(loop);
            return polyBuilder.assemblePolygon();
        }
        catch (Exception e) {
            return new S2Polygon();
        }
    }

    public static S2CellUnion cover(S2Polygon polygon, int minLevel, int maxLevel) {
        if (polygon == null || polygon.numLoops()==0) return null;
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(minLevel);
        coverer.setMaxLevel(maxLevel);
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
