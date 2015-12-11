/**
 * Created by florian on 29.11.15.
 */
// Row Class
function Job(id, initialJob){
    var self = this;
    self.id = id;
    self.job = ko.observable(initialJob);
}

function Language(id, initialLanguage){
    var self = this;
    self.id = id;
    self.language = ko.observable(initialLanguage);
}


function JobHistoryViewModel(){
    var self = this;

    self.jobs = ko.observableArray([]);

    self.languages = ko.observableArray([]);
    self.jobTypes = ko.observableArray([]);

    $.getJSON("/json/jobType", function(result){
        self.jobTypes = result;
    });

    $.getJSON("/json/jobLanguage", function(result){
        for(var i = 0; i < result.length; i++){
            console.log(result[i].name);
            self.languages.push(new Language(i + 1, result[i].name));
        }
    });

    $.getJSON("/json/jobHistory", function(result){
        for(var i = 0; i < result.length; i++){
            self.jobs.push(new Job(i + 1, result[i]));
        }
    });

    function initModal(job) {
        console.log("init modal!");
    }

    self.showModal = function(job){
        console.log("öffne!");
        console.log(job);

        resetFilters();
        reset();
        initModal(job);

        $("#modal-sample-1").modal('show');
    }

}

ko.applyBindings(new JobHistoryViewModel());