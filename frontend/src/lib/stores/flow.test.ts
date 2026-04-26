import { get } from 'svelte/store';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('$app/environment', () => ({
  browser: true
}));

const storageState = new Map<string, string>();

const getItemMock = vi.fn((key: string) => storageState.get(key) ?? null);
const setItemMock = vi.fn((key: string, value: string) => {
  storageState.set(key, value);
});
const removeItemMock = vi.fn((key: string) => {
  storageState.delete(key);
});
const clearMock = vi.fn(() => {
  storageState.clear();
});

const localStorageMock = {
  getItem: getItemMock,
  setItem: setItemMock,
  removeItem: removeItemMock,
  clear: clearMock
} as unknown as Storage;

async function loadFlowModule() {
  vi.resetModules();
  return import('$lib/stores/flow');
}

describe('flow store normalization', () => {
  beforeEach(() => {
    storageState.clear();
    vi.clearAllMocks();
    vi.stubGlobal('localStorage', localStorageMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('starts with canonical structured defaults', async () => {
    const { emptyDeliveryAddress, flow } = await loadFlowModule();

    expect(get(flow).accountDraft.address).toEqual(emptyDeliveryAddress());
    expect(get(flow).pizzaDrafts).toEqual([
      {
        crust: '',
        sauce: '',
        cheese: '',
        toppings: []
      }
    ]);
  });

  it('restores legacy stored drafts into the canonical state shape', async () => {
    storageState.set(
      'restaurant-delivery-flow',
      JSON.stringify({
        accountDraft: {
          name: 'Jordan Lee',
          email: 'jordan@example.com',
          phone: '555-0101',
          address: '17 Market Street'
        },
        pizzaDraft: {
          crust: 'hand-tossed',
          sauce: 'classic-red',
          cheese: 'mozzarella',
          toppings: ['pepperoni']
        },
        customer: null,
        order: null,
        payment: null,
        latestStatus: null
      })
    );

    const { flow } = await loadFlowModule();
    const state = get(flow);
    const persisted = JSON.parse(setItemMock.mock.calls.at(-1)?.[1] ?? '{}') as Record<string, unknown>;

    expect(state.accountDraft.address).toEqual({
      streetAddress: '17 Market Street',
      apartment: '',
      city: '',
      state: '',
      zip: '',
      country: 'United States'
    });
    expect(state.pizzaDrafts).toEqual([
      {
        crust: 'hand-tossed',
        sauce: 'classic-red',
        cheese: 'mozzarella',
        toppings: ['pepperoni']
      }
    ]);
    expect(persisted.pizzaDrafts).toEqual([
      {
        crust: 'hand-tossed',
        sauce: 'classic-red',
        cheese: 'mozzarella',
        toppings: ['pepperoni']
      }
    ]);
    expect(persisted.pizzaDraft).toBeUndefined();
  });
});
