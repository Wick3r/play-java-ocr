package modules.analyse;

import analyse.AnalyseType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import control.MainController;
import control.configuration.LayoutConfiguration;
import control.result.Result;
import modules.cms.CMSController;
import modules.cms.FolderController;
import modules.cms.SessionHolder;
import modules.cms.data.FileType;
import modules.database.entities.Job;
import org.apache.chemistry.opencmis.client.api.Document;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.Json;
import preprocessing.PreProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by FRudi on 17.12.2015.
 */
public class AnalyseWorker implements Runnable {

    private MainController controller;
    private Job job;
    private List<PreProcessor> preProcessors;
    private List<AnalyseType> analyser;
    private BufferedImage image;
    private LayoutConfiguration configuration;

    public AnalyseWorker(){
        this.controller = new MainController();
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();

        Logger.info("start analyse");
        Result result = controller.analyse(image, configuration);
        Logger.info("analyse complete");

        CMSController controller = SessionHolder.getInstance().getController("ocr", "ocr");
        FolderController folderController = new FolderController(controller);

        File file = new File("./job_" + job.getUser().geteMail() + "_" + new Date() + ".json");

        try {
            mapper.writeValue(file, result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Document doc = controller.createDocument(folderController.getUserWorkspaceFolder(), file, FileType.FILE.getType());
            JPA.withTransaction(() -> {
                job.setResultFile(doc.getId());
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            JPA.withTransaction(() -> job.setResultFile("error! " + Arrays.toString(e.getStackTrace())));
        }
        //file.delete();
        Logger.info("worker run complete!");
    }

    public void setImage(BufferedImage image){
        this.image = image;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setConfiguration(LayoutConfiguration configuration) {
        this.configuration = configuration;
    }
}
