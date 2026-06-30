CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.final_nodes_3d (
    id BIGINT PRIMARY KEY,
    node_type VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    geom geometry(PointZ, 4326) NOT NULL
);

CREATE TABLE public.final_edges_split_3d (
    id BIGINT PRIMARY KEY,
    original_edge_id BIGINT NOT NULL,
    highway VARCHAR(50) NOT NULL,
    source BIGINT NOT NULL,
    target BIGINT NOT NULL,
    cost DOUBLE PRECISION NOT NULL,
    geom geometry(LineStringZ, 4326) NOT NULL
);

INSERT INTO public.final_nodes_3d (id, node_type, description, geom) VALUES
    (1, 'intersection', NULL, ST_GeomFromText('POINT Z (0 0 0)', 4326)),
    (2, 'intersection', NULL, ST_GeomFromText('POINT Z (0.001 0 0)', 4326)),
    (3, 'intersection', NULL, ST_GeomFromText('POINT Z (0.002 0 0)', 4326)),
    (4, 'entrance', 'Test Building', ST_GeomFromText('POINT Z (0.003 0 0)', 4326));

INSERT INTO public.final_edges_split_3d (id, original_edge_id, highway, source, target, cost, geom) VALUES
    (
        1,
        1,
        'footway',
        1,
        2,
        ST_Length(ST_GeomFromText('LINESTRING Z (0 0 0, 0.001 0 0)', 4326)),
        ST_GeomFromText('LINESTRING Z (0 0 0, 0.001 0 0)', 4326)
    ),
    (
        2,
        2,
        'footway',
        2,
        3,
        ST_Length(ST_GeomFromText('LINESTRING Z (0.001 0 0, 0.002 0 0)', 4326)),
        ST_GeomFromText('LINESTRING Z (0.001 0 0, 0.002 0 0)', 4326)
    ),
    (
        3,
        3,
        'footway',
        3,
        4,
        ST_Length(ST_GeomFromText('LINESTRING Z (0.002 0 0, 0.003 0 0)', 4326)),
        ST_GeomFromText('LINESTRING Z (0.002 0 0, 0.003 0 0)', 4326)
    );
