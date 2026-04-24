<script lang="ts">
  import { goto } from '$app/navigation';
  import { submitPayment } from '$lib/api/client';
  import FormField from '$lib/components/FormField.svelte';
  import { flow } from '$lib/stores/flow';
  import type { PaymentRequest } from '$lib/contracts';
  import { get } from 'svelte/store';

  const initialState = get(flow);

  let form: PaymentRequest = {
    orderId: initialState.order?.orderId ?? '',
    paymentMethod: 'Visa',
    amount: initialState.order?.amount ?? 0,
    billingName: initialState.customer?.profile.name ?? '',
    cardLast4: '4242'
  };

  let submitError = '';
  let submitting = false;

  $: paymentPreview = JSON.stringify(form, null, 2);
  $: paymentReady = Boolean(form.orderId) && validatePayment();

  async function handleSubmit() {
    submitError = '';

    if (!form.orderId) {
      submitError = 'Complete the order step before payment.';
      return;
    }

    if (!validatePayment()) {
      submitError = 'Enter a billing name and a 4-digit card suffix.';
      return;
    }

    submitting = true;

    try {
      const payment = await submitPayment(fetch, form);
      flow.setPayment(payment);
      await goto('/status');
    } catch (error) {
      submitError = error instanceof Error ? error.message : 'Unable to submit payment.';
    } finally {
      submitting = false;
    }
  }

  function validatePayment() {
    return Boolean(form.billingName.trim()) && /^\d{4}$/.test(form.cardLast4.trim());
  }
</script>

<svelte:head>
  <title>Payment Review | Restaurant Delivery Demo</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Payment details</h2>
      <p>Review the total, confirm the billing name, and submit the checkout.</p>
    </div>

    {#if $flow.order}
      <div class="summary-strip">
        <strong>Current order</strong>
        <span>Order ID: {$flow.order.orderId}</span>
        <span>Pizzas: {$flow.order.pizzas.length}</span>
        <span>Total: ${$flow.order.amount.toFixed(2)}</span>
      </div>
    {:else}
      <p class="error">No order is ready for payment yet. Complete the order step first.</p>
    {/if}

    <div class="field">
      <label for="paymentMethod">Payment method</label>
      <select id="paymentMethod" bind:value={form.paymentMethod}>
        <option>Visa</option>
        <option>Mastercard</option>
        <option>Cash</option>
      </select>
    </div>

    <FormField
      id="billingName"
      label="Billing name"
      bind:value={form.billingName}
      placeholder="Jordan Lee"
      autocomplete="cc-name"
      required
    />
    <FormField
      id="cardLast4"
      label="Card last 4"
      bind:value={form.cardLast4}
      placeholder="4242"
      inputmode="numeric"
      maxlength={4}
      required
    />

    {#if submitError}
      <p class="error">{submitError}</p>
    {/if}

    <div class="actions">
      <button class="primary" type="button" on:click={handleSubmit} disabled={submitting || !paymentReady}>
        {submitting ? 'Submitting payment...' : 'Submit payment and continue'}
      </button>
      <small class="muted">
        {#if paymentReady}
          Payment details are complete.
        {:else}
          Enter the billing name and a 4-digit card suffix to continue.
        {/if}
      </small>
    </div>
  </section>

  <aside class="panel sidebar-card">
    <div class="hero">
      <h3>Payment request preview</h3>
      <p class="muted">The frontend keeps the contract stable whether the backend is mocked or live.</p>
    </div>

    <pre class="code-block">{paymentPreview}</pre>
  </aside>
</div>
