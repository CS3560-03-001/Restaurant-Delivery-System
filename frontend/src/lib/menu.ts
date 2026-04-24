import type { MenuGroup } from '$lib/contracts';

export interface MenuOption {
  value: string;
  label: string;
  description: string;
  price: number;
}

export const menuOptions: Record<MenuGroup, MenuOption[]> = {
  crust: [
    { value: 'thin-crust', label: 'Thin Crust', description: 'Crisp and light with a quick bake.', price: 8.5 },
    { value: 'hand-tossed', label: 'Hand Tossed', description: 'Balanced texture for a classic slice.', price: 9 },
    { value: 'pan', label: 'Pan Style', description: 'Golden and slightly thicker edge.', price: 9.5 }
  ],
  sauce: [
    { value: 'classic-red', label: 'Classic Red', description: 'Tomato-forward with oregano.', price: 1.5 },
    { value: 'garlic-white', label: 'Garlic White', description: 'Creamy garlic base for a richer bite.', price: 1.75 },
    { value: 'spicy-arrabbiata', label: 'Spicy Arrabbiata', description: 'Peppery heat for a sharper finish.', price: 1.75 }
  ],
  cheese: [
    { value: 'mozzarella', label: 'Mozzarella', description: 'Stretchy and familiar.', price: 2.25 },
    { value: 'smoked-provolone', label: 'Smoked Provolone', description: 'A deeper, savory note.', price: 2.5 },
    { value: 'vegan-blend', label: 'Vegan Blend', description: 'Dairy-free option with mild melt.', price: 2.75 }
  ],
  toppings: [
    { value: 'pepperoni', label: 'Pepperoni', description: 'Classic crisped slices.', price: 1.75 },
    { value: 'mushrooms', label: 'Mushrooms', description: 'Earthy and juicy.', price: 1.25 },
    { value: 'roasted-peppers', label: 'Roasted Peppers', description: 'Sweet red pepper strips.', price: 1.5 },
    { value: 'red-onion', label: 'Red Onion', description: 'Sharp bite and crunch.', price: 1 },
    { value: 'black-olives', label: 'Black Olives', description: 'Briny accent.', price: 1.25 },
    { value: 'basil', label: 'Basil', description: 'Fresh green finish.', price: 0.75 }
  ]
};

export const menuLabels: Record<MenuGroup, string> = {
  crust: 'Crust',
  sauce: 'Sauce',
  cheese: 'Cheese',
  toppings: 'Toppings'
};

const priceLookup = Object.values(menuOptions)
  .flat()
  .reduce<Record<string, number>>((lookup, option) => {
    lookup[option.value] = option.price;
    return lookup;
  }, {});

export function calculatePizzaAmount(pizza: { crust: string; sauce: string; cheese: string; toppings: string[] }) {
  return Number(
    (
      (priceLookup[pizza.crust] ?? 0) +
      (priceLookup[pizza.sauce] ?? 0) +
      (priceLookup[pizza.cheese] ?? 0) +
      pizza.toppings.reduce((sum, topping) => sum + (priceLookup[topping] ?? 0), 0)
    ).toFixed(2)
  );
}
