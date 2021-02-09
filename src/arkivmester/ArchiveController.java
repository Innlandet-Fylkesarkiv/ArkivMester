package arkivmester;

public class ArchiveController implements ViewObserver {
    MainView mainView;
    TestView testView;
    ArchiveModel archiveModel;
    RapportModel rapportModel;
    TestModel testModel;
    ThirdPartiesModel thirdPartiesModel;

    ArchiveController() {
        mainView = new MainView();
        archiveModel = new ArchiveModel();
        rapportModel = new RapportModel();
        testModel = new TestModel();
        thirdPartiesModel = new ThirdPartiesModel();
    }

    public void start() {
        mainView.createAndShowGUI();
        mainView.addObserver(this);
    }

    @Override
    public void testStarted() {
        System.out.println("Someone clicked on Start test."); //#NOSONAR
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
    }
}
