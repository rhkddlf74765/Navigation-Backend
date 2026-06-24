CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.final_edges_3d (
    id BIGINT PRIMARY KEY,
    geom geometry(LineStringZ, 4326) NOT NULL,
    highway VARCHAR(50) NOT NULL
);

CREATE TABLE public.entrances (
    id BIGINT PRIMARY KEY,
    geom geometry(PointZ, 4326) NOT NULL,
    description VARCHAR(100) NOT NULL,
    node_type VARCHAR(50) NOT NULL
);

CREATE TABLE public.intersections (
    id BIGINT PRIMARY KEY,
    geom geometry(PointZ, 4326) NOT NULL
);

INSERT INTO public.final_edges_3d (id, geom, highway) VALUES
    (1, ST_GeomFromText('LINESTRING Z (0 0 0, 0.001 0 0)', 4326), 'footway'),
    (2, ST_GeomFromText('LINESTRING Z (0.001 0 0, 0.002 0 0)', 4326), 'footway'),
    (3, ST_GeomFromText('LINESTRING Z (0.002 0 0, 0.003 0 0)', 4326), 'footway');

INSERT INTO public.entrances (id, geom, description, node_type) VALUES
    (100, ST_GeomFromText('POINT Z (0.003 0 0)', 4326), 'Test Building', 'ENTRANCE');

INSERT INTO public.intersections (id, geom) VALUES
    (10, ST_GeomFromText('POINT Z (0.001 0 0)', 4326)),
    (11, ST_GeomFromText('POINT Z (0.002 0 0)', 4326));
