package controllers;


import modules.authentication.AuthResponse;
import modules.authentication.GoogleAuthentication;
import play.*;
//import play.api.mvc.*;
import play.mvc.*;

import views.html.*;

import java.io.IOException;

public class Application extends Controller {

    public Result dummy() {
        return ok(dummy.render());
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result upload() {
        return ok(upload.render("Your new application is ready."));
    }

    public Result buttonup() {
            return ok(buttonup.render("Your new application is ready."));
        }

    public Result oauth(String error, String code) {
        Logger.debug(request().body().asText());
        if(error != null || code == null){
            Logger.debug(request().body().asText());
            return unauthorized();
        }

        try {
            AuthResponse authResponse = GoogleAuthentication.getInstance().exchangeToken(code);
            if(authResponse.isValid()){
                return ok("Your Email is: " + authResponse.getEmail());
            } else {
                return badRequest("invalidToken");
            }
        } catch (IOException e) {
            Logger.error("oauth IO-Error", e);
            return internalServerError("error");
        }
        //return ok(code);
    }

    public Result login() {
        try {
            return redirect(GoogleAuthentication.getInstance().setUpGoogleClient());
        } catch (IOException e) {
            Logger.error("Login IO-Error", e);
            return internalServerError("error");
        }
    }
}
