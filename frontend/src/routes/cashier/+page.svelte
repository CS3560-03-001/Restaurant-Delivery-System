<script lang="ts">
  import { authState, registeredUsers } from '$lib/stores/role';
  
  let walkInName = '';
  let selectedPizzas = 0;
  let cashAmount = 0;
  let processing = false;
  let successMessage = '';

  async function processOrder() {
    processing = true;
    successMessage = '';
    
    // Simulate network request
    await new Promise((resolve) => setTimeout(resolve, 1000));
    
    successMessage = `Successfully placed walk-in order for ${walkInName || 'Guest'} (${selectedPizzas} pizzas) using Cash: $${cashAmount}.`;
    processing = false;
    walkInName = '';
    selectedPizzas = 0;
    cashAmount = 0;
  }

  let isSignup = false;
  let username = '';
  let password = '';
  let email = '';
  let authError = '';

  function handleAuth() {
    authError = '';
    const users = $registeredUsers.Cashier || {};

    if (isSignup) {
      if (users[username.trim()]) {
        authError = 'Username already exists.';
        return;
      }
      if (!email.trim() || !username.trim() || !password.trim()) {
        authError = 'All fields are required to sign up.';
        return;
      }
      $registeredUsers = {
        ...$registeredUsers,
        Cashier: { ...users, [username.trim()]: { password, email } }
      };
      $authState = { ...$authState, cashier: username.trim() };
    } else {
      const userObj = users[username.trim()];
      const isString = typeof userObj === 'string';
      const actualPassword = isString ? userObj : userObj?.password;

      if (userObj && actualPassword === password) {
        $authState = { ...$authState, cashier: username.trim() };
      } else {
        authError = 'Invalid credentials or user does not exist.';
      }
    }
  }

  function handleLogout() {
    $authState = { ...$authState, cashier: '' };
    username = '';
    password = '';
    email = '';
  }
</script>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Cashier Terminal</h2>
      {#if $authState.cashier}
        <p>Logged in as: <strong>{$authState.cashier}</strong></p>
        <button class="secondary" style="margin-top: 0.5rem; max-width: 150px;" type="button" on:click={handleLogout}>Log out</button>
      {:else}
        <p>Please log in to continue.</p>
      {/if}
    </div>

    {#if !$authState.cashier}
      <div class="field">
        <label>
          <input type="radio" bind:group={isSignup} value={false} /> Log In
        </label>
        <label style="margin-left: 1rem;">
          <input type="radio" bind:group={isSignup} value={true} /> Sign Up
        </label>
      </div>

      <div class="field">
        <label for="username">Cashier Username</label>
        <input id="username" type="text" bind:value={username} placeholder="e.g. employee_1" />
      </div>
      <div class="field">
        <label for="password">Password</label>
        <input id="password" type="password" bind:value={password} />
      </div>
      {#if isSignup}
        <div class="field">
          <label for="email">Email</label>
          <input id="email" type="email" bind:value={email} placeholder="e.g. employee_1@restaurant.com" />
        </div>
      {/if}
      {#if authError}
        <p class="error">{authError}</p>
      {/if}
      <div class="actions">
        <button class="primary" type="button" on:click={handleAuth} disabled={!username.trim() || !password.trim() || (isSignup && !email.trim())}>
          {isSignup ? 'Sign Up' : 'Log In'}
        </button>
      </div>
    {:else}
      <div class="field">
        <label for="walkInName">Customer Name (Optional)</label>
        <input id="walkInName" type="text" bind:value={walkInName} placeholder="Walk-in Guest" />
      </div>

      <div class="field">
        <label for="selectedPizzas">Number of Pizzas</label>
        <input id="selectedPizzas" type="number" min="1" max="20" bind:value={selectedPizzas} />
      </div>

      <div class="field">
        <label for="cashAmount">Cash Tendered</label>
        <input id="cashAmount" type="number" min="0" step="0.01" bind:value={cashAmount} />
      </div>

      {#if successMessage}
        <p class="success" style="color: green;">{successMessage}</p>
      {/if}

      <div class="actions">
        <button class="primary" type="button" on:click={processOrder} disabled={processing || selectedPizzas < 1 || cashAmount <= 0}>
          {processing ? 'Processing...' : 'Complete Order & Bypass Card'}
        </button>
      </div>
    {/if}
  </section>
</div>
