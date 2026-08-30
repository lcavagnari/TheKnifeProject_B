package it.uninsubria.laboratoriob.client.gui;

import it.uninsubria.laboratoriob.client.data.ClientDataStore;

/**
 * Holds the single {@link ClientDataStore} the GUI runs against, set up once
 * at startup by {@link it.uninsubria.laboratoriob.client.TheKnifeClient} and read by every controller.
 */
public final class GuiContext {

    private static ClientDataStore dataStore;

    private GuiContext() {
    }

    /**
     * Inizializza il contesto globale della GUI con il data store specificato.
     *
     * @param store il data store client da rendere disponibile a tutti i controller
     */
    public static void init(ClientDataStore store) {
        dataStore = store;
    }

    /**
     * Restituisce il data store client inizializzato.
     *
     * @return il {@link ClientDataStore} corrente
     * @throws IllegalStateException se {@link #init(ClientDataStore)} non è stato ancora chiamato
     */
    public static ClientDataStore getDataStore() {
        return dataStore;
    }
}
