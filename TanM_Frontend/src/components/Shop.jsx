import React, { useEffect, useState } from 'react';
import { productApi } from '../api';

export default function Shop({ onSelectProduct }) {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters State
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedLeather, setSelectedLeather] = useState('');
  const [selectedColor, setSelectedColor] = useState('');
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [inStockOnly, setInStockOnly] = useState(false);
  
  // Sort State
  const [sortOption, setSortOption] = useState('name_asc'); // name_asc, price_asc, price_desc, date_desc

  // Pagination State
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadFilterMetadata();
  }, []);

  useEffect(() => {
    setPage(0);
    loadProducts(0);
  }, [selectedCategory, selectedLeather, selectedColor, priceRange.min, priceRange.max, inStockOnly, sortOption]);

  useEffect(() => {
    loadProducts(page);
  }, [page]);

  const loadFilterMetadata = async () => {
    try {
      const catsPage = await productApi.getCategories(0, 100);
      setCategories(catsPage.content || []);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const loadProducts = async (currentPage) => {
    setLoading(true);
    try {
      // Split sort option (e.g., price_asc -> price, asc)
      const [sortBy, direction] = sortOption.split('_');
      
      const params = {
        page: currentPage,
        size: 9,
        sortBy,
        direction,
        inStockOnly: inStockOnly || null
      };

      if (selectedCategory) params.categoryId = parseInt(selectedCategory, 10);
      if (selectedLeather) params.leatherType = selectedLeather;
      if (selectedColor) params.color = selectedColor;
      if (priceRange.min) params.minPrice = parseFloat(priceRange.min);
      if (priceRange.max) params.maxPrice = parseFloat(priceRange.max);

      const data = await productApi.filter(params);
      setProducts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Failed to load catalog', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilters = () => {
    setSelectedCategory('');
    setSelectedLeather('');
    setSelectedColor('');
    setPriceRange({ min: '', max: '' });
    setInStockOnly(false);
    setSortOption('name_asc');
  };

  return (
    <div className="max-w-container-max mx-auto px-edge-margin-mobile md:px-edge-margin-desktop py-12">
      {/* Title */}
      <header className="mb-16 text-left">
        <nav className="mb-4 text-xs font-semibold text-on-surface-variant/60 uppercase tracking-widest">
          <span>Home</span>
          <span className="mx-2">/</span>
          <span className="text-on-surface">Collections</span>
        </nav>
        <h1 className="font-display-lg text-4xl md:text-5xl font-bold tracking-tight text-primary">
          Atelier Collections
        </h1>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
        
        {/* Left Side: Filter Sidebar (3 columns) */}
        <aside className="lg:col-span-3 bg-surface-container p-8 border border-outline-variant/10 rounded sticky top-32 text-xs font-medium space-y-8 text-left">
          
          <div className="flex justify-between items-center border-b border-outline-variant/20 pb-4">
            <h2 className="font-label-sm uppercase tracking-widest font-bold text-primary text-[11px]">Filters</h2>
            <button
              onClick={handleClearFilters}
              className="text-on-surface-variant hover:text-primary transition-colors font-bold uppercase tracking-tighter"
            >
              Clear All
            </button>
          </div>

          {/* Category Filter */}
          <div className="space-y-2">
            <label className="block uppercase tracking-wider text-[10px] text-outline font-bold">Category</label>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full bg-surface border border-outline-variant/30 rounded p-2 text-on-surface focus:ring-1 focus:ring-primary focus:border-primary"
            >
              <option value="">All Categories</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          {/* Leather Type Filter */}
          <div className="space-y-2">
            <label className="block uppercase tracking-wider text-[10px] text-outline font-bold">Leather Hide</label>
            <select
              value={selectedLeather}
              onChange={(e) => setSelectedLeather(e.target.value)}
              className="w-full bg-surface border border-outline-variant/30 rounded p-2 text-on-surface focus:ring-1 focus:ring-primary"
            >
              <option value="">All Hides</option>
              <option value="Full Grain Vachetta">Full-Grain Vachetta</option>
              <option value="Top Grain Saffiano">Top-Grain Saffiano</option>
              <option value="Pebbled Calfskin">Pebbled Calfskin</option>
              <option value="Suede">Suede Leather</option>
            </select>
          </div>

          {/* Color Filter */}
          <div className="space-y-2">
            <label className="block uppercase tracking-wider text-[10px] text-outline font-bold">Color Palette</label>
            <select
              value={selectedColor}
              onChange={(e) => setSelectedColor(e.target.value)}
              className="w-full bg-surface border border-outline-variant/30 rounded p-2 text-on-surface focus:ring-1 focus:ring-primary"
            >
              <option value="">All Colors</option>
              <option value="Tan">Tuscan Tan</option>
              <option value="Dark Brown">Cognac Dark Brown</option>
              <option value="Black">Obsidian Black</option>
              <option value="Espresso">Espresso</option>
            </select>
          </div>

          {/* Price Range Filter */}
          <div className="space-y-2">
            <label className="block uppercase tracking-wider text-[10px] text-outline font-bold">Price Range</label>
            <div className="flex gap-2">
              <input
                type="number"
                value={priceRange.min}
                onChange={(e) => setPriceRange({ ...priceRange, min: e.target.value })}
                placeholder="Min Price"
                className="w-1/2 bg-surface border border-outline-variant/30 rounded p-2 text-on-surface focus:ring-1 focus:ring-primary text-center"
              />
              <input
                type="number"
                value={priceRange.max}
                onChange={(e) => setPriceRange({ ...priceRange, max: e.target.value })}
                placeholder="Max Price"
                className="w-1/2 bg-surface border border-outline-variant/30 rounded p-2 text-on-surface focus:ring-1 focus:ring-primary text-center"
              />
            </div>
          </div>

          {/* Stock Filter */}
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="inStockCheck"
              checked={inStockOnly}
              onChange={(e) => setInStockOnly(e.target.checked)}
              className="text-primary focus:ring-primary border-outline-variant/30 h-4 w-4 rounded"
            />
            <label htmlFor="inStockCheck" className="font-semibold text-on-surface-variant cursor-pointer">
              Show In-Stock Only
            </label>
          </div>

        </aside>

        {/* Right Side: Sorting & Product Grid (9 columns) */}
        <section className="lg:col-span-9 space-y-8">
          
          {/* Sorting & Result Counts Bar */}
          <div className="flex justify-between items-center text-xs font-semibold bg-surface-container-low px-6 py-4 rounded border border-outline-variant/10">
            <span className="text-on-surface-variant/80 font-bold uppercase tracking-wider">
              {products.length} Products Found
            </span>
            <div className="flex items-center gap-2">
              <span className="text-outline uppercase text-[10px] tracking-wide font-bold">Sort By</span>
              <select
                value={sortOption}
                onChange={(e) => setSortOption(e.target.value)}
                className="bg-surface border border-outline-variant/30 rounded px-2 py-1 text-on-surface focus:ring-1 focus:ring-primary font-bold"
              >
                <option value="name_asc">Alphabetical (A-Z)</option>
                <option value="price_asc">Price (Low to High)</option>
                <option value="price_desc">Price (High to Low)</option>
                <option value="createdAt_desc">Release Date (Newest)</option>
              </select>
            </div>
          </div>

          {/* Catalog grid */}
          {loading ? (
            <div className="flex justify-center items-center py-48">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-48 bg-surface-container rounded border border-dashed border-outline-variant/30 text-on-surface-variant font-medium">
              <span className="material-symbols-outlined text-[48px] mb-4 text-primary">inventory</span>
              <p>No products match your active search filters.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-gutter">
              {products.map((p) => (
                <article
                  key={p.id}
                  onClick={() => onSelectProduct(p)}
                  className="group cursor-pointer flex flex-col gap-6 text-left"
                >
                  <div className="aspect-[4/5] overflow-hidden bg-surface-container rounded shadow">
                    <img
                      src={p.mainImageUrl || 'https://via.placeholder.com/300x400'}
                      alt={p.name}
                      className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                    />
                  </div>
                  <div className="space-y-1">
                    <span className="font-label-sm text-[10px] uppercase text-outline font-bold tracking-widest">
                      {p.leatherType || 'Full Grain Vachetta'}
                    </span>
                    <h3 className="font-headline-md text-xl font-bold group-hover:text-primary transition-colors leading-tight">
                      {p.name}
                    </h3>
                    <p className="font-body-md text-sm text-on-surface-variant font-semibold">
                      ₹{p.price.toFixed(2)}
                    </p>
                  </div>
                </article>
              ))}
            </div>
          )}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 pt-12 border-t border-outline-variant/10 text-xs font-semibold">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="w-10 h-10 border border-outline-variant/30 flex items-center justify-center hover:bg-surface-variant transition-colors disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-[20px]">chevron_left</span>
              </button>
              
              {Array.from({ length: totalPages }).map((_, idx) => (
                <button
                  key={idx}
                  onClick={() => setPage(idx)}
                  className={`w-10 h-10 flex items-center justify-center font-bold transition-colors ${
                    page === idx
                      ? 'bg-primary text-surface'
                      : 'border border-outline-variant/30 hover:bg-surface-variant'
                  }`}
                >
                  {idx + 1}
                </button>
              ))}

              <button
                disabled={page === totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="w-10 h-10 border border-outline-variant/30 flex items-center justify-center hover:bg-surface-variant transition-colors disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-[20px]">chevron_right</span>
              </button>
            </div>
          )}

        </section>

      </div>
    </div>
  );
}
