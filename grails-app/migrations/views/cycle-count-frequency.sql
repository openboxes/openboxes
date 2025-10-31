create or replace view cycle_count_frequency as (
  SELECT abc_class, facility_id, frequency
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