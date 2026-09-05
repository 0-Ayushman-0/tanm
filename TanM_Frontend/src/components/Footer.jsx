import React from 'react';

export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="bg-surface-container-low w-full pt-section-gap pb-12 border-t border-primary/5">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-10 px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">

        {/* Brand */}
        <div>
          <div className="flex flex-col items-start gap-1.5 mb-6">
            <img
              src="/logo-transparent.png"
              alt="TanM"
              className="h-14 w-auto object-contain"
            />
            <span className="text-xs md:text-sm tracking-[0.32em] uppercase font-bold text-primary pl-[0.32em]">
              tanm
            </span>
          </div>
          <p className="text-sm text-on-surface-variant leading-relaxed pr-4">
            Defining modern luxury through the lens of traditional leathercraft since 2020.
          </p>
          <div className="flex items-center gap-4 mt-8 text-on-surface-variant">
            <a
              href="https://www.instagram.com/tanmbags/"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors group"
              title="Follow TanM on Instagram (@tanmbags)"
            >
              <svg className="w-5 h-5 fill-current transition-transform group-hover:scale-110" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
              </svg>
              <span className="text-xs tracking-wider font-semibold">@tanmbags</span>
            </a>
          </div>
        </div>

        {/* The Maison */}
        <div className="flex flex-col gap-3">
          <h4 className="text-[11px] font-bold uppercase tracking-widest text-primary mb-2">The Maison</h4>
          {['Sustainability', 'Atelier History', 'Bespoke Services', 'Store Locator'].map((link) => (
            <a key={link} href="#"
              className="text-sm text-on-surface-variant hover:text-primary hover:translate-x-1 transition-all duration-300 inline-block">
              {link}
            </a>
          ))}
        </div>

        {/* Client Services */}
        <div className="flex flex-col gap-3">
          <h4 className="text-[11px] font-bold uppercase tracking-widest text-primary mb-2">Client Services</h4>
          {['Shipping & Returns', 'Product Care', 'Contact Us', 'FAQs'].map((link) => (
            <a key={link} href="#"
              className="text-sm text-on-surface-variant hover:text-primary hover:translate-x-1 transition-all duration-300 inline-block">
              {link}
            </a>
          ))}
        </div>

        {/* Legal */}
        <div className="flex flex-col gap-3">
          <h4 className="text-[11px] font-bold uppercase tracking-widest text-primary mb-2">Legal</h4>
          {['Legal & Privacy', 'Terms of Service', 'Accessibility', 'Cookie Settings'].map((link) => (
            <a key={link} href="#"
              className="text-sm text-on-surface-variant hover:text-primary hover:translate-x-1 transition-all duration-300 inline-block">
              {link}
            </a>
          ))}
        </div>
      </div>

      {/* Bottom bar */}
      <div className="mt-20 px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto pt-8 border-t border-primary/5 flex flex-col md:flex-row justify-between items-center text-on-surface-variant text-[11px]">
        <span>© {year} TanM Atelier. All Rights Reserved.</span>
        <span className="mt-3 md:mt-0 tracking-widest uppercase">ENG / INR</span>
      </div>
    </footer>
  );
}
