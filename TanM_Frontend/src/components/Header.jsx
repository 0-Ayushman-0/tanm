import React, { useState, useEffect, useRef } from 'react';
import { cmsApi } from '../api';

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
  const [announcement, setAnnouncement] = useState({
    text: 'Free Shipping on Orders Over ₹5,000 · Use Code TANM10',
    isActive: true,
    backgroundColor: '#000000',
    textColor: '#FFFFFF',
  });
  const [announcementDismissed, setAnnouncementDismissed] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    let mounted = true;
    cmsApi.getHydration()
      .then((data) => {
        if (!mounted) return;
        if (data?.activeAnnouncement) {
          setAnnouncement({
            text: data.activeAnnouncement.text || '',
            isActive: data.activeAnnouncement.status === 'PUBLISHED',
            backgroundColor: data.activeAnnouncement.backgroundColor || '#000000',
            textColor: data.activeAnnouncement.textColor || '#FFFFFF',
          });
        } else if (data && data.activeAnnouncement === null) {
          setAnnouncement((prev) => ({ ...prev, isActive: false }));
        }
      })
      .catch((err) => {
        console.warn('Hydration announcement fetch fallback', err);
      });
    return () => { mounted = false; };
  }, []);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 30);
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
      className={`fixed top-0 left-0 w-full z-50 transition-all duration-300 ease-in-out border-b border-black/10
        ${scrolled ? 'shadow-sm bg-background/95 backdrop-blur-md' : isHome ? 'bg-background/80 backdrop-blur-md' : 'bg-background/95 backdrop-blur-md'}
      `}
    >
      {/* Announcement Bar — sits flush at the very top (y=0, zero blank space) */}
      {announcement.isActive && !announcementDismissed && isHome && (
        <div
          className={`relative w-full transition-all duration-300 ease-in-out overflow-hidden selection:bg-[#e6b980] selection:text-black ${scrolled ? 'max-h-0 opacity-0 pointer-events-none' : 'max-h-12 opacity-100'
            }`}
          style={{
            backgroundColor: announcement.backgroundColor || '#000000',
            color: announcement.textColor || '#FFFFFF',
          }}
        >
          <div className="max-w-container-max mx-auto py-2 flex items-center overflow-hidden">
            {/* Desktop View: Centered, elegant text */}
            <div className="hidden md:flex items-center justify-center w-full px-16">
              <p className="text-xs font-semibold uppercase tracking-widest text-center truncate">
                {announcement.text}
              </p>
            </div>

            {/* Mobile View: Continuous smooth infinite marquee effect */}
            <div className="md:hidden w-full overflow-hidden flex items-center pr-12">
              <div className="animate-marquee-mobile flex items-center whitespace-nowrap">
                {/* 1st Half of Marquee Track */}
                <div className="flex shrink-0 items-center">
                  <span className="text-[11px] font-semibold uppercase tracking-widest px-4">
                    {announcement.text}
                  </span>
                  <span className="text-[10px] opacity-40 px-2 font-bold">·</span>
                  <span className="text-[11px] font-semibold uppercase tracking-widest px-4">
                    {announcement.text}
                  </span>
                  <span className="text-[10px] opacity-40 px-2 font-bold">·</span>
                </div>
                {/* 2nd Half of Marquee Track (identical duplicate for seamless 0 to -50% loop) */}
                <div className="flex shrink-0 items-center" aria-hidden="true">
                  <span className="text-[11px] font-semibold uppercase tracking-widest px-4">
                    {announcement.text}
                  </span>
                  <span className="text-[10px] opacity-40 px-2 font-bold">·</span>
                  <span className="text-[11px] font-semibold uppercase tracking-widest px-4">
                    {announcement.text}
                  </span>
                  <span className="text-[10px] opacity-40 px-2 font-bold">·</span>
                </div>
              </div>
            </div>
          </div>

          {/* Right fade mask on mobile so marquee text smoothly glides behind the close button */}
          <div
            className="md:hidden absolute right-0 top-0 bottom-0 w-16 pointer-events-none"
            style={{
              background: `linear-gradient(to right, transparent, ${announcement.backgroundColor || '#000000'} 80%)`,
            }}
          />

          {/* Storefront on/off dismiss button — positioned at the far right end of the screen */}
          <button
            type="button"
            onClick={() => setAnnouncementDismissed(true)}
            className="absolute right-3 sm:right-6 top-1/2 -translate-y-1/2 p-1 rounded-full opacity-70 hover:opacity-100 hover:bg-white/10 active:scale-95 transition-all cursor-pointer flex items-center justify-center z-10"
            aria-label="Close announcement bar"
            title="Close announcement"
          >
            <span className="material-symbols-outlined text-[16px]">close</span>
          </button>
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
