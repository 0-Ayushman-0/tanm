import React, { useState, useEffect } from 'react';
import { reviewApi } from '../api';

export default function ProductDetailsModal({
  product,
  onClose,
  onAddToCart,
  user
}) {
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState(null);
  const [newRating, setNewRating] = useState(5);
  const [newComment, setNewComment] = useState('');
  const [loadingReviews, setLoadingReviews] = useState(false);
  const [submittingReview, setSubmittingReview] = useState(false);

  useEffect(() => {
    if (product) {
      loadReviews();
    }
  }, [product]);

  const loadReviews = async () => {
    setLoadingReviews(true);
    try {
      const [revData, sumData] = await Promise.all([
        reviewApi.getByProduct(product.id, 0, 10),
        reviewApi.getSummary(product.id)
      ]);
      setReviews(revData.content || []);
      setSummary(sumData);
    } catch (err) {
      console.error('Failed to load reviews', err);
    } finally {
      setLoadingReviews(false);
    }
  };

  const handleHelpfulVote = async (reviewId) => {
    try {
      await reviewApi.toggleHelpful(reviewId);
      loadReviews();
    } catch (err) {
      alert(err.message || 'Already voted or failed to vote');
    }
  };

  const handleAddReview = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setSubmittingReview(true);
    try {
      await reviewApi.add(product.id, newRating, newComment);
      setNewComment('');
      setNewRating(5);
      alert('Review submitted successfully! It will appear once approved by moderation.');
      loadReviews();
    } catch (err) {
      alert(err.message || 'Failed to submit review. You must have a verified purchase order.');
    } finally {
      setSubmittingReview(false);
    }
  };

  if (!product) return null;

  // Sort product images so Primary image is always at index 0 (top hero spot)
  const rawImages = product.images && product.images.length > 0 ? product.images : [];
  const sortedImageObjects = [...rawImages].sort((a, b) => {
    if (Boolean(a.isPrimary) !== Boolean(b.isPrimary)) return a.isPrimary ? -1 : 1;
    return (a.displayOrder || 0) - (b.displayOrder || 0);
  });

  const images = sortedImageObjects.length > 0
    ? sortedImageObjects.map(img => img.imageUrl)
    : [product.mainImageUrl || 'https://via.placeholder.com/600x800'];

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-md z-[100] flex justify-center items-center p-4 overflow-y-auto">
      <div className="bg-surface w-full max-w-5xl rounded-lg shadow-2xl overflow-y-auto max-h-[95vh] border border-outline-variant/20 relative">
        
        {/* Close Button overlay */}
        <button
          onClick={onClose}
          className="absolute top-6 right-6 bg-surface/80 hover:bg-primary hover:text-surface text-on-surface p-2.5 rounded-full shadow z-50 flex items-center justify-center transition-all"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>

        <div className="p-6 md:p-12">
          {/* Product Section: Asymmetric Hero Grid */}
          <section className="grid grid-cols-1 md:grid-cols-12 gap-gutter items-start">
            
            {/* Left: Gallery Spread (8 Columns) */}
            <div className="md:col-span-7 flex flex-col gap-8">
              <div className="relative aspect-[4/5] w-full overflow-hidden bg-surface-container rounded">
                <img
                  src={images[0]}
                  alt={product.name}
                  className="object-cover w-full h-full transform hover:scale-105 transition-transform duration-700"
                />
              </div>
              
              <div className="grid grid-cols-2 gap-gutter">
                <div className="aspect-square bg-surface-container overflow-hidden rounded">
                  <img
                    src={images[1] || images[0]}
                    alt="Detail zoom"
                    className="object-cover w-full h-full hover:scale-105 transition-transform duration-500"
                  />
                </div>
                <div className="aspect-square bg-surface-container overflow-hidden rounded translate-y-4">
                  <img
                    src={images[2] || images[0]}
                    alt="Silhouette view"
                    className="object-cover w-full h-full hover:scale-105 transition-transform duration-500"
                  />
                </div>
              </div>
            </div>

            {/* Right: Info Panel (5 Columns) */}
            <div className="md:col-span-5 space-y-8 mt-12 md:mt-0 text-left">
              <div className="space-y-4">
                <div className="flex gap-2">
                  <span className="font-label-sm text-[10px] uppercase text-tertiary border border-outline-variant/30 px-3 py-1 font-bold">
                    {product.stockQuantity > 0 ? 'Limited Edition' : 'Sold Out'}
                  </span>
                </div>
                <h1 className="font-display-lg text-3xl md:text-4xl text-on-surface leading-none tracking-tighter font-bold">
                  {product.name}
                </h1>
                <p className="font-headline-md text-2xl text-primary font-bold">₹{product.price.toFixed(2)}</p>
              </div>

              <div className="space-y-6">
                <p className="font-body-lg text-sm text-on-surface-variant leading-relaxed">
                  {product.description || 'A definitive statement in artisanal utility. Sculpted from a single hide of full-grain Tuscan leather, this piece is designed to age with a unique patina that records your journey.'}
                </p>

                <ul className="space-y-3 pt-4 border-t border-outline-variant/20 text-xs">
                  <li className="flex justify-between items-center py-1">
                    <span className="font-label-sm text-on-surface-variant font-semibold">Material</span>
                    <span className="font-body-md text-on-surface">{product.leatherType || 'Full Grain Vachetta'}</span>
                  </li>
                </ul>
              </div>

              {/* Add to Bag action */}
              <div className="space-y-4 pt-6">
                <button
                  disabled={product.stockQuantity <= 0}
                  onClick={() => onAddToCart(product)}
                  className="w-full bg-primary text-surface py-5 px-8 font-label-sm text-xs uppercase tracking-widest hover:bg-primary-container disabled:bg-surface-dim transition-all duration-300 font-bold"
                >
                  {product.stockQuantity > 0 ? 'Add to Bag' : 'Out of Stock'}
                </button>
              </div>

              {/* Specs Accordions */}
              <div className="pt-6 border-t border-outline-variant/10 text-xs">
                <details className="group cursor-pointer">
                  <summary className="flex justify-between items-center list-none py-3 border-b border-outline-variant/20">
                    <span className="font-label-sm uppercase text-on-surface font-semibold">Dimensions &amp; Fit</span>
                    <span className="material-symbols-outlined group-open:rotate-180 transition-transform">expand_more</span>
                  </summary>
                  <div className="py-3 font-body-md text-on-surface-variant leading-relaxed">
                    {product.dimensions || '15"W x 13"H x 6"D. Handle drop: 9.5". Large enough for a 16-inch laptop and daily essentials. Includes one internal zip pocket and a dedicated phone sleeve.'}
                  </div>
                </details>
                

              </div>

              {/* Review Module inside details drawer */}
              <div className="border-t border-outline-variant/10 pt-6">
                <h3 className="font-headline-md text-lg text-primary mb-4 font-bold">Reviews</h3>

                {summary && (
                  <div className="bg-surface-container-low p-4 rounded mb-4 flex items-center gap-4 text-xs">
                    <div className="text-center shrink-0">
                      <p className="text-2xl font-bold text-primary">{summary.averageRating.toFixed(1)}</p>
                      <p className="text-[9px] text-on-surface-variant uppercase font-bold">{summary.totalReviews} Reviews</p>
                    </div>
                    <div className="flex-1 space-y-1">
                      {[5, 4, 3, 2, 1].map((stars) => {
                        const count = summary.starDistribution?.[stars] || 0;
                        const percentage = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
                        return (
                          <div key={stars} className="flex items-center gap-2">
                            <span className="w-3 text-[10px]">{stars}★</span>
                            <div className="flex-1 h-1.5 bg-surface-container rounded-full overflow-hidden">
                              <div className="h-full bg-primary" style={{ width: `${percentage}%` }}></div>
                            </div>
                            <span className="w-3 text-right text-[10px] text-on-surface-variant">{count}</span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {user && (
                  <form onSubmit={handleAddReview} className="bg-surface-container-low p-4 rounded mb-4 space-y-2">
                    <h4 className="font-semibold text-xs text-primary">Add a verified review</h4>
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] text-on-surface-variant">Stars:</span>
                      <select
                        value={newRating}
                        onChange={(e) => setNewRating(Number(e.target.value))}
                        className="bg-surface border border-outline/25 rounded p-1 text-[10px] text-on-surface"
                      >
                        {[5, 4, 3, 2, 1].map((r) => (
                          <option key={r} value={r}>{r} Star{r > 1 && 's'}</option>
                        ))}
                      </select>
                    </div>
                    <textarea
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      placeholder="Comment on leather grain, brass details..."
                      className="w-full bg-surface border border-outline/20 rounded p-2 text-[10px] focus:ring-1 focus:ring-primary text-on-surface"
                      rows="2"
                      required
                    ></textarea>
                    <button
                      type="submit"
                      disabled={submittingReview}
                      className="bg-primary text-surface px-4 py-1.5 rounded text-[9px] uppercase font-bold tracking-wider"
                    >
                      {submittingReview ? 'Submitting...' : 'Submit'}
                    </button>
                  </form>
                )}

                <div className="space-y-4 max-h-[250px] overflow-y-auto pr-2">
                  {loadingReviews ? (
                    <p className="text-center text-xs text-on-surface-variant">Loading reviews...</p>
                  ) : reviews.length === 0 ? (
                    <p className="text-center text-xs text-on-surface-variant">No reviews yet.</p>
                  ) : (
                    reviews.map((rev) => (
                      <div key={rev.id} className="border-b border-outline-variant/10 pb-3 text-xs">
                        <div className="flex justify-between items-center mb-0.5">
                          <span className="font-semibold">{rev.customerName}</span>
                          <span className="text-primary font-medium">{rev.rating}★</span>
                        </div>
                        <div className="flex items-center gap-2 text-[9px] text-on-surface-variant mb-1">
                          <span>{new Date(rev.createdAt).toLocaleDateString()}</span>
                          {rev.verifiedPurchase && (
                            <span className="bg-secondary-container/20 text-secondary px-1 py-0.2 rounded font-bold uppercase">
                              Verified
                            </span>
                          )}
                        </div>
                        <p className="text-on-surface-variant leading-relaxed mb-1">{rev.comment}</p>
                        <button
                          onClick={() => handleHelpfulVote(rev.id)}
                          className="text-[9px] text-primary hover:underline flex items-center gap-1 font-bold"
                        >
                          <span className="material-symbols-outlined text-[11px]">thumb_up</span>
                          Helpful ({rev.helpfulVotes || 0})
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>

          </section>

          {/* Contextual Craft Section */}
          <section className="mt-20 border-t border-outline-variant/20 pt-16 grid grid-cols-1 md:grid-cols-12 gap-gutter items-center text-left">
            <div className="md:col-span-5 space-y-6">
              <span className="font-label-sm text-xs uppercase tracking-[0.2em] text-outline font-bold">
                The Process
              </span>
              <h2 className="font-headline-lg text-2xl md:text-3xl text-on-surface leading-tight font-bold">
                Mastered over generations.
              </h2>
              <p className="font-body-lg text-sm text-on-surface-variant leading-relaxed">
                Every {product.name} is the result of 24 hours of dedicated handwork. From the saddle-stitching that ensures lifelong durability to the hand-painted edges, no detail is left to chance.
              </p>
            </div>
            <div className="md:col-span-6 md:col-start-7">
              <div className="relative overflow-hidden aspect-video bg-surface-container rounded shadow">
                <img
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuB3hFE9AUkd4C4-IqfrNCXyqcgov1h9PdZ8jngIQ4-8tpm_HExZKMxEtVLW_z05a862Kc1vujTpSE42isDWlp510YnXNhxkVQKm4PH3eIxHofBjc-a4dHeAegUuOR-9v8WWL2onG3vTGadZAg0NMGPhLSNsfN6CWkJ_6XVvQn5A1W-jfEecfAIa8_OT3O1PWZtpZmo1vE5FVTUTLi6qhVy2ctro2VW7HgY5d3OBW-Oit6b23vsEHJprAUcBuXJyem4qPwcMOFDQa84U"
                  alt="Craft workbench"
                  className="object-cover w-full h-full grayscale hover:grayscale-0 transition-all duration-1000"
                />
              </div>
            </div>
          </section>

        </div>
      </div>
    </div>
  );
}
