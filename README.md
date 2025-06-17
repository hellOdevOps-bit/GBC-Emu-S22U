
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

- Contrôles tactiles : 🅰 🅱 Start/Select + D-Pad
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
