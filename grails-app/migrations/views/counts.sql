CREATE OR REPLACE VIEW counts AS
    -- First, just get all of the baseline transactions and adjustment transactions separately, so we can join them later
    WITH inventory_baseline_candidates AS (
        SELECT t.id AS transaction_id,
               ii.id AS inventory_item_id,
               t.transaction_date,
               t.inventory_id
        FROM transaction t
        JOIN transaction_entry te ON te.transaction_id = t.id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        WHERE t.transaction_type_id = '12'
    ),
    adjustments_candidates AS (
        SELECT t.id AS transaction_id,
               ii.id AS inventory_item_id,
               t.transaction_date,
               t.inventory_id
        FROM transaction t
        JOIN transaction_entry te ON te.transaction_id = t.id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        WHERE t.transaction_type_id = '3'
    ),

    -- Having baselines and adjustments above in the helper views, we can now create pairs, when they appear together
    -- Adjustment transaction is hardcoded to be created 1 second after the baseline, so we can rely on TIMESTAMPDIFF
    baseline_adjustment_matches AS (
        SELECT
            a.transaction_id AS adjustment_id,
            b.transaction_id AS baseline_id
        FROM adjustments_candidates a
        JOIN inventory_baseline_candidates b
          ON a.inventory_item_id = b.inventory_item_id
          AND a.inventory_id = b.inventory_id
          AND TIMESTAMPDIFF(SECOND, a.transaction_date, b.transaction_date) = -1
    ),

    -- Case 1: Baseline with adjustment
    -- Having IDs of the pairs baseline + adjustment determined, we can now build the expected response, containing
    -- all needed transaction properties - the main transaction is baseline and we just join the baseline + adjustment candidates
    baseline_with_adjustments AS (
        SELECT
            CRC32(CONCAT(t.id, ii.product_id, facility.id)) as id,
            t.id AS transaction_id,
            ii.product_id AS product_id,
            facility.id AS facility_id,
            t.transaction_date AS date_recorded,
            'BASELINE_WITH_ADJUSTMENT' as transaction_profile,
            bam.adjustment_id as associated_transaction_id
            FROM transaction t
            JOIN baseline_adjustment_matches bam ON t.id = bam.baseline_id
            JOIN transaction_entry te ON te.transaction_id = t.id
            JOIN inventory_item ii ON ii.id = te.inventory_item_id
            JOIN location facility ON facility.inventory_id = t.inventory_id
            WHERE t.transaction_type_id = '12'
    ),

    -- Case 2: Adjustment without baseline ("alone" adjustment) - e.g. record stock for the first time/adjust inventory
    -- Having already determined the baseline + adjustment pairs, to get the alone adjustments, we just get all the adjustment
    -- transactions and check if such adjustment doesn't already exists in the baseline + adjustment pairs - if it does, it means, it is not "alone" adjustment,
    -- so filter it out
    adjustment_without_baseline AS (
        SELECT
            CRC32(CONCAT(t.id, ii.product_id, facility.id)) as id,
            t.id AS transaction_id,
            ii.product_id AS product_id,
            facility.id AS facility_id,
            t.transaction_date AS date_recorded,
            'ADJUSTMENT_WITHOUT_BASELINE' as transaction_profile,
            -- Alone adjustment doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM transaction t
        JOIN transaction_entry te ON te.transaction_id = t.id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        JOIN location facility ON facility.inventory_id = t.inventory_id
        WHERE t.transaction_type_id = '3'
        AND NOT EXISTS (
              SELECT 1
              FROM baseline_adjustment_matches bam
              WHERE bam.adjustment_id = t.id
          )
    ),

    -- Case 2: Baseline without adjustment - e.g. submitting a cycle count with quantityCounted equal to QOH
    -- Having already determined the baseline + adjustment pairs, to get the alone baseline transactions, we just get all the baseline transactions
    -- and check if such baseline doesn't already exists in the baseline + adjustment pairs - if it does, it means, it is not "alone" baseline,
    -- so filter it out
    baseline_without_adjustment AS (
        SELECT
            CRC32(CONCAT(t.id, ii.product_id, facility.id)) as id,
            t.id AS transaction_id,
            ii.product_id AS product_id,
            facility.id AS facility_id,
            t.transaction_date AS date_recorded,
            'BASELINE_WITHOUT_ADJUSTMENT' as transaction_profile,
            -- Alone baseline doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM transaction t
        JOIN transaction_entry te ON te.transaction_id = t.id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        JOIN location facility ON facility.inventory_id = t.inventory_id
        WHERE t.transaction_type_id = '12'
          AND NOT EXISTS (
              SELECT 1
              FROM baseline_adjustment_matches bam
              WHERE bam.baseline_id = t.id
          )
    )

-- In the end make a union of all three subviews - since we separated all possible cases (baseline + adjustment, adjustment, baseline)
-- we can be sure that no duplicates will appear
SELECT * FROM baseline_with_adjustments
UNION
SELECT * FROM adjustment_without_baseline
UNION
SELECT * FROM baseline_without_adjustment
