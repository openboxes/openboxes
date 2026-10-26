import React from 'react';

import {
  configure, fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { wizardStore } from 'regression-tests/stock-movement-wizard/wizardStore';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => {
  const client = Object.assign(jest.fn(), {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  });
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});
jest.mock('react-s-alert', () => ({
  success: jest.fn(), error: jest.fn(), warning: jest.fn(), info: jest.fn(),
}));

configure({ asyncUtilTimeout: 5000 });

/* eslint-disable import/first,import/order */
import Alert from 'react-s-alert';

import AddDestinationModal from 'components/stock-movement-wizard/modals/AddDestinationModal';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const LOCATION_TYPES = [
  { id: 'lt1', name: 'Ward|fr:Service' },
  { id: 'lt2', name: 'Clinic' },
];

const renderModal = ({ isOpen = true, onClose = jest.fn(), onResponse = jest.fn() } = {}) => {
  render(
    <Provider store={wizardStore()}>
      <MemoryRouter>
        <AddDestinationModal isOpen={isOpen} onClose={onClose} onResponse={onResponse} />
      </MemoryRouter>
    </Provider>,
  );
  return { onClose, onResponse };
};

const input = (label) => screen.getAllByLabelText(label)
  .find((element) => element.dataset.testid === 'form-field')
  .querySelector('input:not([type="hidden"])');

const chooseLocationType = async (name) => {
  fireEvent.keyDown(input('Location Type'), { key: 'ArrowDown', keyCode: 40 });
  fireEvent.click(await screen.findByText(name));
};

describe('add destination modal', () => {
  beforeEach(() => {
    apiClient.get.mockResolvedValue({ data: { data: LOCATION_TYPES } });
    apiClient.post.mockImplementation((url) => Promise.resolve({
      data: { data: url === '/api/locations' ? { id: 'new1', name: 'New ward', locationType: { description: 'Ward' } } : {} },
    }));
  });

  afterEach(() => jest.clearAllMocks());

  it('loads the location types that allow creating locations on the fly', async () => {
    renderModal();

    await chooseLocationType('Ward');

    expect(apiClient.get).toHaveBeenCalledWith('/api/locations/locationTypes', { params: { activityCode: 'DYNAMIC_CREATION' } });
  });

  it('creates the destination, hands it over and closes', async () => {
    const { onClose, onResponse } = renderModal();
    fireEvent.change(input('Name'), { target: { value: 'New ward' } });
    await chooseLocationType('Ward');

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(onClose).toHaveBeenCalled());
    expect(apiClient.post).toHaveBeenCalledTimes(1);
    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/locations',
      { name: 'New ward', locationType: 'lt1' },
      { params: { useDefaultActivities: true, assignCurrentLocationGroup: true } },
    );
    expect(onResponse).toHaveBeenCalledWith(expect.objectContaining({ id: 'new1', name: 'New ward' }));
    expect(Alert.success).toHaveBeenCalledWith('Destination was successfully created!', { timeout: 3000 });
  });

  it('saves the address of the new destination as a second request', async () => {
    const { onClose } = renderModal();
    fireEvent.change(input('Name'), { target: { value: 'New ward' } });
    await chooseLocationType('Clinic');
    fireEvent.change(input('Street address'), { target: { value: '1 Main St' } });
    fireEvent.change(input('City'), { target: { value: 'Boston' } });

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(onClose).toHaveBeenCalled());
    expect(apiClient.post).toHaveBeenLastCalledWith('/api/locations/new1', {
      address: { address: '1 Main St', city: 'Boston' },
    });
  });

  it('requires a name, a type and, once any address field is filled, the street address', async () => {
    const { onClose } = renderModal();
    fireEvent.change(input('City'), { target: { value: 'Boston' } });

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(screen.getAllByText('react.default.error.requiredField.label')).toHaveLength(3));
    expect(apiClient.post).not.toHaveBeenCalled();
    expect(onClose).not.toHaveBeenCalled();
  });

  it('keeps the modal open when the creation fails', async () => {
    apiClient.post.mockRejectedValue(new Error('server error'));
    const { onClose, onResponse } = renderModal();
    fireEvent.change(input('Name'), { target: { value: 'New ward' } });
    await chooseLocationType('Ward');

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(apiClient.post).toHaveBeenCalled());
    expect(onResponse).not.toHaveBeenCalled();
    expect(onClose).not.toHaveBeenCalled();
  });

  it('cancel closes without saving', () => {
    const { onClose } = renderModal();

    fireEvent.click(screen.getByText('Cancel'));

    expect(onClose).toHaveBeenCalled();
    expect(apiClient.post).not.toHaveBeenCalled();
  });
});
