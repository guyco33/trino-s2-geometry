package io.trino.s2.geometry.functions;

/**
 * Created by guycohen on 18/05/2017.
 */
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2RegionCoverer;
import io.trino.spi.block.Block;
import io.trino.spi.block.BlockBuilder;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.Description;
import io.trino.spi.function.SqlNullable;
import io.trino.spi.function.SqlType;
import io.airlift.slice.Slice;
import io.trino.spi.type.StandardTypes;
import java.util.List;
import java.util.ArrayList;

import static io.trino.spi.type.VarcharType.VARCHAR;
import static io.trino.spi.type.DoubleType.DOUBLE;
import static io.airlift.slice.Slices.utf8Slice;
import static java.lang.Math.toIntExact;

public class S2GeometryFunctions {

    private S2GeometryFunctions() {}

    @ScalarFunction("s2_cell")
    @Description("Returns cell token for latitude,longitude degrees and level")
    @SqlType(StandardTypes.VARCHAR)
    @SqlNullable
    public static Slice s2Cell(
            @SqlType(StandardTypes.DOUBLE ) double lat,
            @SqlType(StandardTypes.DOUBLE) double lon,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        if (level<0 || level>30) return null;
        return utf8Slice(S2CellId.fromLatLng(S2LatLng.fromDegrees(lat,lon)).parent(toIntExact(level)).toToken());
    }

    @ScalarFunction("s2_cell")
    @Description("Returns cell token for latitude,longitude degrees in level 30")
    @SqlType(StandardTypes.VARCHAR)
    @SqlNullable
    public static Slice s2Cell(
            @SqlType(StandardTypes.DOUBLE ) double lat,
            @SqlType(StandardTypes.DOUBLE) double lon)
    {
        return s2Cell(lat,lon,30);
    }

    @ScalarFunction("s2_parent")
    @Description("Returns cell token parent")
    @SqlType(StandardTypes.VARCHAR)
    @SqlNullable
    public static Slice s2Parent(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken)
    {
        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());
        return utf8Slice(cellid.parent().toToken());
    }

    @ScalarFunction("s2_level")
    @Description("Returns the level of cell token")
    @SqlType(StandardTypes.INTEGER)
    @SqlNullable
    public static Long s2Level(@SqlType(StandardTypes.VARCHAR) Slice celltoken) {
        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());
        return cellid.isValid() ? cellid.level() : -1L;
    }

    @ScalarFunction("s2_distance")
    @Description("Returns the distance in meter from cell token to point")
    @SqlType(StandardTypes.DOUBLE)
    @SqlNullable
    public static Double s2_distance(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken,
            @SqlType(StandardTypes.DOUBLE ) double lat,
            @SqlType(StandardTypes.DOUBLE) double lon)
    {
        return S2CellId.fromToken(celltoken.toStringUtf8()).toLatLng().getDistance(S2LatLng.fromDegrees(lat,lon)).radians() * 6371010.0;
    }

    @ScalarFunction("s2_centroid")
    @Description("Returns the lat,lon point of cell centre")
    @SqlType("array(double)")
    @SqlNullable
    public static Block s2_centroid(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken)
    {
        S2LatLng latlng = S2CellId.fromToken(celltoken.toStringUtf8()).toLatLng();
        BlockBuilder blockBuilder = DOUBLE.createBlockBuilder(null,2);
        DOUBLE.writeDouble(blockBuilder, latlng.latDegrees());
        DOUBLE.writeDouble(blockBuilder, latlng.lngDegrees());
        return blockBuilder.build();
    }

    @ScalarFunction("s2_neighbours")
    @Description("Returns cell token neighbours in a level")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2CellNeighbours(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        if (level<0 || level>30) return null;

        List<S2CellId> output = new ArrayList<S2CellId>();
        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());
        cellid.getAllNeighbors(toIntExact(level), output);
        return cellsArrayBlock(output);
    }

    @ScalarFunction("s2_neighbors")
    @Description("Returns cell token neighbors in a level")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2CellNeighbors(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        return s2CellNeighbours(celltoken, level);
    }

    @ScalarFunction("s2_childs")
    @Description("Returns cell token children")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2CellChilds(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken)
    {
        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());
        return cellsArrayBlock(S2Helper.getChilds(cellid));
    }

    @ScalarFunction("s2_radius_cover")
    @Description("Returns cell tokens in a meter radius for a specific level")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2CellCover(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken,
            @SqlType(StandardTypes.DOUBLE) double radius,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        if (level<0 || level>30) return null;

        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());
        S2Cap circle = S2Cap.fromAxisAngle(cellid.toLatLng().toPoint(), S1Angle.degrees(360 * radius/1000 / (2 * Math.PI * 6371.01)));
        ArrayList<S2CellId> output = new ArrayList<S2CellId>();
        S2RegionCoverer.getSimpleCovering(circle, cellid.toLatLng().toPoint(), toIntExact(level) ,output);
        return cellsArrayBlock(output);
    }

    @ScalarFunction("s2_parse_wkt")
    @Description("Returns s2 region from wkt string")
    @SqlType(StandardTypes.VARCHAR)
    @SqlNullable
    public static Slice s2ParseWkt(
            @SqlType(StandardTypes.VARCHAR ) Slice wktPolygon)
    {
        return utf8Slice(S2Helper.parseWktPolygon(wktPolygon.toStringUtf8()).toString());
    }

    @ScalarFunction("s2_polygon_cover")
    @Description("Returns cell tokens cover of wkt polygon")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2PolygonCover(
            @SqlType(StandardTypes.VARCHAR) Slice wktPolygon,
            @SqlType(StandardTypes.INTEGER) long min_level,
            @SqlType(StandardTypes.INTEGER) long max_level)
    {
        if (min_level<0 || min_level>30) return null;
        if (max_level<0 || max_level>30) return null;

        S2CellUnion cover = S2Helper.cover(S2Helper.parseWktPolygon(wktPolygon.toStringUtf8()),toIntExact(min_level),toIntExact(max_level));
        if (cover==null) return null;
        return cellsArrayBlock(cover.cellIds());
    }

    @ScalarFunction("s2_polygon_cover")
    @Description("Returns cell tokens cover of wkt polygon")
    @SqlType("array(varchar)")
    @SqlNullable
    public static Block s2PolygonCover(
            @SqlType(StandardTypes.VARCHAR) Slice wktPolygon,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        if (level<0 || level>30) return null;

        return s2PolygonCover(wktPolygon,level,level);
    }

    @ScalarFunction("s2_within")
    @Description("Returns TRUE if a cell token is in coverage of a wkt polygon")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Within(
            @SqlType(StandardTypes.VARCHAR) Slice celltoken,
            @SqlType(StandardTypes.VARCHAR) Slice wktPolygon,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        if (level<0 || level>30) return null;

        S2CellUnion cover = S2Helper.cover(S2Helper.parseWktPolygon(wktPolygon.toStringUtf8()), toIntExact(level));
        if (cover==null) return false;
        S2CellId cellid = S2CellId.fromToken(celltoken.toStringUtf8());

        return cover.contains(cellid);
    }

    @ScalarFunction("s2_contains")
    @Description("Returns TRUE if coverage of a wkt polygon contains a cell token")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Contains(
            @SqlType(StandardTypes.VARCHAR) Slice wktPolygon,
            @SqlType(StandardTypes.VARCHAR) Slice cellToken,
            @SqlType(StandardTypes.INTEGER) long level)
    {
        return s2Within(cellToken, wktPolygon, level);
    }

    @ScalarFunction("s2_within")
    @Description("Returns TRUE if a cell token is in coverage of cells list")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Within(
            @SqlType(StandardTypes.VARCHAR) Slice cellToken,
            @SqlType("array(varchar)") Block cellTokens)
    {
        try {
            ArrayList<S2CellId> cellids = new ArrayList<S2CellId>();
            for (int i = 0; i < cellTokens.getPositionCount(); i++) {
                cellids.add(S2CellId.fromToken(VARCHAR.getSlice(cellTokens, i).toStringUtf8()));
            }
            S2CellUnion cellUnion = new S2CellUnion();
            cellUnion.initFromCellIds(cellids);

            return cellUnion.contains(S2CellId.fromToken(cellToken.toStringUtf8()));
        } catch (Exception e) {return null;}
    }

    @ScalarFunction("s2_contains")
    @Description("Returns TRUE if coverage of cell tokens list contains a cell token ")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Contains(
            @SqlType("array(varchar)") Block cellTokens,
            @SqlType(StandardTypes.VARCHAR) Slice cellToken) {
        return s2Within(cellToken, cellTokens);
    }

    @ScalarFunction("s2_within")
    @Description("Returns TRUE if a cell token A is in coverage of cell token B")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Within(
            @SqlType(StandardTypes.VARCHAR) Slice cellTokenA,
            @SqlType(StandardTypes.VARCHAR) Slice cellTokenB) {
        try {
            return S2CellId.fromToken(cellTokenB.toStringUtf8()).contains(S2CellId.fromToken(cellTokenA.toStringUtf8()));
        }
        catch (Exception e) {return null;}
    }

    @ScalarFunction("s2_contains")
    @Description("Returns TRUE if coverage of cell token A contains cell token B")
    @SqlType(StandardTypes.BOOLEAN)
    @SqlNullable
    public static Boolean s2Contains(
            @SqlType(StandardTypes.VARCHAR) Slice cellTokenA,
            @SqlType(StandardTypes.VARCHAR) Slice cellTokenB) {
        return s2Within(cellTokenB,cellTokenA);
    }

    public static Block cellsArrayBlock(List<S2CellId> cells) {
        BlockBuilder blockBuilder = VARCHAR.createBlockBuilder(null, cells.size(), 16);
        for (S2CellId cell: cells)
            VARCHAR.writeSlice(blockBuilder, utf8Slice(cell.toToken()));
        return blockBuilder.build();

    }
}
