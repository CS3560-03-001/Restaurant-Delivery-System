import { browser } from '$app/environment';
import type {
  CustomerCreateRequest,
  CustomerCreateResponse,
  DeliveryAddress,
  OrderCreateResponse,
  OrderStatusResponse,
  PaymentResponse,
  PizzaSelection,
  SavedPaymentMethod
} from '$lib/contracts';
import { writable } from 'svelte/store';

const STORAGE_KEY = 'restaurant-delivery-flow';

type LegacyStoredFlowState = Partial<FlowState> & {
  pizzaDraft?: Partial<PizzaSelection>;
  accountDraft?: Partial<CustomerCreateRequest> & {
    address?: Partial<DeliveryAddress> | string;
  };
};

export interface FlowState {
  accountDraft: CustomerCreateRequest;
  customer: CustomerCreateResponse | null;
  pizzaDrafts: PizzaSelection[];
  order: OrderCreateResponse | null;
  payment: PaymentResponse | null;
  latestStatus: OrderStatusResponse | null;
  savedPaymentMethods: SavedPaymentMethod[];
}

export const emptyDeliveryAddress = (): DeliveryAddress => ({
  streetAddress: '',
  apartment: '',
  city: '',
  state: '',
  zip: '',
  country: 'United States'
});

export const emptyPizzaSelection = (): PizzaSelection => ({
  crust: '',
  sauce: '',
  cheese: '',
  toppings: []
});

export const createDefaultState = (): FlowState => ({
  accountDraft: {
    name: '',
    email: '',
    phone: '',
    address: emptyDeliveryAddress()
  },
  customer: null,
  pizzaDrafts: [emptyPizzaSelection()],
  order: null,
  payment: null,
  latestStatus: null,
  savedPaymentMethods: []
});

function createFlowStore() {
  const initial = browser ? readStoredState() : createDefaultState();
  const store = writable<FlowState>(initial);

  if (browser) {
    store.subscribe((value) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    });
  }

  return {
    subscribe: store.subscribe,
    update: store.update,
    reset: () => store.set(createDefaultState()),
    setAccountDraft: (accountDraft: CustomerCreateRequest) =>
      store.update((state) => ({ ...state, accountDraft })),
    setCustomer: (customer: CustomerCreateResponse, accountDraft: CustomerCreateRequest) =>
      store.update((state) => ({ ...state, customer, accountDraft })),
    setPizzaDrafts: (pizzaDrafts: PizzaSelection[]) => store.update((state) => ({ ...state, pizzaDrafts })),
    setOrder: (order: OrderCreateResponse) => store.update((state) => ({ ...state, order })),
    setPayment: (payment: PaymentResponse) => store.update((state) => ({ ...state, payment })),
    setLatestStatus: (latestStatus: OrderStatusResponse) =>
      store.update((state) => ({ ...state, latestStatus })),
    setSavedPaymentMethods: (savedPaymentMethods: SavedPaymentMethod[]) => store.update((state) => ({ ...state, savedPaymentMethods }))
  };
}

function readStoredState(): FlowState {
  const raw = localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return createDefaultState();
  }

  try {
    return normalizeStoredState(JSON.parse(raw) as LegacyStoredFlowState);
  } catch {
    return createDefaultState();
  }
}

function normalizeStoredState(stored: LegacyStoredFlowState): FlowState {
  const defaultStateSnapshot = createDefaultState();
  const accountDraft = stored.accountDraft ?? defaultStateSnapshot.accountDraft;
  const pizzaDrafts =
    stored.pizzaDrafts?.length
      ? stored.pizzaDrafts.map((selection) => normalizePizzaSelection(selection))
      : stored.pizzaDraft
        ? [normalizePizzaSelection(stored.pizzaDraft)]
        : defaultStateSnapshot.pizzaDrafts.map((selection) => normalizePizzaSelection(selection));
  const { pizzaDraft, pizzaDrafts: _pizzaDrafts, accountDraft: _accountDraft, ...rest } = stored;

  return {
    ...defaultStateSnapshot,
    ...rest,
    accountDraft: {
      ...defaultStateSnapshot.accountDraft,
      name: accountDraft.name ?? '',
      email: accountDraft.email ?? '',
      phone: accountDraft.phone ?? '',
      address: normalizeAddress(accountDraft.address)
    },
    pizzaDrafts,
    customer: stored.customer ?? null,
    order: stored.order ?? null,
    payment: stored.payment ?? null,
    latestStatus: stored.latestStatus ?? null,
    savedPaymentMethods: stored.savedPaymentMethods ?? []
  };
}

function normalizePizzaSelection(selection?: Partial<PizzaSelection>): PizzaSelection {
  return {
    ...emptyPizzaSelection(),
    crust: selection?.crust ?? '',
    sauce: selection?.sauce ?? '',
    cheese: selection?.cheese ?? '',
    toppings: selection?.toppings ? [...selection.toppings] : []
  };
}

function normalizeAddress(
  address: CustomerCreateRequest['address'] | Partial<DeliveryAddress> | string | undefined
): DeliveryAddress {
  if (typeof address === 'string') {
    return {
      ...emptyDeliveryAddress(),
      streetAddress: address
    };
  }

  return {
    ...emptyDeliveryAddress(),
    streetAddress: address?.streetAddress ?? '',
    apartment: address?.apartment ?? '',
    city: address?.city ?? '',
    state: address?.state ?? '',
    zip: address?.zip ?? '',
    country: address?.country ?? 'United States'
  };
}

export function isPizzaReady(selection: PizzaSelection) {
  return Boolean(selection.crust && selection.sauce && selection.cheese);
}

export const flow = createFlowStore();
