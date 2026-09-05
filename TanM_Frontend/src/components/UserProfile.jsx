import React, { useState, useEffect } from 'react';
import { authApi, orderApi } from '../api';

export default function UserProfile({ user, onUpdateUser }) {
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);

  // Profile update form
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phoneNumber: user?.phoneNumber || ''
  });
  const [updatingProfile, setUpdatingProfile] = useState(false);

  // Password change form
  const [passForm, setPassForm] = useState({
    currentPassword: '',
    newPassword: ''
  });
  const [updatingPass, setUpdatingPass] = useState(false);

  // Order Details Modal (Tracking Timeline)
  const [selectedOrderDetails, setSelectedOrderDetails] = useState(null);
  const [loadingTimeline, setLoadingTimeline] = useState(false);

  useEffect(() => {
    if (user) {
      loadOrderHistory();
    }
  }, [user]);

  const loadOrderHistory = async () => {
    setLoadingOrders(true);
    try {
      const data = await orderApi.getMyOrders(0, 50);
      setOrders(data.content || []);
    } catch (err) {
      console.error('Failed to load orders history', err);
    } finally {
      setLoadingOrders(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setUpdatingProfile(true);
    try {
      const updated = await authApi.updateProfile(profileForm);
      onUpdateUser(updated);
      alert('Profile details updated successfully!');
    } catch (err) {
      alert(err.message || 'Failed to update profile');
    } finally {
      setUpdatingProfile(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setUpdatingPass(true);
    try {
      await authApi.changePassword(passForm.currentPassword, passForm.newPassword);
      setPassForm({ currentPassword: '', newPassword: '' });
      alert('Password updated successfully!');
    } catch (err) {
      alert(err.message || 'Failed to change password');
    } finally {
      setUpdatingPass(false);
    }
  };

  const handleInspectOrder = async (orderNumber) => {
    setLoadingTimeline(true);
    try {
      const details = await orderApi.getDetails(orderNumber);
      setSelectedOrderDetails(details);
    } catch (err) {
      alert(err.message || 'Failed to fetch tracking details');
    } finally {
      setLoadingTimeline(false);
    }
  };

  return (
    <div className="max-w-container-max mx-auto px-edge-margin-mobile md:px-edge-margin-desktop py-12 text-left">
      <header className="mb-16">
        <h1 className="font-display-lg text-4xl md:text-5xl font-bold tracking-tight text-primary">
          Customer Suite
        </h1>
        <p className="font-body-lg text-sm text-on-surface-variant opacity-70 mt-2">
          Review purchase archives, modify security credentials, and trace live crafting progress.
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start text-xs">

        {/* Left Column: Profile Settings (4 cols) */}
        <section className="lg:col-span-4 space-y-8 font-medium">

          {/* Profile details form */}
          <div className="bg-surface-container p-6 rounded border border-outline-variant/15 space-y-4">
            <h2 className="font-headline-md text-base text-primary font-bold">Personal Particulars</h2>
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">First Name</label>
                <input
                  type="text"
                  required
                  value={profileForm.firstName}
                  onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">Last Name</label>
                <input
                  type="text"
                  required
                  value={profileForm.lastName}
                  onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })}
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">Phone Number</label>
                <input
                  type="text"
                  value={profileForm.phoneNumber}
                  onChange={(e) => setProfileForm({ ...profileForm, phoneNumber: e.target.value })}
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>
              <button
                type="submit"
                disabled={updatingProfile}
                className="w-full bg-primary text-surface py-2.5 rounded font-bold uppercase tracking-wider"
              >
                {updatingProfile ? 'Saving...' : 'Update Details'}
              </button>
            </form>
          </div>

          {/* Change password form */}
          <div className="bg-surface-container p-6 rounded border border-outline-variant/15 space-y-4">
            <h2 className="font-headline-md text-base text-primary font-bold">Access Credentials</h2>
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">Current Password</label>
                <input
                  type="password"
                  required
                  value={passForm.currentPassword}
                  onChange={(e) => setPassForm({ ...passForm, currentPassword: e.target.value })}
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-[10px] text-outline font-bold mb-1">New Password</label>
                <input
                  type="password"
                  required
                  value={passForm.newPassword}
                  onChange={(e) => setPassForm({ ...passForm, newPassword: e.target.value })}
                  className="w-full bg-surface border border-outline-variant/30 rounded p-2 focus:ring-1 focus:ring-primary"
                />
              </div>
              <button
                type="submit"
                disabled={updatingPass}
                className="w-full bg-secondary text-surface py-2.5 rounded font-bold uppercase tracking-wider"
              >
                {updatingPass ? 'Modifying...' : 'Change Password'}
              </button>
            </form>
          </div>

        </section>

        {/* Right Column: Order History & Tracking Timelines (8 cols) */}
        <section className="lg:col-span-8 bg-surface-container p-8 rounded border border-outline-variant/15 space-y-6">
          <h2 className="font-headline-md text-lg text-primary font-bold">Purchase Archive</h2>

          {loadingOrders ? (
            <p className="text-center py-12 text-on-surface-variant font-semibold">Loading archives...</p>
          ) : orders.length === 0 ? (
            <div className="text-center py-12 text-on-surface-variant font-semibold">
              <span className="material-symbols-outlined text-[48px] text-primary mb-2 block">history</span>
              No purchases recorded on your profile.
            </div>
          ) : (
            <div className="space-y-4">
              {orders.map((o) => (
                <div
                  key={o.id}
                  className="p-6 bg-surface rounded border border-outline-variant/15 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 hover:border-primary transition-colors"
                >
                  <div className="space-y-1">
                    <span className="font-label-sm text-primary font-bold text-sm">
                      #{o.orderNumber}
                    </span>
                    <span className="text-[10px] text-on-surface-variant block">
                      Placed: {o.orderedAt || o.createdAt ? new Date(o.orderedAt || o.createdAt).toLocaleDateString() : 'N/A'}
                    </span>
                    <div className="flex gap-2 pt-2">
                      <span className="px-2 py-0.5 border border-outline-variant/30 bg-surface-container rounded text-[9px] uppercase tracking-wider font-bold">
                        {o.fulfillmentStatus}
                      </span>
                      <span className="px-2 py-0.5 border border-outline-variant/30 bg-surface-container rounded text-[9px] uppercase tracking-wider font-bold text-secondary">
                        {o.paymentStatus}
                      </span>
                    </div>
                  </div>
                  <div className="text-right flex flex-col items-end justify-center w-full md:w-auto">
                    <span className="font-bold text-base text-primary mb-2">₹{o.grandTotal.toFixed(2)}</span>
                    <button
                      onClick={() => handleInspectOrder(o.orderNumber)}
                      className="bg-primary text-surface px-4 py-1.5 rounded uppercase tracking-wider font-bold text-[9px] hover:opacity-95"
                    >
                      Trace Stitching Progress
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

      </div>

      {/* Order Trace / Timeline Modal */}
      {selectedOrderDetails && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-md z-[100] flex justify-center items-center p-4">
          <div className="bg-surface w-full max-w-lg rounded-lg shadow-2xl p-8 border border-outline-variant/20 relative max-h-[85vh] overflow-y-auto text-xs">

            <button
              onClick={() => setSelectedOrderDetails(null)}
              className="absolute top-6 right-6 text-on-surface hover:text-primary transition-colors"
            >
              <span className="material-symbols-outlined text-[20px]">close</span>
            </button>

            <h3 className="font-headline-md text-xl text-primary font-bold mb-1">
              Trace Stitching #{selectedOrderDetails.orderNumber}
            </h3>
            <p className="text-[10px] text-on-surface-variant uppercase tracking-wider font-semibold border-b border-outline-variant/20 pb-4 mb-6">
              Status: {selectedOrderDetails.fulfillmentStatus} | Carrier: {selectedOrderDetails.carrier || 'Atelier Courier'}
            </p>

            {/* Timeline Tree */}
            <div className="relative border-l border-primary/20 ml-4 pl-8 space-y-6">
              {selectedOrderDetails.timeline && selectedOrderDetails.timeline.length > 0 ? (
                selectedOrderDetails.timeline.map((entry, idx) => (
                  <div key={entry.id || idx} className="relative">
                    <span className="absolute -left-[41px] top-0 bg-primary text-surface w-6 h-6 rounded-full flex items-center justify-center font-bold text-[10px] shadow">
                      {selectedOrderDetails.timeline.length - idx}
                    </span>
                    <div>
                      <p className="font-bold text-on-surface text-sm">{entry.newFulfillmentStatus || entry.newPaymentStatus || entry.status || 'Status Updated'}</p>
                      <p className="text-[10px] text-on-surface-variant mt-0.5">{entry.remarks || entry.description || 'Order milestone reached.'}</p>
                      <span className="text-[9px] text-primary block mt-1">
                        {entry.timestamp || entry.createdAt ? new Date(entry.timestamp || entry.createdAt).toLocaleString() : 'N/A'}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <div className="relative">
                  <span className="absolute -left-[41px] top-0 bg-primary text-surface w-6 h-6 rounded-full flex items-center justify-center font-bold text-[10px] shadow">
                    1
                  </span>
                  <div>
                    <p className="font-bold text-on-surface text-sm">Order Placed</p>
                    <p className="text-[10px] text-on-surface-variant mt-0.5">Order confirmed & ateliers notified.</p>
                    <span className="text-[9px] text-primary block mt-1">
                      {selectedOrderDetails.orderedAt || selectedOrderDetails.createdAt ? new Date(selectedOrderDetails.orderedAt || selectedOrderDetails.createdAt).toLocaleString() : 'N/A'}
                    </span>
                  </div>
                </div>
              )}
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
