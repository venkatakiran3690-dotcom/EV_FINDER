import React, { useState, useEffect } from 'react';
import { Car, ShieldAlert, ArrowLeft, Mail, Lock, User, Zap, AlertCircle, Phone } from 'lucide-react';
import { api } from '../utils/api';
import Logo from './Logo';

export default function AuthPortal({ onAuthSuccess }) {
  const [selectedRole, setSelectedRole] = useState(null); // 'driver', 'retailer', or null
  const [isSignUp, setIsSignUp] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Form Fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [username, setUsername] = useState('');
  const [phone, setPhone] = useState('');

  // OTP flow states
  const [showOtp, setShowOtp] = useState(false);
  const [otpCode, setOtpCode] = useState(['', '', '', '', '', '']);
  const [otpTimer, setOtpTimer] = useState(0);
  const [otpHelper, setOtpHelper] = useState('');

  // Manage timer countdown
  useEffect(() => {
    let interval = null;
    if (showOtp && otpTimer > 0) {
      interval = setInterval(() => {
        setOtpTimer(prev => prev - 1);
      }, 1000);
    } else {
      clearInterval(interval);
    }
    return () => clearInterval(interval);
  }, [showOtp, otpTimer]);

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
    setError(null);
    setShowOtp(false);
  };

  const handleBackToGate = () => {
    setSelectedRole(null);
    setError(null);
    setShowOtp(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (isSignUp) {
        // Validation
        if (password !== confirmPassword) {
          throw new Error("Passwords do not match.");
        }
        if (password.length < 6) {
          throw new Error("Password must be at least 6 characters long.");
        }
        if (!phone.trim()) {
          throw new Error("Mobile number is required.");
        }

        // Register user
        const result = await api.register(username, email, password, selectedRole, phone);
        if (result.otpRequired) {
          setShowOtp(true);
          setOtpTimer(30);
          setOtpHelper(result.code || '');
        } else {
          // Auto login on fallback
          const session = await api.login(email, password, selectedRole);
          onAuthSuccess(session);
        }
      } else {
        // Login user
        const result = await api.login(email, password, selectedRole);
        if (result.otpRequired) {
          setShowOtp(true);
          setOtpTimer(30);
          setOtpHelper(result.code || '');
        } else {
          onAuthSuccess(result);
        }
      }
    } catch (err) {
      setError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpVerify = async (e) => {
    e.preventDefault();
    const otpCodeString = otpCode.join('');
    if (otpCodeString.length !== 6) {
      setError("Please enter the complete 6-digit verification code.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const session = await api.verifyOtp(email, otpCodeString, selectedRole);
      onAuthSuccess(session);
    } catch (err) {
      setError(err.message || "Invalid or expired verification code.");
    } finally {
      setLoading(false);
    }
  };

  const handleOtpResend = async () => {
    if (otpTimer > 0) return;
    setLoading(true);
    setError(null);
    try {
      const result = await api.resendOtp(email);
      setOtpTimer(30);
      setOtpCode(['', '', '', '', '', '']);
      setOtpHelper(result.code || '');
      // Shift focus to first box
      setTimeout(() => {
        const firstInput = document.getElementById('otp-0');
        if (firstInput) firstInput.focus();
      }, 50);
    } catch (err) {
      setError(err.message || "Failed to resend verification code.");
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (index, value) => {
    if (value && !/^\d$/.test(value)) return;

    const newOtp = [...otpCode];
    newOtp[index] = value;
    setOtpCode(newOtp);

    // Auto-focus next input
    if (value && index < 5) {
      const nextInput = document.getElementById(`otp-${index + 1}`);
      if (nextInput) nextInput.focus();
    }
  };

  const handleOtpKeyDown = (index, e) => {
    if (e.key === 'Backspace') {
      if (!otpCode[index] && index > 0) {
        const prevInput = document.getElementById(`otp-${index - 1}`);
        if (prevInput) {
          prevInput.focus();
          const newOtp = [...otpCode];
          newOtp[index - 1] = '';
          setOtpCode(newOtp);
        }
      }
    }
  };

  const handleOtpPaste = (e) => {
    e.preventDefault();
    const pasteData = e.clipboardData.getData('text').trim();
    if (/^\d{6}$/.test(pasteData)) {
      const digits = pasteData.split('');
      setOtpCode(digits);
      const lastInput = document.getElementById('otp-5');
      if (lastInput) lastInput.focus();
    }
  };

  const accentColor = selectedRole === 'driver' ? 'var(--color-primary)' : 'var(--color-secondary)';
  const glowShadow = selectedRole === 'driver' ? 'var(--color-primary-glow)' : 'var(--color-secondary-glow)';

  return (
    <div className="auth-page-container">
      {/* Visual logo header overlay */}
      <div style={{
        position: 'absolute',
        top: '40px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        zIndex: 50
      }}>
        <Logo size={42} animated={true} />
        <div>
          <h1 style={{ 
            fontSize: '20px', 
            fontWeight: '900', 
            lineHeight: 1, 
            letterSpacing: '0.05em',
            background: 'linear-gradient(135deg, var(--color-primary), var(--color-secondary))',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>
            SUP CHARGED
          </h1>
          <span style={{ fontSize: '10px', color: 'var(--text-secondary)', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.1em' }}>Smart Grid Hub</span>
        </div>
      </div>

      {selectedRole === null ? (
        /* Gateway Gate Selector Screen */
        <div className="glass-panel auth-gate-card">
          <h2 style={{ fontSize: '22px', fontWeight: '800', marginBottom: '8px' }}>Identity Portal Gateway</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '12px', marginBottom: '32px' }}>
            Choose your profile gateway to access the smart grid network
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px' }}>
            
            {/* Driver Role Card selection */}
            <div 
              onClick={() => handleRoleSelect('driver')}
              className="auth-role-card driver"
            >
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'rgba(255, 179, 0, 0.08)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)'
              }}>
                <Car size={24} />
              </div>
              <div>
                <h3 style={{ fontSize: '14px', fontWeight: '800', marginBottom: '2px', color: 'var(--text-primary)' }}>Driver Access Portal</h3>
                <p style={{ fontSize: '11px', color: 'var(--text-secondary)', lineHeight: 1.3 }}>
                  Locate chargers, reserve time slots, and pay session fees.
                </p>
              </div>
            </div>

            {/* Operator Role Card selection */}
            <div 
              onClick={() => handleRoleSelect('retailer')}
              className="auth-role-card operator"
            >
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'rgba(15, 118, 110, 0.08)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-secondary)'
              }}>
                <ShieldAlert size={24} />
              </div>
              <div>
                <h3 style={{ fontSize: '14px', fontWeight: '800', marginBottom: '2px', color: 'var(--text-primary)' }}>Operator Access Portal</h3>
                <p style={{ fontSize: '11px', color: 'var(--text-secondary)', lineHeight: 1.3 }}>
                  Edit pricing structures, grid load indices, and connector grids.
                </p>
              </div>
            </div>

          </div>
        </div>
      ) : showOtp ? (
        /* OTP Verification Screen */
        <div className="glass-panel auth-gate-card" style={{ position: 'relative' }}>
          {/* Back button */}
          <button 
            onClick={() => { setShowOtp(false); setError(null); }}
            style={{
              position: 'absolute',
              top: '20px',
              left: '20px',
              background: 'rgba(0,0,0,0.03)',
              border: 'none',
              borderRadius: '50%',
              width: '30px',
              height: '30px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              transition: 'var(--transition-smooth)'
            }}
            className="hover-scale"
          >
            <ArrowLeft size={14} />
          </button>

          <span style={{
            fontSize: '9px',
            fontWeight: '800',
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            padding: '3px 8px',
            borderRadius: '4px',
            backgroundColor: selectedRole === 'driver' ? 'rgba(255, 179, 0, 0.12)' : 'rgba(15, 118, 110, 0.12)',
            color: accentColor,
            display: 'inline-block',
            marginBottom: '16px'
          }}>
            Verification Step
          </span>

          <h2 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '6px' }}>
            One-Time Password
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '11px', marginBottom: '24px', lineHeight: 1.4 }}>
            We've simulated sending a 6-digit verification code to <strong>{email}</strong>. Please enter the code below to access your hub.
          </p>

          {error && (
            <div style={{
              backgroundColor: 'rgba(255, 0, 85, 0.08)',
              border: '1px solid var(--color-danger)',
              color: 'var(--text-primary)',
              borderRadius: '8px',
              padding: '10px 12px',
              fontSize: '11px',
              marginBottom: '16px',
              display: 'flex',
              gap: '6px',
              alignItems: 'center',
              textAlign: 'left'
            }}>
              <AlertCircle size={14} style={{ flexShrink: 0, color: 'var(--color-danger)' }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleOtpVerify} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* 6-Digit input boxes */}
            <div style={{ display: 'flex', justifyContent: 'center', gap: '8px' }} onPaste={handleOtpPaste}>
              {otpCode.map((digit, index) => (
                <input
                  key={index}
                  id={`otp-${index}`}
                  type="text"
                  maxLength="1"
                  value={digit}
                  onChange={(e) => handleOtpChange(index, e.target.value)}
                  onKeyDown={(e) => handleOtpKeyDown(index, e)}
                  style={{
                    width: '42px',
                    height: '48px',
                    textAlign: 'center',
                    fontSize: '20px',
                    fontWeight: '800',
                    borderRadius: '8px',
                    border: '1.5px solid var(--border-color)',
                    background: 'var(--bg-input)',
                    color: 'var(--text-primary)',
                    outline: 'none',
                    transition: 'var(--transition-smooth)'
                  }}
                  onFocus={(e) => e.target.style.borderColor = accentColor}
                  onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                  required
                />
              ))}
            </div>

            <button 
              type="submit"
              disabled={loading}
              className="glow-button"
              style={{
                background: `linear-gradient(135deg, ${accentColor}, var(--color-accent))`,
                boxShadow: `0 4px 15px ${glowShadow}`,
                padding: '12px',
                fontSize: '12px',
                marginTop: '10px'
              }}
            >
              {loading ? 'Verifying...' : 'Verify & Log In'}
            </button>
          </form>

          {/* Resend and Countdown section */}
          <div style={{ marginTop: '20px', fontSize: '11px', color: 'var(--text-secondary)' }}>
            {otpTimer > 0 ? (
              <span>Resend code in <strong style={{ color: accentColor }}>{otpTimer}s</strong></span>
            ) : (
              <span>
                Didn't receive a code?{' '}
                <span 
                  onClick={handleOtpResend}
                  style={{ color: accentColor, cursor: 'pointer', textDecoration: 'underline', fontWeight: '700' }}
                >
                  Resend OTP
                </span>
              </span>
            )}
          </div>

          {/* Testing helper showing the simulated OTP */}
          {otpHelper && (
            <div style={{
              marginTop: '24px',
              padding: '10px',
              borderRadius: '6px',
              background: 'rgba(255, 179, 0, 0.08)',
              border: '1.5px solid var(--color-primary)',
              fontSize: '10px',
              textAlign: 'center'
            }}>
              🔑 <span style={{ fontWeight: '800', color: 'var(--text-primary)' }}>Simulated Test OTP:</span>{' '}
              <strong style={{ fontSize: '12px', color: 'var(--color-primary)', letterSpacing: '2px', marginLeft: '4px' }}>{otpHelper}</strong>
              <div style={{ fontSize: '8px', color: 'var(--text-secondary)', marginTop: '4px', textTransform: 'uppercase' }}>
                For easy evaluation, use this code. It is also printed in the backend console logs.
              </div>
            </div>
          )}

        </div>
      ) : (
        /* Login/Register Form */
        <div className="glass-panel auth-gate-card" style={{ position: 'relative' }}>
          {/* Back button */}
          <button 
            onClick={handleBackToGate}
            style={{
              position: 'absolute',
              top: '20px',
              left: '20px',
              background: 'rgba(0,0,0,0.03)',
              border: 'none',
              borderRadius: '50%',
              width: '30px',
              height: '30px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              transition: 'var(--transition-smooth)'
            }}
            className="hover-scale"
          >
            <ArrowLeft size={14} />
          </button>

          <span style={{
            fontSize: '9px',
            fontWeight: '800',
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            padding: '3px 8px',
            borderRadius: '4px',
            backgroundColor: selectedRole === 'driver' ? 'rgba(255, 179, 0, 0.12)' : 'rgba(15, 118, 110, 0.12)',
            color: accentColor,
            display: 'inline-block',
            marginBottom: '16px'
          }}>
            {selectedRole === 'driver' ? 'Driver Portal' : 'Operator Portal'}
          </span>

          <h2 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '6px' }}>
            {isSignUp ? 'Create Smart Account' : 'Gateway Verification'}
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '11px', marginBottom: '24px' }}>
            {isSignUp ? 'Register profile to connect to network grids' : 'Sign in to access maps and grids'}
          </p>

          {error && (
            <div style={{
              backgroundColor: 'rgba(255, 0, 85, 0.08)',
              border: '1px solid var(--color-danger)',
              color: 'var(--text-primary)',
              borderRadius: '8px',
              padding: '10px 12px',
              fontSize: '11px',
              marginBottom: '16px',
              display: 'flex',
              gap: '6px',
              alignItems: 'center',
              textAlign: 'left'
            }}>
              <AlertCircle size={14} style={{ flexShrink: 0, color: 'var(--color-danger)' }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            
            {/* Username field (only on Sign Up) */}
            {isSignUp && (
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <User size={14} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
                <input 
                  type="text"
                  placeholder={selectedRole === 'driver' ? 'Username' : 'Operator / Brand Name'}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="glass-input"
                  style={{ width: '100%', paddingLeft: '36px', fontSize: '12px' }}
                  required
                />
              </div>
            )}

            {/* Phone field (only on Sign Up) */}
            {isSignUp && (
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Phone size={14} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
                <input 
                  type="tel"
                  placeholder="Registered Mobile Number"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="glass-input"
                  style={{ width: '100%', paddingLeft: '36px', fontSize: '12px' }}
                  required
                />
              </div>
            )}

            {/* Email field */}
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Mail size={14} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
              <input 
                type="email"
                placeholder="Email Address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="glass-input"
                style={{ width: '100%', paddingLeft: '36px', fontSize: '12px' }}
                required
              />
            </div>

            {/* Password field */}
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Lock size={14} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
              <input 
                type="password"
                placeholder="Secure Access Key (Password)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="glass-input"
                style={{ width: '100%', paddingLeft: '36px', fontSize: '12px' }}
                required
              />
            </div>

            {/* Confirm Password field (only on Sign Up) */}
            {isSignUp && (
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Lock size={14} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
                <input 
                  type="password"
                  placeholder="Confirm Password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="glass-input"
                  style={{ width: '100%', paddingLeft: '36px', fontSize: '12px' }}
                  required
                />
              </div>
            )}

            <button 
              type="submit"
              disabled={loading}
              className="glow-button"
              style={{
                background: `linear-gradient(135deg, ${accentColor}, var(--color-accent))`,
                boxShadow: `0 4px 15px ${glowShadow}`,
                padding: '12px',
                fontSize: '12px',
                marginTop: '10px'
              }}
            >
              {loading ? 'Verifying...' : isSignUp ? 'Create Credentials' : 'Access Hub'}
            </button>
          </form>

          {/* Toggle login vs register link */}
          <div style={{ marginTop: '20px', fontSize: '11px', color: 'var(--text-secondary)' }}>
            {isSignUp ? (
              <span>
                Already registered?{' '}
                <span 
                  onClick={() => setIsSignUp(false)}
                  style={{ color: accentColor, cursor: 'pointer', textDecoration: 'underline', fontWeight: '700' }}
                >
                  Sign In
                </span>
              </span>
            ) : (
              <span>
                First time using Sup Charged?{' '}
                <span 
                  onClick={() => setIsSignUp(true)}
                  style={{ color: accentColor, cursor: 'pointer', textDecoration: 'underline', fontWeight: '700' }}
                >
                  Create Account
                </span>
              </span>
            )}
          </div>

          {/* Helper details for easy testing */}
          <div style={{
            marginTop: '24px',
            padding: '10px',
            borderRadius: '6px',
            background: 'rgba(0,0,0,0.02)',
            border: '1px solid var(--border-color)',
            fontSize: '10px',
            textAlign: 'left'
          }}>
            <span style={{ fontWeight: '800', color: accentColor, display: 'block', marginBottom: '3px' }}>Quick Test Login:</span>
            {selectedRole === 'driver' ? (
              <span>Email: <strong>driver@sup.in</strong> | Password: <strong>password</strong></span>
            ) : (
              <span>Email: <strong>operator@sup.in</strong> | Password: <strong>password</strong></span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
