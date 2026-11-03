import React, { useId } from 'react';

export default function Logo({ size = 36, className = '', animated = true, variant = 'theme' }) {
  const id = useId();
  const cleanId = id.replace(/:/g, '');

  // Determine colors based on variant:
  // - 'theme': uses dynamic theme variables (Amber Gold to Forest Teal)
  // - 'white': uses solid white (ideal for rendering on gradient background cards)
  // - 'primary': uses solid var(--color-primary)
  // - 'secondary': uses solid var(--color-secondary)
  let mainStroke = `url(#ecoChargedGrad-${cleanId})`;
  let mainFill = `url(#ecoChargedGrad-${cleanId})`;
  let ringFill = `url(#ecoRingGrad-${cleanId})`;
  let shadowColor = 'var(--color-primary)';
  let shadowOpacity = 0.25;

  if (variant === 'white') {
    mainStroke = '#ffffff';
    mainFill = '#ffffff';
    ringFill = 'rgba(255, 255, 255, 0.15)';
    shadowColor = '#ffffff';
    shadowOpacity = 0.35;
  } else if (variant === 'primary') {
    mainStroke = 'var(--color-primary)';
    mainFill = 'var(--color-primary)';
    ringFill = 'var(--color-primary-glow)';
    shadowColor = 'var(--color-primary)';
    shadowOpacity = 0.2;
  } else if (variant === 'secondary') {
    mainStroke = 'var(--color-secondary)';
    mainFill = 'var(--color-secondary)';
    ringFill = 'var(--color-secondary-glow)';
    shadowColor = 'var(--color-secondary)';
    shadowOpacity = 0.2;
  }

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      style={{ display: 'inline-block', verticalAlign: 'middle' }}
    >
      <defs>
        {/* Solar Aurora Gradient: Amber Gold to Forest Teal */}
        <linearGradient id={`ecoChargedGrad-${cleanId}`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="var(--color-primary)" />
          <stop offset="100%" stopColor="var(--color-secondary)" />
        </linearGradient>

        {/* Ambient Ring Gradient */}
        <linearGradient id={`ecoRingGrad-${cleanId}`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="var(--color-primary)" stopOpacity="0.15" />
          <stop offset="100%" stopColor="var(--color-secondary)" stopOpacity="0" />
        </linearGradient>

        {/* Shadow filter for leaf-plug depth */}
        <filter id={`ecoGlow-${cleanId}`} x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor={shadowColor} floodOpacity={shadowOpacity} />
        </filter>
      </defs>

      {/* Ambient background glow ring */}
      <circle cx="50" cy="50" r="46" fill={ringFill} />

      {/* Sleek, geometric outer circle frame with leaf vein gap */}
      <circle
        cx="50"
        cy="50"
        r="40"
        stroke={mainStroke}
        strokeWidth="5"
        strokeLinecap="round"
        strokeDasharray="180 40 20 40"
      >
        {animated && (
          <animateTransform
            attributeName="transform"
            type="rotate"
            from="0 50 50"
            to="360 50 50"
            dur="18s"
            repeatCount="indefinite"
          />
        )}
      </circle>

      {/* Eco Leaf & Power Plug Motif */}
      <g filter={`url(#ecoGlow-${cleanId})`}>
        {/* Stylized Leaf outline */}
        <path
          d="M50,72 C32,72 25,58 25,43 C25,27 42,21 50,21 C58,21 75,27 75,43 C75,53 68,64 60,68"
          stroke={mainStroke}
          strokeWidth="4"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        
        {/* Leaf central vein */}
        <path
          d="M50,72 V30"
          stroke={mainStroke}
          strokeWidth="3.2"
          strokeLinecap="round"
        />

        {/* Plug Cable looping from the base of the leaf */}
        <path
          d="M60,68 C55,73 48,80 48,83"
          stroke={mainStroke}
          strokeWidth="3.2"
          strokeLinecap="round"
        />

        {/* Plug Body */}
        <rect
          x="42"
          y="82"
          width="12"
          height="8"
          rx="2"
          fill={mainFill}
        />

        {/* Plug Prongs */}
        <path
          d="M45,90 V94 M51,90 V94"
          stroke={mainStroke}
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </g>
    </svg>
  );
}
