import React from 'react';

export default function WishlistDrawer({ isOpen, onClose }) {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-y-0 right-0 max-w-sm w-full bg-surface border-l border-outline-variant/20 shadow-2xl z-50 p-8 flex flex-col justify-between">
      <div>
        <div className="flex justify-between items-center mb-8">
          <h2 className="font-headline-md text-xl text-primary font-bold">Saved Items</h2>
          <button onClick={onClose} className="text-on-surface hover:text-primary">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>
        <p className="text-sm text-on-surface-variant font-medium">Your wishlist is currently empty.</p>
      </div>
    </div>
  );
}
