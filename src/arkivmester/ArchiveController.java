package arkivmester;

public class ArchiveController implements ViewObserver {
    UserView userView;
    ArchiveModel archiveModel;
    RapportModel rapportModel;
    TestModel testModel;
    ThirdPartiesModel thirdPartiesModel;

    ArchiveController() {
        userView = new UserView();
        archiveModel = new ArchiveModel();
        rapportModel = new RapportModel();
        testModel = new TestModel();
        thirdPartiesModel = new ThirdPartiesModel();
    }

    public void start() {
        userView.createAndShowGUI();
        userView.addObserver(this);
    }

    @Override
    public void testStarted() {
        System.out.println("Someone clicked on Start test."); //#NOSONAR
    }
}
