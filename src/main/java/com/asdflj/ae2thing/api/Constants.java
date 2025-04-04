package com.asdflj.ae2thing.api;

public interface Constants {

    String DISKUUID = "diskuuid";
    String DISKDATA = "diskdata";
    String FLUID_DISKLIST = "fluid_disklist";
    String DISKLIST = "disklist";
    String LINKED = "linked";
    String DISPLAY_ONLY = "DisplayOnly";
    String TC_CRAFTING = "tc_crafting";
    String IS_EMPTY = "is_empty";
    String COLOR = "color";
    String REMAINING_ITEM_COUNT = "remaining_item_count";
    String CPU_LIST = "cpu_list";
    String CPU_ELAPSED_TIME = "elapsed_time";
    String NAME = "name";
    String IS_LINKED = "is_linked";
    String LINK = "link";
    String USED_CHANNELS = "used";
    String MAGNET_MODE_KEY = "MagnetMode";
    String SIDE = "side";
    String CONFIG_INV = "ConfigInv";
    String NEI_DEFAULT = "nei.default";
    String NEI_BR = "nei.br";
    String NEI_MOUSE_WHEEL = "nei.mouse_wheel";
    String SLOT = "slot";
    String SIZE = "size";
    String VIEW_CELL = "view";
    int OUTPUT_COLOR = 0x4566ccff;
    int ERROR_COLOR = 0x45DA4527;
    int INACTIVE_COLOR = 0x45FFEA05;
    int SELECTED_COLOR = 0x4545DA75;
    String PATTERN = "pattern";
    String REPLACE = "replace";
    String CRAFTING = "crafting";
    String OUTPUT = "output";
    String CRAFTING_EX = "crafting_ex";
    String OUTPUT_EX = "output_ex";
    String UPGRADES = "upgrades";
    String PLAYER = "player";
    String DEBUG_CARD_MODE = "debug_card_mode";
    String DEBUG_CARD_EXPORT_FILENAME = "history.json";
    int MODE_CRAFTING = 1;
    int MODE_PROCESSING = 0;
    String INFINITY_BOOSTER_CARD = "infinityBoosterCard";
    String INFINITY_ENERGY_CARD = "InfinityEnergyCard";

    enum MessageType {

        UPDATE_ITEMS(0),
        UPDATE_PLAYER_ITEM(1),
        UPDATE_PLAYER_CURRENT_ITEM(-1),
        UPDATE_PINNED_ITEMS(-2),
        ADD_PINNED_ITEM(-3),
        NOTIFICATION(-4);

        public final byte type;

        MessageType(int t) {
            this.type = (byte) t;
        }

        MessageType(byte t) {
            this.type = t;
        }
    }

    enum State {
        RUNNING,
        FINISHED,
        CANCELLED
    }

    enum MouseWheel {

        PREVIEW(-1),
        NEXT(1);

        public final int direction;

        MouseWheel(int direction) {
            this.direction = direction;
        }
    }
}
