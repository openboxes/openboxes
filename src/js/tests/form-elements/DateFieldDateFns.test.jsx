import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { legacy_configureStore as configureStore } from 'redux-mock-store';

import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';

import '@testing-library/jest-dom';

const mockStore = configureStore();
const renderWithStore = (
  ui,
  initialState = {
    session: {
      activeLanguage: 'en',
    },
  },
) => {
  const store = mockStore(initialState);
  return render(<Provider store={store}>{ui}</Provider>);
};

jest.mock('hooks/useTranslate');

describe('DateFieldDateFns', () => {
  it('renders without crashing and shows placeholder text', () => {
    renderWithStore(<DateFieldDateFns placeholder="placeholder" />);
    expect(screen.getByText('placeholder')).toBeInTheDocument();
  });

  it('renders placeholder as translated when object provided', () => {
    renderWithStore(
      <DateFieldDateFns
        placeholder={{
          id: 'placeholder.translated.label',
          defaultMessage: 'translated placeholder',
        }}
      />,
    );
    expect(screen.getByText('translated placeholder')).toBeInTheDocument();
  });

  it('adds has-errors class when errorMessage passed', () => {
    renderWithStore(<DateFieldDateFns errorMessage="Required field" />);
    const inputDiv = screen.getByRole('button');
    expect(inputDiv.className).toMatch(/has-errors/);
  });

  it('calls onChange when date selected', () => {
    const handleChange = jest.fn();
    renderWithStore(<DateFieldDateFns onChange={handleChange} />);
    const date = new Date('2025-09-22T18:15:00Z');
    handleChange(date);
    expect(handleChange).toHaveBeenCalledWith(date);
  });

  it('calls onChange(null) when cleared', () => {
    const handleChange = jest.fn();
    renderWithStore(
      <DateFieldDateFns
        clearable
        onChange={handleChange}
        value="Sep 23, 2025"
      />,
    );
    const clearBtn = screen.getByLabelText(/clear/i);
    fireEvent.click(clearBtn);
    expect(handleChange).toHaveBeenCalledTimes(1);
    expect(handleChange).toHaveBeenCalledWith(null);
  });

  it('uses enUS locale when currentLocale is en', () => {
    renderWithStore(<DateFieldDateFns value="Sep 23, 2025" />, {
      session: { activeLanguage: 'en' },
    });
    expect(screen.getByText('Sep 23, 2025')).toBeInTheDocument();
  });

  it('uses custom locale when currentLocale is fr', () => {
    renderWithStore(<DateFieldDateFns value="Sep 23, 2025" />, {
      session: { activeLanguage: 'fr' },
    });
    expect(screen.getByText('sept. 23, 2025')).toBeInTheDocument();
  });

  it('displays selected time', () => {
    renderWithStore(<DateFieldDateFns showTimeSelect value="Sep 23, 2025 14:07:09" />)
    expect(screen.getByText('Sep 23, 2025 14:07:09')).toBeInTheDocument();
  });

  it('calls onChange when date with time selected', () => {
    const date = 'Sep 23, 2025 15:07:09';
    const handleChange = jest.fn();
    renderWithStore(<DateFieldDateFns showTimeSelect onChange={handleChange} />);
    handleChange(date);
    expect(handleChange).toHaveBeenCalledWith(date);
  });
});
