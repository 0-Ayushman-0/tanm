import React, { useState } from 'react';
import { cartApi, couponApi } from '../api';
import { useToast } from '../context/ToastContext';

export default function CartPage({
  cart,
  onUpdateCart,
  onOpenCheckout,
  onNavigate
}) {
  const toast = useToast();
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponError, setCouponError] = useState('');
  const [validating, setValidating] = useState(false);

  const handleQtyChange = async (itemId, currentQty, delta) => {
    const newQty = currentQty + delta;
    if (newQty <= 0) {
      handleRemoveItem(itemId);
      return;
    }
    try {
      const updated = await cartApi.updateItem(itemId, newQty);
      onUpdateCart(updated);
    } catch (err) {
      toast.error(err.message || 'Failed to update quantity');
    }
  };

  const handleRemoveItem = async (itemId) => {
    try {
      const updated = await cartApi.removeItem(itemId);
      onUpdateCart(updated);
      toast.info('Item removed from shopping bag');
    } catch (err) {
      toast.error(err.message || 'Failed to remove item');
    }
  };

  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) return;
    setValidating(true);
    setCouponError('');
    try {
      const result = await couponApi.validate(couponCode, cart.subtotal);
      setAppliedCoupon(result);
      setCouponError('');
      toast.success(`Coupon "${couponCode.toUpperCase()}" applied!`);
    } catch (err) {
      setCouponError(err.message || 'Invalid coupon code');
      setAppliedCoupon(null);
      toast.error(err.message || 'Invalid coupon code');
    } finally {
      setValidating(false);
    }
  };

  const handleRemoveCoupon = () => {
    setAppliedCoupon(null);
    setCouponCode('');
    toast.info('Coupon removed');
  };

  const subtotal = cart?.subtotal ?? cart?.totalPrice ?? 0;
  const shippingFee = cart?.shippingFee || 0;
  const taxFee = cart?.taxFee || 0;
  const discount = appliedCoupon ? appliedCoupon.discountAmount : 0;
  const grandTotal = Math.max(0, subtotal + shippingFee + taxFee - discount);

  return (
    <div className="max-w-container-max mx-auto px-edge-margin-mobile md:px-edge-margin-desktop py-12">
      {/* Header */}
      <header className="mb-16 text-left">
        <nav className="mb-4 text-xs font-semibold text-on-surface-variant/60 uppercase tracking-widest">
          <button
            onClick={() => onNavigate('shop')}
            className="hover:text-primary transition-colors"
          >
            Collections
          </button>
          <span className="mx-2">/</span>
          <span className="text-on-surface">Shopping Bag</span>
        </nav>
        <h1 className="font-headline-lg text-4xl md:text-5xl font-bold tracking-tight text-primary">
          Your Selection
        </h1>
      </header>

      {!cart || cart.items.length === 0 ? (
        <div className="text-center py-24 bg-surface-container rounded border border-dashed border-outline-variant/30 text-on-surface-variant font-medium">
          <span className="material-symbols-outlined text-[64px] mb-4 text-primary">shopping_bag</span>
          <h2 className="font-headline-md text-xl">Your shopping bag is empty</h2>
          <button
            onClick={() => onNavigate('shop')}
            className="mt-6 inline-block bg-primary text-surface px-8 py-3.5 font-label-sm text-xs uppercase tracking-widest hover:opacity-95 font-bold"
          >
            Browse Collections
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start text-left">
          
          {/* Item Lists */}
          <section className="lg:col-span-8">
            <div className="border-t border-outline-variant/30">
              {cart.items.map((item) => (
                <article key={item.id} className="flex py-10 border-b border-outline-variant/30 group">
                  <div className="w-32 h-40 md:w-48 md:h-64 flex-shrink-0 bg-surface-container overflow-hidden rounded shadow">
                    <img
                      src={item.primaryImageUrl || item.productImageUrl || 'https://via.placeholder.com/300x400'}
                      alt={item.productName}
                      className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                    />
                  </div>
                  <div className="ml-8 md:ml-12 flex flex-col justify-between flex-grow">
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="font-headline-md text-xl md:text-2xl leading-none mb-2 font-bold text-on-surface">
                          {item.productName}
                        </h3>
                        <p className="font-label-sm text-[10px] text-outline uppercase tracking-widest font-bold">
                          {item.productSku}
                        </p>
                      </div>
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        className="text-on-surface-variant hover:text-error transition-colors"
                      >
                        <span className="material-symbols-outlined text-[20px]">close</span>
                      </button>
                    </div>

                    <div className="flex justify-between items-end">
                      <div className="flex items-center space-x-6 border border-outline-variant/20 px-4 py-2 bg-surface">
                        <button
                          onClick={() => handleQtyChange(item.id, item.quantity, -1)}
                          className="text-on-surface-variant hover:text-on-surface transition-colors"
                        >
                          <span className="material-symbols-outlined text-[18px]">remove</span>
                        </button>
                        <span className="font-label-sm text-sm w-4 text-center font-bold">{item.quantity}</span>
                        <button
                          onClick={() => handleQtyChange(item.id, item.quantity, 1)}
                          className="text-on-surface-variant hover:text-on-surface transition-colors"
                        >
                          <span className="material-symbols-outlined text-[18px]">add</span>
                        </button>
                      </div>
                      <div className="text-right">
                        <p className="font-body-lg text-lg font-bold text-primary">
                          ₹{item.subtotal.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  </div>
                </article>
              ))}
            </div>
            
            <div className="mt-12 text-xs font-semibold">
              <button
                onClick={() => onNavigate('shop')}
                className="font-label-sm uppercase tracking-widest text-primary hover:opacity-70 transition-opacity flex items-center gap-2 group font-bold"
              >
                <span className="material-symbols-outlined text-[18px] group-hover:-translate-x-1 transition-transform">
                  arrow_back
                </span>
                Back to Atelier
              </button>
            </div>
          </section>

          {/* Sidebar calculations */}
          <aside className="lg:col-span-4 mt-16 lg:mt-0">
            <div className="bg-surface-container p-8 sticky top-32 border border-outline-variant/10 rounded shadow-sm">
              <h2 className="font-label-sm text-xs uppercase tracking-[0.2em] mb-8 border-b border-outline-variant/20 pb-4 text-primary font-bold">
                Order Summary
              </h2>

              <div className="space-y-4 mb-8 text-xs font-medium">
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Subtotal</span>
                  <span className="font-bold">₹{subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Shipping</span>
                  <span className="uppercase tracking-tighter text-secondary font-bold">Complimentary</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Estimated Tax</span>
                  <span className="font-bold">₹{taxFee.toFixed(2)}</span>
                </div>

                {/* Promo Code Validation */}
                <div className="pt-4 border-t border-outline-variant/20 space-y-2">
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={couponCode}
                      onChange={(e) => setCouponCode(e.target.value)}
                      placeholder="PROMO CODE"
                      disabled={!!appliedCoupon}
                      className="flex-1 bg-surface border border-outline-variant/30 rounded px-3 py-1.5 uppercase placeholder:normal-case focus:ring-1 focus:ring-primary focus:border-primary text-on-surface text-center font-bold"
                    />
                    {appliedCoupon ? (
                      <button
                        onClick={handleRemoveCoupon}
                        className="bg-error text-surface px-4 py-1.5 rounded font-bold uppercase tracking-widest"
                      >
                        Remove
                      </button>
                    ) : (
                      <button
                        onClick={handleApplyCoupon}
                        disabled={validating}
                        className="bg-primary text-surface px-4 py-1.5 rounded font-bold uppercase tracking-widest"
                      >
                        Apply
                      </button>
                    )}
                  </div>
                  {couponError && <p className="text-[10px] text-error font-semibold">{couponError}</p>}
                  {appliedCoupon && (
                    <div className="flex justify-between text-secondary font-bold">
                      <span>Discount ({appliedCoupon.couponCode})</span>
                      <span>-₹{discount.toFixed(2)}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="border-t border-outline-variant/30 pt-6 mb-8 text-xs font-bold">
                <div className="flex justify-between items-end">
                  <span className="uppercase tracking-widest text-on-surface font-bold">Total</span>
                  <span className="font-headline-md text-xl text-primary leading-none font-bold">
                    ₹{grandTotal.toFixed(2)}
                  </span>
                </div>
              </div>

              <button
                onClick={() =>
                  onOpenCheckout({
                    couponCode: appliedCoupon ? appliedCoupon.couponCode : null,
                    discount: discount,
                    grandTotal: grandTotal
                  })
                }
                className="w-full bg-primary text-surface py-5 font-label-sm text-xs uppercase tracking-[0.2em] hover:bg-primary-container transition-all duration-300 active:scale-[0.98] font-bold"
              >
                Proceed to Checkout
              </button>

              <div className="mt-8 pt-8 border-t border-outline-variant/20 space-y-4">
                <div className="flex items-center gap-3 text-on-surface-variant">
                  <span className="material-symbols-outlined text-[20px]">verified</span>
                  <span className="font-label-sm text-[9px] uppercase tracking-widest font-bold">
                    LIFETIME CRAFTSMANSHIP GUARANTEE
                  </span>
                </div>
                <div className="flex items-center gap-3 text-on-surface-variant">
                  <span className="material-symbols-outlined text-[20px]">lock</span>
                  <span className="font-label-sm text-[9px] uppercase tracking-widest font-bold">
                    SECURE BESPOKE CHECKOUT
                  </span>
                </div>
              </div>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}
