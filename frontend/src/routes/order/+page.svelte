<script lang="ts">
  import { goto } from '$app/navigation';
  import { createOrder } from '$lib/api/client';
  import CartSummary from '$lib/components/CartSummary.svelte';
  import OptionGroup from '$lib/components/OptionGroup.svelte';
  import type { PizzaSelection } from '$lib/contracts';
  import { menuOptions } from '$lib/menu';
  import { flow, isPizzaReady } from '$lib/stores/flow';
  import { get } from 'svelte/store';

  const initialState = get(flow);
  let pizza: PizzaSelection = {
    ...initialState.pizzaDraft,
    toppings: [...initialState.pizzaDraft.toppings]
  };

  let submitting = false;
  let submitError = '';

  $: flow.setPizzaDraft(pizza);
  $: readyForCheckout = isPizzaReady(pizza);
  $: total = Number((12 + pizza.toppings.length * 1.75).toFixed(2));
  $: orderPreview = JSON.stringify(
    {
      customerId: $flow.customer?.customerId ?? 'customer-id-required',
      pizza
    },
    null,
    2
  );

  async function handleCheckout() {
    submitError = '';

    if (!$flow.customer?.customerId) {
      submitError = 'Complete account creation before submitting an order.';
      return;
    }

    if (!readyForCheckout) {
      submitError = 'Select crust, sauce, and cheese before checkout.';
      return;
    }

    submitting = true;

    try {
      const order = await createOrder(fetch, {
        customerId: $flow.customer.customerId,
        pizza
      });
      flow.setOrder(order);
      await goto('/payment');
    } catch (error) {
      submitError = error instanceof Error ? error.message : 'Unable to create order.';
    } finally {
      submitting = false;
    }
  }
</script>

<svelte:head>
  <title>Build Pizza | Restaurant Delivery Prototype</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Build your pizza</h2>
      <p>Crust, sauce, and cheese are single-choice groups. Toppings stay multi-select.</p>
    </div>

    <OptionGroup
      groupLabel="Crust"
      description="Choose one base for the pizza."
      options={menuOptions.crust}
      bind:selectedValue={pizza.crust}
    />

    <OptionGroup
      groupLabel="Sauce"
      description="Choose one sauce to anchor the flavor profile."
      options={menuOptions.sauce}
      bind:selectedValue={pizza.sauce}
    />

    <OptionGroup
      groupLabel="Cheese"
      description="Choose one cheese option before checkout unlocks."
      options={menuOptions.cheese}
      bind:selectedValue={pizza.cheese}
    />

    <OptionGroup
      groupLabel="Toppings"
      description="Mix and match optional toppings."
      options={menuOptions.toppings}
      multiple={true}
      bind:selectedValues={pizza.toppings}
    />

    {#if submitError}
      <p class="error">{submitError}</p>
    {/if}

    <div class="actions">
      <button class="primary" type="button" on:click={handleCheckout} disabled={!readyForCheckout || submitting}>
        {submitting ? 'Submitting order...' : 'Continue to checkout'}
      </button>
      <small class="muted">
        {#if readyForCheckout}
          Required pizza groups are complete.
        {:else}
          Select crust, sauce, and cheese to enable checkout.
        {/if}
      </small>
    </div>
  </section>

  <aside class="panel sidebar-card">
    <CartSummary pizza={pizza} amount={total} />

    <div class="summary-strip">
      <strong>Order JSON preview</strong>
      <small class="muted">The grouped contract keeps each pizza category distinct.</small>
      <pre class="code-block">{orderPreview}</pre>
    </div>
  </aside>
</div>
