// recoil/userAtom.js
import { atom } from 'recoil';
import { loadState, saveState } from '../utils/recoilPersist';

// Load previous user state from storage
const initialUserState = loadState('userState') || {};

export const userAtom = atom({
  key: 'userAtom',
  default: initialUserState,
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet(newValue => {
        if (newValue) {
          const { password, ...sanitizedState } = newValue;
          saveState('userState', sanitizedState);
        } else {
          saveState('userState', {});
        }
      });
    },
  ],
});
