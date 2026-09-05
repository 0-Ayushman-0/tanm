import React, { useState, useRef, useEffect } from 'react';
import { authApi } from '../api';

// ── Tabs ────────────────────────────────────────────────────────
const TABS = {
  LOGIN: 'login',
  REGISTER: 'register',
  OTP: 'otp',
  FORGOT_REQUEST: 'forgot_request',
  FORGOT_CONFIRM: 'forgot_confirm',
};

// ── Stable sub-components (MUST be outside LoginModal) ──────────
const EyeToggle = ({ show, onToggle }) => (
  <button
    type="button"
    onClick={onToggle}
    className="absolute right-0 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors cursor-pointer"
  >
    <span className="material-symbols-outlined text-[18px]">{show ? 'visibility_off' : 'visibility'}</span>
  </button>
);

const InputField = ({ label, type = 'text', value, onChange, placeholder, autoFocus, suffix }) => (
  <div className="relative">
    <label className="block text-[10px] font-bold uppercase tracking-widest text-on-surface-variant mb-2">
      {label}
    </label>
    <div className="relative">
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoFocus={autoFocus}
        required
        className="w-full bg-transparent border-b border-outline-variant focus:border-primary outline-none py-3 text-sm text-on-surface placeholder:text-outline transition-colors pr-10"
      />
      {suffix}
    </div>
  </div>
);

export default function LoginModal({ isOpen, onClose, onLoginSuccess }) {
  const [tab, setTab] = useState(TABS.LOGIN);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Login fields
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [showLoginPw, setShowLoginPw] = useState(false);

  // Register fields
  const [regFirst, setRegFirst] = useState('');
  const [regLast, setRegLast] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [showRegPw, setShowRegPw] = useState(false);

  // OTP (Registration verification)
  const [otpEmail, setOtpEmail] = useState('');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const otpRefs = useRef([]);

  // Forgot / Reset Password fields
  const [forgotEmail, setForgotEmail] = useState('');
  const [resetOtp, setResetOtp] = useState(['', '', '', '', '', '']);
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [showNewPw, setShowNewPw] = useState(false);
  const [showConfirmNewPw, setShowConfirmNewPw] = useState(false);
  const resetOtpRefs = useRef([]);

  // Reset state on open/close
  useEffect(() => {
    if (!isOpen) {
      setTab(TABS.LOGIN);
      setError('');
      setSuccess('');
      setOtp(['', '', '', '', '', '']);
      setResetOtp(['', '', '', '', '', '']);
      setNewPassword('');
      setConfirmNewPassword('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // ── Handlers ────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const data = await authApi.login(loginEmail, loginPassword);
      onLoginSuccess(data);
      onClose();
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      await authApi.register(regFirst, regLast, regEmail, regPassword);
      setOtpEmail(regEmail);
      setSuccess('Account created! Check your email for the verification code.');
      setTab(TABS.OTP);
    } catch (err) {
      setError(err.message || 'Registration failed. Email may already be in use.');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (val, idx) => {
    if (!/^\d?$/.test(val)) return;
    const next = [...otp];
    next[idx] = val;
    setOtp(next);
    if (val && idx < 5) otpRefs.current[idx + 1]?.focus();
  };

  const handleOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace' && !otp[idx] && idx > 0) {
      otpRefs.current[idx - 1]?.focus();
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    const code = otp.join('');
    if (code.length < 6) { setError('Please enter the full 6-digit code.'); return; }
    setError(''); setLoading(true);
    try {
      await authApi.verifyEmail(otpEmail, code);
      // Auto-login after verification
      setSuccess('Email verified! Signing you in…');
      const data = await authApi.login(otpEmail, regPassword);
      onLoginSuccess(data);
      onClose();
    } catch (err) {
      setError(err.message || 'Invalid or expired code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleDevBypass = async () => {
    setError(''); setLoading(true);
    try {
      await authApi.devBypass(otpEmail);
      setSuccess('Dev Mode: Email verification bypassed! Signing you in…');
      const data = await authApi.login(otpEmail, regPassword);
      onLoginSuccess(data);
      onClose();
    } catch (err) {
      setError(err.message || 'Dev bypass failed.');
    } finally {
      setLoading(false);
    }
  };

  // ── Forgot Password Handlers ────────────────────────────────
  const handleForgotPasswordRequest = async (e) => {
    e.preventDefault();
    if (!forgotEmail) {
      setError('Please enter your registered email address.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await authApi.requestPasswordReset(forgotEmail);
      setSuccess('If that email is registered, you will receive a 6-digit reset code shortly.');
      setResetOtp(['', '', '', '', '', '']);
      setTab(TABS.FORGOT_CONFIRM);
    } catch (err) {
      setError(err.message || 'Failed to send reset code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetOtpChange = (val, idx) => {
    if (!/^\d?$/.test(val)) return;
    const next = [...resetOtp];
    next[idx] = val;
    setResetOtp(next);
    if (val && idx < 5) resetOtpRefs.current[idx + 1]?.focus();
  };

  const handleResetOtpKeyDown = (e, idx) => {
    if (e.key === 'Backspace' && !resetOtp[idx] && idx > 0) {
      resetOtpRefs.current[idx - 1]?.focus();
    }
  };

  const handleForgotPasswordConfirm = async (e) => {
    e.preventDefault();
    const code = resetOtp.join('');
    if (code.length < 6) {
      setError('Please enter the full 6-digit verification code.');
      return;
    }
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    if (newPassword !== confirmNewPassword) {
      setError('Passwords do not match.');
      return;
    }

    setError('');
    setLoading(true);
    try {
      await authApi.confirmPasswordReset(forgotEmail, code, newPassword);
      setSuccess('Password changed successfully! Please sign in with your new password.');
      setLoginEmail(forgotEmail);
      setLoginPassword('');
      setTab(TABS.LOGIN);
    } catch (err) {
      setError(err.message || 'Failed to reset password. Invalid or expired OTP.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendResetCode = async () => {
    setError('');
    setLoading(true);
    try {
      await authApi.requestPasswordReset(forgotEmail);
      setSuccess('A new reset code has been sent to your email.');
      setResetOtp(['', '', '', '', '', '']);
    } catch (err) {
      setError(err.message || 'Failed to resend code.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
      {/* Backdrop - prevents closing on outside click */}
      <div className="absolute inset-0 bg-primary/40 backdrop-blur-sm" />

      {/* Panel */}
      <div className="relative w-full max-w-xl md:max-w-2xl bg-surface shadow-2xl overflow-hidden z-10 flex flex-col md:flex-row" style={{ minHeight: 520 }}>

        {/* Left accent strip */}
        <div className="hidden md:flex flex-col justify-between bg-primary-container p-8 w-52 shrink-0">
          <div>
            <img
              src="/logo-white.png"
              alt="TanM"
              className="h-16 w-auto object-contain mb-2 filter drop-shadow-sm"
            />
            <p className="text-sm font-bold uppercase tracking-[0.32em] text-white">tanm</p>
          </div>
          <div className="space-y-4">
            {[['workspace_premium', 'Premium Quality'], ['favorite', 'Curated for You'], ['lock', 'Secure Account']].map(([icon, text]) => (
              <div key={icon} className="flex items-center gap-2 text-white/70">
                <span className="material-symbols-outlined text-[16px]">{icon}</span>
                <span className="text-[11px]">{text}</span>
              </div>
            ))}
          </div>
          <p className="text-[10px] text-white/40 leading-relaxed">
            Join the TanM circle — exclusive access to new drops, heritage stories, and member-only pricing.
          </p>
        </div>

        {/* Right form area */}
        <div className="flex-1 p-6 md:p-8 flex flex-col min-w-0">

          {/* Close */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-on-surface-variant hover:text-primary transition-colors z-20 cursor-pointer"
          >
            <span className="material-symbols-outlined text-[20px]">close</span>
          </button>

          {/* Tab switcher — not shown on OTP or Forgot Password steps */}
          {tab !== TABS.OTP && tab !== TABS.FORGOT_REQUEST && tab !== TABS.FORGOT_CONFIRM && (
            <div className="flex border-b border-outline-variant/30 mb-8">
              {[TABS.LOGIN, TABS.REGISTER].map((t) => (
                <button
                  key={t}
                  onClick={() => { setTab(t); setError(''); setSuccess(''); }}
                  className={`pb-3 mr-6 text-xs font-bold uppercase tracking-widest transition-colors border-b-2 -mb-[1px] cursor-pointer
                    ${tab === t ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-primary'}`}
                >
                  {t === TABS.LOGIN ? 'Sign In' : 'Register'}
                </button>
              ))}
            </div>
          )}

          {/* Error / Success banners */}
          {error && (
            <div className="mb-5 flex items-start gap-2 text-error text-xs bg-error-container/30 border border-error/20 px-4 py-3 rounded break-words">
              <span className="material-symbols-outlined text-[16px] shrink-0 mt-0.5">error</span>
              <span className="break-words leading-relaxed">{error}</span>
            </div>
          )}
          {success && (
            <div className="mb-5 flex items-start gap-2 text-emerald-700 bg-emerald-50 border border-emerald-200 px-4 py-3 rounded text-xs break-words">
              <span className="material-symbols-outlined text-[16px] shrink-0 mt-0.5 text-emerald-600">check_circle</span>
              <span className="break-words leading-relaxed">{success}</span>
            </div>
          )}

          {/* ── SIGN IN ── */}
          {tab === TABS.LOGIN && (
            <form onSubmit={handleLogin} className="flex flex-col gap-6 flex-1">
              <div>
                <h2 className="font-display-lg text-2xl text-primary">Welcome Back</h2>
                <p className="text-xs text-on-surface-variant mt-1">Sign in to your TanM account</p>
              </div>

              <InputField
                label="Email Address"
                type="email"
                value={loginEmail}
                onChange={setLoginEmail}
                placeholder="you@example.com"
                autoFocus
              />

              <InputField
                label="Password"
                type={showLoginPw ? 'text' : 'password'}
                value={loginPassword}
                onChange={setLoginPassword}
                placeholder="••••••••"
                suffix={<EyeToggle show={showLoginPw} onToggle={() => setShowLoginPw(!showLoginPw)} />}
              />

              <div className="flex justify-end -mt-2">
                <button
                  type="button"
                  onClick={() => {
                    setForgotEmail(loginEmail || '');
                    setError('');
                    setSuccess('');
                    setTab(TABS.FORGOT_REQUEST);
                  }}
                  className="text-[11px] text-on-surface-variant hover:text-primary transition-colors cursor-pointer"
                >
                  Forgot password?
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="mt-auto bg-primary text-white py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading && <span className="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>}
                {loading ? 'Signing In…' : 'Sign In'}
              </button>

              <p className="text-[11px] text-center text-on-surface-variant">
                New to TanM?{' '}
                <button type="button" onClick={() => setTab(TABS.REGISTER)} className="text-primary font-bold hover:opacity-70 transition-opacity cursor-pointer">
                  Create an account
                </button>
              </p>
            </form>
          )}

          {/* ── REGISTER ── */}
          {tab === TABS.REGISTER && (
            <form onSubmit={handleRegister} className="flex flex-col gap-5 flex-1">
              <div>
                <h2 className="font-display-lg text-2xl text-primary">Create Account</h2>
                <p className="text-xs text-on-surface-variant mt-1">Join the TanM circle today</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <InputField label="First Name" value={regFirst} onChange={setRegFirst} placeholder="Julian" autoFocus />
                <InputField label="Last Name" value={regLast} onChange={setRegLast} placeholder="Royce" />
              </div>

              <InputField
                label="Email Address"
                type="email"
                value={regEmail}
                onChange={setRegEmail}
                placeholder="you@example.com"
              />

              <InputField
                label="Password"
                type={showRegPw ? 'text' : 'password'}
                value={regPassword}
                onChange={setRegPassword}
                placeholder="Min 8 characters"
                suffix={<EyeToggle show={showRegPw} onToggle={() => setShowRegPw(!showRegPw)} />}
              />

              <button
                type="submit"
                disabled={loading}
                className="mt-auto bg-primary text-white py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading && <span className="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>}
                {loading ? 'Creating Account…' : 'Create Account'}
              </button>

              <p className="text-[11px] text-center text-on-surface-variant">
                Already a member?{' '}
                <button type="button" onClick={() => setTab(TABS.LOGIN)} className="text-primary font-bold hover:opacity-70 transition-opacity cursor-pointer">
                  Sign in
                </button>
              </p>
            </form>
          )}

          {/* ── OTP VERIFICATION (Register) ── */}
          {tab === TABS.OTP && (
            <form onSubmit={handleVerify} className="flex flex-col gap-6 flex-1">
              <div className="text-center">
                <div className="w-12 h-12 bg-secondary-container rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="material-symbols-outlined text-secondary text-[24px]">mark_email_unread</span>
                </div>
                <h2 className="font-display-lg text-2xl text-primary">Verify Your Email</h2>
                <p className="text-xs text-on-surface-variant mt-2 leading-relaxed">
                  We sent a 6-digit code to<br />
                  <span className="font-bold text-primary">{otpEmail}</span>
                </p>
              </div>

              {/* OTP boxes */}
              <div className="flex justify-center gap-3 my-2">
                {otp.map((digit, i) => (
                  <input
                    key={i}
                    ref={(el) => (otpRefs.current[i] = el)}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handleOtpChange(e.target.value, i)}
                    onKeyDown={(e) => handleOtpKeyDown(e, i)}
                    className={`w-11 h-14 text-center text-xl font-bold border-2 bg-transparent outline-none transition-colors
                      ${digit ? 'border-primary text-primary' : 'border-outline-variant text-on-surface'}
                      focus:border-primary`}
                  />
                ))}
              </div>

              <button
                type="submit"
                disabled={loading || otp.join('').length < 6}
                className="bg-primary text-white py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading && <span className="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>}
                {loading ? 'Verifying…' : 'Verify & Sign In'}
              </button>

              <button
                type="button"
                onClick={handleDevBypass}
                disabled={loading}
                className="w-full bg-amber-600 hover:bg-amber-700 text-white py-3 px-4 text-xs font-bold uppercase tracking-wider transition-colors flex items-center justify-center gap-2 rounded shadow-sm cursor-pointer"
              >
                <span className="material-symbols-outlined text-[18px]">bolt</span>
                ⚡ Bypass Email Verification
              </button>

              <p className="text-[11px] text-center text-on-surface-variant">
                Didn't receive it?{' '}
                <button
                  type="button"
                  className="text-primary font-bold hover:opacity-70 transition-opacity cursor-pointer"
                  onClick={() => { setOtp(['', '', '', '', '', '']); setError(''); }}
                >
                  Resend Code
                </button>
              </p>
            </form>
          )}

          {/* ── FORGOT PASSWORD: REQUEST STEP ── */}
          {tab === TABS.FORGOT_REQUEST && (
            <form onSubmit={handleForgotPasswordRequest} className="flex flex-col gap-6 flex-1">
              <div>
                <button
                  type="button"
                  onClick={() => { setTab(TABS.LOGIN); setError(''); setSuccess(''); }}
                  className="flex items-center gap-1 text-[11px] text-on-surface-variant hover:text-primary transition-colors mb-4 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[16px]">arrow_back</span>
                  Back to Sign In
                </button>
                <h2 className="font-display-lg text-2xl text-primary">Reset Password</h2>
                <p className="text-xs text-on-surface-variant mt-1 leading-relaxed">
                  Enter your registered email address and we'll send you a 6-digit code to reset your password.
                </p>
              </div>

              <InputField
                label="Email Address"
                type="email"
                value={forgotEmail}
                onChange={setForgotEmail}
                placeholder="you@example.com"
                autoFocus
              />

              <button
                type="submit"
                disabled={loading || !forgotEmail}
                className="mt-auto bg-primary text-white py-4 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading && <span className="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>}
                {loading ? 'Sending Code…' : 'Send Reset Code'}
              </button>

              <p className="text-[11px] text-center text-on-surface-variant">
                Remember your password?{' '}
                <button
                  type="button"
                  onClick={() => { setTab(TABS.LOGIN); setError(''); setSuccess(''); }}
                  className="text-primary font-bold hover:opacity-70 transition-opacity cursor-pointer"
                >
                  Sign In
                </button>
              </p>
            </form>
          )}

          {/* ── FORGOT PASSWORD: CONFIRM STEP ── */}
          {tab === TABS.FORGOT_CONFIRM && (
            <form onSubmit={handleForgotPasswordConfirm} className="flex flex-col gap-4 flex-1">
              <div>
                <button
                  type="button"
                  onClick={() => { setTab(TABS.FORGOT_REQUEST); setError(''); setSuccess(''); }}
                  className="flex items-center gap-1 text-[11px] text-on-surface-variant hover:text-primary transition-colors mb-2 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[16px]">arrow_back</span>
                  Change email
                </button>
                <h2 className="font-display-lg text-xl text-primary">Create New Password</h2>
                <p className="text-xs text-on-surface-variant mt-0.5 leading-relaxed">
                  Enter the 6-digit code sent to <span className="font-bold text-primary">{forgotEmail}</span>
                </p>
              </div>

              {/* 6-digit OTP boxes */}
              <div className="flex justify-center gap-2 my-1">
                {resetOtp.map((digit, i) => (
                  <input
                    key={i}
                    ref={(el) => (resetOtpRefs.current[i] = el)}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handleResetOtpChange(e.target.value, i)}
                    onKeyDown={(e) => handleResetOtpKeyDown(e, i)}
                    className={`w-10 h-12 text-center text-lg font-bold border-2 bg-transparent outline-none transition-colors
                      ${digit ? 'border-primary text-primary' : 'border-outline-variant text-on-surface'}
                      focus:border-primary`}
                  />
                ))}
              </div>

              <InputField
                label="New Password"
                type={showNewPw ? 'text' : 'password'}
                value={newPassword}
                onChange={setNewPassword}
                placeholder="Min 8 characters"
                suffix={<EyeToggle show={showNewPw} onToggle={() => setShowNewPw(!showNewPw)} />}
              />

              <InputField
                label="Confirm New Password"
                type={showConfirmNewPw ? 'text' : 'password'}
                value={confirmNewPassword}
                onChange={setConfirmNewPassword}
                placeholder="Confirm password"
                suffix={<EyeToggle show={showConfirmNewPw} onToggle={() => setShowConfirmNewPw(!showConfirmNewPw)} />}
              />

              <button
                type="submit"
                disabled={loading || resetOtp.join('').length < 6 || !newPassword || !confirmNewPassword}
                className="mt-2 bg-primary text-white py-3.5 text-xs font-bold uppercase tracking-widest hover:bg-primary-container transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading && <span className="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>}
                {loading ? 'Resetting Password…' : 'Reset Password'}
              </button>

              <div className="flex justify-between items-center text-[11px] text-on-surface-variant pt-1">
                <button
                  type="button"
                  onClick={handleResendResetCode}
                  className="text-primary font-bold hover:opacity-70 transition-opacity cursor-pointer"
                >
                  Resend Code
                </button>
                <button
                  type="button"
                  onClick={() => { setTab(TABS.LOGIN); setError(''); setSuccess(''); }}
                  className="text-on-surface-variant hover:text-primary transition-colors cursor-pointer"
                >
                  Back to Sign In
                </button>
              </div>
            </form>
          )}

        </div>
      </div>
    </div>
  );
}
