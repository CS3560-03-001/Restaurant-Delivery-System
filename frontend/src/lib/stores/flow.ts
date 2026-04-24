import { browser } from '$app/environment';
import type {
  CustomerCreateRequest,
  CustomerCreateResponse,
  DeliveryAddress,
  OrderCreateResponse,
  OrderStatusResponse,
  PaymentResponse,
  PizzaSelection
} from '$lib/contracts';
import { writable } from 'svelte/store';

const STORAGE_KEY = 'restaurant-delivery-flow';

export interface FlowState {
  accountDraft: CustomerCreateRequest;
  customer: CustomerCreateResponse | null;
  pizzaDrafts: PizzaSelection[];
  order: OrderCreateResponse | null;
  payment: PaymentResponse | null;
  latestStatus: OrderStatusResponse | null;
}

export const emptyPizzaSelection = (): PizzaSelection => ({
  crust: '',
  sauce: '',
  cheese: '',
  toppings: []
});

const defaultState: FlowState = {
  accountDraft: {
    name: '',
    email: '',
    phone: '',
    address: ''
  },
  customer: null,
  pizzaDrafts: [emptyPizzaSelection()],
  order: null,
  payment: null,
  latestStatus: null
};

function createFlowStore() {
  const initial = browser ? readStoredState() : defaultState;
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
      store.update((state) => ({ ...state, latestStatus }))
  };
}

function readStoredState(): FlowState {
  const raw = localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return defaultState;
  }

  try {
    return normalizeStoredState(JSON.parse(raw));
  } catch {
    return createDefaultState();
  }
}

function normalizeStoredState(stored: Partial<FlowState>): FlowState {
  const defaultStateSnapshot = createDefaultState();
  const accountDraft = stored.accountDraft ?? defaultStateSnapshot.accountDraft;

  return {
    ...defaultStateSnapshot,
    ...stored,
    accountDraft: {
      name: accountDraft.name ?? '',
      email: accountDraft.email ?? '',
      phone: accountDraft.phone ?? '',
      address: normalizeAddress(accountDraft.address)
    },
    pizzaDraft: {
      ...emptyPizzaSelection(),
      ...(stored.pizzaDraft ?? {})
    },
    customer: stored.customer ?? null,
    order: stored.order ?? null,
    payment: stored.payment ?? null,
    latestStatus: stored.latestStatus ?? null
  };
}

function normalizeAddress(address: CustomerCreateRequest['address'] | string | undefined): DeliveryAddress {
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
