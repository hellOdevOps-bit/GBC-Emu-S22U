# GBC-Emu

**GBC-Emu** est un projet d’émulateur Game Boy Color open-source, développé en Kotlin avec amour, café, et détermination.  
Objectif : faire tourner des jeux GBC directement sur Android – en mode rétro mais avec la puissance du S22 Ultra.

## Fonctionnalités prévues

- Emulation complète du CPU **LR35902**
- Gestion de la **mémoire**, des **interruptions**, et du **bank switching** (MBC1, MBC3)
- Rendu graphique **tile-based** (sprites, background, palettes)
- Support **audio** 4 canaux (square, wave, noise)
- Prise en charge des **entrées** tactiles & manette BT
- Affichage optimisé Android (Canvas ou SurfaceView)
- Sauvegarde d’état (`.sav` / save states)
- Aucune clé sensible stockée ou versionnée (sécurité first)

## Plateforme ciblée

- Android 10+ (testé sur Galaxy S22 Ultra)
- Kotlin (100%)
- Android Studio + Gradle

## Stack technique

| Composant | Tech |
|----------|------|
| CPU | Kotlin - Simulation cycle par cycle |
| Rendu | Canvas Android (puis OpenGL en option) |
| Audio | SoundPool ou AudioTrack |
| UI | Kotlin + XML / Jetpack Compose |
| Tests | ROMs de Blargg (cpu_instrs, instr_timing, etc.) |

## En cours de dev

Ce projet est en chantier !  
Il est développé en solo pour le kiff, le skill, et le style

## Objectif final

Faire tourner des classiques comme **Pokémon Cristal**, **Zelda Oracle of Ages**, ou **Wario Land 3**, en 60 FPS, avec un rendu propre et du son nickel

---

### Par : Hello_Dev0ps!  
Contact : [hello_dev0ps@protonmail.com](mailto:hello_dev0ps@protonmail.com)

---
