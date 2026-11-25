import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

import Wizard from 'components/wizard/Wizard';
import withRouter from 'utils/withRouter';

import store from '../../store';

const idsSeenBySecondPage = [];

const FirstPage = withRouter(({ navigate, nextPage }) => {
  const save = () => Promise.resolve({ id: '123' })
    .then((saved) => {
      navigate(`/create/${saved.id}`);
      nextPage(saved);
    });

  return <button type="button" onClick={save}>Next</button>;
});

const SecondPage = withRouter(({ match }) => {
  idsSeenBySecondPage.push(match.params.id);

  return <div>{`Second page for ${match.params.id}`}</div>;
});

const renderWizard = () => render(
  <MemoryRouter initialEntries={['/create']}>
    <Provider store={store}>
      <Routes>
        <Route
          path="/create/:id?"
          element={(
            <Wizard
              title={[{ title: '' }]}
              currentPage={1}
              prevPage={1}
              pageList={[FirstPage, SecondPage]}
              stepList={['Create', 'Add items']}
            />
          )}
        />
      </Routes>
    </Provider>
  </MemoryRouter>,
);

describe('wizard navigation', () => {
  it('renders the next page with the id the previous page navigated to', async () => {
    renderWizard();

    fireEvent.click(screen.getByText('Next'));

    expect(await screen.findByText('Second page for 123')).toBeTruthy();
    expect(idsSeenBySecondPage).toEqual(expect.not.arrayContaining([undefined]));
  });
});
