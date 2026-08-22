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

CREATE SCHEMA IF NOT EXISTS log;

CREATE TABLE log.route_session_log (
    route_session_id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    status varchar(30) NOT NULL,
    end_reason varchar(30),
    requested_at timestamptz NOT NULL DEFAULT now(),
    responded_at timestamptz,
    navigation_started_at timestamptz,
    ended_at timestamptz,
    error_message text
);

CREATE TABLE log.route_endpoint_log (
    id bigserial PRIMARY KEY,
    route_session_id uuid NOT NULL,
    role varchar(20) NOT NULL,
    endpoint_type varchar(30) NOT NULL,
    lon double precision,
    lat double precision,
    ele double precision,
    building_name text,
    CONSTRAINT route_endpoint_unique_role UNIQUE (route_session_id, role),
    CONSTRAINT route_endpoint_session_fk
        FOREIGN KEY (route_session_id)
        REFERENCES log.route_session_log(route_session_id)
);

CREATE TABLE log.route_result_log (
    route_session_id uuid PRIMARY KEY,
    destination_building_name text,
    selected_entrance_id bigint,
    total_distance_meters double precision NOT NULL,
    total_cost double precision NOT NULL,
    approach_distance_meters double precision NOT NULL,
    approach_cost double precision NOT NULL,
    graph_distance_meters double precision NOT NULL,
    graph_cost double precision NOT NULL,
    path jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT route_result_session_fk
        FOREIGN KEY (route_session_id)
        REFERENCES log.route_session_log(route_session_id)
);

CREATE TABLE log.route_event_log (
    id bigserial PRIMARY KEY,
    route_session_id uuid NOT NULL,
    user_id uuid NOT NULL,
    event_type varchar(50) NOT NULL,
    event_message text,
    lon double precision,
    lat double precision,
    ele double precision,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT route_event_session_fk
        FOREIGN KEY (route_session_id)
        REFERENCES log.route_session_log(route_session_id)
);

CREATE TABLE log.location_sample_log (
    id bigserial PRIMARY KEY,
    user_id uuid NOT NULL,
    route_session_id uuid,
    gps_lon double precision NOT NULL,
    gps_lat double precision NOT NULL,
    gps_ele double precision,
    actual_lon double precision,
    actual_lat double precision,
    actual_ele double precision,
    error_meters double precision,
    recorded_at timestamptz,
    received_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT location_sample_route_session_fk
        FOREIGN KEY (route_session_id)
        REFERENCES log.route_session_log(route_session_id)
);

CREATE TABLE log.route_arrival_log (
    route_session_id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    arrived_at timestamptz NOT NULL,
    lon double precision,
    lat double precision,
    ele double precision,
    CONSTRAINT route_arrival_session_fk
        FOREIGN KEY (route_session_id)
        REFERENCES log.route_session_log(route_session_id)
);
