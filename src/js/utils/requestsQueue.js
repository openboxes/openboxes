class Queue {
  queue = [];

  enqueue = (request) => this.queue.push(request);

  dequeue = () => this.queue.shift();

  isEmpty = () => !this.queue.length;
}

const requestsQueue = () => {
  const queue = new Queue();

  const currentRequest = { processing: false };

  const enqueueRequest = (request) => {
    queue.enqueue(request);
  };

  const processRequests = (async () => {
    if (!currentRequest.processing && !queue.isEmpty()) {
      const request = queue.dequeue();
      currentRequest.processing = true;

      await request();

      currentRequest.processing = false;
      await processRequests();
    }
  });

  return {
    enqueueRequest,
    processRequests,
  };
};

export default requestsQueue;
