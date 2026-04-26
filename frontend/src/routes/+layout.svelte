<script lang="ts">
  import '../app.css';
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { flow, type FlowState } from '$lib/stores/flow';
  import { role } from '$lib/stores/role';

  const steps = [
    { href: '/account', label: 'Account', detail: 'Create customer profile' },
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
    } else if (
      $role === 'Customer' &&
      (page.url.pathname.startsWith('/cashier') || page.url.pathname.startsWith('/cook') || page.url.pathname.startsWith('/driver'))
    ) {
      goto('/account');
    }
  }
</script>

<div class="shell">
  <div class="role-switcher">
    <strong>I am a:</strong>
    <div class="role-options">
      {#each roles as r}
        <label class="role-option" class:active={$role === r}>
          <input type="radio" bind:group={$role} value={r} />
          <span>{r}</span>
        </label>
      {/each}
    </div>
  </div>

  {#if $role === 'Customer'}
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

    <slot />
  {:else}
    <slot />
  {/if}
</div>
