import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Footer from './components/Footer';
import Home from './components/Home';
import Shop from './components/Shop';
import CartPage from './components/CartPage';
import UserProfile from './components/UserProfile';
import AdminPortal from './components/AdminPortal';
import LoginModal from './components/LoginModal';
import SearchModal from './components/SearchModal';
import WishlistDrawer from './components/WishlistDrawer';
import ProductDetailsModal from './components/ProductDetailsModal';
import CheckoutModal from './components/CheckoutModal';
import { authApi, cartApi, wishlistApi } from './api';
import { useToast } from './context/ToastContext';

function App() {
  const toast = useToast();
  const [currentTab, setCurrentTab] = useState('home');

  const [user, setUser] = useState(null);
  const [cart, setCart] = useState(null);
  const [wishlist, setWishlist] = useState(null);

  // Modals & Overlay Toggles
  const [loginOpen, setLoginOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [wishlistOpen, setWishlistOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [checkoutData, setCheckoutData] = useState(null);

  // On Startup: load user session & cart state
  useEffect(() => {
    loadSession();
    loadCart();
  }, []);

  // Sync cart & wishlist when user session updates
  useEffect(() => {
    if (user) {
      loadCart();
      loadWishlist();
    } else {
      setWishlist(null);
    }
  }, [user]);

  const loadSession = async () => {
    const token = localStorage.getItem('token');
    if (!token) return;
    try {
      const data = await authApi.getMe();
      setUser(data);
    } catch (err) {
      console.error('Session verification expired or failed', err);
      localStorage.removeItem('token');
    }
  };

  const loadCart = async () => {
    try {
      const data = await cartApi.get();
      setCart(data);
    } catch (err) {
      console.error('Failed to load cart state', err);
    }
  };

  const loadWishlist = async () => {
    try {
      const data = await wishlistApi.get();
      setWishlist(data);
    } catch (err) {
      console.error('Failed to load wishlist state', err);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setCart(null);
    setWishlist(null);
    setCurrentTab('home');
    toast.info('You have successfully signed out.');
  };

  const handleLoginSuccess = async (userData) => {
    setLoginOpen(false);

    // Always fetch the full profile — login response may only contain { token }
    try {
      const fullUser = await authApi.getMe();
      setUser(fullUser);
    } catch {
      setUser(userData); // fallback if getMe fails
    }

    // Merge guest cart with user cart on successful login
    const guestToken = localStorage.getItem('guest_token');
    if (guestToken) {
      try {
        await cartApi.merge(guestToken);
        localStorage.removeItem('guest_token');
      } catch (err) {
        console.error('Guest cart merge failed', err);
      }
    }
    loadCart();
  };

  const cartCount = cart?.items?.reduce((acc, curr) => acc + curr.quantity, 0) || 0;
  const wishlistCount = wishlist?.items?.length || 0;

  return (
    <div className="min-h-screen bg-surface flex flex-col justify-between">
      
      {/* Shell Header */}
      <Header
        cartCount={cartCount}
        wishlistCount={wishlistCount}
        onOpenWishlist={() => setWishlistOpen(true)}
        onOpenSearch={() => setSearchOpen(true)}
        onNavigate={setCurrentTab}
        currentTab={currentTab}
        user={user}
        onLogout={handleLogout}
        onOpenLogin={() => setLoginOpen(true)}
      />

      {/* Main Pages Canvas — non-home pages need top padding for fixed header */}
      <main className={`flex-grow${currentTab !== 'home' ? ' pt-24' : ''}`}>
        {currentTab === 'home' && (
          <Home
            onSelectProduct={setSelectedProduct}
            onNavigate={setCurrentTab}
          />
        )}
        {currentTab === 'shop' && (
          <Shop
            onSelectProduct={setSelectedProduct}
          />
        )}
        {currentTab === 'cart' && (
          <CartPage
            cart={cart}
            onUpdateCart={setCart}
            onOpenCheckout={setCheckoutData}
            onNavigate={setCurrentTab}
          />
        )}
        {currentTab === 'profile' && (
          <UserProfile
            user={user}
            onUpdateUser={setUser}
          />
        )}
        {currentTab === 'admin' && (
          <AdminPortal />
        )}
      </main>

      {/* Shell Footer */}
      <Footer />

      {/* Modals & Dialog Drawers */}
      <SearchModal
        isOpen={searchOpen}
        onClose={() => setSearchOpen(false)}
      />

      <WishlistDrawer
        isOpen={wishlistOpen}
        onClose={() => setWishlistOpen(false)}
        wishlist={wishlist}
        onUpdateWishlist={setWishlist}
        onUpdateCart={setCart}
      />

      <ProductDetailsModal
        product={selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onNavigate={setCurrentTab}
        user={user}
        onAddToCart={async (p, qty = 1) => {
          try {
            const updated = await cartApi.addItem(p.id, qty);
            setCart(updated);
            toast.success(`${p.name} ${qty > 1 ? `(${qty} items)` : ''} added to shopping bag.`);
            return true;
          } catch (err) {
            toast.error(err.message || 'Failed to add item to bag');
            return false;
          }
        }}
      />

      <CheckoutModal
        checkoutData={checkoutData}
        onClose={() => setCheckoutData(null)}
        onOrderSuccess={() => {
          setCheckoutData(null);
          setCart(null);
          setCurrentTab('profile');
        }}
        user={user}
        onOpenLogin={() => setLoginOpen(true)}
      />

      <LoginModal
        isOpen={loginOpen}
        onClose={() => setLoginOpen(false)}
        onLoginSuccess={handleLoginSuccess}
      />

    </div>
  );
}

export default App;
