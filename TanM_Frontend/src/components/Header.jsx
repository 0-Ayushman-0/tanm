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
  const [scrolled, setScrolled] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
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
      className={`fixed w-full z-50 transition-all duration-500 ease-in-out border-b border-black/10
        ${scrolled ? 'top-0 shadow-sm bg-background/95 backdrop-blur-md' : isHome ? 'top-10 bg-background/80 backdrop-blur-md' : 'top-0 bg-background/95 backdrop-blur-md'}
      `}
    >
      {/* Announcement Bar — only shown un-scrolled on home */}
      {!scrolled && isHome && (
        <div className="bg-black text-white text-center py-2.5 text-xs font-semibold uppercase tracking-widest">
          Free Shipping on Orders Over ₹5,000 &nbsp;·&nbsp; Use Code <span className="font-bold">TANM10</span>
        </div>
      )}

      <nav className="flex justify-between items-center min-h-[5.5rem] px-margin-desktop max-w-container-max mx-auto py-2">

        {/* Mobile Left Spacer (to center logo on mobile) */}
        <div className="md:hidden flex-1" />

        {/* Left Nav */}
        <div className="flex-1 hidden md:flex gap-stack-lg items-center">
          <button
            onClick={() => onNavigate('shop')}
            className={`text-xs font-semibold uppercase tracking-widest transition-all duration-200 cursor-pointer
              ${currentTab === 'shop'
                ? 'text-black border-b-2 border-black pb-1 font-bold'
                : 'text-black hover:text-secondary hover:opacity-75'}`}
          >
            Collections
          </button>
        </div>

        {/* Logo — center */}
        <button
          onClick={() => onNavigate('home')}
          className="flex-none flex flex-col items-center justify-center group hover:opacity-85 transition-all py-1 cursor-pointer"
        >
          <img
            src="/logo-transparent.png"
            alt="TanM"
            className="h-11 md:h-13 w-auto object-contain transition-transform duration-300 group-hover:scale-105"
          />
          <span
            className="text-[11px] md:text-xs tracking-[0.32em] font-bold uppercase text-black mt-1 leading-none pl-[0.32em]"
            style={{ fontFamily: '"Arial Hebrew", Arial, sans-serif' }}
          >
            TanM
          </span>
        </button>

        {/* Right Actions */}
        <div className="flex-1 hidden md:flex justify-end items-center gap-2">
          <button
            onClick={onOpenWishlist}
            className="relative p-2 rounded-full text-black hover:text-secondary hover:bg-black/5 hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer flex items-center justify-center"
            title="Wishlist"
          >
            <span className="material-symbols-outlined text-[20px]">favorite</span>
            {wishlistCount > 0 && (
              <span className="absolute top-0.5 right-0.5 bg-secondary text-white text-[9px] w-4 h-4 rounded-full flex items-center justify-center font-bold shadow-sm">
                {wishlistCount}
              </span>
            )}
          </button>

          <button
            onClick={() => onNavigate('cart')}
            className="relative p-2 rounded-full text-black hover:text-secondary hover:bg-black/5 hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer flex items-center justify-center"
            title="Shopping Bag"
          >
            <span className="material-symbols-outlined text-[20px]">shopping_bag</span>
            {cartCount > 0 && (
              <span className="absolute top-0.5 right-0.5 bg-secondary text-white text-[9px] w-4 h-4 rounded-full flex items-center justify-center font-bold shadow-sm">
                {cartCount}
              </span>
            )}
          </button>

          <div className="h-4 w-[1px] bg-black/15 mx-1" />

          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                className="flex items-center gap-1.5 text-black hover:text-secondary hover:bg-black/5 px-2.5 py-1.5 rounded-full transition-all duration-200 text-xs font-semibold uppercase tracking-widest cursor-pointer"
              >
                <span className="material-symbols-outlined text-[20px]">account_circle</span>
                <span>{user.firstName}</span>
              </button>
              {userDropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-black/10 shadow-2xl py-2 z-50 rounded-sm">
                  {user.role === 'ADMIN' && (
                    <button
                      onClick={() => { onNavigate('admin'); setUserDropdownOpen(false); }}
                      className="w-full text-left px-4 py-2 hover:bg-black/5 text-black font-bold text-xs uppercase tracking-widest transition-colors cursor-pointer"
                    >
                      Admin Suite
                    </button>
                  )}
                  <button
                    onClick={() => { onNavigate('profile'); setUserDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 hover:bg-black/5 text-black text-xs transition-colors cursor-pointer"
                  >
                    My Profile
                  </button>
                  <button
                    onClick={() => { onLogout(); setUserDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 hover:bg-red-50 text-red-600 font-semibold text-xs transition-colors cursor-pointer"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              onClick={onOpenLogin}
              className="p-2 rounded-full text-black hover:text-secondary hover:bg-black/5 hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer flex items-center justify-center"
              title="Sign In"
            >
              <span className="material-symbols-outlined text-[20px]">person</span>
            </button>
          )}
        </div>

        {/* Mobile */}
        <div className="md:hidden flex items-center gap-2 flex-1 justify-end">
          <button
            onClick={() => onNavigate('cart')}
            className="relative p-1.5 rounded-full text-black hover:text-secondary hover:bg-black/5 transition-all cursor-pointer flex items-center justify-center"
            title="Cart"
          >
            <span className="material-symbols-outlined text-[22px]">shopping_bag</span>
            {cartCount > 0 && (
              <span className="absolute top-0 right-0 bg-secondary text-white text-[8px] w-3.5 h-3.5 rounded-full flex items-center justify-center font-bold">
                {cartCount}
              </span>
            )}
          </button>
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-1.5 rounded-full text-black hover:text-secondary hover:bg-black/5 transition-all cursor-pointer flex items-center justify-center"
          >
            <span className="material-symbols-outlined text-[22px]">
              {mobileMenuOpen ? 'close' : 'menu'}
            </span>
          </button>
        </div>
      </nav>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-background border-t border-black/10 px-margin-mobile py-4 flex flex-col gap-2 shadow-lg">
          <button
            onClick={() => { onNavigate('shop'); setMobileMenuOpen(false); }}
            className="text-left text-xs font-bold uppercase tracking-widest text-black hover:text-secondary py-2 border-b border-black/5 cursor-pointer transition-colors"
          >
            Collections
          </button>
          {user?.role === 'ADMIN' && (
            <button
              onClick={() => { onNavigate('admin'); setMobileMenuOpen(false); }}
              className="text-left text-xs font-bold uppercase tracking-widest text-black hover:text-secondary py-2 border-b border-black/5 cursor-pointer transition-colors"
            >
              Admin Suite
            </button>
          )}
          {user ? (
            <>
              <button
                onClick={() => { onNavigate('profile'); setMobileMenuOpen(false); }}
                className="text-left text-xs font-semibold text-black hover:text-secondary py-2 border-b border-black/5 cursor-pointer transition-colors"
              >
                Profile ({user.firstName})
              </button>
              <button
                onClick={() => { onLogout(); setMobileMenuOpen(false); }}
                className="text-left text-xs font-semibold text-red-600 hover:text-red-700 py-2 cursor-pointer transition-colors"
              >
                Sign Out
              </button>
            </>
          ) : (
            <button
              onClick={() => { onOpenLogin(); setMobileMenuOpen(false); }}
              className="text-left text-xs font-bold uppercase tracking-widest text-black hover:text-secondary py-2 cursor-pointer transition-colors"
            >
              Sign In
            </button>
          )}
        </div>
      )}
    </header>
  );
}
