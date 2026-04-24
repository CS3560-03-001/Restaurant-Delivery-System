<script lang="ts">
  import type { PizzaSelection } from '$lib/contracts';
  import { menuLabels } from '$lib/menu';

  export let pizza: PizzaSelection;
  export let amount = 0;

  const groups = ['crust', 'sauce', 'cheese', 'toppings'] as const;

  function displayItems(group: (typeof groups)[number]) {
    if (group === 'toppings') {
      return pizza.toppings.length ? pizza.toppings : ['None selected'];
    }

    return [pizza[group] || 'Not selected'];
  }
</script>

<section>
  <div class="hero">
    <h3>Current Pizza</h3>
    <p class="muted">Selections stay grouped by menu source so the order JSON stays predictable.</p>
  </div>

  {#each groups as group}
    <div class="cart-group">
      <h4>{menuLabels[group]}</h4>
      <ul>
        {#each displayItems(group) as item}
          <li>{item}</li>
        {/each}
      </ul>
    </div>
  {/each}

  <div class="cart-group">
    <h4>Estimated Total</h4>
    <p>${amount.toFixed(2)}</p>
  </div>
</section>
