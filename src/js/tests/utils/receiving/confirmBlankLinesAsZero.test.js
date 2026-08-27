import { fireEvent, render, screen } from '@testing-library/react';
import { confirmAlert } from 'react-confirm-alert';

import confirmBlankLinesAsZero from 'utils/receiving/confirmBlankLinesAsZero';

import '@testing-library/jest-dom';

jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));

const translate = (id, defaultMessage, data) => Object.entries(data ?? {}).reduce(
  (message, [key, value]) => message.replace(`\${${key}}`, value),
  defaultMessage,
);

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  productCode: 'PROD-1',
  product: { name: 'Ibuprofen 200mg' },
  lotNumber: 'LOT-1',
  expirationDate: '2026-12-31',
  ...overrides,
});

// confirmAlert renders the modal itself, so the options it was called with are the only handle
// on the UI - rendering its customUI is what puts the modal on the screen here.
const showModal = (blankRows) => {
  const confirmed = confirmBlankLinesAsZero({ blankRows, translate, localeKey: 'en' });
  const { customUI, afterClose } = confirmAlert.mock.calls[0][0];
  const onClose = jest.fn();
  render(customUI({ onClose }));
  return { confirmed, onClose, afterClose };
};

describe('confirmBlankLinesAsZero()', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should list every blank line with its code, product, lot and expiration date', () => {
    showModal([
      buildRow('row-1'),
      buildRow('row-2', {
        productCode: 'PROD-2',
        product: { name: 'Paracetamol 500mg' },
        lotNumber: 'LOT-2',
        expirationDate: '2027-01-15',
      }),
    ]);

    const [, firstLine, secondLine] = screen.getAllByRole('row');
    expect(firstLine).toHaveTextContent('PROD-1Ibuprofen 200mgLOT-131/Dec/2026');
    expect(secondLine).toHaveTextContent('PROD-2Paracetamol 500mgLOT-215/Jan/2027');
  });

  it('should report how many lines will be received as zero', () => {
    showModal([buildRow('row-1'), buildRow('row-2'), buildRow('row-3')]);

    expect(screen.getByText(/for 3 line\/lines on this shipment/)).toBeInTheDocument();
  });

  it('should leave the lot and the expiration date empty when the line has none', () => {
    showModal([buildRow('row-1', { lotNumber: null, expirationDate: null })]);

    const [, line] = screen.getAllByRole('row');
    expect(line).toHaveTextContent('PROD-1Ibuprofen 200mg');
  });

  it('should resolve to true and close the modal when the user confirms', async () => {
    const { confirmed, onClose } = showModal([buildRow('row-1')]);

    fireEvent.click(screen.getByRole('button', { name: 'Yes' }));

    await expect(confirmed).resolves.toBe(true);
    expect(onClose).toHaveBeenCalled();
  });

  it('should resolve to false and close the modal when the user declines', async () => {
    const { confirmed, onClose } = showModal([buildRow('row-1')]);

    fireEvent.click(screen.getByRole('button', { name: 'No' }));

    await expect(confirmed).resolves.toBe(false);
    expect(onClose).toHaveBeenCalled();
  });

  it('should resolve to false when the modal is closed any other way', async () => {
    const { confirmed, afterClose } = showModal([buildRow('row-1')]);

    afterClose();

    await expect(confirmed).resolves.toBe(false);
  });

  it('should keep the answer of the first close', async () => {
    const { confirmed, afterClose } = showModal([buildRow('row-1')]);

    fireEvent.click(screen.getByRole('button', { name: 'Yes' }));
    afterClose();

    await expect(confirmed).resolves.toBe(true);
  });
});
