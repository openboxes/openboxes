import { renderHook } from '@testing-library/react-hooks';

import useOutboundImportValidation from 'hooks/outboundImport/useOutboundImportValidation';

jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({ session: { activeLanguage: 'en' } })),
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

const location = (id) => ({ id, label: id, name: id });

const validData = {
  description: 'Shipment',
  origin: location('origin'),
  destination: location('destination'),
  requestedBy: location('user'),
  dateRequested: 'Jun 10, 2026',
  dateShipped: 'Jun 11, 2026 10:00:00',
  shipmentType: {
    id: '1', value: 'AIR', label: 'Air', displayName: 'Air', enumKey: 'AIR', description: null,
  },
  trackingNumber: null,
  expectedDeliveryDate: 'Jun 12, 2026',
  packingList: new File(['a'], 'list.xlsx'),
};

const issuesByPath = (result) => result.error.issues.reduce((acc, issue) => ({
  ...acc,
  [issue.path.join('.')]: [...(acc[issue.path.join('.')] || []), issue.message],
}), {});

describe('useOutboundImportValidation', () => {
  beforeEach(() => {
    jest.useFakeTimers('modern');
    jest.setSystemTime(new Date('2026-06-15T12:00:00'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  const parse = (data) => {
    const { result } = renderHook(() => useOutboundImportValidation());
    return result.current.validationSchema(data).safeParse(data);
  };

  it('accepts a complete form with dates in the past and delivery after shipping', () => {
    expect(parse(validData).success).toBe(true);
  });

  it('reports every missing required field with its message', () => {
    const result = parse({});

    expect(result.success).toBe(false);
    expect(issuesByPath(result)).toEqual(expect.objectContaining({
      description: ['Description is required'],
      origin: ['Origin is required'],
      destination: ['Destination is required'],
      requestedBy: ['Requested by is required'],
      dateRequested: ['Date requested is required'],
      dateShipped: ['Date shipped is required'],
      shipmentType: ['Shipment type is required'],
      expectedDeliveryDate: ['Expected delivery date is required'],
    }));
    expect(issuesByPath(result).packingList).toHaveLength(1);
  });

  it('rejects an empty description', () => {
    const result = parse({ ...validData, description: '' });

    expect(issuesByPath(result)).toEqual({ description: ['Description is required'] });
  });

  it('rejects requested and shipped dates in the future', () => {
    const data = {
      ...validData,
      dateRequested: 'Jun 16, 2026',
      dateShipped: 'Jun 16, 2026 08:00:00',
      expectedDeliveryDate: 'Jun 20, 2026',
    };

    expect(issuesByPath(parse(data))).toEqual({
      dateRequested: ['The date cannot be in the future'],
      dateShipped: ['The date cannot be in the future'],
    });
  });

  it('accepts today as the requested and shipped date', () => {
    const data = {
      ...validData,
      dateRequested: 'Jun 15, 2026',
      dateShipped: 'Jun 15, 2026 23:00:00',
      expectedDeliveryDate: 'Jun 15, 2026',
    };

    expect(parse(data).success).toBe(true);
  });

  it('flags both dates when delivery is before shipping', () => {
    const data = { ...validData, expectedDeliveryDate: 'Jun 10, 2026' };
    const message = 'Please verify timeline. Delivery date cannot be before Ship date.';

    expect(issuesByPath(parse(data))).toEqual({
      dateShipped: [message],
      expectedDeliveryDate: [message],
    });
  });

  it('exposes the location schemas used by the form fields', () => {
    const { result } = renderHook(() => useOutboundImportValidation());

    expect(result.current.originSchema.safeParse(location('a')).success).toBe(true);
    expect(result.current.destinationSchema.safeParse({ id: 'a' }).success).toBe(false);
    expect(result.current.requestedBySchema.safeParse(null).error.issues[0].message)
      .toBe('Requested by is required');
  });
});
