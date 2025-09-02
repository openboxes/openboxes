import getCommonPinningStyles from 'utils/getCommonPinningStyles';

describe('getCommonPinningStyles', () => {
  const mockColumn = {
    getIsPinned: jest.fn(),
    getIsLastColumn: jest.fn(),
    getStart: jest.fn(),
    getSize: jest.fn(),
  };

  const defaultProps = {
    column: mockColumn,
    flexWidth: undefined,
    isScreenWiderThanTable: false,
    dataLength: 10,
    loading: false,
  };

  const getProps = (customProps = {}) => {
    const props = { ...defaultProps, ...customProps };
    return getCommonPinningStyles(
      props.column,
      props.flexWidth,
      props.isScreenWiderThanTable,
      props.dataLength,
      props.loading,
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockColumn.getIsPinned.mockReturnValue(false);
    mockColumn.getIsLastColumn.mockReturnValue(false);
    mockColumn.getStart.mockReturnValue(0);
    mockColumn.getSize.mockReturnValue(100);
  });

  it('returns styles for non-pinned column', () => {
    expect(getProps()).toEqual({
      boxShadow: undefined,
      clipPath: undefined,
      marginRight: undefined,
      left: undefined,
      position: false,
      flex: 100,
      width: 100,
      zIndex: 0,
      background: false,
    });
  });

  it('returns styles for last left-pinned column', () => {
    mockColumn.getIsPinned.mockReturnValue('left');
    mockColumn.getIsLastColumn.mockReturnValue(true);
    mockColumn.getStart.mockReturnValue(50);

    expect(getProps()).toEqual({
      boxShadow: '0 0 15px #00000040',
      clipPath: 'inset(0 -15px 0 0)',
      marginRight: '5px',
      left: '50px',
      position: 'sticky',
      flex: 100,
      width: 100,
      zIndex: 1,
      background: 'white',
    });
  });

  it('disables sticky position when conditions are not met', () => {
    mockColumn.getIsPinned.mockReturnValue('left');

    expect(getProps({ isScreenWiderThanTable: true }).position).toBeFalsy();
    expect(getProps({ dataLength: 0 }).position).toBeFalsy();
    expect(getProps({ loading: true }).position).toBeFalsy();
  });

  it('handles undefined column methods safely', () => {
    const undefinedColumn = {
      getIsPinned: jest.fn().mockReturnValue(undefined),
      getIsLastColumn: jest.fn().mockReturnValue(undefined),
      getStart: jest.fn().mockReturnValue(undefined),
      getSize: jest.fn().mockReturnValue(undefined),
    };
    expect(getCommonPinningStyles(
      undefinedColumn,
      defaultProps.flexWidth,
      defaultProps.isScreenWiderThanTable,
      defaultProps.dataLength,
      defaultProps.loading,
    )).toEqual({
      boxShadow: undefined,
      clipPath: undefined,
      marginRight: undefined,
      left: undefined,
      position: undefined,
      flex: undefined,
      width: undefined,
      zIndex: 0,
      background: undefined,
    });
  });
});
