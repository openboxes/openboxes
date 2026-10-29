import requestsQueue from 'utils/requestsQueue';

import '@testing-library/jest-dom';

const sleep = (ms) => new Promise((resolve) => {
  setTimeout(resolve, ms);
});

describe('requestsQueue', () => {
  it('resolves with the result of the enqueued request', async () => {
    const { enqueueRequest } = requestsQueue();

    await expect(enqueueRequest(() => Promise.resolve(42)))
      .resolves.toBe(42);
  });

  it('returns a rejection of the enqueued request', async () => {
    const { enqueueRequest } = requestsQueue();

    await expect(enqueueRequest(() => Promise.reject(new Error('network'))))
      .rejects.toThrow('network');
  });

  it('runs requests in order', async () => {
    const { enqueueRequest } = requestsQueue();
    const order = [];

    const first = enqueueRequest(async () => {
      order.push('first started');
      await sleep(50);
      order.push('first finished');
    });
    const second = enqueueRequest(async () => {
      order.push('second started');
    });

    expect(order).toEqual(['first started']);

    await Promise.all([first, second]);

    expect(order).toEqual(['first started', 'first finished', 'second started']);
  });

  it('keeps processing after a failed request', async () => {
    const { enqueueRequest } = requestsQueue();

    await expect(enqueueRequest(() => Promise.reject(new Error('network'))))
      .rejects.toThrow('network');
    await expect(enqueueRequest(() => Promise.resolve('next')))
      .resolves.toBe('next');
  });
});
