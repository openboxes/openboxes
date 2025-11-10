import getCommonPinningStyles from 'utils/getCommonPinningStyles';

describe('getCommonPinningStyles', () => {
  const mockColumn = {
    getIsPinned: jest.fn().mockReturnValue(false),
    getIsLastColumn: jest.fn().mockReturnValue(false),
    getStart: jest.fn().mockReturnValue(0),
    getSize: jest.fn().mockReturnValue(100),
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
  });

  it('returns styles for non-pinned column', () => {
    const result = getProps();

    expect(result).toEqual({
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

    const result = getProps();

    expect(result).toEqual({
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

    let result = getProps({ isScreenWiderThanTable: true });
    expect(result.position).toBeFalsy();

    result = getProps({ dataLength: 0 });
    expect(result.position).toBeFalsy();

    result = getProps({ loading: true });
    expect(result.position).toBeFalsy();
  });

  it('handles undefined column methods safely', () => {
    mockColumn.getIsPinned.mockReturnValue(undefined);
    mockColumn.getIsLastColumn.mockReturnValue(undefined);
    mockColumn.getStart.mockReturnValue(undefined);
    mockColumn.getSize.mockReturnValue(undefined);

    const result = getProps();

    expect(result).toEqual({
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
