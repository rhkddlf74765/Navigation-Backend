-- Historical/manual migration note.
-- Production runtime must not execute this file automatically.
-- final_nodes_3d graph-node IDs are not entrance IDs.
-- Preserve the source entrance relationship explicitly on entrance graph nodes.

ALTER TABLE public.final_nodes_3d
    ADD COLUMN IF NOT EXISTS entrance_id BIGINT NULL;

-- Optional only when final_nodes_3d is not dropped/recreated by the graph
-- rebuild pipeline. If the table is rebuilt from scratch, add the same
-- relationship after the CREATE TABLE statement instead.
--
-- ALTER TABLE public.final_nodes_3d
--     ADD CONSTRAINT final_nodes_3d_entrance_id_fk
--     FOREIGN KEY (entrance_id)
--     REFERENCES public.entrances(id)
--     NOT VALID;

-- Preferred sequence:
-- 1. Normalize raw_schemas_osm.entrance_3d.node_type to 'entrance'.
-- 2. Synchronize public.entrances from that SSOT, preserving public.entrances.id.
-- 3. Rebuild final_nodes_3d so each entrance graph node stores its source
--    public.entrances.id in final_nodes_3d.entrance_id.
--
-- Rebuild-time shape for entrance nodes:
--
-- INSERT INTO public.final_nodes_3d (
--     id,
--     node_type,
--     description,
--     entrance_id,
--     geom
-- )
-- SELECT
--     next_graph_node_id,
--     'entrance',
--     e.description,
--     e.id,
--     e.geom
-- FROM public.entrances e
-- WHERE LOWER(BTRIM(e.node_type)) = 'entrance';

-- Validation invariants after rebuild.
SELECT n.id AS graph_node_id
FROM public.final_nodes_3d n
WHERE LOWER(BTRIM(n.node_type)) = 'entrance'
  AND n.entrance_id IS NULL;

SELECT n.id AS graph_node_id,
       n.entrance_id
FROM public.final_nodes_3d n
LEFT JOIN public.entrances e
  ON e.id = n.entrance_id
WHERE n.entrance_id IS NOT NULL
  AND e.id IS NULL;

-- Temporary one-time backfill only if a full graph rebuild is not immediately
-- possible. Do not use geometry proximity as the long-term relationship.
--
-- WITH candidates AS (
--     SELECT DISTINCT ON (n.id)
--         n.id AS graph_node_id,
--         e.id AS entrance_id,
--         ST_Distance(
--             ST_Transform(n.geom, 5179),
--             ST_Transform(e.geom, 5179)
--         ) AS distance_meters
--     FROM public.final_nodes_3d n
--     JOIN public.entrances e
--       ON LOWER(BTRIM(n.node_type)) = 'entrance'
--      AND ST_DWithin(
--          ST_Transform(n.geom, 5179),
--          ST_Transform(e.geom, 5179),
--          0.5
--      )
--     ORDER BY n.id, distance_meters ASC, e.id ASC
-- )
-- UPDATE public.final_nodes_3d n
-- SET entrance_id = c.entrance_id
-- FROM candidates c
-- WHERE n.id = c.graph_node_id;
