<script lang="ts">
  import '../app.css';
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { flow, type FlowState } from '$lib/stores/flow';

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
</script>

<div class="shell">
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

  <slot />
</div>
