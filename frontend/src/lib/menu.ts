import type { MenuGroup } from '$lib/contracts';

export interface MenuOption {
  value: string;
  label: string;
  description: string;
}

export const menuOptions: Record<MenuGroup, MenuOption[]> = {
  crust: [
    { value: 'thin-crust', label: 'Thin Crust', description: 'Crisp and light with a quick bake.' },
    { value: 'hand-tossed', label: 'Hand Tossed', description: 'Balanced texture for a classic slice.' },
    { value: 'pan', label: 'Pan Style', description: 'Golden and slightly thicker edge.' }
  ],
  sauce: [
    { value: 'classic-red', label: 'Classic Red', description: 'Tomato-forward with oregano.' },
    { value: 'garlic-white', label: 'Garlic White', description: 'Creamy garlic base for a richer bite.' },
    { value: 'spicy-arrabbiata', label: 'Spicy Arrabbiata', description: 'Peppery heat for a sharper finish.' }
  ],
  cheese: [
    { value: 'mozzarella', label: 'Mozzarella', description: 'Stretchy and familiar.' },
    { value: 'smoked-provolone', label: 'Smoked Provolone', description: 'A deeper, savory note.' },
    { value: 'vegan-blend', label: 'Vegan Blend', description: 'Dairy-free option with mild melt.' }
  ],
  toppings: [
    { value: 'pepperoni', label: 'Pepperoni', description: 'Classic crisped slices.' },
    { value: 'mushrooms', label: 'Mushrooms', description: 'Earthy and juicy.' },
    { value: 'roasted-peppers', label: 'Roasted Peppers', description: 'Sweet red pepper strips.' },
    { value: 'red-onion', label: 'Red Onion', description: 'Sharp bite and crunch.' },
    { value: 'black-olives', label: 'Black Olives', description: 'Briny accent.' },
    { value: 'basil', label: 'Basil', description: 'Fresh green finish.' }
  ]
};

export const menuLabels: Record<MenuGroup, string> = {
  crust: 'Crust',
  sauce: 'Sauce',
  cheese: 'Cheese',
  toppings: 'Toppings'
};
