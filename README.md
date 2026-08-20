<div align="center">

# 💣 TntWars

### Plugin de mini-jeu **TNT Wars** pour Minecraft Paper 1.21

Arènes par équipes · Canons à TNT · Schémas **constructibles en jeu** · Progression par niveaux · Coffres infinis · GUI · Classement · Cosmétiques · Tournois

`version 1.5.0` · `API 1.21` · `Java 21`

</div>

---

## 🎯 Qu'est-ce que TntWars ?

**TntWars** est un plugin de mini-jeu qui met aux prises plusieurs équipes dans une arène
où l'objectif est de **faire sauter la map adverse** à l'aide de **canons à TNT**. Chaque
équipe dispose de sa propre zone de construction et d'un **coffre infini** rempli de tout
le matériel nécessaire (TNT, redstone, dispensers, slime blocks, seaux d'eau…). Le sol se
fait exploser petit à petit, et **tomber dans le vide** équivaut à une élimination. La
dernière équipe encore debout l'emporte.

Le plugin reprend l'architecture d'un mini-jeu par arènes (menu GUI, console par arène,
classement, cosmétiques, tournois) entièrement réécrite et adaptée au gameplay du TNT
Wars : coffres qui se régénèrent, zones de construction isolées par équipe, régénération
complète de la map en fin de partie et élimination par chute.

> 💡 Idéal pour les serveurs cherchant un mini-jeu explosif, compétitif et facile à
> configurer entièrement en jeu, sans dépendance externe autre que Paper.

---

## 🆕 Nouveautés de la v1.5.0

- 🛡️ **Commandes à l'épreuve des erreurs** : toutes les sous-commandes `/tnt` passent
  désormais par un **filet de sécurité global**. Si une erreur interne survient pendant
  l'exécution d'une commande, elle est **interceptée proprement** au lieu de planter :
  le joueur reçoit un message clair (« Une erreur interne est survenue… ») avec le
  détail de l'exception, et la **trace complète est enregistrée dans la console du
  serveur** pour faciliter le diagnostic.
- 🔎 **Logs de diagnostic enrichis** : chaque erreur de commande indique désormais
  **quelle commande a échoué et qui l'a lancée**, ce qui accélère grandement la
  résolution des problèmes sur les serveurs en production.
- 🧰 **Compilation & version** : passage à `1.5.0` (`pom.xml` / `plugin.yml`),
  correction de l'import `TNTPrimeEvent` selon la version de Paper.

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
  détruit) est éliminé et passe spectateur.
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
- **Pioche du bâtisseur** : chaque joueur reçoit en partie une pioche en netherite
  **incassable, verrouillée dans le slot 1** — impossible à déplacer, jeter ou perdre.
- **Nettoyage automatique des arènes** : les items au sol et les orbes d'expérience sont
  supprimés toutes les 5 secondes en partie, pour des performances optimales.

### 🧨 Schémas de canons à TNT débloquables

Le plugin embarque une **bibliothèque de schémas de canons à TNT** prêts à l'emploi, du
plus simple au plus avancé. Chaque schéma se **débloque en montant de niveau**, et peut
être **construit pour de vrai en jeu directement depuis le GUI** (depuis la v1.4.0) :

| Schéma | Niveau | Description |
|---|---|---|
| 🟢 Lance-TNT basique | 1 | Distributeur vers le haut + levier : la base de tout canon. |
| 🟢 Canon à retard (répéteurs) | 2 | Répéteur dans la ligne redstone pour synchroniser/retarder. |
| 🟡 Canon à rebond (slime) | 3 | Souffle d'explosion + slime block pour propulser une TNT très loin. |
| 🟡 Canon à eau (tube) | 4 | TNT poussée par un courant d'eau dans un tube de verre : précis, sans perte. |
| 🔴 Canon semi-automatique (comparateur) | 5 | Trémie + coffre d'alimentation + comparateur pour un tir soutenu. |
| 🔴 Canon double synchronisé | 6 | Deux distributeurs reliés par une seule ligne de redstone. |
| 🔴 Canon à TNT puissant | 7 | Double batterie convergente (7 distributeurs) + canal d'eau pour un tir séquentiel dévastateur. |

Chaque schéma est livré avec un **livre de plans** (written book) expliquant l'agencement
des blocs et l'ordre de construction.

### 📈 Progression par niveaux

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
2. Placez-le dans le dossier `plugins/` de votre serveur **Paper 1.21**.
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
| `/tnt schema` | Menu des schémas de canons — cliquez pour **construire réellement** le canon en jeu |
| `/tnt schema cancel` | Arrête une construction de schéma en cours (les blocs déjà posés restent) |
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
| `/tnt debug <nom> [fix]` | Diagnostic d'une arène / déblocage si elle est coincée |
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

## ⚙️ Configuration (`config.yml`)

```yaml
game:
  countdown-seconds: 30        # décompte avant le départ
  min-players-to-start: 2
  restarting-seconds: 8        # temps avant la régénération de la map
  post-game-seconds: 5         # temps d'écran de fin avant le retour au lobby (v1.4.0)
  default-teams: 2
  default-team-size: 4
  friendly-fire: false

points:
  kill: 15
  win: 50
  tnt-launched: 1
  enemy-block-destroyed: 2
  cannon-functional-bonus: 25

levels:
  thresholds: [0, 50, 150, 300, 600, 1000, 1500, 2200, 3000]
```

## 🛠️ Notes techniques

- Compatible **Paper 1.21**, Java 21.
- Aucune dépendance externe autre que `paper-api`.
- Les données sont stockées en YAML dans le dossier du plugin : `arenas.yml`,
  `stats.yml`, `progression.yml`, `leaderboards.yml`, `config.yml`.
- Pour un respawn instantané des joueurs éliminés (sans écran de mort), le plugin force
  `player.spigot().respawn()` — vous pouvez aussi activer le gamerule
  `doImmediateRespawn` sur le monde de vos arènes.
- Si `TNTPrimeEvent` ne compile pas avec votre version exacte de paper-api, remplacez
  l'import `com.destroystokyo.paper.event.block.TNTPrimeEvent` par
  `io.papermc.paper.event.block.TNTPrimeEvent` dans `CannonTrackerListener.java` — les
  deux variantes existent selon les versions de Paper, l'une étant un alias de l'autre.

---

## 📋 Changelog

### v1.5.0

- 🛡️ **Gestion globale des erreurs de commandes** : toutes les sous-commandes `/tnt`
  sont entourées d'un try/catch — une erreur interne n'interrompt plus la commande en
  silence, le joueur reçoit un message explicite avec le détail de l'exception, et la
  trace complète (commande + expéditeur) est loguée en console (`TntCommand`).
- 🧰 Passage en `1.5.0`, correction de l'import `TNTPrimeEvent` pour compatibilité Paper.


### v1.4.0

- 🔨 **Construction réelle des schémas de canons** : clic sur un schéma débloqué =
  construction bloc par bloc (~1 bloc / ⅓ s) dans sa zone d'équipe en pleine partie,
  distributeurs auto-remplis de TNT, refus si le schéma dépasse de la zone.
  Remplace l'ancien aperçu en blocs fantômes. `/tnt schema cancel` (ou `hide`) pour
  arrêter.
- 🏁 **Fin de partie repensée** : tous les joueurs passent spectateurs en fin de partie,
  titres VICTOIRE / DÉFAITE / Match nul personnalisés, puis retour automatique au lobby
  après un délai configurable (`game.post-game-seconds`).
- 🧭 **Spectateurs confinés** (`SpectatorContainmentListener`) : impossible de quitter
  les limites de la map en volant pendant la régénération.
- 🛠️ **`/tnt debug <arène> [fix]`** : diagnostic complet + déblocage manuel d'une arène
  coincée ; bouton dédié dans `/tnt console` ; filet de sécurité automatique en cas
  de blocage prolongé.
- 🧰 Passage en `1.4.0`, correction de l'import `TNTPrimeEvent` pour compatibilité Paper.

### v1.3.0

- 🧨 **Nouveau schéma de canon débloquable au niveau 7 : le Canon à TNT puissant** —
  double batterie convergente de 7 distributeurs avec canal d'eau, accumulation et tir
  final déclenchables séparément.
- 👷 **Aperçu des schémas revisité** : prévisualisation progressive bloc par bloc,
  orientée selon la direction regardée.
- ⛏️ **Pioche du bâtisseur** : pioche en netherite incassable verrouillée dans le slot 1.
- 🧹 **Nettoyage automatique des arènes** : items au sol et orbes d'XP supprimés toutes
  les 5 secondes en partie.

### v1.2.0

- 🧨 **Schémas de canons à TNT** : bibliothèque de 6 schémas (lance-TNT basique, canon à
  retard, canon à rebond slime, canon à eau, canon semi-automatique, canon double
  synchronisé), chacun avec un livre de plans.
- 📈 **Progression par niveaux** : système de points (kills, victoires, TNT, blocs
  détruits, canon fonctionnel) déblocant les schémas.
- 👁️ **Aperçu de schémas en blocs fantômes** : visualisation côté client via
  `sendBlockChange`.
- 🎯 **Suivi des TNT par équipe** avec récompense collective et bonus « canon
  fonctionnel ».
- 🖥️ Nouveau GUI **Schémas de canons** (`/tnt schema`).

### v1.1.0

- 🐛 Correction de compilation : `Attribute.MAX_HEALTH` → `Attribute.GENERIC_MAX_HEALTH`.
- ✅ Compilation vérifiée avec `mvn clean package` (JDK 21 + paper-api 1.21.1).

### v1.0.0

- Version initiale : arènes par équipes, canons à TNT, coffres infinis, zones de
  construction, régénération de map, élimination par chute, salle d'attente, GUI,
  classement/leaderboard, cosmétiques et tournois.

---

<div align="center">

_Suivi des versions via les [releases GitHub](../../releases)._

</div>
