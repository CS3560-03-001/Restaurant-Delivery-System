<script lang="ts">
  import '../app.css';
  import { page } from '$app/state';

  const steps = [
    { href: '/account', label: 'Account', detail: 'Create customer profile' },
    { href: '/order', label: 'Order', detail: 'Build grouped pizza' },
    { href: '/payment', label: 'Payment', detail: 'Submit mock checkout' },
    { href: '/status', label: 'Status', detail: 'Poll delivery updates' }
  ];
</script>

<div class="shell">
  <div class="hero">
    <p class="muted">Restaurant delivery prototype</p>
    <h1>Simple pizza ordering with stable JSON contracts.</h1>
    <p>
      The flow stays form-driven: capture customer details, build a pizza, submit mock payment,
      then poll for status updates.
    </p>
  </div>

  <nav class="step-nav" aria-label="Order workflow steps">
    {#each steps as step}
      <a class:active={page.url.pathname === step.href} class="step-link" href={step.href}>
        <strong>{step.label}</strong>
        <span>{step.detail}</span>
      </a>
    {/each}
  </nav>

  <slot />
</div>
