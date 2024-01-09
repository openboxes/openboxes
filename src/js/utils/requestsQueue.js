import { TaskQueue } from 'cwait';

const MAX_SIMULTANEOUS_REQUESTS = 1;

const requestsQueue = () => {
  const queue = new TaskQueue(Promise, MAX_SIMULTANEOUS_REQUESTS);

  // when tasks amount is equal to MAX_SIMULTANEOUS_REQUESTS
  // then the requests are processed automatically,
  // so we don't have to start processing manually
  const enqueueRequest = (request) => {
    queue.add(request);
  };

  return {
    enqueueRequest,
  };
};

export default requestsQueue;
