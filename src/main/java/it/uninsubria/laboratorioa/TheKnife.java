package it.uninsubria.laboratorioa;

import it.uninsubria.laboratorioa.ui.GuestMenus;
import it.uninsubria.laboratorioa.ui.Menus;
import it.uninsubria.laboratorioa.utils.Loader;

public class TheKnife {
    public static void main(String[] args) {
        System.out.println("Loading The Knife...");

        Loader.loadFromFile();
        new GuestMenus().openMenu();
    }
}