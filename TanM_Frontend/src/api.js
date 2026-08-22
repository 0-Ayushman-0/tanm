const BASE_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api');

const getHeaders = () => {
  const headers = {
    'Content-Type': 'application/json',
  };
  const token = localStorage.getItem('token');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const guestToken = localStorage.getItem('guest_token');
  if (guestToken) {
    headers['Guest-Token'] = guestToken;
  }
  return headers;
};

export const apiCall = async (endpoint, options = {}) => {
  const url = `${BASE_URL}${endpoint}`;
  const mergedOptions = {
    ...options,
    headers: {
      ...getHeaders(),
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, mergedOptions);
    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token');
      }
      const errData = await response.json().catch(() => ({}));
      if (errData.validationErrors && Object.keys(errData.validationErrors).length > 0) {
        const details = Object.entries(errData.validationErrors)
          .map(([field, msg]) => `• ${field}: ${msg}`)
          .join('\n');
        const customError = new Error(`${errData.message || 'Validation error:'}\n${details}`);
        customError.validationErrors = errData.validationErrors;
        throw customError;
      }
      throw new Error(errData.message || `API error (status ${response.status})`);
    }
    if (response.status === 204) {
      return null;
    }
    return await response.json();
  } catch (error) {
    console.error(`API Call failed for ${endpoint}:`, error);
    throw error;
  }
};

export const authApi = {
  login: async (email, password) => {
    const data = await apiCall('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (data.token) {
      localStorage.setItem('token', data.token);
    }
    return data;
  },
  register: (firstName, lastName, email, password) =>
    apiCall('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ firstName, lastName, email, password }),
    }),
  verifyEmail: (email, otpCode) =>
    apiCall('/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify({ email, otpCode }),
    }),
  getMe: () => apiCall('/auth/me'),
  updateProfile: (profile) =>
    apiCall('/auth/profile', {
      method: 'PUT',
      body: JSON.stringify(profile),
    }),
  changePassword: (currentPassword, newPassword) =>
    apiCall('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword }),
    }),
  devBypass: (email) =>
    apiCall(`/auth/dev-bypass?email=${encodeURIComponent(email)}`, {
      method: 'POST',
    }),
};

export const productApi = {
  getAll: (page = 0, size = 12) => apiCall(`/products?page=${page}&size=${size}`),
  getByCategory: (categoryId, page = 0, size = 12) =>
    apiCall(`/products/category/${categoryId}?page=${page}&size=${size}`),
  getById: (id) => apiCall(`/products/${id}`),
  getBySlug: (slug) => apiCall(`/products/${slug}`),
  filter: (params = {}) => {
    const query = new URLSearchParams();
    Object.keys(params).forEach((key) => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        query.append(key, params[key]);
      }
    });
    return apiCall(`/products/filter?${query.toString()}`);
  },
  getCategories: (page = 0, size = 100) => apiCall(`/categories?page=${page}&size=${size}`),
  getCollections: (page = 0, size = 100) => apiCall(`/collections?page=${page}&size=${size}`),
  // ── Admin CRUD ──
  create: (data) => apiCall('/products', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => apiCall(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  remove: (id) => apiCall(`/products/${id}`, { method: 'DELETE' }),
  addImage: (id, imageUrl, isPrimary = false) =>
    apiCall(`/products/${id}/images`, { method: 'POST', body: JSON.stringify({ imageUrl, isPrimary }) }),
  addImagesBulk: (id, requests) =>
    apiCall(`/products/${id}/images/bulk`, { method: 'POST', body: JSON.stringify(requests) }),
  deleteImage: (id, imageId) => apiCall(`/products/${id}/images/${imageId}`, { method: 'DELETE' }),
  setPrimaryImage: (id, imageId) => apiCall(`/products/${id}/images/${imageId}/primary`, { method: 'PUT' }),
};

export const categoryApi = {
  getAll: (page = 0, size = 100) => apiCall(`/categories?page=${page}&size=${size}`),
  create: (data) => apiCall('/categories', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => apiCall(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  remove: (id) => apiCall(`/categories/${id}`, { method: 'DELETE' }),
};

export const cartApi = {
  get: () => {
    const hasToken = !!localStorage.getItem('token');
    const guestToken = localStorage.getItem('guest_token');
    if (!hasToken && !guestToken) {
      localStorage.setItem('guest_token', 'guest_' + Math.random().toString(36).substr(2, 9));
    }
    return apiCall('/cart');
  },
  addItem: (productId, quantity = 1) =>
    apiCall('/cart/items', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity }),
    }),
  updateItem: (itemId, quantity) =>
    apiCall(`/cart/items/${itemId}`, {
      method: 'PATCH',
      body: JSON.stringify({ quantity }),
    }),
  removeItem: (itemId) =>
    apiCall(`/cart/items/${itemId}`, {
      method: 'DELETE',
    }),
  clear: () =>
    apiCall('/cart', {
      method: 'DELETE',
    }),
  merge: (guestToken) =>
    apiCall('/cart/merge', {
      method: 'POST',
      body: JSON.stringify({ guestToken }),
    }),
};

export const wishlistApi = {
  get: () => apiCall('/wishlist'),
  add: (productId) =>
    apiCall(`/wishlist/items/${productId}`, {
      method: 'POST',
    }),
  remove: (productId) =>
    apiCall(`/wishlist/items/${productId}`, {
      method: 'DELETE',
    }),
  moveToCart: (productId, quantity = 1) =>
    apiCall(`/wishlist/items/${productId}/move-to-cart?quantity=${quantity}`, {
      method: 'POST',
    }),
  clear: () =>
    apiCall('/wishlist', {
      method: 'DELETE',
    }),
};

export const couponApi = {
  validate: (code, cartTotal) =>
    apiCall('/coupons/validate', {
      method: 'POST',
      body: JSON.stringify({ code, cartTotal }),
    }),
};

export const reviewApi = {
  getByProduct: (productId, page = 0, size = 5) =>
    apiCall(`/products/${productId}/reviews?page=${page}&size=${size}`),
  getSummary: (productId) => apiCall(`/products/${productId}/reviews/summary`),
  add: (productId, rating, comment, mediaIds = []) =>
    apiCall(`/products/${productId}/reviews`, {
      method: 'POST',
      body: JSON.stringify({ rating, comment, mediaIds }),
    }),
  toggleHelpful: (reviewId) =>
    apiCall(`/reviews/${reviewId}/helpful`, {
      method: 'POST',
    }),
};

export const orderApi = {
  create: (shippingAddressId, billingAddressId, paymentMethod = 'RAZORPAY', couponCode = null) =>
    apiCall('/orders', {
      method: 'POST',
      body: JSON.stringify({ shippingAddressId, billingAddressId, paymentMethod, couponCode }),
    }),
  getMyOrders: (page = 0, size = 10) => apiCall(`/orders?page=${page}&size=${size}`),
  getDetails: (orderNumber) => apiCall(`/orders/${orderNumber}`),
  cancel: (orderNumber) =>
    apiCall(`/orders/${orderNumber}/cancel`, {
      method: 'POST',
    }),
  pay: (orderNumber, paymentId = 'pay_mock_123', rpayOrderId = 'ord_mock_123', reference = 'ref_mock_123') =>
    apiCall(`/orders/${orderNumber}/pay?paymentId=${paymentId}&rpayOrderId=${rpayOrderId}&reference=${reference}`, {
      method: 'POST',
    }),
};

export const addressApi = {
  getAll: () => apiCall('/addresses'),
  create: (address) =>
    apiCall('/addresses', {
      method: 'POST',
      body: JSON.stringify(address),
    }),
  update: (id, address) =>
    apiCall(`/addresses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(address),
    }),
  delete: (id) =>
    apiCall(`/addresses/${id}`, {
      method: 'DELETE',
    }),
};

export const cmsApi = {
  getHydration: () => apiCall('/cms/hydration'),
};

export const adminApi = {
  getDashboardSummary: () => apiCall('/admin/dashboard/summary'),
  getRevenueTrends: () => apiCall('/admin/dashboard/revenue-trends'),
  getOrderTrends: () => apiCall('/admin/dashboard/order-trends'),
  getTopProducts: (limit = 5) => apiCall(`/admin/dashboard/top-products?limit=${limit}`),
  getInventoryAlerts: () => apiCall('/admin/dashboard/inventory-alerts'),
  adjustStock: (productId, newStockQuantity, adjustmentType, reason) =>
    apiCall('/admin/inventory/adjust', {
      method: 'POST',
      body: JSON.stringify({ productId, newStockQuantity, adjustmentType, reason }),
    }),
  getInventoryAuditLogs: (page = 0, size = 20) =>
    apiCall(`/admin/inventory/audit?page=${page}&size=${size}`),
  getAllOrders: (page = 0, size = 10) => apiCall(`/admin/orders?page=${page}&size=${size}`),
  getOrderByNumber: (orderNumber) => apiCall(`/admin/orders/${orderNumber}`),
  updateOrderStatus: (orderNumber, status) =>
    apiCall(`/admin/orders/${orderNumber}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ fulfillmentStatus: status }),
    }),
  getReviewsQueue: (page = 0, size = 20) => apiCall(`/admin/reviews?page=${page}&size=${size}`),
  updateReviewStatus: (reviewId, status) =>
    apiCall(`/admin/reviews/${reviewId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
  getCmsHeroSlides: () => apiCall('/admin/cms/hero'),
  createCmsHeroSlide: (dto) =>
    apiCall('/admin/cms/hero', {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
  updateCmsHeroSlide: (id, dto) =>
    apiCall(`/admin/cms/hero/${id}`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    }),
  deleteCmsHeroSlide: (id) =>
    apiCall(`/admin/cms/hero/${id}`, {
      method: 'DELETE',
    }),
  getCmsAnnouncements: () => apiCall('/admin/cms/announcements'),
  createCmsAnnouncement: (dto) =>
    apiCall('/admin/cms/announcements', {
      method: 'POST',
      body: JSON.stringify(dto),
    }),
  updateCmsAnnouncement: (id, dto) =>
    apiCall(`/admin/cms/announcements/${id}`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    }),
  deleteCmsAnnouncement: (id) =>
    apiCall(`/admin/cms/announcements/${id}`, {
      method: 'DELETE',
    }),
};

export const uploadApi = {
  uploadImage: async (file, folder = 'products') => {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);

    const response = await fetch(`${BASE_URL}/upload/image`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: formData,
    });

    if (!response.ok) {
      const errData = await response.json().catch(() => ({}));
      throw new Error(errData.message || 'Image upload failed');
    }
    return await response.json();
  },
  uploadImages: async (files, folder = 'products') => {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    Array.from(files).forEach((file) => formData.append('files', file));
    formData.append('folder', folder);

    const response = await fetch(`${BASE_URL}/upload/images`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: formData,
    });

    if (!response.ok) {
      const errData = await response.json().catch(() => ({}));
      throw new Error(errData.message || 'Batch image upload failed');
    }
    return await response.json();
  },
  deleteImage: (publicId) =>
    apiCall(`/upload/image?publicId=${encodeURIComponent(publicId)}`, {
      method: 'DELETE',
    }),
};
