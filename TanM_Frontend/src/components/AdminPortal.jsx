import React, { useState, useEffect } from 'react';
import { adminApi, productApi, orderApi, categoryApi, uploadApi } from '../api';
import { useToast } from '../context/ToastContext';

export default function AdminPortal() {
  const toast = useToast();
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
    isSignatureCollection: false,
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
    text: '',
    isActive: true,
    backgroundColor: '#000000',
    textColor: '#FFFFFF',
    linkUrl: '',
  });
  const [savingCms, setSavingCms] = useState(false);

  // Dynamic CMS Pages State
  const [cmsSubTab, setCmsSubTab] = useState('homepage'); // 'homepage' | 'pages'
  const [cmsPages, setCmsPages] = useState([]);
  const [_loadingCmsPages, setLoadingCmsPages] = useState(false);
  const [editingCmsPage, setEditingCmsPage] = useState(null); // null, 'new', or page object
  const [pageForm, setPageForm] = useState({
    id: null,
    title: '',
    slug: '',
    content: '',
    status: 'PUBLISHED',
  });
  const [savingPage, setSavingPage] = useState(false);

  // Craft of Patience CMS State
  const [craftForm, setCraftForm] = useState({
    image1: 'https://res.cloudinary.com/tanm/image/upload/v1793431372/tanm/cms/ryqx17zfukzuzkqbjtzw.jpg',
    image2: 'https://res.cloudinary.com/tanm/image/upload/v1793431375/tanm/cms/t0gxzpcotyhlvzbkdfzm.jpg',
    subtitle: 'Our Philosophy',
    title: 'The Craft of Patience',
    description: 'In an era of fleeting trends, TanM stands for the permanent. Each piece is born from a dialogue between artisan and material — a process that cannot be rushed, only perfected through decades of heritage techniques and singular focus.',
  });
  const [savingCraft, setSavingCraft] = useState(false);
  const [uploadingCraft1, setUploadingCraft1] = useState(false);
  const [uploadingCraft2, setUploadingCraft2] = useState(false);

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
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
    if (activeTab === 'pages') loadCmsPages();
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
        isSignatureCollection: Boolean(categoryForm.isSignatureCollection),
      };
      if (editingCategory && editingCategory.id) {
        await categoryApi.update(editingCategory.id, payload);
        toast.success('Category updated successfully!');
      } else {
        await categoryApi.create(payload);
        toast.success('Category created successfully!');
      }
      setEditingCategory(null);
      setCategoryFormError('');
      loadCategories();
    } catch (err) {
      setCategoryFormError(err.message || 'Failed to save category');
      toast.error(err.message || 'Failed to save category');
    }
  };

  const handleDeleteCategory = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category?')) return;
    try {
      await categoryApi.remove(id);
      toast.success('Category deleted successfully!');
      loadCategories();
    } catch (err) {
      toast.error(err.message || 'Failed to delete category');
    }
  };

  const handleFileUpload = async (file, folder, onSuccess) => {
    if (!file) return;
    setUploadingImage(true);
    try {
      const result = await uploadApi.uploadImage(file, folder);
      onSuccess(result.url);
      toast.success('Image uploaded to Cloudinary');
    } catch (err) {
      toast.error(err.message || 'Failed to upload image to Cloudinary');
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
      toast.success(`${results.length} images uploaded to Cloudinary`);
    } catch (err) {
      toast.error(err.message || 'Failed to upload batch images to Cloudinary');
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
        toast.success('Product updated successfully!');
      } else {
        const created = await productApi.create(payload);
        toast.success('Product created successfully!');

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
      toast.error(err.message || 'Failed to save product');
    }
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await productApi.remove(id);
      toast.success('Product deleted!');
      loadProducts();
    } catch (err) {
      toast.error(err.message || 'Failed to delete product');
    }
  };

  const handleAddProductImage = async (productId) => {
    if (!newProductImageUrl) return;
    try {
      await productApi.addImage(productId, newProductImageUrl, isPrimaryImage);
      toast.success('Image added to product!');
      setNewProductImageUrl('');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      toast.error(err.message || 'Failed to add image');
    }
  };

  const handleDeleteProductImage = async (productId, imageId) => {
    if (!window.confirm('Delete image?')) return;
    try {
      await productApi.deleteImage(productId, imageId);
      toast.success('Image deleted!');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      toast.error(err.message || 'Failed to delete image');
    }
  };

  const handleSetPrimaryProductImage = async (productId, imageId) => {
    try {
      await productApi.setPrimaryImage(productId, imageId);
      toast.success('Primary image set!');
      loadProducts();
      if (editingProduct && editingProduct.id === productId) {
        const updated = await productApi.getById(productId);
        setEditingProduct(updated);
      }
    } catch (err) {
      toast.error(err.message || 'Failed to set primary image');
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
      toast.success('Stock adjusted and audit log recorded!');
      setNewQty('');
      setAdjReason('');
      setSelectedProduct(null);
      loadProducts();
      loadAuditLogs();
    } catch (err) {
      toast.error(err.message || 'Failed to adjust stock');
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
      toast.success(`Review marked as ${status}`);
      loadReviewsQueue();
    } catch (err) {
      toast.error(err.message || 'Failed to update review status');
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
      toast.success(`Order status updated to ${status}`);
      if (selectedAdminOrder && selectedAdminOrder.orderNumber === orderNumber) {
        setSelectedAdminOrder(updated);
      }
      loadOrders();
    } catch (err) {
      toast.error(err.message || 'Failed to update order status');
    }
  };

  // CMS Studio CRUD Actions
  const loadCmsData = async () => {
    try {
      const [slidesPage, annPage, settingsRes] = await Promise.all([
        adminApi.getCmsHeroSlides(),
        adminApi.getCmsAnnouncements(),
        adminApi.getCmsSettings().catch(() => [])
      ]);
      setSlides(slidesPage.content || []);
      const annList = annPage.content || [];
      setAnnouncements(annList);
      if (annList.length > 0) {
        setAnnForm({
          id: annList[0].id,
          text: annList[0].text || '',
          isActive: annList[0].status === 'PUBLISHED',
          backgroundColor: annList[0].backgroundColor || '#000000',
          textColor: annList[0].textColor || '#FFFFFF',
          linkUrl: annList[0].linkUrl || '',
        });
      }
      if (Array.isArray(settingsRes)) {
        const map = {};
        settingsRes.forEach((s) => { map[s.key] = s.value; });
        setCraftForm((prev) => ({
          image1: map['craft_image_1'] || prev.image1,
          image2: map['craft_image_2'] || prev.image2,
          subtitle: map['craft_subtitle'] || prev.subtitle,
          title: map['craft_title'] || prev.title,
          description: map['craft_description'] || prev.description,
        }));
      }
      loadCmsPages();
    } catch (err) {
      console.error('CMS load failed', err);
    }
  };

  const loadCmsPages = async () => {
    setLoadingCmsPages(true);
    try {
      const res = await adminApi.getCmsPages(0, 50);
      setCmsPages(res?.content || []);
    } catch (err) {
      console.warn('CMS pages load failed', err);
    } finally {
      setLoadingCmsPages(false);
    }
  };

  const handleOpenEditPage = (pageItem) => {
    if (pageItem && pageItem.id) {
      setPageForm({
        id: pageItem.id,
        title: pageItem.title || '',
        slug: pageItem.slug || '',
        content: pageItem.content || '',
        status: pageItem.status || 'PUBLISHED',
      });
      setEditingCmsPage(pageItem);
    } else if (pageItem && pageItem.slug) {
      const existing = cmsPages.find((p) => p.slug === pageItem.slug);
      if (existing) {
        setPageForm({
          id: existing.id,
          title: existing.title || pageItem.title,
          slug: existing.slug || pageItem.slug,
          content: existing.content || '',
          status: existing.status || 'PUBLISHED',
        });
        setEditingCmsPage(existing);
      } else {
        setPageForm({
          id: null,
          title: pageItem.title || '',
          slug: pageItem.slug || '',
          content: pageItem.defaultContent || '',
          status: 'PUBLISHED',
        });
        setEditingCmsPage(pageItem);
      }
    } else {
      setPageForm({
        id: null,
        title: '',
        slug: '',
        content: '',
        status: 'PUBLISHED',
      });
      setEditingCmsPage('new');
    }
  };

  const handleSavePage = async (e) => {
    if (e) e.preventDefault();
    setSavingPage(true);
    try {
      const cleanedSlug = pageForm.slug.toLowerCase().trim().replace(/[^a-z0-9-]/g, '-');
      const payload = {
        title: pageForm.title,
        slug: cleanedSlug,
        content: pageForm.content,
        status: pageForm.status || 'PUBLISHED',
      };

      let saved;
      if (pageForm.id) {
        saved = await adminApi.updateCmsPage(pageForm.id, payload);
      } else {
        const existing = cmsPages.find((p) => p.slug === cleanedSlug);
        if (existing) {
          saved = await adminApi.updateCmsPage(existing.id, payload);
        } else {
          saved = await adminApi.createCmsPage(payload);
        }
      }

      if (saved && saved.id && payload.status === 'PUBLISHED') {
        try {
          await adminApi.publishCmsPage(saved.id);
        } catch {
          // ignore if already published
        }
      }

      toast.success(`Page "${payload.title}" saved and published successfully!`);
      setEditingCmsPage(null);
      loadCmsPages();
    } catch (err) {
      toast.error(err.message || 'Failed to save page');
    } finally {
      setSavingPage(false);
    }
  };

  const handleDeletePage = async (id, title) => {
    if (!window.confirm(`Are you sure you want to delete "${title}"?`)) return;
    try {
      await adminApi.deleteCmsPage(id);
      toast.success('Page deleted successfully');
      loadCmsPages();
    } catch (err) {
      toast.error(err.message || 'Failed to delete page');
    }
  };

  const handleSaveCraftSection = async (e) => {
    e.preventDefault();
    setSavingCraft(true);
    try {
      const payload = [
        { key: 'craft_image_1', value: craftForm.image1, settingGroup: 'craft_section', valueType: 'IMAGE_URL' },
        { key: 'craft_image_2', value: craftForm.image2, settingGroup: 'craft_section', valueType: 'IMAGE_URL' },
        { key: 'craft_subtitle', value: craftForm.subtitle, settingGroup: 'craft_section', valueType: 'STRING' },
        { key: 'craft_title', value: craftForm.title, settingGroup: 'craft_section', valueType: 'STRING' },
        { key: 'craft_description', value: craftForm.description, settingGroup: 'craft_section', valueType: 'TEXT' },
      ];
      await adminApi.updateBulkCmsSettings(payload);
      toast.success('"The Craft of Patience" section saved successfully!');
    } catch (err) {
      toast.error(err.message || 'Failed to save section settings');
    } finally {
      setSavingCraft(false);
    }
  };

  const handleSaveAnnouncement = async (e) => {
    if (e) e.preventDefault();
    setSavingCms(true);
    try {
      const payload = {
        text: annForm.text || 'Free Shipping on Orders Over ₹5,000 · Use Code TANM10',
        status: annForm.isActive ? 'PUBLISHED' : 'DRAFT',
        backgroundColor: annForm.backgroundColor || '#000000',
        textColor: annForm.textColor || '#FFFFFF',
        linkUrl: annForm.linkUrl || null,
      };
      if (annForm.id) {
        await adminApi.updateCmsAnnouncement(annForm.id, payload);
      } else {
        const created = await adminApi.createCmsAnnouncement(payload);
        setAnnForm((prev) => ({ ...prev, id: created.id }));
      }
      toast.success(annForm.isActive ? 'Store announcement saved and live!' : 'Store announcement saved (currently OFF)');
      loadCmsData();
    } catch (err) {
      toast.error(err.message || 'Failed to save announcement bar');
    } finally {
      setSavingCms(false);
    }
  };

  const handleToggleAnnouncementActive = async () => {
    const nextState = !annForm.isActive;
    setAnnForm((prev) => ({ ...prev, isActive: nextState }));
    setSavingCms(true);
    try {
      const payload = {
        text: annForm.text || 'Free Shipping on Orders Over ₹5,000 · Use Code TANM10',
        status: nextState ? 'PUBLISHED' : 'DRAFT',
        backgroundColor: annForm.backgroundColor || '#000000',
        textColor: annForm.textColor || '#FFFFFF',
        linkUrl: annForm.linkUrl || null,
      };
      if (annForm.id) {
        await adminApi.updateCmsAnnouncement(annForm.id, payload);
      } else {
        const created = await adminApi.createCmsAnnouncement(payload);
        setAnnForm((prev) => ({ ...prev, id: created.id }));
      }
      toast.success(nextState ? 'Storefront Announcement Bar turned ON!' : 'Storefront Announcement Bar turned OFF!');
      loadCmsData();
    } catch (err) {
      toast.error(err.message || 'Failed to toggle announcement bar');
      setAnnForm((prev) => ({ ...prev, isActive: !nextState }));
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
      toast.success('Hero slide configurations saved successfully!');
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
      toast.error(err.message || 'Failed to save slide');
    } finally {
      setSavingCms(false);
    }
  };

  const handleDeleteHeroSlide = async (id) => {
    if (!window.confirm('Delete this hero slide?')) return;
    try {
      await adminApi.deleteCmsHeroSlide(id);
      toast.success('Slide deleted successfully!');
      loadCmsData();
    } catch (err) {
      toast.error(err.message || 'Failed to delete slide');
    }
  };

  return (
    <div className="flex bg-background text-on-surface min-h-screen">

      {/* Left Sidebar Navigation Shell */}
      <aside className="h-screen w-72 fixed left-0 top-0 overflow-y-auto bg-surface border-r border-outline-variant/20 z-50 py-8 px-6 flex flex-col justify-between">
        <div className="space-y-8 text-left">
          {/* Brand Header */}
          <div className="flex items-center gap-3">
            <img src="/logo-transparent.png" alt="TanM" className="h-10 w-auto object-contain" />
            <div>
              <h1 className="font-display-lg text-lg text-primary tracking-tight font-bold leading-tight">TanM Atelier</h1>
              <p className="font-label-sm text-[9px] text-on-surface-variant uppercase tracking-widest opacity-70">
                Admin Suite
              </p>
            </div>
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
              { id: 'pages', label: 'Pages & Policies', icon: 'article' },
            ].map((tab) => {
              const isSelected =
                (tab.id === 'pages' && (activeTab === 'pages' || (activeTab === 'cms' && cmsSubTab === 'pages'))) ||
                (tab.id === 'cms' && activeTab === 'cms' && cmsSubTab === 'homepage') ||
                (tab.id !== 'pages' && tab.id !== 'cms' && activeTab === tab.id);

              return (
                <button
                  key={tab.id}
                  onClick={() => {
                    if (tab.id === 'pages') {
                      setActiveTab('cms');
                      setCmsSubTab('pages');
                      setEditingCmsPage(null);
                    } else if (tab.id === 'cms') {
                      setActiveTab('cms');
                      setCmsSubTab('homepage');
                    } else {
                      setActiveTab(tab.id);
                    }
                    setEditingSlide(null);
                  }}
                  className={`w-full flex items-center gap-4 py-3 px-4 transition-all duration-300 rounded ${isSelected
                    ? 'text-primary bg-secondary-container/20 font-bold border-r-2 border-primary'
                    : 'text-on-surface-variant hover:text-primary hover:bg-secondary-container/10'
                    }`}
                >
                  <span className="material-symbols-outlined text-[20px]">{tab.icon}</span>
                  <span className="font-label-sm text-xs uppercase tracking-wider">{tab.label}</span>
                </button>
              );
            })}
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
                                    className={`font-bold px-1.5 py-0.5 rounded ${img.isPrimary ? 'bg-primary text-surface' : 'text-on-surface-variant hover:text-primary'
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
                      setCategoryForm({ name: '', description: '', imageUrl: '', displayOrder: categories.length + 1, isSignatureCollection: false });
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

                    <div className="flex items-center gap-3 p-3 bg-surface border border-outline-variant/30 rounded">
                      <input
                        type="checkbox"
                        id="isSignatureCollection"
                        checked={categoryForm.isSignatureCollection}
                        onChange={(e) => setCategoryForm({ ...categoryForm, isSignatureCollection: e.target.checked })}
                        className="w-4 h-4 accent-primary cursor-pointer"
                      />
                      <label htmlFor="isSignatureCollection" className="uppercase font-bold text-on-surface cursor-pointer select-none">
                        Feature in Signature Collections (Homepage)
                      </label>
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
                        <th className="p-4">Signature Collection</th>
                        <th className="p-4 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-outline-variant/20">
                      {loadingCategories ? (
                        <tr>
                          <td colSpan={6} className="p-8 text-center text-on-surface-variant">Loading categories...</td>
                        </tr>
                      ) : categories.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="p-8 text-center text-on-surface-variant">No categories found.</td>
                        </tr>
                      ) : (
                        categories.map((c) => (
                          <tr key={c.id} className="hover:bg-surface-container-low transition-colors">
                            <td className="p-4 flex items-center gap-3">
                              {c.imageUrl ? (
                                <img src={c.imageUrl} alt={c.name} className="w-10 h-10 object-cover rounded border border-outline-variant/30" />
                              ) : (
                                <div className="w-10 h-10 bg-surface-container rounded border border-outline-variant/30 flex items-center justify-center text-on-surface-variant shrink-0">
                                  <span className="material-symbols-outlined text-[18px] opacity-40">folder</span>
                                </div>
                              )}
                              <span className="font-bold text-primary text-sm">{c.name}</span>
                            </td>
                            <td className="p-4 text-on-surface-variant">{c.slug}</td>
                            <td className="p-4 text-on-surface-variant">{c.description || '—'}</td>
                            <td className="p-4 font-bold">{c.displayOrder}</td>
                            <td className="p-4">
                              {c.signatureCollection || c.isSignatureCollection ? (
                                <span className="bg-amber-100 text-amber-900 border border-amber-300 text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider">
                                  ★ Signature
                                </span>
                              ) : (
                                <span className="text-on-surface-variant text-[11px]">—</span>
                              )}
                            </td>
                            <td className="p-4 text-right">
                              <button
                                onClick={() => {
                                  setEditingCategory(c);
                                  setCategoryForm({
                                    name: c.name || '',
                                    description: c.description || '',
                                    imageUrl: c.imageUrl || '',
                                    displayOrder: c.displayOrder || 0,
                                    isSignatureCollection: Boolean(c.signatureCollection ?? c.isSignatureCollection ?? false),
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
                                <span className={`px-2.5 py-1 border font-bold text-[9px] tracking-widest uppercase rounded ${ord.fulfillmentStatus === 'DELIVERED'
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
                          className={`font-semibold text-sm ${log.quantityChanged >= 0 ? 'text-secondary' : 'text-error'
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
          {(activeTab === 'cms' || activeTab === 'pages') && (
            <div className="space-y-6 text-xs font-medium">

              {/* Sub-navigation: Storefront Elements vs Dynamic Pages */}
              <div className="flex flex-wrap items-center justify-between gap-4 bg-surface p-4 rounded border border-outline-variant/15 shadow-sm">
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => { setCmsSubTab('homepage'); setEditingCmsPage(null); }}
                    className={`px-4 py-2 rounded text-xs font-bold uppercase tracking-wider transition-all cursor-pointer flex items-center gap-2 ${
                      cmsSubTab === 'homepage'
                        ? 'bg-primary text-surface shadow-sm'
                        : 'bg-surface-container-low text-on-surface-variant hover:text-primary hover:bg-surface-container'
                    }`}
                  >
                    <span className="material-symbols-outlined text-[16px]">view_quilt</span>
                    Storefront & Banners
                  </button>

                  <button
                    type="button"
                    onClick={() => setCmsSubTab('pages')}
                    className={`px-4 py-2 rounded text-xs font-bold uppercase tracking-wider transition-all cursor-pointer flex items-center gap-2 ${
                      cmsSubTab === 'pages'
                        ? 'bg-primary text-surface shadow-sm'
                        : 'bg-surface-container-low text-on-surface-variant hover:text-primary hover:bg-surface-container'
                    }`}
                  >
                    <span className="material-symbols-outlined text-[16px]">article</span>
                    Dynamic Pages & Policies (3)
                  </button>
                </div>

                {cmsSubTab === 'pages' && !editingCmsPage && (
                  <button
                    type="button"
                    onClick={() => handleOpenEditPage(null)}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded bg-primary text-surface text-xs font-bold uppercase tracking-wider hover:opacity-90 active:scale-95 transition-all shadow-sm cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-[16px]">add</span>
                    Create Custom Page
                  </button>
                )}
              </div>

              {cmsSubTab === 'homepage' && (
                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">

              {/* Left Column: Announcement configuration panel */}
              <div className="lg:col-span-5 bg-surface-container-low p-6 rounded border border-outline-variant/15 h-fit space-y-6">
                <div className="flex items-center justify-between border-b border-outline-variant/15 pb-4">
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-secondary block mb-1">
                      Storefront Top Banner
                    </span>
                    <h3 className="font-headline-md text-lg text-primary font-bold">
                      Announcement Bar
                    </h3>
                  </div>
                  <span
                    className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                      annForm.isActive
                        ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/30'
                        : 'bg-outline-variant/20 text-on-surface-variant border border-outline-variant/30'
                    }`}
                  >
                    <span className={`w-2 h-2 rounded-full ${annForm.isActive ? 'bg-emerald-500 animate-pulse' : 'bg-gray-400'}`} />
                    {annForm.isActive ? 'ON / Active' : 'OFF / Inactive'}
                  </span>
                </div>

                {/* Direct On/Off Toggle Button */}
                <div className="flex items-center justify-between p-4 bg-surface rounded border border-outline-variant/20 shadow-sm">
                  <div>
                    <p className="font-bold text-xs text-primary">Storefront Display</p>
                    <p className="text-[11px] text-on-surface-variant">
                      {annForm.isActive ? 'Banner is live and visible on homepage' : 'Banner is disabled and hidden'}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={handleToggleAnnouncementActive}
                    disabled={savingCms}
                    className={`flex items-center gap-2 px-4 py-2 rounded font-bold text-xs uppercase tracking-wider transition-all cursor-pointer ${
                      annForm.isActive
                        ? 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm'
                        : 'bg-surface-variant hover:bg-outline-variant/30 text-on-surface-variant border border-outline-variant/40'
                    }`}
                  >
                    <span className="material-symbols-outlined text-[18px]">
                      {annForm.isActive ? 'toggle_on' : 'toggle_off'}
                    </span>
                    <span>{annForm.isActive ? 'TURN OFF' : 'TURN ON'}</span>
                  </button>
                </div>

                {/* Live Preview Box */}
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <label className="block font-semibold text-on-surface-variant text-[11px] uppercase tracking-wider">
                      Storefront Live Preview
                    </label>
                    <span
                      className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded ${
                        annForm.isActive ? 'bg-emerald-500/15 text-emerald-700' : 'bg-neutral-500/15 text-neutral-600'
                      }`}
                    >
                      {annForm.isActive ? '● Live on Storefront' : '○ Disabled & Hidden'}
                    </span>
                  </div>

                  {annForm.isActive ? (
                    <div
                      className="p-3 text-center rounded text-xs font-semibold uppercase tracking-widest shadow-sm border border-black/10"
                      style={{
                        backgroundColor: annForm.backgroundColor || '#000000',
                        color: annForm.textColor || '#FFFFFF',
                      }}
                    >
                      <span>{annForm.text || 'Free Shipping on Orders Over ₹5,000 · Use Code TANM10'}</span>
                    </div>
                  ) : (
                    <div className="p-4 text-center rounded text-xs font-medium bg-surface border border-outline-variant/30 text-on-surface-variant flex items-center justify-center gap-2 shadow-inner">
                      <span className="material-symbols-outlined text-[18px] text-on-surface-variant/70">visibility_off</span>
                      <span>Banner is currently turned off and will not appear to customers</span>
                    </div>
                  )}
                </div>

                {/* Form to update content text */}
                <form onSubmit={handleSaveAnnouncement} className="space-y-4 pt-2 border-t border-outline-variant/15">
                  <div>
                    <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                      Banner Message Text
                    </label>
                    <input
                      type="text"
                      required
                      value={annForm.text}
                      onChange={(e) => setAnnForm({ ...annForm, text: e.target.value })}
                      placeholder="e.g. Free shipping on all luxury totes this weekend"
                      className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface focus:outline-none focus:border-primary text-xs"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                        Background Color
                      </label>
                      <div className="flex items-center gap-2">
                        <input
                          type="color"
                          value={annForm.backgroundColor || '#000000'}
                          onChange={(e) => setAnnForm({ ...annForm, backgroundColor: e.target.value })}
                          className="h-8 w-10 p-0 rounded border border-outline/25 cursor-pointer bg-transparent"
                        />
                        <span className="text-xs font-mono text-on-surface-variant">{annForm.backgroundColor}</span>
                      </div>
                    </div>
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                        Text Color
                      </label>
                      <div className="flex items-center gap-2">
                        <input
                          type="color"
                          value={annForm.textColor || '#FFFFFF'}
                          onChange={(e) => setAnnForm({ ...annForm, textColor: e.target.value })}
                          className="h-8 w-10 p-0 rounded border border-outline/25 cursor-pointer bg-transparent"
                        />
                        <span className="text-xs font-mono text-on-surface-variant">{annForm.textColor}</span>
                      </div>
                    </div>
                  </div>

                  <div className="pt-2">
                    <button
                      type="submit"
                      disabled={savingCms}
                      className="w-full bg-primary text-surface py-2.5 rounded font-bold uppercase tracking-wider text-xs hover:bg-primary-container transition-all cursor-pointer shadow-sm disabled:opacity-50"
                    >
                      {savingCms ? 'Saving Announcement...' : 'Save Banner Changes'}
                    </button>
                  </div>
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

              {/* Row 2: "The Craft of Patience" Section Editor */}
              <div className="lg:col-span-12 bg-surface-container-low p-6 rounded border border-outline-variant/15 space-y-6">
                <div className="flex justify-between items-center border-b border-outline-variant/20 pb-4">
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-widest text-secondary block">
                      Homepage Brand Story
                    </span>
                    <h3 className="font-headline-md text-xl text-primary font-bold">
                      Section Editor
                    </h3>
                  </div>
                  <span className="text-[10px] bg-secondary/10 text-secondary border border-secondary/20 px-3 py-1 rounded font-bold uppercase">
                    Live CMS Control
                  </span>
                </div>

                <form onSubmit={handleSaveCraftSection} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Image 1 (Artisan at work) */}
                    <div className="bg-surface p-5 rounded border border-outline-variant/15 space-y-3">
                      <label className="block font-bold uppercase tracking-wider text-[10px] text-on-surface-variant">
                        Primary Image (Artisan At Work)
                      </label>
                      <div className="flex gap-2">
                        <input
                          type="url"
                          required
                          value={craftForm.image1}
                          onChange={(e) => setCraftForm({ ...craftForm, image1: e.target.value })}
                          className="flex-1 bg-surface-container border border-outline/25 rounded p-2.5 text-xs text-on-surface focus:outline-none focus:border-primary"
                          placeholder="Cloudinary Image URL..."
                        />
                        <label className="bg-primary text-surface px-4 py-2.5 rounded cursor-pointer font-bold text-xs flex items-center gap-1.5 hover:bg-primary-container shrink-0 transition-all">
                          <span className="material-symbols-outlined text-sm">cloud_upload</span>
                          {uploadingCraft1 ? 'Uploading...' : 'Upload'}
                          <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            disabled={uploadingCraft1}
                            onChange={async (e) => {
                              const file = e.target.files[0];
                              if (file) {
                                setUploadingCraft1(true);
                                try {
                                  const uploaded = await uploadApi.uploadImage(file, 'cms');
                                  setCraftForm((prev) => ({ ...prev, image1: uploaded.url }));
                                  toast.success('Image 1 uploaded to Cloudinary!');
                                } catch (err) {
                                  toast.error(err.message || 'Upload failed');
                                } finally {
                                  setUploadingCraft1(false);
                                }
                              }
                            }}
                          />
                        </label>
                      </div>
                      {craftForm.image1 && (
                        <div className="relative h-48 rounded overflow-hidden border border-outline-variant/30 shadow-inner">
                          <img src={craftForm.image1} alt="Craft 1 Preview" className="w-full h-full object-cover" />
                        </div>
                      )}
                    </div>

                    {/* Image 2 (Leather detail) */}
                    <div className="bg-surface p-5 rounded border border-outline-variant/15 space-y-3">
                      <label className="block font-bold uppercase tracking-wider text-[10px] text-on-surface-variant">
                        Secondary Image (Detail & Tools Overlay)
                      </label>
                      <div className="flex gap-2">
                        <input
                          type="url"
                          required
                          value={craftForm.image2}
                          onChange={(e) => setCraftForm({ ...craftForm, image2: e.target.value })}
                          className="flex-1 bg-surface-container border border-outline/25 rounded p-2.5 text-xs text-on-surface focus:outline-none focus:border-primary"
                          placeholder="Cloudinary Image URL..."
                        />
                        <label className="bg-primary text-surface px-4 py-2.5 rounded cursor-pointer font-bold text-xs flex items-center gap-1.5 hover:bg-primary-container shrink-0 transition-all">
                          <span className="material-symbols-outlined text-sm">cloud_upload</span>
                          {uploadingCraft2 ? 'Uploading...' : 'Upload'}
                          <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            disabled={uploadingCraft2}
                            onChange={async (e) => {
                              const file = e.target.files[0];
                              if (file) {
                                setUploadingCraft2(true);
                                try {
                                  const uploaded = await uploadApi.uploadImage(file, 'cms');
                                  setCraftForm((prev) => ({ ...prev, image2: uploaded.url }));
                                  toast.success('Image 2 uploaded to Cloudinary!');
                                } catch (err) {
                                  toast.error(err.message || 'Upload failed');
                                } finally {
                                  setUploadingCraft2(false);
                                }
                              }
                            }}
                          />
                        </label>
                      </div>
                      {craftForm.image2 && (
                        <div className="relative h-48 rounded overflow-hidden border border-outline-variant/30 shadow-inner">
                          <img src={craftForm.image2} alt="Craft 2 Preview" className="w-full h-full object-cover" />
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Text inputs */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant uppercase text-[10px] tracking-wider">
                        Philosophy Subtitle Tag
                      </label>
                      <input
                        type="text"
                        required
                        value={craftForm.subtitle}
                        onChange={(e) => setCraftForm({ ...craftForm, subtitle: e.target.value })}
                        className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface focus:outline-none focus:border-primary"
                      />
                    </div>
                    <div>
                      <label className="block font-semibold mb-1 text-on-surface-variant uppercase text-[10px] tracking-wider">
                        Section Headline Title
                      </label>
                      <input
                        type="text"
                        required
                        value={craftForm.title}
                        onChange={(e) => setCraftForm({ ...craftForm, title: e.target.value })}
                        className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface focus:outline-none focus:border-primary"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block font-semibold mb-1 text-on-surface-variant uppercase text-[10px] tracking-wider">
                      Philosophy Narrative Description
                    </label>
                    <textarea
                      rows={3}
                      required
                      value={craftForm.description}
                      onChange={(e) => setCraftForm({ ...craftForm, description: e.target.value })}
                      className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface focus:outline-none focus:border-primary"
                    />
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="submit"
                      disabled={savingCraft}
                      className="bg-primary text-surface px-8 py-3 rounded font-bold uppercase tracking-wider text-xs hover:bg-primary-container transition-all cursor-pointer"
                    >
                      {savingCraft ? 'Saving Changes...' : 'Save Section'}
                    </button>
                  </div>
                </form>
              </div>

              </div>
              )}

              {/* SUB-VIEW 2: DYNAMIC PAGES & POLICIES STUDIO */}
              {cmsSubTab === 'pages' && (
                <div className="space-y-6">
                  {editingCmsPage ? (
                    /* Page Editor View */
                    <div className="bg-surface-container-low p-6 md:p-8 rounded border border-outline-variant/15 space-y-6">
                      <div className="flex flex-wrap items-center justify-between border-b border-outline-variant/15 pb-4 gap-4">
                        <div className="flex items-center gap-3">
                          <button
                            type="button"
                            onClick={() => setEditingCmsPage(null)}
                            className="p-1.5 rounded hover:bg-surface text-on-surface-variant hover:text-primary cursor-pointer flex items-center justify-center transition-all"
                            title="Back to pages list"
                          >
                            <span className="material-symbols-outlined text-[20px]">arrow_back</span>
                          </button>
                          <div>
                            <span className="text-[10px] font-bold uppercase tracking-wider text-secondary block">
                              CMS Content Editor
                            </span>
                            <h3 className="font-headline-md text-lg text-primary font-bold">
                              {pageForm.id ? `Editing: ${pageForm.title}` : (pageForm.title ? `Customize: ${pageForm.title}` : 'Create New Dynamic Page')}
                            </h3>
                          </div>
                        </div>

                        <div className="flex items-center gap-3">
                          <button
                            type="button"
                            onClick={() => {
                              const defaultTemplate = {
                                'shipping-returns': `<h3>Payment Notice</h3>\n<p>There is no COD. For customised products, there will be 50% advance payment and rest 50% payment should be done before dispatch.</p>\n\n<h3>Exchange Policy</h3>\n<ol>\n  <li><strong>Exchange Period:</strong> Customers can request an exchange within 10 days of receiving their order.</li>\n  <li><strong>Eligible Products:</strong> Products are eligible for exchange only if they are unworn, undamaged, and returned in their original condition.</li>\n  <li><strong>Original Packaging:</strong> Products must be returned with their original packaging, tags, dust bags, accessories, and invoice.</li>\n  <li><strong>Product Condition:</strong> Signs of use or damage will not be accepted.</li>\n  <li><strong>Leather Products:</strong> Natural variations in grain/colour are not manufacturing defects.</li>\n  <li><strong>Defects:</strong> Contact TANM within 48 hours of delivery with photos/videos.</li>\n  <li><strong>Inspection:</strong> Subject to atelier approval.</li>\n  <li><strong>Shipping Charges:</strong> Customer covers return for change of mind; TANM covers for defects.</li>\n  <li><strong>Availability:</strong> Exchanges subject to stock.</li>\n  <li><strong>Sale Items:</strong> Non-exchangeable unless defective.</li>\n  <li><strong>Bespoke Items:</strong> Customised products cannot be exchanged for change of mind.</li>\n  <li><strong>How to Request:</strong> Email support@tanm.in or WhatsApp.</li>\n  <li><strong>Processing:</strong> Dispatched in 5–7 business days upon inspection.</li>\n  <li><strong>Refunds:</strong> Exchanges do not automatically qualify for refunds.</li>\n  <li><strong>Compliance:</strong> TANM reserves right to reject non-compliant returns.</li>\n  <li><strong>Payment:</strong> No COD, 50% advance on custom products.</li>\n</ol>`,
                                'about': `<h2>Introducing TANM — Timeless by Design. Crafted to Last.</h2>\n\n<p>At TANM, we believe that true luxury lies in the details. Every accessory should be more than just something you carry or wear — it should reflect your personality, complement your style, and become a part of your everyday story.</p>\n\n<p>TANM brings you a thoughtfully curated collection of 100% genuine leather bags, wallets, belts and more designed for those who appreciate timeless aesthetics, understated elegance, and versatile style.</p>\n\n<p>From everyday essentials to statement pieces, our designs combine functionality with refined craftsmanship, making them perfect for modern lifestyles and special occasions alike.</p>\n\n<p>We believe in creating accessories that go beyond fleeting trends — pieces you can cherish, style, and carry with confidence.</p>`,
                                'privacy-policy': `<h2>Privacy Policy</h2>\n<p>At TANM, we respect and safeguard the privacy of every client. This policy explains how we collect, store, and utilize your personal information.</p>\n\n<h3>1. Information We Collect</h3>\n<p>We collect your name, email, phone number, shipping address, and transaction records required to process and deliver your orders securely.</p>\n\n<h3>2. Payment Security</h3>\n<p>All transactions are protected by 256-bit SSL encryption via RBI-compliant gateways. TANM never stores your full card numbers, CVVs, or bank credentials.</p>\n\n<h3>3. Data Protection</h3>\n<p>We never sell or rent your personal details to third parties. Information is shared only with trusted logistics and fulfillment partners necessary to deliver your pieces.</p>`,
                              }[pageForm.slug];
                              if (defaultTemplate) {
                                setPageForm((prev) => ({ ...prev, content: defaultTemplate }));
                                toast.success('Loaded starting template text!');
                              } else {
                                toast.info('No pre-built template for this slug. Write your custom content below.');
                              }
                            }}
                            className="px-3 py-1.5 rounded border border-outline-variant/30 text-on-surface-variant hover:text-primary hover:bg-surface text-xs font-semibold cursor-pointer"
                          >
                            Load Template Text
                          </button>
                        </div>
                      </div>

                      <form onSubmit={handleSavePage} className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                          <div>
                            <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                              Page Title
                            </label>
                            <input
                              type="text"
                              required
                              value={pageForm.title}
                              onChange={(e) => setPageForm({ ...pageForm, title: e.target.value })}
                              placeholder="e.g. Shipping & Returns"
                              className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface focus:outline-none focus:border-primary text-xs"
                            />
                          </div>

                          <div>
                            <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                              URL Slug (Identifier)
                            </label>
                            <input
                              type="text"
                              required
                              value={pageForm.slug}
                              onChange={(e) => setPageForm({ ...pageForm, slug: e.target.value.toLowerCase().replace(/\s+/g, '-') })}
                              placeholder="e.g. shipping-returns"
                              className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface font-mono text-xs focus:outline-none focus:border-primary"
                            />
                            <span className="text-[10px] text-on-surface-variant/70 mt-1 block">
                              Direct storefront slug: /api/cms/pages/{pageForm.slug || 'slug'}
                            </span>
                          </div>

                          <div>
                            <label className="block font-semibold mb-1 text-on-surface-variant text-[11px] uppercase tracking-wider">
                              Storefront Visibility
                            </label>
                            <select
                              value={pageForm.status}
                              onChange={(e) => setPageForm({ ...pageForm, status: e.target.value })}
                              className="w-full bg-surface border border-outline/25 rounded p-2.5 text-on-surface text-xs focus:outline-none focus:border-primary"
                            >
                              <option value="PUBLISHED">PUBLISHED (Live on Storefront)</option>
                              <option value="DRAFT">DRAFT (Hidden / Preview Only)</option>
                            </select>
                          </div>
                        </div>

                        {/* Page Content Editor */}
                        <div>
                          <div className="flex items-center justify-between mb-1.5">
                            <label className="block font-semibold text-on-surface-variant text-[11px] uppercase tracking-wider">
                              Page Body Content (HTML or Markdown)
                            </label>
                            <span className="text-[10px] font-mono text-on-surface-variant/70">
                              {pageForm.content?.length || 0} characters
                            </span>
                          </div>
                          <textarea
                            rows={16}
                            required
                            value={pageForm.content}
                            onChange={(e) => setPageForm({ ...pageForm, content: e.target.value })}
                            placeholder="Write or paste your custom page content here (supports HTML formatting tags)..."
                            className="w-full bg-surface border border-outline/25 rounded p-3 text-on-surface font-mono text-xs leading-relaxed focus:outline-none focus:border-primary"
                          />
                        </div>

                        {/* Form Actions */}
                        <div className="flex items-center justify-end gap-3 pt-4 border-t border-outline-variant/15">
                          <button
                            type="button"
                            onClick={() => setEditingCmsPage(null)}
                            className="px-5 py-2.5 rounded border border-outline-variant/30 text-on-surface-variant hover:text-primary hover:bg-surface text-xs font-semibold cursor-pointer"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            disabled={savingPage}
                            className="bg-primary text-surface px-8 py-2.5 rounded font-bold uppercase tracking-wider text-xs hover:bg-primary-container transition-all cursor-pointer shadow-sm disabled:opacity-50"
                          >
                            {savingPage ? 'Saving & Publishing...' : 'Save & Publish Page'}
                          </button>
                        </div>
                      </form>
                    </div>
                  ) : (
                    /* Pages Cards List */
                    <div className="space-y-4">
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2">
                        <div>
                          <h3 className="font-headline-md text-base text-primary font-bold">
                            Official Maison & Policy Pages
                          </h3>
                          <p className="text-on-surface-variant text-[11px]">
                            These pages are dynamically served across the storefront and linked from the footer.
                          </p>
                        </div>
                        <span className="text-[10px] font-mono text-on-surface-variant bg-surface px-2.5 py-1 rounded border border-outline-variant/20">
                          3 Standard Pages Configured
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {[
                          {
                            slug: 'shipping-returns',
                            title: 'Shipping & Returns',
                            category: 'Client Services',
                            desc: '16-point Exchange Policy, COD restrictions, and 50% advance custom order rules.',
                            icon: 'local_shipping',
                          },
                          {
                            slug: 'about',
                            title: 'About TANM',
                            category: 'The Maison',
                            desc: 'Brand story, 100% genuine leather craftsmanship, and core guiding pillars.',
                            icon: 'storefront',
                          },
                          {
                            slug: 'privacy-policy',
                            title: 'Privacy Policy',
                            category: 'Legal & Compliance',
                            desc: 'Data collection, 256-bit SSL payment protection, and customer privacy rights.',
                            icon: 'policy',
                          },
                        ].map((systemPage) => {
                          const dbPage = cmsPages.find((p) => p.slug === systemPage.slug);
                          const isCustomized = !!dbPage;

                          return (
                            <div
                              key={systemPage.slug}
                              className="p-6 rounded-xl bg-surface-container-low border border-outline-variant/15 hover:border-primary/20 transition-all flex flex-col justify-between shadow-sm"
                            >
                              <div className="space-y-3">
                                <div className="flex items-center justify-between">
                                  <span className="text-[10px] font-bold uppercase tracking-widest text-secondary">
                                    {systemPage.category}
                                  </span>
                                  <span
                                    className={`text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                                      isCustomized
                                        ? 'bg-emerald-500/15 text-emerald-700 border border-emerald-500/30'
                                        : 'bg-primary/5 text-primary border border-primary/15'
                                    }`}
                                  >
                                    {isCustomized ? '● Custom CMS Active' : '★ Master Template'}
                                  </span>
                                </div>

                                <div className="flex items-center gap-2.5">
                                  <div className="w-8 h-8 rounded-full bg-primary/5 text-primary flex items-center justify-center flex-shrink-0">
                                    <span className="material-symbols-outlined text-[18px]">{systemPage.icon}</span>
                                  </div>
                                  <h4 className="font-serif font-bold text-base text-primary">
                                    {dbPage?.title || systemPage.title}
                                  </h4>
                                </div>

                                <p className="text-on-surface-variant text-[11px] leading-relaxed">
                                  {systemPage.desc}
                                </p>

                                <div className="pt-2 font-mono text-[10px] text-on-surface-variant/70 flex items-center gap-1.5">
                                  <span>Slug:</span>
                                  <code className="bg-surface px-1.5 py-0.5 rounded border border-outline-variant/20 text-primary font-bold">
                                    /{systemPage.slug}
                                  </code>
                                </div>
                              </div>

                              <div className="pt-6 border-t border-outline-variant/15 mt-6 flex items-center justify-between gap-2">
                                <button
                                  type="button"
                                  onClick={() => handleOpenEditPage(dbPage || systemPage)}
                                  className="flex-1 py-2 rounded bg-primary text-surface text-xs font-bold uppercase tracking-wider hover:bg-primary-container transition-all cursor-pointer flex items-center justify-center gap-1.5 shadow-sm"
                                >
                                  <span className="material-symbols-outlined text-[14px]">edit</span>
                                  Edit Content
                                </button>
                                {isCustomized && (
                                  <button
                                    type="button"
                                    onClick={() => handleDeletePage(dbPage.id, dbPage.title)}
                                    className="p-2 text-error hover:bg-error/10 rounded transition-all cursor-pointer"
                                    title="Revert to master template"
                                  >
                                    <span className="material-symbols-outlined text-[16px]">restore</span>
                                  </button>
                                )}
                              </div>
                            </div>
                          );
                        })}

                        {/* Additional custom created pages */}
                        {cmsPages
                          .filter((p) => !['shipping-returns', 'about', 'privacy-policy'].includes(p.slug))
                          .map((customPage) => (
                            <div
                              key={customPage.id}
                              className="p-6 rounded-xl bg-surface-container-low border border-outline-variant/15 hover:border-primary/20 transition-all flex flex-col justify-between shadow-sm"
                            >
                              <div className="space-y-3">
                                <div className="flex items-center justify-between">
                                  <span className="text-[10px] font-bold uppercase tracking-widest text-secondary">
                                    Custom Page
                                  </span>
                                  <span className="text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-700 border border-emerald-500/30">
                                    {customPage.status}
                                  </span>
                                </div>

                                <h4 className="font-serif font-bold text-base text-primary">
                                  {customPage.title}
                                </h4>

                                <div className="font-mono text-[10px] text-on-surface-variant/70 flex items-center gap-1.5">
                                  <span>Slug:</span>
                                  <code className="bg-surface px-1.5 py-0.5 rounded border border-outline-variant/20 text-primary font-bold">
                                    /{customPage.slug}
                                  </code>
                                </div>
                              </div>

                              <div className="pt-6 border-t border-outline-variant/15 mt-6 flex items-center justify-between gap-2">
                                <button
                                  type="button"
                                  onClick={() => handleOpenEditPage(customPage)}
                                  className="flex-1 py-2 rounded bg-primary text-surface text-xs font-bold uppercase tracking-wider hover:bg-primary-container transition-all cursor-pointer flex items-center justify-center gap-1.5 shadow-sm"
                                >
                                  <span className="material-symbols-outlined text-[14px]">edit</span>
                                  Edit Content
                                </button>
                                <button
                                  type="button"
                                  onClick={() => handleDeletePage(customPage.id, customPage.title)}
                                  className="p-2 text-error hover:bg-error/10 rounded transition-all cursor-pointer"
                                  title="Delete page"
                                >
                                  <span className="material-symbols-outlined text-[16px]">delete</span>
                                </button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
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
                  <span className={`px-3 py-1 rounded text-[10px] font-bold uppercase tracking-wider ${selectedAdminOrder.paymentStatus === 'PAID' ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/30' :
                    selectedAdminOrder.paymentStatus === 'FAILED' ? 'bg-error/10 text-error border border-error/30' :
                      'bg-amber-500/10 text-amber-700 border border-amber-500/30'
                    }`}>
                    {selectedAdminOrder.paymentStatus || 'PENDING'}
                  </span>
                </div>

                <div>
                  <span className="text-[10px] uppercase font-bold text-on-surface-variant/70 block mb-1">Fulfillment Status</span>
                  <span className={`px-3 py-1 rounded text-[10px] font-bold uppercase tracking-wider ${selectedAdminOrder.fulfillmentStatus === 'DELIVERED' ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/30' :
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
