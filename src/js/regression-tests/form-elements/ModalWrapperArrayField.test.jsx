import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import Modal from 'react-modal';
import { Provider } from 'react-redux';
import testStore from 'regression-tests/data-table/testStore';

import ArrayField from 'components/form-elements/ArrayField';
import CheckboxField from 'components/form-elements/CheckboxField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import TextareaField from 'components/form-elements/TextareaField';
import TextField from 'components/form-elements/TextField';

import '@testing-library/jest-dom';

// The real client imports the store, which needs the reducers this test is still loading.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

// jsdom has no layout, so the auto sizer of the virtualized table gives it a fixed width.
jest.mock('react-virtualized', () => ({
  ...jest.requireActual('react-virtualized'),
  AutoSizer: ({ children }) => children({ width: 600 }),
}));

Modal.setAppElement(document.createElement('div'));

// Moving to a line scrolls it into view; jsdom does not implement scrolling.
beforeAll(() => {
  Element.prototype.scrollIntoView = jest.fn();
});

afterAll(() => {
  delete Element.prototype.scrollIntoView;
});

const lineFields = {
  product: {
    type: LabelField,
    label: 'react.line.product.label',
    defaultMessage: 'Product',
    flexWidth: '4',
    attributes: { formatValue: (product) => product?.name },
    headerTooltip: 'react.line.product.tooltip',
    headerDefaultTooltip: 'The product',
  },
  lot: {
    type: TextField,
    label: 'react.line.lot.label',
    defaultMessage: 'Lot',
    fixedWidth: '100px',
    headerAlign: 'left',
    multilineHeader: true,
  },
  quantity: {
    type: TextField,
    label: 'react.line.quantity.label',
    defaultMessage: 'Quantity',
    required: true,
    headerClassName: 'qty-header',
    attributes: { type: 'number', cellClassName: 'qty-cell' },
  },
  hidden: {
    type: TextField,
    label: 'react.line.hidden.label',
    defaultMessage: 'Hidden',
    getDynamicAttr: () => ({ hide: true }),
  },
};

const fields = (arrayConfig = {}) => ({
  name: {
    type: TextField,
    label: 'react.form.name.label',
    defaultMessage: 'Name',
    attributes: { required: true, subtext: 'Shown to everyone' },
  },
  notes: { type: TextareaField, label: 'react.form.notes.label', defaultMessage: 'Notes' },
  urgent: { type: CheckboxField, label: 'react.form.urgent.label', defaultMessage: 'Urgent' },
  lines: {
    type: ArrayField,
    arrowsNavigation: true,
    addButton: 'react.form.addLine.label',
    headerGroupings: {
      what: { label: 'react.group.what', defaultLabel: 'What', flexWidth: '4' },
      how: {
        label: 'react.group.how', defaultLabel: 'How many', fixedWidth: '100px', headerAlign: 'left',
      },
    },
    fields: lineFields,
    ...arrayConfig,
  },
});

const initialValues = {
  name: 'Restock',
  lines: [
    { product: { name: 'Amoxicillin' }, lot: 'L1', quantity: '5' },
    { product: { name: 'Bandage' }, lot: '', quantity: '2' },
  ],
};

const renderModal = (props = {}, session = {}) => {
  const callbacks = { onOpen: jest.fn(), onSave: jest.fn(), onClose: jest.fn() };
  render(
    <Provider store={testStore(session)}>
      <ModalWrapper
        title="react.form.title"
        defaultTitleMessage="Edit request"
        btnOpenText="react.form.open"
        btnOpenDefaultText="Edit"
        btnOpenIcon="fa-pencil"
        fields={fields()}
        initialValues={initialValues}
        {...callbacks}
        {...props}
      />
    </Provider>,
  );
  return callbacks;
};

const openModal = () => fireEvent.click(screen.getByRole('button', { name: 'Edit' }));

// The form field wrapper of a top-level field (its label and input carry the same name).
const formField = (label) => screen.getAllByLabelText(label)
  .find((element) => element.dataset.testid === 'form-field');

const lineInputs = (label) => screen.getAllByRole('cell', { name: label })
  .map((cell) => within(cell).getByTestId('input'));

describe('modal wrapper with an array field', () => {
  it('opens the form with the initial values in the fields and the lines', () => {
    const { onOpen } = renderModal();

    expect(screen.queryByText('Edit request')).toBeNull();
    openModal();
    expect(onOpen).toHaveBeenCalled();
    expect(screen.getByText('Edit request')).toBeInTheDocument();
    expect(within(formField('Name')).getByTestId('input')).toHaveValue('Restock');
    expect(screen.getByText('Shown to everyone')).toBeInTheDocument();

    // The lines: labels formatted, inputs filled, hidden columns left out
    expect(screen.getAllByRole('row')).toHaveLength(2);
    expect(screen.getByText('Amoxicillin')).toBeInTheDocument();
    expect(lineInputs('Lot').map((input) => input.value)).toEqual(['L1', '']);
    expect(lineInputs('Quantity').map((input) => input.value)).toEqual(['5', '2']);
    expect(screen.getAllByRole('cell', { name: 'Quantity' })[0]).toHaveClass('qty-cell');
    expect(screen.queryByText('Hidden')).toBeNull();
    expect(screen.queryAllByRole('cell', { name: 'Hidden' })).toHaveLength(0);
    // The headers, the groupings above them and the required marker
    expect(screen.getByText('What')).toBeInTheDocument();
    expect(screen.getByText('How many')).toBeInTheDocument();
    expect(screen.getByText('Quantity').closest('.arrayfield-header-required')).not.toBeNull();
    expect(screen.getByText('Quantity').closest('.qty-header')).not.toBeNull();
  });

  it('adds an empty line with the add button', async () => {
    renderModal();
    openModal();

    fireEvent.click(document.querySelector('.add-button button'));

    await waitFor(() => expect(screen.getAllByRole('row')).toHaveLength(3));
    expect(lineInputs('Quantity').map((input) => input.value)).toEqual(['5', '2', '']);
  });

  it('moves between the line fields with the arrow keys and copies a value down', () => {
    renderModal();
    openModal();
    const [firstLot, secondLot] = lineInputs('Lot');
    const [firstQuantity, secondQuantity] = lineInputs('Quantity');

    fireEvent.keyDown(firstLot, { keyCode: 39 });
    expect(firstQuantity).toHaveFocus();
    fireEvent.keyUp(firstQuantity, { keyCode: 39 });

    fireEvent.keyDown(firstQuantity, { keyCode: 40 });
    expect(secondQuantity).toHaveFocus();
    fireEvent.keyUp(secondQuantity, { keyCode: 40 });

    fireEvent.keyDown(secondQuantity, { keyCode: 37 });
    expect(secondLot).toHaveFocus();
    fireEvent.keyUp(secondLot, { keyCode: 37 });

    fireEvent.keyDown(secondLot, { keyCode: 38 });
    expect(firstLot).toHaveFocus();
    fireEvent.keyUp(firstLot, { keyCode: 38 });

    // Ctrl + arrow down copies the value into the same field of the next line
    fireEvent.keyDown(firstLot, { keyCode: 17 });
    fireEvent.keyDown(firstLot, { keyCode: 40 });
    expect(secondLot).toHaveValue('L1');
    expect(secondLot).toHaveFocus();
  });

  it('does not move past the edges of the table', () => {
    renderModal();
    openModal();
    const [firstLot] = lineInputs('Lot');
    const [, lastQuantity] = lineInputs('Quantity');

    firstLot.focus();
    fireEvent.keyDown(firstLot, { keyCode: 37 });
    fireEvent.keyUp(firstLot, { keyCode: 37 });
    fireEvent.keyDown(firstLot, { keyCode: 38 });
    expect(firstLot).toHaveFocus();

    lastQuantity.focus();
    fireEvent.keyDown(lastQuantity, { keyCode: 39 });
    fireEvent.keyUp(lastQuantity, { keyCode: 39 });
    fireEvent.keyDown(lastQuantity, { keyCode: 40 });
    fireEvent.keyUp(lastQuantity, { keyCode: 40 });
    fireEvent.keyDown(lastQuantity, { keyCode: 17 });
    fireEvent.keyDown(lastQuantity, { keyCode: 40 });
    expect(lastQuantity).toHaveFocus();
  });

  it('saves the edited values and closes', async () => {
    const { onSave } = renderModal();
    openModal();

    fireEvent.change(within(formField('Name')).getByTestId('input'), { target: { value: 'Urgent restock' } });
    fireEvent.change(within(formField('Notes')).getByRole('textbox'), { target: { value: 'Before Friday' } });
    fireEvent.click(within(formField('Urgent')).getByRole('checkbox'));
    fireEvent.change(lineInputs('Quantity')[1], { target: { value: '7' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(onSave).toHaveBeenCalled());
    const [saved] = onSave.mock.calls[0];
    expect(saved).toMatchObject({
      name: 'Urgent restock',
      notes: 'Before Friday',
      urgent: true,
    });
    expect(saved.lines.map(({ quantity }) => quantity)).toEqual(['5', '7']);
    await waitFor(() => expect(screen.queryByText('Edit request')).toBeNull());
  });

  it('shows the validation error and does not save while the form is invalid', async () => {
    const { onSave } = renderModal({
      validate: (values) => (values.name ? {} : { name: 'react.form.name.required' }),
    });
    openModal();

    const name = within(formField('Name')).getByTestId('input');
    fireEvent.change(name, { target: { value: '' } });
    fireEvent.blur(name);
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('react.form.name.required')).toBeInTheDocument();
    expect(formField('Name')).toHaveClass('has-error');
    expect(onSave).not.toHaveBeenCalled();
  });

  it('closes on cancel without saving', () => {
    const { onSave, onClose } = renderModal();
    openModal();

    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(onClose).toHaveBeenCalled();
    expect(onSave).not.toHaveBeenCalled();
    expect(screen.queryByText('Edit request')).toBeNull();
  });

  it('opens from a custom button, shown from outside, with a custom title and body', () => {
    renderModal({
      title: () => <h4>Custom title</h4>,
      renderButton: ({ openModal: open }) => <button type="button" onClick={open}>Custom open</button>,
      renderBodyWithValues: (values) => <p>{`Lines: ${values.lines.length}`}</p>,
      children: <p>Static body</p>,
      fields: {},
    });

    fireEvent.click(screen.getByRole('button', { name: 'Custom open' }));
    expect(screen.getByText('Custom title')).toBeInTheDocument();
    expect(screen.getByText('Static body')).toBeInTheDocument();
    expect(screen.getByText('Lines: 2')).toBeInTheDocument();
  });

  it('shows a spinner, or the empty message, instead of the lines', () => {
    renderModal({ showModal: true, formProps: { isLoading: true } });
    expect(screen.queryAllByRole('row')).toHaveLength(0);
    expect(screen.getByText('Edit request')).toBeInTheDocument();
  });

  it('shows the empty message when there are no lines', () => {
    renderModal({
      showModal: true,
      initialValues: { name: 'Empty', lines: [] },
      formProps: { isEmptyData: true, emptyDataMessageId: 'react.lines.empty', defaultEmptyDataMessage: 'No lines yet' },
    });

    expect(screen.getByText('No lines yet')).toBeInTheDocument();
  });

  it('styles, adds tooltips to and hides lines by their values', () => {
    renderModal({
      showModal: true,
      fields: fields({
        getDynamicRowAttr: ({ rowValues }) => {
          if (rowValues.quantity === '2') {
            return { hideRow: true };
          }
          return { className: 'highlighted', tooltip: 'Check this line' };
        },
      }),
    });

    const rows = screen.getAllByRole('row');
    expect(rows).toHaveLength(1);
    expect(rows[0]).toHaveClass('table-row', 'even-row', 'highlighted');
    expect(within(rows[0]).getByText('Amoxicillin')).toBeInTheDocument();
  });

  it('renders lines with their sub lines', () => {
    renderModal({
      showModal: true,
      initialValues: {
        lines: [{
          product: { name: 'Amoxicillin' },
          quantity: '5',
          splitLines: [{ lot: 'A', quantity: '2' }, { lot: 'B', quantity: '3' }],
        }],
      },
      fields: fields({ rowComponent: TableRowWithSubfields, subfieldKey: 'splitLines' }),
    });

    // The parent line and its two sub lines, numbered like the parent for the stripes
    const rows = screen.getAllByRole('row');
    expect(rows).toHaveLength(3);
    rows.forEach((row) => expect(row).toHaveClass('even-row'));
    expect(lineInputs('Lot').map((input) => input.value)).toEqual(['', 'A', 'B']);

    // Arrow down moves within the sub lines, Ctrl + arrow down copies within them
    const [, firstSubLot, secondSubLot] = lineInputs('Lot');
    fireEvent.keyDown(firstSubLot, { keyCode: 40 });
    expect(secondSubLot).toHaveFocus();
    fireEvent.keyUp(secondSubLot, { keyCode: 40 });
    fireEvent.keyDown(firstSubLot, { keyCode: 17 });
    fireEvent.keyDown(firstSubLot, { keyCode: 40 });
    expect(secondSubLot).toHaveValue('A');
  });

  it('hides the sub lines when the line asks for it', () => {
    renderModal({
      showModal: true,
      initialValues: {
        lines: [{ product: { name: 'Amoxicillin' }, splitLines: [{ lot: 'A' }] }],
      },
      fields: fields({
        rowComponent: TableRowWithSubfields,
        subfieldKey: 'splitLines',
        getDynamicRowAttr: () => ({ hideSubfields: true }),
      }),
    });

    expect(screen.getAllByRole('row')).toHaveLength(1);
  });

  it('virtualizes the lines when the table is paginated', () => {
    const loadMoreRows = jest.fn(() => Promise.resolve());
    renderModal({
      showModal: true,
      fields: fields({ virtualized: true }),
      formProps: {
        totalCount: 3,
        loadMoreRows,
        isRowLoaded: ({ index }) => index < 2,
        isFirstPageLoaded: false,
      },
    }, { isPaginated: true, pageSize: 10 });

    // The two loaded lines, then a placeholder for the one still loading
    expect(screen.getAllByRole('row')).toHaveLength(2);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(loadMoreRows).toHaveBeenCalled();
    expect(loadMoreRows.mock.calls[0][0]).toMatchObject({ startIndex: 2 });
  });

  it('virtualizes lines with sub lines, leaving out hidden lines and loaded pages', () => {
    const loadMoreRows = jest.fn();
    renderModal({
      showModal: true,
      initialValues: {
        lines: [
          { product: { name: 'Amoxicillin' }, splitLines: [{ lot: 'A' }, { lot: 'B' }] },
          { product: { name: 'Bandage' }, splitLines: [{ lot: 'C' }], hidden: true },
          { product: { name: 'Gauze' }, splitLines: [{ lot: 'D' }] },
        ],
      },
      fields: fields({
        virtualized: true,
        rowComponent: TableRowWithSubfields,
        subfieldKey: 'splitLines',
        getDynamicRowAttr: ({ rowValues }) => (rowValues?.hidden ? { hideRow: true } : {}),
      }),
      formProps: {
        totalCount: 3,
        loadMoreRows,
        isRowLoaded: () => true,
        isFirstPageLoaded: true,
      },
    }, { isPaginated: true, pageSize: 10 });

    // Amoxicillin with its two sub lines and Gauze with one; Bandage is hidden
    expect(screen.getByText('Amoxicillin')).toBeInTheDocument();
    expect(screen.getByText('Gauze')).toBeInTheDocument();
    expect(screen.queryByText('Bandage')).toBeNull();
    expect(lineInputs('Lot').map((input) => input.value)).toEqual(['', 'A', 'B', '', 'D']);
    expect(loadMoreRows).not.toHaveBeenCalled();
  });
});
