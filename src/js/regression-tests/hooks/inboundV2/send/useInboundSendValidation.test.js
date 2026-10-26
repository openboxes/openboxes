import { renderHook } from '@testing-library/react-hooks';

import useInboundSendValidation from 'hooks/inboundV2/send/useInboundSendValidation';

jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));

const location = (id) => ({ id, name: `Loc ${id}`, label: `Loc ${id} [Depot]` });

const validValues = {
  origin: location('o-1'),
  destination: location('d-1'),
  shipDate: '01/Oct/2026 10:00:00',
  shipmentType: { id: 'st-1', name: 'Air', label: 'Air' },
  trackingNumber: 'T-1',
  driverName: '',
  comments: '',
  expectedDeliveryDate: '05/Oct/2026',
};

const errorsByPath = (result) => (result.success ? {} : result.error.issues.reduce(
  (acc, issue) => ({
    ...acc,
    [issue.path.join('.')]: [...(acc[issue.path.join('.')] || []), issue.message],
  }),
  {},
));

describe('useInboundSendValidation', () => {
  let schema;

  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date(2026, 9, 2, 12, 0, 0));
    const { result } = renderHook(() => useInboundSendValidation());
    schema = result.current.validationSchema();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('accepts a complete shipment', () => {
    expect(schema.safeParse(validValues).success).toBe(true);
  });

  it('requires origin, destination, ship date, shipment type and delivery date', () => {
    const errors = errorsByPath(schema.safeParse({
      ...validValues,
      origin: null,
      destination: undefined,
      shipDate: null,
      shipmentType: { id: '', name: '', label: '' },
      expectedDeliveryDate: undefined,
    }));
    expect(errors.origin).toContain('This field is required');
    expect(errors.destination).toContain('This field is required');
    expect(errors.shipDate).toContain('This field is required');
    expect(errors.shipmentType).toContain('This field is required');
    expect(errors.expectedDeliveryDate).toContain('This field is required');
  });

  it('rejects a ship date in the future', () => {
    const errors = errorsByPath(schema.safeParse({
      ...validValues,
      shipDate: '03/Oct/2026 00:00:01',
      expectedDeliveryDate: '10/Oct/2026',
    }));
    expect(errors).toEqual({ shipDate: ['The date cannot be in the future'] });
  });

  it('accepts a ship date later today', () => {
    expect(schema.safeParse({ ...validValues, shipDate: '02/Oct/2026 23:00:00' }).success).toBe(true);
  });

  it('rejects dates before 2000', () => {
    const errors = errorsByPath(schema.safeParse({
      ...validValues,
      shipDate: '31/Dec/1999 10:00:00',
      expectedDeliveryDate: '31/Dec/1999',
    }));
    expect(errors.shipDate).toContain('This date is invalid. Please enter a date after 2000.');
    expect(errors.expectedDeliveryDate).toContain('This date is invalid. Please enter a date after 2000.');
  });

  it('flags both dates when delivery is before the ship date', () => {
    const errors = errorsByPath(schema.safeParse({ ...validValues, expectedDeliveryDate: '30/Sep/2026' }));
    const message = 'Please verify timeline. Delivery date cannot be before Ship date.';
    expect(errors).toEqual({ shipDate: [message], expectedDeliveryDate: [message] });
  });

  it('allows delivery on the ship day', () => {
    expect(schema.safeParse({ ...validValues, expectedDeliveryDate: '01/Oct/2026' }).success).toBe(true);
  });
});
