const LOCATION_MOCKS = [
  'AlGhanem Group [Supplier]',
  'Amazon [Supplier]',
  'Bangalore 011 [Depot]',
  'Bangalore- 20 Tech [Distributor]',
  'Bangalore-Titan [Supplier]',
  'Bangalore-Vijayanagar [Warehouse]',
  'Belonia foods [Supplier]',
  'Canada [Warehouse]',
  'CCS [Depot]',
  'CDMX [Cliente]',
  'China [Supplier]',
  'CHT [Installation]',
  'Client Test [Cliente]',
  'Corona CA [Depot]',
  'DELHI - BADARPUR [Book Wahrehouse]',
  'DELHI - EAST [Book Wahrehouse]',
  'DELHI - IG AIRPORT [Book Wahrehouse]',
  'DELHI - NORTH [Book Wahrehouse]',
  'DELHI - SOUTH [Book Wahrehouse]',
  'East End SH Clinic [SH Clinic]',
  'East Medical Facility [Depot]',
  'East Pharmacy Depot [Depot]',
  'East Pharmacy Supplier [Supplier]',
  'EXCELINDO [Supplier]',
  'FARIDABAD [Depot]',
  'Florida [Depot]',
  'GBAC [Installation]',
  'Gujarat [Warehouse]',
  'Harbour [Depot]',
  'HO-JKT [HO]',
  'Importaciones VEN [Cliente]',
  'INDIA - NOIDA [Book Wahrehouse]',
  'ISAAC [Cliente]',
  'LC 002 [SH Clinic]',
  'MOM [Depot]',
  'Monitors [Book Wahrehouse]',
  'New location [Depot]',
  'OpenBoxes HQ [Depot]',
  'Palletized [Depot]',
  'PHH [Installation]',
  'PNC [Supplier]',
  'Porto [Book Wahrehouse]',
  'Principal [Principal]',
  'RIM India Pvt Ltd [Warehouse]',
  'Rivercess [Depot]',
  'Sambas - Kalbar [Supplier]',
  'Sepang Air Port [Book Wahrehouse]',
  'SNET [Supplier]',
  'SO MEDAN [SO]',
  'SO SURABAYA [SO]',
  'Toulouse [Supplier]',
  'Zwedru [Installation]',
  'Головной офис [Depot]',
];

const USERNAMES_MOCKS = ['Julian Benson',
  'Alyssa Chandler',
  'Diana Sharp',
  'Caleb Ramirez',
  'Sonia Rios',
  'Wilbur Moran',
  'Nadine Bowen',
  'Bradford Ingram',
  'Johnnie Rodriquez',
  'Kim Dennis',
];

const STOCK_LIST_MOCKS = [
  { value: '1', label: '1st Stock List' },
  { value: '2', label: '2nd Stock List' },
  { value: '3', label: '3rd Stock List' },
  { value: '4', label: '4th Stock List' },
];

const PRODUCTS_MOCKS = [
  { value: { code: 1, name: 'Advil 200mg' }, label: 'Advil 200mg' },
  { value: { code: 2, name: 'Tylenol 325mg' }, label: 'Tylenol 325mg' },
  { value: { code: 3, name: 'Aspirin 20mg' }, label: 'Aspirin 20mg' },
  { value: { code: 4, name: 'Similac Advance low iron 400g' }, label: 'Similac Advance low iron 400g' },
];

const STOCK_LIST_ITEMS_MOCKS = {
  1: [
    {
      product: { code: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 350,
    },
    {
      product: { code: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10, monthlyConsumption: 250,
    },
    {
      product: { code: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 140,
    },
  ],
  2: [
    {
      product: { code: 2, name: 'Tylenol 325mg' }, maxQuantity: 10, monthlyConsumption: 120,
    },
    {
      product: { code: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 400,
    },
    {
      product: { code: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10, monthlyConsumption: 120,
    },
  ],
  3: [
    {
      product: { code: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 55,
    },
    {
      product: { code: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 44,
    },
    {
      product: { code: 2, name: 'Tylenol 325mg' }, maxQuantity: 10, monthlyConsumption: 55,
    },
  ],
  4: [
    { product: { code: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10 },
    {
      product: { code: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 100,
    },
    {
      product: { code: 2, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 140,
    },
  ],
};

const REASON_CODE_MOCKS = [
  { value: '1', label: 'Stocked out' },
  { value: '2', label: 'Low stock' },
  { value: '3', label: 'Because I said so' },
];

const LOT_DATA = [
  {
    productCode: 1,
    lot: '1111',
    bin: '1',
    quantity: '50',
    expiryDate: '2018/05/31',
    recipient: true,
  },
  {
    productCode: 1,
    lot: '1111',
    bin: '2',
    quantity: '20',
    expiryDate: '2018/05/31',
    recipient: false,
  },
  {
    productCode: 1,
    lot: '2222',
    bin: '1',
    quantity: '50',
    expiryDate: '2018/06/10',
    recipient: true,
  },
  {
    productCode: 2,
    lot: '3333',
    bin: '1',
    quantity: '102',
    expiryDate: '2018/06/10',
    recipient: false,
  },
  {
    productCode: 2,
    lot: '3333',
    bin: '1',
    quantity: '103',
    expiryDate: '2018/06/10',
    recipient: false,
  },
  {
    productCode: 2,
    lot: '3333',
    bin: '2',
    quantity: '101',
    expiryDate: '2018/06/10',
    recipient: true,
  },
  {
    productCode: 3,
    lot: '4444',
    bin: '1',
    quantity: '200',
    expiryDate: '2018/06/01',
    recipient: false,
  },
  {
    productCode: 3,
    lot: '5555',
    bin: '1',
    quantity: '300',
    expiryDate: '2018/06/02',
    recipient: true,
  },
  {
    productCode: 3,
    lot: '5555',
    bin: '2',
    quantity: '100',
    expiryDate: '2018/06/03',
    recipient: false,
  },
  {
    productCode: 4,
    lot: '6666',
    bin: '1',
    quantity: '123',
    expiryDate: '2018/06/01',
    recipient: false,
  },
  {
    productCode: 4,
    lot: '7777',
    bin: '1',
    quantity: '111',
    expiryDate: '2018/06/02',
    recipient: false,
  },
];

export {
  LOCATION_MOCKS,
  USERNAMES_MOCKS,
  STOCK_LIST_MOCKS,
  PRODUCTS_MOCKS,
  STOCK_LIST_ITEMS_MOCKS,
  REASON_CODE_MOCKS,
  LOT_DATA,
};
