package modules.database.factory;

import modules.cms.CMSController;
import modules.cms.FolderController;
import modules.cms.SessionHolder;
import modules.cms.data.FileType;
import modules.database.JobController;
import modules.database.entities.Image;
import modules.database.entities.Job;
import modules.database.entities.LayoutConfig;
import modules.database.entities.User;
import modules.upload.FileContainer;
import org.joda.time.DateTime;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 01.12.15.
 */
public class SimpleJobFactory {

    private Job job = new Job();

    private Image image;

    private LayoutConfig layoutConfig;

    /**
     * speichert einen job in der Datenbank mit den vorher über die setter gesetzten werte und gibt diesen job zurück
     * @return erstellter und in der datenbank abgelegter job
     */
    public Job persist(){
        if(image != null && layoutConfig != null && job.getUser() != null){
            new JobController().persistJob(job, image, layoutConfig, job.getUser());
        }else{
            Logger.error("no image set!");
        }

        return job;
    }

    public SimpleJobFactory setImage(Image image){
        this.image = image;
        return this;
    }

    public SimpleJobFactory setUser(User user){
        job.setUser(user);

        return this;
    }

    public SimpleJobFactory setName(String name){
        job.setName(name);

        return this;
    }

    public SimpleJobFactory setStartTime(DateTime startTime){
        job.setStartTime(startTime);

        return this;
    }

    public SimpleJobFactory setCategory(String category){
        job.setCategory(category);

        return this;
    }

    public SimpleJobFactory setLayoutParameters(LayoutConfig layoutConfig){

        this.layoutConfig = layoutConfig;

        return this;
    }

    /**
     * ermöglicht das einfügen mehrerer jobs mit einem aufruf
     * dafür muss eine liste an dateien mit der dazugehörigen uploadid und dem benutzer, dem die jobs hinzugefügt werden sollen, übergeben werden
     * aus den dateien und der uploadid werden einzelne jobs erstellt und die bilder dem dms hinzugefügt
     * zuletzt wird der erstellte job in der datenbank gespeichert und nachdem alle jobs erstellt wurden die liste dieser zurückgegeben
     * @param files zu analysierende bilder
     * @param inUploadId uploadid der analysierten bilder
     * @param session benutzer-email zu selektion des benutzers
     * @return liste der erstellen jobs
     * @throws IOException
     */
    public List<Job> createJobsJsonBulk(List<FileContainer> files, String inUploadId, String session) throws IOException {
        ArrayList<Job> rc = new ArrayList<>();
        CMSController cms = SessionHolder.getInstance().getController("ocr", "ocr");
        JobController jobController = new JobController();
        FolderController folderController = new FolderController(cms);

        for(int i = 0; files != null && i < files.size(); i++){
            Logger.debug("create job: " + files.get(i));
            Job job = new Job();

            String name = files.get(i).getFileName();

            String id = cms.createDocument(folderController.getUserWorkspaceFolder(), files.get(i).getFile(), FileType.FILE.getType()).getId();

            Image image = new SimpleImageFactory()
                    .setCreateDate(new DateTime())
                    .setFocalLength(2.0)
                    .setSource(id)
                    .build();

            Logger.info("FileName: " + name);
            job.setName(name);
            job.setStartTime(new DateTime());
            Logger.info("setting upload id: " + inUploadId);
            job.setUploadId(inUploadId);

            jobController.persistJob(job, image, session);
            rc.add(job);
        }
        return rc;
    }
}
