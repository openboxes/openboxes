import { renderHook } from '@testing-library/react-hooks';

import useCountStepValidation from 'hooks/cycleCount/countStep/useCountStepValidation';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useStore: () => ({ getState: () => mockState }),
}));
jest.mock('actions', () => ({
  setErrors: jest.fn((errors) => ({ type: 'SET_ERRORS', errors })),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (key, defaultMessage) => defaultMessage));

const row = (overrides = {}) => ({
  id: 'item-1',
  product: { id: 'p-1', lotAndExpiryControl: false },
  quantityCounted: 3,
  inventoryItem: { lotNumber: 'LOT-1', expirationDate: '01/05/2027' },
  binLocation: null,
  ...overrides,
});

describe('useCountStepValidation (count step)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('passes and clears the errors when every count is valid', () => {
    mockState = { countWorkflow: { entities: { 'cc-1': { cycleCountItems: [row()] } } } };
    const { result } = renderHook(() => useCountStepValidation());

    expect(result.current.validateCountStep()).toBe(true);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_ERRORS', errors: undefined });
  });

  it('fails and dispatches the formatted errors keyed by cycle count', () => {
    mockState = {
      countWorkflow: {
        entities: {
          'cc-1': { cycleCountItems: [row()] },
          'cc-2': { cycleCountItems: [row({ quantityCounted: null })] },
        },
      },
    };
    const { result } = renderHook(() => useCountStepValidation());

    expect(result.current.validateCountStep()).toBe(false);
    const { errors } = mockDispatch.mock.calls[0][0];
    expect(errors['cc-1']).toBeUndefined();
    expect(errors['cc-2'].cycleCountItems[0].quantityCounted._errors)
      .toEqual(['Quantity counted is required']);
  });

  it('reads the latest store state on every call', () => {
    mockState = { countWorkflow: { entities: { 'cc-1': { cycleCountItems: [row({ quantityCounted: -1 })] } } } };
    const { result } = renderHook(() => useCountStepValidation());
    expect(result.current.validateCountStep()).toBe(false);

    mockState = { countWorkflow: { entities: { 'cc-1': { cycleCountItems: [row()] } } } };
    expect(result.current.validateCountStep()).toBe(true);
  });
});
