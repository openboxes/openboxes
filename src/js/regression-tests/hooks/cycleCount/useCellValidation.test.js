import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useCellValidation from 'hooks/cycleCount/useCellValidation';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useCellValidation', () => {
  let state;

  const buildState = ({ isFormSubmitted = false, error = null } = {}) => ({
    errors: {
      isFormSubmitted,
      errors: {
        'cc-1': {
          cycleCountItems: {
            2: error ? { quantityRecounted: { _errors: [error] } } : {},
          },
        },
      },
    },
  });

  const renderUseCellValidation = (props = {}) => renderHook(() => useCellValidation({
    initialValue: null,
    cycleCountId: 'cc-1',
    index: '2',
    fieldName: 'quantityRecounted',
    ...props,
  }));

  beforeEach(() => {
    state = buildState();
    useSelector.mockImplementation((selector) => selector(state));
  });

  it('returns no error when the store has none for the field', () => {
    const { result } = renderUseCellValidation();

    expect(result.current.error).toBeNull();
    expect(result.current.showError).toBeFalsy();
  });

  it('hides an existing error on initial load of an empty, untouched field', () => {
    state = buildState({ error: 'Quantity recounted is required' });

    const { result } = renderUseCellValidation();

    expect(result.current.error).toBe('Quantity recounted is required');
    expect(result.current.showError).toBeFalsy();
  });

  it('shows the error after the form was submitted', () => {
    state = buildState({ error: 'Quantity recounted is required', isFormSubmitted: true });

    const { result } = renderUseCellValidation();

    expect(result.current.showError).toBe(true);
  });

  it('shows the error for a pre-filled field', () => {
    state = buildState({ error: 'Lot number is required' });

    const { result } = renderUseCellValidation({ initialValue: 'LOT-1' });

    expect(result.current.showError).toBe('LOT-1');
  });

  it('hides the error while the field is being edited and shows it again on blur', () => {
    state = buildState({ error: 'Quantity recounted is required' });
    const { result } = renderUseCellValidation();

    act(() => result.current.onChangeValidationHandler());
    expect(result.current.showError).toBeFalsy();

    act(() => result.current.onBlurValidationHandler());
    expect(result.current.showError).toBe(true);
  });

  it('reads the error using the numeric item index', () => {
    state = buildState({ error: 'Some error' });

    const { result } = renderUseCellValidation({ index: 2 });

    expect(result.current.error).toBe('Some error');
  });
});
