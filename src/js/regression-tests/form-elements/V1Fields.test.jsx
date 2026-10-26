// Not covered on OBAF (OBAF lacks the code): the two v2 ProductSelectField cases
// ('searches the products as the user types and selects one',
// 'does not search for fewer characters than the minimum');
// components/form-elements/v2/ProductSelectField
// does not exist in OBAF.
import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import _ from 'lodash';
import { Form } from 'react-final-form';
import { Provider } from 'react-redux';
import testStore from 'regression-tests/data-table/testStore';

import Button from 'components/form-elements/Button';
import ColorPickerField from 'components/form-elements/ColorPickerField';
import DateField from 'components/form-elements/DateField';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FileField from 'components/form-elements/FileField';
import FilterInput from 'components/form-elements/FilterInput';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import LabelField from 'components/form-elements/LabelField';
import ProductSelectFieldV1 from 'components/form-elements/ProductSelectField';
import SearchField from 'components/form-elements/SearchField';
import SelectField from 'components/form-elements/SelectField';
import DateFormat from 'consts/dateFormat';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';

import '@testing-library/jest-dom';

// The product search goes through the client.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

// Select menus and date pickers open into #root, as in the application.
beforeAll(() => {
  document.body.appendChild(Object.assign(document.createElement('div'), { id: 'root' }));
});

afterEach(() => jest.clearAllMocks());

// Renders the fields in a final form; returns a getter for the current form values.
const renderForm = (fields, { initialValues = {}, formProps = {} } = {}) => {
  let current = {};
  render(
    <Provider store={testStore()}>
      <Form
        onSubmit={() => {}}
        initialValues={initialValues}
        render={({ values }) => {
          current = values;
          return (
            <form>
              {_.map(fields, (config, name) => renderFormField(config, name, formProps))}
            </form>
          );
        }}
      />
    </Provider>,
  );
  return () => current;
};

const field = (label) => screen.getAllByLabelText(label)
  .find((element) => element.dataset.testid === 'form-field');

const dayOfMonth = (day) => [...document.querySelectorAll('.react-datepicker__day:not(.react-datepicker__day--outside-month)')]
  .find((element) => element.textContent === String(day));

describe('search, color and file fields', () => {
  it('types into the search field and clears it', () => {
    const values = renderForm({
      search: { type: SearchField, label: 'react.search.label', defaultMessage: 'Search' },
    });

    const input = within(field('Search')).getByTestId('input');
    fireEvent.change(input, { target: { value: 'ibu' } });
    expect(values().search).toBe('ibu');
    // Clearing empties the field, which the form keeps as no value
    fireEvent.click(within(field('Search')).getByRole('button'));
    expect(values().search).toBeUndefined();
    expect(input).toHaveValue('');
  });

  it('types a color, shows it, and clears it', () => {
    const values = renderForm({
      color: { type: ColorPickerField, label: 'react.color.label', defaultMessage: 'Color' },
    }, { initialValues: { color: 'ff0000' } });

    const swatch = field('Color').querySelector('.input-group-text');
    expect(swatch).toHaveStyle({ backgroundColor: '#ff0000' });
    fireEvent.change(within(field('Color')).getByTestId('input'), { target: { value: '00ff00' } });
    expect(values().color).toBe('00ff00');
    fireEvent.click(within(field('Color')).getByRole('button'));
    expect(values().color).toBe(null);
  });

  it('takes the first chosen file', async () => {
    const values = renderForm({
      file: { type: FileField, label: 'react.file.label', defaultMessage: 'File' },
    });

    expect(screen.getByText('No file chosen')).toBeInTheDocument();
    const file = new File(['a'], 'stock.csv', { type: 'text/csv' });
    fireEvent.change(field('File').querySelector('input[type="file"]'), { target: { files: [file] } });

    expect(await screen.findByText('stock.csv')).toBeInTheDocument();
    expect(values().file.name).toBe('stock.csv');
  });
});

describe('label field', () => {
  it('shows the formatted value with its label', () => {
    renderForm({
      code: {
        type: LabelField,
        label: 'react.code.label',
        defaultMessage: 'Code',
        attributes: { formatValue: (value) => value.toUpperCase(), className: 'code' },
      },
    }, { initialValues: { code: 'ab-1' } });

    expect(screen.getByText('AB-1')).toHaveClass('code');
    expect(screen.getByText('Code').tagName).toBe('LABEL');
  });

  it('takes a component as the label, and shows a link in a table cell', () => {
    renderForm({
      title: { type: LabelField, label: () => <em>Custom label</em>, defaultMessage: 'Title' },
    }, { initialValues: { title: 'Plain' } });
    expect(screen.getByText('Custom label')).toBeInTheDocument();
    expect(screen.getByText('Plain')).toBeInTheDocument();

    render(
      <LabelField
        fieldName="lines[0].order"
        arrayField
        fieldValue="PO-1"
        fieldConfig={{
          defaultMessage: 'Order',
          getDynamicAttr: ({ fieldValue }) => ({ url: `/openboxes/order/show/${fieldValue}`, numberField: true }),
        }}
      />,
    );
    const link = screen.getByRole('link', { name: 'PO-1' });
    expect(link).toHaveAttribute('href', '/openboxes/order/show/PO-1');
    expect(link).toHaveAttribute('target', '_blank');
    expect(link.parentElement).toHaveClass('text-right');
  });
});

describe('filter select field', () => {
  const options = [{ value: 'a', label: 'Aspirin' }, { value: 'b', label: 'Bandage' }];

  it('selects several options with checkboxes and counts them', () => {
    const values = renderForm({
      products: {
        type: FilterSelectField,
        label: 'react.products.label',
        defaultMessage: 'Products',
        attributes: { options, multi: true, filterElement: true },
      },
    });

    const input = field('Products').querySelector('input');
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    expect(screen.getByTestId('custom-select-dropdown-menu')).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(2);
    fireEvent.click(screen.getByText('Bandage'));

    expect(values().products.map(({ value }) => value)).toEqual(['b']);
    expect(screen.getByTestId('filter-count-indicator')).toHaveTextContent('1');
    expect(field('Products').querySelector('.filter-select')).toHaveClass('filter-select-has-value');
  });

  it('does not clear the selection with backspace on an empty search', () => {
    const values = renderForm({
      product: {
        type: FilterSelectField,
        label: 'react.product.label',
        defaultMessage: 'Product',
        attributes: { options, filterElement: true },
      },
    }, { initialValues: { product: options[0] } });

    const input = field('Product').querySelector('input');
    fireEvent.keyDown(input, { key: 'Backspace', keyCode: 8 });
    expect(values().product).toEqual(options[0]);

    fireEvent.change(input, { target: { value: 'Ban' } });
    expect(input).toHaveValue('Ban');
  });
});

describe('select field', () => {
  it('selects an option, telling the field attributes about the change and the blur', () => {
    const onChange = jest.fn();
    const onBlur = jest.fn();
    const values = renderForm({
      reason: {
        type: SelectField,
        label: 'react.reason.label',
        defaultMessage: 'Reason',
        attributes: {
          options: [{ value: 'DAMAGED', label: 'Damaged' }, { value: 'EXPIRED', label: 'Expired' }],
          onChange,
          onBlur,
          className: 'reason',
        },
      },
    });

    const input = field('Reason').querySelector('input');
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('Expired'));
    fireEvent.blur(input);

    expect(values().reason).toMatchObject({ value: 'EXPIRED' });
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ value: 'EXPIRED' }));
    expect(onBlur).toHaveBeenCalled();
    // The class name goes to the field wrapper
    expect(field('Reason')).toHaveClass('reason');
  });
});

describe('date field', () => {
  it('picks a date in the given format, and takes a typed one as typed', () => {
    const values = renderForm({
      date: {
        type: DateField,
        label: 'react.date.label',
        defaultMessage: 'Date',
        attributes: { dateFormat: 'MM/DD/YYYY' },
      },
    }, { initialValues: { date: '02/09/2023' } });

    const input = within(field('Date')).getByRole('textbox');
    expect(input).toHaveValue('02/09/2023');
    fireEvent.click(input);
    fireEvent.click(dayOfMonth(15));
    expect(values().date).toBe('02/15/2023');

    fireEvent.change(input, { target: { value: '03/01/2023' } });
    expect(values().date).toBe('03/01/2023');
  });

  it('moves to the neighbouring field with the arrow keys and passes Tab on', () => {
    const arrows = {
      arrowLeft: jest.fn(() => true),
      arrowRight: jest.fn(() => false),
      arrowUp: jest.fn(() => true),
      arrowDown: jest.fn(() => true),
      onTabPress: jest.fn(),
    };
    renderForm({
      date: {
        type: DateField, label: 'react.date.label', defaultMessage: 'Date', attributes: arrows,
      },
    });

    const input = within(field('Date')).getByRole('textbox');
    [37, 38, 39, 40, 9].forEach((keyCode) => fireEvent.keyDown(input, { keyCode }));

    Object.values(arrows).forEach((handler) => expect(handler).toHaveBeenCalledTimes(1));
  });

  it('shows the localized format as the placeholder when asked', () => {
    renderForm({
      date: {
        type: DateField,
        label: 'react.date.label',
        defaultMessage: 'Date',
        attributes: {
          localizeDate: true,
          showLocalizedPlaceholder: true,
          localizedDateFormat: DateFormat.COMMON,
        },
      },
    });

    expect(within(field('Date')).getByRole('textbox')).toHaveAttribute('placeholder', 'DD/MMM/YYYY');
  });
});

describe('date filter', () => {
  it('shows the label and the placeholder, then the picked date, and clears it', () => {
    const values = renderForm({
      from: {
        type: DateFilter,
        label: 'react.from.label',
        defaultMessage: 'From',
        attributes: {
          label: 'react.filter.from', defaultMessage: 'From:', placeholder: 'Any date', dateFormat: 'MM/DD/YYYY',
        },
      },
    }, { initialValues: { from: '02/09/2023' } });

    const filter = screen.getByTestId('date-filter');
    expect(filter).toHaveClass('date-picker__wrapper--valid');
    expect(within(filter).getByText('02/09/2023')).toBeInTheDocument();

    fireEvent.click(within(filter).getByText('02/09/2023'));
    expect(filter).toHaveClass('date-picker__wrapper--focused');
    fireEvent.click(dayOfMonth(15));
    expect(values().from).toBe('02/15/2023');

    fireEvent.click(within(filter).getByRole('button', { name: 'Pick date' }));
    expect(values().from).toBeNull();
    expect(within(filter).getByText('Any date')).toBeInTheDocument();
    expect(filter).not.toHaveClass('date-picker__wrapper--valid');
  });

  it('opens with Enter and shows a localized date when asked', () => {
    renderForm({
      from: {
        type: DateFilter,
        label: 'react.from.label',
        defaultMessage: 'From',
        attributes: { dateFormat: 'MM/DD/YYYY', localizeDate: true, localizedDateFormat: DateFormat.COMMON },
      },
    }, { initialValues: { from: '02/09/2023' } });

    const filter = screen.getByTestId('date-filter');
    expect(within(filter).getByText('09/Feb/2023')).toBeInTheDocument();
    fireEvent.keyDown(within(filter).getAllByRole('button')[0], { key: 'Enter' });
    expect(document.querySelector('.react-datepicker')).not.toBeNull();
  });
});

describe('product select fields', () => {
  const products = [
    {
      id: 'p1', productCode: 'IB1', name: 'Ibuprofen 200mg', exactMatch: false,
    },
    {
      id: 'p2', productCode: 'IBU', name: 'Ibuprofen 400mg', exactMatch: true,
    },
  ];

  beforeEach(() => {
    apiClient.get.mockResolvedValue({ data: { data: products } });
  });

  it('selects the exact match on Enter in the form field', async () => {
    const onExactProductSelected = jest.fn();
    const values = renderForm({
      product: {
        type: ProductSelectFieldV1,
        label: 'react.product.label',
        defaultMessage: 'Product',
        attributes: { locationId: 'loc1', onExactProductSelected },
      },
    });

    const input = field('Product').querySelector('input');
    fireEvent.change(input, { target: { value: 'IBU' } });
    await screen.findByText(/IBU - Ibuprofen 400mg/);
    fireEvent.keyDown(input, { key: 'Enter', keyCode: 13 });

    await waitFor(() => expect(onExactProductSelected).toHaveBeenCalled());
    expect(onExactProductSelected.mock.calls[0][0].product).toMatchObject({ id: 'p2' });
    expect(values().product).toMatchObject({ id: 'p2' });
  });
});

describe('button and filter input', () => {
  it('renders a translated button with icons, as a dropdown when asked', () => {
    const onClick = jest.fn();
    render(
      <Provider store={testStore()}>
        <Button
          label="react.save"
          defaultLabel="Save"
          variant="secondary"
          className="extra"
          isDropdown
          StartIcon={<i data-testid="start" />}
          EndIcon={<i data-testid="end" />}
          onClick={onClick}
        />
      </Provider>,
    );

    const button = screen.getByRole('button', { name: 'Save' });
    expect(button).toHaveClass('secondary-button', 'dropdown-toggle', 'extra');
    expect(button).toHaveAttribute('data-toggle', 'dropdown');
    expect(screen.getByTestId('start')).toBeInTheDocument();
    expect(screen.getByTestId('end')).toBeInTheDocument();
    fireEvent.click(button);
    expect(onClick).toHaveBeenCalled();
  });

  it('filters and clears from the filter input', () => {
    const onChange = jest.fn();
    const onClear = jest.fn();
    const { rerender } = render(
      <Provider store={testStore()}>
        <FilterInput itemFilter="" onChange={onChange} onClear={onClear} inputRef={() => {}} />
      </Provider>,
    );

    const input = screen.getByPlaceholderText('Search...');
    fireEvent.change(input, { target: { value: 'amox' } });
    expect(onChange).toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: 'Clear' })).toBeNull();

    rerender(
      <Provider store={testStore()}>
        <FilterInput itemFilter="amox" onChange={onChange} onClear={onClear} inputRef={() => {}} />
      </Provider>,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Clear' }));
    fireEvent.keyPress(screen.getByRole('button', { name: 'Clear' }), { key: 'Enter', charCode: 13 });
    expect(onClear).toHaveBeenCalledTimes(2);
  });
});
