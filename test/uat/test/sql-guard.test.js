'use strict';
// node --test test/uat/test (test/uat/run.sh --unit): the audit SQL guard.
const test = require('node:test');
const assert = require('node:assert/strict');
const { assertReadOnly } = require('../lib/sql-guard');

test('the audit guard allows single read-only statements', () => {
  for (const s of [
    'SELECT 1',
    'select status from `order` where id = ?',
    'SELECT COALESCE(SUM(quantity_on_hand), 0) FROM product_availability WHERE location_id = ? AND last_updated > ?;',
    "SELECT * FROM shipment WHERE name = 'please update; delete'",
    'WITH x AS (SELECT 1 AS a) SELECT a FROM x',
    'SHOW TABLES',
    'EXPLAIN SELECT 1',
    'SELECT `update`, `delete` FROM t -- a comment with insert',
  ]) assert.equal(assertReadOnly(s), s);
});

test('the audit guard refuses anything that writes, locks or stacks statements', () => {
  for (const s of [
    'UPDATE `order` SET status = 1',
    'DELETE FROM location',
    'INSERT INTO t VALUES (1)',
    'SELECT 1; DELETE FROM t',
    'SELECT * FROM t FOR UPDATE',
    'SELECT * FROM t LOCK IN SHARE MODE',
    'SELECT 1 INTO @x',
    "SELECT * INTO OUTFILE '/tmp/x' FROM t",
    'WITH x AS (SELECT 1) UPDATE t SET a = 1',
    'SET @a = 1',
    'CALL p()',
    'SELECT /*! 1; DROP TABLE t */ 1',
    'SELECT SLEEP(10)',
    'SELECT GET_LOCK("x", 1)',
    '',
    "SELECT 'unterminated",
  ]) assert.throws(() => assertReadOnly(s), /audit SQL/, s);
});
