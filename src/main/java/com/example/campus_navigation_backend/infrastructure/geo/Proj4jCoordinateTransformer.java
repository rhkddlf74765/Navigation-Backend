package com.example.campus_navigation_backend.infrastructure.geo;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.geo.CoordinateTransformer;
import com.example.campus_navigation_backend.domain.geo.GeoPoint;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Component;

@Component
public class Proj4jCoordinateTransformer
        implements CoordinateTransformer {

    private final CoordinateTransform toMetric;
    private final CoordinateTransform toWgs84;

    public Proj4jCoordinateTransformer(
            NavigationProperties properties
    ) {
        CRSFactory crsFactory =
                new CRSFactory();

        CoordinateTransformFactory transformFactory =
                new CoordinateTransformFactory();

        CoordinateReferenceSystem wgs84 =
                crsFactory.createFromName(
                        "EPSG:"
                                + properties.requestSrid()
                );

        CoordinateReferenceSystem metric =
                crsFactory.createFromName(
                        "EPSG:"
                                + properties.metricSrid()
                );

        this.toMetric =
                transformFactory.createTransform(
                        wgs84,
                        metric
                );

        this.toWgs84 =
                transformFactory.createTransform(
                        metric,
                        wgs84
                );
    }

    @Override
    public MetricPoint toMetric(
            GeoPoint point
    ) {
        ProjCoordinate source =
                new ProjCoordinate(
                        point.lon(),
                        point.lat()
                );

        ProjCoordinate target =
                new ProjCoordinate();

        toMetric.transform(
                source,
                target
        );

        return new MetricPoint(
                target.x,
                target.y,
                point.ele()
        );
    }

    @Override
    public GeoPoint toWgs84(
            MetricPoint point
    ) {
        ProjCoordinate source =
                new ProjCoordinate(
                        point.x(),
                        point.y()
                );

        ProjCoordinate target =
                new ProjCoordinate();

        toWgs84.transform(
                source,
                target
        );

        return new GeoPoint(
                target.x,
                target.y,
                point.z()
        );
    }
}
