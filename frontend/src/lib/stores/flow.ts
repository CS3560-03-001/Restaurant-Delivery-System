import { browser } from '$app/environment';
import type {
  CustomerCreateRequest,
  CustomerCreateResponse,
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
  pizzaDraft: PizzaSelection;
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
  pizzaDraft: emptyPizzaSelection(),
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
    reset: () => store.set(defaultState),
    setAccountDraft: (accountDraft: CustomerCreateRequest) =>
      store.update((state) => ({ ...state, accountDraft })),
    setCustomer: (customer: CustomerCreateResponse, accountDraft: CustomerCreateRequest) =>
      store.update((state) => ({ ...state, customer, accountDraft })),
    setPizzaDraft: (pizzaDraft: PizzaSelection) => store.update((state) => ({ ...state, pizzaDraft })),
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
    return { ...defaultState, ...JSON.parse(raw) } as FlowState;
  } catch {
    return defaultState;
  }
}

export function isPizzaReady(selection: PizzaSelection) {
  return Boolean(selection.crust && selection.sauce && selection.cheese);
}

export const flow = createFlowStore();
