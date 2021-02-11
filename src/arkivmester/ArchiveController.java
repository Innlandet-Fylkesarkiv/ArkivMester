package arkivmester;

public class ArchiveController {
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

    public void start() throws Exception {
        userView.setUpGUI();
        rapportModel.start();
    }
}
