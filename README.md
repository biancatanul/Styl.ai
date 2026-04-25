# Styl.ai

A smart wardrobe assistant for Android. Add your clothing items, build outfits manually, or let the AI suggest one based on the occasion, season, and style you're going for. Every suggestion comes with reasoning, and you can see exactly why each item was picked.

## Features

- **Wardrobe management**: add, edit, and delete clothing items with category, style, color, season, and occasion tags
- **Smart filtering**: search by name and filter by any combination of category, style, season, occasion, or color
- **Manual outfit builder**: pick items from your wardrobe and save outfits
- **AI outfit suggestions**: constraint-based recommendations ranked by compatibility score, with per-item reasoning
- **Data structure visualizations**: interactive pan/zoom views of the underlying Red-Black Tree, Binomial Heap, and Compatibility Graph

## How the AI works

Outfit suggestions are generated using a Constraint Satisfaction Problem (CSP) solver. Items are filtered by occasion, season, and style, then checked for pairwise compatibility using a clothing compatibility graph. Valid outfits are ranked best-first using a Binomial Heap scored by compatible item pairs. Each suggested item displays which constraints it satisfies.

## Tech stack

- Java, Android Studio
- Custom Canvas-based visualizations with pan/zoom gesture support
- In-memory storage (no backend, no login)

## Status

Core features complete. UI polish, photo upload, and persistent storage in progress.
