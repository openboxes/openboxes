CREATE OR REPLACE VIEW inventory_counts AS
    -- The adjustment_candidate and inventory_baseline_candidate helper tables already hold exactly one row per
    -- (transaction, product, facility) for their respective transaction types, with the type and migration-comment
    -- filtering applied at build time. Every branch below reads from them rather than re-deriving from
    -- transaction_entry, so the type filters live in one place and the large scans happen once, at rebuild.
    -- Adjustment transaction is hardcoded to be created 1 second after the baseline, so we can rely on TIMESTAMPDIFF
    -- DISTINCT because the pair join keys on inventory_id while facility_id is carried along: location.inventory_id
    -- is 1:1 by convention but has no unique constraint, so two facilities on one inventory would otherwise fan the
    -- pairs out. Deduplicating the small pair set here is what lets the final union be UNION ALL.
    WITH baseline_adjustment_matches AS (
        SELECT DISTINCT
            a.transaction_id AS adjustment_id,
            b.transaction_id AS baseline_id,
            a.product_id AS product_id,
            a.facility_id AS facility_id
        FROM adjustment_candidate a
        JOIN inventory_baseline_candidate b
          ON a.product_id = b.product_id
          AND a.inventory_id = b.inventory_id
          AND TIMESTAMPDIFF(SECOND, a.transaction_date, b.transaction_date) = -1
    ),

    -- Case 1: Baseline with adjustment
    -- Having IDs of the pairs baseline + adjustment determined, we can now build the expected response, containing
    -- all needed transaction properties - the main transaction is baseline and we just join the baseline + adjustment candidates
    baseline_with_adjustments AS (
        SELECT
            CRC32(CONCAT(bam.baseline_id, bam.product_id, bam.facility_id)) as id,
            bam.baseline_id AS transaction_id,
            bam.product_id AS product_id,
            bam.facility_id AS facility_id,
            t.transaction_date AS date_recorded,
            'BASELINE_ADJUSTMENT' as inventory_count_type_code,
            bam.adjustment_id as associated_transaction_id
            FROM baseline_adjustment_matches bam
            JOIN transaction t ON bam.baseline_id = t.id
    ),

    -- Case 2: Adjustment without baseline ("alone" adjustment) - e.g. record stock for the first time/adjust inventory
    -- Anti-join the adjustment candidates against the pairs: an adjustment that appears in a pair is not "alone",
    -- so the LEFT JOIN finds a match for it and the IS NULL check filters it out
    adjustment_without_baseline AS (
        SELECT
            CRC32(CONCAT(a.transaction_id, a.product_id, a.facility_id)) as id,
            a.transaction_id AS transaction_id,
            a.product_id AS product_id,
            a.facility_id AS facility_id,
            a.transaction_date AS date_recorded,
            'ADJUSTMENT' as inventory_count_type_code,
            -- Alone adjustment doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM adjustment_candidate a
        LEFT JOIN baseline_adjustment_matches bam
          ON bam.adjustment_id = a.transaction_id
          AND bam.product_id = a.product_id
        WHERE bam.adjustment_id IS NULL
    ),

    -- Case 3: Baseline without adjustment - e.g. submitting a cycle count with quantityCounted equal to QOH
    -- Same anti-join as case 2, but against the baseline side of the pairs
    baseline_without_adjustment AS (
        SELECT
            CRC32(CONCAT(b.transaction_id, b.product_id, b.facility_id)) as id,
            b.transaction_id AS transaction_id,
            b.product_id AS product_id,
            b.facility_id AS facility_id,
            b.transaction_date AS date_recorded,
            'BASELINE' as inventory_count_type_code,
            -- Alone baseline doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM inventory_baseline_candidate b
        LEFT JOIN baseline_adjustment_matches bam
          ON bam.baseline_id = b.transaction_id
          AND bam.product_id = b.product_id
        WHERE bam.baseline_id IS NULL
    )

-- In the end make a union of all three subviews - since we separated all possible cases (baseline + adjustment, adjustment, baseline)
-- we can be sure that no duplicates will appear. The helper tables carry a unique key on
-- (transaction_id, product_id, facility_id), so each branch is duplicate-free on its own and UNION ALL can skip
-- the dedupe pass that UNION would otherwise run over the whole result set.
SELECT * FROM baseline_with_adjustments
UNION ALL
SELECT * FROM adjustment_without_baseline
UNION ALL
SELECT * FROM baseline_without_adjustment
