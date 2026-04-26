<script lang="ts">
  import '../app.css';
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { flow, type FlowState } from '$lib/stores/flow';
  import { role } from '$lib/stores/role';

  const steps = [
    { href: '/account', label: 'Account', detail: 'Customer details and delivery address' },
    { href: '/order', label: 'Order', detail: 'Build your pizza selections' },
    { href: '/payment', label: 'Payment', detail: 'Review and submit payment' },
    { href: '/status', label: 'Status', detail: 'Track delivery' }
  ];

  function isUnlocked(index: number, state: FlowState) {
    if (index === 0) {
      return true;
    }

    if (index === 1) {
      return Boolean(state.customer);
    }

    if (index === 2) {
      return Boolean(state.order);
    }

    return Boolean(state.payment);
  }

  async function goToStep(href: string, unlocked: boolean) {
    if (!unlocked) {
      return;
    }

    await goto(href);
  }

  const roles = ['Customer', 'Cashier', 'Cook', 'Driver'];
  
  $: if ($role) {
    if ($role === 'Cashier' && !page.url.pathname.startsWith('/cashier')) {
      goto('/cashier');
    } else if ($role === 'Cook' && !page.url.pathname.startsWith('/cook')) {
      goto('/cook');
    } else if ($role === 'Driver' && !page.url.pathname.startsWith('/driver')) {
      goto('/driver');
    } else if ($role === 'Customer' && (page.url.pathname.startsWith('/cashier') || page.url.pathname.startsWith('/cook') || page.url.pathname.startsWith('/driver'))) {
        goto('/order');
    }
  }
</script>

<div class="shell">
  <div class="role-switcher">
    <strong>I am a:</strong>
    {#each roles as r}
      <label>
        <input type="radio" bind:group={$role} value={r} />
        {r}
      </label>
    {/each}
  </div>

  {#if $role === 'Customer'}
    <div class="hero">
      <p class="muted">Restaurant Delivery Form</p>
      <h1>Place an Order</h1>
      <p>Complete each form before moving onto the next.</p>
    </div>

    <nav class="step-nav" aria-label="Order workflow steps">
      {#each steps as step, index}
        {@const unlocked = isUnlocked(index, $flow)}
        <button
          type="button"
          class:active={page.url.pathname === step.href}
          class:locked={!unlocked}
          class="step-link"
          disabled={!unlocked}
          on:click={() => goToStep(step.href, unlocked)}
        >
          <strong>{step.label}</strong>
          <span>{step.detail}</span>
        </button>
      {/each}
    </nav>
  {/if}

  <slot />
</div>

<style>
  .role-switcher {
    padding: 1rem;
    background: var(--surface-2);
    margin-bottom: 2rem;
    border-radius: 8px;
    display: flex;
    gap: 1rem;
    align-items: center;
  }
</style>
