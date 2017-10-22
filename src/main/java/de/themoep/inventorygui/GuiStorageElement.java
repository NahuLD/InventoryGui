package de.themoep.inventorygui;

/*
 * Copyright 2017 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 *
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This element is used to access an {@link Inventory}. The slots in the inventory are selected
 * by searching through the whole gui the element is in and getting the number of the spot
 * in the character group that this element is in. <br/>
 * E.g. if you have five characters called "s" in the gui setup and the second element is
 * accessed by the player then it will translate to the second slot in the inventory.
 */
public class GuiStorageElement extends GuiElement {
    private final Inventory storage;

    /**
     * An element used to access an {@link Inventory}.
     * @param slotChar  The character to replace in the gui setup string.
     * @param storage   The {@link Inventory} that this element is linked to.
     */
    public GuiStorageElement(char slotChar, Inventory storage) {
        this(slotChar, storage, -1);
    }

    /**
     * An element used to access a specific slot in an {@link Inventory}.
     * @param slotChar  The character to replace in the gui setup string.
     * @param storage   The {@link Inventory} that this element is linked to.
     * @param invSlot   The index of the slot to access in the {@link Inventory}.
     */
    public GuiStorageElement(char slotChar, Inventory storage, int invSlot) {
        super(slotChar, null);
        setAction(click -> {
            int index = invSlot != -1 ? invSlot : getSlotIndex(click.getSlot(), click.getGui().getPageNumber());
            if (index == -1 || index >= storage.getSize()) {
                return true;
            }
            ItemStack storageItem = storage.getItem(index);
            ItemStack slotItem = click.getEvent().getView().getTopInventory().getItem(click.getSlot());
            if (storageItem == null && slotItem != null || storageItem != null && !storageItem.equals(slotItem)) {
                click.getEvent().setCancelled(true);
                click.getGui().draw();
                return false;
            }
            ItemStack movedItem = null;
            switch (click.getEvent().getAction()) {
                case NOTHING:
                case CLONE_STACK:
                    return false;
                case MOVE_TO_OTHER_INVENTORY:
                    if (click.getEvent().getRawSlot() < click.getEvent().getView().getTopInventory().getSize()) {
                        movedItem = null;
                    } else {
                        movedItem = click.getEvent().getCurrentItem();
                    }
                    break;
                case HOTBAR_MOVE_AND_READD:
                case HOTBAR_SWAP:
                    int button = click.getEvent().getHotbarButton();
                    if (button < 0) {
                        return true;
                    }
                    ItemStack hotbarItem = click.getEvent().getView().getBottomInventory().getItem(button);
                    if (hotbarItem != null) {
                        movedItem = hotbarItem.clone();
                    }
                    break;
                case PICKUP_ONE:
                case DROP_ONE_SLOT:
                    movedItem = click.getEvent().getCurrentItem().clone();
                    movedItem.setAmount(movedItem.getAmount() - 1);
                    break;
                case DROP_ALL_SLOT:
                    movedItem = null;
                    break;
                case PICKUP_HALF:
                    movedItem = click.getEvent().getCurrentItem().clone();
                    movedItem.setAmount(movedItem.getAmount() / 2);
                    break;
                case PLACE_SOME:
                    if (click.getEvent().getCurrentItem() == null) {
                        movedItem = click.getEvent().getCursor();
                    } else {
                        movedItem = click.getEvent().getCurrentItem().clone();
                        if (movedItem.getAmount() + click.getEvent().getCursor().getAmount() < movedItem.getMaxStackSize()) {
                            movedItem.setAmount(movedItem.getAmount() + click.getEvent().getCursor().getAmount());
                        } else {
                            movedItem.setAmount(movedItem.getMaxStackSize());
                        }
                    }
                    break;
                case PLACE_ONE:
                    if (click.getEvent().getCurrentItem() == null) {
                        movedItem = click.getEvent().getCursor().clone();
                        movedItem.setAmount(1);
                    } else {
                        movedItem = click.getEvent().getCursor().clone();
                        movedItem.setAmount(click.getEvent().getCurrentItem().getAmount() + 1);
                    }
                    break;
                case PLACE_ALL:
                    movedItem = click.getEvent().getCursor().clone();
                    if (click.getEvent().getCurrentItem() != null && click.getEvent().getCurrentItem().getAmount() > 0) {
                        movedItem.setAmount(click.getEvent().getCurrentItem().getAmount() + movedItem.getAmount());
                    }
                    break;
                case PICKUP_ALL:
                case SWAP_WITH_CURSOR:
                    movedItem = click.getEvent().getCursor();
                    break;
                default:
                    click.getEvent().getWhoClicked().sendMessage(ChatColor.RED + "The action " + click.getEvent().getAction() + " is not supported! Sorry about that :(");
                    return true;
            }
            storage.setItem(index, movedItem);
            return false;
        });
        this.storage = storage;
    }

    @Override
    public ItemStack getItem(int slot) {
        int index = getSlotIndex(slot);
        if (index > -1 && index < storage.getSize()) {
            return storage.getItem(index);
        }
        return null;
    }

    /**
     * Get the {@link Inventory} that this element is linked to.
     * @return  The {@link Inventory} that this element is linked to.
     */
    public Inventory getStorage() {
        return storage;
    }
}
