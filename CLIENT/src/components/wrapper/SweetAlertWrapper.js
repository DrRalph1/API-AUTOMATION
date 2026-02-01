// components/wrapper/SweetAlertWrapper.jsx
import React from 'react';
import { DialogAlertProvider } from '@/lib/sweetAlert';

export default function SweetAlertWrapper({ children }) {
  return (
    <DialogAlertProvider>
      {children}
    </DialogAlertProvider>
  );
}