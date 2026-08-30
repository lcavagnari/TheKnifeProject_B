package it.uninsubria.laboratoriob.client.gui;

import it.uninsubria.laboratoriob.client.data.ClientDataStore;

/**
 * Holds the single {@link ClientDataStore} the GUI runs against, set up once
 * at startup by {@link HelloApplication} and read by every controller.
 */
public final class GuiContext {

    private static ClientDataStore dataStore;

    private GuiContext() {
    }

    public static void init(ClientDataStore store) {
        dataStore = store;
    }

    public static ClientDataStore getDataStore() {
        return dataStore;
    }
}
