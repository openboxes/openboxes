import { renderHook } from '@testing-library/react-hooks';
import Alert from 'react-s-alert';

import RoleType from 'consts/roleType';
import useProductSupplierActions from 'hooks/list-pages/productSupplier/useProductSupplierActions';
import useSpinner from 'hooks/useSpinner';
import useUserHasPermissions from 'hooks/useUserHasPermissions';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import exportFileFromAPI from 'utils/file-download-util';

jest.mock('react-s-alert', () => ({ success: jest.fn() }));
jest.mock('hooks/useSpinner', () => ({ __esModule: true, default: jest.fn() }));
jest.mock('hooks/useUserHasPermissions', () => ({ __esModule: true, default: jest.fn() }));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/confirmationModalUtils', () => ({ __esModule: true, default: jest.fn() }));
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn() }));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useProductSupplierActions', () => {
  let spinner;
  let fireFetchData;

  const renderActions = (filterParams = {}) => renderHook(() => useProductSupplierActions({
    fireFetchData,
    filterParams,
  }));

  beforeEach(() => {
    jest.clearAllMocks();
    spinner = { show: jest.fn(), hide: jest.fn() };
    fireFetchData = jest.fn();
    useSpinner.mockReturnValue(spinner);
    useUserHasPermissions.mockReturnValue(true);
  });

  it('checks for admins or product managers', () => {
    renderActions();

    expect(useUserHasPermissions).toHaveBeenCalledWith({
      minRequiredRole: RoleType.ROLE_ADMIN,
      supplementalRoles: [RoleType.ROLE_PRODUCT_MANAGER],
    });
  });

  it('offers edit and delete actions only to users who can manage products', () => {
    const { result } = renderActions();
    const actions = result.current.getActions('ps-1');
    expect(actions.map(({ label }) => label)).toEqual([
      'react.productSupplier.edit.label',
      'react.productSupplier.delete.label',
    ]);
    expect(actions[1].variant).toBe('danger');

    useUserHasPermissions.mockReturnValue(false);
    const { result: restricted } = renderActions();
    expect(restricted.current.getActions('ps-1')).toEqual([]);
  });

  it('asks for confirmation from the delete action', () => {
    const { result } = renderActions();

    result.current.getActions('ps-1')[1].onClick();

    const { title, content, buttons } = confirmationModal.mock.calls[0][0];
    expect(title).toEqual({
      label: 'react.productSupplier.deleteConfirmation.title.label',
      default: 'Are you sure?',
    });
    expect(content.default).toBe('Are you sure you want to delete this Product Source?');
    const onClose = jest.fn();
    const [cancel, remove] = buttons(onClose);
    expect(cancel).toMatchObject({ defaultLabel: 'Cancel', onClick: onClose });
    expect(remove).toMatchObject({ defaultLabel: 'Delete', variant: 'danger' });
  });

  it('deletes the product source, notifies, refreshes and closes the modal', async () => {
    apiClient.delete.mockResolvedValue({});
    const { result } = renderActions();
    result.current.openConfirmationModal('ps-2');
    const onClose = jest.fn();

    confirmationModal.mock.calls[0][0].buttons(onClose)[1].onClick();
    await flushPromises();

    expect(apiClient.delete).toHaveBeenCalledWith('/api/productSuppliers/ps-2');
    expect(Alert.success).toHaveBeenCalledTimes(1);
    const [message] = Alert.success.mock.calls[0];
    expect(message.props).toMatchObject({
      id: 'react.productSupplier.deleted.label',
      data: { id: 'ps-2' },
    });
    expect(message.props.options.onMissingTranslation()).toBe('Product Source ps-2 deleted');
    expect(fireFetchData).toHaveBeenCalledTimes(1);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('closes the modal without notifying when the delete fails', async () => {
    apiClient.delete.mockRejectedValue(new Error('in use'));
    const { result } = renderActions();
    result.current.openConfirmationModal('ps-3');
    const onClose = jest.fn();

    await expect(confirmationModal.mock.calls[0][0].buttons(onClose)[1].onClick()).rejects.toThrow('in use');

    expect(Alert.success).not.toHaveBeenCalled();
    expect(fireFetchData).not.toHaveBeenCalled();
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('exports every product source as xls and hides the spinner afterwards', () => {
    const { result } = renderActions({ searchTerm: 'abc' });

    result.current.exportProductSuppliers();

    expect(spinner.show).toHaveBeenCalledTimes(1);
    const [{ afterExporting, ...options }] = exportFileFromAPI.mock.calls[0];
    expect(options).toEqual({
      url: '/api/productSuppliers/export',
      params: { includeInactive: true, disableMaxLimit: true, format: 'xls' },
      format: 'xls',
    });
    expect(spinner.hide).not.toHaveBeenCalled();
    afterExporting();
    expect(spinner.hide).toHaveBeenCalledTimes(1);
  });

  it('exports only the filtered results with ids for the selected options', () => {
    const { result } = renderActions({
      searchTerm: 'abc',
      product: { id: 'prod-1' },
      supplier: { id: 'sup-1' },
      defaultPreferenceTypes: [{ id: 'PRIMARY' }],
    });

    result.current.exportProductSuppliers(true);

    expect(exportFileFromAPI.mock.calls[0][0].params).toEqual({
      includeInactive: true,
      disableMaxLimit: true,
      format: 'xls',
      searchTerm: 'abc',
      product: 'prod-1',
      supplier: 'sup-1',
      defaultPreferenceTypes: ['PRIMARY'],
    });
  });

  it('sends no preference types when none are selected', () => {
    const { result } = renderActions({});

    result.current.exportProductSuppliers(true);

    expect(exportFileFromAPI.mock.calls[0][0].params).toMatchObject({
      product: undefined,
      supplier: undefined,
      defaultPreferenceTypes: [],
    });
  });
});
