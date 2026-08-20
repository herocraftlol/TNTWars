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
        register(powerfulWaterCannon());
        // D'autres schémas seront ajoutés ici au fur et à mesure (register(...)).
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
        return new CannonSchematic("powerful_water_cannon", "Canon à TNT puissant", Material.REDSTONE_BLOCK, 1, blocks, instr);
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
        meta.setLore(List.of(ChatColor.GRAY + "Faites /tnt schema puis cliquez dessus en jeu",
                ChatColor.GRAY + "pour le construire pour de vrai dans votre zone."));
        book.setItemMeta(meta);
        return book;
    }
}
