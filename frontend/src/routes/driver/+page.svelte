<script lang="ts">
  import { authState, registeredUsers } from '$lib/stores/role';

  let deliveries = [
    { id: '1001', address: '123 Main St, Apt 4B', status: 'Prepared', customer: 'John Doe' },
    { id: '1002', address: '456 Elm St', status: 'Prepared', customer: 'Jane Smith' },
    { id: '1003', address: '789 Oak Ave', status: 'Prepared', customer: 'Alex Johnson' }
  ];

  function outForDelivery(id: string) {
    deliveries = deliveries.map(d => {
      if (d.id === id) {
        return { ...d, status: 'Out for Delivery' };
      }
      return d;
    });
  }

  function markDelivered(id: string) {
    deliveries = deliveries.filter(d => d.id !== id);
  }

  let username = '';
  let password = '';
  let authError = '';

  function handleAuth() {
    authError = '';
    const users = $registeredUsers.Driver || {};

    const userObj = users[username.trim()];
    const isString = typeof userObj === 'string';
    const actualPassword = isString ? userObj : userObj?.password;

    if (userObj && actualPassword === password) {
      $authState = { ...$authState, driver: username.trim() };
    } else {
      authError = 'Invalid credentials or user does not exist.';
    }
  }

  function handleLogout() {
    $authState = { ...$authState, driver: '' };
    username = '';
    password = '';
  }
</script>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Driver Dashboard</h2>
      {#if $authState.driver}
        <p>Logged in as: <strong>{$authState.driver}</strong></p>
        <button class="secondary compact-action" type="button" on:click={handleLogout}>Log out</button>
      {:else}
        <p>Please log in to continue.</p>
      {/if}
    </div>

    {#if !$authState.driver}
      <div class="field">
        <label for="username">Driver Username</label>
        <input id="username" type="text" bind:value={username} placeholder="e.g. driver_tim" />
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
      {#if deliveries.length === 0}
        <p class="muted">No pending deliveries. Great job!</p>
      {:else}
        <ul class="delivery-list">
          {#each deliveries as delivery}
            <li class="delivery-item">
              <div>
                <strong>Order #{delivery.id} - {delivery.customer}</strong>
                <p class="address">{delivery.address}</p>
                <p>
                  Status:
                  <span class:out={delivery.status === 'Out for Delivery'} class="chip">
                    {delivery.status}
                  </span>
                </p>
              </div>
              
              <div class="actions">
                {#if delivery.status === 'Prepared'}
                  <button class="secondary" type="button" on:click={() => outForDelivery(delivery.id)}>
                    Out for Delivery
                  </button>
                {:else}
                  <button class="primary" type="button" on:click={() => markDelivered(delivery.id)}>
                    Mark Delivered
                  </button>
                {/if}
              </div>
            </li>
          {/each}
        </ul>
      {/if}
    {/if}
  </section>
</div>

<style>
  .delivery-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  .delivery-item {
    background: var(--surface-2);
    padding: 1rem;
    margin-bottom: 1rem;
    border: 1px solid var(--border);
    border-radius: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
  }
  .address {
    margin: 4px 0;
    color: var(--text-2);
  }
  .chip {
    padding: 2px 6px;
    border-radius: 0;
    font-size: 0.85em;
    background: var(--surface-3);
  }
  .chip.out {
    background: var(--warning-surface);
    color: var(--warning-text);
    font-weight: bold;
  }
</style>
