<script lang="ts">
  import { authState, registeredUsers } from '$lib/stores/role';

  let pendingOrders = [
    { id: '1001', pizzas: 3, status: 'Preparing', time: '10:00 AM' },
    { id: '1002', pizzas: 1, status: 'Preparing' , time: '10:05 AM' },
    { id: '1003', pizzas: 5, status: 'Preparing' , time: '10:12 AM' }
  ];

  function markPrepared(id: string) {
    pendingOrders = pendingOrders.filter(o => o.id !== id);
  }

  let username = '';
  let password = '';
  let authError = '';

  function handleAuth() {
    authError = '';
    const users = $registeredUsers.Cook || {};

    const userObj = users[username.trim()];
    const isString = typeof userObj === 'string';
    const actualPassword = isString ? userObj : userObj?.password;

    if (userObj && actualPassword === password) {
      $authState = { ...$authState, cook: username.trim() };
    } else {
      authError = 'Invalid credentials or user does not exist.';
    }
  }

  function handleLogout() {
    $authState = { ...$authState, cook: '' };
    username = '';
    password = '';
  }
</script>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Kitchen Display (Cook)</h2>
      {#if $authState.cook}
        <p>Logged in as: <strong>{$authState.cook}</strong></p>
        <button class="secondary compact-action" type="button" on:click={handleLogout}>Log out</button>
      {:else}
        <p>Please log in to continue.</p>
      {/if}
    </div>

    {#if !$authState.cook}
      <div class="field">
        <label for="username">Cook Username</label>
        <input id="username" type="text" bind:value={username} placeholder="e.g. chef_john" />
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
      {#if pendingOrders.length === 0}
        <p class="muted">No pending orders. Good job!</p>
      {:else}
      <ul class="order-list">
        {#each pendingOrders as order}
          <li class="order-item">
            <div>
              <strong>Order #{order.id}</strong>
              <span> ({order.pizzas} pizzas)</span>
              <br/>
              <small class="muted">Received at {order.time}</small>
            </div>
            <button class="primary" type="button" on:click={() => markPrepared(order.id)}>
              Mark as Prepared
            </button>
          </li>
        {/each}
      </ul>
      {/if}
    {/if}
  </section>
</div>

<style>
  .order-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  .order-item {
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
</style>
