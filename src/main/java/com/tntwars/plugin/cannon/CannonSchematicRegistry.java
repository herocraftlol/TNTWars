package com.tntwars.plugin.cannon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bibliothèque des schémas de canons à TNT connus du plugin, du plus simple au plus
 * avancé. Chaque schéma se débloque à un niveau de joueur donné (voir
 * {@link com.tntwars.plugin.progression.ProgressManager}) et peut être visualisé en jeu
 * avec des blocs "fantômes" visibles uniquement par le joueur.
 *
 * Ce sont des modèles pédagogiques : ils illustrent clairement l'agencement des
 * composants (dispenser, redstone, répéteurs, comparateurs, slime, eau...) ; le livre
 * associé donne en plus les explications de fonctionnement et l'ordre de construction.
 */
public class CannonSchematicRegistry {

    private final Map<String, CannonSchematic> byId = new LinkedHashMap<>();

    public CannonSchematicRegistry() {
        register(basicLauncher());
        register(delayedCannon());
        register(slimeBounceCannon());
        register(waterCannon());
        register(semiAutoCannon());
        register(doubleCannon());
        register(powerfulWaterCannon());
    }

    private void register(CannonSchematic schema) {
        byId.put(schema.getId(), schema);
    }

    public CannonSchematic get(String id) {
        return byId.get(id);
    }

    public List<CannonSchematic> all() {
        return new ArrayList<>(byId.values());
    }

    public List<CannonSchematic> schematicsForLevel(int level) {
        List<CannonSchematic> result = new ArrayList<>();
        for (CannonSchematic s : byId.values()) {
            if (s.getRequiredLevel() == level) result.add(s);
        }
        return result;
    }

    private RelativeBlock b(int dx, int dy, int dz, Material material) {
        return new RelativeBlock(dx, dy, dz, material);
    }

    private RelativeBlock d(int dx, int dy, int dz, String data) {
        return new RelativeBlock(dx, dy, dz, data);
    }

    // ── Schéma 1 : Lance-TNT basique ─────────────────────────────────────

    private CannonSchematic basicLauncher() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(d(0, 1, 0, "minecraft:dispenser[facing=up]"));
        blocks.add(b(1, 0, 0, Material.STONE));
        blocks.add(d(1, 1, 0, "minecraft:lever[face=floor,facing=north,powered=false]"));

        List<String> instr = List.of(
                "1. Posez un bloc plein (pierre) puis un distributeur dessus, orienté vers le haut.",
                "2. Remplissez le distributeur de TNT.",
                "3. Posez un levier sur le bloc juste à côté du distributeur.",
                "4. Actionnez le levier : le distributeur éjecte une TNT amorcée vers le haut.",
                "C'est la base de tout canon : un distributeur rempli de TNT + une source de redstone."
        );
        return new CannonSchematic("basic_launcher", "Lance-TNT basique", Material.DISPENSER, 1, blocks, instr);
    }

    // ── Schéma 2 : Canon à retard (répéteurs) ────────────────────────────

    private CannonSchematic delayedCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(d(0, 1, 0, "minecraft:dispenser[facing=north]"));
        blocks.add(b(0, 0, 1, Material.STONE));
        blocks.add(b(0, 1, 1, Material.REDSTONE_WIRE));
        blocks.add(b(0, 0, 2, Material.STONE));
        blocks.add(d(0, 1, 2, "minecraft:repeater[facing=south,delay=4,locked=false,powered=false]"));
        blocks.add(b(0, 0, 3, Material.STONE));
        blocks.add(b(0, 1, 3, Material.REDSTONE_WIRE));
        blocks.add(b(0, 0, 4, Material.STONE));
        blocks.add(d(0, 1, 4, "minecraft:stone_button[face=floor,facing=north,powered=false]"));

        List<String> instr = List.of(
                "1. Construisez une ligne de blocs pleins avec un distributeur (facing horizontal) à un bout.",
                "2. Reliez-le à un bouton par de la poudre de redstone.",
                "3. Insérez un répéteur (délai 4) dans la ligne pour retarder légèrement le signal.",
                "4. Utile pour synchroniser plusieurs distributeurs qui doivent tirer au même moment.",
                "Le répéteur permet aussi d'éviter qu'un signal trop rapide ne 'saute' un distributeur."
        );
        return new CannonSchematic("delayed_cannon", "Canon à retard (répéteurs)", Material.REPEATER, 2, blocks, instr);
    }

    // ── Schéma 3 : Canon à rebond (slime) ────────────────────────────────

    private CannonSchematic slimeBounceCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(b(0, 1, 0, Material.SLIME_BLOCK));
        blocks.add(b(0, 2, 0, Material.TNT));
        blocks.add(b(0, 3, 0, Material.TNT));
        blocks.add(b(1, 0, 0, Material.STONE));
        blocks.add(d(1, 1, 0, "minecraft:dispenser[facing=west]"));

        List<String> instr = List.of(
                "1. Posez un bloc de slime sur un support, puis 1 à 2 TNT empilées dessus.",
                "2. Placez un distributeur à côté, orienté vers la pile de TNT, chargé de TNT.",
                "3. Le distributeur éjecte une TNT amorcée qui percute la pile : l'explosion du bas",
                "   rebondit sur le slime et propulse la TNT du dessus très haut et très loin.",
                "4. Plus vous empilez de TNT au-dessus du slime, plus la portée augmente (attention aux dégâts !).",
                "C'est le principe du 'canon à explosion' : on utilise le souffle d'une explosion pour",
                "propulser une autre TNT amorcée, redirigé par le rebond du slime block."
        );
        return new CannonSchematic("slime_bounce_cannon", "Canon à rebond (slime)", Material.SLIME_BLOCK, 3, blocks, instr);
    }

    // ── Schéma 4 : Canon à eau (tube de contention) ──────────────────────

    private CannonSchematic waterCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        for (int z = 0; z <= 4; z++) {
            blocks.add(b(-1, 0, z, Material.GLASS));
            blocks.add(b(1, 0, z, Material.GLASS));
            blocks.add(b(0, 1, z, Material.GLASS));
            blocks.add(b(-1, 1, z, Material.GLASS));
            blocks.add(b(1, 1, z, Material.GLASS));
        }
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(d(0, 1, 0, "minecraft:dispenser[facing=south]"));
        blocks.add(d(0, 0, 1, "minecraft:water[level=0]"));
        blocks.add(b(0, 0, 4, Material.STONE));

        List<String> instr = List.of(
                "1. Construisez un tube (verre) de 4-5 blocs de long, ouvert à un bout.",
                "2. Placez un distributeur rempli de TNT à l'entrée du tube, orienté vers l'intérieur.",
                "3. Placez une source d'eau juste devant le distributeur, à l'intérieur du tube.",
                "4. Le distributeur éjecte la TNT amorcée dans l'eau : le courant la pousse le long du",
                "   tube à grande vitesse jusqu'à la sortie, où elle explose avec un impact précis.",
                "C'est le 'canon à eau' : très précis à courte/moyenne portée, très utilisé en TNT Wars",
                "pour viser directement les constructions adverses sans perdre de TNT sur son propre camp."
        );
        return new CannonSchematic("water_cannon", "Canon à eau (tube)", Material.WATER_BUCKET, 4, blocks, instr);
    }

    // ── Schéma 5 : Canon semi-automatique (comparateur) ──────────────────

    private CannonSchematic semiAutoCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(d(0, 1, 0, "minecraft:dispenser[facing=north]"));
        blocks.add(b(0, 0, 1, Material.HOPPER));
        blocks.add(b(0, -1, 1, Material.CHEST));
        blocks.add(b(0, 0, 2, Material.STONE));
        blocks.add(d(0, 1, 2, "minecraft:comparator[facing=south,mode=compare,powered=false]"));
        blocks.add(b(0, 0, 3, Material.STONE));
        blocks.add(d(0, 1, 3, "minecraft:repeater[facing=south,delay=2,locked=false,powered=false]"));
        blocks.add(b(0, 0, 4, Material.STONE));
        blocks.add(d(0, 1, 4, "minecraft:stone_button[face=floor,facing=north,powered=false]"));

        List<String> instr = List.of(
                "1. Reliez un distributeur à un coffre via une trémie (hopper) : le distributeur se",
                "   recharge automatiquement en TNT tant que le coffre en contient.",
                "2. Un comparateur lit le niveau de remplissage du coffre et confirme qu'il reste des",
                "   munitions avant d'autoriser le tir (évite de déclencher le distributeur à vide).",
                "3. Un répéteur cadence les tirs successifs si le bouton reste actionné (blocs à répétition).",
                "4. Idéal pour un tir soutenu sans devoir recharger le distributeur à la main à chaque coup.",
                "Astuce : dans le coffre infini de l'arène, prenez plusieurs piles de TNT pour recharger",
                "le coffre d'alimentation de ce canon régulièrement."
        );
        return new CannonSchematic("semi_auto_cannon", "Canon semi-automatique", Material.COMPARATOR, 5, blocks, instr);
    }

    // ── Schéma 6 : Canon double synchronisé ──────────────────────────────

    private CannonSchematic doubleCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(b(-2, 0, 0, Material.STONE));
        blocks.add(d(-2, 1, 0, "minecraft:dispenser[facing=north]"));
        blocks.add(b(2, 0, 0, Material.STONE));
        blocks.add(d(2, 1, 0, "minecraft:dispenser[facing=north]"));
        blocks.add(b(-1, 0, 0, Material.STONE));
        blocks.add(b(-1, 1, 0, Material.REDSTONE_WIRE));
        blocks.add(b(0, 0, 0, Material.STONE));
        blocks.add(b(0, 1, 0, Material.REDSTONE_WIRE));
        blocks.add(b(1, 0, 0, Material.STONE));
        blocks.add(b(1, 1, 0, Material.REDSTONE_WIRE));
        blocks.add(b(0, 0, 1, Material.STONE));
        blocks.add(d(0, 1, 1, "minecraft:stone_button[face=floor,facing=north,powered=false]"));

        List<String> instr = List.of(
                "1. Construisez deux lance-TNT basiques (schéma 1) côte à côte, espacés de 2-4 blocs.",
                "2. Reliez les deux distributeurs par une seule ligne de poudre de redstone continue.",
                "3. Un seul bouton au centre déclenche les deux canons en même temps.",
                "4. Utile pour un tir de couverture double, ou pour viser deux angles différents de la",
                "   base adverse en un seul appui.",
                "Vous pouvez combiner ce schéma avec le canon à eau ou à retard pour créer une",
                "batterie de canons synchronisés plus avancée."
        );
        return new CannonSchematic("double_cannon", "Canon double synchronisé", Material.TNT, 6, blocks, instr);
    }

    // ── Schéma 7 : Canon à TNT puissant (double batterie convergente) ────
    // Importé depuis un schéma WorldEdit (.schem) fourni : 7 distributeurs, deux
    // batteries de 3 qui convergent dans un canal d'eau bordé d'échelles, redirigées
    // par un 7e distributeur perpendiculaire pour un tir plus puissant et plus loin.

    private CannonSchematic powerfulWaterCannon() {
        List<RelativeBlock> blocks = new ArrayList<>();
        blocks.add(d(-3, 0, 0, "minecraft:lever[face=floor,facing=south,powered=false]"));
        blocks.add(d(-3, 0, 1, "minecraft:redstone_wire[east=none,north=side,power=0,south=up,west=none]"));
        blocks.add(d(2, 0, 1, "minecraft:lever[face=floor,facing=south,powered=false]"));
        blocks.add(b(-3, 0, 2, Material.STONE));
        blocks.add(d(2, 0, 2, "minecraft:redstone_wire[east=none,north=side,power=15,south=up,west=none]"));
        blocks.add(b(-5, 0, 3, Material.STONE));
        blocks.add(b(-4, 0, 3, Material.STONE));
        blocks.add(b(-3, 0, 3, Material.STONE));
        blocks.add(b(-2, 0, 3, Material.STONE));
        blocks.add(b(-1, 0, 3, Material.STONE));
        blocks.add(b(0, 0, 3, Material.STONE));
        blocks.add(d(1, 0, 3, "minecraft:dispenser[facing=south,triggered=false]"));
        blocks.add(d(2, 0, 3, "minecraft:dispenser[facing=south,triggered=false]"));
        blocks.add(d(3, 0, 3, "minecraft:dispenser[facing=south,triggered=false]"));
        blocks.add(d(4, 0, 3, "minecraft:redstone_wire[east=none,north=none,power=12,south=up,west=up]"));
        blocks.add(d(-4, 0, 4, "minecraft:ladder[facing=south,waterlogged=false]"));
        blocks.add(d(-3, 0, 4, "minecraft:ladder[facing=south,waterlogged=false]"));
        blocks.add(d(-2, 0, 4, "minecraft:ladder[facing=south,waterlogged=false]"));
        blocks.add(d(-1, 0, 4, "minecraft:ladder[facing=south,waterlogged=false]"));
        blocks.add(d(0, 0, 4, "minecraft:ladder[facing=south,waterlogged=false]"));
        blocks.add(d(1, 0, 4, "minecraft:water[level=2]"));
        blocks.add(d(2, 0, 4, "minecraft:water[level=1]"));
        blocks.add(d(3, 0, 4, "minecraft:water[level=0]"));
        blocks.add(d(4, 0, 4, "minecraft:dispenser[facing=west,triggered=false]"));
        blocks.add(b(-5, 0, 5, Material.STONE));
        blocks.add(b(-4, 0, 5, Material.STONE));
        blocks.add(b(-3, 0, 5, Material.STONE));
        blocks.add(b(-2, 0, 5, Material.STONE));
        blocks.add(b(-1, 0, 5, Material.STONE));
        blocks.add(b(0, 0, 5, Material.STONE));
        blocks.add(d(1, 0, 5, "minecraft:dispenser[facing=north,triggered=false]"));
        blocks.add(d(2, 0, 5, "minecraft:dispenser[facing=north,triggered=false]"));
        blocks.add(d(3, 0, 5, "minecraft:dispenser[facing=north,triggered=false]"));
        blocks.add(d(4, 0, 5, "minecraft:redstone_wire[east=none,north=up,power=10,south=none,west=up]"));

        List<String> instr = List.of(
                "1. Ce canon avancé pose une dalle de 10x6 directement au sol : chaque bloc affiché",
                "   remplace le bloc du sol à cet endroit précis (ne pas surélever).",
                "2. Deux batteries de 3 distributeurs se font face (une orientée sud, l'autre nord),",
                "   chacune remplie de TNT au maximum, et tirent l'une vers l'autre.",
                "3. Entre les deux, un canal bordé d'échelles contient de l'eau à 3 niveaux : les TNT",
                "   amorcées tombent dedans et sont poussées par le courant vers un 7e distributeur,",
                "   placé perpendiculairement (orienté ouest), qui les redirige avec un élan supplémentaire.",
                "4. Deux leviers permettent de déclencher indépendamment l'accumulation (premier levier)",
                "   puis le tir final (second levier), pour un tir séquentiel plus puissant et plus précis",
                "   qu'un simple canon à eau.",
                "5. Rechargez régulièrement les 7 distributeurs en TNT depuis le coffre infini de l'arène."
        );
        return new CannonSchematic("powerful_water_cannon", "Canon à TNT puissant", Material.REDSTONE_BLOCK, 7, blocks, instr);
    }

    // ── Item représentant le schéma (livre de plans) ─────────────────────

    public ItemStack createBookItem(CannonSchematic schema) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(schema.getDisplayName());
        meta.setAuthor("TntWars");
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Plan : " + ChatColor.RESET + ChatColor.WHITE + schema.getDisplayName());
        StringBuilder page = new StringBuilder();
        page.append(ChatColor.BOLD).append(schema.getDisplayName()).append("\n\n");
        for (String line : schema.getInstructions()) {
            page.append(ChatColor.BLACK).append(line).append("\n");
        }
        meta.addPage(page.toString());
        meta.setLore(List.of(ChatColor.GRAY + "Faites /tnt schema pour prévisualiser",
                ChatColor.GRAY + "ce canon en blocs fantômes en jeu."));
        book.setItemMeta(meta);
        return book;
    }
}
