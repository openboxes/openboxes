CREATE OR REPLACE VIEW cycle_count_candidate AS
(
SELECT *,
       CASE
           -- Calculate the sort order priority for each cycle count candidate
           -- 0 = Has never been counted or overdue for a count
           -- 1 = Has been counted within frequency
           -- 2 = Other unexpected scenarios
           WHEN negative_item_count > 0 THEN 0
           WHEN date_last_count IS NULL THEN 1
           WHEN days_until_next_count < 0 THEN 1
           WHEN days_until_next_count >= 0 THEN 2
           ELSE 3
       END as sort_order
FROM cycle_count_session
ORDER BY sort_order,
         abc_class IS NULL asc,
         abc_class asc,
         days_until_next_count
    );