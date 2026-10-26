import { renderHook } from '@testing-library/react-hooks';

import { PRODUCT_SUPPLIER_PREFERENCES_BY_ID } from 'api/urls';
import useDeletePreferenceType from 'hooks/productSupplier/form/useDeletePreferenceType';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/confirmationModalUtils', () => jest.fn());

const dispatchedTypes = () => mockDispatch.mock.calls.map(([action]) => action.type);

describe('useDeletePreferenceType', () => {
  let afterDelete;
  let onCancel;

  beforeEach(() => {
    jest.clearAllMocks();
    apiClient.delete.mockResolvedValue({});
    afterDelete = jest.fn();
    onCancel = jest.fn();
  });

  const render = (props) => renderHook(
    () => useDeletePreferenceType({ afterDelete, onCancel, ...props }),
  );

  it('treats data with only an id as empty', () => {
    expect(render({ preferenceTypeData: { id: 'p1', comments: '' } }).result.current.isPreferenceTypeEmpty).toBe(true);
    expect(render({ preferenceTypeData: null }).result.current.isPreferenceTypeEmpty).toBe(true);
    expect(render({ preferenceTypeData: { comments: 'x' } }).result.current.isPreferenceTypeEmpty).toBe(false);
  });

  it('removes an unsaved preference immediately without asking', async () => {
    const { result } = render({ preferenceTypeData: { comments: 'x' } });

    await result.current.openConfirmationModal();

    expect(confirmationModal).not.toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
    expect(afterDelete).toHaveBeenCalledTimes(1);
    expect(dispatchedTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('asks before deleting a saved variation and deletes it on confirm', async () => {
    const { result } = render({ preferenceTypeData: { id: 'p1' } });

    result.current.openConfirmationModal();

    const modal = confirmationModal.mock.calls[0][0];
    expect(modal.content.default).toBe('Are you sure you want to delete this preference variation?');
    expect(modal.handleOnClose).toBe(onCancel);

    const onClose = jest.fn();
    const [, confirm] = modal.buttons(onClose);
    await confirm.onClick();

    expect(apiClient.delete).toHaveBeenCalledWith(PRODUCT_SUPPLIER_PREFERENCES_BY_ID('p1'));
    expect(afterDelete).toHaveBeenCalledTimes(1);
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(dispatchedTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('closes the modal and hides the spinner even when the delete fails', async () => {
    apiClient.delete.mockRejectedValue(new Error('fail'));
    const { result } = render({ preferenceTypeData: { id: 'p1' }, isDefaultPreferenceType: true });
    result.current.openConfirmationModal();
    const modal = confirmationModal.mock.calls[0][0];
    const onClose = jest.fn();

    await expect(modal.buttons(onClose)[1].onClick()).rejects.toThrow('fail');

    expect(modal.content.default).toBe('Are you sure you want to delete the default preference for this source?');
    expect(afterDelete).not.toHaveBeenCalled();
    expect(onClose).toHaveBeenCalled();
    expect(dispatchedTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('cancels through the cancel button', () => {
    const { result } = render({ preferenceTypeData: { id: 'p1' } });
    result.current.openConfirmationModal();
    const onClose = jest.fn();

    confirmationModal.mock.calls[0][0].buttons(onClose)[0].onClick();

    expect(onClose).toHaveBeenCalled();
    expect(onCancel).toHaveBeenCalled();
    expect(apiClient.delete).not.toHaveBeenCalled();
  });
});
