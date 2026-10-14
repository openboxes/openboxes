import { useCallback, useRef } from 'react';

import requestsQueue from 'utils/requestsQueue';

/**
 * Serial task queue of the autosave. Tasks run one at a time (so two requests can never race
 * each other on the backend)
 *
 * @returns {{
 *   enqueueTask: Function,
 *   hasPendingTasks: Function,
 *   awaitPendingTasks: Function,
 * }} `enqueueTask(taskFn, { onError, onSettled })` - runs `taskFn` through the queue.
 *    A failed task calls `onError`; `onSettled` runs after the
 *    task is untracked, on success and failure.
 *    `hasPendingTasks()` - true while any task is still running or queued.
 *    `awaitPendingTasks()` - waits until all currently tracked tasks settle.
 */
const useAutosaveQueue = () => {
  const queueRef = useRef(requestsQueue());
  // Promises of the enqueued tasks, removed once they settle - awaitPendingTasks waits
  // for them.
  const pendingTasksRef = useRef(new Set());

  const enqueueTask = useCallback((taskFn, { onError, onSettled } = {}) => {
    const task = queueRef.current.enqueueRequest(taskFn)
      .catch((error) => onError?.(error))
      .finally(() => {
        pendingTasksRef.current.delete(task);
        onSettled?.();
      });
    pendingTasksRef.current.add(task);
    return task;
  }, []);

  const hasPendingTasks = useCallback(() => pendingTasksRef.current.size > 0, []);

  const awaitPendingTasks = useCallback(
    () => Promise.allSettled([...pendingTasksRef.current]),
    [],
  );

  return {
    enqueueTask,
    hasPendingTasks,
    awaitPendingTasks,
  };
};

export default useAutosaveQueue;
