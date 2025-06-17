# GBC Emulator S22U

Un **émulateur Game Boy Color** développé avec amour en **Kotlin**, compatible **Android** et **Desktop** (JVM).  
Projet hybride né de l’alliance redoutable entre Android Studio, VSCode, Cursor et la passion du pixel !

---

## Prérequis

- **Java 17** ou plus récent  
- **Kotlin 1.9+**
- **Gradle 8.1+**
- **Android Studio Giraffe ou +** (pour la version mobile)
- **Un appareil Android** (API 30+) ou un émulateur

---

## Structure du projet

### Version Desktop (VSCode)

src/
├── main/
│ └── java/com/Hello_Dev0ps/GBC-Emu-S22U/core/
│ ├── Memory.kt
│ ├── CPU.kt
│ ├── PPU.kt
│ ├── APU.kt
│ ├── Emulator.kt
│ ├── ROMLoader.kt
│ └── CBOpcodeHandler.kt
├── assets/
│ └── cpu_instrs.gb
└── test/
└── java/com/Hello_Dev0ps/GBC-Emu-S22U/core/
├── MemoryTest.kt
├── CPUTest.kt
├── PPUTest.kt
├── APUTest.kt
├── EmulatorTest.kt
├── ROMLoaderTest.kt
└── CBOpcodeHandlerTest.kt

### Version Android (Android Studio)

app/
├── src/
│ ├── main/java/com/hello_dev0ps/gbcemus22u/
│ │ ├── core/ # Même logique que la version Desktop
│ │ ├── ui/
│ │ │ └── GbcCanvas.kt # Canvas personnalisé pour le rendu
│ │ └── MainActivity.kt # Entrée principale de l'app Android
│ └── assets/
│ └── cpu_instrs.gb
├── test/java/com/hello_dev0ps/gbcemus22u/core/
│ └── ... # Tous les tests unitaires
└── androidTest/ # Tests instrumentés (à venir)

---

## Fonctionnalités Implémentées

### Cœur de l’émulateur

- **Memory** :
  - Mapping mémoire complet (ROM, RAM, VRAM, etc.)
  - Gestion des MBC1/2/3/5
  - Real-Time Clock (RTC MBC3)

- **CPU** :
  - Registres AF, BC, DE, HL, SP, PC
  - Flags : Z, N, H, C
  - Support des interruptions
  - Gestion des cycles + Opcodes

- **PPU (graphisme)** :
  - Modes LCD
  - Rendu des tuiles et sprites
  - Palettes, scrolling, VBlank / HBlank

- **APU (audio)** :
  - Canaux carrés, ondes, bruit
  - Volume, sweep, enveloppes
  - Buffer audio natif

- **ROMLoader** :
  - Validation et parsing des headers
  - Détection des tailles, MBC, batterie
  - Lecture depuis les assets

### Tests Unitaires

- Couverture complète de la stack (CPU, PPU, APU, Memory, ROM)
- ROM de test incluse : `cpu_instrs.gb`

### Interface Android

- UI avec **Jetpack Compose**
- Canvas custom : `GbcCanvas`
- Boutons : Start / Stop / Reset
- Debug zone : FPS, cycles CPU

---

## Fonctionnalités à Implémenter

### Court Terme

- Contrôles tactiles : A, B, Start/Select + D-Pad
- Menu de chargement de ROM
- Save/Load state
- Options utilisateur

### Moyen Terme

- Support couleurs GBC
- Fréquence double (CGB Mode)
- Cheats (GameShark)
- Gestion mémoire batterie + saves
- Optimisations CPU/GPU

### Long Terme

- Débogueur intégré
- Netplay via Wi-Fi
- Traductions FR/EN
- Cloud save avec Firebase

---

## Contrôles Android prévus

| Bouton       | Fonction             |
|--------------|----------------------|
| A / B        | Boutons d'action     |
| Start / Select | Navigation         |
| D-Pad        | Déplacement          |
| Reset        | Redémarrage du jeu   |

---

## À propos des ROMs

> ** Note légale :**  
> Aucune ROM commerciale n’est fournie. Seule la ROM de test `cpu_instrs.gb` est incluse à des fins éducatives.  
> Tu dois **posséder la cartouche originale** pour utiliser une ROM de jeu.

---

## Comment exécuter

### Desktop

```bash
git clone https://github.com/Hello-Dev0ps/GBCEmuS22U.git
cd GBCEmuS22U
./gradlew run

## Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request




“Because emulating the past is building the future.”
— @Hello_Dev0ps
