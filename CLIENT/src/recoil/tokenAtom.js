// recoil/tokenAtom.js
import { atom } from 'recoil';

export const tokenAtom = atom({
  key: 'tokenAtom',
  default: null,
  effects: [
    ({ onSet }) => {
      onSet((newToken) => {
        console.log('🔐 TokenAtom updated:', newToken ? 'Present' : 'Null/Cleared');
      });
    }
  ]
});