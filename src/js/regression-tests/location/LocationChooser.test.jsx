import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
}));

// Switching the location is a request that resolves; the follow-up fetches are recorded.
jest.mock('actions', () => ({
  changeCurrentLocation: jest.fn(() => (dispatch) => {
    dispatch({ type: 'changeCurrentLocation' });
    return Promise.resolve();
  }),
  fetchMenuConfig: jest.fn(() => ({ type: 'fetchMenuConfig' })),
  fetchSessionInfo: jest.fn(() => ({ type: 'fetchSessionInfo' })),
}));

// The chooser modal binds to #root when it loads, so the element exists before.
document.body.appendChild(Object.assign(document.createElement('div'), { id: 'root' }));
/* eslint-disable global-require */
const LocationChooser = require('components/location/LocationChooser').default;
const LocationChooserButton = require('components/location/LocationChooser/LocationChooserButton/LocationChooserButton').default;
const LocationButton = require('components/location/LocationChooser/LocationChooserModal/LocationButton').default;
const { changeCurrentLocation } = require('actions');
const apiClient = require('utils/apiClient').default;
/* eslint-enable global-require */

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

const LOCATIONS = [
  {
    id: 'l1', name: 'Kigali depot', organizationName: 'Partners', locationGroup: { name: 'East' }, backgroundColor: '#ab12cd',
  },
  {
    id: 'l2', name: 'Butaro', organizationName: 'Partners', locationGroup: null,
  },
  { id: 'l3', name: 'Loose depot', organizationName: null },
  {
    id: 'l4', name: 'Boston', organizationName: 'Alpha', locationGroup: { name: 'North' },
  },
];

const renderChooser = (session = {}) => {
  const store = configureStore([thunk])({
    localize,
    session: { currentLocation: { name: 'Main Warehouse', backgroundColor: 'ffffff' }, logoLabel: 'TEST', ...session },
  });
  const utils = render(
    <Provider store={store}>
      <LocationChooser />
    </Provider>,
  );
  return { ...utils, store };
};

describe('LocationChooser', () => {
  beforeEach(() => {
    apiClient.get.mockResolvedValue({ data: { data: LOCATIONS } });
  });

  afterEach(() => jest.clearAllMocks());

  it('shows the current location and environment, and loads the locations the user may log into', async () => {
    renderChooser();
    const button = screen.getByLabelText('location-chooser');
    expect(button).toHaveTextContent('Main Warehouse');
    expect(button).toHaveTextContent('TEST');
    // White is no location color.
    expect(button.style.getPropertyValue('--location-color')).toBe('unset');
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/locations', {
      params: {
        locationChooser: true, applyUserFilter: true, locationTypeCode: 'DEPOT', activityCodes: 'MANAGE_INVENTORY',
      },
    }), { timeout: 5000 });
  });

  it('lists the locations by organization and group, and switches to the chosen one', async () => {
    const { store } = renderChooser();
    fireEvent.click(screen.getByLabelText('location-chooser'));

    const modal = await screen.findByTestId('location-chooser-modal');
    expect(within(modal).getByText('Choose Location')).toBeInTheDocument();
    const tabs = within(screen.getByTestId('location-organization-list')).getAllByRole('tab');
    expect(tabs.map((tab) => tab.textContent)).toEqual(['Alpha', 'Partners', 'No organization']);

    fireEvent.click(tabs[1]);
    const panel = within(modal).getByText('Kigali depot').closest('[data-testid="location-list"]');
    const groupTitles = [...panel.querySelectorAll('.location-chooser__modal__group-title')].map((h) => h.textContent);
    expect(groupTitles).toEqual(['East', 'No location group']);
    expect(within(modal).getByText('Kigali depot').closest('button').style.getPropertyValue('--location-color'))
      .toBe('#AB12CD');

    fireEvent.click(within(modal).getByText('Butaro'));
    expect(changeCurrentLocation).toHaveBeenCalledWith(LOCATIONS[1]);
    await waitFor(() => expect(store.getActions().map((action) => action.type))
      .toEqual(['changeCurrentLocation', 'fetchMenuConfig', 'fetchSessionInfo']));
    await waitFor(() => expect(screen.queryByTestId('location-chooser-modal')).not.toBeInTheDocument());
  });

  it('closes the modal with the close button', async () => {
    renderChooser();
    fireEvent.click(screen.getByLabelText('location-chooser'));
    await screen.findByTestId('location-chooser-modal');
    fireEvent.click(screen.getByLabelText('close'));
    await waitFor(() => expect(screen.queryByTestId('location-chooser-modal')).not.toBeInTheDocument());
  });
});

describe('LocationChooserButton', () => {
  it('asks to choose a location when there is none', () => {
    render(<LocationChooserButton onToggle={jest.fn()} location={{}} />);
    expect(screen.getByLabelText('location-chooser')).toHaveTextContent('Choose Location');
  });

  it('shows a long location name in full with a tooltip, and toggles on click', () => {
    const onToggle = jest.fn();
    const name = 'A location with a very long name';
    render(<LocationChooserButton onToggle={onToggle} location={{ name, backgroundColor: '#123456' }} />);
    const button = screen.getByLabelText('location-chooser');
    expect(button).toHaveTextContent(name);
    expect(button.style.getPropertyValue('--location-color')).toBe('#123456');
    expect(button.querySelector('strong')).toBeNull();
    fireEvent.click(button);
    expect(onToggle).toHaveBeenCalled();
  });
});

describe('LocationButton', () => {
  it.each([
    [undefined, 'unset'],
    ['#FFFF', 'unset'],
    ['ff00aa', '#FF00AA'],
  ])('uses %s as the location color %s', (backgroundColor, expected) => {
    render(<LocationButton location={{ name: 'Depot', backgroundColor }} onClick={jest.fn()} />);
    expect(screen.getByRole('button').style.getPropertyValue('--location-color')).toBe(expected);
  });

  it('chooses its location on click', () => {
    const onClick = jest.fn();
    const location = { id: 'l1', name: 'Depot' };
    render(<LocationButton location={location} onClick={onClick} />);
    fireEvent.click(screen.getByText('Depot'));
    expect(onClick).toHaveBeenCalledWith(location);
  });
});
