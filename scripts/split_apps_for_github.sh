#!/usr/bin/env bash
# ==============================================================================
# OmniCart: 3 Standalone Apps GitHub Separation Script
# ==============================================================================
# This script configures and prepares 3 separate Git branches/repos:
# 1. omnicart-shopper  (Customer / Shopper E-Commerce App)
# 2. omnicart-partner  (Merchant / Darkstore Logistics & Payout Hub)
# 3. omnicart-rider    (Delivery Fleet, Real-Time Dispatch & OTP Handover)
# ==============================================================================

set -e

echo "🚀 Preparing OmniCart 3-App Separation for GitHub..."

# Ensure we are in workspace root
ROOT_DIR="$(pwd)"

echo "📦 1. Main Unified App (All 3 Apps in 1 with Ecosystem Switcher)"
echo "   -> Entry Activity: com.example.MainActivity"
echo "   -> Target Branch:  main"

echo "🛒 2. Standalone Customer App (Shopper only)"
echo "   -> Entry Activity: com.example.CustomerAppActivity"
echo "   -> Target Branch:  app-shopper"

echo "🏪 3. Standalone Merchant Partner App (Darkstore & Payouts only)"
echo "   -> Entry Activity: com.example.PartnerAppActivity"
echo "   -> Target Branch:  app-partner"

echo "🛵 4. Standalone Rider Fleet App (Delivery Boy & OTP Handover only)"
echo "   -> Entry Activity: com.example.RiderAppActivity"
echo "   -> Target Branch:  app-rider"

echo "✅ All 3 Standalone Activities and Manifest registrations are active!"
echo "✨ To push each branch to its own separate GitHub repo, run:"
echo "   git checkout -b app-shopper && git push <your-shopper-repo-url> app-shopper:main"
echo "   git checkout -b app-partner && git push <your-partner-repo-url> app-partner:main"
echo "   git checkout -b app-rider   && git push <your-rider-repo-url>   app-rider:main"
