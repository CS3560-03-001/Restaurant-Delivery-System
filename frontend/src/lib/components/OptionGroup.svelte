<script lang="ts">
  import type { MenuOption } from '$lib/menu';

  export let groupLabel: string;
  export let groupName = '';
  export let description: string;
  export let options: MenuOption[] = [];
  export let multiple = false;
  export let selectedValue = '';
  export let selectedValues: string[] = [];

  $: resolvedGroupName = groupName || groupLabel;

  function toggleValue(value: string, checked: boolean) {
    if (!multiple) {
      selectedValue = value;
      return;
    }

    selectedValues = checked
      ? [...selectedValues, value]
      : selectedValues.filter((entry) => entry !== value);
  }
</script>

<section class="group-list">
  <div class="group-header">
    <h3>{groupLabel}</h3>
    <p class="muted">{description}</p>
  </div>

  <div class="option-grid">
    {#each options as option}
      <label class="option-card" for={`${resolvedGroupName}-${option.value}`}>
        <input
          id={`${resolvedGroupName}-${option.value}`}
          type={multiple ? 'checkbox' : 'radio'}
          name={resolvedGroupName}
          checked={multiple ? selectedValues.includes(option.value) : selectedValue === option.value}
          on:change={(event) => toggleValue(option.value, (event.currentTarget as HTMLInputElement).checked)}
        />

        <span>
          <strong>{option.label}</strong>
          <small class="muted">{option.description}</small>
        </span>
      </label>
    {/each}
  </div>
</section>
