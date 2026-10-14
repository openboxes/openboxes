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

// The API: every GET answers from answers() below; POSTs and DELETEs answer per test.
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
    // OBAF still exports flattenRequest (an identity function in OBAF).
    flattenRequest: actual('flattenRequest'),
  };
});

jest.mock('js-file-download', () => jest.fn());
jest.mock('react-s-alert', () => ({ success: jest.fn(), error: jest.fn() }));

/* eslint-disable import/first,import/order */
import fileDownload from 'js-file-download';
import Alert from 'react-s-alert';

import ZoneAndBinLocations from 'components/locations-configuration/ZoneAndBinLocations';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const ZONE_TYPE = { id: 'lt3', name: 'Zone', locationTypeCode: 'ZONE' };
const BIN_TYPE = { id: 'lt4', name: 'Bin Location', locationTypeCode: 'BIN_LOCATION' };
const INTERNAL_TYPE = { id: 'lt5', name: 'Internal', locationTypeCode: 'INTERNAL' };
const PARENT = { id: 'loc9' };

const ZONES = [
  {
    id: 'z1', name: 'Cold room', active: true, locationType: ZONE_TYPE, parentLocation: PARENT,
  },
  {
    id: 'z2', name: 'Old annex', active: false, locationType: ZONE_TYPE, parentLocation: PARENT,
  },
];
const BINS = [
  {
    id: 'b1', name: 'A-01', active: true, locationType: BIN_TYPE, parentLocation: PARENT, zoneId: 'z1', zoneName: 'Cold room',
  },
];

const answers = (url, config) => {
  if (url === '/api/locations/locationTypes') {
    return { data: [{ id: 'lt1', name: 'Depot', locationTypeCode: 'DEPOT' }, ZONE_TYPE, BIN_TYPE, INTERNAL_TYPE] };
  }
  if (url === '/api/supportLinks') {
    return { data: { knowledgeBase: 'https://help.example.org' } };
  }
  if (url === '/api/internalLocations/search') {
    return config.params.locationTypeCode === 'ZONE'
      ? { data: ZONES, totalCount: 7 }
      : { data: BINS, totalCount: 1 };
  }
  if (url === '/api/locations/binLocations/template') {
    return 'template-bytes';
  }
  return { data: [] };
};

let previousPage;

const renderPage = () => {
  previousPage = jest.fn();
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/openboxes/locationsConfiguration/create/loc9']}>
        <ZoneAndBinLocations initialValues={{ locationId: 'loc9', name: 'Clinic East' }} previousPage={previousPage} />
        <Route path="*" render={({ location }) => <div data-testid="path">{location.pathname}</div>} />
      </MemoryRouter>
    </Provider>,
  );
};

const searches = (locationTypeCode) => apiClient.get.mock.calls
  .filter(([url, config]) => url === '/api/internalLocations/search'
    && JSON.stringify(config.params.locationTypeCode) === JSON.stringify(locationTypeCode));

// react-modal hides the rest of the document from assistive technology, so dialogs are
// found by class.
const findDialog = async () => {
  await waitFor(() => expect(document.querySelector('.ReactModal__Content')).not.toBeNull());
  return document.querySelector('.ReactModal__Content');
};
const dialogClosed = () => expect(document.querySelector('.ReactModal__Content')).toBeNull();
const buttonIn = (element, text) => within(element).getByText(text).closest('button');
// Both tables share the zoneTable class: the zones come first, the bins second.
const zoneTable = () => document.querySelectorAll('.zoneTable')[0];
const binTable = () => document.querySelectorAll('.zoneTable')[1];
const rowOf = (name) => {
  const table = within(zoneTable()).queryByText(name) ? zoneTable() : binTable();
  return within(table).getByText(name).closest('.rt-tr');
};

const openMenu = (element) => {
  const selectInput = element.querySelector('input');
  fireEvent.focus(selectInput);
  fireEvent.keyDown(selectInput, { key: 'ArrowDown', keyCode: 40 });
};
const fieldIn = (element, label) => element.querySelector(`[data-testid="form-field"][aria-label="${label}"]`);

const loaded = async () => {
  await waitFor(() => expect(within(zoneTable()).getByText('Cold room')).toBeInTheDocument());
  await waitFor(() => expect(within(binTable()).getByText('A-01')).toBeInTheDocument());
  await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/locations/locationTypes'));
};

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

describe('zone and bin locations page', () => {
  beforeEach(() => {
    apiClient.get.mockImplementation(
      (url, config) => Promise.resolve({ data: answers(url, config) }),
    );
  });

  afterEach(() => jest.clearAllMocks());

  it('lists the zones and the bins of the location, a page at a time', async () => {
    renderPage();
    await loaded();

    const [, zoneConfig] = searches('ZONE')[0];
    expect(zoneConfig.params).toEqual({
      locationTypeCode: 'ZONE', offset: '0', max: '5', 'parentLocation.id': 'loc9', includeInactive: true,
    });
    const [, binConfig] = searches(['BIN_LOCATION', 'INTERNAL'])[0];
    expect(binConfig.params).toEqual(expect.objectContaining({ offset: '0', max: '10', 'parentLocation.id': 'loc9' }));
    // Repeated parameters go without brackets.
    expect(binConfig.paramsSerializer({ locationTypeCode: ['BIN_LOCATION', 'INTERNAL'] }))
      .toBe('locationTypeCode=BIN_LOCATION&locationTypeCode=INTERNAL');

    // Active and inactive zones are told apart, the zone type and a bin's zone are shown.
    expect(rowOf('Cold room').querySelector('.green-circle')).not.toBeNull();
    expect(rowOf('Old annex').querySelector('.grey-circle')).not.toBeNull();
    expect(within(rowOf('Cold room')).getByText('ZONE')).toBeInTheDocument();
    expect(within(rowOf('A-01')).getByText('Bin Location')).toBeInTheDocument();
    expect(within(rowOf('A-01')).getByText('Cold room')).toBeInTheDocument();
  });

  it('reports zone and bin lists that cannot be loaded', async () => {
    apiClient.get.mockImplementation((url, config) => (url === '/api/internalLocations/search'
      ? Promise.reject(new Error(MOCK_FAILURE))
      : Promise.resolve({ data: answers(url, config) })));
    renderPage();

    await reported('Could not get list of zones');
    await reported('Could not get list of bin locations');
  });

  it('adds a zone with the only zone type preselected and reloads the zones', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    renderPage();
    await loaded();

    fireEvent.click(screen.getByText('Add Zone Location').closest('button'));
    const dialog = await findDialog();
    expect(within(fieldIn(dialog, 'Zone Type')).getByText('Zone')).toBeInTheDocument();
    fireEvent.click(buttonIn(dialog, 'Save'));
    await waitFor(() => expect(within(dialog).getByText('react.default.error.requiredField.label')).toBeInTheDocument());
    expect(apiClient.post).not.toHaveBeenCalled();

    fireEvent.change(dialog.querySelector('#name'), { target: { value: 'Pharmacy' } });
    fireEvent.click(buttonIn(dialog, 'Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/', expect.objectContaining({
      active: true, name: 'Pharmacy', parentLocation: { id: 'loc9' }, locationType: { id: 'lt3' },
    })));
    await waitFor(() => expect(searches('ZONE')).toHaveLength(2));
    expect(Alert.success).toHaveBeenCalledWith('Zone location has been created successfully!', { timeout: 3000 });
    await waitFor(dialogClosed);
  });

  it('adds a bin of a chosen type inside a chosen zone and reloads the bins', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    renderPage();
    await loaded();

    fireEvent.click(screen.getByText('Add Bin Location').closest('button'));
    const dialog = await findDialog();
    fireEvent.change(dialog.querySelector('#name'), { target: { value: 'B-07' } });
    openMenu(fieldIn(dialog, 'Bin Type'));
    fireEvent.click(await screen.findByText('Internal'));
    openMenu(fieldIn(dialog, 'Zone Location'));
    fireEvent.click(await screen.findByText('Old annex', { selector: '[role="list"] *' }));
    fireEvent.click(buttonIn(dialog, 'Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [url, payload] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/locations/');
    expect(payload).toEqual(expect.objectContaining({
      name: 'B-07', parentLocation: { id: 'loc9' }, locationType: { id: 'lt5' }, zone: { id: 'z2' },
    }));
    await waitFor(() => expect(searches(['BIN_LOCATION', 'INTERNAL'])).toHaveLength(2));
    expect(Alert.success).toHaveBeenCalledWith('Bin location has been created successfully!', { timeout: 3000 });
  });

  it('does not reload the zones when a zone cannot be added', async () => {
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderPage();
    await loaded();

    fireEvent.click(screen.getByText('Add Zone Location').closest('button'));
    const dialog = await findDialog();
    fireEvent.change(dialog.querySelector('#name'), { target: { value: 'Pharmacy' } });
    fireEvent.click(buttonIn(dialog, 'Save'));

    await reported('Could not add zone location');
    expect(searches('ZONE')).toHaveLength(1);
    expect(Alert.success).not.toHaveBeenCalled();
  });

  it('edits a zone in place and reloads the zones', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    renderPage();
    await loaded();

    fireEvent.click(rowOf('Old annex').querySelector('.fa-pencil').closest('button'));
    const dialog = await findDialog();
    expect(dialog.querySelector('#name')).toHaveValue('Old annex');
    fireEvent.change(dialog.querySelector('#name'), { target: { value: 'New annex' } });
    fireEvent.click(dialog.querySelector('#active'));
    fireEvent.click(buttonIn(dialog, 'Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/z2', {
      name: 'New annex', parentLocation: { id: 'loc9' }, active: true, locationType: { id: 'lt3' }, zone: undefined,
    }));
    await waitFor(() => expect(searches('ZONE')).toHaveLength(2));
    expect(Alert.success).toHaveBeenCalledWith('Zone location has been edited successfully!', { timeout: 3000 });
  });

  it('edits a bin with its zone and reloads the bins', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    renderPage();
    await loaded();

    fireEvent.click(rowOf('A-01').querySelector('.fa-pencil').closest('button'));
    const dialog = await findDialog();
    expect(within(fieldIn(dialog, 'Zone Location')).getByText('Cold room')).toBeInTheDocument();
    fireEvent.click(buttonIn(dialog, 'Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/b1', {
      name: 'A-01', parentLocation: { id: 'loc9' }, active: true, locationType: { id: 'lt4' }, zone: { id: 'z1' },
    }));
    await waitFor(() => expect(searches(['BIN_LOCATION', 'INTERNAL'])).toHaveLength(2));
    expect(searches('ZONE')).toHaveLength(1);
    expect(Alert.success).toHaveBeenCalledWith('Bin location has been edited successfully!', { timeout: 3000 });
  });

  it('closes the edit dialog on cancel without saving', async () => {
    renderPage();
    await loaded();

    fireEvent.click(rowOf('Cold room').querySelector('.fa-pencil').closest('button'));
    fireEvent.click(buttonIn(await findDialog(), 'Cancel'));

    await waitFor(dialogClosed);
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('does not reload anything when an edit fails', async () => {
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderPage();
    await loaded();

    fireEvent.click(rowOf('Cold room').querySelector('.fa-pencil').closest('button'));
    fireEvent.click(buttonIn(await findDialog(), 'Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalledWith('/api/locations/z1', expect.anything()));
    await reported('Could not edit zone location');
    expect(searches('ZONE')).toHaveLength(1);
    expect(Alert.success).not.toHaveBeenCalled();
  });

  it.each([
    ['zone', 'Cold room', 'z1', 'ZONE'],
    ['bin', 'A-01', 'b1', ['BIN_LOCATION', 'INTERNAL']],
  ])('deletes a %s after confirmation and reloads its table', async (kind, name, id, code) => {
    apiClient.delete.mockResolvedValue({});
    renderPage();
    await loaded();

    fireEvent.click(within(rowOf(name)).getByText('', { selector: '.fa-trash-o' }));
    expect(await screen.findByText('Deleting a location')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Yes' }));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith(`/api/locations/${id}`));
    await waitFor(() => expect(searches(code)).toHaveLength(2));
  });

  it('keeps the location when the deletion is not confirmed', async () => {
    renderPage();
    await loaded();

    fireEvent.click(within(rowOf('A-01')).getByText('', { selector: '.fa-trash-o' }));
    fireEvent.click(await screen.findByRole('button', { name: 'No' }));

    await waitFor(() => expect(screen.queryByText('Deleting a location')).not.toBeInTheDocument());
    expect(apiClient.delete).not.toHaveBeenCalled();
  });

  it.each([
    ['zone', 'Cold room', 'z1', 'ZONE'],
    ['bin', 'A-01', 'b1', ['BIN_LOCATION', 'INTERNAL']],
  ])('reports a failed %s deletion and does not reload its table', async (kind, name, id, code) => {
    apiClient.delete.mockRejectedValue(new Error(MOCK_FAILURE));
    renderPage();
    await loaded();

    fireEvent.click(within(rowOf(name)).getByText('', { selector: '.fa-trash-o' }));
    fireEvent.click(await screen.findByRole('button', { name: 'Yes' }));

    await waitFor(() => expect(apiClient.delete).toHaveBeenCalledWith(`/api/locations/${id}`));
    await reported(`Could not delete ${kind} location`);
    expect(searches(code)).toHaveLength(1);
  });

  it('imports bins from a file and reloads the bins', async () => {
    apiClient.post.mockResolvedValue({ data: {} });
    renderPage();
    await loaded();

    fireEvent.click(screen.getByText('Import Bin Locations').closest('button'));
    const dialog = await findDialog();
    expect(buttonIn(dialog, 'Import')).toBeDisabled();

    fireEvent.click(within(dialog).getByText('here'));
    await waitFor(() => expect(fileDownload).toHaveBeenCalledWith('template-bytes', 'BinLocations_template.xls', 'application/vnd.ms-excel'));
    expect(apiClient.get).toHaveBeenCalledWith('/api/locations/binLocations/template', { responseType: 'blob' });

    const file = new File(['name\nB-09\n'], 'bins.xls', { type: 'application/vnd.ms-excel' });
    fireEvent.change(dialog.querySelector('input[type="file"]'), { target: { files: [file] } });
    await waitFor(() => expect(buttonIn(dialog, 'Import')).toBeEnabled());
    fireEvent.click(buttonIn(dialog, 'Import'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    const [url, body, config] = apiClient.post.mock.calls[0];
    expect(url).toBe('/api/locations/loc9/binLocations/import');
    expect(body.get('fileContents')).toBeInstanceOf(File);
    expect(body.get('fileContents').name).toBe('bins.xls');
    expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
    await waitFor(() => expect(searches(['BIN_LOCATION', 'INTERNAL'])).toHaveLength(2));
    expect(Alert.success).toHaveBeenCalledWith('Bin Location imported successfully');
  });

  it('does not reload the bins when the import fails', async () => {
    apiClient.post.mockRejectedValue(new Error(MOCK_FAILURE));
    renderPage();
    await loaded();

    fireEvent.click(screen.getByText('Import Bin Locations').closest('button'));
    const dialog = await findDialog();
    const file = new File(['x'], 'bins.xls', { type: 'application/vnd.ms-excel' });
    fireEvent.change(dialog.querySelector('input[type="file"]'), { target: { files: [file] } });
    await waitFor(() => expect(buttonIn(dialog, 'Import')).toBeEnabled());
    fireEvent.click(buttonIn(dialog, 'Import'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(searches(['BIN_LOCATION', 'INTERNAL'])).toHaveLength(1);
    expect(Alert.success).not.toHaveBeenCalled();
  });

  it('goes back with the wizard values', async () => {
    renderPage();
    await loaded();

    fireEvent.click(screen.getByRole('button', { name: 'Previous' }));

    expect(previousPage).toHaveBeenCalledWith({ locationId: 'loc9', name: 'Clinic East' });
  });

  it('congratulates on finishing and offers the product configuration next', async () => {
    renderPage();
    await loaded();

    fireEvent.click(screen.getByRole('button', { name: 'Finish' }));

    expect(Alert.success).toHaveBeenCalledWith('Location configuration completed!', { timeout: 3000 });
    const dialog = await findDialog();
    expect(within(dialog).getByText('You have created your first location!')).toBeInTheDocument();
    fireEvent.click(buttonIn(dialog, 'Create Categories and Products'));
    expect(screen.getByTestId('path').textContent).toBe('/openboxes/productsConfiguration/index');
  });

  it('sends the user to another location creation or to the location list from the success message', async () => {
    const { location } = window;
    delete window.location;
    window.location = '';
    try {
      renderPage();
      await loaded();
      fireEvent.click(screen.getByRole('button', { name: 'Finish' }));
      const dialog = await findDialog();

      fireEvent.click(buttonIn(dialog, 'Create another location'));
      expect(window.location).toBe('/openboxes/locationsConfiguration/create');
      fireEvent.click(buttonIn(dialog, 'View Location List'));
      expect(window.location).toBe('/openboxes/location/list');
    } finally {
      window.location = location;
    }
  });

  it('closes the success message', async () => {
    renderPage();
    await loaded();

    fireEvent.click(screen.getByRole('button', { name: 'Finish' }));
    fireEvent.click((await findDialog()).querySelector('.fa-times'));

    await waitFor(dialogClosed);
  });
});
