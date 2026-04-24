<script lang="ts">
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
      aria-invalid={Boolean(error)}
    ></textarea>
  {:else}
    <input id={id} bind:value {type} {placeholder} {required} aria-invalid={Boolean(error)} />
  {/if}

  {#if error}
    <small class="error">{error}</small>
  {:else if help}
    <small>{help}</small>
  {/if}
</div>
