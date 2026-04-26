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

  let username = '';
  let password = '';
  let authError = '';

  function handleAuth() {
    authError = '';
    const users = $registeredUsers.Cashier || {};

    const userObj = users[username.trim()];
    const isString = typeof userObj === 'string';
    const actualPassword = isString ? userObj : userObj?.password;

    if (userObj && actualPassword === password) {
      $authState = { ...$authState, cashier: username.trim() };
    } else {
      authError = 'Invalid credentials or user does not exist.';
    }
  }

  function handleLogout() {
    $authState = { ...$authState, cashier: '' };
    username = '';
    password = '';
  }
</script>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Cashier Terminal</h2>
      {#if $authState.cashier}
        <p>Logged in as: <strong>{$authState.cashier}</strong></p>
        <button class="secondary compact-action" type="button" on:click={handleLogout}>Log out</button>
      {:else}
        <p>Please log in to continue.</p>
      {/if}
    </div>

    {#if !$authState.cashier}
      <div class="field">
        <label for="username">Cashier Username</label>
        <input id="username" type="text" bind:value={username} placeholder="e.g. employee_1" />
      </div>
      <div class="field">
        <label for="password">Password</label>
        <input id="password" type="password" bind:value={password} />
      </div>
      {#if authError}
        <p class="error">{authError}</p>
      {/if}
      <div class="actions">
        <button class="primary" type="button" on:click={handleAuth} disabled={!username.trim() || !password.trim()}>
          Log In
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
        <p class="success">{successMessage}</p>
      {/if}

      <div class="actions">
        <button class="primary" type="button" on:click={processOrder} disabled={processing || selectedPizzas < 1 || cashAmount <= 0}>
          {processing ? 'Processing...' : 'Complete Order & Bypass Card'}
        </button>
      </div>
    {/if}
  </section>
</div>
