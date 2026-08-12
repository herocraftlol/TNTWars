<div align="center">

# 💣 TntWars

### Plugin de mini-jeu **TNT Wars** pour Minecraft Paper 1.21

Arènes par équipes · Canons à TNT · Schémas débloquables · Progression par niveaux · Coffres infinis · GUI · Classement · Cosmétiques · Tournois

`version 1.2.0` · `API 1.21` · `Java 21`

</div>

---

## 🎯 Qu'est-ce que TntWars ?

**TntWars** est un plugin de mini-jeu qui met aux prises plusieurs équipes dans une arène
où l'objectif est de détruire la map adverse à l'aide de **canons à TNT**. Chaque équipe
dispose de sa propre zone de construction et d'un **coffre infini** rempli de tout le
matériel nécessaire (TNT, redstone, dispensers, slime blocks, seaux d'eau…). Le sol se
fait exploser petit à petit, et **tomber dans le vide** équivaut à une élimination. La
dernière équipe encore debout l'emporte.

Le plugin reprend l'architecture d'un mini-jeu par arènes (menu GUI, console par arène,
classement, cosmétiques, tournois) entièrement réécrite et adaptée au gameplay du TNT
Wars : coffres qui se régénèrent, zones de construction isolées par équipe, régénération
complète de la map en fin de partie et élimination par chute.

> 💡 Idéal pour les serveurs cherchant un mini-jeu explosif, compétitif et facile à
> configurer en jeu, sans dépendance externe autre que Paper.

---

## ✨ Fonctionnalités

- **Arènes entièrement configurables en jeu** : map (pos1/pos2), zone de coffre infini,
  zones de construction par équipe, spawns par équipe, salle d'attente, nombre d'équipes
  et de joueurs par équipe.
- **Coffres infinis** : un double coffre rempli des composants de canon à TNT ne peut pas
  être cassé et se remplit automatiquement dès qu'un joueur y prend un item — il reste
  donc toujours plein.
- **Zones de construction séparées** : chaque équipe ne peut construire/casser que dans
  sa propre zone. La zone neutre est protégée.
- **Régénération automatique de la map** : toute la map est sauvegardée au lancement de
  la partie et restaurée bloc par bloc (étalée sur plusieurs ticks) à la fin.
- **Élimination par chute** : un joueur tombant sous la limite basse de la map (sol
  détruit) est éliminé et passe spectateur ; il peut toujours faire `/tnt leave`.
- **Salle d'attente** façon HikaBrain avec 3 items :
  - 💎 **Diamant** (slot 0) : lance la partie, visible uniquement par les admins.
  - 🟥/🟩 **Béton coloré** : change d'équipe en cliquant (cycle rouge → vert → …).
  - 🧱 **Barrière** : quitte l'arène et revient à la position d'avant le `/tnt join`.
- **GUI** : menu des arènes, console d'administration par arène, classement, cosmétiques,
  schémas de canons.
- **Classement / Leaderboard** : kills, morts, victoires, défaites, K/D, persistés dans
  `stats.yml`, + hologrammes de classement placables.
- **Cosmétiques** : effets visuels/sonores de kill sélectionnables via GUI.
- **Tournois** : inscription par équipes, génération automatique d'un tableau à
  élimination directe, matchs enchaînés automatiquement sur une arène dédiée.

### 🧨 Schémas de canons à TNT débloquables *(nouveau v1.2.0)*

Le plugin embarque une **bibliothèque de schémas de canons à TNT** prêts à l'emploi, du
plus simple au plus avancé. Chaque schéma se **débloque en montant de niveau** et peut
être **visualisé en jeu** sous forme de blocs fantômes :

| Schéma | Niveau | Description |
|---|---|---|
| 🟢 Lance-TNT basique | 1 | Distributeur vers le haut + levier : la base de tout canon. |
| 🟢 Canon à retard (répéteurs) | 2 | Répéteur dans la ligne redstone pour synchroniser/retarder. |
| 🟡 Canon à rebond (slime) | 3 | Souffle d'explosion + slime block pour propulser une TNT très loin. |
| 🟡 Canon à eau (tube) | 4 | TNT poussée par un courant d'eau dans un tube de verre : précis, sans perte. |
| 🔴 Canon semi-automatique (comparateur) | 5 | Trémie + coffre d'alimentation + comparateur pour un tir soutenu. |
| 🔴 Canon double synchronisé | 6 | Deux distributeurs reliés par une seule ligne de redstone. |

Chaque schéma est livré avec un **livre de plans** (written book) expliquant l'agencement
des blocs et l'ordre de construction.

### 📈 Progression par niveaux *(nouveau v1.2.0)*

Chaque action en jeu rapporte des **points** configurables (`config.yml`, section
`points`) ; atteindre un palier de points fait monter d'un **niveau** et débloque de
nouveaux schémas de canons.

- 💀 Kill : `15` points · 🏆 Victoire : `50` points
- 🧨 TNT envoyée depuis sa zone : `1` point / TNT
- 🧱 Bloc adverse détruit : `2` points / bloc
- ✦ Canon fonctionnel (1ʳᵉ TNT atteignant la zone adverse) : `25` points bonus

> Les **TNT sont attribuées à l'équipe** qui les a amorcées dans sa zone, et les points
> sont distribués à toute l'équipe en ligne (action collective). Un **canon
> fonctionnel** est salué dans l'arène lors de la première touche adverse.

---

## 📦 Installation

1. Téléchargez le fichier **`TntWars.jar`** depuis la [page des releases](../../releases).
2. Placez-le dans le dossier `plugins/` de votre serveur **Paper 1.21** (ou Spigot 1.21).
3. Redémarrez (ou rechargez) le serveur.
4. Configurez vos arènes en jeu (voir la section ci-dessous).

> ⚠️ Prérequis serveur : **PaperMC 1.21** et **Java 21**.

## 🔨 Compilation depuis les sources

```bash
git clone https://github.com/herocraftlol/TNTWars.git
cd TNTWars
mvn clean package
```

Le jar compilé sera généré dans `target/TntWars.jar`. Prérequis : JDK 21 et une connexion
internet (pour télécharger `paper-api` depuis le dépôt Maven de PaperMC).

## Commandes principales

| Commande | Description |
|---|---|
| `/tnt` | Ouvre le menu des arènes |
| `/tnt join [arène]` | Rejoint une arène (ou ouvre le menu si aucune n'est précisée) |
| `/tnt leave` | Quitte l'arène en cours |
| `/tnt top` | Classement |
| `/tnt cosmetics` | Choisir son effet de kill |
| `/tnt schema` | Ouvre le menu des schémas de canons (aperçu en blocs fantômes) |
| `/tnt schema hide` | Masque l'aperçu du schéma affiché |
| `/tnt level` | Affiche votre progression (points / niveau) |
| `/tnt tournament create\|register\|start\|list` | Gestion des tournois |

## Commandes admin (`tntwars.admin`)

| Commande | Description |
|---|---|
| `/tnt create <nom>` | Crée une arène |
| `/tnt delete <nom>` | Supprime une arène |
| `/tnt setpos1 <nom>` / `setpos2` | Définit la map (bloc visé) |
| `/tnt setchestpos1 <nom>` / `setchestpos2` | Définit la zone du coffre infini |
| `/tnt setzone1 <nom> <équipe>` / `setzone2` | Définit la zone de construction d'une équipe |
| `/tnt setspawn <nom> <équipe>` | Définit le spawn d'une équipe (position du joueur) |
| `/tnt setwaiting <nom>` | Définit le spawn de la salle d'attente |
| `/tnt setteams <nom> <n>` | Nombre d'équipes |
| `/tnt setteamsize <nom> <n>` | Joueurs par équipe |
| `/tnt console <nom>` | Ouvre la console d'administration de l'arène |
| `/tnt forcestart <nom>` / `/tnt stop <nom>` | Force le lancement / arrête la partie |
| `/tnt hologram create\|remove <id>` | Hologramme de classement |

## Mise en place d'une arène (étape par étape)

1. `/tnt create arene1`
2. Construisez votre map. Placez un double coffre et remplissez-le des composants de
   canon à TNT (il deviendra le coffre infini).
3. Visez un coin de la map puis `/tnt setpos1 arene1`, visez le coin opposé puis
   `/tnt setpos2 arene1`.
4. Visez un coin du double coffre puis `/tnt setchestpos1 arene1`, l'autre coin puis
   `/tnt setchestpos2 arene1`.
5. Pour chaque équipe (0, 1, ...) : visez les 2 coins de sa zone de construction avec
   `/tnt setzone1 arene1 0` / `/tnt setzone2 arene1 0`, puis placez-vous à son point de
   spawn et faites `/tnt setspawn arene1 0`.
6. Placez-vous dans le lobby d'attente et faites `/tnt setwaiting arene1`.
7. Ajustez `/tnt setteams` et `/tnt setteamsize` si besoin (par défaut 2 équipes de 4).

Dès que tout est configuré, l'arène passe automatiquement en état `WAITING` et devient
rejoignable.

## 🛠️ Notes techniques

- Compatible **Paper 1.21**, Java 21.
- Aucune dépendance externe autre que `paper-api`.
- Les données sont stockées en YAML dans le dossier du plugin : `arenas.yml`,
  `stats.yml`, `progression.yml`, `leaderboards.yml`, `config.yml`.
- Pour un respawn instantané des joueurs éliminés (sans écran de mort), le plugin force
  `player.spigot().respawn()` — vous pouvez aussi activer le gamerule
  `doImmediateRespawn` sur le monde de vos arènes.

---

## 📋 Changelog

### v1.2.0

- 🧨 **Schémas de canons à TNT** : bibliothèque de 6 schémas prêts à l'emploi (lance-TNT
  basique, canon à retard, canon à rebond slime, canon à eau, canon semi-automatique,
  canon double synchronisé), chacun avec un livre de plans expliquant la construction.
- 📈 **Progression par niveaux** : système de points (kills, victoires, TNT envoyées,
  blocs adverses détruits, canon fonctionnel) et paliers de niveaux déblocant les
  schémas. Configurable dans `config.yml` (`points`, `levels.thresholds`).
- 👁️ **Aperçu de schémas en blocs fantômes** : visualisation en jeu du schéma sélectionné
  devant le joueur via `sendBlockChange` (côté client uniquement, le monde n'est pas
  modifié), orienté selon la direction regardée, auto-expirant ou masquable avec
  `/tnt schema hide`.
- 🎯 **Suivi des TNT par équipe** : attribution des TNT amorcées à l'équipe de la zone
  d'origine et récompense collective ; bonus « canon fonctionnel » à la première touche
  adverse d'une partie.
- 🖥️ Nouveau GUI **Schémas de canons** (`/tnt schema`) listant les schémas débloqués /
  verrouillés (avec niveau requis).
- 📝 Nouvelles commandes `/tnt schema` (et `/tnt schema hide`) et `/tnt level`.
- 🐛 **Corrections de compilation** : import `TNTPrimeEvent` corrigé (`io.papermc.paper`
  → `org.bukkit.event.block`) et `Attribute.MAX_HEALTH` → `GENERIC_MAX_HEALTH` (API
  Paper 1.21).
- 📝 Documentation enrichie (README) et description du plugin mise à jour.
- 🏷️ Mise à jour du numéro de version (`1.1.0` → `1.2.0`) dans `pom.xml` et `plugin.yml`.

### v1.1.0

- 🐛 Correction de compilation : `Attribute.MAX_HEALTH` → `Attribute.GENERIC_MAX_HEALTH`.
- ✅ Compilation vérifiée avec `mvn clean package` (JDK 21 + paper-api 1.21.1).
- 📝 README retravaillé ; mise à jour de la version (`1.0.0` → `1.1.0`).

### v1.0.0

- Version initiale : arènes par équipes, canons à TNT, coffres infinis, zones de
  construction, régénération de map, élimination par chute, salle d'attente, GUI,
  classement/leaderboard, cosmétiques et tournois.

---

<div align="center">

_Suivi des versions via les [releases GitHub](../../releases)._

</div>
