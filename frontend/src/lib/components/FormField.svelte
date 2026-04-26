<script lang="ts">
  import type { HTMLInputAttributes, HTMLTextareaAttributes } from 'svelte/elements';

  export let id: string;
  export let label: string;
  export let value = '';
  export let type = 'text';
  export let placeholder = '';
  export let required = false;
  export let rows = 4;
  export let help = '';
  export let error = '';
  export let multiline = false;
  export let autocomplete: HTMLInputAttributes['autocomplete'] | HTMLTextareaAttributes['autocomplete'] = undefined;
  export let inputmode: HTMLInputAttributes['inputmode'] = undefined;
  export let maxlength: number | undefined = undefined;
</script>

<div class="field">
  <label for={id}>{label}</label>

  {#if multiline}
    <textarea
      id={id}
      bind:value
      {placeholder}
      {required}
      {rows}
      {autocomplete}
      aria-invalid={Boolean(error)}
    ></textarea>
  {:else}
    <input
      id={id}
      bind:value
      {type}
      {placeholder}
      {required}
      {autocomplete}
      {inputmode}
      {maxlength}
      aria-invalid={Boolean(error)}
    />
  {/if}

  {#if error}
    <small class="error">{error}</small>
  {:else if help}
    <small>{help}</small>
  {/if}
</div>
