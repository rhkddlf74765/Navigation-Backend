CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SCHEMA IF NOT EXISTS spatial;
CREATE SCHEMA IF NOT EXISTS routing;
CREATE SCHEMA IF NOT EXISTS log;


CREATE TABLE spatial.buildings (
                                   id BIGINT PRIMARY KEY,
                                   source_id TEXT UNIQUE,
                                   name TEXT,
                                   description TEXT,

                                   geom geometry(MultiPolygon, 5186) NOT NULL,

                                   is_operational BOOLEAN NOT NULL
                                                                   DEFAULT TRUE,

                                   indoor_navigation_status VARCHAR(20)
                                                          NOT NULL DEFAULT 'NONE'
);


CREATE TABLE spatial.entrances (
                                   id BIGINT PRIMARY KEY,

                                   building_id BIGINT NOT NULL
                                       REFERENCES spatial.buildings(id),

                                   description TEXT,

                                   entrance_type VARCHAR(20)
                                                      NOT NULL DEFAULT 'GENERAL',

                                   geom geometry(PointZ, 5186)
        NOT NULL,

                                   is_operational BOOLEAN NOT NULL
                                                               DEFAULT TRUE
);


CREATE TABLE spatial.navigation_segments (
                                             id BIGINT PRIMARY KEY,

                                             scope VARCHAR(20) NOT NULL,

                                             movement_type VARCHAR(30)
                                                               NOT NULL,

                                             direction VARCHAR(20)
                                                               NOT NULL DEFAULT 'BOTH',

                                             geom geometry(LineStringZ, 5186)
        NOT NULL,

                                             is_enabled BOOLEAN NOT NULL
                                                                        DEFAULT TRUE
);


CREATE TABLE routing.graph_versions (
                                        id BIGSERIAL PRIMARY KEY,

                                        status VARCHAR(20) NOT NULL,

                                        created_at TIMESTAMPTZ
                                                           NOT NULL DEFAULT now(),

                                        validated_at TIMESTAMPTZ,

                                        activated_at TIMESTAMPTZ
);


CREATE TABLE routing.graph_nodes (
                                     graph_version_id BIGINT NOT NULL
                                         REFERENCES routing.graph_versions(id),

                                     id BIGINT NOT NULL,

                                     node_type VARCHAR(30) NOT NULL,

                                     scope VARCHAR(20) NOT NULL,

                                     entrance_id BIGINT
                                         REFERENCES spatial.entrances(id),

                                     geom geometry(PointZ, 5186)
        NOT NULL,

                                     PRIMARY KEY (
                                                  graph_version_id,
                                                  id
                                         )
);


CREATE TABLE routing.graph_edges (
                                     graph_version_id BIGINT NOT NULL,

                                     id BIGINT NOT NULL,

                                     source_node_id BIGINT NOT NULL,
                                     target_node_id BIGINT NOT NULL,

                                     source_segment_id BIGINT
                                         REFERENCES spatial.navigation_segments(id),

                                     movement_type VARCHAR(30) NOT NULL,

                                     direction VARCHAR(20)
                                         NOT NULL DEFAULT 'BOTH',

                                     geom geometry(LineStringZ, 5186)
        NOT NULL,

                                     distance_m DOUBLE PRECISION
                                         NOT NULL,

                                     is_enabled BOOLEAN NOT NULL
                                                  DEFAULT TRUE,

                                     PRIMARY KEY (
                                                  graph_version_id,
                                                  id
                                         ),

                                     FOREIGN KEY (
                                                  graph_version_id,
                                                  source_node_id
                                         )
                                         REFERENCES routing.graph_nodes(
                                                                        graph_version_id,
                                                                        id
                                             ),

                                     FOREIGN KEY (
                                                  graph_version_id,
                                                  target_node_id
                                         )
                                         REFERENCES routing.graph_nodes(
                                                                        graph_version_id,
                                                                        id
                                             )
);