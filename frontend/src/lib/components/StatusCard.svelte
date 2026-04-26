<script lang="ts">
  import type { OrderStatusResponse } from '$lib/contracts';

  export let status: OrderStatusResponse | null;
  export let pollingLabel = 'Polling every 5 seconds';
</script>

<section class="status-card">
  <div class="actions">
    <span class="status-pill">{status?.status ?? 'Waiting for status'}</span>
    <small class="muted">{pollingLabel}</small>
  </div>

  <h3>Delivery Status</h3>

  {#if status}
    <dl>
      <dt>ETA</dt>
      <dd>{status.etaLabel}</dd>
      <dt>Updated</dt>
      <dd>{new Date(status.updatedAt).toLocaleTimeString()}</dd>
      
      {#if status.cook}
        <dt>Cook</dt>
        <dd>{status.cook}</dd>
      {/if}

      {#if status.driver}
        <dt>Driver</dt>
        <dd>{status.driver.name}</dd>
        <dt>Phone</dt>
        <dd>{status.driver.phone}</dd>
        <dt>Vehicle</dt>
        <dd>{status.driver.vehicle}</dd>
      {:else}
        <dt>Driver</dt>
        <dd>Assignment pending</dd>
      {/if}
    </dl>
  {:else}
    <p class="muted">Place and pay for an order to start polling for delivery updates.</p>
  {/if}
</section>
