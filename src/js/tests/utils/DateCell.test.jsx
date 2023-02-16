import React from 'react';

import { render, screen, within } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import store from 'store';

import DateCell from 'components/DataTable/DateCell';

import '@testing-library/jest-dom';

let dateCell;


describe('date cell', () => {
  it('should contain proper text when date provided', () => {
    dateCell = (
      <Router>
        <DateCell
          tableDateFormat="MM DD, yyyy"
          value="2023-02-09"
          store={store}
        />
      </Router>
    );
    render(dateCell);
    const { getByText } = within(screen.getByTestId('table-cell'));
    expect(getByText('Feb 09, 2023')).toBeInTheDocument();
  });

  it('should contain default value when date not provided', () => {
    dateCell = (
      <Router>
        <DateCell
          tableDateFormat="MM DD, yyyy"
          value={undefined}
          store={store}
        />
      </Router>
    );
    render(dateCell);
    const { getByText } = within(screen.getByTestId('table-cell'));
    expect(getByText('-')).toBeInTheDocument();
  });

  it('should contain proper text when date provided with hour', () => {
    dateCell = (
      <Router>
        <DateCell
          tableDateFormat="MM DD, yyyy"
          value="2023-02-17T12:00:00Z"
          store={store}
        />
      </Router>
    );
    render(dateCell);
    const { getByText } = within(screen.getByTestId('table-cell'));
    expect(getByText('Feb 17, 2023')).toBeInTheDocument();
  });
});
