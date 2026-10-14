import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch } from 'react-redux';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useFileActions from 'hooks/useFileActions';

const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockShowNotification));
jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));

const file = (name, content = name) => new File([content], name, { type: 'text/plain' });

describe('useFileActions', () => {
  const dispatch = jest.fn();
  let props;

  const renderFileActions = (overrides) =>
    renderHook(() => useFileActions({ ...props, ...overrides }));

  beforeEach(() => {
    jest.clearAllMocks();
    useDispatch.mockReturnValue(dispatch);
    jest.spyOn(window, 'open').mockImplementation(() => null);
    props = {
      entityId: 'inv-1',
      onSave: jest.fn().mockResolvedValue(),
      isValid: true,
      uploadDocuments: jest.fn().mockResolvedValue(),
    };
  });

  afterEach(() => {
    window.open.mockRestore();
  });

  it('adds the selected files, a file with the same name replacing the older one', () => {
    const { result } = renderFileActions();
    const firstPacking = file('packing.pdf', 'v1');
    const secondPacking = file('packing.pdf', 'v2');

    act(() => result.current.handleDownloadFiles([firstPacking, file('invoice.pdf')]));
    act(() => result.current.handleDownloadFiles([secondPacking]));

    expect(result.current.files.map(({ name }) => name)).toEqual(['packing.pdf', 'invoice.pdf']);
    expect(result.current.files[0]).toBe(secondPacking);
  });

  it('removes a file by its name', () => {
    const { result } = renderFileActions();
    act(() => result.current.setFiles([file('packing.pdf'), file('invoice.pdf')]));

    act(() => result.current.handleRemoveFile({ name: 'packing.pdf' }));

    expect(result.current.files.map(({ name }) => name)).toEqual(['invoice.pdf']);
  });

  it('uploads every file as a form part of the entity', async () => {
    const { result } = renderFileActions();
    const files = [file('packing.pdf'), file('invoice.pdf')];
    act(() => result.current.setFiles(files));

    await result.current.sendFiles();

    expect(props.uploadDocuments).toHaveBeenCalledWith('inv-1', expect.any(FormData));
    const formData = props.uploadDocuments.mock.calls[0][1];
    expect(formData.get('filesContents[0]').name).toBe('packing.pdf');
    expect(formData.get('filesContents[1]').name).toBe('invoice.pdf');
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Files uploaded successfully!' });
  });

  it('confirms the upload of a single file', async () => {
    const { result } = renderFileActions();
    act(() => result.current.setFiles([file('packing.pdf')]));

    await result.current.sendFiles();

    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'File uploaded successfully!' });
  });

  it('confirms nothing when the upload fails', async () => {
    props.uploadDocuments.mockRejectedValue(new Error('Upload failed'));
    const { result } = renderFileActions();
    act(() => result.current.setFiles([file('packing.pdf')]));

    await expect(result.current.sendFiles()).rejects.toThrow('Upload failed');
    expect(notification).not.toHaveBeenCalled();
  });

  it('saves the form, then opens the document, with the spinner shown meanwhile', async () => {
    const { result } = renderFileActions();

    await result.current.handleExportFile({ uri: '/openboxes/invoice/print/inv-1' });

    expect(props.onSave).toHaveBeenCalledWith({ showSuccessNotification: false });
    expect(window.open).toHaveBeenCalledWith('/openboxes/invoice/print/inv-1', '_blank');
    expect(dispatch.mock.calls.map(([action]) => action.type))
      .toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('opens nothing and hides the spinner when the save fails', async () => {
    props.onSave.mockRejectedValue(new Error('Save failed'));
    const { result } = renderFileActions();

    await expect(result.current.handleExportFile({ uri: '/openboxes/invoice/print/inv-1' }))
      .rejects.toThrow('Save failed');

    expect(window.open).not.toHaveBeenCalled();
    expect(dispatch.mock.calls.map(([action]) => action.type))
      .toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it.each([
    ['the form is invalid', { isValid: false }, { uri: '/openboxes/x' }],
    ['the document has no uri', {}, {}],
    ['there is no document', {}, undefined],
  ])('exports nothing when %s', async (description, overrides, document) => {
    const { result } = renderFileActions(overrides);

    await result.current.handleExportFile(document);

    expect(props.onSave).not.toHaveBeenCalled();
    expect(window.open).not.toHaveBeenCalled();
    expect(dispatch).not.toHaveBeenCalled();
  });
});
