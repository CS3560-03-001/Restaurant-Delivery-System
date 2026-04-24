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
    paymentMethod: 'Mock Visa',
    amount: initialState.order?.amount ?? 0,
    billingName: initialState.customer?.profile.name ?? '',
    cardLast4: '4242'
  };

  let submitError = '';
  let submitting = false;

  $: paymentPreview = JSON.stringify(form, null, 2);

  async function handleSubmit() {
    submitError = '';

    if (!form.orderId) {
      submitError = 'Create an order before accessing payment.';
      return;
    }

    if (!form.billingName || !form.cardLast4) {
      submitError = 'Billing name and mock card digits are required.';
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
</script>

<svelte:head>
  <title>Mock Payment | Restaurant Delivery Prototype</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Submit mock payment</h2>
      <p>The payment request includes the current order, amount, method, and billing identity.</p>
    </div>

    {#if $flow.order}
      <div class="summary-strip">
        <strong>Current order</strong>
        <span>Order ID: {$flow.order.orderId}</span>
        <span>Total: ${$flow.order.amount.toFixed(2)}</span>
      </div>
    {:else}
      <p class="error">No order is available yet. Return to the order page first.</p>
    {/if}

    <div class="field">
      <label for="paymentMethod">Payment method</label>
      <select id="paymentMethod" bind:value={form.paymentMethod}>
        <option>Mock Visa</option>
        <option>Mock Mastercard</option>
        <option>Mock Cash</option>
      </select>
    </div>

    <FormField id="billingName" label="Billing name" bind:value={form.billingName} required />
    <FormField id="cardLast4" label="Card last 4" bind:value={form.cardLast4} required />

    {#if submitError}
      <p class="error">{submitError}</p>
    {/if}

    <div class="actions">
      <button class="primary" type="button" on:click={handleSubmit} disabled={submitting || !$flow.order}>
        {submitting ? 'Submitting payment...' : 'Pay and track order'}
      </button>
    </div>
  </section>

  <aside class="panel sidebar-card">
    <div class="hero">
      <h3>Payment JSON preview</h3>
      <p class="muted">The frontend keeps the contract stable whether the backend is mocked or real.</p>
    </div>

    <pre class="code-block">{paymentPreview}</pre>
  </aside>
</div>
