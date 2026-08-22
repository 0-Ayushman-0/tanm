import React, { useState, useEffect, useRef } from 'react';

export default function Header({
  cartCount,
  wishlistCount,
  onOpenWishlist,
  onOpenSearch,
  onNavigate,
  currentTab,
  user,
  onLogout,
  onOpenLogin,
}) {
  const [scrolled, setScrolled]         = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen]     = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 50);
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setUserDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const isHome = currentTab === 'home';

  return (
    <header
      className={`fixed w-full z-50 transition-all duration-500 ease-in-out border-b border-primary/5
        ${scrolled ? 'top-0 shadow-sm bg-background/95 backdrop-blur-md' : isHome ? 'top-10 bg-background/80 backdrop-blur-md' : 'top-0 bg-background/95 backdrop-blur-md'}
      `}
    >
      {/* Announcement Bar — only shown un-scrolled on home */}
      {!scrolled && isHome && (
        <div className="bg-primary-container text-on-primary text-center py-2.5 text-xs font-semibold uppercase tracking-widest">
          Free Shipping on Orders Over ₹5,000 &nbsp;·&nbsp; Use Code <span className="font-bold">TANM10</span>
        </div>
      )}

      <nav className="flex justify-between items-center h-20 px-margin-desktop max-w-container-max mx-auto">

        {/* Left Nav */}
        <div className="flex-1 hidden md:flex gap-stack-lg items-center">
          <button
            onClick={() => onNavigate('shop')}
            className={`text-xs font-semibold uppercase tracking-widest transition-colors
              ${currentTab === 'shop' ? 'text-primary border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'}`}
          >
            Collections
          </button>
          <a href="#craft" className="text-xs font-semibold uppercase tracking-widest text-on-surface-variant hover:text-primary transition-colors">
            Craftsmanship
          </a>
        </div>

        {/* Logo — center */}
        <button
          onClick={() => onNavigate('home')}
          className="flex-none font-display-lg text-2xl tracking-tighter text-primary hover:opacity-80 transition-opacity font-semibold"
        >
          TanM
        </button>

        {/* Right Actions */}
        <div className="flex-1 hidden md:flex justify-end items-center gap-5">
          <button
            onClick={onOpenSearch}
            className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors text-[20px]"
            title="Search"
          >
            search
          </button>

          <button
            onClick={onOpenWishlist}
            className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors relative text-[20px]"
            title="Wishlist"
          >
            favorite
            {wishlistCount > 0 && (
              <span className="absolute -top-1.5 -right-1.5 bg-secondary text-white text-[9px] w-3.5 h-3.5 rounded-full flex items-center justify-center font-bold">
                {wishlistCount}
              </span>
            )}
          </button>

          <button
            onClick={() => onNavigate('cart')}
            className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors relative text-[20px]"
            title="Shopping Bag"
          >
            shopping_bag
            {cartCount > 0 && (
              <span className="absolute -top-1.5 -right-1.5 bg-secondary text-white text-[9px] w-3.5 h-3.5 rounded-full flex items-center justify-center font-bold">
                {cartCount}
              </span>
            )}
          </button>

          <div className="h-4 w-[1px] bg-outline-variant/40" />

          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                className="flex items-center gap-1.5 text-on-surface-variant hover:text-primary transition-colors text-xs font-semibold uppercase tracking-widest"
              >
                <span className="material-symbols-outlined text-[20px]">account_circle</span>
                <span>{user.firstName}</span>
              </button>
              {userDropdownOpen && (
                <div className="absolute right-0 mt-3 w-48 bg-surface border border-outline-variant/20 shadow-xl py-2 z-50">
                  {user.role === 'ADMIN' && (
                    <button
                      onClick={() => { onNavigate('admin'); setUserDropdownOpen(false); }}
                      className="w-full text-left px-4 py-2 hover:bg-surface-container-low text-primary font-bold text-xs uppercase tracking-widest"
                    >
                      Admin Suite
                    </button>
                  )}
                  <button
                    onClick={() => { onNavigate('profile'); setUserDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 hover:bg-surface-container-low text-on-surface text-xs"
                  >
                    My Profile
                  </button>
                  <button
                    onClick={() => { onLogout(); setUserDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 hover:bg-surface-container-low text-error text-xs"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              onClick={onOpenLogin}
              className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors text-[20px]"
              title="Sign In"
            >
              person
            </button>
          )}
        </div>

        {/* Mobile */}
        <div className="md:hidden flex items-center gap-4 flex-1 justify-end">
          <button onClick={onOpenSearch} className="material-symbols-outlined text-primary text-[22px]">search</button>
          <button onClick={() => onNavigate('cart')} className="material-symbols-outlined text-primary relative text-[22px]">
            shopping_bag
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-secondary text-white text-[8px] w-3 h-3 rounded-full flex items-center justify-center font-bold">
                {cartCount}
              </span>
            )}
          </button>
          <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="material-symbols-outlined text-primary text-[22px]">
            {mobileMenuOpen ? 'close' : 'menu'}
          </button>
        </div>
      </nav>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-background border-t border-outline-variant/10 px-margin-mobile pb-6 flex flex-col gap-4">
          <button onClick={() => { onNavigate('shop'); setMobileMenuOpen(false); }}
            className="text-left text-xs font-bold uppercase tracking-widest text-on-surface-variant hover:text-primary py-2">
            Collections
          </button>
          {user?.role === 'ADMIN' && (
            <button onClick={() => { onNavigate('admin'); setMobileMenuOpen(false); }}
              className="text-left text-xs font-bold uppercase tracking-widest text-primary py-2">
              Admin Suite
            </button>
          )}
          {user ? (
            <>
              <button onClick={() => { onNavigate('profile'); setMobileMenuOpen(false); }}
                className="text-left text-xs font-semibold text-on-surface py-2">
                Profile ({user.firstName})
              </button>
              <button onClick={() => { onLogout(); setMobileMenuOpen(false); }}
                className="text-left text-xs font-semibold text-error py-2">
                Sign Out
              </button>
            </>
          ) : (
            <button onClick={() => { onOpenLogin(); setMobileMenuOpen(false); }}
              className="text-left text-xs font-bold uppercase tracking-widest text-on-surface-variant hover:text-primary py-2">
              Sign In
            </button>
          )}
        </div>
      )}
    </header>
  );
}
