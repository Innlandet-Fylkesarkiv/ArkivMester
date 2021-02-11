package arkivmester;

public class ArchiveController implements ViewObserver {
    MainView mainView;
    TestView testView;
    AdminInfoView adminInfoView;
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
        mainView.createFrame();
        mainView.createAndShowGUI();
        mainView.addObserver(this);
    }

    //When "Start testing" is clicked.
    @Override
    public void testStarted() {
        testView = new TestView();
        testView.addObserver(this);
        testView.createAndShowGUI(mainView.getContainer());
    }

    //When "Test nytt uttrekk" is clicked.
    @Override
    public void newTest() {
        testView.clearContainer();
        testView = null;
        mainView.createAndShowGUI();
    }

    @Override
    public void editAdminInfo() {
        adminInfoView = new AdminInfoView();
        adminInfoView.addObserver(this);
        adminInfoView.createAndShowGUI(mainView.getContainer());
    }
}
