<script lang="ts">
  import { goto } from '$app/navigation';
  import FormField from '$lib/components/FormField.svelte';
  import { createCustomer } from '$lib/api/client';
  import { flow } from '$lib/stores/flow';
  import type { CustomerCreateRequest } from '$lib/contracts';
  import { get } from 'svelte/store';

  let form: CustomerCreateRequest = { ...get(flow).accountDraft };
  let errors: Partial<Record<keyof CustomerCreateRequest, string>> = {};
  let submitError = '';
  let successMessage = '';
  let submitting = false;

  function validate() {
    errors = {
      name: form.name ? '' : 'Name is required.',
      email: form.email ? '' : 'Email is required.',
      phone: form.phone ? '' : 'Phone is required.',
      address: form.address ? '' : 'Delivery address is required.'
    };

    return !Object.values(errors).some(Boolean);
  }

  async function handleSubmit() {
    submitError = '';
    successMessage = '';
    flow.setAccountDraft(form);

    if (!validate()) {
      return;
    }

    submitting = true;

    try {
      const customer = await createCustomer(fetch, form);
      flow.setCustomer(customer, form);
      successMessage = `Account created as ${customer.customerId}.`;
      await goto('/order');
    } catch (error) {
      submitError = error instanceof Error ? error.message : 'Unable to create account.';
    } finally {
      submitting = false;
    }
  }

  $: requestPreview = JSON.stringify(form, null, 2);
</script>

<svelte:head>
  <title>Create Account | Restaurant Delivery Prototype</title>
</svelte:head>

<div class="layout-grid">
  <section class="panel content-card">
    <div class="hero">
      <h2>Create your delivery account</h2>
      <p>All four fields are required before the frontend will send the customer JSON contract.</p>
    </div>

    <FormField id="name" label="Full name" bind:value={form.name} error={errors.name} required />
    <FormField
      id="email"
      label="Email"
      type="email"
      bind:value={form.email}
      error={errors.email}
      required
    />
    <FormField id="phone" label="Phone" bind:value={form.phone} error={errors.phone} required />
    <FormField
      id="address"
      label="Delivery address"
      bind:value={form.address}
      error={errors.address}
      multiline={true}
      required
    />

    {#if submitError}
      <p class="error">{submitError}</p>
    {/if}

    {#if successMessage}
      <p class="success">{successMessage}</p>
    {/if}

    <div class="actions">
      <button class="primary" type="button" on:click={handleSubmit} disabled={submitting}>
        {submitting ? 'Submitting...' : 'Save account and continue'}
      </button>
    </div>
  </section>

  <aside class="panel sidebar-card">
    <div class="hero">
      <h3>Customer request preview</h3>
      <p class="muted">This is the JSON body the frontend sends to the mock customer endpoint.</p>
    </div>

    <pre class="code-block">{requestPreview}</pre>
  </aside>
</div>
