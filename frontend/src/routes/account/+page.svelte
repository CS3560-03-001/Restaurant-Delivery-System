<script lang="ts">
  import { goto } from '$app/navigation';
  import { createCustomer, createOrder } from '$lib/api/client';
  import FormField from '$lib/components/FormField.svelte';
  import type { CustomerCreateRequest } from '$lib/contracts';
  import { emptyDeliveryAddress, flow, isPizzaReady } from '$lib/stores/flow';
  import { authState, registeredUsers } from '$lib/stores/role';
  import { get } from 'svelte/store';

  const initialDraft = get(flow).accountDraft;

  let form: CustomerCreateRequest = {
    ...initialDraft,
    address: {
      ...emptyDeliveryAddress(),
      ...initialDraft.address
    }
  };

  let isSignup = false;
  let username = '';
  let password = '';
  let email = '';
  let phone = '';
  let submitError = '';
  let successMessage = '';
  let submitting = false;
  let authError = '';

  $: flow.setAccountDraft(form);
  $: accountErrors = buildAccountErrors(form);
  $: accountReady = !Object.values(accountErrors).some(Boolean);

  function syncFormFromFlowState() {
    const snapshot = get(flow);

    form = {
      ...snapshot.accountDraft,
      address: {
        ...emptyDeliveryAddress(),
        ...snapshot.accountDraft.address
      }
    };
  }

  function handleAuth() {
    authError = '';
    submitError = '';
    successMessage = '';

    const users = $registeredUsers.Customer || {};
    const userName = username.trim();
    const userPassword = password.trim();
    const userEmail = email.trim();
    const userPhone = phone.trim();

    if (isSignup) {
      if (users[userName]) {
        authError = 'Username already exists.';
        return;
      }

      if (!userName || !userPassword || !userEmail || !userPhone) {
        authError = 'All fields are required to sign up.';
        return;
      }

      const initialFlow = flow.exportDefaultState();
      $registeredUsers = {
        ...$registeredUsers,
        Customer: {
          ...users,
          [userName]: { password: userPassword, email: userEmail, phone: userPhone, flowState: initialFlow }
        }
      };
      flow.overwriteState(initialFlow);
      $authState = { ...$authState, customer: userName };
      syncFormFromFlowState();
      form = {
        ...form,
        email: userEmail,
        phone: userPhone
      };
      isSignup = false;
      return;
    }

    const userObj = users[userName];
    const isString = typeof userObj === 'string';
    const actualPassword = isString ? userObj : userObj?.password;

    if (userObj && actualPassword === userPassword) {
      if (!isString && userObj.flowState) {
        flow.overwriteState(userObj.flowState);
      } else {
        flow.reset();
      }

      $authState = { ...$authState, customer: userName };
      syncFormFromFlowState();
    } else {
      authError = 'Invalid credentials or user does not exist.';
    }
  }

  function handleLogout() {
    $authState = { ...$authState, customer: '' };
    username = '';
    password = '';
    email = '';
    phone = '';
    authError = '';
    successMessage = '';
    isSignup = false;
    flow.reset();
    syncFormFromFlowState();
  }

  async function handleSubmit() {
    submitError = '';
    successMessage = '';

    if (!accountReady) {
      submitError = 'Complete the profile fields before continuing.';
      return;
    }

    submitting = true;

    try {
      const customer = await createCustomer(fetch, form);
      flow.setCustomer(customer, form);
      successMessage = `Customer profile saved as ${customer.customerId}.`;

      const drafts = get(flow).pizzaDrafts;
      if (drafts.length > 0 && drafts.every(isPizzaReady)) {
        const order = await createOrder(fetch, {
          customerId: customer.customerId,
          pizzas: drafts
        });
        flow.setOrder(order);
        await goto('/payment');
      } else {
        await goto('/order');
      }
    } catch (error) {
      submitError = error instanceof Error ? error.message : 'Unable to save customer profile.';
    } finally {
      submitting = false;
    }
  }

  function buildAccountErrors(value: CustomerCreateRequest) {
    return {
      name: validateName(value.name),
      email: validateEmail(value.email),
      phone: validatePhone(value.phone),
      streetAddress: validateRequired(value.address.streetAddress, 'Street address is required.'),
      apartment: '',
      city: validateRequired(value.address.city, 'City is required.'),
      state: validateState(value.address.state),
      zip: validateZip(value.address.zip),
      country: validateRequired(value.address.country, 'Country is required.')
    };
  }

  function validateRequired(value: string, message: string) {
    return value.trim() ? '' : message;
  }

  function validateName(value: string) {
    return value.trim().length >= 2 ? '' : 'Enter the customer name used on the order.';
  }

  function validateEmail(value: string) {
    return /[^\s@]+@[^\s@]+\.[^\s@]+/.test(value.trim()) ? '' : 'Enter a valid email address.';
  }

  function validatePhone(value: string) {
    return /^(?:\+?1[-.\s]?)?(?:\(?\d{3}\)?[-.\s]?)\d{3}[-.\s]?\d{4}$/.test(value.trim())
      ? ''
      : 'Enter a valid phone number.';
  }

  function validateState(value: string) {
    return /^[A-Za-z]{2}$/.test(value.trim()) ? '' : 'Use a 2-letter state code.';
  }

  function validateZip(value: string) {
    return /^\d{5}(?:-\d{4})?$/.test(value.trim()) ? '' : 'Enter a 5-digit ZIP code.';
  }
</script>

<svelte:head>
  <title>Customer Profile | Restaurant Delivery Demo</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    {#if !$authState.customer}
      <div class="hero">
        <h2>Customer account</h2>
        <p>Log in or create an account to unlock the customer flow.</p>
      </div>

      <div class="mode-toggle" role="tablist" aria-label="Customer authentication mode">
        <button
          class="mode-card"
          class:active={!isSignup}
          type="button"
          aria-pressed={!isSignup}
          on:click={() => (isSignup = false)}
        >
          <strong>Log In</strong>
          <span>Use an existing customer profile.</span>
        </button>
        <button
          class="mode-card"
          class:active={isSignup}
          type="button"
          aria-pressed={isSignup}
          on:click={() => (isSignup = true)}
        >
          <strong>Sign Up</strong>
          <span>Create a new customer account.</span>
        </button>
      </div>

      <div class="field">
        <label for="username">Username</label>
        <input id="username" type="text" bind:value={username} placeholder="e.g. jordanlee" />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" type="password" bind:value={password} />
      </div>

      {#if isSignup}
        <div class="field">
          <label for="email">Email</label>
          <input id="email" type="email" bind:value={email} placeholder="jordan@example.com" />
        </div>

        <div class="field">
          <label for="phone">Phone number</label>
          <input id="phone" type="tel" bind:value={phone} placeholder="(555) 010-0101" />
        </div>
      {/if}

      {#if authError}
        <p class="error">{authError}</p>
      {/if}

      <div class="actions">
        <button class="primary" type="button" on:click={handleAuth} disabled={!username.trim() || !password.trim() || (isSignup && (!email.trim() || !phone.trim()))}>
          {isSignup ? 'Sign Up' : 'Log In'}
        </button>
      </div>
    {:else}
      <div class="hero">
        <h2>Customer account</h2>
        <div class="summary-strip">
          <strong>Logged in as {$authState.customer}</strong>
          <span>Update your customer profile before continuing.</span>
        </div>
      </div>

      <div class="actions">
        <button class="secondary" type="button" on:click={handleLogout}>Log out</button>
      </div>

      <div class="divider"></div>

      <div class="hero">
        <h2>Customer profile</h2>
        <p>Save the contact and delivery details we need to place the order.</p>
      </div>

      <FormField
        id="name"
        label="Full name"
        bind:value={form.name}
        error={accountErrors.name}
        placeholder="Jordan Lee"
        autocomplete="name"
        required
      />
      <FormField
        id="email"
        label="Email"
        type="email"
        bind:value={form.email}
        error={accountErrors.email}
        placeholder="jordan@example.com"
        autocomplete="email"
        required
      />

      <div class="address-grid">
        <FormField
          id="phone"
          label="Phone number"
          type="tel"
          bind:value={form.phone}
          error={accountErrors.phone}
          placeholder="(555) 010-0101"
          autocomplete="tel"
          inputmode="tel"
          required
        />

        <FormField
          id="streetAddress"
          label="Street address"
          bind:value={form.address.streetAddress}
          error={accountErrors.streetAddress}
          placeholder="123 Market Street"
          autocomplete="address-line1"
          required
        />

        <FormField
          id="apartment"
          label="Apartment, suite, etc."
          bind:value={form.address.apartment}
          help="Optional delivery notes like apartment, suite, or unit number."
          autocomplete="address-line2"
        />

        <div class="address-row">
          <FormField
            id="city"
            label="City"
            bind:value={form.address.city}
            error={accountErrors.city}
            placeholder="Austin"
            autocomplete="address-level2"
            required
          />

          <FormField
            id="state"
            label="State"
            bind:value={form.address.state}
            error={accountErrors.state}
            placeholder="TX"
            autocomplete="address-level1"
            maxlength={2}
            required
          />

          <FormField
            id="zip"
            label="ZIP code"
            bind:value={form.address.zip}
            error={accountErrors.zip}
            placeholder="78701"
            autocomplete="postal-code"
            inputmode="numeric"
            maxlength={10}
            required
          />
        </div>

        <FormField
          id="country"
          label="Country"
          bind:value={form.address.country}
          error={accountErrors.country}
          placeholder="United States"
          autocomplete="country"
          required
        />
      </div>

      {#if submitError}
        <p class="error">{submitError}</p>
      {/if}

      {#if successMessage}
        <p class="success">{successMessage}</p>
      {/if}

      <div class="actions">
        <button class="primary" type="button" on:click={handleSubmit} disabled={submitting || !accountReady}>
          {submitting ? 'Saving profile...' : 'Save customer and continue'}
        </button>
        <small class="muted">The next step unlocks after the customer profile is valid and saved.</small>
      </div>
    {/if}
  </section>
</div>
