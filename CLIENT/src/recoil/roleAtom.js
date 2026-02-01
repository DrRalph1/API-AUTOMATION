// recoil/roleAtom.js
import { atom } from 'recoil';

export const roleAtom = atom({
  key: 'roleAtom',
  default: null,
  effects: [
    ({ onSet }) => {
      onSet((newRole) => {
        console.log('🧩 RoleAtom updated:', newRole ?? 'Null/Cleared');
      });
    }
  ]
});
