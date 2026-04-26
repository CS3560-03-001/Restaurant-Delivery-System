import { writable } from 'svelte/store';
import { browser } from '$app/environment';

const STORAGE_KEY = 'restaurant-delivery-role';
const LOGIN_KEY = 'restaurant-delivery-auth';
const USERS_KEY = 'restaurant-delivery-users';
const DEMO_PASSWORD = 'demo';

const demoStaffUsers: Record<string, Record<string, { password: string }>> = {
  Cashier: {
    'demo-cashier-1': { password: DEMO_PASSWORD },
    'demo-cashier-2': { password: DEMO_PASSWORD },
    'demo-cashier-3': { password: DEMO_PASSWORD }
  },
  Cook: {
    'demo-cook-1': { password: DEMO_PASSWORD },
    'demo-cook-2': { password: DEMO_PASSWORD },
    'demo-cook-3': { password: DEMO_PASSWORD }
  },
  Driver: {
    'demo-driver-1': { password: DEMO_PASSWORD },
    'demo-driver-2': { password: DEMO_PASSWORD },
    'demo-driver-3': { password: DEMO_PASSWORD }
  }
};

const createDefaultUsers = (): Record<string, Record<string, any>> => ({
  Customer: {},
  Cashier: { ...demoStaffUsers.Cashier },
  Cook: { ...demoStaffUsers.Cook },
  Driver: { ...demoStaffUsers.Driver }
});

const withDemoStaffUsers = (users: Record<string, Record<string, any>>) => ({
  ...users,
  Customer: users.Customer || {},
  Cashier: { ...(users.Cashier || {}), ...demoStaffUsers.Cashier },
  Cook: { ...(users.Cook || {}), ...demoStaffUsers.Cook },
  Driver: { ...(users.Driver || {}), ...demoStaffUsers.Driver }
});

const initialRole = browser ? (localStorage.getItem(STORAGE_KEY) || 'Customer') : 'Customer';
let initialAuth: Record<string, string> = {};
let initialUsers: Record<string, Record<string, any>> = createDefaultUsers();

if (browser) {
  try {
    initialAuth = JSON.parse(localStorage.getItem(LOGIN_KEY) || '{}');
  } catch {
    initialAuth = {};
  }
  try {
    const storedUsers = localStorage.getItem(USERS_KEY);
    if (storedUsers) {
      initialUsers = withDemoStaffUsers(JSON.parse(storedUsers));
    }
  } catch {
    initialUsers = createDefaultUsers();
  }
}

export const role = writable<string>(initialRole);
export const authState = writable<Record<string, string>>(initialAuth);
export const registeredUsers = writable<Record<string, Record<string, any>>>(initialUsers);

if (browser) {
  role.subscribe((val) => {
    localStorage.setItem(STORAGE_KEY, val);
  });
  authState.subscribe((val) => {
    localStorage.setItem(LOGIN_KEY, JSON.stringify(val));
  });
  registeredUsers.subscribe((val) => {
    localStorage.setItem(USERS_KEY, JSON.stringify(val));
  });
}
