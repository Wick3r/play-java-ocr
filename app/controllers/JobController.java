package controllers;

import modules.database.entities.Job;
import modules.database.entities.LayoutConfig;
import play.mvc.Controller;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Result;
import play.libs.Json;

import java.util.ArrayList;


/**
 * Created by florian on 29.11.15.
 */
public class JobController extends Controller {

    public Result getJobHistory(){
        ArrayList<Job> jobs = new ArrayList<>();
        String username = session().get("session");

        //TODO select from database

        Job job = new Job();
        job.setName("test.png");
        job.setId(1);
        jobs.add(job);

        job = new Job();
        job.setName("test2.png");
        job.setId(2);
        jobs.add(job);

        job = new Job();
        job.setName("test3.png");
        job.setId(3);
        jobs.add(job);

        return ok(Json.toJson(jobs));
    }

    public Result getJobTypes(){
        ArrayList<String> rc = new ArrayList<>();
        String username = session().get("session");

        //TODO select from database

        rc.add("Rechnung");
        rc.add("Dies");
        rc.add("und");
        rc.add("das");

        return ok(Json.toJson(rc));
    }

    public Result getLanguages(){
        ArrayList<String> rc = new ArrayList<>();
        String username = session().get("session");

        //TODO select from database

        rc.add("Detusch");
        rc.add("Anglisch");
        rc.add("Schwiezerdütsch");
        rc.add("Fränggisch");

        return ok(Json.toJson(rc));
    }

}
