import application.DataMock;
import application.FitManager;
import ui.menu.main.MainMenu;
import ui.screen.UserInterface;

/**
 * Ponto de entrada do sistema FitManager.
 *
 * Responsável por instanciar os componentes principais e iniciar
 * o loop do menu principal.
 */
public class FitManagerApp {

    private static final boolean DEV_MODE = true;

    public static void main(String[] args) {
        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        FitManager fitManager = new FitManager();

        if (DEV_MODE) {
            DataMock.mock(fitManager);
        }

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}
