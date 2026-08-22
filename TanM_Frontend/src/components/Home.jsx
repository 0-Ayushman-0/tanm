import React, { useState, useEffect, useRef } from 'react';
import { productApi } from '../api';

// ── Static fallback data ────────────────────────────────────────
const TESTIMONIALS = [
  {
    text: '"The patina on my TanM briefcase after two years of daily use is simply magnificent. It\'s truly a piece that ages with grace."',
    author: '— James R., Architect',
  },
  {
    text: '"A level of detail you rarely see today. Every stitch is a testament to the artisan\'s dedication. My favourite travel companion."',
    author: '— Elena M., Creative Director',
  },
  {
    text: '"The craftsmanship is extraordinary. I received it as a gift and it has become the most-complimented item I own."',
    author: '— Robert K., Collector',
  },
];

const LIFESTYLE_IMAGES = [
  'https://lh3.googleusercontent.com/aida-public/AB6AXuDzVfC-mLrJtP-aUYGTCykVjRzbeRlLrx_OA8OeJGmCGueCiI39-pcK1eFFHwQXggCfo9Kpvgxmcq7VCCcQBrWGW27CtuisIRsCsx1JoEBauD8FP3_311aojp4rVB48ZKNg0PTTuc4OVsa9ebQDXQ-3LVC9bnGo_hS-daxjZKhBEGKpo3FZIurVsNlBAuiIO7ZzxbQaNS7ub3HwpqL01HGEp25OJLr5iozdG6bNZ9UAIfZqcktBjKcutuQV58yV10covzx8hsgGc5A',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuAhktScgGju2w9oLQf7Yrnj6fFhj4o46GWgWVzzIDIBPHcoGwUQrRrQOB_KS0MsjKZk73RNa6FCXh6KCPBBNrcc_S52iT4ZJLebeDdjcQxCU_VkilKv7-ZLMUw2tThG2x1baf5yssh1E2wJnZjNq1OhRbsq4AtJ8xLU8tInACDk302eYCTIWRa0fJ8EmBHdRBAfOHDEBC3EsomLExV0zxe8EDnRwOjdAZ_SJMAmvWfMYBg23LaNFUzMzbtZKdJsB_RM-9SgRXe71zk',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCYYbQ_C97a0er0Na3GSnBCGoYT-lQtPef_Dvv1jZVbJ2cnJ6Lvc0JDsY3jRhnLkZJAjdAMO5Jsfd7dnqHILv_jxZqDOVnEL-noAwuVByS6qj8fNmErP5Ib0jEog_bSMa7b-SpQsqwcP24Rd1iUFA2PnrVnBIn0x2VAw6xFrXwp5OowVuanPfk9J8tYop8ER9h7RVPwmZbCa7qOiZbO1as-4dWVbv953hpgVqOfjFnovspBH3fFnWz6Ug2stpzasgFWcDk3Uo0uAso',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuB6Byf0oyJtYVg-swTPHDgARS63X1-OxFQJDG4ewV6LgphAKFjP6cGWrkScRxN737TuZEnEvN6894Xnvbz8XauDsbjPqLQVQLPSHpeyqV1FWGqS4DUZFR2hGsZXR7bHBOoUp5myMI-6xqpIG-Ha1Bxmhrbmeo8Nl3RKDnf2L9e95lCYCMBKyJiIULJ6e2HyYrUd0LR3-M_aLq67COUYlvput6sx0LvWRTd7mEj_E8Zoky6eSi_U4PqrPrd2-TeFWUfRDaHSJAI1-_c',
];

const COLLECTION_CARDS = [
  {
    label: 'Wallets',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAooTW8YSmpGE5Jx6pEk_5oLy4PRleEm4CeUs_vnoXNoVMrqPlhhfs_VCUZn06XwCNXIC4Mt-RSgMyCZtJqezF9tNTFu0tdaklu3zwa94aoNS2a6CnzB0oEakboeUW_lqoI8XC0rVjx2i0JF7-21OJ-ClDxph5hpyR3p-yjH7X18LqwBVgjvWfWtG6szOn2EyMQfclmjXk0LRCMt9R1HUl02Q0xoQ2amzeGOd6eHUUSxiM1QUi-S-Uie1IgdhylkjS-mBtCuJJQ',
  },
  {
    label: 'Bags',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBa6I-_P44uIaoDvj88lbw5XlLNrZGT0p2o2mkNN4AVEQSqtyiy4IEc16QdGLHcCKBKAcG94UI10YLSe8CSsdbqvc0FoR9hTOaRs69Afgj7ObBjObaKbTR9bvXYfrUj65rTWoF-aFrxZx6epmcOV34dL3x4DQkCA6jKViqC-B_9Hdb2SNcc4-2hQRAza6WIKreTdV7fXUZsS8L8TLy0NjH6g5S5RUbMcbfg-7iTxI_0wWOtkaZ6tFSmaWzeJtiwALzKf6tVG4whYjQ',
  },
  {
    label: 'Belts',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDZ1IZAEqyb2U2-81N2C0r-rThcX30f40oU9wNiJqYmSO3OCXQ8-f18a_MV7D4gmAvU6tXD_gK0qfMaslva4QLFYMFEmIu3B1iPp-LumzAR67_uMQNNBYoz0ivqApdGClUJd74cQJ-733AzcQidPIwkhzvIuXorMWZMLQgBLnqCiX0_7mRpeS4DyRcgcO8xjqkw1wR_tAEwwFzeqgTORf7X-oR13cz6l02pBO5TYba0bHO60xd6UwHzPONpQ3BVkt44y4XwMdk5KJc',
  },
  {
    label: 'Accessories',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAyJJTk_WuZ5lkQH9AF9apq7nIVLkloWnjE4WFfAB5YrMrDif7M0UOXcmdbyMnNCLdStCcNabSyUbsjroYmjdfbcK4YTCRoMLe6OZFc-tjJ3R8MRHtDCBRoKfY73i1_jZeZXRJj2fMdcp1E5b9CW-aKIg5CYo04mty2q6UDf-s87X5pSR_sHHo-w7YN54TV8eUeNF343w0e4LnUNLoF-6Nu7sbWZiPeQxkk1YPerueswOUT_PMGHRB7GbEMMPFIkAs3fkXjHREfjfY',
  },
];

const fmt = (price) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(price);

export default function Home({ onSelectProduct, onNavigate }) {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const parallaxRef = useRef(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await productApi.filter({ isFeatured: true, size: 3 });
        setFeaturedProducts(res.content || []);
      } catch (e) {
        console.error('Failed to load featured products', e);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // Scroll-reveal observer
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => entries.forEach((e) => { if (e.isIntersecting) e.target.classList.add('revealed'); }),
      { threshold: 0.12 }
    );
    document.querySelectorAll('.reveal-item').forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, [loading]);

  // Bento: first product is large (col-span-8), rest are small
  const bentoLarge  = featuredProducts[0] || null;
  const bentoSmall  = featuredProducts.slice(1, 3);

  return (
    <div className="bg-background text-on-background">

      {/* ── 1. FULLSCREEN HERO ──────────────────────────────────── */}
      <section className="relative h-screen w-full overflow-hidden flex items-center justify-center">
        <div className="absolute inset-0 z-0">
          <div
            className="w-full h-full hero-zoom bg-cover bg-center bg-no-repeat"
            style={{ backgroundImage: `url("https://lh3.googleusercontent.com/aida-public/AB6AXuBG3ZcK3WBhUmm_y5908QJLvFVXl1pIYe18WBaiqsADd02Ay2VxmXcP8PCaZ6Wt7866oNy2quInxbwAElcersReVq9oySi8ffMCw8uh-SHSbubRN7S2R8iqBO69jDcSJJ_u5D6KqZqxGBY9wgTAv0ub7_w7MU1ITfeeulBvQvpyGbnO3ec5w-Eya6oVkrnXPc0vojn78oqaGdKQH3ZR6dlItINu-QWdtnfkZxSklrCOwdvnkIabVnsbeFeDYzFfLvLB0J_DW01jBmI")` }}
          />
          <div className="absolute inset-0 bg-primary/25 mix-blend-multiply" />
        </div>
        <div className="relative z-10 text-center text-white px-margin-mobile md:px-margin-desktop">
          <p className="text-xs font-semibold uppercase tracking-[0.4em] mb-6 opacity-80">Est. 2020 · Handcrafted in India</p>
          <h1 className="font-display-lg text-5xl md:text-7xl mb-4 tracking-tight drop-shadow-lg leading-tight">
            ART OF THE STITCH
          </h1>
          <p className="text-base md:text-lg mb-12 tracking-widest uppercase opacity-90 font-light">
            Luxury Leather Crafted For Life.
          </p>
          <div className="flex gap-stack-md justify-center flex-wrap">
            <button
              onClick={() => onNavigate('shop')}
              className="bg-primary-container text-white px-10 py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary transition-all duration-300 border border-primary-container"
            >
              Explore Collection
            </button>
            <button
              onClick={() => {
                document.getElementById('brand-story')?.scrollIntoView({ behavior: 'smooth' });
              }}
              className="bg-transparent text-white px-10 py-4 text-xs font-bold uppercase tracking-widest border border-white hover:bg-white hover:text-primary transition-all duration-300"
            >
              Our Story
            </button>
          </div>
        </div>
        {/* Scroll indicator */}
        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2 text-white/60 animate-bounce">
          <span className="material-symbols-outlined text-[20px]">keyboard_arrow_down</span>
        </div>
      </section>

      {/* ── 2. BRAND STORY ──────────────────────────────────────── */}
      <section id="brand-story" className="py-section-gap px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto overflow-hidden">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-24 items-center">
          {/* Stacked image pair */}
          <div className="relative h-[560px] reveal-item">
            <div
              className="absolute top-0 left-0 w-3/4 h-[420px] shadow-2xl z-10 bg-cover bg-center"
              style={{ backgroundImage: `url("https://lh3.googleusercontent.com/aida-public/AB6AXuBelvgn8qTBy4lpUFXuIWutPmodL_1Xm0aB05hzSIMcUygl_gB8x7bZA7S-R7c8TUjFn5voHFdJcuFNJAqYtInIM4CLfF8C93fnmmEyzyeFxk3lsCAUjeP5DdF_rLeypZ5QbZPohi9KTnGURUKNGXGtHbNfhZgXhDA2kEbLCH64quTpFKxHoSQ1ZRPDoP7BowYGayeInaEJa55X-B3K2PL9nyxCy0Bt9t011eWqMiUbvtMwjws0p8vHkSUT6V7y0obp2n5FIqfXP5w")` }}
            />
            <div
              className="absolute bottom-0 right-0 w-2/3 h-[360px] shadow-2xl z-20 bg-cover bg-center"
              style={{ backgroundImage: `url("https://lh3.googleusercontent.com/aida-public/AB6AXuAgLe3hlE9tuaGo_ket1ujAXOkW1bZeM3OH5kjSrN7XZlAoqTzFns-gFoPJkmEXTZXHB86DuULmQpaqIprTrGPwBYce6wXaaD1vsbgBqeHYP0d02OWYi3C1P4deB81mh6fUKuLmuPTu1jqCom1cEQRRGITIyZTiLEkFgzmUMNizWapna1hnZPjAuqE48nFa_FUV_oPifPofKYTng2r6eJTn0Uf1pLALGqnycOjhIsdA_JIhDxPiC1n82fTkyrc8NW_LH_kmSDtwu3s")` }}
            />
          </div>

          {/* Copy */}
          <div className="space-y-6 reveal-item">
            <span className="text-xs font-bold uppercase tracking-[0.3em] text-secondary">Our Philosophy</span>
            <h2 className="font-display-lg text-4xl md:text-5xl leading-tight text-primary">The Craft of Patience</h2>
            <p className="text-base text-on-surface-variant leading-relaxed max-w-lg">
              In an era of fleeting trends, TanM stands for the permanent. Each piece is born from a dialogue between artisan and material — a process that cannot be rushed, only perfected through decades of heritage techniques and singular focus.
            </p>
            <div className="flex gap-8 pt-4">
              {[['10+', 'Master Artisans'], ['3', 'Leather Grades'], ['100%', 'Hand-Stitched']].map(([val, label]) => (
                <div key={label}>
                  <p className="font-display-lg text-2xl font-semibold text-primary">{val}</p>
                  <p className="text-xs uppercase tracking-widest text-on-surface-variant mt-1">{label}</p>
                </div>
              ))}
            </div>
            <button
              onClick={() => onNavigate('shop')}
              className="inline-block border-b-2 border-primary pb-1 text-xs font-bold uppercase tracking-widest text-primary hover:opacity-60 transition-opacity"
            >
              Discover the Atelier →
            </button>
          </div>
        </div>
      </section>

      {/* ── 3. SIGNATURE COLLECTIONS ────────────────────────────── */}
      <section className="bg-surface-container-low py-section-gap">
        <div className="px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">
          <div className="mb-12 flex justify-between items-end reveal-item">
            <h2 className="font-display-lg text-3xl text-primary">Signature Collections</h2>
            <button
              onClick={() => onNavigate('shop')}
              className="text-xs font-bold uppercase tracking-widest text-secondary hover:text-primary transition-colors"
            >
              View All →
            </button>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-gutter">
            {COLLECTION_CARDS.map((col) => (
              <div
                key={col.label}
                onClick={() => onNavigate('shop')}
                className="group cursor-pointer overflow-hidden relative aspect-[3/4] bg-white shadow-sm reveal-item"
              >
                <div
                  className="absolute inset-0 bg-cover bg-center transition-transform duration-1000 group-hover:scale-110"
                  style={{ backgroundImage: `url("${col.image}")` }}
                />
                <div className="absolute inset-0 bg-black/10 group-hover:bg-black/35 transition-colors duration-500" />
                <div className="absolute bottom-8 left-1/2 -translate-x-1/2 text-center text-white w-full">
                  <h3 className="font-display-lg text-xl mb-2">{col.label}</h3>
                  <span className="text-[10px] font-bold uppercase tracking-[0.2em] border-b border-white pb-1 opacity-0 group-hover:opacity-100 transition-all duration-500">
                    Explore
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── 4. FEATURED PRODUCTS BENTO ──────────────────────────── */}
      <section className="py-section-gap px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">
        <h2 className="font-display-lg text-3xl text-center mb-16 text-primary reveal-item">The Curated Edit</h2>

        {loading ? (
          <div className="grid grid-cols-12 grid-rows-2 gap-gutter h-[700px]">
            <div className="col-span-8 row-span-2 bg-surface-container animate-pulse rounded" />
            <div className="col-span-4 row-span-1 bg-surface-container animate-pulse rounded" />
            <div className="col-span-4 row-span-1 bg-surface-container animate-pulse rounded" />
          </div>
        ) : featuredProducts.length === 0 ? (
          <div className="text-center py-20 text-on-surface-variant">
            <span className="material-symbols-outlined text-4xl mb-4 block">inventory_2</span>
            <p className="text-sm">No featured products yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-12 grid-rows-2 gap-gutter" style={{ height: '700px' }}>
            {/* Large left card */}
            {bentoLarge && (
              <div
                className="col-span-12 md:col-span-8 row-span-2 relative group overflow-hidden bg-surface-container cursor-pointer reveal-item"
                onClick={() => onSelectProduct(bentoLarge)}
              >
                {bentoLarge.mainImageUrl ? (
                  <img
                    src={bentoLarge.mainImageUrl}
                    alt={bentoLarge.name}
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                  />
                ) : (
                  <div className="w-full h-full bg-surface-container-high flex items-center justify-center">
                    <span className="material-symbols-outlined text-6xl text-outline-variant">imagesearch_roller</span>
                  </div>
                )}
                <div className="absolute bottom-0 left-0 w-full p-8 bg-gradient-to-t from-black/70 to-transparent text-white">
                  <span className="text-[10px] font-bold uppercase tracking-widest text-secondary-fixed-dim">Masterpiece Series</span>
                  <h3 className="font-display-lg text-2xl mt-2 mb-1">{bentoLarge.name}</h3>
                  <p className="text-sm opacity-80 mb-3">{bentoLarge.leatherType || 'Full Grain Leather'}</p>
                  <div className="flex items-center justify-between">
                    <span className="text-xl font-semibold">{fmt(bentoLarge.price)}</span>
                    <span className="text-[10px] font-bold uppercase tracking-widest border border-white px-4 py-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                      View Details
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* Small right cards */}
            {bentoSmall.map((product, i) => (
              <div
                key={product.id}
                className="col-span-12 md:col-span-4 row-span-1 relative group overflow-hidden bg-surface-container cursor-pointer reveal-item"
                onClick={() => onSelectProduct(product)}
              >
                {product.mainImageUrl ? (
                  <img
                    src={product.mainImageUrl}
                    alt={product.name}
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                  />
                ) : (
                  <div className="w-full h-full bg-surface-container-high flex items-center justify-center">
                    <span className="material-symbols-outlined text-4xl text-outline-variant">imagesearch_roller</span>
                  </div>
                )}
                <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <button className="bg-white text-primary px-6 py-3 text-[10px] font-bold uppercase tracking-widest">
                    Shop Now
                  </button>
                </div>
                <div className="absolute bottom-5 left-5 text-white pointer-events-none">
                  <h3 className="font-display-lg text-base">{product.name}</h3>
                  <span className="text-xs font-semibold opacity-90">{fmt(product.price)}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="text-center mt-12 reveal-item">
          <button
            onClick={() => onNavigate('shop')}
            className="border border-primary text-primary px-12 py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary hover:text-white transition-all duration-300"
          >
            View Full Collection
          </button>
        </div>
      </section>

      {/* ── 5. HERITAGE PARALLAX ────────────────────────────────── */}
      <section
        className="relative h-[560px] w-full overflow-hidden flex items-center justify-center"
        id="craft"
      >
        <div className="absolute inset-0 z-0">
          <div
            className="w-full h-full bg-cover bg-fixed bg-center"
            style={{ backgroundImage: `url("https://lh3.googleusercontent.com/aida-public/AB6AXuCKK2nAxgWNLFGSvLDaIhxFdUw7-o6kP00suOWm4pt0fhQj_jQyqmg0dHeQVqOK93efgCdFpmhv8dHYMpCXBUTH54NqoJ0jM88n-Me-Q4ciJip1Mxryy-toLYgU9PYsXoZ4EZ9ev5r3HN4AGsUoXeNIXpaiOUWpT6CAY-s3e1Pc9be3uwY9RZGsRdUdTjs-AuDacs7ASHq6P77nEhJ6cFnyb7ZtsbFZwcEH-CSiZ5wT9pzDjgA5mskGqdOVD8N2Zwf2qnvKFfeMzSs")` }}
          />
          <div className="absolute inset-0 bg-primary/60 mix-blend-multiply" />
        </div>
        <div className="relative z-10 text-center text-white px-margin-mobile md:px-margin-desktop reveal-item">
          <h2 className="font-display-lg text-4xl md:text-6xl mb-6 tracking-wider">MASTERED OVER GENERATIONS</h2>
          <p className="text-base md:text-lg max-w-2xl mx-auto opacity-80 italic leading-relaxed">
            "The tool is only as good as the hand that guides it, and the hand only as good as the heritage that guides the heart."
          </p>
        </div>
      </section>

      {/* ── 6. TESTIMONIALS ─────────────────────────────────────── */}
      <section className="py-section-gap px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">
        <h2 className="font-display-lg text-3xl text-center text-primary mb-16 reveal-item">What Our Clients Say</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-gutter">
          {TESTIMONIALS.map((t, i) => (
            <div key={i} className="p-8 border border-primary/5 hover-lift bg-white reveal-item">
              <div className="flex mb-4 text-secondary">
                {[...Array(5)].map((_, s) => (
                  <span key={s} className="material-symbols-outlined text-base" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                ))}
              </div>
              <p className="font-display-lg text-base italic text-on-surface leading-relaxed mb-6">{t.text}</p>
              <span className="text-[11px] font-bold uppercase tracking-widest text-primary">{t.author}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── 7. LIFESTYLE GALLERY ────────────────────────────────── */}
      <section className="pb-section-gap">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-0.5">
          {LIFESTYLE_IMAGES.map((img, i) => (
            <div
              key={i}
              className="aspect-square bg-cover bg-center cursor-crosshair group relative overflow-hidden"
              style={{ backgroundImage: `url("${img}")` }}
            >
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-500 flex items-center justify-center text-white">
                <span className="material-symbols-outlined text-3xl">add</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── 8. NEWSLETTER ───────────────────────────────────────── */}
      <section className="py-section-gap bg-surface-container text-center border-t border-primary/5">
        <div className="max-w-xl mx-auto px-margin-mobile reveal-item">
          <span className="text-xs font-bold uppercase tracking-[0.3em] text-secondary">Exclusive Access</span>
          <h2 className="font-display-lg text-3xl text-primary mt-4 mb-4">Join The TanM Journal</h2>
          <p className="text-sm text-on-surface-variant mb-8 leading-relaxed">
            Receive exclusive access to new releases, atelier stories, and heritage notes. No noise — only craft.
          </p>
          <form
            className="flex flex-col md:flex-row gap-4"
            onSubmit={(e) => { e.preventDefault(); alert('Thank you for subscribing!'); }}
          >
            <input
              type="email"
              placeholder="Your email address"
              required
              className="flex-grow bg-transparent border-0 border-b border-primary/20 focus:border-primary focus:outline-none text-sm py-4 text-on-surface placeholder:text-outline"
            />
            <button
              type="submit"
              className="bg-primary text-white px-12 py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors"
            >
              Subscribe
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
