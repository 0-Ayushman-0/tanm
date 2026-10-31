import React, { useState, useEffect, useRef } from 'react';
import { productApi, categoryApi, cmsApi } from '../api';
import { useToast } from '../context/ToastContext';

// ── Static fallback data ────────────────────────────────────────
const TESTIMONIALS = [
  {
    text: '"The patina on my TanM briefcase after two years of daily use is simply magnificent. It\'s truly a piece that ages with grace."',
    author: '— Kabir Mehta, Architect',
  },
  {
    text: '"A level of detail you rarely see today. Every stitch is a testament to the artisan\'s dedication. My favourite travel companion."',
    author: '— Ananya Sen, Creative Director',
  },
  {
    text: '"The craftsmanship is extraordinary. I received it as a gift and it has become the most-complimented item I own."',
    author: '— Rohan Kapoor, Design Collector',
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
    image: 'https://res.cloudinary.com/tanm/image/upload/v1793429692/tanm/categories/xo4l3q5jt1fq91jvcedk.jpg',
  },
  {
    label: 'Bags',
    image: 'https://res.cloudinary.com/tanm/image/upload/v1793429837/tanm/categories/vx898dnbizmj9eu2flbc.jpg',
  },
  {
    label: 'Belts',
    image: 'https://res.cloudinary.com/tanm/image/upload/v1793429840/tanm/categories/cuwpcffbfvm0o08wcaqu.jpg',
  },
  {
    label: 'Accessories',
    image: 'https://res.cloudinary.com/tanm/image/upload/v1793429843/tanm/categories/kgswsd4fa1ezzasxpzhb.jpg',
  },
];

const fmt = (price) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(price);

export default function Home({ onSelectProduct, onNavigate }) {
  const toast = useToast();
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [signatureCategories, setSignatureCategories] = useState([]);
  const [siteSettings, setSiteSettings] = useState({});
  const [loading, setLoading] = useState(true);
  const parallaxRef = useRef(null);

  const collectionScrollRef = useRef(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const checkScrollBounds = () => {
    if (collectionScrollRef.current) {
      const { scrollLeft, scrollWidth, clientWidth } = collectionScrollRef.current;
      setCanScrollLeft(scrollLeft > 10);
      setCanScrollRight(scrollLeft + clientWidth < scrollWidth - 10);
    }
  };

  const scrollCollections = (direction) => {
    if (collectionScrollRef.current) {
      const scrollAmount = collectionScrollRef.current.clientWidth * 0.75;
      collectionScrollRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

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

    (async () => {
      try {
        const cats = await categoryApi.getSignature();
        const data = Array.isArray(cats) ? cats : (cats?.content || []);
        setSignatureCategories(data);
      } catch (e) {
        console.error('Failed to load signature categories', e);
      }
    })();

    (async () => {
      try {
        const hyd = await cmsApi.getHydration();
        if (hyd?.siteSettings) {
          setSiteSettings(hyd.siteSettings);
        }
      } catch (e) {
        console.error('Failed to load CMS hydration', e);
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
  }, [loading, signatureCategories]);

  useEffect(() => {
    checkScrollBounds();
    const handleResize = () => checkScrollBounds();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [signatureCategories]);

  // Dynamic signature collections list with graceful fallback to curated items
  const displayCollections = signatureCategories.length > 0
    ? signatureCategories.map((c) => ({
        id: c.id,
        label: c.name,
        image: c.imageUrl || 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&q=80',
      }))
    : COLLECTION_CARDS.map((c) => ({
        id: null,
        label: c.label,
        image: c.image,
      }));

  // Bento: first product is large (col-span-8), rest are small
  const bentoLarge = featuredProducts[0] || null;
  const bentoSmall = featuredProducts.slice(1, 3);

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
              className="bg-black text-white px-10 py-4 text-xs font-bold uppercase tracking-[0.2em] border border-black hover:bg-white hover:text-black hover:shadow-2xl hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] transition-all duration-300 cursor-pointer"
            >
              Explore Collection
            </button>
            <button
              onClick={() => {
                document.getElementById('brand-story')?.scrollIntoView({ behavior: 'smooth' });
              }}
              className="bg-black/60 backdrop-blur-md text-white px-10 py-4 text-xs font-bold uppercase tracking-[0.2em] border border-white/60 hover:bg-white hover:text-black hover:border-white hover:shadow-2xl hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] transition-all duration-300 cursor-pointer"
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
              className="absolute top-0 left-0 w-3/4 h-[420px] shadow-2xl z-10 bg-cover bg-center rounded"
              style={{ backgroundImage: `url("${siteSettings['craft_image_1'] || 'https://res.cloudinary.com/tanm/image/upload/v1793431372/tanm/cms/ryqx17zfukzuzkqbjtzw.jpg'}")` }}
            />
            <div
              className="absolute bottom-0 right-0 w-2/3 h-[360px] shadow-2xl z-20 bg-cover bg-center rounded border-4 border-surface"
              style={{ backgroundImage: `url("${siteSettings['craft_image_2'] || 'https://res.cloudinary.com/tanm/image/upload/v1793431375/tanm/cms/t0gxzpcotyhlvzbkdfzm.jpg'}")` }}
            />
          </div>

          {/* Copy */}
          <div className="space-y-6 reveal-item">
            <span className="text-xs font-bold uppercase tracking-[0.3em] text-primary">
              {siteSettings['craft_subtitle'] || 'Our Philosophy'}
            </span>
            <h2 className="font-display-lg text-4xl md:text-5xl leading-tight text-primary">
              {siteSettings['craft_title'] || 'The Craft of Patience'}
            </h2>
            <p className="text-base text-on-surface-variant leading-relaxed max-w-lg">
              {siteSettings['craft_description'] || 'In an era of fleeting trends, TanM stands for the permanent. Each piece is born from a dialogue between artisan and material — a process that cannot be rushed, only perfected through decades of heritage techniques and singular focus.'}
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
              className="inline-block border-b-2 border-primary pb-1 text-xs font-bold uppercase tracking-widest text-primary hover:text-secondary hover:border-secondary transition-all cursor-pointer"
            >
              Discover the Atelier →
            </button>
          </div>
        </div>
      </section>

      {/* ── 3. SIGNATURE COLLECTIONS ────────────────────────────── */}
      <section className="bg-surface-container-low py-section-gap overflow-hidden">
        <div className="px-margin-mobile md:px-margin-desktop max-w-container-max mx-auto">
          <div className="mb-10 flex flex-wrap justify-between items-end gap-4 reveal-item">
            <div>
              <span className="text-[10px] uppercase tracking-[0.25em] font-bold text-secondary">
                Curated Categories
              </span>
              <h2 className="font-display-lg text-3xl text-primary mt-1">Signature Collections</h2>
            </div>

            <div className="flex items-center gap-4">
              <button
                onClick={() => onNavigate('shop')}
                className="text-xs font-bold uppercase tracking-widest text-primary hover:text-secondary transition-colors cursor-pointer mr-2"
              >
                View All →
              </button>

              {/* Navigation Arrow Buttons */}
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  aria-label="Previous collections"
                  onClick={() => scrollCollections('left')}
                  disabled={!canScrollLeft}
                  className={`w-10 h-10 rounded-full border border-outline-variant/40 flex items-center justify-center transition-all ${
                    canScrollLeft
                      ? 'text-primary hover:bg-primary hover:text-surface hover:border-primary cursor-pointer shadow-sm'
                      : 'text-outline-variant opacity-40 cursor-not-allowed'
                  }`}
                >
                  <span className="material-symbols-outlined text-xl">chevron_left</span>
                </button>
                <button
                  type="button"
                  aria-label="Next collections"
                  onClick={() => scrollCollections('right')}
                  disabled={!canScrollRight}
                  className={`w-10 h-10 rounded-full border border-outline-variant/40 flex items-center justify-center transition-all ${
                    canScrollRight
                      ? 'text-primary hover:bg-primary hover:text-surface hover:border-primary cursor-pointer shadow-sm'
                      : 'text-outline-variant opacity-40 cursor-not-allowed'
                  }`}
                >
                  <span className="material-symbols-outlined text-xl">chevron_right</span>
                </button>
              </div>
            </div>
          </div>

          {/* Horizontal Scroll Track (without scrollbar) */}
          <div
            ref={collectionScrollRef}
            onScroll={checkScrollBounds}
            className="flex gap-6 overflow-x-auto no-scrollbar scroll-smooth snap-x snap-mandatory pb-4 pt-1"
          >
            {displayCollections.map((col) => (
              <div
                key={col.id ? `cat-${col.id}` : col.label}
                onClick={() => onNavigate('shop', col.id ? { categoryId: col.id } : {})}
                className="flex-none snap-start w-[260px] sm:w-[280px] md:w-[300px] lg:w-[calc(25%-18px)] aspect-[3/4] group cursor-pointer overflow-hidden relative bg-white shadow-sm hover:shadow-md transition-all duration-500 reveal-item"
              >
                <div
                  className="absolute inset-0 bg-cover bg-center transition-transform duration-1000 group-hover:scale-110"
                  style={{ backgroundImage: `url("${col.image}")` }}
                />
                <div className="absolute inset-0 bg-black/10 group-hover:bg-black/35 transition-colors duration-500" />
                <div className="absolute bottom-8 left-1/2 -translate-x-1/2 text-center text-white w-full px-4">
                  <h3 className="font-display-lg text-xl mb-2 drop-shadow-sm">{col.label}</h3>
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
                    <span className="text-[10px] font-bold uppercase tracking-widest border border-white px-4 py-2 opacity-0 group-hover:opacity-100 hover:bg-white hover:text-black transition-all duration-300">
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
                  <button className="bg-white text-primary hover:bg-black hover:text-white px-6 py-3 text-[10px] font-bold uppercase tracking-widest transition-all duration-300 cursor-pointer">
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
            className="border border-primary text-primary px-12 py-4 text-xs font-bold uppercase tracking-[0.2em] hover:bg-primary hover:text-white hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 cursor-pointer"
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
            onSubmit={(e) => {
              e.preventDefault();
              toast.success('Thank you for subscribing to The TanM Journal!');
              e.target.reset();
            }}
          >
            <input
              type="email"
              placeholder="Your email address"
              required
              className="flex-grow bg-transparent border-0 border-b border-primary/20 focus:border-primary focus:outline-none text-sm py-4 text-on-surface placeholder:text-outline"
            />
            <button
              type="submit"
              className="bg-primary text-white px-12 py-4 text-xs font-bold uppercase tracking-widest hover:bg-neutral-800 hover:shadow-lg transition-all duration-300 cursor-pointer"
            >
              Subscribe
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
