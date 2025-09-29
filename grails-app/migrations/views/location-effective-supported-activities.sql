CREATE OR REPLACE VIEW location_effective_supported_activities AS
-- If location has supported activities use them
SELECT la.location_id, la.supported_activities_string
FROM location_supported_activities la
WHERE EXISTS (
    SELECT 1 FROM location_supported_activities la2
    WHERE la2.location_id = la.location_id
)
UNION ALL
-- Otherwise use the supported activities defined on the location type
SELECT il.id AS location_id, lta.supported_activities_string
FROM location il
         JOIN location_type_supported_activities lta
              ON lta.location_type_id = il.location_type_id
WHERE NOT EXISTS (
    SELECT 1 FROM location_supported_activities la
    WHERE la.location_id = il.id
);