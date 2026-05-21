package io.nadia.ai.aimud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing the details and custom property field names for a specific ItemType.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeDetails {
    /**
     * The programmatic name of the item type (e.g., "WEAPON").
     */
    private String name;

    /**
     * The human-readable label of the item type (e.g., "Weapon").
     */
    private String label;

    /**
     * The name or description of the first custom property field, or null if unused.
     */
    private String property1Name;

    /**
     * The name or description of the second custom property field, or null if unused.
     */
    private String property2Name;

    /**
     * The name or description of the third custom property field, or null if unused.
     */
    private String property3Name;

    /**
     * The name or description of the fourth custom property field, or null if unused.
     */
    private String property4Name;
}
