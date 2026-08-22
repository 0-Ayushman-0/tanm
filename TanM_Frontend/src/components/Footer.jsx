import React from 'react';

export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="bg-surface-container-low w-full pt-section-gap pb-12 border-t border-primary/5">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-10 px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">

        {/* Brand */}
        <div>
          <span className="font-display-lg text-2xl mb-6 block text-primary tracking-tighter">TanM</span>
          <p className="text-sm text-on-surface-variant leading-relaxed pr-4">
            Defining modern luxury through the lens of traditional leathercraft since 2020.
          </p>
          <div className="flex gap-4 mt-8 text-on-surface-variant">
            <span className="material-symbols-outlined cursor-pointer hover:text-primary transition-colors">qr_code_2</span>
            <span className="material-symbols-outlined cursor-pointer hover:text-primary transition-colors">public</span>
            <span className="material-symbols-outlined cursor-pointer hover:text-primary transition-colors">rss_feed</span>
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
