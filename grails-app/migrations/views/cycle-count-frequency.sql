create or replace view cycle_count_frequency as (
  SELECT *
  FROM (
      select 'A' as abc_class, NULL as facility_id, 90 as frequency
      union all
      select 'B' as abc_class, NULL as facility_id, 180 as frequency
      union all
      select 'C' as abc_class, NULL as facility_id, 365 as frequency
      union all
      -- We cannot use NULL here because we cannot JOIN on a NULL value.
      select 'DEFAULT' as abc_class, NULL as facility_id, 365 as frequency
  ) cycle_count_frequency
);
# -- FIXME Move this to a proper database migration if it actually helps with performance.
# DROP VIEW IF EXISTS cycle_count_frequency;
# DROP TABLE IF EXISTS cycle_count_frequency;
# CREATE TEMPORARY TABLE cycle_count_frequency (
#     id INT AUTO_INCREMENT PRIMARY KEY,
#     abc_class VARCHAR(50),  -- e.g., 'A', 'B', 'C'
#     frequency INT NOT NULL,        -- Count frequency in days
#     INDEX abc_class_idx (abc_class)
# );
# INSERT INTO cycle_count_frequency (abc_class, frequency) VALUES
#   ('A', 90),
#   ('B', 180),
#   ('C', 365),
#   ('DEFAULT', 365);