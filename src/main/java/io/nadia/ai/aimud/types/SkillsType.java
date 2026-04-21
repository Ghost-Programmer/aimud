package io.nadia.ai.aimud.types;

/**
 * Constant values container representing precisely recognized skill strings strings system-wide.
 * These are used directly to look up {@link io.nadia.ai.aimud.model.SkillRegistry} entries.
 */
public class SkillsType {
    public static final String CAST_MAGIC = "Cast Magic";
    public static final String SAY_PRAYER = "Say Prayer";
    public static final String SING_SONG = "Sing Song";
    public static final String LIGHT_ARMOR = "Light Armor";
    public static final String MEDIUM_ARMOR = "Medium Armor";
    public static final String HEAVY_ARMOR = "Heavy Armor";
    public static final String ONE_HANDED_WEAPON = "One Handed Weapon";
    public static final String TWO_HANDED_WEAPON = "Two Handed Weapon";
    public static final String BASH = "Bash";
    public static final String BANDAGE = "Bandage";
    public static final String DUAL_WIELD = "Dual Wield";
    public static final String PARRY = "Parry";
    public static final String SHIELD_BLOCK = "Shield Block";
    public static final String HIDE = "Hide";
    public static final String BACKSTAB = "Backstab";
    public static final String PICKPOCKET = "Pickpocket";
    public static final String DOUBLE_ATTACK = "Double Attack";
    public static final String TRIPLE_ATTACK = "Triple Attack";
    public static final String DISARM = "Disarm";
    public static final String WARCRY = "Warcry";
    public static final String INTIMIDATE = "Intimidate";
    public static final String PROVOKE = "Provoke";
    public static final String TAUNT = "Taunt";
    public static final String VANISH = "Vanish";
    public static final String SNEAK = "Sneak";

    /**
     * Private constructor to prevent unexpected instantiation of this purely static constants class.
     */
    private SkillsType() {
        // Private constructor to prevent instantiation
    }
}
