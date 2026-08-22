import React, { useState, useEffect } from 'react';
import { adminApi, productApi, orderApi, categoryApi, uploadApi } from '../api';

export default function AdminPortal() {
  const [activeTab, setActiveTab] = useState('dashboard'); // dashboard, products, categories, orders, inventory, moderation, cms
  
  // Analytics State
  const [summary, setSummary] = useState(null);
  const [revenueTrends, setRevenueTrends] = useState([]);
  const [orderTrends, setOrderTrends] = useState([]);
  const [topProducts, setTopProducts] = useState([]);
  const [inventoryAlerts, setInventoryAlerts] = useState([]);
  const [loadingAnalytics, setLoadingAnalytics] = useState(false);

  // Categories State
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null); // null, 'new', or category object
  const [categoryForm, setCategoryForm] = useState({
    name: '',
    description: '',
    imageUrl: '',
    displayOrder: 0,
  });

  // Product Manager State
  const [editingProduct, setEditingProduct] = useState(null); // null, 'new', or product object
  const [productForm, setProductForm] = useState({
    name: '',
    sku: '',
    shortDescription: '',
    description: '',
    price: '',
    stockQuantity: 0,
    leatherType: '',
    color: '',
    dimensions: '',
    isFeatured: false,
    status: 'PUBLISHED',
    categoryId: '',
  });
  const [newProductImageUrl, setNewProductImageUrl] = useState('');
  const [newProductImages, setNewProductImages] = useState([]);
  const [isPrimaryImage, setIsPrimaryImage] = useState(false);
  const [productFormError, setProductFormError] = useState('');
  const [categoryFormError, setCategoryFormError] = useState('');
  const [uploadingImage, setUploadingImage] = useState(false);

  // Orders State
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [ordersPage, setOrdersPage] = useState(0);
  const [ordersTotalPages, setOrdersTotalPages] = useState(0);
  const [selectedAdminOrder, setSelectedAdminOrder] = useState(null);

  // Inventory State
  const [products, setProducts] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [newQty, setNewQty] = useState('');
  const [adjType, setAdjType] = useState('RESTOCK');
  const [adjReason, setAdjReason] = useState('');
  const [adjusting, setAdjusting] = useState(false);

  // Moderation State
  const [reviewsQueue, setReviewsQueue] = useState([]);
  const [loadingModeration, setLoadingModeration] = useState(false);

  // CMS Studio State
  const [slides, setSlides] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [editingSlide, setEditingSlide] = useState(null);
  const [slideForm, setSlideForm] = useState({
    title: '',
    imageUrl: '',
    buttonText: 'Collections',
    isActive: true,
    sortOrder: 1
  });
  const [annForm, setAnnForm] = useState({
    id: null,
    content: '',
    isActive: true
  });
  const [savingCms, setSavingCms] = useState(false);

  useEffect(() => {
    if (activeTab === 'dashboard') loadAnalytics();
    if (activeTab === 'products') {
      loadProducts();
      loadCategories();
    }
    if (activeTab === 'categories') loadCategories();
    if (activeTab === 'orders') loadOrders();
    if (activeTab === 'inventory') {
      loadProducts();
      loadAuditLogs();
    }
    if (activeTab === 'moderation') loadReviewsQueue();
    if (activeTab === 'cms') loadCmsData();
  }, [activeTab, ordersPage]);

  const loadAnalytics = async () => {
    setLoadingAnalytics(true);
    try {
      const [sum, rev, ord, top, alerts] = await Promise.all([
        adminApi.getDashboardSummary(),
        adminApi.getRevenueTrends(),
        adminApi.getOrderTrends(),
        adminApi.getTopProducts(5),
        adminApi.getInventoryAlerts()
      ]);
      setSummary(sum);
      setRevenueTrends(rev || []);
      setOrderTrends(ord || []);
      setTopProducts(top || []);
      setInventoryAlerts(alerts || []);
    } catch (err) {
      console.error('Analytics load error', err);
    } finally {
      setLoadingAnalytics(false);
    }
  };

  const loadOrders = async () => {
    setLoadingOrders(true);
    try {
      const data = await adminApi.getAllOrders(ordersPage, 10);
      setOrders(data.content || []);
      setOrdersTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Orders load error', err);
    } finally {
      setLoadingOrders(false);
    }
  };

  // Product Catalog Filters in Admin
  const [adminStatusFilter, setAdminStatusFilter] = useState('ALL');
  const [adminCategoryFilter, setAdminCategoryFilter] = useState('ALL');
  const [adminSearchQuery, setAdminSearchQuery] = useState('');

  const loadProducts = async () => {
    try {
      const page = await productApi.getAll(0, 100);
      setProducts(page.content || []);
    } catch (err) {
      console.error('Products load error', err);
    }
  };

  const loadCategories = async () => {
    setLoadingCategories(true);
    try {
      const data = await categoryApi.getAll();
      setCategories(Array.isArray(data) ? data : (data?.content || []));
    } catch (err) {
      console.error('Categories load error', err);
      setCategories([]);
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleSaveCategory = async (e) => {
    e.preventDefault();
    setCategoryFormError('');
    try {
      const payload = {
        name: categoryForm.name,
        description: categoryForm.description,
        imageUrl: categoryForm.imageUrl,
        displayOrder: parseInt(categoryForm.displayOrder || 0, 10),
      };
      if (editingCategory && editingCategory.id) {
        await categoryApi.update(editingCategory.id, payload);
        alert('Category updated successfully!');
      } else {
        await categoryApi.create(payload);
        alert('Category created successfully!');
      }
      setEditingCategory(null);
      setCategoryFormError('');
      loadCategories();
    } catch (err) {
      setCategoryFormError(err.message || 'Failed to save category');
    }
  };

  const handleDeleteCategory = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category?')) return;
    try {
      await categoryApi.remove(id);
      alert('Category deleted successfully!');
      loadCategories();
    } catch (err) {
      alert(err.message || 'Failed to delete category');
    }
  };

  const handleFileUpload = async (file, folder, onSuccess) => {
    if (!file) return;
    setUploadingImage(true);
    try {
      const result = await uploadApi.uploadImage(file, folder);
      onSuccess(result.url);
    } catch (err) {
      alert(err.message || 'Failed to upload image to Cloudinary');
    } finally {
      setUploadingImage(false);
    }
  };

  const handleBatchFileUpload = async (files, folder, onSuccess) => {
    if (!files || files.length === 0) return;
    setUploadingImage(true);
    try {
      const results = await uploadApi.uploadImages(files, folder);
      onSuccess(results);
    } catch (err) {
      alert(err.message || 'Failed to upload batch images to Cloudinary');
    } finally {
      setUploadingImage(false);
    }
  };

  const handleOpenNewProduct = () => {
    setEditingProduct('new');
    setProductFormError('');
    setProductForm({
      name: '',
      sku: 'TANM-' + Math.floor(1000 + Math.random() * 9000),
      shortDescription: '',
      description: '',
      price: '',
      stockQuantity: 10,
      leatherType: 'Full-Grain Vachetta Leather',
      color: 'Chocolate Brown',
      dimensions: '30cm x 40cm x 10cm',
      isFeatured: false,
      status: 'PUBLISHED',
      categoryId: categories[0]?.id || '',
    });
    setNewProductImageUrl('');
  };

  const handleEditProduct = (prod) => {
    setEditingProduct(prod);
    setProductFormError('');
    setProductForm({
      name: prod.name || '',
      sku: prod.sku || '',
      shortDescription: prod.shortDescription || '',
      description: prod.description || '',
      price: prod.price || '',
      stockQuantity: prod.stockQuantity || 0,
      leatherType: prod.leatherType || '',
      color: prod.color || '',
      dimensions: prod.dimensions || '',
      isFeatured: Boolean(prod.isFeatured || prod.featured),
      status: prod.status || 'PUBLISHED',
      categoryId: prod.category?.id || prod.categoryId || '',
    });
    setNewProductImageUrl('');
  };

  const handleSaveProduct = async (e) => {
    e.preventDefault();
    setProductFormError('');
    const catId = parseInt(productForm.categoryId, 10);
    if (!catId || isNaN(catId)) {
      setProductFormError('Please select a valid category for the product.');
      return;
    }
    try {
      const payload = {
        name: productForm.name,
        sku: productForm.sku,
        shortDescription: productForm.shortDescription,
        description: productForm.description,
        price: parseFloat(productForm.price),
        stockQuantity: parseInt(productForm.stockQuantity, 10),
        leatherType: productForm.leatherType,
        color: productForm.color,
        dimensions: productForm.dimensions,
        isFeatured: Boolean(productForm.isFeatured),
        status: productForm.status,
        categoryId: catId,
      };
      if (editingProduct && editingProduct.id) {
        await productApi.update(editingProduct.id, payload);
        alert('Product updated successfully!');
      } else {
        const created = await productApi.create(payload);
        alert('Product created successfully!');
        
        // Process bulk image requests
        const bulkRequests = newProductImages.map((img, idx) => ({
          imageUrl: img.imageUrl,
          publicId: img.publicId,
          isPrimary: img.isPrimary || idx === 0,
          displayOrder: (idx + 1) * 10,
        }));

        if (bulkRequests.length > 0 && created && created.id) {
          await productApi.addImagesBulk(created.id, bulkRequests);
        } else if (newProductImageUrl && created && created.id) {
          await productApi.addImage(created.id, newProductImageUrl, true);
        }
      }
      setEditingProduct(null);
      setProductFormError('');
      setNewProductImageUrl('');
      setNewProductImages([]);
      loadProducts();
    } catch (err) {
      setProductFormError(err.message || 'Failed to save product');
    }
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await productApi.remove(id);
      alert('Product deleted!');
      loadProducts();
    } catch (err) {
      alert(err.message || 'Failed to delete product');
    }
  };

  const handleAddProductImage = async (productId) => {
    if (!newProductImageUrl) return;
    try {
      await productApi.addImage(productId, newProductImageUrl, isPrimaryImage);
      alert('Image added to product!');
      setNewProductImageUrl('');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      alert(err.message || 'Failed to add image');
    }
  };

  const handleDeleteProductImage = async (productId, imageId) => {
    if (!window.confirm('Delete image?')) return;
    try {
      await productApi.deleteImage(productId, imageId);
      alert('Image deleted!');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      alert(err.message || 'Failed to delete image');
    }
  };

  const handleSetPrimaryProductImage = async (productId, imageId) => {
    try {
      await productApi.setPrimaryImage(productId, imageId);
      alert('Primary image set!');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      alert(err.message || 'Failed to set primary image');
    }
  };

  const loadAuditLogs = async () => {
    try {
      const page = await adminApi.getInventoryAuditLogs(0, 20);
      setAuditLogs(page.content || []);
    } catch (err) {
      console.error('Audit logs load error', err);
    }
  };

  const handleAdjustStock = async (e) => {
    e.preventDefault();
    if (!selectedProduct || newQty === '') return;
    setAdjusting(true);
    try {
      await adminApi.adjustStock(
        selectedProduct.id,
        parseInt(newQty, 10),
        adjType,
        adjReason || 'Manual Admin Override'
      );
      alert('Stock successfully adjusted and audit log recorded!');
      setNewQty('');
      setAdjReason('');
      setSelectedProduct(null);
      loadProducts();
      loadAuditLogs();
    } catch (err) {
      alert(err.message || 'Failed to adjust stock');
    } finally {
      setAdjusting(false);
    }
  };

  const loadReviewsQueue = async () => {
    setLoadingModeration(true);
    try {
      const page = await adminApi.getReviewsQueue(0, 50);
      setReviewsQueue(page.content || []);
    } catch (err) {
      console.error('Moderation queue error', err);
    } finally {
      setLoadingModeration(false);
    }
  };

  const handleModerateReview = async (reviewId, status) => {
    try {
      await adminApi.updateReviewStatus(reviewId, status);
      alert(`Review has been successfully marked as ${status}`);
      loadReviewsQueue();
    } catch (err) {
      alert(err.message || 'Failed to update review status');
    }
  };

  const handleInspectOrder = async (ord) => {
    try {
      const fullOrder = await adminApi.getOrderByNumber(ord.orderNumber);
      setSelectedAdminOrder(fullOrder || ord);
    } catch (err) {
      setSelectedAdminOrder(ord);
    }
  };

  const handleUpdateOrderStatus = async (orderNumber, status) => {
    try {
      const updated = await adminApi.updateOrderStatus(orderNumber, status);
      alert(`Order status updated to ${status}`);
      if (selectedAdminOrder && selectedAdminOrder.orderNumber === orderNumber) {
        setSelectedAdminOrder(updated);
      }
      loadOrders();
    } catch (err) {
      alert(err.message || 'Failed to update order status');
    }
  };

  // CMS Studio CRUD Actions
  const loadCmsData = async () => {
    try {
      const [slidesPage, annPage] = await Promise.all([
        adminApi.getCmsHeroSlides(),
        adminApi.getCmsAnnouncements()
      ]);
      setSlides(slidesPage.content || []);
      const annList = annPage.content || [];
      setAnnouncements(annList);
      if (annList.length > 0) {
        setAnnForm({
          id: annList[0].id,
          content: annList[0].content,
          isActive: annList[0].isActive
        });
      }
    } catch (err) {
      console.error('CMS load failed', err);
    }
  };

  const handleSaveAnnouncement = async (e) => {
    e.preventDefault();
    setSavingCms(true);
    try {
      if (annForm.id) {
        await adminApi.updateCmsAnnouncement(annForm.id, annForm);
      } else {
        const created = await adminApi.createCmsAnnouncement(annForm);
        setAnnForm(created);
      }
      alert('Store announcement saved successfully! Clear site cache to verify.');
      loadCmsData();
    } catch (err) {
      alert(err.message || 'Failed to save announcement bar');
    } finally {
      setSavingCms(false);
    }
  };

  const handleSaveHeroSlide = async (e) => {
    e.preventDefault();
    setSavingCms(true);

    const dto = {
      title: slideForm.title,
      subtitle: slideForm.subtitle || '',
      description: slideForm.description || '',
      primaryCtaText: slideForm.buttonText,
      primaryCtaUrl: '/shop',
      sortOrder: slideForm.sortOrder,
      status: slideForm.isActive ? 'PUBLISHED' : 'DRAFT',
      backgroundImage: {
        url: slideForm.imageUrl
      }
    };

    try {
      if (editingSlide) {
        await adminApi.updateCmsHeroSlide(editingSlide.id, dto);
      } else {
        await adminApi.createCmsHeroSlide(dto);
      }
      alert('Hero slide configurations saved successfully!');
      setEditingSlide(null);
      setSlideForm({
        title: '',
        imageUrl: '',
        buttonText: 'Collections',
        isActive: true,
        sortOrder: 1
      });
      loadCmsData();
    } catch (err) {
      alert(err.message || 'Failed to save slide');
    } finally {
      setSavingCms(false);
    }
  };

  const handleDeleteHeroSlide = async (id) => {
    if (!window.confirm('Delete this hero slide?')) return;
    try {
      await adminApi.deleteCmsHeroSlide(id);
      alert('Slide deleted successfully!');
      loadCmsData();
    } catch (err) {
      alert(err.message || 'Failed to delete slide');
    }
  };

  return (
    <div className="flex bg-background text-on-surface min-h-screen">
      
      {/* Left Sidebar Navigation Shell */}
      <aside className="h-screen w-72 fixed left-0 top-0 overflow-y-auto bg-surface border-r border-outline-variant/20 z-50 py-8 px-6 flex flex-col justify-between">
        <div className="space-y-8 text-left">
          {/* Brand Header */}
          <div className="space-y-1">
            <h1 className="font-display-lg text-2xl text-primary tracking-tight font-bold">Artisan Admin</h1>
            <p className="font-label-sm text-[10px] text-on-surface-variant uppercase tracking-widest opacity-70">
              Luxury Leather Goods
            </p>
          </div>

          {/* Quick Action CTA */}
          <button
            onClick={() => {
              setActiveTab('products');
              handleOpenNewProduct();
            }}
            className="w-full bg-primary text-surface py-5 px-6 font-label-sm text-[10px] uppercase tracking-[0.2em] transition-all hover:bg-primary-container font-bold"
          >
            + New Product
          </button>

          {/* Nav Links */}
          <nav className="space-y-2">
            {[
              { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
              { id: 'products', label: 'Products', icon: 'category' },
              { id: 'categories', label: 'Categories', icon: 'folder' },
              { id: 'orders', label: 'Orders', icon: 'shopping_bag' },
              { id: 'inventory', label: 'Inventory', icon: 'inventory_2' },
              { id: 'moderation', label: 'Moderation', icon: 'reviews' },
              { id: 'cms', label: 'CMS Studio', icon: 'edit_note' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id);
                  setEditingSlide(null);
                }}
                className={`w-full flex items-center gap-4 py-3 px-4 transition-all duration-300 rounded ${
                  activeTab === tab.id
                    ? 'text-primary bg-secondary-container/20 font-bold border-r-2 border-primary'
                    : 'text-on-surface-variant hover:text-primary hover:bg-secondary-container/10'
                }`}
              >
                <span className="material-symbols-outlined text-[20px]">{tab.icon}</span>
                <span className="font-label-sm text-xs uppercase tracking-wider">{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Footer actions */}
        <div className="border-t border-outline-variant/30 pt-6 space-y-2 text-xs text-left">
          <a
            href="mailto:support@tanm.com"
            className="flex items-center gap-4 py-3 px-4 text-on-surface-variant hover:text-primary transition-colors"
          >
            <span className="material-symbols-outlined text-[20px]">help</span>
            <span className="font-label-sm uppercase tracking-wider font-bold">Support</span>
          </a>
          <button
            onClick={() => window.location.reload()}
            className="w-full flex items-center gap-4 py-3 px-4 text-on-surface-variant hover:text-primary transition-colors text-left"
          >
            <span className="material-symbols-outlined text-[20px]">logout</span>
            <span className="font-label-sm uppercase tracking-wider font-bold">Exit Suite</span>
          </button>
        </div>
      </aside>

      {/* Top Header Shell */}
      <header className="fixed top-0 right-0 w-[calc(100%-18rem)] h-20 bg-surface/80 backdrop-blur-xl z-40 border-b border-outline-variant/30 flex justify-between items-center px-12 ml-72">
        <div className="flex items-center gap-4 group flex-grow max-w-md">
          <span className="material-symbols-outlined text-outline">search</span>
          <input
            className="bg-transparent border-none focus:ring-0 w-full font-body-md text-on-surface-variant placeholder:text-outline/50 text-sm"
            placeholder="Search analytics, logs or reviews..."
            type="text"
          />
        </div>
        <div className="flex items-center gap-8">
          <span className="font-display-lg text-xl text-primary tracking-tight font-bold">Back-Office</span>
          <div className="h-8 w-[1px] bg-outline-variant/30"></div>
          <div className="flex items-center gap-3">
            <div className="text-right text-xs">
              <p className="font-label-sm text-primary font-bold">Julian Thorne</p>
              <p className="text-[10px] text-on-surface-variant uppercase tracking-tighter">Master Artisan</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-surface-container overflow-hidden">
              <img
                className="w-full h-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuD1YJlVKhp_-r0_-eVdwCxC2L5lFvmsxO1LfMls6CGY2DE06uUMvRTszk-BNY0mYbVY8Whz9i90zJGWdSjpSUKzPIc1hqcCmYVc4vDMOnorVycYQwdHyJMM8bIJ6G_kmFxX0Z-GKe3lbbW0G1_aq_iw1qOPJ3V3UIzRk6Rnj9-QLc2bJJaOHDIeQGoaVqqAAx6Wr_wDlpWvBYlE6MnlnFsJWd-RvvNQ-Z96u4Hmel_9egdfe1inaPEAv7ehi9WgR66ROP79Ug0Q5IJU"
                alt="Julian Thorne avatar"
              />
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Canvas */}
      <main className="ml-72 pt-20 flex-grow min-h-screen text-left">
        <div className="px-12 py-12 max-w-[1440px] mx-auto">
          
          {/* TAB 1: EXECUTIVE ANALYTICS DASHBOARD */}
          {activeTab === 'dashboard' && (
            <div className="space-y-12">
              <section className="mb-12">
                <h2 className="font-display-lg text-4xl md:text-5xl text-primary tracking-tight mb-2 font-bold">
                  Executive Overview
                </h2>
                <p className="font-body-lg text-sm text-on-surface-variant opacity-70">
                  Strategic insights for the current craftsmanship cycle.
                </p>
              </section>

              {loadingAnalytics ? (
                <div className="flex justify-center items-center py-24">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
              ) : (
                <>
                  {/* KPI Cards Grid */}
                  <section className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div className="bg-surface-container-low p-10 flex flex-col justify-between border-b border-outline/10 h-60 relative overflow-hidden group rounded">
                      <div className="z-10">
                        <p className="font-label-sm text-xs uppercase tracking-[0.2em] text-on-surface-variant mb-4">
                          Total Revenue
                        </p>
                        <h3 className="font-display-lg text-4xl font-bold text-primary">
                          ₹{summary?.totalRevenue?.toFixed(2) || '0.00'}
                        </h3>
                      </div>
                      <div className="flex items-center gap-2 text-primary font-bold z-10 text-xs">
                        <span className="material-symbols-outlined">trending_up</span>
                        <span className="font-label-sm">+12.5% vs LW</span>
                      </div>
                      <div className="absolute -right-4 -bottom-4 opacity-5 pointer-events-none group-hover:scale-110 transition-transform duration-700">
                        <span className="material-symbols-outlined text-[120px]">payments</span>
                      </div>
                    </div>

                    <div className="bg-surface-container-low p-10 flex flex-col justify-between border-b border-outline/10 h-60 relative overflow-hidden group rounded">
                      <div className="z-10">
                        <p className="font-label-sm text-xs uppercase tracking-[0.2em] text-on-surface-variant mb-4">
                          Active Orders
                        </p>
                        <h3 className="font-display-lg text-4xl font-bold text-primary">
                          {summary?.totalOrders || 0}
                        </h3>
                      </div>
                      <div className="flex items-center gap-2 text-primary font-bold z-10 text-xs">
                        <span className="material-symbols-outlined">schedule</span>
                        <span className="font-label-sm">Live System Deductions</span>
                      </div>
                      <div className="absolute -right-4 -bottom-4 opacity-5 pointer-events-none group-hover:scale-110 transition-transform duration-700">
                        <span className="material-symbols-outlined text-[120px]">shopping_cart</span>
                      </div>
                    </div>

                    <div className="bg-surface-container-low p-10 flex flex-col justify-between border-b border-outline/10 h-60 relative overflow-hidden group rounded">
                      <div className="z-10">
                        <p className="font-label-sm text-xs uppercase tracking-[0.2em] text-on-surface-variant mb-4">
                          Active Catalog Items
                        </p>
                        <h3 className="font-display-lg text-4xl font-bold text-primary">
                          {summary?.activeProductsCount || 0}
                        </h3>
                      </div>
                      <div className="flex items-center gap-2 text-primary font-bold z-10 text-xs">
                        <span className="material-symbols-outlined">check_circle</span>
                        <span className="font-label-sm">Workshop Peak Perf</span>
                      </div>
                      <div className="absolute -right-4 -bottom-4 opacity-5 pointer-events-none group-hover:scale-110 transition-transform duration-700">
                        <span className="material-symbols-outlined text-[120px]">precision_manufacturing</span>
                      </div>
                    </div>
                  </section>

                  {/* Charts & Top Products */}
                  <div className="grid grid-cols-12 gap-gutter items-start">
                    {/* Simulated SVG line chart */}
                    <div className="col-span-12 lg:col-span-8 bg-surface-container p-10 h-[500px] flex flex-col rounded">
                      <div className="flex justify-between items-center mb-8 text-xs">
                        <div>
                          <h4 className="font-headline-md text-xl text-on-surface mb-2 font-bold">
                            Monthly Performance
                          </h4>
                          <p className="font-label-sm uppercase tracking-widest text-on-surface-variant">
                            Annual Revenue Growth Trend
                          </p>
                        </div>
                        <div className="flex gap-4">
                          <span className="flex items-center gap-2 font-label-sm">
                            <span className="w-3 h-3 bg-primary rounded-full"></span> This Year
                          </span>
                        </div>
                      </div>

                      <div className="flex-grow flex items-end justify-between gap-4 relative">
                        <div className="absolute inset-0 flex flex-col justify-between pointer-events-none opacity-10">
                          {[1, 2, 3, 4, 5].map((idx) => (
                            <div key={idx} className="border-b border-on-surface w-full"></div>
                          ))}
                        </div>
                        <div className="w-full h-full relative z-10">
                          <svg className="w-full h-[90%] drop-shadow-lg" preserveAspectRatio="none" viewBox="0 0 800 300">
                            <path d="M0,250 Q100,220 200,230 T400,100 T600,80 T800,40" fill="none" stroke="#71472f" strokeWidth="3"></path>
                            <path className="fill-primary/5" d="M0,250 Q100,220 200,230 T400,100 T600,80 T800,40 V300 H0 Z"></path>
                          </svg>
                          <div className="flex justify-between mt-4 font-label-sm text-[10px] text-on-surface-variant opacity-60">
                            <span>JAN</span><span>MAR</span><span>MAY</span><span>JUL</span><span>SEP</span><span>NOV</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Top Products */}
                    <div className="col-span-12 lg:col-span-4 space-y-6">
                      <h4 className="font-headline-md text-xl text-on-surface font-bold">Top Pieces</h4>
                      <div className="space-y-4 max-h-[420px] overflow-y-auto pr-2">
                        {topProducts.map((p) => (
                          <div key={p.productId} className="flex items-center gap-4 group cursor-pointer border-b border-outline-variant/30 pb-4">
                            <div className="w-16 h-16 bg-surface-container flex-shrink-0 overflow-hidden rounded">
                              <img
                                className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                                src={p.productImageUrl || 'https://via.placeholder.com/150'}
                                alt={p.productName}
                              />
                            </div>
                            <div className="flex-grow text-xs">
                              <h5 className="font-label-sm uppercase tracking-wider mb-1 font-bold">{p.productName}</h5>
                              <p className="text-on-surface-variant text-[10px] mb-1">{p.productSku}</p>
                              <div className="flex justify-between items-center font-semibold">
                                <span className="text-primary">{p.totalQuantitySold} Units Sold</span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Workshop Insights */}
                  <section className="border-t border-outline-variant pt-12">
                    <div className="flex flex-col md:flex-row justify-between items-start gap-12 text-xs">
                      <div className="md:w-1/3">
                        <h4 className="font-headline-lg text-2xl text-primary mb-4 font-bold">Workshop Insights</h4>
                        <p className="font-body-lg text-sm text-on-surface-variant mb-6 leading-relaxed">
                          Our master artisans are currently prioritizing the "Autumn Collection." Capacity is at 88%, allowing for bespoke commissions to resume.
                        </p>
                        <div className="flex items-center gap-3 text-primary">
                          <span className="material-symbols-outlined">construction</span>
                          <span className="font-label-sm uppercase tracking-widest font-bold">Live Workshop Feed</span>
                        </div>
                      </div>
                      <div className="md:w-2/3 grid grid-cols-2 gap-8 text-xs font-medium">
                        <div className="bg-surface-container-highest p-8 rounded">
                          <div className="text-[10px] font-label-sm uppercase tracking-[0.3em] text-on-surface-variant mb-4 font-bold">
                            Material Usage
                          </div>
                          <div className="flex items-end gap-2 mb-2">
                            <span className="font-display-lg text-3xl font-bold text-primary">64%</span>
                            <span className="font-label-sm text-on-surface-variant pb-1">Efficiency</span>
                          </div>
                          <div className="w-full h-1 bg-outline-variant/30 rounded-full overflow-hidden">
                            <div className="h-full bg-primary w-[64%]"></div>
                          </div>
                        </div>
                        <div className="bg-surface-container-highest p-8 rounded">
                          <div className="text-[10px] font-label-sm uppercase tracking-[0.3em] text-on-surface-variant mb-4 font-bold">
                            Lead Time
                          </div>
                          <div className="flex items-end gap-2 mb-2">
                            <span className="font-display-lg text-3xl font-bold text-primary">14</span>
                            <span className="font-label-sm text-on-surface-variant pb-1">Days Avg</span>
                          </div>
                          <div className="w-full h-1 bg-outline-variant/30 rounded-full overflow-hidden">
                            <div className="h-full bg-primary w-[85%]"></div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </section>
                </>
              )}
            </div>
          )}

          {/* TAB: PRODUCTS MANAGER */}
          {activeTab === 'products' && (
            <div className="space-y-12">
              <div className="flex justify-between items-center">
                <div>
                  <h1 className="font-display-lg text-4xl md:text-5xl text-primary font-bold">
                    Product Catalog
                  </h1>
                  <p className="text-sm text-on-surface-variant italic mt-1">
                    Manage handcrafted leather products, images, specifications, and pricing.
                  </p>
                </div>
                {!editingProduct && (
                  <button
                    onClick={handleOpenNewProduct}
                    className="bg-primary text-surface px-6 py-3 font-label-sm text-xs uppercase tracking-widest font-bold hover:bg-primary-container transition-all"
                  >
                    + Create Product
                  </button>
                )}
              </div>

              {/* PRODUCT CREATE / EDIT FORM */}
              {editingProduct && (
                <div className="bg-surface-container-low p-8 border border-outline-variant/30 rounded space-y-8">
                  <div className="flex justify-between items-center border-b border-outline-variant/20 pb-4">
                    <h3 className="font-display-lg text-2xl font-bold text-primary">
                      {editingProduct === 'new' ? 'New Product Craftsman Form' : `Edit Product: ${editingProduct.name}`}
                    </h3>
                    <button
                      onClick={() => setEditingProduct(null)}
                      className="text-on-surface-variant hover:text-primary font-bold text-xs uppercase"
                    >
                      Close Form ✕
                    </button>
                  </div>

                  {productFormError && (
                    <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded text-xs font-medium whitespace-pre-line flex items-start gap-3">
                      <span className="material-symbols-outlined text-red-500 text-base shrink-0">error</span>
                      <div>
                        <p className="font-bold uppercase tracking-wider text-[10px]">Submission Issue Detected</p>
                        <p className="mt-1 leading-relaxed font-normal">{productFormError}</p>
                      </div>
                    </div>
                  )}

                  <form onSubmit={handleSaveProduct} className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs font-medium">
                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Product Name *</label>
                      <input
                        type="text"
                        required
                        value={productForm.name}
                        onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. Milano Bifold Wallet"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">SKU *</label>
                      <input
                        type="text"
                        required
                        value={productForm.sku}
                        onChange={(e) => setProductForm({ ...productForm, sku: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. TANM-W-001"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Category *</label>
                      <select
                        required
                        value={productForm.categoryId}
                        onChange={(e) => setProductForm({ ...productForm, categoryId: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                      >
                        <option value="">Select Category</option>
                        {categories.map((c) => (
                          <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Price (INR ₹) *</label>
                      <input
                        type="number"
                        step="0.01"
                        required
                        value={productForm.price}
                        onChange={(e) => setProductForm({ ...productForm, price: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. 4500"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Stock Quantity *</label>
                      <input
                        type="number"
                        required
                        value={productForm.stockQuantity}
                        onChange={(e) => setProductForm({ ...productForm, stockQuantity: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. 25"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Leather Type</label>
                      <input
                        type="text"
                        value={productForm.leatherType}
                        onChange={(e) => setProductForm({ ...productForm, leatherType: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. Tuscan Full-Grain Calfskin"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Color</label>
                      <input
                        type="text"
                        value={productForm.color}
                        onChange={(e) => setProductForm({ ...productForm, color: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. Cognac Tan"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Dimensions</label>
                      <input
                        type="text"
                        value={productForm.dimensions}
                        onChange={(e) => setProductForm({ ...productForm, dimensions: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. 11.5cm x 9cm x 1.5cm"
                      />
                    </div>

                    <div className="md:col-span-2">
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Short Description</label>
                      <input
                        type="text"
                        value={productForm.shortDescription}
                        onChange={(e) => setProductForm({ ...productForm, shortDescription: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="Brief summary for product card"
                      />
                    </div>

                    <div className="md:col-span-2">
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Detailed Description</label>
                      <textarea
                        rows={4}
                        value={productForm.description}
                        onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="Heritage details, stitching style, care instructions..."
                      />
                    </div>

                    {editingProduct === 'new' && (
                      <div className="md:col-span-2 space-y-3 bg-surface-container-low p-4 rounded border border-outline-variant/30">
                        <div className="flex justify-between items-center">
                          <label className="block uppercase font-bold text-on-surface-variant text-xs">
                            Product Images Gallery ({newProductImages.length} attached)
                          </label>
                          <label className="bg-primary text-surface px-4 py-2 rounded cursor-pointer font-bold text-xs flex items-center gap-2 hover:bg-primary-container transition-all">
                            <span className="material-symbols-outlined text-base">collections</span>
                            {uploadingImage ? 'Uploading Batch...' : 'Upload Files (Select Multiple)'}
                            <input
                              type="file"
                              accept="image/*"
                              multiple
                              className="hidden"
                              disabled={uploadingImage}
                              onChange={(e) => {
                                const files = e.target.files;
                                if (files && files.length > 0) {
                                  handleBatchFileUpload(files, 'products', (uploadedList) => {
                                    const newItems = uploadedList.map((res, i) => ({
                                      imageUrl: res.url,
                                      publicId: res.publicId,
                                      isPrimary: newProductImages.length === 0 && i === 0,
                                    }));
                                    setNewProductImages((prev) => [...prev, ...newItems]);
                                  });
                                }
                              }}
                            />
                          </label>
                        </div>

                        <div className="flex gap-2">
                          <input
                            type="url"
                            value={newProductImageUrl}
                            onChange={(e) => setNewProductImageUrl(e.target.value)}
                            className="flex-1 bg-surface border border-outline-variant/40 p-2 rounded text-on-surface focus:outline-none focus:border-primary text-xs"
                            placeholder="Or paste direct image URL and click + Add..."
                          />
                          <button
                            type="button"
                            onClick={() => {
                              if (!newProductImageUrl) return;
                              setNewProductImages((prev) => [
                                ...prev,
                                { imageUrl: newProductImageUrl, publicId: null, isPrimary: prev.length === 0 }
                              ]);
                              setNewProductImageUrl('');
                            }}
                            className="bg-secondary text-surface px-3 py-2 rounded font-bold text-xs uppercase shrink-0"
                          >
                            + Add URL
                          </button>
                        </div>

                        {newProductImages.length > 0 && (
                          <div className="grid grid-cols-3 sm:grid-cols-5 gap-3 pt-2">
                            {newProductImages.map((img, idx) => (
                              <div key={idx} className="relative group border border-outline-variant/40 rounded p-1 bg-surface">
                                <img src={img.imageUrl} alt={`Uploaded ${idx + 1}`} className="w-full h-24 object-cover rounded" />
                                <div className="mt-1 flex items-center justify-between text-[10px]">
                                  <button
                                    type="button"
                                    onClick={() => {
                                      setNewProductImages((prev) =>
                                        prev.map((item, i) => ({ ...item, isPrimary: i === idx }))
                                      );
                                    }}
                                    className={`font-bold px-1.5 py-0.5 rounded ${
                                      img.isPrimary ? 'bg-primary text-surface' : 'text-on-surface-variant hover:text-primary'
                                    }`}
                                  >
                                    {img.isPrimary ? 'PRIMARY' : 'Set Primary'}
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => setNewProductImages((prev) => prev.filter((_, i) => i !== idx))}
                                    className="text-error font-bold"
                                  >
                                    ✕
                                  </button>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )}

                    <div className="flex items-center gap-6 md:col-span-2 py-2">
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={productForm.isFeatured}
                          onChange={(e) => setProductForm({ ...productForm, isFeatured: e.target.checked })}
                          className="w-4 h-4 text-primary rounded focus:ring-primary"
                        />
                        <span className="font-bold text-primary uppercase">Mark as Featured Product</span>
                      </label>

                      <div className="flex items-center gap-2">
                        <span className="font-bold uppercase text-on-surface-variant">Status:</span>
                        <select
                          value={productForm.status}
                          onChange={(e) => setProductForm({ ...productForm, status: e.target.value })}
                          className="bg-surface border border-outline-variant/40 p-2 rounded text-on-surface font-bold"
                        >
                          <option value="PUBLISHED">PUBLISHED</option>
                          <option value="DRAFT">DRAFT</option>
                          <option value="OUT_OF_STOCK">OUT_OF_STOCK</option>
                          <option value="ARCHIVED">ARCHIVED</option>
                        </select>
                      </div>
                    </div>

                    <div className="md:col-span-2 flex gap-4 pt-4 border-t border-outline-variant/20">
                      <button
                        type="submit"
                        className="bg-primary text-surface px-8 py-3 font-label-sm uppercase tracking-widest font-bold hover:bg-primary-container"
                      >
                        {editingProduct === 'new' ? 'Save New Product' : 'Update Product'}
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditingProduct(null)}
                        className="bg-surface-container border border-outline-variant/40 px-6 py-3 font-label-sm uppercase text-on-surface font-bold hover:bg-surface-variant"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>

                  {/* EXISTING IMAGES MANAGER FOR EDIT MODE */}
                  {editingProduct !== 'new' && editingProduct.id && (
                    <div className="border-t border-outline-variant/30 pt-8 space-y-6">
                      <h4 className="font-display-lg text-xl font-bold text-primary">Manage Product Images</h4>

                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {(editingProduct.images || []).map((img) => (
                          <div key={img.id} className="relative group border border-outline-variant/30 rounded p-2 bg-surface">
                            <img src={img.imageUrl} alt="Product" className="w-full h-32 object-cover rounded" />
                            {img.isPrimary && (
                              <span className="absolute top-3 left-3 bg-primary text-surface text-[9px] font-bold px-2 py-0.5 rounded">
                                PRIMARY
                              </span>
                            )}
                            <div className="mt-2 flex justify-between items-center text-[10px]">
                              {!img.isPrimary && (
                                <button
                                  type="button"
                                  onClick={() => handleSetPrimaryProductImage(editingProduct.id, img.id)}
                                  className="text-primary font-bold underline"
                                >
                                  Make Primary
                                </button>
                              )}
                              <button
                                type="button"
                                onClick={() => handleDeleteProductImage(editingProduct.id, img.id)}
                                className="text-error font-bold ml-auto"
                              >
                                Delete
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Add new image batch input */}
                      <div className="flex flex-col sm:flex-row gap-3 items-center bg-surface p-4 border border-outline-variant/30 rounded">
                        <input
                          type="url"
                          value={newProductImageUrl}
                          onChange={(e) => setNewProductImageUrl(e.target.value)}
                          placeholder="Paste image URL or select multiple files..."
                          className="flex-1 bg-transparent border-b border-outline-variant/40 p-2 text-xs text-on-surface focus:outline-none w-full"
                        />
                        <label className="bg-primary text-surface px-4 py-2 rounded cursor-pointer font-bold text-[11px] flex items-center gap-1.5 shrink-0 hover:bg-primary-container transition-all">
                          <span className="material-symbols-outlined text-sm">collections</span>
                          {uploadingImage ? 'Uploading Batch...' : 'Upload Files (Select Multiple)'}
                          <input
                            type="file"
                            accept="image/*"
                            multiple
                            className="hidden"
                            disabled={uploadingImage}
                            onChange={async (e) => {
                              const files = e.target.files;
                              if (files && files.length > 0) {
                                handleBatchFileUpload(files, 'products', async (uploadedList) => {
                                  const requests = uploadedList.map((res, idx) => ({
                                    imageUrl: res.url,
                                    publicId: res.publicId,
                                    isPrimary: (editingProduct.images || []).length === 0 && idx === 0,
                                  }));
                                  await productApi.addImagesBulk(editingProduct.id, requests);
                                  const updated = await productApi.getById(editingProduct.id);
                                  setEditingProduct(updated);
                                  loadProducts();
                                });
                              }
                            }}
                          />
                        </label>
                        <button
                          type="button"
                          onClick={() => handleAddProductImage(editingProduct.id)}
                          className="bg-secondary text-surface px-4 py-2 text-[10px] font-bold uppercase tracking-widest rounded shrink-0"
                        >
                          + Add URL
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* PRODUCTS LIST TABLE */}
              {!editingProduct && (
                <div className="space-y-6">
                  {/* ADMIN FILTER BAR */}
                  <div className="bg-surface-container-low p-4 rounded border border-outline-variant/30 flex flex-wrap items-center justify-between gap-4 text-xs font-medium">
                    <div className="flex flex-wrap items-center gap-4 flex-1">
                      {/* Search Input */}
                      <div className="relative flex-1 min-w-[200px]">
                        <input
                          type="text"
                          value={adminSearchQuery}
                          onChange={(e) => setAdminSearchQuery(e.target.value)}
                          placeholder="Search product by name or SKU..."
                          className="w-full bg-surface border border-outline-variant/40 rounded p-2.5 pl-8 text-on-surface focus:outline-none focus:border-primary text-xs"
                        />
                        <span className="material-symbols-outlined absolute left-2.5 top-2.5 text-outline text-[16px]">search</span>
                      </div>

                      {/* Status Filter */}
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-on-surface-variant uppercase text-[10px]">Status:</span>
                        <select
                          value={adminStatusFilter}
                          onChange={(e) => setAdminStatusFilter(e.target.value)}
                          className="bg-surface border border-outline-variant/40 rounded p-2 text-on-surface font-bold text-xs"
                        >
                          <option value="ALL">All Statuses</option>
                          <option value="PUBLISHED">PUBLISHED</option>
                          <option value="DRAFT">DRAFT</option>
                          <option value="OUT_OF_STOCK">OUT_OF_STOCK</option>
                          <option value="ARCHIVED">ARCHIVED</option>
                        </select>
                      </div>

                      {/* Category Filter */}
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-on-surface-variant uppercase text-[10px]">Category:</span>
                        <select
                          value={adminCategoryFilter}
                          onChange={(e) => setAdminCategoryFilter(e.target.value)}
                          className="bg-surface border border-outline-variant/40 rounded p-2 text-on-surface font-bold text-xs"
                        >
                          <option value="ALL">All Categories</option>
                          {categories.map((cat) => (
                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                          ))}
                        </select>
                      </div>
                    </div>

                    {(adminStatusFilter !== 'ALL' || adminCategoryFilter !== 'ALL' || adminSearchQuery) && (
                      <button
                        onClick={() => {
                          setAdminStatusFilter('ALL');
                          setAdminCategoryFilter('ALL');
                          setAdminSearchQuery('');
                        }}
                        className="text-on-surface-variant hover:text-primary font-bold uppercase tracking-wider text-[10px] underline"
                      >
                        Reset Filters
                      </button>
                    )}
                  </div>

                  {/* TABLE */}
                  <div className="bg-surface border border-outline-variant/30 rounded overflow-hidden">
                    <table className="w-full text-left text-xs font-medium">
                      <thead className="bg-surface-container-high uppercase tracking-widest text-[10px] text-on-surface-variant border-b border-outline-variant/30">
                        <tr>
                          <th className="p-4">Product</th>
                          <th className="p-4">Category</th>
                          <th className="p-4">Price</th>
                          <th className="p-4">Stock</th>
                          <th className="p-4">Featured</th>
                          <th className="p-4">Status</th>
                          <th className="p-4 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-outline-variant/20">
                        {(() => {
                          const filtered = products.filter((p) => {
                            if (adminStatusFilter !== 'ALL' && p.status !== adminStatusFilter) return false;
                            if (adminCategoryFilter !== 'ALL' && String(p.category?.id) !== adminCategoryFilter) return false;
                            if (adminSearchQuery) {
                              const q = adminSearchQuery.toLowerCase();
                              const nameMatch = p.name?.toLowerCase().includes(q);
                              const skuMatch = p.sku?.toLowerCase().includes(q);
                              if (!nameMatch && !skuMatch) return false;
                            }
                            return true;
                          });

                          if (filtered.length === 0) {
                            return (
                              <tr>
                                <td colSpan={7} className="p-8 text-center text-on-surface-variant">
                                  No products match the selected filters.
                                </td>
                              </tr>
                            );
                          }

                          return filtered.map((p) => (
                            <tr key={p.id} className="hover:bg-surface-container-low transition-colors">
                            <td className="p-4 flex items-center gap-3">
                              <img
                                src={p.mainImageUrl || 'https://picsum.photos/seed/placeholder/200/200'}
                                alt={p.name}
                                className="w-12 h-12 object-cover rounded border border-outline-variant/30"
                              />
                              <div>
                                <p className="font-bold text-primary text-sm">{p.name}</p>
                                <p className="text-[10px] text-on-surface-variant">SKU: {p.sku}</p>
                              </div>
                            </td>
                            <td className="p-4">{p.category?.name || 'Uncategorized'}</td>
                            <td className="p-4 font-bold text-primary">₹{p.price}</td>
                            <td className="p-4 font-bold">{p.stockQuantity} pcs</td>
                            <td className="p-4">
                              {(p.isFeatured || p.featured) ? (
                                <span className="bg-secondary-container text-on-secondary-container font-bold text-[9px] px-2 py-0.5 rounded">
                                  FEATURED
                                </span>
                              ) : (
                                <span className="text-on-surface-variant/50 text-[10px]">—</span>
                              )}
                            </td>
                            <td className="p-4">
                              <span className={`font-bold text-[9px] px-2 py-0.5 rounded ${p.status === 'PUBLISHED' ? 'bg-green-100 text-green-800' : 'bg-gray-200 text-gray-700'}`}>
                                {p.status}
                              </span>
                            </td>
                            <td className="p-4 text-right">
                              <button
                                onClick={() => handleEditProduct(p)}
                                className="bg-secondary text-surface px-3 py-1 rounded text-[10px] uppercase font-bold mr-2 hover:opacity-90"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteProduct(p.id)}
                                className="bg-error text-surface px-3 py-1 rounded text-[10px] uppercase font-bold hover:opacity-90"
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ));
                      })()}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}

          {/* TAB: CATEGORIES MANAGER */}
          {activeTab === 'categories' && (
            <div className="space-y-12">
              <div className="flex justify-between items-center">
                <div>
                  <h1 className="font-display-lg text-4xl md:text-5xl text-primary font-bold">
                    Category Hierarchy
                  </h1>
                  <p className="text-sm text-on-surface-variant italic mt-1">
                    Manage store collections and product groupings.
                  </p>
                </div>
                {!editingCategory && (
                  <button
                    onClick={() => {
                      setEditingCategory('new');
                      setCategoryForm({ name: '', description: '', imageUrl: '', displayOrder: categories.length + 1 });
                    }}
                    className="bg-primary text-surface px-6 py-3 font-label-sm text-xs uppercase tracking-widest font-bold hover:bg-primary-container transition-all"
                  >
                    + Create Category
                  </button>
                )}
              </div>

              {/* CATEGORY EDIT/CREATE FORM */}
              {editingCategory && (
                <div className="bg-surface-container-low p-8 border border-outline-variant/30 rounded space-y-6 max-w-2xl">
                  <div className="flex justify-between items-center border-b border-outline-variant/20 pb-4">
                    <h3 className="font-display-lg text-2xl font-bold text-primary">
                      {editingCategory === 'new' ? 'Create New Category' : `Edit Category: ${editingCategory.name}`}
                    </h3>
                    <button
                      onClick={() => setEditingCategory(null)}
                      className="text-on-surface-variant hover:text-primary font-bold text-xs uppercase"
                    >
                      Close ✕
                    </button>
                  </div>

                  {categoryFormError && (
                    <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded text-xs font-medium whitespace-pre-line flex items-start gap-3">
                      <span className="material-symbols-outlined text-red-500 text-base shrink-0">error</span>
                      <div>
                        <p className="font-bold uppercase tracking-wider text-[10px]">Submission Issue Detected</p>
                        <p className="mt-1 leading-relaxed font-normal">{categoryFormError}</p>
                      </div>
                    </div>
                  )}

                  <form onSubmit={handleSaveCategory} className="space-y-4 text-xs font-medium">
                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Category Name *</label>
                      <input
                        type="text"
                        required
                        value={categoryForm.name}
                        onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="e.g. Wallets"
                      />
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Description</label>
                      <input
                        type="text"
                        value={categoryForm.description}
                        onChange={(e) => setCategoryForm({ ...categoryForm, description: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                        placeholder="Handcrafted full-grain leather wallets and cardholders"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="block uppercase font-bold text-on-surface-variant">Banner / Tile Image</label>
                      <div className="flex flex-col sm:flex-row gap-3">
                        <input
                          type="url"
                          value={categoryForm.imageUrl}
                          onChange={(e) => setCategoryForm({ ...categoryForm, imageUrl: e.target.value })}
                          className="flex-1 bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                          placeholder="Cloudinary Image URL or paste direct link..."
                        />
                        <label className="bg-primary text-surface px-4 py-3 rounded cursor-pointer font-bold text-xs flex items-center justify-center gap-2 hover:bg-primary-container transition-all shrink-0">
                          <span className="material-symbols-outlined text-sm">cloud_upload</span>
                          {uploadingImage ? 'Uploading...' : 'Upload File to Cloudinary'}
                          <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            disabled={uploadingImage}
                            onChange={(e) => {
                              const file = e.target.files[0];
                              if (file) handleFileUpload(file, 'categories', (url) => setCategoryForm({ ...categoryForm, imageUrl: url }));
                            }}
                          />
                        </label>
                      </div>
                      {categoryForm.imageUrl && (
                        <div className="mt-2 flex items-center gap-3">
                          <img src={categoryForm.imageUrl} alt="Preview" className="w-12 h-12 object-cover rounded border border-outline-variant/30" />
                          <span className="text-[11px] text-green-700 font-bold">✓ Cloudinary URL Attached</span>
                        </div>
                      )}
                    </div>

                    <div>
                      <label className="block uppercase font-bold text-on-surface-variant mb-2">Display Order</label>
                      <input
                        type="number"
                        value={categoryForm.displayOrder}
                        onChange={(e) => setCategoryForm({ ...categoryForm, displayOrder: e.target.value })}
                        className="w-full bg-surface border border-outline-variant/40 p-3 rounded text-on-surface focus:outline-none focus:border-primary"
                      />
                    </div>

                    <div className="flex gap-4 pt-4 border-t border-outline-variant/20">
                      <button
                        type="submit"
                        className="bg-primary text-surface px-8 py-3 font-label-sm uppercase tracking-widest font-bold hover:bg-primary-container"
                      >
                        {editingCategory === 'new' ? 'Save Category' : 'Update Category'}
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditingCategory(null)}
                        className="bg-surface-container border border-outline-variant/40 px-6 py-3 font-label-sm uppercase text-on-surface font-bold hover:bg-surface-variant"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}

              {/* CATEGORIES LIST TABLE */}
              {!editingCategory && (
                <div className="bg-surface border border-outline-variant/30 rounded overflow-hidden">
                  <table className="w-full text-left text-xs font-medium">
                    <thead className="bg-surface-container-high uppercase tracking-widest text-[10px] text-on-surface-variant border-b border-outline-variant/30">
                      <tr>
                        <th className="p-4">Category</th>
                        <th className="p-4">Slug</th>
                        <th className="p-4">Description</th>
                        <th className="p-4">Display Order</th>
                        <th className="p-4 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-outline-variant/20">
                      {loadingCategories ? (
                        <tr>
                          <td colSpan={5} className="p-8 text-center text-on-surface-variant">Loading categories...</td>
                        </tr>
                      ) : categories.length === 0 ? (
                        <tr>
                          <td colSpan={5} className="p-8 text-center text-on-surface-variant">No categories found.</td>
                        </tr>
                      ) : (
                        categories.map((c) => (
                          <tr key={c.id} className="hover:bg-surface-container-low transition-colors">
                            <td className="p-4 flex items-center gap-3">
                              {c.imageUrl && (
                                <img src={c.imageUrl} alt={c.name} className="w-10 h-10 object-cover rounded border border-outline-variant/30" />
                              )}
                              <span className="font-bold text-primary text-sm">{c.name}</span>
                            </td>
                            <td className="p-4 text-on-surface-variant">{c.slug}</td>
                            <td className="p-4 text-on-surface-variant">{c.description || '—'}</td>
                            <td className="p-4 font-bold">{c.displayOrder}</td>
                            <td className="p-4 text-right">
                              <button
                                onClick={() => {
                                  setEditingCategory(c);
                                  setCategoryForm({
                                    name: c.name || '',
                                    description: c.description || '',
                                    imageUrl: c.imageUrl || '',
                                    displayOrder: c.displayOrder || 0,
                                  });
                                }}
                                className="bg-secondary text-surface px-3 py-1 rounded text-[10px] uppercase font-bold mr-2 hover:opacity-90"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteCategory(c.id)}
                                className="bg-error text-surface px-3 py-1 rounded text-[10px] uppercase font-bold hover:opacity-90"
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: ORDER REPOSITORY */}
          {activeTab === 'orders' && (
            <div className="space-y-12">
              <section className="mb-12">
                <h1 className="font-display-lg text-4xl md:text-5xl text-primary leading-none tracking-tight font-bold">
                  Order Repository
                </h1>
                <p className="font-body-lg text-sm text-on-surface-variant italic mt-2">
                  Managing the lifecycle of bespoke leather goods from first stitch to final delivery.
                </p>
              </section>

              <div className="w-full bg-surface-container-lowest border border-outline-variant/20 p-8 rounded shadow-sm text-xs">
                {loadingOrders ? (
                  <div className="flex justify-center items-center py-24">
                    <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
                  </div>
                ) : (
                  <>
                    <div className="overflow-x-auto">
                      <table className="w-full text-left">
                        <thead>
                          <tr className="border-b border-outline-variant/40">
                            <th className="pb-6 font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold">
                              Order ID
                            </th>
                            <th className="pb-6 font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold">
                              Date
                            </th>
                            <th className="pb-6 font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold">
                              Customer
                            </th>
                            <th className="pb-6 font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold">
                              Fulfillment Status
                            </th>
                            <th className="pb-6 font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold text-right">
                              Total
                            </th>
                            <th className="pb-6 text-center font-label-sm text-[10px] text-on-surface-variant tracking-widest uppercase opacity-60 font-bold">
                              Update Status Actions
                            </th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-outline-variant/10 font-medium">
                          {orders.map((ord) => (
                            <tr key={ord.id} className="hover:bg-primary/5 transition-colors">
                              <td className="py-6 font-label-sm text-primary font-bold">
                                #{ord.orderNumber}
                              </td>
                              <td className="py-6 text-on-surface-variant">
                                {ord.orderedAt || ord.createdAt ? new Date(ord.orderedAt || ord.createdAt).toLocaleDateString() : 'N/A'}
                              </td>
                               <td className="py-6 font-semibold">
                                 <div>{ord.userName || ord.customerName || ord.shippingFullName || 'Guest User'}</div>
                                 {ord.userEmail && (
                                   <div className="text-[10px] text-on-surface-variant font-normal">{ord.userEmail}</div>
                                 )}
                               </td>
                              <td className="py-6">
                                <span className={`px-2.5 py-1 border font-bold text-[9px] tracking-widest uppercase rounded ${
                                  ord.fulfillmentStatus === 'DELIVERED'
                                    ? 'border-emerald-500/20 bg-emerald-500/5 text-emerald-600'
                                    : ord.fulfillmentStatus === 'CANCELLED'
                                    ? 'border-error/20 bg-error/5 text-error'
                                    : 'border-primary/20 bg-primary/5 text-primary'
                                }`}>
                                  {ord.fulfillmentStatus}
                                </span>
                              </td>
                              <td className="py-6 text-right font-bold">₹{ord.grandTotal.toFixed(2)}</td>
                              <td className="py-6 text-center">
                                 <div className="flex justify-center items-center gap-2">
                                   <button
                                     onClick={() => handleInspectOrder(ord)}
                                     className="bg-primary text-surface hover:bg-primary-container px-3 py-1 rounded text-[10px] font-bold uppercase tracking-wider transition-colors flex items-center gap-1"
                                   >
                                     <span className="material-symbols-outlined text-[14px]">visibility</span>
                                     Inspect
                                   </button>
                                   <select
                                     value={ord.fulfillmentStatus}
                                     onChange={(e) => handleUpdateOrderStatus(ord.orderNumber, e.target.value)}
                                     className="bg-surface border border-outline/25 rounded p-1 text-[10px] text-on-surface font-semibold focus:ring-1 focus:ring-primary outline-none"
                                   >
                                     <option value="PENDING">PENDING</option>
                                     <option value="CONFIRMED">CONFIRMED</option>
                                     <option value="SHIPPED">SHIPPED</option>
                                     <option value="DELIVERED">DELIVERED</option>
                                     <option value="CANCELLED">CANCELLED</option>
                                   </select>
                                 </div>
                               </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>

                    {/* Pagination */}
                    {ordersTotalPages > 1 && (
                      <div className="mt-8 flex justify-between items-center border-t border-outline-variant/20 pt-6 font-semibold">
                        <span className="text-on-surface-variant uppercase text-[10px]">
                          Page {ordersPage + 1} of {ordersTotalPages}
                        </span>
                        <div className="flex gap-2">
                          <button
                            disabled={ordersPage === 0}
                            onClick={() => setOrdersPage(ordersPage - 1)}
                            className="w-10 h-10 border border-outline-variant/30 flex items-center justify-center hover:bg-surface-variant transition-colors disabled:opacity-50"
                          >
                            <span className="material-symbols-outlined text-[18px]">chevron_left</span>
                          </button>
                          <button
                            disabled={ordersPage === ordersTotalPages - 1}
                            onClick={() => setOrdersPage(ordersPage + 1)}
                            className="w-10 h-10 border border-outline-variant/30 flex items-center justify-center hover:bg-surface-variant transition-colors disabled:opacity-50"
                          >
                            <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                          </button>
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          )}

          {/* TAB 3: INVENTORY STOCK ADJUSTMENT */}
          {activeTab === 'inventory' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 text-xs font-medium">
              
              {/* Stock Adjustment Action Panel */}
              <div className="lg:col-span-4 bg-surface-container-low p-6 rounded border border-outline-variant/15 h-fit space-y-6">
                <h3 className="font-headline-md text-lg text-primary font-bold">Adjust Stock Level</h3>
                <form onSubmit={handleAdjustStock} className="space-y-4">
                  <div>
                    <label className="block font-semibold mb-1 text-on-surface-variant">Select Product</label>
                    <select
                      onChange={(e) => {
                        const prod = products.find((p) => p.id === parseInt(e.target.value, 10));
                        setSelectedProduct(prod || null);
                      }}
                      required
                      className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface"
                    >
                      <option value="">-- Choose Item --</option>
                      {products.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name} ({p.sku} | Current: {p.stockQuantity})
                        </option>
                      ))}
                    </select>
                  </div>

                  {selectedProduct && (
                    <>
                      <div>
                        <label className="block font-semibold mb-1 text-on-surface-variant">
                          New Stock Quantity
                        </label>
                        <input
                          type="number"
                          min="0"
                          value={newQty}
                          onChange={(e) => setNewQty(e.target.value)}
                          required
                          placeholder={`Current: ${selectedProduct.stockQuantity}`}
                          className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface text-center"
                        />
                      </div>
                      <div>
                        <label className="block font-semibold mb-1 text-on-surface-variant">
                          Adjustment Type
                        </label>
                        <select
                          value={adjType}
                          onChange={(e) => setAdjType(e.target.value)}
                          className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface"
                        >
                          <option value="RESTOCK">Supplier Restock</option>
                          <option value="DAMAGED">Damaged Stock Write-Off</option>
                          <option value="MANUAL_CORRECTION">Manual Inventory Correction</option>
                        </select>
                      </div>
                      <div>
                        <label className="block font-semibold mb-1 text-on-surface-variant">Reason</label>
                        <textarea
                          value={adjReason}
                          onChange={(e) => setAdjReason(e.target.value)}
                          placeholder="Input stock modification reason..."
                          className="w-full bg-surface border border-outline/20 rounded p-2.5 text-on-surface"
                          rows="3"
                        ></textarea>
                      </div>
                      <button
                        type="submit"
                        disabled={adjusting}
                        className="w-full bg-primary text-surface py-3 rounded font-bold uppercase tracking-wider transition-colors"
                      >
                        {adjusting ? 'Adjusting...' : 'Commit Stock Adjustment'}
                      </button>
                    </>
                  )}
                </form>
              </div>

              {/* Paginated Audit Log List */}
              <div className="lg:col-span-8 bg-surface-container-low p-6 rounded border border-outline-variant/15 space-y-4">
                <h3 className="font-headline-md text-lg text-primary font-bold">Inventory Stock Audit Logs</h3>
                <div className="space-y-4 max-h-[600px] overflow-y-auto pr-2">
                  {auditLogs.map((log) => (
                    <div key={log.id} className="p-4 bg-surface rounded border border-outline-variant/10 flex justify-between gap-4">
                      <div className="space-y-1">
                        <span className="font-bold text-on-surface">{log.productName}</span>
                        <span className="text-[10px] text-on-surface-variant block">SKU: {log.productSku}</span>
                        <p className="text-on-surface-variant italic mt-1">Reason: {log.reason}</p>
                        <span className="text-[9px] text-primary block mt-1">
                          {new Date(log.createdAt).toLocaleString()} | Adjusted by: {log.adjustedBy}
                        </span>
                      </div>
                      <div className="text-right flex flex-col justify-center">
                        <span
                          className={`font-semibold text-sm ${
                            log.quantityChanged >= 0 ? 'text-secondary' : 'text-error'
                          }`}
                        >
                          {log.quantityChanged >= 0 ? `+${log.quantityChanged}` : log.quantityChanged}
                        </span>
                        <span className="text-[9px] text-on-surface-variant block font-bold">
                          New Total: {log.newStock}
                        </span>
                      </div>
                    </div>
                  ))}
                  {auditLogs.length === 0 && (
                    <p className="text-on-surface-variant text-center py-12">
                      No stock logs currently recorded.
                    </p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: REVIEWS MODERATION QUEUE */}
          {activeTab === 'moderation' && (
            <div className="bg-surface-container-low p-6 rounded border border-outline-variant/15 space-y-4 text-xs font-medium">
              <h3 className="font-headline-md text-lg text-primary font-bold">Pending Reviews Queue</h3>
              <div className="space-y-4">
                {loadingModeration ? (
                  <p className="text-center py-12">Loading moderation queue...</p>
                ) : reviewsQueue.length === 0 ? (
                  <p className="text-center text-on-surface-variant py-12">
                    All reviews are moderated! No pending items.
                  </p>
                ) : (
                  reviewsQueue.map((rev) => (
                    <div
                      key={rev.id}
                      className="p-4 bg-surface rounded border border-outline-variant/15 flex justify-between items-start gap-6"
                    >
                      <div className="flex-1 space-y-1">
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-on-surface">{rev.customerName}</span>
                          <span className="text-primary font-bold">{rev.rating}★</span>
                          {rev.verifiedPurchase && (
                            <span className="bg-secondary-container/20 text-secondary px-1.5 py-0.5 rounded text-[8px] font-bold">
                              Verified Buy
                            </span>
                          )}
                        </div>
                        <span className="text-[9px] text-on-surface-variant block font-bold">
                          Product ID: {rev.productId}
                        </span>
                        <p className="text-on-surface-variant leading-relaxed mt-2">{rev.comment}</p>
                      </div>
                      <div className="flex gap-2 shrink-0">
                        <button
                          onClick={() => handleModerateReview(rev.id, 'APPROVED')}
                          className="bg-secondary text-surface px-4 py-1.5 rounded font-bold uppercase tracking-wider text-[10px]"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleModerateReview(rev.id, 'REJECTED')}
                          className="bg-error text-surface px-4 py-1.5 rounded font-bold uppercase tracking-wider text-[10px]"
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* TAB 5: HEADLESS CMS EDITOR STUDIO */}
          {activeTab === 'cms' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 text-xs font-medium">
              
              {/* Left Column: Announcement configuration panel */}
              <div className="lg:col-span-5 bg-surface-container-low p-6 rounded border border-outline-variant/15 h-fit space-y-6">
                <h3 className="font-headline-md text-lg text-primary font-bold">Storefront Announcement Bar</h3>
                <form onSubmit={handleSaveAnnouncement} className="space-y-4">
                  <div>
                    <label className="block font-semibold mb-1 text-on-surface-variant">Banner Content Text</label>
                    <input
                      type="text"
                      required
                      value={annForm.content}
                      onChange={(e) => setAnnForm({ ...annForm, content: e.target.value })}
                      placeholder="e.g. Free complimentary worldwide shipping on all autumn briefcases"
                      className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface"
                    />
                  </div>
                  <div className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      id="annActive"
                      checked={annForm.isActive}
                      onChange={(e) => setAnnForm({ ...annForm, isActive: e.target.checked })}
                      className="text-primary focus:ring-primary h-4 w-4 rounded border-outline/25"
                    />
                    <label htmlFor="annActive" className="font-semibold text-on-surface-variant cursor-pointer">
                      Enable Banner Display
                    </label>
                  </div>
                  <button
                    type="submit"
                    disabled={savingCms}
                    className="w-full bg-primary text-surface py-2 rounded font-bold uppercase tracking-wider"
                  >
                    {savingCms ? 'Saving...' : 'Save Announcement Bar'}
                  </button>
                </form>
              </div>

              {/* Right Column: Hero Slider management list */}
              <div className="lg:col-span-7 bg-surface-container-low p-6 rounded border border-outline-variant/15 space-y-6">
                <h3 className="font-headline-md text-lg text-primary font-bold">Hero Slides Editor</h3>
                
                {/* Editor slide form */}
                <form onSubmit={handleSaveHeroSlide} className="bg-surface p-4 rounded border border-outline-variant/10 space-y-4">
                  <h4 className="font-semibold text-sm text-primary font-bold">
                    {editingSlide ? 'Edit Slide Configs' : 'Create New Banner Slide'}
                  </h4>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant">Slide Header Title</label>
                      <input
                        type="text"
                        required
                        value={slideForm.title}
                        onChange={(e) => setSlideForm({ ...slideForm, title: e.target.value })}
                        placeholder="e.g. Art of the Stitch"
                        className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface"
                      />
                    </div>
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant">CTA Button Label</label>
                      <input
                        type="text"
                        required
                        value={slideForm.buttonText}
                        onChange={(e) => setSlideForm({ ...slideForm, buttonText: e.target.value })}
                        className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface font-bold text-center"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block font-semibold mb-1 text-on-surface-variant">Image Source URL</label>
                    <input
                      type="text"
                      required
                      value={slideForm.imageUrl}
                      onChange={(e) => setSlideForm({ ...slideForm, imageUrl: e.target.value })}
                      placeholder="https://..."
                      className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant">Display Order Index</label>
                      <input
                        type="number"
                        min="1"
                        required
                        value={slideForm.sortOrder}
                        onChange={(e) => setSlideForm({ ...slideForm, sortOrder: parseInt(e.target.value, 10) })}
                        className="w-full bg-surface border border-outline/25 rounded p-2 text-on-surface text-center"
                      />
                    </div>
                    <div className="flex items-center gap-2 pt-5">
                      <input
                        type="checkbox"
                        id="slideActive"
                        checked={slideForm.isActive}
                        onChange={(e) => setSlideForm({ ...slideForm, isActive: e.target.checked })}
                        className="text-primary focus:ring-primary h-4 w-4 rounded border-outline/25"
                      />
                      <label htmlFor="slideActive" className="font-semibold text-on-surface-variant cursor-pointer">
                        Mark Active
                      </label>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <button
                      type="submit"
                      disabled={savingCms}
                      className="bg-primary text-surface px-6 py-2 rounded font-bold uppercase tracking-wider"
                    >
                      {savingCms ? 'Saving...' : editingSlide ? 'Update Slide' : 'Create Slide'}
                    </button>
                    {editingSlide && (
                      <button
                        type="button"
                        onClick={() => {
                          setEditingSlide(null);
                          setSlideForm({
                            title: '',
                            imageUrl: '',
                            buttonText: 'Collections',
                            isActive: true,
                            sortOrder: 1
                          });
                        }}
                        className="border border-outline-variant text-on-surface-variant px-6 py-2 rounded"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </form>

                {/* Slides List */}
                <div className="space-y-4">
                  <h4 className="font-semibold text-xs text-on-surface">Configured Slides</h4>
                  {slides.length === 0 ? (
                    <p className="text-on-surface-variant text-center py-6">No slides configured yet.</p>
                  ) : (
                    slides.map((s) => (
                      <div key={s.id} className="p-4 bg-surface rounded border border-outline-variant/10 flex justify-between items-center gap-4">
                        <div className="flex items-center gap-4">
                          <div className="w-16 h-12 bg-surface-container rounded overflow-hidden shrink-0 shadow">
                            <img src={s.imageUrl || s.backgroundImage?.url} alt={s.title} className="w-full h-full object-cover" />
                          </div>
                          <div>
                            <span className="font-bold block text-on-surface">{s.title}</span>
                            <span className="text-[10px] text-on-surface-variant font-bold">Order: {s.sortOrder} | Status: {s.status}</span>
                          </div>
                        </div>
                        <div className="flex gap-2 shrink-0">
                          <button
                            onClick={() => {
                              setEditingSlide(s);
                              setSlideForm({
                                title: s.title,
                                imageUrl: s.imageUrl || s.backgroundImage?.url || '',
                                buttonText: s.primaryCtaText || 'Collections',
                                isActive: s.status === 'PUBLISHED',
                                sortOrder: s.sortOrder
                              });
                            }}
                            className="bg-secondary text-surface px-3 py-1.5 rounded font-bold uppercase text-[9px]"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteHeroSlide(s.id)}
                            className="bg-error text-surface px-3 py-1.5 rounded font-bold uppercase text-[9px]"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>

              </div>

            </div>
          )}

        </div>
      </main>

      {/* Admin Order Details Modal */}
      {selectedAdminOrder && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-md z-[100] flex justify-center items-center p-4 overflow-y-auto">
          <div className="bg-surface w-full max-w-4xl rounded-lg shadow-2xl overflow-y-auto max-h-[90vh] border border-outline-variant/20 relative p-6 md:p-10 text-xs text-left">
            
            {/* Header */}
            <div className="flex justify-between items-start border-b border-outline-variant/20 pb-6 mb-6">
              <div>
                <span className="text-[10px] text-primary font-bold uppercase tracking-widest block mb-1">
                  Order Details & Audit Dossier
                </span>
                <h2 className="text-2xl font-bold font-display-lg text-primary">
                  Order #{selectedAdminOrder.orderNumber}
                </h2>
                <p className="text-on-surface-variant text-[11px] mt-1">
                  Placed on {selectedAdminOrder.orderedAt || selectedAdminOrder.createdAt ? new Date(selectedAdminOrder.orderedAt || selectedAdminOrder.createdAt).toLocaleString() : 'N/A'} &nbsp;•&nbsp; Payment Method: <span className="font-bold text-primary">{selectedAdminOrder.paymentMethod || 'RAZORPAY'}</span>
                </p>
              </div>

              <button
                onClick={() => setSelectedAdminOrder(null)}
                className="text-on-surface-variant hover:text-primary transition-colors p-2"
              >
                <span className="material-symbols-outlined text-2xl">close</span>
              </button>
            </div>

            {/* Status Badges & Quick Status Updater */}
            <div className="bg-surface-container p-5 rounded border border-outline-variant/20 mb-8 flex flex-wrap justify-between items-center gap-4">
              <div className="flex items-center gap-4 flex-wrap">
                <div>
                  <span className="text-[10px] uppercase font-bold text-on-surface-variant/70 block mb-1">Payment Status</span>
                  <span className={`px-3 py-1 rounded text-[10px] font-bold uppercase tracking-wider ${
                    selectedAdminOrder.paymentStatus === 'PAID' ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/30' :
                    selectedAdminOrder.paymentStatus === 'FAILED' ? 'bg-error/10 text-error border border-error/30' :
                    'bg-amber-500/10 text-amber-700 border border-amber-500/30'
                  }`}>
                    {selectedAdminOrder.paymentStatus || 'PENDING'}
                  </span>
                </div>

                <div>
                  <span className="text-[10px] uppercase font-bold text-on-surface-variant/70 block mb-1">Fulfillment Status</span>
                  <span className={`px-3 py-1 rounded text-[10px] font-bold uppercase tracking-wider ${
                    selectedAdminOrder.fulfillmentStatus === 'DELIVERED' ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/30' :
                    selectedAdminOrder.fulfillmentStatus === 'CANCELLED' ? 'bg-error/10 text-error border border-error/30' :
                    'bg-primary/10 text-primary border border-primary/30'
                  }`}>
                    {selectedAdminOrder.fulfillmentStatus}
                  </span>
                </div>
              </div>

              {/* Quick Fulfillment Status Changer */}
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-bold text-on-surface">Update Status:</span>
                <select
                  value={selectedAdminOrder.fulfillmentStatus}
                  onChange={(e) => handleUpdateOrderStatus(selectedAdminOrder.orderNumber, e.target.value)}
                  className="bg-surface border border-outline-variant rounded p-2 text-xs font-bold text-primary outline-none focus:border-primary"
                >
                  <option value="PENDING">PENDING</option>
                  <option value="CONFIRMED">CONFIRMED</option>
                  <option value="SHIPPED">SHIPPED</option>
                  <option value="DELIVERED">DELIVERED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </div>
            </div>

            {/* Customer & Address Information Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              
              {/* Customer Profile Card */}
              <div className="bg-surface-container-lowest p-5 rounded border border-outline-variant/20 space-y-2">
                <h4 className="font-bold text-xs uppercase tracking-wider text-primary border-b border-outline-variant/10 pb-2 mb-3">
                  Customer Account
                </h4>
                <p className="font-bold text-sm text-on-surface">
                  {selectedAdminOrder.userName || selectedAdminOrder.customerName || selectedAdminOrder.shippingFullName || 'Guest User'}
                </p>
                {selectedAdminOrder.userEmail && (
                  <p className="text-[11px] text-on-surface-variant flex items-center gap-1.5">
                    <span className="material-symbols-outlined text-[14px]">mail</span>
                    {selectedAdminOrder.userEmail}
                  </p>
                )}
                <p className="text-[10px] text-primary/80 font-bold uppercase tracking-wider pt-1">
                  Account Type: {selectedAdminOrder.userEmail ? 'Registered Member' : 'Guest Checkout'}
                </p>
              </div>

              {/* Shipping Address Card */}
              <div className="bg-surface-container-lowest p-5 rounded border border-outline-variant/20 space-y-1.5">
                <h4 className="font-bold text-xs uppercase tracking-wider text-primary border-b border-outline-variant/10 pb-2 mb-3">
                  Shipping Destination
                </h4>
                <p className="font-bold text-on-surface">{selectedAdminOrder.shippingFullName}</p>
                <p className="text-on-surface-variant">{selectedAdminOrder.shippingAddressLine1}</p>
                {selectedAdminOrder.shippingAddressLine2 && <p className="text-on-surface-variant">{selectedAdminOrder.shippingAddressLine2}</p>}
                <p className="text-on-surface-variant">
                  {selectedAdminOrder.shippingCity}, {selectedAdminOrder.shippingState} - {selectedAdminOrder.shippingPostalCode}
                </p>
                <p className="text-on-surface-variant font-medium">{selectedAdminOrder.shippingCountry}</p>
                <p className="text-[11px] text-on-surface-variant pt-1 flex items-center gap-1">
                  <span className="material-symbols-outlined text-[14px]">call</span>
                  {selectedAdminOrder.shippingPhoneNumber}
                </p>
              </div>

              {/* Billing Address Card */}
              <div className="bg-surface-container-lowest p-5 rounded border border-outline-variant/20 space-y-1.5">
                <h4 className="font-bold text-xs uppercase tracking-wider text-primary border-b border-outline-variant/10 pb-2 mb-3">
                  Billing Address
                </h4>
                <p className="font-bold text-on-surface">{selectedAdminOrder.billingFullName || selectedAdminOrder.shippingFullName}</p>
                <p className="text-on-surface-variant">{selectedAdminOrder.billingAddressLine1 || selectedAdminOrder.shippingAddressLine1}</p>
                {(selectedAdminOrder.billingAddressLine2 || selectedAdminOrder.shippingAddressLine2) && (
                  <p className="text-on-surface-variant">{selectedAdminOrder.billingAddressLine2 || selectedAdminOrder.shippingAddressLine2}</p>
                )}
                <p className="text-on-surface-variant">
                  {selectedAdminOrder.billingCity || selectedAdminOrder.shippingCity}, {selectedAdminOrder.billingState || selectedAdminOrder.shippingState} - {selectedAdminOrder.billingPostalCode || selectedAdminOrder.shippingPostalCode}
                </p>
                <p className="text-on-surface-variant font-medium">{selectedAdminOrder.billingCountry || selectedAdminOrder.shippingCountry}</p>
                <p className="text-[11px] text-on-surface-variant pt-1 flex items-center gap-1">
                  <span className="material-symbols-outlined text-[14px]">call</span>
                  {selectedAdminOrder.billingPhoneNumber || selectedAdminOrder.shippingPhoneNumber}
                </p>
              </div>

            </div>

            {/* Line Items Table */}
            <div className="mb-8 border border-outline-variant/20 rounded overflow-hidden">
              <div className="bg-surface-container-low px-6 py-4 border-b border-outline-variant/20 font-bold uppercase tracking-wider text-[11px] text-primary">
                Purchased Leather Works ({selectedAdminOrder.items?.length || 0} Items)
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-surface-container-lowest border-b border-outline-variant/20 text-[10px] uppercase tracking-wider text-on-surface-variant font-bold">
                      <th className="py-3 px-6">Product</th>
                      <th className="py-3 px-4">SKU / Options</th>
                      <th className="py-3 px-4 text-center">Qty</th>
                      <th className="py-3 px-4 text-right">Unit Price</th>
                      <th className="py-3 px-6 text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-outline-variant/10 text-xs">
                    {selectedAdminOrder.items?.map((item) => (
                      <tr key={item.id} className="hover:bg-primary/5 transition-colors">
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-3">
                            {item.primaryImageUrl && (
                              <img
                                src={item.primaryImageUrl}
                                alt={item.productName}
                                className="w-12 h-12 object-cover rounded shadow-sm border border-outline-variant/20 shrink-0"
                              />
                            )}
                            <div>
                              <p className="font-bold text-primary">{item.productName}</p>
                              <p className="text-[10px] text-on-surface-variant">Slug: {item.slug}</p>
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-4 text-on-surface-variant">
                          <p className="font-mono text-[11px] text-on-surface">{item.sku}</p>
                          {(item.color || item.leatherType) && (
                            <p className="text-[10px] opacity-75 mt-0.5">
                              {item.color} {item.leatherType ? `• ${item.leatherType}` : ''}
                            </p>
                          )}
                        </td>
                        <td className="py-4 px-4 text-center font-bold text-on-surface">{item.quantity}</td>
                        <td className="py-4 px-4 text-right text-on-surface-variant">₹{item.unitPrice?.toFixed(2)}</td>
                        <td className="py-4 px-6 text-right font-bold text-primary">₹{item.subtotal?.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Financial Calculation & Timeline Grid */}
            <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-start mb-8">
              
              {/* Order Audit Timeline Log */}
              <div className="md:col-span-7 bg-surface-container-lowest p-5 rounded border border-outline-variant/20">
                <h4 className="font-bold text-xs uppercase tracking-wider text-primary border-b border-outline-variant/10 pb-2 mb-4">
                  Order Audit Timeline Log
                </h4>
                {!selectedAdminOrder.timeline || selectedAdminOrder.timeline.length === 0 ? (
                  <p className="text-on-surface-variant italic text-[11px]">No timeline events recorded.</p>
                ) : (
                  <div className="space-y-4">
                    {selectedAdminOrder.timeline.map((evt) => (
                      <div key={evt.id} className="relative pl-4 border-l-2 border-primary/30 space-y-1">
                        <div className="flex justify-between items-center text-[10px] font-bold">
                          <span className="text-primary uppercase tracking-wider">
                            {evt.newFulfillmentStatus || evt.newPaymentStatus}
                          </span>
                          <span className="text-on-surface-variant/70">
                            {new Date(evt.timestamp).toLocaleString()}
                          </span>
                        </div>
                        <p className="text-on-surface-variant text-[11px]">{evt.remarks}</p>
                        <p className="text-[9px] text-outline uppercase font-semibold">Actor: {evt.changedBy}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Financial Breakdown Table */}
              <div className="md:col-span-5 bg-surface-container p-5 rounded border border-outline-variant/20 space-y-3">
                <h4 className="font-bold text-xs uppercase tracking-wider text-primary border-b border-outline-variant/10 pb-2 mb-3">
                  Payment Summary
                </h4>
                <div className="flex justify-between text-on-surface-variant">
                  <span>Items Subtotal</span>
                  <span className="font-semibold text-on-surface">₹{selectedAdminOrder.subtotal?.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-on-surface-variant">
                  <span>Shipping Fee</span>
                  <span className="font-semibold text-on-surface">
                    {selectedAdminOrder.shippingFee === 0 ? 'FREE' : `₹${selectedAdminOrder.shippingFee?.toFixed(2)}`}
                  </span>
                </div>
                <div className="flex justify-between text-on-surface-variant">
                  <span>Estimated Tax (5%)</span>
                  <span className="font-semibold text-on-surface">₹{selectedAdminOrder.taxFee?.toFixed(2)}</span>
                </div>
                {selectedAdminOrder.discountAmount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-bold">
                    <span>Discount ({selectedAdminOrder.couponCode})</span>
                    <span>-₹{selectedAdminOrder.discountAmount?.toFixed(2)}</span>
                  </div>
                )}
                <div className="border-t border-outline-variant/20 pt-3 flex justify-between items-center text-sm font-bold text-primary">
                  <span>Grand Total</span>
                  <span className="text-base font-display-lg text-primary">₹{selectedAdminOrder.grandTotal?.toFixed(2)}</span>
                </div>
              </div>

            </div>

            {/* Footer Actions */}
            <div className="flex justify-end pt-4 border-t border-outline-variant/20">
              <button
                onClick={() => setSelectedAdminOrder(null)}
                className="bg-primary text-surface px-6 py-2.5 rounded font-bold text-xs uppercase tracking-wider hover:bg-primary-container transition-colors"
              >
                Close Dossier
              </button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
}
