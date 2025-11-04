import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import '../styles/globals.css';

const rootEl = document.getElementById('root');
if (rootEl) {
  const root = createRoot(rootEl);
  root.render(<App />);
}