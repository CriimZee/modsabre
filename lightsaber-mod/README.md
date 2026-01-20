# ⚔️ Lightsaber Mod for Hytale

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Hytale](https://img.shields.io/badge/Hytale-Early%20Access-green)

Un mod complet qui ajoute des sabres laser avec effets de lumière dynamiques à Hytale !

## ✨ Fonctionnalités

### 🗡️ Sabres Laser
- **6 couleurs disponibles** : Bleu, Vert, Rouge, Violet, Jaune, Blanc
- **Activation/Désactivation** avec clic droit
- **Animations fluides** d'allumage et d'extinction
- **Sons iconiques** (activation, bourdonnement, swing, impact)

### 💡 Effets de Lumière Dynamiques
- Chaque sabre illumine son environnement
- Couleur de lumière correspondant à la lame
- Effet de scintillement pour les sabres Sith (rouge)
- Rayon lumineux ajustable selon la couleur

### ✨ Effets Visuels
- **Particules de glow** autour de la lame
- **Traînées lumineuses** lors des swings
- **Étincelles** lors des impacts
- **Flash** lors des clashes entre sabres

### ⚔️ Système de Combat
- Dégâts augmentés avec sabre activé
- **Clashes** entre deux sabres laser (knockback + effets)
- **Système de duel** entre joueurs
- Animations de combat

### 🏆 Système de Duel
- Défiez d'autres joueurs avec `/lightsaber duel <joueur>`
- Compte à rebours épique
- Annonce du vainqueur
- Effets visuels de victoire/défaite

## 📦 Installation

### Pack (Assets & Contenu)
1. Téléchargez le dossier `pack/`
2. Placez-le dans `%APPDATA%/Hytale/Packs/` (Windows) ou `~/.hytale/Packs/` (Linux/Mac)
3. Activez le pack dans le menu des mods de Hytale

### Plugin (Logique serveur)
1. Compilez le plugin avec Maven : `mvn clean package`
2. Copiez `LightsaberMod-1.0.0.jar` dans le dossier `Mods/` de votre serveur
3. Redémarrez le serveur

## 🎮 Utilisation

### Commandes
| Commande | Description |
|----------|-------------|
| `/lightsaber give [couleur] [joueur]` | Donne un sabre laser |
| `/lightsaber colors` | Liste les couleurs disponibles |
| `/lightsaber duel <joueur>` | Défier un joueur |
| `/lightsaber duel accept` | Accepter un duel |
| `/lightsaber duel decline` | Refuser un duel |

### Contrôles
- **Clic droit** : Activer/Désactiver le sabre
- **Clic gauche** : Attaquer (avec effets quand activé)

## 🎨 Couleurs Disponibles

| Couleur | Description | Caractéristiques |
|---------|-------------|------------------|
| 🔵 Bleu | Jedi Guardian | Équilibré |
| 🟢 Vert | Jedi Consular | Équilibré |
| 🔴 Rouge | Sith | Plus de dégâts, scintillement |
| 🟣 Violet | Rare | Rayon lumineux étendu |
| 🟡 Jaune | Jedi Sentinel | Attaque rapide |
| ⚪ Blanc | Purifié | Lumière maximale |

## 🔧 Crafting

### Poignée de Sabre Laser
```
  I
 IGI
  I
```
- I = Lingot de Fer
- G = Lingot d'Or

### Sabre Laser
```
 C
 H
 B
```
- C = Cristal Kyber (de la couleur souhaitée)
- H = Poignée de Sabre
- B = Batterie

### Sabres Spéciaux
- **Violet** : Cristal Bleu + Cristal Rouge + Poignée + Batterie
- **Jaune** : Cristal Vert + 2x Or + Poignée + Batterie
- **Blanc** : Cristal Rouge + Essence de Purification + Poignée + Batterie

## 📁 Structure du Mod

```
lightsaber-mod/
├── pack/
│   ├── manifest.json
│   ├── assets/
│   │   ├── models/          # Modèles 3D Blockbench
│   │   ├── textures/        # Textures des items
│   │   ├── sounds/          # Effets sonores
│   │   ├── particles/       # Définitions de particules
│   │   └── animations/      # Animations des sabres
│   └── behaviors/
│       └── items/           # Définitions des items et recettes
│
└── plugin/
    ├── pom.xml
    └── src/main/java/com/lightsaber/
        ├── LightsaberPlugin.java
        ├── commands/
        ├── data/
        ├── events/
        └── managers/
```

## 🛠️ Compilation

```bash
cd lightsaber-mod/plugin
mvn clean package
```

Le JAR compilé sera dans `target/LightsaberMod-1.0.0.jar`

## 📝 Permissions

| Permission | Description | Défaut |
|------------|-------------|--------|
| `lightsaber.give` | Donner des sabres | OP |
| `lightsaber.craft` | Crafter des sabres | Tous |
| `lightsaber.duel` | Participer aux duels | Tous |

## 🔊 Sons Requis

Pour les sons, vous devez fournir les fichiers audio suivants dans `assets/sounds/lightsaber/`:
- `lightsaber_on_01.ogg`, `lightsaber_on_02.ogg`
- `lightsaber_off_01.ogg`
- `lightsaber_hum_loop.ogg`
- `lightsaber_hum_sith_loop.ogg`
- `lightsaber_swing_01.ogg` à `lightsaber_swing_04.ogg`
- `lightsaber_hit_01.ogg` à `lightsaber_hit_03.ogg`
- `lightsaber_clash_01.ogg` à `lightsaber_clash_03.ogg`

> 💡 Vous pouvez créer ou télécharger des sons libres de droits similaires aux effets Star Wars.

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
- Signaler des bugs
- Proposer des améliorations
- Soumettre des pull requests

## 📄 License

MIT License - Libre d'utilisation et modification.

## 🙏 Crédits

- Créé par Claude AI
- Inspiré par Star Wars™
- Utilise l'API de modding Hytale

---

**May the Force be with you!** ⚔️✨
