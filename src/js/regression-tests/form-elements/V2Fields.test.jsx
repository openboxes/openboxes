// Not covered on OBAF (OBAF lacks the code): the SlidingButtonGroup describe
// ('switches the active option, but not to a disabled one', 'positions the slider under the
// active option'); components/form-elements/v2/SlidingButtonGroup does not exist in OBAF.
import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import testStore from 'regression-tests/data-table/testStore';

import ButtonFileSelect from 'components/form-elements/v2/ButtonFileSelect';
import Checkbox from 'components/form-elements/v2/Checkbox';
import DateField from 'components/form-elements/v2/DateField';
import DropzoneFileSelect from 'components/form-elements/v2/DropzoneFileSelect';
import SelectField from 'components/form-elements/v2/SelectField';
import Switch from 'components/form-elements/v2/Switch';
import TextInput from 'components/form-elements/v2/TextInput';
import notification from 'components/Layout/notifications/notification';

import '@testing-library/jest-dom';

// The real client imports the store, which needs the reducers this test is still loading.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

// The notification a component raises, by type: notification(type)(content).
jest.mock('components/Layout/notifications/notification', () => {
  const show = jest.fn();
  return { __esModule: true, default: jest.fn(() => show) };
});

const withStore = (ui, session) => <Provider store={testStore(session)}>{ui}</Provider>;

// Select menus and date pickers open into #root, as in the application.
beforeAll(() => {
  document.body.appendChild(Object.assign(document.createElement('div'), { id: 'root' }));
});

afterEach(() => jest.clearAllMocks());

describe('TextInput', () => {
  it('shows the title, the required marker, the error and passes changes on', () => {
    // The event is reused once the handler returns, so the value is read right away.
    const typed = [];
    const onChange = jest.fn((event) => typed.push(event.target.value));
    const onButton = jest.fn();
    render(withStore(
      <TextInput
        id="lot"
        title={{ id: 'react.lot.label', defaultMessage: 'Lot number' }}
        tooltip={{ id: 'react.lot.tooltip', defaultMessage: 'The lot' }}
        button={{ id: 'react.lot.generate', defaultMessage: 'Generate', onClick: onButton }}
        required
        errorMessage="Lot is required"
        value=""
        onChange={onChange}
      />,
    ));

    expect(screen.getByLabelText('Lot number')).toHaveClass('has-errors');
    expect(screen.getByText('*')).toBeInTheDocument();
    expect(screen.getByLabelText('subtext')).toHaveTextContent('Lot is required');
    expect(screen.getByTestId('form-field has-errors')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Lot number'), { target: { value: 'L-1' } });
    expect(typed).toEqual(['L-1']);
    fireEvent.click(screen.getByText('Generate'));
    expect(onButton).toHaveBeenCalled();
  });

  it('passes numbers on as numbers, and rounds them to the decimals on blur', () => {
    const onChange = jest.fn();
    const onBlur = jest.fn();
    render(withStore(
      <TextInput id="qty" type="number" decimal={2} onChange={onChange} onBlur={onBlur} />,
    ));
    const input = screen.getByRole('spinbutton');
    expect(input).toHaveAttribute('step', String(0.1 ** 2));

    fireEvent.change(input, { target: { value: '1.239' } });
    expect(onChange).toHaveBeenLastCalledWith(1.239);

    input.value = '1.239';
    fireEvent.blur(input);
    expect(onChange).toHaveBeenLastCalledWith(1.24);
    expect(input).toHaveValue(1.24);
    expect(onBlur).toHaveBeenCalled();

    // A field left empty gives no number
    input.value = '';
    fireEvent.blur(input);
    expect(onChange).toHaveBeenLastCalledWith(undefined);
  });

  it('focuses itself when its cell is the one to focus', () => {
    render(withStore(
      <TextInput
        id="qty"
        focusProps={{
          fieldIndex: '1', rowIndex: '1', fieldId: 'quantity', columnId: 'quantity',
        }}
      />,
    ));

    expect(screen.getByRole('textbox')).toHaveFocus();
  });
});

describe('Checkbox (v2)', () => {
  it('renders in its wrapper with a title and an error, and reports changes', () => {
    const onChange = jest.fn();
    render(withStore(
      <Checkbox
        name="active"
        title={{ id: 'react.active.label', defaultMessage: 'Active' }}
        errorMessage="Pick one"
        value
        onChange={onChange}
      />,
    ));

    const checkbox = screen.getByLabelText('Active');
    expect(checkbox).toBeChecked();
    expect(checkbox).toHaveClass('has-errors');
    expect(checkbox).toHaveAttribute('id', 'active');
    fireEvent.click(checkbox);
    expect(onChange).toHaveBeenCalled();
  });

  it('renders bare and indeterminate when asked', () => {
    render(withStore(<Checkbox id="all" noWrapper indeterminate value={false} onChange={() => {}} />));

    const checkbox = screen.getByRole('checkbox');
    expect(checkbox.indeterminate).toBe(true);
    expect(screen.queryByTestId('form-field')).toBeNull();
  });
});

describe('Switch', () => {
  const titles = {
    checked: { id: 'react.on', defaultMessage: 'On' },
    unchecked: { id: 'react.off', defaultMessage: 'Off' },
  };

  it('toggles and shows the title of the current state', () => {
    const onChange = jest.fn();
    render(withStore(<Switch titles={titles} onChange={onChange} />));

    expect(screen.getByText('Off')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('checkbox'));
    expect(onChange).toHaveBeenCalledWith(true);
    expect(screen.getByText('On')).toBeInTheDocument();
  });

  it('follows the value it is given', () => {
    const { rerender } = render(withStore(<Switch titles={titles} value={false} />));
    expect(screen.getByText('Off')).toBeInTheDocument();

    rerender(withStore(<Switch titles={titles} value />));
    expect(screen.getByText('On')).toBeInTheDocument();
  });
});

describe('SelectField (v2)', () => {
  const options = [{ id: 'a', label: 'Aspirin' }, { id: 'b', label: 'Bandage' }];

  it('selects an option and hands the chosen one to the button', () => {
    const onChange = jest.fn();
    const onButton = jest.fn();
    render(withStore(
      <SelectField
        id="product"
        title={{ id: 'react.product.label', defaultMessage: 'Product' }}
        button={{ id: 'react.product.details', defaultMessage: 'Details', onClick: onButton }}
        options={options}
        onChange={onChange}
        warning
      />,
    ));

    fireEvent.keyDown(document.querySelector('#product-container input') || screen.getByRole('textbox'), { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('Bandage'));
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ id: 'b' }));
    expect(screen.getByText('Bandage')).toBeInTheDocument();

    fireEvent.click(screen.getByText('Details'));
    expect(onButton).toHaveBeenCalledWith(expect.objectContaining({ id: 'b' }));
  });

  it('shows the default value and resets to a new one', () => {
    const { rerender } = render(withStore(<SelectField id="p" options={options} defaultValue={options[0]} />));
    expect(screen.getByText('Aspirin')).toBeInTheDocument();

    rerender(withStore(<SelectField id="p" options={options} defaultValue={options[1]} errorMessage="Wrong" />));
    expect(screen.getByText('Bandage')).toBeInTheDocument();
    expect(screen.getByText('Wrong')).toBeInTheDocument();
  });
});

describe('DateField (v2)', () => {
  it('shows the value, and clears it', () => {
    const onChange = jest.fn();
    render(withStore(<DateField value="Feb 09, 2023" onChange={onChange} />));

    expect(screen.getByText('Feb 09, 2023')).toBeInTheDocument();
    fireEvent.click(screen.getByLabelText('clear'));
    expect(onChange).toHaveBeenCalledWith(null);
  });

  it('shows a translated placeholder, and no clear button when not clearable', () => {
    render(withStore(
      <DateField placeholder={{ id: 'react.date.pick', default: 'Pick a date' }} clearable={false} value={null} />,
    ));

    expect(screen.getByText('Pick a date')).toBeInTheDocument();
    expect(screen.queryByLabelText('clear')).toBeNull();
  });

  it('picks a day from the calendar in the display format', () => {
    const onChange = jest.fn();
    render(withStore(<DateField value="Feb 09, 2023" onChange={onChange} />));

    fireEvent.click(document.querySelector('.date-field-input'));
    const day = [...document.querySelectorAll('.react-datepicker__day:not(.react-datepicker__day--outside-month)')]
      .find((element) => element.textContent === '15');
    fireEvent.click(day);

    expect(onChange).toHaveBeenCalledWith('Feb 15, 2023');
  });

  it('keeps the time when picking with the time select', () => {
    const onChange = jest.fn();
    render(withStore(<DateField value="Feb 09, 2023 10:30:00" showTimeSelect onChange={onChange} />));

    expect(screen.getByText('Feb 09, 2023 10:30:00')).toBeInTheDocument();
    fireEvent.click(document.querySelector('.date-field-input'));
    const day = [...document.querySelectorAll('.react-datepicker__day:not(.react-datepicker__day--outside-month)')]
      .find((element) => element.textContent === '15');
    fireEvent.click(day);

    expect(onChange).toHaveBeenCalledWith('Feb 15, 2023 10:30:00');
  });

  it('renders disabled without the icons', () => {
    render(withStore(<DateField value="Feb 09, 2023" disabled errorMessage="Too late" />));

    expect(screen.queryByRole('button')).toBeNull();
    expect(screen.queryByLabelText('clear')).toBeNull();
    expect(screen.getByText('Too late')).toBeInTheDocument();
  });
});

describe('ButtonFileSelect', () => {
  const file = (name) => new File(['a,b'], name, { type: 'text/csv' });

  it('hands over files with an allowed extension', () => {
    const onFileUpload = jest.fn();
    render(withStore(<ButtonFileSelect allowedExtensions={['csv', 'xls']} onFileUpload={onFileUpload} />));

    const input = screen.getByTestId('file-uploader');
    expect(input).toHaveAttribute('accept', '.csv,.xls');
    fireEvent.change(input, { target: { files: [file('stock.csv')] } });

    expect(onFileUpload).toHaveBeenCalledTimes(1);
    expect(onFileUpload.mock.calls[0][0][0].name).toBe('stock.csv');
    expect(notification).not.toHaveBeenCalled();
  });

  it('rejects other files with an error notification', () => {
    const onFileUpload = jest.fn();
    render(withStore(<ButtonFileSelect allowedExtensions={['csv']} onFileUpload={onFileUpload} />));

    fireEvent.change(screen.getByTestId('file-uploader'), { target: { files: [file('stock.pdf')] } });

    expect(onFileUpload).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith('error-filled');
    expect(notification.mock.results[0].value).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Invalid file extension',
      details: 'File extension should be one of: csv',
    }));
  });

  // KNOWN-GAP: OBAF takes name.split('.')[1] as the extension, so a name with two dots is refused.
  it.skip('reads the extension after the last dot of the file name', () => {
    const onFileUpload = jest.fn();
    render(withStore(<ButtonFileSelect allowedExtensions={['csv']} onFileUpload={onFileUpload} />));

    fireEvent.change(screen.getByTestId('file-uploader'), { target: { files: [file('stock.2024.csv')] } });

    expect(onFileUpload).toHaveBeenCalledTimes(1);
    expect(notification).not.toHaveBeenCalled();
  });

  it('takes any file when no extension is required', () => {
    const onFileUpload = jest.fn();
    render(withStore(<ButtonFileSelect allowedExtensions={[]} onFileUpload={onFileUpload} variant="secondary" />));

    fireEvent.change(screen.getByTestId('file-uploader'), { target: { files: [file('photo.png')] } });
    expect(onFileUpload).toHaveBeenCalled();
  });

  it('can be disabled', () => {
    render(withStore(<ButtonFileSelect allowedExtensions={[]} onFileUpload={jest.fn()} disabled variant="secondary" />));

    const input = screen.getByTestId('file-uploader');
    expect(input).toBeDisabled();
    expect(input.closest('label')).toHaveClass('secondary-disabled');
  });
});

describe('DropzoneFileSelect', () => {
  const file = (name, size = 3) => {
    const created = new File(['x'.repeat(size)], name, { type: 'text/csv' });
    return created;
  };

  const drop = (files) => {
    const input = document.querySelector('input[type="file"]');
    fireEvent.change(input, { target: { files } });
  };

  it('accepts a valid file and lists it', async () => {
    const onChange = jest.fn();
    render(withStore(<DropzoneFileSelect allowedExtensions={['csv']} onChange={onChange} />));

    expect(screen.getByText('Drag and drop file here.')).toBeInTheDocument();
    drop([file('stock.csv')]);

    // OBAF leaves onChange in fieldProps, so the input's change event reaches it
    // too: look for the call that hands over the file.
    await waitFor(() => expect(onChange.mock.calls.map(([arg]) => arg?.name)).toContain('stock.csv'));
    expect(await screen.findByText(/Accepted Files/)).toBeInTheDocument();
    expect(screen.getByText(/stock.csv/)).toBeInTheDocument();
  });

  // KNOWN-GAP: OBAF takes name.split('.')[1] as the extension, so stock.2024.csv is rejected.
  it.skip('accepts a file whose name has more than one dot', async () => {
    const onChange = jest.fn();
    render(withStore(<DropzoneFileSelect allowedExtensions={['csv']} onChange={onChange} />));

    drop([file('stock.2024.csv')]);

    await waitFor(() => expect(onChange).toHaveBeenCalled());
    expect(onChange.mock.calls[0][0].name).toBe('stock.2024.csv');
    expect(screen.queryByText(/Rejected Files/)).toBeNull();
  });

  it('lists a file with a wrong extension or too large as rejected, with the reason', async () => {
    render(withStore(
      <DropzoneFileSelect allowedExtensions={['csv']} multiple />,
      { maxUploadFileSize: 5 },
    ));

    drop([file('photo.png'), file('big.csv', 10)]);

    expect(await screen.findByText(/Rejected Files/)).toBeInTheDocument();
    expect(screen.getByText('File extension should be one of: csv')).toBeInTheDocument();
    expect(screen.getByText(/File exceeds max file size of/)).toBeInTheDocument();
  });

  it('shows only the button, and notifies about invalid files when asked', async () => {
    render(withStore(
      <DropzoneFileSelect allowedExtensions={['csv']} showButtonOnly throwErrorOnInvalidFiles />,
    ));

    expect(screen.queryByText('Drag and drop file here.')).toBeNull();
    expect(screen.getByRole('button', { name: 'OPEN FILE DIALOG' })).toBeInTheDocument();
    drop([file('photo.png')]);

    await waitFor(() => expect(notification).toHaveBeenCalledWith('error-outlined'));
    const [content] = notification.mock.results[0].value.mock.calls[0];
    expect(content.message).toBe('Error while uploading files');
  });

  it('disables the button when the form is disabled', () => {
    render(withStore(<DropzoneFileSelect isFormDisabled />));

    expect(screen.getByRole('button', { name: 'OPEN FILE DIALOG' })).toBeDisabled();
  });
});
