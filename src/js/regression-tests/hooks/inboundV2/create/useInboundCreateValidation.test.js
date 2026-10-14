import { renderHook } from '@testing-library/react-hooks';

import useInboundCreateValidation from 'hooks/inboundV2/create/useInboundCreateValidation';

jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));

const option = (id) => ({ id, name: `Name ${id}`, label: `Label ${id}` });

const validValues = {
  description: 'Restock',
  origin: option('o-1'),
  destination: option('d-1'),
  stocklist: null,
  requestedBy: option('u-1'),
  dateRequested: '01/Oct/2026',
};

const messagesByPath = (result) => (result.success ? {} : Object.fromEntries(
  result.error.issues.map((issue) => [issue.path.join('.'), issue.message]),
));

describe('useInboundCreateValidation', () => {
  let schema;

  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date(2026, 9, 2, 12, 0, 0));
    const { result } = renderHook(() => useInboundCreateValidation());
    schema = result.current.validationSchema();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('accepts complete details with or without a stock list', () => {
    expect(schema.safeParse(validValues).success).toBe(true);
    expect(schema.safeParse({ ...validValues, stocklist: option('sl-1') }).success).toBe(true);
    expect(schema.safeParse({ ...validValues, stocklist: undefined }).success).toBe(true);
  });

  it('requires every field except the stock list', () => {
    expect(messagesByPath(schema.safeParse({}))).toEqual({
      description: 'This field is required',
      origin: 'This field is required',
      destination: 'Destination is required',
      requestedBy: 'This field is required',
      dateRequested: 'This field is required',
    });
    expect(messagesByPath(schema.safeParse({ ...validValues, description: '' })))
      .toEqual({ description: 'This field is required' });
  });

  it('rejects a requested date in the future or before 2000', () => {
    expect(messagesByPath(schema.safeParse({ ...validValues, dateRequested: '03/Oct/2026' })))
      .toEqual({ dateRequested: 'The date cannot be in the future' });
    expect(messagesByPath(schema.safeParse({ ...validValues, dateRequested: '31/Dec/1999' })))
      .toEqual({ dateRequested: 'This date is invalid. Please enter a date after 2000.' });
  });
});
