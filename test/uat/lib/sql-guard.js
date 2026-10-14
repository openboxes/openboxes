'use strict';
/**
 * The audit checkpoints' SELECT-only guard (one of three layers: the audit connection signs in as
 * `uat_audit`, which has SELECT only, and every session is SET ... READ ONLY). Refuses any statement
 * that is not a single SELECT / WITH ... SELECT / SHOW / EXPLAIN / DESCRIBE, or that carries a word
 * that writes or locks, outside string literals, quoted identifiers and comments.
 */
const FIRST = new Set(['SELECT', 'WITH', 'SHOW', 'EXPLAIN', 'DESCRIBE', 'DESC']);
const FORBIDDEN = new Set([
  'INSERT', 'UPDATE', 'DELETE', 'REPLACE', 'MERGE', 'UPSERT', 'CREATE', 'ALTER', 'DROP', 'TRUNCATE', 'RENAME',
  'GRANT', 'REVOKE', 'LOCK', 'UNLOCK', 'CALL', 'DO', 'HANDLER', 'LOAD', 'SET', 'INTO', 'OUTFILE', 'DUMPFILE',
  'COMMIT', 'ROLLBACK', 'SAVEPOINT', 'START', 'BEGIN', 'KILL', 'FLUSH', 'RESET', 'PURGE', 'OPTIMIZE', 'ANALYZE',
  'REPAIR', 'INSTALL', 'UNINSTALL', 'PREPARE', 'EXECUTE', 'DEALLOCATE', 'SHUTDOWN', 'SLEEP', 'GET_LOCK', 'RELEASE_LOCK',
  'NEXTVAL', 'SETVAL', 'LAST_INSERT_ID',
]);

/** Removes comments, string literals and quoted identifiers (replaced by a space). */
function scrub(sql) {
  let out = '';
  let i = 0;
  while (i < sql.length) {
    const c = sql[i];
    const n = sql[i + 1];
    if (c === '-' && n === '-') { const e = sql.indexOf('\n', i); i = e < 0 ? sql.length : e; out += ' '; continue; }
    if (c === '#') { const e = sql.indexOf('\n', i); i = e < 0 ? sql.length : e; out += ' '; continue; }
    if (c === '/' && n === '*') {
      // MySQL executes /*! ... */ and /*+ ... */ bodies: refuse them outright
      if (sql[i + 2] === '!' || sql[i + 2] === '+') throw new Error('audit SQL: executable comments (/*! */, /*+ */) are not allowed');
      const e = sql.indexOf('*/', i + 2);
      if (e < 0) throw new Error('audit SQL: unterminated comment');
      i = e + 2; out += ' '; continue;
    }
    if (c === "'" || c === '"' || c === '`') {
      let j = i + 1;
      for (;;) {
        if (j >= sql.length) throw new Error('audit SQL: unterminated quoted text');
        if (sql[j] === '\\' && c !== '`') { j += 2; continue; }
        if (sql[j] === c) { if (sql[j + 1] === c) { j += 2; continue; } break; }
        j++;
      }
      i = j + 1; out += c === '`' ? ' _ident_ ' : ' _str_ '; continue;
    }
    out += c; i++;
  }
  return out;
}

/** Throws unless `sql` is one read-only statement. Returns the statement unchanged. */
function assertReadOnly(sql) {
  if (typeof sql !== 'string' || sql.trim() === '') throw new Error('audit SQL: empty statement');
  const s = scrub(sql).trim().replace(/;\s*$/, '');
  if (s.includes(';')) throw new Error('audit SQL: one statement only');
  const words = (s.match(/[A-Za-z_][A-Za-z0-9_$]*/g) || []).map((w) => w.toUpperCase());
  if (!FIRST.has(words[0])) throw new Error(`audit SQL: only SELECT, WITH, SHOW, EXPLAIN or DESCRIBE (got ${words[0] || 'nothing'})`);
  const bad = words.find((w) => FORBIDDEN.has(w));
  if (bad) throw new Error(`audit SQL: "${bad}" is not allowed in an audit (read only)`);
  return sql;
}

module.exports = { assertReadOnly, scrub };
