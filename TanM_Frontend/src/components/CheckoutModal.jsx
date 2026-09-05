import React, { useState, useEffect } from 'react';
import { addressApi, orderApi } from '../api';
import { useToast } from '../context/ToastContext';

export default function CheckoutModal({
  checkoutData,
  onClose,
  onOrderSuccess,
  user,
  onOpenLogin
}) {
  const toast = useToast();
  const [addresses, setAddresses] = useState([]);
  const [shippingAddressId, setShippingAddressId] = useState('');
  const [billingAddressId, setBillingAddressId] = useState('');

  // Address creation form (fallback if no address exists)
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [newAddress, setNewAddress] = useState({
    fullName: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    phoneNumber: '',
    addressType: 'SHIPPING'
  });

  const [placing, setPlacing] = useState(false);
  const [loadingAddresses, setLoadingAddresses] = useState(false);

  useEffect(() => {
    if (checkoutData) {
      loadAddresses();
    }
  }, [checkoutData, user]);

  const loadAddresses = async () => {
    setLoadingAddresses(true);
    try {
      const list = await addressApi.getAll();
      setAddresses(list);
      if (list.length > 0) {
        setShippingAddressId(list[0].id.toString());
        setBillingAddressId(list[0].id.toString());
      } else {
        setShowAddressForm(true);
      }
    } catch (err) {
      console.error('Failed to load address book', err);
      setShowAddressForm(true);
    } finally {
      setLoadingAddresses(false);
    }
  };

  const handleCreateAddress = async (e) => {
    e.preventDefault();
    try {
      const created = await addressApi.create(newAddress);
      toast.success('Address saved to your profile!');
      setShowAddressForm(false);
      
      const list = await addressApi.getAll();
      setAddresses(list);
      
      const newIdStr = created.id.toString();
      setShippingAddressId(newIdStr);
      setBillingAddressId(newIdStr);
    } catch (err) {
      toast.error(err.message || 'Failed to create address');
    }
  };

  const handlePlaceOrder = async () => {
    if (!user) {
      toast.warning('Please sign in or create an account to complete your order.');
      if (onOpenLogin) onOpenLogin();
      return;
    }
    if (!shippingAddressId) {
      toast.warning('Please select or add a shipping address.');
      return;
    }
    const finalBillingId = billingAddressId || shippingAddressId;
    
    setPlacing(true);
    try {
      const order = await orderApi.create(
        parseInt(shippingAddressId, 10),
        parseInt(finalBillingId, 10),
        'RAZORPAY',
        checkoutData?.couponCode
      );

      toast.info(`Order TNM-${order.orderNumber} created! Simulating payment gateway...`);
      
      await orderApi.pay(order.orderNumber);
      
      toast.success('Payment authorized! Your bespoke leather goods are queued in the workshop.');
      onOrderSuccess();
    } catch (err) {
      toast.error(err.message || 'Failed to complete order checkout');
    } finally {
      setPlacing(false);
    }
  };

  if (!checkoutData) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-md z-[100] flex justify-center items-center p-4 overflow-y-auto">
      <div className="bg-surface w-full max-w-2xl rounded-lg shadow-2xl overflow-y-auto max-h-[90vh] border border-outline-variant/20 relative p-8 md:p-12 text-xs text-left">
        
        {/* Guest Notice Banner */}
        {!user && (
          <div className="bg-primary/10 border border-primary/30 p-4 rounded mb-6 flex items-center justify-between">
            <div>
              <h4 className="font-bold text-primary text-xs uppercase tracking-wider">Account Required for Checkout</h4>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                You can add your address now, but you must sign in or create an account to place your order.
              </p>
            </div>
            <button
              onClick={onOpenLogin}
              className="bg-primary text-surface px-3.5 py-2 font-bold text-[11px] uppercase tracking-widest rounded hover:bg-primary-container shrink-0 ml-4 shadow-sm"
            >
              Sign In / Sign Up
            </button>
          </div>
        )}
        
        {/* Close */}
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-on-surface hover:text-primary transition-colors"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>

        <h2 className="font-display-lg text-2xl md:text-3xl text-primary font-bold mb-6">Secure Checkout</h2>

        <div className="space-y-8 font-medium">
          
          {/* Order Details Header */}
          <div className="bg-surface-container p-4 rounded border border-outline-variant/10 flex justify-between font-bold">
            <div>
              <p className="text-[10px] text-outline uppercase tracking-wider">Estimated Total</p>
              <p className="text-lg text-primary mt-1">₹{checkoutData.grandTotal.toFixed(2)}</p>
            </div>
            {checkoutData.discount > 0 && (
              <div className="text-right">
                <p className="text-[10px] text-outline uppercase tracking-wider">Coupon Discount</p>
                <p className="text-lg text-secondary mt-1">-₹{checkoutData.discount.toFixed(2)}</p>
              </div>
            )}
          </div>

          {/* Address selectors */}
          {!showAddressForm && (
            <div className="space-y-4">
              {loadingAddresses ? (
                <p className="text-center text-on-surface-variant">Loading address profiles...</p>
              ) : (
                <>
                  <div className="space-y-2">
                    <label className="block uppercase text-[10px] text-outline font-bold">Shipping Address</label>
                    <select
                      value={shippingAddressId}
                      onChange={(e) => setShippingAddressId(e.target.value)}
                      className="w-full bg-surface border border-outline-variant/30 rounded p-2.5 text-on-surface focus:ring-1 focus:ring-primary"
                    >
                      <option value="">-- Choose Shipping Address --</option>
                      {addresses.map((addr) => (
                        <option key={addr.id} value={addr.id}>
                          {addr.fullName} - {addr.addressLine1}, {addr.city} ({addr.postalCode})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-2">
                    <label className="block uppercase text-[10px] text-outline font-bold">Billing Address</label>
                    <select
                      value={billingAddressId}
                      onChange={(e) => setBillingAddressId(e.target.value)}
                      className="w-full bg-surface border border-outline-variant/30 rounded p-2.5 text-on-surface focus:ring-1 focus:ring-primary"
                    >
                      <option value="">-- Same as Shipping Address --</option>
                      {addresses.map((addr) => (
                        <option key={addr.id} value={addr.id}>
                          {addr.fullName} - {addr.addressLine1}, {addr.city} ({addr.postalCode})
                        </option>
                      ))}
                    </select>
                  </div>

                  <button
                    onClick={() => setShowAddressForm(true)}
                    className="text-xs text-primary font-bold hover:underline flex items-center gap-1"
                  >
                    <span className="material-symbols-outlined text-sm">add_location</span>
                    Add a new shipping address
                  </button>
                </>
              )}
            </div>
          )}

          {/* Address creation form */}
          {showAddressForm && (
            <form onSubmit={handleCreateAddress} className="space-y-4 bg-surface-container-low p-6 rounded border border-outline-variant/10">
              <div className="flex justify-between items-center mb-2">
                <h3 className="font-bold text-sm text-primary">New Shipping Address</h3>
                {addresses.length > 0 && (
                  <button
                    type="button"
                    onClick={() => setShowAddressForm(false)}
                    className="text-[10px] text-on-surface-variant hover:text-primary uppercase tracking-wider"
                  >
                    Use Existing Address
                  </button>
                )}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] text-outline font-bold mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    value={newAddress.fullName}
                    onChange={(e) => setNewAddress({ ...newAddress, fullName: e.target.value })}
                    className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-[10px] text-outline font-bold mb-1">Phone Number</label>
                  <input
                    type="text"
                    required
                    value={newAddress.phoneNumber}
                    onChange={(e) => setNewAddress({ ...newAddress, phoneNumber: e.target.value })}
                    className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">Address Line 1</label>
                <input
                  type="text"
                  required
                  value={newAddress.addressLine1}
                  onChange={(e) => setNewAddress({ ...newAddress, addressLine1: e.target.value })}
                  placeholder="Street address, P.O. box, company name"
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-[10px] text-outline font-bold mb-1">City</label>
                  <input
                    type="text"
                    required
                    value={newAddress.city}
                    onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                    className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-[10px] text-outline font-bold mb-1">State</label>
                  <input
                    type="text"
                    required
                    value={newAddress.state}
                    onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                    className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-[10px] text-outline font-bold mb-1">Postal Code</label>
                  <input
                    type="text"
                    required
                    value={newAddress.postalCode}
                    onChange={(e) => setNewAddress({ ...newAddress, postalCode: e.target.value })}
                    className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full bg-secondary text-surface py-2 rounded font-bold uppercase tracking-wider"
              >
                Save Address &amp; Select
              </button>
            </form>
          )}

          {/* Secure Place Order CTA */}
          <div className="pt-6 border-t border-outline-variant/20">
            <button
              onClick={handlePlaceOrder}
              disabled={placing || showAddressForm}
              className="w-full bg-primary text-surface py-4 rounded font-bold uppercase tracking-widest text-xs hover:bg-primary-container disabled:bg-surface-dim transition-all duration-300"
            >
              {placing ? 'Authorizing Payment Gateway...' : 'Authorize Simulated Payment (Razorpay)'}
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}
