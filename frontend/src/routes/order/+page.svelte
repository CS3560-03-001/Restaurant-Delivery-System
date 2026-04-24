<script lang="ts">
  import { goto } from '$app/navigation';
  import { createOrder } from '$lib/api/client';
  import CartSummary from '$lib/components/CartSummary.svelte';
  import OptionGroup from '$lib/components/OptionGroup.svelte';
  import type { PizzaSelection } from '$lib/contracts';
  import { calculatePizzaAmount, menuOptions } from '$lib/menu';
  import { emptyPizzaSelection, flow, isPizzaReady } from '$lib/stores/flow';
  import { get } from 'svelte/store';

  const initialState = get(flow);
  let pizzas: PizzaSelection[] = initialState.pizzaDrafts.length
    ? initialState.pizzaDrafts.map((pizza) => ({ ...pizza, toppings: [...pizza.toppings] }))
    : [emptyPizzaSelection()];

  let submitting = false;
  let submitError = '';

  $: flow.setPizzaDrafts(pizzas.map((pizza) => ({ ...pizza, toppings: [...pizza.toppings] })));
  $: readyForCheckout = pizzas.length > 0 && pizzas.every((pizza) => isPizzaReady(pizza));
  $: total = Number(pizzas.reduce((sum, pizza) => sum + calculatePizzaAmount(pizza), 0).toFixed(2));
  $: orderPreview = JSON.stringify(
    {
      customerId: $flow.customer?.customerId ?? 'customer-id-required',
      pizzas
    },
    null,
    2
  );

  function addPizza() {
    pizzas = [...pizzas, emptyPizzaSelection()];
  }

  function removePizza(index: number) {
    pizzas = pizzas.filter((_, currentIndex) => currentIndex !== index);
  }

  async function handleCheckout() {
    submitError = '';

    if (!$flow.customer?.customerId) {
      submitError = 'Complete the customer profile before saving an order.';
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
        pizzas
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
  <title>Build Order | Restaurant Delivery Prototype</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Build your order</h2>
      <p>Each pizza keeps crust, sauce, and cheese as single-choice groups while toppings remain multi-select.</p>
    </div>

    {#each pizzas as _pizza, index}
      <div class="cart-group">
        <div class="actions">
          <h3>Pizza {index + 1}</h3>
          {#if pizzas.length > 1}
            <button class="secondary" type="button" on:click={() => removePizza(index)}>Remove pizza</button>
          {/if}
        </div>

        <OptionGroup
          groupLabel="Crust"
          groupName={`pizza-${index}-crust`}
          description="Choose one base for the pizza."
          options={menuOptions.crust}
          bind:selectedValue={pizzas[index].crust}
        />

        <OptionGroup
          groupLabel="Sauce"
          groupName={`pizza-${index}-sauce`}
          description="Choose one sauce to anchor the flavor profile."
          options={menuOptions.sauce}
          bind:selectedValue={pizzas[index].sauce}
        />

        <OptionGroup
          groupLabel="Cheese"
          groupName={`pizza-${index}-cheese`}
          description="Choose one cheese option before checkout unlocks."
          options={menuOptions.cheese}
          bind:selectedValue={pizzas[index].cheese}
        />

        <OptionGroup
          groupLabel="Toppings"
          groupName={`pizza-${index}-toppings`}
          description="Mix and match optional toppings."
          options={menuOptions.toppings}
          multiple={true}
          bind:selectedValues={pizzas[index].toppings}
        />
      </div>
    {/each}

    {#if submitError}
      <p class="error">{submitError}</p>
    {/if}

    <div class="actions">
      <button class="secondary" type="button" on:click={addPizza}>Add another pizza</button>
      <button class="primary" type="button" on:click={handleCheckout} disabled={!readyForCheckout || submitting}>
        {submitting ? 'Submitting order...' : 'Continue to checkout'}
      </button>
      <small class="muted">
        {#if readyForCheckout}
          Every pizza has its required groups completed.
        {:else}
          Select crust, sauce, and cheese for every pizza to enable checkout.
        {/if}
      </small>
    </div>
  </section>

  <aside class="panel sidebar-card">
    <CartSummary pizzas={pizzas} amount={total} />

    <div class="summary-strip">
      <strong>Order JSON preview</strong>
      <small class="muted">The grouped contract keeps each pizza distinct inside one order payload.</small>
      <pre class="code-block">{orderPreview}</pre>
    </div>
  </aside>
</div>
