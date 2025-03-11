CREATE OR REPLACE VIEW cycle_count_candidate AS
(
SELECT *,
       CASE
           -- Calculate the sort order priority for each cycle count candidate
           -- 0 = Has never been counted or overdue for a count
           -- 1 = Has been counted within frequency
           -- 2 = Other unexpected scenarios
           WHEN date_last_count IS NULL THEN 0
           WHEN days_until_next_count < 0 THEN 0
           WHEN days_until_next_count >= 0 THEN 1
           ELSE 2
           END as count_priority_sort_order
FROM cycle_count_session
ORDER BY count_priority_sort_order,
         abc_class IS NULL asc,
         abc_class asc,
         days_until_next_count
    );