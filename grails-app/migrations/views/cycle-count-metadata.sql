CREATE OR REPLACE VIEW cycle_count_metadata AS
(
SELECT CRC32(CONCAT(inventory_id, product_id))                                                                        as id,
       cycle_count_metadata_tmp.inventory_id                                                                          as inventory_id,
       cycle_count_metadata_tmp.product_id                                                                            as product_id,
       cycle_count_metadata_tmp.date_counted                                                                          as date_counted,
       cycle_count_metadata_tmp.abc_class                                                                             as abc_class,
       cycle_count_frequency.frequency                                                                                as frequency,
       datediff(now(), date_counted)                                                                                  as days_since_last_count,
       date_add(cycle_count_metadata_tmp.date_counted, INTERVAL cycle_count_frequency.frequency
                DAY)                                                                                                  as date_expected,
       datediff(date_add(cycle_count_metadata_tmp.date_counted, INTERVAL cycle_count_frequency.frequency DAY),
                now())                                                                                                as days_until_next_count
FROM (SELECT product_count_history.inventory_id as inventory_id,
             product_count_history.product_id   as product_id,
             product_count_history.date_counted as date_counted,
             -- FIXME Used to clean data, this should not be used in the actual solution
             CASE
                 WHEN abc_class = 'A' THEN 'A'
                 WHEN abc_class = 'B' THEN 'B'
                 WHEN abc_class = 'C' THEN 'C'
                 ELSE 'DEFAULT' END             as abc_class
      FROM product_count_history
               LEFT OUTER JOIN product_classification
                               ON product_count_history.inventory_id = product_classification.inventory_id
                                   AND product_count_history.product_id =
                                       product_classification.product_id) as cycle_count_metadata_tmp
         LEFT OUTER JOIN cycle_count_frequency ON cycle_count_frequency.abc_class = cycle_count_metadata_tmp.abc_class
    );