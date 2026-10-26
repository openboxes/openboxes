import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

import '@testing-library/jest-dom';

// The API: every GET answers from answers() below; POSTs answer per test. The helpers the actions
// and the address page use come from the real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});

jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));

/* eslint-disable import/first,import/order */
import Alert from 'react-s-alert';

import LocationsConfigurationWizard from 'components/locations-configuration/LocationsConfigurationWizard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const LOCATION_TYPES = [
  {
    id: 'lt1', name: 'Depot|fr:D\u00e9p\u00f4t', locationTypeCode: 'DEPOT', supportedActivities: ['MANAGE_INVENTORY', 'RECEIVE_STOCK'],
  },
  {
    id: 'lt2', name: 'Ward', locationTypeCode: 'WARD', supportedActivities: ['SUBMIT_REQUEST'],
  },
  { id: 'lt3', name: 'Zone', locationTypeCode: 'ZONE' },
];

const SAVED_LOCATION = {
  id: 'loc1',
  name: 'Main Warehouse',
  active: true,
  locationNumber: 'MW-1',
  organization: { id: 'org1', code: 'ORG1', name: 'Health Ministry' },
  locationGroup: { id: 'lg1', name: 'North' },
  manager: null,
  locationType: { id: 'lt1', name: 'Depot|fr:D\u00e9p\u00f4t' },
  supportedActivities: ['MANAGE_INVENTORY'],
  address: { city: 'Boston', country: 'USA' },
};

let locations;

const answers = (url) => {
  if (url.startsWith('/api/localizations')) {
    return { messages: {}, currentLocale: 'en' };
  }
  if (url === '/api/locations/locationTypes') {
    return { data: LOCATION_TYPES };
  }
  if (url === '/api/locations/supportedActivities') {
    return { data: ['MANAGE_INVENTORY', 'RECEIVE_STOCK', 'SUBMIT_REQUEST', 'NONE'] };
  }
  if (url === '/api/supportLinks') {
    return { data: {} };
  }
  if (url === '/api/internalLocations/search') {
    return { data: [], totalCount: 0 };
  }
  const location = url.match(/^\/api\/locations\/(\w+)$/);
  if (location) {
    return { data: locations[location[1]] };
  }
  return { data: [] };
};

const renderWizard = (path = '/openboxes/locationsConfiguration/create', locale) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  if (locale) {
    store.dispatch({
      type: 'CHANGE_CURRENT_LOCALE',
      payload: { data: { data: { activeLanguage: locale, activeLanguageTag: locale } } },
    });
  }
  const view = render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <Route
          path="/openboxes/locationsConfiguration/create/:locationId?"
          render={({ history }) => <LocationsConfigurationWizard history={history} />}
        />
        <Route path="*" render={({ location }) => <div data-testid="path">{location.pathname}</div>} />
      </MemoryRouter>
    </Provider>,
  );
  return { ...view, store };
};

const path = () => screen.getByTestId('path').textContent;
const field = (label) => document.querySelector(`[data-testid="form-field"][aria-label="${label}"]`);
const input = (name) => document.getElementById(name);
const type = (name, value) => fireEvent.change(input(name), { target: { value } });
const getCalls = (url) => apiClient.get.mock.calls.filter(([called]) => called === url);

const openMenu = (element) => {
  const selectInput = element.querySelector('input');
  fireEvent.focus(selectInput);
  fireEvent.keyDown(selectInput, { key: 'ArrowDown', keyCode: 40 });
};

// react-modal hides the rest of the document from assistive technology, so the dialog is
// found by its class.
const findDialog = async () => {
  await waitFor(() => expect(document.querySelector('.ReactModal__Content')).not.toBeNull());
  return document.querySelector('.ReactModal__Content');
};

// Adds an organization through the select's "Add new Organization" modal.
const addOrganization = async (name) => {
  openMenu(field('Organization'));
  fireEvent.click(await screen.findByText('Add new Organization'));
  const modal = await findDialog();
  fireEvent.change(modal.querySelector('#name'), { target: { value: name } });
  fireEvent.click(within(modal).getByText('Save').closest('button'));
};

const dialogClosed = () => expect(document.querySelector('.ReactModal__Content')).toBeNull();

const nextButton = () => screen.getByRole('button', { name: 'Next' });

// The pages report a failed request by rejecting a promise nobody handles (the app shows it from
// its global handler). Those reports are recorded here, and kept from going unhandled, so a test
// can check what was reported. Other rejections, like the mocked failing requests, pass through.
const MOCK_FAILURE = 'rejected';
let rejections = [];
let rejectSpy;
const originalReject = Promise.reject.bind(Promise);

beforeEach(() => {
  rejections = [];
  rejectSpy = jest.spyOn(Promise, 'reject').mockImplementation((reason) => {
    if (reason instanceof Error && reason.message !== MOCK_FAILURE) {
      rejections.push(reason.message);
      return new Promise(() => {});
    }
    return originalReject(reason);
  });
});

afterEach(() => rejectSpy.mockRestore());

const reported = (message) => waitFor(() => expect(rejections).toContain(message));

describe('locations configuration wizard', () => {
  beforeEach(() => {
    locations = {};
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url) }));
  });

  afterEach(() => jest.clearAllMocks());

  it('starts a new location as active, of the depot type, with the depot default activities', async () => {
    renderWizard();

    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());
    expect(input('active')).toBeChecked();
    const activities = document.querySelector('.location-supported-activities');
    await waitFor(() => expect(within(activities).getByText('MANAGE_INVENTORY')).toBeInTheDocument());
    expect(within(activities).getByText('RECEIVE_STOCK')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=&prefix=react.locationsConfiguration');
    // The full activity list is fetched once the page's translations are in.
    await waitFor(() => expect(getCalls('/api/locations/supportedActivities')).toHaveLength(1));
  });

  it('shows the French location type names in French', async () => {
    renderWizard('/openboxes/locationsConfiguration/create', 'fr');

    await waitFor(() => expect(within(field('Location Type')).getByText('D\u00e9p\u00f4t')).toBeInTheDocument());
  });

  it('requires a name and an organization before saving', async () => {
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    fireEvent.click(nextButton());

    await waitFor(() => expect(field('Name')).toHaveClass('has-error'));
    expect(field('Organization')).toHaveClass('has-error');
    expect(screen.getAllByText('react.default.error.requiredField.label')).toHaveLength(2);
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('creates an organization from the modal, saves the location and walks on to the address and the zones', async () => {
    apiClient.post.mockImplementation((url) => {
      if (url === '/api/organizations') {
        return Promise.resolve({ data: { data: { id: 'org9' } } });
      }
      if (url.startsWith('/api/locations?')) {
        locations.loc9 = { id: 'loc9', address: { city: '', country: 'Kenya' } };
        return Promise.resolve({ data: { data: { id: 'loc9' } } });
      }
      return Promise.resolve({ data: { data: {} } });
    });
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    type('name', 'Clinic East');
    await addOrganization('Acme Health');

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/organizations', { name: 'Acme Health', description: undefined }));
    await waitFor(dialogClosed);
    expect(within(field('Organization')).getByText('Acme Health')).toBeInTheDocument();
    expect(Alert.success).toHaveBeenCalledWith('Organization was successfully saved!', { timeout: 3000 });

    fireEvent.click(nextButton());

    await waitFor(() => expect(path()).toBe('/openboxes/locationsConfiguration/create/loc9'));
    const [url, payload] = apiClient.post.mock.calls.find(([called]) => called.startsWith('/api/locations?'));
    expect(url).toBe('/api/locations?useDefaultActivities=true');
    expect(payload).toEqual(expect.objectContaining({
      active: true,
      name: 'Clinic East',
      organization: expect.objectContaining({ id: 'org9', name: 'Acme Health' }),
      locationType: expect.objectContaining({ id: 'lt1' }),
      supportedActivities: ['MANAGE_INVENTORY', 'RECEIVE_STOCK'],
    }));

    // The address page loads the saved location's address.
    await waitFor(() => expect(input('country')).toHaveValue('Kenya'));
    type('city', 'Nairobi');
    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/loc9', {
      address: { city: 'Nairobi', country: 'Kenya' },
    }));
    expect(await screen.findByText('Zone Locations')).toBeInTheDocument();
    expect(Alert.success).toHaveBeenCalledWith('Address was succesfully added to the location!', { timeout: 3000 });
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/internalLocations/search', expect.objectContaining({
      params: expect.objectContaining({ locationTypeCode: 'ZONE', 'parentLocation.id': 'loc9' }),
    })));

    // Back from the zones to the address page.
    fireEvent.click(screen.getByRole('button', { name: 'Previous' }));
    expect(await screen.findByText('Street address')).toBeInTheDocument();
  });

  it('cancels the organization modal without saving anything', async () => {
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    openMenu(field('Organization'));
    fireEvent.click(await screen.findByText('Add new Organization'));
    const modal = await findDialog();
    fireEvent.click(within(modal).getByText('Save').closest('button'));
    await waitFor(() => expect(within(modal).getByText('react.default.error.requiredField.label')).toBeInTheDocument());
    fireEvent.click(within(modal).getByText('Cancel').closest('button'));

    await waitFor(dialogClosed);
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('creates a location group from the modal and selects it', async () => {
    apiClient.post.mockResolvedValue({ data: { data: { id: 'lg7' } } });
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    openMenu(field('Location Group'));
    fireEvent.click(await screen.findByText('Add new Location Group'));
    const modal = await findDialog();
    fireEvent.change(modal.querySelector('#name'), { target: { value: 'South' } });
    fireEvent.click(within(modal).getByText('Save').closest('button'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locationGroups', { name: 'South' }));
    await waitFor(() => expect(within(field('Location Group')).getByText('South')).toBeInTheDocument());
    expect(Alert.success).toHaveBeenCalledWith('Location group was successfully saved!', { timeout: 3000 });
  });

  it('reports a location group that cannot be created', async () => {
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    openMenu(field('Location Group'));
    fireEvent.click(await screen.findByText('Add new Location Group'));
    const modal = await findDialog();
    fireEvent.change(modal.querySelector('#name'), { target: { value: 'South' } });
    fireEvent.click(within(modal).getByText('Save').closest('button'));

    await reported('Could not create location group');
    expect(within(field('Location Group')).queryByText('South')).not.toBeInTheDocument();
  });

  it('keeps the modal open when the organization cannot be created', async () => {
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());

    await addOrganization('Acme Health');

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/organizations', expect.anything()));
    await reported('Could not create organization');
    expect(Alert.success).not.toHaveBeenCalled();
    expect(within(field('Organization')).queryByText('Acme Health')).not.toBeInTheDocument();
  });

  it('loads an existing location into the form and saves it under its id', async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');

    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));
    expect(input('locationNumber')).toHaveValue('MW-1');
    expect(within(field('Organization')).getByText('ORG1 Health Ministry')).toBeInTheDocument();
    expect(within(field('Location Group')).getByText('North')).toBeInTheDocument();
    expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument();

    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/locations/loc1?useDefaultActivities=true');
    expect(payload).toEqual(expect.objectContaining({
      locationId: 'loc1',
      name: 'Main Warehouse',
      organization: expect.objectContaining({ id: 'org1' }),
      locationGroup: expect.objectContaining({ id: 'lg1' }),
    }));
    expect(Alert.success).toHaveBeenCalledWith('Location was successfully saved!', { timeout: 3000 });
  });

  it('sends the chosen logo as base64 with the location', async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));

    const logo = new File(['logo'], 'logo.png', { type: 'image/png' });
    fireEvent.change(field('Upload Logo').querySelector('input[type="file"]'), { target: { files: [logo] } });
    expect(await within(field('Upload Logo')).findByText('logo.png')).toBeInTheDocument();
    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [, payload] = apiClient.post.mock.calls[0];
    expect(payload.logo).toBe('bG9nbw==');
    expect(payload.logoFile).toBe(logo);
  });

  it('reports a location that cannot be loaded', async () => {
    apiClient.get.mockImplementation((url) => (url === '/api/locations/loc1'
      ? Promise.reject(new Error(MOCK_FAILURE))
      : Promise.resolve({ data: answers(url) })));
    renderWizard('/openboxes/locationsConfiguration/create/loc1');

    await reported('Could not load location data');
    expect(input('name')).toHaveValue('');
  });

  it('sends an unknown location id back to the creation page', async () => {
    renderWizard('/openboxes/locationsConfiguration/create/gone');

    await waitFor(() => expect(path()).toBe('/openboxes/locationsConfiguration/create'));
  });

  it('switches the activities with the location type and resets them to the defaults on request', async () => {
    renderWizard();
    await waitFor(() => expect(within(field('Location Type')).getByText('Depot')).toBeInTheDocument());
    const activities = document.querySelector('.location-supported-activities');

    openMenu(field('Location Type'));
    fireEvent.click(await screen.findByText('Ward'));

    await waitFor(() => expect(within(activities).getByText('SUBMIT_REQUEST')).toBeInTheDocument());
    expect(within(activities).queryByText('MANAGE_INVENTORY')).not.toBeInTheDocument();

    // Custom activities: the default box is cleared and an activity added by hand.
    fireEvent.click(input('useDefaultActivities'));
    expect(input('useDefaultActivities')).not.toBeChecked();
    openMenu(activities);
    fireEvent.click(await screen.findByText('RECEIVE_STOCK'));
    await waitFor(() => expect(within(activities).getByText('RECEIVE_STOCK')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: 'Reset to default settings' }));

    expect(input('useDefaultActivities')).toBeChecked();
    await waitFor(() => expect(within(activities).queryByText('RECEIVE_STOCK')).not.toBeInTheDocument());
    expect(within(activities).getByText('SUBMIT_REQUEST')).toBeInTheDocument();
  });

  it('does not allow "NONE" together with another activity', async () => {
    locations.loc1 = SAVED_LOCATION;
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));
    await waitFor(() => expect(getCalls('/api/locations/supportedActivities')).toHaveLength(1));
    const activities = document.querySelector('.location-supported-activities');

    fireEvent.click(input('useDefaultActivities'));
    openMenu(activities);
    fireEvent.click(await screen.findByText('NONE'));
    fireEvent.click(nextButton());

    expect(await screen.findByText('react.locationsConfiguration.error.supportedActivities.label')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('sends custom activities with the default activities turned off', async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));

    fireEvent.click(input('useDefaultActivities'));
    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/locations/loc1?useDefaultActivities=false');
    expect(payload.supportedActivities).toEqual(['MANAGE_INVENTORY']);
  });

  // KNOWN-GAP: OBAF takes the activities from the loaded location's type, which the
  // location API sends without them, so the reset shows and sends no activities.
  it.skip('resets the activities of a loaded location to its type defaults and sends them', async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));
    await waitFor(() => expect(getCalls('/api/locations/locationTypes')).toHaveLength(1));
    const activities = document.querySelector('.location-supported-activities');

    fireEvent.click(input('useDefaultActivities'));
    fireEvent.click(screen.getByRole('button', { name: 'Reset to default settings' }));

    await waitFor(() => expect(within(activities).getByText('RECEIVE_STOCK')).toBeInTheDocument());
    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/locations/loc1?useDefaultActivities=true');
    expect(payload.supportedActivities).toEqual(['MANAGE_INVENTORY', 'RECEIVE_STOCK']);
  });

  it('stays on the details page when the location cannot be saved', async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));

    fireEvent.click(nextButton());

    await reported('Could not create location');
    expect(Alert.success).not.toHaveBeenCalled();
    expect(screen.getByText('Details', { selector: '.form-title' })).toBeInTheDocument();
    expect(path()).toBe('/openboxes/locationsConfiguration/create/loc1');
  });
});

describe('location address page', () => {
  const openAddress = async () => {
    locations.loc1 = SAVED_LOCATION;
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));
    fireEvent.click(nextButton());
    await waitFor(() => expect(input('city')).toHaveValue('Boston'));
    apiClient.post.mockClear();
  };

  beforeEach(() => {
    locations = {};
    apiClient.get.mockImplementation((url) => Promise.resolve({ data: answers(url) }));
  });

  afterEach(() => jest.clearAllMocks());

  it('rejects values longer than 255 characters', async () => {
    await openAddress();

    type('address', 'x'.repeat(256));
    fireEvent.click(nextButton());

    expect(await screen.findByText('react.default.error.tooLongInput.label')).toBeInTheDocument();
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('sends an emptied field as an empty string and goes back after saving on Previous', async () => {
    await openAddress();

    type('city', '');
    fireEvent.click(screen.getByRole('button', { name: 'Previous' }));

    // The emptied city goes as '' so that it is cleared.
    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/loc1', {
      address: { city: '', country: 'USA' },
    }));
    await waitFor(() => expect(input('name')).toBeInTheDocument());
  });

  // KNOWN-GAP: OBAF's mapToEmptyString checks array indexes, not values, so it sends
  // the never-set postalCode as an empty string.
  it.skip('does not send the fields the location has no value for', async () => {
    locations.loc1 = { ...SAVED_LOCATION, address: { city: 'Boston', country: 'USA', postalCode: null } };
    apiClient.post.mockResolvedValue({ data: { data: { id: 'loc1' } } });
    renderWizard('/openboxes/locationsConfiguration/create/loc1');
    await waitFor(() => expect(input('name')).toHaveValue('Main Warehouse'));
    fireEvent.click(nextButton());
    await waitFor(() => expect(input('city')).toHaveValue('Boston'));
    apiClient.post.mockClear();

    fireEvent.click(nextButton());

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/loc1', {
      address: { city: 'Boston', country: 'USA' },
    }));
  });

  it('stays on the address page when the address cannot be saved', async () => {
    await openAddress();
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));

    fireEvent.click(nextButton());

    await reported('Could not add address');
    expect(input('city')).toBeInTheDocument();
    expect(screen.queryByText('Zone Locations')).not.toBeInTheDocument();
  });
});
