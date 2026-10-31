import React, { useState, useEffect } from 'react';
import { cmsApi } from '../api';

const STATIC_CONTENT = {
  'shipping-returns': {
    category: 'Client Services',
    title: 'Shipping & Returns',
    subtitle: 'Exchange Policy & Order Fulfillment Guidelines',
    lastUpdated: 'October 2026',
    renderContent: () => (
      <div className="space-y-10">
        {/* Important Advance & COD Notice Card */}
        <div className="relative overflow-hidden rounded-2xl bg-amber-500/10 border border-amber-500/30 p-6 md:p-8 backdrop-blur-sm">
          <div className="flex items-start gap-4">
            <div className="w-10 h-10 rounded-full bg-amber-500/20 text-amber-700 flex items-center justify-center flex-shrink-0 mt-0.5">
              <span className="material-symbols-outlined text-[22px]">payments</span>
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1.5">
                <span className="text-[10px] uppercase font-bold tracking-widest text-amber-800 bg-amber-500/20 px-2 py-0.5 rounded-full">
                  Payment & Bespoke Orders Policy
                </span>
              </div>
              <h3 className="text-base md:text-lg font-serif font-bold text-amber-950">
                No Cash on Delivery (COD) · 50% Advance on Custom Orders
              </h3>
              <p className="text-sm text-amber-900/90 leading-relaxed mt-1">
                There is <strong>no Cash on Delivery (COD)</strong> option available. For all customised, personalised, 
                and specially commissioned products, an <strong>advance payment of 50%</strong> is required upon order confirmation, 
                and the remaining <strong>50% payment</strong> must be completed prior to order dispatch.
              </p>
            </div>
          </div>
        </div>

        {/* Section Header */}
        <div className="border-b border-primary/10 pb-4 flex flex-col md:flex-row md:items-end justify-between gap-2">
          <div>
            <span className="text-[11px] font-bold uppercase tracking-[0.25em] text-primary/60">
              Maison Standards
            </span>
            <h2 className="text-2xl md:text-3xl font-serif text-primary font-normal mt-1">
              Exchange Policy
            </h2>
          </div>
          <span className="text-xs text-on-surface-variant font-medium">
            16-Point Verification & Care Framework
          </span>
        </div>

        {/* 16 Policy Points Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
          {[
            {
              num: '01',
              title: 'Exchange Period',
              text: 'Customers can request an exchange within 10 days of receiving their order.',
              highlight: '10 Days Period',
            },
            {
              num: '02',
              title: 'Eligible Products',
              text: 'Products are eligible for exchange only if they are unworn, undamaged, and returned in their original condition.',
              highlight: 'Original Condition',
            },
            {
              num: '03',
              title: 'Original Packaging',
              text: 'Products must be returned with their original packaging, tags, dust bags, accessories, and invoice, wherever applicable.',
              highlight: 'Packaging & Tags Intact',
            },
            {
              num: '04',
              title: 'Product Condition',
              text: 'Products showing signs of use, scratches, stains, damage, alterations, or improper handling will not be accepted for exchange.',
              highlight: 'Strict Inspection',
            },
            {
              num: '05',
              title: 'Natural Leather Characteristics',
              text: 'As genuine leather is a natural material, slight variations in texture, grain, and colour may occur. These natural characteristics are not considered manufacturing defects.',
              highlight: '100% Genuine Leather',
            },
            {
              num: '06',
              title: 'Manufacturing Defects & Transit Issues',
              text: 'If a product has a manufacturing defect or an incorrect item has been delivered, customers must contact TANM within 48 hours of delivery, preferably with clear photos or videos.',
              highlight: 'Report within 48h',
            },
            {
              num: '07',
              title: 'Exchange Approval',
              text: 'All exchange requests are subject to inspection and approval by the TANM team upon physical receipt at our atelier.',
              highlight: 'Atelier Inspection',
            },
            {
              num: '08',
              title: 'Exchange Shipping Charges',
              text: 'For exchanges due to customer preference, such as a change of mind, the customer will bear the applicable shipping charges. For confirmed manufacturing defects or incorrect deliveries, TANM will cover the return and replacement shipping costs.',
              highlight: 'Customer vs. Maison Shipping',
            },
            {
              num: '09',
              title: 'Product Availability',
              text: 'Exchanges are subject to product availability. If the requested replacement is unavailable, TANM will offer an appropriate alternative or another mutually agreed resolution.',
              highlight: 'Inventory Dependent',
            },
            {
              num: '10',
              title: 'Sale & Promotional Items',
              text: 'Products purchased during sales or promotional offers are not eligible for exchange unless defective, incorrectly delivered, or otherwise required by applicable law.',
              highlight: 'Final Sale Exception',
            },
            {
              num: '11',
              title: 'Non-Exchangeable Bespoke Products',
              text: 'Customised, personalised, or specially made-to-order products cannot be exchanged for a change of mind, subject to applicable consumer rights.',
              highlight: 'Bespoke Non-Exchangeable',
            },
            {
              num: '12',
              title: 'How to Request an Exchange',
              text: 'Customers must contact TANM through the official customer support email or WhatsApp number, providing their order number, reason for exchange, and supporting photographs where necessary.',
              highlight: 'Email / WhatsApp Support',
            },
            {
              num: '13',
              title: 'Inspection and Processing Timelines',
              text: 'Once the returned product is received and inspected, customers will be notified about the exchange approval. Approved exchanges will be dispatched within 5–7 business days, subject to stock availability.',
              highlight: '5–7 Days Dispatch',
            },
            {
              num: '14',
              title: 'Refund Policy & Cancellations',
              text: 'Exchanges do not automatically qualify for a refund. Refunds, cancellations, and remedies for defective or incorrect products will be handled in accordance with TANM’s applicable policies and consumer protection laws.',
              highlight: 'Exchange-First Principle',
            },
            {
              num: '15',
              title: 'Policy Compliance & Integrity',
              text: 'TANM reserves the right to reject returns that do not meet the stated conditions, without limiting any rights available to customers under applicable law.',
              highlight: 'Right to Decline',
            },
            {
              num: '16',
              title: 'Payment Terms & Custom Orders',
              text: 'There is no COD; for customised products, there will be 50% advance payment and rest 50% payment should be done before dispatch.',
              highlight: 'Advance Payment Rule',
            },
          ].map((item) => (
            <div
              key={item.num}
              className="group p-5 md:p-6 rounded-2xl bg-surface-container-low/60 border border-primary/5 hover:border-primary/20 hover:bg-surface-container-low transition-all duration-300"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-mono font-bold tracking-widest text-primary/40 group-hover:text-primary transition-colors">
                  RULE #{item.num}
                </span>
                <span className="text-[10px] font-semibold uppercase tracking-wider text-primary/70 bg-primary/5 px-2.5 py-0.5 rounded-full">
                  {item.highlight}
                </span>
              </div>
              <h4 className="text-base font-serif font-semibold text-primary mb-2">
                {item.title}
              </h4>
              <p className="text-sm text-on-surface-variant leading-relaxed">
                {item.text}
              </p>
            </div>
          ))}
        </div>

        {/* Support Callout Box */}
        <div className="rounded-2xl border border-primary/10 bg-surface-container-high/40 p-6 md:p-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div>
            <h3 className="text-lg font-serif font-medium text-primary">
              Need Assistance with an Exchange?
            </h3>
            <p className="text-sm text-on-surface-variant mt-1">
              Have your order number and item details ready. Our client concierges are available Monday–Saturday.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <a
              href="mailto:support@tanm.in"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-primary text-on-primary text-xs font-semibold uppercase tracking-widest hover:opacity-90 active:scale-95 transition-all shadow-sm"
            >
              <span className="material-symbols-outlined text-[16px]">mail</span>
              Email Support
            </a>
            <a
              href="https://www.instagram.com/tanmbags/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full border border-primary/20 hover:border-primary text-primary text-xs font-semibold uppercase tracking-widest hover:bg-primary/5 active:scale-95 transition-all"
            >
              <span className="material-symbols-outlined text-[16px]">chat</span>
              Message on Instagram
            </a>
          </div>
        </div>
      </div>
    ),
  },

  'about': {
    category: 'The Maison',
    title: 'About TANM',
    subtitle: 'Timeless by Design. Crafted to Last.',
    lastUpdated: 'Atelier Heritage',
    renderContent: (onNavigate) => (
      <div className="space-y-16">
        {/* Hero Brand Statement */}
        <div className="text-center max-w-3xl mx-auto space-y-6 pt-4">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/5 border border-primary/10 text-[10px] font-bold tracking-[0.25em] uppercase text-primary">
            The Philosophy of Details
          </div>
          <h2 className="text-3xl md:text-5xl font-serif font-light text-primary tracking-tight leading-tight">
            True luxury lies in the <span className="italic font-normal">details</span>.
          </h2>
          <p className="text-base md:text-lg text-on-surface-variant leading-relaxed">
            Every accessory should be more than just something you carry or wear — it should reflect 
            your personality, complement your style, and become a part of your everyday story.
          </p>
        </div>

        {/* Narrative Feature 1 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-14 items-center">
          <div className="space-y-5">
            <span className="text-[11px] font-bold uppercase tracking-[0.25em] text-primary/60">
              Thoughtfully Curated
            </span>
            <h3 className="text-2xl md:text-3xl font-serif text-primary leading-snug">
              100% Genuine Leather, Designed with Intention
            </h3>
            <p className="text-sm md:text-base text-on-surface-variant leading-relaxed">
              TANM brings you a thoughtfully curated collection of <strong>100% genuine leather</strong> bags, 
              wallets, belts, and more designed for those who appreciate timeless aesthetics, understated 
              elegance, and versatile style.
            </p>
            <p className="text-sm md:text-base text-on-surface-variant leading-relaxed">
              Every hide is selected for its authentic character, subtle variations, and natural grain. 
              As you carry your TANM piece through seasons of life, the leather develops an irreplaceable, 
              distinguished patina unique to you.
            </p>
          </div>
          <div className="relative rounded-2xl overflow-hidden bg-surface-container-high aspect-[4/3] shadow-md border border-primary/10">
            <img
              src="https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=1200&q=85"
              alt="TANM Leather Craftsmanship"
              className="w-full h-full object-cover hover:scale-105 transition-transform duration-700"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent pointer-events-none" />
            <span className="absolute bottom-4 left-4 text-[10px] tracking-widest uppercase font-mono text-white/90 bg-black/40 backdrop-blur-md px-3 py-1 rounded-full">
              Full-Grain Leather · Hand-Finished
            </span>
          </div>
        </div>

        {/* Narrative Feature 2 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-14 items-center md:flex-row-reverse">
          <div className="relative rounded-2xl overflow-hidden bg-surface-container-high aspect-[4/3] shadow-md border border-primary/10 order-2 md:order-1">
            <img
              src="https://images.unsplash.com/photo-1590874103328-eac38a683ce7?auto=format&fit=crop&w=1200&q=85"
              alt="TANM Functional Refinement"
              className="w-full h-full object-cover hover:scale-105 transition-transform duration-700"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent pointer-events-none" />
            <span className="absolute bottom-4 left-4 text-[10px] tracking-widest uppercase font-mono text-white/90 bg-black/40 backdrop-blur-md px-3 py-1 rounded-full">
              Engineered Everyday Proportions
            </span>
          </div>
          <div className="space-y-5 order-1 md:order-2">
            <span className="text-[11px] font-bold uppercase tracking-[0.25em] text-primary/60">
              Modern Lifestyle
            </span>
            <h3 className="text-2xl md:text-3xl font-serif text-primary leading-snug">
              Functionality Combined with Refined Craftsmanship
            </h3>
            <p className="text-sm md:text-base text-on-surface-variant leading-relaxed">
              From everyday essentials to statement pieces, our designs combine functionality with 
              refined craftsmanship, making them perfect for modern lifestyles and special occasions alike.
            </p>
            <p className="text-sm md:text-base text-on-surface-variant leading-relaxed">
              We believe in creating accessories that go beyond fleeting trends — pieces you can cherish, 
              style, and carry with lasting confidence.
            </p>
          </div>
        </div>

        {/* Brand Pillars */}
        <div className="border-t border-primary/10 pt-12 space-y-8">
          <div className="text-center max-w-xl mx-auto space-y-2">
            <span className="text-[10px] font-bold uppercase tracking-[0.25em] text-primary/60">
              Our Guiding Pillars
            </span>
            <h3 className="text-2xl font-serif text-primary">The TANM Promise</h3>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: 'verified',
                title: '100% Genuine Leather',
                desc: 'Only authentic top and full-grain leathers, naturally tanned with zero compromises.',
              },
              {
                icon: 'diamond',
                title: 'Timeless Aesthetics',
                desc: 'Clean, understated silhouettes created to outlive seasonal fast-fashion trends.',
              },
              {
                icon: 'handyman',
                title: 'Refined Craftsmanship',
                desc: 'Meticulous saddle stitching, smooth hand-painted edges, and reinforced stress points.',
              },
              {
                icon: 'all_inclusive',
                title: 'Crafted to Last',
                desc: 'Durable hardware and enduring materials made to grow more beautiful with every year of use.',
              },
            ].map((pillar) => (
              <div
                key={pillar.title}
                className="p-6 rounded-2xl bg-surface-container-low border border-primary/5 hover:border-primary/20 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="w-10 h-10 rounded-full bg-primary/5 text-primary flex items-center justify-center mb-4">
                    <span className="material-symbols-outlined text-[20px]">{pillar.icon}</span>
                  </div>
                  <h4 className="font-serif font-semibold text-primary text-base mb-2">
                    {pillar.title}
                  </h4>
                  <p className="text-xs text-on-surface-variant leading-relaxed">
                    {pillar.desc}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Explore Collections CTA */}
        <div className="rounded-3xl bg-primary text-on-primary p-8 md:p-12 text-center space-y-6 relative overflow-hidden shadow-lg">
          <div className="max-w-xl mx-auto space-y-3">
            <span className="text-[10px] uppercase font-bold tracking-[0.3em] opacity-70">
              Curated Accessories
            </span>
            <h3 className="text-2xl md:text-3xl font-serif font-light">
              Experience the Touch of Genuine Leather
            </h3>
            <p className="text-sm text-on-primary/80 leading-relaxed">
              Explore our latest line of bags, wallets, belts, and bespoke leather essentials.
            </p>
          </div>
          <div>
            <button
              onClick={() => onNavigate && onNavigate('shop')}
              className="inline-flex items-center gap-2 px-8 py-3.5 rounded-full bg-on-primary text-primary text-xs font-bold uppercase tracking-widest hover:opacity-90 active:scale-95 transition-all shadow-md cursor-pointer"
            >
              <span>Explore The Collection</span>
              <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
            </button>
          </div>
        </div>
      </div>
    ),
  },

  'privacy-policy': {
    category: 'Legal & Compliance',
    title: 'Privacy Policy',
    subtitle: 'How TANM Collects, Safeguards & Manages Your Personal Information',
    lastUpdated: 'Effective October 2026',
    renderContent: () => (
      <div className="space-y-10 max-w-4xl mx-auto">
        <div className="p-6 rounded-2xl bg-surface-container-low border border-primary/10 text-sm text-on-surface-variant leading-relaxed">
          At <strong>TANM</strong> ("we", "our", or "us"), we are committed to respecting and protecting the privacy 
          of every visitor and client who accesses our storefront, places an order, or interacts with our atelier services. 
          This Privacy Policy outlines how your personal data is collected, used, shared, and safeguarded in accordance 
          with applicable Indian and international consumer protection and privacy standards.
        </div>

        <div className="space-y-8 divide-y divide-primary/10">
          {[
            {
              sec: '01',
              title: 'Information We Collect',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>When you browse our storefront, create an account, or make a purchase, we may collect:</p>
                  <ul className="list-disc pl-5 space-y-1.5">
                    <li><strong>Contact & Identification Details:</strong> Your full name, email address, mobile phone number, and account credentials.</li>
                    <li><strong>Delivery & Billing Addresses:</strong> Street address, city, state, postal PIN code, and dispatch instructions.</li>
                    <li><strong>Transaction & Order Details:</strong> Products purchased, order amounts, fulfillment status, and history of correspondence.</li>
                    <li><strong>Device & Usage Data:</strong> IP address, browser type, device identifiers, and pages viewed to optimize browsing experience.</li>
                  </ul>
                </div>
              ),
            },
            {
              sec: '02',
              title: 'Payment Data & Financial Security',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>
                    All online payments are processed through secure, RBI-compliant payment gateways equipped with 
                    <strong> 256-bit SSL encryption</strong>.
                  </p>
                  <p>
                    <strong>TANM does not store or have direct access to your full credit card, debit card, CVV, or banking passwords.</strong> 
                    All payment processing complies strictly with the highest PCI-DSS safety standards.
                  </p>
                </div>
              ),
            },
            {
              sec: '03',
              title: 'How We Use Your Information',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>We use your information exclusively for legitimate commercial and fulfillment purposes:</p>
                  <ul className="list-disc pl-5 space-y-1.5">
                    <li>Fulfilling and delivering your orders, including tracking notifications and status alerts.</li>
                    <li>Facilitating client inquiries, exchanges, and customer support via email and WhatsApp.</li>
                    <li>Managing bespoke orders, custom specifications, and installment payment verifications.</li>
                    <li>Detecting and mitigating fraud, unauthorized access, and payment discrepancies.</li>
                    <li>Sending transactional emails (invoices, shipping manifests, dispatch confirmations).</li>
                  </ul>
                </div>
              ),
            },
            {
              sec: '04',
              title: 'Information Sharing & Third-Party Disclosures',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>
                    <strong>We never sell, trade, or rent your personal information to third parties for marketing purposes.</strong>
                  </p>
                  <p>Your details are shared solely with trusted service providers necessary to operate our business:</p>
                  <ul className="list-disc pl-5 space-y-1.5">
                    <li><strong>Logistics Partners:</strong> Verified shipping carriers and courier services for order delivery.</li>
                    <li><strong>Payment Gateways:</strong> Banking and UPI partners for authenticating transactions.</li>
                    <li><strong>Statutory Authorities:</strong> If required by Indian law, judicial decree, or law enforcement summons.</li>
                  </ul>
                </div>
              ),
            },
            {
              sec: '05',
              title: 'Cookies & Browser Preferences',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>
                    Our storefront utilizes essential cookies and session storage to maintain your shopping cart, 
                    wishlist, and authenticated login state. You may modify your browser settings to decline non-essential 
                    cookies; however, certain interactive features of our store may be impaired.
                  </p>
                </div>
              ),
            },
            {
              sec: '06',
              title: 'Your Privacy Rights & Choices',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>As a valued client, you retain the right to:</p>
                  <ul className="list-disc pl-5 space-y-1.5">
                    <li>Access, review, or update your saved profile information at any time in the <strong>User Profile</strong> tab.</li>
                    <li>Request the correction of inaccurate or incomplete contact and delivery information.</li>
                    <li>Request deletion of your registered account and personal data, subject to tax and audit retention obligations.</li>
                    <li>Opt out of promotional newsletters or marketing communications by clicking unsubscribe or emailing us.</li>
                  </ul>
                </div>
              ),
            },
            {
              sec: '07',
              title: 'Data Retention & Safeguards',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>
                    We implement rigorous physical, electronic, and managerial safeguards to secure the personal data 
                    we collect. Personal data is retained only for as long as necessary to fulfill order obligations, 
                    resolve disputes, and meet legal and accounting requirements.
                  </p>
                </div>
              ),
            },
            {
              sec: '08',
              title: 'Contact Information & Grievance Redressal',
              content: (
                <div className="space-y-3 text-sm text-on-surface-variant leading-relaxed">
                  <p>
                    If you have questions, concerns, or requests regarding this Privacy Policy or how your data is handled, 
                    please reach out to our team:
                  </p>
                  <div className="p-4 rounded-xl bg-surface-container-high/50 border border-primary/10 mt-2 space-y-1 font-mono text-xs text-primary">
                    <p><strong>Brand:</strong> TANM Atelier</p>
                    <p><strong>Email:</strong> privacy@tanm.in / support@tanm.in</p>
                    <p><strong>Instagram:</strong> @tanmbags</p>
                  </div>
                </div>
              ),
            },
          ].map((section) => (
            <div key={section.sec} className="pt-8">
              <div className="flex items-center gap-3 mb-3">
                <span className="text-xs font-mono font-bold tracking-widest text-primary/40">
                  {section.sec}.
                </span>
                <h3 className="text-xl font-serif font-semibold text-primary">
                  {section.title}
                </h3>
              </div>
              {section.content}
            </div>
          ))}
        </div>
      </div>
    ),
  },
};

export default function StaticPage({ slug = 'shipping-returns', onNavigate }) {
  const [activeSlug, setActiveSlug] = useState(slug);
  const [cmsPage, setCmsPage] = useState(null);

  // Sync internal slug when parent passes a new slug prop
  useEffect(() => {
    if (slug) {
      setActiveSlug(slug);
    }
  }, [slug]);

  // Try fetching dynamic CMS content if available
  useEffect(() => {
    let mounted = true;

    cmsApi.getPage(activeSlug)
      .then((data) => {
        if (!mounted) return;
        if (data && data.status === 'PUBLISHED') {
          setCmsPage(data);
        } else {
          setCmsPage(null);
        }
      })
      .catch(() => {
        if (!mounted) return;
        setCmsPage(null);
      });

    return () => {
      mounted = false;
    };
  }, [activeSlug]);

  // Current page metadata & fallback content
  const pageData = STATIC_CONTENT[activeSlug] || {
    category: 'Information',
    title: cmsPage?.title || 'Policy & Information',
    subtitle: 'Official Maison Guidelines',
    lastUpdated: '2026',
    renderContent: () => (
      <div className="text-sm text-on-surface-variant leading-relaxed">
        {cmsPage?.content ? (
          <div dangerouslySetInnerHTML={{ __html: cmsPage.content }} />
        ) : (
          <p>The requested page information is currently being updated by the Maison team.</p>
        )}
      </div>
    ),
  };

  const handleTabSwitch = (newSlug) => {
    setActiveSlug(newSlug);
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
    if (onNavigate) {
      onNavigate(newSlug);
    }
  };

  return (
    <div className="min-h-screen bg-surface py-8 md:py-14 px-margin-mobile md:px-margin-desktop">
      <div className="max-w-container-max mx-auto">
        
        {/* Breadcrumbs */}
        <nav className="flex items-center gap-2 text-xs text-on-surface-variant mb-8 tracking-wider uppercase">
          <button
            onClick={() => onNavigate && onNavigate('home')}
            className="hover:text-primary transition-colors cursor-pointer"
          >
            Home
          </button>
          <span>/</span>
          <span className="text-on-surface-variant/70">{pageData.category}</span>
          <span>/</span>
          <span className="text-primary font-semibold">{pageData.title}</span>
        </nav>

        {/* Dynamic Navigation Pill Bar */}
        <div className="flex flex-wrap items-center gap-2 mb-10 pb-6 border-b border-primary/10">
          {[
            { slug: 'shipping-returns', label: 'Shipping & Returns' },
            { slug: 'about', label: 'About TANM' },
            { slug: 'privacy-policy', label: 'Privacy Policy' },
          ].map((item) => {
            const isActive = activeSlug === item.slug;
            return (
              <button
                key={item.slug}
                onClick={() => handleTabSwitch(item.slug)}
                className={`px-4 py-2 rounded-full text-xs font-semibold tracking-wider uppercase transition-all cursor-pointer ${
                  isActive
                    ? 'bg-primary text-on-primary shadow-sm font-bold scale-[1.02]'
                    : 'bg-surface-container-low text-on-surface-variant hover:text-primary hover:bg-surface-container border border-primary/5'
                }`}
              >
                {item.label}
              </button>
            );
          })}
        </div>

        {/* Page Hero Header */}
        <header className="mb-12 text-center md:text-left">
          <div className="inline-block px-3 py-1 rounded-full bg-primary/5 border border-primary/10 text-[10px] font-bold tracking-[0.25em] uppercase text-primary mb-3">
            {pageData.category}
          </div>
          <h1 className="text-3xl md:text-5xl font-serif text-primary tracking-tight font-normal">
            {cmsPage?.title || pageData.title}
          </h1>
          <p className="text-base md:text-lg text-on-surface-variant mt-2 font-light max-w-2xl">
            {pageData.subtitle}
          </p>
          <div className="flex items-center gap-4 mt-4 text-[11px] font-mono text-on-surface-variant/70 tracking-wider">
            <span>{pageData.lastUpdated}</span>
            <span>·</span>
            <span>TANM OFFICIAL PUBLICATION</span>
          </div>
        </header>

        {/* Main Content Area */}
        <div className="mt-8">
          {cmsPage?.content ? (
            <div
              className="prose prose-neutral max-w-none text-on-surface-variant leading-relaxed"
              dangerouslySetInnerHTML={{ __html: cmsPage.content }}
            />
          ) : (
            pageData.renderContent(onNavigate)
          )}
        </div>

      </div>
    </div>
  );
}
