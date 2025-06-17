# GBC Emulator S22U

Un émulateur Game Boy Color développé en Kotlin, avec une version desktop (VSCode) et une version Android.

## Structure du Projet

### Version Desktop (VSCode)

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── Hello_Dev0ps/
│   │           └── GBC-Emu-S22U/
│   │               └── core/
│   │                   ├── Memory.kt      # Gestion de la mémoire
│   │                   ├── CPU.kt         # Processeur
│   │                   ├── PPU.kt         # Processeur graphique
│   │                   ├── APU.kt         # Processeur audio
│   │                   ├── Emulator.kt    # Émulateur principal
│   │                   ├── ROMLoader.kt   # Chargement des ROMs
│   │                   └── CBOpcodeHandler.kt # Gestion des opcodes CB
│   └── assets/
│       └── cpu_instrs.gb  # ROM de test
└── test/
    └── java/
        └── com/
            └── Hello_Dev0ps/
                └── GBC-Emu-S22U/
                    └── core/
                        ├── MemoryTest.kt
                        ├── CPUTest.kt
                        ├── PPUTest.kt
                        ├── APUTest.kt
                        ├── EmulatorTest.kt
                        ├── ROMLoaderTest.kt
                        └── CBOpcodeHandlerTest.kt
```

### Version Android (Android Studio)

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hello_dev0ps/
│   │   │           └── gbcemus22u/
│   │   │               ├── core/          # Même structure que la version desktop
│   │   │               ├── ui/
│   │   │               │   └── GbcCanvas.kt  # Composant d'affichage
│   │   │               └── MainActivity.kt   # Activité principale
│   │   └── assets/
│   │       └── cpu_instrs.gb
│   └── test/
│       └── java/
│           └── com/
│               └── hello_dev0ps/
│                   └── gbcemus22u/
│                       └── core/          # Tests unitaires
```

## Fonctionnalités Implémentées

### Core

- **Memory** : Gestion complète de la mémoire (ROM, RAM, VRAM, WRAM, etc.)

  - Support des différents types de cartouches (MBC1, MBC2, MBC3, MBC5)
  - Gestion du RTC pour MBC3
  - Banking de mémoire

- **CPU** : Implémentation du processeur

  - Registres (AF, BC, DE, HL, SP, PC)
  - Flags (Zero, Subtract, Half Carry, Carry)
  - Opcodes de base (NOP, LD, INC, DEC, JP, etc.)
  - Gestion des cycles d'horloge
  - Support des interruptions

- **PPU** : Processeur graphique

  - Modes d'affichage
  - Rendu des tuiles et sprites
  - Gestion des palettes
  - Scrolling
  - Interruptions VBlank, HBlank

- **APU** : Processeur audio

  - 4 canaux sonores
  - Enveloppes sonores
  - Sons carrés, ondes, bruit
  - Contrôle du volume et de la fréquence

- **ROMLoader** : Chargement des ROMs
  - Validation des ROMs
  - Détection du type de cartouche
  - Gestion des en-têtes
  - Support des différentes tailles

### Tests

- Tests unitaires complets pour chaque composant
- Validation des opérations de mémoire
- Tests des opcodes CPU
- Tests du chargement des ROMs
- Tests PPU et APU

### Interface Android

- Composant `GbcCanvas` pour l'affichage
- Interface utilisateur avec Jetpack Compose
- Contrôles de base (Start/Stop, Reset)
- Affichage des informations de debug

## Fonctionnalités à Implémenter

### Court Terme

1. **Interface Android**

   - Contrôles tactiles (croix, boutons A/B, Start/Select)
   - Menu de sélection de ROM
   - Sauvegarde/chargement d'état
   - Options de configuration

2. **Optimisations**
   - Amélioration des performances
   - Gestion de la batterie
   - Support des différents ratios d'écran

### Moyen Terme

1. **Fonctionnalités GBC**

   - Support des couleurs
   - Double vitesse CPU
   - Registres spécifiques GBC

2. **Améliorations**
   - Support des sauvegardes
   - Cheats
   - Netplay (multi-joueur en ligne)

### Long Terme

1. **Fonctionnalités Avancées**
   - Support des link cables
   - Support des accessoires (Game Boy Printer, etc.)
   - Débogueur intégré
   - Support des homebrews

## Comment Tester

### Version Desktop

1. Cloner le dépôt
2. Ouvrir dans VSCode
3. Exécuter les tests : `./gradlew test`
4. Lancer l'application : `./gradlew run`

### Version Android

1. Cloner le dépôt
2. Ouvrir dans Android Studio
3. Exécuter les tests : `./gradlew test`
4. Lancer sur un émulateur ou un appareil Android

## Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request