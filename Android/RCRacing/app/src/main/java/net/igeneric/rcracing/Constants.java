package net.igeneric.rcracing;

import java.util.UUID;

interface Constants {

    int ACTION_REQUEST_PERMISSION = 0;
    int ACTION_REQUEST_ENABLE = 1;
    int MY_DATA_CHECK_CODE = 2;
    int ACTION_REQUEST_SETUP = 3;
    int ACTION_REQUEST_DIALOG = 4;
    int ACTION_REQUEST_WELCOME = 5;

    String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    String EXTRA_DATA = "EXTRA_DATA";
    UUID UUID_BLE_HM10_RX_TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    UUID UUID_BLE_HM10_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

}
