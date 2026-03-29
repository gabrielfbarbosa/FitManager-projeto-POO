import application.FitManager;
import ui.menu.main.MainMenu;
import ui.screen.UserScreen;

/**
 * Ponto de entrada do sistema FitManager.
 *
 * Responsável por instanciar os componentes principais e iniciar
 * o loop do menu principal.
 */
public class FitManagerApp {
    public static void main(String[] args) {
        // Instancia os componentes principais
        UserScreen ui = new UserScreen();
        FitManager fitManager = new FitManager();
        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}
