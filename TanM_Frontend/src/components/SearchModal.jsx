import React from 'react';

export default function SearchModal({ isOpen, onClose }) {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-surface max-w-lg w-full p-8 border border-outline-variant/20 rounded shadow-2xl relative">
        <button onClick={onClose} className="absolute top-4 right-4 text-on-surface-variant hover:text-primary">
          <span className="material-symbols-outlined">close</span>
        </button>
        <h2 className="font-headline-md text-xl mb-4 text-primary font-bold">Search Catalog</h2>
        <input type="text" className="w-full bg-surface-container border border-outline-variant/30 p-3 rounded" placeholder="Search..." />
      </div>
    </div>
  );
}
