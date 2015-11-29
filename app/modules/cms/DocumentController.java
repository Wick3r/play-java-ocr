package modules.cms;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import play.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benedikt Linke on 23.11.15.
 */
public class DocumentController {

    private CmsController cmsController;

    public DocumentController(CmsController cmsController){
        this.cmsController = cmsController;
    }

    public Document getDocumentByPath(Folder parentFolder, String fileName){
        return (Document) cmsController.getSession().getObjectByPath(parentFolder.getPath()+ "/"+ fileName);
    }

    public Document getDocumentById(String documentId){
        return (Document) cmsController.getSession().getObject(documentId);
    }

    public Document createDocument(Folder parentFolder, File file, String fileType) throws FileNotFoundException {

        String fileName = file.getName();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, fileName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

        ContentStream contentStream = cmsController.getSession().getObjectFactory().createContentStream(
                fileName,
                file.length(),
                fileType,
                new FileInputStream(file)
        );

        Document document = null;
        try {
            document = parentFolder.createDocument(properties, contentStream, null);
            Logger.info("Created new document: " + document.getId() + "   " + document.getPaths());
        } catch (CmisContentAlreadyExistsException ccaee) {
            document = (Document) cmsController.getSession().getObjectByPath(parentFolder.getPath() + "/" + fileName);
            Logger.info("Document already exists: " + fileName);
        }
        document.getPaths();
        return document;
    }

    public boolean deleteDocument(String object){
        try {
            Document document = (Document) cmsController.getSession().getObject(object);
            document.delete();
            Logger.info("Deleted document");
            return true;
        } catch (CmisObjectNotFoundException ccaee) {
            Logger.info("Dokument not found");
            return false;
        }
    }


}